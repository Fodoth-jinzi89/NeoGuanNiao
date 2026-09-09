package net.fodoth.skina.neoguanniao.client.cage;

import net.fodoth.skina.neoguanniao.content.cage.BirdCageBlockEntity;
import net.fodoth.skina.neoguanniao.content.cage.BirdCageVariant;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.AABB;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import software.bernie.geckolib.renderer.GeoBlockRenderer;


public class BirdCageRenderer extends GeoBlockRenderer<BirdCageBlockEntity> {


    public BirdCageRenderer(
            BlockEntityRendererProvider.Context context
    ) {
        super(new BirdCageModel());
    }

    @Override
    public void render(@NotNull BirdCageBlockEntity cage, float partialTick, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int light, int overlay) {
        super.render(cage, partialTick, poseStack, buffer, light, overlay);
        if (cage.isEmpty() || cage.getLevel() == null) return;
        CompoundTag tag = cage.capturedBird();
        Entity entity = EntityType.create(tag, cage.getLevel()).orElse(null);
        if (entity == null) return;
        poseStack.pushPose();
        poseStack.translate(0.5, 0.15, 0.5);
        float scale = 0.45F / Math.max(0.1F, Math.max(entity.getBbWidth(), entity.getBbHeight()));
        poseStack.scale(scale, scale, scale);
        var entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        var entityRenderer = entityRenderDispatcher.getRenderer(entity);
        var renderOffset = entityRenderer.getRenderOffset(entity, partialTick);
        poseStack.translate(renderOffset.x, renderOffset.y, renderOffset.z);
        entityRenderer.render(entity, 0, partialTick, poseStack, buffer, light);
        poseStack.translate(-renderOffset.x, -renderOffset.y, -renderOffset.z);
        if (entity.hasCustomName()) {
            try {
                var renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity);
                var method = renderer.getClass().getSuperclass().getDeclaredMethod("renderNameTag", Entity.class, Component.class, PoseStack.class, MultiBufferSource.class, int.class, float.class);
                method.setAccessible(true);
                method.invoke(renderer, entity, entity.getDisplayName(), poseStack, buffer, light, partialTick);
            } catch (ReflectiveOperationException ignored) { }
        }
        poseStack.popPose();
    }

    public @NotNull AABB getRenderBoundingBox(@NotNull BirdCageBlockEntity cage) {
        var pos = cage.getBlockPos();
        double width = cage.variant() == BirdCageVariant.SMALL ? 1.0D : 3.0D;
        double height = switch (cage.variant()) {
            case SMALL -> 1.0D;
            case MEDIUM -> 3.0D;
            case LARGE -> 4.0D;
        };
        return new AABB(pos.getX(), pos.getY(), pos.getZ(),
                pos.getX() + width, pos.getY() + height, pos.getZ() + width);
    }


}
