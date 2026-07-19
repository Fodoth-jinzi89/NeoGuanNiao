package net.fodoth.skina.neoguanniao.content.bird.core.controller;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.impl.neo.budgerigar.BudgerigarEntity;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * 鸟类繁殖控制器，处理玩家与鸟类的繁育交互
 * @param <T> 鸟类实体类型
 */
public class BirdBreedController<T extends AbstractBirdEntity<T>> extends AbstractBirdController<T> {

    /**
     * 处理玩家交互（右键点击）
     * @param player 玩家
     * @param hand   交互手
     * @return 交互结果
     */
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // ----- 前置检查 -----
        if (!isBreedingFood(stack)) return InteractionResult.PASS; // 不是繁育食物
        if (bird().isBaby()) return InteractionResult.PASS;        // 幼年禁止繁殖
        if (bird().getAge() > 0) return InteractionResult.PASS;    // 冷却中（贤者时间）
        if (bird().getEatingController().isEating()) return InteractionResult.PASS; // 正在进食
        if (bird() instanceof BudgerigarEntity b && b.isBusyWithMusicOrSleep()) {
            return InteractionResult.PASS; // 虎皮鹦鹉在跳舞/睡觉
        }
        if (bird().getRoutineController().isSleepingOrRoosting()) return InteractionResult.PASS; // 睡眠/栖息中

        // 客户端：仅显示成功动画，不执行逻辑
        if (bird().level().isClientSide) {
            return InteractionResult.sidedSuccess(true);
        }

        // ----- 服务端执行繁育 -----
        // 复制食物用于后续（保留原物品信息）
        ItemStack eaten = stack.copy();
        eaten.setCount(1);

        // 非创造模式消耗食物
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        startBreeding(eaten, player);
        return InteractionResult.SUCCESS;
    }

    // ======================== 子类可重写方法 ========================

    /** 判断物品是否为繁育食物（默认使用标签） */
    protected boolean isBreedingFood(ItemStack stack) {
        return !stack.isEmpty() && stack.is(NeoGuanNiaoItemTags.BIRD_BREED_FOOD);
    }

    /** 开始繁育（设置恋爱状态） */
    protected void startBreeding(ItemStack eaten, Player player) {
        bird().setInLove(player);
    }
}