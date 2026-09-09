package net.fodoth.skina.neoguanniao.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

public class TransformUtil {

    // ============ 常量定义 ============

    /**
     * 正等轴测图标准角度（Minecraft左手坐标系适配）
     * 绕X轴旋转 arctan(1/√2) ≈ 35.264°（左手系取正）
     * 绕Y轴旋转 45°
     */
    private static final float ISOMETRIC_ROT_X = 35.264f;
    private static final float ISOMETRIC_ROT_Y = 45.0f;

    /**
     * 斜二测图标准角度（Minecraft左手坐标系适配）
     * 特点：正面保持真实形状，侧面和顶面按比例缩短
     * 绕X轴旋转 30°（左手系取正）
     * 绕Y轴旋转 45°
     */
    private static final float CABINET_ROT_X = 30.0f;
    private static final float CABINET_ROT_Y = 45.0f;

    /**
     * 斜等测图标准角度（Minecraft左手坐标系适配）
     * 特点：Z轴不缩短，保持真实长度
     * 绕X轴旋转 45°（左手系取正）
     * 绕Y轴旋转 45°
     */
    private static final float CAVALIER_ROT_X = 45.0f;
    private static final float CAVALIER_ROT_Y = 45.0f;

    // ============ 原始通用方法 ============

    public static void applyTransform(PoseStack poseStack, float offsetX, float offsetY, float offsetZ,
                                      float scale, float rotY, float rotX, float rotZ) {
        poseStack.mulPose(Axis.YP.rotationDegrees(rotY));
        poseStack.mulPose(Axis.XP.rotationDegrees(rotX));
        poseStack.mulPose(Axis.ZP.rotationDegrees(rotZ));
        poseStack.translate(offsetX, offsetY, offsetZ);
        poseStack.scale(scale, scale, scale);
    }

    public static void applyTransformNew(PoseStack poseStack, float offsetX, float offsetY, float offsetZ,
                                      float scale, float rotY, float rotX, float rotZ) {
        poseStack.mulPose(Axis.ZP.rotationDegrees(rotZ));
        poseStack.mulPose(Axis.XP.rotationDegrees(rotX));
        poseStack.mulPose(Axis.YP.rotationDegrees(rotY));
        poseStack.translate(offsetX, offsetY, offsetZ);
        poseStack.scale(scale, scale, scale);
    }

    // ============ 核心变换方法（统一顺序：缩放 -> 平移 -> 旋转） ============

    /**
     * 核心变换方法
     * 按照 缩放 -> 平移 -> 旋转 的顺序
     * 旋转顺序：先绕Y轴旋转，再绕X轴旋转
     * 适配Minecraft左手坐标系（X轴旋转取正）
     *
     * @param poseStack 当前的PoseStack
     * @param offsetX X轴偏移量
     * @param offsetY Y轴偏移量
     * @param offsetZ Z轴偏移量
     * @param scaleX X轴缩放
     * @param scaleY Y轴缩放
     * @param scaleZ Z轴缩放
     * @param rotX 绕X轴旋转角度（度）
     * @param rotY 绕Y轴旋转角度（度）
     */
    private static void applyCoreTransform(PoseStack poseStack, float offsetX, float offsetY, float offsetZ,
                                           float scaleX, float scaleY, float scaleZ,
                                           float rotX, float rotY) {
        // 按照 缩放 -> 平移 -> 旋转 的顺序
        // 旋转顺序：先绕Y轴旋转，再绕X轴旋转
        // PoseStack变换是后进先出（LIFO），所以代码从后往前写
        poseStack.mulPose(Axis.XP.rotationDegrees(rotX));  // 3. 最后执行：X轴旋转
        poseStack.mulPose(Axis.YP.rotationDegrees(rotY));  // 2. 次之：Y轴旋转
        poseStack.translate(offsetX, offsetY, offsetZ);     // 1. 平移
        poseStack.scale(scaleX, scaleY, scaleZ);            // 0. 最先执行：缩放
    }

    // ============ 正等轴测图方法 (Isometric Projection) ============

    /**
     * 应用正等轴测图变换（适配Minecraft左手坐标系）
     * 旋转顺序：先绕Y轴旋转，再绕X轴旋转
     *
     * @param poseStack 当前的PoseStack
     * @param offsetX X轴偏移量
     * @param offsetY Y轴偏移量
     * @param offsetZ Z轴偏移量
     * @param scale 缩放比例
     */
    public static void applyIsometricTransform(PoseStack poseStack, float offsetX, float offsetY, float offsetZ,
                                               float scale) {
        applyIsometricTransform(poseStack, offsetX, offsetY, offsetZ, scale, scale, scale);
    }

