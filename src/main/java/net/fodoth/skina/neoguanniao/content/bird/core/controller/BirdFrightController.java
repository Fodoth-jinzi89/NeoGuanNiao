package net.fodoth.skina.neoguanniao.content.bird.core.controller;

import net.fodoth.skina.neoguanniao.content.bird.core.BirdBehaviorState;
import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.data.BirdData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * 鸟类受惊控制器
 * <p>
 * 负责处理鸟类受惊、逃跑、警戒以及惊吓传播等行为。
 * </p>
 */
public class BirdFrightController {

    public Vec3 frightSource;
    public Vec3 pendingFrightSource;
    private final AbstractBirdEntity<?> bird;

    /**
     * 创建鸟类受惊控制器
     *
     * @param bird 鸟类实体
     */
    public BirdFrightController(AbstractBirdEntity<?> bird) {
        this.bird = bird;
    }

    /**
     * 处理受到伤害后的受惊行为
     *
     * @param source 伤害来源
     */
    public void processHurt(@NotNull DamageSource source) {
        var tickController = bird.getTickController();
        var timer = tickController.getTickTimer();
        var stateController = bird.getBehaviorStateController();
        var eatingController = bird.getEatingController();
        var brain = bird.getBirdBrain();
        BirdData birdData = bird.getBirdData();

        // 清理进食状态
        eatingController.clearEating();
        bird.getTameController().setInterestedPlayerUUID(null);

        // 获取攻击者位置
        Entity attacker = source.getEntity();
        Vec3 sourcePos = attacker == null ? bird.position() : attacker.position();
        boolean isPlayer = attacker instanceof Player;

        // 处理受惊信任损失
        float trustLoss = isPlayer
                ? birdData.frightenedTrustLossPlayer()
                : birdData.frightenedTrustLossOther();
        brain.onFrightened(trustLoss);

        // 停止移动
        bird.getNavigation().stop();

        // 设置为警戒状态
        int alertTicks = isPlayer
                ? birdData.alertTicksPlayer()
                : birdData.alertTicksOther();
        stateController.setBehaviorStateFor(BirdBehaviorState.ALERT, alertTicks);

        // 非玩家攻击时延迟触发受惊
        if (!isPlayer) {
            int delayTicks = birdData.frightDelayMin()
                    + bird.getRandom().nextInt(birdData.frightDelayVariance());
            queueFrightFrom(sourcePos, delayTicks);
        }
    }

    /**
     * 判断鸟类当前是否应该逃跑
     *
     * @return 如果应该逃跑返回 true
     */
    public boolean shouldFlee() {
        var timer = bird.getTickController().getTickTimer();
        return timer.getBirdExternalFrightTicker().getTicks() > 0 && frightSource != null;
    }

    /**
     * 使鸟类受到惊吓
     *
     * @param sourcePos 惊吓来源位置
     * @param ticks     受惊持续 Tick 数
     */
    public void frightenFrom(Vec3 sourcePos, int ticks) {
        var tickController = bird.getTickController();
        var timer = tickController.getTickTimer();
        var stateController = bird.getBehaviorStateController();
        var flyingController = bird.getFlyingController();
        BirdData birdData = bird.getBirdData();

        // 设置受惊源
        frightSource = sourcePos;

        // 设置受惊计时器
        var externalTicker = timer.getBirdExternalFrightTicker();
        var pendingTicker = timer.getBirdPendingFrightTicker();
        int currentTicks = externalTicker.getTicks();
        externalTicker.setTicks(Math.max(currentTicks, ticks));

        // 清除待处理的受惊状态
        pendingTicker.setTicks(0);
        timer.getBirdPendingFrightTicker().pendingFrightDuration = 0;
        pendingFrightSource = null;

        // 设置逃跑状态
        int frightLimit = Math.min(birdData.frightTicksLimit(), ticks);
        stateController.setBehaviorStateFor(BirdBehaviorState.FLEEING, frightLimit);

        // 如果在地面且未飞行，立即开始逃跑飞行
        if (timer.getBirdFlyingTicker().getTicks() <= 0 && bird.onGround()) {
            startEscapeFlight(sourcePos);
        }
    }

