package net.fodoth.skina.neoguanniao.content.bird.core.controller;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.data.BirdData;
import net.fodoth.skina.neoguanniao.content.bird.core.data.datum.BirdMiscDatum;
import net.fodoth.skina.neoguanniao.content.bird.impl.neo.budgerigar.BudgerigarEntity;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import static net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity.*;

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

    public void setGender(boolean isMale) {
        bird().getEntityData().set(GENDER, isMale);
    }

    public boolean getGender() {
        return bird().getEntityData().get(GENDER);
    }

    public void randomizeGender() {
        setGender(bird().getRandom().nextBoolean());
    }

    public boolean getRandomGender() {
        return bird().getRandom().nextBoolean();
    }

    public void setEggCount(int eggCount) {
        BirdData birdData = bird().getBirdData();
        BirdMiscDatum miscDatum = birdData.misc();
        bird().getEntityData().set(EGG_COUNT, Mth.clamp(eggCount, miscDatum.eggCountMin(), miscDatum.eggCountMin() + miscDatum.eggCountVariance()));
    }

    public int getEggCount() {
        BirdData birdData = bird().getBirdData();
        BirdMiscDatum miscDatum = birdData.misc();
        int eggCount = bird().getEntityData().get(EGG_COUNT);
        return Mth.clamp(eggCount, miscDatum.eggCountMin(), miscDatum.eggCountMin() + miscDatum.eggCountVariance());
    }

    public void randomizeEggCount() {
        BirdData birdData = bird().getBirdData();
        BirdMiscDatum miscDatum = birdData.misc();
        setEggCount(miscDatum.eggCountMin() + bird().getRandom().nextInt(miscDatum.eggCountVariance()));
    }

    public int inheritEggCount(AbstractBirdEntity<?> parent,
                               AbstractBirdEntity<?> mate) {
        int parentEggCount = parent.getEggCount();
        int mateEggCount = mate.getEggCount();

        // 计算平均值作为正态分布的中心值
        double mean = (parentEggCount + mateEggCount) / 2.0;

        // 标准差设定为1.5，使得大部分值分布在均值附近
        // 约68%的值在 mean±1.5 范围内，95%在 mean±3.0 范围内
        double stdDev = 1.5;

        // 使用随机数生成器生成正态分布的值
        double value = mean + stdDev * parent.getRandom().nextGaussian();

        // 四舍五入到最接近的整数
        int result = (int) Math.round(value);

        BirdMiscDatum misc = parent.getBirdData().misc();
        BirdMiscDatum misc1 = mate.getBirdData().misc();
        int min = Math.min(misc.eggCountMin(), misc1.eggCountMin());
        int max = Math.min(misc.eggCountMin() + misc.eggCountVariance(), misc1.eggCountMin() + misc1.eggCountVariance());

        result = Math.clamp(result, min, max);

        return result;
    }

}