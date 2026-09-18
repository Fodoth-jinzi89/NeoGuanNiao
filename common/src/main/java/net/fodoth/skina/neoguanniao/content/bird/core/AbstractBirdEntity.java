package net.fodoth.skina.neoguanniao.content.bird.core;

import net.fodoth.skina.neoguanniao.content.bath.BirdBathContentType;
import net.fodoth.skina.neoguanniao.content.bath.BirdBathFeedingAnimatable;
import net.fodoth.skina.neoguanniao.content.bath.BirdBathMountable;
import net.fodoth.skina.neoguanniao.content.bird.core.controller.*;
import net.fodoth.skina.neoguanniao.content.bird.core.controller.BirdControllers;
import net.fodoth.skina.neoguanniao.content.bird.core.data.BirdData;
import net.fodoth.skina.neoguanniao.content.bird.core.controller.BirdTickController;
import net.fodoth.skina.neoguanniao.content.bird.core.data.datum.BirdModelSkinDatum;
import net.fodoth.skina.neoguanniao.content.bird.core.goal.goals.*;
import net.fodoth.skina.neoguanniao.content.bird.core.model.BirdModel;
import net.fodoth.skina.neoguanniao.content.bird.core.model.BirdModelRarity;
import net.fodoth.skina.neoguanniao.content.bird.core.skin.BirdSkin;
import net.fodoth.skina.neoguanniao.content.bird.core.skin.BirdSkinRarity;
import net.fodoth.skina.neoguanniao.content.bird.core.model.BirdModelScale;
import net.fodoth.skina.neoguanniao.content.bird.core.model.ScalableBirdModel;
import net.fodoth.skina.neoguanniao.content.bird.core.flight.*;
import net.fodoth.skina.neoguanniao.content.bird.core.navigation.BirdPathNavigation;
import net.fodoth.skina.neoguanniao.content.bird.core.data.datum.BirdFlightProfile;
import net.fodoth.skina.neoguanniao.content.bird.core.data.datum.BirdModelScaleProfile;
import net.fodoth.skina.neoguanniao.content.bird.core.controller.BirdTameController;
import net.fodoth.skina.neoguanniao.content.egg.BirdEggData;
import net.fodoth.skina.neoguanniao.content.egg.BirdEggItem;
import net.fodoth.skina.neoguanniao.content.nest.BirdNestBlockEntity;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoBlockTags;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoCriteriaTriggers;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
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
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractBirdEntity<T extends AbstractBirdEntity<T>> extends TamableAnimal implements GeoEntity, FlyingAnimal, ScalableBirdModel, BirdFlightAware, BirdBathMountable, BirdBathFeedingAnimatable {

    private static final int MAX_LOCAL_BIRDS = 12;

    /** 展示用预览（笼中鸟、被 Carry On 等移出世界但仍渲染的鸟）待机动画播完一轮后，再按 1/N 的概率好奇一次。 */
    private static final int CURIOUS_CHANCE = 4;

    /** {@link #tickAnimationPreview(long)} 的去重时间戳；预览不参与存档。 */
    private long previewTickedAt = Long.MIN_VALUE;

    /**
     * 是否由展示预览驱动过（鸟笼 / 观鸟手册实体页 / GT 罐子 / Carry On 抱持等，见
     * {@link #tickAnimationPreview(long)}）。
     *
     * <p>这类鸟的行为状态是照搬活体实体的（可能停在 WALKING、或悬空让 {@code shouldPlayFlyAnimation()}
     * 为真），会误播走路 / 飞行 / 奔跑动画，所以一律只播待机动画。世界里的鸟从不进入预览驱动，
     * 因此不受影响（务必不要用 gameTime 之类的时序条件来判定「有没有人 tick 我」，渲染帧多于 tick，
     * 那会让世界里的鸟在 tick 刚自增的几帧被误判，动画每帧反复切换而完全不播）。</p>
     */
    private boolean previewDriven;

    /** 游离的渲染副本（{@link #isRenderCopy()}）共享的动画缓存，按实体类型缓存。 */
    private static final Map<EntityType<?>, AnimatableInstanceCache> RENDER_COPY_CACHES = new ConcurrentHashMap<>();

    // ================== 数据序列化器 ===================
    public static final EntityDataAccessor<Integer> BEHAVIOR_STATE =
            SynchedEntityData.defineId(AbstractBirdEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> SKIN_VARIANT =
            SynchedEntityData.defineId(AbstractBirdEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> MODEL_VARIANT =
            SynchedEntityData.defineId(AbstractBirdEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Float> MODEL_SCALE =
            SynchedEntityData.defineId(AbstractBirdEntity.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Boolean> GENDER =
            SynchedEntityData.defineId(AbstractBirdEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Integer> EGG_COUNT =
            SynchedEntityData.defineId(AbstractBirdEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> FEATHER_COUNT =
            SynchedEntityData.defineId(AbstractBirdEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> FEATHER_INTERVAL =
            SynchedEntityData.defineId(AbstractBirdEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Boolean> GROWTH_STOPPED =
            SynchedEntityData.defineId(AbstractBirdEntity.class, EntityDataSerializers.BOOLEAN);
    private boolean growthStopped = false;



    // ==================== 常量 ========================
    protected final BirdData BIRD_DATA;
    protected final BirdControllers<T> BIRD_CONTROLLERS;

    // ==================== 变量 =======================
    protected AbstractBirdEntity(EntityType<T> entityType,
                                 Level level, BirdData birdData, BirdControllers<T> birdControllers) {
        super(entityType, level);
        this.BIRD_DATA = birdData;
        this.BIRD_CONTROLLERS = birdControllers;
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
    }

    public BirdControllers<T> getBirdControllers() {
        return BIRD_CONTROLLERS;
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
        goals.add(new BirdEscapeGoal(this)); //1
        goals.add(new BirdBreedGoal(this)); //2
        goals.add(new BirdEatFoodGoal(this));  //3
        goals.add(new BirdBathUseGoal(this)); //4
        goals.add(new BirdSentinelGoal(this)); //5
        goals.add(new BirdWakeUpGoal(this)); //6
        goals.add(new BirdRoostGoal(this)); //7
        goals.add(new BirdFollowOwnerGoal(this)); //8
        goals.add(new BirdFlockGoal(this)); //9
        goals.add(new BirdCuriousFollowGoal(this));  //10
        goals.add(new BirdWalkAroundGoal(this)); //11
        goals.add(new BirdRandomLookAroundGoal(this)); //12
        goals.add(new BirdSkinValidateGoal(this)); //13
        goals.add(new BirdModelValidateGoal(this)); //14

        return goals;
    }

    /** Whether this species participates in the shared flight controller. */
    public boolean canFly() {
        return true;
    }

    /**
     * 被放出后的落地缓冲时间（Tick）：先落地站定，过了这段时间才恢复自主行动。
     */
    public static final int RELEASE_SETTLE_TICKS = 100;

    private int releaseSettleTicks;

    /**
     * 刚从鸟笼里放出来：落到地面站定一段时间，期间不走动也不主动起飞。
     */
    public void settleAfterRelease() {
        this.releaseSettleTicks = RELEASE_SETTLE_TICKS;
        setNoGravity(false);
        setDeltaMovement(Vec3.ZERO);
        hasImpulse = true;
        fallDistance = 0.0F;
        getNavigation().stop();
        // ALERT 属于逃逸状态，会挡住所有地面移动目标，让鸟先站在原地。
        getBehaviorStateController().setBehaviorStateFor(BirdBehaviorState.ALERT, RELEASE_SETTLE_TICKS);
    }

    /** 是否还在“刚被放出”的缓冲时间内。 */
    public boolean isSettlingAfterRelease() {
        return releaseSettleTicks > 0;
    }

    /** Species-specific multiplier for the ground walk-around goal. */
    public double getWalkAroundSpeedMultiplier() {
        return 1.0D;
    }

    /**
     * 生成规则由平台事件注册。
     */
    public static boolean canSpawn(EntityType<? extends AbstractBirdEntity<?>> entityType, ServerLevelAccessor level,
                                   MobSpawnType spawnType, BlockPos pos, RandomSource random, BirdData birdData) {
        BlockState below = level.getBlockState(pos.below());
        boolean validGround = below.is(BlockTags.DIRT)
                || below.is(BlockTags.SAND)
                || below.is(Blocks.GRASS_BLOCK)
                || below.is(Blocks.DIRT_PATH)
                || below.is(Blocks.FARMLAND)
                || below.is(NeoGuanNiaoBlockTags.BIRD_PERCHES);

        if (!validGround) {
            return false;
        }

        return hasLocalSpawnCapacity(entityType, level, spawnType, pos, birdData);
    }

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

        var entities = level.getEntitiesOfClass(AbstractBirdEntity.class,
                new AABB(pos.getX() - 8, pos.getY() - 4, pos.getZ() - 8,
                        pos.getX() + 8, pos.getY() + 4, pos.getZ() + 8));
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

    @Override
    public void spawnChildFromBreeding(
            @NotNull ServerLevel level,
            @NotNull Animal mate
    ) {

        if (mate instanceof AbstractBirdEntity<?> bird) {

            int remainingEggs = tryLayEggInNest(bird, this);

            if (remainingEggs > 0) {
                triggerBreedEggAdvancement(bird);
                // 巢放不下的蛋，在雌鸟处生成
                for (int i = 0; i < remainingEggs; i++) {
                    spawnEgg(bird);
                }
            }

        }


        // 繁育冷却
        this.setAge(getBirdData().misc().breedCooldown());
        mate.setAge(getBirdData().misc().breedCooldown());


        // 清除爱心状态
        this.resetLove();
        mate.resetLove();


        // 爱心效果
        level.broadcastEntityEvent(this, (byte) 18);


        // 经验
        if (level.getGameRules()
                .getBoolean(GameRules.RULE_DOMOBLOOT)) {

            level.addFreshEntity(
                    new ExperienceOrb(
                            level,
                            this.getX(),
                            this.getY(),
                            this.getZ(),
                            getEggCount() * (
                                    this.getRandom().nextInt(getBirdData().misc().layEggExpVariance()) + getBirdData().misc().layEggExp())
                    )
            );
        }
    }

    private int tryLayEggInNest(
            AbstractBirdEntity<?> male,
            AbstractBirdEntity<?> female
    ) {

        BlockPos center = female.blockPosition();

        int range = getBirdData()
                .misc()
                .layEggRange();


        List<BirdNestBlockEntity> nests = new ArrayList<>();


        for (BlockPos pos :
                BlockPos.betweenClosed(
                        center.offset(-range, -range, -range),
                        center.offset(range, range, range)
                )) {


            if (female.level()
                    .getBlockEntity(pos)
                    instanceof BirdNestBlockEntity nest) {

                if (nest.hasEmptySlot()) {
                    nests.add(nest);
                }
            }
        }


        // 最近优先
        nests.sort(
                Comparator.comparingDouble(
                        nest -> center.distSqr(
                                nest.getBlockPos()
                        )
                )
        );


        int remainingEggs = getEggCount();


        List<BirdNestBlockEntity> usedNests =
                new ArrayList<>();


        boolean eggLaid = false;

        for (BirdNestBlockEntity nest : nests) {

            if (remainingEggs <= 0)
                break;


            boolean added = false;

            while (
                    remainingEggs > 0
                            &&
                            nest.hasEmptySlot()
            ) {

                nest.addEgg(createEgg(male));

                if (!eggLaid) {

                    triggerBreedEggAdvancement(
                            female
                    );

                    eggLaid = true;
                }

                remainingEggs--;
                added = true;
            }


            if (added) {
                usedNests.add(nest);
            }
        }


        // 粒子效果
        if (female.level() instanceof ServerLevel serverLevel) {

            for (BirdNestBlockEntity nest : usedNests) {

                serverLevel.sendParticles(
                        ParticleTypes.HAPPY_VILLAGER,
                        nest.getBlockPos().getX() + 0.5,
                        nest.getBlockPos().getY() + 0.375,
                        nest.getBlockPos().getZ() + 0.5,
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

        BirdEggData eggData = createEggData(male);

        ItemStack eggStack = new ItemStack(
                NeoGuanNiaoItems.BIRD_EGG.get()
        );

        BirdEggItem.setEggData(
                eggStack,
                eggData
        );

        return eggStack;
    }

    protected BirdEggData createEggData(AbstractBirdEntity<?> mate) {

        boolean gender = getBreedController().getRandomGender();

        return BirdEggData.create(
                BuiltInRegistries.ENTITY_TYPE.getKey(this.getType()),
                gender,
                getModelController().inheritModelVariant(
                        mate,
                        this,
                        gender
                ),
                getSkinController().inheritSkinVariant(
                        mate,
                        this,
                        gender
                ),
                getBreedController().inheritEggCount(mate, this),
                getFeatherController().inheritFeatherCount(mate, this),
                getFeatherController().inheritFeatherInterval(mate, this),
                BirdModelScale.inheritIndividualScale(
                        this.getRandom(),
                        mate.getIndividualModelScale(),
                        this.getIndividualModelScale(),
                        modelScaleProfile()
                ),
                getBirdData().misc().eggDefaultHatchTime(),
                true
        );
    }

    private void triggerBreedEggAdvancement(
            AbstractBirdEntity<?> female
    ) {

        if (!(female.level() instanceof ServerLevel serverLevel)) {
            return;
        }


        serverLevel.getEntitiesOfClass(
                ServerPlayer.class,
                female.getBoundingBox().inflate(16)
        ).forEach(player -> {

            NeoGuanNiaoCriteriaTriggers.BIRD_EGG_BREED
                    .get()
                    .trigger(player);

        });
    }


    public ResourceLocation getTextureResource() {
        return getSkinController().textureForVariant(getSkinController().getSkinVariant());
    }

    public ResourceLocation getModelResource() {
        return getModelController().modelForVariant(getModelController().getModelVariant());
    }

    public ResourceLocation getModelId() {
        return getModelController().modelForVariantId(getModelController().getModelVariant());
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

        getBreedController().randomizeGender();
        getBreedController().randomizeEggCount();
        getFeatherController().randomizeFeatherCount();
        getFeatherController().randomizeFeatherInterval();
        getModelController().setModelVariant(getModelController().getRandomizeModelVariant(BirdModelRarity.COMMON, true, false, isBaby(), isMale(), !isMale(), false));
        getSkinController().setSkinVariant(getSkinController().getRandomizeSkinVariant(BirdSkinRarity.COMMON, true, false, isBaby(), isMale(), !isMale(), false));
        getModelController().randomizeModelScale();


        return data;
    }

    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level level) {
        BirdPathNavigation navigation = new BirdPathNavigation(this, level);
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
        return null;
    }


    protected void spawnEgg(AbstractBirdEntity<?> mate) {

        ItemStack eggStack = createEgg(mate);

        ItemEntity entity = new ItemEntity(
                mate.level(),
                getX(),
                getY() + 0.2,
                getZ(),
                eggStack
        );

        entity.setDefaultPickUpDelay();

        mate.level().addFreshEntity(entity);
    }




    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
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
        if (releaseSettleTicks > 0) {
            --releaseSettleTicks;
        }
        if (this.level().isClientSide) {
            getTickController().tickClient();
        } else {
            getTickController().tick();
            if (growthStopped && this.getAge() < 0) {
                this.setAge(this.getAge() - 1);
            }
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
        compoundTag.putInt("BirdTrustTicks", getTickController().getTickTimer().getBirdTrustTicker().getTicks());
        compoundTag.putInt("BirdCuriousTicks", getTickController().getTickTimer().getBirdCuriousTicker().getTicks());
        compoundTag.putBoolean("BirdGender", getBreedController().getGender());
        compoundTag.putInt("BirdModelVariant", getModelController().getModelVariant());
        compoundTag.putInt("BirdSkinVariant", getSkinController().getSkinVariant());
        BirdModelScale.save(compoundTag, this.getIndividualModelScale(), this.modelScaleProfile());
        compoundTag.putInt("BirdEggCount", getBreedController().getEggCount());
        compoundTag.putInt("BirdFeatherCount", getFeatherController().getFeatherCount());
        compoundTag.putInt("BirdFeatherInterval", getFeatherController().getFeatherInterval());
        compoundTag.putBoolean("BirdGrowthStopped", isGrowthStopped());
        if (getTameController().getInterestedPlayerUUID() != null) {
            compoundTag.putUUID("BirdInterestedPlayer", getTameController().getInterestedPlayerUUID());
        }
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        getTickController().getTickTimer().getBirdTrustTicker().setTicks(compoundTag.getInt("BirdTrustTicks"));
        getTickController().getTickTimer().getBirdCuriousTicker().setTicks(compoundTag.getInt("BirdCuriousTicks"));
        if (compoundTag.contains("BirdGender", CompoundTag.TAG_BYTE)) {
            getBreedController().setGender(compoundTag.getBoolean("BirdGender"));
        } else {
            getBreedController().randomizeGender();
        }
        if (compoundTag.contains("BirdModelVariant", CompoundTag.TAG_INT)) {
            getModelController().setModelVariant(compoundTag.getInt("BirdModelVariant"));
        } else {
            getModelController().setModelVariant(getModelController().getRandomizeModelVariant(BirdModelRarity.COMMON, true, false, isBaby(), isMale(), !isMale(), false));
        }
        if (compoundTag.contains("BirdSkinVariant", CompoundTag.TAG_INT)) {
            getSkinController().setSkinVariant(compoundTag.getInt("BirdSkinVariant"));
        } else {
            getSkinController().setSkinVariant(getSkinController().getRandomizeSkinVariant(BirdSkinRarity.COMMON, true, false, isBaby(), isMale(), !isMale(), false));
        }
        if (compoundTag.contains("BirdModelScale", CompoundTag.TAG_FLOAT)) {
            this.setIndividualModelScale(BirdModelScale.load(compoundTag, this.modelScaleProfile()));
        } else {
            getModelController().randomizeModelScale();
        }
        if (compoundTag.hasUUID("BirdInterestedPlayer")) {
            getTameController().setInterestedPlayerUUID(compoundTag.getUUID("BirdInterestedPlayer"));
        }
        if (compoundTag.contains("BirdEggCount", CompoundTag.TAG_INT)) {
            getBreedController().setEggCount(compoundTag.getInt("BirdEggCount"));
        } else {
            getBreedController().randomizeEggCount();
        }
        if (compoundTag.contains("BirdFeatherCount", CompoundTag.TAG_INT)) {
            getFeatherController().setFeatherCount(compoundTag.getInt("BirdFeatherCount"));
        } else {
            getFeatherController().randomizeFeatherCount();
        }
        if (compoundTag.contains("BirdFeatherInterval", CompoundTag.TAG_INT)) {
            getFeatherController().setFeatherInterval(compoundTag.getInt("BirdFeatherInterval"));
        } else {
            getFeatherController().randomizeFeatherInterval();
        }
        if (compoundTag.contains("BirdGrowthStopped", CompoundTag.TAG_BYTE)) {
            setGrowthStopped(compoundTag.getBoolean("BirdGrowthStopped"));
        } else {
            setGrowthStopped(false);
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

    /**
     * 这个对象是不是「游离的渲染副本」：不在 level 的实体表里（{@code level.getEntity(id)} 不是它）。
     *
     * <p>Carry On 抱持时每帧都会新建一个实体副本交给渲染器，它就是这样；鸟笼 / 观鸟手册 / GT 罐子里的鸟
     * 同样不在 level 里，但它们各有自己的预览驱动（见 {@link #tickPreviewIfNotLevelTicked(long)}）。
     * 世界里的鸟永远在 level 里，因此这个判断对它们恒为 false。</p>
     */
    /**
     * 这个对象是不是「游离的渲染副本」：不在 level 的实体表里。
     *
     * <p>Carry On 抱持时每帧都会新建一个实体副本交给渲染器，它就是这样（鸟笼 / 观鸟手册 / GT 罐子里的鸟
     * 同样不在 level 里，但它们是稳定复用的对象）。<b>判断必须用对象身份，不能用 gameTime 之类的时序比较：</b>
     * 渲染帧远多于 tick，用「这一 tick 有没有被 tick 过」会让世界里的鸟在 tick 刚自增的那几帧被误判，
     * 动画每帧在 idle 与 walk/fly 间反复切换、看起来完全不播。用身份判定则世界里的鸟恒为 false。</p>
     */
    public boolean isRenderCopy() {
        // 不检查 id > 0：新建的副本往往还没被 level 分配 id（id == 0），加了这个条件会把被抱持的对象漏掉。
        return this.level() != null && this.level().getEntity(this.getId()) != this;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        // 被抱持时每帧都是新对象：各自持有缓存的话 GeckoLib 每帧都会新建 AnimatableManager，
        // 动画永远从第 0 帧开始（画出来就是模型默认姿势、看着完全不动）。这类对象共用一份按实体类型的缓存，
        // 配合 BirdModelRenderer#getInstanceId 给的稳定 id，manager 才能跨帧延续、动画正常推进。
        if (this.isRenderCopy()) {
            return RENDER_COPY_CACHES.computeIfAbsent(this.getType(),
                    type -> GeckoLibUtil.createInstanceCache(this));
        }

        return getAnimationController().getCache();
    }

    /**
     * 让鸟在游戏暂停时也推进动画（单人存档打开背包 / 观鸟手册等 GUI 时 {@code Minecraft#isPaused()} 为真）。
     *
     * <p>GeckoLib 默认在暂停时不更新动画时钟（{@code GeoModel.handleAnimations} 里
     * {@code if (!mc.isPaused() || animatable.shouldPlayAnimsWhileGamePaused())}），而鸟笼物品、
     * 观鸟手册实体页这类预览恰恰都是在 GUI 打开时渲染的：不返回 true 它们全都停在预置姿势。
     * 实测日志里这些鸟的 {@code lastUpdate} 恒为 0.0，而世界里未被暂停渲染的鸟则正常递增。</p>
     */
    @Override
    public boolean shouldPlayAnimsWhileGamePaused() {
        return true;
    }

    /**
     * 控制实体动画状态机，根据行为状态和计时器决定播放哪种动画
     *
     * @param animationState 动画状态对象，用于设置和切换动画
     * @return 播放状态，包含要播放的动画
     */
    public <E extends AbstractBirdEntity<?>> PlayState movementController(AnimationState<E> animationState) {
        BirdBehaviorState state = getBehaviorStateController().getBehaviorState();
        var tickTimer = getTickController().getTickTimer();

        // 展示预览驱动的鸟（鸟笼 / 观鸟手册 / GT 罐子 / 被 Carry On 抱起的鸟）一律只播待机动画：
        // 它的行为状态是照搬活体实体的（可能停在 WALKING，或者悬空让 shouldPlayFlyAnimation() 为真），
        // 照原逻辑会误播行走 / 飞行 / 奔跑动画。世界里的鸟从不进入预览驱动，完整播放所有动画。
        if (this.previewDriven) {
            return animationState.setAndContinue(getAnimationController().pickIdleAnimation());
        }

        // Keep the selected sleep transition while the roost goal hands off
        // from ROOSTING to SLEEPING. Clearing it during that hand-off causes
        // the entry animation to play again.
        if (state != BirdBehaviorState.SLEEPING && state != BirdBehaviorState.ROOSTING
                && getRoutineController().isActiveTime()) {
            getAnimationController().resetSleepAnimation();
        }

        // 舞蹈动画条件：只有在非舞蹈状态且音乐计时器已归零时，才进入常规动画逻辑
        // 否则直接播放舞蹈动画
        if (state != BirdBehaviorState.DANCING && tickTimer.getBirdMusicTicker().getTicks() <= 0) {

            // 进食动画条件：进食状态中或进食计时器未归零
            if (state != BirdBehaviorState.EATING && tickTimer.getBirdEatingTicker().getTicks() <= 0) {

                // 睡眠动画：根据行为计时器判断是进入睡眠还是睡眠循环
                if ((state == BirdBehaviorState.SLEEPING || state == BirdBehaviorState.ROOSTING) && isLeashed()) {
                    var flyAnimation = BIRD_DATA.animation().animationMap().get("fly");
                    if (flyAnimation == null) {
                        flyAnimation = BIRD_DATA.animation().animationMap().get("fly_glide");
                    }
                    if (flyAnimation == null) {
                        flyAnimation = BIRD_DATA.animation().animationMap().get("walk");
                    }
                    if (flyAnimation != null) {
                        return animationState.setAndContinue(flyAnimation);
                    }
                }
                if (state == BirdBehaviorState.SLEEPING) {
                    // Cache the transition animation for the whole sleep session. Re-selecting
                    // between "sleep" and "sleep_loop" every tick restarts the wake-up segment.
                    return animationState.setAndContinue(getAnimationController().pickSleepAnimation());
                }

                // 飞行动画：由飞行条件控制器决定
                if (getAnimationController().shouldPlayFlyAnimation()) {
                    return animationState.setAndContinue(getAnimationController().pickFlyAnimation());
                }


                // 地面移动逻辑
                if (state == BirdBehaviorState.FLEEING && !canFly()) {
                    var escapeWalk = BIRD_DATA.animation().animationMap().get("walk");
                    if (escapeWalk != null) {
                        return animationState.setAndContinue(escapeWalk);
                    }
                }

                double deltaMovementSqr = this.getDeltaMovement().lengthSqr();
                double walkingThreshold = BIRD_DATA.misc().walkingSpeedThreshold();
                double runningSpeedThreshold = BIRD_DATA.misc().runningSpeedThreshold();
                boolean isNavigationDone = this.getNavigation().isDone();

                // 静止状态：移动速度低于阈值、导航结束且不是行走状态
                if ((!(deltaMovementSqr > walkingThreshold) && isNavigationDone && state != BirdBehaviorState.WALKING && state != BirdBehaviorState.FORAGING)) {

                    // 梳理羽毛
                    if (state == BirdBehaviorState.PREENING) {
                        return setAndContinueOrIdle(animationState, "preen");
                    }

                    // 空闲动画：非好奇、非警觉状态且好奇计时器归零时播放
                    if (state != BirdBehaviorState.CURIOUS && state != BirdBehaviorState.ALERT
                            && tickTimer.getBirdCuriousTicker().getTicks() <= 0) {
                        return animationState.setAndContinue(getAnimationController().pickIdleAnimation());
                    }

                    var animation = BIRD_DATA.animation().animationMap().get("curious");
                    // 好奇动画（默认的静止状态动画）
                    if (animation != null) {
                        return animationState.setAndContinue(animation);
                    } else return animationState.setAndContinue(getAnimationController().pickIdleAnimation());
                }

                if (deltaMovementSqr > runningSpeedThreshold) {
                    var animation = BIRD_DATA.animation().animationMap().get("run");
                    if (animation != null) {
                        return animationState.setAndContinue(animation);
                    }
                }

                // 行走动画（移动中）
                return setAndContinueOrIdle(animationState, "walk");
            }

            // 进食动画（进食状态或进食计时器未归零）
            return setAndContinueOrIdle(animationState, "eat");
        }

        // 舞蹈动画（音乐计时器运行中或处于舞蹈状态）
        return setAndContinueOrIdle(animationState, "dance");
    }

    /**
     * 播放 {@code animationMap} 里的 {@code key}，该鸟没有这个动画时回退到 {@code idle}。
     *
     * <p>GeckoLib 收到 null 动画时不会套用任何骨骼变换（{@code AnimationController.process} 里
     * {@code currentAnimation == null} 就跳过整段），模型会停在预置姿势、看起来完全静止，
     * 所以宁可用待机动画兜底。</p>
     */
    private <E extends AbstractBirdEntity<?>> PlayState setAndContinueOrIdle(AnimationState<E> animationState, String key) {
        RawAnimation animation = BIRD_DATA.animation().animationMap().get(key);

        if (animation == null) {
            animation = BIRD_DATA.animation().animationMap().get("idle");
        }

        return animationState.setAndContinue(animation);
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
    protected void playStepSound(@NotNull BlockPos pos, @NotNull BlockState blockState) {
        this.playSound(SoundEvents.CHICKEN_STEP, 0.12F, 0.9F);
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

    @Override
    public boolean causeFallDamage(float fallDistance, float damageMultiplier, @NotNull DamageSource damageSource) {
        return false;
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

    public BirdSkinController<T> getSkinController() {
        return BIRD_CONTROLLERS.birdSkinController();
    }

    public BirdFeatherController<T> getFeatherController() {
        return BIRD_CONTROLLERS.birdFeatherController();
    }

    public BirdModelController<T> getModelController() {
        return BIRD_CONTROLLERS.birdModelController();
    }

    public BirdBreedController<T> getBreedController() {
        return BIRD_CONTROLLERS.birdBreedController();
    }

    public BirdBehaviorStateController<T> getBehaviorStateController() {
        return BIRD_CONTROLLERS.birdBehaviorStateController();
    }

    public BirdAnimationController<T> getAnimationController() {
        return BIRD_CONTROLLERS.birdAnimationController();
    }

    public BirdFoodBagController<T> getFoodBagController() {
        return BIRD_CONTROLLERS.birdFoodBagController();
    }

    public BirdData getBirdData() {
        return BIRD_DATA;
    }

    public void setMoveControl(MoveControl control) {
        this.moveControl = control;
    }


    public boolean isDancing() {
        return this.getBirdControllers().getBirdTickController().getTickTimer().getBirdMusicTicker().getTicks() > 0
                || this.getBirdControllers().getBirdBehaviorStateController().getBehaviorState() == BirdBehaviorState.DANCING;
    }

    public void applyEggData(BirdEggData data) {
        getBreedController().setGender(data.gender());
        getBreedController().setEggCount(data.eggCount());
        getFeatherController().setFeatherCount(data.featherCount());
        getFeatherController().setFeatherInterval(data.featherInterval());
        getModelController().setModelVariant(data.model());
        getSkinController().setSkinVariant(data.skin());
        setIndividualModelScale(data.size());
    }

    public boolean isMale() {
        return getBreedController().getGender();
    }

    public void setMale(boolean male) {
        getBreedController().setGender(male);
    }

    public int getEggCount() {
        return getBreedController().getEggCount();
    }

    public void setEggCount(int i) {
        getBreedController().setEggCount(i);
    }

    public int getFeatherCount() {
        return getFeatherController().getFeatherCount();
    }

    public void setFeatherCount(int i) {
        getFeatherController().setFeatherCount(i);
    }

    public int getFeatherInterval() {
        return getFeatherController().getFeatherInterval();
    }

    public void setFeatherInterval(int i)
    {
        getFeatherController().setFeatherInterval(i);
    }

    public BirdSkin getSkin() {
        BirdData birdData = getBirdData();
        BirdModelSkinDatum modelDatum = birdData.model();
        return modelDatum.birdSkin().get(getSkinController().getSkinVariant());
    }

    public BirdModel getModel() {
        BirdData birdData = getBirdData();
        BirdModelSkinDatum modelDatum = birdData.model();
        return modelDatum.birdModel().get(getModelController().getModelVariant());
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
                    xOffset,
                    yOffset + 0.035,
                    zOffset
            );
        }
    }

    public boolean isGrowthStopped() {
        return growthStopped;
    }

    public void setGrowthStopped(boolean growthStopped) {
        this.growthStopped = growthStopped;
    }

    /**
     * 只推进「展示用」的动画与行为状态：适用于**被移出世界但仍在渲染**的鸟
     * （Carry On 抱持、鸟笼预览等），它们不会被 level tick，动画时间与状态机都得自己走一遍。
     *
     * <p>与笼中鸟的表现一致：同步 GeckoLib 的动画时间（{@code tickCount}）、推进客户端计时器，
     * 并在没有 goal 的情况下自己走一遍睡眠 / 清醒 / 好奇的状态机。</p>
     *
     * <p><b>调用方不要改用 {@code Entity#tick()}：</b>那会同时驱动 AI、重力与骑乘逻辑，
     * 而 Carry On 抱持时玩家是骑在这只实体上的，会导致玩家按方向键被「拖着冲刺」。</p>
     *
     * @param gameTime 客户端 {@code level.getGameTime()}；同一 gameTime 内重复调用直接返回
     */
    public void tickAnimationPreview(long gameTime) {
        if (this.previewTickedAt == gameTime) {
            return;
        }
        this.previewTickedAt = gameTime;

        // 只要被展示预览驱动过（鸟笼 / 观鸟手册 / GT 罐子 / Carry On 抱持），就标记为展示态：
        // 这类鸟的行为状态照搬活体实体（WALKING、悬空等），照原逻辑会误播走 / 飞 / 跑，必须只播待机动画。
        this.previewDriven = true;

        // 被移出世界的鸟（Carry On 抱持等）不会被 tick：抱起那一刻残留的 Motion 永远不会衰减，
        // 会让鸟一直被判定为「移动中」而落进行走 / 奔跑分支，播不到待机动画；捕捉时残留的好奇
        // 计时也会让它一直张望。这里按鸟笼预览同样的方式清一次——只对真正被移出世界的鸟做，
        // 正在世界里正常 tick 的鸟必须保留自己的 Motion。
        if (this.isRemoved()) {
            this.setDeltaMovement(0.0D, 0.0D, 0.0D);
            getTickController().getTickTimer().getBirdCuriousTicker().setTicks(0);
        }

        // ① GeckoLib 的动画时间，必须用**绝对时钟**（gameTime）而不能按实体自增。
        // GeoModel.handleAnimations 的动画时钟就是 tickCount（currentFrameTime = tickCount + partialTick），
        // 而 Carry On 抱持时每帧都会**新建一个实体副本**交给渲染器（实测 renderRaw 每帧一个不同 id、各画一次）：
        // 副本的 AnimatableManager 是全新的，若 tickCount 从 0 自增，每个副本都只走 1 帧、永远采样动画第 0 帧，
        // 看起来完全静止。用 gameTime 则任何一帧新建的副本都直接落在正确的动画相位上。
        this.tickCount = (int) gameTime;
        // ② 客户端计时器（待机动画会像真实鸟一样按自己的节奏更换）。
        getTickController().tickClient();

        var stateController = getBehaviorStateController();
        var routineController = getRoutineController();

        // ③ 行为状态机：等价于各个 goal。
        // 休息时间：等价于 roost and sleep goal，预览鸟无法飞到栖息点，直接入睡。
        if (!routineController.isActiveTime()) {
            stateController.setBehaviorState(BirdBehaviorState.SLEEPING);
            return;
        }

        // 回到活动时间：等价于 wake up goal，从 sleep_loop 转回 idle。
        if (routineController.isSleeping()) {
            stateController.setBehaviorState(BirdBehaviorState.IDLE);
            return;
        }

        var timer = getTickController().getTickTimer();

        // 好奇结束：等价于 curious follow goal 的 onStop。
        if (stateController.getBehaviorState() == BirdBehaviorState.CURIOUS) {
            if (!timer.getBirdBehaviorStateTicker().isRunning()) {
                stateController.setBehaviorState(BirdBehaviorState.IDLE);
            }
            return;
        }

        // 偶尔好奇：等价于 curious follow goal，只是预览鸟无法跟过去，只能张望。
        if (stateController.getBehaviorState() == BirdBehaviorState.IDLE
                && timer.getBirdIdleAnimationTicker().getTicks() <= 0
                && getRandom().nextInt(CURIOUS_CHANCE) == 0) {
            var goalDatum = getBirdData().goal();
            stateController.setBehaviorStateFor(BirdBehaviorState.CURIOUS,
                    goalDatum.curiousTicks() + getRandom().nextInt(goalDatum.curiousTicksVariance()));
        }
    }

    /**
     * 渲染前的兜底：这只鸟的动画时钟这一 tick 没往前走的话，就自己推进一次。
     *
     * <p>GeckoLib 的动画时钟就是 {@code tickCount}（{@code currentFrameTime = tickCount + partialTick}）。
     * Carry On 抱持的鸟在服务端被 {@code remove()}，客户端虽然不是 {@code isRemoved()}，却也没人给它
     * {@code tickCount++}（实测日志里 `tick` 五帧都停在 100、`idleTicker` 也一动不动），于是时钟停在原地、
     * 模型永远采样第 0 帧，表现为「纯预置姿势」。</p>
     *
     * <p>判据用「这一 gameTime 里有没有跑过 {@code aiStep}」：跑了就说明 level 在 tick 它（世界里的鸟），不动手；
     * {@code previewTickedAt} 相同 ⇒ 这一 tick 已由鸟笼 / 观鸟手册 / GT 推进过，也不动手。</p>
     *
     * <p>不用 {@code tickCount} 是否变化当判据：被抱的鸟身上 {@code tickCount} 会被别的东西改动，
     * 但它自己的 {@code idleTicker} 一动不动（说明 {@code aiStep} 没跑），用 tickCount 会误判成「有人在 tick」。</p>
     *
     * @param gameTime 客户端 {@code level.getGameTime()}
     * @return 本次是否由这里推进
     */
    public boolean tickPreviewIfNotLevelTicked(long gameTime) {
        // 只有「游离的渲染副本」（不在 level 实体表里，即被 Carry On 抱持的那种每帧新建的对象）才需要兜底。
        // 不能改成按 gameTime 判断「这一 tick 有没有被 tick 过」：渲染帧多于 tick，tick 刚自增而实体还没 tick
        // 的那几帧里世界里的鸟也会满足，于是它们每帧被误标成展示态、动画在 idle 与 walk/fly 间反复切换。
        if (!this.isRenderCopy() || this.previewTickedAt == gameTime) {
            return false;
        }

        // 走到这里说明这一 tick 还没有任何预览驱动推进过它——自己补一次。
        this.tickAnimationPreview(gameTime);
        return true;
    }

}
