package net.fodoth.skina.neoguanniao.client;

import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoParticleTypes;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.NotNull;

public final class NeoGuanNiaoFabricClientParticles {
    private NeoGuanNiaoFabricClientParticles() {}

    public static void register() {
        ParticleFactoryRegistry registry = ParticleFactoryRegistry.getInstance();
        registry.register(NeoGuanNiaoParticleTypes.KILL_FEATHER.get(), provider(0.92F, 0.92F, 0.82F, 18));
        registry.register(NeoGuanNiaoParticleTypes.BURIAL_WIND.get(), provider(0.72F, 0.84F, 1.0F, 14));
        registry.register(NeoGuanNiaoParticleTypes.BURIAL_CYCLONE.get(), provider(0.45F, 0.68F, 1.0F, 16));
        registry.register(NeoGuanNiaoParticleTypes.RIVEN_SPLIT.get(), provider(1.0F, 0.42F, 0.32F, 15));
        registry.register(NeoGuanNiaoParticleTypes.RIVEN_STREAK.get(), provider(1.0F, 0.68F, 0.28F, 11));
        registry.register(NeoGuanNiaoParticleTypes.HUNTING_MARK.get(), provider(1.0F, 0.22F, 0.18F, 10));
        registry.register(NeoGuanNiaoParticleTypes.HUNTING_STREAK.get(), provider(1.0F, 0.38F, 0.22F, 9));
    }

    private static ParticleProvider<SimpleParticleType> provider(float red, float green, float blue, int lifetime) {
        return (type, level, x, y, z, xd, yd, zd) ->
                new BasicParticle(level, x, y, z, xd, yd, zd, red, green, blue, lifetime);
    }

    private static final class BasicParticle extends TextureSheetParticle {
        private final float baseSize;
        private final float red;
        private final float green;
        private final float blue;

        private BasicParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd,
                              float red, float green, float blue, int lifetime) {
            super(level, x, y, z, xd, yd, zd);
            this.lifetime = lifetime + level.random.nextInt(4);
            this.baseSize = 0.18F + level.random.nextFloat() * 0.12F;
            this.quadSize = this.baseSize;
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.setColor(red, green, blue);
            this.alpha = 0.0F;
            this.gravity = 0.0F;
            this.hasPhysics = false;
        }

        @Override
        public void tick() {
            super.tick();
            if (this.removed) return;
            float progress = (float) this.age / this.lifetime;
            float fadeIn = Math.min(1.0F, progress * 8.0F);
            float fadeOut = 1.0F - Math.max(0.0F, (progress - 0.58F) / 0.42F);
            this.alpha = 0.78F * fadeIn * fadeOut;
            this.quadSize = this.baseSize * (0.75F + (float) Math.sin(progress * Math.PI) * 0.45F);
            this.roll += 0.08F;
        }

        @Override
        public int getLightColor(float partialTick) {
            return 0xF000F0;
        }

        @Override
        public @NotNull ParticleRenderType getRenderType() {
            return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
        }
    }
}
