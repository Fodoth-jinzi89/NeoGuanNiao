package net.fodoth.skina.neoguanniao.content.bird.columbid.goal;

import net.fodoth.skina.neoguanniao.content.bird.columbid.AbstractColumbidEntity;
import net.fodoth.skina.neoguanniao.content.bird.columbid.ColumbidBehaviorState;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class ColumbidRoostGoal extends Goal {
    private final AbstractColumbidEntity columbid;
    private BlockPos roostPos;
    private int roostTicks;
    private int repathTicks;

    public ColumbidRoostGoal(AbstractColumbidEntity columbid) {
        this.columbid = columbid;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.columbid.isRoostTime() && !this.columbid.isControlledFlightActive()
                && this.columbid.getRandom().nextInt(80) == 0) {
            this.roostPos = this.findRoost();
            return this.roostPos != null;
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return this.roostTicks > 0 && this.roostPos != null && this.columbid.isRoostTime()
                && !this.columbid.isInWater();
    }

    @Override
    public void start() {
        this.roostTicks = 260 + this.columbid.getRandom().nextInt(360);
        this.repathTicks = 0;
        this.columbid.setBehaviorState(ColumbidBehaviorState.ROOSTING);
    }

    @Override
    public void tick() {
        --this.roostTicks;
        double distanceSqr = this.columbid.distanceToSqr(Vec3.atCenterOf(this.roostPos));

        if (distanceSqr > 2.5) {
            Vec3 target = Vec3.atBottomCenterOf(this.roostPos);
            if (distanceSqr > 64.0 && this.columbid.onGround() && this.columbid.flightCooldown <= 0) {
                this.columbid.startControlledFlight(target, 95, 0.38, false, true);
            } else {
                if (--this.repathTicks <= 0 || this.columbid.getNavigation().isDone()) {
                    this.repathTicks = 18 + this.columbid.getRandom().nextInt(16);
                    this.columbid.getNavigation().moveTo(target.x, target.y, target.z, 0.9);
                }
            }
        } else {
            this.columbid.getNavigation().stop();
            this.columbid.setBehaviorState(ColumbidBehaviorState.ROOSTING);
            if (this.roostTicks % 80 == 0) {
                this.columbid.birdBrain().onRest(0.03F);
            }
        }
    }

    @Override
    public void stop() {
        this.roostPos = null;
        this.repathTicks = 0;
        if (this.columbid.getBehaviorState() == ColumbidBehaviorState.ROOSTING) {
            this.columbid.setBehaviorState(ColumbidBehaviorState.IDLE);
        }
    }

    private BlockPos findRoost() {
        BlockPos origin = this.columbid.homePos != null && this.columbid.isTame()
                ? this.columbid.homePos
                : this.columbid.blockPosition();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int attempt = 0; attempt < 48; ++attempt) {
            int x = origin.getX() + this.columbid.getRandom().nextInt(19) - 9;
            int z = origin.getZ() + this.columbid.getRandom().nextInt(19) - 9;
            int y = origin.getY() + this.columbid.getRandom().nextInt(9) + 1;
            mutable.set(x, y, z);
            if (this.columbid.isSafeDryLanding(mutable) && this.isRoostBlock(this.columbid.level().getBlockState(mutable.below()))) {
                return mutable.immutable();
            }
        }
        return null;
    }

    private boolean isRoostBlock(BlockState state) {
        return state.is(BlockTags.WALLS) || state.is(BlockTags.LEAVES)
                || state.getBlock() instanceof FenceBlock || state.getBlock() instanceof FenceGateBlock
                || state.is(net.minecraft.world.level.block.Blocks.FARMLAND);
    }
}