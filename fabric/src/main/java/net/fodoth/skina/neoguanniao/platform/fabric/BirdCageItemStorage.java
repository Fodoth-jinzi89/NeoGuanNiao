package net.fodoth.skina.neoguanniao.platform.fabric;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedSlottedStorage;
import net.fodoth.skina.neoguanniao.content.cage.BirdCageBlockEntity;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoItemTags;
import net.minecraft.world.item.ItemStack;

/**
 * 把小型鸟笼的物品栏暴露成 fabric-transfer 的物品存储。
 * <p>
 * 槽 0 是食物槽：接受 {@link NeoGuanNiaoItemTags#BIRD_FOOD} 与 {@link NeoGuanNiaoItemTags#BIRD_FOOD_FISH}，
 * 上限 16，只能输入；
 * 槽 1-4 是产量槽：上限 64，只能输出。任意面行为一致，与 NeoForge 侧规则保持对应。
 * </p>
 */
public final class BirdCageItemStorage extends CombinedSlottedStorage<ItemVariant, SingleStackStorage> {

    public BirdCageItemStorage(BirdCageBlockEntity cage) {
        super(createSlots(cage));
    }

    private static List<SingleStackStorage> createSlots(BirdCageBlockEntity cage) {
        List<SingleStackStorage> slots = new ArrayList<>(cage.itemHandler().getSlots());
        for (int slot = 0; slot < cage.itemHandler().getSlots(); slot++) {
            slots.add(new CageSlot(cage, slot));
        }
        return slots;
    }

    private static final class CageSlot extends SingleStackStorage {

        private final BirdCageBlockEntity cage;
        private final int slot;

        private CageSlot(BirdCageBlockEntity cage, int slot) {
            this.cage = cage;
            this.slot = slot;
        }

        @Override
        protected ItemStack getStack() {
            return cage.itemHandler().getStackInSlot(slot);
        }

        @Override
        protected void setStack(ItemStack stack) {
            cage.itemHandler().setStackInSlot(slot, stack);
        }

        @Override
        protected boolean canInsert(ItemVariant itemVariant) {
            if (slot >= cage.foodSlotCount()) return false;
            var item = itemVariant.getItem().builtInRegistryHolder();
            return item.is(NeoGuanNiaoItemTags.BIRD_FOOD) || item.is(NeoGuanNiaoItemTags.BIRD_FOOD_FISH);
        }

        @Override
        protected boolean canExtract(ItemVariant itemVariant) {
            return slot >= cage.foodSlotCount();
        }

        @Override
        protected int getCapacity(ItemVariant itemVariant) {
            int limit = slot < cage.foodSlotCount()
                    ? cage.foodStackLimit()
                    : BirdCageBlockEntity.OUTPUT_STACK_LIMIT;
            return itemVariant.isBlank() ? limit : Math.min(limit, itemVariant.toStack().getMaxStackSize());
        }

        @Override
        public boolean supportsInsertion() {
            return slot < cage.foodSlotCount();
        }

        @Override
        public boolean supportsExtraction() {
            return slot >= cage.foodSlotCount();
        }

        @Override
        protected void onFinalCommit() {
            cage.setChanged();
        }
    }
}
