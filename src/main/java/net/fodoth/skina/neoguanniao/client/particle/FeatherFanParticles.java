package net.fodoth.skina.neoguanniao.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

/**
 * Ported custom particle visuals from guaniao-3.1.4 (official 1.20.1 mappings),
 * adapted to NeoForge 1.21.1.
 */
public final class FeatherFanParticles {
    private FeatherFanParticles() {
    }

    public static final class BurialCycloneParticle extends TextureSheetParticle {
        private final float baseQuadSize;
        private final float baseAlpha;
        private final float spinSpeed;

        private BurialCycloneParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
            super(level, x, y, z, xSpeed, ySpeed, zSpeed);
            this.pickSprite(sprites);
            this.hasPhysics = false;
            this.gravity = 0.0f;
            this.friction = 0.91f;
            this.xd = xSpeed;
            this.yd = ySpeed;
            this.zd = zSpeed;
            this.lifetime = 13 + level.random.nextInt(7);
            this.baseQuadSize = 0.56f + level.random.nextFloat() * 0.28f;
            this.quadSize = this.baseQuadSize * 0.72f;
            this.baseAlpha = 0.42f + level.random.nextFloat() * 0.18f;
            this.alpha = 0.0f;
            this.spinSpeed = (level.random.nextBoolean() ? 1.0f : -1.0f) * (0.025f + level.random.nextFloat() * 0.035f);
            this.oRoll = this.roll = level.random.nextFloat() * (float)Math.PI * 2;
        }

        @Override
        public void tick() {
            this.oRoll = this.roll;
            super.tick();
            if (this.removed) {
                return;
            }
            this.roll += this.spinSpeed;
            float progress = Mth.clamp((float)this.age / (float)this.lifetime, 0.0f, 1.0f);
            float fadeIn = Mth.clamp(progress * 6.0f, 0.0f, 1.0f);
            float fadeOut = 1.0f - Mth.clamp((progress - 0.48f) / 0.52f, 0.0f, 1.0f);
            this.alpha = this.baseAlpha * fadeIn * fadeOut;
            this.quadSize = this.baseQuadSize * (0.78f + Mth.sin(progress * (float)Math.PI) * 0.3f);
        }

        @Override
        public ParticleRenderType getRenderType() {
            return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
        }

        public static final class Provider implements ParticleProvider<SimpleParticleType> {
            private final SpriteSet sprites;

            public Provider(SpriteSet sprites) {
                this.sprites = sprites;
            }

