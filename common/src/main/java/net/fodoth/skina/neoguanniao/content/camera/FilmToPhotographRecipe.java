package net.fodoth.skina.neoguanniao.content.camera;

import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoItems;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoRecipeSerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/** Mounts or reframes a captured film with one arbitrary block. */
public final class FilmToPhotographRecipe extends CustomRecipe {

    public FilmToPhotographRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(@NotNull CraftingInput input, @NotNull Level level) {
        return !findSource(input).isEmpty();
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull CraftingInput input, HolderLookup.@NotNull Provider registries) {
        ItemStack source = findSource(input);
        if (source.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack result = new ItemStack(NeoGuanNiaoItems.PHOTOGRAPH.get());
        PhotographData.copyImage(source, result);
        for (int slot = 0; slot < input.size(); slot++) {
            if (slot != 4 && input.getItem(slot).getItem() instanceof BlockItem blockItem) {
                PhotographData.setFrameBlock(result, net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(blockItem.getBlock()));
                break;
            }
        }
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 1 && height >= 1;
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registries) {
        return new ItemStack(NeoGuanNiaoItems.PHOTOGRAPH.get());
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return NeoGuanNiaoRecipeSerializers.FILM_TO_PHOTOGRAPH.get();
    }

    private static ItemStack findSource(CraftingInput input) {
        ItemStack source = ItemStack.EMPTY;
        ItemStack frame = ItemStack.EMPTY;
        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if ((stack.is(NeoGuanNiaoItems.FILM.get()) || stack.is(NeoGuanNiaoItems.PHOTOGRAPH.get())) && PhotographData.hasImage(stack) && source.isEmpty()) {
                source = stack;
            } else if (stack.getItem() instanceof BlockItem && frame.isEmpty()) {
                frame = stack;
            } else {
                return ItemStack.EMPTY;
            }
        }
        return source.isEmpty() || frame.isEmpty() ? ItemStack.EMPTY : source;
    }
}

