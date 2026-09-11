package net.fodoth.skina.neoguanniao.compat;

import java.util.ArrayList;
import java.util.List;
import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.content.camera.PhotographData;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

/**
 * JEI/EMI 展示用的配方视图（只用原版类型，不引用任何配方查看器 API），
 * 供各加载器的 JEI/EMI 插件翻译成自己的配方对象。
 */
public final class PhotoRecipeDisplays {

    /** 一条展示：唯一 id + 每个合成格可接受的物品 + 输出。 */
    public record Display(ResourceLocation id, List<List<ItemStack>> inputs, ItemStack output) {

        /** 把合成格按最多 3 列排成 JEI 形状配方用的行字符串。 */
        public List<String> patternRows() {
            int width = Math.min(3, this.inputs.size());
            List<String> rows = new ArrayList<>();
            for (int start = 0; start < this.inputs.size(); start += width) {
                StringBuilder row = new StringBuilder();
                for (int index = start; index < Math.min(start + width, this.inputs.size()); index++) {
                    row.append((char)('A' + index));
                }
                rows.add(row.toString());
            }
            return rows;
        }
    }

    private PhotoRecipeDisplays() {
    }

    /**
     * 今天新增的相机相关自定义配方（1x1/8x8 各取一例）。
     * <p>
     * id 的 path 以 {@code /} 开头（{@code neoguanniao:/display/...}）是 EMI 对「非数据驱动/合成配方」的要求：
     * 配方 id 不在配方管理器里时，必须用这种 synthetic 形式，否则 EMI 会报 “未能在配方管理器中找到”。
     * </p>
     */
    public static List<Display> all() {
        List<ItemStack> anyBlock = anyBlockStacks();
        List<ItemStack> film = List.of(new ItemStack(NeoGuanNiaoItems.FILM.get()));
        List<ItemStack> blankFilm = List.of(new ItemStack(NeoGuanNiaoItems.BLANK_FILM.get()));
        List<ItemStack> photograph = List.of(new ItemStack(NeoGuanNiaoItems.PHOTOGRAPH.get()));

        List<Display> displays = new ArrayList<>();
        displays.add(new Display(NeoGuanNiao.resource("/display/film_to_photograph_1x1"),
                List.of(film, anyBlock), framedPhotograph(1)));
        displays.add(new Display(NeoGuanNiao.resource("/display/film_to_photograph_8x8"),
                concat(List.of(film), repeat(anyBlock, 8)), framedPhotograph(8)));
        displays.add(new Display(NeoGuanNiao.resource("/display/film_copy_1"),
                List.of(film, blankFilm), filmStack(2)));
        displays.add(new Display(NeoGuanNiao.resource("/display/film_copy_8"),
                concat(List.of(film), repeat(blankFilm, 8)), filmStack(9)));
        displays.add(new Display(NeoGuanNiao.resource("/display/photograph_replace_image"),
                List.of(photograph, film), framedPhotograph(1)));
        displays.add(new Display(NeoGuanNiao.resource("/display/photograph_to_film"),
                List.of(photograph), filmStack(1)));
        return displays;
    }

    /** 配方里的「任意方块」。 */
    private static List<ItemStack> anyBlockStacks() {
        return BuiltInRegistries.ITEM.stream()
                .filter(item -> item instanceof BlockItem)
                .map(ItemStack::new)
                .filter(stack -> !stack.isEmpty())
                .toList();
    }

    /** 展示用的相框相片：带上边长与示例方块，方便 tooltip 看出尺寸。 */
    private static ItemStack framedPhotograph(int size) {
        ItemStack stack = new ItemStack(NeoGuanNiaoItems.PHOTOGRAPH.get());
        PhotographData.setFrameSize(stack, size);
        PhotographData.setFrameBlock(stack, BuiltInRegistries.BLOCK.getKey(Blocks.OAK_PLANKS));
        return stack;
    }

    private static ItemStack filmStack(int count) {
        ItemStack stack = new ItemStack(NeoGuanNiaoItems.FILM.get());
        stack.setCount(count);
        return stack;
    }

    private static List<List<ItemStack>> concat(List<List<ItemStack>> head, List<List<ItemStack>> tail) {
        List<List<ItemStack>> result = new ArrayList<>(head.size() + tail.size());
        result.addAll(head);
        result.addAll(tail);
        return result;
    }

    private static List<List<ItemStack>> repeat(List<ItemStack> slot, int times) {
        List<List<ItemStack>> result = new ArrayList<>(times);
        for (int i = 0; i < times; i++) {
            result.add(slot);
        }
        return result;
    }
}
