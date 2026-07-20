package net.fodoth.skina.neoguanniao.registry;

import net.fodoth.skina.neoguanniao.content.nest.BirdNestBlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class NeoGuanNiaoCapabilities {

    public static void register(RegisterCapabilitiesEvent event) {

        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                NeoGuanNiaoBlockEntityTypes.BIRD_NEST.get(),
                (blockEntity, side) -> {

                    if (blockEntity instanceof BirdNestBlockEntity nest) {
                        return nest.getInventory();
                    }

                    return null;
                }
        );
    }
}