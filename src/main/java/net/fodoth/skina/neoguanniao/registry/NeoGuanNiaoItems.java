package net.fodoth.skina.neoguanniao.registry;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.content.bath.BirdBathItem;
import net.fodoth.skina.neoguanniao.content.bath.BirdBathVariant;
import net.fodoth.skina.neoguanniao.content.bird.impl.old.columbid.PigeonDefinition;
import net.fodoth.skina.neoguanniao.content.bird.impl.old.columbid.SpottedDoveDefinition;
import net.fodoth.skina.neoguanniao.content.cage.BirdCageItem;
import net.fodoth.skina.neoguanniao.content.cage.BirdCageVariant;
import net.fodoth.skina.neoguanniao.content.egg.BirdEggItem;
import net.fodoth.skina.neoguanniao.content.feed.BirdFoodBagFishItem;
import net.fodoth.skina.neoguanniao.content.feed.BirdFoodBagItem;
import net.fodoth.skina.neoguanniao.content.feed.BirdFoodBagSeedItem;
import net.fodoth.skina.neoguanniao.content.feed.BreadcrumbItem;
import net.fodoth.skina.neoguanniao.content.guide.BirdGuideItem;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class NeoGuanNiaoItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(NeoGuanNiao.MODID);

    public static final DeferredItem<Item> NIGHT_HERON_SPAWN_EGG;
    public static final DeferredItem<Item> SPARROW_SPAWN_EGG;
    public static final DeferredItem<Item> BUDGERIGAR_SPAWN_EGG;
    public static final DeferredItem<Item> SPOTTED_DOVE_SPAWN_EGG;
    public static final DeferredItem<Item> PIGEON_SPAWN_EGG;

    public static final DeferredItem<Item> BREADCRUMBS;
    public static final DeferredItem<Item> BIRD_GUIDE;

    public static final DeferredItem<Item> SMALL_BIRD_CAGE;
    public static final DeferredItem<Item> MEDIUM_BIRD_CAGE;
    public static final DeferredItem<Item> LARGE_BIRD_CAGE;

    public static final DeferredItem<Item> WOODEN_BIRD_BATH;
    public static final DeferredItem<Item> STONE_BIRD_BATH;
    public static final DeferredItem<Item> BIRD_BATH;
    public static final DeferredItem<Item> WOODEN_BIRD_BATH_2;
    public static final DeferredItem<Item> STONE_BIRD_BATH_2;
    public static final DeferredItem<Item> BIRD_BATH_2;

    public static final DeferredItem<Item> BIRD_EGG;
    public static final DeferredItem<Item> BIRD_FOOD_BAG;
    public static final DeferredItem<Item> BIRD_FOOD_BAG_SEED;
    public static final DeferredItem<Item> BIRD_FOOD_BAG_FISH;

    private NeoGuanNiaoItems() {
    }

    private static DeferredItem<Item> registerSpawnEgg(
            String id,
            Supplier<? extends EntityType<? extends Mob>> entityType,
            int baseColor,
            int spotColor
    ) {
        return ITEMS.register(id,
                () -> new DeferredSpawnEggItem(
                        entityType,
                        baseColor,
                        spotColor,
                        new Item.Properties()
                ));
    }

    private static DeferredItem<Item> registerBirdCageItem(
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

    private static DeferredItem<Item> registerBirdBathItem(
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
        NIGHT_HERON_SPAWN_EGG = registerSpawnEgg(
                "night_heron_spawn_egg",
                NeoGuanNiaoEntityTypes.NIGHT_HERON,
                6121331,
                14198125
        );

        SPARROW_SPAWN_EGG = registerSpawnEgg(
                "sparrow_spawn_egg",
                NeoGuanNiaoEntityTypes.SPARROW,
                9072205,
                14141346
        );

        BUDGERIGAR_SPAWN_EGG = registerSpawnEgg(
                "budgerigar_spawn_egg",
                NeoGuanNiaoEntityTypes.BUDGERIGAR,
                7323461,
                16111690
        );

        SPOTTED_DOVE_SPAWN_EGG = registerSpawnEgg(
                "spotted_dove_spawn_egg",
                NeoGuanNiaoEntityTypes.SPOTTED_DOVE,
                SpottedDoveDefinition.SPAWN_EGG_BASE_COLOR,
                SpottedDoveDefinition.SPAWN_EGG_SPOT_COLOR
        );

        PIGEON_SPAWN_EGG = registerSpawnEgg(
                "pigeon_spawn_egg",
                NeoGuanNiaoEntityTypes.PIGEON,
                PigeonDefinition.SPAWN_EGG_BASE_COLOR,
                PigeonDefinition.SPAWN_EGG_SPOT_COLOR
        );

        BREADCRUMBS = ITEMS.register(
                "breadcrumbs",
                () -> new BreadcrumbItem(new Item.Properties())
        );

        BIRD_GUIDE = ITEMS.register(
                "bird_guide",
                () -> new BirdGuideItem(new Item.Properties().stacksTo(1))
        );


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
    }
}