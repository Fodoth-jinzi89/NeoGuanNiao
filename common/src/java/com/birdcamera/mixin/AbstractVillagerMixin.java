package com.birdcamera.mixin;

import com.birdcamera.content.villager.MerchantOfferAccessor;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * AbstractVillager Mixin - 保存/加载村民交易的额外显示数据
 * 当村民交易被修改时，将自定义显示物品数据保存到NBT
 */
@Mixin(AbstractVillager.class)
public abstract class AbstractVillagerMixin {

    /**
     * 在村民读取NBT数据时，恢复自定义显示数据
     */
    @Inject(method = "readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("TAIL"))
    private void birdcamera$readDisplayData(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains("NeoguanniaoDisplayOffers", 9)) {
            AbstractVillager self = (AbstractVillager) (Object) this;
            HolderLookup.Provider registries = self.registryAccess();
            MerchantOffers offers = self.getOffers();
            ListTag displayList = tag.getList("NeoguanniaoDisplayOffers", 10);

            for (int i = 0; i < displayList.size() && i < offers.size(); i++) {
                CompoundTag offerTag = displayList.getCompound(i);
                MerchantOffer offer = offers.get(i);
                MerchantOfferAccessor accessor = (MerchantOfferAccessor) offer;

                if (offerTag.contains("DisplayCostA", 10)) {
                    accessor.birdcamera$setDisplayCost(ItemStack.parseOptional(registries, offerTag.getCompound("DisplayCostA")));
                }
                if (offerTag.contains("DisplayCostB", 10)) {
                    accessor.birdcamera$setCostBDisplay(ItemStack.parseOptional(registries, offerTag.getCompound("DisplayCostB")));
                }
                if (offerTag.contains("DisplayResult", 10)) {
                    accessor.birdcamera$setResultDisplay(ItemStack.parseOptional(registries, offerTag.getCompound("DisplayResult")));
                }
            }
        }
    }

    /**
     * 在村民保存NBT数据时，保存自定义显示数据
     */
    @Inject(method = "addAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("TAIL"))
    private void birdcamera$writeDisplayData(CompoundTag tag, CallbackInfo ci) {
        AbstractVillager self = (AbstractVillager) (Object) this;
        HolderLookup.Provider registries = self.registryAccess();
        MerchantOffers offers = self.getOffers();

        ListTag displayList = new ListTag();
        for (int i = 0; i < offers.size(); i++) {
            MerchantOffer offer = offers.get(i);
            MerchantOfferAccessor accessor = (MerchantOfferAccessor) offer;

            CompoundTag offerTag = new CompoundTag();
            ItemStack displayCost = accessor.birdcamera$getDisplayCost();
            ItemStack costBDisplay = accessor.birdcamera$getCostBDisplay();
            ItemStack resultDisplay = accessor.birdcamera$getResultDisplay();

            if (!displayCost.isEmpty()) {
                offerTag.put("DisplayCostA", displayCost.save(registries));
            }
            if (!costBDisplay.isEmpty()) {
                offerTag.put("DisplayCostB", costBDisplay.save(registries));
            }
            if (!resultDisplay.isEmpty()) {
                offerTag.put("DisplayResult", resultDisplay.save(registries));
            }

            if (!offerTag.isEmpty()) {
                displayList.add(offerTag);
            }
        }

        if (!displayList.isEmpty()) {
            tag.put("NeoguanniaoDisplayOffers", displayList);
        }
    }
}
