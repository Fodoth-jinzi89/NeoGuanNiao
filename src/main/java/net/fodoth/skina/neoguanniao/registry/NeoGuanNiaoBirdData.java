package net.fodoth.skina.neoguanniao.registry;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.content.bird.core.data.BirdData;
import net.fodoth.skina.neoguanniao.content.bird.core.data.datum.*;
import net.fodoth.skina.neoguanniao.content.bird.core.model.BirdModel;
import net.fodoth.skina.neoguanniao.content.bird.core.model.BirdModelRarity;
import net.fodoth.skina.neoguanniao.content.bird.core.skin.BirdSkin;
import net.fodoth.skina.neoguanniao.content.bird.core.skin.BirdSkinRarity;
import net.fodoth.skina.neoguanniao.content.bird.feature.flight.BirdFlightProfile;
import net.fodoth.skina.neoguanniao.content.bird.feature.scale.BirdModelScaleProfile;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import software.bernie.geckolib.animation.RawAnimation;

import java.util.List;
import java.util.Map;

public final class NeoGuanNiaoBirdData {


    public static final DeferredRegister<BirdData> BIRD_DATA =
            DeferredRegister.create(
                    ResourceKey.createRegistryKey(
                            resource(
                                    "bird_data"
                            )
                    ),
                    NeoGuanNiao.MODID
            );


    static {
        BIRD_DATA.makeRegistry(builder -> builder
                .sync(true)
                .defaultKey(resource("default_bird"))
                .maxId(Integer.MAX_VALUE));
    }

