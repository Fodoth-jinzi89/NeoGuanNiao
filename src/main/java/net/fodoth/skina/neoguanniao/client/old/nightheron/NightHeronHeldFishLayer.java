package net.fodoth.skina.neoguanniao.client.old.nightheron;

import net.fodoth.skina.neoguanniao.content.bird.impl.old.nightheron.NightHeronEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import java.util.Set;

/**
 * 夜鹭持鱼层
 * 在夜鹭的嘴部渲染持有的鱼
 */
public class NightHeronHeldFishLayer extends GeoRenderLayer<NightHeronEntity> {

    private static final Set<String> MOUTH_BONES = Set.of(
            "fish_anchor", "mouth_anchor", "beak", "mouth", "bill", "head", "upper_beak", "lower_beak"
    );

    public NightHeronHeldFishLayer(GeoRenderer<NightHeronEntity> renderer) {
        super(renderer);
    }

    @Override
    public void renderForBone(@NotNull PoseStack poseStack, @NotNull NightHeronEntity nightHeron,
                              @NotNull GeoBone bone, @NotNull RenderType renderType,
                              @NotNull MultiBufferSource bufferSource, @NotNull VertexConsumer buffer,
                              float partialTick, int packedLight, int packedOverlay) {
        ItemStack stack = nightHeron.getHeldFishForRendering();
        if (MOUTH_BONES.contains(bone.getName()) && !stack.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.0, -0.03, -0.42);
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            poseStack.scale(0.45F, 0.45F, 0.45F);
            Minecraft.getInstance().getItemRenderer().renderStatic(
                    stack,
                    ItemDisplayContext.FIXED,
                    packedLight,
                    OverlayTexture.NO_OVERLAY,
                    poseStack,
                    bufferSource,
                    nightHeron.level(),
                    nightHeron.getId()
            );
            poseStack.popPose();
        }
    }
}