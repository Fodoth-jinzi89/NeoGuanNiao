package net.fodoth.skina.neoguanniao.content.bird.feature.scale;

/**
 * 鸟类模型缩放配置文件
 * 定义不同鸟类的模型缩放参数，包括基础渲染缩放和个体缩放范围
 *
 * <p>每种鸟类都有其独特的缩放配置：
 * <ul>
 *   <li>基础渲染缩放：模型的基础大小</li>
 *   <li>个体缩放范围：个体之间可以变化的范围（遗传和随机变异）</li>
 * </ul>
 */
public final class BirdModelScaleProfile {

    // ============ 预定义配置文件 ============

    /**
     * 夜鹭缩放配置 - 大型鸟类，个体变化较小
     */
    public static final BirdModelScaleProfile NIGHT_HERON = new BirdModelScaleProfile(1.0F, 0.9F, 1.1F);

    /**
     * 麻雀缩放配置 - 小型鸟类，个体变化适中
     */
    public static final BirdModelScaleProfile SPARROW = new BirdModelScaleProfile(1.0F, 0.86F, 1.14F);

    /**
     * 虎皮鹦鹉缩放配置 - 小型鸟类，基础缩放较小，个体变化较大
     */
    public static final BirdModelScaleProfile BUDGERIGAR = new BirdModelScaleProfile(0.6F, 0.42F, 1.16F);

    /**
     * 鸽类缩放配置 - 中型鸟类，个体变化较小
     */
    public static final BirdModelScaleProfile COLUMBID = new BirdModelScaleProfile(1.0F, 0.9F, 1.1F);

    // ============ 成员变量 ============

    private final float baseRenderScale;
    private final float minIndividualScale;
    private final float maxIndividualScale;

    // ============ 构造方法 ============

    private BirdModelScaleProfile(float baseRenderScale, float minIndividualScale, float maxIndividualScale) {
        this.baseRenderScale = baseRenderScale;
        this.minIndividualScale = minIndividualScale;
        this.maxIndividualScale = maxIndividualScale;

        // 验证参数有效性
        if (baseRenderScale <= 0.0F) {
            throw new IllegalArgumentException("baseRenderScale must be positive: " + baseRenderScale);
        }
        if (minIndividualScale <= 0.0F || maxIndividualScale <= 0.0F) {
            throw new IllegalArgumentException("Individual scale bounds must be positive: min=" + minIndividualScale + ", max=" + maxIndividualScale);
        }
        if (minIndividualScale > maxIndividualScale) {
            throw new IllegalArgumentException("minIndividualScale must be <= maxIndividualScale: " + minIndividualScale + " > " + maxIndividualScale);
        }
    }

    // ============ Getter 方法 ============

    /**
     * 获取基础渲染缩放值
     * 这是模型的基础大小，乘以个体缩放后得到最终渲染大小
     */
    public float baseRenderScale() {
        return this.baseRenderScale;
    }

    /**
     * 获取最小个体缩放值
     */
    public float minIndividualScale() {
        return this.minIndividualScale;
    }

    /**
     * 获取最大个体缩放值
     */
    public float maxIndividualScale() {
        return this.maxIndividualScale;
    }

    // ============ 工具方法 ============

    /**
     * 计算随机个体缩放值
     *
     * @param random 随机源
     * @return 随机缩放值
     */
    public float randomIndividualScale(net.minecraft.util.RandomSource random) {
        return BirdModelScale.randomIndividualScale(random, this);
    }

    /**
     * 获取个体缩放范围
     *
     * @return 范围大小 (max - min)
     */
    public float individualScaleRange() {
        return this.maxIndividualScale - this.minIndividualScale;
    }

    /**
     * 检查缩放值是否在有效范围内
     *
     * @param scale 要检查的缩放值
     * @return 如果在范围内返回 true
     */
    public boolean isScaleValid(float scale) {
        return scale >= this.minIndividualScale && scale <= this.maxIndividualScale;
    }

    /**
     * 获取中间个体缩放值
     */
    public float midIndividualScale() {
        return (this.minIndividualScale + this.maxIndividualScale) * 0.5F;
    }

    @Override
    public String toString() {
        return "BirdModelScaleProfile{" +
                "baseRenderScale=" + baseRenderScale +
                ", minIndividualScale=" + minIndividualScale +
                ", maxIndividualScale=" + maxIndividualScale +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof BirdModelScaleProfile that)) return false;

        return Float.compare(that.baseRenderScale, baseRenderScale) == 0
                && Float.compare(that.minIndividualScale, minIndividualScale) == 0
                && Float.compare(that.maxIndividualScale, maxIndividualScale) == 0;
    }

    @Override
    public int hashCode() {
        int result = Float.hashCode(baseRenderScale);
        result = 31 * result + Float.hashCode(minIndividualScale);
        result = 31 * result + Float.hashCode(maxIndividualScale);
        return result;
    }

    // ============ 工厂方法 ============

    /**
     * 创建自定义缩放配置文件
     *
     * @param baseRenderScale 基础渲染缩放 (必须 > 0)
     * @param minIndividualScale 最小个体缩放 (必须 > 0)
     * @param maxIndividualScale 最大个体缩放 (必须 > 0 且 >= minIndividualScale)
     * @return 新的缩放配置文件
     * @throws IllegalArgumentException 如果参数无效
     */
    public static BirdModelScaleProfile of(float baseRenderScale, float minIndividualScale, float maxIndividualScale) {
        return new BirdModelScaleProfile(baseRenderScale, minIndividualScale, maxIndividualScale);
    }
}