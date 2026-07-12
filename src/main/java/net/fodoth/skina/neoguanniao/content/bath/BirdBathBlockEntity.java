package net.fodoth.skina.neoguanniao.content.bath;

import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;

public class BirdBathBlockEntity extends BlockEntity implements GeoBlockEntity {

    private static final int MAX_CONTENT_LEVEL = 3;
    private static final int RAIN_REFILL_AMOUNT = 3;
    private static final int ENVIRONMENT_TICK_INTERVAL = 200;
    private static final int EVAPORATION_CHANCE = 72;
    private static final int WATER_DIRT_CHANCE = 36;
    private static final int FISH_MEAT_SPOIL_TICKS = 72000;
    private static final int BREAD_SPOIL_TICKS = 144000;
    private static final int SUNLIGHT_SPOIL_BONUS_DIVISOR = 8;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private BirdBathContentType contentType = BirdBathContentType.EMPTY;
    private BirdBathContentType spoiledContentType = BirdBathContentType.EMPTY;
    private BirdBathCleanliness cleanliness = BirdBathCleanliness.CLEAN;
    private int contentLevel;
    private int spoilTicks;
    private int environmentalTickOffset;
    private UUID currentUser;
    private int occupiedTicks;
    private int recentBirdUseTicks;

    public BirdBathBlockEntity(BlockPos pos, BlockState state) {
        super(NeoGuanNiaoBlockEntityTypes.BIRD_BATH.get(), pos, state);
        this.environmentalTickOffset = Math.floorMod(Long.hashCode(pos.asLong()), 20000);
    }

    public BirdBathVariant variant() {
        Block block = getBlockState().getBlock();
        if (block instanceof BirdBathBlock birdBathBlock) {
            return birdBathBlock.variant();
        }
        return BirdBathVariant.BIRD_BATH;
    }

    public BirdBathContentType getContentType() {
        return this.contentType;
    }

    public BirdBathContentType getRenderContentType() {
        if (this.contentType == BirdBathContentType.SPOILED) {
            return this.spoiledContentType.isEmpty() ? BirdBathContentType.FISH : this.spoiledContentType;
        }
        return this.contentType;
    }

    public int getContentLevel() {
        return this.contentLevel;
    }

    public BirdBathCleanliness getCleanliness() {
        return this.cleanliness;
    }

    public boolean isEmpty() {
        return this.contentType.isEmpty() || this.contentLevel <= 0;
    }

    public boolean hasUsableWater() {
        return this.contentType == BirdBathContentType.WATER && this.contentLevel > 0
                && this.cleanliness != BirdBathCleanliness.FILTHY;
    }

    public boolean hasUsableFood() {
        return this.contentType.isFood() && this.contentLevel > 0
                && this.cleanliness != BirdBathCleanliness.FILTHY;
    }

    public boolean hasFoodForBird(BirdBathFoodPreference preference) {
        return preference != null && this.contentLevel > 0 && !this.isSpoiled()
                && this.cleanliness != BirdBathCleanliness.FILTHY
                && preference.matches(this.contentType);
    }

    public boolean isFrozen() {
        return this.contentType == BirdBathContentType.FROZEN_WATER;
    }

    public boolean isSpoiled() {
        return this.contentType == BirdBathContentType.SPOILED;
    }

    public boolean isDirty() {
        return this.cleanliness.isDirty();
    }

    public boolean canAccept(BirdBathContentType type) {
        if (type != null && !type.isEmpty() && !this.isSpoiled()) {
            if (type == BirdBathContentType.WATER) {
                return !this.isEmpty() && this.contentType != BirdBathContentType.WATER
                        && this.contentType != BirdBathContentType.FROZEN_WATER;
            } else if (!type.isFood()) {
                return true;
            } else {
                return !this.isEmpty() && this.contentType != type;
            }
        }
        return true;
    }

    public boolean setContent(BirdBathContentType type, int level) {
        BirdBathContentType normalizedType = type == null ? BirdBathContentType.EMPTY : type;
        int normalizedLevel = Mth.clamp(level, 0, MAX_CONTENT_LEVEL);
        if (normalizedType.isEmpty() || normalizedLevel <= 0) {
            normalizedType = BirdBathContentType.EMPTY;
            normalizedLevel = 0;
        }

        boolean changed = this.contentType != normalizedType || this.contentLevel != normalizedLevel;
        if (!changed) {
            if (normalizedType.isFood() && this.spoilTicks != 0) {
                this.spoilTicks = 0;
                this.sync();
                return true;
            }
            return false;
        }

        this.contentType = normalizedType;
        this.contentLevel = normalizedLevel;
        if (normalizedType != BirdBathContentType.SPOILED) {
            this.spoiledContentType = BirdBathContentType.EMPTY;
        }

        this.spoilTicks = normalizedType.isFood() ? 0 : this.spoilTicks;
        if (!normalizedType.isFood() && normalizedType != BirdBathContentType.SPOILED) {
            this.spoilTicks = 0;
        }

        this.sync();
        return true;
    }

