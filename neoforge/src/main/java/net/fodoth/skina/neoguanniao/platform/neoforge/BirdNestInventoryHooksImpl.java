package net.fodoth.skina.neoguanniao.platform.neoforge;

import net.fodoth.skina.neoguanniao.content.nest.BirdNestBlockEntity;
import net.fodoth.skina.neoguanniao.content.nest.SimpleItemStackHandler;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public final class BirdNestInventoryHooksImpl {
    private BirdNestInventoryHooksImpl() {}

    public static SimpleItemStackHandler create(BirdNestBlockEntity nest) {
        return new Inventory(nest);
    }

    public static ItemStackHandler itemHandler(BirdNestBlockEntity nest) {
        return ((Inventory) nest.getItemHandler()).delegate;
    }

    private static final class Inventory extends SimpleItemStackHandler {
        private final ItemStackHandler delegate;

        private Inventory(BirdNestBlockEntity nest) {
            super(0);
            delegate = new ItemStackHandler(4) {
                @Override
                public int getSlotLimit(int slot) { return 1; }

                @Override
                public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                    return stack.is(NeoGuanNiaoItems.BIRD_EGG.get());
                }

                @Override
                protected void onContentsChanged(int slot) { nest.setChanged(); }
            };
        }

        @Override
        public int getSlots() { return delegate.getSlots(); }
        @Override
        public int getSlotLimit(int slot) { return delegate.getSlotLimit(slot); }
        @Override
        public ItemStack getStackInSlot(int slot) { return delegate.getStackInSlot(slot); }
        @Override
        public void setStackInSlot(int slot, ItemStack stack) { delegate.setStackInSlot(slot, stack); }
        @Override
        public CompoundTag serializeNBT(HolderLookup.Provider provider) { return delegate.serializeNBT(provider); }
        @Override
        public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) { delegate.deserializeNBT(provider, tag); }
    }
}
