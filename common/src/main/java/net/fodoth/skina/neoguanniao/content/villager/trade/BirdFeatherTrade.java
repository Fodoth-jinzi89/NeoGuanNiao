package net.fodoth.skina.neoguanniao.content.villager.trade;

import net.fodoth.skina.neoguanniao.content.bird.core.data.datum.BirdModelSkinDatum;
import net.fodoth.skina.neoguanniao.content.bird.core.skin.BirdSkin;
import net.fodoth.skina.neoguanniao.content.bird.core.skin.BirdSkinRarity;
import net.fodoth.skina.neoguanniao.content.feather.BirdFeatherData;
import net.fodoth.skina.neoguanniao.content.feather.BirdFeatherItem;
import net.fodoth.skina.neoguanniao.content.villager.MerchantOfferBuilder;
import net.fodoth.skina.neoguanniao.content.villager.compat.QuestShopCompat;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoBirdData;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoDataComponents;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoItems;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 村民收购羽毛交易实现
 * <p>
 * 交易机制：
 * - 不同等级的村民收购不同稀有度的羽毛
 * - 村民用代币换取玩家手中的特定羽毛
 * - 稀有度越高，单次交易所需的羽毛越少，价格越高
 * <p>
 * 村民等级与可交易稀有度对照：
 * 新手(1级)  → COMMON / UNCOMMON
 * 学徒(2级)  → UNCOMMON / RARE
 * 熟手(3级)  → RARE / EPIC
 * 专家(4级)  → EPIC / LEGENDARY
 * 大师(5级)  → LEGENDARY / ANCIENT / UNIQUE / HIDDEN
 */
public class BirdFeatherTrade implements VillagerTrades.ItemListing {

    /** 最大重试次数，防止无限循环查找稀有度 */
    private static final int MAX_RETRY = 10;

    /** 保底稀有度，当所有尝试失败时使用 */
    private static final BirdSkinRarity FALLBACK_RARITY = BirdSkinRarity.COMMON;

    @Override
    public MerchantOffer getOffer(@NotNull Entity trader, @NotNull RandomSource random) {
        // 1. 获取村民等级
        int level = getVillagerLevel(trader);

        // 2. 根据等级随机选择稀有度（带重试机制确保有效性）
        BirdSkinRarity rarity = selectValidRarity(level, random);

        // 3. 生成随机倍率 (1-4倍)，增加交易变化性
        int multiplier = random.nextInt(4) + 1;

        // 4. 根据稀有度计算所需羽毛数量 × 倍率
        int featherCount = getFeatherCountForTrade(rarity) * multiplier;

        // 5. 根据稀有度计算代币价格 × 倍率
        int priceCount = getPrice(rarity) * multiplier;

        // 6. 创建带数据组件匹配条件的交易成本（只接受指定稀有度的羽毛）
        FeatherTradeData tradeData = createFeatherCost(rarity, featherCount, random);

        // 7. 构建交易结果（村民支付代币）
        ItemStack result = QuestShopCompat.createCurrency(priceCount);

        // 8. 创建基础交易对象
        MerchantOffer offer = new MerchantOffer(
                tradeData.cost(),      // 成本：指定数量和稀有度的羽毛
                result,                // 结果：代币
                getMaxUses(rarity),    // 最大使用次数
                getExperience(rarity), // 经验值
                0.05F                  // 价格浮动
        );

        // 9. 构建显示用的羽毛物品（用于交易界面展示）
        ItemStack display = new ItemStack(NeoGuanNiaoItems.BIRD_FEATHER.get(), featherCount);
        BirdFeatherItem.setFeatherData(display, tradeData.featherData());

        // 10. 返回带有显示信息的完整交易
        return MerchantOfferBuilder
                .of(offer)
                .displayCost(display)
                .displayResult(result)
                .build();
    }

