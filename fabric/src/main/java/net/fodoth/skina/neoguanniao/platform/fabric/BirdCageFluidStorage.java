package net.fodoth.skina.neoguanniao.platform.fabric;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.fodoth.skina.neoguanniao.content.cage.BirdCageBlockEntity;
import net.fodoth.skina.neoguanniao.content.cage.SimpleFluidTank;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

/**
 * 把小型鸟笼的流体槽暴露成 fabric-transfer 的流体存储。
 * <p>
 * 单槽、容量 1000 mB，接受任意流体，只能输入不能输出；任意面行为一致。
 * fabric 的流体量以液滴计（1 桶 = 81000 液滴 = 1000 mB），这里负责换算。
 * </p>
 */
public final class BirdCageFluidStorage extends SnapshotParticipant<CompoundTag>
        implements SingleSlotStorage<FluidVariant> {

    /** 1 mB 对应的液滴数。 */
    private static final int DROPLETS_PER_MB = (int) (FluidConstants.BUCKET / 1000);

    private final BirdCageBlockEntity cage;


    public BirdCageFluidStorage(BirdCageBlockEntity cage) {
        this.cage = cage;
    }


    private SimpleFluidTank tank() {
        return cage.fluidTank(0);
    }


    @Override
    public long insert(FluidVariant insertedVariant, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(insertedVariant, maxAmount);

        int requested = (int) Math.min(maxAmount / DROPLETS_PER_MB, Integer.MAX_VALUE);
        if (requested <= 0) return 0;

        ResourceLocation id = BuiltInRegistries.FLUID.getKey(insertedVariant.getFluid());
        if (id == null) return 0;

        // 和手动倒桶一致：优先空槽，其次装着同一种流体且没满的槽。
        for (SimpleFluidTank fluidTank : cage.fluidTanks()) {
            int filled = fluidTank.simulateFill(id, requested);
            if (filled <= 0) continue;
            updateSnapshots(transaction);
            fluidTank.fill(id, filled);
            return (long) filled * DROPLETS_PER_MB;
        }
        return 0;
    }

    @Override
    public long extract(FluidVariant variant, long maxAmount, TransactionContext transaction) {
        // 只进不出。
        return 0;
    }

    @Override
    public boolean supportsExtraction() {
        return false;
    }

    @Override
    public boolean isResourceBlank() {
        return tank().isEmpty();
    }

    @Override
    public FluidVariant getResource() {
        if (tank().isEmpty()) return FluidVariant.blank();
        Fluid fluid = BuiltInRegistries.FLUID.get(tank().getFluidId());
        return fluid == Fluids.EMPTY ? FluidVariant.blank() : FluidVariant.of(fluid);
    }

    @Override
    public long getAmount() {
        long amount = 0;
        for (SimpleFluidTank fluidTank : cage.fluidTanks()) amount += fluidTank.getAmount();
        return amount * DROPLETS_PER_MB;
    }

    @Override
    public long getCapacity() {
        long capacity = 0;
        for (SimpleFluidTank fluidTank : cage.fluidTanks()) capacity += fluidTank.getCapacity();
        return capacity * DROPLETS_PER_MB;
    }

    @Override
    protected CompoundTag createSnapshot() {
        CompoundTag snapshot = new CompoundTag();
        for (int index = 0; index < cage.fluidTanks().size(); index++) {
            snapshot.put(String.valueOf(index), cage.fluidTank(index).serializeNBT());
        }
        return snapshot;
    }

    @Override
    protected void readSnapshot(CompoundTag snapshot) {
        for (int index = 0; index < cage.fluidTanks().size(); index++) {
            cage.fluidTank(index).deserializeNBT(snapshot.getCompound(String.valueOf(index)));
        }
        cage.setChanged();
    }

    @Override
    protected void onFinalCommit() {
        cage.setChanged();
    }
}
