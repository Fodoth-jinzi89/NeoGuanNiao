package net.fodoth.skina.neoguanniao.content.bird.budgerigar;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.content.bath.BirdBathAttraction;
import net.fodoth.skina.neoguanniao.content.bath.BirdBathBlockEntity;
import net.fodoth.skina.neoguanniao.content.bath.BirdBathContentType;
import net.fodoth.skina.neoguanniao.content.bath.BirdBathFeedingAnimatable;
import net.fodoth.skina.neoguanniao.content.bath.BirdBathMountable;
import net.fodoth.skina.neoguanniao.content.bath.BirdBathUseGoal;
import net.fodoth.skina.neoguanniao.content.bird.brain.BirdBrain;
import net.fodoth.skina.neoguanniao.content.bird.budgerigar.goal.*;
import net.fodoth.skina.neoguanniao.content.bird.flight.BirdFlightAware;
import net.fodoth.skina.neoguanniao.content.bird.flight.BirdFlightBoids;
import net.fodoth.skina.neoguanniao.content.bird.flight.BirdFlightController;
import net.fodoth.skina.neoguanniao.content.bird.flight.BirdFlightProfile;
import net.fodoth.skina.neoguanniao.content.bird.flight.BirdFlightTargeting;
import net.fodoth.skina.neoguanniao.content.bird.scale.BirdModelScale;
import net.fodoth.skina.neoguanniao.content.bird.scale.BirdModelScaleProfile;
import net.fodoth.skina.neoguanniao.content.bird.scale.ScalableBirdModel;
import net.fodoth.skina.neoguanniao.content.bird.species.BudgerigarProfile;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoEntityTypes;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Objects;
import java.util.UUID;

public class BudgerigarEntity extends TamableAnimal implements GeoEntity, FlyingAnimal, ScalableBirdModel, BirdFlightAware, BirdBathMountable, BirdBathFeedingAnimatable {

