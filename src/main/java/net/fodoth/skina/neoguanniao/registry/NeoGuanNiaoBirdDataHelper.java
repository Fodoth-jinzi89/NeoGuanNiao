package net.fodoth.skina.neoguanniao.registry;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.content.bird.core.data.BirdData;
import net.fodoth.skina.neoguanniao.content.bird.core.data.datum.BirdAnimationDatum;
import net.fodoth.skina.neoguanniao.content.bird.core.data.datum.BirdFlightProfile;
import net.fodoth.skina.neoguanniao.content.bird.core.data.datum.BirdFlyingDatum;
import net.fodoth.skina.neoguanniao.content.bird.core.data.datum.BirdModelScaleProfile;
import net.fodoth.skina.neoguanniao.content.bird.core.data.datum.BirdModelSkinDatum;
import net.fodoth.skina.neoguanniao.content.bird.core.data.datum.BirdSoundDatum;
import net.fodoth.skina.neoguanniao.content.bird.core.model.BirdModel;
import net.fodoth.skina.neoguanniao.content.bird.core.skin.BirdSkin;
import net.fodoth.skina.neoguanniao.content.bird.core.skin.BirdSkinRarity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import software.bernie.geckolib.animation.RawAnimation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

final class NeoGuanNiaoBirdDataHelper {

    private NeoGuanNiaoBirdDataHelper() {
    }

