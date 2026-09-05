package net.fodoth.skina.neoguanniao.event;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.content.fan.FeatherFanEnchantments;
import net.fodoth.skina.neoguanniao.content.fan.FeatherFanItem;
import net.fodoth.skina.neoguanniao.content.fan.FeatherFanProjectileEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = NeoGuanNiao.MODID)
public final class FeatherFanInteractionEvents {
    private FeatherFanInteractionEvents() {
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        if (!player.isCrouching() || !(event.getItemStack().getItem() instanceof FeatherFanItem fan)) {
            return;
        }
        boolean flyingFan = player.level().getEntitiesOfClass(FeatherFanProjectileEntity.class,
                player.getBoundingBox().inflate(64.0), projectile -> projectile.getOwner() == player && projectile.isFlying()).stream().findAny().isPresent();
        if (!flyingFan) {
            return;
        }
        int mode = (FeatherFanEnchantments.mode(event.getItemStack()) + 1) % 3;
        event.getItemStack().set(net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoDataComponents.FEATHER_FAN_MODE.get(), mode);
        event.setCancellationResult(InteractionResult.sidedSuccess(player.level().isClientSide));
        event.setCanceled(true);
    }
}
