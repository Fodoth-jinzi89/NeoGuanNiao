package net.fodoth.skina.neoguanniao.client.cage;

import net.fodoth.skina.neoguanniao.content.cage.BirdCageBlockEntity;
import net.fodoth.skina.neoguanniao.content.cage.BirdCageVariant;
import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.AABB;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import software.bernie.geckolib.renderer.GeoBlockRenderer;

import java.util.Map;
import java.util.WeakHashMap;


public class BirdCageRenderer extends GeoBlockRenderer<BirdCageBlockEntity> {

    private final Map<BirdCageBlockEntity, CachedEntity> cachedEntities = new WeakHashMap<>();

    public BirdCageRenderer(
            BlockEntityRendererProvider.Context context
    ) {
        super(new BirdCageModel());
    }

    @Override
    public void render(@NotNull BirdCageBlockEntity cage, float partialTick, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int light, int overlay) {
        super.render(cage, partialTick, poseStack, buffer, light, overlay);
        if (cage.isEmpty() || cage.getLevel() == null) {
            cachedEntities.remove(cage);
            return;
        }
        CompoundTag tag = cage.capturedBird();
        CachedEntity cachedEntity = cachedEntities.get(cage);
        if (cachedEntity == null || !cachedEntity.tag().equals(tag)) {
            Entity entity = EntityType.create(tag, cage.getLevel()).orElse(null);
            if (entity == null) return;
            cachedEntity = new CachedEntity(tag.copy(), entity);
            cachedEntities.put(cage, cachedEntity);
        }
        Entity entity = cachedEntity.entity();
        entity.setYRot(0.0F);
        entity.yRotO = 0.0F;
        entity.setXRot(0.0F);
        entity.xRotO = 0.0F;
        if (entity instanceof LivingEntity livingEntity) {
            livingEntity.yBodyRot = 0.0F;
            livingEntity.yBodyRotO = 0.0F;
            livingEntity.yHeadRot = 0.0F;
            livingEntity.yHeadRotO = 0.0F;
        }
        float entityPartialTick = 0.0F;
        if (entity instanceof AbstractBirdEntity<?> bird) {
            bird.tickCount = (int) cage.getLevel().getGameTime();
            var animations = bird.getBirdData().animation().animationMap();
            var animation = bird.getRoutineController().isActiveTime()
                    ? bird.getAnimationController().pickIdleAnimation()
                    : animations.getOrDefault("sleep", animations.get("sleep_loop"));
            bird.getAnimationController().setGuidePreviewAnimation(animation);
            entityPartialTick = partialTick;
        }
        poseStack.pushPose();
        double cageHeight = switch (cage.variant()) {
            case SMALL -> 1.0D;
            case MEDIUM -> 3.0D;
            case LARGE -> 4.0D;
        };
        var bounds = entity.getBoundingBox();
        double maxEntitySize = cage.variant().maxEntitySize();
        float scale = (float) Math.min(1.0D, Math.min(maxEntitySize / bounds.getXsize(),
                Math.min(maxEntitySize / bounds.getYsize(), maxEntitySize / bounds.getZsize())));
        var entityCenter = bounds.getCenter().subtract(entity.position());
        poseStack.translate(0.5, cageHeight * 0.5, 0.5);
        poseStack.scale(scale, scale, scale);
        poseStack.translate(-entityCenter.x, -entityCenter.y, -entityCenter.z);
        var entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        var entityRenderer = entityRenderDispatcher.getRenderer(entity);
        var renderOffset = entityRenderer.getRenderOffset(entity, entityPartialTick);
        poseStack.translate(renderOffset.x, renderOffset.y, renderOffset.z);
        entityRenderer.render(entity, 0, entityPartialTick, poseStack, buffer, light);
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

    private record CachedEntity(CompoundTag tag, Entity entity) {
    }


}