            @Nullable
            @Override
            public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
                return new BurialCycloneParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
            }
        }
    }

    public static final class BurialWindParticle extends TextureSheetParticle {
        private final float baseQuadSize;
        private final float baseAlpha;
        private final float spinSpeed;

        private BurialWindParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
            super(level, x, y, z, xSpeed, ySpeed, zSpeed);
            this.pickSprite(sprites);
            this.hasPhysics = false;
            this.gravity = 0.0f;
            this.friction = 0.88f;
            this.lifetime = 10 + level.random.nextInt(8);
            this.quadSize = this.baseQuadSize = 0.52f + level.random.nextFloat() * 0.42f;
            this.baseAlpha = 0.68f + level.random.nextFloat() * 0.24f;
            this.alpha = 0.0f;
            this.spinSpeed = (level.random.nextBoolean() ? 1.0f : -1.0f) * (0.18f + level.random.nextFloat() * 0.16f);
            this.oRoll = this.roll = level.random.nextFloat() * (float)Math.PI * 2;
        }

        @Override
        public void tick() {
            this.oRoll = this.roll;
            super.tick();
            if (this.removed) {
                return;
            }
            this.roll += this.spinSpeed;
            float progress = Mth.clamp((float)this.age / (float)this.lifetime, 0.0f, 1.0f);
            float fadeIn = Mth.clamp(progress * 5.0f, 0.0f, 1.0f);
            float fadeOut = 1.0f - Mth.clamp((progress - 0.52f) / 0.48f, 0.0f, 1.0f);
            this.alpha = this.baseAlpha * fadeIn * fadeOut;
            this.quadSize = this.baseQuadSize * (0.72f + Mth.sin(progress * (float)Math.PI) * 0.48f);
        }

        @Override
        public ParticleRenderType getRenderType() {
            return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
        }

        public static final class Provider implements ParticleProvider<SimpleParticleType> {
            private final SpriteSet sprites;

            public Provider(SpriteSet sprites) {
                this.sprites = sprites;
            }

            @Nullable
            @Override
            public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
                return new BurialWindParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
            }
        }
    }

    public static final class HuntingMarkParticle extends TextureSheetParticle {
        private final float baseAlpha;
        private final float baseQuadSize;

        private HuntingMarkParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
            super(level, x, y, z, xSpeed, ySpeed, zSpeed);
            this.pickSprite(sprites);
            this.hasPhysics = false;
            this.gravity = 0.0f;
            this.friction = 0.82f;
            this.xd = xSpeed;
            this.yd = ySpeed;
            this.zd = zSpeed;
            this.lifetime = 8 + level.random.nextInt(4);
            this.baseQuadSize = 0.82f + level.random.nextFloat() * 0.16f;
            this.quadSize = this.baseQuadSize * 0.82f;
            this.baseAlpha = 0.7f + level.random.nextFloat() * 0.18f;
            this.alpha = 0.0f;
            this.oRoll = this.roll = level.random.nextFloat() * (float)Math.PI * 2;
        }

        @Override
        public void tick() {
            this.oRoll = this.roll;
            super.tick();
            if (this.removed) {
                return;
            }
            this.roll += 0.055f;
            float progress = Mth.clamp((float)this.age / (float)this.lifetime, 0.0f, 1.0f);
            float fadeIn = Mth.clamp(progress * 6.0f, 0.0f, 1.0f);
            float fadeOut = 1.0f - Mth.clamp((progress - 0.48f) / 0.52f, 0.0f, 1.0f);
            this.alpha = this.baseAlpha * fadeIn * fadeOut;
            this.quadSize = this.baseQuadSize * (0.82f + Mth.sin(progress * (float)Math.PI) * 0.24f);
        }

        @Override
        public ParticleRenderType getRenderType() {
            return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
        }

        public static final class Provider implements ParticleProvider<SimpleParticleType> {
            private final SpriteSet sprites;

            public Provider(SpriteSet sprites) {
                this.sprites = sprites;
            }

            @Nullable
            @Override
            public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
                return new HuntingMarkParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
            }
        }
    }

    public static final class HuntingStreakParticle extends TextureSheetParticle {
        private final float baseAlpha;
        private final float baseQuadSize;
        private final float spinSpeed;

        private HuntingStreakParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
            super(level, x, y, z, xSpeed, ySpeed, zSpeed);
            this.pickSprite(sprites);
            this.hasPhysics = false;
            this.gravity = 0.0f;
            this.friction = 0.88f;
            this.xd = xSpeed;
            this.yd = ySpeed;
            this.zd = zSpeed;
            this.lifetime = 8 + level.random.nextInt(5);
            this.quadSize = this.baseQuadSize = 0.46f + level.random.nextFloat() * 0.18f;
            this.baseAlpha = 0.65f + level.random.nextFloat() * 0.22f;
            this.alpha = 0.0f;
            this.spinSpeed = (level.random.nextBoolean() ? 1.0f : -1.0f) * (0.018f + level.random.nextFloat() * 0.022f);
            this.oRoll = this.roll = (float)level.random.nextInt(8) * 0.7853982f;
        }

        @Override
        public void tick() {
            this.oRoll = this.roll;
            super.tick();
            if (this.removed) {
                return;
            }
            this.roll += this.spinSpeed;
            float progress = Mth.clamp((float)this.age / (float)this.lifetime, 0.0f, 1.0f);
            float fadeIn = Mth.clamp(progress * 8.0f, 0.0f, 1.0f);
            float fadeOut = 1.0f - Mth.clamp((progress - 0.38f) / 0.62f, 0.0f, 1.0f);
            this.alpha = this.baseAlpha * fadeIn * fadeOut;
            this.quadSize = this.baseQuadSize * (1.0f - progress * 0.28f);
        }

        @Override
        public ParticleRenderType getRenderType() {
            return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
        }

        public static final class Provider implements ParticleProvider<SimpleParticleType> {
            private final SpriteSet sprites;

            public Provider(SpriteSet sprites) {
                this.sprites = sprites;
            }

            @Nullable
            @Override
            public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
                return new HuntingStreakParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
            }
        }
    }

    public static final class KillFeatherParticle extends TextureSheetParticle {
        private final double driftX;
        private final double driftZ;
        private final double initialYSpeed;
        private final float swayPhase;
        private final float swaySpeed;
        private final float swayAmount;
        private final float spinDirection;
        private final float fallGravity;
        private final float terminalFallSpeed;
        private final float baseAlpha;
        private final float baseQuadSize;
        private final int startDelay;
        private final int fadeTicks;
        private final int groundHoldTicks;
        private final boolean fadesAfterLanding;
        private int fadeStartAge;
        private boolean started;
        private boolean landed;

        private KillFeatherParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
            super(level, x, y, z);
            this.pickSprite(sprites);
            this.hasPhysics = true;
            this.startDelay = level.random.nextInt(11);
            this.fadeTicks = 10 + level.random.nextInt(13);
            this.groundHoldTicks = 4 + level.random.nextInt(13);
            this.fadesAfterLanding = level.random.nextBoolean();
            this.fadeStartAge = this.startDelay + (this.fadesAfterLanding ? 180 : 24 + level.random.nextInt(25));
            this.lifetime = this.fadeStartAge + this.fadeTicks;
            this.fallGravity = 0.025f + level.random.nextFloat() * 0.035f;
            this.gravity = 0.0f;
            this.terminalFallSpeed = 0.02f + level.random.nextFloat() * 0.025f;
            this.quadSize = this.baseQuadSize = 0.17f + level.random.nextFloat() * 0.09f;
            this.baseAlpha = 0.78f + level.random.nextFloat() * 0.18f;
            this.alpha = 0.0f;
            this.driftX = (level.random.nextDouble() - 0.5) * 0.014;
            this.driftZ = (level.random.nextDouble() - 0.5) * 0.014;
            this.initialYSpeed = 0.01 + level.random.nextDouble() * 0.04;
            this.xd = 0.0;
            this.yd = 0.0;
            this.zd = 0.0;
            this.swayPhase = level.random.nextFloat() * (float)Math.PI * 2;
            this.swaySpeed = 0.13f + level.random.nextFloat() * 0.16f;
            this.swayAmount = 0.007f + level.random.nextFloat() * 0.017f;
            this.spinDirection = level.random.nextBoolean() ? 1.0f : -1.0f;
            this.oRoll = this.roll = level.random.nextFloat() * (float)Math.PI * 2;
            this.started = this.startDelay == 0;
            if (this.started) {
                this.beginFalling();
            }
        }

        @Override
        public void tick() {
            if (!this.started && this.age >= this.startDelay) {
                this.started = true;
                this.beginFalling();
            }
            super.tick();
            if (this.removed) {
                return;
            }
            if (!this.started) {
                this.alpha = 0.0f;
                return;
            }
            float swayTime = (float)this.age * this.swaySpeed + this.swayPhase;
            this.oRoll = this.roll;
            if (this.onGround || this.landed) {
                if (!this.landed) {
                    this.landed = true;
                    if (this.fadesAfterLanding) {
                        this.fadeStartAge = this.age + this.groundHoldTicks;
                        this.lifetime = this.fadeStartAge + this.fadeTicks;
                    }
                }
                this.xd *= 0.18;
                this.yd = 0.0;
                this.zd *= 0.18;
                this.roll += this.spinDirection * 0.012f;
            } else {
                this.xd = this.driftX + Mth.sin(swayTime) * this.swayAmount;
                this.zd = this.driftZ + Mth.cos(swayTime * 0.82f) * this.swayAmount * 0.75;
                this.yd = Math.max(this.yd, -this.terminalFallSpeed);
                this.roll += this.spinDirection * (0.035f + Mth.sin(swayTime) * 0.03f);
            }
            int visibleAge = this.age - this.startDelay;
            float fadeIn = Mth.clamp((float)visibleAge / 4.0f, 0.0f, 1.0f);
            if (this.age >= this.fadeStartAge) {
                float fade = Mth.clamp((float)(this.age - this.fadeStartAge) / (float)this.fadeTicks, 0.0f, 1.0f);
                this.alpha = this.baseAlpha * fadeIn * (1.0f - KillFeatherParticle.smootherStep(fade));
                this.quadSize = this.baseQuadSize * (1.0f - fade * 0.12f);
            } else {
                this.alpha = this.baseAlpha * fadeIn;
            }
        }

        private void beginFalling() {
            this.gravity = this.fallGravity;
            this.xd = this.driftX;
            this.yd = this.initialYSpeed;
            this.zd = this.driftZ;
        }

        private static float smootherStep(float value) {
            return value * value * value * (value * (value * 6.0f - 15.0f) + 10.0f);
        }

        @Override
        public ParticleRenderType getRenderType() {
            return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
        }

        public static final class Provider implements ParticleProvider<SimpleParticleType> {
            private final SpriteSet sprites;

            public Provider(SpriteSet sprites) {
                this.sprites = sprites;
            }

            @Nullable
            @Override
            public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
                return new KillFeatherParticle(level, x, y, z, this.sprites);
            }
        }
    }

    public static final class RivenSplitParticle extends TextureSheetParticle {
        private final float baseQuadSize;
        private final float baseAlpha;
        private final float spinSpeed;

        private RivenSplitParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
            super(level, x, y, z, xSpeed, ySpeed, zSpeed);
            this.pickSprite(sprites);
            this.hasPhysics = false;
            this.gravity = 0.0f;
            this.friction = 0.93f;
            this.xd = xSpeed;
            this.yd = ySpeed;
            this.zd = zSpeed;
            this.lifetime = 12 + level.random.nextInt(7);
            this.baseQuadSize = 0.54f + level.random.nextFloat() * 0.22f;
            this.quadSize = this.baseQuadSize * 0.72f;
            this.baseAlpha = 0.62f + level.random.nextFloat() * 0.2f;
            this.alpha = 0.0f;
            this.spinSpeed = (level.random.nextBoolean() ? 1.0f : -1.0f) * (0.045f + level.random.nextFloat() * 0.055f);
            this.oRoll = this.roll = level.random.nextFloat() * (float)Math.PI * 2;
        }

        @Override
        public void tick() {
            this.oRoll = this.roll;
            super.tick();
            if (this.removed) {
                return;
            }
            this.roll += this.spinSpeed;
            float progress = Mth.clamp((float)this.age / (float)this.lifetime, 0.0f, 1.0f);
            float fadeIn = Mth.clamp(progress * 7.0f, 0.0f, 1.0f);
            float fadeOut = 1.0f - Mth.clamp((progress - 0.48f) / 0.52f, 0.0f, 1.0f);
            this.alpha = this.baseAlpha * fadeIn * fadeOut;
            this.quadSize = this.baseQuadSize * (0.72f + Mth.sin(progress * (float)Math.PI) * 0.42f);
        }

        @Override
        public ParticleRenderType getRenderType() {
            return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
        }

        public static final class Provider implements ParticleProvider<SimpleParticleType> {
            private final SpriteSet sprites;

            public Provider(SpriteSet sprites) {
                this.sprites = sprites;
            }

            @Nullable
            @Override
            public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
                return new RivenSplitParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
            }
        }
    }

    public static final class RivenStreakParticle extends TextureSheetParticle {
        private final float baseQuadSize;
        private final float baseAlpha;

        private RivenStreakParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
            super(level, x, y, z, xSpeed, ySpeed, zSpeed);
            this.pickSprite(sprites);
            this.hasPhysics = false;
            this.gravity = 0.0f;
            this.friction = 0.86f;
            this.xd = xSpeed;
            this.yd = ySpeed;
            this.zd = zSpeed;
            this.lifetime = 7 + level.random.nextInt(5);
            this.quadSize = this.baseQuadSize = 0.48f + level.random.nextFloat() * 0.18f;
            this.baseAlpha = 0.72f + level.random.nextFloat() * 0.2f;
            this.alpha = 0.0f;
            this.oRoll = this.roll = (float)level.random.nextInt(8) * 0.7853982f;
        }

        @Override
        public void tick() {
            super.tick();
            if (this.removed) {
                return;
            }
            float progress = Mth.clamp((float)this.age / (float)this.lifetime, 0.0f, 1.0f);
            float fadeIn = Mth.clamp(progress * 9.0f, 0.0f, 1.0f);
            float fadeOut = 1.0f - Mth.clamp((progress - 0.32f) / 0.68f, 0.0f, 1.0f);
            this.alpha = this.baseAlpha * fadeIn * fadeOut;
            this.quadSize = this.baseQuadSize * (1.0f - progress * 0.34f);
        }

        @Override
        public ParticleRenderType getRenderType() {
            return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
        }

        public static final class Provider implements ParticleProvider<SimpleParticleType> {
            private final SpriteSet sprites;

            public Provider(SpriteSet sprites) {
                this.sprites = sprites;
            }

            @Nullable
            @Override
            public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
                return new RivenStreakParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
            }
        }
    }
}
