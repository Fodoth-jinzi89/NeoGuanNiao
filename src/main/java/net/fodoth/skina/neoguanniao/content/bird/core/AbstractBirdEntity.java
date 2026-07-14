package net.fodoth.skina.neoguanniao.content.bird.core;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.content.bath.BirdBathContentType;
import net.fodoth.skina.neoguanniao.content.bath.BirdBathFeedingAnimatable;
import net.fodoth.skina.neoguanniao.content.bath.BirdBathMountable;
import net.fodoth.skina.neoguanniao.content.bird.core.controller.*;
import net.fodoth.skina.neoguanniao.content.bird.core.data.BirdData;
import net.fodoth.skina.neoguanniao.content.bird.core.controller.BirdTickController;
import net.fodoth.skina.neoguanniao.content.bird.feature.brain.BirdBrain;
import net.fodoth.skina.neoguanniao.content.bird.feature.flight.*;
import net.fodoth.skina.neoguanniao.content.bird.feature.scale.BirdModelScale;
import net.fodoth.skina.neoguanniao.content.bird.feature.scale.BirdModelScaleProfile;
import net.fodoth.skina.neoguanniao.content.bird.feature.scale.ScalableBirdModel;
import net.fodoth.skina.neoguanniao.content.bird.core.controller.BirdTameController;
import net.fodoth.skina.neoguanniao.event.NeoGuanNiaoModEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.animation.AnimationState;

public abstract class AbstractBirdEntity<T extends AbstractBirdEntity<T>> extends TamableAnimal implements GeoEntity, FlyingAnimal, ScalableBirdModel, BirdFlightAware, BirdBathMountable, BirdBathFeedingAnimatable {

