package net.fodoth.skina.neoguanniao.registry;

import net.fodoth.skina.neoguanniao.content.nest.BirdNestBlockEntity;
import net.fodoth.skina.neoguanniao.platform.neoforge.BirdNestInventoryHooksImpl;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class NeoGuanNiaoCapabilities {

    public static void register(RegisterCapabilitiesEvent event) {

        if (!NeoGuanNiaoBlockEntityTypes.BIRD_NEST.isPresent()) {
            return;
        }

        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                NeoGuanNiaoBlockEntityTypes.BIRD_NEST.get(),
                (blockEntity, side) -> {
                    if (blockEntity instanceof BirdNestBlockEntity nest) {
                        return BirdNestInventoryHooksImpl.itemHandler(nest);
                    }
                    return null;
                }
        );
    }
}
