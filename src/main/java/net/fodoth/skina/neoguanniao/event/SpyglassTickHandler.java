package net.fodoth.skina.neoguanniao.event;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.content.bird.impl.neo.night_heron.NeoNightHeronEntity;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoCriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = NeoGuanNiao.MODID)
public class SpyglassTickHandler {


    @SubscribeEvent
    public static void playerTick(PlayerTickEvent.Post event) {

        if (!(event.getEntity() instanceof ServerPlayer player))
            return;


        if (!player.isUsingItem())
            return;


        if (player.getUseItem().getItem() != Items.SPYGLASS)
            return;


        Entity target = getLookingEntity(player);


        if(target instanceof NeoNightHeronEntity e) {

            NeoGuanNiaoCriteriaTriggers.SPYGLASS_AT_BIRD
                    .get()
                    .trigger(player, e);
        }
    }

    private static Entity getLookingEntity(Player player) {

        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle();

        Vec3 end = start.add(
                look.scale(64)
        );


        AABB box = player.getBoundingBox()
                .expandTowards(look.scale(64))
                .inflate(1);


        EntityHitResult result =
                ProjectileUtil.getEntityHitResult(
                        player.level(),
                        player,
                        start,
                        end,
                        box,
                        Entity::isPickable
                );


        return result == null
                ? null
                : result.getEntity();
    }
}
