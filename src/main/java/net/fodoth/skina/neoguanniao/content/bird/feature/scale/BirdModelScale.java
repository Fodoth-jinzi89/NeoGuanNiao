package net.fodoth.skina.neoguanniao.content.bird.feature.scale;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;

/**
 * 鸟类模型缩放工具类
 * 负责管理鸟类模型的个体缩放，包括随机生成、遗传继承和渲染缩放
 * <p>
 * 每种鸟类都有一个基础的模型缩放配置，个体之间可以有不同的缩放值，
 * 这使得同一种鸟类在视觉上有个体差异。
 */
public final class BirdModelScale {

    public static final String NBT_KEY = "BirdModelScale";
    public static final float DEFAULT_INDIVIDUAL_SCALE = 1.0F;

    private BirdModelScale() {
    }

    /**
     * 生成随机的个体缩放值
     *
     * @param random 随机源
     * @param profile 缩放配置文件
     * @return 随机缩放值
     */
    public static float randomIndividualScale(RandomSource random, BirdModelScaleProfile profile) {
        return Mth.lerp(random.nextFloat(), profile.minIndividualScale(), profile.maxIndividualScale());
    }

    public static float randomIndividualScale(RandomSource random, BirdModelScaleProfile profile, boolean isBaby) {
        float end = (profile.minIndividualScale() + profile.maxIndividualScale()) / 2.0F;
        if (!isBaby) return Mth.lerp(random.nextFloat(), end, profile.maxIndividualScale());
        return Mth.lerp(random.nextFloat(), profile.minIndividualScale(), end);
    }

    /**
     * 继承父母的个体缩放值（带随机变异）
     *
     * @param random 随机源
     * @param firstParentScale 第一个父母的缩放值
     * @param secondParentScale 第二个父母的缩放值
     * @param profile 缩放配置文件
     * @return 继承后的缩放值
     */
    public static float inheritIndividualScale(
            RandomSource random,
            float firstParentScale,
            float secondParentScale,
            BirdModelScaleProfile profile
    ) {
        float average = (sanitize(firstParentScale, profile) + sanitize(secondParentScale, profile)) * 0.5F;
        float smallMutation = (random.nextFloat() - 0.5F) * 0.06F;

        // 12% 的概率完全随机，否则继承父母平均值并添加小变异
        if (random.nextFloat() < 0.12F) {
            return randomIndividualScale(random, profile);
        }
        return sanitize(average + smallMutation, profile);
    }

    /**
     * 计算最终渲染缩放值
     *
     * @param profile 缩放配置文件
     * @param individualScale 个体缩放值
     * @return 最终渲染缩放值
     */
    public static float renderScale(BirdModelScaleProfile profile, float individualScale) {
        return profile.baseRenderScale() * sanitize(individualScale, profile);
    }



    /**
     * 清理并规范化缩放值
     *
     * @param scale 原始缩放值
     * @param profile 缩放配置文件
     * @return 规范化的缩放值
     */
    public static float sanitize(float scale, BirdModelScaleProfile profile) {
        if (!Float.isFinite(scale) || scale <= 0.0F) {
            return 1.0F;
        }
        return Math.clamp(scale, profile.minIndividualScale(), profile.maxIndividualScale());
    }

    /**
     * 保存缩放值到 NBT
     *
     * @param compoundTag NBT 标签
     * @param individualScale 个体缩放值
     * @param profile 缩放配置文件
     */
    public static void save(@NotNull CompoundTag compoundTag, float individualScale, BirdModelScaleProfile profile) {
        compoundTag.putFloat(NBT_KEY, sanitize(individualScale, profile));
    }

    /**
     * 从 NBT 加载缩放值
     *
     * @param compoundTag NBT 标签
     * @param profile 缩放配置文件
     * @return 加载的缩放值，如果不存在则返回默认值
     */
    public static float load(@NotNull CompoundTag compoundTag, BirdModelScaleProfile profile) {
        if (!compoundTag.contains(NBT_KEY, CompoundTag.TAG_FLOAT)) {
            return DEFAULT_INDIVIDUAL_SCALE;
        }
        return sanitize(compoundTag.getFloat(NBT_KEY), profile);
    }

    /**
     * 检查两个缩放值是否近似相等（用于调试）
     *
     * @param a 第一个值
     * @param b 第二个值
     * @param epsilon 容差
     * @return 如果近似相等返回 true
     */
    public static boolean approximatelyEqual(float a, float b, float epsilon) {
        return Math.abs(a - b) < epsilon;
    }
}