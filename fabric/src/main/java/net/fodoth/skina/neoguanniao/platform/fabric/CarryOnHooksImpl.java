package net.fodoth.skina.neoguanniao.platform.fabric;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public final class CarryOnHooksImpl {
    private CarryOnHooksImpl() {}

    public static boolean tryCarryEntity(Player player, Entity entity) { return false; }

    public static @Nullable Entity carriedEntity(Player player) { return null; }

    public static void clearCarriedEntity(Player player) {}
}
