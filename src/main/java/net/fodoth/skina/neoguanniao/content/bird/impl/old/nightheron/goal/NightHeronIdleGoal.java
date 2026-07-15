package net.fodoth.skina.neoguanniao.content.bird.impl.old.nightheron.goal;

import net.fodoth.skina.neoguanniao.content.bird.feature.brain.BirdMotivation;
import net.fodoth.skina.neoguanniao.content.bird.impl.old.nightheron.NightHeronBehaviorState;
import net.fodoth.skina.neoguanniao.content.bird.impl.old.nightheron.NightHeronEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * 夜鹭空闲目标
 * 控制夜鹭在空闲时的行为，包括站立、环顾、散步、整理羽毛和伸脖子
 */
public class NightHeronIdleGoal extends Goal {
    private final NightHeronEntity nightHeron;
    private int remainingTicks;
    private IdleAction action;

    public NightHeronIdleGoal(NightHeronEntity nightHeron) {
        this.action = IdleAction.STAND;
        this.nightHeron = nightHeron;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return this.nightHeron.onGround() && !this.nightHeron.getBehaviorState().isAirborne()
                && !this.nightHeron.getBehaviorState().isEscape() && this.nightHeron.getTarget() == null
                && (this.nightHeron.hasBlockedFlightRecoveryActivity() || this.nightHeron.getRandom().nextInt(10) == 0);
    }

    @Override
    public boolean canContinueToUse() {
        return this.remainingTicks > 0 && this.nightHeron.onGround()
                && !this.nightHeron.getBehaviorState().isAirborne()
                && !this.nightHeron.getBehaviorState().isEscape();
    }

    @Override
    public void start() {
        this.action = this.nightHeron.consumeBlockedFlightRecoveryActivity()
                ? IdleAction.STROLL
                : this.chooseAction();
        this.remainingTicks = this.randomBetween(50, 140);

        if (this.action == IdleAction.STROLL) {
            this.remainingTicks = this.randomBetween(35, 80);
            this.nightHeron.setBehaviorState(NightHeronBehaviorState.MICRO_STROLL);
            this.moveToShortStrollTarget();
        } else if (this.action == IdleAction.PREEN) {
            this.nightHeron.triggerPreen();
            this.remainingTicks = 80;
        } else if (this.action == IdleAction.NECK_STRETCH) {
            this.nightHeron.triggerNeckStretch();
            this.remainingTicks = 72;
        } else if (this.action == IdleAction.LOOK) {
            this.nightHeron.setBehaviorState(NightHeronBehaviorState.LOOK_AROUND);
            this.lookAtRandomPoint();
        } else {
            this.nightHeron.setBehaviorState(NightHeronBehaviorState.REST_STAND);
            this.nightHeron.getNavigation().stop();
        }
    }

    @Override
    public void stop() {
        this.remainingTicks = 0;
        if (!this.nightHeron.getBehaviorState().isAirborne() && !this.nightHeron.getBehaviorState().isEscape()) {
            this.nightHeron.setBehaviorState(NightHeronBehaviorState.IDLE);
        }
    }

    @Override
    public void tick() {
        --this.remainingTicks;

        if (this.action == IdleAction.STROLL) {
            if (this.nightHeron.getNavigation().isDone() && this.remainingTicks > 20
                    && this.nightHeron.getRandom().nextInt(18) == 0) {
                this.moveToShortStrollTarget();
            }
        } else {
            if (this.action == IdleAction.LOOK && this.remainingTicks % 24 == 0) {
                this.lookAtRandomPoint();
            }
            this.nightHeron.getNavigation().stop();
        }
    }

    /**
     * 根据动机状态选择空闲行为
     */
    private IdleAction chooseAction() {
        BirdMotivation motivation = this.nightHeron.birdBrain().motivation();
        float hunger = motivation.hunger();
        float fear = motivation.fear();
        float fatigue = motivation.fatigue();
        float comfort = motivation.comfort();
        float alertness = motivation.alertness();

        int standWeight = 30;
        int lookWeight = 18;
        int strollWeight = 14;
        int preenWeight = 10;
        int neckStretchWeight = 10;

        if (this.nightHeron.isRoosting()) {
            standWeight += 28;
            lookWeight += 8;
            strollWeight -= 10;
            preenWeight += 8;
            neckStretchWeight += 4;
        }

        if (comfort > 0.65F) {
            standWeight += 12;
            preenWeight += 18;
        }

        if (alertness > 0.45F) {
            lookWeight += 24;
            neckStretchWeight += 22;
            preenWeight -= 6;
            strollWeight -= 4;
        }

        if (fatigue > 0.6F) {
            standWeight += 25;
            strollWeight -= 10;
            preenWeight += 5;
        }

        if (hunger > 0.55F && this.nightHeron.isNearWater(this.nightHeron.blockPosition(), 4)) {
            lookWeight += 18;
            neckStretchWeight += 10;
        }

        if (fear > 0.55F) {
            preenWeight -= 10;
            strollWeight -= 8;
            lookWeight += 12;
            neckStretchWeight += 12;
        }

        return this.weightedPick(
                positive(standWeight),
                positive(lookWeight),
                positive(strollWeight),
                positive(preenWeight),
                positive(neckStretchWeight)
        );
    }

    /**
     * 加权随机选择
     */
    private IdleAction weightedPick(int standWeight, int lookWeight, int strollWeight, int preenWeight, int neckStretchWeight) {
        int total = standWeight + lookWeight + strollWeight + preenWeight + neckStretchWeight;
        if (total <= 0) {
            return IdleAction.STAND;
        }

        int roll = this.nightHeron.getRandom().nextInt(total);
        roll -= standWeight;
        if (roll < 0) return IdleAction.STAND;
        roll -= lookWeight;
        if (roll < 0) return IdleAction.LOOK;
        roll -= strollWeight;
        if (roll < 0) return IdleAction.STROLL;
        roll -= preenWeight;
        return roll < 0 ? IdleAction.PREEN : IdleAction.NECK_STRETCH;
    }

    /**
     * 确保值为正数
     */
    private static int positive(int value) {
        return Math.max(0, value);
    }

    /**
     * 移动到短途散步目标
     */
    private void moveToShortStrollTarget() {
        Vec3 target = LandRandomPos.getPos(this.nightHeron, 5, 3);
        if (target != null) {
            this.nightHeron.getNavigation().moveTo(target.x, target.y, target.z, 0.16);
        }
    }

    /**
     * 看向随机点
     */
    private void lookAtRandomPoint() {
        float angle = this.nightHeron.getRandom().nextFloat() * (float) (2.0 * Math.PI);
        double distance = 3.0 + this.nightHeron.getRandom().nextDouble() * 4.0;
        this.nightHeron.getLookControl().setLookAt(
                this.nightHeron.getX() + Mth.cos(angle) * distance,
                this.nightHeron.getEyeY(),
                this.nightHeron.getZ() + Mth.sin(angle) * distance
        );
    }

    /**
     * 生成随机值
     */
    private int randomBetween(int min, int max) {
        return min + this.nightHeron.getRandom().nextInt(max - min + 1);
    }

    /**
     * 空闲行为类型枚举
     */
    private enum IdleAction {
        /** 站立 */
        STAND,
        /** 环顾四周 */
        LOOK,
        /** 短途散步 */
        STROLL,
        /** 整理羽毛 */
        PREEN,
        /** 伸脖子 */
        NECK_STRETCH
    }
}