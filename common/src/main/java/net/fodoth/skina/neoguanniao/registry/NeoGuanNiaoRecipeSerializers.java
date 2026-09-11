package net.fodoth.skina.neoguanniao.registry;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.content.camera.FilmCopyRecipe;
import net.fodoth.skina.neoguanniao.content.camera.FilmToPhotographRecipe;
import net.fodoth.skina.neoguanniao.content.camera.PhotographReplaceImageRecipe;
import net.fodoth.skina.neoguanniao.content.camera.PhotographToFilmRecipe;
import net.fodoth.skina.neoguanniao.content.fan.FeatherFanRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.architectury.registry.registries.DeferredRegister;

public class NeoGuanNiaoRecipeSerializers {

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(NeoGuanNiao.MODID, Registries.RECIPE_SERIALIZER);

    public static final RegistrySupplier<RecipeSerializer<FilmToPhotographRecipe>> FILM_TO_PHOTOGRAPH =
            RECIPE_SERIALIZERS.register("film_to_photograph", () -> new SimpleCraftingRecipeSerializer<>(FilmToPhotographRecipe::new));
    public static final RegistrySupplier<RecipeSerializer<FilmCopyRecipe>> FILM_COPY =
            RECIPE_SERIALIZERS.register("film_copy", () -> new SimpleCraftingRecipeSerializer<>(FilmCopyRecipe::new));
    public static final RegistrySupplier<RecipeSerializer<PhotographReplaceImageRecipe>> PHOTOGRAPH_REPLACE_IMAGE =
            RECIPE_SERIALIZERS.register("photograph_replace_image", () -> new SimpleCraftingRecipeSerializer<>(PhotographReplaceImageRecipe::new));
    public static final RegistrySupplier<RecipeSerializer<PhotographToFilmRecipe>> PHOTOGRAPH_TO_FILM =
            RECIPE_SERIALIZERS.register("photograph_to_film", () -> new SimpleCraftingRecipeSerializer<>(PhotographToFilmRecipe::new));
    public static final RegistrySupplier<RecipeSerializer<FeatherFanRecipe>> FEATHER_FAN = RECIPE_SERIALIZERS.register("feather_fan", () -> new SimpleCraftingRecipeSerializer<>(FeatherFanRecipe::new));


    private NeoGuanNiaoRecipeSerializers() {
    }
}

