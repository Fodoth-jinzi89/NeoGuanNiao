package net.fodoth.skina.neoguanniao.client.nest;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fodoth.skina.neoguanniao.content.nest.BirdNestBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/**
 * 鸟巢方块实体渲染器
 * 负责渲染鸟巢模型本体以及巢内动态排列的鸟蛋
 */
public class BirdNestRenderer extends GeoBlockRenderer<BirdNestBlockEntity> {

    // ==================== 蛋的布局参数 ====================
    private static final float EGG_BASE_Y = 0.18F;          // 蛋的基础高度（从方块底部算起）
    private static final float EGG_SCALE = 0.32F;           // 蛋的整体缩放
    private static final float EGG_RADIUS_3 = 0.16F;        // 3个蛋时的分布半径
    private static final float EGG_RADIUS_4 = 0.18F;        // 4个蛋时的分布半径
    private static final float EGG_OFFSET_3_X = -0.05F;     // 3个蛋时的X轴偏移微调

    /**
     * 构造函数
     * @param context 方块实体渲染器上下文
     */
    public BirdNestRenderer(BlockEntityRendererProvider.Context context) {
        super(new BirdNestModel());
    }

    /**
     * 渲染鸟巢方块实体
     * 流程：先渲染鸟巢模型本体，再渲染巢内的鸟蛋
     */
    @Override
    @SuppressWarnings("all")
    public void render(BirdNestBlockEntity nest, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        // 1. 渲染鸟巢模型本体（Geo模型）
        super.render(nest, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
        // 2. 渲染巢内的鸟蛋
        renderEggs(nest, poseStack, bufferSource, packedLight, packedOverlay);
    }

    /**
     * 渲染鸟巢内的所有鸟蛋
     * 根据蛋的数量自动选择排列样式：
     * - 1个：居中
     * - 2个：左右对称
     * - 3个：正三角形
     * - 4个：四叶草形（正方形）
     */
    private void renderEggs(BirdNestBlockEntity nest, PoseStack poseStack,
                            MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        // ----- 统计有效蛋的数量 -----
        int eggCount = 0;
        for (int slot = 0; slot < nest.getContainerSize(); slot++) {
            if (!nest.getItem(slot).isEmpty()) {
                eggCount++;
            }
        }
        if (eggCount == 0) return;  // 没有蛋则直接返回

        // ----- 遍历所有槽位，依次渲染每个蛋 -----
        int renderIndex = 0;  // 当前已渲染的蛋序号（用于布局计算）
        for (int slot = 0; slot < nest.getContainerSize(); slot++) {
            ItemStack eggStack = nest.getItem(slot);
            if (eggStack.isEmpty()) continue;

            // 计算当前蛋的位置偏移和旋转角度
            float offsetX, offsetZ, rotationAngle;

            switch (eggCount) {
                // ---------- 1个蛋：居中 ----------
                case 1 -> {
                    offsetX = 0;
                    offsetZ = 0;
                    rotationAngle = 0;
                }

                // ---------- 2个蛋：左右对称 ----------
                case 2 -> {
                    offsetX = renderIndex == 0 ? -0.15f : 0.15f;
                    offsetZ = 0;
                    rotationAngle = renderIndex == 0 ? -90 : 90;
                }

                // ---------- 3个蛋：正三角形 ----------
                // 从-30°开始，间隔120°，形成正三角形
                case 3 -> {
                    double rad = Math.toRadians(renderIndex * 120 - 30);
                    offsetX = (float) Math.sin(rad) * EGG_RADIUS_3 + EGG_OFFSET_3_X;
                    offsetZ = (float) Math.cos(rad) * EGG_RADIUS_3;
                    rotationAngle = (float) Math.toDegrees(rad);
                }

                // ---------- 4个蛋：四叶草形（正方形） ----------
                // 从45°开始，间隔90°，形成正方形
                default -> {
                    double rad = Math.toRadians(renderIndex * 90 + 45);
                    offsetX = (float) Math.sin(rad) * EGG_RADIUS_4;
                    offsetZ = (float) Math.cos(rad) * EGG_RADIUS_4;
                    rotationAngle = (float) Math.toDegrees(rad);
                }
            }

            // ----- 应用变换矩阵并渲染蛋 -----
            poseStack.pushPose();

            // 移至方块中心底部（方块坐标原点在底部中心）
            poseStack.translate(0.5, EGG_BASE_Y, 0.5);
            // 应用当前蛋的位置偏移
            poseStack.translate(offsetX, 0, offsetZ);

            // 旋转使蛋朝外（绕Y轴旋转朝向中心方向）
            poseStack.mulPose(Axis.YP.rotationDegrees(rotationAngle));
            // 将蛋放平（绕X轴旋转90度使蛋横向放置）
            poseStack.mulPose(Axis.XP.rotationDegrees(90));

            // 缩放到合适大小
            poseStack.scale(EGG_SCALE, EGG_SCALE, EGG_SCALE);

            // 渲染蛋的物品模型（固定视角，不随玩家视角变化）
            itemRenderer.renderStatic(eggStack, ItemDisplayContext.FIXED, packedLight, packedOverlay,
                    poseStack, bufferSource, nest.getLevel(), 0);

            poseStack.popPose();
            renderIndex++;
        }
    }
}