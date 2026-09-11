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

/** Copies a captured film onto blank films: 1 film + 1~8 blank films -> 2~9 identical films. */
public final class FilmCopyRecipe extends CustomRecipe {

    /** 一次最多一起复印的空白胶片数（3x3 合成格除原片外还有 8 格）。 */
    public static final int MAX_BLANKS = 8;

    public FilmCopyRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(@NotNull CraftingInput input, @NotNull Level level) {
        return resolve(input) != null;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull CraftingInput input, HolderLookup.@NotNull Provider registries) {
        Copy copy = resolve(input);
        if (copy == null) {
            return ItemStack.EMPTY;
        }
        ItemStack result = copy.film().copy();
        result.setCount(1 + copy.blanks());
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height > 1;
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registries) {
        return new ItemStack(NeoGuanNiaoItems.FILM.get());
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return NeoGuanNiaoRecipeSerializers.FILM_COPY.get();
    }

    /**
     * 解析一次合成：恰好一张带影像的胶片 + 1~8 张空白胶片，其余槽位必须为空。
     *
     * @return 原片与空白胶片数量；不匹配时返回 null
     */
    private static Copy resolve(CraftingInput input) {
        ItemStack film = ItemStack.EMPTY;
        int blanks = 0;
        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.is(NeoGuanNiaoItems.BLANK_FILM.get())) {
                blanks++;
            } else if (film.isEmpty() && stack.is(NeoGuanNiaoItems.FILM.get()) && PhotographData.hasImage(stack)) {
                film = stack;
            } else {
                return null;
            }
        }
        if (film.isEmpty() || blanks < 1 || blanks > MAX_BLANKS) {
            return null;
        }
        return new Copy(film, blanks);
    }

    private record Copy(ItemStack film, int blanks) {
    }
}
