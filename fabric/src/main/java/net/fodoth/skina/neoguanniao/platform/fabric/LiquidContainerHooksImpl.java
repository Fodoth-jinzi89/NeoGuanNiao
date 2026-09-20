package net.fodoth.skina.neoguanniao.platform.fabric;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fodoth.skina.neoguanniao.content.cage.SimpleFluidTank;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;

/** 1 mB 对应的液滴数；fabric 的流体量以液滴计。 */
public final class LiquidContainerHooksImpl {

    private static final int DROPLETS_PER_MB = (int) (FluidConstants.BUCKET / 1000);

    private LiquidContainerHooksImpl() {
    }

    public static int drainIntoContainer(@NotNull SimpleFluidTank tank, @NotNull Player player,
                                         @NotNull InteractionHand hand, boolean simulate) {
        if (tank.isEmpty()) return 0;
        Fluid fluid = BuiltInRegistries.FLUID.get(tank.getFluidId());
        if (fluid == Fluids.EMPTY) return 0;

        // forPlayerInteraction 负责把桶/瓶换成装好的容器；只试算时事务不提交，什么都不会变。
        ContainerItemContext context = ContainerItemContext.forPlayerInteraction(player, hand);
        Storage<FluidVariant> storage = context.find(FluidStorage.ITEM);
        if (storage == null) return 0;

        long room = 0;
        for (StorageView<FluidVariant> view : storage) {
            room += Math.max(0, view.getCapacity() - view.getAmount());
        }
        long offered = Math.min((long) tank.getAmount() * DROPLETS_PER_MB, room);
        if (offered <= 0) return 0;

        try (Transaction transaction = Transaction.openOuter()) {
            long inserted = storage.insert(FluidVariant.of(fluid), offered, transaction);
            if (inserted <= 0) return 0;
            if (!simulate) {
                transaction.commit();
                tank.drain((int) (inserted / DROPLETS_PER_MB));
            }
            return (int) (inserted / DROPLETS_PER_MB);
        }
    }
}
