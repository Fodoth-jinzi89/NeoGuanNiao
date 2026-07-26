package net.fodoth.skina.neoguanniao.content.bird.core.controller;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.skin.BirdSkin;
import net.fodoth.skina.neoguanniao.content.bird.core.skin.BirdSkinUtils;
import net.fodoth.skina.neoguanniao.content.bird.impl.neo.budgerigar.NeoBudgerigarEntity;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoItemTags;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoItems;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * 鸟食袋控制器 - 处理各种食物袋对鸟类的交互效果
 * 采用策略模式处理不同类型的食物袋，支持：
 * <ul>
 *   <li>年龄控制：生长促进、返老还童、生长开关</li>
 *   <li>性别转换</li>
 *   <li>稀有度升降级</li>
 *   <li>羽毛数量/间隔调节</li>
 *   <li>蛋数量调节</li>
 * </ul>
 *
 * @param <T> 鸟类实体类型
 */
public class BirdFoodBagController<T extends AbstractBirdEntity<T>> extends AbstractBirdController<T> {

    // ==================== 公共交互入口 ====================

    /**
     * 玩家交互入口（右键点击）
     */
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // 前置校验链（短路求值）
        if (!isFilledFoodBag(stack)) return InteractionResult.PASS;
        if (bird().getEatingController().isEating()) return InteractionResult.PASS;
        if (isBusyWithSpecialActivities()) return InteractionResult.PASS;
        if (isRestingOrFlying()) return InteractionResult.PASS;

        // 客户端仅反馈成功（不执行业务逻辑）
        if (bird().level().isClientSide) {
            return InteractionResult.sidedSuccess(true);
        }

