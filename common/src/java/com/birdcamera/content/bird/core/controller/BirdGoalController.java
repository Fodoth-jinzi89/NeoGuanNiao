package com.birdcamera.content.bird.core.controller;

import java.util.List;
import com.birdcamera.content.bird.core.AbstractBirdEntity;
import com.birdcamera.content.bird.core.controller.goal.AbstractGoalController;
import com.birdcamera.content.bird.core.controller.goal.BirdBathUseGoalController;
import com.birdcamera.content.bird.core.controller.goal.BirdBreedGoalController;
import com.birdcamera.content.bird.core.controller.goal.BirdCuriousFollowGoalController;
import com.birdcamera.content.bird.core.controller.goal.BirdEatFoodGoalController;
import com.birdcamera.content.bird.core.controller.goal.BirdFlockGoalController;
import com.birdcamera.content.bird.core.controller.goal.BirdFollowOwnerGoalController;
import com.birdcamera.content.bird.core.controller.goal.BirdIdleGoalController;
import com.birdcamera.content.bird.core.controller.goal.BirdModelValidateGoalController;
import com.birdcamera.content.bird.core.controller.goal.BirdMusicDanceGoalController;
import com.birdcamera.content.bird.core.controller.goal.BirdRandomLookAroundGoalController;
import com.birdcamera.content.bird.core.controller.goal.BirdRandomWalkAroundGoalController;
import com.birdcamera.content.bird.core.controller.goal.BirdRoostGoalController;
import com.birdcamera.content.bird.core.controller.goal.BirdSentinelGoalController;
import com.birdcamera.content.bird.core.controller.goal.BirdSkinValidateGoalController;
import com.birdcamera.content.bird.core.controller.goal.BirdWakeUpGoalController;

public class BirdGoalController<T extends AbstractBirdEntity<T>> extends AbstractBirdController<T> {
   private final BirdCuriousFollowGoalController<T> birdCuriousFollowGoalController;
   private final BirdEatFoodGoalController<T> birdEatFoodGoalController;
   private final BirdFlockGoalController<T> birdFlockGoalController;
   private final BirdFollowOwnerGoalController<T> birdFollowOwnerGoalController;
   private final BirdIdleGoalController<T> birdIdleGoalController;
   private final BirdMusicDanceGoalController<T> birdMusicDanceGoalController;
   private final BirdRandomLookAroundGoalController<T> birdRandomLookAroundGoalController;
   private final BirdRoostGoalController<T> birdRoostGoalController;
   private final BirdSentinelGoalController<T> birdSentinelGoalController;
   private final BirdWakeUpGoalController<T> birdWakeUpGoalController;
   private final BirdBathUseGoalController<T> birdBathUseGoalController;
   private final BirdBreedGoalController<T> birdBreedGoalController;
   private final BirdSkinValidateGoalController<T> birdSkinValidateGoalController;
   private final BirdModelValidateGoalController<T> birdModelValidateGoalController;
   private final BirdRandomWalkAroundGoalController<T> birdRandomWalkAroundGoalController;
   private final List<AbstractGoalController<T>> controllers;

   private BirdGoalController(BirdGoalController.Builder<T> builder) {
      this.birdCuriousFollowGoalController = builder.birdCuriousFollowGoalController;
      this.birdEatFoodGoalController = builder.birdEatFoodGoalController;
      this.birdFlockGoalController = builder.birdFlockGoalController;
      this.birdFollowOwnerGoalController = builder.birdFollowOwnerGoalController;
      this.birdIdleGoalController = builder.birdIdleGoalController;
      this.birdMusicDanceGoalController = builder.birdMusicDanceGoalController;
      this.birdRandomLookAroundGoalController = builder.birdRandomLookAroundGoalController;
      this.birdRoostGoalController = builder.birdRoostGoalController;
      this.birdSentinelGoalController = builder.birdSentinelGoalController;
      this.birdWakeUpGoalController = builder.birdWakeUpGoalController;
      this.birdBathUseGoalController = builder.birdBathUseGoalController;
      this.birdBreedGoalController = builder.birdBreedGoalController;
      this.birdSkinValidateGoalController = builder.birdSkinValidateGoalController;
      this.birdModelValidateGoalController = builder.birdModelValidateGoalController;
      this.birdRandomWalkAroundGoalController = builder.birdRandomWalkAroundGoalController;
      this.controllers = List.of(
         this.birdCuriousFollowGoalController,
         this.birdEatFoodGoalController,
         this.birdFlockGoalController,
         this.birdFollowOwnerGoalController,
         this.birdIdleGoalController,
         this.birdMusicDanceGoalController,
         this.birdRandomLookAroundGoalController,
         this.birdRoostGoalController,
         this.birdSentinelGoalController,
         this.birdWakeUpGoalController,
         this.birdBathUseGoalController,
         this.birdBreedGoalController,
         this.birdSkinValidateGoalController,
         this.birdModelValidateGoalController,
         this.birdRandomWalkAroundGoalController
      );
   }

