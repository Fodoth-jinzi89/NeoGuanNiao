package net.fodoth.skina.neoguanniao.registry;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.architectury.registry.registries.Registrar;

public final class NeoGuanNiaoSoundEvents {
    public static final Registrar<SoundEvent> SOUND_EVENTS = NeoGuanNiaoRegistrar.MANAGER.get(Registries.SOUND_EVENT);

    public static final RegistrySupplier<SoundEvent> FEATHER_FAN_BURIAL_VORTEX = register("item.feather_fan.burial_vortex");
    public static final RegistrySupplier<SoundEvent> FEATHER_FAN_BURIAL_SLASH = register("item.feather_fan.burial_slash");
    public static final RegistrySupplier<SoundEvent> FEATHER_FAN_RIVEN_PIN = register("item.feather_fan.riven_pin");
    public static final RegistrySupplier<SoundEvent> FEATHER_FAN_RIVEN_SPLIT = register("item.feather_fan.riven_split");
    public static final RegistrySupplier<SoundEvent> FEATHER_FAN_RIVEN_LOCK = register("item.feather_fan.riven_lock");
    public static final RegistrySupplier<SoundEvent> FEATHER_FAN_RIVEN_BURST = register("item.feather_fan.riven_burst");
    public static final RegistrySupplier<SoundEvent> FEATHER_FAN_HUNT_LOCK = register("item.feather_fan.hunt_lock");
    public static final RegistrySupplier<SoundEvent> FEATHER_FAN_HUNT_START = register("item.feather_fan.hunt_start");
    public static final RegistrySupplier<SoundEvent> FEATHER_FAN_HUNT_TURN = register("item.feather_fan.hunt_turn");
    public static final RegistrySupplier<SoundEvent> FEATHER_FAN_HUNT_HIT = register("item.feather_fan.hunt_hit");

    public static final RegistrySupplier<SoundEvent> NIGHT_HERON_AMBIENT =
            register("entity.night_heron.ambient");
    public static final RegistrySupplier<SoundEvent> NIGHT_HERON_HURT =
            register("entity.night_heron.hurt");
    public static final RegistrySupplier<SoundEvent> NIGHT_HERON_DEATH =
            register("entity.night_heron.death");
    public static final RegistrySupplier<SoundEvent> NIGHT_HERON_ATTACK =
            register("entity.night_heron.attack");

    public static final RegistrySupplier<SoundEvent> SPARROW_AMBIENT =
            register("entity.sparrow.ambient");
    public static final RegistrySupplier<SoundEvent> SPARROW_HURT =
            register("entity.sparrow.hurt");
    public static final RegistrySupplier<SoundEvent> SPARROW_DEATH =
            register("entity.sparrow.death");

    public static final RegistrySupplier<SoundEvent> BUDGERIGAR_AMBIENT =
            register("entity.budgerigar.ambient");
    public static final RegistrySupplier<SoundEvent> BUDGERIGAR_HURT =
            register("entity.budgerigar.hurt");
    public static final RegistrySupplier<SoundEvent> BUDGERIGAR_DEATH =
            register("entity.budgerigar.death");
    public static final RegistrySupplier<SoundEvent> BUDGERIGAR_INTERACT =
            register("entity.budgerigar.interact");

    public static final RegistrySupplier<SoundEvent> SPOTTED_DOVE_AMBIENT =
            register("entity.spotted_dove.ambient");
    public static final RegistrySupplier<SoundEvent> SPOTTED_DOVE_HURT =
            register("entity.spotted_dove.hurt");
    public static final RegistrySupplier<SoundEvent> SPOTTED_DOVE_DEATH =
            register("entity.spotted_dove.death");
    public static final RegistrySupplier<SoundEvent> SPOTTED_DOVE_MATE =
            register("entity.spotted_dove.mate");

    public static final RegistrySupplier<SoundEvent> PIGEON_AMBIENT =
            register("entity.pigeon.ambient");

    public static final RegistrySupplier<SoundEvent> KIWI_AMBIENT =
            register("entity.kiwi.ambient");
    public static final RegistrySupplier<SoundEvent> KIWI_HURT =
            register("entity.kiwi.hurt");
    public static final RegistrySupplier<SoundEvent> KIWI_DEATH =
            register("entity.kiwi.death");
    public static final RegistrySupplier<SoundEvent> MYNA_AMBIENT =
            register("entity.myna.ambient");
    public static final RegistrySupplier<SoundEvent> MYNA_HURT =
            register("entity.myna.hurt");
    public static final RegistrySupplier<SoundEvent> MYNA_DEATH =
            register("entity.myna.death");


    private NeoGuanNiaoSoundEvents() {
    }


    private static RegistrySupplier<SoundEvent> register(String id) {
        return SOUND_EVENTS.register(
                id,
                () -> SoundEvent.createVariableRangeEvent(
                        ResourceLocation.fromNamespaceAndPath(NeoGuanNiao.MODID, id)
                )
        );
    }
}

