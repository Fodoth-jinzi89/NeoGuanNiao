package net.fodoth.skina.neoguanniao.content.bird.impl.sparrow.goal;

import net.fodoth.skina.neoguanniao.content.bird.impl.sparrow.SparrowBehaviorState;
import net.fodoth.skina.neoguanniao.content.bird.impl.sparrow.SparrowEntity;
import net.fodoth.skina.neoguanniao.content.feed.BreadcrumbPileBlock;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class SparrowEatBreadcrumbGoal extends Goal {
    private final SparrowEntity sparrow;
    private BlockPos pilePos;
    private Vec3 standPos;
    private int nextPeckTicks;

    public SparrowEatBreadcrumbGoal(SparrowEntity sparrow) {
        this.sparrow = sparrow;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if ((this.sparrow.isHungry() || this.sparrow.hasBreadcrumbInterest())
                && !this.sparrow.shouldAvoidBreadcrumbs() && !this.sparrow.isInWater()) {
            this.pilePos = null;

            if (this.sparrow.hasBreadcrumbInterest() && this.sparrow.noticedBreadcrumbPos != null
                    && this.sparrow.level().getBlockState(this.sparrow.noticedBreadcrumbPos)
                    .is(NeoGuanNiaoBlocks.BREADCRUMBS.get())) {
                this.pilePos = this.sparrow.noticedBreadcrumbPos.immutable();
            }

            if (this.pilePos == null) {
                this.pilePos = this.sparrow.findNearbyBreadcrumbs(
                        this.sparrow.hasBreadcrumbInterest() ? 20 : 14, 3);
            }

            if (this.pilePos == null) {
                return false;
            }
            if (!this.sparrow.hasBreadcrumbInterest() && !this.sparrow.brainWantsForage()
                    && this.sparrow.getRandom().nextInt(3) != 0) {
                return false;
            }

            this.standPos = this.sparrow.breadcrumbStandPosition(this.pilePos);
            return true;
        }
        return false;
    }

    @Override
    public void start() {
        this.nextPeckTicks = this.sparrow.randomBetween(8, 14);
        this.sparrow.setBehaviorState(SparrowBehaviorState.FORAGING);
        this.moveTowardsPile();
    }

    @Override
    public void tick() {
        if (this.pilePos == null) return;

        if (!this.sparrow.isControlledFlightActive() && this.sparrow.getBehaviorState() != SparrowBehaviorState.PECKING) {
            this.sparrow.setBehaviorState(SparrowBehaviorState.FORAGING);
        }

        BlockState state = this.sparrow.level().getBlockState(this.pilePos);
        if (!state.is(NeoGuanNiaoBlocks.BREADCRUMBS.get()) || this.sparrow.shouldAvoidBreadcrumbs()) {
            this.stop();
            return;
        }

        this.sparrow.getLookControl().setLookAt(
                this.pilePos.getX() + 0.5, this.pilePos.getY() + 0.2, this.pilePos.getZ() + 0.5,
                20.0F, 20.0F);

        double distanceToPile = this.sparrow.position().distanceToSqr(Vec3.atCenterOf(this.pilePos));
        if (distanceToPile > 3.1) {
            this.moveTowardsPile();
        } else {
            this.sparrow.getNavigation().stop();
            if (!this.sparrow.isControlledFlightActive() && --this.nextPeckTicks <= 0) {
                if (this.sparrow.getRandom().nextFloat() < 0.28F) {
                    this.sparrow.triggerLookAround();
                    this.nextPeckTicks = this.sparrow.randomBetween(12, 20);
                } else {
                    this.sparrow.triggerPeck();
                    this.sparrow.gainBreadcrumbConfidence();
                    if (state.getBlock() instanceof BreadcrumbPileBlock breadcrumbPileBlock) {
                        if (breadcrumbPileBlock.consumeOneServing(this.sparrow.level(), this.pilePos, state)) {
                            this.sparrow.restoreBreadcrumbSatiation();
                            this.sparrow.birdBrain().onEat(0.35F);
                        }
                        if (!this.sparrow.level().getBlockState(this.pilePos)
                                .is(NeoGuanNiaoBlocks.BREADCRUMBS.get())) {
                            this.stop();
                            return;
                        }
                    }
                    this.nextPeckTicks = this.sparrow.randomBetween(10, 16);
                }
            }
        }
    }

    @Override
    public boolean canContinueToUse() {
        return this.pilePos != null && this.sparrow.level().getBlockState(this.pilePos)
                .is(NeoGuanNiaoBlocks.BREADCRUMBS.get()) && !this.sparrow.shouldAvoidBreadcrumbs();
    }

    @Override
    public void stop() {
        this.pilePos = null;
        this.standPos = null;
        this.nextPeckTicks = 0;
        if (this.sparrow.breadcrumbInterestTicks <= 0) {
            this.sparrow.noticedBreadcrumbPos = null;
        }
        this.sparrow.getNavigation().stop();
        this.sparrow.flightCooldown = Math.max(this.sparrow.flightCooldown, 20 + this.sparrow.getRandom().nextInt(41));
        if (this.sparrow.getBehaviorState() == SparrowBehaviorState.FORAGING) {
            this.sparrow.setBehaviorStateFor(SparrowBehaviorState.LOOK_AROUND, 24);
        }
    }

    private void moveTowardsPile() {
        if (this.standPos == null) {
            this.standPos = this.sparrow.breadcrumbStandPosition(this.pilePos);
        }
        if (this.sparrow.getBehaviorState() != SparrowBehaviorState.PECKING) {
            this.sparrow.setBehaviorState(SparrowBehaviorState.FORAGING);
        }
        this.sparrow.flightCooldown = Math.max(this.sparrow.flightCooldown, 80);
        this.sparrow.getNavigation().moveTo(this.standPos.x, this.standPos.y, this.standPos.z, 0.98);
    }
}