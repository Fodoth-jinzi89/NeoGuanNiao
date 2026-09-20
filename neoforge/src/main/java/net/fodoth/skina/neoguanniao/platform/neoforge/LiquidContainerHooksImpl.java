package net.fodoth.skina.neoguanniao.platform.neoforge;

import net.fodoth.skina.neoguanniao.content.cage.SimpleFluidTank;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;

public final class LiquidContainerHooksImpl {
    private LiquidContainerHooksImpl() {}

    public static int drainIntoContainer(@NotNull SimpleFluidTank tank, @NotNull Player player,
                                         @NotNull InteractionHand hand, boolean simulate) {
        if (tank.isEmpty()) return 0;
        Fluid fluid = BuiltInRegistries.FLUID.get(tank.getFluidId());
        if (fluid == Fluids.EMPTY) return 0;

        ItemStack held = player.getItemInHand(hand);
        // 桶的物品流体能力只认单件（原版 FluidBucketWrapper 的 fill/drain 都直接要求 count == 1），
        // 手里叠着多个时先分出一件来操作，装好后把这一件还进背包、剩下的留在手上。
        boolean split = held.getCount() > 1;
        IFluidHandlerItem handler = Capabilities.FluidHandler.ITEM.getCapability(
                split ? held.copyWithCount(1) : held, null);
        if (handler == null) return 0;

        int room = 0;
        for (int i = 0; i < handler.getTanks(); i++) {
            // 空桶的 getFluidInTank 会给出一个「空」但带桶容量的堆，这里按空算。
            FluidStack existing = handler.getFluidInTank(i);
            int used = existing.isEmpty() ? 0 : existing.getAmount();
            room += Math.max(0, handler.getTankCapacity(i) - used);
        }
        int offered = Math.min(tank.getAmount(), room);
        if (offered <= 0) return 0;

        FluidStack resource = new FluidStack(fluid, offered);
        int filled = handler.fill(resource, IFluidHandler.FluidAction.SIMULATE);
        if (filled <= 0) return 0;
        if (simulate) return filled;

        filled = handler.fill(resource, IFluidHandler.FluidAction.EXECUTE);
        if (filled <= 0) return 0;
        tank.drain(filled);
        // 容器自己换好形态（比如水桶倒空成空桶、空桶装满成水桶）。
        ItemStack container = handler.getContainer();
        if (split) {
            held.shrink(1);
            if (!player.getInventory().add(container)) player.drop(container, false);
        } else {
            player.setItemInHand(hand, container);
        }
        return filled;
    }
}
