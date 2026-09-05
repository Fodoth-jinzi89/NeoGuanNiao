package net.fodoth.skina.neoguanniao.registry;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.content.bath.BirdBathItem;
import net.fodoth.skina.neoguanniao.content.bath.BirdBathVariant;
import net.fodoth.skina.neoguanniao.content.cage.BirdCageItem;
import net.fodoth.skina.neoguanniao.content.cage.BirdCageVariant;
import net.fodoth.skina.neoguanniao.content.camera.FilmItem;
import net.fodoth.skina.neoguanniao.content.camera.NikonD750Item;
import net.fodoth.skina.neoguanniao.content.camera.PhotographItem;
import net.fodoth.skina.neoguanniao.content.egg.BirdEggItem;
import net.fodoth.skina.neoguanniao.content.feather.BirdFeatherItem;
import net.fodoth.skina.neoguanniao.content.fan.FeatherFanItem;
import net.fodoth.skina.neoguanniao.content.feed.*;
import net.fodoth.skina.neoguanniao.content.guide.BirdGuideItem;
import net.fodoth.skina.neoguanniao.content.nest.BirdNestItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.architectury.registry.registries.Registrar;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.function.Supplier;

public final class NeoGuanNiaoItems {

    public static final Registrar<Item> ITEMS = NeoGuanNiaoRegistrar.MANAGER.get(BuiltInRegistries.ITEM);

    public static final RegistrySupplier<Item> BREADCRUMBS;
    public static final RegistrySupplier<Item> BIRD_GUIDE;
    public static final RegistrySupplier<Item> NIKON_D750;
    public static final RegistrySupplier<Item> FILM;
    public static final RegistrySupplier<Item> PHOTOGRAPH;

    public static final RegistrySupplier<Item> SMALL_BIRD_CAGE;
    public static final RegistrySupplier<Item> MEDIUM_BIRD_CAGE;
    public static final RegistrySupplier<Item> LARGE_BIRD_CAGE;

    public static final RegistrySupplier<Item> WOODEN_BIRD_BATH;
    public static final RegistrySupplier<Item> STONE_BIRD_BATH;
    public static final RegistrySupplier<Item> BIRD_BATH;
    public static final RegistrySupplier<Item> WOODEN_BIRD_BATH_2;
    public static final RegistrySupplier<Item> STONE_BIRD_BATH_2;
    public static final RegistrySupplier<Item> BIRD_BATH_2;

    public static final RegistrySupplier<Item> BIRD_EGG;
    public static final RegistrySupplier<Item> BIRD_FEATHER;
    public static final RegistrySupplier<Item> BIRD_FOOD_BAG;
    public static final RegistrySupplier<Item> BIRD_FOOD_BAG_SEED;
    public static final RegistrySupplier<Item> BIRD_FOOD_BAG_FISH;

    public static final RegistrySupplier<Item> GREEN_FOOD_BAG;
    public static final RegistrySupplier<Item> GREEN_FOOD_BAG_GROWTH;
    public static final RegistrySupplier<Item> GREEN_FOOD_BAG_REJUVENATE;
    public static final RegistrySupplier<Item> GREEN_FOOD_BAG_STOP;
    public static final RegistrySupplier<Item> GREEN_FOOD_BAG_TRANSMUTE;
    public static final RegistrySupplier<Item> GREEN_FOOD_BAG_FEATHER_ADD;
    public static final RegistrySupplier<Item> GREEN_FOOD_BAG_FEATHER_MINUS;
    public static final RegistrySupplier<Item> GREEN_FOOD_BAG_FEATHER_FAST;
    public static final RegistrySupplier<Item> GREEN_FOOD_BAG_FEATHER_SLOW;
    public static final RegistrySupplier<Item> GREEN_FOOD_BAG_SIZE_UP;
    public static final RegistrySupplier<Item> GREEN_FOOD_BAG_SIZE_DOWN;

