package net.fodoth.skina.neoguanniao.content.bird.core.controller;

import net.fodoth.skina.neoguanniao.content.bath.BirdBathBlockEntity;
import net.fodoth.skina.neoguanniao.content.bath.BirdBathContentType;
import net.fodoth.skina.neoguanniao.content.bird.core.BirdBehaviorState;
import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.data.BirdData;

import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoItemTags;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * 鸟类进食控制器。
 * <p>
 * 负责处理鸟类进食、喂食、信任值增长以及驯服相关逻辑。
 */
public record BirdEatingController(AbstractBirdEntity<?> bird) {

    /**
     * 判断鸟类当前是否处于进食状态。
     *
     * @return 如果正在进食返回 true
     */
    public boolean isEating() {
        return bird.getTickController().eatingTicks > 0
                || bird.getBehaviorStateController().getBehaviorState() == BirdBehaviorState.EATING;
    }


    /**
     * 处理玩家喂食交互。
     *
     * @param player 玩家
     * @param hand 交互使用的手
     * @return 交互结果
     */
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!isEdibleFood(stack)) {
            return null;
        }

        if (bird.level().isClientSide) {
            return InteractionResult.sidedSuccess(true);
        }

        if (isEating()) {
            return InteractionResult.SUCCESS;
        }

        ItemStack eaten = stack.copy();
        eaten.setCount(1);

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        BirdData data = bird.getBirdData();

        bird.getTameController().checkTame(
                player,
                eaten,
                data.addTrustValue(),
                data.addTrustNearbyValue()
        );

        startEatingFood(eaten);

        return InteractionResult.SUCCESS;
    }


    /**
     * 消耗掉落物作为食物。
     *
     * @param itemEntity 掉落物实体
     */
    public void consumeItemEntity(ItemEntity itemEntity) {
        ItemStack stack = itemEntity.getItem();

        ItemStack eaten = stack.copy();
        eaten.setCount(1);

        stack.shrink(1);

        if (stack.isEmpty()) {
            itemEntity.discard();
        } else {
            itemEntity.setItem(stack);
        }

        BirdData data = bird.getBirdData();

        startEatingFood(eaten);

        bird.getTickController().addTrust(
                (int) (data.addTrustValue()
                        * data.droppedItemTrustMultiplier())
        );

        bird.getTickController().curiousTicks =
                Math.max(
                        bird.getTickController().curiousTicks,
                        data.curiousTicksLimitForDroppedFood()
                );

        shareTrustNearby(
                (int) (data.addTrustNearbyValue()
                        * data.droppedItemTrustMultiplier())
        );
    }


    /**
     * 向附近同类鸟类共享信任值。
     *
     * @param amount 增加的信任值
     */
    public void shareTrustNearby(int amount) {
        BirdData birdData = bird.getBirdData();
        for (AbstractBirdEntity<?> b : bird.level().getEntitiesOfClass(
                AbstractBirdEntity.class, bird.getBoundingBox().inflate(birdData.trustShareRange()))) {
            if (b != bird) {
                b.getTickController().addTrust(amount);
                b.getTickController().curiousTicks = Math.max(b.getTickController().curiousTicks, birdData.curiousTicksLimitForSharedTrust());
            }
        }
    }

    /**
     * 开始普通进食行为。
     *
     * @param foodStack 食物物品
     */
    public void startEatingFood(ItemStack foodStack) {
        BirdData birdData = bird.getBirdData();
        startEatingBehavior(
                birdData.eatingTicks() + bird.getRandom().nextInt(birdData.eatingTicksVariant()),
                birdData.foodTicks() + bird.getRandom().nextInt(birdData.foodTicksVariant()),
                birdData.eatAmount(),
                birdData.eatSoundVolume() + bird.getRandom().nextFloat() * birdData.eatSoundVolumeVariant(),
                birdData.eatSoundPitch() + bird.getRandom().nextFloat() * birdData.eatSoundPitchVariant()
        );
    }

    /**
     * 消耗鸟浴盆中的一份食物。
     *
     * @param bath 鸟浴盆方块实体
     * @param contentType 食物类型
     */
    public void consumeBirdBathServing(BirdBathBlockEntity bath, BirdBathContentType contentType) {
        if (contentType == BirdBathContentType.BREAD) {
            this.startEatingFood(new ItemStack(Items.BREAD));
        } else {
            BirdData birdData = bird.getBirdData();
            startEatingBehavior(
                    birdData.eatingTicks() + bird.getRandom().nextInt(birdData.eatingTicksVariant()),
                    birdData.foodTicks() + bird.getRandom().nextInt(birdData.foodTicksVariant()),
                    birdData.eatAmount(),
                    birdData.eatSoundVolume() + bird.getRandom().nextFloat() * birdData.eatSoundVolumeVariant(),
                    birdData.eatSoundPitch() + bird.getRandom().nextFloat() * birdData.eatSoundPitchVariant(), birdData.eatBathMultiplier()
            );
        }
    }

    /**
     * 结束当前进食状态。
     */
    public void clearEating() {
        bird.getTickController().eatingTicks = 0;
        if (bird.getBehaviorStateController().getBehaviorState() == BirdBehaviorState.EATING) {
            bird.getBehaviorStateController().setBehaviorState(bird.getBirdBrain().wantsForage()
                    ? BirdBehaviorState.FORAGING
                    : BirdBehaviorState.IDLE);
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
        bird.getNavigation().stop();
        bird.getTickController().eatingTicks = (int) (eatingTicks * multiplier);
        bird.getTickController().foodTicks = (int) (foodTicks * multiplier);
        bird.getBehaviorStateController().setBehaviorStateFor(BirdBehaviorState.EATING, bird.getTickController().eatingTicks);
        bird.getBirdBrain().onEat(eatAmount * multiplier);
        bird.playSound(SoundEvents.GENERIC_EAT, volume * multiplier, pitch * multiplier);
    }

    /**
     * 判断物品是否可作为鸟类食物。
     *
     * @param stack 物品堆
     * @return 如果可以食用返回 true
     */
    public boolean isEdibleFood(ItemStack stack) {
        return stack.is(NeoGuanNiaoItemTags.BIRD_FOOD);
    }

    /**
     * 开始鸟浴盆进食或饮水动画。
     *
     * @param contentType 鸟浴盆内容类型
     * @param ticks 动画持续 Tick 数
     */
    public void startBirdBathFeedingAnimation(BirdBathContentType contentType, int ticks) {
        bird.getNavigation().stop();
        BirdData birdData = bird.getBirdData();
        if (contentType.isFood()) {
            bird.getTickController().eatingTicks = Math.max(
                    bird.getTickController().eatingTicks,
                    Math.max(birdData.eatingTicksLimitForBath(), ticks)
            );
            bird.getBehaviorStateController().setBehaviorStateFor(BirdBehaviorState.EATING, bird.getTickController().eatingTicks);
        } else {
            bird.getBehaviorStateController().setBehaviorStateFor(
                    BirdBehaviorState.CURIOUS,
                    Math.max(birdData.curiousTicksLimitForBath(), ticks / 2)
            );
        }
    }



}