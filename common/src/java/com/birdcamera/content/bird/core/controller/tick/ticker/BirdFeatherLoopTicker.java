package com.birdcamera.content.bird.core.controller.tick.ticker;

import com.birdcamera.content.bird.core.AbstractBirdEntity;
import com.birdcamera.content.feather.BirdFeatherData;
import com.birdcamera.content.feather.BirdFeatherItem;
import com.birdcamera.registry.BirdCameraItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class BirdFeatherLoopTicker<T extends AbstractBirdEntity<T>> extends AbstractBirdTicker<T> {
   private boolean initialized = false;

   public BirdFeatherLoopTicker() {
      super(true, false, true);
   }

   @Override
   protected void reset() {
      super.reset();
      this.setTicks(this.bird().getFeatherInterval());
      if (!this.initialized) {
         this.initialized = true;
      } else {
         this.dropFeather();
      }
   }

   private void dropFeather() {
      if (!this.bird().level().isClientSide) {
         if (!this.bird().isBaby()) {
            int featherCount = this.bird().getFeatherCount();
            if (featherCount > 0) {
               ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(this.bird().getType());
               BirdFeatherData featherData = BirdFeatherData.create(id, this.bird().getSkin().rarity().getRarity());
               ItemStack featherStack = new ItemStack((ItemLike)BirdCameraItems.BIRD_FEATHER, featherCount);
               BirdFeatherItem.setFeatherData(featherStack, featherData);
               ItemEntity itemEntity = new ItemEntity(this.bird().level(), this.bird().getX(), this.bird().getY(), this.bird().getZ(), featherStack);
               itemEntity.setDeltaMovement(
                  (this.bird().getRandom().nextDouble() - 0.5) * 0.2,
                  this.bird().getRandom().nextDouble() * 0.1 + 0.05,
                  (this.bird().getRandom().nextDouble() - 0.5) * 0.2
               );
               itemEntity.setPickUpDelay(40);
               this.bird().level().addFreshEntity(itemEntity);
            }
         }
      }
   }
}
