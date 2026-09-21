package net.fodoth.skina.neoguanniao.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FluidBucketHooks {
    private FluidBucketHooks() {}

    /**
     * 桶装物品里装着的流体；取不到（不是流体容器、或是空桶）时返回 null。
     * <p>
     * 两个平台各自由自己的物品流体容器能力回答：NeoForge 是 {@code Capabilities.FluidHandler.ITEM}，
     * Fabric 是 {@code FluidStorage.ITEM}。原版 {@code BucketItem} 的流体字段是私有的、没有读取接口，
     * 只能走这条能力查询。
     * </p>
     */
    @ExpectPlatform
    public static @Nullable ResourceLocation bucketFluid(@NotNull ItemStack stack) {
        throw new AssertionError();
    }
}
