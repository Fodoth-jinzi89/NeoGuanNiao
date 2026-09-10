package net.fodoth.skina.neoguanniao.mixin.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fodoth.skina.neoguanniao.client.camera.CameraClientEvents;
import net.fodoth.skina.neoguanniao.client.fan.FeatherFanHandTransform;
import net.fodoth.skina.neoguanniao.content.fan.FeatherFanItem;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
    @Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
    private void gt$cameraHands(AbstractClientPlayer player, float partialTick, float pitch, InteractionHand hand,
                                float swingProgress, ItemStack stack, float equipProgress, PoseStack poseStack,
                                MultiBufferSource bufferSource, int packedLight, CallbackInfo ci) {
        if (stack.getItem() instanceof FeatherFanItem && player instanceof LocalPlayer localPlayer) {
            HumanoidArm arm = hand == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
            FeatherFanHandTransform.apply(poseStack, localPlayer, arm, stack, partialTick, equipProgress);
        }
        if (CameraClientEvents.shouldCancelRenderHand()) {
            CameraClientEvents.onRenderHand(hand, poseStack, bufferSource, packedLight, swingProgress, equipProgress);
            ci.cancel();
        }
    }
}
