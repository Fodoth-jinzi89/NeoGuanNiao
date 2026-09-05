package net.fodoth.skina.neoguanniao.client.guide.layout;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.HashMap;
import java.util.Map;

public record BirdGuideLayoutData(
        int version,
        int baseWidth,
        int baseHeight,
        String screen,
        Map<String, BirdGuideLayoutRect> rects
) {
    public static final int CURRENT_VERSION = 1;

    // 自定义 GuiLayoutRect 的 Codec
    private static final Codec<BirdGuideLayoutRect> RECT_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("x").forGetter(BirdGuideLayoutRect::x),
                    Codec.INT.fieldOf("y").forGetter(BirdGuideLayoutRect::y),
                    Codec.INT.fieldOf("w").forGetter(BirdGuideLayoutRect::w),
                    Codec.INT.fieldOf("h").forGetter(BirdGuideLayoutRect::h)
            ).apply(instance, BirdGuideLayoutRect::new)
    );

    // 自定义 GuiLayoutRect 的 StreamCodec
    private static final StreamCodec<RegistryFriendlyByteBuf, BirdGuideLayoutRect> RECT_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, BirdGuideLayoutRect::x,
            ByteBufCodecs.INT, BirdGuideLayoutRect::y,
            ByteBufCodecs.INT, BirdGuideLayoutRect::w,
            ByteBufCodecs.INT, BirdGuideLayoutRect::h,
            BirdGuideLayoutRect::new
    );

    public static final Codec<BirdGuideLayoutData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("version").forGetter(BirdGuideLayoutData::version),
                    Codec.INT.fieldOf("baseWidth").forGetter(BirdGuideLayoutData::baseWidth),
                    Codec.INT.fieldOf("baseHeight").forGetter(BirdGuideLayoutData::baseHeight),
                    Codec.STRING.optionalFieldOf("screen", "").forGetter(BirdGuideLayoutData::screen),
                    Codec.unboundedMap(Codec.STRING, RECT_CODEC).fieldOf("rects").forGetter(BirdGuideLayoutData::rects)
            ).apply(instance, BirdGuideLayoutData::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, BirdGuideLayoutData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, BirdGuideLayoutData::version,
            ByteBufCodecs.INT, BirdGuideLayoutData::baseWidth,
            ByteBufCodecs.INT, BirdGuideLayoutData::baseHeight,
            ByteBufCodecs.STRING_UTF8, BirdGuideLayoutData::screen,
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, RECT_STREAM_CODEC), BirdGuideLayoutData::rects,
            BirdGuideLayoutData::new
    );

    @SuppressWarnings("all")
    public boolean isValid() {
        return baseWidth > 0 && baseHeight > 0 && rects != null && !rects.isEmpty();
    }
}