    // ================== 数据序列化器 ===================
    public static final EntityDataAccessor<Integer> BEHAVIOR_STATE =
            SynchedEntityData.defineId(AbstractBirdEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> SKIN_VARIANT =
            SynchedEntityData.defineId(AbstractBirdEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Float> MODEL_SCALE =
            SynchedEntityData.defineId(AbstractBirdEntity.class, EntityDataSerializers.FLOAT);

    // ==================== 常量 ========================
    protected static BirdData birdData;

    // ==================== 变量 =======================
    protected BirdBrain birdBrain;

    // ========= 控制器 =========
    protected BirdTickController birdTickController;
    protected BirdFlyingController birdFlyingController;
    protected BirdRoutineController birdRoutineController;
    protected BirdEatingController birdEatingController;
    protected BirdTameController birdTameController;
    protected BirdGoalController birdGoalController;
    protected BirdFrightController birdFrightController;
    protected BirdSoundController birdSoundController;
    protected BirdAnimationController birdAnimationController;
    protected BirdModelController birdModelController;
    protected BirdBehaviorStateController birdBehaviorStateController;


    // ============ 构造方法 ============
    public AbstractBirdEntity(EntityType<T> entityType, Level level) {
        super(entityType, level);
        this.initFeatures();
        this.initControllers();
        this.initPathfindingMalus();
    }

    private void initFeatures() {
        birdData = BirdData.createDefault();
        this.birdBrain = new BirdBrain(this, null);
    }

    private void initControllers() {
        this.birdTickController = new BirdTickController(this);
        this.birdFlyingController = new BirdFlyingController(this);
        this.birdRoutineController = new BirdRoutineController(this);
        this.birdEatingController = new BirdEatingController(this);
        this.birdTameController = new BirdTameController(this);
        this.birdGoalController = new BirdGoalController(this);
        this.birdFrightController = new BirdFrightController(this);
        this.birdSoundController = new BirdSoundController(this);
        this.birdAnimationController = new BirdAnimationController(this);
        this.birdModelController = new BirdModelController(this);
        this.birdBehaviorStateController = new BirdBehaviorStateController(this);
    }

    protected void initPathfindingMalus() {
        this.setPathfindingMalus(PathType.LEAVES, 0.0F);
        this.setPathfindingMalus(PathType.WATER, -1.0F);
        this.setPathfindingMalus(PathType.DANGER_FIRE, 16.0F);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, 16.0F);
    }

    protected static AttributeSupplier.Builder createAttributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.FLYING_SPEED, 0.30)
                .add(Attributes.FOLLOW_RANGE, 15.0);
    }

    /**
     * 生成规则由 {@link NeoGuanNiaoModEvents} 注册。
     */
    public static boolean canSpawn(EntityType<AbstractBirdEntity<?>> entityType, ServerLevelAccessor level,
                                   MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        BlockState below = level.getBlockState(pos.below());
        boolean validGround = below.is(BlockTags.DIRT)
                || below.is(BlockTags.SAND)
                || below.is(Blocks.GRASS_BLOCK)
                || below.is(Blocks.DIRT_PATH)
                || below.is(Blocks.FARMLAND);

        if (!validGround) {
            return false;
        }

        // 获取附近的实体列表
        var entities = level.getEntitiesOfClass(AbstractBirdEntity.class,
                new AABB(pos.getX() - 8, pos.getY() - 4, pos.getZ() - 8,
                        pos.getX() + 8, pos.getY() + 4, pos.getZ() + 8));

        return entities.size() <= birdData.misc().spawnRarity();
    }




    protected abstract @Nullable T createChild(ServerLevel level);

    protected void initializeChild(T child) {
    }

    @Override
    public @NotNull SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level,
                                                 @NotNull DifficultyInstance difficulty,
                                                 @NotNull MobSpawnType spawnType,
                                                 @Nullable SpawnGroupData spawnGroupData) {
        SpawnGroupData data = super.finalizeSpawn(
                level,
                difficulty,
                spawnType,
                spawnGroupData
        );

        this.birdModelController.randomizeSkinVariant();

        birdModelController.randomizeModelScale();

        return data;
    }

    @Override
    protected abstract void registerGoals();

    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanFloat(false);
        navigation.setCanOpenDoors(true);
        navigation.setCanPassDoors(true);
        return navigation;
    }

    @Override
    public @Nullable T getBreedOffspring(
            @NotNull ServerLevel level,
            @NotNull AgeableMob mate
    ) {
        T child = this.createChild(level);

        if (child != null) {
            this.initializeChild(child);

            if (mate instanceof AbstractBirdEntity<?> other) {
                child.birdModelController.inheritSkinVariant(
                        this,
                        other
                );
            } else {
                child.birdModelController.randomizeSkinVariant();
            }


            float mateScale = mate instanceof AbstractBirdEntity<?> other
                    ? other.getIndividualModelScale()
                    : this.getIndividualModelScale();

            child.setIndividualModelScale(
                    BirdModelScale.inheritIndividualScale(
                            child.getRandom(),
                            this.getIndividualModelScale(),
                            mateScale,
                            child.modelScaleProfile()
                    )
            );
        }

        return child;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(BEHAVIOR_STATE, BirdBehaviorState.IDLE.ordinal());
        builder.define(SKIN_VARIANT, 0);
        builder.define(MODEL_SCALE, 1.0F);
    }

    @Override
    public void onSyncedDataUpdated(@NotNull EntityDataAccessor<?> key) {
        if (BEHAVIOR_STATE.equals(key)) {
            birdBehaviorStateController.decodeBehaviorState();
        }
        super.onSyncedDataUpdated(key);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) {
            birdTickController.tickClient();
        } else {
            birdBrain.tick();
            birdTickController.tick();
        }
    }

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        InteractionResult result = birdEatingController.mobInteract(player, hand);
        if (result != null) {
            return result;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void handleEntityEvent(byte id) {
        birdTameController.handleTameEvent(id);
        super.handleEntityEvent(id);
    }


    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && !this.level().isClientSide) {
            birdFrightController.processHurt(source);
        }
        return hurt;
    }


    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        birdBrain.save(compoundTag);
        compoundTag.putInt("BirdTrustTicks", birdTickController.getTickTimer().getBirdTrustTicker().getTicks());
        compoundTag.putInt("BirdCuriousTicks", birdTickController.getTickTimer().getBirdCuriousTicker().getTicks());
        compoundTag.putInt("BirdSkinVariant", birdModelController.getSkinVariant());
        BirdModelScale.save(compoundTag, this.getIndividualModelScale(), this.modelScaleProfile());
        if (birdTameController.getInterestedPlayerUUID() != null) {
            compoundTag.putUUID("BirdInterestedPlayer", birdTameController.getInterestedPlayerUUID());
        }
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        birdBrain.load(compoundTag);
        birdTickController.getTickTimer().getBirdTrustTicker().setTicks(compoundTag.getInt("BirdTrustTicks"));
        birdTickController.getTickTimer().getBirdCuriousTicker().setTicks(compoundTag.getInt("BirdCuriousTicks"));
        if (compoundTag.contains("BirdSkinVariant", CompoundTag.TAG_INT)) {
            birdModelController.setSkinVariant(compoundTag.getInt("BirdSkinVariant"));
        } else {
            NeoGuanNiao.LOGGER.warn("[NeoGuanNiao] BirdSkinVariant compound tag is missing! + Entity: {}", this.getStringUUID());
            birdModelController.randomizeSkinVariant();
        }
        if (compoundTag.contains("BirdModelScale", CompoundTag.TAG_FLOAT)) {
            this.setIndividualModelScale(BirdModelScale.load(compoundTag, this.modelScaleProfile()));
        } else {
            NeoGuanNiao.LOGGER.warn("[NeoGuanNiao] BirdModelScale compound tag is missing! + Entity: {}", this.getStringUUID());
            birdModelController.randomizeModelScale();
        }
        if (compoundTag.hasUUID("BirdInterestedPlayer")) {
            birdTameController.setInterestedPlayerUUID(compoundTag.getUUID("BirdInterestedPlayer"));
        }
    }


    @Override
    public BirdFlightProfile birdFlightProfile() {
        return birdData.flying().flightProfile();
    }

    @Override
    public boolean isBirdFlightActive() {
        return birdFlyingController.isBirdFlightActive();
    }

    @Override
    public boolean isBirdLanding() {
        return birdFlyingController.isLandingFlight;
    }

    @Override
    public boolean isBirdEscaping() {
        return birdFlyingController.isEscapeFlightActive;
    }

    @Override
    public BirdModelScaleProfile modelScaleProfile() {
        return birdModelController.modelScaleProfile();
    }

    @Override
    public float getIndividualModelScale() {
        return birdModelController.getIndividualModelScale();
    }

    @Override
    public void setIndividualModelScale(float scale) {
        birdModelController.setIndividualModelScale(scale);
    }

    @Override
    public boolean isFlying() {
        return birdFlyingController.isBirdFlying();
    }

    @Override
    public boolean startBirdBathMountFlight(Vec3 standPosition) {
        return birdFlyingController.startBirdBathMountFlight(standPosition);
    }

    @Override
    public void startBirdBathFeedingAnimation(BirdBathContentType contentType, int ticks) {
        birdEatingController.startBirdBathFeedingAnimation(contentType, ticks);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        birdAnimationController.registerControllers(controllers);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return birdAnimationController.cache;
    }

    public <E extends AbstractBirdEntity<?>> PlayState movementController(AnimationState<E> animationState) {
        return animationState.setAndContinue(getBirdData().animation().guidePreviewAnimation().animation());
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return birdSoundController.getAmbientSound();
    }

    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource source) {
        return birdSoundController.getHurtSound(source);
    }

    @Override
    protected SoundEvent getDeathSound() {
        return birdSoundController.getDeathSound();
    }

    @Override
    public int getAmbientSoundInterval() {
        return birdSoundController.getAmbientSoundInterval();
    }

    @Override
    public float getVoicePitch() {
        return birdSoundController.getVoicePitch();
    }

    @Override
    public boolean isFood(@NotNull ItemStack itemStack) {
        return birdEatingController.isEdibleFood(itemStack);
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, @NotNull BlockState state, @NotNull BlockPos pos) {
        this.fallDistance = 0.0F;
        super.checkFallDamage(y, onGround, state, pos);
    }

    public BirdTickController getTickController() {
        return birdTickController;
    }

    public BirdFlyingController getFlyingController() {
        return birdFlyingController;
    }

    public BirdFrightController getFrightController() {
        return birdFrightController;
    }

    public BirdRoutineController getRoutineController() {
        return birdRoutineController;
    }

    public BirdEatingController getEatingController() {
        return birdEatingController;
    }

    public BirdTameController getTameController() {
        return birdTameController;
    }

    public BirdGoalController getGoalController() {
        return birdGoalController;
    }

    public BirdSoundController getSoundController() {
        return birdSoundController;
    }

    public BirdModelController getModelController() {
        return birdModelController;
    }

    public BirdBehaviorStateController getBehaviorStateController() {
        return birdBehaviorStateController;
    }

    public BirdData getBirdData() {
        return birdData;
    }

    public BirdBrain getBirdBrain() {
        return birdBrain;
    }

    public void setMoveControl(MoveControl control) {
        this.moveControl = control;
    }

}
