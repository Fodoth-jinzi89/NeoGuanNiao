package com.birdcamera;

import com.birdcamera.content.bird.core.data.BirdData;
import com.birdcamera.content.bird.core.model.BirdModel;
import com.birdcamera.content.bird.core.skin.BirdSkin;
import com.birdcamera.command.PhotoAdminCommands;
import com.birdcamera.event.BirdCameraEvents;
import com.birdcamera.event.PhotoTransferEvents;
import com.birdcamera.network.BirdCameraNetworking;
import com.birdcamera.registry.*;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BirdCameraMod implements ModInitializer {
    public static final String MOD_ID = "birdcamera";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("观鸟相机模组正在初始化...");

        // 注册所有内容
        BirdCameraBlocks.register();
        BirdCameraItems.register();
        BirdCameraEntityTypes.register();
        BirdCameraBlockEntityTypes.register();
        BirdCameraSoundEvents.register();
        BirdCameraCreativeTabs.register();
        BirdCameraDataComponents.register();
        BirdCameraRecipeSerializers.register();
        BirdCameraVillagerProfessions.register();
        BirdCameraCriteriaTriggers.register();
        registerBirdSkinsAndModels();

        // 注册实体属性
        BirdCameraEvents.registerEntityAttributes();

        // 注册生成规则
        BirdCameraEvents.registerSpawnRestrictions();

        // 注册生物群系生成
        BirdCameraEvents.registerBiomeSpawns();

        // 注册村民交易
        BirdCameraEvents.registerVillagerTrades();

        // 注册服务端游戏事件（望远镜观察夜鹭成就等）
        BirdCameraEvents.registerGameEvents();

        // 相机系统：网络注册、服务端 tick、命令
        BirdCameraNetworking.register();
        PhotoTransferEvents.register();
        PhotoAdminCommands.register();

        LOGGER.info("观鸟相机模组初始化完成！");
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    private static void registerBirdSkinsAndModels() {
        for (BirdData data : BirdCameraBirdData.VIEW.values()) {
            for (BirdSkin skin : data.model().birdSkin()) {
                BirdCameraBirdSkins.register(skin);
            }
            for (BirdModel model : data.model().birdModel()) {
                BirdCameraBirdModels.register(model);
            }
        }
    }
}