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


    private NeoGuanNiaoSoundEvents() {
    }


    private static DeferredHolder<SoundEvent, SoundEvent> register(String id) {
        return SOUND_EVENTS.register(
                id,
                () -> SoundEvent.createVariableRangeEvent(
                        ResourceLocation.fromNamespaceAndPath(NeoGuanNiao.MODID, id)
                )
        );
    }
}