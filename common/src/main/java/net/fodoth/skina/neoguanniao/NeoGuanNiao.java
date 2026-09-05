package net.fodoth.skina.neoguanniao;

import net.minecraft.resources.ResourceLocation;

public final class NeoGuanNiao {
    public static final String MODID = "neoguanniao";
    private NeoGuanNiao() {}
    public static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
