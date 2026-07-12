package net.fodoth.skina.neoguanniao.content.bird.nightheron;

import net.fodoth.skina.neoguanniao.content.bath.BirdBathAttraction;
import net.fodoth.skina.neoguanniao.content.bath.BirdBathBlockEntity;
import net.fodoth.skina.neoguanniao.content.bath.BirdBathContentType;
import net.fodoth.skina.neoguanniao.content.bath.BirdBathFeedingAnimatable;
import net.fodoth.skina.neoguanniao.content.bath.BirdBathMountable;
import net.fodoth.skina.neoguanniao.content.bath.BirdBathUseGoal;
import net.fodoth.skina.neoguanniao.content.bird.brain.BirdBrain;
import net.fodoth.skina.neoguanniao.content.bird.flight.BirdFlightAware;
import net.fodoth.skina.neoguanniao.content.bird.flight.BirdFlightController;
import net.fodoth.skina.neoguanniao.content.bird.flight.BirdFlightProfile;
import net.fodoth.skina.neoguanniao.content.bird.nightheron.goal.*;
import net.fodoth.skina.neoguanniao.content.bird.scale.BirdModelScale;
import net.fodoth.skina.neoguanniao.content.bird.scale.BirdModelScaleProfile;
import net.fodoth.skina.neoguanniao.content.bird.scale.ScalableBirdModel;
import net.fodoth.skina.neoguanniao.content.bird.species.NightHeronProfile;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
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
import software.bernie.geckolib.util.GeckoLibUtil;

import static net.fodoth.skina.neoguanniao.content.bird.nightheron.NightHeronDefinition.*;

public class NightHeronEntity extends PathfinderMob implements GeoEntity, FlyingAnimal, ScalableBirdModel, BirdFlightAware, BirdBathMountable, BirdBathFeedingAnimatable {

