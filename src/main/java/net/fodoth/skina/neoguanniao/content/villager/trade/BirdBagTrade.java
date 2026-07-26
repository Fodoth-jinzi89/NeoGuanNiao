package net.fodoth.skina.neoguanniao.content.villager.trade;

import net.fodoth.skina.neoguanniao.content.villager.MerchantOfferBuilder;
import net.fodoth.skina.neoguanniao.content.villager.compat.QuestShopCompat;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoItems;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.neoforged.neoforge.registries.DeferredItem;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 鸟袋商品交易 - 根据村民等级提供不同档位的随机交易
 */
public class BirdBagTrade implements VillagerTrades.ItemListing {

    // ==================== 各等级交易池 ====================
    // 新手 (1级) - 基础鸟食: 8绿宝石/组 → 4个/组
    private static final List<TradeEntry> BASIC = List.of(
            new TradeEntry(NeoGuanNiaoItems.BIRD_FOOD_BAG_SEED, 8, 4),
            new TradeEntry(NeoGuanNiaoItems.BIRD_FOOD_BAG_FISH, 12, 4)
    );

    // 学徒 (2级) - 绿色功能袋: 16绿宝石/组 → 4个/组
    private static final List<TradeEntry> GREEN = List.of(
            new TradeEntry(NeoGuanNiaoItems.GREEN_FOOD_BAG_GROWTH, 16, 4),
            new TradeEntry(NeoGuanNiaoItems.GREEN_FOOD_BAG_REJUVENATE, 16, 4),
            new TradeEntry(NeoGuanNiaoItems.GREEN_FOOD_BAG_STOP, 16, 4)
    );

    // 老手 (3级) - 高级绿色袋: 不同物品不同价格
    private static final List<TradeEntry> ADVANCED = List.of(
            new TradeEntry(NeoGuanNiaoItems.GREEN_FOOD_BAG_TRANSMUTE, 20, 4),
            new TradeEntry(NeoGuanNiaoItems.GREEN_FOOD_BAG_SIZE_UP, 10, 4),
            new TradeEntry(NeoGuanNiaoItems.GREEN_FOOD_BAG_SIZE_DOWN, 10, 4)
    );

    // 专家 (4级) - 羽毛系列: 快速/缓慢32绿宝石/组, 增减24绿宝石/个
    private static final List<TradeEntry> EXPERT = List.of(
            new TradeEntry(NeoGuanNiaoItems.GREEN_FOOD_BAG_FEATHER_FAST, 32, 4),
            new TradeEntry(NeoGuanNiaoItems.GREEN_FOOD_BAG_FEATHER_ADD, 24, 1)
    );

    // 大师 (5级) - 金色系列: 升级/降级64绿宝石/个, 蛋增/减32绿宝石/个
    private static final List<TradeEntry> MASTER = List.of(
            new TradeEntry(NeoGuanNiaoItems.GOLDEN_FOOD_BAG_UPGRADE, 64, 1),
            new TradeEntry(NeoGuanNiaoItems.GOLDEN_FOOD_BAG_EGG_ADD, 32, 1)
    );

    // ==================== 核心方法 ====================

    @Override
    public MerchantOffer getOffer(@NotNull Entity trader, @NotNull RandomSource random) {
        int level = getVillagerLevel(trader);                       // 获取村民等级
        List<TradeEntry> pool = getPool(level);                     // 获取对应交易池
        TradeEntry entry = pool.get(random.nextInt(pool.size()));  // 随机选择一项

        // 使用条目指定的数量(不再随机)
        int count = entry.count();

        ItemStack result = new ItemStack(entry.item().get(), count);
        ItemStack cost = QuestShopCompat.createCurrency(entry.price());

        // 构建基础交易
        MerchantOffer offer = new MerchantOffer(
                new ItemCost(cost.getItem(), cost.getCount()),
                result,
                getMaxUses(level),      // 最大使用次数
                getExperience(level),   // 经验值
                0.05F                   // 价格波动
        );

        // 美化展示 (显示具体货币和物品)
        return MerchantOfferBuilder.of(offer)
                .displayCost(cost)
                .displayResult(result)
                .build();
    }

    // ==================== 辅助方法 ====================

    /** 根据村民等级获取交易池 */
    private List<TradeEntry> getPool(int level) {
        return switch (level) {
            case 1 -> BASIC;
            case 2 -> GREEN;
            case 3 -> ADVANCED;
            case 4 -> EXPERT;
            default -> MASTER;
        };
    }

    /** 获取交易者村民等级 (非村民默认为1) */
    private int getVillagerLevel(Entity entity) {
        return entity instanceof Villager villager
                ? villager.getVillagerData().getLevel()
                : 1;
    }

    /** 等级越高, 可用次数越少 (平衡性) */
    private int getMaxUses(int level) {
        return switch (level) {
            case 1 -> 16;
            case 2 -> 12;
            case 3 -> 8;
            case 4 -> 6;
            default -> 4;
        };
    }

    /** 等级越高, 单次交易经验越多 */
    private int getExperience(int level) {
        return switch (level) {
            case 1 -> 3;
            case 2 -> 5;
            case 3 -> 8;
            case 4 -> 12;
            default -> 20;
        };
    }

    /** 交易条目: 物品 + 单价 + 数量 */
    private record TradeEntry(DeferredItem<Item> item, int price, int count) {}
}