    public static final DeferredHolder<BirdData, BirdData> BUDGERIGAR =
            BIRD_DATA.register("neo_budgerigar", () ->
                    BirdData.createDefault()
                            .withSound(
                                    new BirdSoundDatum(
                                            0.5F,
                                            180,
                                            NeoGuanNiaoSoundEvents.BUDGERIGAR_AMBIENT.get(),
                                            NeoGuanNiaoSoundEvents.BUDGERIGAR_HURT.get(),
                                            NeoGuanNiaoSoundEvents.BUDGERIGAR_DEATH.get(),
                                            NeoGuanNiaoSoundEvents.BUDGERIGAR_INTERACT.get(),
                                            SoundEvents.PARROT_EAT
                                    )
                            )
                            .withFlying(
                                    BirdFlyingDatum.createDefault()
                                            .withFlightProfile(BirdFlightProfile.BUDGERIGAR)
                            )
                            .withModel(
                                    BirdModelSkinDatum.createDefault()
                                            .withBirdModel(List.of(
                                                    BirdModel.createDefault()
                                                            .withId(resource("budgerigar"))
                                                            .withLocation(resource("geo/budgerigar.geo.json"))
                                                            .withRarity(BirdModelRarity.COMMON)
                                            ))
                                            .withBirdSkin(List.of(
                                                    BirdSkin.createDefault()
                                                            .withId(resource("green_male"))
                                                            .withLocation(resource("textures/entity/budgerigar/green_male.png"))
                                                            .withRarity(BirdSkinRarity.COMMON)
                                                            .withMale(true).withFemale(false),

                                                    BirdSkin.createDefault()
                                                            .withId(resource("green_female"))
                                                            .withLocation(resource("textures/entity/budgerigar/green_female.png"))
                                                            .withRarity(BirdSkinRarity.COMMON)
                                                            .withMale(false).withFemale(true),

                                                    BirdSkin.createDefault()
                                                            .withId(resource("yellow_male"))
                                                            .withLocation(resource("textures/entity/budgerigar/yellow_male.png"))
                                                            .withRarity(BirdSkinRarity.COMMON)
                                                            .withMale(true).withFemale(false),

                                                    BirdSkin.createDefault()
                                                            .withId(resource("yellow_female"))
                                                            .withLocation(resource("textures/entity/budgerigar/yellow_female.png"))
                                                            .withRarity(BirdSkinRarity.COMMON)
                                                            .withMale(false).withFemale(true),

                                                    BirdSkin.createDefault()
                                                            .withId(resource("blue_white_male"))
                                                            .withLocation(resource("textures/entity/budgerigar/blue_white_male.png"))
                                                            .withRarity(BirdSkinRarity.COMMON)
                                                            .withMale(true).withFemale(false),

                                                    BirdSkin.createDefault()
                                                            .withId(resource("blue_white_female"))
                                                            .withLocation(resource("textures/entity/budgerigar/blue_white_female.png"))
                                                            .withRarity(BirdSkinRarity.COMMON)
                                                            .withMale(false).withFemale(true),

                                                    BirdSkin.createDefault()
                                                            .withId(resource("white_lark_male"))
                                                            .withLocation(resource("textures/entity/budgerigar/white_lark_male.png"))
                                                            .withRarity(BirdSkinRarity.UNCOMMON)
                                                            .withMale(true).withFemale(false),

                                                    BirdSkin.createDefault()
                                                            .withId(resource("white_lark_female"))
                                                            .withLocation(resource("textures/entity/budgerigar/white_lark_female.png"))
                                                            .withRarity(BirdSkinRarity.UNCOMMON)
                                                            .withMale(false).withFemale(true),

                                                    BirdSkin.createDefault()
                                                            .withId(resource("blue_lark_male"))
                                                            .withLocation(resource("textures/entity/budgerigar/blue_lark_male.png"))
                                                            .withRarity(BirdSkinRarity.UNCOMMON)
                                                            .withMale(true).withFemale(false),

                                                    BirdSkin.createDefault()
                                                            .withId(resource("blue_lark_female"))
                                                            .withLocation(resource("textures/entity/budgerigar/blue_lark_female.png"))
                                                            .withRarity(BirdSkinRarity.UNCOMMON)
                                                            .withMale(false).withFemale(true),

                                                    BirdSkin.createDefault()
                                                            .withId(resource("yellow_lark_male"))
                                                            .withLocation(resource("textures/entity/budgerigar/yellow_lark_male.png"))
                                                            .withRarity(BirdSkinRarity.RARE)
                                                            .withMale(true).withFemale(false),

                                                    BirdSkin.createDefault()
                                                            .withId(resource("yellow_lark_female"))
                                                            .withLocation(resource("textures/entity/budgerigar/yellow_lark_female.png"))
                                                            .withRarity(BirdSkinRarity.RARE)
                                                            .withMale(false).withFemale(true),

                                                    BirdSkin.createDefault()
                                                            .withId(resource("yellow_black_male"))
                                                            .withLocation(resource("textures/entity/budgerigar/yellow_black_male.png"))
                                                            .withRarity(BirdSkinRarity.RARE)
                                                            .withMale(true).withFemale(false),

                                                    BirdSkin.createDefault()
                                                            .withId(resource("yellow_black_female"))
                                                            .withLocation(resource("textures/entity/budgerigar/yellow_black_female.png"))
                                                            .withRarity(BirdSkinRarity.RARE)
                                                            .withMale(false).withFemale(true),

                                                    BirdSkin.createDefault()
                                                            .withId(resource("mystery_green_male"))
                                                            .withLocation(resource("textures/entity/budgerigar/mystery_green_male.png"))
                                                            .withRarity(BirdSkinRarity.EPIC)
                                                            .withNatureSpawn(false)
                                                            .withMale(true).withFemale(false),

                                                    BirdSkin.createDefault()
                                                            .withId(resource("mystery_green_female"))
                                                            .withLocation(resource("textures/entity/budgerigar/mystery_green_female.png"))
                                                            .withRarity(BirdSkinRarity.EPIC)
                                                            .withNatureSpawn(false)
                                                            .withMale(false).withFemale(true),

                                                    BirdSkin.createDefault()
                                                            .withId(resource("blue_porcelain_male"))
                                                            .withLocation(resource("textures/entity/budgerigar/blue_porcelain_male.png"))
                                                            .withRarity(BirdSkinRarity.EPIC)
                                                            .withNatureSpawn(false)
                                                            .withMale(true).withFemale(false),

                                                    BirdSkin.createDefault()
                                                            .withId(resource("blue_porcelain_female"))
                                                            .withLocation(resource("textures/entity/budgerigar/blue_porcelain_female.png"))
                                                            .withRarity(BirdSkinRarity.EPIC)
                                                            .withNatureSpawn(false)
                                                            .withMale(false).withFemale(true),

                                                    BirdSkin.createDefault()
                                                            .withId(resource("black_white_male"))
                                                            .withLocation(resource("textures/entity/budgerigar/black_white_male.png"))
                                                            .withRarity(BirdSkinRarity.LEGENDARY)
                                                            .withNatureSpawn(false)
                                                            .withMale(true).withFemale(false),

                                                    BirdSkin.createDefault()
                                                            .withId(resource("black_white_female"))
                                                            .withLocation(resource("textures/entity/budgerigar/black_white_female.png"))
                                                            .withRarity(BirdSkinRarity.LEGENDARY)
                                                            .withNatureSpawn(false)
                                                            .withMale(false).withFemale(true),

                                                    BirdSkin.createDefault()
                                                            .withId(resource("golden_male"))
                                                            .withLocation(resource("textures/entity/budgerigar/golden_male.png"))
                                                            .withBreed(false)
                                                            .withNatureSpawn(false)
                                                            .withUnique(true)
                                                            .withRarity(BirdSkinRarity.UNIQUE)
                                                            .withMale(true).withFemale(false),

                                                    BirdSkin.createDefault()
                                                            .withId(resource("golden_female"))
                                                            .withLocation(resource("textures/entity/budgerigar/golden_female.png"))
                                                            .withBreed(false)
                                                            .withNatureSpawn(false)
                                                            .withUnique(true)
                                                            .withRarity(BirdSkinRarity.UNIQUE)
                                                            .withMale(false).withFemale(true))
                                            )
                                            .withModelScaleProfile(BirdModelScaleProfile.BUDGERIGAR)
                                            .withShadowRadius(0.12F)
                                            .withGlobalScale(1.0F)
                                            .withBabyScale(0.75F)
                            )
                            .withAnimation(
                                    BirdAnimationDatum.withAnimationIdAndMap(
                                            resource("animations/budgerigar.animation.json"),
                                            Map.of(
                                                    "idle", RawAnimation.begin().thenLoop("idle"),
                                                    "preen", RawAnimation.begin().thenPlay("idle_diff_1").thenLoop("idle"),
                                                    "curious", RawAnimation.begin().thenPlay("idle_diff_2").thenLoop("idle"),
                                                    "dance", RawAnimation.begin().thenLoop("idle_diff_3"),
                                                    "eat", RawAnimation.begin().thenPlay("eat").thenLoop("idle"),
                                                    "sleep", RawAnimation.begin().thenPlay("sleep").thenLoop("sleep_loop"),
                                                    "sleep_loop", RawAnimation.begin().thenLoop("sleep_loop"),
                                                    "walk", RawAnimation.begin().thenLoop("walk"),
                                                    "fly", RawAnimation.begin().thenLoop("fly_flapping_wing_loop")
                                            )
                                    ).withCuriousAndTrustingIndexRange(2, 3)
                            )
            );

