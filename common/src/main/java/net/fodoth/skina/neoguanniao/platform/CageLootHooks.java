package net.fodoth.skina.neoguanniao.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

/**
 * 和 goldentweaks 的右键拾取对接：鸟笼里掏出来的东西是直接塞给玩家，还是留在地上，由它说了算。
 * <p>
 * goldentweaks 是 NeoForge 专用模组，Fabric 侧没有它，对应的实现一律按「没装」处理。
 * </p>
 */
public final class CageLootHooks {
    private CageLootHooks() {}

    /**
     * 一次右键最多取出几组物品（0 表示不限）。
     * <p>
     * 没装 goldentweaks、或它的 Lootr 右键拾取没开时是 1 组；开了就按它对 Lootr 容器配置的量来。
     * </p>
     */
    @ExpectPlatform
    public static int pickupGroups() {
        throw new AssertionError();
    }

    /**
     * 把刚生成的掉落物交给 goldentweaks 的右键拾取：和它对 Lootr 容器的处理一致，
     * 按 {@code lootr_show_flying_items} 决定让物品飞向玩家还是直接拾取。
     *
     * @return 是否已经拾取（没装 goldentweaks 或功能没开时返回 false，掉落物就留在地上）
     */
    @ExpectPlatform
    public static boolean tryPickup(@NotNull Player player, @NotNull ItemEntity entity) {
        throw new AssertionError();
    }
}
