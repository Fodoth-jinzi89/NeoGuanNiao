package net.fodoth.skina.neoguanniao.content.bird.flight;

/**
 * 鸟类飞行配置文件
 * 定义不同鸟类的飞行参数，包括速度、高度、飞行时长等
 * 每种鸟类都有其独特的飞行特性，这些参数会影响飞行行为：
 *   巡航速度：正常飞行速度
 *   逃逸速度：受惊时的飞行速度
 *   飞行高度范围：最小和最大巡航高度
 *   飞行时长：最小和最大连续飞行时间
 */
public final class BirdFlightProfile {

    // ============ 预定义配置文件 ============

    /**
     * 麻雀飞行配置 - 小型鸟类，快速灵活
     */
    public static final BirdFlightProfile SPARROW = new BirdFlightProfile(
            2.0, 6.0,      // 巡航高度范围 (min, max)
            0.24, 0.42,    // 巡航速度, 逃逸速度
            0.18,          // 着陆速度
            22, 122,       // 飞行时长范围 (min, max)
            4.0, 18.0,     // 空中目标距离范围 (min, max)
            3.2,           // 最大垂直步进
            42.0F          // 最大俯仰角度
    );

    /**
     * 虎皮鹦鹉飞行配置 - 小型鹦鹉，活跃好动
     */
    public static final BirdFlightProfile BUDGERIGAR = new BirdFlightProfile(
            3.0, 9.0,      // 巡航高度范围
            0.26, 0.34,    // 巡航速度, 逃逸速度
            0.2,           // 着陆速度
            90, 260,       // 飞行时长范围
            4.0, 12.0,     // 空中目标距离范围
            3.8,           // 最大垂直步进
            42.0F          // 最大俯仰角度
    );

    /**
     * 鸽类飞行配置 - 中型鸟类，耐力好
     */
    public static final BirdFlightProfile COLUMBID = new BirdFlightProfile(
            12.0, 24.0,    // 巡航高度范围
            0.38, 0.44,    // 巡航速度, 逃逸速度
            0.24,          // 着陆速度
            520, 820,      // 飞行时长范围
            24.0, 68.0,    // 空中目标距离范围
            9.5,           // 最大垂直步进
            40.0F          // 最大俯仰角度
    );

    /**
     * 夜鹭飞行配置 - 大型鸟类，优雅缓慢
     */
    public static final BirdFlightProfile NIGHT_HERON = new BirdFlightProfile(
            7.0, 36.0,     // 巡航高度范围
            0.4, 0.55,     // 巡航速度, 逃逸速度
            0.24,          // 着陆速度
            80, 320,       // 飞行时长范围
            18.0, 64.0,    // 空中目标距离范围
            9.0,           // 最大垂直步进
            36.0F          // 最大俯仰角度
    );

    // ============ 成员变量 ============

    private final double minCruiseHeight;
    private final double maxCruiseHeight;
    private final double cruiseSpeed;
    private final double escapeSpeed;
    private final double landingSpeed;
    private final int minFlightTicks;
    private final int maxFlightTicks;
    private final double minAirTargetDistance;
    private final double maxAirTargetDistance;
    private final double maxVerticalStep;
    private final float maxPitchDegrees;

    // ============ 构造方法 ============

    private BirdFlightProfile(
            double minCruiseHeight,
            double maxCruiseHeight,
            double cruiseSpeed,
            double escapeSpeed,
            double landingSpeed,
            int minFlightTicks,
            int maxFlightTicks,
            double minAirTargetDistance,
            double maxAirTargetDistance,
            double maxVerticalStep,
            float maxPitchDegrees
    ) {
        this.minCruiseHeight = minCruiseHeight;
        this.maxCruiseHeight = maxCruiseHeight;
        this.cruiseSpeed = cruiseSpeed;
        this.escapeSpeed = escapeSpeed;
        this.landingSpeed = landingSpeed;
        this.minFlightTicks = minFlightTicks;
        this.maxFlightTicks = Math.max(minFlightTicks, maxFlightTicks);
        this.minAirTargetDistance = minAirTargetDistance;
        this.maxAirTargetDistance = Math.max(minAirTargetDistance, maxAirTargetDistance);
        this.maxVerticalStep = maxVerticalStep;
        this.maxPitchDegrees = maxPitchDegrees;
    }

    // ============ Getter 方法 ============

    /**
     * 获取最小巡航高度
     */
    public double minCruiseHeight() {
        return this.minCruiseHeight;
    }

