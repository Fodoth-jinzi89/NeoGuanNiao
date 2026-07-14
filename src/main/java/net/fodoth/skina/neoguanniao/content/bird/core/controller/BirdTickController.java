package net.fodoth.skina.neoguanniao.content.bird.core.controller;

import net.fodoth.skina.neoguanniao.content.bird.core.BirdBehaviorState;
import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.data.BirdData;
import net.fodoth.skina.neoguanniao.content.bird.feature.flight.BirdFlightBoids;
import net.fodoth.skina.neoguanniao.content.bird.feature.flight.BirdFlightController;
import net.fodoth.skina.neoguanniao.content.bird.feature.flight.BirdFlightTargeting;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * 鸟类 Tick 生命周期控制器。
 *
 * <p>负责维护鸟实体运行过程中各种基于 Tick 的临时状态，
 * 并驱动鸟类核心行为逻辑，包括：</p>
 *
 * <ul>
 *     <li>行为状态锁定与恢复</li>
 *     <li>进食计时</li>
 *     <li>驯服后的庆祝行为</li>
 *     <li>落水逃离行为</li>
 *     <li>受惊延迟处理</li>
 *     <li>飞行控制与巡航</li>
 *     <li>默认行为状态修正</li>
 *     <li>地面移动朝向同步</li>
 * </ul>
 *
 * <p>该控制器主要负责时间驱动逻辑，
 * 不直接定义具体行为规则，行为选择由各个 Controller 或 AI 系统负责。</p>
 */
public class BirdTickController {
    /**
     * 行为状态锁定剩余 Tick。
     *
     * <p>锁定期间禁止其他行为覆盖当前状态。</p>
     */
    public int behaviorStateLockTicks;

    /**
     * 好奇状态剩余 Tick。
     */
    public int curiousTicks;

    /**
     * 进食动画剩余 Tick。
     */
    public int eatingTicks;

    /**
     * 外部惊吓状态剩余 Tick。
     */
    public int externalFrightTicks;

    /**
     * 飞行状态剩余 Tick。
     */
    public int flightTicks;

    /**
     * 食物相关行为剩余 Tick。
     */
    public int foodTicks;

    /**
     * 当前信任值。
     *
     * <p>用于判断鸟是否满足驯服条件。</p>
     */
    public int trustTicks;

    /**
     * 空中悬停目标重新选择计时器。
     */
    public int hoverRetargetTicks;

    /**
     * 等待触发惊吓行为的剩余 Tick。
     */
    public int pendingFrightTicks;

    /**
     * 待执行的闲置动画剩余 Tick。
     */
    public int idleAnimationTicks;

    /**
     * 驯服庆祝行为剩余 Tick。
     */
    public int postTameActionTicks;

    /**
     * 驯服庆祝行为切换计时器。
     */
    public int postTameActionSwapTicks;

    /**
     * 当前飞行持续时间。
     */
    public int flyingTime;

    /**
     * 当前飞行任务剩余持续时间。
     */
    public int flightDuration;

    /**
     * 惊吓行为触发后的持续时间。
     */
    public int pendingFrightDuration;

    /**
     * 当前控制的鸟实体。
     */
    private final AbstractBirdEntity<?> bird;

    /**
     * 创建鸟类 Tick 控制器。
     *
     * @param entity 当前控制的鸟实体
     */
    public BirdTickController(AbstractBirdEntity<?> entity){
        this.bird = entity;
    }

    /**
     * 执行服务端 Tick 更新。
     *
     * <p>处理所有需要服务器同步的鸟类状态，
     * 包括移动、行为、飞行和状态计时。</p>
     */
    public void tick() {
        run();
        tickEating();
        tickPostTameAction();
        tickWaterEscape();
        tickPendingFright();
        tickFlight();
        tickAmbientAirCruise();
        tickBehaviorFallback();
        tickGroundMovementFacing();
    }

    /**
     * 执行客户端 Tick 更新。
     *
     * <p>主要处理客户端表现相关计时，
     * 例如动画播放。</p>
     */
    public void tickClient() {
        runClient();
    }

