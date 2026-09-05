package net.fodoth.skina.neoguanniao.registry;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.architectury.registry.registries.DeferredRegister;

public final class NeoGuanNiaoParticleTypes {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(NeoGuanNiao.MODID, Registries.PARTICLE_TYPE);
    public static final RegistrySupplier<SimpleParticleType> KILL_FEATHER = register("kill_feather");
    public static final RegistrySupplier<SimpleParticleType> BURIAL_WIND = register("burial_wind");
    public static final RegistrySupplier<SimpleParticleType> BURIAL_CYCLONE = register("burial_cyclone");
    public static final RegistrySupplier<SimpleParticleType> RIVEN_SPLIT = register("riven_split");
    public static final RegistrySupplier<SimpleParticleType> RIVEN_STREAK = register("riven_streak");
    public static final RegistrySupplier<SimpleParticleType> HUNTING_MARK = register("hunting_mark");
    public static final RegistrySupplier<SimpleParticleType> HUNTING_STREAK = register("hunting_streak");

    private static RegistrySupplier<SimpleParticleType> register(String id) {
        return PARTICLE_TYPES.register(id, () -> new SimpleParticleType(false) {});
    }

    private NeoGuanNiaoParticleTypes() {
    }
}