    /**
     * 获取最大巡航高度
     */
    public double maxCruiseHeight() {
        return this.maxCruiseHeight;
    }

    /**
     * 获取巡航速度
     */
    public double cruiseSpeed() {
        return this.cruiseSpeed;
    }

    /**
     * 获取逃逸速度（受惊时飞行速度）
     */
    public double escapeSpeed() {
        return this.escapeSpeed;
    }

    /**
     * 获取着陆速度
     */
    public double landingSpeed() {
        return this.landingSpeed;
    }

    /**
     * 获取最小飞行时长（tick）
     */
    public int minFlightTicks() {
        return this.minFlightTicks;
    }

    /**
     * 获取最大飞行时长（tick）
     */
    public int maxFlightTicks() {
        return this.maxFlightTicks;
    }

    /**
     * 获取最小空中目标距离
     */
    public double minAirTargetDistance() {
        return this.minAirTargetDistance;
    }

    /**
     * 获取最大空中目标距离
     */
    public double maxAirTargetDistance() {
        return this.maxAirTargetDistance;
    }

    /**
     * 获取最大垂直步进
     */
    public double maxVerticalStep() {
        return this.maxVerticalStep;
    }

    /**
     * 获取最大俯仰角度（度）
     */
    public float maxPitchDegrees() {
        return this.maxPitchDegrees;
    }

    // ============ 工具方法 ============

    /**
     * 获取随机的飞行时长
     *
     * @param random 随机数生成器
     * @return 飞行时长（tick）
     */
    public int randomFlightTicks(java.util.Random random) {
        return this.minFlightTicks + random.nextInt(this.maxFlightTicks - this.minFlightTicks + 1);
    }

    /**
     * 获取随机的空中目标距离
     *
     * @param random 随机数生成器
     * @return 空中目标距离
     */
    public double randomAirTargetDistance(java.util.Random random) {
        return this.minAirTargetDistance + random.nextDouble() * (this.maxAirTargetDistance - this.minAirTargetDistance);
    }

    @Override
    public String toString() {
        return "BirdFlightProfile{" +
                "minCruiseHeight=" + minCruiseHeight +
                ", maxCruiseHeight=" + maxCruiseHeight +
                ", cruiseSpeed=" + cruiseSpeed +
                ", escapeSpeed=" + escapeSpeed +
                ", landingSpeed=" + landingSpeed +
                ", minFlightTicks=" + minFlightTicks +
                ", maxFlightTicks=" + maxFlightTicks +
                ", minAirTargetDistance=" + minAirTargetDistance +
                ", maxAirTargetDistance=" + maxAirTargetDistance +
                ", maxVerticalStep=" + maxVerticalStep +
                ", maxPitchDegrees=" + maxPitchDegrees +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof BirdFlightProfile that)) return false;

        return Double.compare(that.minCruiseHeight, minCruiseHeight) == 0
                && Double.compare(that.maxCruiseHeight, maxCruiseHeight) == 0
                && Double.compare(that.cruiseSpeed, cruiseSpeed) == 0
                && Double.compare(that.escapeSpeed, escapeSpeed) == 0
                && Double.compare(that.landingSpeed, landingSpeed) == 0
                && minFlightTicks == that.minFlightTicks
                && maxFlightTicks == that.maxFlightTicks
                && Double.compare(that.minAirTargetDistance, minAirTargetDistance) == 0
                && Double.compare(that.maxAirTargetDistance, maxAirTargetDistance) == 0
                && Double.compare(that.maxVerticalStep, maxVerticalStep) == 0
                && Float.compare(that.maxPitchDegrees, maxPitchDegrees) == 0;
    }

    @Override
    public int hashCode() {
        int result = Double.hashCode(minCruiseHeight);
        result = 31 * result + Double.hashCode(maxCruiseHeight);
        result = 31 * result + Double.hashCode(cruiseSpeed);
        result = 31 * result + Double.hashCode(escapeSpeed);
        result = 31 * result + Double.hashCode(landingSpeed);
        result = 31 * result + minFlightTicks;
        result = 31 * result + maxFlightTicks;
        result = 31 * result + Double.hashCode(minAirTargetDistance);
        result = 31 * result + Double.hashCode(maxAirTargetDistance);
        result = 31 * result + Double.hashCode(maxVerticalStep);
        result = 31 * result + Float.hashCode(maxPitchDegrees);
        return result;
    }
}