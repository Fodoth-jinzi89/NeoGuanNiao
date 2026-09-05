package net.fodoth.skina.neoguanniao.registry;

import net.fodoth.skina.neoguanniao.content.bird.core.model.BirdModel;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class NeoGuanNiaoBirdModels {
    private static final Map<ResourceLocation, BirdModel> MODELS = new HashMap<>();

    public static void register(BirdModel model) {
        MODELS.put(model.id(), model);
    }

    public static BirdModel get(ResourceLocation id) {
        return MODELS.get(id);
    }
}