    static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(NeoGuanNiao.MODID, path);
    }

    static BirdModel model(String id, String geometry) {
        return BirdModel.createDefault()
                .withId(resource(id))
                .withLocation(resource("geo/" + geometry + ".geo.json"));
    }

    static BirdSkin skin(String id, String texture, BirdSkinRarity rarity) {
        return BirdSkin.createDefault()
                .withId(resource(id))
                .withLocation(resource("textures/entity/" + texture + ".png"))
                .withRarity(rarity);
    }

    static List<BirdSkin> genderedSkins(String folder, SkinVariant... variants) {
        List<BirdSkin> skins = new ArrayList<>(variants.length * 2);
        for (SkinVariant variant : variants) {
            String texture = folder + "/" + variant.id();
            BirdSkin base = skin(variant.id(), texture, variant.rarity())
                    .withNatureSpawn(variant.natureSpawn())
                    .withBreed(variant.breed())
                    .withUnique(variant.unique());
            skins.add(base.withId(resource(variant.id() + "_male"))
                    .withLocation(resource("textures/entity/" + texture + "_male.png"))
                    .withFemale(false));
            skins.add(base.withId(resource(variant.id() + "_female"))
                    .withLocation(resource("textures/entity/" + texture + "_female.png"))
                    .withMale(false));
        }
        return List.copyOf(skins);
    }

    static List<BirdSkin> ageSkins(String folder, AgeSkinVariant... variants) {
        List<BirdSkin> skins = new ArrayList<>(variants.length * 2);
        for (AgeSkinVariant variant : variants) {
            BirdSkin base = skin(variant.id(), folder + "/" + variant.id(), variant.rarity())
                    .withNatureSpawn(variant.natureSpawn())
                    .withUnique(variant.unique());
            skins.add(base.withBaby(false));
            skins.add(base.withId(resource(variant.id() + "_baby"))
                    .withLocation(resource("textures/entity/" + folder + "/" + variant.id() + "_baby.png"))
                    .withMature(false));
        }
        return List.copyOf(skins);
    }

    static BirdModelSkinDatum modelData(String modelId, String geometry, List<BirdSkin> skins,
                                        BirdModelScaleProfile scaleProfile, float shadowRadius,
                                        float globalScale) {
        return BirdModelSkinDatum.createDefault()
                .withBirdModel(List.of(model(modelId, geometry)))
                .withBirdSkin(skins)
                .withModelScaleProfile(scaleProfile)
                .withShadowRadius(shadowRadius)
                .withGlobalScale(globalScale);
    }

    static BirdSoundDatum sound(int interval, SoundEvent ambient, SoundEvent hurt,
                                SoundEvent death, SoundEvent interact, SoundEvent eat) {
        return new BirdSoundDatum(0.5F, interval, ambient, hurt, death, interact, eat);
    }

    static BirdFlyingDatum flying(BirdFlightProfile profile) {
        return BirdFlyingDatum.createDefault().withFlightProfile(profile);
    }

    static BirdAnimationDatum animation(String model, String file,
                                         Map<String, RawAnimation> animations) {
        return animations(Map.of(resource(model), resource("animations/" + file + ".animation.json")),
                animations);
    }

    static BirdAnimationDatum animations(Map<ResourceLocation, ResourceLocation> modelAnimations,
                                          Map<String, RawAnimation> animations) {
        return BirdAnimationDatum.withAnimationIdAndMap(modelAnimations, animations);
    }

    static List<BirdSkin> budgerigarSkins() {
        return genderedSkins("budgerigar",
                skinVariant("green", BirdSkinRarity.COMMON),
                skinVariant("yellow", BirdSkinRarity.COMMON),
                skinVariant("blue_white", BirdSkinRarity.COMMON),
                skinVariant("white_lark", BirdSkinRarity.UNCOMMON),
                skinVariant("blue_lark", BirdSkinRarity.UNCOMMON),
                skinVariant("yellow_lark", BirdSkinRarity.RARE),
                skinVariant("yellow_black", BirdSkinRarity.RARE),
                specialSkinVariant("mystery_green", BirdSkinRarity.EPIC, false, true, false),
                specialSkinVariant("blue_porcelain", BirdSkinRarity.EPIC, false, true, false),
                specialSkinVariant("black_white", BirdSkinRarity.LEGENDARY, false, true, false),
                specialSkinVariant("golden", BirdSkinRarity.UNIQUE, false, false, true));
    }

    static List<BirdSkin> nightHeronSkins() {
        List<BirdSkin> skins = new ArrayList<>(ageSkins("night_heron",
                ageSkinVariant("night_heron_0", BirdSkinRarity.COMMON, true, false),
                ageSkinVariant("night_heron_1", BirdSkinRarity.COMMON, true, false),
                ageSkinVariant("night_heron_2", BirdSkinRarity.UNCOMMON, true, false),
                ageSkinVariant("night_heron_3", BirdSkinRarity.UNCOMMON, true, false),
                ageSkinVariant("night_heron_4", BirdSkinRarity.RARE, false, false),
                ageSkinVariant("night_heron_5", BirdSkinRarity.EPIC, false, false),
                ageSkinVariant("night_heron_6", BirdSkinRarity.LEGENDARY, false, false),
                ageSkinVariant("night_heron_7", BirdSkinRarity.ANCIENT, false, false),
                ageSkinVariant("night_heron_golden", BirdSkinRarity.UNIQUE, false, true)));
        skins.add(skin("cheng_he_guang", "night_heron/cheng_he_guang", BirdSkinRarity.UNIQUE)
                .withUnique(true).withNatureSpawn(false).withFemale(false));
        return List.copyOf(skins);
    }

    static List<BirdModel> nightHeronModels() {
        return List.of(
                model("night_heron", "night_heron").withBlackList(Set.of(resource("cheng_he_guang"))),
                model("cheng_he_guang", "cheng_he_guang")
                        .withRarity(net.fodoth.skina.neoguanniao.content.bird.core.model.BirdModelRarity.UNIQUE)
                        .withFemale(false).withNatureSpawn(false)
                        .withWhiteList(Set.of(resource("cheng_he_guang"))));
    }

    static Map<String, RawAnimation> budgerigarAnimations() {
        return Map.of(
                "idle", loop("idle"),
                "preen", playThenIdle("idle_diff_1"),
                "curious", playThenIdle("idle_diff_2"),
                "dance", loop("idle_diff_3"),
                "eat", playThenIdle("eat"),
                "sleep", playThenLoop("sleep", "sleep_loop"),
                "sleep_loop", loop("sleep_loop"),
                "walk", loop("walk"),
                "fly", loop("fly_flapping_wing_loop"));
    }

    static Map<String, RawAnimation> nightHeronAnimations() {
        return Map.ofEntries(
                Map.entry("idle", loop("idle")),
                Map.entry("preen", loop("idle_diff_4").thenLoop("idle")),
                Map.entry("idle_1", playThenIdle("idle_diff_1")),
                Map.entry("idle_2", playThenIdle("idle_diff_2")),
                Map.entry("idle_3", loop("idle_diff_3").thenLoop("idle")),
                Map.entry("curious", loop("idle_diff_5").thenLoop("idle")),
                Map.entry("walk", loop("walk")),
                Map.entry("run", loop("run")),
                Map.entry("fly", loop("fly_flapping_wing_loop")),
                Map.entry("fly_glide", loop("fly_loop")),
                Map.entry("eat", playThenIdle("eat")),
                Map.entry("sleep", playThenLoop("sleep", "sleep_loop")),
                Map.entry("sleep_loop", loop("sleep_loop")));
    }

    static Map<String, RawAnimation> columbidAnimations() {
        return Map.ofEntries(
                Map.entry("idle", loop("idle")),
                Map.entry("preen", loop("idle_diff_1").thenLoop("idle")),
                Map.entry("idle_1", playThenIdle("idle_diff_2")),
                Map.entry("curious", loop("idle_diff_3").thenLoop("idle")),
                Map.entry("walk", loop("walk")),
                Map.entry("fly", loop("fly_flapping_wing_loop")),
                Map.entry("fly_glide", loop("fly_loop")),
                Map.entry("eat", playThenIdle("eat")),
                Map.entry("sleep", playThenLoop("sleep", "sleep_loop")),
                Map.entry("sleep_loop", loop("sleep_loop")));
    }

    static Map<String, RawAnimation> sparrowAnimations() {
        return Map.ofEntries(
                Map.entry("idle", loop("idle")),
                Map.entry("preen", loop("idle")),
                Map.entry("idle_1", playThenIdle("idle_diff_1")),
                Map.entry("curious", loop("idle_diff_3").thenLoop("idle")),
                Map.entry("walk", loop("walk")),
                Map.entry("fly", loop("fly")),
                Map.entry("eat", playThenIdle("eat")),
                Map.entry("sleep", playThenLoop("sleep", "sleep_loop")),
                Map.entry("sleep_loop", loop("sleep_loop")));
    }

    static SkinVariant skinVariant(String id, BirdSkinRarity rarity) {
        return new SkinVariant(id, rarity, true, true, false);
    }

    static SkinVariant specialSkinVariant(String id, BirdSkinRarity rarity,
                                          boolean natureSpawn, boolean breed, boolean unique) {
        return new SkinVariant(id, rarity, natureSpawn, breed, unique);
    }

    static AgeSkinVariant ageSkinVariant(String id, BirdSkinRarity rarity, boolean natureSpawn,
                                         boolean unique) {
        return new AgeSkinVariant(id, rarity, natureSpawn, unique);
    }

    static RawAnimation loop(String animation) {
        return RawAnimation.begin().thenLoop(animation);
    }

    static RawAnimation playThenIdle(String animation) {
        return RawAnimation.begin().thenPlay(animation).thenLoop("idle");
    }

    static RawAnimation playThenLoop(String animation, String loop) {
        return RawAnimation.begin().thenPlay(animation).thenLoop(loop);
    }

    static BirdData simpleBird(String id, String assetName, List<SkinVariant> skins,
                               BirdFlightProfile flightProfile, BirdModelScaleProfile scaleProfile,
                               float shadowRadius, float globalScale, String animationName,
                               Map<String, String> animations) {
        BirdSoundDatum sound = switch (assetName) {
            case "kiwi" -> sound(240, NeoGuanNiaoSoundEvents.KIWI_AMBIENT.get(),
                    NeoGuanNiaoSoundEvents.KIWI_HURT.get(), NeoGuanNiaoSoundEvents.KIWI_DEATH.get(),
                    NeoGuanNiaoSoundEvents.KIWI_AMBIENT.get(), SoundEvents.PARROT_EAT);
            case "myna" -> sound(240, NeoGuanNiaoSoundEvents.MYNA_AMBIENT.get(),
                    NeoGuanNiaoSoundEvents.MYNA_HURT.get(), NeoGuanNiaoSoundEvents.MYNA_DEATH.get(),
                    NeoGuanNiaoSoundEvents.MYNA_AMBIENT.get(), SoundEvents.PARROT_EAT);
            default -> new BirdSoundDatum(0.5F, 240, SoundEvents.PARROT_AMBIENT,
                    SoundEvents.PARROT_HURT, SoundEvents.PARROT_DEATH,
                    SoundEvents.PARROT_AMBIENT, SoundEvents.PARROT_EAT);
        };
        return BirdData.createDefault()
                .withSound(sound)
                .withFlying(BirdFlyingDatum.createDefault().withFlightProfile(flightProfile))
                .withModel(BirdModelSkinDatum.createDefault()
                        .withBirdModel(List.of(model(assetName, assetName)))
                        .withBirdSkin(genderedSkins(assetName, skins.toArray(SkinVariant[]::new)))
                        .withModelScaleProfile(scaleProfile)
                        .withShadowRadius(shadowRadius)
                        .withGlobalScale(globalScale))
                .withAnimation(BirdAnimationDatum.withAnimationIdAndMap(
                        Map.of(resource(assetName), resource("animations/" + animationName + ".animation.json")),
                        animations.entrySet().stream().collect(Collectors.toUnmodifiableMap(
                                Map.Entry::getKey,
                                entry -> entry.getKey().startsWith("idle_")
                                        ? playThenLoop(entry.getValue(), animations.get("idle"))
                                        : loop(entry.getValue())))));
    }

    record SkinVariant(String id, BirdSkinRarity rarity, boolean natureSpawn,
                       boolean breed, boolean unique) {
    }

    record AgeSkinVariant(String id, BirdSkinRarity rarity, boolean natureSpawn, boolean unique) {
    }
}
