package net.fodoth.skina.neoguanniao.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.architectury.platform.Platform;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public final class CarryOnHooks {
    private CarryOnHooks() {}

    /** Carry On 是可选依赖：未安装时不能调用下面的平台实现，否则会解析到缺失的 Carry On 类。 */
    public static boolean isLoaded() {
        return Platform.isModLoaded("carryon");
    }

    @ExpectPlatform
    public static boolean tryCarryEntity(Player player, Entity entity) { return false; }

    @ExpectPlatform
    public static @Nullable Entity carriedEntity(Player player) { return null; }

    @ExpectPlatform
    public static void clearCarriedEntity(Player player) {}
}
