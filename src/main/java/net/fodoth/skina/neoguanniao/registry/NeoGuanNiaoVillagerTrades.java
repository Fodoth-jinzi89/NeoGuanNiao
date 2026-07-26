package net.fodoth.skina.neoguanniao.registry;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.content.villager.trade.BirdBagTrade;
import net.fodoth.skina.neoguanniao.content.villager.trade.BirdFeatherTrade;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;


@EventBusSubscriber(modid = NeoGuanNiao.MODID)
public class NeoGuanNiaoVillagerTrades {

    @SubscribeEvent
    public static void registerTrades(VillagerTradesEvent event) {
        if (event.getType() == NeoGuanNiaoVillagerProfessions.BIRD_KEEPER.get()) {
            event.getTrades().get(1).add(new BirdFeatherTrade());  // 新手
            event.getTrades().get(2).add(new BirdFeatherTrade());  // 学徒
            event.getTrades().get(3).add(new BirdFeatherTrade());  // 老手
            event.getTrades().get(4).add(new BirdFeatherTrade());  // 专家
            event.getTrades().get(5).add(new BirdFeatherTrade());  // 大师
            event.getTrades().get(1).add(new BirdBagTrade());
            event.getTrades().get(2).add(new BirdBagTrade());
            event.getTrades().get(3).add(new BirdBagTrade());
            event.getTrades().get(4).add(new BirdBagTrade());
            event.getTrades().get(5).add(new BirdBagTrade());
        }
    }
}