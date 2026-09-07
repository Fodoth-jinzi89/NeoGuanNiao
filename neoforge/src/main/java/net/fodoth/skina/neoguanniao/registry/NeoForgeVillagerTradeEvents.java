package net.fodoth.skina.neoguanniao.registry;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;

@EventBusSubscriber(modid = NeoGuanNiao.MODID)
public final class NeoForgeVillagerTradeEvents {
    private NeoForgeVillagerTradeEvents() {}

    @SubscribeEvent
    public static void registerTrades(VillagerTradesEvent event) {
        NeoGuanNiaoVillagerTrades.registerTrades(event.getType(), event.getTrades());
    }
}
