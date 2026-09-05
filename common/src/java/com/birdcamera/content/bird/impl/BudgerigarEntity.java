package com.birdcamera.content.bird.impl;

import java.util.ArrayList;
import java.util.List;
import com.birdcamera.content.bird.core.AbstractBirdEntity;
import com.birdcamera.content.bird.core.BirdBehaviorState;
import com.birdcamera.content.bird.core.SimpleNeoBirdEntity;
import com.birdcamera.content.bird.core.controller.BirdControllers;
import com.birdcamera.content.bird.core.controller.BirdTameController;
import com.birdcamera.content.bird.core.data.BirdData;
import com.birdcamera.content.bird.core.goal.goals.BirdMusicDanceGoal;
import com.birdcamera.registry.BirdCameraBirdData;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class BudgerigarEntity extends SimpleNeoBirdEntity<BudgerigarEntity> {
   private static final ResourceLocation CHIRPY_PARTNER_ADVANCEMENT = ResourceLocation.fromNamespaceAndPath("birdcamera", "husbandry/chirpy_partner");

   // 附近播放音乐盒的缓存（减少搜索开销，与原版 2.9.1 一致）
   private BlockPos cachedPlayingJukebox;
   private Level cachedJukeboxLevel;
   private long nextJukeboxSearchGameTime;

   public BudgerigarEntity(EntityType<BudgerigarEntity> entityType, Level level) {
      super(
         entityType,
         level,
         (BirdData)BirdCameraBirdData.BUDGERIGAR,
         BirdControllers.<BudgerigarEntity>builder().birdTameController(new BirdTameController<BudgerigarEntity>() {
            @Override
            public void triggerTameSideEffects(Player player) {
               if (player instanceof ServerPlayer serverPlayer) {
                  ServerAdvancementManager advancements = serverPlayer.server.getAdvancements();
                  AdvancementHolder chirpyPartnerAdvancement = advancements.get(BudgerigarEntity.CHIRPY_PARTNER_ADVANCEMENT);
                  if (chirpyPartnerAdvancement != null) {
                     serverPlayer.getAdvancements().award(chirpyPartnerAdvancement, "tame_budgerigar");
                  }
               }
            }
         }).build()
      );
   }

   protected BudgerigarEntity getSelf() {
      return this;
   }

   @Override
   protected void initControllers() {
      super.initControllers();
      this.getBirdControllers().getBirdTickController().getTickTimer().getBirdFindNearbyMusicLoopTicker().setTicks(10 + this.getRandom().nextInt(20));
   }

   public static Builder createAttributes() {
      return SimpleNeoBirdEntity.createAttributes(6.0, 0.24, 0.32, 18.0);
   }

   public static boolean canSpawn(
      EntityType<? extends AbstractBirdEntity<?>> entityType, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random
   ) {
      return SimpleNeoBirdEntity.canSpawn(entityType, level, spawnType, pos, random, (BirdData)BirdCameraBirdData.BUDGERIGAR);
   }

   @Override
   protected List<Goal> buildGoals() {
      List<Goal> goals = new ArrayList<>(super.buildGoals());
      goals.add(1, new BirdMusicDanceGoal(this));
      return goals;
   }

   @NotNull
   @Override
   public InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (this.isCocoa(stack)) {
         if (!this.level().isClientSide) {
            this.getBirdControllers()
               .getBirdBehaviorStateController()
               .setBehaviorStateFor(BirdBehaviorState.CURIOUS, ((BirdData)BirdCameraBirdData.BUDGERIGAR).eating().curiousTicksLimitForDroppedFood());
            SoundEvent sound = this.getBirdControllers().getBirdSoundController().getInteractionSound();
            if (sound != null) {
               this.playSound(
                  this.getBirdControllers().getBirdSoundController().getInteractionSound(),
                  ((BirdData)BirdCameraBirdData.BUDGERIGAR).eating().eatSoundVolume(),
                  ((BirdData)BirdCameraBirdData.BUDGERIGAR).eating().eatSoundPitch()
               );
            }
         }

         return InteractionResult.sidedSuccess(this.level().isClientSide);
      } else {
         return super.mobInteract(player, hand);
      }
   }

   private boolean isCocoa(ItemStack stack) {
      return !stack.isEmpty() && stack.is(Items.COCOA_BEANS);
   }

   public boolean isBusyWithMusicOrSleep() {
      return this.isDancing() || this.getBirdControllers().getBirdRoutineController().isSleepingOrRoosting();
   }

   public void triggerMusic(int ticks) {
      this.getBirdControllers()
         .getBirdTickController()
         .getTickTimer()
         .getBirdMusicTicker()
         .setTicks(Math.max(this.getBirdControllers().getBirdTickController().getTickTimer().getBirdMusicTicker().getTicks(), ticks));
      if (!this.getBirdControllers().getBirdEatingController().isEating()
         && !this.getBirdControllers().getBirdBehaviorStateController().getBehaviorState().isEscape()) {
         this.getBirdControllers().getBirdBehaviorStateController().setBehaviorStateFor(BirdBehaviorState.DANCING, Math.min(ticks, 80));
      }
   }

   public BlockPos findNearbyJukebox() {
      if (this.cachedPlayingJukebox != null) {
         BlockPos origin = this.blockPosition();
         int dx = Math.abs(this.cachedPlayingJukebox.getX() - origin.getX());
         int dy = Math.abs(this.cachedPlayingJukebox.getY() - origin.getY());
         int dz = Math.abs(this.cachedPlayingJukebox.getZ() - origin.getZ());
         if (this.cachedJukeboxLevel == this.level()
            && dx <= 8
            && dy <= 3
            && dz <= 8
            && this.level().hasChunkAt(this.cachedPlayingJukebox)) {
            BlockState cachedState = this.level().getBlockState(this.cachedPlayingJukebox);
            if (cachedState.is(Blocks.JUKEBOX)
               && cachedState.hasProperty(JukeboxBlock.HAS_RECORD)
               && (Boolean)cachedState.getValue(JukeboxBlock.HAS_RECORD)) {
               return this.cachedPlayingJukebox;
            }
         }

         this.cachedPlayingJukebox = null;
         this.cachedJukeboxLevel = null;
      }

      long gameTime = this.level().getGameTime();
      if (gameTime < this.nextJukeboxSearchGameTime) {
         return null;
      }

      this.nextJukeboxSearchGameTime = gameTime + 100L + (long)this.getRandom().nextInt(41);
      BlockPos origin = this.blockPosition();

      for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-8, -3, -8), origin.offset(8, 3, 8))) {
         BlockState state = this.level().getBlockState(pos);
         if (state.is(Blocks.JUKEBOX) && state.hasProperty(JukeboxBlock.HAS_RECORD) && (Boolean)state.getValue(JukeboxBlock.HAS_RECORD)) {
            this.cachedPlayingJukebox = pos.immutable();
            this.cachedJukeboxLevel = this.level();
            this.nextJukeboxSearchGameTime = gameTime + 20L;
            return this.cachedPlayingJukebox;
         }
      }

      return null;
   }
}