    public static final DeferredHolder<BirdData, BirdData> NIGHT_HERON =
            BIRD_DATA.register("neo_night_heron", () ->
                    BirdData.createDefault()
                            .withSound(
                                    new BirdSoundDatum(
                                            0.5F,
                                            240,
                                            NeoGuanNiaoSoundEvents.NIGHT_HERON_AMBIENT.get(),
                                            NeoGuanNiaoSoundEvents.NIGHT_HERON_HURT.get(),
                                            NeoGuanNiaoSoundEvents.NIGHT_HERON_DEATH.get(),
                                            NeoGuanNiaoSoundEvents.NIGHT_HERON_AMBIENT.get(),
                                            SoundEvents.ARMADILLO_EAT
                                    )
                            )
                            .withFlying(
                                    BirdFlyingDatum.createDefault()
                                            .withFlightProfile(BirdFlightProfile.NIGHT_HERON)
                            )
                            .withModel(
                                    BirdModelSkinDatum.createDefault()
                                            .withBirdModel(List.of(
                                                    BirdModel.createDefault()
                                                            .withId(resource("night_heron"))
                                                            .withLocation(resource("geo/night_heron.geo.json"))
                                                            .withRarity(BirdModelRarity.COMMON)
                                                    ))
                                            .withBirdSkin(List.of(
                                                    BirdSkin.createDefault()
                                                            .withId(resource("night_heron_0"))
                                                            .withLocation(resource("textures/entity/night_heron/night_heron_0.png"))
                                                            .withRarity(BirdSkinRarity.COMMON).withBaby(false),
                                                    BirdSkin.createDefault()
                                                            .withId(resource("night_heron_0_baby"))
                                                            .withLocation(resource("textures/entity/night_heron/night_heron_0_baby.png"))
                                                            .withRarity(BirdSkinRarity.COMMON).withBaby(true),

                                                    BirdSkin.createDefault()
                                                            .withId(resource("night_heron_1"))
                                                            .withLocation(resource("textures/entity/night_heron/night_heron_1.png"))
                                                            .withRarity(BirdSkinRarity.COMMON).withBaby(false),
                                                    BirdSkin.createDefault()
                                                            .withId(resource("night_heron_1_baby"))
                                                            .withLocation(resource("textures/entity/night_heron/night_heron_1_baby.png"))
                                                            .withRarity(BirdSkinRarity.COMMON).withBaby(true),

                                                    BirdSkin.createDefault()
                                                            .withId(resource("night_heron_2"))
                                                            .withLocation(resource("textures/entity/night_heron/night_heron_2.png"))
                                                            .withRarity(BirdSkinRarity.UNCOMMON).withBaby(false),
                                                    BirdSkin.createDefault()
                                                            .withId(resource("night_heron_2_baby"))
                                                            .withLocation(resource("textures/entity/night_heron/night_heron_2_baby.png"))
                                                            .withRarity(BirdSkinRarity.UNCOMMON).withBaby(true),

                                                    BirdSkin.createDefault()
                                                            .withId(resource("night_heron_3"))
                                                            .withLocation(resource("textures/entity/night_heron/night_heron_3.png"))
                                                            .withRarity(BirdSkinRarity.UNCOMMON).withBaby(false),
                                                    BirdSkin.createDefault()
                                                            .withId(resource("night_heron_3_baby"))
                                                            .withLocation(resource("textures/entity/night_heron/night_heron_3_baby.png"))
                                                            .withRarity(BirdSkinRarity.UNCOMMON).withBaby(true),

                                                    BirdSkin.createDefault()
                                                            .withId(resource("night_heron_4"))
                                                            .withLocation(resource("textures/entity/night_heron/night_heron_4.png"))
                                                            .withRarity(BirdSkinRarity.RARE).withBaby(false).withNatureSpawn(false),
                                                    BirdSkin.createDefault()
                                                            .withId(resource("night_heron_4_baby"))
                                                            .withLocation(resource("textures/entity/night_heron/night_heron_4_baby.png"))
                                                            .withRarity(BirdSkinRarity.RARE).withBaby(true).withNatureSpawn(false),

                                                    BirdSkin.createDefault()
                                                            .withId(resource("night_heron_5"))
                                                            .withLocation(resource("textures/entity/night_heron/night_heron_5.png"))
                                                            .withRarity(BirdSkinRarity.EPIC).withBaby(false).withNatureSpawn(false),
                                                    BirdSkin.createDefault()
                                                            .withId(resource("night_heron_5_baby"))
                                                            .withLocation(resource("textures/entity/night_heron/night_heron_5_baby.png"))
                                                            .withRarity(BirdSkinRarity.EPIC).withBaby(true).withNatureSpawn(false),

                                                    BirdSkin.createDefault()
                                                            .withId(resource("night_heron_6"))
                                                            .withLocation(resource("textures/entity/night_heron/night_heron_6.png"))
                                                            .withRarity(BirdSkinRarity.LEGENDARY).withBaby(false).withNatureSpawn(false),
                                                    BirdSkin.createDefault()
                                                            .withId(resource("night_heron_6_baby"))
                                                            .withLocation(resource("textures/entity/night_heron/night_heron_6_baby.png"))
                                                            .withRarity(BirdSkinRarity.LEGENDARY).withBaby(true).withNatureSpawn(false),

                                                    BirdSkin.createDefault()
                                                            .withId(resource("night_heron_7"))
                                                            .withLocation(resource("textures/entity/night_heron/night_heron_7.png"))
                                                            .withRarity(BirdSkinRarity.ANCIENT).withBaby(false).withNatureSpawn(false),
                                                    BirdSkin.createDefault()
                                                            .withId(resource("night_heron_7_baby"))
                                                            .withLocation(resource("textures/entity/night_heron/night_heron_7_baby.png"))
                                                            .withRarity(BirdSkinRarity.ANCIENT).withBaby(true).withNatureSpawn(false),

                                                    BirdSkin.createDefault()
                                                            .withId(resource("night_heron_golden"))
                                                            .withLocation(resource("textures/entity/night_heron/night_heron_golden.png"))
                                                            .withRarity(BirdSkinRarity.UNIQUE).withUnique(true).withBaby(false).withNatureSpawn(false),
                                                    BirdSkin.createDefault()
                                                            .withId(resource("night_heron_golden_baby"))
                                                            .withLocation(resource("textures/entity/night_heron/night_heron_golden_baby.png"))
                                                            .withRarity(BirdSkinRarity.UNIQUE).withUnique(true).withBaby(true).withNatureSpawn(false))
                                            )
                                            .withModelScaleProfile(BirdModelScaleProfile.NIGHT_HERON)
                                            .withShadowRadius(0.25F)
                                            .withGlobalScale(1.0F)
                                            .withBabyScale(0.75F)
                            )
                            .withAnimation(
                                    BirdAnimationDatum.withAnimationIdAndMap(
                                            resource("animations/night_heron.animation.json"),
                                            Map.ofEntries(
                                                    Map.entry("idle", RawAnimation.begin().thenLoop("idle")),
                                                    Map.entry("preen", RawAnimation.begin().thenLoop("idle_diff_4").thenLoop("idle")),
                                                    Map.entry("idle_1", RawAnimation.begin().thenPlay("idle_diff_1").thenLoop("idle")),
                                                    Map.entry("idle_2", RawAnimation.begin().thenPlay("idle_diff_2").thenLoop("idle")),
                                                    Map.entry("idle_3", RawAnimation.begin().thenLoop("idle_diff_3").thenLoop("idle")),
                                                    Map.entry("curious", RawAnimation.begin().thenLoop("idle_diff_5").thenLoop("idle")),
                                                    Map.entry("walk", RawAnimation.begin().thenLoop("walk")),
                                                    Map.entry("run", RawAnimation.begin().thenLoop("run")),
                                                    Map.entry("fly", RawAnimation.begin().thenLoop("fly_flapping_wing_loop")),
                                                    Map.entry("fly_glide", RawAnimation.begin().thenLoop("fly_loop")),
                                                    Map.entry("eat", RawAnimation.begin().thenPlay("eat").thenLoop("idle")),
                                                    Map.entry("sleep", RawAnimation.begin().thenLoop("idle")),
                                                    Map.entry("sleep_loop", RawAnimation.begin().thenLoop("idle"))
                                            )
                                    ).withCuriousAndTrustingIndexRange(5, 5)
                            ).withMisc(
                                    BirdMiscDatum.createDefault().withActiveTime(11000, 1500)
                            ).withGoal(BirdGoalDatum.createDefault().withBreedDistance(2.0D).withBreedMoveSpeed(1.5D))
            );


    private static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(NeoGuanNiao.MODID, path);
    }

}