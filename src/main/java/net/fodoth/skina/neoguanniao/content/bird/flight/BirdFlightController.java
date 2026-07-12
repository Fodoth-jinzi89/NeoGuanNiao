package net.fodoth.skina.neoguanniao.content.bird.flight;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * 鸟类飞行控制器
 * 提供飞行控制相关的工具方法，包括移动混合、转向、减速和朝向控制
 */
public final class BirdFlightController {

    private BirdFlightController() {
    }

    /**
     * 混合当前移动和期望移动
     *
     * @param current 当前移动向量
     * @param desired 期望移动向量
     * @param desiredWeight 期望权重 (0-1)
     * @return 混合后的移动向量
     */
    @NotNull
    public static Vec3 blendMovement(Vec3 current, Vec3 desired, double desiredWeight) {
        double weight = Mth.clamp(desiredWeight, 0.0, 1.0);
        return current.scale(1.0 - weight).add(desired.scale(weight));
    }

    /**
     * 计算朝向目标的转向向量
     *
     * @param bird 鸟类实体
     * @param target 目标位置
     * @param speed 速度
     * @param minVertical 最小垂直速度
     * @param maxVertical 最大垂直速度
     * @return 转向向量
     */
    @NotNull
    public static Vec3 steerToward(Mob bird, Vec3 target, double speed, double minVertical, double maxVertical) {
        Vec3 toTarget = target.subtract(bird.position());
        Vec3 horizontal = new Vec3(toTarget.x, 0.0, toTarget.z);

        if (horizontal.lengthSqr() <= 1.0E-4) {
            horizontal = bird.getDeltaMovement().multiply(1.0, 0.0, 1.0);
        }
        if (horizontal.lengthSqr() <= 1.0E-4) {
            horizontal = bird.getLookAngle().multiply(1.0, 0.0, 1.0);
        }
        if (horizontal.lengthSqr() <= 1.0E-4) {
            horizontal = new Vec3(1.0, 0.0, 0.0);
        }

        double vertical = Mth.clamp(toTarget.y * 0.12, minVertical, maxVertical);
        return horizontal.normalize().scale(speed).add(0.0, vertical, 0.0);
    }

    /**
     * 计算着陆时的减速效果
     *
     * @param baseSpeed 基础速度
     * @param distance 到目标距离
     * @param decelerationDistance 减速距离
     * @param minFactor 最小速度因子
     * @return 减速后的速度
     */
    public static double decelerateNearLanding(double baseSpeed, double distance, double decelerationDistance, double minFactor) {
        if (decelerationDistance <= 0.0 || distance >= decelerationDistance) {
            return baseSpeed;
        }
        double factor = Mth.clamp(distance / decelerationDistance, minFactor, 1.0);
        return baseSpeed * factor;
    }

    /**
     * 检查是否在空中失速
     *
     * @param bird 鸟类实体
     * @param timeFlying 飞行时间
     * @param minMovementSqr 最小移动平方阈值
     * @return 如果失速返回 true
     */
    public static boolean isStalledInAir(Mob bird, int timeFlying, double minMovementSqr) {
        return timeFlying > 15 && !bird.onGround() && bird.getDeltaMovement().lengthSqr() < minMovementSqr;
    }

    /**
     * 使实体面朝移动方向（用于飞行）
     *
     * @param bird 鸟类实体
     * @param movement 移动向量
     * @param maxPitchDegrees 最大俯仰角度
     */
    public static void faceMovement(Mob bird, Vec3 movement, float maxPitchDegrees) {
        double horizontalLength = Math.sqrt(movement.x * movement.x + movement.z * movement.z);
        if (horizontalLength <= 1.0E-4) {
            return;
        }

        float yaw = (float) (Mth.atan2(movement.z, movement.x) * (180.0 / Math.PI)) - 90.0F;
        float pitch = Mth.clamp(
                (float) (-(Math.atan2(movement.y, horizontalLength) * (180.0 / Math.PI))),
                -maxPitchDegrees,
                maxPitchDegrees
        );

        bird.setYRot(yaw);
        bird.yRotO = yaw;
        bird.yBodyRot = yaw;
        bird.yHeadRot = yaw;
        bird.yHeadRotO = yaw;
        bird.setXRot(pitch);
        bird.xRotO = pitch;
    }

    /**
     * 使实体面朝地面移动方向
     *
     * @param bird 鸟类实体
     * @param movement 移动向量
     * @param minHorizontalSpeedSqr 最小水平速度平方阈值
     * @return 如果成功转向返回 true
     */

    public static boolean faceGroundMovement(Mob bird, Vec3 movement, double minHorizontalSpeedSqr) {
        if (movement.lengthSqr() <= minHorizontalSpeedSqr) {
            return false;
        }

        float yaw = (float) (Mth.atan2(movement.z, movement.x) * (180.0 / Math.PI)) - 90.0F;
        bird.setYRot(yaw);
        bird.yRotO = yaw;
        bird.yBodyRot = yaw;
        bird.yHeadRot = yaw;
        bird.yHeadRotO = yaw;
        bird.setXRot(0.0F);
        bird.xRotO = 0.0F;
        return true;
    }

    /**
     * 判断是否应该播放飞行动画
     *
     * @param bird 鸟类实体（实现了 BirdFlightAware 接口）
     * @param airborneState 空中状态
     * @param onGround 是否在地面
     * @param noGravity 是否无重力
     * @param movement 移动向量
     * @param airborneGraceTicks 空中宽限期
     * @return 如果应该播放飞行动画返回 true
     */
    public static boolean shouldPlayFlyAnimation(
            BirdFlightAware bird,
            boolean airborneState,
            boolean onGround,
            boolean noGravity,
            Vec3 movement,
            int airborneGraceTicks
    ) {
        if (!bird.isBirdFlightActive() && !airborneState) {
            if (onGround) {
                return false;
            }
            if (airborneGraceTicks > 0) {
                return true;
            }
            if (!noGravity && !bird.isBirdLanding() && !bird.isBirdEscaping()) {
                if (movement.y > -0.85) {
                    return true;
                }
                return movement.lengthSqr() > 0.001;
            }
            return true;
        }
        return true;
    }
}