package net.fodoth.skina.neoguanniao.content.bird.core.controller;

import net.fodoth.skina.neoguanniao.content.bird.core.*;
import net.fodoth.skina.neoguanniao.content.bird.core.data.BirdData;
import net.fodoth.skina.neoguanniao.content.bird.feature.flight.BirdFlightController;
import net.fodoth.skina.neoguanniao.content.bird.feature.flight.BirdFlightTargeting;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.phys.Vec3;

/**
 * 鸟类飞行控制器
 * <p>
 * 负责管理鸟类飞行状态、飞行目标、起飞、巡航、降落以及飞行朝向等逻辑。
 * </p>
 */
public class BirdFlyingController {

    public boolean isEscapeFlightActive;
    public boolean isLandingFlight;
    public Vec3 flightTarget;
    private final AbstractBirdEntity<?> bird;

    /**
     * 创建鸟类飞行控制器
     *
     * @param entity 鸟类实体
     */
    public BirdFlyingController(AbstractBirdEntity<?> entity) {
        this.bird = entity;
        this.isEscapeFlightActive = false;
        this.isLandingFlight = false;
        bird.setMoveControl(new FlyingMoveControl(bird, bird.getBirdData().maxTurns(), true));
    }

    /**
     * 判断鸟类当前是否处于飞行状态
     *
     * @return 如果处于飞行状态返回 true
     */
    public boolean isBirdFlightActive() {
        var timer = bird.getTickController().getTickTimer();

        boolean hasFlightDuration = timer.getBirdFlyingTicker().flightDuration > 0;
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

        return timer.getBirdFlyingTicker().flightDuration > 0
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
        return timer.getBirdFlyingTicker().flightDuration > 0 || bird.isBirdLanding();
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
        BirdData birdData = bird.getBirdData();

        if (flyingTicker.getTicks() <= 0 && flyingTicker.flightDuration <= 0 && !isLandingFlight) {
            isEscapeFlightActive = fleeing;
            flightTarget = target == null ? findAirCruiseTarget(fleeing) : target;

            flyingTicker.flightDuration = fleeing
                    ? birdData.escapeAirCruiseMinTicks() + bird.getRandom().nextInt(birdData.escapeAirCruiseRandomTicks())
                    : birdData.ambientAirCruiseMinTicks() + bird.getRandom().nextInt(birdData.ambientAirCruiseRandomTicks());
            flyingTicker.flyingTime = 0;
            flyingTicker.hoverRetargetTicks = nextHoverRetargetDelay();

            bird.setNoGravity(true);
            bird.setSilent(false);
            bird.getNavigation().stop();

            int stateTicks = fleeing ? birdData.shortFleeTicks() : birdData.shortFlyTicks();
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
        BirdData birdData = bird.getBirdData();
        return birdData.hoverRetargetMinDelay() + bird.getRandom().nextInt(birdData.hoverRetargetDelayVariance());
    }

    /**
     * 开始一次掠飞
     *
     * @param target 飞行目标，为 {@code null} 时自动寻找目标
     */
    public void startFlybyFlight(Vec3 target) {
        var timer = bird.getTickController().getTickTimer();
        var flyingTicker = timer.getBirdFlyingTicker();
        BirdData birdData = bird.getBirdData();

        isEscapeFlightActive = false;
        isLandingFlight = false;
        flightTarget = target == null ? this.findAirCruiseTarget(false) : this.clampFlightTarget(target);

        flyingTicker.flightDuration = birdData.minFlybyDuration() + bird.getRandom().nextInt(birdData.flybyDurationVariance() + 1);
        flyingTicker.flyingTime = 0;
        flyingTicker.hoverRetargetTicks = birdData.minHoverRetargetTicks() + bird.getRandom().nextInt(birdData.hoverRetargetTicksVariance() + 1);
        flyingTicker.setTicks(Math.max(flyingTicker.getTicks(), birdData.minimumFlightTicks()));

        bird.getNavigation().stop();
        bird.setNoGravity(true);
        bird.setSilent(false);
        bird.getBehaviorStateController().setBehaviorStateFor(BirdBehaviorState.FLYING, birdData.minimumFlightTicks());

        Vec3 direction = bird.getFlyingController().flightTarget.subtract(bird.position()).multiply(1.0, 0.0, 1.0);
        if (direction.length() <= 1.0E-4) {
            direction = randomHorizontalDirection();
        }
        Vec3 movement = direction.normalize().scale(birdData.flybyHorizontalSpeed())
                .add(0, birdData.flybyUpwardSpeed(), 0);
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
            BirdData birdData = bird.getBirdData();
            Vec3 horizontal = standPosition.subtract(bird.position()).multiply(1.0, 0.0, 1.0);

            if (horizontal.length() > 1.0E-4) {
                horizontal = horizontal.normalize().scale(birdData.birdBathMountHorizontalSpeed());
            } else {
                horizontal = Vec3.ZERO;
            }

            Vec3 movement = new Vec3(horizontal.x, birdData.birdBathMountUpwardSpeed(), horizontal.z);

            bird.getNavigation().stop();
            bird.setNoGravity(false);
            bird.setSilent(false);
            bird.getBehaviorStateController().setBehaviorStateFor(BirdBehaviorState.FLYING, birdData.birdBathMountFlightTicks());
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
        BirdData birdData = bird.getBirdData();

        boolean wasEscaping = isEscapeFlightActive;
        flyingTicker.flightDuration = 0;
        flyingTicker.flyingTime = 0;
        flightTarget = null;
        flyingTicker.hoverRetargetTicks = 0;

        isEscapeFlightActive = false;
        isLandingFlight = false;
        bird.setNoGravity(false);

        bird.setDeltaMovement(bird.getDeltaMovement().multiply(
                birdData.flightLandingHorizontalDamping(),
                0,
                birdData.flightLandingHorizontalDamping()
        ));

        int cooldownTicks = wasEscaping
                ? birdData.escapeCooldownMin() + bird.getRandom().nextInt(birdData.escapeCooldownVariance())
                : (bird.isTame()
                ? birdData.tameCooldownMin() + bird.getRandom().nextInt(birdData.tameCooldownVariance())
                : birdData.wildCooldownMin() + bird.getRandom().nextInt(birdData.wildCooldownVariance())
        );
        flyingTicker.setTicks(cooldownTicks);

        if (bird.getBehaviorStateController().getBehaviorState().isAirborne()) {
            bird.getBehaviorStateController().setBehaviorStateFor(BirdBehaviorState.ALERT, birdData.postFlightAlertTicks());
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
            BirdData birdData = bird.getBirdData();

            isLandingFlight = true;
            isEscapeFlightActive = false;
            timer.getBirdFlyingTicker().flightDuration = birdData.landingFlightMinDuration()
                    + bird.getRandom().nextInt(birdData.landingFlightDurationVariance());
            flightTarget = landingTarget;
            timer.getBirdFlyingTicker().hoverRetargetTicks = 0;
            bird.getBehaviorStateController().setBehaviorStateFor(BirdBehaviorState.FLYING, birdData.landingFlightStateTicks());
        }
    }

    /**
     * 延长巡航飞行，等待新的降落机会
     */
    public void extendCruiseAfterUnsafeLanding() {
        var timer = bird.getTickController().getTickTimer();
        BirdData birdData = bird.getBirdData();

        isLandingFlight = false;
        isEscapeFlightActive = false;
        timer.getBirdFlyingTicker().flightDuration = birdData.unsafeLandingCruiseMinDuration()
                + bird.getRandom().nextInt(birdData.unsafeLandingCruiseDurationVariance());
        retargetAirCruise(false);
        bird.setNoGravity(true);
        bird.getBehaviorStateController().setBehaviorStateFor(BirdBehaviorState.FLYING, birdData.unsafeLandingCruiseStateTicks());
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
        BirdData birdData = bird.getBirdData();
        Vec3 direction;

        if (fleeing && bird.getFrightController().frightSource != null) {
            Vec3 away = bird.position().subtract(bird.getFrightController().frightSource);
            direction = away.lengthSqr() > 0.01
                    ? new Vec3(away.x, 0, away.z).normalize()
                    : this.randomHorizontalDirection();
        } else {
            direction = bird.getRandom().nextInt(birdData.cruiseLookChanceDenominator()) == 0
                    ? bird.getLookAngle()
                    : this.randomHorizontalDirection();
        }

        Vec3 target = BirdFlightTargeting.findAirTarget(bird, birdData.flightProfile(), direction, fleeing);
        return target != null ? this.clampFlightTarget(target)
                : this.clampFlightTarget(bird.position().add(
                0,
                bird.onGround() ? birdData.cruiseFallbackHeightGround() : birdData.cruiseFallbackHeightAir(),
                0
        ));
    }

    /**
     * 寻找安全的降落目标
     *
     * @return 降落目标；如果不存在返回 {@code null}
     */
    public Vec3 findLandingTarget() {
        BirdData birdData = bird.getBirdData();

        Vec3 sharedLanding = BirdFlightTargeting.findNearestDryLandingTarget(
                bird,
                birdData.landingSharedRadius(),
                birdData.landingSharedVerticalRange()
        );
        if (sharedLanding != null) {
            return this.clampFlightTarget(sharedLanding);
        }

        BlockPos origin = bird.blockPosition();
        BlockPos landing = this.findDryLandingSurface(origin, birdData.landingSurfaceSearchRadius());
        if (landing != null) {
            return this.clampFlightTarget(Vec3.atBottomCenterOf(landing));
        }

        for (int attempt = 0; attempt < birdData.landingRandomAttempts(); ++attempt) {
            int halfRange = birdData.landingRandomHorizontalRange() / 2;
            int x = origin.getX() + bird.getRandom().nextInt(birdData.landingRandomHorizontalRange()) - halfRange;
            int z = origin.getZ() + bird.getRandom().nextInt(birdData.landingRandomHorizontalRange()) - halfRange;
            BlockPos candidate = this.findDryLandingSurface(
                    new BlockPos(x, origin.getY(), z),
                    birdData.landingSurfaceSearchRadius()
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
        BirdData birdData = bird.getBirdData();
        double y = Mth.clamp(
                target.y,
                bird.level().getMinBuildHeight() + birdData.flightTargetMinHeightOffset(),
                bird.level().getMaxBuildHeight() - birdData.flightTargetMaxHeightOffset()
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
        BirdFlightController.faceMovement(bird, movement, bird.getBirdData().flightProfile().maxPitchDegrees());
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
                BirdData birdData = bird.getBirdData();
                return bird.getDeltaMovement().lengthSqr() > birdData.walkingSpeedThreshold()
                        || !bird.getNavigation().isDone();
            }
        }
        return false;
    }
}