package net.fodoth.skina.neoguanniao.registry;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.content.advancement.criterion.*;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.architectury.registry.registries.DeferredRegister;

public class NeoGuanNiaoCriteriaTriggers {

    public static final DeferredRegister<CriterionTrigger<?>> TRIGGERS =
            DeferredRegister.create(NeoGuanNiao.MODID, Registries.TRIGGER_TYPE);


    public static final RegistrySupplier<SpyglassAtBirdTrigger
                > SPYGLASS_AT_BIRD;

    public static final RegistrySupplier<BreedBirdEggTrigger
            > BIRD_EGG_BREED;

    public static final RegistrySupplier<HatchBirdEggTrigger
            > HATCH_BIRD_EGG;

    public static final RegistrySupplier<PickupBirdFeatherTrigger
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


    public static void register() {
        TRIGGERS.register();
    }
}

