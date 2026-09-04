package net.fodoth.skina.neoguanniao.client.fan;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoItems;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

/**
 * Client-side item extensions for the Wind Feather Fan. Registered through
 * {@link RegisterClientExtensionsEvent} instead of the deprecated
 * {@code Item.initializeClient}.
 */
public final class FeatherFanItemExtensions {
    private FeatherFanItemExtensions() {
    }

    public static void register(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            @Override
            public boolean applyForgeHandTransform(PoseStack poseStack, LocalPlayer player, HumanoidArm arm, ItemStack itemInHand, float partialTick, float equipProgress, float swingProgress) {
                InteractionHand renderedHand = arm == player.getMainArm() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                if (!player.isUsingItem() || player.getUsedItemHand() != renderedHand || !ItemStack.isSameItemSameComponents(player.getUseItem(), itemInHand)) {
                    return false;
                }
                int usedTicks = itemInHand.getItem().getUseDuration(itemInHand, player) - player.getUseItemRemainingTicks();
                float elapsedTicks = (float)usedTicks + partialTick;
                float chargeProgress = Mth.clamp(elapsedTicks / 30.0f, 0.0f, 1.0f);
                float chargeEase = FeatherFanItemExtensions.smootherStep(chargeProgress);
                float progress = FeatherFanItemExtensions.getDampedPoseProgress(elapsedTicks);
                float tensionEnvelope = 4.0f * chargeProgress * (1.0f - chargeProgress);
                float tensionWave = Mth.sin(elapsedTicks * 0.55f) * tensionEnvelope;
                float handDirection = arm == HumanoidArm.RIGHT ? 1.0f : -1.0f;
                poseStack.translate(
                        Mth.lerp(progress, handDirection * 0.56f, handDirection * 0.1f) + handDirection * tensionWave * 0.003f,
                        Mth.lerp(progress, -0.52f - equipProgress * 0.6f, -0.42f) - chargeEase * 0.018f + Mth.cos(elapsedTicks * 0.48f) * tensionEnvelope * 0.0015f,
                        Mth.lerp(progress, -0.72f, -0.7f) + chargeEase * 0.025f
                );
                float preChargeRotation = Mth.sin(elapsedTicks * 0.45f) * tensionEnvelope * 0.6f;
                float shakeRotation = 0.0f;
                if (usedTicks >= 30) {
                    float shakeTime = (float)(usedTicks - 30) + partialTick;
                    poseStack.translate(Mth.sin(shakeTime * 2.6f) * 0.006f, Mth.sin(shakeTime * 3.3f) * 0.004f, 0.0f);
                    shakeRotation = Mth.sin(shakeTime * 2.9f) * 1.5f;
                }
                double modelOffsetX = (double)handDirection * 0.9 / 16.0;
                double modelOffsetY = 0.21875;
                double modelOffsetZ = 0.0625;
                poseStack.translate(modelOffsetX, modelOffsetY, modelOffsetZ);
                poseStack.mulPose(Axis.XP.rotationDegrees(18.0f * progress + 7.0f * chargeEase));
                poseStack.mulPose(Axis.ZP.rotationDegrees(handDirection * (90.0f * progress + preChargeRotation + shakeRotation)));
                poseStack.translate(-modelOffsetX, -modelOffsetY, -modelOffsetZ);
                return true;
            }
        }, NeoGuanNiaoItems.WIND_FEATHER_FAN.get());
    }

    private static float getDampedPoseProgress(float elapsedTicks) {
        float time = Mth.clamp(elapsedTicks / 30.0f, 0.0f, 1.0f);
        if (time >= 1.0f) {
            return 1.0f;
        }
        float decay = (float)Math.exp(-6.0f * time);
        float phase = 5.3f * time;
        return 1.0f - decay * (Mth.cos(phase) + 1.13f * Mth.sin(phase));
    }

    private static float smootherStep(float value) {
        value = Mth.clamp(value, 0.0f, 1.0f);
        return value * value * value * (value * (value * 6.0f - 15.0f) + 10.0f);
    }
}