    public boolean clearContent() {
        return this.setContent(BirdBathContentType.EMPTY, 0);
    }

    public boolean addContent(BirdBathContentType type, int amount) {
        if (type != null && !type.isEmpty() && amount > 0) {
            if (this.canAccept(type)) {
                return false;
            }
            if (this.contentLevel >= MAX_CONTENT_LEVEL) {
                return false;
            }
            int baseLevel = this.isEmpty() ? 0 : this.contentLevel;
            return this.setContent(type, Math.min(MAX_CONTENT_LEVEL, baseLevel + amount));
        }
        return false;
    }

    public boolean cleanByHand() {
        if (this.isSpoiled()) {
            this.contentType = BirdBathContentType.EMPTY;
            this.spoiledContentType = BirdBathContentType.EMPTY;
            this.contentLevel = 0;
            this.spoilTicks = 0;
            this.cleanliness = dirtierOf(this.cleanliness, BirdBathCleanliness.DIRTY);
            this.sync();
            return true;
        }
        if (!this.cleanliness.isDirty()) {
            return false;
        }
        this.cleanliness = this.cleanliness.cleanOneStep();
        this.sync();
        return true;
    }

    public boolean consumeOneServing() {
        if (this.contentLevel > 0 && !this.isSpoiled() && !this.isFrozen()) {
            boolean water = this.contentType == BirdBathContentType.WATER;
            if (!water && !this.contentType.isFood()) {
                return false;
            }
            --this.contentLevel;
            if (this.contentLevel <= 0) {
                this.contentType = BirdBathContentType.EMPTY;
                this.spoiledContentType = BirdBathContentType.EMPTY;
                this.contentLevel = 0;
                this.spoilTicks = 0;
            }

            this.markUsedByBird();
            if (this.level != null) {
                BirdBathEffects.birdUsed(this.level, this.worldPosition, water);
            }

            this.sync();
            return true;
        }
        return false;
    }

    public void markUsedByBird() {
        this.recentBirdUseTicks = 600;
        this.cleanliness = this.cleanliness.nextDirtier();
        this.sync();
    }

    public boolean tryClaimUse(UUID birdUuid, int ticks) {
        if (birdUuid != null && ticks > 0) {
            if (this.currentUser != null && this.occupiedTicks > 0
                    && !this.currentUser.equals(birdUuid)) {
                return false;
            }
            this.currentUser = birdUuid;
            this.occupiedTicks = Math.max(this.occupiedTicks, ticks);
            this.sync();
            return true;
        }
        return false;
    }

    public void releaseUse(UUID birdUuid) {
        if (birdUuid != null && birdUuid.equals(this.currentUser)) {
            this.currentUser = null;
            this.occupiedTicks = 0;
            this.sync();
        }
    }

    public boolean isOccupied() {
        return this.currentUser != null && this.occupiedTicks > 0;
    }

    public boolean isOccupiedBy(UUID birdUuid) {
        return birdUuid != null && birdUuid.equals(this.currentUser) && this.occupiedTicks > 0;
    }

    public static void serverTick(
            Level level,
            BlockPos pos,
            BlockState state,
            BlockEntity blockEntity
    ) {
        if (level.isClientSide()) { return; }

        ((BirdBathBlockEntity) blockEntity)
                .serverTick((ServerLevel) level, pos, state);
    }


    private void serverTick(
            ServerLevel level,
            BlockPos pos,
            BlockState state
    ) {

        boolean changed = tickOccupation();

        long gameTime = level.getGameTime();

        if (Math.floorMod(
                (int)(gameTime + environmentalTickOffset),
                ENVIRONMENT_TICK_INTERVAL
        ) == 0) {

            changed |= runEnvironmentTick(
                    level,
                    pos,
                    state,
                    level.getRandom(),
                    ENVIRONMENT_TICK_INTERVAL
            );
        }

        if (changed) {
            sync();
        }
    }

    public void environmentTick(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        if (this.runEnvironmentTick(level, pos, state, random, ENVIRONMENT_TICK_INTERVAL)) {
            this.sync();
        }
    }

