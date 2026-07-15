package net.fodoth.skina.neoguanniao.content.bird.core.controller;

import net.fodoth.skina.neoguanniao.content.bird.core.BirdBehaviorState;
import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.data.BirdData;
import net.fodoth.skina.neoguanniao.content.bird.core.data.datum.BirdMiscDatum;
import net.fodoth.skina.neoguanniao.content.bird.core.data.datum.BirdTameDatum;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

/**
 * 鸟类驯服控制器
 * <p>
 * 负责处理鸟实体的驯服流程，包括：
 * <ul>
 *     <li>记录当前尝试驯服的玩家</li>
 *     <li>增加玩家对鸟的信任值</li>
 *     <li>检查并更新驯服状态</li>
 *     <li>同步驯服成功与失败事件</li>
 *     <li>执行驯服成功后的庆祝行为</li>
 * </ul>
 * 驯服机制基于信任值系统。
 * 玩家通过投喂增加鸟的信任值，当信任值达到鸟种设定阈值后，
 * 鸟将被玩家驯服并建立主人关系。
 * </p>
 */
public class BirdTameController<T extends AbstractBirdEntity<T>> extends AbstractBirdController<T>{

    /**
     * 驯服失败事件 ID，用于通过实体事件同步客户端表现
     */
    private static final byte EVENT_TAME_FAILED = 6;

    /**
     * 驯服成功事件 ID，用于通过实体事件同步客户端表现
     */
    private static final byte EVENT_TAME_SUCCESS = 7;

    /**
     * 当前正在尝试驯服该鸟的玩家 UUID
     * <p>
     * 用于记录最近一次参与驯服交互的玩家，
     * 例如驯服成功后的视线追踪和庆祝行为。
     * </p>
     */
    private UUID interestedPlayerUUID;

    /**
     * 广播驯服结果事件
     * <p>
     * 服务端通过实体事件通知客户端，
     * 由客户端执行对应的驯服表现。
     * </p>
     *
     * @param tame 是否驯服成功
     */
    public void broadcastTameEvent(boolean tame) {
        byte event = tame ? EVENT_TAME_SUCCESS : EVENT_TAME_FAILED;
        bird.level().broadcastEntityEvent(bird, event);
    }

    /**
     * 处理客户端收到的驯服事件
     * <p>
     * 根据事件 ID 更新客户端实体的驯服状态。
     * </p>
     *
     * @param id 实体事件 ID
     */
    public void handleTameEvent(byte id) {
        if (id == EVENT_TAME_SUCCESS) {
            bird.setTame(true, true);
        } else if (id == EVENT_TAME_FAILED) {
            bird.setTame(false, true);
        }
    }

    /**
     * 检查并执行驯服流程
     * <p>
     * 该方法会：
     * <ol>
     *     <li>记录交互玩家</li>
     *     <li>播放进食行为</li>
     *     <li>增加信任值</li>
     *     <li>同步附近鸟类获得的信任值</li>
     *     <li>检查是否达到驯服条件</li>
     *     <li>触发成功或失败事件</li>
     * </ol>
     * </p>
     *
     * @param player         当前交互玩家
     * @param eaten          用于驯服的食物
     * @param addTrust       增加的信任值
     * @param addTrustNearby 附近鸟类获得的额外信任值
     */
    public void checkTame(Player player, ItemStack eaten, int addTrust, int addTrustNearby) {
        var tickController = bird.getTickController();
        var timer = tickController.getTickTimer();
        var eatingController = bird.getEatingController();
        BirdData birdData = bird.getbirdData();
        BirdMiscDatum miscDatum = birdData.misc();

        boolean wasTame = bird.isTame();

        // 记录当前交互玩家
        setInterestedPlayerUUID(player.getUUID());

        // 开始进食
        eatingController.startEatingFood(eaten);

        // 增加信任值
        timer.getBirdTrustTicker().addTrust(addTrust);

        // 设置好奇计时器
        int currentCuriousTicks = timer.getBirdCuriousTicker().getTicks();
        int curiousLimit = miscDatum.curiousTicksLimitForTame();
        timer.getBirdCuriousTicker().setTicks(Math.max(currentCuriousTicks, curiousLimit));

        // 向附近鸟类分享信任值
        eatingController.shareTrustNearby(addTrustNearby);

        // 更新驯服状态和主人信息
        updateTrustedOwner(player);

        // 触发驯服成功或失败事件
        if (!wasTame && bird.isTame()) {
            startTameCelebration(player);
            triggerTameSideEffects(player);
            broadcastTameEvent(true);
        } else if (!wasTame) {
            broadcastTameEvent(false);
        }
    }

