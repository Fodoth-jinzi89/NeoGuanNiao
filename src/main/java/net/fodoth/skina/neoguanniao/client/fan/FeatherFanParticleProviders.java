package net.fodoth.skina.neoguanniao.client.fan;

import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoParticleTypes;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.client.particle.FlameParticle;

@EventBusSubscriber(modid = "neoguanniao", value = Dist.CLIENT)
public final class FeatherFanParticleProviders {
    @SubscribeEvent
    public static void register(RegisterParticleProvidersEvent e) {
        e.registerSpriteSet(NeoGuanNiaoParticleTypes.KILL_FEATHER.get(), FlameParticle.Provider::new);
        e.registerSpriteSet(NeoGuanNiaoParticleTypes.BURIAL_WIND.get(), FlameParticle.Provider::new);
        e.registerSpriteSet(NeoGuanNiaoParticleTypes.BURIAL_CYCLONE.get(), FlameParticle.Provider::new);
        e.registerSpriteSet(NeoGuanNiaoParticleTypes.RIVEN_SPLIT.get(), FlameParticle.Provider::new);
        e.registerSpriteSet(NeoGuanNiaoParticleTypes.RIVEN_STREAK.get(), FlameParticle.Provider::new);
        e.registerSpriteSet(NeoGuanNiaoParticleTypes.HUNTING_MARK.get(), FlameParticle.Provider::new);
        e.registerSpriteSet(NeoGuanNiaoParticleTypes.HUNTING_STREAK.get(), FlameParticle.Provider::new);
    }

    private FeatherFanParticleProviders() {
    }
}
