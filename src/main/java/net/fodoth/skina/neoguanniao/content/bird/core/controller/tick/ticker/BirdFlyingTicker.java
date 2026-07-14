package net.fodoth.skina.neoguanniao.content.bird.core.controller.tick.ticker;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.BirdBehaviorState;
import net.fodoth.skina.neoguanniao.content.bird.core.data.BirdData;
import net.fodoth.skina.neoguanniao.content.bird.core.data.datum.BirdFlyingDatum;
import net.fodoth.skina.neoguanniao.content.bird.core.data.datum.BirdMiscDatum;
import net.fodoth.skina.neoguanniao.content.bird.feature.flight.BirdFlightBoids;
import net.fodoth.skina.neoguanniao.content.bird.feature.flight.BirdFlightManager;
import net.fodoth.skina.neoguanniao.content.bird.feature.flight.BirdFlightTargeting;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * 鸟类飞行计时器
 * <p>
 * 负责管理鸟类飞行的完整生命周期，包括：
 * <ul>
 *     <li>落水逃离行为</li>
 *     <li>飞行目标追踪与重定向</li>
 *     <li>飞行方向与速度控制</li>
 *     <li>群体飞行修正（Boids 算法）</li>
 *     <li>降落过程处理</li>
 *     <li>失速恢复</li>
 *     <li>自然空中巡航启动</li>
 *     <li>地面移动朝向同步</li>
 * </ul>
 * 该计时器仅在服务端执行。
 * </p>
 */
public class BirdFlyingTicker extends AbstractBirdTicker {

    /**
     * 当前飞行持续剩余 Tick 数
     */
    public int flightDuration;

    /**
     * 悬停重定向剩余 Tick 数
     */
    public int hoverRetargetTicks;

    /**
     * 已飞行时间
     */
    public int flyingTime;

    /**
     * 创建飞行计时器（仅在服务端执行）
     *
     * @param bird 鸟类实体
     */
    public BirdFlyingTicker(AbstractBirdEntity<?> bird) {
        super(bird, true, false);
    }

    @Override
    protected void run() {
        tickWaterEscape();
        tickFlight();
        tickAmbientAirCruise();
        tickGroundMovementFacing();
    }

    /**
     * 处理落水逃离行为
     * <p>
     * 鸟进入水中时自动寻找空中目标，强制进入飞行状态并脱离水面。
     * </p>
     */
    private void tickWaterEscape() {
        // 仅当鸟类在水中时处理
        if (!bird.isInWater()) {
            return;
        }

        var flyingController = bird.getFlyingController();
        var stateController = bird.getBehaviorStateController();
        BirdData birdData = bird.getBirdData();
        BirdFlyingDatum flyingDatum = birdData.flying();
        BirdMiscDatum miscDatum = birdData.misc();
        var random = bird.getRandom();

        // 停止导航并重置飞行状态
        bird.getNavigation().stop();
        flyingController.isLandingFlight = false;
        flyingController.isEscapeFlightActive = false;

        // 寻找空中目标
        flyingController.flightTarget = flyingController.findAirCruiseTarget(false);

        // 设置飞行持续时长
        int minDuration = flyingDatum.waterEscapeMinDuration();
        int randomDuration = random.nextInt(flyingDatum.waterEscapeRandomDuration());
        flightDuration = Math.max(flightDuration, minDuration + randomDuration);

        // 限制悬停重定向间隔
        int retargetMin = flyingDatum.waterEscapeHoverRetargetMin();
        int retargetMax = flyingDatum.waterEscapeHoverRetargetMax();
        hoverRetargetTicks = Math.clamp(hoverRetargetTicks, retargetMin, retargetMax);

        // 进入飞行状态
        bird.setNoGravity(true);
        stateController.setBehaviorStateFor(BirdBehaviorState.FLYING, flyingDatum.waterEscapeBehaviorTicks());

        // 计算逃离方向和速度
        Vec3 toTarget = flyingController.flightTarget.subtract(bird.position());
        Vec3 horizontal = toTarget.multiply(1.0, 0.0, 1.0);

        if (horizontal.length() <= 1.0E-4) {
            horizontal = flyingController.randomHorizontalDirection();
        }

        Vec3 direction = horizontal.normalize();
        double horizontalSpeed = flyingDatum.waterEscapeHorizontalSpeed();
        double verticalSpeed = flyingDatum.waterEscapeVerticalSpeed();
        Vec3 movement = direction.scale(horizontalSpeed).add(0, verticalSpeed, 0);

        // 应用移动并朝向飞行方向
        bird.setDeltaMovement(movement);
        flyingController.faceFlightDirection(movement);
        bird.xxa = 0.0F;
        bird.hasImpulse = true;
    }

