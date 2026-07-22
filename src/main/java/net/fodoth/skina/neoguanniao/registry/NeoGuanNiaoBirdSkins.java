package net.fodoth.skina.neoguanniao.registry;

import net.fodoth.skina.neoguanniao.content.bird.core.skin.BirdSkin;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class NeoGuanNiaoBirdSkins {
    private static final Map<ResourceLocation, BirdSkin> SKINS = new HashMap<>();

    public static void register(BirdSkin skin) {
        SKINS.put(skin.id(), skin);
    }

    public static BirdSkin get(ResourceLocation id) {
        return SKINS.get(id);
    }
}
