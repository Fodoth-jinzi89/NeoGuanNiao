package net.fodoth.skina.neoguanniao.client.camera;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;

public final class NikonCameraArmPose {

    private NikonCameraArmPose() {
    }

    public static HumanoidModel.ArmPose cameraHold() {
        return HumanoidModel.ArmPose.ITEM;
    }

    private static void applyCameraHold(HumanoidModel<?> model, LivingEntity entity, HumanoidArm arm) {
        float headYaw = model.head.yRot * 0.35f;
        float idle = Mth.sin((float)((float)entity.tickCount * 0.067f)) * 0.012f;
        model.rightArm.xRot = -1.32f + model.head.xRot * 0.18f + idle;
        model.rightArm.yRot = -0.24f + headYaw;
        model.rightArm.zRot = 0.04f;
        model.leftArm.xRot = -1.32f + model.head.xRot * 0.18f + idle;
        model.leftArm.yRot = 0.24f + headYaw;
        model.leftArm.zRot = -0.04f;
    }
}

