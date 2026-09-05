package net.fodoth.skina.neoguanniao.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

public class TransformUtil {

    public static void applyTransform(PoseStack poseStack, float offsetX, float offsetY, float offsetZ,
                                          float scale, float rotY, float rotX, float rotZ) {
        poseStack.mulPose(Axis.YP.rotationDegrees(rotY));
        poseStack.mulPose(Axis.XP.rotationDegrees(rotX));
        poseStack.mulPose(Axis.ZP.rotationDegrees(rotZ));
        poseStack.translate(offsetX, offsetY, offsetZ);
        poseStack.scale(scale, scale, scale);
    }
}