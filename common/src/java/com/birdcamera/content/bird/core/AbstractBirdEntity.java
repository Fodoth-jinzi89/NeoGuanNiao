package com.birdcamera.content.bird.core;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import com.birdcamera.content.advancement.criterion.BreedBirdEggTrigger;
import com.birdcamera.content.bath.BirdBathContentType;
import com.birdcamera.content.bath.BirdBathFeedingAnimatable;
import com.birdcamera.content.bath.BirdBathMountable;
import com.birdcamera.content.bird.core.controller.BirdAnimationController;
import com.birdcamera.content.bird.core.controller.BirdBehaviorStateController;
import com.birdcamera.content.bird.core.controller.BirdBreedController;
import com.birdcamera.content.bird.core.controller.BirdControllers;
import com.birdcamera.content.bird.core.controller.BirdEatingController;
import com.birdcamera.content.bird.core.controller.BirdFeatherController;
import com.birdcamera.content.bird.core.controller.BirdFlyingController;
import com.birdcamera.content.bird.core.controller.BirdFoodBagController;
import com.birdcamera.content.bird.core.controller.BirdFrightController;
import com.birdcamera.content.bird.core.controller.BirdGoalController;
import com.birdcamera.content.bird.core.controller.BirdModelController;
import com.birdcamera.content.bird.core.controller.BirdRoutineController;
import com.birdcamera.content.bird.core.controller.BirdSkinController;
import com.birdcamera.content.bird.core.controller.BirdSoundController;
import com.birdcamera.content.bird.core.controller.BirdTameController;
import com.birdcamera.content.bird.core.controller.BirdTickController;
import com.birdcamera.content.bird.core.controller.tick.BirdTickTimer;
import com.birdcamera.content.bird.core.data.BirdData;
import com.birdcamera.content.bird.core.data.datum.BirdFlightProfile;
import com.birdcamera.content.bird.core.data.datum.BirdModelScaleProfile;
import com.birdcamera.content.bird.core.data.datum.BirdModelSkinDatum;
import com.birdcamera.content.bird.core.flight.BirdFlightAware;
import com.birdcamera.content.bird.core.goal.goals.BirdBathUseGoal;
import com.birdcamera.content.bird.core.goal.goals.BirdBreedGoal;
import com.birdcamera.content.bird.core.goal.goals.BirdCuriousFollowGoal;
import com.birdcamera.content.bird.core.goal.goals.BirdEatFoodGoal;
import com.birdcamera.content.bird.core.goal.goals.BirdFlockGoal;
import com.birdcamera.content.bird.core.goal.goals.BirdFollowOwnerGoal;
import com.birdcamera.content.bird.core.goal.goals.BirdIdleGoal;
import com.birdcamera.content.bird.core.goal.goals.BirdModelValidateGoal;
import com.birdcamera.content.bird.core.goal.goals.BirdRandomLookAroundGoal;
import com.birdcamera.content.bird.core.goal.goals.BirdRoostGoal;
import com.birdcamera.content.bird.core.goal.goals.BirdSentinelGoal;
import com.birdcamera.content.bird.core.goal.goals.BirdSkinValidateGoal;
import com.birdcamera.content.bird.core.goal.goals.BirdWakeUpGoal;
import com.birdcamera.content.bird.core.model.BirdModel;
import com.birdcamera.content.bird.core.model.BirdModelRarity;
import com.birdcamera.content.bird.core.model.BirdModelScale;
import com.birdcamera.content.bird.core.model.ScalableBirdModel;
import com.birdcamera.content.bird.core.skin.BirdSkin;
import com.birdcamera.content.bird.core.skin.BirdSkinRarity;
import com.birdcamera.content.egg.BirdEggData;
import com.birdcamera.content.egg.BirdEggItem;
import com.birdcamera.content.nest.BirdNestBlockEntity;
import com.birdcamera.registry.BirdCameraBlockTags;
import com.birdcamera.registry.BirdCameraCriteriaTriggers;
import com.birdcamera.registry.BirdCameraItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.AnimatableManager.ControllerRegistrar;

