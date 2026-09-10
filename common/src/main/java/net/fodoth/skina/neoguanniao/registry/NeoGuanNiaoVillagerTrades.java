package net.fodoth.skina.neoguanniao.registry;

import net.fodoth.skina.neoguanniao.content.villager.trade.BirdBagTrade;
import net.fodoth.skina.neoguanniao.content.villager.trade.BirdFeatherTrade;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import java.util.List;


public class NeoGuanNiaoVillagerTrades {

    public static void registerTrades(VillagerProfession profession, Int2ObjectMap<List<VillagerTrades.ItemListing>> trades) {
        if (!NeoGuanNiaoVillagerProfessions.BIRD_KEEPER.isPresent()
                || profession != NeoGuanNiaoVillagerProfessions.BIRD_KEEPER.get()) {
            return;
        }
        if (trades.size() >= 6) {
            trades.get(1).add(new BirdFeatherTrade());  // 新手
            trades.get(2).add(new BirdFeatherTrade());  // 学徒
            trades.get(3).add(new BirdFeatherTrade());  // 老手
            trades.get(4).add(new BirdFeatherTrade());  // 专家
            trades.get(5).add(new BirdFeatherTrade());  // 大师
            trades.get(1).add(new BirdBagTrade());
            trades.get(2).add(new BirdBagTrade());
            trades.get(3).add(new BirdBagTrade());
            trades.get(4).add(new BirdBagTrade());
            trades.get(5).add(new BirdBagTrade());
        }
    }
}
