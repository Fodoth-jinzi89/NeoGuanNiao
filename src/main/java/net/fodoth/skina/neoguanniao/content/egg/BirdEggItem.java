package net.fodoth.skina.neoguanniao.content.egg;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.data.BirdData;
import net.fodoth.skina.neoguanniao.content.bird.core.skin.BirdSkin;
import net.fodoth.skina.neoguanniao.content.bird.core.skin.BirdSkinRarity;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoBirdData;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoBirdModels;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoBirdSkins;
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

import static net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoDataComponents.*;

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
        stack.set(BIRD_EGG_RARITY.get(), NeoGuanNiaoBirdSkins.get(data.skin()).rarity().getRarity());
        stack.set(BIRD_EGG_MODEL_RARITY.get(), NeoGuanNiaoBirdModels.get(data.model()).rarity().getRarity());
        stack.set(BIRD_EGG_GENDER.get(), data.gender() ? 0 : 1);
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
                .append(translateResource("entity", data.birdType(), data.birdType())));

        // 模型
        tooltip.add(Component.translatable("tooltip.neoguanniao.model")
                .append(translateResource("model", data.model(), data.birdType())));

        // 皮肤（带稀有度颜色）
        int rarityId = stack.getOrDefault(
                BIRD_EGG_RARITY.get(),
                BirdSkinRarity.COMMON.getRarity()
        );

        BirdSkinRarity rarity = BirdSkinRarity.byRarity(rarityId);
        String skinId = data.skin().getPath();
        // 移除 _male 和 _female 后缀
        skinId = skinId.replaceAll("_(male|female)$", "");
        ResourceLocation cleanedSkinId = ResourceLocation.fromNamespaceAndPath(
                data.skin().getNamespace(),
                skinId
        );
        Component skinText = ((MutableComponent) translateResource("skin", cleanedSkinId, data.birdType()))
                .withStyle(rarity.getChatColor());
        tooltip.add(Component.translatable("tooltip.neoguanniao.skin").append(skinText));

        // 性别
        tooltip.add(Component.translatable(data.gender() ? "tooltip.neoguanniao.male"
                : "tooltip.neoguanniao.female"));

        // 蛋数量
        tooltip.add(Component.translatable("tooltip.neoguanniao.egg_count",
                data.eggCount()));

        // 体型大小
        tooltip.add(Component.translatable("tooltip.neoguanniao.size",
                String.format("%.4f", data.size())));

        // 孵化时间
        tooltip.add(
                Component.translatable("tooltip.neoguanniao.hatch_time")
                        .append(formatHatchTime(data.hatchTime()))
        );

        // 存活状态
        tooltip.add(Component.translatable(data.alive() ? "tooltip.neoguanniao.alive"
                : "tooltip.neoguanniao.dead"));
    }

    /**
     * 翻译资源路径（去除目录和后缀）
     * 生成：
     * entity.neoguanniao.budgerigar
     * model.neoguanniao.budgerigar.default
     * skin.neoguanniao.budgerigar.yellow
     */
    private static Component translateResource(String prefix, ResourceLocation id, ResourceLocation birdType) {
        String path = id.getPath();
        path = path.substring(path.lastIndexOf('/') + 1); // 去目录

        if (path.endsWith(".geo.json")) {
            path = path.substring(0, path.length() - ".geo.json".length());
        } else if (path.endsWith(".png")) {
            path = path.substring(0, path.length() - ".png".length());
        }

        String entityName = birdType.getPath();

        // entity 本身不重复添加实体名
        if ("entity".equals(prefix)) {
            return Component.translatable(prefix + "." + id.getNamespace() + "." + entityName);
        }

        return Component.translatable(
                prefix + "." + id.getNamespace() + "." + entityName + "." + path
        );
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
    public static BirdSkinRarity getSkinRarity(BirdEggData data) {
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

    /**
     * 将 Minecraft tick 转换为 时:分:秒
     */
    private static Component formatHatchTime(int ticks) {
        int totalSeconds = ticks / 20;

        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        MutableComponent result = Component.empty();

        if (hours > 0) {
            result.append(Component.translatable(
                    "tooltip.neoguanniao.time.hour",
                    hours
            ));
        }

        if (minutes > 0) {
            if (!result.getString().isEmpty()) {
                result.append(" ");
            }
            result.append(Component.translatable(
                    "tooltip.neoguanniao.time.minute",
                    minutes
            ));
        }

        if (seconds > 0 || result.getString().isEmpty()) {
            if (!result.getString().isEmpty()) {
                result.append(" ");
            }
            result.append(Component.translatable(
                    "tooltip.neoguanniao.time.second",
                    seconds
            ));
        }

        return result;
    }
}