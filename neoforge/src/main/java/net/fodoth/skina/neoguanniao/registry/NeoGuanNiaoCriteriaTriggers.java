package net.fodoth.skina.neoguanniao.registry;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.content.advancement.criterion.*;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class NeoGuanNiaoCriteriaTriggers {

    public static final DeferredRegister<CriterionTrigger<?>> TRIGGERS =
            DeferredRegister.create(
                    Registries.TRIGGER_TYPE,
                    NeoGuanNiao.MODID
            );


    public static final DeferredHolder<
                CriterionTrigger<?>,
                SpyglassAtBirdTrigger
                > SPYGLASS_AT_BIRD;

    public static final DeferredHolder<
            CriterionTrigger<?>,
            BreedBirdEggTrigger
            > BIRD_EGG_BREED;

    public static final DeferredHolder<
            CriterionTrigger<?>,
            HatchBirdEggTrigger
            > HATCH_BIRD_EGG;

    public static final DeferredHolder<
            CriterionTrigger<?>,
            PickupBirdFeatherTrigger
            > PICKUP_BIRD_FEATHER;

    static {
        SPYGLASS_AT_BIRD =
                TRIGGERS.register(
                        "spyglass_at_bird",
                        SpyglassAtBirdTrigger::new
                );
        BIRD_EGG_BREED =
                TRIGGERS.register(
                        "bird_egg_breed",
                        BreedBirdEggTrigger::new
                );

        HATCH_BIRD_EGG =
                TRIGGERS.register(
                        "hatch_bird_egg",
                        HatchBirdEggTrigger::new
                );

        PICKUP_BIRD_FEATHER =
                TRIGGERS.register(
                        "pickup_bird_feather",
                        PickupBirdFeatherTrigger::new
                );
    }


    public static void register(IEventBus bus) {
        TRIGGERS.register(bus);
    }
}
