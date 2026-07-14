package net.fodoth.skina.neoguanniao.content.bird.impl.nightheron.goal;

import net.fodoth.skina.neoguanniao.content.bird.impl.nightheron.NightHeronBehaviorState;
import net.fodoth.skina.neoguanniao.content.bird.impl.nightheron.NightHeronEntity;
import net.fodoth.skina.neoguanniao.content.bird.impl.nightheron.NightHeronFlightController;
import net.fodoth.skina.neoguanniao.content.bird.impl.nightheron.NightHeronLandingSelector;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * 夜鹭恐惧目标
 * 控制夜鹭对威胁的反应，包括警觉、逃跑和飞行
 */
public class NightHeronFrightGoal extends Goal {
    private final NightHeronEntity nightHeron;
    private Player threat;
    private Vec3 externalThreatPosition;
    private Vec3 escapeDirection;
    private Response response;
    private int remainingTicks;
    private BlockPos landingTarget;

    public NightHeronFrightGoal(NightHeronEntity nightHeron) {
        this.escapeDirection = Vec3.ZERO;
        this.response = Response.NONE;
        this.nightHeron = nightHeron;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        this.threat = this.findNearestRelevantPlayer();
        this.externalThreatPosition = this.nightHeron.getExternalFrightSource();
        this.response = this.chooseResponse();
        return this.response != Response.NONE;
    }

    @Override
    public boolean canContinueToUse() {
        this.threat = this.findNearestRelevantPlayer();
        if (this.nightHeron.hasExternalFright()) {
            this.externalThreatPosition = this.nightHeron.getExternalFrightSource();
        }

        if (this.response.isFlight()) {
            return this.nightHeron.isControlledFlightActive() && (this.remainingTicks > 0 || !this.nightHeron.onGround());
        }
        return this.remainingTicks > 0 && this.chooseResponse() != Response.NONE;
    }

    @Override
    public void start() {
        this.nightHeron.rememberFright(this.response == Response.LONG_FLIGHT);
        this.notifyNearbyNightHerons(this.response.isFlight());
        this.beginResponse(this.response);
    }

    @Override
    public void stop() {
        this.response = Response.NONE;
        this.remainingTicks = 0;
        this.threat = null;
        this.externalThreatPosition = null;
        this.landingTarget = null;
        this.escapeDirection = Vec3.ZERO;
        this.nightHeron.clearExternalFright();
        this.nightHeron.getNavigation().stop();

        if (this.nightHeron.getBehaviorState().isAirborne()) {
            this.nightHeron.settleInterruptedFlight(NightHeronBehaviorState.IDLE);
        } else if (this.nightHeron.getBehaviorState().isEscape()) {
            this.nightHeron.setBehaviorState(NightHeronBehaviorState.IDLE);
        }
    }

    @Override
    public void tick() {
        Response currentPressure = this.chooseResponse();
        if (currentPressure.ordinal() > this.response.ordinal()) {
            this.response = currentPressure;
            this.beginResponse(this.response);
        }

        --this.remainingTicks;

        if (this.response == Response.ALERT) {
            this.tickAlert();
        } else if (this.response == Response.WALK) {
            this.tickGroundEscape(NightHeronBehaviorState.WALK_ESCAPE, 0.36);
        } else if (this.response == Response.RUN) {
            this.tickGroundEscape(NightHeronBehaviorState.RUN_ESCAPE, 1.35);
        } else {
            this.tickFlightEscape(this.response == Response.LONG_FLIGHT);
        }
    }

