package net.fodoth.skina.neoguanniao.registry;

import com.mojang.serialization.Codec;
import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.client.guide.layout.BirdGuideLayoutData;
import net.fodoth.skina.neoguanniao.content.egg.BirdEggData;
import net.fodoth.skina.neoguanniao.content.feather.BirdFeatherData;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.architectury.registry.registries.DeferredRegister;


public final class NeoGuanNiaoDataComponents {


    public static final ComponentRegister DATA_COMPONENTS = new ComponentRegister();
    public static final class ComponentRegister {
        private final DeferredRegister<DataComponentType<?>> delegate = DeferredRegister.create(NeoGuanNiao.MODID, Registries.DATA_COMPONENT_TYPE);
        public <T> RegistrySupplier<DataComponentType<T>> registerComponentType(String id, java.util.function.UnaryOperator<DataComponentType.Builder<T>> op) { return delegate.register(id, () -> op.apply(DataComponentType.builder()).build()); }
    }

    public static final RegistrySupplier<DataComponentType<BirdGuideLayoutData>> BIRD_GUIDE_LAYOUT =
            DATA_COMPONENTS.registerComponentType(
                    "bird_guide_layout",
                    builder -> builder
                            .persistent(BirdGuideLayoutData.CODEC)
                            .networkSynchronized(BirdGuideLayoutData.STREAM_CODEC)
                            .cacheEncoding()
            );

    public static final RegistrySupplier<DataComponentType<BirdEggData>> BIRD_EGG_DATA =
            DATA_COMPONENTS.registerComponentType(
                    "bird_egg_data",
                    builder -> builder
                            .persistent(BirdEggData.CODEC)
                            .networkSynchronized(BirdEggData.STREAM_CODEC)
            );

    public static final RegistrySupplier<DataComponentType<Integer>>
            BIRD_EGG_RARITY = DATA_COMPONENTS.registerComponentType(
            "bird_egg_rarity",
            builder -> builder
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.INT)
    );

    public static final RegistrySupplier<DataComponentType<Integer>>
            BIRD_EGG_MODEL_RARITY = DATA_COMPONENTS.registerComponentType(
            "bird_egg_model_rarity",
            builder -> builder
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.INT)
    );

    public static final RegistrySupplier<DataComponentType<Integer>>
            BIRD_EGG_GENDER = DATA_COMPONENTS.registerComponentType(
            "bird_egg_gender",
            builder -> builder
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.INT)
    );

    public static final RegistrySupplier<DataComponentType<BirdFeatherData>> BIRD_FEATHER_DATA =
            DATA_COMPONENTS.registerComponentType(
                    "bird_feather_data",
                    builder -> builder
                            .persistent(BirdFeatherData.CODEC)
                            .networkSynchronized(BirdFeatherData.STREAM_CODEC)
            );

    public static final RegistrySupplier<DataComponentType<Integer>>
            BIRD_FEATHER_BIRD_TYPE = DATA_COMPONENTS.registerComponentType(
            "bird_feather_bird_type",
            builder -> builder
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.INT)
    );

    public static final RegistrySupplier<DataComponentType<Integer>>
            BIRD_FEATHER_SKIN_RARITY = DATA_COMPONENTS.registerComponentType(
            "bird_feather_skin_rarity",
            builder -> builder
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.INT)
    );

    public static final RegistrySupplier<DataComponentType<Integer>>
            FEATHER_FAN_MODE = DATA_COMPONENTS.registerComponentType(
            "feather_fan_mode",
            builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT)
    );

}