    public static final RegistrySupplier<Item> GOLDEN_FOOD_BAG;
    public static final RegistrySupplier<Item> GOLDEN_FOOD_BAG_UPGRADE;
    public static final RegistrySupplier<Item> GOLDEN_FOOD_BAG_DOWNGRADE;
    public static final RegistrySupplier<Item> GOLDEN_FOOD_BAG_EGG_ADD;
    public static final RegistrySupplier<Item> GOLDEN_FOOD_BAG_EGG_MINUS;
    public static final RegistrySupplier<Item> GOLDEN_FOOD_BAG_UNIQUE;

    public static final RegistrySupplier<Item> BIRD_NEST;
    public static final RegistrySupplier<Item> WIND_FEATHER_FAN;

    private NeoGuanNiaoItems() {
    }

    private static RegistrySupplier<Item> registerBirdCageItem(
            BirdCageVariant variant,
            Supplier<? extends Block> block
    ) {
        return ITEMS.register(variant.id(),
                () -> new BirdCageItem(
                        variant,
                        block.get(),
                        new Item.Properties()
                ));
    }

    private static RegistrySupplier<Item> registerBirdBathItem(
            BirdBathVariant variant,
            Supplier<? extends Block> block
    ) {
        return ITEMS.register(variant.id(),
                () -> new BirdBathItem(
                        variant,
                        block.get(),
                        new Item.Properties()
                ));
    }

