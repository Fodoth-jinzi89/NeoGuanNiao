package net.fodoth.skina.neoguanniao.event;

import net.fodoth.skina.neoguanniao.content.fan.FeatherFanEnchantments;
import net.fodoth.skina.neoguanniao.content.fan.FeatherFanItem;
import net.fodoth.skina.neoguanniao.content.fan.FeatherFanProjectileEntity;
import net.minecraft.world.item.ItemStack;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoDataComponents;
import net.minecraft.world.entity.player.Player;

public final class FeatherFanInteractionEvents {
    private FeatherFanInteractionEvents() {
    }

    public static boolean onRightClickItem(Player player, ItemStack stack) {
        if (!player.isCrouching() || !(stack.getItem() instanceof FeatherFanItem fan)) {
            return false;
        }
        boolean flyingFan = player.level().getEntitiesOfClass(FeatherFanProjectileEntity.class,
                player.getBoundingBox().inflate(64.0), projectile -> projectile.getOwner() == player && projectile.isFlying()).stream().findAny().isPresent();
        if (!flyingFan) {
            return false;
        }
        int mode = (FeatherFanEnchantments.mode(stack) + 1) % 3;
        stack.set(NeoGuanNiaoDataComponents.FEATHER_FAN_MODE.get(), mode);
        return true;
    }
}