    /**
     * 开始响应
     */
    private void beginResponse(Response response) {
        this.nightHeron.getNavigation().stop();
        this.nightHeron.birdBrain().onFrightened(this.frightAmount(response));
        this.escapeDirection = this.computeEscapeDirection();

        if (response == Response.ALERT) {
            this.remainingTicks = this.randomBetween(18, 36);
            this.nightHeron.setBehaviorState(NightHeronBehaviorState.ALERT_FREEZE);
        } else if (response == Response.WALK) {
            this.remainingTicks = this.randomBetween(30, 58);
            this.nightHeron.setBehaviorState(NightHeronBehaviorState.WALK_ESCAPE);
            this.moveAwayOnGround(0.36);
        } else if (response == Response.RUN) {
            this.remainingTicks = this.randomBetween(30, 58);
            this.nightHeron.setBehaviorState(NightHeronBehaviorState.RUN_ESCAPE);
            this.moveAwayOnGround(1.35);
        } else {
            this.remainingTicks = response == Response.LONG_FLIGHT ? this.randomBetween(150, 260) : this.randomBetween(40, 72);
            this.landingTarget = NightHeronLandingSelector.findEscapeLanding(
                    this.nightHeron,
                    this.getThreatPosition(),
                    response == Response.LONG_FLIGHT ? 36 : 14,
                    response == Response.LONG_FLIGHT ? 96 : 34
            );
            NightHeronFlightController.takeOff(this.nightHeron, this.escapeDirection, 0.48, 0.72);
        }
    }

    /**
     * 选择响应级别
     */
    private Response chooseResponse() {
        Player player = this.threat != null ? this.threat : this.findNearestRelevantPlayer();
        if (player == null && !this.nightHeron.hasExternalFright()) {
            return Response.NONE;
        }

        float risk = this.nightHeron.birdBrain().computeRiskScore();
        float fatigue = this.nightHeron.birdBrain().motivation().fatigue();
        int recentFrightCount = this.nightHeron.getRecentFrightCount();

        if (this.nightHeron.hasExternalFright()) {
            risk = Math.max(risk, this.nightHeron.hasSevereExternalFright() ? 0.88F : 0.62F);
        }

        if (recentFrightCount > 0) {
            risk += Math.min(0.12F, (float) recentFrightCount * 0.04F);
        }

        if (this.nightHeron.isInWater() && risk >= 0.45F) {
            risk = Math.max(risk, 0.62F);
        }

        if (this.nightHeron.getBehaviorState() == NightHeronBehaviorState.ROOSTING && risk >= 0.45F) {
            risk = Math.max(risk, 0.78F);
        }

        risk = Mth.clamp(risk, 0.0F, 1.0F);

        if (recentFrightCount >= 2 && risk >= 0.78F && fatigue <= 0.75F) {
            return Response.LONG_FLIGHT;
        }
        if (risk < 0.25F) {
            return Response.NONE;
        }
        if (risk < 0.45F) {
            return Response.ALERT;
        }
        if (risk < 0.62F) {
            return Response.WALK;
        }
        if (risk < 0.78F) {
            return Response.RUN;
        }
        if (risk < 0.88F) {
            return Response.LOW_FLIGHT;
        }
        return fatigue > 0.75F ? Response.LOW_FLIGHT : Response.LONG_FLIGHT;
    }

    /**
     * 查找最近的威胁玩家
     */
    private Player findNearestRelevantPlayer() {
        Player player = this.nightHeron.level().getNearestPlayer(this.nightHeron, 17.0);
        return player != null && !player.isSpectator() ? player : null;
    }

    /**
     * 警觉状态 tick
     */
    private void tickAlert() {
        Vec3 threatPosition = this.getThreatPosition();
        if (threatPosition != null) {
            this.nightHeron.getLookControl().setLookAt(threatPosition.x, threatPosition.y + 1.0, threatPosition.z);
        }
        this.nightHeron.getNavigation().stop();
    }

    /**
     * 地面逃跑 tick
     */
    private void tickGroundEscape(NightHeronBehaviorState state, double speed) {
        if (this.remainingTicks % 12 == 0 || this.nightHeron.getNavigation().isDone()) {
            this.escapeDirection = this.computeEscapeDirection();
            this.moveAwayOnGround(speed);
        }

        this.nightHeron.setBehaviorState(state);
        if (this.threat != null) {
            this.nightHeron.getLookControl().setLookAt(this.threat, 30.0F, 30.0F);
        }
    }

