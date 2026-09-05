package com.birdcamera.mixin;

import com.birdcamera.content.advancement.criterion.PickupBirdFeatherTrigger;
import com.birdcamera.registry.BirdCameraCriteriaTriggers;
import com.birdcamera.registry.BirdCameraItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 羽毛拾取成就触发（对应原版 BirdFeatherPickupHandler 的 ItemEntityPickupEvent）
 * 玩家拾取 bird_feather 时触发 PICKUP_BIRD_FEATHER 进度
 */
@Mixin(ItemEntity.class)
public abstract class ItemEntityPickupMixin {

    @Inject(method = "playerTouch", at = @At("HEAD"))
    private void birdcamera$onPickup(Player player, CallbackInfo ci) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        ItemEntity self = (ItemEntity) (Object) this;
        if (self.getItem().is(BirdCameraItems.BIRD_FEATHER)) {
            ((PickupBirdFeatherTrigger) BirdCameraCriteriaTriggers.PICKUP_BIRD_FEATHER).trigger(serverPlayer);
        }
    }
}