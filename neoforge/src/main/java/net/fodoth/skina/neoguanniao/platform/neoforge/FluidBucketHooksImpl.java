package net.fodoth.skina.neoguanniao.platform.neoforge;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FluidBucketHooksImpl {
    private FluidBucketHooksImpl() {}

    public static @Nullable ResourceLocation bucketFluid(@NotNull ItemStack stack) {
        IFluidHandlerItem handler = Capabilities.FluidHandler.ITEM.getCapability(stack, null);
        if (handler == null || handler.getTanks() == 0) return null;
        FluidStack fluid = handler.getFluidInTank(0);
        return fluid.isEmpty() ? null : BuiltInRegistries.FLUID.getKey(fluid.getFluid());
    }
}