    /**
     * 更新飞行状态
     * <p>
     * 负责处理：飞行目标追踪、飞行方向调整、群体飞行修正、降落过程、失速恢复。
     * </p>
     */
    private void tickFlight() {
        var flyingController = bird.getFlyingController();
        var stateController = bird.getBehaviorStateController();
        BirdData birdData = bird.getBirdData();
        BirdFlyingDatum flyingDatum = birdData.flying();

        // 飞行未激活时重置状态
        if (flightDuration <= 0 && !flyingController.isLandingFlight) {
            flyingTime = 0;
            bird.setNoGravity(false);
            return;
        }

        // 飞行激活时的核心逻辑
        bird.getNavigation().stop();
        bird.setNoGravity(true);
        bird.xxa = 0.0F;
        ++flyingTime;

        // 设置行为状态
        BirdBehaviorState flightState = flyingController.isEscapeFlightActive
                ? BirdBehaviorState.FLEEING
                : BirdBehaviorState.FLYING;
        stateController.setBehaviorState(flightState);

        // 减少飞行持续时间
        if (flightDuration > 0) {
            --flightDuration;
        }

        // 飞行结束且未在降落中时，尝试开始降落
        if (flightDuration <= 0 && !flyingController.isLandingFlight) {
            flyingController.beginLandingFlight();
        }

        // 确保飞行目标存在
        if (flyingController.flightTarget == null) {
            if (flyingController.isLandingFlight) {
                flyingController.flightTarget = flyingController.findLandingTarget();
                if (flyingController.flightTarget == null) {
                    flyingController.extendCruiseAfterUnsafeLanding();
                    return;
                }
            } else {
                flyingController.retargetAirCruise(flyingController.isEscapeFlightActive);
            }
        }

        // 计算到目标的距离
        Vec3 toTarget = flyingController.flightTarget.subtract(bird.position());
        double horizontalDistance = Math.sqrt(toTarget.x * toTarget.x + toTarget.z * toTarget.z);

        // 处理降落逻辑
        if (flyingController.isLandingFlight) {
            if (bird.onGround()) {
                flyingController.finishFlight();
                return;
            }

            double reachDistance = flyingDatum.flightLandingReachDistance();
            if (flightDuration <= 0 && toTarget.length() < reachDistance) {
                flyingController.extendCruiseAfterUnsafeLanding();
                return;
            }
        }

        // 飞行目标重定向
        double reachDistance = flyingDatum.flightTargetReachDistance();
        if (toTarget.length() >= reachDistance && hoverRetargetTicks > 0) {
            --hoverRetargetTicks;
        } else {
            flyingController.retargetAirCruise(flyingController.isEscapeFlightActive);
            toTarget = flyingController.flightTarget.subtract(bird.position());
            horizontalDistance = Math.sqrt(toTarget.x * toTarget.x + toTarget.z * toTarget.z);
        }

        // 计算飞行方向
        Vec3 direction = toTarget.length() > 1.0E-4
                ? toTarget.normalize()
                : flyingController.randomHorizontalDirection();

        Vec3 horizontalDirection = BirdFlightTargeting.normalizeHorizontal(
                new Vec3(direction.x, 0, direction.z),
                bird.getDeltaMovement()
        );

        // 群体飞行修正（非降落状态）
        if (!flyingController.isLandingFlight) {
            double range = flyingDatum.flockRange();
            double separation = flyingDatum.flockSeparation();
            double alignment = flyingDatum.flockAlignment();
            double cohesion = flyingDatum.flockCohesion();
            double weightEscape = flyingDatum.flockWeightEscape();
            double flockWeight = flyingController.isEscapeFlightActive
                    ? flyingDatum.flockEscapeWeight()
                    : flyingDatum.flockAmbientWeight();

            Vec3 flockHeading = BirdFlightBoids.sameTypeHeading(
                    bird, range, separation,
                    alignment, cohesion, weightEscape,
                    flockWeight
            );

            if (flockHeading.length() > 1.0E-4) {
                horizontalDirection = BirdFlightTargeting.normalizeHorizontal(
                        horizontalDirection.add(flockHeading),
                        horizontalDirection
                );
            }
        }

        // 计算飞行速度和期望移动
        double speed = getSpeed(horizontalDistance);
        Vec3 desired = getDesired(toTarget, horizontalDirection, speed);

        // 应用阻尼和期望力
        double movementScale = flyingDatum.flightMovementScale();
        double desiredScale = flyingDatum.flightDesiredScale();
        Vec3 movement = bird.getDeltaMovement().scale(movementScale)
                .add(desired.scale(desiredScale));

        // 失速恢复
        if (!flyingController.isLandingFlight) {
            var stalledThreshold = flyingDatum.flightStalledThreshold();
            if (BirdFlightManager.isStalledInAir(bird, flyingTime, stalledThreshold)) {
                flyingController.retargetAirCruise(flyingController.isEscapeFlightActive);
                double minSpeed = flyingDatum.flightStalledMinSpeed();
                double verticalBoost = flyingDatum.flightStalledVerticalBoost();
                movement = horizontalDirection.scale(Math.max(speed, minSpeed))
                        .add(0, verticalBoost, 0);
            }
        }

        // 应用移动并朝向飞行方向
        bird.setDeltaMovement(movement);
        flyingController.faceFlightDirection(movement);
        bird.hasImpulse = true;
    }

