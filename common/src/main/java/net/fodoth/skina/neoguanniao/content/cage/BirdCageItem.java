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
import net.minecraft.world.item.context.BlockPlaceContext;
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
import net.fodoth.skina.neoguanniao.platform.ClientConfigHooks;
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

    /**
     * 放置失败提示：仅在服务端、且玩家瞄准的是可替换方块（即确实是体积不够，而不是
     * 在对着已经放好的鸟笼重复右键）时提示一次，避免放置成功后继续按住右键刷屏。
     * <p>
     * {@code getStateForPlacement} 会在客户端预测等场合被调用，不能在里面对玩家发消息。
     * </p>
     */
    @Override
    public @NotNull InteractionResult place(@NotNull BlockPlaceContext context) {
        if (!context.getLevel().isClientSide
                && getBlock() instanceof BirdCageBlock cage
                && context.getLevel().getBlockState(context.getClickedPos()).canBeReplaced(context)
                && !cage.hasRoomFor(context)) {
            Player player = context.getPlayer();
            if (player != null) {
                player.displayClientMessage(Component.translatable(
                        "message.neoguanniao.bird_cage.place_failed", 3, cage.structureHeight(), 3), true);
            }
            return InteractionResult.FAIL;
        }
        return super.place(context);
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
        tooltip.addAll(birdLines(capturedBirds(stack)));
    }

    /**
     * 把每只笼中实体格式化成提示行（槽位 + 实体名 / 注册名 / 生命值），
     * 槽位之间空一行；注册名与生命值行由配置开关控制。
     * 物品提示与 Jade 共用同一份实现。
     */
    public static List<Component> birdLines(List<CompoundTag> birds) {
        List<Component> lines = new ArrayList<>();
        for (int i = 0; i < birds.size(); i++) {
            if (i > 0) lines.add(Component.empty());
            CompoundTag bird = birds.get(i);
            lines.add(Component.translatable("item.neoguanniao.bird_cage.slot",
                            BirdCageBlockEntity.slotOf(bird, i) + 1).withStyle(ChatFormatting.GOLD)
                    .append(Component.literal(": ").withStyle(ChatFormatting.GOLD))
                    .append(birdName(bird).copy().withStyle(ChatFormatting.AQUA)));
            String id = bird.getString("id");
            if (ClientConfigHooks.showCageRegistryName()) {
                lines.add(Component.translatable("item.neoguanniao.bird_cage.registry").withStyle(ChatFormatting.YELLOW)
                        .append(Component.literal(": ").withStyle(ChatFormatting.YELLOW))
                        .append(Component.literal(id).withStyle(ChatFormatting.WHITE)));
            }
            if (ClientConfigHooks.showCageHealth()) {
                float health = bird.contains("Health") ? bird.getFloat("Health") : 0.0F;
                float maxHealth = bird.contains("MaxHealth") ? bird.getFloat("MaxHealth") : health;
                lines.add(Component.translatable("item.neoguanniao.bird_cage.health").withStyle(ChatFormatting.YELLOW)
                        .append(Component.literal(": ").withStyle(ChatFormatting.YELLOW))
                        .append(Component.literal(formatHealth(health) + "/" + formatHealth(maxHealth)).withStyle(ChatFormatting.WHITE)));
            }
        }
        return lines;
    }

    private static Component birdName(CompoundTag bird) {
        if (bird.contains("CustomName")) {
            try {
                Component name = Component.Serializer.fromJson(bird.getString("CustomName"), RegistryAccess.EMPTY);
                if (name != null) return name;
            } catch (Exception ignored) {
            }
        }
        String id = bird.getString("id");
        return id.isEmpty()
                ? Component.translatable("entity.minecraft.generic")
                : Component.translatable("entity." + id.replace(':', '.'));
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
            List<CompoundTag> birds = capturedBirds(stack);
            // 鸟笼物品没有朝向，笼位只影响它在笼中的站位，挑编号最小的空笼位即可。
            tag.putInt(BirdCageBlockEntity.SLOT_KEY, BirdCageBlockEntity.freeSlot(birds, variant.capacity(), -1));
            ListTag list = new ListTag();
            for (CompoundTag bird : birds) list.add(bird.copy());
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
