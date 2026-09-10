package net.fodoth.skina.neoguanniao.content.cage;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.platform.ConfigHooks;


import org.jetbrains.annotations.NotNull;

import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;


public class BirdCageItem extends BlockItem implements GeoItem, Equipable {

    private final BirdCageVariant variant;

    private final AnimatableInstanceCache animationCache =
            GeckoLibUtil.createInstanceCache(this);


    public BirdCageItem(
            BirdCageVariant variant,
            Block block,
            Item.Properties properties
    ) {
        super(block, properties);
        this.variant = variant;
    }


    public BirdCageVariant variant() {
        return variant;
    }

    @Override public @NotNull EquipmentSlot getEquipmentSlot() { return variant == BirdCageVariant.SMALL ? EquipmentSlot.HEAD : EquipmentSlot.MAINHAND; }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        if (variant != BirdCageVariant.SMALL || isFull(player.getItemInHand(hand))) {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }
        return swapWithEquipmentSlot(this, level, player, hand);
    }

    public boolean isFull(ItemStack stack) {
        return capturedBirds(stack).size() >= variant.capacity();
    }

    /** 读取鸟笼物品中按捕捉顺序排列的实体 NBT；兼容只存一只的旧格式。 */
    public static List<CompoundTag> capturedBirds(ItemStack stack) {
        CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        List<CompoundTag> birds = new ArrayList<>();
        if (data.contains("CapturedBirds", Tag.TAG_LIST)) {
            ListTag list = data.getList("CapturedBirds", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) birds.add(list.getCompound(i));
        } else if (data.contains("CapturedBird")) {
            birds.add(data.getCompound("CapturedBird"));
        }
        return birds;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context,
                                @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        List<CompoundTag> birds = capturedBirds(stack);
        for (int i = 0; i < birds.size(); i++) {
            if (i > 0) tooltip.add(Component.empty());
            appendBirdTooltip(tooltip, birds.get(i));
        }
    }

    private static void appendBirdTooltip(List<Component> tooltip, CompoundTag bird) {
        Component name = null;
        if (bird.contains("CustomName")) {
            try { name = Component.Serializer.fromJson(bird.getString("CustomName"), RegistryAccess.EMPTY); }
            catch (Exception ignored) { }
        }
        if (name == null) {
            String id = bird.getString("id");
            name = id.isEmpty() ? Component.translatable("entity.minecraft.generic") : Component.translatable("entity." + id.replace(':', '.'));
        }
        tooltip.add(Component.translatable("item.neoguanniao.bird_cage.contains").withStyle(ChatFormatting.GOLD)
                .append(Component.literal(": ").withStyle(ChatFormatting.GOLD))
                .append(name.copy().withStyle(ChatFormatting.AQUA)));
        String id = bird.getString("id");
        tooltip.add(Component.translatable("item.neoguanniao.bird_cage.registry").withStyle(ChatFormatting.GOLD)
                .append(Component.literal(": ").withStyle(ChatFormatting.GOLD))
                .append(Component.literal(id).withStyle(ChatFormatting.AQUA)));
        float health = bird.contains("Health") ? bird.getFloat("Health") : 0.0F;
        float maxHealth = bird.contains("MaxHealth") ? bird.getFloat("MaxHealth") : health;
        tooltip.add(Component.translatable("item.neoguanniao.bird_cage.health").withStyle(ChatFormatting.GOLD)
                .append(Component.literal(": ").withStyle(ChatFormatting.GOLD))
                .append(Component.literal(formatHealth(health) + "/" + formatHealth(maxHealth)).withStyle(ChatFormatting.AQUA)));
    }

    private static String formatHealth(float value) {
        return value == (int) value ? Integer.toString((int) value) : String.format(Locale.ROOT, "%.1f", value);
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        return capture(stack, player, target);
    }

    public InteractionResult capture(ItemStack stack, Player player, Entity target) {
        if (isFull(stack) || !canFit(target) || !canCapture(target)) return InteractionResult.PASS;
        if (!player.level().isClientSide) {
            CompoundTag tag = new CompoundTag();
            target.saveWithoutId(tag);
            tag.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(target.getType()).toString());
            if (target instanceof LivingEntity living) tag.putFloat("MaxHealth", living.getMaxHealth());
            ListTag list = new ListTag();
            for (CompoundTag bird : capturedBirds(stack)) list.add(bird.copy());
            list.add(tag);
            CustomData.update(DataComponents.CUSTOM_DATA, stack, t -> {
                t.remove("CapturedBird");
                t.put("CapturedBirds", list);
            });
            target.discard();
        }
        return InteractionResult.sidedSuccess(player.level().isClientSide);
    }
    static boolean canCapture(Entity entity) {
        if (ConfigHooks.birdCagesAllowAllEntities()) return true;
        if (!(entity instanceof LivingEntity)) return false;
        if (entity instanceof Enemy) return ConfigHooks.birdCagesAllowHostile();
        if (entity instanceof NeutralMob) return ConfigHooks.birdCagesAllowHostile() || ConfigHooks.birdCagesAllowNeutral();
        return ConfigHooks.birdCagesAllowFriendly() || entity instanceof AbstractBirdEntity<?>;
    }

    boolean canFit(Entity entity) {
        return Math.max(entity.getBbWidth(), entity.getBbHeight()) <= ConfigHooks.birdCageMaxEntitySize(variant.ordinal());
    }


    @Override
    public void registerControllers(
            AnimatableManager.ControllerRegistrar controllers
    ) {

    }


    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animationCache;
    }
}
