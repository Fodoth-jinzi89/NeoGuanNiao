package net.fodoth.skina.neoguanniao.content.bird.impl.old.sparrow;

import net.fodoth.skina.neoguanniao.content.bath.BirdBathAttraction;
import net.fodoth.skina.neoguanniao.content.bath.BirdBathBlockEntity;
import net.fodoth.skina.neoguanniao.content.bath.BirdBathContentType;
import net.fodoth.skina.neoguanniao.content.bath.BirdBathFeedingAnimatable;
import net.fodoth.skina.neoguanniao.content.bath.BirdBathMountable;
import net.fodoth.skina.neoguanniao.content.bath.BirdBathUseGoal;
import net.fodoth.skina.neoguanniao.content.bird.feature.brain.BirdBrain;
import net.fodoth.skina.neoguanniao.content.bird.feature.brain.BirdIntent;
import net.fodoth.skina.neoguanniao.content.bird.feature.flight.BirdFlightAware;
import net.fodoth.skina.neoguanniao.content.bird.feature.flight.BirdFlightBoids;
import net.fodoth.skina.neoguanniao.content.bird.feature.flight.BirdFlightManager;
import net.fodoth.skina.neoguanniao.content.bird.feature.flight.BirdFlightProfile;
import net.fodoth.skina.neoguanniao.content.bird.feature.flight.BirdFlightTargeting;
import net.fodoth.skina.neoguanniao.content.bird.feature.scale.BirdModelScale;
import net.fodoth.skina.neoguanniao.content.bird.feature.scale.BirdModelScaleProfile;
import net.fodoth.skina.neoguanniao.content.bird.feature.scale.ScalableBirdModel;
import net.fodoth.skina.neoguanniao.content.bird.feature.species.SparrowProfile;
import net.fodoth.skina.neoguanniao.content.bird.impl.old.sparrow.goal.*;
import net.fodoth.skina.neoguanniao.content.feed.BreadcrumbPileBlock;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoBlocks;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoEntityTypes;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoItems;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
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

import java.util.UUID;

import static net.fodoth.skina.neoguanniao.content.bird.impl.old.sparrow.SparrowDefinition.*;

public class SparrowEntity extends TamableAnimal implements GeoEntity, ScalableBirdModel, BirdFlightAware, BirdBathMountable, BirdBathFeedingAnimatable {

    // ============ 数据序列化器 ============
    private static final EntityDataAccessor<Integer> BEHAVIOR_STATE =
            SynchedEntityData.defineId(SparrowEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> MODEL_SCALE =
            SynchedEntityData.defineId(SparrowEntity.class, EntityDataSerializers.FLOAT);

    // ============ 诱惑物品 ============
    static final Ingredient TAMING_ITEMS = Ingredient.of(Items.WHEAT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.BEETROOT_SEEDS, Items.TORCHFLOWER_SEEDS, Items.PITCHER_POD);

    // ============ 动画定义 ============
    protected static final RawAnimation IDLE_ANIMATION = RawAnimation.begin().thenLoop("idle");
    protected static final RawAnimation TAIL_ANIMATION = RawAnimation.begin().thenPlay("idle_diff_1").thenLoop("idle");
    protected static final RawAnimation PECK_ANIMATION = RawAnimation.begin().thenPlay("idle_diff_2").thenLoop("idle");
    protected static final RawAnimation LOOK_AROUND_ANIMATION = RawAnimation.begin().thenPlay("idle_diff_3").thenLoop("idle");
    protected static final RawAnimation WALK_ANIMATION = RawAnimation.begin().thenLoop("walk");
    protected static final RawAnimation FLY_ANIMATION = RawAnimation.begin().thenLoop("fly");

    // ============ 常量 ============
    private static final double WALKING_SPEED_THRESHOLD = 0.0018;
    private static final double SHORT_FLIGHT_SPEED = 0.24;
    private static final double ESCAPE_FLIGHT_SPEED = 0.42;
    private static final BirdFlightProfile FLIGHT_PROFILE = BirdFlightProfile.SPARROW;
    private static final float FLIGHT_YAW_TURN_RATE = 22.0F;
    private static final float FLIGHT_PITCH_TURN_RATE = 10.0F;
    private static final int MAX_FAMILIAR_TICKS = 7200;
    private static final int ATTACK_DISTRUST_TICKS = 48000;
    private static final int FULL_SATIATION_TICKS = 2400;
    private static final int MAX_SATIATION_TICKS = 4800;
    private static final int HOME_RADIUS = 36;
    private static final int SETTLEMENT_SCAN_RADIUS = 14;

    // ============ 成员变量 ============
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    private final BirdBrain birdBrain;
    private SparrowBehaviorState behaviorState;
    private SparrowIdleAnimationChoice currentIdleAnimation;
    private long nextIdleAnimationSwapTick;
    private int forcedIdleAnimationTicks;
    private int familiarTicks;
    private int calmAroundPlayerTicks;
    private int satiatedTicks;
    private int flightTicks;
    private int flightDuration;
    private int timeFlying;
    public int flightCooldown;
    private int blockedFlightTicks;
    private int flightLandingTicks;
    private int airborneFlightAnimationTicks;
    private Vec3 flightTarget;
    private double flightSpeed;
    private boolean escapeFlight;
    private UUID distrustedPlayer;
    private int distrustTicks;
    private Vec3 pendingScareSource;
    public int pendingScareTicks;
    private SparrowScareReaction pendingScareReaction;
    public BlockPos noticedBreadcrumbPos;
    public int breadcrumbInterestTicks;
    private BlockPos homePos;
    public int perchCooldown;
    public int behaviorStateLockTicks;
    public int ownerFollowSuppressedTicks;
    private SparrowGuidePreviewAnimation guidePreviewAnimation;

    // ============ 构造方法 ============
    public SparrowEntity(EntityType<? extends SparrowEntity> entityType, Level level) {
        super(entityType, level);
        this.birdBrain = new BirdBrain(this, SparrowProfile.INSTANCE);
        this.behaviorState = SparrowBehaviorState.IDLE;
        this.currentIdleAnimation = SparrowIdleAnimationChoice.BASE;
        this.flightSpeed = SHORT_FLIGHT_SPEED;
        this.pendingScareReaction = SparrowScareReaction.ESCAPE_FLIGHT;
        this.guidePreviewAnimation = SparrowGuidePreviewAnimation.NONE;
        this.setPathfindingMalus(PathType.LEAVES, 0.0F);
        this.setPathfindingMalus(PathType.WATER, -1.0F);
        this.setPathfindingMalus(PathType.DANGER_FIRE, 16.0F);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, 16.0F);
        this.satiatedTicks = this.getRandom().nextFloat() < 0.62F ? 0 : 300 + this.getRandom().nextInt(1800);
    }

