package com.birdcamera.content.bird.core.controller;

import java.util.List;
import com.birdcamera.content.bird.core.AbstractBirdEntity;

public final class BirdControllers<T extends AbstractBirdEntity<T>> {
   private final BirdTickController<T> birdTickController;
   private final BirdFlyingController<T> birdFlyingController;
   private final BirdRoutineController<T> birdRoutineController;
   private final BirdEatingController<T> birdEatingController;
   private final BirdTameController<T> birdTameController;
   private final BirdGoalController<T> birdGoalController;
   private final BirdFrightController<T> birdFrightController;
   private final BirdSoundController<T> birdSoundController;
   private final BirdAnimationController<T> birdAnimationController;
   private final BirdSkinController<T> birdSkinController;
   private final BirdBehaviorStateController<T> birdBehaviorStateController;
   private final BirdFeatherController<T> birdFeatherController;
   private final BirdModelController<T> birdModelController;
   private final BirdBreedController<T> birdBreedController;
   private final BirdFoodBagController<T> birdFoodBagController;
   private final List<AbstractBirdController<T>> controllers;

   private BirdControllers(BirdControllers.Builder<T> builder) {
      this.birdTickController = builder.birdTickController;
      this.birdFlyingController = builder.birdFlyingController;
      this.birdRoutineController = builder.birdRoutineController;
      this.birdEatingController = builder.birdEatingController;
      this.birdTameController = builder.birdTameController;
      this.birdGoalController = builder.birdGoalController;
      this.birdFrightController = builder.birdFrightController;
      this.birdSoundController = builder.birdSoundController;
      this.birdAnimationController = builder.birdAnimationController;
      this.birdSkinController = builder.birdSkinController;
      this.birdBehaviorStateController = builder.birdBehaviorStateController;
      this.birdFeatherController = builder.birdFeatherController;
      this.birdModelController = builder.birdModelController;
      this.birdBreedController = builder.birdBreedController;
      this.birdFoodBagController = builder.birdFoodBagController;
      this.controllers = List.of(
         this.birdTickController,
         this.birdFlyingController,
         this.birdRoutineController,
         this.birdEatingController,
         this.birdTameController,
         this.birdGoalController,
         this.birdFrightController,
         this.birdSoundController,
         this.birdAnimationController,
         this.birdSkinController,
         this.birdBehaviorStateController,
         this.birdFeatherController,
         this.birdBreedController,
         this.birdModelController,
         this.birdFoodBagController
      );
   }

   public void attach(T bird) {
      this.controllers.forEach(controller -> controller.attach(bird));
   }

   public static <T extends AbstractBirdEntity<T>> BirdControllers<T> withBird(T bird) {
      BirdControllers<T> controllers = BirdControllers.<T>builder().build();
      controllers.attach(bird);
      return controllers;
   }

   public static <T extends AbstractBirdEntity<T>> BirdControllers.Builder<T> builder() {
      return new BirdControllers.Builder<>();
   }

   public BirdTickController<T> getBirdTickController() {
      return this.birdTickController;
   }

   public BirdFlyingController<T> getBirdFlyingController() {
      return this.birdFlyingController;
   }

   public BirdRoutineController<T> getBirdRoutineController() {
      return this.birdRoutineController;
   }

   public BirdEatingController<T> getBirdEatingController() {
      return this.birdEatingController;
   }

   public BirdTameController<T> getBirdTameController() {
      return this.birdTameController;
   }

   public BirdGoalController<T> getBirdGoalController() {
      return this.birdGoalController;
   }

   public BirdFrightController<T> getBirdFrightController() {
      return this.birdFrightController;
   }

   public BirdSoundController<T> getBirdSoundController() {
      return this.birdSoundController;
   }

   public BirdAnimationController<T> getBirdAnimationController() {
      return this.birdAnimationController;
   }

   public BirdSkinController<T> getBirdSkinController() {
      return this.birdSkinController;
   }

   public BirdBehaviorStateController<T> getBirdBehaviorStateController() {
      return this.birdBehaviorStateController;
   }

   public BirdFeatherController<T> getBirdFeatherController() {
      return this.birdFeatherController;
   }

   public BirdBreedController<T> getBirdBreedController() {
      return this.birdBreedController;
   }

   public BirdModelController<T> getBirdModelController() {
      return this.birdModelController;
   }

   public BirdFoodBagController<T> getBirdFoodBagController() {
      return this.birdFoodBagController;
   }

   public List<AbstractBirdController<T>> getControllers() {
      return this.controllers;
   }

   public BirdTickController<T> birdTickController() {
      return this.birdTickController;
   }

   public BirdFlyingController<T> birdFlyingController() {
      return this.birdFlyingController;
   }

   public BirdRoutineController<T> birdRoutineController() {
      return this.birdRoutineController;
   }

