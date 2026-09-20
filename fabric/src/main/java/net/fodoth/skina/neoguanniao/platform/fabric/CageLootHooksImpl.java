package net.fodoth.skina.neoguanniao.platform.fabric;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

/** goldentweaks 是 NeoForge 专用模组，Fabric 侧一律按「没装」处理。 */
public final class CageLootHooksImpl {
    private CageLootHooksImpl() {}

    public static int pickupGroups() {
        return 1;
    }

    public static boolean tryPickup(@NotNull Player player, @NotNull ItemEntity entity) {
        return false;
    }
}
