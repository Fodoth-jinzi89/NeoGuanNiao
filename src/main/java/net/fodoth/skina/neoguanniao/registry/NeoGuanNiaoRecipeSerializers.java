package net.fodoth.skina.neoguanniao.registry;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.content.camera.FilmToPhotographRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class NeoGuanNiaoRecipeSerializers {

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, NeoGuanNiao.MODID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<FilmToPhotographRecipe>> FILM_TO_PHOTOGRAPH =
            RECIPE_SERIALIZERS.register("film_to_photograph", () -> new SimpleCraftingRecipeSerializer<>(FilmToPhotographRecipe::new));


    private NeoGuanNiaoRecipeSerializers() {
    }
}
