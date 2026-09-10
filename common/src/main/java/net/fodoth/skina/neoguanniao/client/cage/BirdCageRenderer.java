package net.fodoth.skina.neoguanniao.client.cage;

import net.fodoth.skina.neoguanniao.content.cage.BirdCageBlockEntity;
import net.fodoth.skina.neoguanniao.content.cage.BirdCageVariant;
import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import software.bernie.geckolib.animation.RawAnimation;
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
        BirdCageEntityRender.resetRotation(entity);
        float entityPartialTick = 0.0F;
        if (entity instanceof AbstractBirdEntity<?> bird) {
            bird.tickCount = (int) cage.getLevel().getGameTime();
            if (!cachedEntity.idleAnimationPicked()) {
                // 待机动画只挑选一次：每帧重选会让动画不断从头播放，看起来就是在抽搐。
                cachedEntity.setIdleAnimation(pickIdleAnimation(bird));
            }
            var animations = bird.getBirdData().animation().animationMap();
            bird.getAnimationController().setGuidePreviewAnimation(
                    bird.getRoutineController().isActiveTime()
                            ? cachedEntity.idleAnimation()
                            : animations.getOrDefault("sleep", animations.get("sleep_loop")));
            entityPartialTick = partialTick;
        }
        poseStack.pushPose();
        poseStack.translate(0.5, BirdCageEntityRender.centerY(cage.variant()), 0.5);
        // 鸟笼模型已经被 GeoBlockRenderer 按 FACING 旋转，笼中的实体跟随同一朝向。
        BirdCageEntityRender.render(entity, cage.variant(), -getFacing(cage).toYRot(),
                entityPartialTick, poseStack, buffer, light);
        poseStack.popPose();
    }

    private static RawAnimation pickIdleAnimation(AbstractBirdEntity<?> bird) {
        RawAnimation animation = bird.getAnimationController().pickIdleAnimation();
        return animation != null ? animation : bird.getBirdData().animation().animationMap().get("idle");
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

    private static final class CachedEntity {

        private final CompoundTag tag;
        private final Entity entity;
        private RawAnimation idleAnimation;
        private boolean idleAnimationPicked;

        private CachedEntity(CompoundTag tag, Entity entity) {
            this.tag = tag;
            this.entity = entity;
        }

        private CompoundTag tag() {
            return tag;
        }

        private Entity entity() {
            return entity;
        }

        private RawAnimation idleAnimation() {
            return idleAnimation;
        }

        private void setIdleAnimation(RawAnimation idleAnimation) {
            this.idleAnimation = idleAnimation;
            this.idleAnimationPicked = true;
        }

        private boolean idleAnimationPicked() {
            return idleAnimationPicked;
        }
    }


}