    /**
     * 飞行逃跑 tick
     */
    private void tickFlightEscape(boolean longEscape) {
        if (this.remainingTicks <= 0 && this.landingTarget == null) {
            this.landingTarget = NightHeronLandingSelector.findEscapeLanding(
                    this.nightHeron, this.getThreatPosition(), 4, 28
            );
            if (this.landingTarget == null) {
                NightHeronFlightController.tickOpenLanding(this.nightHeron, this.escapeDirection);
                return;
            }
        }

        if (NightHeronFlightController.shouldBeginLandingApproach(
                this.nightHeron, this.landingTarget, this.remainingTicks, longEscape ? 34.0 : 14.0)) {
            if (NightHeronFlightController.tickLandingApproach(this.nightHeron, this.landingTarget)) {
                this.remainingTicks = 0;
            }
        } else {
            if (this.escapeDirection.lengthSqr() <= 1.0E-4 || this.nightHeron.isInWater() || this.remainingTicks % 16 == 0) {
                this.escapeDirection = this.landingTarget != null
                        ? NightHeronLandingSelector.directionTo(this.landingTarget, this.nightHeron)
                        : this.computeEscapeDirection();
            }

            if (longEscape) {
                NightHeronFlightController.tickLongEscapeFlight(this.nightHeron, this.escapeDirection, 0.55, 20.0, 32.0);
            } else {
                NightHeronFlightController.tickLowEscapeFlight(this.nightHeron, this.escapeDirection, 0.36, 4.0, 8.0);
            }
        }
    }

    /**
     * 在地面上逃离
     */
    private void moveAwayOnGround(double speed) {
        Vec3 threatPosition = this.getThreatPosition();
        if (threatPosition != null) {
            Vec3 target = LandRandomPos.getPosAway(this.nightHeron, 12, 5, threatPosition);
            if (target == null) {
                target = this.nightHeron.position().add(this.escapeDirection.scale(8.0));
            }
            this.nightHeron.getNavigation().moveTo(target.x, target.y, target.z, speed);
        }
    }

    /**
     * 计算逃离方向
     */
    private Vec3 computeEscapeDirection() {
        Vec3 threatPosition = this.getThreatPosition();
        if (threatPosition == null) {
            float angle = this.nightHeron.getRandom().nextFloat() * (float) (2.0 * Math.PI);
            return new Vec3(Mth.cos(angle), 0.0, Mth.sin(angle));
        }

        Vec3 away = this.nightHeron.position().subtract(threatPosition);
        Vec3 horizontal = new Vec3(away.x, 0.0, away.z);
        if (horizontal.lengthSqr() <= 1.0E-4) {
            float angle = this.nightHeron.getRandom().nextFloat() * (float) (2.0 * Math.PI);
            return new Vec3(Mth.cos(angle), 0.0, Mth.sin(angle));
        }

        double jitter = (this.nightHeron.getRandom().nextDouble() - 0.5) * 0.55;
        double cos = Math.cos(jitter);
        double sin = Math.sin(jitter);
        Vec3 direction = horizontal.normalize();
        return new Vec3(
                direction.x * cos - direction.z * sin,
                0.0,
                direction.x * sin + direction.z * cos
        ).normalize();
    }

    /**
     * 获取威胁位置
     */
    private Vec3 getThreatPosition() {
        return this.threat != null ? this.threat.position() : this.externalThreatPosition;
    }

    /**
     * 通知附近的夜鹭
     */
    private void notifyNearbyNightHerons(boolean severe) {
        for (NightHeronEntity neighbor : this.nightHeron.level().getEntitiesOfClass(
                NightHeronEntity.class,
                this.nightHeron.getBoundingBox().inflate(12.0),
                other -> other != this.nightHeron
        )) {
            neighbor.receiveFlockFright(this.nightHeron.position(), severe);
        }
    }

    /**
     * 生成随机值
     */
    private int randomBetween(int min, int max) {
        return min + this.nightHeron.getRandom().nextInt(max - min + 1);
    }

    /**
     * 获取恐惧量
     */
    private float frightAmount(Response response) {
        return switch (response) {
            case ALERT -> 0.08F;
            case WALK -> 0.1F;
            case RUN -> 0.16F;
            case LOW_FLIGHT -> 0.24F;
            case LONG_FLIGHT -> 0.35F;
            default -> 0.0F;
        };
    }

    /**
     * 响应级别枚举
     */
    private enum Response {
        NONE,
        ALERT,
        WALK,
        RUN,
        LOW_FLIGHT,
        LONG_FLIGHT;

        public boolean isFlight() {
            return this == LOW_FLIGHT || this == LONG_FLIGHT;
        }
    }
}