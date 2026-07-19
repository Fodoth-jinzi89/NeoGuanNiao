package net.fodoth.skina.neoguanniao.content.egg;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

/**
 * 鸟类蛋数据记录，存储蛋的所有属性信息
 * @param birdType  鸟类实体注册名
 * @param model     模型资源路径
 * @param skin      皮肤资源路径
 * @param size      体型大小
 * @param hatchTime 剩余孵化时间（刻）
 * @param alive     是否存活
 */
public record BirdEggData(
        ResourceLocation birdType,
        ResourceLocation model,
        ResourceLocation skin,
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
                    Codec.FLOAT.fieldOf("size").forGetter(BirdEggData::size),
                    Codec.INT.fieldOf("hatch_time").forGetter(BirdEggData::hatchTime),
                    Codec.BOOL.fieldOf("alive").forGetter(BirdEggData::alive)
            ).apply(instance, BirdEggData::new)
    );

    // ======================== 工厂方法 ========================

    /** 创建一个蛋 */
    public static BirdEggData create(ResourceLocation birdType, ResourceLocation model,
                                     ResourceLocation skin, float size, int hatchTime, boolean alive) {
        return new BirdEggData(birdType, model, skin, size, hatchTime, alive);
    }

    // ======================== 状态查询 ========================

    /** 判断是否已达到孵化条件（孵化倒计时归零） */
    public boolean canHatch() {
        return hatchTime <= 0;
    }

    // ======================== 状态更新 ========================

    /** 减少一刻孵化时间，返回新实例（不可变对象） */
    public BirdEggData tickDown() {
        return new BirdEggData(
                birdType,
                model,
                skin,
                size,
                Math.max(0, hatchTime - 1), // 最小为0，防止负数
                alive
        );
    }
}