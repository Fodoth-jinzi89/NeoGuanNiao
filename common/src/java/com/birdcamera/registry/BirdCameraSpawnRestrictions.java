package com.birdcamera.registry;

import com.birdcamera.BirdCameraMod;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.util.RandomSource;
import net.minecraft.core.BlockPos;

public class BirdCameraSpawnRestrictions {

    public static void register() {
        BirdCameraMod.LOGGER.info("注册生成规则...");

        // 虎皮鹦鹉 - 地面生成
        registerBirdSpawn(BirdCameraEntityTypes.NEO_BUDGERIGAR);

        // 夜鹭 - 地面生成
        registerBirdSpawn(BirdCameraEntityTypes.NEO_NIGHT_HERON);

        // 斑鸠 - 地面生成
        registerBirdSpawn(BirdCameraEntityTypes.NEO_PIGEON);

        // 鸽子 - 地面生成
        registerBirdSpawn(BirdCameraEntityTypes.NEO_DOVE);

        // 麻雀 - 地面生成
        registerBirdSpawn(BirdCameraEntityTypes.NEO_SPARROW);

        // 玄凤鹦鹉 - 地面生成
        registerBirdSpawn(BirdCameraEntityTypes.NEO_COCKATIEL);

        // 长尾山雀 - 地面生成
        registerBirdSpawn(BirdCameraEntityTypes.NEO_LONG_TAILED_TIT);

        // 金刚鹦鹉 - 地面生成
        registerBirdSpawn(BirdCameraEntityTypes.NEO_MACAW);

        // 乌鸦 - 地面生成
        registerBirdSpawn(BirdCameraEntityTypes.NEO_CROW);

        // 海鸥 - 地面生成
        registerBirdSpawn(BirdCameraEntityTypes.NEO_SEAGULL);
    }

    private static void registerBirdSpawn(EntityType<?> entityType) {
        SpawnPlacements.register(
                (EntityType<? extends Mob>) entityType,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                BirdCameraSpawnRestrictions::canBirdSpawn
        );
    }

    /**
     * 鸟类生成条件检查
     */
    public static boolean canBirdSpawn(
            EntityType<? extends Mob> type,
            LevelAccessor world,
            MobSpawnType spawnReason,
            BlockPos pos,
            RandomSource random) {
        return world.getMaxLocalRawBrightness(pos) > 6;
    }
}