    /**
     * 根据飞行目标计算期望移动向量
     *
     * @param toTarget            到目标位置的方向向量
     * @param horizontalDirection 水平方向
     * @param speed               当前飞行速度
     * @return 期望移动速度向量
     */
    private @NotNull Vec3 getDesired(Vec3 toTarget, Vec3 horizontalDirection, double speed) {
        BirdData birdData = bird.getBirdData();
        BirdFlyingDatum flyingDatum = birdData.flying();
        var flyingController = bird.getFlyingController();

        // 计算垂直目标
        double vertical;
        if (flyingController.isLandingFlight) {
            double factor = flyingDatum.flightVerticalLandingFactor();
            double clampMin = flyingDatum.flightVerticalClampMin();
            double clampMax = flyingDatum.flightVerticalClampMax();
            vertical = Mth.clamp(
                    toTarget.y * factor + flyingDatum.flightLandingHoverBob(),
                    clampMin,
                    clampMax
            );
        } else {
            double factor = flyingDatum.flightVerticalAmbientFactor();
            double min = flyingDatum.flightVerticalAmbientMin();
            double max = flyingDatum.flightVerticalAmbientMax();
            double hoverBob = Math.sin((bird.tickCount + bird.getId()) * flyingDatum.flightHoverBobFrequency())
                    * flyingDatum.flightHoverBobAmplitude();
            vertical = Mth.clamp(
                    toTarget.y * factor + hoverBob,
                    min,
                    max
            );
        }

        return new Vec3(horizontalDirection.x * speed, vertical, horizontalDirection.z * speed);
    }

    /**
     * 获取当前飞行速度
     * <p>
     * 根据飞行类型（逃离、巡航、降落）使用不同速度参数。
     * </p>
     *
     * @param horizontalDistance 与目标的水平距离
     * @return 当前飞行速度
     */
    private double getSpeed(double horizontalDistance) {
        BirdData birdData = bird.getBirdData();
        BirdFlyingDatum flyingDatum = birdData.flying();
        var flyingController = bird.getFlyingController();

        // 选择基础速度
        double speed;
        if (flyingController.isEscapeFlightActive) {
            speed = flyingDatum.flightEscapeSpeed();
        } else if (flyingController.isLandingFlight) {
            speed = flyingDatum.flightLandingSpeed();
        } else {
            speed = flyingDatum.flightAmbientSpeed();
        }

        // 降落时减速
        if (flyingController.isLandingFlight) {
            double decalDistance = flyingDatum.flightLandingDecalDistance();
            double decalFactor = flyingDatum.flightLandingDecalFactor();
            speed = BirdFlightManager.decelerateNearLanding(
                    speed,
                    horizontalDistance,
                    decalDistance,
                    decalFactor
            );
        }

        return speed;
    }

    /**
     * 尝试启动自然空中巡航行为
     * <p>
     * 根据鸟种配置概率，随机触发短距离飞行。
     * </p>
     */
    private void tickAmbientAirCruise() {
        if (!bird.getFlyingController().canStartAmbientAirCruise()) {
            return;
        }

        BirdData birdData = bird.getBirdData();
        BirdFlyingDatum flyingDatum = birdData.flying();
        int chance = bird.isTame()
                ? flyingDatum.ambientAirCruiseChanceTame()
                : flyingDatum.ambientAirCruiseChanceWild();

        if (bird.getRandom().nextInt(chance) == 0) {
            Vec3 target = bird.getFlyingController().findAirCruiseTarget(false);
            bird.getFlyingController().startShortFlight(target, false);
        }
    }

    /**
     * 同步地面移动方向
     * <p>
     * 根据移动速度调整鸟实体朝向，保持模型旋转与移动方向一致。
     * </p>
     */
    private void tickGroundMovementFacing() {
        if (bird.getFlyingController().shouldFaceGroundMovement()) {
            BirdFlightManager.faceGroundMovement(bird, bird.getDeltaMovement(), 1.0E-4);
        }
    }
}