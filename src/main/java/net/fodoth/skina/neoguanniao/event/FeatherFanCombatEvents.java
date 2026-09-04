package net.fodoth.skina.neoguanniao.event;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.content.fan.FeatherFanProjectileEntity;
import net.fodoth.skina.neoguanniao.content.fan.FeatherFanItem;
import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

@EventBusSubscriber(modid = NeoGuanNiao.MODID)
public final class FeatherFanCombatEvents {
    private FeatherFanCombatEvents() {
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent e) {
        if (e.getEntity().level().isClientSide) return;
        if (e.getSource().getDirectEntity() instanceof FeatherFanProjectileEntity p && p.level() instanceof net.minecraft.server.level.ServerLevel s)
            s.sendParticles(net.minecraft.core.particles.ParticleTypes.CLOUD, e.getEntity().getX(), e.getEntity().getY() + e.getEntity().getBbHeight() * .7, e.getEntity().getZ(), 6, .2, .1, .2, .02);
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent e) {
        if (!(e.getEntity() instanceof AbstractBirdEntity<?>)) return;
        if (e.getSource().getDirectEntity() instanceof FeatherFanProjectileEntity || e.getSource().getEntity() instanceof net.minecraft.world.entity.player.Player p && p.getMainHandItem().getItem() instanceof FeatherFanItem)
            e.setCanceled(true);
    }
}
