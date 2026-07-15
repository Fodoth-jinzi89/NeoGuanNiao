package net.fodoth.skina.neoguanniao.content.bird.impl.old.nightheron.goal;

import net.fodoth.skina.neoguanniao.content.bird.impl.old.nightheron.NightHeronBehaviorState;
import net.fodoth.skina.neoguanniao.content.bird.impl.old.nightheron.NightHeronEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

/**
 * 夜鹭觅食目标
 * 控制夜鹭在水边寻找和捕食鱼类
 */
public class NightHeronForagingGoal extends Goal {
    private final NightHeronEntity nightHeron;
    private int remainingTicks;
    private int repositionCooldown;

    public NightHeronForagingGoal(NightHeronEntity nightHeron) {
        this.nightHeron = nightHeron;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!this.nightHeron.birdBrain().wantsForage()) {
            return false;
        }
        int chance = this.nightHeron.level().isRaining() ? 7 : 12;
        return this.nightHeron.isActiveTime() && this.nightHeron.onGround()
                && this.nightHeron.isNearWater(this.nightHeron.blockPosition(), 4)
                && this.nightHeron.getTarget() == null
                && this.nightHeron.getRandom().nextInt(chance) == 0;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.nightHeron.birdBrain().motivation().fear() > 0.7F) {
            return false;
        }
        if (this.nightHeron.birdBrain().computeRiskScore() > 0.75F) {
            return false;
        }
        return this.remainingTicks > 0 && this.nightHeron.isActiveTime()
                && this.nightHeron.onGround() && !this.nightHeron.getBehaviorState().isEscape();
    }

    @Override
    public void start() {
        this.remainingTicks = 120 + this.nightHeron.getRandom().nextInt(141);
        this.repositionCooldown = 0;
        this.nightHeron.setBehaviorState(NightHeronBehaviorState.FORAGING);
    }

    @Override
    public void stop() {
        this.remainingTicks = 0;
        this.nightHeron.getNavigation().stop();
        if (!this.nightHeron.getBehaviorState().isEscape()) {
            this.nightHeron.setBehaviorState(NightHeronBehaviorState.IDLE);
        }
    }

    @Override
    public void tick() {
        --this.remainingTicks;
        if (this.repositionCooldown > 0) {
            --this.repositionCooldown;
        }

        Optional<LivingEntity> prey = this.findPrey();
        if (prey.isPresent()) {
            this.stalkPrey(prey.get());
        } else {
            this.nightHeron.setBehaviorState(NightHeronBehaviorState.WATER_EDGE_WAIT);
            if (this.repositionCooldown <= 0 && this.nightHeron.getRandom().nextInt(26) == 0) {
                this.repositionNearWater();
                this.repositionCooldown = 35 + this.nightHeron.getRandom().nextInt(35);
            } else {
                this.nightHeron.getNavigation().stop();
            }

            if (this.nightHeron.getRandom().nextInt(90) == 0) {
                this.nightHeron.triggerNeckStretch();
            }
        }
    }

    /**
     * 寻找附近的猎物
     */
    private Optional<LivingEntity> findPrey() {
        List<LivingEntity> nearby = this.nightHeron.level().getEntitiesOfClass(
                LivingEntity.class,
                this.nightHeron.getBoundingBox().inflate(7.0),
                entity -> entity.isAlive() && this.isPrey(entity)
        );
        return nearby.stream().min(Comparator.comparingDouble(this.nightHeron::distanceToSqr));
    }

    /**
     * 判断是否为猎物
     */
    private boolean isPrey(LivingEntity entity) {
        return entity instanceof AbstractFish
                || entity.getType() == EntityType.SALMON
                || entity.getType() == EntityType.PUFFERFISH;
    }

    /**
     * 跟踪猎物
     */
    private void stalkPrey(LivingEntity prey) {
        this.nightHeron.setBehaviorState(NightHeronBehaviorState.FORAGING);
        this.nightHeron.getLookControl().setLookAt(prey, 30.0F, 30.0F);
        double distanceSqr = this.nightHeron.distanceToSqr(prey);

        if (distanceSqr > 4.41) {
            Vec3 stalkingPosition = this.findStalkingPosition(prey);
            if (stalkingPosition != null) {
                this.nightHeron.getNavigation().moveTo(stalkingPosition.x, stalkingPosition.y, stalkingPosition.z, 0.14);
            } else {
                this.nightHeron.getNavigation().stop();
            }
        } else {
            this.nightHeron.getNavigation().stop();
            if (this.nightHeron.canStrikePrey()) {
                this.nightHeron.triggerNeckStretch();
                boolean damaged = this.nightHeron.doHurtTarget(prey);
                if (damaged) {
                    this.nightHeron.birdBrain().onEat(0.45F);
                }
                this.nightHeron.afterPreyStrike();
            }
        }
    }

    /**
     * 重新定位到水边
     */
    private void repositionNearWater() {
        Vec3 best = null;
        float bestScore = Float.NEGATIVE_INFINITY;

        for (int attempt = 0; attempt < 10; ++attempt) {
            Vec3 candidate = LandRandomPos.getPos(this.nightHeron, 6, 3);
            if (candidate != null) {
                float score = this.nightHeron.getWalkTargetValue(BlockPos.containing(candidate), this.nightHeron.level());
                if (score > bestScore) {
                    best = candidate;
                    bestScore = score;
                }
            }
        }

        if (best != null) {
            this.nightHeron.getNavigation().moveTo(best.x, best.y, best.z, 0.14);
        }
    }

    /**
     * 寻找跟踪猎物的位置
     */
    private Vec3 findStalkingPosition(LivingEntity prey) {
        Level level = this.nightHeron.level();
        BlockPos preyPos = prey.blockPosition();
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        BlockPos bestPos = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (int xOffset = -4; xOffset <= 4; ++xOffset) {
            for (int zOffset = -4; zOffset <= 4; ++zOffset) {
                if (xOffset * xOffset + zOffset * zOffset <= 16) {
                    for (int yOffset = -2; yOffset <= 2; ++yOffset) {
                        mutablePos.set(preyPos.getX() + xOffset, preyPos.getY() + yOffset, preyPos.getZ() + zOffset);
                        if (this.isSafeStalkingPosition(level, mutablePos)) {
                            double distanceToPrey = Vec3.atCenterOf(mutablePos).distanceToSqr(prey.position());
                            double distanceToHeron = Vec3.atCenterOf(mutablePos).distanceToSqr(this.nightHeron.position());
                            double score = -distanceToPrey * 1.35 - distanceToHeron * 0.18;

                            if (NightHeronEntity.isWaterEdge(level, mutablePos)) {
                                score += 10.0;
                            }
                            if (this.nightHeron.isNearWater(mutablePos, 3)) {
                                score += 4.0;
                            }

                            if (score > bestScore) {
                                bestScore = score;
                                bestPos = mutablePos.immutable();
                            }
                        }
                    }
                }
            }
        }

        return bestPos == null ? null : Vec3.atBottomCenterOf(bestPos);
    }

    /**
     * 检查是否为安全的跟踪位置
     */
    private boolean isSafeStalkingPosition(Level level, BlockPos pos) {
        if (!NightHeronEntity.canReadChunk(level, pos)) {
            return false;
        }

        BlockState below = level.getBlockState(pos.below());
        BlockState feet = level.getBlockState(pos);
        BlockState head = level.getBlockState(pos.above());

        if (!feet.getCollisionShape(level, pos).isEmpty() || !head.getCollisionShape(level, pos.above()).isEmpty()) {
            return false;
        }

        if (!level.getFluidState(pos).isEmpty()) {
            return false;
        }

        return below.isFaceSturdy(level, pos.below(), Direction.UP)
                || below.is(Blocks.WATER)
                || below.is(Blocks.SEAGRASS)
                || below.is(Blocks.GRASS_BLOCK)
                || below.is(Blocks.DIRT_PATH);
    }
}