    // ============ 数据序列化器 ============
    private static final EntityDataAccessor<Integer> BEHAVIOR_STATE =
            SynchedEntityData.defineId(BudgerigarEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SKIN_VARIANT =
            SynchedEntityData.defineId(BudgerigarEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> MODEL_SCALE =
            SynchedEntityData.defineId(BudgerigarEntity.class, EntityDataSerializers.FLOAT);

    // ============ 常量 ============
    private static final byte TAMING_FAILED_EVENT = 6;
    private static final byte TAMING_SUCCEEDED_EVENT = 7;
    private static final ResourceLocation CHIRPY_PARTNER_ADVANCEMENT =
            ResourceLocation.fromNamespaceAndPath(NeoGuanNiao.MODID, "husbandry/chirpy_partner");
    private static final double WALKING_SPEED_THRESHOLD = 0.0025;
    private static final int MUSIC_SCAN_RADIUS = 8;
    private static final int MUSIC_GROUP_RADIUS = 10;
    private static final int AMBIENT_AIR_CRUISE_MIN_TICKS = 110;
    private static final int AMBIENT_AIR_CRUISE_RANDOM_TICKS = 120;
    private static final int ESCAPE_AIR_CRUISE_MIN_TICKS = 80;
    private static final int ESCAPE_AIR_CRUISE_RANDOM_TICKS = 70;
    private static final BirdFlightProfile FLIGHT_PROFILE = BirdFlightProfile.BUDGERIGAR;

    // ============ 动画定义 ============
    public static final RawAnimation IDLE_ANIMATION = RawAnimation.begin().thenLoop("idle");
    public static final RawAnimation PREEN_ANIMATION = RawAnimation.begin().thenPlay("idle_diff_1").thenLoop("idle");
    public static final RawAnimation CURIOUS_ANIMATION = RawAnimation.begin().thenPlay("idle_diff_2").thenLoop("idle");
    public static final RawAnimation DANCE_ANIMATION = RawAnimation.begin().thenLoop("idle_diff_3");
    public static final RawAnimation EAT_ANIMATION = RawAnimation.begin().thenPlay("eat").thenLoop("idle");
    public static final RawAnimation SLEEP_ANIMATION = RawAnimation.begin().thenPlay("sleep").thenLoop("sleep_loop");
    public static final RawAnimation SLEEP_LOOP_ANIMATION = RawAnimation.begin().thenLoop("sleep_loop");
    public static final RawAnimation WALK_ANIMATION = RawAnimation.begin().thenLoop("walk");
    public static final RawAnimation FLY_ANIMATION = RawAnimation.begin().thenLoop("fly_flapping_wing_loop");

    // ============ 成员变量 ============
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    private final BirdBrain birdBrain;
    private BudgerigarBehaviorState behaviorState;
    private BudgerigarGuidePreviewAnimation guidePreviewAnimation;
    private int behaviorStateLockTicks;
    private int eatingTicks;
    private int foodCooldown;
    private int trustTicks;
    public int curiousTicks;
    private int nearbyMusicTicks;
    private int musicScanCooldown;
    private int externalFrightTicks;
    private int flightTicks;
    private int timeFlying;
    public int flightCooldown;
    private int hoverRetargetTicks;
    private int pendingFrightTicks;
    private int pendingFrightDuration;
    private int idleAnimationTicks;
    private int postTameActionTicks;
    private int postTameActionSwapTicks;
    private boolean escapeFlightActive;
    private boolean landingFlight;
    private RawAnimation currentIdleAnimation;
    private UUID interestedPlayerUUID;
    private BlockPos musicSourcePos;
    private Vec3 frightSource;
    private Vec3 pendingFrightSource;
    private Vec3 flightTarget;

    // ============ 构造方法 ============
    public BudgerigarEntity(EntityType<? extends BudgerigarEntity> entityType, Level level) {
        super(entityType, level);
        this.birdBrain = new BirdBrain(this, BudgerigarProfile.INSTANCE);
        this.behaviorState = BudgerigarBehaviorState.IDLE;
        this.guidePreviewAnimation = BudgerigarGuidePreviewAnimation.NONE;
        this.currentIdleAnimation = IDLE_ANIMATION;

        this.moveControl = new FlyingMoveControl(this, 10, true);
        this.setPathfindingMalus(PathType.LEAVES, 0.0F);
        this.setPathfindingMalus(PathType.WATER, -1.0F);
        this.setPathfindingMalus(PathType.DANGER_FIRE, 16.0F);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, 16.0F);

        this.musicScanCooldown = 10 + this.getRandom().nextInt(20);
    }

    // ============ 静态工厂方法 ============
    public static AttributeSupplier.Builder createAttributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 6.0)
                .add(Attributes.MOVEMENT_SPEED, 0.24)
                .add(Attributes.FLYING_SPEED, 0.32)
                .add(Attributes.FOLLOW_RANGE, 18.0);
    }

    public static boolean canSpawn(EntityType<BudgerigarEntity> entityType, ServerLevelAccessor level,
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
        var entities = level.getEntitiesOfClass(BudgerigarEntity.class,
                new AABB(pos.getX() - 8, pos.getY() - 4, pos.getZ() - 8,
                        pos.getX() + 8, pos.getY() + 4, pos.getZ() + 8));

        // 如果附近同类型实体少于 8 个，允许生成
        return entities.size() <= 8;
    }

    // ============ 繁殖 ============
    @Override
    public @Nullable BudgerigarEntity getBreedOffspring(@NotNull ServerLevel level, @NotNull AgeableMob mate) {
        BudgerigarEntity child = NeoGuanNiaoEntityTypes.BUDGERIGAR.get().create(level);
        if (child != null) {
            child.setSkinVariant(this.getRandom().nextInt(BudgerigarDefinition.TEXTURE_VARIANTS.length));
            float mateScale = mate instanceof BudgerigarEntity other ? other.getIndividualModelScale() : this.getIndividualModelScale();
            child.setIndividualModelScale(BirdModelScale.inheritIndividualScale(
                    child.getRandom(), this.getIndividualModelScale(), mateScale, child.modelScaleProfile()));
        }
        return child;
    }

    // ============ AI 注册 ============
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BudgerigarFrightGoal(this));
        this.goalSelector.addGoal(2, new BudgerigarMusicDanceGoal(this));
        this.goalSelector.addGoal(3, new BudgerigarEatFoodGoal(this));
        this.goalSelector.addGoal(4, new BirdBathUseGoal(
                this, 1.0, 11.0, 36,
                BirdBathAttraction::isAttractiveToBudgerigar,
                this::canStartFoodGoal,
                (bath) -> this.setBehaviorState(BudgerigarBehaviorState.FORAGING),
                this::consumeBirdBathServing,
                (bath, consumed) -> {
                    if (!this.isEating() && this.getBehaviorState() == BudgerigarBehaviorState.FORAGING) {
                        this.setBehaviorState(BudgerigarBehaviorState.IDLE);
                    }
                }
        ));
        this.goalSelector.addGoal(5, new BudgerigarSentinelGoal(this));
        this.goalSelector.addGoal(6, new BudgerigarRoostGoal(this));
        this.goalSelector.addGoal(7, new BudgerigarFollowOwnerGoal(this, 1.0, 2.5F, 8.5F));
        this.goalSelector.addGoal(8, new BudgerigarFlockGoal(this));
        this.goalSelector.addGoal(9, new BudgerigarCuriousFollowGoal(this));
        this.goalSelector.addGoal(10, new BudgerigarIdleGoal(this));
        this.goalSelector.addGoal(11, new RandomLookAroundGoal(this));
    }

    // ============ 导航 ============
    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanFloat(false);
        navigation.setCanOpenDoors(true);
        navigation.setCanPassDoors(true);
        return navigation;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, @NotNull BlockState state, @NotNull BlockPos pos) {
        this.fallDistance = 0.0F;
    }

    // ============ 数据序列化 ============
    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(BEHAVIOR_STATE, BudgerigarBehaviorState.IDLE.ordinal());
        builder.define(SKIN_VARIANT, 0);
        builder.define(MODEL_SCALE, 1.0F);
    }

    @Override
    public void onSyncedDataUpdated(@NotNull EntityDataAccessor<?> key) {
        if (BEHAVIOR_STATE.equals(key)) {
            this.behaviorState = decodeBehaviorState(this.getEntityData().get(BEHAVIOR_STATE));
        }
        super.onSyncedDataUpdated(key);
    }

    // ============ 生成数据 ============
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

        this.setSkinVariant(
                this.getRandom().nextInt(BudgerigarDefinition.TEXTURE_VARIANTS.length)
        );

        this.randomizeModelScale();

        return data;
    }

    // ============ Tick 更新 ============
    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) {
            this.tickClientAnimationCounters();
        } else {
            this.birdBrain.tick();
            this.tickCounters();
            this.tickMusicAwareness();
            this.tickEating();
            this.tickPostTameAction();
            this.tickWaterEscape();
            this.tickPendingFright();
            this.tickFlight();
            this.tickAmbientAirCruise();
            this.tickBehaviorFallback();
            this.tickGroundMovementFacing();
        }
    }

    // ============ 交互 ============
    @Override
    public @NotNull InteractionResult mobInteract(Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (isCocoa(stack)) {
            if (!this.level().isClientSide) {
                this.setBehaviorStateFor(BudgerigarBehaviorState.CURIOUS, 30);
                this.playInteractionSound();
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (isEdibleFood(stack)) {
            if (this.level().isClientSide) {
                return InteractionResult.sidedSuccess(true);
            }
            if (this.isEating()) {
                this.playInteractionSound();
                return InteractionResult.SUCCESS;
            }

            ItemStack eaten = stack.copy();
            eaten.setCount(1);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }

            boolean wasTame = this.isTame();
            this.interestedPlayerUUID = player.getUUID();
            this.playInteractionSound();
            this.startEatingFood(eaten);
            this.addTrust(420);
            this.curiousTicks = Math.max(this.curiousTicks, 260);
            this.shareTrustNearby(120);
            this.updateTrustedOwner(player);

            if (!wasTame && this.isTame()) {
                this.startTameCelebration(player);
                this.awardChirpyPartnerAdvancement(player);
                this.level().broadcastEntityEvent(this, TAMING_SUCCEEDED_EVENT);
            } else if (!wasTame) {
                this.level().broadcastEntityEvent(this, TAMING_FAILED_EVENT);
            }

            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == TAMING_SUCCEEDED_EVENT) {
            this.setTame(true, false);
        } else if (id == TAMING_FAILED_EVENT) {
            this.setTame(false, false);
        } else {
            super.handleEntityEvent(id);
        }
    }

    // ============ 受伤 ============
    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && !this.level().isClientSide) {
            this.clearEating();
            this.interestedPlayerUUID = null;
            this.nearbyMusicTicks = 0;
            Entity attacker = source.getEntity();
            Vec3 sourcePos = attacker == null ? this.position() : attacker.position();
            this.birdBrain.onFrightened(attacker instanceof Player ? 0.08F : 0.18F);
            this.getNavigation().stop();
            this.setBehaviorStateFor(BudgerigarBehaviorState.ALERT, attacker instanceof Player ? 35 : 55);
            if (!(attacker instanceof Player)) {
                this.queueFrightFrom(sourcePos, 18 + this.getRandom().nextInt(16));
            }
        }
        return hurt;
    }

    // ============ NBT 保存/加载 ============
    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        this.birdBrain.save(compoundTag);
        compoundTag.putInt("BudgerigarTrustTicks", this.trustTicks);
        compoundTag.putInt("BudgerigarCuriousTicks", this.curiousTicks);
        compoundTag.putInt("BudgerigarSkinVariant", this.getSkinVariant());
        BirdModelScale.save(compoundTag, this.getIndividualModelScale(), this.modelScaleProfile());
        if (this.interestedPlayerUUID != null) {
            compoundTag.putUUID("BudgerigarInterestedPlayer", this.interestedPlayerUUID);
        }
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.birdBrain.load(compoundTag);
        this.trustTicks = compoundTag.getInt("BudgerigarTrustTicks");
        this.curiousTicks = compoundTag.getInt("BudgerigarCuriousTicks");
        if (compoundTag.contains("BudgerigarSkinVariant", CompoundTag.TAG_INT)) {
            this.setSkinVariant(compoundTag.getInt("BudgerigarSkinVariant"));
        }
        if (compoundTag.contains("BirdModelScale", CompoundTag.TAG_FLOAT)) {
            this.setIndividualModelScale(BirdModelScale.load(compoundTag, this.modelScaleProfile()));
        } else {
            this.randomizeModelScale();
        }
        if (compoundTag.hasUUID("BudgerigarInterestedPlayer")) {
            this.interestedPlayerUUID = compoundTag.getUUID("BudgerigarInterestedPlayer");
        }
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
        return this.flightTicks > 0 || this.landingFlight
                || this.getBehaviorState().isAirborne()
                || this.isInWater() && !this.onGround();
    }

    @Override
    public boolean isBirdLanding() {
        return this.landingFlight;
    }

    @Override
    public boolean isBirdEscaping() {
        return this.escapeFlightActive;
    }

    // ============ 行为状态 ============
    public BudgerigarBehaviorState getBehaviorState() {
        return decodeBehaviorState(this.getEntityData().get(BEHAVIOR_STATE));
    }

    public void setBehaviorState(BudgerigarBehaviorState state) {
        if (state == null) {
            state = BudgerigarBehaviorState.IDLE;
        }
        this.behaviorState = state;
        this.getEntityData().set(BEHAVIOR_STATE, state.ordinal());
    }

    public void setBehaviorStateFor(BudgerigarBehaviorState state, int ticks) {
        this.setBehaviorState(state);
        this.behaviorStateLockTicks = Math.max(this.behaviorStateLockTicks, ticks);
    }

    // ============ 纹理和缩放 ============
    public ResourceLocation getTextureResource() {
        return BudgerigarDefinition.textureForVariant(this.getSkinVariant());
    }

    @Override
    public BirdModelScaleProfile modelScaleProfile() {
        return BirdModelScaleProfile.BUDGERIGAR;
    }

    @Override
    public float getIndividualModelScale() {
        return BirdModelScale.sanitize(this.getEntityData().get(MODEL_SCALE), this.modelScaleProfile());
    }

    @Override
    public void setIndividualModelScale(float scale) {
        this.getEntityData().set(MODEL_SCALE, BirdModelScale.sanitize(scale, this.modelScaleProfile()));
    }

    public int getSkinVariant() {
        return Mth.clamp(this.getEntityData().get(SKIN_VARIANT), 0, BudgerigarDefinition.TEXTURE_VARIANTS.length - 1);
    }

    private void setSkinVariant(int variant) {
        int clamped = Mth.clamp(variant, 0, BudgerigarDefinition.TEXTURE_VARIANTS.length - 1);
        this.getEntityData().set(SKIN_VARIANT, clamped);
    }

    public void setSkinVariantForRendering(int variant) {
        this.setSkinVariant(variant);
    }

    public void setGuidePreviewAnimation(BudgerigarGuidePreviewAnimation guidePreviewAnimation) {
        this.guidePreviewAnimation = guidePreviewAnimation == null
                ? BudgerigarGuidePreviewAnimation.NONE
                : guidePreviewAnimation;
    }

    // ============ 状态查询 ============
    public boolean canStartFoodGoal() {
        return this.foodCooldown <= 0 && !this.isEating() && !this.isPassenger()
                && !this.isDancing() && !this.isSleepingOrRoosting()
                && !this.getBehaviorState().isEscape();
    }

    public boolean canStartSocialGoal() {
        return this.isActiveTime() && !this.isEating() && !this.isDancing() && !this.isSleepingOrRoosting() && !this.getBehaviorState().isEscape();
    }

    public boolean isActiveTime() {
        long time = this.level().getDayTime() % 24000L;
        return time >= 23000L || time < 11500L;
    }

    public boolean isRoostTime() {
        long time = this.level().getDayTime() % 24000L;
        return time >= 11500L && time < 23000L;
    }

    public boolean isBusyWithMusicOrSleep() {
        return this.isDancing() || this.isSleepingOrRoosting();
    }

    public boolean isDancing() {
        return this.nearbyMusicTicks > 0 || this.getBehaviorState() == BudgerigarBehaviorState.DANCING;
    }

    public boolean isEating() {
        return this.eatingTicks > 0 || this.getBehaviorState() == BudgerigarBehaviorState.EATING;
    }

    @Override
    public boolean isFlying() {
        BudgerigarBehaviorState state = this.getBehaviorState();
        return this.flightTicks > 0 || this.landingFlight || !this.onGround()
                || this.isInWater() || state == BudgerigarBehaviorState.FLYING
                || state == BudgerigarBehaviorState.FLEEING && !this.onGround();
    }

    public boolean isFlightInProgress() {
        return this.flightTicks > 0 || this.landingFlight;
    }

    public boolean isSleepingOrRoosting() {
        BudgerigarBehaviorState state = this.getBehaviorState();
        return state == BudgerigarBehaviorState.SLEEPING || state == BudgerigarBehaviorState.ROOSTING;
    }

    // ============ Getter 方法 ============
    int trustTicks() {
        return this.trustTicks;
    }

    public int nearbyMusicTicks() {
        return this.nearbyMusicTicks;
    }

    BlockPos musicSourcePos() {
        return this.musicSourcePos;
    }

    public boolean shouldFlee() {
        return this.externalFrightTicks > 0 && this.frightSource != null;
    }

    public Vec3 frightSource() {
        return this.frightSource;
    }

    // ============ 核心行为方法 ============

    void addTrust(int amount) {
        this.trustTicks = Mth.clamp(this.trustTicks + amount, 0, 6000);
    }

    void shareTrustNearby(int amount) {
        for (BudgerigarEntity budgerigar : this.level().getEntitiesOfClass(
                BudgerigarEntity.class, this.getBoundingBox().inflate(10.0))) {
            if (budgerigar != this) {
                budgerigar.addTrust(amount);
                budgerigar.curiousTicks = Math.max(budgerigar.curiousTicks, 80);
            }
        }
    }

    void startEatingFood(ItemStack foodStack) {
        this.getNavigation().stop();
        this.eatingTicks = 35 + this.getRandom().nextInt(21);
        this.foodCooldown = 90 + this.getRandom().nextInt(60);
        this.setBehaviorStateFor(BudgerigarBehaviorState.EATING, this.eatingTicks);
        this.birdBrain.onEat(0.35F);
        this.playSound(SoundEvents.GENERIC_EAT, 0.45F, 1.35F + this.getRandom().nextFloat() * 0.2F);
    }

    private void consumeBirdBathServing(BirdBathBlockEntity bath, BirdBathContentType contentType) {
        if (contentType == BirdBathContentType.BREAD) {
            this.startEatingFood(new ItemStack(Items.BREAD));
        } else {
            this.getNavigation().stop();
            this.eatingTicks = 24 + this.getRandom().nextInt(13);
            this.foodCooldown = 70 + this.getRandom().nextInt(45);
            this.setBehaviorStateFor(BudgerigarBehaviorState.EATING, this.eatingTicks);
            this.birdBrain.onEat(0.16F);
            this.playSound(SoundEvents.PARROT_EAT, 0.32F, 1.25F + this.getRandom().nextFloat() * 0.18F);
        }
    }

    public void consumeItemEntity(ItemEntity itemEntity) {
        ItemStack stack = itemEntity.getItem();
        ItemStack eaten = stack.copy();
        eaten.setCount(1);
        stack.shrink(1);
        if (stack.isEmpty()) {
            itemEntity.discard();
        } else {
            itemEntity.setItem(stack);
        }
        this.startEatingFood(eaten);
        this.addTrust(260);
        this.curiousTicks = Math.max(this.curiousTicks, 180);
        this.shareTrustNearby(80);
    }

    void frightenFrom(Vec3 sourcePos, int ticks) {
        this.frightSource = sourcePos;
        this.externalFrightTicks = Math.max(this.externalFrightTicks, ticks);
        this.pendingFrightTicks = 0;
        this.pendingFrightDuration = 0;
        this.pendingFrightSource = null;
        this.setBehaviorStateFor(BudgerigarBehaviorState.FLEEING, Math.min(90, ticks));
        if (this.flightCooldown <= 0 && this.onGround()) {
            this.startEscapeFlight(sourcePos);
        }
    }

    void alertNearbyBudgerigars(Vec3 sourcePos, int ticks) {
        for (BudgerigarEntity budgerigar : this.level().getEntitiesOfClass(
                BudgerigarEntity.class, this.getBoundingBox().inflate(14.0))) {
            if (budgerigar != this) {
                budgerigar.birdBrain.onFrightened(0.06F);
                budgerigar.setBehaviorStateFor(BudgerigarBehaviorState.ALERT, 24 + budgerigar.getRandom().nextInt(24));
                budgerigar.curiousTicks = Math.max(budgerigar.curiousTicks, 40);
            }
        }
    }

    void queueFrightFrom(Vec3 sourcePos, int delayTicks) {
        if (this.isEating()) {
            this.clearEating();
        }
        this.pendingFrightSource = sourcePos;
        this.pendingFrightDuration = Math.max(this.pendingFrightDuration, 45);
        if (this.pendingFrightTicks <= 0) {
            this.pendingFrightTicks = Math.max(1, delayTicks);
        } else {
            this.pendingFrightTicks = Math.clamp(delayTicks, 1, this.pendingFrightTicks);
        }
        this.setBehaviorStateFor(BudgerigarBehaviorState.ALERT, Math.min(32, this.pendingFrightTicks + 10));
    }

    void triggerMusic(BlockPos sourcePos, int ticks) {
        this.musicSourcePos = sourcePos;
        this.nearbyMusicTicks = Math.max(this.nearbyMusicTicks, ticks);
        if (!this.isEating() && !this.getBehaviorState().isEscape()) {
            this.setBehaviorStateFor(BudgerigarBehaviorState.DANCING, Math.min(ticks, 80));
        }
    }

    public void startShortFlight(Vec3 target, boolean fleeing) {
        if (this.flightCooldown <= 0 && this.flightTicks <= 0 && !this.landingFlight) {
            this.escapeFlightActive = fleeing;
            this.flightTarget = target == null ? this.findAirCruiseTarget(fleeing) : target;
            this.flightTicks = fleeing
                    ? ESCAPE_AIR_CRUISE_MIN_TICKS + this.getRandom().nextInt(ESCAPE_AIR_CRUISE_RANDOM_TICKS)
                    : AMBIENT_AIR_CRUISE_MIN_TICKS + this.getRandom().nextInt(AMBIENT_AIR_CRUISE_RANDOM_TICKS);
            this.timeFlying = 0;
            this.hoverRetargetTicks = this.nextHoverRetargetDelay();
            this.setNoGravity(true);
            this.setSilent(false);
            this.getNavigation().stop();
            this.setBehaviorStateFor(fleeing ? BudgerigarBehaviorState.FLEEING : BudgerigarBehaviorState.FLYING,
                    fleeing ? 100 : 90);
        }
    }

    // ============ 静态工具方法 ============
    public static boolean isEdibleFood(ItemStack stack) {
        return !stack.isEmpty() && (stack.is(Items.WHEAT_SEEDS) || stack.is(Items.MELON_SEEDS)
                || stack.is(Items.PUMPKIN_SEEDS) || stack.is(Items.BEETROOT_SEEDS)
                || stack.is(Items.TORCHFLOWER_SEEDS) || stack.is(Items.PITCHER_POD)
                || stack.is(Items.APPLE) || stack.is(Items.GOLDEN_APPLE)
                || stack.is(Items.ENCHANTED_GOLDEN_APPLE));
    }

    static boolean isCocoa(ItemStack stack) {
        return !stack.isEmpty() && stack.is(Items.COCOA_BEANS);
    }

    // ============ 私有辅助方法 ============
    private void randomizeModelScale() {
        this.setIndividualModelScale(BirdModelScale.randomIndividualScale(this.getRandom(), this.modelScaleProfile()));
    }

    private void playInteractionSound() {
        this.playSound(NeoGuanNiaoSoundEvents.BUDGERIGAR_INTERACT.get(), 0.42F, 0.95F + this.getRandom().nextFloat() * 0.18F);
    }

    private static BudgerigarBehaviorState decodeBehaviorState(int ordinal) {
        BudgerigarBehaviorState[] values = BudgerigarBehaviorState.values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : BudgerigarBehaviorState.IDLE;
    }

    // ============ Tick 方法 ============

    private void tickCounters() {
        if (this.behaviorStateLockTicks > 0) {
            --this.behaviorStateLockTicks;
        }
        if (this.foodCooldown > 0) {
            --this.foodCooldown;
        }
        if (this.flightCooldown > 0) {
            --this.flightCooldown;
        }
        if (this.externalFrightTicks > 0) {
            --this.externalFrightTicks;
        }
        if (this.curiousTicks > 0) {
            --this.curiousTicks;
        }
        if (this.trustTicks > 0 && this.tickCount % 40 == 0) {
            --this.trustTicks;
        }
        if (this.idleAnimationTicks > 0) {
            --this.idleAnimationTicks;
        }
        if (this.nearbyMusicTicks > 0) {
            --this.nearbyMusicTicks;
        }
        if (this.postTameActionSwapTicks > 0) {
            --this.postTameActionSwapTicks;
        }
    }

    private void tickClientAnimationCounters() {
        if (this.behaviorStateLockTicks > 0) {
            --this.behaviorStateLockTicks;
        }
        if (this.eatingTicks > 0) {
            --this.eatingTicks;
        }
        if (this.idleAnimationTicks > 0) {
            --this.idleAnimationTicks;
        }
        if (this.postTameActionTicks > 0) {
            --this.postTameActionTicks;
        }
        if (this.postTameActionSwapTicks > 0) {
            --this.postTameActionSwapTicks;
        }
    }

    private void tickMusicAwareness() {
        if (this.musicScanCooldown-- <= 0) {
            this.musicScanCooldown = 18 + this.getRandom().nextInt(14);
            BlockPos sourcePos = this.findNearbyJukebox();
            if (sourcePos != null) {
                this.triggerMusic(sourcePos, 85 + this.getRandom().nextInt(35));
                for (BudgerigarEntity budgerigar : this.level().getEntitiesOfClass(
                        BudgerigarEntity.class, this.getBoundingBox().inflate(10.0))) {
                    if (budgerigar != this && budgerigar.getRandom().nextFloat() < 0.8F) {
                        budgerigar.triggerMusic(sourcePos, 65 + budgerigar.getRandom().nextInt(35));
                    }
                }
            }
        }
    }

    private BlockPos findNearbyJukebox() {
        BlockPos origin = this.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(
                origin.offset(-8, -3, -8), origin.offset(8, 3, 8))) {
            BlockState state = this.level().getBlockState(pos);
            if (state.is(Blocks.JUKEBOX) && state.hasProperty(JukeboxBlock.HAS_RECORD)
                    && state.getValue(JukeboxBlock.HAS_RECORD)) {
                return pos.immutable();
            }
        }
        return null;
    }

    private void tickEating() {
        if (this.eatingTicks > 0) {
            this.getNavigation().stop();
            this.setBehaviorState(BudgerigarBehaviorState.EATING);
            if (--this.eatingTicks <= 0) {
                this.clearEating();
            }
        }
    }

    private void clearEating() {
        this.eatingTicks = 0;
        if (this.getBehaviorState() == BudgerigarBehaviorState.EATING) {
            this.setBehaviorState(this.birdBrain.wantsForage()
                    ? BudgerigarBehaviorState.FORAGING
                    : BudgerigarBehaviorState.IDLE);
        }
    }

    private void tickPostTameAction() {
        if (this.postTameActionTicks > 0) {
            --this.postTameActionTicks;
            if (!this.isPassenger()) {
                if (this.isEating()) {
                    this.clearEating();
                }
                if (this.getBehaviorState() == BudgerigarBehaviorState.SLEEPING
                        || this.getBehaviorState() == BudgerigarBehaviorState.ROOSTING) {
                    this.behaviorStateLockTicks = 0;
                    this.setBehaviorState(BudgerigarBehaviorState.CURIOUS);
                }
                if (this.getOwner() != null && this.tickCount % 8 == 0) {
                    this.getLookControl().setLookAt(this.getOwner(), 35.0F, 35.0F);
                }
                if (this.postTameActionSwapTicks <= 0 || this.getBehaviorState() == BudgerigarBehaviorState.IDLE) {
                    BudgerigarBehaviorState state = this.getRandom().nextBoolean()
                            ? BudgerigarBehaviorState.CURIOUS
                            : BudgerigarBehaviorState.PREENING;
                    this.setBehaviorStateFor(state, 32 + this.getRandom().nextInt(32));
                    this.postTameActionSwapTicks = 30 + this.getRandom().nextInt(28);
                }
                if (this.postTameActionTicks <= 0 && (this.getBehaviorState() == BudgerigarBehaviorState.CURIOUS
                        || this.getBehaviorState() == BudgerigarBehaviorState.PREENING)) {
                    this.behaviorStateLockTicks = 0;
                    this.setBehaviorState(BudgerigarBehaviorState.IDLE);
                }
            }
        }
    }

    private void tickWaterEscape() {
        if (this.isInWater()) {
            this.getNavigation().stop();
            this.landingFlight = false;
            this.escapeFlightActive = false;
            this.flightTarget = this.findAirCruiseTarget(false);
            this.flightTicks = Math.max(this.flightTicks, 90 + this.getRandom().nextInt(50));
            this.hoverRetargetTicks = Math.clamp(this.hoverRetargetTicks, 1, 12);
            this.setNoGravity(true);
            this.setBehaviorStateFor(BudgerigarBehaviorState.FLYING, 70);

            Vec3 direction = this.flightTarget.subtract(this.position())
                    .multiply(1.0, 0.0, 1.0);
            if (direction.length() <= 1.0E-4) {
                direction = this.randomHorizontalDirection();
            }
            Vec3 movement = direction.normalize().scale(0.22).add(0, 0.28, 0);
            this.setDeltaMovement(movement);
            this.faceFlightDirection(movement);
            this.xxa = 0.0F;
            this.hasImpulse = true;
        }
    }

    private void tickPendingFright() {
        if (this.pendingFrightTicks > 0) {
            --this.pendingFrightTicks;
            this.getNavigation().stop();
            if (this.pendingFrightTicks > 0) {
                if (this.pendingFrightSource != null) {
                    this.getLookControl().setLookAt(
                            this.pendingFrightSource.x,
                            this.pendingFrightSource.y + 0.6,
                            this.pendingFrightSource.z,
                            35.0F, 35.0F);
                }
            } else {
                Vec3 sourcePos = this.pendingFrightSource == null ? this.position() : this.pendingFrightSource;
                int duration = Math.max(60, this.pendingFrightDuration);
                this.pendingFrightSource = null;
                this.pendingFrightDuration = 0;
                this.frightenFrom(sourcePos, duration);
            }
        }
    }

    // ============ 飞行逻辑 ============
    public void startFlybyFlight(Vec3 target) {
        this.escapeFlightActive = false;
        this.landingFlight = false;
        this.flightTarget = target == null ? this.findAirCruiseTarget(false) : this.clampFlightTarget(target);
        this.flightTicks = 150 + this.getRandom().nextInt(91);
        this.timeFlying = 0;
        this.hoverRetargetTicks = 52 + this.getRandom().nextInt(46);
        this.flightCooldown = Math.max(this.flightCooldown, 120);
        this.getNavigation().stop();
        this.setNoGravity(true);
        this.setSilent(false);
        this.setBehaviorStateFor(BudgerigarBehaviorState.FLYING, 120);

        Vec3 direction = this.flightTarget.subtract(this.position()).multiply(1.0, 0.0, 1.0);
        if (direction.length() <= 1.0E-4) {
            direction = this.randomHorizontalDirection();
        }
        Vec3 movement = direction.normalize().scale(0.24).add(0, 0.07, 0);
        this.setDeltaMovement(movement);
        this.faceFlightDirection(movement);
        this.xxa = 0.0F;
        this.hasImpulse = true;
    }

    @Override
    public boolean startBirdBathMountFlight(Vec3 standPosition) {
        if (standPosition != null && !this.isFlightInProgress()) {
            Vec3 horizontal = standPosition.subtract(this.position()).multiply(1.0, 0.0, 1.0);
            if (horizontal.length() <= 1.0E-4) {
                horizontal = Vec3.ZERO;
            } else {
                horizontal = horizontal.normalize().scale(0.27);
            }
            Vec3 movement = new Vec3(horizontal.x, 0.64, horizontal.z);
            this.getNavigation().stop();
            this.setNoGravity(false);
            this.setSilent(false);
            this.setBehaviorStateFor(BudgerigarBehaviorState.FLYING, 32);
            this.setDeltaMovement(movement);
            this.faceFlightDirection(movement);
            this.xxa = 0.0F;
            this.hasImpulse = true;
            return true;
        }
        return false;
    }

    @Override
    public void startBirdBathFeedingAnimation(BirdBathContentType contentType, int ticks) {
        this.getNavigation().stop();
        if (contentType.isFood()) {
            this.eatingTicks = Math.max(this.eatingTicks, Math.max(30, ticks));
            this.setBehaviorStateFor(BudgerigarBehaviorState.EATING, this.eatingTicks);
        } else {
            this.setBehaviorStateFor(BudgerigarBehaviorState.CURIOUS, Math.max(24, ticks / 2));
        }
    }

    private void startEscapeFlight(Vec3 sourcePos) {
        Vec3 away = this.position().subtract(sourcePos);
        if (away.lengthSqr() < 0.01) {
            away = new Vec3(this.getRandom().nextDouble() - 0.5, 0, this.getRandom().nextDouble() - 0.5);
        }
        Vec3 direction = new Vec3(away.x, 0, away.z).normalize();
        Vec3 target = this.position().add(direction.scale(4.5 + this.getRandom().nextDouble() * 5.0))
                .add(0, 1.5 + this.getRandom().nextDouble() * 1.8, 0);
        this.startShortFlight(target, true);
    }

    private void tickFlight() {
        if (this.flightTicks <= 0 && !this.landingFlight) {
            this.timeFlying = 0;
            this.setNoGravity(false);
        } else {
            this.getNavigation().stop();
            this.setNoGravity(true);
            this.xxa = 0.0F;
            ++this.timeFlying;
            this.setBehaviorState(this.escapeFlightActive
                    ? BudgerigarBehaviorState.FLEEING
                    : BudgerigarBehaviorState.FLYING);

            if (this.flightTicks > 0) {
                --this.flightTicks;
            }
            if (this.flightTicks <= 0 && !this.landingFlight) {
                this.beginLandingFlight();
            }

            if (this.flightTarget == null) {
                if (this.landingFlight) {
                    this.flightTarget = this.findLandingTarget();
                    if (this.flightTarget == null) {
                        this.extendCruiseAfterUnsafeLanding();
                        return;
                    }
                } else {
                    this.retargetAirCruise(this.escapeFlightActive);
                }
            }

            Vec3 toTarget = this.flightTarget.subtract(this.position());
            double horizontalDistance = Math.sqrt(toTarget.x * toTarget.x + toTarget.z * toTarget.z);

            if (this.landingFlight) {
                if (this.onGround()) {
                    this.finishFlight();
                    return;
                }
                if (this.flightTicks <= 0 && toTarget.length() < 0.35) {
                    this.extendCruiseAfterUnsafeLanding();
                    return;
                }
            } else if (toTarget.length() < 1.85 || --this.hoverRetargetTicks <= 0) {
                this.retargetAirCruise(this.escapeFlightActive);
                toTarget = this.flightTarget.subtract(this.position());
                horizontalDistance = Math.sqrt(toTarget.x * toTarget.x + toTarget.z * toTarget.z);
            }

            Vec3 direction = toTarget.length() > 1.0E-4 ? toTarget.normalize() : this.randomHorizontalDirection();
            Vec3 horizontalDirection = BirdFlightTargeting.normalizeHorizontal(
                    new Vec3(direction.x, 0, direction.z), this.getDeltaMovement());

            if (!this.landingFlight) {
                Vec3 flockHeading = BirdFlightBoids.sameTypeHeading(
                        this, 13.0, 2.4, 0.035, 0.45, 0.1,
                        this.escapeFlightActive ? 0.18 : 0.08);
                if (flockHeading.length() > 1.0E-4) {
                    horizontalDirection = BirdFlightTargeting.normalizeHorizontal(
                            horizontalDirection.add(flockHeading), horizontalDirection);
                }
            }

            double speed = this.escapeFlightActive ? 0.34 : (this.landingFlight ? 0.2 : 0.26);
            if (this.landingFlight) {
                speed = BirdFlightController.decelerateNearLanding(speed, horizontalDistance, 3.4, 0.42);
            }

            double hoverBob = this.landingFlight ? -0.035 : Math.sin((this.tickCount + this.getId()) * 0.28) * 0.025;
            double vertical = this.landingFlight
                    ? Mth.clamp(toTarget.y * 0.11 - 0.035, -0.13, 0.055)
                    : Mth.clamp(toTarget.y * 0.12 + hoverBob, -0.075, 0.16);

            Vec3 desired = new Vec3(horizontalDirection.x * speed, vertical, horizontalDirection.z * speed);
            Vec3 movement = this.getDeltaMovement().scale(0.32).add(desired.scale(0.68));

            if (!this.landingFlight && BirdFlightController.isStalledInAir(this, this.timeFlying, 0.006)) {
                this.retargetAirCruise(this.escapeFlightActive);
                movement = horizontalDirection.scale(Math.max(speed, 0.18)).add(0, 0.08, 0);
            }

            this.setDeltaMovement(movement);
            this.faceFlightDirection(movement);
            this.hasImpulse = true;
        }
    }

    private void finishFlight() {
        boolean wasEscaping = this.escapeFlightActive;
        this.flightTicks = 0;
        this.timeFlying = 0;
        this.flightTarget = null;
        this.hoverRetargetTicks = 0;
        this.escapeFlightActive = false;
        this.landingFlight = false;
        this.setNoGravity(false);
        this.setDeltaMovement(this.getDeltaMovement().multiply(0.35, 0, 0.35));
        this.flightCooldown = wasEscaping
                ? 120 + this.getRandom().nextInt(120)
                : (this.isTame() ? 140 + this.getRandom().nextInt(160) : 160 + this.getRandom().nextInt(180));
        if (this.getBehaviorState().isAirborne()) {
            this.setBehaviorStateFor(BudgerigarBehaviorState.ALERT, 28);
        }
    }

    private void beginLandingFlight() {
        Vec3 landingTarget = this.findLandingTarget();
        if (landingTarget == null) {
            this.extendCruiseAfterUnsafeLanding();
        } else {
            this.landingFlight = true;
            this.escapeFlightActive = false;
            this.flightTicks = 55 + this.getRandom().nextInt(45);
            this.flightTarget = landingTarget;
            this.hoverRetargetTicks = 0;
            this.setBehaviorStateFor(BudgerigarBehaviorState.FLYING, 50);
        }
    }

    private void extendCruiseAfterUnsafeLanding() {
        this.landingFlight = false;
        this.escapeFlightActive = false;
        this.flightTicks = 70 + this.getRandom().nextInt(50);
        this.retargetAirCruise(false);
        this.setNoGravity(true);
        this.setBehaviorStateFor(BudgerigarBehaviorState.FLYING, 60);
    }

    private void retargetAirCruise(boolean fleeing) {
        this.flightTarget = this.findAirCruiseTarget(fleeing);
        this.hoverRetargetTicks = this.nextHoverRetargetDelay();
    }

    private int nextHoverRetargetDelay() {
        return 36 + this.getRandom().nextInt(46);
    }

    private Vec3 findAirCruiseTarget(boolean fleeing) {
        Vec3 direction;
        if (fleeing && this.frightSource != null) {
            Vec3 away = this.position().subtract(this.frightSource);
            direction = away.lengthSqr() > 0.01
                    ? new Vec3(away.x, 0, away.z).normalize()
                    : this.randomHorizontalDirection();
        } else {
            direction = this.getRandom().nextInt(3) == 0
                    ? this.getLookAngle()
                    : this.randomHorizontalDirection();
        }
        Vec3 target = BirdFlightTargeting.findAirTarget(this, FLIGHT_PROFILE, direction, fleeing);
        return target != null ? this.clampFlightTarget(target)
                : this.clampFlightTarget(this.position().add(0, this.onGround() ? 2.0 : 0.8, 0));
    }

    private Vec3 findLandingTarget() {
        Vec3 sharedLanding = BirdFlightTargeting.findNearestDryLandingTarget(this, 8, 16);
        if (sharedLanding != null) {
            return this.clampFlightTarget(sharedLanding);
        }

        BlockPos origin = this.blockPosition();
        BlockPos landing = this.findDryLandingSurface(origin, 16);
        if (landing != null) {
            return this.clampFlightTarget(Vec3.atBottomCenterOf(landing));
        }

        for (int attempt = 0; attempt < 24; ++attempt) {
            int x = origin.getX() + this.getRandom().nextInt(13) - 6;
            int z = origin.getZ() + this.getRandom().nextInt(13) - 6;
            BlockPos candidate = this.findDryLandingSurface(new BlockPos(x, origin.getY(), z), 16);
            if (candidate != null) {
                return this.clampFlightTarget(Vec3.atBottomCenterOf(candidate));
            }
        }
        return null;
    }

    @SuppressWarnings("SameParameterValue")
    private BlockPos findDryLandingSurface(BlockPos center, int verticalRange) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int yOffset = verticalRange; yOffset >= -verticalRange; --yOffset) {
            mutable.set(center.getX(), center.getY() + yOffset, center.getZ());
            if (this.isSafeDryLanding(mutable)) {
                return mutable.immutable();
            }
        }
        return null;
    }

    private boolean isSafeDryLanding(BlockPos pos) {
        return BirdFlightTargeting.isSafeDryLanding(this, pos);
    }

    private Vec3 clampFlightTarget(Vec3 target) {
        double y = Mth.clamp(target.y, this.level().getMinBuildHeight() + 1.5, this.level().getMaxBuildHeight() - 2.0);
        return new Vec3(target.x, y, target.z);
    }

    private Vec3 randomHorizontalDirection() {
        return BirdFlightTargeting.randomHorizontalDirection(this.getRandom());
    }

    private void tickAmbientAirCruise() {
        if (this.canStartAmbientAirCruise()) {
            int chance = this.isTame() ? 170 : 150;
            if (this.getRandom().nextInt(chance) == 0) {
                this.startShortFlight(this.findAirCruiseTarget(false), false);
            }
        }
    }

    private boolean canStartAmbientAirCruise() {
        BudgerigarBehaviorState state = this.getBehaviorState();
        return this.flightCooldown <= 0 && this.onGround() && this.isActiveTime()
                && this.getNavigation().isDone() && !this.isEating() && !this.isDancing()
                && !this.isSleepingOrRoosting() && state != BudgerigarBehaviorState.FORAGING
                && state != BudgerigarBehaviorState.PERCHING && state != BudgerigarBehaviorState.FOLLOWING
                && state != BudgerigarBehaviorState.SENTINEL && !state.isEscape();
    }

    private void updateTrustedOwner(Player player) {
        if (!this.isTame() && this.trustTicks >= 900) {
            this.tame(player);
        } else if (this.isTame() && this.getOwner() == null) {
            this.setOwnerUUID(player.getUUID());
        }
    }

    private void awardChirpyPartnerAdvancement(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.getAdvancements().award(
                    Objects.requireNonNull(
                            serverPlayer.server.getAdvancements()
                                    .get(CHIRPY_PARTNER_ADVANCEMENT)
                    ),
                    "tame_budgerigar"
            );
        }
    }

    @Override
    public boolean isTame() {
        return false;
    }

    private void startTameCelebration(Player player) {
        this.clearEating();
        this.getNavigation().stop();
        this.postTameActionTicks = 150 + this.getRandom().nextInt(50);
        this.postTameActionSwapTicks = 42;
        this.curiousTicks = Math.max(this.curiousTicks, 220);
        this.idleAnimationTicks = 0;
        this.foodCooldown = Math.max(this.foodCooldown, 45);
        this.behaviorStateLockTicks = 0;
        this.setBehaviorStateFor(BudgerigarBehaviorState.CURIOUS, 55);
        this.getLookControl().setLookAt(player, 35.0F, 35.0F);
    }

    @Override
    protected void applyTamingSideEffects() {
        super.applyTamingSideEffects();

        for (int i = 0; i < 9; ++i) {
            double xOffset = this.getRandom().nextGaussian() * 0.03;
            double yOffset = this.getRandom().nextGaussian() * 0.04;
            double zOffset = this.getRandom().nextGaussian() * 0.03;

            this.level().addParticle(
                    ParticleTypes.HEART,
                    this.getX(0.7),
                    this.getY() + 0.22,
                    this.getZ(0.7),
                    this.getRandom().nextDouble(),
                    yOffset + 0.035,
                    zOffset
            );
        }
    }

    // ============ 行为回退 ============

    private void tickBehaviorFallback() {
        if (this.behaviorStateLockTicks <= 0 && this.postTameActionTicks <= 0
                && !this.isEating() && !this.isPassenger()) {
            if (this.nearbyMusicTicks > 0) {
                this.setBehaviorState(BudgerigarBehaviorState.DANCING);
            } else if (this.isRoostTime() && this.onGround() && this.getNavigation().isDone()) {
                this.setBehaviorState(BudgerigarBehaviorState.SLEEPING);
            } else {
                BudgerigarBehaviorState state = this.getBehaviorState();
                if (state != BudgerigarBehaviorState.FLEEING && state != BudgerigarBehaviorState.FLYING) {
                    if (this.isTame() && this.getOwner() != null && !this.getNavigation().isDone()
                            && this.distanceToSqr(this.getOwner()) > 9.0) {
                        this.setBehaviorState(BudgerigarBehaviorState.FOLLOWING);
                    } else if (!(this.getDeltaMovement().lengthSqr() > WALKING_SPEED_THRESHOLD)
                            && this.getNavigation().isDone()) {
                        if (state == BudgerigarBehaviorState.WALKING || state == BudgerigarBehaviorState.FORAGING
                                || state == BudgerigarBehaviorState.FOLLOWING || state == BudgerigarBehaviorState.ALERT) {
                            this.setBehaviorState(BudgerigarBehaviorState.IDLE);
                        }
                    } else {
                        this.setBehaviorState(BudgerigarBehaviorState.WALKING);
                    }
                } else {
                    this.setBehaviorState(BudgerigarBehaviorState.ALERT);
                }
            }
        }
    }

    // ============ 朝向控制 ============

    private void faceFlightDirection(Vec3 movement) {
        BirdFlightController.faceMovement(this, movement, FLIGHT_PROFILE.maxPitchDegrees());
    }


    private void tickGroundMovementFacing() {
        if (this.shouldFaceGroundMovement()) {
            @SuppressWarnings("unused")
            boolean unused = BirdFlightController.faceGroundMovement(this, this.getDeltaMovement(), 1.0E-4);
        }
    }

    private boolean shouldFaceGroundMovement() {
        if (this.onGround() && !this.isPassenger() && !this.isInWater() && !this.isVehicle()) {
            BudgerigarBehaviorState state = this.getBehaviorState();
            if (!state.isAirborne() && state != BudgerigarBehaviorState.EATING
                    && state != BudgerigarBehaviorState.PREENING && state != BudgerigarBehaviorState.DANCING
                    && state != BudgerigarBehaviorState.SLEEPING && state != BudgerigarBehaviorState.ROOSTING) {
                return this.getDeltaMovement().lengthSqr() > WALKING_SPEED_THRESHOLD
                        || !this.getNavigation().isDone();
            }
        }
        return false;
    }

    // ============ 动画 ============

    private boolean shouldPlayFlyAnimation() {
        return BirdFlightController.shouldPlayFlyAnimation(this,
                this.getBehaviorState().isAirborne(), this.onGround(), this.isInWater(),
                this.getDeltaMovement(), 0);
    }

    private RawAnimation pickIdleAnimation() {
        if (this.idleAnimationTicks <= 0) {
            int roll = this.getRandom().nextInt(this.trustTicks <= 800 && this.curiousTicks <= 0 ? 9 : 5);
            if (roll == 0) {
                this.currentIdleAnimation = PREEN_ANIMATION;
                this.idleAnimationTicks = 45 + this.getRandom().nextInt(45);
            } else if (roll > 2 || (this.trustTicks <= 400 && this.curiousTicks <= 0)) {
                this.currentIdleAnimation = IDLE_ANIMATION;
                this.idleAnimationTicks = 55 + this.getRandom().nextInt(70);
            } else {
                this.currentIdleAnimation = CURIOUS_ANIMATION;
                this.idleAnimationTicks = 35 + this.getRandom().nextInt(35);
            }
        }
        return this.currentIdleAnimation;
    }

    private <T extends BudgerigarEntity> PlayState movementController(AnimationState<T> animationState) {
        RawAnimation guidePreviewRawAnimation = this.guidePreviewAnimation.animation();
        if (guidePreviewRawAnimation != null) {
            return animationState.setAndContinue(guidePreviewRawAnimation);
        }

        BudgerigarBehaviorState state = this.getBehaviorState();

        if (state != BudgerigarBehaviorState.DANCING && this.nearbyMusicTicks <= 0) {
            if (state != BudgerigarBehaviorState.EATING && this.eatingTicks <= 0) {
                if (state == BudgerigarBehaviorState.SLEEPING) {
                    return animationState.setAndContinue(this.behaviorStateLockTicks > 0
                            ? SLEEP_ANIMATION : SLEEP_LOOP_ANIMATION);
                }
                if (this.shouldPlayFlyAnimation()) {
                    return animationState.setAndContinue(FLY_ANIMATION);
                }
                if (!(this.getDeltaMovement().lengthSqr() > WALKING_SPEED_THRESHOLD)
                        && this.getNavigation().isDone() && state != BudgerigarBehaviorState.WALKING) {
                    if (state == BudgerigarBehaviorState.PREENING) {
                        return animationState.setAndContinue(PREEN_ANIMATION);
                    }
                    if (state != BudgerigarBehaviorState.CURIOUS && state != BudgerigarBehaviorState.ALERT
                            && this.curiousTicks <= 0) {
                        return animationState.setAndContinue(this.pickIdleAnimation());
                    }
                    return animationState.setAndContinue(CURIOUS_ANIMATION);
                }
                return animationState.setAndContinue(WALK_ANIMATION);
            }
            return animationState.setAndContinue(EAT_ANIMATION);
        }
        return animationState.setAndContinue(DANCE_ANIMATION);
    }

    // ============ GeckoLib 注册 ============

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 4, this::movementController));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animationCache;
    }

    // ============ 声音 ============

    @Override
    protected SoundEvent getAmbientSound() {
        return NeoGuanNiaoSoundEvents.BUDGERIGAR_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return NeoGuanNiaoSoundEvents.BUDGERIGAR_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return NeoGuanNiaoSoundEvents.BUDGERIGAR_DEATH.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 180;
    }

    @Override
    public boolean isFood(@NotNull ItemStack itemStack) {
        return false;
    }

    @Override
    public float getVoicePitch() {
        return 0.45F;
    }

}