package net.fodoth.skina.neoguanniao.registry;

import com.mojang.serialization.Codec;
import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.client.guide.layout.BirdGuideLayoutData;
import net.fodoth.skina.neoguanniao.content.egg.BirdEggData;
import net.fodoth.skina.neoguanniao.content.feather.BirdFeatherData;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;


public final class NeoGuanNiaoDataComponents {


    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(
                    Registries.DATA_COMPONENT_TYPE,
                    NeoGuanNiao.MODID
            );


    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BirdGuideLayoutData>> BIRD_GUIDE_LAYOUT =
            DATA_COMPONENTS.registerComponentType(
                    "bird_guide_layout",
                    builder -> builder
                            .persistent(BirdGuideLayoutData.CODEC)
                            .networkSynchronized(BirdGuideLayoutData.STREAM_CODEC)
                            .cacheEncoding()
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BirdEggData>> BIRD_EGG_DATA =
            DATA_COMPONENTS.registerComponentType(
                    "bird_egg_data",
                    builder -> builder
                            .persistent(BirdEggData.CODEC)
                            .networkSynchronized(BirdEggData.STREAM_CODEC)
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>>
            BIRD_EGG_RARITY = DATA_COMPONENTS.registerComponentType(
            "bird_egg_rarity",
            builder -> builder
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.INT)
    );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>>
            BIRD_EGG_MODEL_RARITY = DATA_COMPONENTS.registerComponentType(
            "bird_egg_model_rarity",
            builder -> builder
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.INT)
    );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>>
            BIRD_EGG_GENDER = DATA_COMPONENTS.registerComponentType(
            "bird_egg_gender",
            builder -> builder
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.INT)
    );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BirdFeatherData>> BIRD_FEATHER_DATA =
            DATA_COMPONENTS.registerComponentType(
                    "bird_feather_data",
                    builder -> builder
                            .persistent(BirdFeatherData.CODEC)
                            .networkSynchronized(BirdFeatherData.STREAM_CODEC)
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>>
            BIRD_FEATHER_BIRD_TYPE = DATA_COMPONENTS.registerComponentType(
            "bird_feather_bird_type",
            builder -> builder
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.INT)
    );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>>
            BIRD_FEATHER_SKIN_RARITY = DATA_COMPONENTS.registerComponentType(
            "bird_feather_skin_rarity",
            builder -> builder
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.INT)
    );

}