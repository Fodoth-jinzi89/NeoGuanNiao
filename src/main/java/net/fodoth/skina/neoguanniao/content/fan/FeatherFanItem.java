package net.fodoth.skina.neoguanniao.content.fan;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.jetbrains.annotations.NotNull;

public class FeatherFanItem extends Item {
    public static float attackDamage(ItemStack stack) {
        ListTag feathers = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getList("Feathers", 10);
        if (feathers.isEmpty()) return 6.0F;
        float value = 6.0F; java.util.Set<String> birds = new java.util.HashSet<>();
        for (int i = 0; i < Math.min(6, feathers.size()); i++) { CompoundTag f = feathers.getCompound(i); value += Math.min(3, Math.max(0, f.getInt("rarity"))) * 0.5F; birds.add(f.getString("bird_type")); }
        return (float)(value * Math.pow(1.15D, Math.max(0, birds.size() - 1)));
    }
    public void tryLaunchPiercing(ServerPlayer p) {
        if (!isFullyCharged(p)) return;
        ItemStack s = p.getMainHandItem();
        var x = new FeatherFanProjectileEntity(p.level(), p);
        x.configure(1f, "riven", attackDamage(s));
        x.shootFromRotation(p, p.getXRot(), p.getYRot(), 0, 2.65f, 0);
        p.level().addFreshEntity(x);
        s.hurtAndBreak(1, p, LivingEntity.getSlotForHand(p.getUsedItemHand()));
        p.releaseUsingItem();
    }

    public static boolean isFullyCharged(LivingEntity e) {
        return e.isUsingItem() && e.getUseItem().getItem() instanceof FeatherFanItem && e.getTicksUsingItem() >= 30;
    }


    public FeatherFanItem(Properties p) {
        super(p.durability(256));
    }


    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level l, Player p, @NotNull InteractionHand h) {
        ItemStack s = p.getItemInHand(h);
        p.startUsingItem(h);
        return InteractionResultHolder.consume(s);
    }

    public int getUseDuration(@NotNull ItemStack s, @NotNull LivingEntity e) {
        return 72000;
    }

    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack s) {
        return UseAnim.BOW;
    }

    public void releaseUsing(@NotNull ItemStack s, @NotNull Level l, @NotNull LivingEntity e, int left) {
        if (!(e instanceof Player p) || l.isClientSide) return;
        int u = 72000 - left;
        if (u < 5) return;
        float c = Math.min(1, (u - 5) / 25f);
        String mode = FeatherFanEnchantments.mode(s);
        if ("hunting".equals(mode)) {
            var targets = l.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(18), x -> x != p && x.isAlive());
            int n = Math.min(1 + (int) (c * 6), targets.size());
            for (int i = 0; i < n; i++) {
                var x = new FeatherFanProjectileEntity(l, p);
                x.configure(c, "hunting", attackDamage(s));
                x.shootFromRotation(p, p.getXRot(), p.getYRot(), 0, 1.75f, 0);
                l.addFreshEntity(x);
            }
        } else {
            var x = new FeatherFanProjectileEntity(l, p);
            x.configure(c, mode, attackDamage(s));
            x.shootFromRotation(p, p.getXRot(), p.getYRot(), 0, .8f + c * .8f, 0);
            l.addFreshEntity(x);
        }
        s.hurtAndBreak(1, p, LivingEntity.getSlotForHand(p.getUsedItemHand()));
    }
}
