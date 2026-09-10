package net.fodoth.skina.neoguanniao.client;

import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoParticleTypes;
import net.minecraft.client.multiplayer.ClientLevel;
import net.fodoth.skina.neoguanniao.client.particle.FeatherFanParticles;

public final class NeoGuanNiaoFabricClientParticles {
    private NeoGuanNiaoFabricClientParticles() {
    }

    public static void register() {
        ParticleFactoryRegistry registry = ParticleFactoryRegistry.getInstance();
        registry.register(NeoGuanNiaoParticleTypes.KILL_FEATHER.get(), FeatherFanParticles.KillFeatherParticle.Provider::new);
        registry.register(NeoGuanNiaoParticleTypes.BURIAL_WIND.get(), FeatherFanParticles.BurialWindParticle.Provider::new);
        registry.register(NeoGuanNiaoParticleTypes.BURIAL_CYCLONE.get(), FeatherFanParticles.BurialCycloneParticle.Provider::new);
        registry.register(NeoGuanNiaoParticleTypes.RIVEN_SPLIT.get(), FeatherFanParticles.RivenSplitParticle.Provider::new);
        registry.register(NeoGuanNiaoParticleTypes.RIVEN_STREAK.get(), FeatherFanParticles.RivenStreakParticle.Provider::new);
        registry.register(NeoGuanNiaoParticleTypes.HUNTING_MARK.get(), FeatherFanParticles.HuntingMarkParticle.Provider::new);
        registry.register(NeoGuanNiaoParticleTypes.HUNTING_STREAK.get(), FeatherFanParticles.HuntingStreakParticle.Provider::new);
    }
}
