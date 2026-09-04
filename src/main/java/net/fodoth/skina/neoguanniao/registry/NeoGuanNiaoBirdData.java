package net.fodoth.skina.neoguanniao.registry;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.content.bird.core.data.BirdData;
import net.fodoth.skina.neoguanniao.content.bird.core.data.datum.*;
import net.fodoth.skina.neoguanniao.content.bird.core.skin.BirdSkinRarity;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.Map;

import static net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoBirdDataHelper.*;

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

    private NeoGuanNiaoBirdData() {
    }

    // Existing birds

    public static final DeferredHolder<BirdData, BirdData> BUDGERIGAR =
            BIRD_DATA.register("neo_budgerigar", () ->
                    BirdData.createDefault()
                            .withSound(
                                    sound(180,
                                            NeoGuanNiaoSoundEvents.BUDGERIGAR_AMBIENT.get(),
                                            NeoGuanNiaoSoundEvents.BUDGERIGAR_HURT.get(),
                                            NeoGuanNiaoSoundEvents.BUDGERIGAR_DEATH.get(),
                                            NeoGuanNiaoSoundEvents.BUDGERIGAR_INTERACT.get(),
                                            SoundEvents.PARROT_EAT)
                            )
                            .withFlying(flying(BirdFlightProfile.BUDGERIGAR))
                            .withModel(
                                    modelData("budgerigar", "budgerigar", budgerigarSkins(),
                                            BirdModelScaleProfile.BUDGERIGAR, 0.12F, 1.0F)
                            )
                            .withAnimation(
                                    animation("budgerigar", "budgerigar", budgerigarAnimations())
                                            .withCuriousAndTrustingIndexRange(2, 3)
                            )
                            .withMisc(BirdMiscDatum.createDefault().withSpawnRarity(5))
            );

    public static final DeferredHolder<BirdData, BirdData> NIGHT_HERON =
            BIRD_DATA.register("neo_night_heron", () ->
                    BirdData.createDefault()
                            .withSound(
                                    sound(240,
                                            NeoGuanNiaoSoundEvents.NIGHT_HERON_AMBIENT.get(),
                                            NeoGuanNiaoSoundEvents.NIGHT_HERON_HURT.get(),
                                            NeoGuanNiaoSoundEvents.NIGHT_HERON_DEATH.get(),
                                            NeoGuanNiaoSoundEvents.NIGHT_HERON_AMBIENT.get(),
                                            SoundEvents.ARMADILLO_EAT)
                            )
                            .withFlying(flying(BirdFlightProfile.NIGHT_HERON))
                            .withModel(
                                    BirdModelSkinDatum.createDefault()
                                            .withBirdModel(nightHeronModels())
                                            .withBirdSkin(nightHeronSkins())
                                            .withModelScaleProfile(BirdModelScaleProfile.NIGHT_HERON)
                                            .withShadowRadius(0.25F)
                                            .withGlobalScale(1.0F)
                            )
                            .withAnimation(
                                    animations(
                                            Map.of(
                                                    resource("night_heron"), resource("animations/night_heron.animation.json"),
                                                    resource("cheng_he_guang"), resource("animations/cheng_he_guang.animation.json")),
                                            nightHeronAnimations()).withCuriousAndTrustingIndexRange(5, 5)
                            ).withMisc(
                                    BirdMiscDatum.createDefault().withSpawnRarity(2).withActiveTime(11000, 1500)
                            ).withGoal(BirdGoalDatum.createDefault().withBreedDistance(2.0D).withBreedMoveSpeed(1.5D).withBathUseConsumeChance(1.0F))
            );


    public static final DeferredHolder<BirdData, BirdData> PIGEON =
            BIRD_DATA.register("neo_pigeon", () ->
                    BirdData.createDefault()
                            .withSound(
                                    sound(480,
                                            NeoGuanNiaoSoundEvents.PIGEON_AMBIENT.get(),
                                            NeoGuanNiaoSoundEvents.SPOTTED_DOVE_HURT.get(),
                                            NeoGuanNiaoSoundEvents.SPOTTED_DOVE_DEATH.get(),
                                            NeoGuanNiaoSoundEvents.PIGEON_AMBIENT.get(),
                                            SoundEvents.PARROT_EAT)
                            )
                            .withFlying(flying(BirdFlightProfile.COLUMBID))
                            .withModel(
                                    modelData(
                                            "pigeon", "columbid",
                                            genderedSkins("pigeon",
                                                    skinVariant("pigeon_gray", BirdSkinRarity.COMMON),
                                                    skinVariant("pigeon_white", BirdSkinRarity.COMMON)),
                                            BirdModelScaleProfile.COLUMBID, 0.25F, 1.0F)
                            )
                            .withAnimation(
                                    animation("pigeon", "columbid", columbidAnimations())
                                            .withCuriousAndTrustingIndexRange(3, 3)
                            ).withMisc(BirdMiscDatum.createDefault().withSpawnRarity(5))
                            .withGoal(BirdGoalDatum.createDefault().withBreedDistance(1.5D).withBreedMoveSpeed(1.1D).withBathUseConsumeChance(0.5F))
            );

    public static final DeferredHolder<BirdData, BirdData> DOVE =
            BIRD_DATA.register("neo_dove", () ->
                    BirdData.createDefault()
                            .withSound(
                                    sound(480,
                                            NeoGuanNiaoSoundEvents.SPOTTED_DOVE_AMBIENT.get(),
                                            NeoGuanNiaoSoundEvents.SPOTTED_DOVE_HURT.get(),
                                            NeoGuanNiaoSoundEvents.SPOTTED_DOVE_DEATH.get(),
                                            NeoGuanNiaoSoundEvents.SPOTTED_DOVE_MATE.get(),
                                            SoundEvents.PARROT_EAT)
                            )
                            .withFlying(flying(BirdFlightProfile.COLUMBID))
                            .withModel(
                                    modelData(
                                            "dove", "columbid",
                                            genderedSkins("dove",
                                                    skinVariant("spotted_dove", BirdSkinRarity.COMMON),
                                                    skinVariant("orienta_turtle_dove", BirdSkinRarity.UNCOMMON),
                                                    skinVariant("treron", BirdSkinRarity.RARE)),
                                            BirdModelScaleProfile.COLUMBID, 0.25F, 1.0F)
                            )
                            .withAnimation(
                                    animation("dove", "columbid", columbidAnimations())
                                            .withCuriousAndTrustingIndexRange(3, 3)
                            ).withMisc(BirdMiscDatum.createDefault().withSpawnRarity(4))
                            .withGoal(BirdGoalDatum.createDefault().withBreedDistance(1.5D).withBreedMoveSpeed(1.1D).withBathUseConsumeChance(0.6F))
            );


    public static final DeferredHolder<BirdData, BirdData> SPARROW =
            BIRD_DATA.register("neo_sparrow", () ->
                    BirdData.createDefault()
                            .withSound(
                                    sound(240,
                                            NeoGuanNiaoSoundEvents.SPARROW_AMBIENT.get(),
                                            NeoGuanNiaoSoundEvents.SPARROW_HURT.get(),
                                            NeoGuanNiaoSoundEvents.SPARROW_DEATH.get(),
                                            NeoGuanNiaoSoundEvents.SPARROW_AMBIENT.get(),
                                            SoundEvents.PARROT_EAT)
                            )
                            .withFlying(flying(BirdFlightProfile.SPARROW))
                            .withModel(
                                    modelData(
                                            "sparrow", "sparrow",
                                            genderedSkins("sparrow",
                                                    skinVariant("sparrow", BirdSkinRarity.COMMON)),
                                            BirdModelScaleProfile.SPARROW, 0.18F, 0.9F)
                            )
                            .withAnimation(
                                    animation("sparrow", "sparrow", sparrowAnimations())
                                            .withCuriousAndTrustingIndexRange(3, 3)
                            ).withMisc(BirdMiscDatum.createDefault().withSpawnRarity(6))
                            .withGoal(BirdGoalDatum.createDefault().withBreedDistance(1.2D).withBreedMoveSpeed(1.1D).withBathUseConsumeChance(0.25F))
            );

    // Guaniao 2.1.3 birds

    public static final DeferredHolder<BirdData, BirdData> COCKATIEL = registerSimpleBird(
            "neo_cockatiel", "cockatiel", List.of(
                    skinVariant("dark_gray_yellow_face", BirdSkinRarity.COMMON),
                    skinVariant("gray_yellow_face", BirdSkinRarity.COMMON),
                    skinVariant("gray_white_face", BirdSkinRarity.UNCOMMON),
                    skinVariant("pale_yellow", BirdSkinRarity.UNCOMMON),
                    skinVariant("white_yellow_face", BirdSkinRarity.RARE)),
            BirdFlightProfile.BUDGERIGAR, BirdModelScaleProfile.BUDGERIGAR, 0.2F, 1.3F, 4,
            "cockatiel", Map.ofEntries(
                    Map.entry("idle", "idle"), Map.entry("preen", "idle_diff_2"),
                    Map.entry("curious", "idle_diff_1"), Map.entry("dance", "idle_diff_1"),
                    Map.entry("idle_1", "idle_diff_3"),
                    Map.entry("walk", "walk"), Map.entry("fly", "fly"), Map.entry("eat", "eat"),
                    Map.entry("sleep", "sleep"), Map.entry("sleep_loop", "sleep_loop")));

    public static final DeferredHolder<BirdData, BirdData> LONG_TAILED_TIT = registerSimpleBird(
            "neo_long_tailed_tit", "long_tailed_tit",
            List.of(skinVariant("long_tailed_tit", BirdSkinRarity.COMMON)),
            BirdFlightProfile.SPARROW, BirdModelScaleProfile.SPARROW, 0.16F, 0.72F, 6,
            "long_tailed_tit", Map.ofEntries(
                    Map.entry("idle", "idle"), Map.entry("preen", "idle_diff_1"),
                    Map.entry("curious", "idle_diff_2"), Map.entry("idle_1", "idle_diff_3"),
                    Map.entry("dance", "idle_diff_3"),
                    Map.entry("walk", "walk"), Map.entry("fly", "fly_loop"), Map.entry("eat", "eat"),
                    Map.entry("sleep", "idle"), Map.entry("sleep_loop", "idle")));

    public static final DeferredHolder<BirdData, BirdData> MACAW = registerSimpleBird(
            "neo_macaw", "macaw", List.of(
                    skinVariant("scarlet", BirdSkinRarity.COMMON),
                    skinVariant("blue_yellow", BirdSkinRarity.UNCOMMON),
                    skinVariant("catalina", BirdSkinRarity.UNCOMMON),
                    skinVariant("hyacinth", BirdSkinRarity.RARE),
                    skinVariant("glaucous", BirdSkinRarity.EPIC)),
            BirdFlightProfile.COLUMBID, BirdModelScaleProfile.COLUMBID, 0.32F, 1.04F, 2,
            "macaw", Map.ofEntries(
                    Map.entry("idle", "idle"), Map.entry("preen", "idle_diff_1"),
                    Map.entry("curious", "idle_diff_2"), Map.entry("dance", "idle_diff_3"),
                    Map.entry("idle_1", "idle_diff_4"),
                    Map.entry("walk", "walk"), Map.entry("fly", "fly_flapping_wing_loop"),
                    Map.entry("eat", "eat"), Map.entry("sleep", "sleep"),
                    Map.entry("sleep_loop", "sleep_loop")));

    public static final DeferredHolder<BirdData, BirdData> CROW = registerSimpleBird(
            "neo_crow", "crow", List.of(skinVariant("crow", BirdSkinRarity.COMMON)), BirdFlightProfile.COLUMBID,
            BirdModelScaleProfile.COLUMBID, 0.26F, 0.8F, 4, "crow", Map.ofEntries(
                    Map.entry("idle", "idle"), Map.entry("preen", "idle_diff_1"),
                    Map.entry("curious", "idle_diff_2"), Map.entry("dance", "idle"),
                    Map.entry("walk", "walk"), Map.entry("fly", "fly"),
                    Map.entry("fly_glide", "fly_loop"), Map.entry("eat", "eat"),
                    Map.entry("sleep", "sleep"), Map.entry("sleep_1", "sleep2")));

    public static final DeferredHolder<BirdData, BirdData> SEAGULL = registerSimpleBird(
            "neo_seagull", "seagull", List.of(skinVariant("seagull", BirdSkinRarity.COMMON)), BirdFlightProfile.NIGHT_HERON,
            BirdModelScaleProfile.COLUMBID, 0.3F, 0.9F, 5, "seagull", Map.ofEntries(
                    Map.entry("idle", "idle"), Map.entry("preen", "idle_diff_1"),
                    Map.entry("curious", "idle_diff_3"),
                    Map.entry("idle_1", "idle_diff_2"), Map.entry("idle_2", "idle_diff_4"),
                    Map.entry("idle_3", "idle_diff_5"), Map.entry("dance", "idle_diff_4"),
                    Map.entry("walk", "walk"), Map.entry("fly", "fly_flapping_wing_loop"),
                    Map.entry("fly_glide", "fly_loop"), Map.entry("eat", "eat"),
                    Map.entry("sleep", "sleep"), Map.entry("sleep_loop", "sleep_loop")));

    public static final DeferredHolder<BirdData, BirdData> KIWI = BIRD_DATA.register("neo_kiwi", () ->
            BirdData.createDefault()
                    .withSound(sound(240, NeoGuanNiaoSoundEvents.KIWI_AMBIENT.get(), NeoGuanNiaoSoundEvents.KIWI_HURT.get(),
                            NeoGuanNiaoSoundEvents.KIWI_DEATH.get(), NeoGuanNiaoSoundEvents.KIWI_AMBIENT.get(), SoundEvents.PARROT_EAT))
                    .withModel(modelData("kiwi", "kiwi", genderedSkins("kiwi", skinVariant("kiwi", BirdSkinRarity.COMMON)),
                            BirdModelScaleProfile.COLUMBID, 0.2F, 1.0F))
                    .withAnimation(animation("kiwi", "kiwi", Map.ofEntries(
                            Map.entry("idle", loop("idle")), Map.entry("preen", playThenIdle("idle_diff_1")),
                            Map.entry("curious", playThenIdle("idle_diff_2")), Map.entry("walk", loop("walk")),
                            Map.entry("eat", playThenIdle("idle_diff_2")), Map.entry("sleep", playThenLoop("sleep", "sleep_loop")),
                            Map.entry("sleep_loop", loop("sleep_loop")))))
                    .withMisc(BirdMiscDatum.createDefault().withSpawnRarity(3)));

    public static final DeferredHolder<BirdData, BirdData> MYNA = BIRD_DATA.register("neo_myna", () ->
            BirdData.createDefault()
                    .withSound(sound(240, NeoGuanNiaoSoundEvents.MYNA_AMBIENT.get(), NeoGuanNiaoSoundEvents.MYNA_HURT.get(),
                            NeoGuanNiaoSoundEvents.MYNA_DEATH.get(), NeoGuanNiaoSoundEvents.MYNA_AMBIENT.get(), SoundEvents.PARROT_EAT))
                    .withFlying(flying(BirdFlightProfile.BUDGERIGAR))
                    .withModel(modelData("myna", "myna", genderedSkins("myna", skinVariant("myna", BirdSkinRarity.COMMON)),
                            BirdModelScaleProfile.COLUMBID, 0.2F, 1.15F))
                    .withAnimation(animation("myna", "myna", Map.ofEntries(
                            Map.entry("idle", loop("idle")), Map.entry("preen", playThenIdle("idle_diff_1")),
                            Map.entry("curious", playThenIdle("idle_diff_2")), Map.entry("walk", loop("walk")),
                            Map.entry("fly", loop("fly")), Map.entry("eat", playThenIdle("idle_diff_2")),
                            Map.entry("sleep", playThenLoop("sleep", "sleep_loop")), Map.entry("sleep_loop", loop("sleep_loop")))))
                    .withMisc(BirdMiscDatum.createDefault().withSpawnRarity(4)));

    private static DeferredHolder<BirdData, BirdData> registerSimpleBird(
            String id, String assetName, List<SkinVariant> skins, BirdFlightProfile flightProfile,
            BirdModelScaleProfile scaleProfile, float shadowRadius, float globalScale, int localSpawnCap,
            String animationName,
            Map<String, String> animations) {
        return BIRD_DATA.register(id, () -> NeoGuanNiaoBirdDataHelper.simpleBird(
                id, assetName, skins, flightProfile, scaleProfile,
                shadowRadius, globalScale, animationName, animations)
                .withMisc(BirdMiscDatum.createDefault().withSpawnRarity(localSpawnCap)));
    }

}