    public void triggerTameSideEffects(Player player) {
    }

    /**
     * 设置当前感兴趣玩家
     *
     * @param interestedPlayerUUID 玩家 UUID
     */
    public void setInterestedPlayerUUID(UUID interestedPlayerUUID) {
        this.interestedPlayerUUID = interestedPlayerUUID;
    }

    /**
     * 获取当前感兴趣玩家 UUID
     *
     * @return 玩家 UUID，可能为 {@code null}
     */
    public UUID getInterestedPlayerUUID() {
        return interestedPlayerUUID;
    }

    /**
     * 更新鸟的驯服状态和主人信息
     * <p>
     * 当信任值达到驯服阈值时执行驯服；
     * 如果已经驯服但尚未设置主人，则补充主人 UUID。
     * </p>
     *
     * @param player 当前驯服玩家
     */
    public void updateTrustedOwner(Player player) {
        var timer = bird.getTickController().getTickTimer();
        BirdData birdData = bird.getbirdData();
        BirdTameDatum tameDatum = birdData.tame();

        // 检查信任值是否达到驯服阈值
        int trustTicks = timer.getBirdTrustTicker().getTicks();
        int tameThreshold = tameDatum.trustTameThreshold();

        if (!bird.isTame() && trustTicks >= tameThreshold) {
            bird.tame(player);
        } else if (bird.isTame() && bird.getOwner() == null) {
            bird.setOwnerUUID(player.getUUID());
        }
    }

    /**
     * 开始驯服成功后的庆祝行为
     * <p>
     * 清理当前进食状态，停止移动，
     * 并根据鸟种配置进入短暂的好奇庆祝状态。
     * </p>
     *
     * @param player 驯服玩家
     */
    public void startTameCelebration(Player player) {
        var tickController = bird.getTickController();
        var timer = tickController.getTickTimer();
        var eatingController = bird.getEatingController();
        var stateController = bird.getBehaviorStateController();
        BirdData birdData = bird.getbirdData();
        BirdTameDatum tameDatum = birdData.tame();
        var random = bird.getRandom();

        // 清理进食状态并停止移动
        eatingController.clearEating();
        bird.getNavigation().stop();

        // 计算庆祝行为持续时长
        int postTameActionTicks = tameDatum.tameCelebrationPostTameActionTicksMin()
                + random.nextInt(tameDatum.tameCelebrationPostTameActionTicksVariance());

        // 设置各种计时器
        timer.getBirdPostTameActionTicker().setTicks(postTameActionTicks);
        timer.getBirdPostTameActionSwapTicker().setTicks(tameDatum.tameCelebrationPostTameActionSwapTicks());

        // 更新好奇计时器
        int currentCuriousTicks = timer.getBirdCuriousTicker().getTicks();
        int curiousLimit = tameDatum.tameCelebrationCuriousTicks();
        timer.getBirdCuriousTicker().setTicks(Math.max(currentCuriousTicks, curiousLimit));

        // 重置空闲动画计时器
        timer.getBirdIdleAnimationTicker().setTicks(0);

        // 更新食物效果计时器
        int currentFoodTicks = timer.getBirdFoodTicker().getTicks();
        int foodTicks = tameDatum.tameCelebrationFoodTicks();
        timer.getBirdFoodTicker().setTicks(Math.max(currentFoodTicks, foodTicks));

        // 设置为好奇状态
        int behaviorTicks = tameDatum.tameCelebrationBehaviorStateTicks();
        stateController.setBehaviorStateFor(BirdBehaviorState.CURIOUS, behaviorTicks);

        // 看向玩家
        bird.getLookControl().setLookAt(player, 35.0F, 35.0F);
    }
}