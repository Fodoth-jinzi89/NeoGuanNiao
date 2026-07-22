package net.fodoth.skina.neoguanniao.content.feather;

import net.fodoth.skina.neoguanniao.content.bird.core.skin.BirdSkinRarity;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 鸟类羽毛物品，用于存储和展示鸟类羽毛数据
 */
public class BirdFeatherItem extends Item {

    public BirdFeatherItem(Properties properties) {
        super(properties);
    }

    // ======================== 数据操作 ========================

    /** 将羽毛数据设置到物品栈中 */
    public static void setFeatherData(ItemStack stack, BirdFeatherData data) {
        stack.set(NeoGuanNiaoDataComponents.BIRD_FEATHER_DATA.get(), data);
        stack.set(NeoGuanNiaoDataComponents.BIRD_FEATHER_BIRD_TYPE.get(), data.toTypeInt());
        stack.set(NeoGuanNiaoDataComponents.BIRD_FEATHER_SKIN_RARITY.get(), data.rarity());
    }

    /** 从物品栈中获取羽毛数据 */
    public static BirdFeatherData getFeatherData(ItemStack stack) {
        return stack.get(NeoGuanNiaoDataComponents.BIRD_FEATHER_DATA.get());
    }

    // ======================== 提示文本 ========================

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        BirdFeatherData data = getFeatherData(stack);
        if (data == null) {
            tooltip.add(Component.translatable("tooltip.neoguanniao.empty_feather"));
            return;
        }

        // 鸟类类型
        tooltip.add(Component.translatable("tooltip.neoguanniao.bird_type")
                .append(Component.translatable("entity." + data.birdType().getNamespace() + "." + data.birdType().getPath())));

        // 稀有度（带颜色）
        BirdSkinRarity rarity = BirdSkinRarity.fromValue(data.rarity());
        Component rarityText = Component.translatable("tooltip.neoguanniao.rarity." + rarity.getTranslationKey())
                .withStyle(rarity.getChatColor());
        tooltip.add(Component.translatable("tooltip.neoguanniao.rarity").append(rarityText));
    }
}