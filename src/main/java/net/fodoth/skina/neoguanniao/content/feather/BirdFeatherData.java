package net.fodoth.skina.neoguanniao.content.feather;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

/**
 * 鸟类羽毛数据记录，存储羽毛的所有属性信息
 * @param birdType  鸟类实体注册名
 * @param rarity    稀有度（数值越高越稀有）
 */
public record BirdFeatherData(
        ResourceLocation birdType,
        int rarity
) {

    // ======================== 序列化 ========================

    /** Codec 用于数据存储和网络传输 */
    public static final Codec<BirdFeatherData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("bird_type").forGetter(BirdFeatherData::birdType),
                    Codec.INT.fieldOf("rarity").forGetter(BirdFeatherData::rarity)
            ).apply(instance, BirdFeatherData::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, BirdFeatherData> STREAM_CODEC =
            StreamCodec.of(
                    (buf, data) -> {
                        ResourceLocation.STREAM_CODEC.encode(buf, data.birdType());
                        buf.writeInt(data.rarity());
                    },

                    buf -> new BirdFeatherData(
                            ResourceLocation.STREAM_CODEC.decode(buf),
                            buf.readInt()
                    )
            );

    // ======================== 工厂方法 ========================

    /** 创建一个羽毛数据 */
    public static BirdFeatherData create(ResourceLocation birdType, int rarity) {
        return new BirdFeatherData(birdType, rarity);
    }

    /** 创建默认的羽毛（稀有度为1） */
    public static BirdFeatherData createDefault(ResourceLocation birdType) {
        return new BirdFeatherData(birdType, 0);
    }

    // ======================== 状态更新 ========================

    /** 设置稀有度，返回新实例（不可变对象） */
    public BirdFeatherData withRarity(int newRarity) {
        return new BirdFeatherData(
                birdType,
                Math.max(0, newRarity)
        );
    }

    /** 增加稀有度，返回新实例 */
    public BirdFeatherData addRarity(int amount) {
        return new BirdFeatherData(
                birdType,
                Math.max(0, rarity + amount)
        );
    }

    /** 设置鸟类类型，返回新实例 */
    public BirdFeatherData withBirdType(ResourceLocation newBirdType) {
        return new BirdFeatherData(
                newBirdType,
                rarity
        );
    }

    public int toTypeInt() {
        if (!birdType().getNamespace().equals(NeoGuanNiao.MODID)) {
            return 0;
        }

        return switch (birdType().getPath()) {
            case "neo_budgerigar" -> 0;
            case "neo_night_heron" -> 1;
            case "neo_spotted_dove" -> 2;
            case "neo_pigeon" -> 3;
            case "neo_sparrow" -> 4;
            default -> -1;
        };
    }

    // ======================== 辅助方法 ========================

}