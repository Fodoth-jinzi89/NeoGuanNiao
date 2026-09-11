package net.fodoth.skina.neoguanniao.content.camera;

import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoItems;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoRecipeSerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/** Mounts or reframes a captured film with identical blocks, producing a 1x1, 2x2 or 3x3 framed photograph. */
public final class FilmToPhotographRecipe extends CustomRecipe {

    public FilmToPhotographRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(@NotNull CraftingInput input, @NotNull Level level) {
        return resolve(input) != null;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull CraftingInput input, HolderLookup.@NotNull Provider registries) {
        Mount mount = resolve(input);
        if (mount == null) {
            return ItemStack.EMPTY;
        }
        ItemStack result = new ItemStack(NeoGuanNiaoItems.PHOTOGRAPH.get());
        PhotographData.copyImage(mount.source(), result);
        PhotographData.setFrameBlock(result, mount.frameBlock());
        PhotographData.setFrameSize(result, mount.size());
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

    /**
     * 解析一次合成：一张带影像的胶片/相片，加上 1~8 个同种方块，其余槽位必须为空。
     * 方块数量即相框边长：N 个方块 → NxN（1x1 ~ 8x8）。
     */
    private static Mount resolve(CraftingInput input) {
        ItemStack source = ItemStack.EMPTY;
        ItemStack frame = ItemStack.EMPTY;
        int frameCount = 0;
        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (source.isEmpty() && (stack.is(NeoGuanNiaoItems.FILM.get()) || stack.is(NeoGuanNiaoItems.PHOTOGRAPH.get())) && PhotographData.hasImage(stack)) {
                source = stack;
            } else if (stack.getItem() instanceof BlockItem && (frame.isEmpty() || stack.is(frame.getItem()))) {
                if (frame.isEmpty()) {
                    frame = stack;
                }
                frameCount++;
            } else {
                return null;
            }
        }
        if (source.isEmpty() || frameCount < PhotographData.MIN_FRAME_SIZE || frameCount > PhotographData.MAX_FRAME_SIZE) {
            return null;
        }
        return new Mount(source, BuiltInRegistries.BLOCK.getKey(((BlockItem)frame.getItem()).getBlock()), frameCount);
    }

    private record Mount(ItemStack source, ResourceLocation frameBlock, int size) {
    }
}