    /**
     * 应用正等轴测图变换（适配Minecraft左手坐标系，支持不同轴向缩放）
     * 旋转顺序：先绕Y轴旋转，再绕X轴旋转
     *
     * @param poseStack 当前的PoseStack
     * @param offsetX X轴偏移量
     * @param offsetY Y轴偏移量
     * @param offsetZ Z轴偏移量
     * @param scaleX X轴缩放
     * @param scaleY Y轴缩放
     * @param scaleZ Z轴缩放
     */
    public static void applyIsometricTransform(PoseStack poseStack, float offsetX, float offsetY, float offsetZ,
                                               float scaleX, float scaleY, float scaleZ) {
        applyIsometricTransform(poseStack, offsetX, offsetY, offsetZ, scaleX, scaleY, scaleZ,
                ISOMETRIC_ROT_X, ISOMETRIC_ROT_Y);
    }

    /**
     * 应用正等轴测图变换（自定义角度，适配Minecraft左手坐标系）
     * 旋转顺序：先绕Y轴旋转，再绕X轴旋转
     *
     * @param poseStack 当前的PoseStack
     * @param offsetX X轴偏移量
     * @param offsetY Y轴偏移量
     * @param offsetZ Z轴偏移量
     * @param scaleX X轴缩放
     * @param scaleY Y轴缩放
     * @param scaleZ Z轴缩放
     * @param rotX 绕X轴旋转角度（度）
     * @param rotY 绕Y轴旋转角度（度）
     */
    public static void applyIsometricTransform(PoseStack poseStack, float offsetX, float offsetY, float offsetZ,
                                               float scaleX, float scaleY, float scaleZ,
                                               float rotX, float rotY) {
        applyCoreTransform(poseStack, offsetX, offsetY, offsetZ, scaleX, scaleY, scaleZ, rotX, rotY);
    }

    /**
     * 应用正等轴测图变换（Minecraft物品渲染专用）
     * 旋转顺序：先绕Y轴旋转，再绕X轴旋转
     *
     * @param poseStack 当前的PoseStack
     * @param itemScale 物品缩放
     * @param xOffset X轴偏移
     * @param yOffset Y轴偏移
     */
    public static void applyMinecraftIsometric(PoseStack poseStack, float itemScale, float xOffset, float yOffset) {
        applyIsometricTransform(poseStack, xOffset, yOffset, 0.0f, itemScale, itemScale, itemScale,
                ISOMETRIC_ROT_X, ISOMETRIC_ROT_Y);
    }

    /**
     * 应用正等轴测图变换（Minecraft物品渲染专用，支持不同轴向缩放）
     * 旋转顺序：先绕Y轴旋转，再绕X轴旋转
     *
     * @param poseStack 当前的PoseStack
     * @param scaleX X轴缩放
     * @param scaleY Y轴缩放
     * @param scaleZ Z轴缩放
     * @param xOffset X轴偏移
     * @param yOffset Y轴偏移
     */
    public static void applyMinecraftIsometric(PoseStack poseStack, float scaleX, float scaleY, float scaleZ,
                                               float xOffset, float yOffset) {
        applyIsometricTransform(poseStack, xOffset, yOffset, 0.0f, scaleX, scaleY, scaleZ,
                ISOMETRIC_ROT_X, ISOMETRIC_ROT_Y);
    }

    // ============ 斜二测图方法 (Cabinet Projection) ============

    /**
     * 应用斜二测图变换（适配Minecraft左手坐标系）
     * 旋转顺序：先绕Y轴旋转，再绕X轴旋转
     *
     * @param poseStack 当前的PoseStack
     * @param offsetX X轴偏移量
     * @param offsetY Y轴偏移量
     * @param offsetZ Z轴偏移量
     * @param scale 缩放比例
     */
    public static void applyCabinetTransform(PoseStack poseStack, float offsetX, float offsetY, float offsetZ,
                                             float scale) {
        applyCabinetTransform(poseStack, offsetX, offsetY, offsetZ, scale, scale, scale);
    }

    /**
     * 应用斜二测图变换（适配Minecraft左手坐标系，支持不同轴向缩放）
     * 旋转顺序：先绕Y轴旋转，再绕X轴旋转
     *
     * @param poseStack 当前的PoseStack
     * @param offsetX X轴偏移量
     * @param offsetY Y轴偏移量
     * @param offsetZ Z轴偏移量
     * @param scaleX X轴缩放
     * @param scaleY Y轴缩放
     * @param scaleZ Z轴缩放
     */
    public static void applyCabinetTransform(PoseStack poseStack, float offsetX, float offsetY, float offsetZ,
                                             float scaleX, float scaleY, float scaleZ) {
        applyCabinetTransform(poseStack, offsetX, offsetY, offsetZ, scaleX, scaleY, scaleZ,
                CABINET_ROT_X, CABINET_ROT_Y);
    }

