package net.fodoth.skina.neoguanniao.content.fan;

import net.fodoth.skina.neoguanniao.content.feather.BirdFeatherItem;
import net.fodoth.skina.neoguanniao.registry.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public final class FeatherFanRecipe extends CustomRecipe {
    public FeatherFanRecipe(CraftingBookCategory c) {
        super(c);
    }

    public boolean matches(@NotNull CraftingInput in, @NotNull Level l) {
        if (in.width() != 3 || in.height() != 3) return false;
        for (int i = 0; i < 9; i++) {
            ItemStack s = in.getItem(i);
            if (i < 6 && !s.is(NeoGuanNiaoItems.BIRD_FEATHER.get())) return false;
            if (i >= 6 && ((i == 7 && !s.is(Items.STICK)) || ((i == 6 || i == 8) && !s.is(Items.ECHO_SHARD))))
                return false;
        }
        return true;
    }

    public @NotNull ItemStack assemble(@NotNull CraftingInput in, HolderLookup.@NotNull Provider r) {
        ItemStack out = new ItemStack(NeoGuanNiaoItems.WIND_FEATHER_FAN.get());
        ListTag list = new ListTag();
        for (int i = 0; i < 6; i++) {
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

    public boolean canCraftInDimensions(int w, int h) {
        return w >= 3 && h >= 3;
    }

    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider r) {
        return new ItemStack(NeoGuanNiaoItems.WIND_FEATHER_FAN.get());
    }

    public @NotNull RecipeSerializer<?> getSerializer() {
        return NeoGuanNiaoRecipeSerializers.FEATHER_FAN.get();
    }
}