   @Override
   protected void onAttach() {
      this.controllers.forEach(controller -> controller.attach(this.bird()));
   }

   public static <T extends AbstractBirdEntity<T>> BirdGoalController.Builder<T> builder() {
      return new BirdGoalController.Builder<>();
   }

   public static <T extends AbstractBirdEntity<T>> BirdGoalController<T> withBird(T bird) {
      BirdGoalController<T> controller = BirdGoalController.<T>builder().build();
      controller.attach(bird);
      return controller;
   }

   public BirdCuriousFollowGoalController<T> getBirdCuriousFollowGoalController() {
      return this.birdCuriousFollowGoalController;
   }

   public BirdEatFoodGoalController<T> getBirdEatFoodGoalController() {
      return this.birdEatFoodGoalController;
   }

   public BirdFlockGoalController<T> getBirdFlockGoalController() {
      return this.birdFlockGoalController;
   }

   public BirdFollowOwnerGoalController<T> getBirdFollowOwnerGoalController() {
      return this.birdFollowOwnerGoalController;
   }

   public BirdIdleGoalController<T> getBirdIdleGoalController() {
      return this.birdIdleGoalController;
   }

   public BirdMusicDanceGoalController<T> getBirdMusicDanceGoalController() {
      return this.birdMusicDanceGoalController;
   }

   public BirdRandomLookAroundGoalController<T> getBirdRandomLookAroundGoalController() {
      return this.birdRandomLookAroundGoalController;
   }

   public BirdRoostGoalController<T> getBirdRoostGoalController() {
      return this.birdRoostGoalController;
   }

   public BirdSentinelGoalController<T> getBirdSentinelGoalController() {
      return this.birdSentinelGoalController;
   }

   public BirdWakeUpGoalController<T> getBirdWakeUpGoalController() {
      return this.birdWakeUpGoalController;
   }

   public BirdBathUseGoalController<T> getBirdBathUseGoalController() {
      return this.birdBathUseGoalController;
   }

   public BirdBreedGoalController<T> getBirdBreedGoalController() {
      return this.birdBreedGoalController;
   }

   public BirdSkinValidateGoalController<T> getBirdSkinValidateGoalController() {
      return this.birdSkinValidateGoalController;
   }

   public BirdModelValidateGoalController<T> getBirdModelValidateGoalController() {
      return this.birdModelValidateGoalController;
   }

   public BirdRandomWalkAroundGoalController<T> getBirdRandomWalkAroundGoalController() {
      return this.birdRandomWalkAroundGoalController;
   }

   public List<AbstractGoalController<T>> getControllers() {
      return this.controllers;
   }

   public BirdCuriousFollowGoalController<T> birdCuriousFollowGoalController() {
      return this.birdCuriousFollowGoalController;
   }

   public BirdEatFoodGoalController<T> birdEatFoodGoalController() {
      return this.birdEatFoodGoalController;
   }

   public BirdFlockGoalController<T> birdFlockGoalController() {
      return this.birdFlockGoalController;
   }

   public BirdFollowOwnerGoalController<T> birdFollowOwnerGoalController() {
      return this.birdFollowOwnerGoalController;
   }

   public BirdIdleGoalController<T> birdIdleGoalController() {
      return this.birdIdleGoalController;
   }

   public BirdMusicDanceGoalController<T> birdMusicDanceGoalController() {
      return this.birdMusicDanceGoalController;
   }

   public BirdRandomLookAroundGoalController<T> birdRandomLookAroundGoalController() {
      return this.birdRandomLookAroundGoalController;
   }

   public BirdRoostGoalController<T> birdRoostGoalController() {
      return this.birdRoostGoalController;
   }

   public BirdSentinelGoalController<T> birdSentinelGoalController() {
      return this.birdSentinelGoalController;
   }

   public BirdWakeUpGoalController<T> birdWakeUpGoalController() {
      return this.birdWakeUpGoalController;
   }

   public BirdBathUseGoalController<T> birdBathUseGoalController() {
      return this.birdBathUseGoalController;
   }

   public BirdBreedGoalController<T> birdBreedGoalController() {
      return this.birdBreedGoalController;
   }

   public BirdSkinValidateGoalController<T> birdSkinValidateGoalController() {
      return this.birdSkinValidateGoalController;
   }

   public BirdModelValidateGoalController<T> birdModelValidateGoalController() {
      return this.birdModelValidateGoalController;
   }

   public BirdRandomWalkAroundGoalController<T> birdRandomWalkAroundGoalController() {
      return this.birdRandomWalkAroundGoalController;
   }

