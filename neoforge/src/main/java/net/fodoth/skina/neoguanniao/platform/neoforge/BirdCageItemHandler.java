package net.fodoth.skina.neoguanniao.platform.neoforge;

import net.fodoth.skina.neoguanniao.content.cage.BirdCageBlockEntity;
import net.fodoth.skina.neoguanniao.content.nest.SimpleItemStackHandler;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoItemTags;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

/**
 * 把小型鸟笼的物品栏暴露成 NeoForge 的 {@link IItemHandler}。
 * <p>
 * 槽 0 是食物槽：接受 {@link NeoGuanNiaoItemTags#BIRD_FOOD} 与 {@link NeoGuanNiaoItemTags#BIRD_FOOD_FISH}，
 * 上限 16，只能输入；
 * 槽 1-4 是产量槽：上限 64，只能输出。任意面行为一致。
 * </p>
 */
public final class BirdCageItemHandler implements IItemHandler {

    private final BirdCageBlockEntity cage;


    public BirdCageItemHandler(BirdCageBlockEntity cage) {
        this.cage = cage;
    }


    private SimpleItemStackHandler inventory() {
        return cage.itemHandler();
    }

    @Override
    public int getSlots() {
        return inventory().getSlots();
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        return inventory().getStackInSlot(slot);
    }

    @Override
    public int getSlotLimit(int slot) {
        return slot < cage.foodSlotCount() ? cage.foodStackLimit() : BirdCageBlockEntity.OUTPUT_STACK_LIMIT;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return slot < cage.foodSlotCount()
                && (stack.is(NeoGuanNiaoItemTags.BIRD_FOOD) || stack.is(NeoGuanNiaoItemTags.BIRD_FOOD_FISH));
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        // 只有食物槽接受输入。
        if (slot < 0 || slot >= cage.foodSlotCount() || stack.isEmpty() || !isItemValid(slot, stack)) {
            return stack;
        }

        SimpleItemStackHandler inventory = inventory();
        ItemStack existing = inventory.getStackInSlot(slot);
        int limit = Math.min(getSlotLimit(slot), stack.getMaxStackSize());

        if (!existing.isEmpty()) {
            if (!ItemStack.isSameItemSameComponents(existing, stack)) return stack;
            limit -= existing.getCount();
        }
        if (limit <= 0) return stack;

        int inserted = Math.min(stack.getCount(), limit);
        if (!simulate) {
            if (existing.isEmpty()) inventory.setStackInSlot(slot, stack.copyWithCount(inserted));
            else existing.grow(inserted);
            cage.setChanged();
        }

        ItemStack remainder = stack.copy();
        remainder.shrink(inserted);
        return remainder;
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        // 食物槽不对外输出。
        if (slot < cage.foodSlotCount() || slot >= getSlots() || amount <= 0) return ItemStack.EMPTY;

        SimpleItemStackHandler inventory = inventory();
        ItemStack existing = inventory.getStackInSlot(slot);
        if (existing.isEmpty()) return ItemStack.EMPTY;

        int extracted = Math.min(amount, existing.getCount());
        ItemStack result = existing.copyWithCount(extracted);
        if (!simulate) {
            existing.shrink(extracted);
            if (existing.isEmpty()) inventory.setStackInSlot(slot, ItemStack.EMPTY);
            cage.setChanged();
        }
        return result;
    }
}
