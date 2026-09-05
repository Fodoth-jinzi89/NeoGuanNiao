package com.birdcamera.event;

import com.birdcamera.content.bird.impl.NightHeronEntity;
import com.birdcamera.content.advancement.criterion.SpyglassAtBirdTrigger;
import com.birdcamera.registry.BirdCameraCriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 望远镜观察夜鹭成就触发（对应原版 SpyglassTickHandler）
 */
public class SpyglassTickHandler {

    public static void tick(ServerPlayer player) {
        if (!player.isUsingItem()) return;
        if (player.getUseItem().getItem() != Items.SPYGLASS) return;

        Entity target = getLookingEntity(player);
        if (target instanceof NightHeronEntity e) {
            ((SpyglassAtBirdTrigger) BirdCameraCriteriaTriggers.SPYGLASS_AT_BIRD).trigger(player, e);
        }
    }

    private static Entity getLookingEntity(Player player) {
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle();

        Vec3 end = start.add(look.scale(64));

        AABB box = player.getBoundingBox()
                .expandTowards(look.scale(64))
                .inflate(1);

        EntityHitResult result = ProjectileUtil.getEntityHitResult(
                player.level(),
                player,
                start,
                end,
                box,
                Entity::isPickable
        );

        return result == null ? null : result.getEntity();
    }
}