    /**
     * 选择有效的稀有度（带重试机制）
     *
     * @param level  村民等级
     * @param random 随机源
     * @return 有效的稀有度，确保该稀有度在游戏中真实存在
     */
    private BirdSkinRarity selectValidRarity(int level, RandomSource random) {
        BirdSkinRarity rarity = FALLBACK_RARITY;

        for (int attempt = 0; attempt < MAX_RETRY; attempt++) {
            rarity = randomRarityByLevel(random, level);
            if (hasFeatherRarity(rarity)) {
                break;
            }
            if (attempt == MAX_RETRY - 1) {
                rarity = FALLBACK_RARITY;
            }
        }

        return rarity;
    }

    /**
     * 创建带数据组件匹配条件的交易成本
     * <p>
     * 原理：通过 DataComponentPredicate 指定羽毛必须包含特定的 BirdFeatherData，
     * 从而确保玩家只能使用指定稀有度的羽毛进行交易
     *
     * @param target 目标稀有度
     * @param count  所需羽毛数量
     * @param random 随机源
     * @return 包含交易成本和对应羽毛数据的记录
     */
    private FeatherTradeData createFeatherCost(BirdSkinRarity target, int count, RandomSource random) {
        // 1. 收集所有符合条件的羽毛数据
        List<BirdFeatherData> candidates = new ArrayList<>();

        for (var holder : NeoGuanNiaoBirdData.BIRD_DATA) {
            BirdModelSkinDatum modelDatum = holder.get().model();
            Set<Integer> generatedRarities = new HashSet<>();

            for (BirdSkin skin : modelDatum.birdSkin()) {
                int rarityValue = skin.rarity().getRarity();
                if (generatedRarities.contains(rarityValue)) continue;
                generatedRarities.add(rarityValue);

                if (rarityValue == target.getRarity()) {
                    candidates.add(BirdFeatherData.create(holder.getId(), rarityValue));
                }
            }
        }

        // 2. 如果没有候选项，使用保底数据
        if (candidates.isEmpty()) {
            BirdFeatherData fallback = BirdFeatherData.create(
                    NeoGuanNiaoBirdData.BUDGERIGAR.getId(),
                    target.getRarity()
            );

            var predicate = DataComponentPredicate.builder()
                    .expect(NeoGuanNiaoDataComponents.BIRD_FEATHER_DATA.get(), fallback)
                    .build();

            return new FeatherTradeData(
                    new ItemCost(NeoGuanNiaoItems.BIRD_FEATHER, count, predicate),
                    fallback
            );
        }

        // 3. 随机选择一个候选项构建交易成本
        BirdFeatherData data = candidates.get(random.nextInt(candidates.size()));
        var predicate = DataComponentPredicate.builder()
                .expect(NeoGuanNiaoDataComponents.BIRD_FEATHER_DATA.get(), data)
                .build();

        return new FeatherTradeData(
                new ItemCost(NeoGuanNiaoItems.BIRD_FEATHER, count, predicate),
                data
        );
    }

