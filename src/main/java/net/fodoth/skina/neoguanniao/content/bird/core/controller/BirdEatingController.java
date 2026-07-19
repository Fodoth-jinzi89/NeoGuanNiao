package net.fodoth.skina.neoguanniao.content.bird.core.controller;

import net.fodoth.skina.neoguanniao.content.bath.BirdBathBlockEntity;
import net.fodoth.skina.neoguanniao.content.bath.BirdBathContentType;
import net.fodoth.skina.neoguanniao.content.bird.core.BirdBehaviorState;
import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.data.BirdData;
import net.fodoth.skina.neoguanniao.content.bird.core.data.datum.BirdEatingDatum;
import net.fodoth.skina.neoguanniao.content.bird.core.data.datum.BirdMiscDatum;
import net.fodoth.skina.neoguanniao.content.bird.core.data.datum.BirdTameDatum;
import net.fodoth.skina.neoguanniao.content.bird.impl.neo.budgerigar.BudgerigarEntity;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoItemTags;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * 鸟类进食控制器
 * <p>
 * 负责处理鸟类进食、喂食、信任值增长以及驯服相关逻辑。
 * </p>
 */
public class BirdEatingController<T extends AbstractBirdEntity<T>> extends AbstractBirdController<T> {


    /**
     * 判断鸟类当前是否处于进食状态
     *
     * @return 如果正在进食返回 true
     */
    public boolean isEating() {
        BirdBehaviorState currentState = bird.getBehaviorStateController().getBehaviorState();
        return currentState == BirdBehaviorState.EATING;
    }

    public boolean isForagingOrEating() {
        BirdBehaviorState currentState = bird.getBehaviorStateController().getBehaviorState();
        return currentState == BirdBehaviorState.EATING || currentState == BirdBehaviorState.FORAGING;
    }

    /**
     * 处理玩家喂食交互
     *
     * @param player 玩家
     * @param hand   交互使用的手
     * @return 交互结果
     */
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        // 获取玩家手中的物品
        ItemStack stack = player.getItemInHand(hand);


        // 优先检查繁育
        InteractionResult breedResult =
                bird().getBreedController()
                        .mobInteract(player, hand);

        if (breedResult.consumesAction()) {
            return breedResult;
        }

        // 检查物品是否可食用
        // 用 CONSUME 防止客户端看不到
        if (!isEdibleFood(stack)) {
            return InteractionResult.CONSUME;
        }

        // 如果正在进食，阻止重复交互
        if (isEating()) {
            return InteractionResult.CONSUME;
        }

        // 鹦鹉跳舞也不吃东西
        if (bird() instanceof BudgerigarEntity budgerigar) {
            if (budgerigar.isBusyWithMusicOrSleep()) {
                return InteractionResult.CONSUME;
            }
        }

        // 睡觉不吃东西
        if (bird().getRoutineController().isSleepingOrRoosting()) {
            return InteractionResult.CONSUME;
        }

        // 客户端处理
        if (bird().level().isClientSide) {
            return InteractionResult.sidedSuccess(true);
        }


        // 复制一份食物用于后续处理
        ItemStack eaten = stack.copy();
        eaten.setCount(1);

        // 非创造模式下减少物品数量
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        // 获取鸟类数据
        BirdData data = bird().getBirdData();
        BirdTameDatum tameDatum = data.tame();

        // 检查驯服逻辑
        bird().getTameController().checkTame(
                player,
                eaten,
                tameDatum.addTrustValue(),
                tameDatum.addTrustNearbyValue()
        );

        // 开始进食动画
        startEatingFood(eaten);

