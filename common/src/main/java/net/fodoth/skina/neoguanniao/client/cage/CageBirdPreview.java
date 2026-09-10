package net.fodoth.skina.neoguanniao.client.cage;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.BirdBehaviorState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * 鸟笼中预览实体的缓存与行为驱动。
 *
 * <p>
 * 笼中的鸟不会运行真实的 goal，这里按游戏 tick 复刻 idle、curious、roost and sleep
 * 与 wake up goal 的关键行为，使笼中鸟播放与野外一致的动画：活动时间播放各种待机动画
 * 并偶尔理毛、好奇，休息时间进入 sleep 动画再进入 sleep_loop，回到活动时间后从
 * sleep_loop 转回 idle。
 * </p>
 */
final class CageBirdPreview {

    /**
     * 待机动画播完一轮后再按 1/N 的概率好奇一次
     */
    private static final int CURIOUS_CHANCE = 4;

    private CompoundTag tag;
    private Entity entity;
    private long tickedAt = Long.MIN_VALUE;

    /**
     * 获取与给定 NBT 对应的预览实体，NBT 变化时重新创建。
     *
     * @return 预览实体，创建失败时为 null
     */
    Entity entity(Level level, CompoundTag tag) {
        if (this.entity == null || !tag.equals(this.tag)) {
            Entity created = EntityType.create(tag, level).orElse(null);
            this.entity = created;
            this.tag = created == null ? null : tag.copy();
            this.tickedAt = Long.MIN_VALUE;
            if (created instanceof AbstractBirdEntity<?> bird) {
                // 预览实体不会被 tick，捕捉时存档下来的残余 Motion 永远不会衰减，
                // 会让鸟一直被判定为“移动中”而播放行走/奔跑动画，这里直接让它静止。
                created.setDeltaMovement(0.0D, 0.0D, 0.0D);
                // 笼中鸟不再与玩家互动，清掉捕捉时残留的好奇计时，避免一直播放好奇动画。
                bird.getTickController().getTickTimer().getBirdCuriousTicker().setTicks(0);
            }
        }
        return this.entity;
    }

    /**
     * 按游戏 tick 推进预览鸟自身的计时器与行为状态，同一 tick 内重复调用（重渲染）不会重复推进。
     */
    void tick(Level level) {
        tick(level, null);
    }

    /**
     * 同上，并让笼中鸟在 {@code soundAnchor} 处像笼外的鸟一样鸣叫。
     *
     * @param soundAnchor 鸣叫的声源位置，{@code null} 表示没有世界位置（例如物品栏、手中的鸟笼）而不发声
     */
    void tick(Level level, @Nullable Vec3 soundAnchor) {
        if (!(this.entity instanceof AbstractBirdEntity<?> bird)) {
            return;
        }
        long gameTime = level.getGameTime();
        if (this.tickedAt == gameTime) {
            return;
        }
        this.tickedAt = gameTime;

        bird.tickCount = (int) gameTime;
        // 推进待机动画等客户端计时器，待机动画会像真实鸟一样按自己的节奏更换。
        bird.getTickController().tickClient();

        if (soundAnchor != null) {
            // 预览实体不在世界里，声源位置得自己摆到鸟笼上，否则会从捕捉时记录的坐标发声。
            bird.setPos(soundAnchor.x, soundAnchor.y, soundAnchor.z);
            // 预览实体不会被 tick，Mob.baseTick 里的环境音判定不会执行；这里按同样的规则补一次：
            // 计时每刻 +1，命中后按 interval 复位并播放环境音，鸣叫频率与笼外的鸟一致。
            if (bird.getRandom().nextInt(1000) < bird.ambientSoundTime++) {
                bird.ambientSoundTime = -bird.getAmbientSoundInterval();
                bird.playAmbientSound();
            }
        }

        var stateController = bird.getBehaviorStateController();
        var routineController = bird.getRoutineController();

        // 休息时间：等价于 roost and sleep goal，笼中鸟无法飞到栖息点，直接入睡。
        if (!routineController.isActiveTime()) {
            stateController.setBehaviorState(BirdBehaviorState.SLEEPING);
            return;
        }

        // 回到活动时间：等价于 wake up goal，从 sleep_loop 转回 idle。
        if (routineController.isSleeping()) {
            stateController.setBehaviorState(BirdBehaviorState.IDLE);
            return;
        }

        var timer = bird.getTickController().getTickTimer();

        // 好奇结束：等价于 curious follow goal 的 onStop。
        if (stateController.getBehaviorState() == BirdBehaviorState.CURIOUS) {
            if (!timer.getBirdBehaviorStateTicker().isRunning()) {
                stateController.setBehaviorState(BirdBehaviorState.IDLE);
            }
            return;
        }

        // 偶尔好奇：等价于 curious follow goal，只是笼中鸟无法跟过去，只能张望。
        if (stateController.getBehaviorState() == BirdBehaviorState.IDLE
                && timer.getBirdIdleAnimationTicker().getTicks() <= 0
                && bird.getRandom().nextInt(CURIOUS_CHANCE) == 0) {
            var goalDatum = bird.getBirdData().goal();
            stateController.setBehaviorStateFor(BirdBehaviorState.CURIOUS,
                    goalDatum.curiousTicks() + bird.getRandom().nextInt(goalDatum.curiousTicksVariance()));
        }
    }
}
