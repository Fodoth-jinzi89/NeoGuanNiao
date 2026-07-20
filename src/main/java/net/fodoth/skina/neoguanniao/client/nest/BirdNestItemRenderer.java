package net.fodoth.skina.neoguanniao.client.nest;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fodoth.skina.neoguanniao.content.nest.BirdNestItem;
import net.fodoth.skina.neoguanniao.util.TransformUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/**
 * 鸟巢物品的渲染器
 * 负责在不同视角下对鸟巢物品进行位置、旋转和缩放的调整
 */
public class BirdNestItemRenderer extends GeoItemRenderer<BirdNestItem> {

    // ==================== 默认变换参数 ====================
    // 这些是各个视角共用或基准的变换值，实际使用时会根据不同视角覆盖

    // ==================== GUI视角参数 ====================
    private static final float GUI_OFFSET_X = 0.0F;
    private static final float GUI_OFFSET_Y = 0.0F;
    private static final float GUI_OFFSET_Z = 0.0F;
    private static final float GUI_SCALE = 0.7F;
    private static final float GUI_ROTATION_Y = 45.0F;
    private static final float GUI_ROTATION_Z = 20.0F;
    private static final float GUI_ROTATION_X = 20.0F;

    // ==================== 地面视角参数 ====================
    private static final float GROUND_OFFSET_X = 0.3F;
    private static final float GROUND_OFFSET_Y = 0.1F;
    private static final float GROUND_OFFSET_Z = 0.3F;
    private static final float GROUND_SCALE = 0.35F;

    // ==================== 手持/第三人称视角参数 ====================
    private static final float HAND_OFFSET_X = 0.25F;
    private static final float HAND_OFFSET_Y = 0.375F;
    private static final float HAND_OFFSET_Z = -0.375F;
    private static final float HAND_SCALE = 0.5F;
    private static final float HAND_ROTATION_Z = 45.0F;  // 手持时绕Z轴旋转

    // ==================== 固定展示视角参数 ====================
    private static final float FIXED_OFFSET_Y = -1.125F;
    private static final float FIXED_SCALE = 1.0F;
    private static final float FIXED_ROTATION_Z = -90.0F;

    // ==================== 头部穿戴视角参数 ====================
    private static final float HEAD_OFFSET_X = -0.125F;
    private static final float HEAD_OFFSET_Y = 0.25F;
    private static final float HEAD_OFFSET_Z = -0.125F;
    private static final float HEAD_SCALE = 1.25F;

    /**
     * 构造函数，初始化鸟巢物品的模型
     */
    public BirdNestItemRenderer() {
        super(new BirdNestItemModel());
    }

    /**
     * 在渲染前对物品进行变换处理
     * 根据不同的渲染视角（GUI、地面、手持、固定展示、头部等）应用不同的位置/旋转/缩放
     *
     * @param poseStack      变换矩阵堆栈，用于应用位置/旋转/缩放
     * @param animatable     当前渲染的鸟巢物品实例
     * @param model          已烘焙的几何模型
     * @param bufferSource   多缓冲源，用于获取渲染缓冲
     * @param buffer         顶点消费者，用于构建几何数据
     * @param isReRender     是否为重新渲染
     * @param partialTick    部分帧时间，用于动画插值
     * @param packedLight    打包的光照数据
     * @param packedOverlay  打包的覆盖数据
     * @param colour         颜色值（ARGB格式）
     */
    @Override
    public void preRender(
            PoseStack poseStack,
            BirdNestItem animatable,
            BakedGeoModel model,
            MultiBufferSource bufferSource,
            VertexConsumer buffer,
            boolean isReRender,
            float partialTick,
            int packedLight,
            int packedOverlay,
            int colour
    ) {
        // 根据当前渲染视角应用不同的变换
        switch (this.renderPerspective) {
            // ---------- GUI背包界面 ----------
            case GUI -> TransformUtil.applyTransform(
                    poseStack,
                    GUI_OFFSET_X, GUI_OFFSET_Y, GUI_OFFSET_Z,
                    GUI_SCALE,
                    GUI_ROTATION_Y, GUI_ROTATION_X, GUI_ROTATION_Z
            );

            // ---------- 丢在地上/物品展示 ----------
            case GROUND -> TransformUtil.applyTransform(
                    poseStack,
                    GROUND_OFFSET_X, GROUND_OFFSET_Y, GROUND_OFFSET_Z,
                    GROUND_SCALE,
                    0F, 0F, 0F
            );

            // ---------- 第一人称/第三人称手持（左右手） ----------
            case FIRST_PERSON_LEFT_HAND,
                 FIRST_PERSON_RIGHT_HAND,
                 THIRD_PERSON_LEFT_HAND,
                 THIRD_PERSON_RIGHT_HAND -> TransformUtil.applyTransform(
                    poseStack,
                    HAND_OFFSET_X, HAND_OFFSET_Y, HAND_OFFSET_Z,
                    HAND_SCALE,
                    0F, HAND_ROTATION_Z, 0F
            );

            // ---------- 固定在物品展示架等 ----------
            case FIXED -> TransformUtil.applyTransform(
                    poseStack,
                    0F, FIXED_OFFSET_Y, 0F,
                    FIXED_SCALE,
                    0F, FIXED_ROTATION_Z, 0F
            );

            // ---------- 戴在头上 ----------
            case HEAD -> TransformUtil.applyTransform(
                    poseStack,
                    HEAD_OFFSET_X, HEAD_OFFSET_Y, HEAD_OFFSET_Z,
                    HEAD_SCALE,
                    0F, 0F, 0F
            );

            // 其他未覆盖的视角不进行额外变换
            default -> {
                // 保持原样
            }
        }

        // 调用父类方法继续渲染流程
        super.preRender(poseStack, animatable, model, bufferSource, buffer,
                isReRender, partialTick, packedLight, packedOverlay, colour);
    }
}