    // ============ 数据序列化器 ============
    private static final EntityDataAccessor<Integer> BEHAVIOR_STATE =
            SynchedEntityData.defineId(NightHeronEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> MODEL_SCALE =
            SynchedEntityData.defineId(NightHeronEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<ItemStack> HELD_FISH =
            SynchedEntityData.defineId(NightHeronEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Integer> EATING_TICKS =
            SynchedEntityData.defineId(NightHeronEntity.class, EntityDataSerializers.INT);

    // ============ 诱惑物品 ============
    static final Ingredient TEMPT_ITEMS = Ingredient.of(Items.COD, Items.SALMON, Items.COOKED_COD, Items.COOKED_SALMON);

    // ============ 动画定义 ============
    protected static final RawAnimation IDLE_ANIMATION = RawAnimation.begin().thenLoop("idle");
    protected static final RawAnimation IDLE_DIFF_1_ANIMATION = RawAnimation.begin().thenPlay("idle_diff_1").thenLoop("idle");
    protected static final RawAnimation IDLE_DIFF_2_ANIMATION = RawAnimation.begin().thenPlay("idle_diff_2").thenLoop("idle");
    protected static final RawAnimation IDLE_DIFF_3_ANIMATION = RawAnimation.begin().thenPlay("idle_diff_3").thenLoop("idle");
    protected static final RawAnimation IDLE_DIFF_4_ANIMATION = RawAnimation.begin().thenPlay("idle_diff_4").thenLoop("idle");
    protected static final RawAnimation IDLE_DIFF_5_ANIMATION = RawAnimation.begin().thenPlay("idle_diff_5").thenLoop("idle");
    protected static final RawAnimation WALK_ANIMATION = RawAnimation.begin().thenLoop("walk");
    protected static final RawAnimation RUN_ANIMATION = RawAnimation.begin().thenLoop("run");
    protected static final RawAnimation FLY_LOOP_ANIMATION = RawAnimation.begin().thenLoop("fly_loop");
    protected static final RawAnimation FLY_FLAPPING_WING_ANIMATION = RawAnimation.begin().thenPlay("fly_flapping_wing").thenLoop("fly_flapping_wing_loop");
    protected static final RawAnimation FLY_FLAPPING_WING_LOOP_ANIMATION = RawAnimation.begin().thenLoop("fly_flapping_wing_loop");
    protected static final RawAnimation EAT_ANIMATION = RawAnimation.begin().thenPlay("eat").thenLoop("idle");

    // ============ 时间常量 ============
    private static final int ACTIVE_START_TIME = 11000;
    private static final int ACTIVE_END_TIME = 1500;
    private static final int WATER_SEARCH_RADIUS = 8;
    private static final double WALKING_SPEED_THRESHOLD = 0.0025;
    private static final double RUNNING_SPEED_THRESHOLD = 0.018;
    private static final double FLYING_SPEED_THRESHOLD = 0.03;
    private static final float FLIGHT_YAW_TURN_RATE = 10.0F;
    private static final float FLIGHT_PITCH_TURN_RATE = 6.0F;

    // ============ 成员变量 ============
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    private final BirdBrain birdBrain;
    private NightHeronBehaviorState behaviorState;
    private NightHeronIdleAnimationChoice currentIdleAnimation;
    private long nextIdleAnimationSwapTick;
    private int forcedIdleAnimationTicks;
    private int takeoffFlapTicks;
    private int frightMemoryTicks;
    private int recentFrightCount;
    private int externalFrightTicks;
    private boolean severeExternalFright;
    private Vec3 externalFrightSource;
    private int preyStrikeCooldown;
    private int thrownFishEatCooldown;
    private int controlledFlightTicks;
    private int groundedAirborneTicks;
    private int blockedFlightRecoveryActivityTicks;
    private int obstructedFlightTicks;
    private Vec3 lastControlledFlightPosition;
    private int blockedFlightRecoveryDirectionTicks;
    private Vec3 blockedFlightRecoveryDirection;
    private BlockPos landingApproachTarget;
    private Vec3 landingApproachDirection;
    private NightHeronGuidePreviewAnimation guidePreviewAnimation;
    private int flybyFlightTicks;
    private Vec3 flybyFlightDirection;
    private BlockPos flybyLandingTarget;

    // ============ 构造方法 ============
    public NightHeronEntity(EntityType<? extends NightHeronEntity> entityType, Level level) {
        super(entityType, level);
        this.birdBrain = new BirdBrain(this, NightHeronProfile.INSTANCE);
        this.behaviorState = NightHeronBehaviorState.IDLE;
        this.currentIdleAnimation = NightHeronIdleAnimationChoice.BASE;
        this.lastControlledFlightPosition = Vec3.ZERO;
        this.blockedFlightRecoveryDirection = Vec3.ZERO;
        this.landingApproachDirection = Vec3.ZERO;
        this.guidePreviewAnimation = NightHeronGuidePreviewAnimation.NONE;
        this.flybyFlightDirection = Vec3.ZERO;
        this.setPathfindingMalus(PathType.LEAVES, 0.0F);
        this.setPathfindingMalus(PathType.WATER, -1.0F);
        this.setPathfindingMalus(PathType.WATER_BORDER, 0.0F);
    }

    // ============ 数据序列化 ============
    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(BEHAVIOR_STATE, NightHeronBehaviorState.IDLE.ordinal());
        builder.define(MODEL_SCALE, 1.0F);
        builder.define(HELD_FISH, ItemStack.EMPTY);
        builder.define(EATING_TICKS, 0);
    }

    @Override
    public void onSyncedDataUpdated(@NotNull EntityDataAccessor<?> key) {
        if (BEHAVIOR_STATE.equals(key)) {
            this.behaviorState = decodeBehaviorState(this.getEntityData().get(BEHAVIOR_STATE));
        }
        super.onSyncedDataUpdated(key);
    }

    // ============ 属性 ============
    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, WALK_SPEED)
                .add(Attributes.FLYING_SPEED, FLY_SPEED)
                .add(Attributes.FOLLOW_RANGE, FOLLOW_RANGE)
                .add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE);
    }

    // ============ 生成 ============
    public static boolean canSpawn(EntityType<NightHeronEntity> entityType, ServerLevelAccessor level,
                                   MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        BlockState below = level.getBlockState(pos.below());
        boolean validGround = below.is(BlockTags.DIRT) || below.is(Blocks.WATER)
                || below.is(Blocks.SEAGRASS) || below.is(Blocks.GRASS_BLOCK) || below.is(Blocks.DIRT_PATH);

        if (!validGround) {
            return false;
        }

        // 使用 AABB 检查附近同类型实体数量
        AABB searchBox = new AABB(pos).inflate(8.0, 4.0, 8.0);
        int entityCount = level.getEntitiesOfClass(NightHeronEntity.class, searchBox).size();
        if (entityCount > 8) {
            return false;
        }

        return isNearWaterForWorldgen(level, pos, WATER_SEARCH_RADIUS);
    }

    @Override
    @SuppressWarnings("all")
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

        this.randomizeModelScale();

        return data;
    }

    // ============ AI 注册 ============
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new NightHeronFrightGoal(this));
        this.goalSelector.addGoal(3, new NightHeronEatThrownFishGoal(this));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.0, TEMPT_ITEMS, false));
        this.goalSelector.addGoal(5, new BirdBathUseGoal(
                this, 1.0, 13.0, 42,
                BirdBathAttraction::isAttractiveToNightHeron,
                this::canUseBirdBath,
                (bath) -> this.setBehaviorState(NightHeronBehaviorState.FORAGING),
                this::consumeBirdBathServing,
                (bath, consumed) -> {
                    if (!this.isEatingFish() && this.getBehaviorState() == NightHeronBehaviorState.FORAGING) {
                        this.setBehaviorState(NightHeronBehaviorState.IDLE);
                    }
                }
        ));
        this.goalSelector.addGoal(6, new NightHeronForagingGoal(this));
        this.goalSelector.addGoal(7, new NightHeronHighTransitGoal(this));
        this.goalSelector.addGoal(8, new NightHeronAmbientFlightGoal(this));
        this.goalSelector.addGoal(9, new NightHeronRoostFlightGoal(this));
        this.goalSelector.addGoal(10, new NightHeronRoostGoal(this));
        this.goalSelector.addGoal(11, new NightHeronFlockGoal(this));
        this.goalSelector.addGoal(12, new NightHeronIdleGoal(this));
        this.goalSelector.addGoal(13, new RandomLookAroundGoal(this));
    }

    // ============ Tick ============
    @Override
    public void aiStep() {
        super.aiStep();
        if (this.isControlledFlightActive()) {
            this.xxa = 0.0F;
        }

        if (!this.level().isClientSide) {
            this.birdBrain.tick();
            this.tickStaleFlightRecovery();
            this.tickFlightStateGuard();
            this.tickTakeoffFlap();
            this.tickFrightMemory();
            this.tickExternalFright();
            this.tickCooldowns();
            this.tickFlybyFlight();
            this.tickEatingFish();
            this.tickWaterEscape();
            this.tickBlockedFlightRecovery();
            this.tickForcedIdleAnimation();
        }

        if (this.getBehaviorState().isAirborne()) {
            this.faceMovementDirection(this.getDeltaMovement());
        } else {
            this.tickGroundMovementFacing();
        }
    }

    // ============ Tick 辅助方法 ============
    private void tickTakeoffFlap() {
        if (this.takeoffFlapTicks > 0) {
            --this.takeoffFlapTicks;
        }
    }

    private void tickFrightMemory() {
        if (this.frightMemoryTicks > 0) {
            --this.frightMemoryTicks;
        } else {
            this.recentFrightCount = 0;
        }
    }

    private void tickExternalFright() {
        if (this.externalFrightTicks > 0) {
            --this.externalFrightTicks;
        } else {
            this.externalFrightSource = null;
            this.severeExternalFright = false;
        }
    }

    private void tickCooldowns() {
        if (this.preyStrikeCooldown > 0) --this.preyStrikeCooldown;
        if (this.thrownFishEatCooldown > 0) --this.thrownFishEatCooldown;
    }

    private void tickBlockedFlightRecovery() {
        if (this.blockedFlightRecoveryActivityTicks > 0) {
            --this.blockedFlightRecoveryActivityTicks;
        }
        if (this.blockedFlightRecoveryDirectionTicks > 0) {
            --this.blockedFlightRecoveryDirectionTicks;
            if (this.blockedFlightRecoveryDirectionTicks <= 0) {
                this.blockedFlightRecoveryDirection = Vec3.ZERO;
            }
        }
    }

    private void tickForcedIdleAnimation() {
        if (this.forcedIdleAnimationTicks > 0) {
            --this.forcedIdleAnimationTicks;
        }
    }

    // ============ 受伤 ============
    @Override
    public boolean hurt(@NotNull DamageSource damageSource, float amount) {
        boolean hurt = super.hurt(damageSource, amount);
        if (hurt) {
            this.clearEatingFish();
            Entity attacker = damageSource.getEntity();
            this.receiveFlockFright(attacker != null ? attacker.position() : this.position(), true);
            this.rememberFright(true);
        }
        return hurt;
    }

    // ============ NBT ============
    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putBoolean("NoGravity", false);
        this.birdBrain.save(compoundTag);
        BirdModelScale.save(compoundTag, this.getIndividualModelScale(), this.modelScaleProfile());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.clearSerializedFlightState();
        this.birdBrain.load(compoundTag);
        if (compoundTag.contains("BirdModelScale", CompoundTag.TAG_FLOAT)) {
            this.setIndividualModelScale(BirdModelScale.load(compoundTag, this.modelScaleProfile()));
        } else {
            this.randomizeModelScale();
        }
    }

    // ============ 寻路评分 ============
    @Override
    public float getWalkTargetValue(@NotNull BlockPos pos, @NotNull LevelReader level) {
        float score = super.getWalkTargetValue(pos, level);
        BlockState below = level.getBlockState(pos.below());
        if (isNearWaterForWorldgen(level, pos, 4)) {
            score += 8.0F;
        }
        if (isWaterEdgeForWorldgen(level, pos)) {
            score += 6.0F;
        }
        if (below.is(Blocks.WATER) || below.is(Blocks.SEAGRASS)
                || below.is(Blocks.GRASS_BLOCK) || below.is(BlockTags.SAND)) {
            score += 2.0F;
        }
        return score;
    }

    // ============ 声音 ============
    @Override
    protected SoundEvent getAmbientSound() {
        return NeoGuanNiaoSoundEvents.NIGHT_HERON_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return NeoGuanNiaoSoundEvents.NIGHT_HERON_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return NeoGuanNiaoSoundEvents.NIGHT_HERON_DEATH.get();
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float damageMultiplier, @NotNull DamageSource damageSource) {
        return false;
    }

    @Override
    public boolean isInWater() {
        return this.isControlledFlightActive() || super.isInWater();
    }

    @Override
    protected void playStepSound(@NotNull BlockPos pos, @NotNull BlockState blockState) {
        this.playSound(SoundEvents.CHICKEN_STEP, 0.12F, 0.9F);
    }

    @Override
    public int getAmbientSoundInterval() {
        return 240;
    }

    @Override
    public float getVoicePitch() {
        return 0.5F;
    }

    // ============ 时间状态 ============
    @SuppressWarnings("all")
    public boolean isActiveTime() {
        long timeOfDay = this.level().getDayTime() % 24000L;
        return timeOfDay >= ACTIVE_START_TIME || timeOfDay <= ACTIVE_END_TIME;
    }

    public boolean shouldRoost() {
        return !this.isActiveTime();
    }

    public boolean isRoosting() {
        return this.getBehaviorState() == NightHeronBehaviorState.ROOSTING || this.shouldRoost();
    }

    // ============ 渲染数据 ============
    public ItemStack getHeldFishForRendering() {
        return this.getEntityData().get(HELD_FISH);
    }

    public boolean hasHeldFishForRendering() {
        return !this.getHeldFishForRendering().isEmpty();
    }

    public boolean isEatingFish() {
        return this.getEntityData().get(EATING_TICKS) > 0 || this.hasHeldFishForRendering();
    }

    // ============ 行为状态 ============
    public NightHeronBehaviorState getBehaviorState() {
        return decodeBehaviorState(this.getEntityData().get(BEHAVIOR_STATE));
    }

    public void setBehaviorState(NightHeronBehaviorState behaviorState) {
        this.behaviorState = behaviorState;
        this.getEntityData().set(BEHAVIOR_STATE, behaviorState.ordinal());
        if (!behaviorState.isAirborne()) {
            this.controlledFlightTicks = 0;
            this.groundedAirborneTicks = 0;
            this.resetFlightObstructionProbe();
        }
        if (behaviorState != NightHeronBehaviorState.PREEN
                && behaviorState != NightHeronBehaviorState.NECK_STRETCH
                && behaviorState != NightHeronBehaviorState.EATING) {
            this.forcedIdleAnimationTicks = 0;
        }
    }

    // ============ 缩放接口 ============
    @Override
    public BirdModelScaleProfile modelScaleProfile() {
        return BirdModelScaleProfile.NIGHT_HERON;
    }

    @Override
    public float getIndividualModelScale() {
        return BirdModelScale.sanitize(this.getEntityData().get(MODEL_SCALE), this.modelScaleProfile());
    }

    @Override
    public void setIndividualModelScale(float scale) {
        this.getEntityData().set(MODEL_SCALE, BirdModelScale.sanitize(scale, this.modelScaleProfile()));
    }

    // ============ 飞行接口 ============

    public BirdBrain birdBrain() {
        return this.birdBrain;
    }

    @Override
    public BirdFlightProfile birdFlightProfile() {
        return BirdFlightProfile.NIGHT_HERON;
    }

    @Override
    public boolean isBirdFlightActive() {
        return this.isControlledFlightActive() || this.getBehaviorState().isAirborne();
    }

    @Override
    public boolean isBirdLanding() {
        return this.getBehaviorState() == NightHeronBehaviorState.LANDING;
    }

    @Override
    public boolean isBirdEscaping() {
        return this.getBehaviorState().isEscape();
    }

    @Override
    public boolean isFlying() {
        return this.shouldUseFlyingAnimation();
    }

    // ============ 预览动画 ============
    public void setGuidePreviewAnimation(NightHeronGuidePreviewAnimation guidePreviewAnimation) {
        this.guidePreviewAnimation = guidePreviewAnimation == null
                ? NightHeronGuidePreviewAnimation.NONE
                : guidePreviewAnimation;
    }

    public NightHeronGuidePreviewAnimation getGuidePreviewAnimation() {
        return this.guidePreviewAnimation;
    }

    // ============ 动作触发 ============
    public void triggerPreen() {
        this.setBehaviorState(NightHeronBehaviorState.PREEN);
        this.currentIdleAnimation = NightHeronIdleAnimationChoice.SCRATCH;
        this.forcedIdleAnimationTicks = 88;
        this.nextIdleAnimationSwapTick = this.level().getGameTime() + this.forcedIdleAnimationTicks;
    }

    public void triggerNeckStretch() {
        this.setBehaviorState(NightHeronBehaviorState.NECK_STRETCH);
        this.currentIdleAnimation = switch (this.getRandom().nextInt(4)) {
            case 0 -> NightHeronIdleAnimationChoice.LONG_NECK_1;
            case 1 -> NightHeronIdleAnimationChoice.LONG_NECK_2;
            case 2 -> NightHeronIdleAnimationChoice.LONG_NECK_3;
            default -> NightHeronIdleAnimationChoice.LONG_NECK_5;
        };
        this.forcedIdleAnimationTicks = 76;
        this.nextIdleAnimationSwapTick = this.level().getGameTime() + this.forcedIdleAnimationTicks;
    }

    void markTakeoffFlapping() {
        this.takeoffFlapTicks = 20;
        this.controlledFlightTicks = 0;
        this.groundedAirborneTicks = 0;
        this.resetFlightObstructionProbe();
        this.clearLandingApproach();
    }

    boolean isTakeoffFlapping() {
        return this.takeoffFlapTicks > 0;
    }

    int getControlledFlightTicks() {
        return this.controlledFlightTicks;
    }

    // ============ 飞行状态 ============
    public boolean isControlledFlightActive() {
        if (!this.getBehaviorState().isAirborne()) {
            return false;
        }
        return !this.onGround() || this.getDeltaMovement().y > 0.04;
    }

    void finishFlight(NightHeronBehaviorState nextState) {
        this.flybyFlightTicks = 0;
        this.flybyFlightDirection = Vec3.ZERO;
        this.flybyLandingTarget = null;
        this.takeoffFlapTicks = 0;
        this.controlledFlightTicks = 0;
        this.groundedAirborneTicks = 0;
        this.resetFlightObstructionProbe();
        this.getNavigation().stop();
        this.setDeltaMovement(Vec3.ZERO);
        this.xxa = 0.0F;
        this.hasImpulse = true;
        this.clearLandingApproach();
        this.setBehaviorState(nextState);
    }

    public void settleInterruptedFlight(NightHeronBehaviorState groundedState) {
        if (this.getBehaviorState().isAirborne()) {
            if (this.onGround()) {
                this.finishFlight(groundedState);
            } else {
                this.takeoffFlapTicks = 0;
                this.groundedAirborneTicks = 0;
                this.resetFlightObstructionProbe();
                this.getNavigation().stop();
                Vec3 movement = this.getDeltaMovement();
                this.setDeltaMovement(movement.x * 0.45, Math.min(movement.y, -0.045), movement.z * 0.45);
                this.xxa = 0.0F;
                this.hasImpulse = true;
                this.setBehaviorState(NightHeronBehaviorState.LANDING);
            }
        }
    }

    // ============ 飞行接口实现 ============
    public void startFlybyFlight(Vec3 direction, int ticks) {
        this.startFlybyFlight(direction, null, ticks);
    }

    public void startFlybyFlight(Vec3 direction, BlockPos landingTarget, int ticks) {
        this.clearEatingFish();
        this.clearExternalFright();
        this.flybyFlightDirection = this.normalizeHorizontal(direction);
        this.flybyFlightTicks = Math.max(80, ticks);
        this.flybyLandingTarget = landingTarget == null
                ? NightHeronLandingSelector.findTransitLanding(this, 22, 74)
                : landingTarget.immutable();
        this.getNavigation().stop();
        this.markTakeoffFlapping();
        this.setBehaviorState(NightHeronBehaviorState.HIGH_TRANSIT);
        this.setSilent(false);
        Vec3 movement = this.flybyFlightDirection.scale(0.44).add(0, 0.06, 0);
        this.setDeltaMovement(movement);
        this.faceMovementDirection(movement);
        this.xxa = 0.0F;
        this.hasImpulse = true;
    }

    @Override
    public boolean startBirdBathMountFlight(Vec3 standPosition) {
        if (standPosition != null && !this.isControlledFlightActive()) {
            Vec3 horizontal = standPosition.subtract(this.position()).multiply(1.0, 0.0, 1.0);
            if (horizontal.lengthSqr() <= 1.0E-4) {
                horizontal = Vec3.ZERO;
            } else {
                horizontal = horizontal.normalize().scale(0.24);
            }
            Vec3 movement = new Vec3(horizontal.x, 0.62, horizontal.z);
            this.getNavigation().stop();
            this.markTakeoffFlapping();
            this.setSilent(false);
            this.setDeltaMovement(movement);
            this.faceMovementDirection(movement);
            this.xxa = 0.0F;
            this.hasImpulse = true;
            return true;
        }
        return false;
    }

    @Override
    public void startBirdBathFeedingAnimation(BirdBathContentType contentType, int ticks) {
        this.getNavigation().stop();
        if (contentType == BirdBathContentType.FISH) {
            this.showHeldFoodDuringBirdBathFeeding(new ItemStack(Items.COD), ticks);
        } else if (contentType == BirdBathContentType.MEAT) {
            this.showHeldFoodDuringBirdBathFeeding(new ItemStack(Items.MUTTON), ticks);
        } else if (contentType == BirdBathContentType.BREAD) {
            this.showHeldFoodDuringBirdBathFeeding(new ItemStack(Items.BREAD), ticks);
        } else {
            this.setBehaviorState(NightHeronBehaviorState.FORAGING);
            this.forcedIdleAnimationTicks = Math.max(this.forcedIdleAnimationTicks, Math.max(24, ticks / 2));
        }
    }

    // ============ 着陆辅助 ============
    Vec3 updateLandingApproachDirection(BlockPos landingTarget, Vec3 preferredDirection, double correctionWeight) {
        Vec3 preferred = this.normalizeHorizontal(preferredDirection);
        boolean newTarget = this.landingApproachTarget == null || !this.landingApproachTarget.equals(landingTarget);
        if (!newTarget && this.landingApproachDirection.lengthSqr() > 1.0E-4) {
            double dot = this.landingApproachDirection.dot(preferred);
            if (dot < -0.25) {
                preferred = this.landingApproachDirection;
                correctionWeight = 0.0;
            }
            correctionWeight = Mth.clamp(correctionWeight, 0.0, 0.35);
            Vec3 blended = this.landingApproachDirection.scale(1.0 - correctionWeight)
                    .add(preferred.scale(correctionWeight));
            this.landingApproachDirection = this.normalizeHorizontal(blended);
        } else {
            this.landingApproachTarget = landingTarget.immutable();
            this.landingApproachDirection = preferred;
        }
        return this.landingApproachDirection;
    }

    void clearLandingApproach() {
        this.landingApproachTarget = null;
        this.landingApproachDirection = Vec3.ZERO;
    }

    // ============ 飞行受阻恢复 ============
    void markBlockedFlightRecovery() {
        this.blockedFlightRecoveryActivityTicks = 70;
    }

    public boolean hasBlockedFlightRecoveryActivity() {
        return this.blockedFlightRecoveryActivityTicks > 0;
    }

    public boolean consumeBlockedFlightRecoveryActivity() {
        if (this.blockedFlightRecoveryActivityTicks <= 0) {
            return false;
        }
        this.blockedFlightRecoveryActivityTicks = 0;
        return true;
    }

    boolean tickFlightObstructionProbe(boolean pathBlocked) {
        if (this.getBehaviorState().isAirborne() && !this.onGround()) {
            Vec3 currentPosition = this.position();
            if (this.lastControlledFlightPosition == Vec3.ZERO) {
                this.lastControlledFlightPosition = currentPosition;
                return false;
            }
            double progressSqr = currentPosition.subtract(this.lastControlledFlightPosition).lengthSqr();
            boolean stagnant = this.controlledFlightTicks > 8 && progressSqr < 0.0025;
            this.obstructedFlightTicks = (!pathBlocked && !this.isInWater() && !stagnant)
                    ? Math.max(0, this.obstructedFlightTicks - 2)
                    : ++this.obstructedFlightTicks;
            this.lastControlledFlightPosition = currentPosition;
            return this.obstructedFlightTicks >= 5;
        } else {
            this.resetFlightObstructionProbe();
            return false;
        }
    }

    void resetFlightObstructionProbe() {
        this.obstructedFlightTicks = 0;
        this.lastControlledFlightPosition = Vec3.ZERO;
        this.blockedFlightRecoveryDirectionTicks = 0;
        this.blockedFlightRecoveryDirection = Vec3.ZERO;
    }

    Vec3 getBlockedFlightRecoveryDirection() {
        return this.blockedFlightRecoveryDirectionTicks > 0 ? this.blockedFlightRecoveryDirection : Vec3.ZERO;
    }

    @SuppressWarnings("SameParameterValue")
    void lockBlockedFlightRecoveryDirection(Vec3 direction, int ticks) {
        this.blockedFlightRecoveryDirection = this.normalizeHorizontal(direction);
        this.blockedFlightRecoveryDirectionTicks = ticks;
    }

    // ============ 向量工具 ============
    private Vec3 normalizeHorizontal(Vec3 vector) {
        Vec3 horizontal = new Vec3(vector.x, 0, vector.z);
        if (horizontal.lengthSqr() <= 1.0E-4) {
            Vec3 movement = this.getDeltaMovement();
            horizontal = new Vec3(movement.x, 0, movement.z);
        }
        if (horizontal.lengthSqr() <= 1.0E-4) {
            Vec3 look = this.getLookAngle();
            horizontal = new Vec3(look.x, 0, look.z);
        }
        return horizontal.lengthSqr() <= 1.0E-4 ? new Vec3(1, 0, 0) : horizontal.normalize();
    }

    // ============ 交互 ============
    boolean isTemptingPlayer(Player player) {
        return TEMPT_ITEMS.test(player.getMainHandItem()) || TEMPT_ITEMS.test(player.getOffhandItem());
    }

    public boolean canStrikePrey() {
        return this.preyStrikeCooldown <= 0;
    }

    public boolean canEatThrownFish() {
        NightHeronBehaviorState state = this.getBehaviorState();
        return !this.isEatingFish() && this.thrownFishEatCooldown <= 0 && this.preyStrikeCooldown <= 0
                && this.onGround() && !state.isAirborne() && !state.isEscape()
                && state != NightHeronBehaviorState.ROOSTING && this.getTarget() == null
                && !this.hasExternalFright() && this.birdBrain().motivation().fear() < 0.55F;
    }

    private boolean canUseBirdBath() {
        NightHeronBehaviorState state = this.getBehaviorState();
        return !this.isEatingFish() && this.thrownFishEatCooldown <= 0
                && this.onGround() && !state.isAirborne() && !state.isEscape()
                && state != NightHeronBehaviorState.ROOSTING && this.getTarget() == null
                && !this.hasExternalFright() && this.birdBrain().motivation().fear() < 0.6F;
    }

    private void consumeBirdBathServing(BirdBathBlockEntity bath, BirdBathContentType contentType) {
        if (contentType == BirdBathContentType.FISH) {
            this.startEatingFish(new ItemStack(Items.COD), 45 + this.getRandom().nextInt(21));
        } else {
            this.birdBrain.onEat(0.18F);
            this.setBehaviorState(NightHeronBehaviorState.FORAGING);
            this.forcedIdleAnimationTicks = Math.max(this.forcedIdleAnimationTicks, 24);
            this.playSound(SoundEvents.PARROT_EAT, 0.42F, 0.9F + this.getRandom().nextFloat() * 0.12F);
        }
    }

    public static boolean isEdibleFishItem(ItemStack stack) {
        return !stack.isEmpty() && TEMPT_ITEMS.test(stack);
    }

    // ============ 进食 ============
    public void eatThrownFish(ItemEntity itemEntity) {
        ItemStack stack = itemEntity.getItem();
        if (isEdibleFishItem(stack)) {
            ItemStack shownStack = stack.copy();
            shownStack.setCount(1);
            stack.shrink(1);
            if (stack.isEmpty()) {
                itemEntity.discard();
            } else {
                itemEntity.setItem(stack);
            }
            this.startEatingFish(shownStack, 45 + this.getRandom().nextInt(21));
        }
    }

    void startEatingFish(ItemStack fishStack, int ticks) {
        ItemStack copy = fishStack.copy();
        copy.setCount(1);
        this.getEntityData().set(HELD_FISH, copy);
        this.getEntityData().set(EATING_TICKS, Math.max(1, ticks));
        this.getNavigation().stop();
        this.setBehaviorState(NightHeronBehaviorState.EATING);
        this.forcedIdleAnimationTicks = Math.max(this.forcedIdleAnimationTicks, ticks);
        this.nextIdleAnimationSwapTick = this.level().getGameTime() + ticks;
        this.birdBrain.onEat(0.45F);
        this.playSound(NeoGuanNiaoSoundEvents.NIGHT_HERON_ATTACK.get(), 0.55F, 0.9F + this.getRandom().nextFloat() * 0.18F);
    }

    private void showHeldFoodDuringBirdBathFeeding(ItemStack foodStack, int ticks) {
        ItemStack copy = foodStack.copy();
        copy.setCount(1);
        this.getEntityData().set(HELD_FISH, copy);
        this.getEntityData().set(EATING_TICKS, Math.max(1, ticks));
        this.getNavigation().stop();
        this.setBehaviorState(NightHeronBehaviorState.EATING);
        this.forcedIdleAnimationTicks = Math.max(this.forcedIdleAnimationTicks, ticks);
        this.nextIdleAnimationSwapTick = this.level().getGameTime() + ticks;
    }

    private void tickEatingFish() {
        int ticks = this.getEntityData().get(EATING_TICKS);
        if (ticks <= 0) {
            if (this.hasHeldFishForRendering()) {
                this.clearEatingFish();
            }
            return;
        }
        this.getEntityData().set(EATING_TICKS, ticks - 1);
        this.getNavigation().stop();
        if (!this.getBehaviorState().isEscape()) {
            this.setBehaviorState(NightHeronBehaviorState.EATING);
        }
        if (ticks - 1 <= 0) {
            this.clearEatingFish();
        }
    }

    private void clearEatingFish() {
        boolean wasEating = this.isEatingFish();
        this.getEntityData().set(HELD_FISH, ItemStack.EMPTY);
        this.getEntityData().set(EATING_TICKS, 0);
        if (wasEating) {
            this.thrownFishEatCooldown = Math.max(this.thrownFishEatCooldown, 80 + this.getRandom().nextInt(81));
        }
        if (this.getBehaviorState() == NightHeronBehaviorState.EATING) {
            this.setBehaviorState(NightHeronBehaviorState.IDLE);
        }
    }

    public void afterPreyStrike() {
        this.preyStrikeCooldown = 45;
        this.playSound(NeoGuanNiaoSoundEvents.NIGHT_HERON_ATTACK.get(), 0.65F, 0.9F + this.getRandom().nextFloat() * 0.18F);
        this.heal(0.5F);
    }

    // ============ 恐惧 ============
    @SuppressWarnings("SameParameterValue")
    public void rememberFright(boolean severe) {
        this.frightMemoryTicks = 220;
        this.recentFrightCount = Math.min(6, this.recentFrightCount + (severe ? 2 : 1));
    }

    public int getRecentFrightCount() {
        return this.recentFrightCount;
    }

    @SuppressWarnings("SameParameterValue")
    public void receiveFlockFright(Vec3 source, boolean severe) {
        this.clearEatingFish();
        this.externalFrightSource = source;
        this.externalFrightTicks = severe ? 100 : 55;
        this.severeExternalFright = severe;
        if (severe) {
            this.rememberFright(true);
        }
    }

    public boolean hasExternalFright() {
        return this.externalFrightTicks > 0 && this.externalFrightSource != null;
    }

    public boolean hasSevereExternalFright() {
        return this.hasExternalFright() && this.severeExternalFright;
    }

    public Vec3 getExternalFrightSource() {
        return this.hasExternalFright() ? this.externalFrightSource : null;
    }

    public void clearExternalFright() {
        this.externalFrightTicks = 0;
        this.externalFrightSource = null;
        this.severeExternalFright = false;
    }

    // ============ 飞越飞行 ============
    private void tickFlybyFlight() {
        if (this.flybyFlightTicks <= 0) {
            return;
        }
        if (!this.getBehaviorState().isAirborne()) {
            this.flybyFlightTicks = 0;
            this.flybyFlightDirection = Vec3.ZERO;
            this.flybyLandingTarget = null;
            return;
        }

        this.getNavigation().stop();
        --this.flybyFlightTicks;

        if (NightHeronFlightController.shouldBeginLandingApproach(
                this, this.flybyLandingTarget, this.flybyFlightTicks, 28.0)) {
            if (NightHeronFlightController.tickLandingApproach(this, this.flybyLandingTarget)) {
                this.flybyFlightTicks = 0;
                this.flybyFlightDirection = Vec3.ZERO;
                this.flybyLandingTarget = null;
            } else if (this.flybyFlightTicks <= 0) {
                this.flybyFlightTicks = 1;
            }
        } else if (this.flybyFlightTicks <= 0) {
            if (this.flybyLandingTarget == null) {
                this.flybyLandingTarget = NightHeronLandingSelector.findTransitLanding(this, 8, 36);
            }
            if (this.flybyLandingTarget != null) {
                if (NightHeronFlightController.tickLandingApproach(this, this.flybyLandingTarget)) {
                    this.flybyFlightDirection = Vec3.ZERO;
                    this.flybyLandingTarget = null;
                } else {
                    this.flybyFlightTicks = 1;
                }
            } else {
                NightHeronFlightController.tickOpenLanding(this, this.flybyFlightDirection);
                if (!this.onGround()) {
                    this.flybyFlightTicks = 1;
                }
            }
        } else {
            NightHeronFlightController.tickHighTransitFlight(this, this.flybyFlightDirection);
        }
    }

    // ============ 朝向控制 ============
    void faceMovementDirection(Vec3 movement) {
        BirdFlightController.faceMovement(this, movement, BirdFlightProfile.NIGHT_HERON.maxPitchDegrees());
    }

    private void tickGroundMovementFacing() {
        if (this.shouldFaceGroundMovement()) {
            BirdFlightController.faceGroundMovement(this, this.getDeltaMovement(), 1.0E-4);
        }
    }

    private boolean shouldFaceGroundMovement() {
        if (this.onGround() && !this.isControlledFlightActive() && !this.isInWater() && !this.isVehicle()) {
            NightHeronBehaviorState state = this.getBehaviorState();
            if (!state.isAirborne() && state != NightHeronBehaviorState.EATING
                    && state != NightHeronBehaviorState.PREEN && state != NightHeronBehaviorState.NECK_STRETCH
                    && state != NightHeronBehaviorState.REST_STAND && state != NightHeronBehaviorState.LOOK_AROUND
                    && state != NightHeronBehaviorState.ALERT_FREEZE && state != NightHeronBehaviorState.ROOSTING) {
                return this.getDeltaMovement().lengthSqr() > WALKING_SPEED_THRESHOLD
                        || !this.getNavigation().isDone();
            }
        }
        return false;
    }

    // ============ 高度检测 ============
    double heightAboveSurface() {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        int x = Mth.floor(this.getX());
        int z = Mth.floor(this.getZ());
        int startY = Mth.floor(this.getY());
        int minY = this.level().getMinBuildHeight();

        for (int y = startY; y >= minY; --y) {
            mutablePos.set(x, y, z);
            BlockState state = this.level().getBlockState(mutablePos);
            if (!state.getCollisionShape(this.level(), mutablePos).isEmpty()
                    || this.level().getFluidState(mutablePos).is(FluidTags.WATER)) {
                return Math.max(0.0, this.getY() - (y + 1.0));
            }
        }
        return 18.0;
    }

    // ============ 环境检测 ============
    @SuppressWarnings("SameParameterValue")
    public boolean isNearWater(BlockPos pos, int radius) {
        return isNearWater(this.level(), pos, radius);
    }

    public static boolean isWaterEdge(LevelReader level, BlockPos pos) {
        if (!canReadChunk(level, pos)) {
            return false;
        }
        if (level.getFluidState(pos).is(FluidTags.WATER) && !level.getFluidState(pos.above()).is(FluidTags.WATER)) {
            return true;
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos adjacentPos = pos.relative(direction);
            if (canReadChunk(level, adjacentPos)
                    && (level.getFluidState(adjacentPos).is(FluidTags.WATER)
                    || level.getFluidState(adjacentPos.below()).is(FluidTags.WATER))) {
                return true;
            }
        }
        return false;
    }

    static boolean isNearWater(LevelReader level, BlockPos pos, int radius) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        for (int xOffset = -radius; xOffset <= radius; ++xOffset) {
            for (int zOffset = -radius; zOffset <= radius; ++zOffset) {
                if (xOffset * xOffset + zOffset * zOffset <= radius * radius) {
                    for (int yOffset = -1; yOffset <= 1; ++yOffset) {
                        mutablePos.set(pos.getX() + xOffset, pos.getY() + yOffset, pos.getZ() + zOffset);
                        if (canReadChunk(level, mutablePos) && level.getFluidState(mutablePos).is(FluidTags.WATER)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private static boolean isNearWaterForWorldgen(LevelReader level, BlockPos pos, int radius) {
        return level instanceof WorldGenRegion
                ? isNearWaterInSpawnChunk(level, pos, radius)
                : isNearWater(level, pos, radius);
    }

    private static boolean isWaterEdgeForWorldgen(LevelReader level, BlockPos pos) {
        return level instanceof WorldGenRegion
                ? isWaterEdgeInSpawnChunk(level, pos)
                : isWaterEdge(level, pos);
    }

    private static boolean isWaterEdgeInSpawnChunk(LevelReader level, BlockPos pos) {
        int spawnChunkX = SectionPos.blockToSectionCoord(pos.getX());
        int spawnChunkZ = SectionPos.blockToSectionCoord(pos.getZ());
        if (level.getFluidState(pos).is(FluidTags.WATER) && !level.getFluidState(pos.above()).is(FluidTags.WATER)) {
            return true;
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos adjacentPos = pos.relative(direction);
            if (isInChunk(adjacentPos, spawnChunkX, spawnChunkZ)
                    && (level.getFluidState(adjacentPos).is(FluidTags.WATER)
                    || level.getFluidState(adjacentPos.below()).is(FluidTags.WATER))) {
                return true;
            }
        }
        return false;
    }

    private static boolean isNearWaterInSpawnChunk(LevelReader level, BlockPos pos, int radius) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        int spawnChunkX = SectionPos.blockToSectionCoord(pos.getX());
        int spawnChunkZ = SectionPos.blockToSectionCoord(pos.getZ());

        for (int xOffset = -radius; xOffset <= radius; ++xOffset) {
            for (int zOffset = -radius; zOffset <= radius; ++zOffset) {
                int x = pos.getX() + xOffset;
                int z = pos.getZ() + zOffset;
                if (xOffset * xOffset + zOffset * zOffset <= radius * radius
                        && isInChunk(x, z, spawnChunkX, spawnChunkZ)) {
                    for (int yOffset = -1; yOffset <= 1; ++yOffset) {
                        mutablePos.set(x, pos.getY() + yOffset, z);
                        if (level.getFluidState(mutablePos).is(FluidTags.WATER)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private static boolean isInChunk(BlockPos pos, int chunkX, int chunkZ) {
        return isInChunk(pos.getX(), pos.getZ(), chunkX, chunkZ);
    }

    private static boolean isInChunk(int x, int z, int chunkX, int chunkZ) {
        return SectionPos.blockToSectionCoord(x) == chunkX && SectionPos.blockToSectionCoord(z) == chunkZ;
    }

    @SuppressWarnings("deprecation")
    public static boolean canReadChunk(LevelReader level, BlockPos pos) {
        return level.hasChunk(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
    }

    // ============ 水上逃生 ============
    private void tickWaterEscape() {
        if (this.isInWater()) {
            this.clearEatingFish();
            this.getNavigation().stop();
            BlockPos dryTarget = this.findNearestDryEscapePosition();
            Vec3 direction = dryTarget == null
                    ? this.getLookAngle().multiply(1.0, 0.0, 1.0)
                    : Vec3.atBottomCenterOf(dryTarget).subtract(this.position()).multiply(1.0, 0.0, 1.0);
            if (direction.lengthSqr() <= 1.0E-4) {
                double angle = this.getRandom().nextDouble() * Math.PI * 2.0;
                direction = new Vec3(Math.cos(angle), 0, Math.sin(angle));
            }
            direction = direction.normalize();
            this.markTakeoffFlapping();
            this.setBehaviorState(NightHeronBehaviorState.TAKEOFF);
            Vec3 movement = direction.scale(0.34).add(0, 0.36, 0);
            this.setDeltaMovement(movement);
            this.faceMovementDirection(movement);
            this.xxa = 0.0F;
            this.hasImpulse = true;
        }
    }

    private BlockPos findNearestDryEscapePosition() {
        BlockPos origin = this.blockPosition();
        BlockPos best = null;
        int bestDistanceSqr = Integer.MAX_VALUE;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int radius = 2; radius <= 9; ++radius) {
            for (int xOffset = -radius; xOffset <= radius; ++xOffset) {
                for (int zOffset = -radius; zOffset <= radius; ++zOffset) {
                    if (Math.abs(xOffset) == radius || Math.abs(zOffset) == radius) {
                        for (int yOffset = 3; yOffset >= -3; --yOffset) {
                            mutable.set(origin.getX() + xOffset, origin.getY() + yOffset, origin.getZ() + zOffset);
                            if (this.isDryEscapePosition(mutable)) {
                                int distanceSqr = xOffset * xOffset + zOffset * zOffset + yOffset * yOffset;
                                if (distanceSqr < bestDistanceSqr) {
                                    bestDistanceSqr = distanceSqr;
                                    best = mutable.immutable();
                                }
                            }
                        }
                    }
                }
            }
            if (best != null) {
                return best;
            }
        }
        return null;
    }

    private boolean isDryEscapePosition(BlockPos pos) {
        if (!canReadChunk(this.level(), pos)) {
            return false;
        }
        BlockState below = this.level().getBlockState(pos.below());
        BlockState feet = this.level().getBlockState(pos);
        BlockState head = this.level().getBlockState(pos.above());

        if (!feet.getCollisionShape(this.level(), pos).isEmpty()
                || !head.getCollisionShape(this.level(), pos.above()).isEmpty()) {
            return false;
        }
        if (this.level().getFluidState(pos).is(FluidTags.WATER)
                || this.level().getFluidState(pos).is(FluidTags.LAVA)
                || this.level().getFluidState(pos.below()).is(FluidTags.WATER)
                || this.level().getFluidState(pos.below()).is(FluidTags.LAVA)) {
            return false;
        }
        return below.isFaceSturdy(this.level(), pos.below(), Direction.UP)
                || below.is(BlockTags.WALLS)
                || below.is(BlockTags.LEAVES)
                || below.is(Blocks.WATER)
                || below.is(Blocks.SEAGRASS)
                || below.is(Blocks.GRASS_BLOCK)
                || below.is(Blocks.DIRT_PATH);
    }

    // ============ 飞行状态守卫 ============
    private void tickFlightStateGuard() {
        NightHeronBehaviorState state = this.getBehaviorState();
        if (!state.isAirborne()) {
            this.controlledFlightTicks = 0;
            this.groundedAirborneTicks = 0;
            this.resetFlightObstructionProbe();
        } else if (!this.onGround()) {
            ++this.controlledFlightTicks;
            this.groundedAirborneTicks = 0;
        } else if (this.takeoffFlapTicks <= 0 || !(this.getDeltaMovement().y > 0.04)) {
            ++this.groundedAirborneTicks;
            if (this.groundedAirborneTicks >= 2) {
                this.finishFlight(NightHeronBehaviorState.IDLE);
            }
        }
    }

    private void tickStaleFlightRecovery() {
        if (!this.getBehaviorState().isAirborne()) {
            if (super.isInWater()) {
                this.setNoGravity(false);
                if (!this.onGround() && this.getDeltaMovement().lengthSqr() < 0.0025) {
                    this.setDeltaMovement(0, -0.08, 0);
                    this.hasImpulse = true;
                }
            }
        } else {
            if (!this.onGround() && this.controlledFlightTicks > 6 && this.getDeltaMovement().lengthSqr() < 0.0025) {
                this.setNoGravity(false);
                this.setBehaviorState(NightHeronBehaviorState.IDLE);
                this.setDeltaMovement(0, -0.08, 0);
                this.hasImpulse = true;
                this.xxa = 0.0F;
            }
        }
    }

    // ============ 动画 ============
    private RawAnimation pickIdleAnimation() {
        if (this.forcedIdleAnimationTicks > 0) {
            return this.currentIdleAnimation.animation;
        }
        if (this.level().getGameTime() >= this.nextIdleAnimationSwapTick) {
            this.currentIdleAnimation = this.chooseIdleAnimation();
            this.nextIdleAnimationSwapTick = this.level().getGameTime()
                    + this.currentIdleAnimation.nextDuration(this.getRandom());
        }
        return this.currentIdleAnimation.animation;
    }

    private NightHeronIdleAnimationChoice chooseIdleAnimation() {
        int roll = this.getRandom().nextInt(100);
        NightHeronBehaviorState state = this.getBehaviorState();

        if (state == NightHeronBehaviorState.WATER_EDGE_WAIT || state == NightHeronBehaviorState.FORAGING) {
            if (roll < 58) return NightHeronIdleAnimationChoice.BASE;
            if (roll < 73) return NightHeronIdleAnimationChoice.LONG_NECK_1;
            if (roll < 86) return NightHeronIdleAnimationChoice.LONG_NECK_3;
            if (roll < 92) return NightHeronIdleAnimationChoice.SCRATCH;
            return NightHeronIdleAnimationChoice.LONG_NECK_5;
        }

        if (this.isRoosting()) {
            if (roll < 48) return NightHeronIdleAnimationChoice.BASE;
            if (roll < 63) return NightHeronIdleAnimationChoice.LONG_NECK_1;
            if (roll < 78) return NightHeronIdleAnimationChoice.LONG_NECK_2;
            if (roll < 90) return NightHeronIdleAnimationChoice.SCRATCH;
            return NightHeronIdleAnimationChoice.LONG_NECK_5;
        }

        if (roll < 60) return NightHeronIdleAnimationChoice.BASE;
        if (roll < 70) return NightHeronIdleAnimationChoice.LONG_NECK_1;
        if (roll < 80) return NightHeronIdleAnimationChoice.LONG_NECK_2;
        if (roll < 89) return NightHeronIdleAnimationChoice.LONG_NECK_3;
        if (roll < 94) return NightHeronIdleAnimationChoice.SCRATCH;
        return NightHeronIdleAnimationChoice.LONG_NECK_5;
    }

    private boolean shouldUseFlyingAnimation() {
        return !this.isInWater() && BirdFlightController.shouldPlayFlyAnimation(
                this, this.getBehaviorState().isAirborne(), this.onGround(),
                this.isInWater(), this.getDeltaMovement(), this.groundedAirborneTicks
        );
    }

    private RawAnimation chooseFlyingAnimation() {
        NightHeronBehaviorState state = this.getBehaviorState();
        if (state == NightHeronBehaviorState.TAKEOFF || this.takeoffFlapTicks > 0) {
            return FLY_FLAPPING_WING_ANIMATION;
        }
        if (state == NightHeronBehaviorState.LOCAL_FLIGHT || state == NightHeronBehaviorState.LOW_FLAP_ESCAPE
                || state == NightHeronBehaviorState.CLIMB || state == NightHeronBehaviorState.LANDING) {
            return FLY_FLAPPING_WING_LOOP_ANIMATION;
        }
        boolean highFlight = state == NightHeronBehaviorState.HIGH_TRANSIT
                || state == NightHeronBehaviorState.LONG_FLIGHT_ESCAPE
                || state == NightHeronBehaviorState.SOARING
                || state == NightHeronBehaviorState.GLIDE;
        if (highFlight && NightHeronFlightController.shouldGlide(this) && state != NightHeronBehaviorState.GLIDE) {
            return FLY_LOOP_ANIMATION;
        }
        return FLY_FLAPPING_WING_LOOP_ANIMATION;
    }

    private <T extends NightHeronEntity> PlayState movementController(AnimationState<T> animationState) {
        RawAnimation guidePreviewRawAnimation = this.guidePreviewAnimation.animation();
        if (guidePreviewRawAnimation != null) {
            return animationState.setAndContinue(guidePreviewRawAnimation);
        }
        if (this.isEatingFish()) {
            return animationState.setAndContinue(EAT_ANIMATION);
        }
        if (this.shouldUseFlyingAnimation()) {
            return animationState.setAndContinue(this.chooseFlyingAnimation());
        }

        double horizontalSpeed = this.getDeltaMovement().lengthSqr();
        if (this.getBehaviorState() == NightHeronBehaviorState.RUN_ESCAPE || horizontalSpeed > RUNNING_SPEED_THRESHOLD) {
            return animationState.setAndContinue(RUN_ANIMATION);
        }
        if (horizontalSpeed > WALKING_SPEED_THRESHOLD || !this.getNavigation().isDone()) {
            return animationState.setAndContinue(WALK_ANIMATION);
        }
        return animationState.setAndContinue(this.pickIdleAnimation());
    }

    // ============ GeckoLib ============
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 4, this::movementController));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animationCache;
    }

    // ============ 序列化恢复 ============
    private void clearSerializedFlightState() {
        this.takeoffFlapTicks = 0;
        this.controlledFlightTicks = 0;
        this.groundedAirborneTicks = 0;
        this.resetFlightObstructionProbe();
        this.clearLandingApproach();
        if (super.isInWater()) {
            this.setNoGravity(false);
        }
        if (this.getBehaviorState().isAirborne()) {
            this.setBehaviorState(NightHeronBehaviorState.IDLE);
        }
        if (!this.onGround() && this.getDeltaMovement().lengthSqr() < 0.0025) {
            this.setDeltaMovement(0, -0.08, 0);
            this.hasImpulse = true;
            this.xxa = 0.0F;
        }
    }

    // ============ 工具方法 ============
    private static NightHeronBehaviorState decodeBehaviorState(int ordinal) {
        NightHeronBehaviorState[] states = NightHeronBehaviorState.values();
        return ordinal >= 0 && ordinal < states.length ? states[ordinal] : NightHeronBehaviorState.IDLE;
    }

    private void randomizeModelScale() {
        this.setIndividualModelScale(BirdModelScale.randomIndividualScale(this.getRandom(), this.modelScaleProfile()));
    }
}
