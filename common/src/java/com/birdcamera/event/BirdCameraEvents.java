package com.birdcamera.event;

import com.birdcamera.BirdCameraMod;
import com.birdcamera.content.bath.BirdBathAttraction;
import com.birdcamera.content.bird.impl.*;
import com.birdcamera.content.villager.trade.BirdFeatherTrade;
import com.birdcamera.content.villager.trade.BirdBagTrade;
import com.birdcamera.registry.BirdCameraEntityTypes;
import com.birdcamera.registry.BirdCameraVillagerProfessions;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * 事件处理器 - 注册实体属性、生成规则和村民交易
 * 使用Fabric API回调替代NeoForge事件系统
 */
public class BirdCameraEvents {

    /**
     * 注册实体属性
     */
    public static void registerEntityAttributes() {
        BirdCameraMod.LOGGER.info("正在注册鸟类实体属性...");

        FabricDefaultAttributeRegistry.register(BirdCameraEntityTypes.NEO_BUDGERIGAR, BudgerigarEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(BirdCameraEntityTypes.NEO_NIGHT_HERON, NightHeronEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(BirdCameraEntityTypes.NEO_PIGEON, PigeonEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(BirdCameraEntityTypes.NEO_DOVE, DoveEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(BirdCameraEntityTypes.NEO_SPARROW, SparrowEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(BirdCameraEntityTypes.NEO_COCKATIEL, CockatielEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(BirdCameraEntityTypes.NEO_LONG_TAILED_TIT, LongTailedTitEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(BirdCameraEntityTypes.NEO_MACAW, MacawEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(BirdCameraEntityTypes.NEO_CROW, CrowEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(BirdCameraEntityTypes.NEO_SEAGULL, SeagullEntity.createAttributes().build());
    }

    /**
     * 注册生成规则 - 使用与原版一致的各鸟类专属 canSpawn（替代原版的 Operation.REPLACE）
     */
    public static void registerSpawnRestrictions() {
        BirdCameraMod.LOGGER.info("正在注册鸟类生成限制...");

        SpawnPlacements.register(BirdCameraEntityTypes.NEO_BUDGERIGAR, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, BudgerigarEntity::canSpawn);
        SpawnPlacements.register(BirdCameraEntityTypes.NEO_NIGHT_HERON, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, NightHeronEntity::canSpawn);
        SpawnPlacements.register(BirdCameraEntityTypes.NEO_PIGEON, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, PigeonEntity::canSpawn);
        SpawnPlacements.register(BirdCameraEntityTypes.NEO_DOVE, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, DoveEntity::canSpawn);
        SpawnPlacements.register(BirdCameraEntityTypes.NEO_SPARROW, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SparrowEntity::canSpawn);
        SpawnPlacements.register(BirdCameraEntityTypes.NEO_COCKATIEL, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CockatielEntity::canSpawn);
        SpawnPlacements.register(BirdCameraEntityTypes.NEO_LONG_TAILED_TIT, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, LongTailedTitEntity::canSpawn);
        SpawnPlacements.register(BirdCameraEntityTypes.NEO_MACAW, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, MacawEntity::canSpawn);
        SpawnPlacements.register(BirdCameraEntityTypes.NEO_CROW, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CrowEntity::canSpawn);
        SpawnPlacements.register(BirdCameraEntityTypes.NEO_SEAGULL, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SeagullEntity::canSpawn);
    }

    /**
     * 注册生物群系生成（替代 NeoForge biome_modifier JSON）
     */
    public static void registerBiomeSpawns() {
        BirdCameraMod.LOGGER.info("正在注册鸟类生物群系生成...");

        // 虎皮鹦鹉 - 热带/草原（数量下调，与原版 2.9.1 平衡一致）
        addSpawn("budgerigar_habitat", BirdCameraEntityTypes.NEO_BUDGERIGAR, 3, 1, 3);
        // 玄凤鹦鹉
        addSpawn("cockatiel_habitat", BirdCameraEntityTypes.NEO_COCKATIEL, 3, 1, 2);
        // 乌鸦
        addSpawn("crow_habitat", BirdCameraEntityTypes.NEO_CROW, 3, 1, 2);
        // 斑鸠
        addSpawn("spotted_dove_habitat", BirdCameraEntityTypes.NEO_DOVE, 3, 1, 2);
        // 长尾山雀
        addSpawn("long_tailed_tit_habitat", BirdCameraEntityTypes.NEO_LONG_TAILED_TIT, 5, 2, 4);
        // 金刚鹦鹉
        addSpawn("macaw_habitat", BirdCameraEntityTypes.NEO_MACAW, 2, 1, 2);
        // 夜鹭
        addSpawn("night_heron_habitat", BirdCameraEntityTypes.NEO_NIGHT_HERON, 3, 1, 1);
        // 鸽子
        addSpawn("pigeon_habitat", BirdCameraEntityTypes.NEO_PIGEON, 3, 1, 3);
        // 海鸥
        addSpawn("seagull_habitat", BirdCameraEntityTypes.NEO_SEAGULL, 4, 1, 3);
        // 麻雀
        addSpawn("sparrow_habitat", BirdCameraEntityTypes.NEO_SPARROW, 3, 2, 4);
    }

    @SuppressWarnings("unchecked")
    private static void addSpawn(String habitatTag, net.minecraft.world.entity.EntityType<?> type, int weight, int min, int max) {
        TagKey<net.minecraft.world.level.biome.Biome> tag = TagKey.create(Registries.BIOME, BirdCameraMod.id(habitatTag));
        BiomeModifications.addSpawn(
                BiomeSelectors.tag(tag),
                MobCategory.CREATURE,
                (net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.Mob>) type,
                weight, min, max
        );
    }

    /**
     * 注册村民交易 - 与原版一致：每个等级都加入 羽毛收购 和 鸟食袋交易
     */
    public static void registerVillagerTrades() {
        BirdCameraMod.LOGGER.info("正在注册观鸟村民交易...");

        for (int level = 1; level <= 5; level++) {
            int lvl = level;
            TradeOfferHelper.registerVillagerOffers(
                BirdCameraVillagerProfessions.BIRD_KEEPER,
                lvl,
                factories -> {
                    factories.add(new BirdFeatherTrade());
                    factories.add(new BirdBagTrade());
                }
            );
        }
    }

    /**
     * 注册服务端游戏事件（望远镜观察夜鹭成就触发等），对应原版 SpyglassTickHandler
     */
    public static void registerGameEvents() {
        BirdCameraMod.LOGGER.info("正在注册观鸟游戏事件...");

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (net.minecraft.server.level.ServerPlayer player : server.getPlayerList().getPlayers()) {
                SpyglassTickHandler.tick(player);
            }
        });

        // 世界卸载时清除浴盆索引，防止内存泄漏（对应原版 BirdBathIndexEvents）
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents.UNLOAD.register((server, world) -> {
            BirdBathAttraction.clearLevel(world);
        });
    }
}