    /**
     * 应用斜二测图变换（自定义角度，适配Minecraft左手坐标系）
     * 旋转顺序：先绕Y轴旋转，再绕X轴旋转
     *
     * @param poseStack 当前的PoseStack
     * @param offsetX X轴偏移量
     * @param offsetY Y轴偏移量
     * @param offsetZ Z轴偏移量
     * @param scaleX X轴缩放
     * @param scaleY Y轴缩放
     * @param scaleZ Z轴缩放
     * @param rotX 绕X轴旋转角度（度）
     * @param rotY 绕Y轴旋转角度（度）
     */
    public static void applyCabinetTransform(PoseStack poseStack, float offsetX, float offsetY, float offsetZ,
                                             float scaleX, float scaleY, float scaleZ,
                                             float rotX, float rotY) {
        applyCoreTransform(poseStack, offsetX, offsetY, offsetZ, scaleX, scaleY, scaleZ, rotX, rotY);
    }

    /**
     * 应用斜二测图变换（带Z轴缩短系数，适配Minecraft左手坐标系）
     * 旋转顺序：先绕Y轴旋转，再绕X轴旋转
     *
     * @param poseStack 当前的PoseStack
     * @param offsetX X轴偏移量
     * @param offsetY Y轴偏移量
     * @param offsetZ Z轴偏移量
     * @param scale 基本缩放
     * @param zShorten Z轴缩短系数（0.5为斜二测标准）
     */
    public static void applyCabinetTransformWithZShorten(PoseStack poseStack, float offsetX, float offsetY, float offsetZ,
                                                         float scale, float zShorten) {
        applyCabinetTransformWithZShorten(poseStack, offsetX, offsetY, offsetZ, scale, zShorten,
                CABINET_ROT_X, CABINET_ROT_Y);
    }

    /**
     * 应用斜二测图变换（带Z轴缩短系数和自定义角度，适配Minecraft左手坐标系）
     * 旋转顺序：先绕Y轴旋转，再绕X轴旋转
     *
     * @param poseStack 当前的PoseStack
     * @param offsetX X轴偏移量
     * @param offsetY Y轴偏移量
     * @param offsetZ Z轴偏移量
     * @param scale 基本缩放
     * @param zShorten Z轴缩短系数
     * @param rotX 绕X轴旋转角度（度）
     * @param rotY 绕Y轴旋转角度（度）
     */
    public static void applyCabinetTransformWithZShorten(PoseStack poseStack, float offsetX, float offsetY, float offsetZ,
                                                         float scale, float zShorten,
                                                         float rotX, float rotY) {
        applyCabinetTransform(poseStack, offsetX, offsetY, offsetZ,
                scale, scale, scale * zShorten,
                rotX, rotY);
    }

    /**
     * 应用斜二测图变换（Minecraft物品渲染专用）
     * 旋转顺序：先绕Y轴旋转，再绕X轴旋转
     *
     * @param poseStack 当前的PoseStack
     * @param itemScale 物品缩放
     * @param xOffset X轴偏移
     * @param yOffset Y轴偏移
     */
    public static void applyMinecraftCabinet(PoseStack poseStack, float itemScale, float xOffset, float yOffset) {
        applyCabinetTransformWithZShorten(poseStack, xOffset, yOffset, 0.0f, itemScale, 0.5f);
    }

    /**
     * 应用斜二测图变换（Minecraft物品渲染专用，支持不同轴向缩放）
     * 旋转顺序：先绕Y轴旋转，再绕X轴旋转
     *
     * @param poseStack 当前的PoseStack
     * @param scaleX X轴缩放
     * @param scaleY Y轴缩放
     * @param scaleZ Z轴缩放
     * @param xOffset X轴偏移
     * @param yOffset Y轴偏移
     */
    public static void applyMinecraftCabinet(PoseStack poseStack, float scaleX, float scaleY, float scaleZ,
                                             float xOffset, float yOffset) {
        applyCabinetTransform(poseStack, xOffset, yOffset, 0.0f, scaleX, scaleY, scaleZ,
                CABINET_ROT_X, CABINET_ROT_Y);
    }

    // ============ 斜等测图方法 (Cavalier Projection) ============

    /**
     * 应用斜等测图变换（Cavalier Projection，适配Minecraft左手坐标系）
     * 旋转顺序：先绕Y轴旋转，再绕X轴旋转
     *
     * @param poseStack 当前的PoseStack
     * @param offsetX X轴偏移量
     * @param offsetY Y轴偏移量
     * @param offsetZ Z轴偏移量
     * @param scale 缩放比例
     */
    public static void applyCavalierTransform(PoseStack poseStack, float offsetX, float offsetY, float offsetZ,
                                              float scale) {
        applyCavalierTransform(poseStack, offsetX, offsetY, offsetZ, scale, scale, scale);
    }

