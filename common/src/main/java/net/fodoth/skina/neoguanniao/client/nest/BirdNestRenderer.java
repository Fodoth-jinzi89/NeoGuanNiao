package net.fodoth.skina.neoguanniao.client.nest;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fodoth.skina.neoguanniao.content.nest.BirdNestBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/**
 * 鸟巢渲染器 - 负责渲染鸟巢方块实体及其中的蛋
 */
public class BirdNestRenderer extends GeoBlockRenderer<BirdNestBlockEntity> {

    // 蛋的渲染常量
    private static final float EGG_BASE_Y = 0.18F;          // 蛋的基础Y轴位置
    private static final float EGG_SCALE = 0.32F;            // 蛋的缩放比例
    private static final float EGG_RADIUS_3 = 0.16F;         // 3个蛋时的分布半径
    private static final float EGG_RADIUS_4 = 0.18F;         // 4个蛋时的分布半径
    private static final float EGG_OFFSET_3_X = -0.05F;      // 3个蛋时的X轴偏移

    public BirdNestRenderer(BlockEntityRendererProvider.Context context) {
        super(new BirdNestModel());
    }

    @Override
    @SuppressWarnings("all")
    public void render(@NotNull BirdNestBlockEntity nest, float partialTick, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        // 渲染鸟巢模型
        super.render(nest, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
        // 渲染巢中的蛋
        renderEggs(nest, poseStack, bufferSource, packedLight, packedOverlay);
    }

    /**
     * 渲染鸟巢中的所有蛋
     */
    private void renderEggs(BirdNestBlockEntity nest, PoseStack poseStack, MultiBufferSource bufferSource,
                            int packedLight, int packedOverlay) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        // 统计蛋的数量
        int eggCount = 0;
        for (int slot = 0; slot < nest.getContainerSize(); slot++) {
            if (!nest.getItem(slot).isEmpty()) eggCount++;
        }
        if (eggCount == 0) return;

        // 遍历所有槽位渲染蛋
        int renderIndex = 0;
        for (int slot = 0; slot < nest.getContainerSize(); slot++) {
            ItemStack eggStack = nest.getItem(slot);
            if (eggStack.isEmpty()) continue;

            // 计算每个蛋的位置和旋转
            float offsetX, offsetZ, rotationAngle;
            switch (eggCount) {
                case 1 -> {
                    offsetX = 0;
                    offsetZ = 0;
                    rotationAngle = 0;
                }
                case 2 -> {
                    offsetX = renderIndex == 0 ? -0.15f : 0.15f;
                    offsetZ = 0;
                    rotationAngle = renderIndex == 0 ? -90 : 90;
                }
                case 3 -> {
                    double rad = Math.toRadians(renderIndex * 120 - 30);
                    offsetX = (float) Math.sin(rad) * EGG_RADIUS_3 + EGG_OFFSET_3_X;
                    offsetZ = (float) Math.cos(rad) * EGG_RADIUS_3;
                    rotationAngle = (float) Math.toDegrees(rad);
                }
                default -> {
                    double rad = Math.toRadians(renderIndex * 90 + 45);
                    offsetX = (float) Math.sin(rad) * EGG_RADIUS_4;
                    offsetZ = (float) Math.cos(rad) * EGG_RADIUS_4;
                    rotationAngle = (float) Math.toDegrees(rad);
                }
            }

            // 应用变换并渲染蛋
            poseStack.pushPose();
            poseStack.translate(0.5, EGG_BASE_Y, 0.5);
            poseStack.translate(offsetX, 0, offsetZ);
            poseStack.mulPose(Axis.YP.rotationDegrees(rotationAngle));
            poseStack.mulPose(Axis.XP.rotationDegrees(90));
            poseStack.scale(EGG_SCALE, EGG_SCALE, EGG_SCALE);

            itemRenderer.renderStatic(eggStack, ItemDisplayContext.FIXED, packedLight, packedOverlay,
                    poseStack, bufferSource, nest.getLevel(), 0);

            poseStack.popPose();
            renderIndex++;
        }
    }
}