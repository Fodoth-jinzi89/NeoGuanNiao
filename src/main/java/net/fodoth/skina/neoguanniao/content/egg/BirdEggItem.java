package net.fodoth.skina.neoguanniao.content.egg;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.data.BirdData;
import net.fodoth.skina.neoguanniao.content.bird.core.skin.BirdSkin;
import net.fodoth.skina.neoguanniao.content.bird.core.skin.BirdSkinRarity;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoBirdData;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 鸟类蛋物品，用于存储和孵化鸟类数据
 */
public class BirdEggItem extends Item {

    public BirdEggItem(Properties properties) {
        super(properties);
    }

    // ======================== 数据操作 ========================

    /** 将蛋数据设置到物品栈中 */
    public static void setEggData(ItemStack stack, BirdEggData data) {
        stack.set(NeoGuanNiaoDataComponents.BIRD_EGG_DATA.get(), data);
    }

    /** 从物品栈中获取蛋数据 */
    public static BirdEggData getEggData(ItemStack stack) {
        return stack.get(NeoGuanNiaoDataComponents.BIRD_EGG_DATA.get());
    }

    // ======================== 提示文本 ========================

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Item.@NotNull TooltipContext context,
                                @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        BirdEggData data = getEggData(stack);
        if (data == null) {
            tooltip.add(Component.translatable("tooltip.neoguanniao.empty_egg"));
            return;
        }

        // 鸟类类型
        tooltip.add(Component.translatable("tooltip.neoguanniao.bird_type")
                .append(translateResource("entity", data.birdType())));

        // 模型
        tooltip.add(Component.translatable("tooltip.neoguanniao.model")
                .append(translateResource("model", data.model())));

        // 皮肤（带稀有度颜色）
        BirdSkinRarity rarity = getSkinRarity(data);
        Component skinText = ((MutableComponent) translateResource("skin", data.skin()))
                .withStyle(rarity.getChatColor());
        tooltip.add(Component.translatable("tooltip.neoguanniao.skin").append(skinText));

        // 体型大小
        tooltip.add(Component.translatable("tooltip.neoguanniao.size",
                String.format("%.4f", data.size())));

        // 孵化时间
        tooltip.add(Component.translatable("tooltip.neoguanniao.hatch_time", data.hatchTime()));

        // 存活状态
        tooltip.add(Component.translatable(data.alive() ? "tooltip.neoguanniao.alive"
                : "tooltip.neoguanniao.dead"));
    }

    /** 翻译资源路径（去除目录和后缀） */
    private static Component translateResource(String prefix, ResourceLocation id) {
        String path = id.getPath();
        path = path.substring(path.lastIndexOf('/') + 1); // 去目录
        if (path.endsWith(".geo.json")) {
            path = path.substring(0, path.length() - ".geo.json".length());
        } else if (path.endsWith(".png")) {
            path = path.substring(0, path.length() - ".png".length());
        }
        return Component.translatable(prefix + "." + id.getNamespace() + "." + path);
    }

    // ======================== 交互（孵化） ========================

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        // 仅创造模式玩家可孵化
        if (player == null || !player.getAbilities().instabuild) return InteractionResult.FAIL;

        Level level = context.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;

        ItemStack stack = context.getItemInHand();
        BirdEggData data = getEggData(stack);
        if (data == null || !data.alive()) return InteractionResult.FAIL;

        // 获取实体类型
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(data.birdType());
        if (!(type.create(level) instanceof AbstractBirdEntity<?> bird)) {
            return InteractionResult.FAIL;
        }

        // 放置位置（点击面的外侧）
        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());

        // 设置位置和旋转
        bird.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                context.getRotation(), 0);

        // 应用蛋数据
        bird.applyEggData(data);
        // 潜行时变为幼年
        if (player.isShiftKeyDown()) {
            bird.setAge(-24000);
        }
        level.addFreshEntity(bird);

        return InteractionResult.CONSUME;
    }

    // ======================== 工具方法 ========================

    /** 根据蛋数据获取皮肤稀有度 */
    private BirdSkinRarity getSkinRarity(BirdEggData data) {
        for (var holder : NeoGuanNiaoBirdData.BIRD_DATA.getEntries()) {
            if (!holder.getId().equals(data.birdType())) continue;
            BirdData birdData = holder.get();
            for (BirdSkin skin : birdData.model().birdSkin()) {
                if (skin.id().equals(data.skin())) {
                    return skin.rarity();
                }
            }
        }
        return BirdSkinRarity.COMMON;
    }
}