        // 服务端执行业务逻辑
        return processFoodBag(stack, player);
    }

    // ==================== 前置条件校验 ====================

    /** 判断是否为装满的食袋 */
    protected boolean isFilledFoodBag(ItemStack stack) {
        return !stack.isEmpty() && stack.is(NeoGuanNiaoItemTags.FILLED_FOOD_BAG);
    }

    /** 判断鸟类是否处于特殊活动状态（跳舞/睡觉） */
    protected boolean isBusyWithSpecialActivities() {
        return bird() instanceof NeoBudgerigarEntity b && b.isBusyWithMusicOrSleep();
    }

    /** 判断鸟类是否处于休息或飞行状态 */
    protected boolean isRestingOrFlying() {
        return bird().getRoutineController().isSleepingOrRoosting()
                || bird().getFlyingController().isBirdFlightActive();
    }

    // ==================== 核心处理 ====================

    /**
     * 处理食物袋逻辑 - 策略模式分发
     */
    protected InteractionResult processFoodBag(ItemStack stack, Player player) {
        ItemStack single = stack.copy();
        single.setCount(1);

        // 根据物品匹配策略并执行
        FoodBagStrategy strategy = FoodBagStrategy.fromItem(single);
        return strategy == null ? InteractionResult.PASS
                : strategy.execute(this, bird(), single, player);
    }

    // ==================== 通用工具方法 ====================

    /**
     * 校验鸟类是否为幼年，若非幼年则发送失败消息
     * @return true=是幼年，false=非幼年（已发送失败消息）
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean checkBabyOrFail(Player player) {
        if (bird().isBaby()) return true;
        player.displayClientMessage(
                Component.translatable("message.neoguanniao.bird_food_bag.adult_failed"),
                true
        );
        return false;
    }

    /**
     * 校验鸟类是否为成年，若非成年则发送失败消息
     * @return true=是成年，false=非成年（已发送失败消息）
     */
    private boolean checkAdultOrFail(Player player) {
        if (!bird().isBaby()) return true;
        player.displayClientMessage(
                Component.translatable("message.neoguanniao.bird_food_bag.baby_failed"),
                true
        );
        return false;
    }

    /**
     * 发送粒子效果（仅服务端执行）
     * @param particle 粒子类型
     * @param count 粒子数量
     * @param speed 粒子速度（0为静止）
     */
    private void spawnParticles(ParticleOptions particle, int count, double speed) {
        if (bird().level().isClientSide) return;
        ServerLevel level = (ServerLevel) bird().level();
        level.sendParticles(
                particle,
                bird().getX(),
                bird().getY() + bird().getBbHeight() * 0.5,
                bird().getZ(),
                count,
                0.3, 0.3, 0.3,
                speed
        );
    }

    /**
     * 消耗物品（非创造模式）
     */
    private void consumeItem(ItemStack stack, Player player) {
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
    }

    /**
     * 尝试切换皮肤
     * @param target 目标皮肤（null表示失败）
     * @param failKey 失败消息的本地化键
     * @return true=切换成功
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean tryApplySkin(Player player, BirdSkin target, String failKey) {
        if (target == null) {
            player.displayClientMessage(Component.translatable(failKey), true);
            return false;
        }
        bird().getSkinController().setSkinVariant(target.id());
        return true;
    }

    /**
     * 获取属性边界值（最小值 + 方差 = 最大值）
     */
    private int getMaxValue(int min, int variance) {
        return min + variance;
    }

    /**
     * 获取间隔边界值（中间值 ± 方差/2）
     */
    private int getIntervalLowerBound(int middle, int variance) {
        return middle - variance / 2;
    }

    private int getIntervalUpperBound(int middle, int variance) {
        return middle + variance / 2;
    }

    // ==================== 食物袋策略枚举 ====================

    /**
     * 食物袋策略枚举
     * 每个策略封装完整的业务逻辑：校验 → 执行 → 反馈
     */
    private enum FoodBagStrategy {

        // ==================== 年龄控制 ====================

        /** 生长促进：幼年 → 成年 */
        GROWTH(NeoGuanNiaoItems.GREEN_FOOD_BAG_GROWTH, (ctrl, bird, stack, player) -> {
            if (!ctrl.checkBabyOrFail(player)) return InteractionResult.FAIL;
            bird.setAge(0);
            ctrl.spawnParticles(ParticleTypes.HAPPY_VILLAGER, 7, 0);
            ctrl.consumeItem(stack, player);
            return InteractionResult.SUCCESS;
        }),

        /** 返老还童：成年 → 幼年（重置生长周期） */
        REJUVENATE(NeoGuanNiaoItems.GREEN_FOOD_BAG_REJUVENATE, (ctrl, bird, stack, player) -> {
            if (!ctrl.checkAdultOrFail(player)) return InteractionResult.FAIL;
            bird.setAge(-24000);
            ctrl.spawnParticles(ParticleTypes.HAPPY_VILLAGER, 7, 0);
            ctrl.consumeItem(stack, player);
            return InteractionResult.SUCCESS;
        }),

        /** 生长开关：切换停止/恢复生长 */
        STOP_GROWTH(NeoGuanNiaoItems.GREEN_FOOD_BAG_STOP, (ctrl, bird, stack, player) -> {
            if (!ctrl.checkBabyOrFail(player)) {
                player.displayClientMessage(
                        Component.translatable("message.neoguanniao.bird_food_bag.stop_failed"),
                        true
                );
                return InteractionResult.FAIL;
            }

            boolean stopped = bird.isGrowthStopped();
            bird.setGrowthStopped(!stopped);

            if (stopped) {
                ctrl.spawnParticles(ParticleTypes.HAPPY_VILLAGER, 7, 0);
                player.displayClientMessage(
                        Component.translatable("message.neoguanniao.bird_food_bag.growth_resumed"),
                        true
                );
            } else {
                ctrl.spawnParticles(ParticleTypes.SMOKE, 8, 0.01);
                player.displayClientMessage(
                        Component.translatable("message.neoguanniao.bird_food_bag.growth_stopped"),
                        true
                );
            }
            ctrl.consumeItem(stack, player);
            return InteractionResult.SUCCESS;
        }),

        // ==================== 性别与皮肤 ====================

        /** 性别转换 + 异性皮肤切换 */
        TRANSMUTE(NeoGuanNiaoItems.GREEN_FOOD_BAG_TRANSMUTE, (ctrl, bird, stack, player) -> {
            BirdSkin target = BirdSkinUtils.findOppositeGenderSkin(bird, bird.getSkin());
            if (!ctrl.tryApplySkin(player, target,
                    "message.neoguanniao.bird_food_bag.transmute_failed")) {
                return InteractionResult.FAIL;
            }
            bird.setMale(!bird.isMale());
            ctrl.spawnParticles(ParticleTypes.DRAGON_BREATH, 10, 0);
            ctrl.consumeItem(stack, player);
            return InteractionResult.SUCCESS;
        }),

        /** 稀有度升级（不包含远古） */
        UPGRADE(NeoGuanNiaoItems.GOLDEN_FOOD_BAG_UPGRADE, (ctrl, bird, stack, player) -> {
            BirdSkin target = BirdSkinUtils.findUpgradeSkinBeforeAncient(bird, bird.getSkin());
            if (!ctrl.tryApplySkin(player, target,
                    "message.neoguanniao.golden_food_bag.upgrade_failed")) {
                return InteractionResult.FAIL;
            }
            ctrl.spawnParticles(ParticleTypes.ELECTRIC_SPARK, 15, 0);
            ctrl.consumeItem(stack, player);
            return InteractionResult.SUCCESS;
        }),

        /** 稀有度降级（不包含远古） */
        DOWNGRADE(NeoGuanNiaoItems.GOLDEN_FOOD_BAG_DOWNGRADE, (ctrl, bird, stack, player) -> {
            BirdSkin target = BirdSkinUtils.findDowngradeSkinBeforeAncient(bird, bird.getSkin());
            if (!ctrl.tryApplySkin(player, target,
                    "message.neoguanniao.golden_food_bag.downgrade_failed")) {
                return InteractionResult.FAIL;
            }
            ctrl.spawnParticles(ParticleTypes.FALLING_NECTAR, 15, 0.01);
            ctrl.consumeItem(stack, player);
            return InteractionResult.SUCCESS;
        }),

        /** 随机独特皮肤 */
        UNIQUE(NeoGuanNiaoItems.GOLDEN_FOOD_BAG_UNIQUE, (ctrl, bird, stack, player) -> {
            BirdSkin target = BirdSkinUtils.findRandomUniqueSkin(bird);
            if (!ctrl.tryApplySkin(player, target,
                    "message.neoguanniao.golden_food_bag.unique_failed")) {
                return InteractionResult.FAIL;
            }
            ctrl.spawnParticles(ParticleTypes.CHERRY_LEAVES, 20, 0.05);
            ctrl.consumeItem(stack, player);
            return InteractionResult.SUCCESS;
        }),

        // ==================== 羽毛控制 ====================

        /** 增加一根羽毛（上限 = 最小值 + 方差） */
        FEATHER_ADD(NeoGuanNiaoItems.GREEN_FOOD_BAG_FEATHER_ADD, (ctrl, bird, stack, player) -> {
            int max = ctrl.getMaxValue(
                    bird.getBirdData().misc().featherCountMin(),
                    bird.getBirdData().misc().featherCountVariance()
            );
            if (bird.getFeatherCount() >= max) {
                player.displayClientMessage(
                        Component.translatable("message.neoguanniao.bird_food_bag.feather_added_failed"),
                        true
                );
                return InteractionResult.FAIL;
            }
            bird.setFeatherCount(bird.getFeatherCount() + 1);
            ctrl.spawnParticles(ParticleTypes.ENCHANTED_HIT, 20, 0.05);
            ctrl.consumeItem(stack, player);
            return InteractionResult.SUCCESS;
        }),

        /** 减少一根羽毛（下限 = 最小值） */
        FEATHER_MINUS(NeoGuanNiaoItems.GREEN_FOOD_BAG_FEATHER_MINUS, (ctrl, bird, stack, player) -> {
            int min = bird.getBirdData().misc().featherCountMin();
            if (bird.getFeatherCount() <= min) {
                player.displayClientMessage(
                        Component.translatable("message.neoguanniao.bird_food_bag.feather_minus_failed"),
                        true
                );
                return InteractionResult.FAIL;
            }
            bird.setFeatherCount(bird.getFeatherCount() - 1);
            ctrl.spawnParticles(ParticleTypes.CRIT, 20, 0.05);
            ctrl.consumeItem(stack, player);
            return InteractionResult.SUCCESS;
        }),

        /** 加快羽毛再生速度（间隔减小 1000） */
        FEATHER_FAST(NeoGuanNiaoItems.GREEN_FOOD_BAG_FEATHER_FAST, (ctrl, bird, stack, player) -> {
            int fasten = bird.getFeatherInterval() - 1000;
            int lowerBound = ctrl.getIntervalLowerBound(
                    bird.getBirdData().misc().featherIntervalMiddle(),
                    bird.getBirdData().misc().featherIntervalVariance()
            );
            if (fasten < lowerBound) {
                player.displayClientMessage(
                        Component.translatable("message.neoguanniao.bird_food_bag.feather_interval_fasten_failed"),
                        true
                );
                return InteractionResult.FAIL;
            }
            bird.setFeatherInterval(fasten);
            ctrl.spawnParticles(ParticleTypes.FLAME, 20, 0.05);
            ctrl.consumeItem(stack, player);
            return InteractionResult.SUCCESS;
        }),

        /** 减慢羽毛再生速度（间隔增大 1000） */
        FEATHER_SLOW(NeoGuanNiaoItems.GREEN_FOOD_BAG_FEATHER_SLOW, (ctrl, bird, stack, player) -> {
            int slow = bird.getFeatherInterval() + 1000;
            int upperBound = ctrl.getIntervalUpperBound(
                    bird.getBirdData().misc().featherIntervalMiddle(),
                    bird.getBirdData().misc().featherIntervalVariance()
            );
            if (slow > upperBound) {
                player.displayClientMessage(
                        Component.translatable("message.neoguanniao.bird_food_bag.feather_interval_slow_failed"),
                        true
                );
                return InteractionResult.FAIL;
            }
            bird.setFeatherInterval(slow);
            ctrl.spawnParticles(ParticleTypes.FALLING_WATER, 20, 0.05);
            ctrl.consumeItem(stack, player);
            return InteractionResult.SUCCESS;
        }),

        // ==================== 蛋控制 ====================

        /** 增加一个蛋（上限 = 最小值 + 方差） */
        EGG_ADD(NeoGuanNiaoItems.GOLDEN_FOOD_BAG_EGG_ADD, (ctrl, bird, stack, player) -> {
            int max = ctrl.getMaxValue(
                    bird.getBirdData().misc().eggCountMin(),
                    bird.getBirdData().misc().eggCountVariance()
            );
            if (bird.getEggCount() >= max) {
                player.displayClientMessage(
                        Component.translatable("message.neoguanniao.bird_food_bag.egg_added_failed"),
                        true
                );
                return InteractionResult.FAIL;
            }
            bird.setEggCount(bird.getEggCount() + 1);
            ctrl.spawnParticles(ParticleTypes.TRIAL_SPAWNER_DETECTED_PLAYER, 20, 0.05);
            ctrl.consumeItem(stack, player);
            return InteractionResult.SUCCESS;
        }),

        /** 减少一个蛋（下限 = 最小值） */
        EGG_MINUS(NeoGuanNiaoItems.GOLDEN_FOOD_BAG_EGG_MINUS, (ctrl, bird, stack, player) -> {
            int min = bird.getBirdData().misc().eggCountMin();
            if (bird.getEggCount() <= min) {
                player.displayClientMessage(
                        Component.translatable("message.neoguanniao.bird_food_bag.egg_minus_failed"),
                        true
                );
                return InteractionResult.FAIL;
            }
            bird.setEggCount(bird.getEggCount() - 1);
            ctrl.spawnParticles(ParticleTypes.TRIAL_SPAWNER_DETECTED_PLAYER_OMINOUS, 20, 0.05);
            ctrl.consumeItem(stack, player);
            return InteractionResult.SUCCESS;
        }),

        SIZE_UP(NeoGuanNiaoItems.GREEN_FOOD_BAG_SIZE_UP, (ctrl, bird, stack, player) -> {
            float sizeUp = bird.getIndividualModelScale() + 0.05F;
            float upperBound =  bird.getBirdData().model().modelScaleProfile().maxIndividualScale();
            if (sizeUp > upperBound) {
                player.displayClientMessage(
                        Component.translatable("message.neoguanniao.bird_food_bag.size_up_failed"),
                        true
                );
                return InteractionResult.FAIL;
            }
            bird.setIndividualModelScale(sizeUp);
            ctrl.spawnParticles(ParticleTypes.ITEM_SLIME, 20, 0.05);
            ctrl.consumeItem(stack, player);
            return InteractionResult.SUCCESS;
        }),

        SIZE_DOWN(NeoGuanNiaoItems.GREEN_FOOD_BAG_SIZE_DOWN, (ctrl, bird, stack, player) -> {
            float sizeDown = bird.getIndividualModelScale() - 0.05F;
            float lowerBound =  bird.getBirdData().model().modelScaleProfile().minIndividualScale();
            if (sizeDown < lowerBound) {
                player.displayClientMessage(
                        Component.translatable("message.neoguanniao.bird_food_bag.size_down_failed"),
                        true
                );
                return InteractionResult.FAIL;
            }
            bird.setIndividualModelScale(sizeDown);
            ctrl.spawnParticles(ParticleTypes.ITEM_SNOWBALL, 20, 0.05);
            ctrl.consumeItem(stack, player);
            return InteractionResult.SUCCESS;
        });

        // ==================== 策略字段 ====================

        private final Item item;              // 对应的物品
        private final FoodBagAction action;   // 业务逻辑

        FoodBagStrategy(DeferredItem<Item> item, FoodBagAction action) {
            this.item = item.get();
            this.action = action;
        }

        // ==================== 策略匹配与执行 ====================

        /**
         * 根据物品栈匹配对应策略
         * @return 匹配的策略，无匹配则返回null
         */
        static FoodBagStrategy fromItem(ItemStack stack) {
            for (FoodBagStrategy strategy : values()) {
                if (stack.is(strategy.item)) return strategy;
            }
            return null;
        }

        /**
         * 执行策略
         */
        InteractionResult execute(BirdFoodBagController<?> ctrl, AbstractBirdEntity<?> bird,
                                  ItemStack stack, Player player) {
            return action.apply(ctrl, bird, stack, player);
        }

        // ==================== 函数式接口 ====================

        @FunctionalInterface
        private interface FoodBagAction {
            InteractionResult apply(BirdFoodBagController<?> ctrl, AbstractBirdEntity<?> bird,
                                    ItemStack stack, Player player);
        }
    }
}