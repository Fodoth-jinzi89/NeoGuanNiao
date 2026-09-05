package net.fodoth.skina.neoguanniao.event;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoCriteriaTriggers;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoItems;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;

@EventBusSubscriber(modid = NeoGuanNiao.MODID)
public class BirdFeatherPickupHandler {


    @SubscribeEvent
    public static void onPickup(
            ItemEntityPickupEvent.Pre event
    ) {

        if (!(event.getPlayer() instanceof ServerPlayer player)) {
            return;
        }


        if (event.getItemEntity()
                .getItem()
                .is(NeoGuanNiaoItems.BIRD_FEATHER.get())) {


            NeoGuanNiaoCriteriaTriggers
                    .PICKUP_BIRD_FEATHER
                    .get()
                    .trigger(player);
        }
    }
}