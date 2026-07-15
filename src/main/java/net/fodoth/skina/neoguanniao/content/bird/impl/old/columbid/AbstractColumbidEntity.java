package net.fodoth.skina.neoguanniao.content.bird.impl.old.columbid;

import net.fodoth.skina.neoguanniao.content.bath.BirdBathAttraction;
import net.fodoth.skina.neoguanniao.content.bath.BirdBathBlockEntity;
import net.fodoth.skina.neoguanniao.content.bath.BirdBathContentType;
import net.fodoth.skina.neoguanniao.content.bath.BirdBathFeedingAnimatable;
import net.fodoth.skina.neoguanniao.content.bath.BirdBathMountable;
import net.fodoth.skina.neoguanniao.content.bath.BirdBathUseGoal;
import net.fodoth.skina.neoguanniao.content.bird.feature.brain.BirdBrain;
import net.fodoth.skina.neoguanniao.content.bird.feature.brain.BirdIntent;
import net.fodoth.skina.neoguanniao.content.bird.feature.brain.BirdSpeciesProfile;
import net.fodoth.skina.neoguanniao.content.bird.impl.old.columbid.goal.*;
import net.fodoth.skina.neoguanniao.content.bird.feature.flight.BirdFlightAware;
import net.fodoth.skina.neoguanniao.content.bird.feature.flight.BirdFlightBoids;
import net.fodoth.skina.neoguanniao.content.bird.feature.flight.BirdFlightManager;
import net.fodoth.skina.neoguanniao.content.bird.feature.flight.BirdFlightProfile;
import net.fodoth.skina.neoguanniao.content.bird.feature.flight.BirdFlightTargeting;
import net.fodoth.skina.neoguanniao.content.bird.feature.scale.BirdModelScale;
import net.fodoth.skina.neoguanniao.content.bird.feature.scale.BirdModelScaleProfile;
import net.fodoth.skina.neoguanniao.content.bird.feature.scale.ScalableBirdModel;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.*;
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
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Optional;
import java.util.UUID;

public abstract class AbstractColumbidEntity extends TamableAnimal implements GeoEntity, FlyingAnimal, ScalableBirdModel, BirdFlightAware, BirdBathMountable, BirdBathFeedingAnimatable {

    // ============ 数据序列化器 ============
    private static final EntityDataAccessor<Integer> BEHAVIOR_STATE =
            SynchedEntityData.defineId(AbstractColumbidEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> MODEL_SCALE =
            SynchedEntityData.defineId(AbstractColumbidEntity.class, EntityDataSerializers.FLOAT);

    // ============ 常量 ============
    private static final double WALKING_SPEED_THRESHOLD = 0.0025;
    private static final double FLIGHT_SPEED = 0.34;
    private static final double HIGH_FLIGHT_SPEED = 0.38;
    private static final double ESCAPE_FLIGHT_SPEED = 0.44;
    private static final int AUTONOMOUS_FLIGHT_MIN_TICKS = 520;
    private static final int AUTONOMOUS_FLIGHT_RANDOM_TICKS = 300;
    private static final BirdFlightProfile FLIGHT_PROFILE = BirdFlightProfile.COLUMBID;

    // ============ 动画定义 ============
    protected static final RawAnimation IDLE_ANIMATION = RawAnimation.begin().thenLoop("idle");
    protected static final RawAnimation IDLE_DIFF_1_ANIMATION = RawAnimation.begin().thenPlay("idle_diff_1").thenLoop("idle");
    protected static final RawAnimation IDLE_DIFF_2_ANIMATION = RawAnimation.begin().thenPlay("idle_diff_2").thenLoop("idle");
    protected static final RawAnimation IDLE_DIFF_3_ANIMATION = RawAnimation.begin().thenPlay("idle_diff_3").thenLoop("idle");
    protected static final RawAnimation WALK_ANIMATION = RawAnimation.begin().thenLoop("walk");
    protected static final RawAnimation FLY_LOOP_ANIMATION = RawAnimation.begin().thenLoop("fly_loop");
    protected static final RawAnimation FLY_FLAP_ONCE_ANIMATION = RawAnimation.begin().thenPlay("fly_flapping_wing").thenLoop("fly_loop");
    protected static final RawAnimation FLY_FLAPPING_LOOP_ANIMATION = RawAnimation.begin().thenLoop("fly_flapping_wing_loop");

    // ============ 成员变量 ============
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    private final BirdBrain birdBrain;
    private ColumbidBehaviorState behaviorState;
    private ColumbidGuidePreviewAnimation guidePreviewAnimation;
    private ColumbidIdleAnimationChoice currentIdleAnimation;
    private long nextIdleAnimationSwapTick;

    public int behaviorStateLockTicks;
    protected int eatingTicks;
    public int seedTrustTicks;
    protected int foodCooldown;
    public int flightCooldown;
    protected int flightTicks;
    protected int flightDuration;
    protected int timeFlying;
    protected int flightLandingTicks;
    protected int landingSettleTicks;
    protected int flapOnceTicks;
    public int pairScanCooldown;
    public int pairLostTicks;
    public int courtshipCooldown;
    public int chaseCooldown;

    protected boolean escapeFlight;
    protected Vec3 flightTarget;
    protected double flightSpeed;
    protected double flightCruiseY;
    protected boolean highCruiseFlight;
    protected boolean autonomousCruiseFlight;
    protected Vec3 flightWaypoint;
    protected int flightWaypointTicks;

    public BlockPos homePos;
    public UUID pairPartnerUUID;

    // ============ 构造方法 ============
    protected AbstractColumbidEntity(EntityType<? extends AbstractColumbidEntity> entityType, Level level, BirdSpeciesProfile profile) {
        super(entityType, level);
        this.behaviorState = ColumbidBehaviorState.IDLE;
        this.guidePreviewAnimation = ColumbidGuidePreviewAnimation.NONE;
        this.currentIdleAnimation = ColumbidIdleAnimationChoice.BASE;
        this.flightSpeed = FLIGHT_SPEED;
        this.birdBrain = new BirdBrain(this, profile);
        this.setPathfindingMalus(PathType.LEAVES, 0.0F);
        this.setPathfindingMalus(PathType.WATER, -1.0F);
        this.setPathfindingMalus(PathType.DANGER_FIRE, 16.0F);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, 16.0F);
    }

