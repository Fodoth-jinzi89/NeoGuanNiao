package com.birdcamera.registry;

import com.birdcamera.BirdCameraMod;
import com.birdcamera.content.bath.BirdBathItem;
import com.birdcamera.content.bath.BirdBathVariant;
import com.birdcamera.content.cage.BirdCageItem;
import com.birdcamera.content.cage.BirdCageVariant;
import com.birdcamera.content.camera.FilmItem;
import com.birdcamera.content.camera.NikonD750Item;
import com.birdcamera.content.camera.PhotographItem;
import com.birdcamera.content.egg.BirdEggItem;
import com.birdcamera.content.feather.BirdFeatherItem;
import com.birdcamera.content.feed.*;
import com.birdcamera.content.guide.BirdGuideItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;

public class BirdCameraItems {

    // 面包屑
    public static final Item BREADCRUMBS = register("breadcrumbs",
            new BreadcrumbItem(new Item.Properties()));

    // 鸟类图鉴
    public static final Item BIRD_GUIDE = register("bird_guide",
            new BirdGuideItem(new Item.Properties().stacksTo(1)));

    // ===== 鸟笼方块物品 =====
    public static final Item SMALL_BIRD_CAGE = register("small_bird_cage",
            new BirdCageItem(BirdCageVariant.SMALL, BirdCameraBlocks.SMALL_BIRD_CAGE, new Item.Properties()));
    public static final Item MEDIUM_BIRD_CAGE = register("medium_bird_cage",
            new BirdCageItem(BirdCageVariant.MEDIUM, BirdCameraBlocks.MEDIUM_BIRD_CAGE, new Item.Properties()));
    public static final Item LARGE_BIRD_CAGE = register("large_bird_cage",
            new BirdCageItem(BirdCageVariant.LARGE, BirdCameraBlocks.LARGE_BIRD_CAGE, new Item.Properties()));

    // ===== 鸟盆方块物品 =====
    public static final Item WOODEN_BIRD_BATH = register("wooden_bird_bath",
            new BirdBathItem(BirdBathVariant.WOODEN_BIRD_BATH, BirdCameraBlocks.WOODEN_BIRD_BATH, new Item.Properties()));
    public static final Item STONE_BIRD_BATH = register("stone_bird_bath",
            new BirdBathItem(BirdBathVariant.STONE_BIRD_BATH, BirdCameraBlocks.STONE_BIRD_BATH, new Item.Properties()));
    public static final Item BIRD_BATH = register("bird_bath",
            new BirdBathItem(BirdBathVariant.BIRD_BATH, BirdCameraBlocks.BIRD_BATH, new Item.Properties()));
    public static final Item WOODEN_BIRD_BATH_2 = register("wooden_bird_bath_2",
            new BirdBathItem(BirdBathVariant.WOODEN_BIRD_BATH_2, BirdCameraBlocks.WOODEN_BIRD_BATH_2, new Item.Properties()));
    public static final Item STONE_BIRD_BATH_2 = register("stone_bird_bath_2",
            new BirdBathItem(BirdBathVariant.STONE_BIRD_BATH_2, BirdCameraBlocks.STONE_BIRD_BATH_2, new Item.Properties()));
    public static final Item BIRD_BATH_2 = register("bird_bath_2",
            new BirdBathItem(BirdBathVariant.BIRD_BATH_2, BirdCameraBlocks.BIRD_BATH_2, new Item.Properties()));

    // 鸟巢方块物品（GeoItem，支持 Geo 渲染）
    public static final Item BIRD_NEST = register("bird_nest",
            new com.birdcamera.content.nest.BirdNestItem(BirdCameraBlocks.BIRD_NEST, new Item.Properties()));

    // 鸟蛋
    public static final Item BIRD_EGG = register("bird_egg",
            new BirdEggItem(new Item.Properties()));

    // 鸟羽毛
    public static final Item BIRD_FEATHER = register("bird_feather",
            new BirdFeatherItem(new Item.Properties()));