    /**
     * 更新服务端状态计时器。
     *
     * <p>减少各种持续时间计数，
     * 并处理信任值自然衰减。</p>
     */
    private void run() {
        if (behaviorStateLockTicks > 0) {
            --behaviorStateLockTicks;
        }
        if (foodTicks > 0) {
            --foodTicks;
        }
        if (flightTicks > 0) {
            --flightTicks;
        }
        if (externalFrightTicks > 0) {
            --externalFrightTicks;
        }
        if (curiousTicks > 0) {
            --curiousTicks;
        }
        if (trustTicks > 0 && bird.tickCount % 40 == 0) {
            --trustTicks;
        }
        if (idleAnimationTicks > 0) {
            --idleAnimationTicks;
        }
        if (postTameActionSwapTicks > 0) {
            --postTameActionSwapTicks;
        }
    }

    /**
     * 更新客户端表现计时器。
     *
     * <p>处理客户端动画相关状态。</p>
     */
    private void runClient() {
        if (behaviorStateLockTicks > 0) {
            --behaviorStateLockTicks;
        }
        if (eatingTicks > 0) {
            --eatingTicks;
        }
        if (idleAnimationTicks > 0) {
            --idleAnimationTicks;
        }
        if (postTameActionTicks > 0) {
            --postTameActionTicks;
        }
        if (postTameActionSwapTicks > 0) {
            --postTameActionSwapTicks;
        }
    }

    /**
     * 更新进食状态。
     *
     * <p>进食期间停止移动并保持 EATING 行为状态，
     * 时间结束后清除进食状态。</p>
     */
    private void tickEating() {
        if (eatingTicks > 0) {
            bird.getNavigation().stop();
            bird.getBehaviorStateController().setBehaviorState(BirdBehaviorState.EATING);
            if (--eatingTicks <= 0) {
                bird.getEatingController().clearEating();
            }
        }
    }

    /**
     * 更新驯服后的庆祝行为。
     *
     * <p>控制驯服成功后的一段特殊行为周期，
     * 包括好奇、整理羽毛以及观察主人等动作。</p>
     */
    private void tickPostTameAction() {
        if (postTameActionTicks > 0) {
            --postTameActionTicks;
            if (!bird.isPassenger()) {
                if (bird.getEatingController().isEating()) {
                    bird.getEatingController().clearEating();
                }
                if (bird.getBehaviorStateController().getBehaviorState() == BirdBehaviorState.SLEEPING
                        || bird.getBehaviorStateController().getBehaviorState() == BirdBehaviorState.ROOSTING) {
                    behaviorStateLockTicks = 0;
                    bird.getBehaviorStateController().setBehaviorState(BirdBehaviorState.CURIOUS);
                }
                if (bird.getOwner() != null && bird.tickCount % 8 == 0) {
                    bird.getLookControl().setLookAt(bird.getOwner(), 35.0F, 35.0F);
                }
                if (postTameActionSwapTicks <= 0 || bird.getBehaviorStateController().getBehaviorState() == BirdBehaviorState.IDLE) {
                    BirdBehaviorState state = bird.getRandom().nextBoolean()
                            ? BirdBehaviorState.CURIOUS
                            : BirdBehaviorState.PREENING;
                    BirdData birdData = bird.getBirdData();
                    bird.getBehaviorStateController().setBehaviorStateFor(state, birdData.tamedBehaviorTicks() + bird.getRandom().nextInt(birdData.tamedBehaviorTicksVariance()));
                    postTameActionSwapTicks = birdData.postTameActionSwapTicks() + bird.getRandom().nextInt(birdData.postTameActionSwapTicksVariance());
                }
                if (postTameActionTicks <= 0 && (bird.getBehaviorStateController().getBehaviorState() == BirdBehaviorState.CURIOUS
                        || bird.getBehaviorStateController().getBehaviorState() == BirdBehaviorState.PREENING)) {
                    behaviorStateLockTicks = 0;
                    bird.getBehaviorStateController().setBehaviorState(BirdBehaviorState.IDLE);
                }
            }
        }
    }

