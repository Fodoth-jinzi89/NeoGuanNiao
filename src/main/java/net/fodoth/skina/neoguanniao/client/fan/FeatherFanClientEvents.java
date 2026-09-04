package net.fodoth.skina.neoguanniao.client.fan;

import net.fodoth.skina.neoguanniao.content.fan.FeatherFanItem;
import net.fodoth.skina.neoguanniao.network.FeatherFanPiercePacket;
import net.fodoth.skina.neoguanniao.network.NeoGuanNiaoNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;

@EventBusSubscriber(modid = "neoguanniao", value = Dist.CLIENT)
public final class FeatherFanClientEvents {
    private FeatherFanClientEvents() {
    }

    @SubscribeEvent
    public static void onMouseButton(InputEvent.MouseButton.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (event.getButton() != 0 || event.getAction() != 1 || minecraft.screen != null) {
            return;
        }
        LocalPlayer player = minecraft.player;
        if (player == null || !FeatherFanItem.isFullyCharged(player)) {
            return;
        }
        event.setCanceled(true);
        NeoGuanNiaoNetwork.sendToServer(new FeatherFanPiercePacket());
    }
}