    @SuppressWarnings("SameParameterValue")
    private boolean runEnvironmentTick(ServerLevel level, BlockPos pos, BlockState state, RandomSource random, int elapsedTicks) {
        boolean changed = false;
        boolean openToSky = level.canSeeSky(pos.above());
        boolean rainingHere = openToSky && level.isRainingAt(pos.above());

        if (rainingHere) {
            changed |= this.tickRainRefill(level, pos, random);
        } else if (this.contentType == BirdBathContentType.WATER && openToSky
                && level.isDay() && random.nextInt(EVAPORATION_CHANCE) == 0) {
            changed |= this.reduceWaterByEvaporation(level, pos);
        }

        if (openToSky) {
            changed |= this.tickFreezeAndMelt(level, pos, random);
        }

        changed |= this.tickSpoilage(level, pos, elapsedTicks);
        changed |= this.tickLongTermDirt(random);

        if (this.isDirty() || this.isSpoiled()) {
            BirdBathEffects.idleDirty(level, pos, this.cleanliness, this.isSpoiled());
        }

        return changed;
    }


    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt("ContentType", this.contentType.ordinal());
        tag.putInt("ContentLevel", this.contentLevel);
        tag.putInt("Cleanliness", this.cleanliness.ordinal());
        tag.putInt("SpoiledContentType", this.spoiledContentType.ordinal());
        tag.putInt("SpoilTicks", this.spoilTicks);
        tag.putInt("EnvironmentalTickOffset", this.environmentalTickOffset);
        tag.putInt("OccupiedTicks", this.occupiedTicks);
        tag.putInt("RecentBirdUseTicks", this.recentBirdUseTicks);
        if (this.currentUser != null) {
            tag.putUUID("CurrentUser", this.currentUser);
        }
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        super.loadAdditional(tag, provider);

        BirdBathContentType loadedType = tag.contains("ContentType")
                ? BirdBathContentType.fromOrdinal(tag.getInt("ContentType"))
                : BirdBathContentType.EMPTY;
        int loadedLevel = tag.contains("ContentLevel")
                ? Mth.clamp(tag.getInt("ContentLevel"), 0, MAX_CONTENT_LEVEL)
                : 0;

        if (!loadedType.isEmpty() && loadedLevel > 0) {
            this.contentType = loadedType;
            this.contentLevel = loadedLevel;
        } else {
            this.contentType = BirdBathContentType.EMPTY;
            this.contentLevel = 0;
        }

        this.cleanliness = tag.contains("Cleanliness")
                ? BirdBathCleanliness.fromOrdinal(tag.getInt("Cleanliness"))
                : BirdBathCleanliness.CLEAN;

        this.spoiledContentType = tag.contains("SpoiledContentType")
                ? BirdBathContentType.fromOrdinal(tag.getInt("SpoiledContentType"))
                : BirdBathContentType.EMPTY;

        if (this.contentType != BirdBathContentType.SPOILED || !this.spoiledContentType.isFood()) {
            this.spoiledContentType = BirdBathContentType.EMPTY;
        }

        this.spoilTicks = tag.contains("SpoilTicks") ? Math.max(0, tag.getInt("SpoilTicks")) : 0;
        this.environmentalTickOffset = tag.contains("EnvironmentalTickOffset")
                ? tag.getInt("EnvironmentalTickOffset")
                : this.environmentalTickOffset;
        this.occupiedTicks = tag.contains("OccupiedTicks") ? Math.max(0, tag.getInt("OccupiedTicks")) : 0;
        this.recentBirdUseTicks = tag.contains("RecentBirdUseTicks") ? Math.max(0, tag.getInt("RecentBirdUseTicks")) : 0;
        this.currentUser = tag.hasUUID("CurrentUser") ? tag.getUUID("CurrentUser") : null;

