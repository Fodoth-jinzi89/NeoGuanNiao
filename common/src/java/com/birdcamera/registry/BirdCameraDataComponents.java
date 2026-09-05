package com.birdcamera.registry;

import com.birdcamera.BirdCameraMod;
import com.birdcamera.content.guide.layout.BirdGuideLayoutData;
import com.mojang.serialization.Codec;
import com.birdcamera.content.egg.BirdEggData;
import com.birdcamera.content.feather.BirdFeatherData;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;

public final class BirdCameraDataComponents {

    public static final DataComponentType<BirdGuideLayoutData> BIRD_GUIDE_LAYOUT = register(
            "bird_guide_layout",
            DataComponentType.<BirdGuideLayoutData>builder()
                    .persistent(BirdGuideLayoutData.CODEC)
                    .networkSynchronized(BirdGuideLayoutData.STREAM_CODEC)
                    .build());

    public static final DataComponentType<BirdEggData> BIRD_EGG_DATA = register(
            "bird_egg_data",
            DataComponentType.<BirdEggData>builder()
                    .persistent(BirdEggData.CODEC)
                    .networkSynchronized(BirdEggData.STREAM_CODEC)
                    .build());

    public static final DataComponentType<Integer> BIRD_EGG_RARITY = register(
            "bird_egg_rarity",
            DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.INT)
                    .build());

    public static final DataComponentType<Integer> BIRD_EGG_MODEL_RARITY = register(
            "bird_egg_model_rarity",
            DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.INT)
                    .build());

    public static final DataComponentType<Integer> BIRD_EGG_GENDER = register(
            "bird_egg_gender",
            DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.INT)
                    .build());

    public static final DataComponentType<BirdFeatherData> BIRD_FEATHER_DATA = register(
            "bird_feather_data",
            DataComponentType.<BirdFeatherData>builder()
                    .persistent(BirdFeatherData.CODEC)
                    .networkSynchronized(BirdFeatherData.STREAM_CODEC)
                    .build());

    public static final DataComponentType<Integer> BIRD_FEATHER_BIRD_TYPE = register(
            "bird_feather_bird_type",
            DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.INT)
                    .build());

    public static final DataComponentType<Integer> BIRD_FEATHER_SKIN_RARITY = register(
            "bird_feather_skin_rarity",
            DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.INT)
                    .build());

    // 相片数据（相机系统，迁移自 guaniao-2.1.3）：照片引用元数据
    public static final net.minecraft.core.component.DataComponentType<net.minecraft.nbt.CompoundTag> PHOTO_DATA = register(
            "photo_data",
            net.minecraft.core.component.DataComponentType.<net.minecraft.nbt.CompoundTag>builder()
                    .persistent(net.minecraft.nbt.CompoundTag.CODEC)
                    .networkSynchronized(ByteBufCodecs.COMPOUND_TAG)
                    .build());

    private static <T> DataComponentType<T> register(String id, DataComponentType<T> type) {
        ResourceLocation key = BirdCameraMod.id(id);
        DataComponentType<T> registered = type;
        // 注册到 DataComponentType 注册表
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, key, registered);
    }

    public static void register() {
        BirdCameraMod.LOGGER.info("注册数据组件...");
    }
}