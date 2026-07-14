package net.fodoth.skina.neoguanniao.content.bird.feature.brain;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;

/**
 * 鸟类性格系统
 * 包含鸟类的五种核心性格特征，影响行为决策
 */
public class BirdPersonality {
    private final float boldness;      // 胆量
    private final float wariness;      // 警惕性
    private final float activity;      // 活跃度
    private final float sociability;   // 社交性
    private final float flightiness;   // 易惊性

    private BirdPersonality(float boldness, float wariness, float activity, float sociability, float flightiness) {
        this.boldness = clamp(boldness);
        this.wariness = clamp(wariness);
        this.activity = clamp(activity);
        this.sociability = clamp(sociability);
        this.flightiness = clamp(flightiness);
    }

    /**
     * 创建新的性格实例，基于物种基础值加上随机变化
     *
     * @param random 随机源
     * @param profile 物种配置文件
     * @return 新的性格实例
     */
    public static BirdPersonality create(RandomSource random, BirdSpeciesProfile profile) {
        return new BirdPersonality(
                vary(random, profile.baseBoldness()),
                vary(random, profile.baseWariness()),
                vary(random, profile.baseActivity()),
                vary(random, profile.baseSociability()),
                vary(random, profile.baseFlightiness())
        );
    }

    /**
     * 从 NBT 加载性格数据，如果数据无效则创建新的
     *
     * @param tag NBT 标签
     * @param random 随机源
     * @param profile 物种配置文件
     * @return 加载或创建的性格实例
     */
    public static BirdPersonality load(@NotNull CompoundTag tag, RandomSource random, BirdSpeciesProfile profile) {
        if (tag.contains("Boldness", CompoundTag.TAG_FLOAT)
                && tag.contains("Wariness", CompoundTag.TAG_FLOAT)
                && tag.contains("Activity", CompoundTag.TAG_FLOAT)
                && tag.contains("Sociability", CompoundTag.TAG_FLOAT)
                && tag.contains("Flightiness", CompoundTag.TAG_FLOAT)) {
            return new BirdPersonality(
                    tag.getFloat("Boldness"),
                    tag.getFloat("Wariness"),
                    tag.getFloat("Activity"),
                    tag.getFloat("Sociability"),
                    tag.getFloat("Flightiness")
            );
        }
        return create(random, profile);
    }

    /**
     * 保存性格数据到 NBT
     *
     * @param tag NBT 标签
     */
    public void save(@NotNull CompoundTag tag) {
        tag.putFloat("Boldness", this.boldness);
        tag.putFloat("Wariness", this.wariness);
        tag.putFloat("Activity", this.activity);
        tag.putFloat("Sociability", this.sociability);
        tag.putFloat("Flightiness", this.flightiness);
    }

    // Getters
    public float boldness() {
        return this.boldness;
    }

    public float wariness() {
        return this.wariness;
    }

    public float activity() {
        return this.activity;
    }

    public float sociability() {
        return this.sociability;
    }

    public float flightiness() {
        return this.flightiness;
    }

    /**
     * 对基础值应用随机变化
     * 变化范围为 ±0.12
     *
     * @param random 随机源
     * @param base 基础值
     * @return 变化后的值
     */
    private static float vary(RandomSource random, float base) {
        return clamp(base + (random.nextFloat() - 0.5F) * 0.24F);
    }

    /**
     * 将值限制在 0.0 到 1.0 之间
     *
     * @param value 要限制的值
     * @return 限制后的值
     */
    private static float clamp(float value) {
        return Mth.clamp(value, 0.0F, 1.0F);
    }

    @Override
    public String toString() {
        return "BirdPersonality{" +
                "boldness=" + boldness +
                ", wariness=" + wariness +
                ", activity=" + activity +
                ", sociability=" + sociability +
                ", flightiness=" + flightiness +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof BirdPersonality that)) return false;

        return Float.compare(that.boldness, boldness) == 0
                && Float.compare(that.wariness, wariness) == 0
                && Float.compare(that.activity, activity) == 0
                && Float.compare(that.sociability, sociability) == 0
                && Float.compare(that.flightiness, flightiness) == 0;
    }

    @Override
    public int hashCode() {
        int result = Float.hashCode(boldness);
        result = 31 * result + Float.hashCode(wariness);
        result = 31 * result + Float.hashCode(activity);
        result = 31 * result + Float.hashCode(sociability);
        result = 31 * result + Float.hashCode(flightiness);
        return result;
    }
}