package net.fodoth.skina.neoguanniao.client.fan;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.client.particle.FeatherFanParticles;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoParticleTypes;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.SimpleParticleType;

@EventBusSubscriber(modid = NeoGuanNiao.MODID, value = Dist.CLIENT)
public final class FeatherFanParticleProviders {
    @SubscribeEvent
    public static void register(RegisterParticleProvidersEvent e) {
        register(e, NeoGuanNiaoParticleTypes.KILL_FEATHER, FeatherFanParticles.KillFeatherParticle.Provider::new);
        register(e, NeoGuanNiaoParticleTypes.BURIAL_WIND, FeatherFanParticles.BurialWindParticle.Provider::new);
        register(e, NeoGuanNiaoParticleTypes.BURIAL_CYCLONE, FeatherFanParticles.BurialCycloneParticle.Provider::new);
        register(e, NeoGuanNiaoParticleTypes.RIVEN_SPLIT, FeatherFanParticles.RivenSplitParticle.Provider::new);
        register(e, NeoGuanNiaoParticleTypes.RIVEN_STREAK, FeatherFanParticles.RivenStreakParticle.Provider::new);
        register(e, NeoGuanNiaoParticleTypes.HUNTING_MARK, FeatherFanParticles.HuntingMarkParticle.Provider::new);
        register(e, NeoGuanNiaoParticleTypes.HUNTING_STREAK, FeatherFanParticles.HuntingStreakParticle.Provider::new);
    }

    private static void register(RegisterParticleProvidersEvent event,
                                 RegistrySupplier<SimpleParticleType> type,
                                 ParticleEngine.SpriteParticleRegistration<SimpleParticleType> provider) {
        if (type.isPresent()) {
            event.registerSpriteSet(type.get(), provider);
        }
    }

    private FeatherFanParticleProviders() {
    }
}
