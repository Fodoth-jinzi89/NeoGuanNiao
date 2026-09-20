package net.fodoth.skina.neoguanniao.platform.fabric;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FluidBucketHooksImpl {
    private FluidBucketHooksImpl() {}

    public static @Nullable ResourceLocation bucketFluid(@NotNull ItemStack stack) {
        // withConstant：只查询、不改动这个物品堆，取走的流体不会真的被抽出来。
        Storage<FluidVariant> storage = FluidStorage.ITEM.find(stack, ContainerItemContext.withConstant(stack));
        FluidVariant fluid = StorageUtil.findExtractableResource(storage, null);
        return fluid == null || fluid.isBlank() ? null : BuiltInRegistries.FLUID.getKey(fluid.getFluid());
    }
}
