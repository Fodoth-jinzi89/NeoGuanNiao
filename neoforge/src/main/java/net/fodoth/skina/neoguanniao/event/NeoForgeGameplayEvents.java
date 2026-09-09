package net.fodoth.skina.neoguanniao.event;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.content.cage.BirdCageItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = NeoGuanNiao.MODID)
public final class NeoForgeGameplayEvents {
    private NeoForgeGameplayEvents() {}

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        FeatherFanCombatEvents.onDeath(event.getEntity(), event.getSource());
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (FeatherFanCombatEvents.onIncomingDamage(event.getEntity(), event.getSource())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (FeatherFanInteractionEvents.onRightClickItem(event.getEntity(), event.getItemStack())) {
            event.setCancellationResult(InteractionResult.sidedSuccess(event.getEntity().level().isClientSide));
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickEntity(PlayerInteractEvent.EntityInteract event) {
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof BirdCageItem cage)) return;
        InteractionResult result = cage.capture(stack, event.getEntity(), event.getTarget());
        if (result.consumesAction()) {
            event.getEntity().setItemInHand(event.getHand(), stack);
            event.setCancellationResult(result);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPickup(ItemEntityPickupEvent.Pre event) {
        BirdFeatherPickupHandler.onPickup(event.getPlayer(), event.getItemEntity());
    }

    @SubscribeEvent
    public static void playerTick(PlayerTickEvent.Post event) {
        SpyglassTickHandler.playerTick(event.getEntity());
    }
}
