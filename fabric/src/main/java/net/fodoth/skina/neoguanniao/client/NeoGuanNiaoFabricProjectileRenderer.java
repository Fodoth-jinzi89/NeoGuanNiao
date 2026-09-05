package net.fodoth.skina.neoguanniao.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fodoth.skina.neoguanniao.content.fan.FeatherFanProjectileEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.inventory.InventoryMenu;
import org.jetbrains.annotations.NotNull;

public final class NeoGuanNiaoFabricProjectileRenderer extends EntityRenderer<FeatherFanProjectileEntity> {
    private final ItemRenderer itemRenderer;

    public NeoGuanNiaoFabricProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(FeatherFanProjectileEntity entity, float yaw, float partialTick,
                       @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffers, int light) {
        poseStack.pushPose();
        poseStack.scale(1.25F, 1.25F, 1.25F);
        this.itemRenderer.renderStatic(entity.getItem(), ItemDisplayContext.GROUND, light,
                OverlayTexture.NO_OVERLAY, poseStack, buffers, entity.level(), entity.getId());
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffers, light);
    }

    @Override
    public @NotNull net.minecraft.resources.ResourceLocation getTextureLocation(@NotNull FeatherFanProjectileEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