        if (this.occupiedTicks <= 0) {
            this.currentUser = null;
        }
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("ContentType", this.contentType.ordinal());
        tag.putInt("ContentLevel", this.contentLevel);
        tag.putInt("Cleanliness", this.cleanliness.ordinal());
        tag.putInt("SpoiledContentType", this.spoiledContentType.ordinal());
        tag.putInt("OccupiedTicks", this.occupiedTicks);
        return tag;
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(@NotNull Connection connection, ClientboundBlockEntityDataPacket packet,
                             HolderLookup.@NotNull Provider provider) {
        CompoundTag tag = packet.getTag();
        this.loadAdditional(tag, provider);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "bird_bath", 0, state -> PlayState.CONTINUE));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    private void sync() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            BlockState state = this.getBlockState();
            this.level.sendBlockUpdated(this.worldPosition, state, state, 3);
        }
    }

    private boolean tickOccupation() {
        boolean changed = false;
        if (this.occupiedTicks > 0) {
            this.occupiedTicks = Math.max(0, this.occupiedTicks - 1);
            this.setChanged();
            if (this.occupiedTicks == 0) {
                this.currentUser = null;
                changed = true;
            }
        }

        if (this.recentBirdUseTicks > 0) {
            this.recentBirdUseTicks = Math.max(0, this.recentBirdUseTicks - 1);
            this.setChanged();
            if (this.recentBirdUseTicks == 0) {
                changed = true;
            }
        }

        return changed;
    }

    private boolean tickRainRefill(ServerLevel level, BlockPos pos, RandomSource random) {
        boolean changed = false;
        if (this.isEmpty() || this.contentType == BirdBathContentType.WATER) {
            int oldLevel = this.contentLevel;
            BirdBathContentType oldType = this.contentType;
            int newLevel = Math.min(MAX_CONTENT_LEVEL, (this.isEmpty() ? 0 : this.contentLevel) + RAIN_REFILL_AMOUNT);
            this.contentType = BirdBathContentType.WATER;
            this.contentLevel = newLevel;
            this.spoilTicks = 0;
            changed = oldType != this.contentType || oldLevel != newLevel;
            if (changed) {
                BirdBathEffects.waterAdded(level, pos, SoundEvents.WATER_AMBIENT);
            }
        }

        if (!this.isSpoiled() && this.cleanliness.isDirty() && random.nextInt(3) == 0) {
            this.cleanliness = this.cleanliness.cleanOneStep();
            changed = true;
        }

        return changed;
    }

    private boolean reduceWaterByEvaporation(ServerLevel level, BlockPos pos) {
        if (this.contentType == BirdBathContentType.WATER && this.contentLevel > 0) {
            --this.contentLevel;
            if (this.contentLevel <= 0) {
                this.contentType = BirdBathContentType.EMPTY;
            }
            BirdBathEffects.evaporated(level, pos);
            return true;
        }
        return false;
    }

    private boolean tickFreezeAndMelt(ServerLevel level, BlockPos pos, RandomSource random) {
        boolean coldEnough = level.getBiome(pos).value().coldEnoughToSnow(pos);
        if (this.contentType == BirdBathContentType.WATER && coldEnough && random.nextInt(3) == 0) {
            this.contentType = BirdBathContentType.FROZEN_WATER;
            this.spoilTicks = 0;
            BirdBathEffects.froze(level, pos);
            return true;
        } else if (this.contentType == BirdBathContentType.FROZEN_WATER && !coldEnough
                && level.isDay() && random.nextInt(4) == 0) {
            this.contentType = BirdBathContentType.WATER;
            BirdBathEffects.melted(level, pos);
            return true;
        }
        return false;
    }

    private boolean tickSpoilage(ServerLevel level, BlockPos pos, int elapsedTicks) {
        if (!this.contentType.isFood()) {
            return false;
        }

        int increment = Math.max(1, elapsedTicks);
        if (level.isDay() && level.canSeeSky(pos.above()) && !level.isRaining()) {
            increment += Math.max(1, elapsedTicks / SUNLIGHT_SPOIL_BONUS_DIVISOR);
        }

        this.spoilTicks += increment;
        int threshold = this.contentType == BirdBathContentType.BREAD ? BREAD_SPOIL_TICKS : FISH_MEAT_SPOIL_TICKS;

        if (this.spoilTicks >= threshold) {
            this.spoiledContentType = this.contentType;
            this.contentType = BirdBathContentType.SPOILED;
            this.cleanliness = dirtierOf(this.cleanliness, BirdBathCleanliness.DIRTY);
            this.spoilTicks = 0;
            BirdBathEffects.spoiled(level, pos);
        }

        return true;
    }

    private boolean tickLongTermDirt(RandomSource random) {
        if (this.contentType == BirdBathContentType.WATER
                && this.cleanliness != BirdBathCleanliness.FILTHY
                && random.nextInt(WATER_DIRT_CHANCE) == 0) {
            this.cleanliness = this.cleanliness.nextDirtier();
            return true;
        }
        return false;
    }

    @SuppressWarnings("SameParameterValue")
    private static BirdBathCleanliness dirtierOf(BirdBathCleanliness first, BirdBathCleanliness second) {
        return first.ordinal() >= second.ordinal() ? first : second;
    }
}