        return InteractionResult.SUCCESS;
    }

    /**
     * 消耗掉落物作为食物
     *
     * @param bird       鸟类实体
     * @param itemEntity 掉落物实体
     */
    public void consumeItemEntity(AbstractBirdEntity<?> bird, ItemEntity itemEntity) {
        // 获取掉落物中的物品
        ItemStack stack = itemEntity.getItem();

        // 复制一份食物用于后续处理
        ItemStack eaten = stack.copy();
        eaten.setCount(1);

        // 减少掉落物数量
        stack.shrink(1);

        // 如果掉落物为空则移除，否则更新
        if (stack.isEmpty()) {
            itemEntity.discard();
        } else {
            itemEntity.setItem(stack);
        }

        // 获取鸟类数据
        BirdData data = bird().getBirdData();
        BirdTameDatum tameDatum = data.tame();
        BirdMiscDatum miscDatum = data.misc();

        // 获取计时器控制器
        var tickController = bird().getTickController();
        var timer = tickController.getTickTimer();

        // ========== 新增：食物飞向鸟类动画 ==========
        spawnFlyingFood(bird, itemEntity, eaten);

        // 开始进食动画
        startEatingFood(eaten);

        // 增加信任值（使用掉落物信任倍率）
        int trustAmount = (int) (tameDatum.addTrustValue() * miscDatum.droppedItemTrustMultiplier());
        timer.getBirdTrustTicker().addTrust(trustAmount);

        // 设置好奇计时器（使用掉落物好奇时长限制）
        int currentCuriousTicks = timer.getBirdCuriousTicker().getTicks();
        int curiousLimit = data.eating().curiousTicksLimitForDroppedFood();
        timer.getBirdCuriousTicker().setTicks(Math.max(currentCuriousTicks, curiousLimit));

        // 向附近同类分享信任值
        int nearbyTrustAmount = (int) (tameDatum.addTrustNearbyValue() * miscDatum.droppedItemTrustMultiplier());
        shareTrustNearby(nearbyTrustAmount);
    }

    private void spawnFlyingFood(AbstractBirdEntity<?> bird, ItemEntity itemEntity, ItemStack food) {
        ItemStack flyingStack = food.copy();
        // 添加唯一标记，防止和普通掉落物合并
        flyingStack.set(
                DataComponents.CUSTOM_DATA,
                CustomData.of(new CompoundTag() {{
                    putBoolean("NeoGuanNiaoFlyingFood", true);
                }})
        );

        ItemEntity flyingFood = new ItemEntity(
                bird.level(),
                itemEntity.getX(),
                itemEntity.getY(),
                itemEntity.getZ(),
                flyingStack
        );
        flyingFood.setNeverPickUp();
        flyingFood.lifespan = 10;
        double dx = bird.getX() - flyingFood.getX();
        double dy = bird.getY() + 0.5 * bird.getBbHeight() - flyingFood.getY();
        double dz = bird.getZ() - flyingFood.getZ();
        double speed = 0.18;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (distance > 0) {
            flyingFood.setDeltaMovement((dx / distance) * speed, (dy / distance) * speed, (dz / distance) * speed);
        }
        bird.level().addFreshEntity(flyingFood);
    }

    /**
     * 向附近同类鸟类共享信任值
     *
     * @param amount 增加的信任值
     */
    public void shareTrustNearby(int amount) {
        // 获取鸟类数据
        BirdData birdData = bird().getBirdData();
        BirdMiscDatum miscDatum = birdData.misc();


        // 获取范围内的所有鸟类实体
        double range = miscDatum.trustShareRange();
        var entities = bird().level().getEntitiesOfClass(
                AbstractBirdEntity.class,
                bird().getBoundingBox().inflate(range)
        );

        // 遍历范围内的鸟类
        for (AbstractBirdEntity<?> b : entities) {
            // 排除自身
            if (b == bird()) {
                continue;
            }

            // 获取目标鸟类的计时器
            var targetTimer = b.getTickController().getTickTimer();

            // 增加信任值
            targetTimer.getBirdTrustTicker().addTrust(amount);

            // 设置好奇计时器
            int currentCuriousTicks = targetTimer.getBirdCuriousTicker().getTicks();
            int curiousLimit = miscDatum.curiousTicksLimitForSharedTrust();
            targetTimer.getBirdCuriousTicker().setTicks(Math.max(currentCuriousTicks, curiousLimit));
        }
    }

    /**
     * 开始普通进食行为
     *
     * @param foodStack 食物物品
     */
    public void startEatingFood(ItemStack foodStack) {
        // 获取鸟类数据
        BirdData birdData = bird().getBirdData();
        BirdEatingDatum eatingDatum = birdData.eating();

        // 计算进食参数（加上随机变化）
        int eatingTicks = eatingDatum.eatingTicks() + bird().getRandom().nextInt(eatingDatum.eatingTicksVariant());
        int foodTicks = eatingDatum.foodTicks() + bird().getRandom().nextInt(eatingDatum.foodTicksVariant());
        float eatAmount = eatingDatum.eatAmount();
        float volume = eatingDatum.eatSoundVolume() + bird().getRandom().nextFloat() * eatingDatum.eatSoundVolumeVariant();
        float pitch = eatingDatum.eatSoundPitch() + bird().getRandom().nextFloat() * eatingDatum.eatSoundPitchVariant();

        // 开始进食行为
        startEatingBehavior(eatingTicks, foodTicks, eatAmount, volume, pitch);
    }

    /**
     * 消耗鸟浴盆中的一份食物
     *
     * @param bath        鸟浴盆方块实体
     * @param contentType 食物类型
     */
    public void consumeBirdBathServing(BirdBathBlockEntity bath, BirdBathContentType contentType) {
        // 获取鸟类数据
        BirdData birdData = bird().getBirdData();
        BirdEatingDatum eatingDatum = birdData.eating();

        // 计算进食参数（加上随机变化）
        int eatingTicks = eatingDatum.eatingTicks() + bird().getRandom().nextInt(eatingDatum.eatingTicksVariant());
        int foodTicks = eatingDatum.foodTicks() + bird().getRandom().nextInt(eatingDatum.foodTicksVariant());
        float eatAmount = eatingDatum.eatAmount();
        float volume = eatingDatum.eatSoundVolume() + bird().getRandom().nextFloat() * eatingDatum.eatSoundVolumeVariant();
        float pitch = eatingDatum.eatSoundPitch() + bird().getRandom().nextFloat() * eatingDatum.eatSoundPitchVariant();
        float multiplier = eatingDatum.eatBathMultiplier();

        // 根据内容类型开始进食
        startEatingBehavior(eatingTicks, foodTicks, eatAmount, volume, pitch, multiplier);
    }

    /**
     * 结束当前进食状态
     */
    public void clearEating() {
        // 获取行为状态控制器
        var behaviorController = bird.getBehaviorStateController();

        // 如果当前状态为进食，根据大脑决策切换到合适状态
        if (behaviorController.getBehaviorState() == BirdBehaviorState.EATING) {
            boolean wantsForage = bird().getBirdBrain().wantsForage();
            BirdBehaviorState nextState = wantsForage ? BirdBehaviorState.FORAGING : BirdBehaviorState.IDLE;
            behaviorController.setBehaviorState(nextState);
        }
    }

    /**
     * 通用进食行为（使用默认倍率1.0）
     *
     * @param eatingTicks 进食持续刻数
     * @param foodTicks   食物效果持续刻数
     * @param eatAmount   进食恢复量/效果系数
     * @param volume      声音音量
     * @param pitch       声音音调
     */
    private void startEatingBehavior(int eatingTicks, int foodTicks, float eatAmount, float volume, float pitch) {
        startEatingBehavior(eatingTicks, foodTicks, eatAmount, volume, pitch, 1.0F);
    }

    /**
     * 通用进食行为（支持全局倍率调整）
     *
     * @param eatingTicks 进食持续刻数
     * @param foodTicks   食物效果持续刻数
     * @param eatAmount   进食恢复量/效果系数
     * @param volume      声音音量
     * @param pitch       声音音调
     * @param multiplier  全局倍率（影响所有数值参数）
     */
    private void startEatingBehavior(int eatingTicks, int foodTicks, float eatAmount, float volume, float pitch, float multiplier) {
        // 获取计时器控制器
        var tickController = bird().getTickController();
        var timer = tickController.getTickTimer();

        // 计算应用倍率后的值
        int finalEatingTicks = (int) (eatingTicks * multiplier);
        int finalFoodTicks = (int) (foodTicks * multiplier);
        float finalEatAmount = eatAmount * multiplier;
        float finalVolume = volume * multiplier;
        float finalPitch = pitch * multiplier;

        // 停止导航移动
        bird().getNavigation().stop();

        // 设置进食计时器和食物效果计时器
        timer.getBirdEatingTicker().setTicks(finalEatingTicks);
        timer.getBirdFoodTicker().setTicks(finalFoodTicks);

        // 使用行为状态控制器设置状态并锁定
        bird.getBehaviorStateController().setBehaviorStateFor(
                BirdBehaviorState.EATING,
                finalEatingTicks
        );

        // 通知大脑进食事件
        bird().getBirdBrain().onEat(finalEatAmount);

        // 播放进食音效
        bird().playSound(bird().getBirdData().sound().eatSound(), finalVolume, finalPitch);
    }

    /**
     * 检查物品是否为鸟类可食用食物
     *
     * @param stack 物品堆
     * @return 是否为可食用食物
     */
    public boolean isEdibleFood(ItemStack stack) {

        return stack.is(NeoGuanNiaoItemTags.BIRD_FOOD);
    }

    /**
     * 开始鸟浴盆进食或饮水动画
     *
     * @param contentType 鸟浴盆内容类型
     * @param ticks       动画持续 Tick 数
     */
    public void startBirdBathFeedingAnimation(BirdBathContentType contentType, int ticks) {
        // 获取行为状态控制器
        var behaviorController = bird.getBehaviorStateController();

        // 停止导航移动
        bird().getNavigation().stop();

        // 获取鸟类数据
        BirdData birdData = bird().getBirdData();
        BirdEatingDatum eatingDatum = birdData.eating();

        // 根据内容类型设置不同的行为状态
        if (contentType.isFood()) {
            // 食物类型：设置为进食状态
            behaviorController.setBehaviorStateFor(
                    BirdBehaviorState.EATING,
                    Math.max(eatingDatum.eatingTicksLimitForBath(), ticks)
            );
        } else {
            // 非食物类型（如饮水）：设置为好奇状态
            int curiousLimit = Math.max(birdData.misc().curiousTicksLimitForBath(), ticks / 2);
            behaviorController.setBehaviorStateFor(
                    BirdBehaviorState.CURIOUS,
                    curiousLimit
            );
        }
    }
}