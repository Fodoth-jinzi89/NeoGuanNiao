package com.birdcamera.registry;

import com.birdcamera.BirdCameraMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;

/**
 * 音效注册 - 注册 ID 必须与 assets/birdcamera/sounds.json 的 key 完全一致
 */
public class BirdCameraSoundEvents {

    // ===== 夜鹭音效 =====
    public static final SoundEvent NIGHT_HERON_AMBIENT = register("entity.night_heron.ambient");
    public static final SoundEvent NIGHT_HERON_HURT = register("entity.night_heron.hurt");
    public static final SoundEvent NIGHT_HERON_DEATH = register("entity.night_heron.death");
    public static final SoundEvent NIGHT_HERON_ATTACK = register("entity.night_heron.attack");

    // ===== 麻雀音效 =====
    public static final SoundEvent SPARROW_AMBIENT = register("entity.sparrow.ambient");
    public static final SoundEvent SPARROW_HURT = register("entity.sparrow.hurt");
    public static final SoundEvent SPARROW_DEATH = register("entity.sparrow.death");

    // ===== 虎皮鹦鹉音效 =====
    public static final SoundEvent BUDGERIGAR_AMBIENT = register("entity.budgerigar.ambient");
    public static final SoundEvent BUDGERIGAR_HURT = register("entity.budgerigar.hurt");
    public static final SoundEvent BUDGERIGAR_DEATH = register("entity.budgerigar.death");
    public static final SoundEvent BUDGERIGAR_INTERACT = register("entity.budgerigar.interact");

    // ===== 斑背鸽音效 =====
    public static final SoundEvent SPOTTED_DOVE_AMBIENT = register("entity.spotted_dove.ambient");
    public static final SoundEvent SPOTTED_DOVE_HURT = register("entity.spotted_dove.hurt");
    public static final SoundEvent SPOTTED_DOVE_DEATH = register("entity.spotted_dove.death");
    public static final SoundEvent SPOTTED_DOVE_MATE = register("entity.spotted_dove.mate");

    // ===== 鸽子音效 =====
    public static final SoundEvent PIGEON_AMBIENT = register("entity.pigeon.ambient");

    private static SoundEvent register(String id) {
        ResourceLocation identifier = BirdCameraMod.id(id);
        SoundEvent soundEvent = SoundEvent.createVariableRangeEvent(identifier);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, identifier, soundEvent);
    }

    public static void register() {
        BirdCameraMod.LOGGER.info("注册音效事件...");
    }
}