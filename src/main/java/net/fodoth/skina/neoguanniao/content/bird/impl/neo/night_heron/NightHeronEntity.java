package net.fodoth.skina.neoguanniao.content.bird.impl.neo.night_heron;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.controller.BirdAnimationController;
import net.fodoth.skina.neoguanniao.content.bird.core.controller.BirdBreedController;
import net.fodoth.skina.neoguanniao.content.bird.core.controller.BirdEatingController;
import net.fodoth.skina.neoguanniao.content.bird.core.data.BirdControllers;
import net.fodoth.skina.neoguanniao.content.bird.core.data.BirdData;
import net.fodoth.skina.neoguanniao.content.bird.feature.brain.BirdBrain;
import net.fodoth.skina.neoguanniao.content.bird.feature.species.NightHeronProfile;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoBirdData;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoBlockTags;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoItemTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
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
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animation.RawAnimation;

import java.util.ArrayList;
import java.util.List;

public class NightHeronEntity extends AbstractBirdEntity<NightHeronEntity> {

    private static final EntityDataAccessor<ItemStack> HELD_FISH =
            SynchedEntityData.defineId(NightHeronEntity.class, EntityDataSerializers.ITEM_STACK);

    static final Ingredient TEMPT_ITEMS = Ingredient.of(Items.COD, Items.SALMON, Items.COOKED_COD, Items.COOKED_SALMON);

    public NightHeronEntity(EntityType<NightHeronEntity> entityType, Level level) {
        super(
                entityType,
                level,
                NeoGuanNiaoBirdData.NIGHT_HERON.get(),
                BirdControllers.<NightHeronEntity>builder().birdEatingController(new BirdEatingController<>(){
                    @Override
                    public boolean isEdibleFood(ItemStack stack) {
                        return stack.is(NeoGuanNiaoItemTags.BIRD_FOOD_FISH);
                    }
                }).birdBreedController(new BirdBreedController<>(){
                    @Override
                    public boolean isBreedingFood(ItemStack stack) {
                        return !stack.isEmpty() && stack.is(NeoGuanNiaoItemTags.BIRD_BREED_FOOD_FISH);
                    }
                }).birdAnimationController(new BirdAnimationController<>(){
                    @Override
                    public RawAnimation pickFlyAnimation() {
                        var animationMap = bird().getBirdData().animation().animationMap();
                        if (bird().getDeltaMovement().y() > 0.05) {
                            return animationMap.get("fly");
                        }
                        return animationMap.get("fly_glide");
                    }
                }).build()
        );
        initControllers();
    }

    @Override
    protected NightHeronEntity getSelf() {
        return this;
    }

    @Override
    protected void initFeatures() {
        birdBrain = new BirdBrain(
                this,
                NightHeronProfile.INSTANCE
        );
    }

    @Override
    protected void initControllers() {
        super.initControllers();
    }

    @Override
    protected void initPathfindingMalus() {
        this.setPathfindingMalus(PathType.LEAVES, 0.0F);
        this.setPathfindingMalus(PathType.WATER, -1.0F);
        this.setPathfindingMalus(PathType.WATER_BORDER, 0.0F);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(HELD_FISH, ItemStack.EMPTY);
    }

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

        // 获取附近的实体列表
        var entities = level.getEntitiesOfClass(AbstractBirdEntity.class,
                new AABB(pos.getX() - 8, pos.getY() - 4, pos.getZ() - 8,
                        pos.getX() + 8, pos.getY() + 4, pos.getZ() + 8));

        return entities.size() <= birdData.misc().spawnRarity() && isNearWaterForWorldgen(level, pos, 8);
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


    public static AttributeSupplier.Builder createAttributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 14.0)
                .add(Attributes.MOVEMENT_SPEED, 0.60)
                .add(Attributes.FLYING_SPEED, 0.68)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.ATTACK_DAMAGE, 2.0);
    }

    public static boolean canSpawn(
            EntityType<? extends AbstractBirdEntity<?>> entityType,
            ServerLevelAccessor level,
            MobSpawnType spawnType,
            BlockPos pos,
            RandomSource random
    ) {
        return AbstractBirdEntity.canSpawn(
                entityType,
                level,
                spawnType,
                pos,
                random,
                NeoGuanNiaoBirdData.NIGHT_HERON.get()
        );
    }

    @Override
    protected List<Goal> buildGoals() {
        return new ArrayList<>(super.buildGoals());
    }

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

    public ItemStack getHeldFishForRendering() {
        return this.getEntityData().get(HELD_FISH);
    }

    public boolean hasHeldFishForRendering() {
        return !this.getHeldFishForRendering().isEmpty();
    }




}
