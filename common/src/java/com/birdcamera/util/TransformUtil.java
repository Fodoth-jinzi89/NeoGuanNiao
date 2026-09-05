package com.birdcamera.util;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * 变换工具类 - 提供骨骼动画渲染所需的矩阵、四元数、向量变换操作
 */
public final class TransformUtil {

    private TransformUtil() {}

    /**
     * 线性插值 float
     */
    public static float lerp(float start, float end, float delta) {
        return Mth.lerp(delta, start, end);
    }

    /**
     * 线性插值 double
     */
    public static double lerp(double start, double end, double delta) {
        return start + (end - start) * delta;
    }

    /**
     * 线性插值 Vec3
     */
    public static Vec3 lerpVec(Vec3 start, Vec3 end, float delta) {
        return new Vec3(
                lerp(start.x, end.x, delta),
                lerp(start.y, end.y, delta),
                lerp(start.z, end.z, delta)
        );
    }

    /**
     * 线性插值四元数（球面线性插值的近似）
     */
    public static Quaternionf lerpQuaternion(Quaternionf start, Quaternionf end, float delta) {
        Quaternionf result = new Quaternionf(start);
        result.slerp(end, delta);
        return result;
    }

    /**
     * 创建绕X轴旋转的四元数
     */
    public static Quaternionf rotationX(float radians) {
        return new Quaternionf().rotationX(radians);
    }

    /**
     * 创建绕Y轴旋转的四元数
     */
    public static Quaternionf rotationY(float radians) {
        return new Quaternionf().rotationY(radians);
    }

    /**
     * 创建绕Z轴旋转的四元数
     */
    public static Quaternionf rotationZ(float radians) {
        return new Quaternionf().rotationZ(radians);
    }

    /**
     * 创建绕自定义轴旋转的四元数
     */
    public static Quaternionf rotationAxis(float radians, Vector3f axis) {
        return new Quaternionf().fromAxisAngleRad(axis, radians);
    }

    /**
     * 组合多个四元数旋转
     */
    public static Quaternionf combineRotations(Quaternionf... rotations) {
        Quaternionf result = new Quaternionf();
        for (Quaternionf rotation : rotations) {
            result.mul(rotation);
        }
        return result;
    }

    /**
     * 角度转弧度
     */
    public static float toRadians(float degrees) {
        return (float) Math.toRadians(degrees);
    }

    /**
     * 弧度转角度
     */
    public static float toDegrees(float radians) {
        return (float) Math.toDegrees(radians);
    }

    /**
     * 计算两点之间的距离
     */
    public static double distance(Vec3 a, Vec3 b) {
        return a.distanceTo(b);
    }

    /**
     * 计算两点之间的水平距离（忽略Y）
     */
    public static double horizontalDistance(Vec3 a, Vec3 b) {
        double dx = a.x - b.x;
        double dz = a.z - b.z;
        return Math.sqrt(dx * dx + dz * dz);
    }

    /**
     * 计算朝向角度（弧度）
     */
    public static float lookAtAngle(Vec3 from, Vec3 to) {
        double dx = to.x - from.x;
        double dz = to.z - from.z;
        return (float) Math.atan2(dz, dx);
    }

    /**
     * 平滑阻尼
     */
    public static float smoothDamp(float current, float target, float velocity, float smoothTime, float deltaTime) {
        float omega = 2.0f / smoothTime;
        float x = omega * deltaTime;
        float exp = 1.0f / (1.0f + x + 0.48f * x * x + 0.235f * x * x * x);
        float change = current - target;
        float temp = (velocity + omega * change) * deltaTime;
        velocity = (velocity - omega * temp) * exp;
        float result = target + (change + temp) * exp;
        return result;
    }

    /**
     * 将值钳制在范围内
     */
    public static float clamp(float value, float min, float max) {
        return Mth.clamp(value, min, max);
    }

    /**
     * 将值钳制在范围内
     */
    public static int clamp(int value, int min, int max) {
        return Mth.clamp(value, min, max);
    }

    /**
     * 创建缩放向量
     */
    public static Vector3f scale(float x, float y, float z) {
        return new Vector3f(x, y, z);
    }

    /**
     * 创建均匀缩放向量
     */
    public static Vector3f uniformScale(float scale) {
        return new Vector3f(scale, scale, scale);
    }

    /**
     * 创建位移向量
     */
    public static Vector3f translate(double x, double y, double z) {
        return new Vector3f((float) x, (float) y, (float) z);
    }
}