    static {
        BREADCRUMBS = ITEMS.register(
                "breadcrumbs",
                () -> new BreadcrumbItem(new Item.Properties())
        );

        BIRD_GUIDE = ITEMS.register(
                "bird_guide",
                () -> new BirdGuideItem(new Item.Properties().stacksTo(1))
        );

        NIKON_D750 = ITEMS.register("nikon_d750", () -> new NikonD750Item(new Item.Properties().stacksTo(1)));
        FILM = ITEMS.register("film", () -> new FilmItem(new Item.Properties().stacksTo(1)));
        PHOTOGRAPH = ITEMS.register("photograph", () -> new PhotographItem(new Item.Properties().stacksTo(16)));


        SMALL_BIRD_CAGE = registerBirdCageItem(BirdCageVariant.SMALL, NeoGuanNiaoBlocks.SMALL_BIRD_CAGE);
        MEDIUM_BIRD_CAGE = registerBirdCageItem(BirdCageVariant.MEDIUM, NeoGuanNiaoBlocks.MEDIUM_BIRD_CAGE);
        LARGE_BIRD_CAGE = registerBirdCageItem(BirdCageVariant.LARGE, NeoGuanNiaoBlocks.LARGE_BIRD_CAGE);

        WOODEN_BIRD_BATH = registerBirdBathItem(BirdBathVariant.WOODEN_BIRD_BATH, NeoGuanNiaoBlocks.WOODEN_BIRD_BATH);
        STONE_BIRD_BATH = registerBirdBathItem(BirdBathVariant.STONE_BIRD_BATH, NeoGuanNiaoBlocks.STONE_BIRD_BATH);
        BIRD_BATH = registerBirdBathItem(BirdBathVariant.BIRD_BATH, NeoGuanNiaoBlocks.BIRD_BATH);

        WOODEN_BIRD_BATH_2 = registerBirdBathItem(BirdBathVariant.WOODEN_BIRD_BATH_2, NeoGuanNiaoBlocks.WOODEN_BIRD_BATH_2);
        STONE_BIRD_BATH_2 = registerBirdBathItem(BirdBathVariant.STONE_BIRD_BATH_2, NeoGuanNiaoBlocks.STONE_BIRD_BATH_2);
        BIRD_BATH_2 = registerBirdBathItem(BirdBathVariant.BIRD_BATH_2, NeoGuanNiaoBlocks.BIRD_BATH_2);

        BIRD_EGG =
                ITEMS.registerItem(
                        "bird_egg",
                        BirdEggItem::new,
                        new Item.Properties()
                );

        BIRD_FEATHER =
                ITEMS.registerItem(
                        "bird_feather",
                        BirdFeatherItem::new,
                        new Item.Properties()
                );

        BIRD_FOOD_BAG =
                ITEMS.registerItem("bird_food_bag",
                        BirdFoodBagItem::new,
                        new Item.Properties());

        BIRD_FOOD_BAG_SEED =
                ITEMS.registerItem("bird_food_bag_seed",
                        BirdFoodBagSeedItem::new,
                        new Item.Properties());

        BIRD_FOOD_BAG_FISH =
                ITEMS.registerItem("bird_food_bag_fish",
                        BirdFoodBagFishItem::new,
                        new Item.Properties());

        GREEN_FOOD_BAG =
                ITEMS.registerItem("green_food_bag",
                        GreenFoodBagItem::new,
                        new Item.Properties());

        GREEN_FOOD_BAG_GROWTH =
                ITEMS.registerItem("green_food_bag_growth",
                        GreenFoodBagGrowthItem::new,
                        new Item.Properties());

        GREEN_FOOD_BAG_REJUVENATE =
                ITEMS.registerItem("green_food_bag_rejuvenate",
                        GreenFoodBagRejuvenateItem::new,
                        new Item.Properties());

        GREEN_FOOD_BAG_STOP =
                ITEMS.registerItem("green_food_bag_stop",
                        GreenFoodBagStopItem::new,
                        new Item.Properties());

        GREEN_FOOD_BAG_TRANSMUTE =
                ITEMS.registerItem("green_food_bag_transmute",
                        GreenFoodBagTransmuteItem::new,
                        new Item.Properties());

        GREEN_FOOD_BAG_FEATHER_ADD =
                ITEMS.registerItem("green_food_bag_feather_add",
                        GreenFoodBagFeatherAddItem::new,
                        new Item.Properties());

        GREEN_FOOD_BAG_FEATHER_MINUS =
                ITEMS.registerItem("green_food_bag_feather_minus",
                        GreenFoodBagFeatherMinusItem::new,
                        new Item.Properties());

        GREEN_FOOD_BAG_FEATHER_FAST =
                ITEMS.registerItem("green_food_bag_feather_fast",
                        GreenFoodBagFeatherFastItem::new,
                        new Item.Properties());

        GREEN_FOOD_BAG_FEATHER_SLOW =
                ITEMS.registerItem("green_food_bag_feather_slow",
                        GreenFoodBagFeatherSlowItem::new,
                        new Item.Properties());

        GREEN_FOOD_BAG_SIZE_UP =
                ITEMS.registerItem("green_food_bag_size_up",
                        GreenFoodBagSizeUpItem::new,
                        new Item.Properties());

        GREEN_FOOD_BAG_SIZE_DOWN =
                ITEMS.registerItem("green_food_bag_size_down",
                        GreenFoodBagSizeDownItem::new,
                        new Item.Properties());

        GOLDEN_FOOD_BAG =
                ITEMS.registerItem("golden_food_bag",
                        GoldenFoodBagItem::new,
                        new Item.Properties());

        GOLDEN_FOOD_BAG_UPGRADE =
                ITEMS.registerItem("golden_food_bag_upgrade",
                        GoldenFoodBagUpgradeItem::new,
                        new Item.Properties());

        GOLDEN_FOOD_BAG_DOWNGRADE =
                ITEMS.registerItem("golden_food_bag_downgrade",
                        GoldenFoodBagDowngradeItem::new,
                        new Item.Properties());

        GOLDEN_FOOD_BAG_UNIQUE =
                ITEMS.registerItem("golden_food_bag_unique",
                        GoldenFoodBagUniqueItem::new,
                        new Item.Properties());

        GOLDEN_FOOD_BAG_EGG_ADD =
                ITEMS.registerItem("golden_food_bag_egg_add",
                        GoldenFoodBagEggAddItem::new,
                        new Item.Properties());

        GOLDEN_FOOD_BAG_EGG_MINUS =
                ITEMS.registerItem("golden_food_bag_egg_minus",
                        GoldenFoodBagEggMinusItem::new,
                        new Item.Properties());


        BIRD_NEST =
                ITEMS.register("bird_nest",
                        () -> new BirdNestItem(
                                NeoGuanNiaoBlocks.BIRD_NEST.get(),
                                new Item.Properties()
                        ));
        WIND_FEATHER_FAN = ITEMS.registerItem("wind_feather_fan", FeatherFanItem::new, new Item.Properties());
    }
}

