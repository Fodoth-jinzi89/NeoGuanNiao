package net.fodoth.skina.neoguanniao.platform.fabric;

import net.fodoth.skina.neoguanniao.content.nest.BirdNestBlockEntity;
import net.fodoth.skina.neoguanniao.content.nest.SimpleItemStackHandler;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoItems;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class BirdNestInventoryHooksImpl {
    private BirdNestInventoryHooksImpl() {}

    public static SimpleItemStackHandler create(BirdNestBlockEntity nest) {
        return new SimpleItemStackHandler(4) {
            @Override
            public int getSlotLimit(int slot) { return 1; }

            @Override
            public void setStackInSlot(int slot, @NotNull ItemStack stack) {
                if (!stack.isEmpty() && !stack.is(NeoGuanNiaoItems.BIRD_EGG.get())) {
                    return;
                }
                super.setStackInSlot(slot, stack.copyWithCount(Math.min(stack.getCount(), 1)));
                nest.setChanged();
            }
        };
    }
}
