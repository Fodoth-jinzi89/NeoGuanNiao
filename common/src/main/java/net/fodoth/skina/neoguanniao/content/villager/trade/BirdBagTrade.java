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
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BirdBagTrade implements VillagerTrades.ItemListing {

    private static final List<TradeEntry> BASIC = List.of(
            new TradeEntry(NeoGuanNiaoItems.BIRD_FOOD_BAG_SEED, 8, 4),
            new TradeEntry(NeoGuanNiaoItems.BIRD_FOOD_BAG_FISH, 12, 4)
    );

    private static final List<TradeEntry> GREEN = List.of(
            new TradeEntry(NeoGuanNiaoItems.GREEN_FOOD_BAG_GROWTH, 16, 4),
            new TradeEntry(NeoGuanNiaoItems.GREEN_FOOD_BAG_REJUVENATE, 16, 4),
            new TradeEntry(NeoGuanNiaoItems.GREEN_FOOD_BAG_STOP, 16, 4)
    );

    private static final List<TradeEntry> ADVANCED = List.of(
            new TradeEntry(NeoGuanNiaoItems.GREEN_FOOD_BAG_TRANSMUTE, 20, 4),
            new TradeEntry(NeoGuanNiaoItems.GREEN_FOOD_BAG_SIZE_UP, 10, 4),
            new TradeEntry(NeoGuanNiaoItems.GREEN_FOOD_BAG_SIZE_DOWN, 10, 4)
    );

    private static final List<TradeEntry> EXPERT = List.of(
            new TradeEntry(NeoGuanNiaoItems.GREEN_FOOD_BAG_FEATHER_FAST, 32, 4),
            new TradeEntry(NeoGuanNiaoItems.GREEN_FOOD_BAG_FEATHER_ADD, 24, 1)
    );

    private static final List<TradeEntry> MASTER = List.of(
            new TradeEntry(NeoGuanNiaoItems.GOLDEN_FOOD_BAG_UPGRADE, 64, 1),
            new TradeEntry(NeoGuanNiaoItems.GOLDEN_FOOD_BAG_EGG_ADD, 32, 1)
    );

    @Override
    public MerchantOffer getOffer(@NotNull Entity trader, @NotNull RandomSource random) {
        int level = getVillagerLevel(trader);
        List<TradeEntry> pool = getPool(level);
        TradeEntry entry = pool.get(random.nextInt(pool.size()));
        int count = entry.count();

        ItemStack result = new ItemStack(entry.item().get(), count);
        ItemStack cost = QuestShopCompat.createCurrency(entry.price());

        MerchantOffer offer = new MerchantOffer(
                new ItemCost(cost.getItem(), cost.getCount()),
                result,
                getMaxUses(level),
                getExperience(level),
                0.05F
        );

        return MerchantOfferBuilder.of(offer)
                .displayCost(cost)
                .displayResult(result)
                .build();
    }

    private List<TradeEntry> getPool(int level) {
        return switch (level) {
            case 1 -> BASIC;
            case 2 -> GREEN;
            case 3 -> ADVANCED;
            case 4 -> EXPERT;
            default -> MASTER;
        };
    }

    private int getVillagerLevel(Entity entity) {
        return entity instanceof Villager villager
                ? villager.getVillagerData().getLevel()
                : 1;
    }

    private int getMaxUses(int level) {
        return switch (level) {
            case 1 -> 16;
            case 2 -> 12;
            case 3 -> 8;
            case 4 -> 6;
            default -> 4;
        };
    }

    private int getExperience(int level) {
        return switch (level) {
            case 1 -> 3;
            case 2 -> 5;
            case 3 -> 8;
            case 4 -> 12;
            default -> 20;
        };
    }

    private record TradeEntry(Supplier<Item> item, int price, int count) {}
}
