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
 * 鸟类受惊控制器。
 * <p>
 * 负责处理鸟类受惊、逃跑、警戒以及惊吓传播等行为。
 */
public class BirdFrightController {

    public Vec3 frightSource;
    public Vec3 pendingFrightSource;
    private final AbstractBirdEntity<?> bird;

    /**
     * 创建鸟类受惊控制器。
     *
     * @param bird 鸟类实体
     */
    public BirdFrightController(AbstractBirdEntity<?> bird) {
        this.bird = bird;
    }


    /**
     * 处理受到伤害后的受惊行为。
     *
     * @param source 伤害来源
     */
    public void processHurt(@NotNull DamageSource source) {
        bird.getEatingController().clearEating();
        bird.getTameController().setInterestedPlayerUUID(null);

        Entity attacker = source.getEntity();

        Vec3 sourcePos = attacker == null
                ? bird.position()
                : attacker.position();

        boolean isPlayer = attacker instanceof Player;


        BirdData birdData = bird.getBirdData();
        bird.getBirdBrain().onFrightened(
                isPlayer
                        ? birdData.frightenedTrustLossPlayer()
                        : birdData.frightenedTrustLossOther()
        );


        bird.getNavigation().stop();


        bird.getBehaviorStateController().setBehaviorStateFor(
                BirdBehaviorState.ALERT,
                isPlayer
                        ? birdData.alertTicksPlayer()
                        : birdData.alertTicksOther()
        );


        if (!isPlayer) {
            queueFrightFrom(
                    sourcePos,
                    birdData.frightDelayMin()
                            + bird.getRandom()
                            .nextInt(birdData.frightDelayVariance())
            );
        }
    }

    /**
     * 判断鸟类当前是否应该逃跑。
     *
     * @return 如果应该逃跑返回 true
     */
    public boolean shouldFlee() {
        return bird.getTickController().externalFrightTicks > 0 && frightSource != null;
    }

    /**
     * 使鸟类受到惊吓。
     *
     * @param sourcePos 惊吓来源位置
     * @param ticks 受惊持续 Tick 数
     */
    public void frightenFrom(Vec3 sourcePos, int ticks) {
        frightSource = sourcePos;
        bird.getTickController().externalFrightTicks = Math.max(bird.getTickController().externalFrightTicks, ticks);
        bird.getTickController().pendingFrightTicks = 0;
        bird.getTickController().pendingFrightDuration = 0;
        pendingFrightSource = null;
        bird.getBehaviorStateController().setBehaviorStateFor(BirdBehaviorState.FLEEING, Math.min(bird.getBirdData().frightTicksLimit(), ticks));
        if (bird.getTickController().flightTicks <= 0 && bird.onGround()) {
            startEscapeFlight(sourcePos);
        }
    }

    /**
     * 开始逃跑飞行。
     *
     * @param sourcePos 惊吓来源位置
     */
    protected void startEscapeFlight(Vec3 sourcePos) {
        Vec3 away = bird.position().subtract(sourcePos);
        if (away.lengthSqr() < 0.01) {
            away = new Vec3(
                    bird.getRandom().nextDouble() - 0.5,
                    0,
                    bird.getRandom().nextDouble() - 0.5
            );
        }
        Vec3 direction = new Vec3(away.x, 0, away.z).normalize();
        BirdData birdData = bird.getBirdData();
        Vec3 target = bird.position()
                .add(direction.scale(
                        birdData.escapeFlightMinDistance() +
                                bird.getRandom().nextDouble() * birdData.escapeFlightDistanceVariance()
                ))
                .add(0,
                        birdData.escapeFlightMinHeight() +
                                bird.getRandom().nextDouble() * birdData.escapeFlightHeightVariance(),
                        0
                );
        bird.getFlyingController().startShortFlight(target, true);
    }

    /**
     * 向附近鸟类传播警戒状态。
     */
    public void alertNearbyBirds() {
        BirdData birdData = bird.getBirdData();
        for (AbstractBirdEntity<?> b : bird.level().getEntitiesOfClass(
                AbstractBirdEntity.class, bird.getBoundingBox().inflate(birdData.alertNearbyRange()))) {
            if (b != bird) {
                bird.getBirdBrain().onFrightened(birdData.frightenAmount());
                b.getBehaviorStateController().setBehaviorStateFor(BirdBehaviorState.ALERT, birdData.alertTicks() + b.getRandom().nextInt(birdData.alertTicksVariant()));
                b.getTickController().curiousTicks = Math.max(b.getTickController().curiousTicks, birdData.curiousTicksLimitForAlert());
            }
        }
    }

    /**
     * 延迟触发一次受惊行为。
     *
     * @param sourcePos 惊吓来源位置
     * @param delayTicks 延迟 Tick 数
     */
    public void queueFrightFrom(Vec3 sourcePos, int delayTicks) {
        if (bird.getEatingController().isEating()) {
            bird.getEatingController().clearEating();
        }
        pendingFrightSource = sourcePos;
        BirdData birdData = bird.getBirdData();
        bird.getTickController().pendingFrightDuration = Math.max(bird.getTickController().pendingFrightDuration, birdData.pendingFrightDurationLimit());
        if (bird.getTickController().pendingFrightTicks <= 0) {
            bird.getTickController().pendingFrightTicks = Math.max(1, delayTicks);
        } else {
            bird.getTickController().pendingFrightTicks = Math.clamp(delayTicks, 1, bird.getTickController().pendingFrightTicks);
        }
        bird.getBehaviorStateController().setBehaviorStateFor(BirdBehaviorState.ALERT, Math.min(birdData.pendingFrightTicksLimit(), bird.getTickController().pendingFrightTicks + 10));
    }

}