    /**
     * 检查指定稀有度是否存在对应的真实羽毛数据
     * 对应 generateBirdFeathers() 中实际生成的羽毛
     *
     * @param rarity 要检查的稀有度
     * @return true 表示该稀有度至少存在一种羽毛
     */
    private boolean hasFeatherRarity(BirdSkinRarity rarity) {
        for (var holder : NeoGuanNiaoBirdData.BIRD_DATA) {
            for (BirdSkin skin : holder.get().model().birdSkin()) {
                if (skin.rarity() == rarity) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 根据稀有度获取每次交易所需的羽毛基础数量
     * 规则：稀有度越高，需求越少（物以稀为贵）
     *
     * @param rarity 羽毛稀有度
     * @return 基础数量
     */
    private int getFeatherCountForTrade(BirdSkinRarity rarity) {
        return switch (rarity) {
            case COMMON -> 16;      // 普通：需要16根
            case UNCOMMON -> 8;     // 罕见：需要8根
            case RARE -> 4;         // 稀有：需要4根
            case EPIC -> 2;         // 史诗：需要2根
            default -> 1;           // 传说及以上：仅需1根
        };
    }

    /**
     * 根据稀有度获取每根羽毛的基础代币价格
     * 规则：稀有度越高，单根价格越贵
     *
     * @param rarity 羽毛稀有度
     * @return 每根羽毛的代币价格
     */
    private int getPrice(BirdSkinRarity rarity) {
        return switch (rarity) {
            case COMMON, UNCOMMON, RARE, EPIC, LEGENDARY -> 1;   // 普通~传说：1代币/根
            case ANCIENT -> 2;      // 远古：2代币/根
            case HIDDEN -> 3;       // 隐藏：3代币/根
            case UNIQUE -> 4;       // 独特：4代币/根
        };
    }

    /**
     * 根据稀有度获取完成交易获得的经验值
     * 规则：稀有度越高，经验越多
     *
     * @param rarity 羽毛稀有度
     * @return 经验值
     */
    private int getExperience(BirdSkinRarity rarity) {
        return switch (rarity) {
            case COMMON -> 4;
            case UNCOMMON -> 6;
            case RARE -> 8;
            case EPIC -> 10;
            case LEGENDARY -> 12;
            case ANCIENT -> 14;
            case HIDDEN -> 16;
            case UNIQUE -> 18;
        };
    }

    /**
     * 根据稀有度获取最大交易次数（即村民补货前可交易次数）
     * 规则：稀有度越高，可交易次数越少
     *
     * @param rarity 羽毛稀有度
     * @return 最大交易次数
     */
    private int getMaxUses(BirdSkinRarity rarity) {
        return switch (rarity) {
            case COMMON, UNCOMMON -> 32;   // 普通/罕见：32次
            case RARE, EPIC -> 16;          // 稀有/史诗：16次
            default -> 8;                   // 传说及以上：8次
        };
    }

    /**
     * 获取村民的职业等级
     * 若交易者不是村民，默认返回1级（新手）
     *
     * @param trader 交易者实体
     * @return 村民等级 (1-5)
     */
    private int getVillagerLevel(Entity trader) {
        if (trader instanceof Villager villager) {
            return villager.getVillagerData().getLevel();
        }
        return 1;
    }

    /**
     * 根据村民等级随机生成对应的羽毛稀有度
     * <p>
     * 概率分布策略：
     * - 1-4级：80%概率获得较低稀有度，20%概率获得较高稀有度
     * - 5级（大师）：均匀分布在四种高级稀有度之间
     *
     * @param random 随机源
     * @param level  村民等级
     * @return 随机选中的稀有度
     */
    private BirdSkinRarity randomRarityByLevel(RandomSource random, int level) {
        return switch (level) {
            case 1 -> random.nextInt(100) < 80 ? BirdSkinRarity.COMMON : BirdSkinRarity.UNCOMMON;
            case 2 -> random.nextInt(100) < 80 ? BirdSkinRarity.UNCOMMON : BirdSkinRarity.RARE;
            case 3 -> random.nextInt(100) < 80 ? BirdSkinRarity.RARE : BirdSkinRarity.EPIC;
            case 4 -> random.nextInt(100) < 80 ? BirdSkinRarity.EPIC : BirdSkinRarity.LEGENDARY;
            default -> {                // 5级大师及以上
                int r = random.nextInt(100);
                if (r < 40) yield BirdSkinRarity.LEGENDARY;
                if (r < 60) yield BirdSkinRarity.ANCIENT;
                if (r < 80) yield BirdSkinRarity.UNIQUE;
                yield BirdSkinRarity.HIDDEN;
            }
        };
    }

    /**
     * 存储交易成本和对应羽毛数据的记录类
     *
     * @param cost        交易成本（包含匹配条件的物品）
     * @param featherData 对应的羽毛数据（用于显示）
     */
    private record FeatherTradeData(ItemCost cost, BirdFeatherData featherData) {}
}
