package net.fodoth.skina.neoguanniao.content.egg;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * 鸟类蛋数据记录，存储蛋的所有属性信息
 * @param birdType        鸟类实体注册名
 * @param gender          性别（true为雄性，false为雌性）
 * @param model           模型资源路径
 * @param skin            皮肤资源路径
 * @param eggCount        蛋的数量
 * @param featherCount    羽毛数量
 * @param featherInterval 羽毛掉落间隔
 * @param size            体型大小
 * @param hatchTime       剩余孵化时间（刻）
 * @param alive           是否存活
 */
public record BirdEggData(
        ResourceLocation birdType,
        boolean gender,
        ResourceLocation model,
        ResourceLocation skin,
        int eggCount,
        int featherCount,
        int featherInterval,
        float size,
        int hatchTime,
        boolean alive
) {

    // ======================== 序列化 ========================

    /** Codec 用于数据存储和网络传输 */
    public static final Codec<BirdEggData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("bird_type").forGetter(BirdEggData::birdType),
                    Codec.BOOL.fieldOf("gender").forGetter(BirdEggData::gender),
                    ResourceLocation.CODEC.fieldOf("model").forGetter(BirdEggData::model),
                    ResourceLocation.CODEC.fieldOf("skin").forGetter(BirdEggData::skin),
                    Codec.INT.fieldOf("egg_count").forGetter(BirdEggData::eggCount),
                    Codec.INT.fieldOf("feather_count").forGetter(BirdEggData::featherCount),
                    Codec.INT.fieldOf("feather_interval").forGetter(BirdEggData::featherInterval),
                    Codec.FLOAT.fieldOf("size").forGetter(BirdEggData::size),
                    Codec.INT.fieldOf("hatch_time").forGetter(BirdEggData::hatchTime),
                    Codec.BOOL.fieldOf("alive").forGetter(BirdEggData::alive)
            ).apply(instance, BirdEggData::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, BirdEggData> STREAM_CODEC =
            StreamCodec.of(
                    (buf, data) -> {
                        ResourceLocation.STREAM_CODEC.encode(buf, data.birdType());
                        buf.writeBoolean(data.gender());
                        ResourceLocation.STREAM_CODEC.encode(buf, data.model());
                        ResourceLocation.STREAM_CODEC.encode(buf, data.skin());
                        buf.writeInt(data.eggCount());
                        buf.writeInt(data.featherCount());
                        buf.writeInt(data.featherInterval());
                        buf.writeFloat(data.size());
                        buf.writeInt(data.hatchTime());
                        buf.writeBoolean(data.alive());
                    },

                    buf -> new BirdEggData(
                            ResourceLocation.STREAM_CODEC.decode(buf),
                            buf.readBoolean(),
                            ResourceLocation.STREAM_CODEC.decode(buf),
                            ResourceLocation.STREAM_CODEC.decode(buf),
                            buf.readInt(),
                            buf.readInt(),
                            buf.readInt(),
                            buf.readFloat(),
                            buf.readInt(),
                            buf.readBoolean()
                    )
            );

    // ======================== 工厂方法 ========================

    /** 创建一个蛋 */
    public static BirdEggData create(ResourceLocation birdType, boolean gender,
                                     ResourceLocation model, ResourceLocation skin,
                                     int eggCount, int featherCount, int featherInterval,
                                     float size, int hatchTime, boolean alive) {
        return new BirdEggData(birdType, gender, model, skin, eggCount, featherCount, featherInterval, size, hatchTime, alive);
    }

    /** 创建默认的蛋（简化版本） */
    public static BirdEggData createDefault(ResourceLocation birdType, ResourceLocation model,
                                            ResourceLocation skin, float size) {
        return new BirdEggData(birdType, true, model, skin, 1, 1, 24000, size, 6000, true);
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
                gender,
                model,
                skin,
                eggCount,
                featherCount,
                featherInterval,
                size,
                Math.max(0, hatchTime - 1), // 最小为0，防止负数
                alive
        );
    }

    /** 设置孵化时间，返回新实例 */
    public BirdEggData withHatchTime(int newHatchTime) {
        return new BirdEggData(
                birdType,
                gender,
                model,
                skin,
                eggCount,
                featherCount,
                featherInterval,
                size,
                Math.max(0, newHatchTime),
                alive
        );
    }

    /** 设置存活状态，返回新实例 */
    public BirdEggData withAlive(boolean newAlive) {
        return new BirdEggData(
                birdType,
                gender,
                model,
                skin,
                eggCount,
                featherCount,
                featherInterval,
                size,
                hatchTime,
                newAlive
        );
    }

    /** 增加蛋的数量，返回新实例 */
    public BirdEggData withEggCount(int newEggCount) {
        return new BirdEggData(
                birdType,
                gender,
                model,
                skin,
                Math.max(0, newEggCount),
                featherCount,
                featherInterval,
                size,
                hatchTime,
                alive
        );
    }

    /** 设置性别，返回新实例 */
    public BirdEggData withGender(boolean newGender) {
        return new BirdEggData(
                birdType,
                newGender,
                model,
                skin,
                eggCount,
                featherCount,
                featherInterval,
                size,
                hatchTime,
                alive
        );
    }

    /** 设置大小，返回新实例 */
    public BirdEggData withSize(float newSize) {
        return new BirdEggData(
                birdType,
                gender,
                model,
                skin,
                eggCount,
                featherCount,
                featherInterval,
                Math.max(0.1f, newSize),
                hatchTime,
                alive
        );
    }

    /** 设置羽毛数量，返回新实例 */
    public BirdEggData withFeatherCount(int newFeatherCount) {
        return new BirdEggData(
                birdType,
                gender,
                model,
                skin,
                eggCount,
                Math.max(0, newFeatherCount),
                featherInterval,
                size,
                hatchTime,
                alive
        );
    }

    /** 设置羽毛间隔，返回新实例 */
    public BirdEggData withFeatherInterval(int newFeatherInterval) {
        return new BirdEggData(
                birdType,
                gender,
                model,
                skin,
                eggCount,
                featherCount,
                Math.max(0, newFeatherInterval),
                size,
                hatchTime,
                alive
        );
    }

    // ======================== 辅助方法 ========================

    @Override
    public @NotNull String toString() {
        return String.format("BirdEggData{birdType=%s, gender=%s, model=%s, skin=%s, eggCount=%d, featherCount=%d, featherInterval=%d, size=%.2f, hatchTime=%d, alive=%s}",
                birdType, gender ? "male" : "female", model, skin, eggCount, featherCount, featherInterval, size, hatchTime, alive);
    }

    public BirdEggData tickDown(int ticks) {
        return new BirdEggData(
                birdType,
                gender,
                model,
                skin,
                eggCount,
                featherCount,
                featherInterval,
                size,
                Math.max(0, hatchTime - ticks),
                alive
        );
    }
}