package net.fodoth.skina.neoguanniao.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.architectury.platform.Platform;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

public final class ModonomiconHooks {
    private ModonomiconHooks() {}

    /** Modonomicon 是可选依赖：未安装时不能调用下面的平台实现，否则会解析到缺失的 Modonomicon 类。 */
    public static boolean isLoaded() {
        return Platform.isModLoaded("modonomicon");
    }

    /** 观鸟手册物品；仅在 {@link #isLoaded()} 为 true 时可用。 */
    @ExpectPlatform
    public static @Nullable Item birdGuide() { throw new AssertionError(); }
}
