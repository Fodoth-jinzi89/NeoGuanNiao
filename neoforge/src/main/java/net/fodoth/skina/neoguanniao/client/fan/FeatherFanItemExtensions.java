package net.fodoth.skina.neoguanniao.client.fan;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoItems;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Client-side item extensions for the Wind Feather Fan. Registered through
 * {@link RegisterClientExtensionsEvent} instead of the deprecated
 * {@code Item.initializeClient}.
 */
public final class FeatherFanItemExtensions {
    private FeatherFanItemExtensions() {
    }

    public static void register(RegisterClientExtensionsEvent event) {
        if (!NeoGuanNiaoItems.WIND_FEATHER_FAN.isPresent()) {
            return;
        }
        event.registerItem(new IClientItemExtensions() {
            @Override
            public boolean applyForgeHandTransform(@NotNull PoseStack poseStack, @NotNull LocalPlayer player, @NotNull HumanoidArm arm, @NotNull ItemStack itemInHand, float partialTick, float equipProgress, float swingProgress) {
                return FeatherFanHandTransform.apply(poseStack, player, arm, itemInHand, partialTick, equipProgress);
            }
        }, NeoGuanNiaoItems.WIND_FEATHER_FAN.get());
    }

}
