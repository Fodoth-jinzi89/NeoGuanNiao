package net.fodoth.skina.neoguanniao.registry;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.content.egg.BirdEggData;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;


public final class NeoGuanNiaoDataComponents {


    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(
                    Registries.DATA_COMPONENT_TYPE,
                    NeoGuanNiao.MODID
            );


    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BirdEggData>> BIRD_EGG_DATA =
            DATA_COMPONENTS.registerComponentType(
                    "bird_egg_data",
                    builder -> builder
                            .persistent(BirdEggData.CODEC)
            );

}