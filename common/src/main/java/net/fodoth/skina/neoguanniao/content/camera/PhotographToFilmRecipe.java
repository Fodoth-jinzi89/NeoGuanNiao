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

/** Takes a framed photograph apart: the image becomes a film again and the frame material is lost. */
public final class PhotographToFilmRecipe extends CustomRecipe {

    public PhotographToFilmRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(@NotNull CraftingInput input, @NotNull Level level) {
        return findPhotograph(input) != null;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull CraftingInput input, HolderLookup.@NotNull Provider registries) {
        ItemStack photograph = findPhotograph(input);
        if (photograph == null) {
            return ItemStack.EMPTY;
        }
        ItemStack result = new ItemStack(NeoGuanNiaoItems.FILM.get());
        PhotographData.copyImage(photograph, result);
        // 胶片不保留相框信息（框已拆掉）。
        CameraItemData.update(result, tag -> {
            tag.remove(PhotographData.TAG_FRAME_BLOCK);
            tag.remove(PhotographData.TAG_FRAME_SIZE);
        });
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 1;
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registries) {
        return new ItemStack(NeoGuanNiaoItems.FILM.get());
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return NeoGuanNiaoRecipeSerializers.PHOTOGRAPH_TO_FILM.get();
    }

    /**
     * 解析一次合成：恰好一张带影像的相框相片，其余槽位必须为空。
     *
     * @return 相框相片；不匹配时返回 null
     */
    private static ItemStack findPhotograph(CraftingInput input) {
        ItemStack photograph = ItemStack.EMPTY;
        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (photograph.isEmpty() && stack.is(NeoGuanNiaoItems.PHOTOGRAPH.get()) && PhotographData.hasImage(stack)) {
                photograph = stack;
            } else {
                return null;
            }
        }
        return photograph.isEmpty() ? null : photograph;
    }
}