    // ============ 属性 ============
    public static AttributeSupplier.Builder createAttributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, WALK_SPEED)
                .add(Attributes.FOLLOW_RANGE, FOLLOW_RANGE);
    }

    // ============ 生成 ============
    public static boolean canSpawn(EntityType<SparrowEntity> entityType, ServerLevelAccessor level,
                                   MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        BlockState below = level.getBlockState(pos.below());
        boolean validGround = below.is(BlockTags.DIRT) || below.is(Blocks.FARMLAND) || below.is(BlockTags.SAND);
        if (!validGround) {
            return false;
        }

        // 检查附近同类型实体数量
        AABB searchBox = new AABB(pos).inflate(8.0, 4.0, 8.0);
        int entityCount = level.getEntitiesOfClass(SparrowEntity.class, searchBox).size();
        if (entityCount > 8) {
            return false;
        }

        int settlementScore = settlementScore(level, pos, SETTLEMENT_SCAN_RADIUS, 4);
        if (settlementScore >= 14) {
            return true;
        } else if (settlementScore >= 6) {
            return random.nextFloat() < 0.68F;
        } else {
            return random.nextFloat() < 0.28F;
        }
    }

    // ============ 数据序列化 ============
    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(BEHAVIOR_STATE, SparrowBehaviorState.IDLE.ordinal());
        builder.define(MODEL_SCALE, 1.0F);
    }

    @Override
    public void onSyncedDataUpdated(@NotNull EntityDataAccessor<?> key) {
        if (BEHAVIOR_STATE.equals(key)) {
            this.behaviorState = decodeBehaviorState(this.getEntityData().get(BEHAVIOR_STATE));
        }
        super.onSyncedDataUpdated(key);
    }

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
        this.goalSelector.addGoal(1, new SparrowFleePlayerGoal(this));
        this.goalSelector.addGoal(2, new SparrowFollowOwnerGoal(this, 1.02, 3.0F, 10.0F));
        this.goalSelector.addGoal(3, new TemptGoal(this, 0.9, TAMING_ITEMS, false));
        this.goalSelector.addGoal(4, new BreedGoal(this, 0.8));
        this.goalSelector.addGoal(5, new SparrowEatBreadcrumbGoal(this));
        this.goalSelector.addGoal(6, new BirdBathUseGoal(
                this, 0.82, 9.0, 42,
                BirdBathAttraction::isAttractiveToSmallSeedBird,
                this::canUseBirdBath,
                (bath) -> this.setBehaviorState(SparrowBehaviorState.FORAGING),
                this::consumeBirdBathServing,
                (bath, consumed) -> {
                    if (this.getBehaviorState() == SparrowBehaviorState.FORAGING) {
                        this.setBehaviorStateFor(SparrowBehaviorState.LOOK_AROUND, consumed ? 24 : 8);
                    }
                }
        ));
        this.goalSelector.addGoal(7, new SparrowPerchGoal(this));
        this.goalSelector.addGoal(8, new SparrowFlockGoal(this));
        this.goalSelector.addGoal(9, new RandomStrollGoal(this, 0.72));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(11, new RandomLookAroundGoal(this));
    }

    // ============ Tick ============
    @Override
    public void aiStep() {
        super.aiStep();
        this.tickCounters();

        if (!this.level().isClientSide) {
            this.birdBrain.tick();
            this.ensureHomePos();
            this.tickStaleFlightRecovery();
            this.tickWaterEscape();
        }

        if (!this.level().isClientSide && this.pendingScareTicks > 0) {
            --this.pendingScareTicks;
            if (this.pendingScareTicks <= 0) {
                this.releasePendingScare();
            }
        }

        if (!this.level().isClientSide && this.isControlledFlightActive()) {
            this.tickControlledFlight();
        }

        if (!this.level().isClientSide && this.onGround() && !this.isInWater()) {
            this.tickGroundActivities();
        }

        if (!this.onGround() || this.isControlledFlightActive()) {
            this.xxa = 0.0F;
        }

        if (!this.level().isClientSide) {
            this.tickBehaviorStateFallback();
            this.tickGroundMovementFacing();
        }
    }

    private void tickCounters() {
        if (this.familiarTicks > 0) --this.familiarTicks;
        if (this.calmAroundPlayerTicks > 0) --this.calmAroundPlayerTicks;
        if (this.satiatedTicks > 0) --this.satiatedTicks;
        if (this.breadcrumbInterestTicks > 0) {
            --this.breadcrumbInterestTicks;
            if (this.breadcrumbInterestTicks <= 0) {
                this.noticedBreadcrumbPos = null;
            }
        }
        if (this.distrustTicks > 0) {
            --this.distrustTicks;
            if (this.distrustTicks <= 0) {
                this.distrustedPlayer = null;
            }
        }
        if (this.flightCooldown > 0) --this.flightCooldown;
        if (this.airborneFlightAnimationTicks > 0) {
            if (this.onGround()) {
                this.airborneFlightAnimationTicks = 0;
            } else {
                --this.airborneFlightAnimationTicks;
            }
        }
        if (this.ownerFollowSuppressedTicks > 0) --this.ownerFollowSuppressedTicks;
        if (this.perchCooldown > 0) --this.perchCooldown;
        if (this.forcedIdleAnimationTicks > 0) --this.forcedIdleAnimationTicks;
        if (this.behaviorStateLockTicks > 0) --this.behaviorStateLockTicks;
    }

    private void tickGroundActivities() {
        if (this.getNavigation().isDone() && this.getRandom().nextInt(160) == 0) {
            this.triggerPeck();
        }

        int ambientFlightChance = this.level().isDay()
                ? (this.isTame() ? 360 : 260)
                : (this.isTame() ? 720 : 520);
        if (this.canStartAmbientShortFlight() && this.getRandom().nextInt(ambientFlightChance) == 0) {
            this.startAmbientShortFlight();
        }

        int shortHopChance = this.isTame() ? 900 : 520;
        if (this.canRandomShortHop() && this.getRandom().nextInt(shortHopChance) == 0) {
            this.shortHop();
        }
    }

    // ============ 交互 ============
    @Override
    public @NotNull InteractionResult mobInteract(Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (TAMING_ITEMS.test(stack)) {
            if (this.isDistrusted(player)) {
                if (!this.level().isClientSide) {
                    this.startEscapeFlight(player.position());
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }

            this.familiarTicks = Math.max(this.familiarTicks, 3600);
            if (!this.level().isClientSide) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                this.birdBrain.onEat(0.2F);
                this.satiatedTicks = Math.max(this.satiatedTicks, 900);
                this.calmAroundPlayerTicks = Math.max(this.calmAroundPlayerTicks, 600);

                if (this.isTame()) {
                    if (this.getHealth() < this.getMaxHealth()) {
                        this.heal(2.0F);
                    }
                    if (!this.isBaby() && !this.isInLove()) {
                        this.setInLove(player);
                    }
                    this.birdBrain.onRest(0.05F);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                } else if (this.getRandom().nextInt(3) == 0) {
                    this.tame(player);
                    this.getNavigation().stop();
                    this.birdBrain.onRest(0.1F);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                } else {
                    this.level().broadcastEntityEvent(this, (byte) 6);
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    // ============ 受伤 ============
    @Override
    @SuppressWarnings("ConstantConditions")
    public boolean hurt(@NotNull DamageSource damageSource, float amount) {
        boolean hurt = super.hurt(damageSource, amount);
        if (hurt) {
            this.familiarTicks = 0;
            this.calmAroundPlayerTicks = 0;
            Entity attacker = damageSource.getEntity();
            if (attacker instanceof Player player) {
                if (this.isOwnedBy(player)) {
                    this.birdBrain.onFrightened(0.25F);
                    this.suppressOwnerFollow(120);
                    this.getNavigation().stop();
                    this.getLookControl().setLookAt(player, 30.0F, 30.0F);
                    this.setBehaviorStateFor(SparrowBehaviorState.ALERT, 60);
                    return hurt;
                }
            }

            this.birdBrain.onFrightened(0.65F);
            if (attacker != null) {
                if (attacker instanceof Player player) {
                    this.rememberDistrustedPlayer(player);
                }
                if (this.isTame()) {
                    this.suppressOwnerFollow(160);
                }
                if (!this.isControlledFlightActive()) {
                    this.startEscapeFlight(attacker.position());
                }
                this.alertNearbySparrows(attacker);
            }
        }
        return hurt;
    }

    // ============ NBT ============
    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putBoolean("NoGravity", false);
        if (this.distrustedPlayer != null && this.distrustTicks > 0) {
            compoundTag.putUUID("DistrustedPlayer", this.distrustedPlayer);
            compoundTag.putInt("DistrustTicks", this.distrustTicks);
        }
        compoundTag.putInt("SatiatedTicks", this.satiatedTicks);
        if (this.homePos != null) {
            compoundTag.putInt("HomeX", this.homePos.getX());
            compoundTag.putInt("HomeY", this.homePos.getY());
            compoundTag.putInt("HomeZ", this.homePos.getZ());
        }
        BirdModelScale.save(compoundTag, this.getIndividualModelScale(), this.modelScaleProfile());
        this.birdBrain.save(compoundTag);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        if (compoundTag.hasUUID("DistrustedPlayer")) {
            this.distrustedPlayer = compoundTag.getUUID("DistrustedPlayer");
            this.distrustTicks = compoundTag.getInt("DistrustTicks");
            if (this.distrustTicks <= 0) {
                this.distrustedPlayer = null;
            }
        }
        this.satiatedTicks = Math.max(0, compoundTag.getInt("SatiatedTicks"));
        if (compoundTag.contains("HomeX", CompoundTag.TAG_INT)
                && compoundTag.contains("HomeY", CompoundTag.TAG_INT)
                && compoundTag.contains("HomeZ", CompoundTag.TAG_INT)) {
            this.homePos = new BlockPos(
                    compoundTag.getInt("HomeX"),
                    compoundTag.getInt("HomeY"),
                    compoundTag.getInt("HomeZ")
            );
        }
        if (compoundTag.contains("BirdModelScale", CompoundTag.TAG_FLOAT)) {
            this.setIndividualModelScale(BirdModelScale.load(compoundTag, this.modelScaleProfile()));
        } else {
            this.randomizeModelScale();
        }
        this.birdBrain.load(compoundTag);
        this.clearSerializedFlightState();
    }

    // ============ 接口实现 ============
    public BirdBrain birdBrain() {
        return this.birdBrain;
    }

    @Override
    public BirdFlightProfile birdFlightProfile() {
        return FLIGHT_PROFILE;
    }

    @Override
    public boolean isBirdFlightActive() {
        return this.isControlledFlightActive() || this.getBehaviorState().isAirborne()
                || (this.airborneFlightAnimationTicks > 0 && !this.onGround());
    }

    @Override
    public boolean isBirdLanding() {
        return this.flightLandingTicks > 0;
    }

    @Override
    public boolean isBirdEscaping() {
        return this.escapeFlight;
    }

    public boolean startFlybyFlight(Vec3 landingTarget) {
        if (landingTarget == null) return false;
        return this.startControlledFlight(landingTarget, this.randomBetween(78, 122),
                0.29 + this.getRandom().nextDouble() * 0.04, false);
    }

    @Override
    public boolean startBirdBathMountFlight(Vec3 standPosition) {
        if (standPosition == null || this.isControlledFlightActive()) return false;

        Vec3 horizontal = standPosition.subtract(this.position()).multiply(1.0, 0.0, 1.0);
        if (horizontal.lengthSqr() <= 1.0E-4) {
            horizontal = Vec3.ZERO;
        } else {
            horizontal = horizontal.normalize().scale(0.27);
        }

        Vec3 movement = new Vec3(horizontal.x, 0.64, horizontal.z);
        this.getNavigation().stop();
        this.setSilent(false);
        this.airborneFlightAnimationTicks = Math.max(this.airborneFlightAnimationTicks, 32);
        this.setBehaviorStateFor(SparrowBehaviorState.SHORT_FLIGHT, 32);
        this.setDeltaMovement(movement);
        this.faceMovement(movement);
        this.xxa = 0.0F;
        this.hasImpulse = true;
        return true;
    }

    @Override
    public void startBirdBathFeedingAnimation(BirdBathContentType contentType, int ticks) {
        this.getNavigation().stop();
        if (contentType.isFood()) {
            this.setBehaviorStateFor(SparrowBehaviorState.PECKING, Math.max(28, ticks));
        } else {
            this.setBehaviorStateFor(SparrowBehaviorState.LOOK_AROUND, Math.max(24, ticks / 2));
        }
    }

    // ============ 行为状态 ============
    public SparrowBehaviorState getBehaviorState() {
        return decodeBehaviorState(this.getEntityData().get(BEHAVIOR_STATE));
    }

    public void setBehaviorState(SparrowBehaviorState behaviorState) {
        if (behaviorState == null) {
            behaviorState = SparrowBehaviorState.IDLE;
        }
        this.behaviorState = behaviorState;
        this.getEntityData().set(BEHAVIOR_STATE, behaviorState.ordinal());
    }

    public void setBehaviorStateFor(SparrowBehaviorState behaviorState, int ticks) {
        this.setBehaviorState(behaviorState);
        this.behaviorStateLockTicks = Math.max(this.behaviorStateLockTicks, ticks);
    }

    private static SparrowBehaviorState decodeBehaviorState(int ordinal) {
        SparrowBehaviorState[] values = SparrowBehaviorState.values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : SparrowBehaviorState.IDLE;
    }

    public BlockPos findPerchTarget(boolean roosting) {
        this.ensureHomePos();
        BlockPos center = roosting ? this.homePos : this.blockPosition();
        int radius = roosting ? HOME_RADIUS : 18;
        int minY = roosting ? -5 : -3;
        int maxY = roosting ? 13 : 8;
        int attempts = roosting ? 130 : 64;
        BlockPos bestPos = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        for (int attempt = 0; attempt < attempts; ++attempt) {
            int xOffset = this.randomBetween(-radius, radius);
            int zOffset = this.randomBetween(-radius, radius);
            if (xOffset * xOffset + zOffset * zOffset <= radius * radius) {
                for (int yOffset = maxY; yOffset >= minY; --yOffset) {
                    mutablePos.set(center.getX() + xOffset, center.getY() + yOffset, center.getZ() + zOffset);
                    BlockPos perchPos = mutablePos.above();
                    if (this.isSafePerchPosition(perchPos)) {
                        double score = this.scorePerchPosition(perchPos, roosting);
                        if (score > bestScore) {
                            bestScore = score;
                            bestPos = perchPos.immutable();
                        }
                    }
                }
            }
        }

        return bestPos;
    }

    private double scorePerchPosition(BlockPos pos, boolean roosting) {
        BlockState below = this.level().getBlockState(pos.below());
        double score = 0.0;

        if (below.getBlock() instanceof FenceBlock || below.getBlock() instanceof FenceGateBlock) {
            score += 24.0;
        }
        if (below.is(BlockTags.WALLS)) {
            score += roosting ? 25.0 : 15.0;
        }
        if (below.is(BlockTags.LEAVES)) {
            score += roosting ? 20.0 : 13.0;
        }
        if (below.is(Blocks.GRASS_BLOCK) || below.is(Blocks.PODZOL)) {
            score += 17.0;
        }
        if (below.isFaceSturdy(this.level(), pos.below(), Direction.UP)) {
            score += 6.0;
        }

        score += Math.clamp((pos.getY() - this.blockPosition().getY()) * 1.2, 0.0, 10.0);

        if (roosting) {
            score += this.hasPerchCoverNear(pos, 4) ? 10.0 : 0.0;
            score += this.nearbyRoostingSparrowScore(pos);
        }

        score += Math.min(12.0, this.localSettlementScore(pos, 6) * 0.5);

        if (this.homePos != null) {
            score -= Math.sqrt(this.blockDistanceSqr(pos, this.homePos)) * 0.08;
        }
        score -= Math.sqrt(this.blockDistanceSqr(pos, this.blockPosition())) * (roosting ? 0.03 : 0.08);

        return score;
    }

    @SuppressWarnings("SameParameterValue")
    private boolean hasPerchCoverNear(BlockPos pos, int radius) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        for (int xOffset = -radius; xOffset <= radius; ++xOffset) {
            for (int zOffset = -radius; zOffset <= radius; ++zOffset) {
                for (int yOffset = 0; yOffset <= 4; ++yOffset) {
                    mutablePos.set(pos.getX() + xOffset, pos.getY() + yOffset, pos.getZ() + zOffset);
                    if (this.canReadChunk(mutablePos)) {
                        BlockState state = this.level().getBlockState(mutablePos);
                        if (state.is(BlockTags.WALLS) || state.is(BlockTags.LEAVES)
                                || state.getBlock() instanceof FenceBlock) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private double nearbyRoostingSparrowScore(BlockPos pos) {
        return this.level().getEntitiesOfClass(
                SparrowEntity.class,
                this.getBoundingBox().inflate(12.0),
                other -> other != this && other.isAlive() && other.onGround()
        ).stream().mapToDouble(other -> {
            double distance = Vec3.atCenterOf(pos).distanceToSqr(other.position());
            if (distance < 0.9) {
                return -10.0;
            }
            return distance <= 5.5 ? 6.0 : 0.0;
        }).sum();
    }

    @SuppressWarnings("SameParameterValue")
    private int localSettlementScore(BlockPos origin, int radius) {
        int score = 0;
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        for (int xOffset = -radius; xOffset <= radius; ++xOffset) {
            for (int zOffset = -radius; zOffset <= radius; ++zOffset) {
                for (int yOffset = -2; yOffset <= 2; ++yOffset) {
                    mutablePos.set(origin.getX() + xOffset, origin.getY() + yOffset, origin.getZ() + zOffset);
                    if (this.canReadChunk(mutablePos)) {
                        score += settlementBlockScore(this.level().getBlockState(mutablePos));
                        if (score >= 28) {
                            return score;
                        }
                    }
                }
            }
        }
        return score;
    }

    public boolean isSafePerchPosition(BlockPos pos) {
        if (!this.canReadChunk(pos)) {
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
                || this.level().getFluidState(pos).is(FluidTags.LAVA)) {
            return false;
        }

        if (below.isAir() || below.is(Blocks.WATER) || below.is(Blocks.LAVA)
                || below.is(Blocks.SNOW) || below.is(Blocks.SNOW_BLOCK)
                || below.is(Blocks.POWDER_SNOW) || below.is(Blocks.SNOW)) {
            return false;
        }

        return this.isPreferredPerchBase(below) || below.isFaceSturdy(this.level(), pos.below(), Direction.UP);
    }

    private boolean isPreferredPerchBase(BlockState state) {
        return state.getBlock() instanceof FenceBlock
                || state.getBlock() instanceof FenceGateBlock
                || state.is(BlockTags.WALLS)
                || state.is(BlockTags.LEAVES)
                || state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.PODZOL);
    }

    // ============ 缩放接口 ============
    @Override
    public BirdModelScaleProfile modelScaleProfile() {
        return BirdModelScaleProfile.SPARROW;
    }

    @Override
    public float getIndividualModelScale() {
        return BirdModelScale.sanitize(this.getEntityData().get(MODEL_SCALE), this.modelScaleProfile());
    }

    @Override
    public void setIndividualModelScale(float scale) {
        this.getEntityData().set(MODEL_SCALE, BirdModelScale.sanitize(scale, this.modelScaleProfile()));
    }

    private void randomizeModelScale() {
        this.setIndividualModelScale(BirdModelScale.randomIndividualScale(this.getRandom(), this.modelScaleProfile()));
    }

    // ============ 其他实体方法 ============
    @Override
    public boolean isFood(@NotNull ItemStack stack) {
        return TAMING_ITEMS.test(stack);
    }

    @Override
    public boolean isTame() {
        return false;
    }

    @Override
    public SparrowEntity getBreedOffspring(@NotNull ServerLevel level, @NotNull AgeableMob mate) {
        SparrowEntity child = NeoGuanNiaoEntityTypes.SPARROW.get().create(level);
        if (child != null) {
            float mateScale = mate instanceof SparrowEntity other
                    ? other.getIndividualModelScale()
                    : this.getIndividualModelScale();
            child.setIndividualModelScale(BirdModelScale.inheritIndividualScale(
                    child.getRandom(), this.getIndividualModelScale(), mateScale, child.modelScaleProfile()));
        }
        return child;
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        BlockState below = level.getBlockState(pos.below());
        float score = super.getWalkTargetValue(pos, level);
        if (below.is(Blocks.FARMLAND)) {
            score += 5.0F;
        }
        if (below.is(BlockTags.SAND) || below.is(BlockTags.DIRT)) {
            score += 2.0F;
        }
        return score;
    }

    // ============ 预览动画 ============
    public void setGuidePreviewAnimation(SparrowGuidePreviewAnimation guidePreviewAnimation) {
        this.guidePreviewAnimation = guidePreviewAnimation == null
                ? SparrowGuidePreviewAnimation.NONE
                : guidePreviewAnimation;
    }

    public SparrowGuidePreviewAnimation getGuidePreviewAnimation() {
        return this.guidePreviewAnimation;
    }

    // ============ 动作触发 ============
    public void triggerPeck() {
        this.currentIdleAnimation = SparrowIdleAnimationChoice.PECK;
        this.forcedIdleAnimationTicks = 34;
        this.nextIdleAnimationSwapTick = this.level().getGameTime() + this.forcedIdleAnimationTicks;
        this.setBehaviorStateFor(SparrowBehaviorState.PECKING, 30);
    }

    public void triggerLookAround() {
        this.currentIdleAnimation = SparrowIdleAnimationChoice.LOOK_AROUND;
        this.forcedIdleAnimationTicks = 42;
        this.nextIdleAnimationSwapTick = this.level().getGameTime() + this.forcedIdleAnimationTicks;
        this.setBehaviorStateFor(SparrowBehaviorState.LOOK_AROUND, 40);
    }

    public void triggerTailFlick() {
        this.currentIdleAnimation = SparrowIdleAnimationChoice.TAIL;
        this.forcedIdleAnimationTicks = 42;
        this.nextIdleAnimationSwapTick = this.level().getGameTime() + this.forcedIdleAnimationTicks;
    }

    // ============ 声音 ============
    @Override
    protected SoundEvent getAmbientSound() {
        return NeoGuanNiaoSoundEvents.SPARROW_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return NeoGuanNiaoSoundEvents.SPARROW_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return NeoGuanNiaoSoundEvents.SPARROW_DEATH.get();
    }

    @Override
    protected void playStepSound(@NotNull BlockPos pos, @NotNull BlockState blockState) {
        this.playSound(SoundEvents.CHICKEN_STEP, 0.08F, 1.55F);
    }

    @Override
    public int getAmbientSoundInterval() {
        return this.level().isDay() ? 120 : 260;
    }

    @Override
    public float getVoicePitch() {
        return 0.38F;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float damageMultiplier, @NotNull DamageSource damageSource) {
        return false;
    }

    // ============ 状态查询 ============
    @SuppressWarnings("all")
    public boolean brainWantsForage() {
        BirdIntent intent = this.birdBrain.currentIntent();
        return this.birdBrain.wantsForage() || intent == BirdIntent.FORAGE;
    }

    public boolean brainWantsRoost() {
        BirdIntent intent = this.birdBrain.currentIntent();
        return this.birdBrain.wantsRoost() || intent == BirdIntent.ROOST;
    }

    public boolean brainWantsEscapeOrAlert() {
        BirdIntent intent = this.birdBrain.currentIntent();
        return this.birdBrain.wantsShortEscape() || this.birdBrain.wantsLongEscape()
                || intent == BirdIntent.ALERT || intent == BirdIntent.SHORT_FLIGHT
                || intent == BirdIntent.LONG_FLIGHT;
    }

    private boolean canUseBirdBath() {
        SparrowBehaviorState state = this.getBehaviorState();
        return this.onGround() && !this.isControlledFlightActive() && this.flightCooldown <= 0
                && this.pendingScareTicks <= 0 && !this.hasBreadcrumbInterest()
                && !this.brainWantsRoost() && this.getTarget() == null
                && state != SparrowBehaviorState.PERCHING && state != SparrowBehaviorState.ROOSTING
                && !state.isEscape();
    }

    private void consumeBirdBathServing(BirdBathBlockEntity bath, BirdBathContentType contentType) {
        if (contentType == BirdBathContentType.BREAD) {
            this.satiatedTicks = Math.max(this.satiatedTicks, 700);
            this.birdBrain.onEat(0.24F);
            this.triggerPeck();
        } else {
            this.satiatedTicks = Math.max(this.satiatedTicks, 420);
            this.birdBrain.onEat(0.12F);
            this.setBehaviorStateFor(SparrowBehaviorState.LOOK_AROUND, 26);
            this.playSound(SoundEvents.PARROT_EAT, 0.18F, 1.35F + this.getRandom().nextFloat() * 0.18F);
        }
    }

    private boolean canStartAmbientShortFlight() {
        if (!this.isControlledFlightActive() && this.onGround() && this.flightCooldown <= 0
                && this.getNavigation().isDone() && this.behaviorStateLockTicks <= 0
                && !this.hasBreadcrumbInterest() && !this.brainWantsForage() && !this.brainWantsRoost()) {
            SparrowBehaviorState state = this.getBehaviorState();
            if (state != SparrowBehaviorState.FORAGING && state != SparrowBehaviorState.PECKING
                    && state != SparrowBehaviorState.PERCHING && state != SparrowBehaviorState.ROOSTING
                    && state != SparrowBehaviorState.FOLLOWING_OWNER && state != SparrowBehaviorState.ALERT
                    && !state.isEscape()) {
                if (!this.isTame()) {
                    return true;
                }
                LivingEntity owner = this.getOwner();
                return owner == null || this.distanceToSqr(owner) > 49.0;
            }
        }
        return false;
    }

    private boolean canRandomShortHop() {
        if (!this.isControlledFlightActive() && this.onGround() && this.getNavigation().isDone()
                && this.behaviorStateLockTicks <= 0 && !this.hasBreadcrumbInterest()
                && !this.brainWantsForage() && !this.brainWantsRoost()) {
            SparrowBehaviorState state = this.getBehaviorState();
            return state == SparrowBehaviorState.IDLE || state == SparrowBehaviorState.LOOK_AROUND;
        }
        return false;
    }

    private void suppressOwnerFollow(int ticks) {
        if (this.isTame()) {
            this.ownerFollowSuppressedTicks = Math.max(this.ownerFollowSuppressedTicks, ticks);
        }
    }

    // ============ 行为回退 ============
    private void tickBehaviorStateFallback() {
        if (this.behaviorStateLockTicks > 0) {
            --this.behaviorStateLockTicks;
        }

        if (this.isControlledFlightActive()) {
            this.setBehaviorState(this.escapeFlight ? SparrowBehaviorState.FLEEING : SparrowBehaviorState.SHORT_FLIGHT);
        } else if (!this.updateFollowingOwnerBehaviorState()) {
            SparrowBehaviorState state = this.getBehaviorState();
            BirdIntent intent = this.birdBrain.currentIntent();
            if (this.behaviorStateLockTicks > 0 || this.forcedIdleAnimationTicks > 0
                    || !this.getNavigation().isDone() || state == SparrowBehaviorState.PERCHING
                    || state == SparrowBehaviorState.ROOSTING || state == SparrowBehaviorState.FORAGING
                    || (intent != BirdIntent.ALERT && intent != BirdIntent.WATCH)) {
                if (this.behaviorStateLockTicks <= 0 && this.forcedIdleAnimationTicks <= 0
                        && state != SparrowBehaviorState.PERCHING && state != SparrowBehaviorState.ROOSTING
                        && state != SparrowBehaviorState.FORAGING) {
                    this.setBehaviorState(SparrowBehaviorState.IDLE);
                }
            } else {
                this.setBehaviorState(SparrowBehaviorState.ALERT);
            }
        }
    }

    private boolean updateFollowingOwnerBehaviorState() {
        LivingEntity owner = this.getOwner();
        if (this.isTame() && owner != null && owner.isAlive() && this.ownerFollowSuppressedTicks <= 0
                && this.distanceToSqr(owner) > 9.0 && !this.getNavigation().isDone()) {
            this.setBehaviorState(SparrowBehaviorState.FOLLOWING_OWNER);
            return true;
        }
        return false;
    }

    // ============ 地面朝向 ============
    private void tickGroundMovementFacing() {
        if (this.shouldFaceGroundMovement()) {
            BirdFlightManager.faceGroundMovement(this, this.getDeltaMovement(), 1.0E-4);
        }
    }

    private boolean shouldFaceGroundMovement() {
        if (this.onGround() && !this.isControlledFlightActive() && !this.isInWater() && !this.isVehicle()) {
            SparrowBehaviorState state = this.getBehaviorState();
            if (state != SparrowBehaviorState.PECKING && state != SparrowBehaviorState.LOOK_AROUND
                    && state != SparrowBehaviorState.PERCHING && state != SparrowBehaviorState.ROOSTING
                    && !state.isEscape()) {
                return this.getDeltaMovement().lengthSqr() > WALKING_SPEED_THRESHOLD
                        || !this.getNavigation().isDone();
            }
        }
        return false;
    }

    // ============ 工具方法 ============
    private boolean isOwnedBy(Player player) {
        return this.isTame() && this.getOwner() != null && this.getOwner().equals(player);
    }

    public int randomBetween(int min, int max) {
        return min + this.getRandom().nextInt(max - min + 1);
    }

    public double randomSigned(double range) {
        return (this.getRandom().nextDouble() * 2.0 - 1.0) * range;
    }

    // ============ 生成评分 ============
    @SuppressWarnings("SameParameterValue")
    private static int settlementScore(ServerLevelAccessor level, BlockPos origin, int horizontalRadius, int verticalRadius) {
        int score = 0;
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        for (int xOffset = -horizontalRadius; xOffset <= horizontalRadius; ++xOffset) {
            for (int zOffset = -horizontalRadius; zOffset <= horizontalRadius; ++zOffset) {
                if (xOffset * xOffset + zOffset * zOffset <= horizontalRadius * horizontalRadius) {
                    for (int yOffset = -verticalRadius; yOffset <= verticalRadius; ++yOffset) {
                        mutablePos.set(origin.getX() + xOffset, origin.getY() + yOffset, origin.getZ() + zOffset);
                        if (canReadSpawnScan(level, origin, mutablePos)) {
                            score += settlementBlockScore(level.getBlockState(mutablePos));
                            if (score >= 42) return score;
                        }
                    }
                }
            }
        }
        return score;
    }

    private static boolean canReadSpawnScan(ServerLevelAccessor level, BlockPos origin, BlockPos pos) {
        if (!(level instanceof WorldGenRegion)) {
            return true;
        }
        return SectionPos.blockToSectionCoord(origin.getX()) == SectionPos.blockToSectionCoord(pos.getX())
                && SectionPos.blockToSectionCoord(origin.getZ()) == SectionPos.blockToSectionCoord(pos.getZ());
    }

    private static int settlementBlockScore(BlockState state) {
        if (state.is(Blocks.FARMLAND)) return 4;
        if (state.getBlock() instanceof CropBlock) return 3;
        if (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.PODZOL)) return 5;
        if (state.getBlock() instanceof BedBlock || state.getBlock() instanceof DoorBlock) return 6;
        if (state.getBlock() instanceof FenceBlock || state.getBlock() instanceof FenceGateBlock) return 3;
        return 0;
    }

    // ============ 栖息地方法 ============
    private void ensureHomePos() {
        if (this.homePos == null) {
            this.homePos = this.blockPosition().immutable();
        }
    }

    public boolean shouldSeekNightRoost() {
        return !this.level().isDay() || this.level().isRaining();
    }

    private boolean canReadChunk(BlockPos pos) {
        return this.level().hasChunk(SectionPos.blockToSectionCoord(pos.getX()),
                SectionPos.blockToSectionCoord(pos.getZ()));
    }

    private double blockDistanceSqr(BlockPos first, BlockPos second) {
        double x = first.getX() - second.getX();
        double y = first.getY() - second.getY();
        double z = first.getZ() - second.getZ();
        return x * x + y * y + z * z;
    }

    // ============ 飞行控制 ============
    public boolean isControlledFlightActive() {
        return this.flightTarget != null && (this.flightTicks > 0 || !this.onGround());
    }

    private void clearSerializedFlightState() {
        this.flightTarget = null;
        this.flightTicks = 0;
        this.flightDuration = 0;
        this.timeFlying = 0;
        this.blockedFlightTicks = 0;
        this.flightLandingTicks = 0;
        this.airborneFlightAnimationTicks = 0;
        this.escapeFlight = false;
        if (this.isInWater()) {
            this.setNoGravity(false);
        }
        if (!this.onGround() && this.getDeltaMovement().lengthSqr() < 0.0025) {
            this.setDeltaMovement(0, -0.08, 0);
            this.hasImpulse = true;
            this.xxa = 0.0F;
        }
    }

    private void tickStaleFlightRecovery() {
        if (!this.isControlledFlightActive() && this.isInWater()) {
            this.setNoGravity(false);
            if (!this.onGround() && this.getDeltaMovement().lengthSqr() < 0.0025) {
                this.setDeltaMovement(0, -0.08, 0);
                this.hasImpulse = true;
            }
        }
    }

    @SuppressWarnings("all")
    private boolean startAmbientShortFlight() {
        Vec3 target = this.findShortFlightTarget(null, false, 4, 11);
        return target != null && this.startControlledFlight(target,
                this.randomBetween(22, 42), SHORT_FLIGHT_SPEED + this.getRandom().nextDouble() * 0.04, false);
    }

    public boolean startEscapeFlight(Vec3 threatPosition) {
        if (this.isControlledFlightActive()) return false;

        Vec3 target = this.findShortFlightTarget(threatPosition, true, 12, 22);
        if (target == null) {
            Vec3 away = this.position().subtract(threatPosition).multiply(1.0, 0.0, 1.0);
            if (away.lengthSqr() <= 1.0E-4) {
                double angle = this.getRandom().nextDouble() * Math.PI * 2.0;
                away = new Vec3(Math.cos(angle), 0, Math.sin(angle));
            }
            away = away.normalize();
            target = this.position().add(away.scale(12.0 + this.getRandom().nextDouble() * 7.0))
                    .add(0, 3.4, 0);
        }

        this.suppressOwnerFollow(160);
        return this.startControlledFlight(target, this.randomBetween(48, 86), ESCAPE_FLIGHT_SPEED, true);
    }

    public boolean startControlledFlight(Vec3 target, int duration, double speed, boolean escapeFlight) {
        this.pendingScareSource = null;
        this.pendingScareTicks = 0;
        this.flightTarget = target;
        this.flightTicks = duration;
        this.flightDuration = duration;
        this.timeFlying = 0;
        this.flightSpeed = speed;
        this.escapeFlight = escapeFlight;
        this.blockedFlightTicks = 0;
        this.flightLandingTicks = 0;
        this.airborneFlightAnimationTicks = duration + 12;
        this.setBehaviorStateFor(escapeFlight ? SparrowBehaviorState.FLEEING : SparrowBehaviorState.SHORT_FLIGHT,
                escapeFlight ? 70 : 32);
        this.flightCooldown = escapeFlight ? 45 + this.getRandom().nextInt(65)
                : (this.isTame() ? 90 + this.getRandom().nextInt(110) : 70 + this.getRandom().nextInt(90));
        this.getNavigation().stop();
        this.setNoGravity(true);
        this.setSilent(false);
        Vec3 direction = target.subtract(this.position()).multiply(1.0, 0.0, 1.0);
        if (direction.lengthSqr() <= 1.0E-4) {
            direction = this.getLookAngle().multiply(1.0, 0.0, 1.0);
        }
        direction = direction.normalize();
        this.setDeltaMovement(direction.scale(speed * 0.75).add(0, escapeFlight ? 0.48 : 0.28, 0));
        this.faceMovement(this.getDeltaMovement());
        this.hasImpulse = true;
        return true;
    }

    private void faceMovement(Vec3 movement) {
        BirdFlightManager.faceMovement(this, movement, FLIGHT_PROFILE.maxPitchDegrees());
    }

    @SuppressWarnings("deprecation")
    private void tickControlledFlight() {
        if (this.flightTarget == null) {
            this.finishControlledFlight(false);
            return;
        }

        this.getNavigation().stop();
        this.setNoGravity(true);
        --this.flightTicks;
        ++this.timeFlying;

        if (this.flightTicks <= 0 && !this.onGround()) {
            ++this.flightLandingTicks;
            this.flightTicks = 1;
            if (this.flightLandingTicks == 1 || this.flightLandingTicks % 14 == 0) {
                Vec3 landing = this.findNearestShortFlightLandingTarget(this.flightLandingTicks > 70 ? 18 : 11);
                if (landing != null) {
                    this.flightTarget = landing;
                }
            }
        }

        Vec3 toTarget = this.flightTarget.subtract(this.position());
        double distance = toTarget.length();
        double horizontalDistance = Math.sqrt(toTarget.x * toTarget.x + toTarget.z * toTarget.z);
        int flightAge = this.flightDuration - this.flightTicks;
        boolean closeToTarget = distance < (this.escapeFlight ? 0.65 : 0.9);
        boolean groundedNearTarget = this.onGround() && flightAge > 8 && horizontalDistance < (this.escapeFlight ? 1.3 : 1.8);

        if (groundedNearTarget || (closeToTarget && this.onGround())) {
            this.finishControlledFlight(true);
            return;
        }

        Vec3 horizontalDirection = new Vec3(toTarget.x, 0, toTarget.z);
        if (horizontalDirection.lengthSqr() <= 1.0E-4) {
            horizontalDirection = this.getDeltaMovement().multiply(1.0, 0.0, 1.0);
        }
        if (horizontalDirection.lengthSqr() <= 1.0E-4) {
            horizontalDirection = this.getLookAngle().multiply(1.0, 0.0, 1.0);
        }

        double heightAboveTarget = this.getY() - this.flightTarget.y;
        if (this.flightLandingTicks > 0 && horizontalDistance < 1.5 && heightAboveTarget > 2.4) {
            Vec3 drift = this.getDeltaMovement().multiply(1.0, 0.0, 1.0);
            if (drift.lengthSqr() > 1.0E-4) {
                horizontalDirection = drift;
            }
        }

        horizontalDirection = horizontalDirection.normalize();
        Vec3 flockHeading = BirdFlightBoids.sameTypeHeading(this,
                this.escapeFlight ? 14.0 : 10.0, 1.9,
                this.escapeFlight ? 0.0 : 0.035,
                this.escapeFlight ? 0.1 : 0.34,
                this.escapeFlight ? 0.22 : 0.1,
                this.escapeFlight ? 0.2 : 0.06);
        if (flockHeading.lengthSqr() > 1.0E-4) {
            horizontalDirection = BirdFlightTargeting.normalizeHorizontal(
                    horizontalDirection.add(flockHeading), horizontalDirection);
        }

        double speed = BirdFlightManager.decelerateNearLanding(this.flightSpeed, horizontalDistance,
                this.escapeFlight ? 3.0 : 2.4, 0.5);
        double lift = Mth.clamp(toTarget.y * 0.16, -0.11, 0.16);
        if (flightAge < 8) {
            lift += this.escapeFlight ? 0.24 : 0.11;
        }
        if (horizontalDistance < 1.6) {
            lift = Mth.clamp(toTarget.y * 0.22 - 0.05, -0.14, 0.06);
        }
        if (this.flightLandingTicks > 0) {
            speed = Math.max(speed, this.flightSpeed * 0.46);
            lift = Math.min(lift, this.flightLandingTicks > 70 ? -0.05 : -0.032);
            if (heightAboveTarget < 1.15) {
                lift = Math.max(lift, -0.026);
            }
        }

        Vec3 desired = horizontalDirection.scale(speed).add(0, lift, 0);
        Vec3 movement = this.getDeltaMovement().scale(0.32).add(desired.scale(0.68));

        if (BirdFlightManager.isStalledInAir(this, this.timeFlying, 0.006)) {
            Vec3 newTarget = this.findShortFlightTarget(null, this.escapeFlight,
                    this.escapeFlight ? 8 : 4, this.escapeFlight ? 16 : 10);
            if (newTarget != null) {
                this.flightTarget = newTarget;
                this.flightTicks = Math.max(this.flightTicks, 18);
                this.flightLandingTicks = 0;
            }
            movement = horizontalDirection.scale(Math.max(speed, this.flightSpeed * 0.65))
                    .add(0, this.escapeFlight ? 0.12 : 0.08, 0);
        }

        if (!this.isInWater() && !this.isEyeInFluid(FluidTags.WATER)) {
            this.blockedFlightTicks = Math.max(0, this.blockedFlightTicks - 1);
        } else {
            ++this.blockedFlightTicks;
            movement = movement.add(0, 0.08, 0);
        }

        if (this.blockedFlightTicks > 5) {
            Vec3 newTarget = this.findShortFlightTarget(null, false, 3, 8);
            if (newTarget == null) {
                this.finishControlledFlight(false);
                return;
            }
            this.flightTarget = newTarget;
            this.blockedFlightTicks = 0;
        }

        if (this.flightTicks <= 0) {
            if (this.onGround()) {
                this.finishControlledFlight(true);
                return;
            }
            this.flightTicks = 1;
        }

        this.setDeltaMovement(movement);
        this.faceMovement(movement);
        this.xxa = 0.0F;
        this.hasImpulse = true;
    }

    private void finishControlledFlight(boolean landed) {
        boolean wasEscapeFlight = this.escapeFlight;
        this.flightTarget = null;
        this.flightTicks = 0;
        this.flightDuration = 0;
        this.timeFlying = 0;
        this.blockedFlightTicks = 0;
        this.flightLandingTicks = 0;
        this.escapeFlight = false;
        this.setNoGravity(false);
        Vec3 movement = this.getDeltaMovement();

        if (landed) {
            this.setDeltaMovement(movement.x * 0.35, 0, movement.z * 0.35);
            this.airborneFlightAnimationTicks = 0;
        } else {
            this.setDeltaMovement(movement.x * 0.55, Math.max(movement.y * 0.35, -0.04), movement.z * 0.55);
            this.airborneFlightAnimationTicks = Math.max(this.airborneFlightAnimationTicks, 18);
        }

        int cooldown = wasEscapeFlight ? 55 + this.getRandom().nextInt(75)
                : (this.isTame() ? 100 + this.getRandom().nextInt(120) : 85 + this.getRandom().nextInt(95));
        this.flightCooldown = Math.max(this.flightCooldown, cooldown);
        if (wasEscapeFlight) {
            this.suppressOwnerFollow(140);
        }

        if (this.getBehaviorState().isEscape()) {
            this.behaviorStateLockTicks = 0;
            if (!landed && !this.onGround()) {
                this.setBehaviorStateFor(wasEscapeFlight ? SparrowBehaviorState.FLEEING : SparrowBehaviorState.SHORT_FLIGHT, 18);
            } else {
                this.setBehaviorState(SparrowBehaviorState.IDLE);
            }
        }
    }

    // ============ 飞行目标查找 ============
    private Vec3 findShortFlightTarget(Vec3 threatPosition, boolean escape, int minRadius, int maxRadius) {
        BlockPos origin = this.blockPosition();
        Vec3 bestTarget = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (int attempt = 0; attempt < 28; ++attempt) {
            double angle;
            if (escape && threatPosition != null) {
                Vec3 away = this.position().subtract(threatPosition).multiply(1.0, 0.0, 1.0);
                angle = away.lengthSqr() > 1.0E-4
                        ? Math.atan2(away.z, away.x) + this.randomSigned(0.75)
                        : this.getRandom().nextDouble() * Math.PI * 2.0;
            } else {
                Vec3 forward = BirdFlightTargeting.normalizeHorizontal(this.getViewVector(1.0F), this.getLookAngle());
                angle = attempt < 20
                        ? Math.atan2(forward.z, forward.x) + this.randomSigned(Math.toRadians(15.0))
                        : this.getRandom().nextDouble() * Math.PI * 2.0;
            }

            double radius = minRadius + this.getRandom().nextDouble() * (maxRadius - minRadius);
            int x = origin.getX() + Mth.floor(Math.cos(angle) * radius);
            int z = origin.getZ() + Mth.floor(Math.sin(angle) * radius);
            int y = origin.getY() + (escape ? this.randomBetween(2, 8) : this.randomBetween(-1, 4));
            BlockPos landing = this.findLandingSurface(new BlockPos(x, y, z), escape ? 9 : 6);

            if (landing != null) {
                double score = this.scoreShortFlightLanding(landing, threatPosition, escape);
                if (score > bestScore) {
                    bestScore = score;
                    bestTarget = new Vec3(landing.getX() + 0.5, landing.getY() + 0.05, landing.getZ() + 0.5);
                }
            }
        }
        return bestTarget;
    }

    private Vec3 findNearestShortFlightLandingTarget(int radius) {
        BlockPos origin = this.blockPosition();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int r = 1; r <= radius; ++r) {
            for (int xOffset = -r; xOffset <= r; ++xOffset) {
                for (int zOffset = -r; zOffset <= r; ++zOffset) {
                    if (Math.abs(xOffset) == r || Math.abs(zOffset) == r) {
                        mutable.set(origin.getX() + xOffset, origin.getY(), origin.getZ() + zOffset);
                        BlockPos landing = this.findLandingSurface(mutable, 10);
                        if (landing != null) {
                            return new Vec3(landing.getX() + 0.5, landing.getY() + 0.05, landing.getZ() + 0.5);
                        }
                    }
                }
            }
        }
        return null;
    }

    private BlockPos findLandingSurface(BlockPos center, int verticalRange) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int yOffset = verticalRange; yOffset >= -verticalRange; --yOffset) {
            mutable.set(center.getX(), center.getY() + yOffset, center.getZ());
            if (this.isSafeShortFlightLanding(mutable)) {
                return mutable.immutable();
            }
        }
        return null;
    }

    private boolean isSafeShortFlightLanding(BlockPos pos) {
        return BirdFlightTargeting.isSafeDryLanding(this, pos);
    }

    private double scoreShortFlightLanding(BlockPos pos, Vec3 threatPosition, boolean escape) {
        BlockState below = this.level().getBlockState(pos.below());
        double score = 0;
        if (below.is(Blocks.FARMLAND)) score += 6.0;
        if (below.is(BlockTags.WALLS) || below.is(BlockTags.LEAVES)) score += 11.0;
        if (below.getBlock() instanceof FenceBlock || below.getBlock() instanceof FenceGateBlock) score += 12.0;
        if (below.is(Blocks.GRASS_BLOCK) || below.is(Blocks.PODZOL)) score += 9.0;
        if (below.is(BlockTags.DIRT) || below.is(BlockTags.SAND)) score += 2.5;
        score -= Math.abs(pos.getY() - this.getY()) * 0.35;
        if (escape && threatPosition != null) {
            score += Math.min(16.0, new Vec3(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5)
                    .distanceToSqr(threatPosition) * 0.6);
        }
        return score;
    }

    // ============ 水上逃生 ============
    private void tickWaterEscape() {
        if (this.isInWater()) {
            this.getNavigation().stop();
            Vec3 target = this.findShortFlightTarget(null, true, 7, 16);
            if (target != null && !this.isControlledFlightActive()) {
                this.startControlledFlight(target, this.randomBetween(42, 72), ESCAPE_FLIGHT_SPEED, true);
            } else {
                Vec3 movement = this.getDeltaMovement().multiply(0.45, 0, 0.45).add(0, 0.32, 0);
                this.setNoGravity(true);
                this.setBehaviorStateFor(SparrowBehaviorState.SHORT_FLIGHT, 35);
                this.setDeltaMovement(movement);
                this.faceMovement(movement);
                this.xxa = 0.0F;
                this.hasImpulse = true;
            }
        }
    }

    // ============ 面包屑交互 ============
    public boolean isDistrusted(Player player) {
        if (this.isOwnedBy(player)) return false;
        return this.distrustedPlayer != null && this.distrustTicks > 0
                && this.distrustedPlayer.equals(player.getUUID());
    }

    public boolean hasDistrustMemory() {
        return this.distrustedPlayer != null && this.distrustTicks > 0;
    }

    private void rememberDistrustedPlayer(Player player) {
        this.distrustedPlayer = player.getUUID();
        this.distrustTicks = ATTACK_DISTRUST_TICKS;
        this.familiarTicks = 0;
        this.calmAroundPlayerTicks = 0;
    }

    public boolean isHungry() {
        return this.satiatedTicks <= 0;
    }

    public void restoreBreadcrumbSatiation() {
        this.satiatedTicks = Math.min(MAX_SATIATION_TICKS, this.satiatedTicks + FULL_SATIATION_TICKS);
    }

    public boolean hasBreadcrumbInterest() {
        return this.breadcrumbInterestTicks > 0 && this.noticedBreadcrumbPos != null;
    }

    private void noticeBreadcrumbs(BlockPos pos, Player player) {
        if (player == null || !this.isDistrusted(player)) {
            this.noticedBreadcrumbPos = pos.immutable();
            this.breadcrumbInterestTicks = Math.max(this.breadcrumbInterestTicks, 160 + this.getRandom().nextInt(120));
            this.satiatedTicks = Math.min(this.satiatedTicks, 700);
            this.calmAroundPlayerTicks = Math.max(this.calmAroundPlayerTicks, 60);
        }
    }

    public static void alertNearbyBreadcrumbs(ServerLevel level, BlockPos pos, Player player) {
        Vec3 center = Vec3.atCenterOf(pos);
        for (SparrowEntity sparrow : level.getEntitiesOfClass(SparrowEntity.class,
                new AABB(pos).inflate(20.0))) {
            if (!sparrow.isRemoved() && !sparrow.isInWater()) {
                double distance = sparrow.position().distanceToSqr(center);
                float chance = distance <= 36.0 ? 1.0F : (distance <= 144.0 ? 0.82F : 0.58F);
                if (sparrow.getRandom().nextFloat() <= chance) {
                    sparrow.noticeBreadcrumbs(pos, player);
                }
            }
        }
    }

    public BlockPos findNearbyBreadcrumbs(int horizontalRadius, int verticalRadius) {
        BlockPos origin = this.blockPosition();
        BlockPos bestPos = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (int xOffset = -horizontalRadius; xOffset <= horizontalRadius; ++xOffset) {
            for (int zOffset = -horizontalRadius; zOffset <= horizontalRadius; ++zOffset) {
                for (int yOffset = -verticalRadius; yOffset <= verticalRadius; ++yOffset) {
                    BlockPos candidate = origin.offset(xOffset, yOffset, zOffset);
                    if (this.canReadChunk(candidate)) {
                        BlockState state = this.level().getBlockState(candidate);
                        if (state.is(NeoGuanNiaoBlocks.BREADCRUMBS.get())) {
                            double distanceSqr = this.position().distanceToSqr(Vec3.atCenterOf(candidate));
                            int layers = state.getValue(BreadcrumbPileBlock.LAYERS);
                            double crowdedPenalty = this.level().getEntitiesOfClass(
                                    SparrowEntity.class, new AABB(candidate).inflate(1.15),
                                    other -> other != this && other.onGround()).size() * 1.35;
                            double score = layers * 6.0 - distanceSqr * 0.14 - crowdedPenalty;
                            if (bestPos == null || score > bestScore) {
                                bestPos = candidate.immutable();
                                bestScore = score;
                            }
                        }
                    }
                }
            }
        }
        return bestPos;
    }

    public Vec3 breadcrumbStandPosition(BlockPos pilePos) {
        double angle = (this.getId() & 7) / 8.0 * Math.PI * 2.0;
        double radius = 0.45 + (this.getId() % 3) * 0.08;
        return new Vec3(pilePos.getX() + 0.5 + Math.cos(angle) * radius,
                pilePos.getY(),
                pilePos.getZ() + 0.5 + Math.sin(angle) * radius);
    }

    public boolean shouldAvoidBreadcrumbs() {
        if (this.pendingScareTicks <= 0 && !this.isControlledFlightActive()) {
            Player player = this.level().getNearestPlayer(this, this.hasDistrustMemory() ? 12.0 : 7.0);
            return player != null && !this.isComfortableNear(player);
        }
        return true;
    }

    public void gainBreadcrumbConfidence() {
        this.familiarTicks = Math.min(MAX_FAMILIAR_TICKS, this.familiarTicks + 160);
        this.calmAroundPlayerTicks = Math.max(this.calmAroundPlayerTicks, 160);
    }

    // ============ 惊吓和警报 ============
    @SuppressWarnings("all")
    public boolean isComfortableNear(Player player) {
        if (!player.isCreative() && !player.isSpectator()) {
            if (this.isDistrusted(player)) return false;
            if (this.isTame()) return true;
            if (TAMING_ITEMS.test(player.getMainHandItem()) || TAMING_ITEMS.test(player.getOffhandItem())) return true;
            if (player.getMainHandItem().is(NeoGuanNiaoItems.BREADCRUMBS.get())
                    || player.getOffhandItem().is(NeoGuanNiaoItems.BREADCRUMBS.get())) return true;
            if (this.calmAroundPlayerTicks > 0) return true;

            float calmChance = 0.7F + Mth.clamp((float) this.familiarTicks / 3600.0F, 0.0F, 0.3F);
            if (this.getRandom().nextFloat() < calmChance) {
                this.calmAroundPlayerTicks = 80 + this.getRandom().nextInt(100);
                return true;
            }
            return false;
        }
        return true;
    }

    private void queueScareReaction(Vec3 sourcePosition, int delayTicks, SparrowScareReaction reaction) {
        if (delayTicks <= 0) {
            this.pendingScareSource = sourcePosition;
            this.pendingScareTicks = 1;
            this.pendingScareReaction = reaction;
        } else {
            if (this.pendingScareTicks == 0 || delayTicks < this.pendingScareTicks) {
                this.pendingScareSource = sourcePosition;
                this.pendingScareTicks = delayTicks;
                this.pendingScareReaction = reaction;
            }
        }
    }

    private void releasePendingScare() {
        Vec3 sourcePosition = this.pendingScareSource;
        SparrowScareReaction reaction = this.pendingScareReaction;
        this.pendingScareSource = null;
        this.pendingScareTicks = 0;
        this.pendingScareReaction = SparrowScareReaction.ESCAPE_FLIGHT;

        if (sourcePosition != null && !this.isRemoved()) {
            this.getNavigation().stop();
            if (this.isTame()) {
                this.triggerLookAround();
            } else if (reaction == SparrowScareReaction.LOOK_AROUND) {
                this.getLookControl().setLookAt(sourcePosition.x, sourcePosition.y + 0.4, sourcePosition.z, 30.0F, 30.0F);
                this.triggerLookAround();
            } else if (reaction == SparrowScareReaction.SHORT_HOP) {
                this.triggerLookAround();
                Vec3 away = DefaultRandomPos.getPosAway(this, 5, 3, sourcePosition);
                if (away != null) {
                    this.getNavigation().moveTo(away.x, away.y, away.z, 1.05);
                }
                if (this.onGround()) {
                    this.shortHop();
                }
            } else {
                if (!this.startEscapeFlight(sourcePosition)) {
                    Vec3 away = DefaultRandomPos.getPosAway(this, 12, 7, sourcePosition);
                    if (away != null) {
                        this.setBehaviorStateFor(SparrowBehaviorState.FLEEING, 50);
                        this.getNavigation().moveTo(away.x, away.y, away.z, 1.2);
                        if (this.onGround() && this.getRandom().nextBoolean()) {
                            this.shortHop();
                        }
                    }
                }
            }
        }
    }

    public void alertNearbySparrows(Entity source) {
        Vec3 sourcePosition = source.position();
        Player attacker = source instanceof Player player ? player : null;

        for (SparrowEntity sparrow : this.level().getEntitiesOfClass(SparrowEntity.class,
                this.getBoundingBox().inflate(18.0))) {
            if (sparrow != this && !sparrow.isTame()) {
                sparrow.familiarTicks = 0;
                sparrow.calmAroundPlayerTicks = 0;
                if (attacker != null) {
                    sparrow.rememberDistrustedPlayer(attacker);
                }
                sparrow.birdBrain.onFrightened(attacker != null ? 0.4F : 0.25F);
                sparrow.getNavigation().stop();
                double distance = sparrow.position().distanceToSqr(sourcePosition);
                int delay = scareDelayForDistance(sparrow, distance);
                SparrowScareReaction reaction = scareReactionForDistance(sparrow, distance, attacker != null);
                sparrow.queueScareReaction(sourcePosition, delay, reaction);
            }
        }
    }

    private static int scareDelayForDistance(SparrowEntity sparrow, double distance) {
        int delay = 3 + Mth.floor(distance * 0.62);
        delay += sparrow.getRandom().nextInt(7);
        if (distance < 3.0) {
            delay = 1 + sparrow.getRandom().nextInt(3);
        }
        return Mth.clamp(delay, 1, 22);
    }

    private static SparrowScareReaction scareReactionForDistance(SparrowEntity sparrow, double distance, boolean severe) {
        float familiar = Mth.clamp((float) sparrow.familiarTicks / 3600.0F, 0.0F, 1.0F);
        float escapeChance;
        if (distance < 3.0) {
            escapeChance = severe ? 1.0F : 0.86F;
        } else if (distance < 8.0) {
            escapeChance = severe ? 0.82F : 0.55F;
        } else {
            escapeChance = severe ? 0.52F : 0.24F;
        }
        escapeChance -= familiar * 0.22F;

        float roll = sparrow.getRandom().nextFloat();
        if (roll < escapeChance) {
            return SparrowScareReaction.ESCAPE_FLIGHT;
        } else if (roll < escapeChance + (distance < 8.0 ? 0.35F : 0.28F)) {
            return SparrowScareReaction.SHORT_HOP;
        } else {
            return SparrowScareReaction.LOOK_AROUND;
        }
    }

    // ============ 短跳 ============
    public void shortHop() {
        float yaw = this.getYRot() * ((float) Math.PI / 180F);
        double side = (this.getRandom().nextDouble() - 0.5) * 0.12;
        this.setDeltaMovement(-Math.sin(yaw) * 0.13 + side, 0.23, Math.cos(yaw) * 0.13 - side);
        this.hasImpulse = true;
        if (this.getBehaviorState() == SparrowBehaviorState.FLEEING) {
            this.setBehaviorStateFor(SparrowBehaviorState.FLEEING, 24);
        } else {
            this.setBehaviorStateFor(SparrowBehaviorState.LOOK_AROUND, 18);
        }
    }

    // ============ 主人传送 ============
    public boolean tryTeleportNearOwner(LivingEntity owner) {
        BlockPos ownerPos = owner.blockPosition();
        for (int attempt = 0; attempt < 12; ++attempt) {
            int xOffset = this.randomBetween(-4, 4);
            int zOffset = this.randomBetween(-4, 4);
            if (Math.abs(xOffset) >= 2 || Math.abs(zOffset) >= 2) {
                BlockPos candidate = ownerPos.offset(xOffset, 0, zOffset);
                for (int yOffset = 2; yOffset >= -3; --yOffset) {
                    BlockPos landing = candidate.offset(0, yOffset, 0);
                    if (this.isSafeOwnerTeleportPosition(landing)) {
                        this.teleportTo(landing.getX() + 0.5, landing.getY(), landing.getZ() + 0.5);
                        this.setDeltaMovement(Vec3.ZERO);
                        this.getNavigation().stop();
                        this.xxa = 0.0F;
                        this.setBehaviorStateFor(SparrowBehaviorState.FOLLOWING_OWNER, 20);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isSafeOwnerTeleportPosition(BlockPos pos) {
        if (this.canReadChunk(pos) && this.canReadChunk(pos.above())) {
            Level level = this.level();
            BlockPos belowPos = pos.below();
            BlockState below = level.getBlockState(belowPos);
            if ((below.isFaceSturdy(level, belowPos, Direction.UP) || below.is(BlockTags.WALLS))
                    && !below.is(Blocks.WATER) && !below.is(Blocks.LAVA)) {
                if (level.getFluidState(pos).isEmpty() && level.getFluidState(pos.above()).isEmpty()) {
                    return level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()
                            && level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty();
                }
            }
        }
        return false;
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

    @SuppressWarnings("all")
    private SparrowIdleAnimationChoice chooseIdleAnimation() {
        BirdIntent intent = this.birdBrain.currentIntent();
        float fear = this.birdBrain.motivation().fear();
        float alertness = this.birdBrain.motivation().alertness();

        int baseWeight = 2;
        int tailWeight = 1;
        int peckWeight = 1;
        int lookWeight = 1;

        if (intent == BirdIntent.FORAGE) {
            peckWeight += 3;
            ++lookWeight;
        } else if (intent == BirdIntent.WATCH || intent == BirdIntent.ALERT) {
            lookWeight += 3;
            peckWeight = Math.max(0, peckWeight - 1);
        } else if (intent == BirdIntent.ROOST) {
            baseWeight += 3;
            tailWeight += 2;
            peckWeight = 0;
        }

        if (fear > 0.45F || alertness > 0.45F) {
            lookWeight += 2;
            peckWeight = Math.max(0, peckWeight - 1);
        }

        int total = baseWeight + tailWeight + peckWeight + lookWeight;
        int roll = this.getRandom().nextInt(Math.max(1, total));
        roll -= baseWeight;
        if (roll < 0) return SparrowIdleAnimationChoice.BASE;
        roll -= tailWeight;
        if (roll < 0) return SparrowIdleAnimationChoice.TAIL;
        roll -= lookWeight;
        return roll < 0 ? SparrowIdleAnimationChoice.LOOK_AROUND : SparrowIdleAnimationChoice.PECK;
    }

    private boolean shouldPlayFlyAnimation() {
        return BirdFlightManager.shouldPlayFlyAnimation(this, this.getBehaviorState().isAirborne(),
                this.onGround(), this.isInWater(), this.getDeltaMovement(), this.airborneFlightAnimationTicks);
    }

    private <T extends SparrowEntity> PlayState movementController(AnimationState<T> animationState) {
        RawAnimation guidePreviewRawAnimation = this.guidePreviewAnimation.animation();
        if (guidePreviewRawAnimation != null) {
            return animationState.setAndContinue(guidePreviewRawAnimation);
        }

        SparrowBehaviorState state = this.getBehaviorState();
        if (this.shouldPlayFlyAnimation()) {
            return animationState.setAndContinue(FLY_ANIMATION);
        }

        double horizontalSpeed = this.getDeltaMovement().lengthSqr();
        if (horizontalSpeed > WALKING_SPEED_THRESHOLD || !this.getNavigation().isDone()) {
            return animationState.setAndContinue(WALK_ANIMATION);
        }

        if (state == SparrowBehaviorState.PECKING) {
            return animationState.setAndContinue(PECK_ANIMATION);
        }
        if (state == SparrowBehaviorState.LOOK_AROUND || state == SparrowBehaviorState.ALERT) {
            return animationState.setAndContinue(LOOK_AROUND_ANIMATION);
        }
        if (state == SparrowBehaviorState.PERCHING || state == SparrowBehaviorState.ROOSTING) {
            return animationState.setAndContinue(this.currentIdleAnimation == SparrowIdleAnimationChoice.TAIL
                    ? TAIL_ANIMATION : IDLE_ANIMATION);
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