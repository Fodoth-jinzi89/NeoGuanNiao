package net.fodoth.skina.neoguanniao.content.nest;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

class SimpleItemStackHandler {
    private final ItemStack[] stacks;
    SimpleItemStackHandler(int size) { stacks = new ItemStack[size]; java.util.Arrays.fill(stacks, ItemStack.EMPTY); }
    int getSlots() { return stacks.length; }
    protected int getSlotLimit(int slot) { return 64; }
    ItemStack getStackInSlot(int slot) { return stacks[slot]; }
    void setStackInSlot(int slot, ItemStack stack) { stacks[slot] = stack; }
    CompoundTag serializeNBT(HolderLookup.Provider ignored) { CompoundTag tag = new CompoundTag(); for (int i=0;i<stacks.length;i++) if (!stacks[i].isEmpty()) tag.put(String.valueOf(i), stacks[i].save(ignored)); return tag; }
    void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) { for (int i=0;i<stacks.length;i++) stacks[i] = tag.contains(String.valueOf(i)) ? ItemStack.parse(provider, tag.getCompound(String.valueOf(i))).orElse(ItemStack.EMPTY) : ItemStack.EMPTY; }
}


