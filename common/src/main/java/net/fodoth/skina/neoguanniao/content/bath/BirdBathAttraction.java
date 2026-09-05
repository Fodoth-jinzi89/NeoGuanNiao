package net.fodoth.skina.neoguanniao.content.bath;

import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;


public final class BirdBathAttraction {

    private static final double TOP_USE_Y_OFFSET = 1.42;
    private static final double TOP_STAND_Y_OFFSET = 1.52;
    private static final double EDGE_APPROACH_DISTANCE = 1.35;
    private static final Map<Level, Map<Long, Set<BirdBathBlockEntity>>> LOADED_BATHS =
            new IdentityHashMap<>();


    private BirdBathAttraction() {
    }


    public static Optional<BirdBathBlockEntity> findNearbyUsableBath(
            Level level,
            BlockPos origin,
            double radius,
            Predicate<BirdBathBlockEntity> predicate
    ) {

        Map<Long, Set<BirdBathBlockEntity>> levelBaths = LOADED_BATHS.get(level);
        if (levelBaths == null || levelBaths.isEmpty()) {
            return Optional.empty();
        }

        int range = (int) Math.ceil(radius);
        int verticalRange = Math.min(4, range);
        double bestDistance = radius * radius;
        BirdBathBlockEntity best = null;

        int minChunkX = (origin.getX() - range) >> 4;
        int maxChunkX = (origin.getX() + range) >> 4;
        int minChunkZ = (origin.getZ() - range) >> 4;
        int maxChunkZ = (origin.getZ() + range) >> 4;

        for (int chunkX = minChunkX; chunkX <= maxChunkX; ++chunkX) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; ++chunkZ) {
                long chunkKey = ChunkPos.asLong(chunkX, chunkZ);
                Set<BirdBathBlockEntity> baths = levelBaths.get(chunkKey);
                if (baths == null) {
                    continue;
                }

                Iterator<BirdBathBlockEntity> iterator = baths.iterator();
                while (iterator.hasNext()) {
                    BirdBathBlockEntity birdBath = iterator.next();
                    if (birdBath.isRemoved() || birdBath.getLevel() != level) {
                        iterator.remove();
                        continue;
                    }

                    BlockPos pos = birdBath.getBlockPos();
                    if (Math.abs(pos.getY() - origin.getY()) > verticalRange) {
                        continue;
                    }

                    double distance = pos.distSqr(origin);
                    if (distance <= bestDistance && predicate.test(birdBath)) {
                        best = birdBath;
                        bestDistance = distance;
                    }
                }

                if (baths.isEmpty()) {
                    levelBaths.remove(chunkKey);
                }
            }
        }

        return Optional.ofNullable(best);
    }


    static void registerBath(BirdBathBlockEntity birdBath) {
        Level level = birdBath.getLevel();
        if (level == null || level.isClientSide()) {
            return;
        }

        long chunkKey = ChunkPos.asLong(
                birdBath.getBlockPos().getX() >> 4,
                birdBath.getBlockPos().getZ() >> 4
        );
        LOADED_BATHS
                .computeIfAbsent(level, ignored -> new HashMap<>())
                .computeIfAbsent(chunkKey, ignored ->
                        Collections.newSetFromMap(new IdentityHashMap<>()))
                .add(birdBath);
    }


    static void unregisterBath(BirdBathBlockEntity birdBath) {
        Level level = birdBath.getLevel();
        if (level == null || level.isClientSide()) {
            return;
        }

        Map<Long, Set<BirdBathBlockEntity>> levelBaths = LOADED_BATHS.get(level);
        if (levelBaths == null) {
            return;
        }

        long chunkKey = ChunkPos.asLong(
                birdBath.getBlockPos().getX() >> 4,
                birdBath.getBlockPos().getZ() >> 4
        );
        Set<BirdBathBlockEntity> baths = levelBaths.get(chunkKey);
        if (baths != null) {
            baths.remove(birdBath);
            if (baths.isEmpty()) {
                levelBaths.remove(chunkKey);
            }
        }
        if (levelBaths.isEmpty()) {
            LOADED_BATHS.remove(level);
        }
    }


    public static void clearLevel(Level level) {
        if (level.isClientSide()) {
            return;
        }
        LOADED_BATHS.remove(level);
    }


    public static boolean isAttractiveToNightHeron(BirdBathBlockEntity bath) {
        return bath != null
                && (bath.hasFoodForBird(BirdBathFoodPreference.FISH)
                || bath.hasUsableWater());
    }


    public static boolean isAttractiveToSmallSeedBird(BirdBathBlockEntity bath) {
        return bath != null
                && (bath.hasFoodForBird(BirdBathFoodPreference.BREAD)
                || bath.hasUsableWater());
    }


    public static boolean isAttractiveToColumbid(BirdBathBlockEntity bath) {
        return bath != null
                && (bath.hasFoodForBird(BirdBathFoodPreference.BREAD)
                || bath.hasUsableWater());
    }


    public static boolean isAttractiveToBudgerigar(BirdBathBlockEntity bath) {
        return bath != null
                && (bath.hasFoodForBird(BirdBathFoodPreference.BREAD)
                || bath.hasUsableWater());
    }


    public static boolean isAttractiveToWaterBird(BirdBathBlockEntity bath) {
        return bath != null && bath.hasUsableWater();
    }


    public static Vec3 topUsePosition(BirdBathBlockEntity bath) {

        BlockPos pos = bath.getBlockPos();

        return new Vec3(
                pos.getX() + 0.5,
                pos.getY() + TOP_USE_Y_OFFSET,
                pos.getZ() + 0.5
        );
    }


    public static Vec3 topStandPosition(BirdBathBlockEntity bath) {

        BlockPos pos = bath.getBlockPos();

        return new Vec3(
                pos.getX() + 0.5,
                pos.getY() + TOP_STAND_Y_OFFSET,
                pos.getZ() + 0.5
        );
    }


    public static Vec3 edgeStandPosition(
            BirdBathBlockEntity bath,
            Vec3 birdPosition
    ) {

        Vec3 center = topUsePosition(bath);

        Vec3 horizontal = new Vec3(
                birdPosition.x - center.x,
                0,
                birdPosition.z - center.z
        );

        if (horizontal.lengthSqr() <= 1.0E-4) {
            horizontal = new Vec3(1, 0, 0);
        } else {
            horizontal = horizontal.normalize();
        }


        return new Vec3(
                center.x + horizontal.x * 0.5,
                center.y + 0.20,
                center.z + horizontal.z * 0.5
        );
    }


    public static Vec3 edgeApproachPosition(
            BirdBathBlockEntity bath,
            Vec3 birdPosition
    ) {

        Vec3 center = topUsePosition(bath);

        Vec3 horizontal = new Vec3(
                birdPosition.x - center.x,
                0,
                birdPosition.z - center.z
        );

        if (horizontal.lengthSqr() <= 1.0E-4) {
            horizontal = new Vec3(1, 0, 0);
        } else {
            horizontal = horizontal.normalize();
        }

        BlockPos pos = bath.getBlockPos();

        return new Vec3(
                center.x + horizontal.x * EDGE_APPROACH_DISTANCE,
                pos.getY() + 0.05,
                center.z + horizontal.z * EDGE_APPROACH_DISTANCE
        );
    }


    public static boolean consumeServingForBird(BirdBathBlockEntity bath) {
        return bath != null && bath.consumeOneServing();
    }


    public static boolean tryClaimUse(
            BirdBathBlockEntity bath,
            Entity bird,
            int ticks
    ) {

        if (bath != null && bird != null) {
            UUID uuid = bird.getUUID();
            return bath.tryClaimUse(uuid, ticks);
        }

        return false;
    }


    public static int getCompetitionPriority(Entity bird) {

        if (bird == null) {
            return 0;
        }

        String key = BuiltInRegistries.ENTITY_TYPE
                .getKey(bird.getType())
                .toString();


        if (key.contains("night_heron")) {
            return 4;

        } else if (key.contains("pigeon")
                || key.contains("dove")
                || key.contains("collared")) {

            return 3;

        } else if (key.contains("budgerigar")
                || key.contains("parakeet")) {

            return 2;

        } else if (key.contains("sparrow")) {

            return 1;
        }

        return 1;
    }
}
