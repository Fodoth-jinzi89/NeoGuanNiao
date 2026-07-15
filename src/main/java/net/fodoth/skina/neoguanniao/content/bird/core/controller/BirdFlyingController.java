package net.fodoth.skina.neoguanniao.content.bird.core.controller;

import net.fodoth.skina.neoguanniao.content.bird.core.*;
import net.fodoth.skina.neoguanniao.content.bird.core.data.BirdData;
import net.fodoth.skina.neoguanniao.content.bird.core.data.datum.BirdFlyingDatum;
import net.fodoth.skina.neoguanniao.content.bird.core.data.datum.BirdMiscDatum;
import net.fodoth.skina.neoguanniao.content.bird.feature.flight.BirdFlightManager;
import net.fodoth.skina.neoguanniao.content.bird.feature.flight.BirdFlightTargeting;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.phys.Vec3;

/**
 * 鸟类飞行控制器
 * <p>
 * 负责管理鸟类飞行状态、飞行目标、起飞、巡航、降落以及飞行朝向等逻辑。
 * </p>
 */
public class BirdFlyingController<T extends AbstractBirdEntity<T>>
        extends AbstractBirdController<T> {

    public boolean isEscapeFlightActive;
    public boolean isLandingFlight;
    public Vec3 flightTarget;


    public BirdFlyingController() {
        this.isEscapeFlightActive = false;
        this.isLandingFlight = false;
        this.flightTarget = null;
    }


    @Override
    protected void onAttach() {
        super.onAttach();

        BirdData birdData = bird().getbirdData();
        BirdMiscDatum miscDatum = birdData.misc();

        bird().setMoveControl(
                new FlyingMoveControl(
                        bird(),
                        miscDatum.maxTurns(),
                        true
                )
        );
    }

    /**
     * 判断鸟类当前是否处于飞行状态
     *
     * @return 如果处于飞行状态返回 true
     */
    public boolean isBirdFlightActive() {
        var timer = bird.getTickController().getTickTimer();

        boolean hasFlightDuration = timer.getBirdFlyingTicker().getTicks() > 0;
        boolean isLandingFlight = bird.isBirdLanding();
        boolean isAirborne = bird.getBehaviorStateController().getBehaviorState().isAirborne();
        boolean isInWaterAndNotOnGround = bird.isInWater() && !bird.onGround();

        return hasFlightDuration || isLandingFlight || isAirborne || isInWaterAndNotOnGround;
    }

    /**
     * 判断鸟类当前是否正在飞行
     *
     * @return 如果正在飞行返回 true
     */
    public boolean isBirdFlying() {
        var timer = bird.getTickController().getTickTimer();
        BirdBehaviorState state = bird.getBehaviorStateController().getBehaviorState();

        return timer.getBirdFlyingTicker().getTicks() > 0
                || bird.isBirdLanding()
                || !bird.onGround()
                || bird.isInWater()
                || state == BirdBehaviorState.FLYING
                || (state == BirdBehaviorState.FLEEING && !bird.onGround());
    }

    /**
     * 判断当前是否存在未完成的飞行过程
     *
     * @return 如果飞行尚未结束返回 true
     */
    public boolean isFlightInProgress() {
        var timer = bird.getTickController().getTickTimer();
        return timer.getBirdFlyingTicker().getTicks() > 0 || bird.isBirdLanding();
    }

    /**
     * 开始一次短距离飞行
     *
     * @param target  飞行目标，为 {@code null} 时自动寻找目标
     * @param fleeing 是否为逃跑飞行
     */
    public void startShortFlight(Vec3 target, boolean fleeing) {
        var timer = bird.getTickController().getTickTimer();
        var flyingTicker = timer.getBirdFlyingTicker();
        var stateController = bird.getBehaviorStateController();
        BirdData birdData = bird.getbirdData();
        BirdFlyingDatum flyingDatum = birdData.flying();

        if (flyingTicker.getTicks() <= 0 && flyingTicker.flyingTime <= 0 && !isLandingFlight) {
            isEscapeFlightActive = fleeing;
            flightTarget = target == null ? findAirCruiseTarget(fleeing) : target;

            flyingTicker.setTicks(fleeing
                    ? flyingDatum.escapeAirCruiseMinTicks() + bird.getRandom().nextInt(flyingDatum.escapeAirCruiseRandomTicks())
                    : flyingDatum.ambientAirCruiseMinTicks() + bird.getRandom().nextInt(flyingDatum.ambientAirCruiseRandomTicks()));
            flyingTicker.flyingTime = 0;
            flyingTicker.hoverRetargetTicks = nextHoverRetargetDelay();

            bird.setNoGravity(true);
            bird.setSilent(true);
            bird.getNavigation().stop();

            int stateTicks = fleeing ? flyingDatum.shortFleeTicks() : flyingDatum.shortFlyTicks();
            BirdBehaviorState state = fleeing ? BirdBehaviorState.FLEEING : BirdBehaviorState.FLYING;
            stateController.setBehaviorStateFor(state, stateTicks);
        }
    }

    /**
     * 生成下一次悬停重定向延迟
     *
     * @return 延迟 Tick 数
     */
    private int nextHoverRetargetDelay() {
        BirdData birdData = bird.getbirdData();
        BirdFlyingDatum flyingDatum = birdData.flying();
        return flyingDatum.hoverRetargetMinDelay() + bird.getRandom().nextInt(flyingDatum.hoverRetargetDelayVariance());
    }

    /**
     * 开始一次掠飞
     *
     * @param target 飞行目标，为 {@code null} 时自动寻找目标
     */
    public void startFlybyFlight(Vec3 target) {
        var timer = bird.getTickController().getTickTimer();
        var flyingTicker = timer.getBirdFlyingTicker();
        BirdData birdData = bird.getbirdData();
        BirdFlyingDatum flyingDatum = birdData.flying();
        BirdMiscDatum miscDatum = birdData.misc();

        isEscapeFlightActive = false;
        isLandingFlight = false;
        flightTarget = target == null ? this.findAirCruiseTarget(false) : this.clampFlightTarget(target);

        flyingTicker.setTicks(flyingDatum.minFlybyDuration() + bird.getRandom().nextInt(flyingDatum.flybyDurationVariance() + 1));
        flyingTicker.flyingTime = 0;
        flyingTicker.hoverRetargetTicks = flyingDatum.minHoverRetargetTicks() + bird.getRandom().nextInt(flyingDatum.hoverRetargetTicksVariance() + 1);
        flyingTicker.setTicks(Math.max(flyingTicker.getTicks(), flyingDatum.minimumFlightTicks()));

        bird.getNavigation().stop();
        bird.setNoGravity(true);
        bird.setSilent(false);
        bird.getBehaviorStateController().setBehaviorStateFor(BirdBehaviorState.FLYING, flyingDatum.minimumFlightTicks());

        Vec3 direction = bird.getFlyingController().flightTarget.subtract(bird.position()).multiply(1.0, 0.0, 1.0);
        if (direction.length() <= 1.0E-4) {
            direction = randomHorizontalDirection();
        }
        Vec3 movement = direction.normalize().scale(flyingDatum.flybyHorizontalSpeed())
                .add(0, flyingDatum.flybyUpwardSpeed(), 0);
        bird.setDeltaMovement(movement);
        faceFlightDirection(movement);
        bird.xxa = 0.0F;
        bird.hasImpulse = true;
    }

    /**
     * 开始飞向鸟浴盆
     *
     * @param standPosition 鸟浴盆停留位置
     * @return 如果成功开始飞行返回 true
     */
    public boolean startBirdBathMountFlight(Vec3 standPosition) {
        if (standPosition != null && !isFlightInProgress()) {
            BirdData birdData = bird.getbirdData();
            BirdFlyingDatum flyingDatum = birdData.flying();
            BirdMiscDatum miscDatum = birdData.misc();

            Vec3 horizontal = standPosition.subtract(bird.position()).multiply(1.0, 0.0, 1.0);

            if (horizontal.length() > 1.0E-4) {
                horizontal = horizontal.normalize().scale(flyingDatum.birdBathMountHorizontalSpeed());
            } else {
                horizontal = Vec3.ZERO;
            }

            Vec3 movement = new Vec3(horizontal.x, flyingDatum.birdBathMountUpwardSpeed(), horizontal.z);

            bird.getNavigation().stop();
            bird.setNoGravity(false);
            bird.setSilent(false);
            bird.getBehaviorStateController().setBehaviorStateFor(BirdBehaviorState.FLYING, flyingDatum.birdBathMountFlightTicks());
            bird.setDeltaMovement(movement);
            faceFlightDirection(movement);
            bird.xxa = 0.0F;
            bird.hasImpulse = true;
            return true;
        }
        return false;
    }

    /**
     * 结束当前飞行
     */
    public void finishFlight() {
        var timer = bird.getTickController().getTickTimer();
        var flyingTicker = timer.getBirdFlyingTicker();
        BirdData birdData = bird.getbirdData();
        BirdFlyingDatum flyingDatum = birdData.flying();
        BirdMiscDatum miscDatum = birdData.misc();

        boolean wasEscaping = isEscapeFlightActive;
        flyingTicker.setTicks(0);
        flyingTicker.flyingTime = 0;
        flightTarget = null;
        flyingTicker.hoverRetargetTicks = 0;

        isEscapeFlightActive = false;
        isLandingFlight = false;
        bird.setNoGravity(false);
        bird.setSilent(false);
        bird.noCulling = false;

        bird.setDeltaMovement(bird.getDeltaMovement().multiply(
                flyingDatum.flightLandingHorizontalDamping(),
                flyingDatum.flightLandingVerticalDamping(),
                flyingDatum.flightLandingHorizontalDamping()
        ));

        int cooldownTicks = wasEscaping
                ? miscDatum.escapeCooldownMin() + bird.getRandom().nextInt(miscDatum.escapeCooldownVariance())
                : (bird.isTame()
                ? miscDatum.tameCooldownMin() + bird.getRandom().nextInt(miscDatum.tameCooldownVariance())
                : miscDatum.wildCooldownMin() + bird.getRandom().nextInt(miscDatum.wildCooldownVariance())
        );
        bird().getTickController().getTickTimer().getBirdLandingTicker().setTicks(cooldownTicks);

        bird().addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, cooldownTicks, 0, false, false));

        if (bird.getBehaviorStateController().getBehaviorState().isAirborne()) {
            bird.getBehaviorStateController().setBehaviorStateFor(BirdBehaviorState.ALERT, miscDatum.postFlightAlertTicks());
        }
    }

    /**
     * 开始降落飞行
     */
    public void beginLandingFlight() {
        Vec3 landingTarget = this.findLandingTarget();
        if (landingTarget == null) {
            this.extendCruiseAfterUnsafeLanding();
        } else {
            var timer = bird.getTickController().getTickTimer();
            BirdData birdData = bird.getbirdData();
            BirdFlyingDatum flyingDatum = birdData.flying();

            isLandingFlight = true;
            isEscapeFlightActive = false;
            timer.getBirdFlyingTicker().setTicks(flyingDatum.landingFlightMinDuration()
                    + bird.getRandom().nextInt(flyingDatum.landingFlightDurationVariance()));
            flightTarget = landingTarget;
            timer.getBirdFlyingTicker().hoverRetargetTicks = 0;
            bird.getBehaviorStateController().setBehaviorStateFor(BirdBehaviorState.FLYING, flyingDatum.landingFlightStateTicks());
        }
    }

    /**
     * 延长巡航飞行，等待新的降落机会
     */
    public void extendCruiseAfterUnsafeLanding() {
        var timer = bird.getTickController().getTickTimer();
        BirdData birdData = bird.getbirdData();
        BirdFlyingDatum flyingDatum = birdData.flying();

        isLandingFlight = false;
        isEscapeFlightActive = false;
        timer.getBirdFlyingTicker().setTicks(flyingDatum.unsafeLandingCruiseMinDuration()
                + bird.getRandom().nextInt(flyingDatum.unsafeLandingCruiseDurationVariance()));
        retargetAirCruise(false);
        bird.setNoGravity(true);
        bird.getBehaviorStateController().setBehaviorStateFor(BirdBehaviorState.FLYING, flyingDatum.unsafeLandingCruiseStateTicks());
    }

    /**
     * 重新选择巡航目标
     *
     * @param fleeing 是否为逃跑飞行
     */
    public void retargetAirCruise(boolean fleeing) {
        flightTarget = this.findAirCruiseTarget(fleeing);
        bird.getTickController().getTickTimer().getBirdFlyingTicker().hoverRetargetTicks = this.nextHoverRetargetDelay();
    }

    /**
     * 寻找空中巡航目标
     *
     * @param fleeing 是否为逃跑飞行
     * @return 巡航目标位置
     */
    public Vec3 findAirCruiseTarget(boolean fleeing) {
        BirdData birdData = bird.getbirdData();
        BirdFlyingDatum flyingDatum = birdData.flying();

        Vec3 direction;

        if (fleeing && bird.getFrightController().frightSource != null) {
            Vec3 away = bird.position().subtract(bird.getFrightController().frightSource);
            direction = away.lengthSqr() > 0.01
                    ? new Vec3(away.x, 0, away.z).normalize()
                    : this.randomHorizontalDirection();
        } else {
            direction = bird.getRandom().nextInt(flyingDatum.cruiseLookChanceDenominator()) == 0
                    ? bird.getLookAngle()
                    : this.randomHorizontalDirection();
        }

        Vec3 target = BirdFlightTargeting.findAirTarget(bird, birdData.flying().flightProfile(), direction, fleeing);
        return target != null ? this.clampFlightTarget(target)
                : this.clampFlightTarget(bird.position().add(
                0,
                bird.onGround() ? flyingDatum.cruiseFallbackHeightGround() : flyingDatum.cruiseFallbackHeightAir(),
                0
        ));
    }

    /**
     * 寻找安全的降落目标
     *
     * @return 降落目标；如果不存在返回 {@code null}
     */
    public Vec3 findLandingTarget() {
        BirdData birdData = bird.getbirdData();
        BirdFlyingDatum flyingDatum = birdData.flying();

        Vec3 sharedLanding = BirdFlightTargeting.findNearestDryLandingTarget(
                bird,
                flyingDatum.landingSharedRadius(),
                flyingDatum.landingSharedVerticalRange()
        );
        if (sharedLanding != null) {
            return this.clampFlightTarget(sharedLanding);
        }

        BlockPos origin = bird.blockPosition();
        BlockPos landing = this.findDryLandingSurface(origin, flyingDatum.landingSurfaceSearchRadius());
        if (landing != null) {
            return this.clampFlightTarget(Vec3.atBottomCenterOf(landing));
        }

        for (int attempt = 0; attempt < flyingDatum.landingRandomAttempts(); ++attempt) {
            int halfRange = flyingDatum.landingRandomHorizontalRange() / 2;
            int x = origin.getX() + bird.getRandom().nextInt(flyingDatum.landingRandomHorizontalRange()) - halfRange;
            int z = origin.getZ() + bird.getRandom().nextInt(flyingDatum.landingRandomHorizontalRange()) - halfRange;
            BlockPos candidate = this.findDryLandingSurface(
                    new BlockPos(x, origin.getY(), z),
                    flyingDatum.landingSurfaceSearchRadius()
            );
            if (candidate != null) {
                return this.clampFlightTarget(Vec3.atBottomCenterOf(candidate));
            }
        }
        return null;
    }

    /**
     * 搜索安全的降落地面
     *
     * @param center        搜索中心
     * @param verticalRange 垂直搜索范围
     * @return 安全降落位置；如果不存在返回 {@code null}
     */
    @SuppressWarnings("SameParameterValue")
    private BlockPos findDryLandingSurface(BlockPos center, int verticalRange) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int yOffset = verticalRange; yOffset >= -verticalRange; --yOffset) {
            mutable.set(center.getX(), center.getY() + yOffset, center.getZ());
            if (this.isSafeDryLanding(mutable)) {
                return mutable.immutable();
            }
        }
        return null;
    }

    /**
     * 判断指定位置是否适合作为安全降落点
     *
     * @param pos 方块位置
     * @return 如果可以安全降落返回 true
     */
    private boolean isSafeDryLanding(BlockPos pos) {
        return BirdFlightTargeting.isSafeDryLanding(bird, pos);
    }

    /**
     * 将飞行目标限制在合法高度范围内
     *
     * @param target 原始飞行目标
     * @return 调整后的飞行目标
     */
    private Vec3 clampFlightTarget(Vec3 target) {
        BirdData birdData = bird.getbirdData();
        BirdFlyingDatum flyingDatum = birdData.flying();
        double y = Mth.clamp(
                target.y,
                bird.level().getMinBuildHeight() + flyingDatum.flightTargetMinHeightOffset(),
                bird.level().getMaxBuildHeight() - flyingDatum.flightTargetMaxHeightOffset()
        );
        return new Vec3(target.x, y, target.z);
    }

    /**
     * 生成随机水平方向
     *
     * @return 单位方向向量
     */
    public Vec3 randomHorizontalDirection() {
        return BirdFlightTargeting.randomHorizontalDirection(bird.getRandom());
    }

    /**
     * 判断是否可以开始自主巡航飞行
     *
     * @return 如果可以开始巡航返回 true
     */
    public boolean canStartAmbientAirCruise() {
        var timer = bird.getTickController().getTickTimer();
        BirdBehaviorState state = bird.getBehaviorStateController().getBehaviorState();

        return timer.getBirdFlyingTicker().getTicks() <= 0
                && bird.onGround()
                && bird.getRoutineController().isActiveTime()
                && bird.getNavigation().isDone()
                && !bird.getEatingController().isEating()
                && !bird.getRoutineController().isSleepingOrRoosting()
                && state != BirdBehaviorState.FORAGING
                && state != BirdBehaviorState.PERCHING
                && state != BirdBehaviorState.FOLLOWING
                && state != BirdBehaviorState.SENTINEL
                && !state.isEscape();
    }

    /**
     * 使鸟类朝向飞行方向
     *
     * @param movement 飞行运动向量
     */
    public void faceFlightDirection(Vec3 movement) {
        BirdData birdData = bird.getbirdData();
        BirdFlyingDatum flyingDatum = birdData.flying();
        BirdFlightManager.faceMovement(bird, movement, flyingDatum.flightProfile().maxPitchDegrees());
    }

    /**
     * 判断是否应该朝向地面移动方向
     *
     * @return 如果应该更新朝向返回 true
     */
    public boolean shouldFaceGroundMovement() {
        if (bird.onGround() && !bird.isPassenger() && !bird.isInWater() && !bird.isVehicle()) {
            BirdBehaviorState state = bird.getBehaviorStateController().getBehaviorState();
            if (!state.isAirborne() && state != BirdBehaviorState.EATING
                    && state != BirdBehaviorState.PREENING && state != BirdBehaviorState.DANCING
                    && state != BirdBehaviorState.SLEEPING && state != BirdBehaviorState.ROOSTING) {
                BirdData birdData = bird.getbirdData();
                BirdMiscDatum miscDatum = birdData.misc();
                return bird.getDeltaMovement().lengthSqr() > miscDatum.walkingSpeedThreshold()
                        || !bird.getNavigation().isDone();
            }
        }
        return false;
    }
}