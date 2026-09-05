package com.birdcamera.content.camera;

import com.birdcamera.registry.BirdCameraItems;
import com.birdcamera.registry.BirdCameraRecipeSerializers;
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

/**
 * 胶片 → 相框相片 特殊合成（中间胶片，周围八张纸）（迁移自 guaniao-2.1.3）。
 */
public class FilmToPhotographRecipe extends CustomRecipe {
    public FilmToPhotographRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, @NotNull Level level) {
        return input.width() == 3 && input.height() == 3 && !this.findFilm(input).isEmpty();
    }

    @Override
    @NotNull
    public ItemStack assemble(CraftingInput input, @NotNull HolderLookup.Provider registries) {
        ItemStack film = this.findFilm(input);
        if (film.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack result = new ItemStack(BirdCameraItems.PHOTOGRAPH);
        PhotographData.copyImage(film, result);
        if (film.has(DataComponents.CUSTOM_NAME)) {
            result.set(DataComponents.CUSTOM_NAME,
                    Component.translatable("item.birdcamera.photograph.named", film.getHoverName().getString()));
        }
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 3 && height >= 3;
    }

    @Override
    @NotNull
    public ItemStack getResultItem(@NotNull HolderLookup.Provider registries) {
        return new ItemStack(BirdCameraItems.PHOTOGRAPH);
    }

    @Override
    @NotNull
    public RecipeSerializer<?> getSerializer() {
        return BirdCameraRecipeSerializers.FILM_TO_PHOTOGRAPH;
    }

    private ItemStack findFilm(CraftingInput input) {
        if (input.width() == 3 && input.height() == 3) {
            ItemStack film = ItemStack.EMPTY;
            for (int slot = 0; slot < input.size(); slot++) {
                ItemStack stack = input.getItem(slot);
                if (slot == 4) {
                    if (!stack.is(BirdCameraItems.FILM) || !PhotographData.hasImage(stack)) {
                        return ItemStack.EMPTY;
                    }
                    film = stack;
                } else if (!stack.is(Items.PAPER)) {
                    return ItemStack.EMPTY;
                }
            }
            return film;
        }
        return ItemStack.EMPTY;
    }
}