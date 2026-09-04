package net.fodoth.skina.neoguanniao.registry;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NeoGuanNiaoSoundEvents {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, NeoGuanNiao.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> FEATHER_FAN_BURIAL_VORTEX = register("item.feather_fan.burial_vortex");
    public static final DeferredHolder<SoundEvent, SoundEvent> FEATHER_FAN_BURIAL_SLASH = register("item.feather_fan.burial_slash");
    public static final DeferredHolder<SoundEvent, SoundEvent> FEATHER_FAN_RIVEN_PIN = register("item.feather_fan.riven_pin");
    public static final DeferredHolder<SoundEvent, SoundEvent> FEATHER_FAN_RIVEN_SPLIT = register("item.feather_fan.riven_split");
    public static final DeferredHolder<SoundEvent, SoundEvent> FEATHER_FAN_RIVEN_LOCK = register("item.feather_fan.riven_lock");
    public static final DeferredHolder<SoundEvent, SoundEvent> FEATHER_FAN_RIVEN_BURST = register("item.feather_fan.riven_burst");
    public static final DeferredHolder<SoundEvent, SoundEvent> FEATHER_FAN_HUNT_LOCK = register("item.feather_fan.hunt_lock");
    public static final DeferredHolder<SoundEvent, SoundEvent> FEATHER_FAN_HUNT_START = register("item.feather_fan.hunt_start");
    public static final DeferredHolder<SoundEvent, SoundEvent> FEATHER_FAN_HUNT_TURN = register("item.feather_fan.hunt_turn");
    public static final DeferredHolder<SoundEvent, SoundEvent> FEATHER_FAN_HUNT_HIT = register("item.feather_fan.hunt_hit");

    public static final DeferredHolder<SoundEvent, SoundEvent> NIGHT_HERON_AMBIENT =
            register("entity.night_heron.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> NIGHT_HERON_HURT =
            register("entity.night_heron.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> NIGHT_HERON_DEATH =
            register("entity.night_heron.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> NIGHT_HERON_ATTACK =
            register("entity.night_heron.attack");

    public static final DeferredHolder<SoundEvent, SoundEvent> SPARROW_AMBIENT =
            register("entity.sparrow.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> SPARROW_HURT =
            register("entity.sparrow.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> SPARROW_DEATH =
            register("entity.sparrow.death");

    public static final DeferredHolder<SoundEvent, SoundEvent> BUDGERIGAR_AMBIENT =
            register("entity.budgerigar.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> BUDGERIGAR_HURT =
            register("entity.budgerigar.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> BUDGERIGAR_DEATH =
            register("entity.budgerigar.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> BUDGERIGAR_INTERACT =
            register("entity.budgerigar.interact");

    public static final DeferredHolder<SoundEvent, SoundEvent> SPOTTED_DOVE_AMBIENT =
            register("entity.spotted_dove.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> SPOTTED_DOVE_HURT =
            register("entity.spotted_dove.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> SPOTTED_DOVE_DEATH =
            register("entity.spotted_dove.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> SPOTTED_DOVE_MATE =
            register("entity.spotted_dove.mate");

    public static final DeferredHolder<SoundEvent, SoundEvent> PIGEON_AMBIENT =
            register("entity.pigeon.ambient");

    public static final DeferredHolder<SoundEvent, SoundEvent> KIWI_AMBIENT =
            register("entity.kiwi.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> KIWI_HURT =
            register("entity.kiwi.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> KIWI_DEATH =
            register("entity.kiwi.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> MYNA_AMBIENT =
            register("entity.myna.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> MYNA_HURT =
            register("entity.myna.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> MYNA_DEATH =
            register("entity.myna.death");


    private NeoGuanNiaoSoundEvents() {
    }


    private static DeferredHolder<SoundEvent, SoundEvent> register(String id) {
        assert SOUND_EVENTS != null;
        return SOUND_EVENTS.register(
                id,
                () -> SoundEvent.createVariableRangeEvent(
                        ResourceLocation.fromNamespaceAndPath(NeoGuanNiao.MODID, id)
                )
        );
    }
}
