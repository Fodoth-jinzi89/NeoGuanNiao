package net.fodoth.skina.neoguanniao.content.camera;

import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoItems;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoRecipeSerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/** Puts a film's image into a framed photograph, keeping the frame and replacing the stored image. */
public final class PhotographReplaceImageRecipe extends CustomRecipe {

    public PhotographReplaceImageRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(@NotNull CraftingInput input, @NotNull Level level) {
        return resolve(input) != null;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull CraftingInput input, HolderLookup.@NotNull Provider registries) {
        Swap swap = resolve(input);
        if (swap == null) {
            return ItemStack.EMPTY;
        }
        ItemStack result = new ItemStack(NeoGuanNiaoItems.PHOTOGRAPH.get());
        PhotographData.copyImage(swap.film(), result);
        PhotographData.setFrameBlock(result, PhotographData.frameBlock(swap.photograph()));
        PhotographData.setFrameSize(result, PhotographData.frameSize(swap.photograph()));
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registries) {
        return new ItemStack(NeoGuanNiaoItems.PHOTOGRAPH.get());
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return NeoGuanNiaoRecipeSerializers.PHOTOGRAPH_REPLACE_IMAGE.get();
    }

    /**
     * 解析一次合成：恰好一张相框相片 + 一张带影像的胶片，其余槽位必须为空。
     *
     * @return 相框相片与胶片；不匹配时返回 null
     */
    private static Swap resolve(CraftingInput input) {
        ItemStack photograph = ItemStack.EMPTY;
        ItemStack film = ItemStack.EMPTY;
        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (photograph.isEmpty() && stack.is(NeoGuanNiaoItems.PHOTOGRAPH.get())) {
                photograph = stack;
            } else if (film.isEmpty() && stack.is(NeoGuanNiaoItems.FILM.get()) && PhotographData.hasImage(stack)) {
                film = stack;
            } else {
                return null;
            }
        }
        if (photograph.isEmpty() || film.isEmpty()) {
            return null;
        }
        return new Swap(photograph, film);
    }

    private record Swap(ItemStack photograph, ItemStack film) {
    }
}
