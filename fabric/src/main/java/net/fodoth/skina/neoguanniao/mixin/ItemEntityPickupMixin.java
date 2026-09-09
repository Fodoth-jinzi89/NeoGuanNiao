package net.fodoth.skina.neoguanniao.mixin;

import net.fodoth.skina.neoguanniao.event.BirdFeatherPickupHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityPickupMixin {
    @Inject(method = "playerTouch", at = @At("HEAD"))
    private void gt$birdFeatherPickup(Player player, CallbackInfo ci) {
        if (player instanceof ServerPlayer serverPlayer) {
            BirdFeatherPickupHandler.onPickup(serverPlayer, (ItemEntity) (Object) this);
        }
    }
}
