package net.fodoth.skina.neoguanniao.client.cage;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.BirdBehaviorState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

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
