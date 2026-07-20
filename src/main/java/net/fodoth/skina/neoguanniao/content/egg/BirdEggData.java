package net.fodoth.skina.neoguanniao.content.egg;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * 鸟类蛋数据记录，存储蛋的所有属性信息
 * @param birdType  鸟类实体注册名
 * @param model     模型资源路径
 * @param skin      皮肤资源路径
 * @param gender    性别（true为雄性，false为雌性）
 * @param eggCount  蛋的数量
 * @param size      体型大小
 * @param hatchTime 剩余孵化时间（刻）
 * @param alive     是否存活
 */
public record BirdEggData(
        ResourceLocation birdType,
        ResourceLocation model,
        ResourceLocation skin,
        boolean gender,
        int eggCount,
        float size,
        int hatchTime,
        boolean alive
) {

    // ======================== 序列化 ========================

    /** Codec 用于数据存储和网络传输 */
    public static final Codec<BirdEggData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("bird_type").forGetter(BirdEggData::birdType),
                    ResourceLocation.CODEC.fieldOf("model").forGetter(BirdEggData::model),
                    ResourceLocation.CODEC.fieldOf("skin").forGetter(BirdEggData::skin),
                    Codec.BOOL.fieldOf("gender").forGetter(BirdEggData::gender),
                    Codec.INT.fieldOf("egg_count").forGetter(BirdEggData::eggCount),
                    Codec.FLOAT.fieldOf("size").forGetter(BirdEggData::size),
                    Codec.INT.fieldOf("hatch_time").forGetter(BirdEggData::hatchTime),
                    Codec.BOOL.fieldOf("alive").forGetter(BirdEggData::alive)
            ).apply(instance, BirdEggData::new)
    );

    // ======================== 工厂方法 ========================

    /** 创建一个蛋 */
    public static BirdEggData create(ResourceLocation birdType, ResourceLocation model,
                                     ResourceLocation skin, boolean gender, int eggCount,
                                     float size, int hatchTime, boolean alive) {
        return new BirdEggData(birdType, model, skin, gender, eggCount, size, hatchTime, alive);
    }

    /** 创建默认的蛋（简化版本） */
    public static BirdEggData createDefault(ResourceLocation birdType, ResourceLocation model,
                                            ResourceLocation skin, float size) {
        return new BirdEggData(birdType, model, skin, true, 1, size, 6000, true);
    }

    // ======================== 状态查询 ========================

    /** 判断是否已达到孵化条件（孵化倒计时归零） */
    public boolean canHatch() {
        return alive && hatchTime <= 0;
    }

    /** 判断是否为雄性 */
    public boolean isMale() {
        return gender;
    }

    /** 判断是否为雌性 */
    public boolean isFemale() {
        return !gender;
    }

    /** 判断蛋是否存活 */
    public boolean isAlive() {
        return alive;
    }

    // ======================== 状态更新 ========================

    /** 减少一刻孵化时间，返回新实例（不可变对象） */
    public BirdEggData tickDown() {
        return new BirdEggData(
                birdType,
                model,
                skin,
                gender,
                eggCount,
                size,
                Math.max(0, hatchTime - 1), // 最小为0，防止负数
                alive
        );
    }

    /** 设置孵化时间，返回新实例 */
    public BirdEggData withHatchTime(int newHatchTime) {
        return new BirdEggData(
                birdType,
                model,
                skin,
                gender,
                eggCount,
                size,
                Math.max(0, newHatchTime),
                alive
        );
    }

    /** 设置存活状态，返回新实例 */
    public BirdEggData withAlive(boolean newAlive) {
        return new BirdEggData(
                birdType,
                model,
                skin,
                gender,
                eggCount,
                size,
                hatchTime,
                newAlive
        );
    }

    /** 增加蛋的数量，返回新实例 */
    public BirdEggData withEggCount(int newEggCount) {
        return new BirdEggData(
                birdType,
                model,
                skin,
                gender,
                Math.max(0, newEggCount),
                size,
                hatchTime,
                alive
        );
    }

    /** 设置性别，返回新实例 */
    public BirdEggData withGender(boolean newGender) {
        return new BirdEggData(
                birdType,
                model,
                skin,
                newGender,
                eggCount,
                size,
                hatchTime,
                alive
        );
    }

    /** 设置大小，返回新实例 */
    public BirdEggData withSize(float newSize) {
        return new BirdEggData(
                birdType,
                model,
                skin,
                gender,
                eggCount,
                Math.max(0.1f, newSize),
                hatchTime,
                alive
        );
    }

    // ======================== 辅助方法 ========================

    @Override
    public @NotNull String toString() {
        return String.format("BirdEggData{birdType=%s, gender=%s, eggCount=%d, size=%.2f, hatchTime=%d, alive=%s}",
                birdType, gender ? "male" : "female", eggCount, size, hatchTime, alive);
    }

    public BirdEggData tickDown(int ticks) {
        return new BirdEggData(
                birdType,
                model,
                skin,
                gender,
                eggCount,
                size,
                Math.max(0, hatchTime - ticks),
                alive
        );
    }
}