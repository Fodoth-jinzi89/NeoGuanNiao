package net.fodoth.skina.neoguanniao.event;

import net.fodoth.skina.neoguanniao.content.fan.FeatherFanProjectileEntity;
import net.fodoth.skina.neoguanniao.content.fan.FeatherFanItem;
import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoParticleTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;

public final class FeatherFanCombatEvents {
    private FeatherFanCombatEvents() {
    }

    public static void onDeath(LivingEntity entity, DamageSource source) {
        if (!(entity.level() instanceof ServerLevel s)) {
            return;
        }
        boolean projectileKill = source.getDirectEntity() instanceof FeatherFanProjectileEntity;
        boolean meleeKill = source.getEntity() instanceof Player p
                && source.getDirectEntity() == p
                && p.getMainHandItem().getItem() instanceof FeatherFanItem;
        if (!projectileKill && !meleeKill) {
            return;
        }
        double x = entity.getX();
        double y = entity.getY() + entity.getBbHeight() * 0.72;
        double z = entity.getZ();
        s.sendParticles(ParticleTypes.POOF, x, y, z, 3, 0.16, 0.18, 0.16, 0.018);
        int featherCount = 3 + s.random.nextInt(5);
        s.sendParticles(NeoGuanNiaoParticleTypes.KILL_FEATHER.get(), x, y + 0.1, z, featherCount, 0.22, 0.1, 0.22, 0.0);
    }

    public static boolean onIncomingDamage(LivingEntity entity, DamageSource source) {
        if (!(entity instanceof AbstractBirdEntity<?>)) {
            return false;
        }
        if (source.getDirectEntity() instanceof FeatherFanProjectileEntity) {
            return true;
        }
        if (source.getEntity() instanceof Player p && p.getMainHandItem().getItem() instanceof FeatherFanItem) {
            return true;
        }
        return false;
    }
}
