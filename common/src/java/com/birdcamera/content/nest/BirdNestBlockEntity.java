package com.birdcamera.content.nest;

import com.birdcamera.content.advancement.criterion.HatchBirdEggTrigger;
import com.birdcamera.content.bird.core.AbstractBirdEntity;
import com.birdcamera.content.egg.BirdEggData;
import com.birdcamera.content.egg.BirdEggItem;
import com.birdcamera.registry.BirdCameraBlockEntityTypes;
import com.birdcamera.registry.BirdCameraCriteriaTriggers;
import com.birdcamera.registry.BirdCameraDataComponents;
import com.birdcamera.registry.BirdCameraItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.AnimatableManager.ControllerRegistrar;

public class BirdNestBlockEntity extends BlockEntity implements Container, GeoBlockEntity {
   private final NonNullList<ItemStack> items = NonNullList.withSize(4, ItemStack.EMPTY);
   private final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
   private final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");

   public BirdNestBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)BirdCameraBlockEntityTypes.BIRD_NEST, pos, state);
   }

   public int getContainerSize() {
      return this.items.size();
   }

   public boolean isEmpty() {
      for (ItemStack stack : this.items) {
         if (!stack.isEmpty()) {
            return false;
         }
      }

      return true;
   }

   @NotNull
   public ItemStack getItem(int slot) {
      return this.items.get(slot);
   }

   @NotNull
   public ItemStack removeItem(int slot, int amount) {
      return ContainerHelper.removeItem(this.items, slot, amount);
   }

   @NotNull
   public ItemStack removeItemNoUpdate(int slot) {
      return ContainerHelper.takeItem(this.items, slot);
   }

   public void setItem(int slot, @NotNull ItemStack stack) {
      this.items.set(slot, stack);
      this.setChanged();
   }

   public void clearContent() {
      this.items.clear();
      this.setChanged();
   }

   public boolean stillValid(@NotNull Player player) {
      return true;
   }

   public int getMaxStackSize(@NotNull ItemStack stack) {
      return 1;
   }

   public boolean canPlaceItem(int slot, ItemStack stack) {
      return stack.is(BirdCameraItems.BIRD_EGG);
   }

   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(new AnimationController(this, "main", 0, state -> state.setAndContinue(this.IDLE)));
   }

   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.cache;
   }

   public NonNullList<ItemStack> getItemHandler() {
      return this.items;
   }

   public boolean hasEmptySlot() {
      for (ItemStack stack : this.items) {
         if (stack.isEmpty()) {
            return true;
         }
      }

      return false;
   }

   public void addEgg(ItemStack egg) {
      for (int i = 0; i < this.items.size(); i++) {
         if (this.items.get(i).isEmpty()) {
            this.items.set(i, egg.copyWithCount(1));
            this.setChanged();
            return;
         }
      }
   }

   public void tickEggs() {
      if (this.level != null && !this.level.isClientSide) {
         for (int i = 0; i < this.getContainerSize(); i++) {
            ItemStack egg = this.items.get(i);
            if (!egg.isEmpty()) {
               BirdEggData data = BirdEggItem.getEggData(egg);
               if (data != null && data.alive()) {
                  BirdEggData newData = data.tickDown(20);
                  if (newData.canHatch()) {
                     this.hatchEgg(i, egg, newData);
                  } else {
                     egg.set((DataComponentType)BirdCameraDataComponents.BIRD_EGG_DATA, newData);
                     this.setItem(i, egg);
                  }
               }
            }
         }
      }
   }

   private void hatchEgg(int slot, ItemStack egg, BirdEggData data) {
      if (this.level != null) {
         EntityType<?> type = (EntityType<?>)BuiltInRegistries.ENTITY_TYPE.get(data.birdType());
         if (type.create(this.level) instanceof AbstractBirdEntity<?> bird) {
            bird.moveTo(
               (double)this.worldPosition.getX() + 0.5,
               (double)this.worldPosition.getY() + 0.3,
               (double)this.worldPosition.getZ() + 0.5,
               this.level.random.nextFloat() * 360.0F,
               0.0F
            );
            bird.applyEggData(data);
            bird.setAge(-24000);
            Component name = (Component)egg.get(DataComponents.CUSTOM_NAME);
            if (name != null) {
               bird.setCustomName(name);
            }

            this.level.addFreshEntity(bird);
            this.triggerHatchEggAdvancement();
            this.removeItemNoUpdate(slot);
            this.setChanged();
         }
      }
   }

   private void triggerHatchEggAdvancement() {
      if (this.level instanceof ServerLevel server) {
         server.getEntitiesOfClass(ServerPlayer.class, new AABB(this.worldPosition).inflate(16.0))
            .forEach(player -> ((HatchBirdEggTrigger)BirdCameraCriteriaTriggers.HATCH_BIRD_EGG).trigger(player));
      }
   }

   protected void saveAdditional(@NotNull CompoundTag tag, @NotNull Provider registries) {
      super.saveAdditional(tag, registries);
      ContainerHelper.saveAllItems(tag, this.items, registries);
   }

   protected void loadAdditional(@NotNull CompoundTag tag, @NotNull Provider registries) {
      super.loadAdditional(tag, registries);
      ContainerHelper.loadAllItems(tag, this.items, registries);
   }

   @NotNull
   public CompoundTag getUpdateTag(@NotNull Provider provider) {
      return this.saveWithoutMetadata(provider);
   }

   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   public void setChanged() {
      super.setChanged();
      if (this.level != null && !this.level.isClientSide) {
         this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
      }
   }
}