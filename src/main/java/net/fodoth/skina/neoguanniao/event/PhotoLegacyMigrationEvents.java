package net.fodoth.skina.neoguanniao.event;

import net.fodoth.skina.neoguanniao.content.camera.LegacyPhotoMigration;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid="neoguanniao")
public final class PhotoLegacyMigrationEvents {
    private PhotoLegacyMigrationEvents() {
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        for (int slot = 0; slot < event.getEntity().getInventory().getContainerSize(); ++slot) {
            LegacyPhotoMigration.queue(event.getEntity().level(), event.getEntity().getInventory().getItem(slot));
        }
    }

    @SubscribeEvent
    public static void onContainerOpen(PlayerContainerEvent.Open event) {
        for (Slot slot : event.getContainer().slots) {
            LegacyPhotoMigration.queue(event.getEntity().level(), slot.getItem());
        }
    }
}

