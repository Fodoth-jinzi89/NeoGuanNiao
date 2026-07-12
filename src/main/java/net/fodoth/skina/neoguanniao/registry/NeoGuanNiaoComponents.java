package net.fodoth.skina.neoguanniao.registry;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.client.guide.layout.BirdGuideLayoutData;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class NeoGuanNiaoComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, NeoGuanNiao.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BirdGuideLayoutData>> BIRD_GUIDE_LAYOUT =
            DATA_COMPONENTS.registerComponentType(
                    "bird_guide_layout",
                    builder -> builder
                            .persistent(BirdGuideLayoutData.CODEC)
                            .networkSynchronized(BirdGuideLayoutData.STREAM_CODEC)
                            .cacheEncoding()
            );
}