package net.fodoth.skina.neoguanniao;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoVillagerProfessions;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoVillagerTrades;
import net.minecraft.world.entity.npc.VillagerTrades;

import java.util.ArrayList;
import java.util.List;

final class NeoGuanNiaoFabricVillagerTrades {
    private NeoGuanNiaoFabricVillagerTrades() {}

    static void register() {
        var trades = new Int2ObjectOpenHashMap<List<VillagerTrades.ItemListing>>();
        for (int level = 0; level <= 5; level++) {
            trades.put(level, new ArrayList<>());
        }
        NeoGuanNiaoVillagerTrades.registerTrades(NeoGuanNiaoVillagerProfessions.BIRD_KEEPER.get(), trades);
        for (int level = 1; level <= 5; level++) {
            List<VillagerTrades.ItemListing> levelTrades = List.copyOf(trades.get(level));
            TradeOfferHelper.registerVillagerOffers(NeoGuanNiaoVillagerProfessions.BIRD_KEEPER.get(), level,
                    offers -> offers.addAll(levelTrades));
        }
    }
}
