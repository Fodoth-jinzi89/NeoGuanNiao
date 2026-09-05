package com.birdcamera.content.villager;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;

public class MerchantOfferBuilder {
   private final MerchantOffer offer;

   private MerchantOfferBuilder(MerchantOffer offer) {
      this.offer = offer;
   }

   public static MerchantOfferBuilder of(MerchantOffer offer) {
      return new MerchantOfferBuilder(offer);
   }

   public MerchantOfferBuilder displayCost(ItemStack stack) {
      ((MerchantOfferAccessor)this.offer).birdcamera$setDisplayCost(stack);
      return this;
   }

   public MerchantOfferBuilder displayCostB(ItemStack stack) {
      ((MerchantOfferAccessor)this.offer).birdcamera$setCostBDisplay(stack);
      return this;
   }

   public MerchantOfferBuilder displayResult(ItemStack stack) {
      ((MerchantOfferAccessor)this.offer).birdcamera$setResultDisplay(stack);
      return this;
   }

   public MerchantOfferBuilder display(ItemStack cost, ItemStack costB, ItemStack result) {
      MerchantOfferAccessor accessor = (MerchantOfferAccessor)this.offer;
      accessor.birdcamera$setDisplayCost(cost);
      accessor.birdcamera$setCostBDisplay(costB);
      accessor.birdcamera$setResultDisplay(result);
      return this;
   }

   public MerchantOffer build() {
      return this.offer;
   }
}
