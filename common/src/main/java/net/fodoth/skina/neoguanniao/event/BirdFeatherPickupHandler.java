package net.fodoth.skina.neoguanniao.event;

import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoCriteriaTriggers;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.item.ItemEntity;

public class BirdFeatherPickupHandler {


    public static void onPickup(
            Player entity, ItemEntity itemEntity
    ) {

        if (!(entity instanceof ServerPlayer player)) {
            return;
        }


        if (itemEntity
                .getItem()
                .is(NeoGuanNiaoItems.BIRD_FEATHER.get())) {


            NeoGuanNiaoCriteriaTriggers
                    .PICKUP_BIRD_FEATHER
                    .get()
                    .trigger(player);
        }
    }
}