package net.fodoth.skina.neoguanniao.content.fan;

import net.fodoth.skina.neoguanniao.content.feather.BirdFeatherItem;
import net.fodoth.skina.neoguanniao.registry.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.NotNull;

/**
 * 风翎扇的合成表：3x3 有形配方（FFF / FFF / ESE，F 任意鸟羽、E 回响碎片、S 木棍）。
 * 继承 ShapedRecipe 是为了让配方查看器（观鸟手册的合成表页、JEI、EMI）按「有形配方」渲染，
 * 只有 assemble 被改写：把合成格里的羽毛数据存进扇子，决定攻击伤害与攻击距离。
 */
public final class FeatherFanRecipe extends ShapedRecipe {
    private static final int FEATHER_SLOTS = 6;

    /** 自留一份，父类的 pattern / result 没有公开的读取方法，序列化时需要。 */
    private final ShapedRecipePattern pattern;
    private final ItemStack result;

    public FeatherFanRecipe(String group, CraftingBookCategory category, ShapedRecipePattern pattern,
                            ItemStack result, boolean showNotification) {
        super(group, category, pattern, result, showNotification);
        this.pattern = pattern;
        this.result = result;
    }

    public ShapedRecipePattern pattern() {
        return this.pattern;
    }

    public ItemStack result() {
        return this.result;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull CraftingInput in, HolderLookup.@NotNull Provider r) {
        ItemStack out = new ItemStack(NeoGuanNiaoItems.WIND_FEATHER_FAN.get());
        ListTag list = new ListTag();
        for (int i = 0; i < in.width() * in.height() && list.size() < FEATHER_SLOTS; i++) {
            var d = BirdFeatherItem.getFeatherData(in.getItem(i));
            if (d != null) {
                CompoundTag t = new CompoundTag();
                t.putString("bird_type", d.birdType().toString());
                t.putInt("rarity", d.rarity());
                list.add(t);
            }
        }
        CustomData.update(DataComponents.CUSTOM_DATA, out, t -> t.put("Feathers", list));
        return out;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return FeatherFanRecipeSerializer.INSTANCE;
    }
}