    /**
     * 开始逃跑飞行
     *
     * @param sourcePos 惊吓来源位置
     */
    protected void startEscapeFlight(Vec3 sourcePos) {
        BirdData birdData = bird.getBirdData();

        // 计算远离来源的方向
        Vec3 away = bird.position().subtract(sourcePos);
        if (away.lengthSqr() < 0.01) {
            away = new Vec3(
                    bird.getRandom().nextDouble() - 0.5,
                    0,
                    bird.getRandom().nextDouble() - 0.5
            );
        }

        Vec3 direction = new Vec3(away.x, 0, away.z).normalize();

        // 计算逃跑目标位置
        double horizontalDistance = birdData.escapeFlightMinDistance()
                + bird.getRandom().nextDouble() * birdData.escapeFlightDistanceVariance();
        double verticalHeight = birdData.escapeFlightMinHeight()
                + bird.getRandom().nextDouble() * birdData.escapeFlightHeightVariance();

        Vec3 target = bird.position()
                .add(direction.scale(horizontalDistance))
                .add(0, verticalHeight, 0);

        // 开始短距离逃跑飞行
        bird.getFlyingController().startShortFlight(target, true);
    }

    /**
     * 向附近鸟类传播警戒状态
     */
    public void alertNearbyBirds() {
        var tickController = bird.getTickController();
        var timer = tickController.getTickTimer();
        BirdData birdData = bird.getBirdData();

        // 获取范围内的所有鸟类
        double range = birdData.alertNearbyRange();
        var entities = bird.level().getEntitiesOfClass(
                AbstractBirdEntity.class,
                bird.getBoundingBox().inflate(range)
        );

        for (AbstractBirdEntity<?> b : entities) {
            // 排除自身
            if (b == bird) {
                continue;
            }

            // 通知大脑受惊
            bird.getBirdBrain().onFrightened(birdData.frightenAmount());

            // 设置警戒状态
            int alertTicks = birdData.alertTicks() + b.getRandom().nextInt(birdData.alertTicksVariant());
            b.getBehaviorStateController().setBehaviorStateFor(BirdBehaviorState.ALERT, alertTicks);

            // 设置好奇计时器
            var targetTimer = b.getTickController().getTickTimer();
            int currentCuriousTicks = targetTimer.getBirdCuriousTicker().getTicks();
            int curiousLimit = birdData.curiousTicksLimitForAlert();
            targetTimer.getBirdCuriousTicker().setTicks(Math.max(currentCuriousTicks, curiousLimit));
        }
    }

    /**
     * 延迟触发一次受惊行为
     *
     * @param sourcePos  惊吓来源位置
     * @param delayTicks 延迟 Tick 数
     */
    public void queueFrightFrom(Vec3 sourcePos, int delayTicks) {
        var tickController = bird.getTickController();
        var timer = tickController.getTickTimer();
        var stateController = bird.getBehaviorStateController();
        var eatingController = bird.getEatingController();
        BirdData birdData = bird.getBirdData();

        // 如果正在进食，清除进食状态
        if (eatingController.isEating()) {
            eatingController.clearEating();
        }

        // 设置待处理的受惊来源
        pendingFrightSource = sourcePos;

        // 设置待处理受惊计时器
        var pendingTicker = timer.getBirdPendingFrightTicker();
        int currentPendingDuration = timer.getBirdPendingFrightTicker().pendingFrightDuration;
        int pendingDurationLimit = birdData.pendingFrightDurationLimit();
        timer.getBirdPendingFrightTicker().pendingFrightDuration = Math.max(currentPendingDuration, pendingDurationLimit);

        // 设置延迟时间
        if (pendingTicker.getTicks() <= 0) {
            pendingTicker.setTicks(Math.max(1, delayTicks));
        } else {
            int clampedDelay = Math.clamp(delayTicks, 1, pendingTicker.getTicks());
            pendingTicker.setTicks(Math.max(1, clampedDelay));
        }

        // 设置为警戒状态
        int alertLimit = Math.min(birdData.pendingFrightTicksLimit(), pendingTicker.getTicks() + 10);
        stateController.setBehaviorStateFor(BirdBehaviorState.ALERT, alertLimit);
    }
}