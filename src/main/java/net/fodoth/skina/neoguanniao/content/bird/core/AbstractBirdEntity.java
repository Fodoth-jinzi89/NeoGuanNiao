package net.fodoth.skina.neoguanniao.content.bird.core;

import net.fodoth.skina.neoguanniao.content.bath.BirdBathContentType;
import net.fodoth.skina.neoguanniao.content.bath.BirdBathFeedingAnimatable;
import net.fodoth.skina.neoguanniao.content.bath.BirdBathMountable;
import net.fodoth.skina.neoguanniao.content.bird.core.controller.*;
import net.fodoth.skina.neoguanniao.content.bird.core.data.BirdControllers;
import net.fodoth.skina.neoguanniao.content.bird.core.data.BirdData;
import net.fodoth.skina.neoguanniao.content.bird.core.controller.BirdTickController;
import net.fodoth.skina.neoguanniao.content.bird.core.goal.goals.*;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
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

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractBirdEntity<T extends AbstractBirdEntity<T>> extends TamableAnimal implements GeoEntity, FlyingAnimal, ScalableBirdModel, BirdFlightAware, BirdBathMountable, BirdBathFeedingAnimatable {

    // ================== 数据序列化器 ===================
    public static final EntityDataAccessor<Integer> BEHAVIOR_STATE =
            SynchedEntityData.defineId(AbstractBirdEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> SKIN_VARIANT =
            SynchedEntityData.defineId(AbstractBirdEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Float> MODEL_SCALE =
            SynchedEntityData.defineId(AbstractBirdEntity.class, EntityDataSerializers.FLOAT);

    // ==================== 常量 ========================
    protected final BirdData BIRD_DATA;
    protected final BirdControllers<T> BIRD_CONTROLLERS;

    // ==================== 变量 =======================
    protected BirdBrain birdBrain;


    protected AbstractBirdEntity(EntityType<T> entityType,
                                 Level level, BirdData birdData, BirdControllers<T> birdControllers) {
        super(entityType, level);
        this.BIRD_DATA = birdData;
        this.BIRD_CONTROLLERS = birdControllers;
        initFeatures();
        initPathfindingMalus();
    }

    /**
     * 子类需要实现此方法返回自身
     * 用于安全的类型转换
     */
    protected abstract T getSelf();

    /**
     * 初始化控制器，在子类构造完成后调用
     */
    protected void initControllers() {
        if (BIRD_CONTROLLERS != null) {
            BIRD_CONTROLLERS.attach(getSelf());
        }

        // 用于兼容坐垫之类的
        getBirdControllers().getBirdTickController().getTickTimer().getBirdLandingTicker().setTicks(100);
    }

    public BirdControllers<T> getBirdControllers() {
        return BIRD_CONTROLLERS;
    }

    protected void initFeatures() {
        if (birdBrain == null) {
            birdBrain = new BirdBrain(this, null);
        }
    }

    protected void initPathfindingMalus() {
        this.setPathfindingMalus(PathType.LEAVES, 0.0F);
        this.setPathfindingMalus(PathType.WATER, -1.0F);
        this.setPathfindingMalus(PathType.DANGER_FIRE, 16.0F);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, 16.0F);
    }


    @Override
    protected void registerGoals() {
        super.registerGoals();
        int priority = 0;
        for (Goal goal : buildGoals()) {
            goalSelector.addGoal(priority, goal);
            priority += 1;
        }
    }

    protected List<Goal> buildGoals() {
        List<Goal> goals = new ArrayList<>();

        goals.add(new FloatGoal(this)); //0
        // Subtypes should add from 1
        goals.add(new BirdEatFoodGoal(this)); //1
        goals.add(new BirdBathUseGoal(this)); //2
        goals.add(new BirdSentinelGoal(this)); //3
        goals.add(new BirdWakeUpGoal(this)); //4
        goals.add(new BirdRoostGoal(this)); //5
        goals.add(new BirdFollowOwnerGoal(this)); //6
        goals.add(new BirdFlockGoal(this));  //7
        goals.add(new BirdCuriousFollowGoal(this)); //8
        goals.add(new BirdIdleGoal(this)); //9
        goals.add(new BirdRandomLookAroundGoal(this)); //10

        return goals;
    }
    /**
     * 生成规则由 {@link NeoGuanNiaoModEvents} 注册。
     */
    public static boolean canSpawn(EntityType<? extends AbstractBirdEntity<?>> entityType, ServerLevelAccessor level,
                                   MobSpawnType spawnType, BlockPos pos, RandomSource random, BirdData birdData) {
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

    public ResourceLocation getTextureResource() {
        return getModelController().textureForVariant(getModelController().getSkinVariant());
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

        getModelController().randomizeSkinVariant();
        getModelController().randomizeModelScale();

        return data;
    }

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
                child.getModelController().inheritSkinVariant(
                        this,
                        other
                );
            } else {
                child.getModelController().randomizeSkinVariant();
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
            getBehaviorStateController().decodeBehaviorState();
        }
        super.onSyncedDataUpdated(key);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) {
            getTickController().tickClient();
        } else {
            birdBrain.tick();
            getTickController().tick();
        }
    }

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        InteractionResult result = getEatingController().mobInteract(player, hand);
        if (result != null) {
            return result;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void handleEntityEvent(byte id) {
        getTameController().handleTameEvent(id);
        super.handleEntityEvent(id);
    }


    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && !this.level().isClientSide) {
            getFrightController().processHurt(source);
        }
        return hurt;
    }


    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        birdBrain.save(compoundTag);
        compoundTag.putInt("BirdTrustTicks", getTickController().getTickTimer().getBirdTrustTicker().getTicks());
        compoundTag.putInt("BirdCuriousTicks", getTickController().getTickTimer().getBirdCuriousTicker().getTicks());
        compoundTag.putInt("BirdSkinVariant", getModelController().getSkinVariant());
        BirdModelScale.save(compoundTag, this.getIndividualModelScale(), this.modelScaleProfile());
        if (getTameController().getInterestedPlayerUUID() != null) {
            compoundTag.putUUID("BirdInterestedPlayer", getTameController().getInterestedPlayerUUID());
        }
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        birdBrain.load(compoundTag);
        getTickController().getTickTimer().getBirdTrustTicker().setTicks(compoundTag.getInt("BirdTrustTicks"));
        getTickController().getTickTimer().getBirdCuriousTicker().setTicks(compoundTag.getInt("BirdCuriousTicks"));
        if (compoundTag.contains("BirdSkinVariant", CompoundTag.TAG_INT)) {
            getModelController().setSkinVariant(compoundTag.getInt("BirdSkinVariant"));
        } else {
            getModelController().randomizeSkinVariant();
        }
        if (compoundTag.contains("BirdModelScale", CompoundTag.TAG_FLOAT)) {
            this.setIndividualModelScale(BirdModelScale.load(compoundTag, this.modelScaleProfile()));
        } else {
            getModelController().randomizeModelScale();
        }
        if (compoundTag.hasUUID("BirdInterestedPlayer")) {
            getTameController().setInterestedPlayerUUID(compoundTag.getUUID("BirdInterestedPlayer"));
        }
    }


    @Override
    public BirdFlightProfile birdFlightProfile() {
        return BIRD_DATA.flying().flightProfile();
    }

    @Override
    public boolean isBirdFlightActive() {
        return getFlyingController().isBirdFlightActive();
    }

    @Override
    public boolean isBirdLanding() {
        return getFlyingController().isLandingFlight;
    }

    @Override
    public boolean isBirdEscaping() {
        return getFlyingController().isEscapeFlightActive;
    }

    @Override
    public BirdModelScaleProfile modelScaleProfile() {
        return getModelController().modelScaleProfile();
    }

    @Override
    public float getIndividualModelScale() {
        return getModelController().getIndividualModelScale();
    }

    @Override
    public void setIndividualModelScale(float scale) {
        getModelController().setIndividualModelScale(scale);
    }

    @Override
    public boolean isFlying() {
        return getFlyingController().isBirdFlyingOrLanding();
    }

    @Override
    public boolean startBirdBathMountFlight(Vec3 standPosition) {
        return getFlyingController().startBirdBathMountFlight(standPosition);
    }

    @Override
    public void startBirdBathFeedingAnimation(BirdBathContentType contentType, int ticks) {
        getEatingController().startBirdBathFeedingAnimation(contentType, ticks);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        getAnimationController().registerControllers(controllers);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return getAnimationController().getCache();
    }

    /**
     * 控制实体动画状态机，根据行为状态和计时器决定播放哪种动画
     *
     * @param animationState 动画状态对象，用于设置和切换动画
     * @return 播放状态，包含要播放的动画
     */
    public <E extends AbstractBirdEntity<?>> PlayState movementController(AnimationState<E> animationState) {
        // 优先检查是否有引导预览动画（外部强制指定的动画）
        RawAnimation guidePreviewRawAnimation = getAnimationController().getCurrentGuideAnimation();
        if (guidePreviewRawAnimation != null) {
            return animationState.setAndContinue(guidePreviewRawAnimation);
        }

        BirdBehaviorState state = getBehaviorStateController().getBehaviorState();
        var tickTimer = getTickController().getTickTimer();

        // 舞蹈动画条件：只有在非舞蹈状态且音乐计时器已归零时，才进入常规动画逻辑
        // 否则直接播放舞蹈动画
        if (state != BirdBehaviorState.DANCING && tickTimer.getBirdMusicTicker().getTicks() <= 0) {

            // 进食动画条件：进食状态中或进食计时器未归零
            if (state != BirdBehaviorState.EATING && tickTimer.getBirdEatingTicker().getTicks() <= 0) {

                // 睡眠动画：根据行为计时器判断是进入睡眠还是睡眠循环
                if (state == BirdBehaviorState.SLEEPING) {
                    String sleepAnimation = tickTimer.getBirdBehaviorStateTicker().getTicks() > 0
                            ? "sleep" : "sleep_loop";
                    return animationState.setAndContinue(BIRD_DATA.animation().animationMap().get(sleepAnimation));
                }

                // 飞行动画：由飞行条件控制器决定
                if (getAnimationController().shouldPlayFlyAnimation()) {
                    return animationState.setAndContinue(BIRD_DATA.animation().animationMap().get("fly"));
                }


                // 地面移动逻辑
                double deltaMovementSqr = this.getDeltaMovement().lengthSqr();
                double walkingThreshold = BIRD_DATA.misc().walkingSpeedThreshold();
                boolean isNavigationDone = this.getNavigation().isDone();

                // 静止状态：移动速度低于阈值、导航结束且不是行走状态
                if ((!(deltaMovementSqr > walkingThreshold) && isNavigationDone && state != BirdBehaviorState.WALKING && state != BirdBehaviorState.FORAGING)) {

                    // 梳理羽毛
                    if (state == BirdBehaviorState.PREENING) {
                        return animationState.setAndContinue(BIRD_DATA.animation().animationMap().get("preen"));
                    }

                    // 空闲动画：非好奇、非警觉状态且好奇计时器归零时播放
                    if (state != BirdBehaviorState.CURIOUS && state != BirdBehaviorState.ALERT
                            && tickTimer.getBirdCuriousTicker().getTicks() <= 0) {
                        return animationState.setAndContinue(getAnimationController().pickIdleAnimation());
                    }

                    // 好奇动画（默认的静止状态动画）
                    return animationState.setAndContinue(BIRD_DATA.animation().animationMap().get("curious"));
                }

                // 行走动画（移动中）
                return animationState.setAndContinue(BIRD_DATA.animation().animationMap().get("walk"));
            }

            // 进食动画（进食状态或进食计时器未归零）
            return animationState.setAndContinue(BIRD_DATA.animation().animationMap().get("eat"));
        }

        // 舞蹈动画（音乐计时器运行中或处于舞蹈状态）
        return animationState.setAndContinue(BIRD_DATA.animation().animationMap().get("dance"));
    }


    @Override
    protected SoundEvent getAmbientSound() {
        if (getRoutineController().isSleeping()) {
            return null; // 大多数鸟睡觉都不会打呼噜，对吧？
        }
        return getSoundController().getAmbientSound();
    }

    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource source) {
        return getSoundController().getHurtSound(source);
    }

    @Override
    protected SoundEvent getDeathSound() {
        return getSoundController().getDeathSound();
    }

    @Override
    public int getAmbientSoundInterval() {
        return getSoundController().getAmbientSoundInterval();
    }

    @Override
    public float getVoicePitch() {
        return getSoundController().getVoicePitch();
    }

    @Override
    public boolean isFood(@NotNull ItemStack itemStack) {
        return getEatingController().isEdibleFood(itemStack);
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, @NotNull BlockState state, @NotNull BlockPos pos) {
        this.fallDistance = 0.0F;
        super.checkFallDamage(y, onGround, state, pos);
    }

    public BirdTickController<T> getTickController() {
        return BIRD_CONTROLLERS.birdTickController();
    }

    public BirdFlyingController<T> getFlyingController() {
        return BIRD_CONTROLLERS.birdFlyingController();
    }

    public BirdFrightController<T> getFrightController() {
        return BIRD_CONTROLLERS.birdFrightController();
    }

    public BirdRoutineController<T> getRoutineController() {
        return BIRD_CONTROLLERS.birdRoutineController();
    }

    public BirdEatingController<T> getEatingController() {
        return BIRD_CONTROLLERS.birdEatingController();
    }

    public BirdTameController<T> getTameController() {
        return BIRD_CONTROLLERS.birdTameController();
    }

    public BirdGoalController<T> getGoalController() {
        return BIRD_CONTROLLERS.birdGoalController();
    }

    public BirdSoundController<T> getSoundController() {
        return BIRD_CONTROLLERS.birdSoundController();
    }

    public BirdSkinController<T> getModelController() {
        return BIRD_CONTROLLERS.birdModelController();
    }

    public BirdBehaviorStateController<T> getBehaviorStateController() {
        return BIRD_CONTROLLERS.birdBehaviorStateController();
    }

    public BirdAnimationController<T> getAnimationController() {
        return BIRD_CONTROLLERS.birdAnimationController();
    }

    public BirdData getBirdData() {
        return BIRD_DATA;
    }

    public BirdBrain getBirdBrain() {
        return birdBrain;
    }

    public void setMoveControl(MoveControl control) {
        this.moveControl = control;
    }


    public boolean isDancing() {
        return this.getBirdControllers().getBirdTickController().getTickTimer().getBirdMusicTicker().getTicks() > 0
                || this.getBirdControllers().getBirdBehaviorStateController().getBehaviorState() == BirdBehaviorState.DANCING;
    }
}