    // ===== 相机系统（迁移自 guaniao-2.1.3）=====
    // 尼康 D750 相机
    public static final Item NIKON_D750 = register("nikon_d750",
            new NikonD750Item(new Item.Properties().stacksTo(1)));
    // 胶片（拍摄成果）
    public static final Item FILM = register("film",
            new FilmItem(new Item.Properties().stacksTo(1)));
    // 相框相片
    public static final Item PHOTOGRAPH = register("photograph",
            new PhotographItem(new Item.Properties().stacksTo(16)));

    // ===== 鸟食包 =====
    public static final Item BIRD_FOOD_BAG = register("bird_food_bag",
            new BirdFoodBagItem(new Item.Properties()));
    public static final Item BIRD_FOOD_BAG_SEED = register("bird_food_bag_seed",
            new BirdFoodBagSeedItem(new Item.Properties()));
    public static final Item BIRD_FOOD_BAG_FISH = register("bird_food_bag_fish",
            new BirdFoodBagFishItem(new Item.Properties()));

    // ===== 绿色食物袋（与资源模型/原版注册对齐） =====
    public static final Item GREEN_FOOD_BAG = register("green_food_bag",
            new GreenFoodBagItem(new Item.Properties()));
    public static final Item GREEN_FOOD_BAG_GROWTH = register("green_food_bag_growth",
            new GreenFoodBagGrowthItem(new Item.Properties()));
    public static final Item GREEN_FOOD_BAG_REJUVENATE = register("green_food_bag_rejuvenate",
            new GreenFoodBagRejuvenateItem(new Item.Properties()));
    public static final Item GREEN_FOOD_BAG_STOP = register("green_food_bag_stop",
            new GreenFoodBagStopItem(new Item.Properties()));
    public static final Item GREEN_FOOD_BAG_TRANSMUTE = register("green_food_bag_transmute",
            new GreenFoodBagTransmuteItem(new Item.Properties()));
    public static final Item GREEN_FOOD_BAG_FEATHER_ADD = register("green_food_bag_feather_add",
            new GreenFoodBagFeatherAddItem(new Item.Properties()));
    public static final Item GREEN_FOOD_BAG_FEATHER_MINUS = register("green_food_bag_feather_minus",
            new GreenFoodBagFeatherMinusItem(new Item.Properties()));
    public static final Item GREEN_FOOD_BAG_FEATHER_FAST = register("green_food_bag_feather_fast",
            new GreenFoodBagFeatherFastItem(new Item.Properties()));
    public static final Item GREEN_FOOD_BAG_FEATHER_SLOW = register("green_food_bag_feather_slow",
            new GreenFoodBagFeatherSlowItem(new Item.Properties()));
    public static final Item GREEN_FOOD_BAG_SIZE_UP = register("green_food_bag_size_up",
            new GreenFoodBagSizeUpItem(new Item.Properties()));
    public static final Item GREEN_FOOD_BAG_SIZE_DOWN = register("green_food_bag_size_down",
            new GreenFoodBagSizeDownItem(new Item.Properties()));

    // ===== 金色食物袋 =====
    public static final Item GOLDEN_FOOD_BAG = register("golden_food_bag",
            new GoldenFoodBagItem(new Item.Properties()));
    public static final Item GOLDEN_FOOD_BAG_UPGRADE = register("golden_food_bag_upgrade",
            new GoldenFoodBagUpgradeItem(new Item.Properties()));
    public static final Item GOLDEN_FOOD_BAG_DOWNGRADE = register("golden_food_bag_downgrade",
            new GoldenFoodBagDowngradeItem(new Item.Properties()));
    public static final Item GOLDEN_FOOD_BAG_UNIQUE = register("golden_food_bag_unique",
            new GoldenFoodBagUniqueItem(new Item.Properties()));
    public static final Item GOLDEN_FOOD_BAG_EGG_ADD = register("golden_food_bag_egg_add",
            new GoldenFoodBagEggAddItem(new Item.Properties()));
    public static final Item GOLDEN_FOOD_BAG_EGG_MINUS = register("golden_food_bag_egg_minus",
            new GoldenFoodBagEggMinusItem(new Item.Properties()));

    private static Item register(String id, Item item) {
        return Registry.register(BuiltInRegistries.ITEM, BirdCameraMod.id(id), item);
    }

    public static void register() {
        BirdCameraMod.LOGGER.info("注册物品...");
    }
}
