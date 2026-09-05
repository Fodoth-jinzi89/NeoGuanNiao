package net.fodoth.skina.neoguanniao.event;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.content.fan.FeatherFanProjectileEntity;
import net.fodoth.skina.neoguanniao.content.fan.FeatherFanItem;
import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoParticleTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid = NeoGuanNiao.MODID)
public final class FeatherFanCombatEvents {
    private FeatherFanCombatEvents() {
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent e) {
        if (!(e.getEntity().level() instanceof ServerLevel s)) {
            return;
        }
        boolean projectileKill = e.getSource().getDirectEntity() instanceof FeatherFanProjectileEntity;
        boolean meleeKill = e.getSource().getEntity() instanceof Player p
                && e.getSource().getDirectEntity() == p
                && p.getMainHandItem().getItem() instanceof FeatherFanItem;
        if (!projectileKill && !meleeKill) {
            return;
        }
        double x = e.getEntity().getX();
        double y = e.getEntity().getY() + e.getEntity().getBbHeight() * 0.72;
        double z = e.getEntity().getZ();
        s.sendParticles(ParticleTypes.POOF, x, y, z, 3, 0.16, 0.18, 0.16, 0.018);
        int featherCount = 3 + s.random.nextInt(5);
        s.sendParticles(NeoGuanNiaoParticleTypes.KILL_FEATHER.get(), x, y + 0.1, z, featherCount, 0.22, 0.1, 0.22, 0.0);
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent e) {
        if (!(e.getEntity() instanceof AbstractBirdEntity<?>)) {
            return;
        }
        if (e.getSource().getDirectEntity() instanceof FeatherFanProjectileEntity) {
            e.setCanceled(true);
            return;
        }
        if (e.getSource().getEntity() instanceof Player p && p.getMainHandItem().getItem() instanceof FeatherFanItem) {
            e.setCanceled(true);
        }
    }
}
