package net.fodoth.skina.neoguanniao.registry;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NeoGuanNiaoParticleTypes {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(Registries.PARTICLE_TYPE, NeoGuanNiao.MODID);
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> KILL_FEATHER = register("kill_feather");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> BURIAL_WIND = register("burial_wind");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> BURIAL_CYCLONE = register("burial_cyclone");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RIVEN_SPLIT = register("riven_split");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RIVEN_STREAK = register("riven_streak");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> HUNTING_MARK = register("hunting_mark");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> HUNTING_STREAK = register("hunting_streak");

    private static DeferredHolder<ParticleType<?>, SimpleParticleType> register(String id) {
        return PARTICLE_TYPES.register(id, () -> new SimpleParticleType(false));
    }

    private NeoGuanNiaoParticleTypes() {
    }
}
