package net.fodoth.skina.neoguanniao.platform.neoforge;

import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import net.fodoth.skina.goldentweaks.util.GTState;
import net.fodoth.skina.goldentweaks.util.ItemPickupUtil;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public final class CageLootHooksImpl {
    private CageLootHooksImpl() {}

    public static int pickupGroups() {
        if (!quickLootEnabled()) return 1;
        int groups = GoldenTweaksCommonConfig.LOOTR_PICKUP_GROUPS.get();
        int interval = GoldenTweaksCommonConfig.LOOTR_HOLD_PICKUP_INTERVAL.get();
        // 和 goldentweaks 取物时算的数量一致：按住连取的时间间隔越短，一次拿的组数越多（上限 64）。
        if (groups != 0 && interval > 0 && interval < 5) {
            groups = Math.min(64, groups * ((5 + interval - 1) / interval));
        }
        return groups;
    }

    public static boolean tryPickup(@NotNull Player player, @NotNull ItemEntity entity) {
        // 和 goldentweaks 对 Lootr 容器的处理一致：刚生成的掉落物直接飞向玩家或直接拾取，
        // 不走 canPickup（那里会按拾取延迟把刚掉出来的东西挡掉）。
        if (!quickLootEnabled()) return false;
        if (GoldenTweaksCommonConfig.LOOTR_SHOW_FLYING_ITEMS.get()) {
            ItemPickupUtil.pullToPlayer(player, entity);
        } else {
            ItemPickupUtil.pickup(player, entity);
        }
        return true;
    }

    /** goldentweaks 初始化完了、并且它的 Lootr 右键拾取开着。 */
    private static boolean quickLootEnabled() {
        return GTState.isReady() && GoldenTweaksCommonConfig.LOOTR_QUICK_LOOT.get();
    }
}
