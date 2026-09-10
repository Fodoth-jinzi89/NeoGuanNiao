package net.fodoth.skina.neoguanniao.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public final class CarryOnHooks {
    private CarryOnHooks() {}

    @ExpectPlatform
    public static boolean tryCarryEntity(Player player, Entity entity) { return false; }

    @ExpectPlatform
    public static @Nullable Entity carriedEntity(Player player) { return null; }

    @ExpectPlatform
    public static void clearCarriedEntity(Player player) {}
}