    /**
     * 处理落水逃离行为。
     *
     * <p>鸟进入水中时自动寻找空中目标，
     * 强制进入飞行状态并脱离水面。</p>
     */
    private void tickWaterEscape() {
        if (bird.isInWater()) {
            bird.getNavigation().stop();
            bird.getFlyingController().isLandingFlight = false;
            bird.getFlyingController().isEscapeFlightActive = false;
            bird.getFlyingController().flightTarget = bird.getFlyingController().findAirCruiseTarget(false);
            BirdData birdData = bird.getBirdData();
            flightDuration = Math.max(flightDuration, birdData.waterEscapeMinDuration() + bird.getRandom().nextInt(birdData.waterEscapeRandomDuration()));
            hoverRetargetTicks = Math.clamp(hoverRetargetTicks, birdData.waterEscapeHoverRetargetMin(), birdData.waterEscapeHoverRetargetMax());
            bird.setNoGravity(true);
            bird.getBehaviorStateController().setBehaviorStateFor(BirdBehaviorState.FLYING, birdData.waterEscapeBehaviorTicks());

            Vec3 direction = bird.getFlyingController().flightTarget.subtract(bird.position())
                    .multiply(1.0, 0.0, 1.0);
            if (direction.length() <= 1.0E-4) {
                direction = bird.getFlyingController().randomHorizontalDirection();
            }
            Vec3 movement = direction.normalize().scale(birdData.waterEscapeHorizontalSpeed()).add(0, birdData.waterEscapeVerticalSpeed(), 0);
            bird.setDeltaMovement(movement);
            bird.getFlyingController().faceFlightDirection(movement);
            bird.xxa = 0.0F;
            bird.hasImpulse = true;
        }
    }

    /**
     * 处理延迟触发的惊吓行为。
     *
     * <p>等待惊吓计时结束后，
     * 根据记录的位置执行逃跑行为。</p>
     */
    private void tickPendingFright() {
        if (pendingFrightTicks > 0) {
            --pendingFrightTicks;
            bird.getNavigation().stop();
            BirdData birdData = bird.getBirdData();
            if (pendingFrightTicks > 0) {
                if (bird.getFrightController().pendingFrightSource != null) {
                    bird.getLookControl().setLookAt(
                            bird.getFrightController().pendingFrightSource.x,
                            bird.getFrightController().pendingFrightSource.y + birdData.pendingFrightLookYOffset(),
                            bird.getFrightController().pendingFrightSource.z,
                            birdData.pendingFrightLookSpeed(), birdData.pendingFrightLookSpeed());
                }
            } else {
                Vec3 sourcePos = bird.getFrightController().pendingFrightSource == null ? bird.position() : bird.getFrightController().pendingFrightSource;
                int duration = Math.max(birdData.pendingFrightMinDuration(), pendingFrightDuration);
                bird.getFrightController().pendingFrightSource = null;
                pendingFrightDuration = 0;
                bird.getFrightController().frightenFrom(sourcePos, duration);
            }
        }
    }

