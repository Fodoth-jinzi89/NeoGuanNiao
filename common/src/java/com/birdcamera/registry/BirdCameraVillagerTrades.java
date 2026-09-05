package com.birdcamera.registry;

import com.birdcamera.BirdCameraMod;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.minecraft.world.entity.npc.VillagerTrades;

import java.util.List;

public class BirdCameraVillagerTrades {

    public static void register() {
        BirdCameraMod.LOGGER.info("注册村民交易...");

        Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = new Int2ObjectOpenHashMap<>();

        // 1级交易
        trades.put(1, List.of(
                // 买面包屑 - 村民用绿宝石买面包屑
                new VillagerTrades.EmeraldForItems(BirdCameraItems.BREADCRUMBS, 16, 16, 2, 1),
                // 卖鸟食包 - 村民用鸟食包换绿宝石
                new VillagerTrades.ItemsForEmeralds(BirdCameraItems.BIRD_FOOD_BAG, 1, 1, 16, 1)
        ));

        // 2级交易
        trades.put(2, List.of(
                // 卖种子鸟食包
                new VillagerTrades.ItemsForEmeralds(BirdCameraItems.BIRD_FOOD_BAG_SEED, 2, 1, 16, 5),
                // 卖鱼类鸟食包
                new VillagerTrades.ItemsForEmeralds(BirdCameraItems.BIRD_FOOD_BAG_FISH, 2, 1, 16, 5)
        ));

        // 3级交易
        trades.put(3, List.of(
                // 卖鸟类图鉴
                new VillagerTrades.ItemsForEmeralds(BirdCameraItems.BIRD_GUIDE, 1, 1, 16, 10),
                // 卖鸟蛋
                new VillagerTrades.ItemsForEmeralds(BirdCameraItems.BIRD_EGG, 1, 3, 16, 10)
        ));

        // 4级交易
        trades.put(4, List.of(
                // 卖小鸟笼
                new VillagerTrades.ItemsForEmeralds(BirdCameraItems.SMALL_BIRD_CAGE, 1, 1, 16, 15),
                // 卖中鸟笼
                new VillagerTrades.ItemsForEmeralds(BirdCameraItems.MEDIUM_BIRD_CAGE, 3, 1, 16, 15)
        ));

        // 5级交易
        trades.put(5, List.of(
                // 卖大鸟笼
                new VillagerTrades.ItemsForEmeralds(BirdCameraItems.LARGE_BIRD_CAGE, 5, 1, 8, 30),
                // 卖鸟羽毛
                new VillagerTrades.EmeraldForItems(BirdCameraItems.BIRD_FEATHER, 1, 16, 30, 2)
        ));

        TradeOfferHelper.registerVillagerOffers(
                BirdCameraVillagerProfessions.BIRD_KEEPER,
                5,
                factories -> trades.forEach((level, offers) -> {
                    if (level <= 5) {
                        factories.addAll(offers);
                    }
                })
        );
    }
}
