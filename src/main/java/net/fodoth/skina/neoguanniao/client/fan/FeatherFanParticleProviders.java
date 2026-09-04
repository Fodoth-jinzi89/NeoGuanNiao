package net.fodoth.skina.neoguanniao.client.fan;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.client.particle.FeatherFanParticles;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoParticleTypes;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = NeoGuanNiao.MODID, value = Dist.CLIENT)
public final class FeatherFanParticleProviders {
    @SubscribeEvent
    public static void register(RegisterParticleProvidersEvent e) {
        e.registerSpriteSet(NeoGuanNiaoParticleTypes.KILL_FEATHER.get(), FeatherFanParticles.KillFeatherParticle.Provider::new);
        e.registerSpriteSet(NeoGuanNiaoParticleTypes.BURIAL_WIND.get(), FeatherFanParticles.BurialWindParticle.Provider::new);
        e.registerSpriteSet(NeoGuanNiaoParticleTypes.BURIAL_CYCLONE.get(), FeatherFanParticles.BurialCycloneParticle.Provider::new);
        e.registerSpriteSet(NeoGuanNiaoParticleTypes.RIVEN_SPLIT.get(), FeatherFanParticles.RivenSplitParticle.Provider::new);
        e.registerSpriteSet(NeoGuanNiaoParticleTypes.RIVEN_STREAK.get(), FeatherFanParticles.RivenStreakParticle.Provider::new);
        e.registerSpriteSet(NeoGuanNiaoParticleTypes.HUNTING_MARK.get(), FeatherFanParticles.HuntingMarkParticle.Provider::new);
        e.registerSpriteSet(NeoGuanNiaoParticleTypes.HUNTING_STREAK.get(), FeatherFanParticles.HuntingStreakParticle.Provider::new);
    }

    private FeatherFanParticleProviders() {
    }
}