    /**
     * 更新飞行状态。
     *
     * <p>负责处理：</p>
     * <ul>
     *     <li>飞行目标追踪</li>
     *     <li>飞行方向调整</li>
     *     <li>群体飞行修正</li>
     *     <li>降落过程</li>
     *     <li>失速恢复</li>
     * </ul>
     */
    private void tickFlight() {
        if (flightDuration <= 0 && !bird.getFlyingController().isLandingFlight) {
            flyingTime = 0;
            bird.setNoGravity(false);
        } else {
            bird.getNavigation().stop();
            bird.setNoGravity(true);
            bird.xxa = 0.0F;
            ++flyingTime;
            bird.getBehaviorStateController().setBehaviorState(bird.getFlyingController().isEscapeFlightActive
                    ? BirdBehaviorState.FLEEING
                    : BirdBehaviorState.FLYING);

            if (flightDuration > 0) {
                --flightDuration;
            }
            if (flightDuration <= 0 && !bird.getFlyingController().isLandingFlight) {
                bird.getFlyingController().beginLandingFlight();
            }

            if (bird.getFlyingController().flightTarget == null) {
                if (bird.getFlyingController().isLandingFlight) {
                    bird.getFlyingController().flightTarget = bird.getFlyingController().findLandingTarget();
                    if (bird.getFlyingController().flightTarget == null) {
                        bird.getFlyingController().extendCruiseAfterUnsafeLanding();
                        return;
                    }
                } else {
                    bird.getFlyingController().retargetAirCruise(bird.getFlyingController().isEscapeFlightActive);
                }
            }

            Vec3 toTarget = bird.getFlyingController().flightTarget.subtract(bird.position());
            double horizontalDistance = Math.sqrt(toTarget.x * toTarget.x + toTarget.z * toTarget.z);

            BirdData birdData = bird.getBirdData();
            if (bird.getFlyingController().isLandingFlight) {
                if (bird.onGround()) {
                    bird.getFlyingController().finishFlight();
                    return;
                }
                if (flightDuration <= 0 && toTarget.length() < birdData.flightLandingReachDistance()) {
                    bird.getFlyingController().extendCruiseAfterUnsafeLanding();
                    return;
                }
            } else if (toTarget.length() < birdData.flightTargetReachDistance() || --hoverRetargetTicks <= 0) {
                bird.getFlyingController().retargetAirCruise(bird.getFlyingController().isEscapeFlightActive);
                toTarget = bird.getFlyingController().flightTarget.subtract(bird.position());
                horizontalDistance = Math.sqrt(toTarget.x * toTarget.x + toTarget.z * toTarget.z);
            }

            Vec3 direction = toTarget.length() > 1.0E-4 ? toTarget.normalize() : bird.getFlyingController().randomHorizontalDirection();
            Vec3 horizontalDirection = BirdFlightTargeting.normalizeHorizontal(
                    new Vec3(direction.x, 0, direction.z), bird.getDeltaMovement());

            if (!bird.getFlyingController().isLandingFlight) {
                Vec3 flockHeading = BirdFlightBoids.sameTypeHeading(
                        bird, birdData.flockRange(), birdData.flockSeparation(),
                        birdData.flockAlignment(), birdData.flockCohesion(), birdData.flockWeightEscape(),
                        bird.getFlyingController().isEscapeFlightActive ? birdData.flockEscapeWeight() : birdData.flockAmbientWeight());
                if (flockHeading.length() > 1.0E-4) {
                    horizontalDirection = BirdFlightTargeting.normalizeHorizontal(
                            horizontalDirection.add(flockHeading), horizontalDirection);
                }
            }

            double speed = getSpeed(horizontalDistance);

            Vec3 desired = getDesired(toTarget, horizontalDirection, speed);
            Vec3 movement = bird.getDeltaMovement().scale(birdData.flightMovementScale())
                    .add(desired.scale(birdData.flightDesiredScale()));

            if (!bird.getFlyingController().isLandingFlight && BirdFlightController.isStalledInAir(bird, flyingTime, birdData.flightStalledThreshold())) {
                bird.getFlyingController().retargetAirCruise(bird.getFlyingController().isEscapeFlightActive);
                movement = horizontalDirection.scale(Math.max(speed, birdData.flightStalledMinSpeed()))
                        .add(0, birdData.flightStalledVerticalBoost(), 0);
            }

            bird.setDeltaMovement(movement);
            bird.getFlyingController().faceFlightDirection(movement);
            bird.hasImpulse = true;
        }
    }

    /**
     * 根据飞行目标计算期望移动向量。
     *
     * @param toTarget 到目标位置的方向向量
     * @param horizontalDirection 水平方向
     * @param speed 当前飞行速度
     * @return 期望移动速度向量
     */
    private @NotNull Vec3 getDesired(Vec3 toTarget, Vec3 horizontalDirection, double speed) {
        BirdData birdData = bird.getBirdData();
        double hoverBob = bird.getFlyingController().isLandingFlight ? birdData.flightLandingHoverBob()
                : Math.sin((bird.tickCount + bird.getId()) * birdData.flightHoverBobFrequency()) * birdData.flightHoverBobAmplitude();
        double vertical = bird.getFlyingController().isLandingFlight
                ? Mth.clamp(toTarget.y * birdData.flightVerticalLandingFactor() + birdData.flightLandingHoverBob(),
                birdData.flightVerticalClampMin(), birdData.flightVerticalClampMax())
                : Mth.clamp(toTarget.y * birdData.flightVerticalAmbientFactor() + hoverBob,
                birdData.flightVerticalAmbientMin(), birdData.flightVerticalAmbientMax());

        return new Vec3(horizontalDirection.x * speed, vertical, horizontalDirection.z * speed);
    }

