package com.birdcamera.registry;

import com.birdcamera.BirdCameraMod;
import java.util.List;
import java.util.Map;
import com.birdcamera.content.bird.core.data.BirdData;
import com.birdcamera.content.bird.core.data.datum.BirdFlightProfile;
import com.birdcamera.content.bird.core.data.datum.BirdGoalDatum;
import com.birdcamera.content.bird.core.data.datum.BirdMiscDatum;
import com.birdcamera.content.bird.core.data.datum.BirdModelScaleProfile;
import com.birdcamera.content.bird.core.data.datum.BirdModelSkinDatum;
import com.birdcamera.content.bird.core.skin.BirdSkinRarity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.function.Supplier;

public final class BirdCameraBirdData {
   private static final java.util.Map<ResourceLocation, BirdData> REGISTRY = new LinkedHashMap<>();
   public static final java.util.Map<ResourceLocation, BirdData> VIEW = Collections.unmodifiableMap(REGISTRY);

   private static BirdData register(String id, Supplier<BirdData> supplier) {
      BirdData birdData = supplier.get();
      REGISTRY.put(BirdCameraMod.id(id), birdData);
      return birdData;
   }
   public static final BirdData BUDGERIGAR = register("neo_budgerigar", () -> BirdData.createDefault()
            .withSound(
               BirdCameraBirdDataHelper.sound(
                  180,
                  (SoundEvent)BirdCameraSoundEvents.BUDGERIGAR_AMBIENT,
                  (SoundEvent)BirdCameraSoundEvents.BUDGERIGAR_HURT,
                  (SoundEvent)BirdCameraSoundEvents.BUDGERIGAR_DEATH,
                  (SoundEvent)BirdCameraSoundEvents.BUDGERIGAR_INTERACT,
                  SoundEvents.PARROT_EAT
               )
            )
            .withFlying(BirdCameraBirdDataHelper.flying(BirdFlightProfile.BUDGERIGAR))
            .withModel(
               BirdCameraBirdDataHelper.modelData(
                  "budgerigar", "budgerigar", BirdCameraBirdDataHelper.budgerigarSkins(), BirdModelScaleProfile.BUDGERIGAR, 0.12F, 1.0F
               )
            )
            .withAnimation(
               BirdCameraBirdDataHelper.animation("budgerigar", "budgerigar", BirdCameraBirdDataHelper.budgerigarAnimations())
                  .withCuriousAndTrustingIndexRange(2, 3)
            )
            .withMisc(BirdMiscDatum.createDefault().withSpawnRarity(5))
   );
   public static final BirdData NIGHT_HERON = register("neo_night_heron", () -> BirdData.createDefault()
            .withSound(
               BirdCameraBirdDataHelper.sound(
                  240,
                  (SoundEvent)BirdCameraSoundEvents.NIGHT_HERON_AMBIENT,
                  (SoundEvent)BirdCameraSoundEvents.NIGHT_HERON_HURT,
                  (SoundEvent)BirdCameraSoundEvents.NIGHT_HERON_DEATH,
                  (SoundEvent)BirdCameraSoundEvents.NIGHT_HERON_AMBIENT,
                  SoundEvents.ARMADILLO_EAT
               )
            )
            .withFlying(BirdCameraBirdDataHelper.flying(BirdFlightProfile.NIGHT_HERON))
            .withModel(
               BirdModelSkinDatum.createDefault()
                  .withBirdModel(BirdCameraBirdDataHelper.nightHeronModels())
                  .withBirdSkin(BirdCameraBirdDataHelper.nightHeronSkins())
                  .withModelScaleProfile(BirdModelScaleProfile.NIGHT_HERON)
                  .withShadowRadius(0.25F)
                  .withGlobalScale(1.0F)
            )
            .withAnimation(
               BirdCameraBirdDataHelper.animations(
                     Map.of(
                        BirdCameraBirdDataHelper.resource("night_heron"),
                        BirdCameraBirdDataHelper.resource("animations/night_heron.animation.json"),
                        BirdCameraBirdDataHelper.resource("cheng_he_guang"),
                        BirdCameraBirdDataHelper.resource("animations/cheng_he_guang.animation.json")
                     ),
                     BirdCameraBirdDataHelper.nightHeronAnimations()
                  )
                  .withCuriousAndTrustingIndexRange(5, 5)
            )
            .withMisc(BirdMiscDatum.createDefault().withSpawnRarity(2).withActiveTime(11000L, 1500L))
            .withGoal(BirdGoalDatum.createDefault().withBreedDistance(2.0).withBreedMoveSpeed(1.5).withBathUseConsumeChance(1.0F))
   );
   public static final BirdData PIGEON = register("neo_pigeon", () -> BirdData.createDefault()
            .withSound(
               BirdCameraBirdDataHelper.sound(
                  480,
                  (SoundEvent)BirdCameraSoundEvents.PIGEON_AMBIENT,
                  (SoundEvent)BirdCameraSoundEvents.SPOTTED_DOVE_HURT,
                  (SoundEvent)BirdCameraSoundEvents.SPOTTED_DOVE_DEATH,
                  (SoundEvent)BirdCameraSoundEvents.PIGEON_AMBIENT,
                  SoundEvents.PARROT_EAT
               )
            )
            .withFlying(BirdCameraBirdDataHelper.flying(BirdFlightProfile.COLUMBID))
            .withModel(
               BirdCameraBirdDataHelper.modelData(
                  "pigeon",
                  "columbid",
                  BirdCameraBirdDataHelper.genderedSkins(
                     "pigeon",
                     BirdCameraBirdDataHelper.skinVariant("pigeon_gray", BirdSkinRarity.COMMON),
                     BirdCameraBirdDataHelper.skinVariant("pigeon_white", BirdSkinRarity.COMMON)
                  ),
                  BirdModelScaleProfile.COLUMBID,
                  0.25F,
                  1.0F
               )
            )
            .withAnimation(
               BirdCameraBirdDataHelper.animation("pigeon", "columbid", BirdCameraBirdDataHelper.columbidAnimations()).withCuriousAndTrustingIndexRange(3, 3)
            )
            .withMisc(BirdMiscDatum.createDefault().withSpawnRarity(5))
            .withGoal(BirdGoalDatum.createDefault().withBreedDistance(1.5).withBreedMoveSpeed(1.1).withBathUseConsumeChance(0.5F))
   );
   public static final BirdData DOVE = register("neo_dove", () -> BirdData.createDefault()
            .withSound(
               BirdCameraBirdDataHelper.sound(
                  480,
                  (SoundEvent)BirdCameraSoundEvents.SPOTTED_DOVE_AMBIENT,
                  (SoundEvent)BirdCameraSoundEvents.SPOTTED_DOVE_HURT,
                  (SoundEvent)BirdCameraSoundEvents.SPOTTED_DOVE_DEATH,
                  (SoundEvent)BirdCameraSoundEvents.SPOTTED_DOVE_MATE,
                  SoundEvents.PARROT_EAT
               )
            )
            .withFlying(BirdCameraBirdDataHelper.flying(BirdFlightProfile.COLUMBID))
            .withModel(
               BirdCameraBirdDataHelper.modelData(
                  "dove",
                  "columbid",
                  BirdCameraBirdDataHelper.genderedSkins(
                     "dove",
                     BirdCameraBirdDataHelper.skinVariant("spotted_dove", BirdSkinRarity.COMMON),
                     BirdCameraBirdDataHelper.skinVariant("orienta_turtle_dove", BirdSkinRarity.UNCOMMON),
                     BirdCameraBirdDataHelper.skinVariant("treron", BirdSkinRarity.RARE)
                  ),
                  BirdModelScaleProfile.COLUMBID,
                  0.25F,
                  1.0F
               )
            )
            .withAnimation(
               BirdCameraBirdDataHelper.animation("dove", "columbid", BirdCameraBirdDataHelper.columbidAnimations()).withCuriousAndTrustingIndexRange(3, 3)
            )
            .withMisc(BirdMiscDatum.createDefault().withSpawnRarity(4))
            .withGoal(BirdGoalDatum.createDefault().withBreedDistance(1.5).withBreedMoveSpeed(1.1).withBathUseConsumeChance(0.6F))
   );
   public static final BirdData SPARROW = register("neo_sparrow", () -> BirdData.createDefault()
            .withSound(
               BirdCameraBirdDataHelper.sound(
                  240,
                  (SoundEvent)BirdCameraSoundEvents.SPARROW_AMBIENT,
                  (SoundEvent)BirdCameraSoundEvents.SPARROW_HURT,
                  (SoundEvent)BirdCameraSoundEvents.SPARROW_DEATH,
                  (SoundEvent)BirdCameraSoundEvents.SPARROW_AMBIENT,
                  SoundEvents.PARROT_EAT
               )
            )
            .withFlying(BirdCameraBirdDataHelper.flying(BirdFlightProfile.SPARROW))
            .withModel(
               BirdCameraBirdDataHelper.modelData(
                  "sparrow",
                  "sparrow",
                  BirdCameraBirdDataHelper.genderedSkins("sparrow", BirdCameraBirdDataHelper.skinVariant("sparrow", BirdSkinRarity.COMMON)),
                  BirdModelScaleProfile.SPARROW,
                  0.18F,
                  0.9F
               )
            )
            .withAnimation(
               BirdCameraBirdDataHelper.animation("sparrow", "sparrow", BirdCameraBirdDataHelper.sparrowAnimations()).withCuriousAndTrustingIndexRange(3, 3)
            )
            .withMisc(BirdMiscDatum.createDefault().withSpawnRarity(6))
            .withGoal(BirdGoalDatum.createDefault().withBreedDistance(1.2).withBreedMoveSpeed(1.1).withBathUseConsumeChance(0.25F))
   );
   public static final BirdData COCKATIEL = registerSimpleBird(
      "neo_cockatiel",
      "cockatiel",
      List.of(
         BirdCameraBirdDataHelper.skinVariant("dark_gray_yellow_face", BirdSkinRarity.COMMON),
         BirdCameraBirdDataHelper.skinVariant("gray_yellow_face", BirdSkinRarity.COMMON),
         BirdCameraBirdDataHelper.skinVariant("gray_white_face", BirdSkinRarity.UNCOMMON),
         BirdCameraBirdDataHelper.skinVariant("pale_yellow", BirdSkinRarity.UNCOMMON),
         BirdCameraBirdDataHelper.skinVariant("white_yellow_face", BirdSkinRarity.RARE)
      ),
      BirdFlightProfile.BUDGERIGAR,
      BirdModelScaleProfile.BUDGERIGAR,
      0.2F,
      1.3F,
      4,
      "cockatiel",
      Map.ofEntries(
         Map.entry("idle", "idle"),
         Map.entry("preen", "idle_diff_2"),
         Map.entry("curious", "idle_diff_1"),
         Map.entry("dance", "idle_diff_1"),
         Map.entry("idle_1", "idle_diff_3"),
         Map.entry("walk", "walk"),
         Map.entry("fly", "fly"),
         Map.entry("eat", "eat"),
         Map.entry("sleep", "sleep"),
         Map.entry("sleep_loop", "sleep_loop")
      )
   );
   public static final BirdData LONG_TAILED_TIT = registerSimpleBird(
      "neo_long_tailed_tit",
      "long_tailed_tit",
      List.of(BirdCameraBirdDataHelper.skinVariant("long_tailed_tit", BirdSkinRarity.COMMON)),
      BirdFlightProfile.SPARROW,
      BirdModelScaleProfile.SPARROW,
      0.16F,
      0.72F,
      6,
      "long_tailed_tit",
      Map.ofEntries(
         Map.entry("idle", "idle"),
         Map.entry("preen", "idle_diff_1"),
         Map.entry("curious", "idle_diff_2"),
         Map.entry("idle_1", "idle_diff_3"),
         Map.entry("dance", "idle_diff_3"),
         Map.entry("walk", "walk"),
         Map.entry("fly", "fly_loop"),
         Map.entry("eat", "eat"),
         Map.entry("sleep", "idle"),
         Map.entry("sleep_loop", "idle")
      )
   );
   public static final BirdData MACAW = registerSimpleBird(
      "neo_macaw",
      "macaw",
      List.of(
         BirdCameraBirdDataHelper.skinVariant("scarlet", BirdSkinRarity.COMMON),
         BirdCameraBirdDataHelper.skinVariant("blue_yellow", BirdSkinRarity.UNCOMMON),
         BirdCameraBirdDataHelper.skinVariant("catalina", BirdSkinRarity.UNCOMMON),
         BirdCameraBirdDataHelper.skinVariant("hyacinth", BirdSkinRarity.RARE),
         BirdCameraBirdDataHelper.skinVariant("glaucous", BirdSkinRarity.EPIC)
      ),
      BirdFlightProfile.COLUMBID,
      BirdModelScaleProfile.COLUMBID,
      0.32F,
      1.04F,
      2,
      "macaw",
      Map.ofEntries(
         Map.entry("idle", "idle"),
         Map.entry("preen", "idle_diff_1"),
         Map.entry("curious", "idle_diff_2"),
         Map.entry("dance", "idle_diff_3"),
         Map.entry("idle_1", "idle_diff_4"),
         Map.entry("walk", "walk"),
         Map.entry("fly", "fly_flapping_wing_loop"),
         Map.entry("eat", "eat"),
         Map.entry("sleep", "sleep"),
         Map.entry("sleep_loop", "sleep_loop")
      )
   );
   public static final BirdData CROW = registerSimpleBird(
      "neo_crow",
      "crow",
      List.of(BirdCameraBirdDataHelper.skinVariant("crow", BirdSkinRarity.COMMON)),
      BirdFlightProfile.COLUMBID,
      BirdModelScaleProfile.COLUMBID,
      0.26F,
      0.8F,
      4,
      "crow",
      Map.ofEntries(
         Map.entry("idle", "idle"),
         Map.entry("preen", "idle_diff_1"),
         Map.entry("curious", "idle_diff_2"),
         Map.entry("dance", "idle"),
         Map.entry("walk", "walk"),
         Map.entry("fly", "fly"),
         Map.entry("fly_glide", "fly_loop"),
         Map.entry("eat", "eat"),
         Map.entry("sleep", "sleep"),
         Map.entry("sleep_1", "sleep2")
      )
   );
   public static final BirdData SEAGULL = registerSimpleBird(
      "neo_seagull",
      "seagull",
      List.of(BirdCameraBirdDataHelper.skinVariant("seagull", BirdSkinRarity.COMMON)),
      BirdFlightProfile.NIGHT_HERON,
      BirdModelScaleProfile.COLUMBID,
      0.3F,
      0.9F,
      5,
      "seagull",
      Map.ofEntries(
         Map.entry("idle", "idle"),
         Map.entry("preen", "idle_diff_1"),
         Map.entry("curious", "idle_diff_3"),
         Map.entry("idle_1", "idle_diff_2"),
         Map.entry("idle_2", "idle_diff_4"),
         Map.entry("idle_3", "idle_diff_5"),
         Map.entry("dance", "idle_diff_4"),
         Map.entry("walk", "walk"),
         Map.entry("fly", "fly_flapping_wing_loop"),
         Map.entry("fly_glide", "fly_loop"),
         Map.entry("eat", "eat"),
         Map.entry("sleep", "sleep"),
         Map.entry("sleep_loop", "sleep_loop")
      )
   );

   private BirdCameraBirdData() {
   }

   private static BirdData registerSimpleBird(
      String id,
      String assetName,
      List<BirdCameraBirdDataHelper.SkinVariant> skins,
      BirdFlightProfile flightProfile,
      BirdModelScaleProfile scaleProfile,
      float shadowRadius,
      float globalScale,
      int localSpawnRarity,
      String animationName,
      Map<String, String> animations
   ) {
      return register(
         id,
         () -> BirdCameraBirdDataHelper.simpleBird(id, assetName, skins, flightProfile, scaleProfile, shadowRadius, globalScale, animationName, animations)
            .withMisc(BirdMiscDatum.createDefault().withSpawnRarity(localSpawnRarity))
      );
   }
}