    /**
     * 应用斜等测图变换（Cavalier Projection，适配Minecraft左手坐标系，支持不同轴向缩放）
     * 旋转顺序：先绕Y轴旋转，再绕X轴旋转
     *
     * @param poseStack 当前的PoseStack
     * @param offsetX X轴偏移量
     * @param offsetY Y轴偏移量
     * @param offsetZ Z轴偏移量
     * @param scaleX X轴缩放
     * @param scaleY Y轴缩放
     * @param scaleZ Z轴缩放
     */
    public static void applyCavalierTransform(PoseStack poseStack, float offsetX, float offsetY, float offsetZ,
                                              float scaleX, float scaleY, float scaleZ) {
        applyCavalierTransform(poseStack, offsetX, offsetY, offsetZ, scaleX, scaleY, scaleZ,
                CAVALIER_ROT_X, CAVALIER_ROT_Y);
    }

    /**
     * 应用斜等测图变换（Cavalier Projection，自定义角度，适配Minecraft左手坐标系）
     * 旋转顺序：先绕Y轴旋转，再绕X轴旋转
     *
     * @param poseStack 当前的PoseStack
     * @param offsetX X轴偏移量
     * @param offsetY Y轴偏移量
     * @param offsetZ Z轴偏移量
     * @param scaleX X轴缩放
     * @param scaleY Y轴缩放
     * @param scaleZ Z轴缩放
     * @param rotX 绕X轴旋转角度（度）
     * @param rotY 绕Y轴旋转角度（度）
     */
    public static void applyCavalierTransform(PoseStack poseStack, float offsetX, float offsetY, float offsetZ,
                                              float scaleX, float scaleY, float scaleZ,
                                              float rotX, float rotY) {
        applyCoreTransform(poseStack, offsetX, offsetY, offsetZ, scaleX, scaleY, scaleZ, rotX, rotY);
    }

    /**
     * 应用斜等测图变换（Minecraft物品渲染专用）
     * 旋转顺序：先绕Y轴旋转，再绕X轴旋转
     *
     * @param poseStack 当前的PoseStack
     * @param itemScale 物品缩放
     * @param xOffset X轴偏移
     * @param yOffset Y轴偏移
     */
    public static void applyMinecraftCavalier(PoseStack poseStack, float itemScale, float xOffset, float yOffset) {
        applyCavalierTransform(poseStack, xOffset, yOffset, 0.0f, itemScale, itemScale, itemScale,
                CAVALIER_ROT_X, CAVALIER_ROT_Y);
    }

    /**
     * 应用斜等测图变换（Minecraft物品渲染专用，支持不同轴向缩放）
     * 旋转顺序：先绕Y轴旋转，再绕X轴旋转
     *
     * @param poseStack 当前的PoseStack
     * @param scaleX X轴缩放
     * @param scaleY Y轴缩放
     * @param scaleZ Z轴缩放
     * @param xOffset X轴偏移
     * @param yOffset Y轴偏移
     */
    public static void applyMinecraftCavalier(PoseStack poseStack, float scaleX, float scaleY, float scaleZ,
                                              float xOffset, float yOffset) {
        applyCavalierTransform(poseStack, xOffset, yOffset, 0.0f, scaleX, scaleY, scaleZ,
                CAVALIER_ROT_X, CAVALIER_ROT_Y);
    }

    // ============ 通用辅助方法 ============

    /**
     * 获取正等轴测图标准角度（Minecraft左手坐标系）
     *
     * @return float数组 [rotX, rotY]
     */
    public static float[] getStandardIsometricAngles() {
        return new float[]{ISOMETRIC_ROT_X, ISOMETRIC_ROT_Y};
    }

    /**
     * 获取标准斜二测角度（Minecraft左手坐标系）
     *
     * @return float数组 [rotX, rotY]
     */
    public static float[] getStandardCabinetAngles() {
        return new float[]{CABINET_ROT_X, CABINET_ROT_Y};
    }

    /**
     * 获取标准斜等测角度（Minecraft左手坐标系）
     *
     * @return float数组 [rotX, rotY]
     */
    public static float[] getStandardCavalierAngles() {
        return new float[]{CAVALIER_ROT_X, CAVALIER_ROT_Y};
    }

    /**
     * 计算斜二测图的Z轴缩短系数
     *
     * @param rotX 绕X轴旋转角度（度）
     * @return Z轴缩短系数
     */
    public static float calculateZShortenFactor(float rotX) {
        double rad = Math.toRadians(rotX);
        return (float) Math.cos(rad);
    }
}