    /**
     * 获取当前飞行速度。
     *
     * <p>根据飞行类型（逃离、巡航、降落）
     * 使用不同速度参数。</p>
     *
     * @param horizontalDistance 与目标的水平距离
     * @return 当前飞行速度
     */
    private double getSpeed(double horizontalDistance) {
        BirdData birdData = bird.getBirdData();
        double speed = bird.getFlyingController().isEscapeFlightActive ? birdData.flightEscapeSpeed()
                : (bird.getFlyingController().isLandingFlight ? birdData.flightLandingSpeed() : birdData.flightAmbientSpeed());
        if (bird.getFlyingController().isLandingFlight) {
            speed = BirdFlightController.decelerateNearLanding(speed, horizontalDistance,
                    birdData.flightLandingDecelDistance(), birdData.flightLandingDecelFactor());
        }
        return speed;
    }

    /**
     * 尝试启动自然空中巡航行为。
     *
     * <p>根据鸟种配置概率，
     * 随机触发短距离飞行。</p>
     */
    private void tickAmbientAirCruise() {
        if (bird.getFlyingController().canStartAmbientAirCruise()) {
            BirdData birdData = bird.getBirdData();
            int chance = bird.isTame() ? birdData.ambientAirCruiseChanceTame() : birdData.ambientAirCruiseChanceWild();
            if (bird.getRandom().nextInt(chance) == 0) {
                bird.getFlyingController().startShortFlight(bird.getFlyingController().findAirCruiseTarget(false), false);
            }
        }
    }

    /**
     * 修正默认行为状态。
     *
     * <p>当没有特殊行为占用时，
     * 根据环境、驯服状态和移动情况选择：</p>
     *
     * <ul>
     *     <li>睡眠</li>
     *     <li>跟随主人</li>
     *     <li>行走</li>
     *     <li>空闲</li>
     * </ul>
     */
    private void tickBehaviorFallback() {
        if (behaviorStateLockTicks <= 0 && postTameActionTicks <= 0
                && !bird.getEatingController().isEating() && !bird.isPassenger()) {
            if (bird.getRoutineController().isRoostTime() && bird.onGround() && bird.getNavigation().isDone()) {
                bird.getBehaviorStateController().setBehaviorState(BirdBehaviorState.SLEEPING);
            } else {
                BirdBehaviorState state = bird.getBehaviorStateController().getBehaviorState();
                if (state != BirdBehaviorState.FLEEING && state != BirdBehaviorState.FLYING) {
                    BirdData birdData = bird.getBirdData();
                    if (bird.isTame() && bird.getOwner() != null && !bird.getNavigation().isDone()
                            && bird.distanceToSqr(bird.getOwner()) > birdData.followingDistanceThreshold()) {
                        bird.getBehaviorStateController().setBehaviorState(BirdBehaviorState.FOLLOWING);
                    } else if (!(bird.getDeltaMovement().lengthSqr() > birdData.walkingSpeedThreshold())
                            && bird.getNavigation().isDone()) {
                        if (state == BirdBehaviorState.WALKING || state == BirdBehaviorState.FORAGING
                                || state == BirdBehaviorState.FOLLOWING || state == BirdBehaviorState.ALERT) {
                            bird.getBehaviorStateController().setBehaviorState(BirdBehaviorState.IDLE);
                        }
                    } else {
                        bird.getBehaviorStateController().setBehaviorState(BirdBehaviorState.WALKING);
                    }
                } else {
                    bird.getBehaviorStateController().setBehaviorState(BirdBehaviorState.ALERT);
                }
            }
        }
    }

    /**
     * 同步地面移动方向。
     *
     * <p>根据移动速度调整鸟实体朝向，
     * 保持模型旋转与移动方向一致。</p>
     */
    private void tickGroundMovementFacing() {
        if (bird.getFlyingController().shouldFaceGroundMovement()) {
            @SuppressWarnings("unused")
            boolean unused = BirdFlightController.faceGroundMovement(bird, bird.getDeltaMovement(), 1.0E-4);
        }
    }

    /**
     * 增加鸟的信任值。
     *
     * <p>增加后的值会限制在当前鸟种允许范围内。</p>
     *
     * @param amount 增加的信任值
     */
    public void addTrust(int amount) {
        trustTicks = Mth.clamp(trustTicks + amount, 0, bird.getBirdData().trustTicksLimit());
    }
}