public abstract class AbstractBirdEntity<T extends AbstractBirdEntity<T>>
   extends TamableAnimal
   implements GeoEntity,
   FlyingAnimal,
   ScalableBirdModel,
   BirdFlightAware,
   BirdBathMountable,
   BirdBathFeedingAnimatable {
   public static final EntityDataAccessor<Integer> BEHAVIOR_STATE = SynchedEntityData.defineId(AbstractBirdEntity.class, EntityDataSerializers.INT);
   public static final EntityDataAccessor<Integer> SKIN_VARIANT = SynchedEntityData.defineId(AbstractBirdEntity.class, EntityDataSerializers.INT);
   public static final EntityDataAccessor<Integer> MODEL_VARIANT = SynchedEntityData.defineId(AbstractBirdEntity.class, EntityDataSerializers.INT);
   public static final EntityDataAccessor<Float> MODEL_SCALE = SynchedEntityData.defineId(AbstractBirdEntity.class, EntityDataSerializers.FLOAT);
   public static final EntityDataAccessor<Boolean> GENDER = SynchedEntityData.defineId(AbstractBirdEntity.class, EntityDataSerializers.BOOLEAN);
   public static final EntityDataAccessor<Integer> EGG_COUNT = SynchedEntityData.defineId(AbstractBirdEntity.class, EntityDataSerializers.INT);
   public static final EntityDataAccessor<Integer> FEATHER_COUNT = SynchedEntityData.defineId(AbstractBirdEntity.class, EntityDataSerializers.INT);
   public static final EntityDataAccessor<Integer> FEATHER_INTERVAL = SynchedEntityData.defineId(AbstractBirdEntity.class, EntityDataSerializers.INT);
   public static final EntityDataAccessor<Boolean> GROWTH_STOPPED = SynchedEntityData.defineId(AbstractBirdEntity.class, EntityDataSerializers.BOOLEAN);
   private boolean growthStopped = false;
   protected final BirdData BIRD_DATA;
   protected final BirdControllers<T> BIRD_CONTROLLERS;

   protected AbstractBirdEntity(EntityType<T> entityType, Level level, BirdData birdData, BirdControllers<T> birdControllers) {
      super(entityType, level);
      this.BIRD_DATA = birdData;
      this.BIRD_CONTROLLERS = birdControllers;
      this.initPathfindingMalus();
   }

   protected abstract T getSelf();

   protected void initControllers() {
      if (this.BIRD_CONTROLLERS != null) {
         this.BIRD_CONTROLLERS.attach(this.getSelf());
      }
   }

   public BirdControllers<T> getBirdControllers() {
      return this.BIRD_CONTROLLERS;
   }

   protected void initPathfindingMalus() {
      this.setPathfindingMalus(PathType.LEAVES, 0.0F);
      this.setPathfindingMalus(PathType.WATER, -1.0F);
      this.setPathfindingMalus(PathType.DANGER_FIRE, 16.0F);
      this.setPathfindingMalus(PathType.DAMAGE_FIRE, 16.0F);
   }

   protected void registerGoals() {
      super.registerGoals();
      int priority = 0;

      for (Goal goal : this.buildGoals()) {
         this.goalSelector.addGoal(priority, goal);
         priority++;
      }
   }

   protected List<Goal> buildGoals() {
      List<Goal> goals = new ArrayList<>();
      goals.add(new FloatGoal(this));
      goals.add(new BirdBreedGoal(this));
      goals.add(new BirdEatFoodGoal(this));
      goals.add(new BirdBathUseGoal(this));
      goals.add(new BirdSentinelGoal(this));
      goals.add(new BirdWakeUpGoal(this));
      goals.add(new BirdRoostGoal(this));
      goals.add(new BirdFollowOwnerGoal(this));
      goals.add(new BirdFlockGoal(this));
      goals.add(new BirdCuriousFollowGoal(this));
      goals.add(new BirdIdleGoal(this));
      goals.add(new BirdRandomLookAroundGoal(this));
      goals.add(new BirdSkinValidateGoal(this));
      goals.add(new BirdModelValidateGoal(this));
      return goals;
   }

   public static boolean canSpawn(
      EntityType<? extends AbstractBirdEntity<?>> entityType,
      ServerLevelAccessor level,
      MobSpawnType spawnType,
      BlockPos pos,
      RandomSource random,
      BirdData birdData
   ) {
      BlockState below = level.getBlockState(pos.below());
      boolean validGround = below.is(BlockTags.DIRT)
         || below.is(BlockTags.SAND)
         || below.is(Blocks.GRASS_BLOCK)
         || below.is(Blocks.DIRT_PATH)
         || below.is(Blocks.FARMLAND)
         || below.is(BirdCameraBlockTags.BIRD_PERCHES);
      if (!validGround) {
         return false;
      } else {
         return hasLocalSpawnCapacity(entityType, level, spawnType, pos, birdData);
      }
   }

   // 本地已加载鸟类总数上限（与原版 2.9.1 一致）
   private static final int MAX_LOCAL_BIRDS = 12;

   /**
    * 检查局部生成容量：仅对自然生成生效
    * - 附近全部鸟类总数不得超过 MAX_LOCAL_BIRDS
    * - 附近同种鸟类数量不得超过 spawnRarity
    */
   public static boolean hasLocalSpawnCapacity(
      EntityType<? extends AbstractBirdEntity<?>> entityType,
      ServerLevelAccessor level,
      MobSpawnType spawnType,
      BlockPos pos,
      BirdData birdData
   ) {
      if (spawnType != MobSpawnType.NATURAL && spawnType != MobSpawnType.CHUNK_GENERATION) {
         return true;
      }

      List<AbstractBirdEntity> entities = level.getEntitiesOfClass(
         AbstractBirdEntity.class,
         new AABB(
            (double)(pos.getX() - 8),
            (double)(pos.getY() - 4),
            (double)(pos.getZ() - 8),
            (double)(pos.getX() + 8),
            (double)(pos.getY() + 4),
            (double)(pos.getZ() + 8)
         )
      );
      if (entities.size() >= MAX_LOCAL_BIRDS) {
         return false;
      }

      int sameSpecies = 0;
      for (AbstractBirdEntity<?> bird : entities) {
         if (bird.getType() == entityType && ++sameSpecies >= birdData.misc().spawnRarity()) {
            return false;
         }
      }
      return true;
   }

   public void spawnChildFromBreeding(@NotNull ServerLevel level, @NotNull Animal mate) {
      if (mate instanceof AbstractBirdEntity<?> bird) {
         int remainingEggs = this.tryLayEggInNest(bird, this);
         if (remainingEggs > 0) {
            this.triggerBreedEggAdvancement(bird);

            for (int i = 0; i < remainingEggs; i++) {
               this.spawnEgg(bird);
            }
         }
      }

      this.setAge(this.getBirdData().misc().breedCooldown());
      mate.setAge(this.getBirdData().misc().breedCooldown());
      this.resetLove();
      mate.resetLove();
      level.broadcastEntityEvent(this, (byte)18);
      if (level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
         level.addFreshEntity(
            new ExperienceOrb(
               level,
               this.getX(),
               this.getY(),
               this.getZ(),
               this.getEggCount() * (this.getRandom().nextInt(this.getBirdData().misc().layEggExpVariance()) + this.getBirdData().misc().layEggExp())
            )
         );
      }
   }

   private int tryLayEggInNest(AbstractBirdEntity<?> male, AbstractBirdEntity<?> female) {
      BlockPos center = female.blockPosition();
      int range = this.getBirdData().misc().layEggRange();
      List<BirdNestBlockEntity> nests = new ArrayList<>();

      for (BlockPos pos : BlockPos.betweenClosed(center.offset(-range, -range, -range), center.offset(range, range, range))) {
         BlockEntity serverLevel = female.level().getBlockEntity(pos);
         if (serverLevel instanceof BirdNestBlockEntity) {
            BirdNestBlockEntity nest = (BirdNestBlockEntity)serverLevel;
            if (nest.hasEmptySlot()) {
               nests.add(nest);
            }
         }
      }

      nests.sort(Comparator.comparingDouble(nestx -> center.distSqr(nestx.getBlockPos())));
      int remainingEggs = this.getEggCount();
      List<BirdNestBlockEntity> usedNests = new ArrayList<>();
      boolean eggLaid = false;

      for (BirdNestBlockEntity nest : nests) {
         if (remainingEggs <= 0) {
            break;
         }

         boolean added;
         for (added = false; remainingEggs > 0 && nest.hasEmptySlot(); added = true) {
            nest.addEgg(this.createEgg(male));
            if (!eggLaid) {
               this.triggerBreedEggAdvancement(female);
               eggLaid = true;
            }

            remainingEggs--;
         }

         if (added) {
            usedNests.add(nest);
         }
      }

      if (female.level() instanceof ServerLevel serverLevel) {
         for (BirdNestBlockEntity nest : usedNests) {
            serverLevel.sendParticles(
               ParticleTypes.HAPPY_VILLAGER,
               (double)nest.getBlockPos().getX() + 0.5,
               (double)nest.getBlockPos().getY() + 0.375,
               (double)nest.getBlockPos().getZ() + 0.5,
               5,
               0.2,
               0.2,
               0.2,
               0.02
            );
         }
      }

      return remainingEggs;
   }

   protected ItemStack createEgg(AbstractBirdEntity<?> male) {
      BirdEggData eggData = this.createEggData(male);
      ItemStack eggStack = new ItemStack((ItemLike)BirdCameraItems.BIRD_EGG);
      BirdEggItem.setEggData(eggStack, eggData);
      return eggStack;
   }

   protected BirdEggData createEggData(AbstractBirdEntity<?> mate) {
      boolean gender = this.getBreedController().getRandomGender();
      return BirdEggData.create(
         BuiltInRegistries.ENTITY_TYPE.getKey(this.getType()),
         gender,
         this.getModelController().inheritModelVariant(mate, this, gender),
         this.getSkinController().inheritSkinVariant(mate, this, gender),
         this.getBreedController().inheritEggCount(mate, this),
         this.getFeatherController().inheritFeatherCount(mate, this),
         this.getFeatherController().inheritFeatherInterval(mate, this),
         BirdModelScale.inheritIndividualScale(this.getRandom(), mate.getIndividualModelScale(), this.getIndividualModelScale(), this.modelScaleProfile()),
         this.getBirdData().misc().eggDefaultHatchTime(),
         true
      );
   }

   private void triggerBreedEggAdvancement(AbstractBirdEntity<?> female) {
      if (female.level() instanceof ServerLevel serverLevel) {
         serverLevel.getEntitiesOfClass(ServerPlayer.class, female.getBoundingBox().inflate(16.0))
            .forEach(player -> ((BreedBirdEggTrigger)BirdCameraCriteriaTriggers.BREED_BIRD_EGG).trigger(player));
      }
   }

   public ResourceLocation getTextureResource() {
      return this.getSkinController().textureForVariant(this.getSkinController().getSkinVariant());
   }

   public ResourceLocation getModelResource() {
      return this.getModelController().modelForVariant(this.getModelController().getModelVariant());
   }

   public ResourceLocation getModelId() {
      return this.getModelController().modelForVariantId(this.getModelController().getModelVariant());
   }

   @NotNull
   public SpawnGroupData finalizeSpawn(
      @NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty, @NotNull MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData
   ) {
      SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
      this.getBreedController().randomizeGender();
      this.getBreedController().randomizeEggCount();
      this.getFeatherController().randomizeFeatherCount();
      this.getFeatherController().randomizeFeatherInterval();
      this.getModelController()
         .setModelVariant(
            this.getModelController().getRandomizeModelVariant(BirdModelRarity.COMMON, true, false, this.isBaby(), this.isMale(), !this.isMale(), false)
         );
      this.getSkinController()
         .setSkinVariant(
            this.getSkinController().getRandomizeSkinVariant(BirdSkinRarity.COMMON, true, false, this.isBaby(), this.isMale(), !this.isMale(), false)
         );
      this.getModelController().randomizeModelScale();
      return data;
   }

   @NotNull
   protected PathNavigation createNavigation(@NotNull Level level) {
      FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
      navigation.setCanFloat(false);
      navigation.setCanOpenDoors(true);
      navigation.setCanPassDoors(true);
      return navigation;
   }

   @Nullable
   public T getBreedOffspring(@NotNull ServerLevel level, @NotNull AgeableMob mate) {
      return null;
   }

   protected void spawnEgg(AbstractBirdEntity<?> mate) {
      ItemStack eggStack = this.createEgg(mate);
      ItemEntity entity = new ItemEntity(mate.level(), this.getX(), this.getY() + 0.2, this.getZ(), eggStack);
      entity.setDefaultPickUpDelay();
      mate.level().addFreshEntity(entity);
   }

   protected void defineSynchedData(@NotNull Builder builder) {
      super.defineSynchedData(builder);
      builder.define(BEHAVIOR_STATE, BirdBehaviorState.IDLE.ordinal());
      builder.define(GENDER, true);
      builder.define(SKIN_VARIANT, 0);
      builder.define(MODEL_VARIANT, 0);
      builder.define(MODEL_SCALE, 1.0F);
      builder.define(EGG_COUNT, 1);
      builder.define(FEATHER_COUNT, 1);
      builder.define(FEATHER_INTERVAL, 24000);
      builder.define(GROWTH_STOPPED, false);
   }

   public void onSyncedDataUpdated(@NotNull EntityDataAccessor<?> key) {
      if (BEHAVIOR_STATE.equals(key)) {
         this.getBehaviorStateController().decodeBehaviorState();
      }

      super.onSyncedDataUpdated(key);
   }

   public void aiStep() {
      super.aiStep();
      if (this.level().isClientSide) {
         this.getTickController().tickClient();
      } else {
         this.getTickController().tick();
         if (this.growthStopped && this.getAge() < 0) {
            this.setAge(this.getAge() - 1);
         }
      }
   }

   @NotNull
   public InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
      InteractionResult result = this.getEatingController().mobInteract(player, hand);
      return result != null ? result : super.mobInteract(player, hand);
   }

   public void handleEntityEvent(byte id) {
      this.getTameController().handleTameEvent(id);
      super.handleEntityEvent(id);
   }

   public boolean hurt(@NotNull DamageSource source, float amount) {
      boolean hurt = super.hurt(source, amount);
      if (hurt && !this.level().isClientSide) {
         this.getFrightController().processHurt(source);
      }

      return hurt;
   }

   public void addAdditionalSaveData(@NotNull CompoundTag compoundTag) {
      super.addAdditionalSaveData(compoundTag);
      compoundTag.putInt("BirdTrustTicks", this.getTickController().getTickTimer().getBirdTrustTicker().getTicks());
      compoundTag.putInt("BirdCuriousTicks", this.getTickController().getTickTimer().getBirdCuriousTicker().getTicks());
      compoundTag.putBoolean("BirdGender", this.getBreedController().getGender());
      compoundTag.putInt("BirdModelVariant", this.getModelController().getModelVariant());
      compoundTag.putInt("BirdSkinVariant", this.getSkinController().getSkinVariant());
      BirdModelScale.save(compoundTag, this.getIndividualModelScale(), this.modelScaleProfile());
      compoundTag.putInt("BirdEggCount", this.getBreedController().getEggCount());
      compoundTag.putInt("BirdFeatherCount", this.getFeatherController().getFeatherCount());
      compoundTag.putInt("BirdFeatherInterval", this.getFeatherController().getFeatherInterval());
      compoundTag.putBoolean("BirdGrowthStopped", this.isGrowthStopped());
      if (this.getTameController().getInterestedPlayerUUID() != null) {
         compoundTag.putUUID("BirdInterestedPlayer", this.getTameController().getInterestedPlayerUUID());
      }
   }

   public void readAdditionalSaveData(@NotNull CompoundTag compoundTag) {
      super.readAdditionalSaveData(compoundTag);
      this.getTickController().getTickTimer().getBirdTrustTicker().setTicks(compoundTag.getInt("BirdTrustTicks"));
      this.getTickController().getTickTimer().getBirdCuriousTicker().setTicks(compoundTag.getInt("BirdCuriousTicks"));
      if (compoundTag.contains("BirdGender", 1)) {
         this.getBreedController().setGender(compoundTag.getBoolean("BirdGender"));
      } else {
         this.getBreedController().randomizeGender();
      }

      if (compoundTag.contains("BirdModelVariant", 3)) {
         this.getModelController().setModelVariant(compoundTag.getInt("BirdModelVariant"));
      } else {
         this.getModelController()
            .setModelVariant(
               this.getModelController().getRandomizeModelVariant(BirdModelRarity.COMMON, true, false, this.isBaby(), this.isMale(), !this.isMale(), false)
            );
      }

      if (compoundTag.contains("BirdSkinVariant", 3)) {
         this.getSkinController().setSkinVariant(compoundTag.getInt("BirdSkinVariant"));
      } else {
         this.getSkinController()
            .setSkinVariant(
               this.getSkinController().getRandomizeSkinVariant(BirdSkinRarity.COMMON, true, false, this.isBaby(), this.isMale(), !this.isMale(), false)
            );
      }

      if (compoundTag.contains("BirdModelScale", 5)) {
         this.setIndividualModelScale(BirdModelScale.load(compoundTag, this.modelScaleProfile()));
      } else {
         this.getModelController().randomizeModelScale();
      }

      if (compoundTag.hasUUID("BirdInterestedPlayer")) {
         this.getTameController().setInterestedPlayerUUID(compoundTag.getUUID("BirdInterestedPlayer"));
      }

      if (compoundTag.contains("BirdEggCount", 3)) {
         this.getBreedController().setEggCount(compoundTag.getInt("BirdEggCount"));
      } else {
         this.getBreedController().randomizeEggCount();
      }

      if (compoundTag.contains("BirdFeatherCount", 3)) {
         this.getFeatherController().setFeatherCount(compoundTag.getInt("BirdFeatherCount"));
      } else {
         this.getFeatherController().randomizeFeatherCount();
      }

      if (compoundTag.contains("BirdFeatherInterval", 3)) {
         this.getFeatherController().setFeatherInterval(compoundTag.getInt("BirdFeatherInterval"));
      } else {
         this.getFeatherController().randomizeFeatherInterval();
      }

      if (compoundTag.contains("BirdGrowthStopped", 1)) {
         this.setGrowthStopped(compoundTag.getBoolean("BirdGrowthStopped"));
      } else {
         this.setGrowthStopped(false);
      }
   }

   @Override
   public BirdFlightProfile birdFlightProfile() {
      return this.BIRD_DATA.flying().flightProfile();
   }

   @Override
   public boolean isBirdFlightActive() {
      return this.getFlyingController().isBirdFlightActive();
   }

   @Override
   public boolean isBirdLanding() {
      return this.getFlyingController().isLandingFlight;
   }

   @Override
   public boolean isBirdEscaping() {
      return this.getFlyingController().isEscapeFlightActive;
   }

   @Override
   public BirdModelScaleProfile modelScaleProfile() {
      return this.getModelController().modelScaleProfile();
   }

   @Override
   public float getIndividualModelScale() {
      return this.getModelController().getIndividualModelScale();
   }

   @Override
   public void setIndividualModelScale(float scale) {
      this.getModelController().setIndividualModelScale(scale);
   }

   public boolean isFlying() {
      return this.getFlyingController().isBirdFlyingOrLanding();
   }

   @Override
   public boolean startBirdBathMountFlight(Vec3 standPosition) {
      return this.getFlyingController().startBirdBathMountFlight(standPosition);
   }

   @Override
   public void startBirdBathFeedingAnimation(BirdBathContentType contentType, int ticks) {
      this.getEatingController().startBirdBathFeedingAnimation(contentType, ticks);
   }

   public void registerControllers(ControllerRegistrar controllers) {
      this.getAnimationController().registerControllers(controllers);
   }

   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.getAnimationController().getCache();
   }

   public <E extends AbstractBirdEntity<?>> PlayState movementController(AnimationState<E> animationState) {
      RawAnimation guidePreviewRawAnimation = this.getAnimationController().getCurrentGuideAnimation();
      if (guidePreviewRawAnimation != null) {
         return animationState.setAndContinue(guidePreviewRawAnimation);
      } else {
         BirdBehaviorState state = this.getBehaviorStateController().getBehaviorState();
         BirdTickTimer<? extends AbstractBirdEntity<?>> tickTimer = (BirdTickTimer<? extends AbstractBirdEntity<?>>)this.getTickController().getTickTimer();
         if (state != BirdBehaviorState.SLEEPING) {
            this.getAnimationController().resetSleepAnimation();
         }

         if (state == BirdBehaviorState.DANCING || tickTimer.getBirdMusicTicker().getTicks() > 0) {
            return animationState.setAndContinue(this.BIRD_DATA.animation().animationMap().get("dance"));
         } else if (state == BirdBehaviorState.EATING || tickTimer.getBirdEatingTicker().getTicks() > 0) {
            return animationState.setAndContinue(this.BIRD_DATA.animation().animationMap().get("eat"));
         } else if (state == BirdBehaviorState.SLEEPING) {
            if (this.BIRD_DATA.animation().animationMap().containsKey("sleep_1")) {
               return animationState.setAndContinue(this.getAnimationController().pickSleepAnimation());
            } else {
               String sleepAnimation = tickTimer.getBirdBehaviorStateTicker().getTicks() > 0 ? "sleep" : "sleep_loop";
               return animationState.setAndContinue(this.BIRD_DATA.animation().animationMap().get(sleepAnimation));
            }
         } else if (this.getAnimationController().shouldPlayFlyAnimation()) {
            return animationState.setAndContinue(this.getAnimationController().pickFlyAnimation());
         } else {
            double deltaMovementSqr = this.getDeltaMovement().lengthSqr();
            double walkingThreshold = this.BIRD_DATA.misc().walkingSpeedThreshold();
            double runningSpeedThreshold = this.BIRD_DATA.misc().runningSpeedThreshold();
            boolean isNavigationDone = this.getNavigation().isDone();
            if (!(deltaMovementSqr > walkingThreshold) && isNavigationDone && state != BirdBehaviorState.WALKING && state != BirdBehaviorState.FORAGING) {
               if (state == BirdBehaviorState.PREENING) {
                  return animationState.setAndContinue(this.BIRD_DATA.animation().animationMap().get("preen"));
               } else if (state != BirdBehaviorState.CURIOUS && state != BirdBehaviorState.ALERT && tickTimer.getBirdCuriousTicker().getTicks() <= 0) {
                  return animationState.setAndContinue(this.getAnimationController().pickIdleAnimation());
               } else {
                  RawAnimation animation = this.BIRD_DATA.animation().animationMap().get("curious");
                  return animation != null
                     ? animationState.setAndContinue(animation)
                     : animationState.setAndContinue(this.getAnimationController().pickIdleAnimation());
               }
            } else {
               if (deltaMovementSqr > runningSpeedThreshold) {
                  RawAnimation animation = this.BIRD_DATA.animation().animationMap().get("run");
                  if (animation != null) {
                     return animationState.setAndContinue(animation);
                  }
               }

               return animationState.setAndContinue(this.BIRD_DATA.animation().animationMap().get("walk"));
            }
         }
      }
   }

   protected SoundEvent getAmbientSound() {
      return this.getRoutineController().isSleeping() ? null : this.getSoundController().getAmbientSound();
   }

   protected SoundEvent getHurtSound(@NotNull DamageSource source) {
      return this.getSoundController().getHurtSound(source);
   }

   protected SoundEvent getDeathSound() {
      return this.getSoundController().getDeathSound();
   }

   protected void playStepSound(@NotNull BlockPos pos, @NotNull BlockState blockState) {
      this.playSound(SoundEvents.CHICKEN_STEP, 0.12F, 0.9F);
   }

   public int getAmbientSoundInterval() {
      return this.getSoundController().getAmbientSoundInterval();
   }

   public float getVoicePitch() {
      return this.getSoundController().getVoicePitch();
   }

   public boolean isFood(@NotNull ItemStack itemStack) {
      return this.getEatingController().isEdibleFood(itemStack);
   }

   protected void checkFallDamage(double y, boolean onGround, @NotNull BlockState state, @NotNull BlockPos pos) {
      this.fallDistance = 0.0F;
      super.checkFallDamage(y, onGround, state, pos);
   }

   public boolean causeFallDamage(float fallDistance, float damageMultiplier, @NotNull DamageSource damageSource) {
      return false;
   }

   public BirdTickController<T> getTickController() {
      return this.BIRD_CONTROLLERS.birdTickController();
   }

   public BirdFlyingController<T> getFlyingController() {
      return this.BIRD_CONTROLLERS.birdFlyingController();
   }

   public BirdFrightController<T> getFrightController() {
      return this.BIRD_CONTROLLERS.birdFrightController();
   }

   public BirdRoutineController<T> getRoutineController() {
      return this.BIRD_CONTROLLERS.birdRoutineController();
   }

   public BirdEatingController<T> getEatingController() {
      return this.BIRD_CONTROLLERS.birdEatingController();
   }

   public BirdTameController<T> getTameController() {
      return this.BIRD_CONTROLLERS.birdTameController();
   }

   public BirdGoalController<T> getGoalController() {
      return this.BIRD_CONTROLLERS.birdGoalController();
   }

   public BirdSoundController<T> getSoundController() {
      return this.BIRD_CONTROLLERS.birdSoundController();
   }

   public BirdSkinController<T> getSkinController() {
      return this.BIRD_CONTROLLERS.birdSkinController();
   }

   public BirdFeatherController<T> getFeatherController() {
      return this.BIRD_CONTROLLERS.birdFeatherController();
   }

   public BirdModelController<T> getModelController() {
      return this.BIRD_CONTROLLERS.birdModelController();
   }

   public BirdBreedController<T> getBreedController() {
      return this.BIRD_CONTROLLERS.birdBreedController();
   }

   public BirdBehaviorStateController<T> getBehaviorStateController() {
      return this.BIRD_CONTROLLERS.birdBehaviorStateController();
   }

   public BirdAnimationController<T> getAnimationController() {
      return this.BIRD_CONTROLLERS.birdAnimationController();
   }

   public BirdFoodBagController<T> getFoodBagController() {
      return this.BIRD_CONTROLLERS.birdFoodBagController();
   }

   public BirdData getBirdData() {
      return this.BIRD_DATA;
   }

   public void setMoveControl(MoveControl control) {
      this.moveControl = control;
   }

   public boolean isDancing() {
      return this.getBirdControllers().getBirdTickController().getTickTimer().getBirdMusicTicker().getTicks() > 0
         || this.getBirdControllers().getBirdBehaviorStateController().getBehaviorState() == BirdBehaviorState.DANCING;
   }

   public void applyEggData(BirdEggData data) {
      this.getBreedController().setGender(data.gender());
      this.getBreedController().setEggCount(data.eggCount());
      this.getFeatherController().setFeatherCount(data.featherCount());
      this.getFeatherController().setFeatherInterval(data.featherInterval());
      this.getModelController().setModelVariant(data.model());
      this.getSkinController().setSkinVariant(data.skin());
      this.setIndividualModelScale(data.size());
   }

   public boolean isMale() {
      return this.getBreedController().getGender();
   }

   public void setMale(boolean male) {
      this.getBreedController().setGender(male);
   }

   public int getEggCount() {
      return this.getBreedController().getEggCount();
   }

   public void setEggCount(int i) {
      this.getBreedController().setEggCount(i);
   }

   public int getFeatherCount() {
      return this.getFeatherController().getFeatherCount();
   }

   public void setFeatherCount(int i) {
      this.getFeatherController().setFeatherCount(i);
   }

   public int getFeatherInterval() {
      return this.getFeatherController().getFeatherInterval();
   }

   public void setFeatherInterval(int i) {
      this.getFeatherController().setFeatherInterval(i);
   }

   public BirdSkin getSkin() {
      BirdData birdData = this.getBirdData();
      BirdModelSkinDatum modelDatum = birdData.model();
      return modelDatum.birdSkin().get(this.getSkinController().getSkinVariant());
   }

   public BirdModel getModel() {
      BirdData birdData = this.getBirdData();
      BirdModelSkinDatum modelDatum = birdData.model();
      return modelDatum.birdModel().get(this.getModelController().getModelVariant());
   }

   protected void applyTamingSideEffects() {
      super.applyTamingSideEffects();

      for (int i = 0; i < 9; i++) {
         double xOffset = this.getRandom().nextGaussian() * 0.03;
         double yOffset = this.getRandom().nextGaussian() * 0.04;
         double zOffset = this.getRandom().nextGaussian() * 0.03;
         this.level().addParticle(ParticleTypes.HEART, this.getX(0.7), this.getY() + 0.22, this.getZ(0.7), xOffset, yOffset + 0.035, zOffset);
      }
   }

   public boolean isGrowthStopped() {
      return this.growthStopped;
   }

   public void setGrowthStopped(boolean growthStopped) {
      this.growthStopped = growthStopped;
   }
}
