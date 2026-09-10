package net.fodoth.skina.neoguanniao;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.fodoth.skina.neoguanniao.event.FeatherFanInteractionEvents;
import net.fodoth.skina.neoguanniao.event.PhotoTransferEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fodoth.skina.neoguanniao.command.PhotoAdminCommands;
import net.fodoth.skina.neoguanniao.content.bath.BirdBathAttraction;
import net.fodoth.skina.neoguanniao.content.cage.BirdCageItem;
import net.fodoth.skina.neoguanniao.event.SpyglassTickHandler;
import net.fodoth.skina.neoguanniao.event.FeatherFanCombatEvents;
import net.fodoth.skina.neoguanniao.network.PhotoUploadManager;

public final class NeoGuanNiaoFabricServerEvents {
    private NeoGuanNiaoFabricServerEvents() {}

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(PhotoUploadManager::tick);
        ServerTickEvents.END_SERVER_TICK.register(PhotoTransferEvents::onServerTick);
        ServerTickEvents.END_WORLD_TICK.register(level -> level.players().forEach(SpyglassTickHandler::playerTick));
        ServerWorldEvents.UNLOAD.register((server, level) -> BirdBathAttraction.clearLevel(level));
        ServerLivingEntityEvents.AFTER_DEATH.register(FeatherFanCombatEvents::onDeath);
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> !FeatherFanCombatEvents.onIncomingDamage(entity, source));
        UseItemCallback.EVENT.register((player, level, hand) -> {
            var stack = player.getItemInHand(hand);
            return FeatherFanInteractionEvents.onRightClickItem(player, stack)
                    ? InteractionResultHolder.sidedSuccess(stack, level.isClientSide())
                    : InteractionResultHolder.pass(stack);
        });
        UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
            var stack = player.getItemInHand(hand);
            if (!(stack.getItem() instanceof BirdCageItem cage)) return InteractionResult.PASS;
            InteractionResult result = cage.capture(stack, player, entity);
            if (!result.consumesAction()) return InteractionResult.PASS;
            player.setItemInHand(hand, stack);
            return result;
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> PhotoUploadManager.disconnect(handler.player.getUUID()));
        ServerLifecycleEvents.SERVER_STARTED.register(PhotoTransferEvents::onServerStarted);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> PhotoUploadManager.clear());
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> PhotoTransferEvents.onServerStopping());
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> PhotoAdminCommands.onRegisterCommands(dispatcher));
    }
}
