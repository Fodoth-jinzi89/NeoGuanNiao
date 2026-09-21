package net.fodoth.skina.neoguanniao.platform.neoforge;

import net.fodoth.skina.neoguanniao.content.cage.BirdCageBlockEntity;
import net.fodoth.skina.neoguanniao.content.cage.SimpleFluidTank;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

/**
 * 把鸟笼的水槽暴露成 NeoForge 的 {@link IFluidHandler}。
 * <p>
 * 小型/中型 1 个水槽、大型 2 个；每个都能装任意流体，只能输入不能输出；任意面行为一致。
 * </p>
 */
public final class BirdCageFluidHandler implements IFluidHandler {

    private final BirdCageBlockEntity cage;


    public BirdCageFluidHandler(BirdCageBlockEntity cage) {
        this.cage = cage;
    }


    private SimpleFluidTank tank(int tank) {
        return cage.fluidTank(tank);
    }

    @Override
    public int getTanks() {
        return cage.fluidTanks().size();
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        SimpleFluidTank fluidTank = tank(tank);
        if (fluidTank.isEmpty()) return FluidStack.EMPTY;
        Fluid fluid = BuiltInRegistries.FLUID.get(fluidTank.getFluidId());
        return fluid == Fluids.EMPTY ? FluidStack.EMPTY : new FluidStack(fluid, fluidTank.getAmount());
    }

    @Override
    public int getTankCapacity(int tank) {
        return tank(tank).getCapacity();
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return true;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (resource.isEmpty()) return 0;
        ResourceLocation id = BuiltInRegistries.FLUID.getKey(resource.getFluid());
        if (id == null) return 0;

        // 和手动倒桶一致：优先空槽，其次装着同一种流体且没满的槽。
        for (SimpleFluidTank fluidTank : cage.fluidTanks()) {
            if (!fluidTank.isEmpty() && !id.equals(fluidTank.getFluidId())) continue;
            if (fluidTank.getAmount() >= fluidTank.getCapacity()) continue;
            int filled = fluidTank.simulateFill(id, resource.getAmount());
            if (filled <= 0) continue;
            if (action.execute()) {
                fluidTank.fill(id, filled);
                cage.setChanged();
            }
            return filled;
        }
        return 0;
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        // 只进不出。
        return FluidStack.EMPTY;
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        // 只进不出。
        return FluidStack.EMPTY;
    }
}
