package net.fodoth.skina.neoguanniao.client.guide;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class BirdGuideClient {
    private BirdGuideClient() {
    }

    public static void open() {
        Minecraft.getInstance().setScreen(new BirdGuideScreen());
    }
}