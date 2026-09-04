package net.fodoth.skina.neoguanniao.content.fan;

import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoEntityTypes;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoItems;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FeatherFanProjectileEntity extends ThrowableItemProjectile {
    private float damage = 4;
    private String skill = "";
    private int life;
    private LivingEntity anchor;
    private int phase;
    private final List<LivingEntity> chain = new ArrayList<>();

    public FeatherFanProjectileEntity(EntityType<? extends FeatherFanProjectileEntity> t, Level l) {
        super(t, l);
    }

    public FeatherFanProjectileEntity(Level l, LivingEntity o) {
        super(NeoGuanNiaoEntityTypes.FEATHER_FAN_PROJECTILE.get(), o, l);
    }

    public void configure(float c) {
        configure(c, "");
    }

    public void configure(float c, String s) {
        damage = 4 + c * 4;
        skill = s == null ? "" : s;
    }
    public void configure(float c, String s, float attack) { damage = attack * (0.6F + c * 0.4F); skill = s == null ? "" : s; }

    protected @NotNull Item getDefaultItem() {
        return NeoGuanNiaoItems.WIND_FEATHER_FAN.get();
    }

    public void tick() {
        super.tick();
        if (level() instanceof ServerLevel serverLevel) {
            spawnTrailParticles(serverLevel);
        }
        if (!level().isClientSide && "hunting".equals(skill)) {
            if (anchor == null || !anchor.isAlive()) {
                anchor = level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(10), x -> x != getOwner() && x.isAlive()).stream().min(java.util.Comparator.comparingDouble(x -> x.distanceToSqr(this))).orElse(null);
            }
            if (anchor != null) {
                var d = anchor.getEyePosition().subtract(position()).normalize();
                setDeltaMovement(getDeltaMovement().scale(.75).add(d.scale(.25)));
            }
        }
        if (!level().isClientSide && "burial".equals(skill) && anchor != null && anchor.isAlive()) {
            var a = level().getEntitiesOfClass(LivingEntity.class, anchor.getBoundingBox().inflate(3), x -> x != anchor && x != getOwner() && x.isAlive());
            for (var e : a) e.setDeltaMovement(anchor.position().subtract(e.position()).normalize().scale(.18));
            if (++phase % 10 == 0) for (var e : a) e.hurt(damageSources().thrown(this, getOwner()), damage * .2f);
            if (phase > 60) {
                for (var e : a) e.hurt(damageSources().thrown(this, getOwner()), damage * .8f);
                discard();
                return;
            }
        }
        if ("riven".equals(skill) && !level().isClientSide && anchor != null && phase++ > 20) {
            var d = anchor.getEyePosition().subtract(position()).normalize();
            setDeltaMovement(d.scale(1.8));
            if (phase > 30) discard();
        }
        if (++life > 100) discard();
    }

    private void spawnTrailParticles(ServerLevel level) {
        var particle = switch (skill) {
            case "burial" -> NeoGuanNiaoParticleTypes.BURIAL_WIND.get();
            case "riven" -> NeoGuanNiaoParticleTypes.RIVEN_STREAK.get();
            case "hunting" -> NeoGuanNiaoParticleTypes.HUNTING_STREAK.get();
            default -> NeoGuanNiaoParticleTypes.KILL_FEATHER.get();
        };
        level.sendParticles(particle, getX(), getY(), getZ(), 1, 0.03D, 0.03D, 0.03D, 0.0D);
    }

    protected void onHitEntity(@NotNull EntityHitResult r) {
        if (level().isClientSide || !(r.getEntity() instanceof LivingEntity e) || e == getOwner()) return;
        e.hurt(damageSources().thrown(this, getOwner()), damage);
        if ("burial".equals(skill)) {
            anchor = e;
            phase = 0;
            return;
        }
        if ("riven".equals(skill)) {
            anchor = e;
            phase = 0;
            for (LivingEntity a : level().getEntitiesOfClass(LivingEntity.class, e.getBoundingBox().inflate(3), x -> x != e && x != getOwner())) {
                a.hurt(damageSources().thrown(this, getOwner()), damage * .35f);
                chain.add(a);
            }
            return;
        }
        discard();
    }
}
