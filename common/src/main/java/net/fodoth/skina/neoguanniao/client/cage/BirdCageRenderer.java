package net.fodoth.skina.neoguanniao.client.cage;

import net.fodoth.skina.neoguanniao.content.cage.BirdCageBlockEntity;
import net.fodoth.skina.neoguanniao.content.cage.BirdCageVariant;
import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import software.bernie.geckolib.renderer.GeoBlockRenderer;

import java.util.Map;
import java.util.WeakHashMap;


public class BirdCageRenderer extends GeoBlockRenderer<BirdCageBlockEntity> {

    private final Map<BirdCageBlockEntity, CageBirdPreview> cachedPreviews = new WeakHashMap<>();

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
            cachedPreviews.remove(cage);
            return;
        }
        CageBirdPreview preview = cachedPreviews.computeIfAbsent(cage, key -> new CageBirdPreview());
        Entity entity = preview.entity(cage.getLevel(), cage.capturedBird());
        if (entity == null) {
            cachedPreviews.remove(cage);
            return;
        }
        preview.tick(cage.getLevel());
        BirdCageEntityRender.resetRotation(entity);
        float entityPartialTick = entity instanceof AbstractBirdEntity<?> ? partialTick : 0.0F;
        poseStack.pushPose();
        poseStack.translate(0.5, BirdCageEntityRender.centerY(cage.variant()), 0.5);
        // 鸟笼模型已经被 GeoBlockRenderer 按 FACING 旋转，笼中的实体跟随同一朝向。
        BirdCageEntityRender.render(entity, cage.variant(), -getFacing(cage).toYRot(),
                entityPartialTick, poseStack, buffer, light);
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