   public static final class Builder<T extends AbstractBirdEntity<T>> {
      private BirdCuriousFollowGoalController<T> birdCuriousFollowGoalController = new BirdCuriousFollowGoalController<>();
      private BirdEatFoodGoalController<T> birdEatFoodGoalController = new BirdEatFoodGoalController<>();
      private BirdFlockGoalController<T> birdFlockGoalController = new BirdFlockGoalController<>();
      private BirdFollowOwnerGoalController<T> birdFollowOwnerGoalController = new BirdFollowOwnerGoalController<>();
      private BirdIdleGoalController<T> birdIdleGoalController = new BirdIdleGoalController<>();
      private BirdMusicDanceGoalController<T> birdMusicDanceGoalController = new BirdMusicDanceGoalController<>();
      private BirdRandomLookAroundGoalController<T> birdRandomLookAroundGoalController = new BirdRandomLookAroundGoalController<>();
      private BirdRoostGoalController<T> birdRoostGoalController = new BirdRoostGoalController<>();
      private BirdSentinelGoalController<T> birdSentinelGoalController = new BirdSentinelGoalController<>();
      private BirdWakeUpGoalController<T> birdWakeUpGoalController = new BirdWakeUpGoalController<>();
      private BirdBathUseGoalController<T> birdBathUseGoalController = new BirdBathUseGoalController<>();
      private BirdBreedGoalController<T> birdBreedGoalController = new BirdBreedGoalController<>();
      private BirdSkinValidateGoalController<T> birdSkinValidateGoalController = new BirdSkinValidateGoalController<>();
      private BirdModelValidateGoalController<T> birdModelValidateGoalController = new BirdModelValidateGoalController<>();
      private BirdRandomWalkAroundGoalController<T> birdRandomWalkAroundGoalController = new BirdRandomWalkAroundGoalController<>();

      public BirdGoalController.Builder<T> birdCuriousFollowGoalController(BirdCuriousFollowGoalController<T> controller) {
         this.birdCuriousFollowGoalController = controller;
         return this;
      }

      public BirdGoalController.Builder<T> birdEatFoodGoalController(BirdEatFoodGoalController<T> controller) {
         this.birdEatFoodGoalController = controller;
         return this;
      }

      public BirdGoalController.Builder<T> birdFlockGoalController(BirdFlockGoalController<T> controller) {
         this.birdFlockGoalController = controller;
         return this;
      }

      public BirdGoalController.Builder<T> birdFollowOwnerGoalController(BirdFollowOwnerGoalController<T> controller) {
         this.birdFollowOwnerGoalController = controller;
         return this;
      }

      public BirdGoalController.Builder<T> birdIdleGoalController(BirdIdleGoalController<T> controller) {
         this.birdIdleGoalController = controller;
         return this;
      }

      public BirdGoalController.Builder<T> birdMusicDanceGoalController(BirdMusicDanceGoalController<T> controller) {
         this.birdMusicDanceGoalController = controller;
         return this;
      }

      public BirdGoalController.Builder<T> birdRandomLookAroundGoalController(BirdRandomLookAroundGoalController<T> controller) {
         this.birdRandomLookAroundGoalController = controller;
         return this;
      }

      public BirdGoalController.Builder<T> birdRoostGoalController(BirdRoostGoalController<T> controller) {
         this.birdRoostGoalController = controller;
         return this;
      }

      public BirdGoalController.Builder<T> birdSentinelGoalController(BirdSentinelGoalController<T> controller) {
         this.birdSentinelGoalController = controller;
         return this;
      }

      public BirdGoalController.Builder<T> birdWakeUpGoalController(BirdWakeUpGoalController<T> controller) {
         this.birdWakeUpGoalController = controller;
         return this;
      }

      public BirdGoalController.Builder<T> birdBathUseGoalController(BirdBathUseGoalController<T> controller) {
         this.birdBathUseGoalController = controller;
         return this;
      }

      public BirdGoalController.Builder<T> birdBreedGoalController(BirdBreedGoalController<T> controller) {
         this.birdBreedGoalController = controller;
         return this;
      }

      public BirdGoalController.Builder<T> birdSkinValidateGoalController(BirdSkinValidateGoalController<T> controller) {
         this.birdSkinValidateGoalController = controller;
         return this;
      }

      public BirdGoalController.Builder<T> birdModelValidateGoalController(BirdModelValidateGoalController<T> controller) {
         this.birdModelValidateGoalController = controller;
         return this;
      }

      public BirdGoalController.Builder<T> birdRandomWalkAroundGoalController(BirdRandomWalkAroundGoalController<T> controller) {
         this.birdRandomWalkAroundGoalController = controller;
         return this;
      }

      public BirdGoalController<T> build() {
         return new BirdGoalController<>(this);
      }
   }
}