   public BirdEatingController<T> birdEatingController() {
      return this.birdEatingController;
   }

   public BirdTameController<T> birdTameController() {
      return this.birdTameController;
   }

   public BirdGoalController<T> birdGoalController() {
      return this.birdGoalController;
   }

   public BirdFrightController<T> birdFrightController() {
      return this.birdFrightController;
   }

   public BirdSoundController<T> birdSoundController() {
      return this.birdSoundController;
   }

   public BirdAnimationController<T> birdAnimationController() {
      return this.birdAnimationController;
   }

   public BirdSkinController<T> birdSkinController() {
      return this.birdSkinController;
   }

   public BirdBehaviorStateController<T> birdBehaviorStateController() {
      return this.birdBehaviorStateController;
   }

   public BirdFeatherController<T> birdFeatherController() {
      return this.birdFeatherController;
   }

   public BirdBreedController<T> birdBreedController() {
      return this.birdBreedController;
   }

   public BirdModelController<T> birdModelController() {
      return this.birdModelController;
   }

   public BirdFoodBagController<T> birdFoodBagController() {
      return this.birdFoodBagController;
   }

   public static final class Builder<T extends AbstractBirdEntity<T>> {
      private BirdTickController<T> birdTickController = new BirdTickController<>();
      private BirdFlyingController<T> birdFlyingController = new BirdFlyingController<>();
      private BirdRoutineController<T> birdRoutineController = new BirdRoutineController<>();
      private BirdEatingController<T> birdEatingController = new BirdEatingController<>();
      private BirdTameController<T> birdTameController = new BirdTameController<>();
      private BirdGoalController<T> birdGoalController = BirdGoalController.<T>builder().build();
      private BirdFrightController<T> birdFrightController = new BirdFrightController<>();
      private BirdSoundController<T> birdSoundController = new BirdSoundController<>();
      private BirdAnimationController<T> birdAnimationController = new BirdAnimationController<>();
      private BirdSkinController<T> birdSkinController = new BirdSkinController<>();
      private BirdBehaviorStateController<T> birdBehaviorStateController = new BirdBehaviorStateController<>();
      private BirdFeatherController<T> birdFeatherController = new BirdFeatherController<>();
      private BirdBreedController<T> birdBreedController = new BirdBreedController<>();
      private BirdModelController<T> birdModelController = new BirdModelController<>();
      private BirdFoodBagController<T> birdFoodBagController = new BirdFoodBagController<>();

      public BirdControllers.Builder<T> birdFlyingController(BirdFlyingController<T> controller) {
         this.birdFlyingController = controller;
         return this;
      }

      public BirdControllers.Builder<T> birdTickController(BirdTickController<T> controller) {
         this.birdTickController = controller;
         return this;
      }

      public BirdControllers.Builder<T> birdRoutineController(BirdRoutineController<T> controller) {
         this.birdRoutineController = controller;
         return this;
      }

      public BirdControllers.Builder<T> birdEatingController(BirdEatingController<T> controller) {
         this.birdEatingController = controller;
         return this;
      }

      public BirdControllers.Builder<T> birdTameController(BirdTameController<T> controller) {
         this.birdTameController = controller;
         return this;
      }

      public BirdControllers.Builder<T> birdGoalController(BirdGoalController<T> controller) {
         this.birdGoalController = controller;
         return this;
      }

      public BirdControllers.Builder<T> birdFrightController(BirdFrightController<T> controller) {
         this.birdFrightController = controller;
         return this;
      }

      public BirdControllers.Builder<T> birdSoundController(BirdSoundController<T> controller) {
         this.birdSoundController = controller;
         return this;
      }

      public BirdControllers.Builder<T> birdAnimationController(BirdAnimationController<T> controller) {
         this.birdAnimationController = controller;
         return this;
      }

      public BirdControllers.Builder<T> birdModelController(BirdSkinController<T> controller) {
         this.birdSkinController = controller;
         return this;
      }

      public BirdControllers.Builder<T> birdBehaviorStateController(BirdBehaviorStateController<T> controller) {
         this.birdBehaviorStateController = controller;
         return this;
      }

      public BirdControllers.Builder<T> birdFeatherController(BirdFeatherController<T> controller) {
         this.birdFeatherController = controller;
         return this;
      }

      public BirdControllers.Builder<T> birdBreedController(BirdBreedController<T> controller) {
         this.birdBreedController = controller;
         return this;
      }

      public BirdControllers.Builder<T> birdModelController(BirdModelController<T> controller) {
         this.birdModelController = controller;
         return this;
      }

      public BirdControllers.Builder<T> birdFoodBagController(BirdFoodBagController<T> controller) {
         this.birdFoodBagController = controller;
         return this;
      }

      public BirdControllers<T> build() {
         return new BirdControllers<>(this);
      }
   }
}
