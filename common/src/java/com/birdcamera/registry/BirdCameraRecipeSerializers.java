package com.birdcamera.registry;

import com.birdcamera.BirdCameraMod;
import com.birdcamera.content.camera.FilmToPhotographRecipe;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;

public class BirdCameraRecipeSerializers {

    // 胶片 → 相框相片（相机系统，迁移自 guaniao-2.1.3）
    public static final RecipeSerializer<FilmToPhotographRecipe> FILM_TO_PHOTOGRAPH = register(
            "film_to_photograph",
            new SimpleCraftingRecipeSerializer<FilmToPhotographRecipe>(
                    category -> new FilmToPhotographRecipe(category))
    );

    private static <T extends RecipeSerializer<?>> T register(String id, T serializer) {
        return Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, BirdCameraMod.id(id), serializer);
    }

    public static void register() {
        BirdCameraMod.LOGGER.info("注册配方序列化器...");
    }
}