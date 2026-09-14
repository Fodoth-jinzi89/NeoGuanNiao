package net.fodoth.skina.neoguanniao.content.fan;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fodoth.skina.neoguanniao.registry.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

/**
 * 风翎扇合成表的序列化器：JSON 与网络上都沿用原版有形配方的 pattern / key / result 格式，
 * 这样其它按原版格式解析的工具（配方查看器、数据生成等）都能直接读懂。
 */
public final class FeatherFanRecipeSerializer implements RecipeSerializer<FeatherFanRecipe> {
    public static final FeatherFanRecipeSerializer INSTANCE = new FeatherFanRecipeSerializer();

    private static final MapCodec<FeatherFanRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("group", "").forGetter(FeatherFanRecipe::getGroup),
            Codec.STRING.optionalFieldOf("category", CraftingBookCategory.MISC.getSerializedName())
                    .xmap(FeatherFanRecipeSerializer::categoryOf, CraftingBookCategory::getSerializedName)
                    .forGetter(FeatherFanRecipe::category),
            ShapedRecipePattern.MAP_CODEC.forGetter(FeatherFanRecipe::pattern),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(FeatherFanRecipe::result),
            Codec.BOOL.optionalFieldOf("show_notification", true).forGetter(FeatherFanRecipe::showNotification)
    ).apply(instance, FeatherFanRecipe::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, FeatherFanRecipe> STREAM_CODEC = StreamCodec.of(
            (buffer, recipe) -> {
                buffer.writeUtf(recipe.getGroup());
                buffer.writeEnum(recipe.category());
                ShapedRecipePattern.STREAM_CODEC.encode(buffer, recipe.pattern());
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, recipe.result());
                buffer.writeBoolean(recipe.showNotification());
            },
            buffer -> new FeatherFanRecipe(buffer.readUtf(), buffer.readEnum(CraftingBookCategory.class),
                    ShapedRecipePattern.STREAM_CODEC.decode(buffer),
                    ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer), buffer.readBoolean()));

    private static CraftingBookCategory categoryOf(String name) {
        for (CraftingBookCategory value : CraftingBookCategory.values()) {
            if (value.getSerializedName().equals(name)) {
                return value;
            }
        }
        return CraftingBookCategory.MISC;
    }

    @Override
    public MapCodec<FeatherFanRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, FeatherFanRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
