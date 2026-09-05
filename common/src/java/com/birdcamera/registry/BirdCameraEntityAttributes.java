package com.birdcamera.registry;

import com.birdcamera.BirdCameraMod;
import com.birdcamera.content.bird.impl.BudgerigarEntity;
import com.birdcamera.content.bird.impl.CockatielEntity;
import com.birdcamera.content.bird.impl.CrowEntity;
import com.birdcamera.content.bird.impl.DoveEntity;
import com.birdcamera.content.bird.impl.LongTailedTitEntity;
import com.birdcamera.content.bird.impl.MacawEntity;
import com.birdcamera.content.bird.impl.NightHeronEntity;
import com.birdcamera.content.bird.impl.PigeonEntity;
import com.birdcamera.content.bird.impl.SeagullEntity;
import com.birdcamera.content.bird.impl.SparrowEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.world.entity.Mob;

public class BirdCameraEntityAttributes {

    public static void register() {
        BirdCameraMod.LOGGER.info("注册实体属性...");

        // 注册所有鸟类实体属性
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
}
