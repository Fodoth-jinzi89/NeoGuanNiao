package net.fodoth.skina.neoguanniao.client.fan;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fodoth.skina.neoguanniao.content.fan.FeatherFanProjectileEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import org.jetbrains.annotations.NotNull;

/**
 * Ported guaniao-3.1.4 fan projectile renderer: spinning 3D fan in flight,
 * angled fan when stuck, and the Riven split ring array.
 */
public class FeatherFanProjectileRenderer extends EntityRenderer<FeatherFanProjectileEntity> {
    private static final float RENDER_SCALE = 1.35f;
    private final ItemRenderer itemRenderer;

    public FeatherFanProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
        this.shadowRadius = 0.0f;
    }

    @Override
    public void render(FeatherFanProjectileEntity entity, float entityYaw, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight) {
        if (entity.tickCount < 2) {
            super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
            return;
        }
        boolean renderMainFan = !entity.isRivenActive() || entity.getRivenTicks() < 5 || entity.getRivenTicks() >= 26;
        if (renderMainFan) {
            this.renderMainFan(entity, partialTick, poseStack, bufferSource, packedLight);
        }
        if (entity.isRivenActive()) {
            this.renderRivenArray(entity, partialTick, poseStack, bufferSource);
        }
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private void renderMainFan(FeatherFanProjectileEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(RENDER_SCALE, RENDER_SCALE, RENDER_SCALE);
        if (entity.isPiercing() || entity.isStuck()) {
            float yaw = Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());
            float pitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
            poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0f - pitch));
            poseStack.mulPose(Axis.ZP.rotationDegrees(43.0f));
            if (entity.isBurialActive()) {
                poseStack.mulPose(Axis.YP.rotationDegrees(((float)entity.tickCount + partialTick) * 18.0f));
            }
            poseStack.scale(0.9f, 0.9f, 0.9f);
            if (entity.isRivenActive() && entity.getRivenTicks() >= 26) {
                float reformProgress = Mth.clamp(((float)entity.getRivenTicks() + partialTick - 26.0f) / 10.0f, 0.0f, 1.0f);
                float eased = reformProgress * reformProgress * (3.0f - 2.0f * reformProgress);
                float reformScale = Mth.lerp(eased, 0.34f, 1.0f);
                poseStack.mulPose(Axis.ZP.rotationDegrees((1.0f - eased) * 240.0f));
                poseStack.scale(reformScale, reformScale, reformScale);
            }
        } else {
            float spinSpeed = entity.isHunting() ? 72.0f : Mth.lerp(entity.getCharge(), 30.0f, 55.0f);
            float spinDirection = entity.isReturning() ? -1.0f : 1.0f;
            poseStack.mulPose(Axis.YP.rotationDegrees(((float)entity.tickCount + partialTick) * spinSpeed * spinDirection));
            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0f));
            if (entity.isHunting()) {
                poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.sin(((float)entity.tickCount + partialTick) * 0.72f) * 7.5f));
                poseStack.scale(1.08f, 1.08f, 1.08f);
            }
        }
        this.renderFanCopy(entity, poseStack, bufferSource, entity.isHunting() ? 0xF000F0 : packedLight, entity.getId());
        poseStack.popPose();
    }

    private void renderRivenArray(FeatherFanProjectileEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource) {
        float age = (float)entity.getRivenTicks() + partialTick;
        float radius = FeatherFanProjectileEntity.getRivenArrayRadius(age);
        if (radius <= 0.0f) {
            return;
        }
        double ringRotation = FeatherFanProjectileEntity.getRivenRingRotation(age);
        double heightScale = (double)radius / 3.8;
        float pulse = 0.82f + Mth.sin(age * 0.82f) * 0.035f;
        for (int i = 0; i < 8; ++i) {
            double baseAngle = (float)Math.PI * 2.0 * (float)i / 8.0;
            double angle = baseAngle + ringRotation;
            double x = Math.cos(angle) * radius;
            double y = Math.sin(baseAngle * 2.0) * 0.85 * heightScale;
            double z = Math.sin(angle) * radius;
            double directionLength = Math.sqrt(x * x + y * y + z * z);
            if (directionLength < 1.0E-5) {
                continue;
            }
            double directionX = -x / directionLength;
            double directionY = -y / directionLength;
            double directionZ = -z / directionLength;
            double horizontal = Math.sqrt(directionX * directionX + directionZ * directionZ);
            float yaw = (float)Math.toDegrees(Mth.atan2(directionX, directionZ));
            float pitch = (float)Math.toDegrees(Mth.atan2(directionY, horizontal));
            poseStack.pushPose();
            poseStack.translate(x, y, z);
            poseStack.scale(RENDER_SCALE * pulse, RENDER_SCALE * pulse, RENDER_SCALE * pulse);
            poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0f - pitch));
            poseStack.mulPose(Axis.ZP.rotationDegrees(43.0f));
            poseStack.scale(0.82f, 0.82f, 0.82f);
            this.renderFanCopy(entity, poseStack, bufferSource, 0xF000F0, entity.getId() * 31 + i);
            poseStack.popPose();
        }
    }

    private void renderFanCopy(FeatherFanProjectileEntity entity, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int seed) {
        this.itemRenderer.renderStatic(entity.getItem(), ItemDisplayContext.GROUND, packedLight, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, entity.level(), seed);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull FeatherFanProjectileEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
