package com.birdcamera.registry;

import com.birdcamera.BirdCameraMod;
import com.birdcamera.content.bird.core.data.BirdData;
import com.birdcamera.content.bird.core.data.datum.BirdModelSkinDatum;
import com.birdcamera.content.bird.core.model.BirdModel;
import com.birdcamera.content.bird.core.skin.BirdSkin;
import com.birdcamera.content.egg.BirdEggData;
import com.birdcamera.content.egg.BirdEggItem;
import com.birdcamera.content.feather.BirdFeatherData;
import com.birdcamera.content.feather.BirdFeatherItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BirdCameraCreativeTabs {

    public static final ResourceKey<CreativeModeTab> BIRD_CAMERA_MAIN =
            ResourceKey.create(Registries.CREATIVE_MODE_TAB, BirdCameraMod.id("main"));

    public static final ResourceKey<CreativeModeTab> BIRD_CAMERA_EGGS =
            ResourceKey.create(Registries.CREATIVE_MODE_TAB, BirdCameraMod.id("eggs"));

    public static final ResourceKey<CreativeModeTab> BIRD_CAMERA_FEATHERS =
            ResourceKey.create(Registries.CREATIVE_MODE_TAB, BirdCameraMod.id("feathers"));

    private static CreativeModeTab register(ResourceKey<CreativeModeTab> key, CreativeModeTab group) {
        return Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, key, group);
    }

    public static void register() {
        BirdCameraMod.LOGGER.info("注册创造模式标签页...");

        // 主标签页 - 包含所有主要物品
        register(BIRD_CAMERA_MAIN, CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                .title(Component.translatable("itemGroup.birdcamera.main"))
                .icon(() -> new ItemStack(BirdCameraItems.BIRD_GUIDE))
                .build());

        // 鸟蛋标签页
        register(BIRD_CAMERA_EGGS, CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                .title(Component.translatable("itemGroup.birdcamera.eggs"))
                .icon(() -> new ItemStack(BirdCameraItems.BIRD_EGG))
                .build());

        // 鸟羽毛标签页
        register(BIRD_CAMERA_FEATHERS, CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                .title(Component.translatable("itemGroup.birdcamera.feathers"))
                .icon(() -> new ItemStack(BirdCameraItems.BIRD_FEATHER))
                .build());

        // 向主标签页添加物品
        ItemGroupEvents.modifyEntriesEvent(BIRD_CAMERA_MAIN).register(entries -> {
            entries.accept(BirdCameraItems.BIRD_GUIDE);
            entries.accept(BirdCameraItems.BREADCRUMBS);
            entries.accept(BirdCameraItems.SMALL_BIRD_CAGE);
            entries.accept(BirdCameraItems.MEDIUM_BIRD_CAGE);
            entries.accept(BirdCameraItems.LARGE_BIRD_CAGE);
            entries.accept(BirdCameraItems.WOODEN_BIRD_BATH);
            entries.accept(BirdCameraItems.STONE_BIRD_BATH);
            entries.accept(BirdCameraItems.BIRD_BATH);
            entries.accept(BirdCameraItems.WOODEN_BIRD_BATH_2);
            entries.accept(BirdCameraItems.STONE_BIRD_BATH_2);
            entries.accept(BirdCameraItems.BIRD_BATH_2);
            entries.accept(BirdCameraItems.BIRD_NEST);
            entries.accept(BirdCameraItems.BIRD_FOOD_BAG);
            entries.accept(BirdCameraItems.BIRD_FOOD_BAG_SEED);
            entries.accept(BirdCameraItems.BIRD_FOOD_BAG_FISH);
            entries.accept(BirdCameraItems.GREEN_FOOD_BAG);
            entries.accept(BirdCameraItems.GREEN_FOOD_BAG_GROWTH);
            entries.accept(BirdCameraItems.GREEN_FOOD_BAG_REJUVENATE);
            entries.accept(BirdCameraItems.GREEN_FOOD_BAG_STOP);
            entries.accept(BirdCameraItems.GREEN_FOOD_BAG_TRANSMUTE);
            entries.accept(BirdCameraItems.GREEN_FOOD_BAG_FEATHER_ADD);
            entries.accept(BirdCameraItems.GREEN_FOOD_BAG_FEATHER_MINUS);
            entries.accept(BirdCameraItems.GREEN_FOOD_BAG_FEATHER_FAST);
            entries.accept(BirdCameraItems.GREEN_FOOD_BAG_FEATHER_SLOW);
            entries.accept(BirdCameraItems.GREEN_FOOD_BAG_SIZE_UP);
            entries.accept(BirdCameraItems.GREEN_FOOD_BAG_SIZE_DOWN);
            entries.accept(BirdCameraItems.GOLDEN_FOOD_BAG);
            entries.accept(BirdCameraItems.GOLDEN_FOOD_BAG_UPGRADE);
            entries.accept(BirdCameraItems.GOLDEN_FOOD_BAG_DOWNGRADE);
            entries.accept(BirdCameraItems.GOLDEN_FOOD_BAG_UNIQUE);
            entries.accept(BirdCameraItems.GOLDEN_FOOD_BAG_EGG_ADD);
            entries.accept(BirdCameraItems.GOLDEN_FOOD_BAG_EGG_MINUS);
            // 相机系统物品（迁移自 guaniao-2.1.3）
            entries.accept(BirdCameraItems.NIKON_D750);
            entries.accept(BirdCameraItems.FILM);
            entries.accept(BirdCameraItems.PHOTOGRAPH);
        });

        // 鸟蛋标签页：生成所有鸟类的蛋变体（与原版 NeoGuanNiaoCreativeTabs.generateBirdEggs 一致）
        ItemGroupEvents.modifyEntriesEvent(BIRD_CAMERA_EGGS).register(entries -> {
            generateBirdEggs(entries);
        });

        // 鸟羽毛标签页：生成所有鸟类的羽毛变体（与原版 generateBirdFeathers 一致）
        ItemGroupEvents.modifyEntriesEvent(BIRD_CAMERA_FEATHERS).register(entries -> {
            generateBirdFeathers(entries);
        });
    }

    /**
     * 生成所有鸟类的蛋变体（鸟种 × 模型 × 皮肤 × 性别），与原版逻辑完全一致
     */
    private static void generateBirdEggs(CreativeModeTab.Output output) {
        for (Map.Entry<ResourceLocation, BirdData> holder : BirdCameraBirdData.VIEW.entrySet()) {
            BirdModelSkinDatum modelDatum = holder.getValue().model();

            // 遍历所有模型
            for (BirdModel model : modelDatum.birdModel()) {
                // 遍历所有皮肤
                for (BirdSkin skin : modelDatum.birdSkin()) {
                    // 检查皮肤是否被当前模型支持
                    if (!model.supportsSkin(skin.id())) continue;

                    for (boolean gender : new boolean[]{true, false}) {
                        // 如果皮肤是雄性，生成雄性蛋；如果是雌性，生成雌性蛋；如果是通用皮肤，则生成两种
                        if ((skin.male() && gender) || (skin.female() && !gender)) {
                            ItemStack egg = new ItemStack(BirdCameraItems.BIRD_EGG);
                            BirdEggItem.setEggData(egg, BirdEggData.create(
                                    holder.getKey(),
                                    gender,
                                    model.id(),
                                    skin.id(),
                                    2,
                                    3,
                                    24000,
                                    1.0F,
                                    20,
                                    true
                            ));
                            output.accept(egg);
                        }
                    }
                }
            }
        }
    }

    /**
     * 生成所有鸟类的羽毛变体（每种鸟类按稀有度去重），与原版逻辑完全一致
     */
    private static void generateBirdFeathers(CreativeModeTab.Output output) {
        for (Map.Entry<ResourceLocation, BirdData> holder : BirdCameraBirdData.VIEW.entrySet()) {
            BirdModelSkinDatum modelDatum = holder.getValue().model();

            // 用于记录当前鸟类已经生成过的稀有度
            Set<Integer> generatedRarities = new HashSet<>();

            // 遍历所有皮肤
            for (BirdSkin skin : modelDatum.birdSkin()) {
                int rarityValue = skin.rarity().getRarity();

                // 如果这个稀有度已经生成过了，跳过
                if (generatedRarities.contains(rarityValue)) {
                    continue;
                }

                // 记录这个稀有度
                generatedRarities.add(rarityValue);

                // 生成羽毛
                ItemStack feather = new ItemStack(BirdCameraItems.BIRD_FEATHER);
                BirdFeatherItem.setFeatherData(feather, BirdFeatherData.create(
                        holder.getKey(),
                        rarityValue
                ));
                output.accept(feather);
            }
        }
    }
}