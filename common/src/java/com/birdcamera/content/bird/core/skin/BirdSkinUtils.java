package com.birdcamera.content.bird.core.skin;

import java.util.ArrayList;
import java.util.List;
import com.birdcamera.content.bird.core.AbstractBirdEntity;
import com.birdcamera.content.bird.core.model.BirdModel;

public final class BirdSkinUtils {
   private BirdSkinUtils() {
   }

   public static String getSkinFamily(String id) {
      String[] parts = id.split("_");
      StringBuilder family = new StringBuilder();

      for (String part : parts) {
         if (!isModifier(part)) {
            if (!family.isEmpty()) {
               family.append("_");
            }

            family.append(part);
         }
      }

      return family.toString();
   }

   private static boolean isModifier(String part) {
      return switch (part) {
         case "mature", "baby", "male", "female" -> true;
         default -> false;
      };
   }

   public static <T extends AbstractBirdEntity<?>> BirdSkin findReplacement(T bird, BirdSkin current) {
      String family = getSkinFamily(current.id().getPath());
      BirdModel model = bird.getModel();
      BirdSkin best = null;

      for (BirdSkin skin : bird.getBirdData().model().birdSkin()) {
         if (getSkinFamily(skin.id().getPath()).equals(family) && isSkinAvailable(bird, skin) && model.supportsSkin(skin.id())) {
            best = skin;
            if (skin.id().equals(current.id())) {
               return skin;
            }
         }
      }

      return best == null ? current : best;
   }

   public static <T extends AbstractBirdEntity<?>> boolean isSkinAvailable(T bird, BirdSkin skin) {
      if (bird.isBaby() && !skin.baby()) {
         return false;
      } else if (!bird.isBaby() && !skin.mature()) {
         return false;
      } else {
         return bird.isMale() && !skin.male() ? false : bird.isMale() || skin.female();
      }
   }

   public static <T extends AbstractBirdEntity<?>> boolean hasBabyCompatibleSkin(T bird, String family) {
      BirdModel model = bird.getModel();

      for (BirdSkin skin : bird.getBirdData().model().birdSkin()) {
         if (getSkinFamily(skin.id().getPath()).equals(family) && model.supportsSkin(skin.id()) && skin.baby()) {
            return true;
         }
      }

      return false;
   }

   public static <T extends AbstractBirdEntity<?>> boolean hasGenderVariant(T bird, String family) {
      BirdModel model = bird.getModel();
      boolean male = false;
      boolean female = false;

      for (BirdSkin skin : bird.getBirdData().model().birdSkin()) {
         if (getSkinFamily(skin.id().getPath()).equals(family) && model.supportsSkin(skin.id())) {
            male |= skin.male();
            female |= skin.female();
            if (male && female) {
               return true;
            }
         }
      }

      return false;
   }

   public static <T extends AbstractBirdEntity<?>> BirdSkin findUpgradeSkinBeforeAncient(T bird, BirdSkin current) {
      BirdSkinRarity next = BirdSkinRarity.getNextRarityBeforeAncient(current.rarity());
      return next == null ? null : findSkinByRarity(bird, next);
   }

   public static <T extends AbstractBirdEntity<?>> BirdSkin findDowngradeSkinBeforeAncient(T bird, BirdSkin current) {
      BirdSkinRarity previous = BirdSkinRarity.getPreviousRarityBeforeAncient(current.rarity());
      return previous == null ? null : findSkinByRarity(bird, previous);
   }

   private static <T extends AbstractBirdEntity<?>> BirdSkin findSkinByRarity(T bird, BirdSkinRarity rarity) {
      BirdModel model = bird.getModel();

      for (BirdSkin skin : bird.getBirdData().model().birdSkin()) {
         if (skin.rarity() == rarity && model.supportsSkin(skin.id()) && isSkinAvailable(bird, skin)) {
            return skin;
         }
      }

      return null;
   }

   public static <T extends AbstractBirdEntity<?>> BirdSkin findOppositeGenderSkin(T bird, BirdSkin current) {
      String family = getSkinFamily(current.id().getPath());
      boolean targetMale = !bird.isMale();
      BirdModel model = bird.getModel();

      for (BirdSkin skin : bird.getBirdData().model().birdSkin()) {
         if (getSkinFamily(skin.id().getPath()).equals(family) && model.supportsSkin(skin.id()) && isSkinAvailable(bird, skin)) {
            if (skin.male() && skin.female()) {
               return skin;
            }

            if (targetMale && skin.male()) {
               return skin;
            }

            if (!targetMale && skin.female()) {
               return skin;
            }
         }
      }

      return null;
   }

   public static <T extends AbstractBirdEntity<?>> BirdSkin findRandomUniqueSkin(T bird) {
      BirdModel model = bird.getModel();
      List<BirdSkin> candidates = new ArrayList<>();

      for (BirdSkin skin : bird.getBirdData().model().birdSkin()) {
         if (skin.rarity() == BirdSkinRarity.UNIQUE && model.supportsSkin(skin.id()) && isSkinAvailable(bird, skin)) {
            candidates.add(skin);
         }
      }

      return candidates.isEmpty() ? null : candidates.get(bird.getRandom().nextInt(candidates.size()));
   }
}
