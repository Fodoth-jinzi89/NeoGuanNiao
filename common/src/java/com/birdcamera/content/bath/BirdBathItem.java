package com.birdcamera.content.bath;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.Block;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.util.GeckoLibUtil;

public class BirdBathItem extends BlockItem implements GeoItem {
   private final BirdBathVariant variant;
   private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);

   public BirdBathItem(BirdBathVariant variant, Block block, Properties properties) {
      super(block, properties);
      this.variant = variant;
   }

   public BirdBathVariant variant() {
      return this.variant;
   }

   public void registerControllers(ControllerRegistrar controllers) {
   }

   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.animationCache;
   }
}
