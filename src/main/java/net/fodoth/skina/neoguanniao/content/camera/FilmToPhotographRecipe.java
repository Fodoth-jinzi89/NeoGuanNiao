package net.fodoth.skina.neoguanniao.content.camera;

import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoItems;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoRecipeSerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/** Mounts a captured film in a stick frame while preserving its photo reference. */
public final class FilmToPhotographRecipe extends CustomRecipe {

    public FilmToPhotographRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(@NotNull CraftingInput input, @NotNull Level level) {
        return !findFilm(input).isEmpty();
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull CraftingInput input, HolderLookup.@NotNull Provider registries) {
        ItemStack film = findFilm(input);
        if (film.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack result = new ItemStack(NeoGuanNiaoItems.PHOTOGRAPH.get());
        PhotographData.copyImage(film, result);
        Component name = film.get(DataComponents.CUSTOM_NAME);
        if (name != null) {
            result.set(DataComponents.CUSTOM_NAME, Component.translatable("item.neoguanniao.photograph.named", name));
        }
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 3 && height >= 3;
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registries) {
        return new ItemStack(NeoGuanNiaoItems.PHOTOGRAPH.get());
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return NeoGuanNiaoRecipeSerializers.FILM_TO_PHOTOGRAPH.get();
    }

    private static ItemStack findFilm(CraftingInput input) {
        if (input.width() != 3 || input.height() != 3) {
            return ItemStack.EMPTY;
        }
        ItemStack film = ItemStack.EMPTY;
        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (slot == 4) {
                if (!stack.is(NeoGuanNiaoItems.FILM.get()) || !PhotographData.hasImage(stack)) {
                    return ItemStack.EMPTY;
                }
                film = stack;
            } else if (!stack.is(Items.STICK)) {
                return ItemStack.EMPTY;
            }
        }
        return film;
    }
}
