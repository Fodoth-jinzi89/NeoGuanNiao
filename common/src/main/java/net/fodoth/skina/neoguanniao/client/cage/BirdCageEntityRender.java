package net.fodoth.skina.neoguanniao.client.cage;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fodoth.skina.neoguanniao.content.cage.BirdCageVariant;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;


/**
 * 鸟笼中实体的共用渲染逻辑：把实体碰撞箱中心对齐到当前坐标系原点，并按鸟笼变体
 * 允许的最大尺寸等比缩放，保证实体始终完整地待在笼子里。
 */
final class BirdCageEntityRender {

    private BirdCageEntityRender() {
    }


    /** 鸟笼几何中心相对方块原点的纵向偏移（水平方向即方块中心）。 */
    static double centerY(BirdCageVariant variant) {
        return switch (variant) {
            case SMALL -> 0.5D;
            case MEDIUM -> 1.5D;
            case LARGE -> 2.0D;
        };
    }


    /** 笼中实体只跟随鸟笼朝向渲染，自身朝向一律归零。 */
    static void resetRotation(Entity entity) {
        entity.setYRot(0.0F);
        entity.yRotO = 0.0F;
        entity.setXRot(0.0F);
        entity.xRotO = 0.0F;
        if (entity instanceof LivingEntity living) {
            living.yBodyRot = 0.0F;
            living.yBodyRotO = 0.0F;
            living.yHeadRot = 0.0F;
            living.yHeadRotO = 0.0F;
        }
    }


    /**
     * 调用前需要先把当前坐标系原点移动到鸟笼中心，实体随后会被摆到 {@code slot} 号笼位上。
     */
    static void render(Entity entity, BirdCageVariant variant, int slot, float yaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int light) {
        AABB bounds = entity.getBoundingBox();
        double maxEntitySize = variant.maxEntitySize();
        float scale = (float) Math.min(1.0D, Math.min(maxEntitySize / bounds.getXsize(),
                Math.min(maxEntitySize / bounds.getYsize(), maxEntitySize / bounds.getZsize())));
        Vec3 entityCenter = bounds.getCenter().subtract(entity.position());
        double[] slotOffset = variant.slotOffset(slot);
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
        // 笼位落点相对鸟笼中心的位置；纵向要再补上缩放后的碰撞箱中心偏移，脚底才会正好踩在支撑面上。
        poseStack.translate(slotOffset[0] / 16.0D,
                slotOffset[1] / 16.0D - centerY(variant) + scale * entityCenter.y,
                slotOffset[2] / 16.0D);
        poseStack.scale(scale, scale, scale);
        poseStack.translate(-entityCenter.x, -entityCenter.y, -entityCenter.z);
        var dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        var entityRenderer = dispatcher.getRenderer(entity);
        // 必须经由 EntityRenderDispatcher.render 渲染：EMF/ETF 等实体模型/纹理模组只在它的
        // HEAD/RETURN 建立与清理“当前实体”上下文。直接调用 EntityRenderer.render 会让上下文
        // 停留在上一个实体（或鸟笼方块实体）上，后放进去的实体会因此套用前一只缓存的模型与纹理。
        dispatcher.setRenderShadow(false);
        try {
            dispatcher.render(entity, 0.0D, 0.0D, 0.0D, 0.0F, partialTick, poseStack, buffer, light);
        } finally {
            dispatcher.setRenderShadow(true);
        }
        if (entity.hasCustomName()) {
            try {
                var method = entityRenderer.getClass().getSuperclass().getDeclaredMethod("renderNameTag",
                        Entity.class, Component.class, PoseStack.class, MultiBufferSource.class, int.class, float.class);
                method.setAccessible(true);
                method.invoke(entityRenderer, entity, entity.getDisplayName(), poseStack, buffer, light, partialTick);
            } catch (ReflectiveOperationException ignored) { }
        }
        poseStack.popPose();
    }
}