    // ============ 静态方法 ============
    public static AttributeSupplier.Builder createColumbidAttributes(double maxHealth, double walkSpeed, double flyingSpeed, double followRange) {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, maxHealth)
                .add(Attributes.MOVEMENT_SPEED, walkSpeed)
                .add(Attributes.FLYING_SPEED, flyingSpeed)
                .add(Attributes.FOLLOW_RANGE, followRange);
    }

    public static boolean isSeedFood(ItemStack stack) {
        return !stack.isEmpty() && (stack.is(Items.WHEAT_SEEDS) || stack.is(Items.BEETROOT_SEEDS)
                || stack.is(Items.MELON_SEEDS) || stack.is(Items.PUMPKIN_SEEDS)
                || stack.is(Items.TORCHFLOWER_SEEDS) || stack.is(Items.PITCHER_POD));
    }

    public static boolean isPreferredTamingSeed(ItemStack stack) {
        return !stack.isEmpty() && (stack.is(Items.WHEAT_SEEDS) || stack.is(Items.BEETROOT_SEEDS));
    }

    protected static boolean canColumbidSpawn(ServerLevelAccessor level, BlockPos pos, RandomSource random, boolean urbanBias) {
        BlockState below = level.getBlockState(pos.below());
        boolean validGround = below.is(BlockTags.DIRT) || below.is(Blocks.GRASS_BLOCK)
                || below.is(BlockTags.SAND) || below.is(Blocks.FARMLAND);

        if (!validGround) {
            return false;
        }

        // 检查附近同类型实体数量（使用鸽形目基类）
        AABB searchBox = new AABB(
                pos.getX() - 8, pos.getY() - 4, pos.getZ() - 8,
                pos.getX() + 8, pos.getY() + 4, pos.getZ() + 8
        );
        int entityCount = level.getEntitiesOfClass(AbstractColumbidEntity.class, searchBox).size();

        // 如果附近同类型实体超过 12 个，不允许生成
        if (entityCount > 12) {
            return false;
        }

        // 计算栖息地分数
        int score = habitatScore(level, pos, urbanBias);

        if (urbanBias) {
            // 城市偏好：高分直接生成，中分概率生成
            return score >= 12 || (score >= 7 && random.nextFloat() < 0.55F);
        } else {
            // 自然偏好：高分直接生成，中分概率生成
            return score >= 16 || (score >= 9 && random.nextFloat() < 0.55F);
        }
    }

    private static int habitatScore(LevelReader level, BlockPos origin, boolean urbanBias) {
        int score = 0;
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-7, -2, -7), origin.offset(7, 5, 7))) {
            if (canReadChunk(level, pos)) {
                BlockState state = level.getBlockState(pos);
                if (!state.is(Blocks.FARMLAND) && !state.is(Blocks.DIRT_PATH) && !(state.getBlock() instanceof CropBlock)) {
                    if (!state.is(Blocks.PODZOL) && !state.is(Blocks.GRASS_BLOCK) && !(state.getBlock() instanceof ComposterBlock)) {
                        if (!state.is(BlockTags.WALLS) && !state.is(BlockTags.LEAVES)) {
                            if (!(state.getBlock() instanceof FenceBlock) && !(state.getBlock() instanceof FenceGateBlock)) {
                                if (urbanBias && (state.getBlock() instanceof DoorBlock || state.getBlock() instanceof BedBlock)) {
                                    score += 4;
                                }
                            } else {
                                score += 2;
                            }
                        } else {
                            score += urbanBias ? 1 : 3;
                        }
                    } else {
                        score += 3;
                    }
                } else {
                    score += urbanBias ? 2 : 4;
                }
                if (score >= 24) return score;
            }
        }
        return score;
    }

    @SuppressWarnings("deprecation")
    private static boolean canReadChunk(LevelReader level, BlockPos pos) {
        return level.hasChunk(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
    }

    // ============ AI 注册 ============
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new ColumbidEatSeedGoal(this));
        this.goalSelector.addGoal(2, new ColumbidFollowOwnerGoal(this, 1.0, 3.0F, 10.0F));
        this.goalSelector.addGoal(3, new ColumbidRoostGoal(this));
        this.goalSelector.addGoal(4, new ColumbidChaseSmallBirdGoal(this));
        this.goalSelector.addGoal(5, new ColumbidPairBondGoal(this));
        this.goalSelector.addGoal(6, new ColumbidFlockOrPairGoal(this));
        this.goalSelector.addGoal(7, new ColumbidCourtshipGoal(this));
        this.goalSelector.addGoal(8, new ColumbidAmbientFlightGoal(this));
        this.goalSelector.addGoal(9, new BirdBathUseGoal(
                this, 0.92, 11.0, 38,
                BirdBathAttraction::isAttractiveToColumbid,
                this::canStartSeedGoal,
                (bath) -> this.setBehaviorState(ColumbidBehaviorState.FORAGING),
                this::consumeBirdBathServing,
                (bath, consumed) -> {
                    if (this.getBehaviorState() == ColumbidBehaviorState.FORAGING) {
                        this.setBehaviorState(ColumbidBehaviorState.IDLE);
                    }
                }
        ));
        this.goalSelector.addGoal(10, new ColumbidGroundForagingGoal(this));
        this.goalSelector.addGoal(11, new ColumbidIdleGoal(this));
        this.goalSelector.addGoal(12, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(13, new RandomLookAroundGoal(this));
    }

    // ============ 导航 ============
    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level level) {
        GroundPathNavigation navigation = new GroundPathNavigation(this, level);
        navigation.setCanFloat(false);
        navigation.setCanOpenDoors(false);
        navigation.setCanPassDoors(true);
        return navigation;
    }

    // ============ 数据序列化 ============
    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(BEHAVIOR_STATE, ColumbidBehaviorState.IDLE.ordinal());
        builder.define(MODEL_SCALE, 1.0F);
    }

    @Override
    public void onSyncedDataUpdated(@NotNull EntityDataAccessor<?> key) {
        if (BEHAVIOR_STATE.equals(key)) {
            this.behaviorState = decodeBehaviorState(this.getEntityData().get(BEHAVIOR_STATE));
        }
        super.onSyncedDataUpdated(key);
    }

    // ============ 生成 ============
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

        this.randomizeModelScale();

        return data;
    }

    // ============ Tick ============
    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide) {
            this.birdBrain.tick();
            this.tickCounters();
            this.tickFlight();
            this.tickWaterEscape();
            this.tickBehaviorFallback();
            this.tickGroundMovementFacing();
        }
    }

    // ============ 交互 ============
    @Override
    public @NotNull InteractionResult mobInteract(Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!isSeedFood(stack)) {
            return super.mobInteract(player, hand);
        }
        if (this.level().isClientSide) {
            return InteractionResult.sidedSuccess(true);
        }

        ItemStack offeredStack = stack.copy();
        float chance = this.tamingChance(offeredStack);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        this.birdBrain.onEat(0.18F);
        this.triggerEatingAnimation(28);

        if (this.isTame()) {
            this.seedTrustTicks = Math.max(this.seedTrustTicks, 700);
            if (this.getHealth() < this.getMaxHealth()) {
                this.heal(2.0F);
            }
            this.spawnTrustParticles(true);
            return InteractionResult.SUCCESS;
        }

        if (this.getRandom().nextFloat() < chance) {
            this.tame(player);
            this.level().broadcastEntityEvent(this, (byte) 7);
            this.homePos = this.blockPosition();
            this.spawnTrustParticles(true);
        } else {
            this.spawnTrustParticles(false);
        }

        this.seedTrustTicks = Math.max(this.seedTrustTicks, 700);
        return InteractionResult.SUCCESS;
    }

    protected float tamingChance(ItemStack stack) {
        float chance = isPreferredTamingSeed(stack) ? 0.22F : 0.12F;
        if (this.seedTrustTicks > 0) {
            chance += 0.22F;
        }
        return this.supportsPairBond() ? chance : chance * 0.65F;
    }

    // ============ 受伤 ============
    @Override
    public boolean hurt(@NotNull DamageSource damageSource, float amount) {
        boolean hurt = super.hurt(damageSource, amount);
        if (hurt && !this.level().isClientSide) {
            this.birdBrain.onFrightened(0.45F);
            Entity source = damageSource.getEntity();
            if (source instanceof Player player && this.isOwnedBy(player)) {
                this.getNavigation().stop();
                this.setBehaviorStateFor(ColumbidBehaviorState.ALERT, 45);
                return true;
            }
            this.startEscapeFlight(source == null ? this.position() : source.position());
        }
        return hurt;
    }

    // ============ 其他实体方法 ============
    @Override
    public boolean isFood(@NotNull ItemStack stack) {
        return isSeedFood(stack);
    }

    @Override
    public boolean isTame() {
        return false;
    }

    @Override
    public boolean isFlying() {
        return this.shouldPlayFlyAnimation();
    }

    @Override
    public boolean isInWater() {
        return this.isControlledFlightActive() || super.isInWater();
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float damageMultiplier, @NotNull DamageSource damageSource) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, @NotNull BlockState state, @NotNull BlockPos pos) {
        this.fallDistance = 0.0F;
    }

    // ============ NBT ============
    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        this.birdBrain.save(compoundTag);
        BirdModelScale.save(compoundTag, this.getIndividualModelScale(), this.modelScaleProfile());
        if (this.homePos != null) {
            compoundTag.putInt("HomeX", this.homePos.getX());
            compoundTag.putInt("HomeY", this.homePos.getY());
            compoundTag.putInt("HomeZ", this.homePos.getZ());
        }
        if (this.pairPartnerUUID != null) {
            compoundTag.putUUID("PairPartner", this.pairPartnerUUID);
        }
        compoundTag.putInt("SeedTrustTicks", this.seedTrustTicks);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.birdBrain.load(compoundTag);
        if (compoundTag.contains("BirdModelScale", CompoundTag.TAG_FLOAT)) {
            this.setIndividualModelScale(BirdModelScale.load(compoundTag, this.modelScaleProfile()));
        } else {
            this.randomizeModelScale();
        }
        if (compoundTag.contains("HomeX", CompoundTag.TAG_INT)
                && compoundTag.contains("HomeY", CompoundTag.TAG_INT)
                && compoundTag.contains("HomeZ", CompoundTag.TAG_INT)) {
            this.homePos = new BlockPos(
                    compoundTag.getInt("HomeX"),
                    compoundTag.getInt("HomeY"),
                    compoundTag.getInt("HomeZ")
            );
        }
        this.pairPartnerUUID = compoundTag.hasUUID("PairPartner")
                ? compoundTag.getUUID("PairPartner") : null;
        this.seedTrustTicks = compoundTag.getInt("SeedTrustTicks");
        this.clearFlightState();
    }

    // ============ 缩放接口 ============
    @Override
    public BirdModelScaleProfile modelScaleProfile() {
        return BirdModelScaleProfile.COLUMBID;
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
        return FLIGHT_PROFILE;
    }

    @Override
    public boolean isBirdFlightActive() {
        ColumbidBehaviorState state = this.getBehaviorState();
        return this.isControlledFlightActive() || state.isAirborne() && (!this.onGround() || super.isInWater());
    }

    @Override
    public boolean isBirdLanding() {
        return this.flightLandingTicks > 0;
    }

    @Override
    public boolean isBirdEscaping() {
        return this.escapeFlight;
    }

    // ============ 行为状态 ============
    public ColumbidBehaviorState getBehaviorState() {
        return decodeBehaviorState(this.getEntityData().get(BEHAVIOR_STATE));
    }

    public void setBehaviorState(ColumbidBehaviorState state) {
        if (state == null) {
            state = ColumbidBehaviorState.IDLE;
        }
        this.behaviorState = state;
        this.getEntityData().set(BEHAVIOR_STATE, state.ordinal());
    }

    public void setBehaviorStateFor(ColumbidBehaviorState state, int ticks) {
        this.setBehaviorState(state);
        this.behaviorStateLockTicks = Math.max(this.behaviorStateLockTicks, ticks);
    }

    public void setGuidePreviewAnimation(ColumbidGuidePreviewAnimation guidePreviewAnimation) {
        this.guidePreviewAnimation = guidePreviewAnimation == null
                ? ColumbidGuidePreviewAnimation.NONE : guidePreviewAnimation;
    }

    // ============ 变体和纹理 ============
    public ColumbidVariant getColumbidVariant() {
        return ColumbidVariant.SPOTTED_DOVE;
    }

    public ResourceLocation getTextureResource() {
        return this.getColumbidVariant().texture();
    }

    // ============ 可重写方法 ============
    public boolean sensesIncomingBadWeather() {
        return this.usesWeatherSense() && (this.level().isRaining() || this.level().isThundering());
    }

    protected boolean usesWeatherSense() {
        return false;
    }

    public boolean supportsPairBond() {
        return false;
    }

    public boolean supportsChasing() {
        return false;
    }

    public boolean prefersHumanSettlements() {
        return false;
    }

    public int ambientFlightChance() {
        int chance = this.prefersHumanSettlements() ? 70 : 92;
        if (this.isTame()) chance += 180;
        if (this.level().isRaining()) chance += 260;
        if (this.birdBrain.wantsForage()) chance += 120;
        return chance;
    }

    protected boolean isActiveTime() {
        long time = this.level().getDayTime() % 24000L;
        return time >= 23000L || time < 12500L;
    }

    public boolean isRoostTime() {
        return !this.isActiveTime();
    }

    protected abstract AbstractColumbidEntity createChildEntity(ServerLevel level);

    // ============ 声音 ============
    @Override
    protected SoundEvent getAmbientSound() {
        return NeoGuanNiaoSoundEvents.PIGEON_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return SoundEvents.GENERIC_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.GENERIC_DEATH;
    }

    @Override
    public int getAmbientSoundInterval() {
        if (this.sensesIncomingBadWeather()) {
            return 360;
        }
        return this.level().isDay() ? 190 : 320;
    }

    @Override
    public float getVoicePitch() {
        return 0.32F;
    }

    @Override
    protected void playStepSound(@NotNull BlockPos pos, @NotNull BlockState blockState) {
        this.playSound(SoundEvents.CHICKEN_STEP, 0.09F, 0.86F);
    }

    // ============ 繁殖 ============
    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel level, @NotNull AgeableMob mate) {
        AbstractColumbidEntity child = this.createChildEntity(level);
        if (child != null) {
            float mateScale = mate instanceof AbstractColumbidEntity other
                    ? other.getIndividualModelScale()
                    : this.getIndividualModelScale();
            child.setIndividualModelScale(BirdModelScale.inheritIndividualScale(
                    child.getRandom(), this.getIndividualModelScale(), mateScale, child.modelScaleProfile()));
        }
        return child;
    }

    // ============ 飞行方法 ============
    public boolean startFlybyFlight(Vec3 landingTarget, int ticks) {
        if (landingTarget == null) return false;
        int duration = Math.max(ticks, AUTONOMOUS_FLIGHT_MIN_TICKS + this.getRandom().nextInt(AUTONOMOUS_FLIGHT_RANDOM_TICKS));
        return this.startAutonomousCruiseFlight(landingTarget, duration);
    }

    @Override
    public boolean startBirdBathMountFlight(Vec3 standPosition) {
        if (standPosition == null || this.isControlledFlightActive()) return false;

        Vec3 horizontal = standPosition.subtract(this.position()).multiply(1.0, 0.0, 1.0);
        if (horizontal.lengthSqr() <= 1.0E-4) {
            horizontal = Vec3.ZERO;
        } else {
            horizontal = horizontal.normalize().scale(0.28);
        }

        Vec3 movement = new Vec3(horizontal.x, 0.66, horizontal.z);
        this.getNavigation().stop();
        this.setNoGravity(false);
        this.setSilent(false);
        this.flapOnceTicks = Math.max(this.flapOnceTicks, 24);
        this.setBehaviorStateFor(ColumbidBehaviorState.FLAP_FLYING, 34);
        this.setDeltaMovement(movement);
        this.faceFlightDirection(movement);
        this.xxa = 0.0F;
        this.hasImpulse = true;
        return true;
    }

    @Override
    public void startBirdBathFeedingAnimation(BirdBathContentType contentType, int ticks) {
        this.getNavigation().stop();
        if (contentType.isFood()) {
            this.triggerEatingAnimation(Math.max(32, ticks));
        } else {
            this.triggerPeckAnimation(Math.max(24, ticks / 2));
        }
    }

    public boolean startAutonomousCruiseFlight(Vec3 landingTarget, int duration) {
        return this.startControlledFlight(landingTarget, duration, HIGH_FLIGHT_SPEED, false, true, true);
    }

    // ============ 私有辅助方法 ============
    private static ColumbidBehaviorState decodeBehaviorState(int ordinal) {
        ColumbidBehaviorState[] values = ColumbidBehaviorState.values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : ColumbidBehaviorState.IDLE;
    }

    void randomizeModelScale() {
        this.setIndividualModelScale(BirdModelScale.randomIndividualScale(this.getRandom(), this.modelScaleProfile()));
    }

    private boolean isOwnedBy(Player player) {
        return this.isTame() && this.getOwner() != null && this.getOwner().equals(player);
    }

    private void spawnTrustParticles(boolean success) {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    success ? ParticleTypes.HEART : ParticleTypes.SMOKE,
                    this.getX(), this.getY() + 0.6, this.getZ(),
                    6, 0.25, 0.25, 0.25, 0.02
            );
        }
    }

    public void spawnCourtshipParticles(int count) {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.HEART,
                    this.getX(), this.getY() + 0.7, this.getZ(),
                    count, 0.25, 0.25, 0.25, 0.01
            );
        }
    }

    public void triggerEatingAnimation(int ticks) {
        this.eatingTicks = Math.max(this.eatingTicks, ticks);
        this.currentIdleAnimation = this.getRandom().nextBoolean()
                ? ColumbidIdleAnimationChoice.PECK_1
                : ColumbidIdleAnimationChoice.PECK_2;
        this.nextIdleAnimationSwapTick = this.level().getGameTime() + ticks;
        this.setBehaviorStateFor(ColumbidBehaviorState.EATING, ticks);
        this.foodCooldown = Math.max(this.foodCooldown, 45 + this.getRandom().nextInt(45));
    }

    private void consumeBirdBathServing(BirdBathBlockEntity bath, BirdBathContentType contentType) {
        if (contentType == BirdBathContentType.BREAD) {
            this.birdBrain.onEat(0.24F);
            this.triggerEatingAnimation(30 + this.getRandom().nextInt(14));
        } else {
            this.birdBrain.onEat(0.12F);
            this.eatingTicks = Math.max(this.eatingTicks, 22 + this.getRandom().nextInt(12));
            this.currentIdleAnimation = ColumbidIdleAnimationChoice.PECK_1;
            this.nextIdleAnimationSwapTick = this.level().getGameTime() + this.eatingTicks;
            this.setBehaviorStateFor(ColumbidBehaviorState.EATING, this.eatingTicks);
            this.foodCooldown = Math.max(this.foodCooldown, 55 + this.getRandom().nextInt(45));
            this.playSound(SoundEvents.PARROT_EAT, 0.28F, 0.9F + this.getRandom().nextFloat() * 0.12F);
        }
    }

    public void triggerPeckAnimation(int ticks) {
        this.currentIdleAnimation = this.getRandom().nextBoolean()
                ? ColumbidIdleAnimationChoice.PECK_1
                : ColumbidIdleAnimationChoice.PECK_2;
        this.nextIdleAnimationSwapTick = this.level().getGameTime() + ticks;
        this.setBehaviorStateFor(ColumbidBehaviorState.FORAGING, ticks);
    }

    public boolean canStartSeedGoal() {
        return this.foodCooldown <= 0 && !this.isControlledFlightActive()
                && !this.isRoostTime() && !this.isInWater()
                && this.getTarget() == null && !this.getBehaviorState().isEscape();
    }

    public boolean canStartGroundSocialGoal() {
        return this.isActiveTime() && this.onGround() && !this.isControlledFlightActive()
                && this.landingSettleTicks <= 0 && !this.isInWater()
                && !this.getBehaviorState().isEscape() && this.getTarget() == null;
    }

    private void tickBehaviorFallback() {
        if (!this.isControlledFlightActive()) {
            if (this.behaviorStateLockTicks <= 0) {
                if (!this.onGround() && !this.isInWater()) {
                    this.setBehaviorState(ColumbidBehaviorState.FLAP_FLYING);
                } else {
                    ColumbidBehaviorState state = this.getBehaviorState();
                    if (state != ColumbidBehaviorState.ROOSTING && state != ColumbidBehaviorState.SLEEPING) {
                        if (this.isRoostTime() && this.getNavigation().isDone()) {
                            this.setBehaviorState(ColumbidBehaviorState.SLEEPING);
                            if (this.tickCount % 80 == 0) {
                                this.birdBrain.onRest(0.02F);
                            }
                        } else {
                            BirdIntent intent = this.birdBrain.currentIntent();
                            if (!this.getNavigation().isDone() || (intent != BirdIntent.ALERT && intent != BirdIntent.WATCH)) {
                                if (!this.getNavigation().isDone()) {
                                    this.setBehaviorState(this.birdBrain.wantsForage()
                                            ? ColumbidBehaviorState.FORAGING
                                            : ColumbidBehaviorState.WALKING);
                                } else {
                                    this.setBehaviorState(this.birdBrain.wantsForage()
                                            ? ColumbidBehaviorState.FORAGING
                                            : ColumbidBehaviorState.IDLE);
                                }
                            } else {
                                this.setBehaviorState(ColumbidBehaviorState.ALERT);
                            }
                        }
                    } else {
                        if (!this.isRoostTime()) {
                            this.setBehaviorState(ColumbidBehaviorState.IDLE);
                        }
                    }
                }
            }
        }
    }

    private void tickCounters() {
        if (this.behaviorStateLockTicks > 0) --this.behaviorStateLockTicks;
        if (this.eatingTicks > 0) {
            --this.eatingTicks;
            if (this.eatingTicks <= 0 && this.getBehaviorState() == ColumbidBehaviorState.EATING) {
                this.setBehaviorState(ColumbidBehaviorState.FORAGING);
            }
        }
        if (this.seedTrustTicks > 0) --this.seedTrustTicks;
        if (this.foodCooldown > 0) --this.foodCooldown;
        if (this.flightCooldown > 0) --this.flightCooldown;
        if (this.landingSettleTicks > 0) --this.landingSettleTicks;
        if (this.flapOnceTicks > 0) --this.flapOnceTicks;
        if (this.pairScanCooldown > 0) --this.pairScanCooldown;
        if (this.courtshipCooldown > 0) --this.courtshipCooldown;
        if (this.chaseCooldown > 0) --this.chaseCooldown;
    }

    // ============ 飞行控制 ============
    private void startEscapeFlight(Vec3 threatPosition) {
        if (!this.isControlledFlightActive() && this.flightCooldown <= 0) {
            Vec3 away = this.position().subtract(threatPosition).multiply(1.0, 0.0, 1.0);
            if (away.lengthSqr() <= 1.0E-4) {
                away = this.randomHorizontalDirection();
            }
            Vec3 target = this.findFlightLandingTarget(away.normalize(), 18, 34, true);
            if (target == null) {
                target = this.position().add(away.normalize().scale(22.0)).add(0, 4.0, 0);
            }
            this.startControlledFlight(target, 105 + this.getRandom().nextInt(65), ESCAPE_FLIGHT_SPEED, true, true);
        }
    }

    private boolean startControlledFlight(Vec3 target, int duration, double speed, boolean escape) {
        return this.startControlledFlight(target, duration, speed, escape, escape);
    }

    public boolean startControlledFlight(Vec3 target, int duration, double speed, boolean escape, boolean highCruise) {
        return this.startControlledFlight(target, duration, speed, escape, highCruise, false);
    }

    private boolean startControlledFlight(Vec3 target, int duration, double speed, boolean escape, boolean highCruise, boolean autonomousCruise) {
        if (target == null) return false;

        this.flightTarget = target;
        this.flightTicks = duration;
        this.flightDuration = duration;
        this.timeFlying = 0;
        this.flightLandingTicks = 0;
        this.flightSpeed = speed;
        this.escapeFlight = escape;
        this.highCruiseFlight = highCruise;
        this.autonomousCruiseFlight = autonomousCruise;

        double horizontalDistance = target.subtract(this.position()).multiply(1.0, 0.0, 1.0).length();
        double clearance = highCruise
                ? Mth.clamp(FLIGHT_PROFILE.minCruiseHeight() + horizontalDistance * 0.18,
                FLIGHT_PROFILE.minCruiseHeight(), escape ? 22.0 : FLIGHT_PROFILE.maxCruiseHeight())
                : Mth.clamp(5.0 + horizontalDistance * 0.1, 6.0, 10.0);
        double minimumCruiseY = this.getY() + (highCruise ? (this.onGround() ? 7.0 : 2.0) : 3.5);
        this.flightCruiseY = Math.max(target.y + clearance, minimumCruiseY);

        Vec3 initialDirection = target.subtract(this.position()).multiply(1.0, 0.0, 1.0);
        if (initialDirection.lengthSqr() <= 1.0E-4) {
            initialDirection = this.getLookAngle().multiply(1.0, 0.0, 1.0);
        }
        if (initialDirection.lengthSqr() <= 1.0E-4) {
            initialDirection = this.randomHorizontalDirection();
        }

        this.flightWaypoint = autonomousCruise ? this.chooseAutonomousFlightWaypoint(initialDirection.normalize()) : null;
        this.flightWaypointTicks = autonomousCruise ? 48 + this.getRandom().nextInt(76) : 0;
        this.flapOnceTicks = 0;
        this.getNavigation().stop();
        this.setNoGravity(true);
        this.setSilent(false);
        this.setBehaviorStateFor(escape ? ColumbidBehaviorState.FLEEING : ColumbidBehaviorState.FLAP_FLYING,
                escape ? 120 : (autonomousCruise ? 140 : (highCruise ? 105 : 70)));
        this.flightCooldown = Math.max(this.flightCooldown,
                escape ? 160 : (autonomousCruise ? 520 : (highCruise ? 260 : 180)));

        Vec3 movement = initialDirection.normalize().scale(speed * (highCruise ? 0.9 : 0.7))
                .add(0, escape ? 0.64 : (highCruise ? 0.52 : 0.28), 0);
        this.setDeltaMovement(movement);
        this.faceFlightDirection(movement);
        this.xxa = 0.0F;
        this.hasImpulse = true;
        return true;
    }

    public boolean isControlledFlightActive() {
        return this.flightTarget != null && (this.flightTicks > 0 || !this.onGround());
    }

    private void tickFlight() {
        if (!this.isControlledFlightActive()) {
            this.timeFlying = 0;
            if (this.isInWater()) {
                this.setNoGravity(false);
            }
            return;
        }

        this.getNavigation().stop();
        this.setNoGravity(true);
        ++this.timeFlying;
        --this.flightTicks;

        Vec3 toLanding = this.flightTarget.subtract(this.position());
        double landingDistance = toLanding.length();
        double horizontalDistance = Math.sqrt(toLanding.x * toLanding.x + toLanding.z * toLanding.z);
        int flightAge = this.flightDuration - this.flightTicks;

        if (this.onGround() && flightAge > 8) {
            this.finishControlledFlight(true);
            return;
        }
        if ((!this.onGround() || flightAge <= 14 || !(horizontalDistance < 1.6))
                && (!(landingDistance < 0.95) || !this.onGround())) {

            if (this.onGround() && flightAge > 18 && this.getDeltaMovement().y <= 0.03) {
                this.finishControlledFlight(false);
                return;
            }

            int landingWindow = this.autonomousCruiseFlight
                    ? Mth.clamp(this.flightDuration / 7, 64, 100)
                    : (this.highCruiseFlight ? Math.max(44, this.flightDuration / 3) : Math.max(24, this.flightDuration / 4));
            boolean landingPhase = this.flightTicks <= landingWindow
                    || (!this.autonomousCruiseFlight && horizontalDistance < (this.highCruiseFlight ? 7.0 : 3.2));

            if (this.autonomousCruiseFlight && !landingPhase) {
                this.updateAutonomousFlightWaypoint();
            }

            Vec3 steeringTarget = landingPhase ? this.flightTarget
                    : (this.autonomousCruiseFlight && this.flightWaypoint != null
                    ? this.flightWaypoint
                    : new Vec3(this.flightTarget.x, this.flightCruiseY, this.flightTarget.z));
            Vec3 toSteeringTarget = steeringTarget.subtract(this.position());
            Vec3 horizontal = new Vec3(toSteeringTarget.x, 0, toSteeringTarget.z);
            double heightAboveLanding = this.getY() - this.flightTarget.y;
            boolean closeLandingApproach = landingPhase && horizontalDistance < 1.25F;

            if (horizontal.lengthSqr() <= 1.0E-4) {
                horizontal = this.getDeltaMovement().multiply(1.0, 0.0, 1.0);
            }
            if (landingPhase && horizontalDistance < 2.6 && heightAboveLanding > 4.0) {
                Vec3 drift = this.getDeltaMovement().multiply(1.0, 0.0, 1.0);
                if (drift.lengthSqr() > 1.0E-4) {
                    horizontal = drift;
                }
            }
            if (closeLandingApproach && horizontalDistance < 0.55) {
                horizontal = this.getDeltaMovement().multiply(1.0, 0.0, 1.0);
            }
            if (horizontal.lengthSqr() <= 1.0E-4) {
                horizontal = closeLandingApproach ? Vec3.ZERO : this.randomHorizontalDirection();
            }

            boolean hasHorizontalSteering = horizontal.lengthSqr() > 1.0E-4;
            if (hasHorizontalSteering) {
                horizontal = horizontal.normalize();
            }

            if (!landingPhase) {
                Vec3 flockHeading = BirdFlightBoids.sameTypeHeading(this, 30.0, 4.6, 0.03, 0.52, 0.11,
                        this.escapeFlight ? 0.2 : 0.09);
                if (flockHeading.lengthSqr() > 1.0E-4) {
                    horizontal = BirdFlightTargeting.normalizeHorizontal(horizontal.add(flockHeading), horizontal);
                }
            }

            double speed = getSpeed(landingPhase, horizontalDistance, closeLandingApproach);

            double lift = Mth.clamp(toSteeringTarget.y * (landingPhase ? 0.1 : 0.12),
                    landingPhase ? -0.16 : (this.autonomousCruiseFlight ? -0.045 : -0.025),
                    this.highCruiseFlight ? 0.24 : 0.17);
            if (!landingPhase && this.getY() < this.flightCruiseY - 1.0) {
                lift = Math.max(lift, Mth.clamp((this.flightCruiseY - this.getY()) * 0.08, 0.1,
                        this.escapeFlight ? 0.25 : 0.21));
            }

            if (flightAge < 10) {
                lift += this.escapeFlight ? 0.22 : 0.14;
            }

            if (this.flightTicks <= 0) {
                if (this.onGround()) {
                    this.finishControlledFlight(true);
                    return;
                }
                ++this.flightLandingTicks;
                this.flightTicks = 1;
                if (this.flightLandingTicks == 1 || this.flightLandingTicks % 24 == 0 || this.flightTarget == null) {
                    Vec3 landing = this.findNearestDryLandingTarget(this.flightLandingTicks > 80 ? 24 : 14);
                    if (landing != null) {
                        this.flightTarget = landing;
                    }
                }
                lift = Math.min(lift, this.flightLandingTicks > 90 ? -0.065 : -0.045);
                if (this.getY() < this.flightTarget.y + 1.35) {
                    lift = Math.max(lift, -0.026);
                }
            }

            Vec3 desired = (hasHorizontalSteering ? horizontal.scale(speed) : Vec3.ZERO).add(0, lift, 0);
            Vec3 movement = this.getDeltaMovement().scale(0.42).add(desired.scale(0.58));

            if (!landingPhase && BirdFlightManager.isStalledInAir(this, this.timeFlying, 0.008)) {
                Vec3 nextWaypoint = this.chooseAutonomousFlightWaypoint(horizontal);
                if (nextWaypoint != null) {
                    this.flightWaypoint = nextWaypoint;
                    this.flightWaypointTicks = 44 + this.getRandom().nextInt(48);
                    this.flightCruiseY = Mth.clamp(nextWaypoint.y, this.level().getMinBuildHeight() + 8.0,
                            this.level().getMaxBuildHeight() - 8.0);
                }
                movement = horizontal.scale(Math.max(speed, this.flightSpeed * 0.72))
                        .add(0, this.highCruiseFlight ? 0.16 : 0.1, 0);
            }

            if (!this.escapeFlight && flightAge > 22 && movement.y <= 0.04 && this.getRandom().nextInt(34) == 0) {
                this.flapOnceTicks = 18;
            }

            this.setBehaviorState(this.escapeFlight ? ColumbidBehaviorState.FLEEING
                    : (movement.y < 0.03 && movement.lengthSqr() > 0.035 && this.flapOnceTicks <= 0
                    ? ColumbidBehaviorState.GLIDING : ColumbidBehaviorState.FLAP_FLYING));

            this.setDeltaMovement(movement);
            this.faceFlightDirection(movement);
            this.xxa = 0.0F;
            this.hasImpulse = true;
        } else {
            this.finishControlledFlight(true);
        }
    }

    private double getSpeed(boolean landingPhase, double horizontalDistance, boolean closeLandingApproach) {
        double speed = landingPhase
                ? BirdFlightManager.decelerateNearLanding(this.flightSpeed, horizontalDistance,
                this.highCruiseFlight ? 7.0 : 4.0, 0.22)
                : this.flightSpeed;
        if (closeLandingApproach) {
            speed = Math.min(speed, 0.035 + horizontalDistance * 0.06);
        }
        if (this.highCruiseFlight && !landingPhase) {
            speed = this.autonomousCruiseFlight ? Math.min(speed, 0.38) : Math.min(speed + 0.015, 0.44);
        }
        return speed;
    }

    private void finishControlledFlight(boolean landed) {
        this.flightTarget = null;
        this.flightTicks = 0;
        this.flightDuration = 0;
        this.timeFlying = 0;
        this.flightLandingTicks = 0;
        this.escapeFlight = false;
        this.highCruiseFlight = false;
        this.autonomousCruiseFlight = false;
        this.flightWaypoint = null;
        this.flightWaypointTicks = 0;
        this.flightCruiseY = 0.0;
        this.flapOnceTicks = 0;
        this.setNoGravity(false);
        this.getNavigation().stop();

        if (landed) {
            this.landingSettleTicks = Math.max(this.landingSettleTicks, 18);
            this.setDeltaMovement(Vec3.ZERO);
        } else {
            Vec3 movement = this.getDeltaMovement();
            this.setDeltaMovement(movement.x * 0.35, Math.max(movement.y * 0.3, -0.04), movement.z * 0.35);
        }

        if (this.getBehaviorState().isAirborne()) {
            this.setBehaviorStateFor(ColumbidBehaviorState.ALERT, 24);
        }
    }

    private void clearFlightState() {
        this.flightTarget = null;
        this.flightTicks = 0;
        this.flightDuration = 0;
        this.timeFlying = 0;
        this.flightLandingTicks = 0;
        this.escapeFlight = false;
        this.highCruiseFlight = false;
        this.autonomousCruiseFlight = false;
        this.flightWaypoint = null;
        this.flightWaypointTicks = 0;
        this.flightCruiseY = 0.0;
        this.flapOnceTicks = 0;
        this.landingSettleTicks = 0;
        this.setNoGravity(false);
    }

    private void tickWaterEscape() {
        if (this.isInWater()) {
            this.getNavigation().stop();
            Vec3 target = this.findNearestDryLandingTarget(14);
            if (target != null) {
                this.startControlledFlight(target, 85 + this.getRandom().nextInt(45), ESCAPE_FLIGHT_SPEED, true, true);
            } else {
                Vec3 movement = this.getDeltaMovement().multiply(0.4, 0, 0.4).add(0, 0.32, 0);
                this.setNoGravity(true);
                this.setBehaviorStateFor(ColumbidBehaviorState.FLAP_FLYING, 35);
                this.setDeltaMovement(movement);
                this.faceFlightDirection(movement);
                this.hasImpulse = true;
            }
        }
    }

    private void updateAutonomousFlightWaypoint() {
        Vec3 waypoint = this.flightWaypoint;
        boolean shouldPickNewWaypoint = waypoint == null || --this.flightWaypointTicks <= 0;
        if (!shouldPickNewWaypoint) {
            Vec3 toWaypoint = waypoint.subtract(this.position());
            double horizontalDistance = Math.sqrt(toWaypoint.x * toWaypoint.x + toWaypoint.z * toWaypoint.z);
            shouldPickNewWaypoint = horizontalDistance < (this.autonomousCruiseFlight ? 8.0 : 5.0)
                    || Math.abs(toWaypoint.y) > FLIGHT_PROFILE.maxVerticalStep() + 3.0;
        }
        if (shouldPickNewWaypoint) {
            Vec3 direction = this.getDeltaMovement().multiply(1.0, 0.0, 1.0);
            if (direction.lengthSqr() <= 1.0E-4) {
                direction = this.getLookAngle().multiply(1.0, 0.0, 1.0);
            }
            if (direction.lengthSqr() <= 1.0E-4) {
                direction = this.randomHorizontalDirection();
            }
            Vec3 nextWaypoint = this.chooseAutonomousFlightWaypoint(direction.normalize());
            if (nextWaypoint != null) {
                this.flightWaypoint = nextWaypoint;
                this.flightWaypointTicks = 54 + this.getRandom().nextInt(86);
                this.flightCruiseY = Mth.clamp(nextWaypoint.y, this.level().getMinBuildHeight() + 8.0,
                        this.level().getMaxBuildHeight() - 8.0);
            } else {
                this.flightWaypointTicks = 24;
            }
        }
    }

    private Vec3 chooseAutonomousFlightWaypoint(Vec3 preferredDirection) {
        return BirdFlightTargeting.findAirTarget(this, FLIGHT_PROFILE, preferredDirection, this.escapeFlight);
    }

    public Vec3 findFlightLandingTarget(Vec3 direction, int minRadius, int maxRadius, boolean high) {
        return BirdFlightTargeting.findLandingInDirection(this, direction, minRadius, maxRadius,
                high ? 8 : 5, high ? 22 : 12);
    }

    private Vec3 findNearestDryLandingTarget(int radius) {
        return BirdFlightTargeting.findNearestDryLandingTarget(this, radius, 16);
    }

    public Vec3 findDryLandingTarget(BlockPos center, int verticalRange) {
        return BirdFlightTargeting.findDryLandingTarget(this, center, verticalRange);
    }

    public Vec3 findDryLandingTargetNear(BlockPos center, int horizontalRange, int verticalRange) {
        return BirdFlightTargeting.findDryLandingTargetNear(this, center, horizontalRange, verticalRange);
    }

    public Vec3 findGroundStrollTarget(int horizontalRange, int verticalRange) {
        for (int attempt = 0; attempt < 10; ++attempt) {
            Vec3 candidate = LandRandomPos.getPos(this, horizontalRange, verticalRange);
            if (candidate != null) {
                BlockPos pos = BlockPos.containing(candidate);
                if (this.isSafeDryLanding(pos)) {
                    return Vec3.atBottomCenterOf(pos).add(0, 0.05, 0);
                }
            }
        }
        return null;
    }

    public boolean isSafeDryLanding(BlockPos pos) {
        return BirdFlightTargeting.isSafeDryLanding(this, pos);
    }

    // ============ 配对相关 ============
    public Optional<AbstractColumbidEntity> pairPartner() {
        if (this.pairPartnerUUID != null && this.level() instanceof ServerLevel serverLevel) {
            Entity entity = serverLevel.getEntity(this.pairPartnerUUID);
            if (entity instanceof AbstractColumbidEntity columbid
                    && columbid.isAlive() && columbid.getClass() == this.getClass()) {
                return Optional.of(columbid);
            }
        }
        return Optional.empty();
    }

    public boolean isPairedWith(Entity entity) {
        return entity != null && this.pairPartnerUUID != null
                && this.pairPartnerUUID.equals(entity.getUUID());
    }

    public boolean hasReciprocalPairWith(AbstractColumbidEntity other) {
        return other != null && this.pairPartnerUUID != null
                && this.pairPartnerUUID.equals(other.getUUID())
                && other.pairPartnerUUID != null
                && other.pairPartnerUUID.equals(this.getUUID());
    }

    // ============ 朝向控制 ============
    private void faceFlightDirection(Vec3 movement) {
        BirdFlightManager.faceMovement(this, movement, FLIGHT_PROFILE.maxPitchDegrees());
    }

    private void tickGroundMovementFacing() {
        if (this.shouldFaceGroundMovement()) {
            BirdFlightManager.faceGroundMovement(this, this.getDeltaMovement(), 1.0E-4);
        }
    }

    private boolean shouldFaceGroundMovement() {
        if (this.onGround() && !this.isControlledFlightActive() && !this.isInWater() && !this.isVehicle()) {
            ColumbidBehaviorState state = this.getBehaviorState();
            if (!state.isAirborne() && state != ColumbidBehaviorState.EATING
                    && state != ColumbidBehaviorState.PREENING && state != ColumbidBehaviorState.COURTING
                    && state != ColumbidBehaviorState.ROOSTING && state != ColumbidBehaviorState.SLEEPING) {
                return this.getDeltaMovement().lengthSqr() > WALKING_SPEED_THRESHOLD
                        || !this.getNavigation().isDone();
            }
        }
        return false;
    }

    // ============ 随机工具 ============
    public Vec3 randomHorizontalDirection() {
        return BirdFlightTargeting.randomHorizontalDirection(this.getRandom());
    }

    public double randomSigned(double value) {
        return (this.getRandom().nextDouble() * 2.0 - 1.0) * value;
    }

    public static Vec3 rotateHorizontal(Vec3 direction, double angle) {
        return BirdFlightTargeting.rotateHorizontal(direction, angle);
    }

    // ============ 动画 ============
    private RawAnimation pickIdleAnimation() {
        ColumbidBehaviorState state = this.getBehaviorState();

        if (state != ColumbidBehaviorState.EATING && this.eatingTicks <= 0) {
            if (state == ColumbidBehaviorState.PREENING) {
                return IDLE_DIFF_1_ANIMATION;
            }
            if (state == ColumbidBehaviorState.CURIOUS || state == ColumbidBehaviorState.ALERT) {
                return IDLE_DIFF_3_ANIMATION;
            }
            if (state == ColumbidBehaviorState.COURTING) {
                return IDLE_DIFF_3_ANIMATION;
            }
            if (state == ColumbidBehaviorState.ROOSTING || state == ColumbidBehaviorState.SLEEPING) {
                return IDLE_ANIMATION;
            }

            if (this.level().getGameTime() >= this.nextIdleAnimationSwapTick) {
                this.currentIdleAnimation = this.chooseIdleAnimation();
                this.nextIdleAnimationSwapTick = this.level().getGameTime()
                        + this.currentIdleAnimation.nextDuration(this.getRandom());
            }
        } else {
            if (this.currentIdleAnimation != ColumbidIdleAnimationChoice.PECK_1
                    && this.currentIdleAnimation != ColumbidIdleAnimationChoice.PECK_2) {
                this.currentIdleAnimation = this.getRandom().nextBoolean()
                        ? ColumbidIdleAnimationChoice.PECK_1
                        : ColumbidIdleAnimationChoice.PECK_2;
            }
        }
        return this.currentIdleAnimation.animation;
    }

    private boolean shouldPlayFlyAnimation() {
        boolean airborneState = this.getBehaviorState().isAirborne()
                && (!this.onGround() || this.isControlledFlightActive() || super.isInWater());
        return BirdFlightManager.shouldPlayFlyAnimation(this, airborneState, this.onGround(),
                this.isInWater(), this.getDeltaMovement(), 0);
    }

    private boolean shouldPlayWalkAnimation() {
        ColumbidBehaviorState state = this.getBehaviorState();
        if (state == ColumbidBehaviorState.WALKING || state == ColumbidBehaviorState.FOLLOWING_OWNER
                || state == ColumbidBehaviorState.PAIR_FOLLOWING || state == ColumbidBehaviorState.CHASING) {
            return true;
        }
        if (this.getDeltaMovement().lengthSqr() > WALKING_SPEED_THRESHOLD) {
            return true;
        }
        return !this.getNavigation().isDone() && state != ColumbidBehaviorState.EATING
                && state != ColumbidBehaviorState.PREENING && state != ColumbidBehaviorState.COURTING
                && state != ColumbidBehaviorState.ROOSTING && state != ColumbidBehaviorState.SLEEPING;
    }

    private ColumbidIdleAnimationChoice chooseIdleAnimation() {
        ColumbidBehaviorState state = this.getBehaviorState();
        int roll = this.getRandom().nextInt(100);

        if (state == ColumbidBehaviorState.COURTING) {
            return ColumbidIdleAnimationChoice.DISPLAY;
        }
        if (state == ColumbidBehaviorState.FORAGING || state == ColumbidBehaviorState.EATING) {
            if (roll < 46) return ColumbidIdleAnimationChoice.PECK_1;
            if (roll < 76) return ColumbidIdleAnimationChoice.PECK_2;
            return ColumbidIdleAnimationChoice.BASE;
        }

        if (roll < 58) return ColumbidIdleAnimationChoice.BASE;
        if (roll < 74) return ColumbidIdleAnimationChoice.PECK_1;
        if (roll < 90) return ColumbidIdleAnimationChoice.PECK_2;
        return ColumbidIdleAnimationChoice.DISPLAY;
    }

    private <T extends AbstractColumbidEntity> PlayState movementController(AnimationState<T> animationState) {
        RawAnimation preview = this.guidePreviewAnimation.animation();
        if (preview != null) {
            return animationState.setAndContinue(preview);
        }

        if (this.shouldPlayFlyAnimation()) {
            if (this.flapOnceTicks > 0) {
                return animationState.setAndContinue(FLY_FLAP_ONCE_ANIMATION);
            }
            return animationState.setAndContinue(this.getBehaviorState() == ColumbidBehaviorState.GLIDING
                    ? FLY_LOOP_ANIMATION : FLY_FLAPPING_LOOP_ANIMATION);
        }

        if (this.shouldPlayWalkAnimation()) {
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
}
