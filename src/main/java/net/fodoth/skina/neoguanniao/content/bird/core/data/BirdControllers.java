package net.fodoth.skina.neoguanniao.content.bird.core.data;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.controller.*;

import java.util.List;

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


    private final List<AbstractBirdController<T>> controllers;


    private BirdControllers(Builder<T> builder) {

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


        this.controllers = List.of(
                birdTickController,
                birdFlyingController,
                birdRoutineController,
                birdEatingController,
                birdTameController,
                birdGoalController,
                birdFrightController,
                birdSoundController,
                birdAnimationController,
                birdSkinController,
                birdBehaviorStateController
        );
    }


    public void attach(T bird) {
        controllers.forEach(controller -> controller.attach(bird));
    }

    public static <T extends AbstractBirdEntity<T>> BirdControllers<T> withBird(T bird) {
        BirdControllers<T> controllers = BirdControllers.<T>builder().build();
        controllers.attach(bird);
        return controllers;
    }


    public static <T extends AbstractBirdEntity<T>> Builder<T> builder() {
        return new Builder<>();
    }


    public static final class Builder<T extends AbstractBirdEntity<T>> {

        private BirdTickController<T> birdTickController = new BirdTickController<>();
        private BirdFlyingController<T> birdFlyingController = new BirdFlyingController<>();
        private BirdRoutineController<T> birdRoutineController = new BirdRoutineController<>();
        private BirdEatingController<T> birdEatingController = new BirdEatingController<>();
        private BirdTameController<T> birdTameController = new BirdTameController<>();
        private BirdGoalController<T> birdGoalController = new BirdGoalController<>();
        private BirdFrightController<T> birdFrightController = new BirdFrightController<>();
        private BirdSoundController<T> birdSoundController = new BirdSoundController<>();
        private BirdAnimationController<T> birdAnimationController = new BirdAnimationController<>();
        private BirdSkinController<T> birdSkinController = new BirdSkinController<>();
        private BirdBehaviorStateController<T> birdBehaviorStateController = new BirdBehaviorStateController<>();


        public Builder<T> birdFlyingController(BirdFlyingController<T> controller) {
            this.birdFlyingController = controller;
            return this;
        }

        public Builder<T> birdTickController(BirdTickController<T> controller) {
            this.birdTickController = controller;
            return this;
        }

        public Builder<T> birdRoutineController(BirdRoutineController<T> controller) {
            this.birdRoutineController = controller;
            return this;
        }

        public Builder<T> birdEatingController(BirdEatingController<T> controller) {
            this.birdEatingController = controller;
            return this;
        }

        public Builder<T> birdTameController(BirdTameController<T> controller) {
            this.birdTameController = controller;
            return this;
        }

        public Builder<T> birdGoalController(BirdGoalController<T> controller) {
            this.birdGoalController = controller;
            return this;
        }

        public Builder<T> birdFrightController(BirdFrightController<T> controller) {
            this.birdFrightController = controller;
            return this;
        }

        public Builder<T> birdSoundController(BirdSoundController<T> controller) {
            this.birdSoundController = controller;
            return this;
        }

        public Builder<T> birdAnimationController(BirdAnimationController<T> controller) {
            this.birdAnimationController = controller;
            return this;
        }

        public Builder<T> birdModelController(BirdSkinController<T> controller) {
            this.birdSkinController = controller;
            return this;
        }

        public Builder<T> birdBehaviorStateController(BirdBehaviorStateController<T> controller) {
            this.birdBehaviorStateController = controller;
            return this;
        }

        public BirdControllers<T> build() {
            return new BirdControllers<>(this);
        }
    }


    // Getter 方法
    public BirdTickController<T> getBirdTickController() {
        return birdTickController;
    }

    public BirdFlyingController<T> getBirdFlyingController() {
        return birdFlyingController;
    }

    public BirdRoutineController<T> getBirdRoutineController() {
        return birdRoutineController;
    }

    public BirdEatingController<T> getBirdEatingController() {
        return birdEatingController;
    }

    public BirdTameController<T> getBirdTameController() {
        return birdTameController;
    }

    public BirdGoalController<T> getBirdGoalController() {
        return birdGoalController;
    }

    public BirdFrightController<T> getBirdFrightController() {
        return birdFrightController;
    }

    public BirdSoundController<T> getBirdSoundController() {
        return birdSoundController;
    }

    public BirdAnimationController<T> getBirdAnimationController() {
        return birdAnimationController;
    }

    public BirdSkinController<T> getBirdModelController() {
        return birdSkinController;
    }

    public BirdBehaviorStateController<T> getBirdBehaviorStateController() {
        return birdBehaviorStateController;
    }

    public List<AbstractBirdController<T>> getControllers() {
        return controllers;
    }


    // 便捷方法（与 getter 相同）
    public BirdTickController<T> birdTickController() {
        return birdTickController;
    }

    public BirdFlyingController<T> birdFlyingController() {
        return birdFlyingController;
    }

    public BirdRoutineController<T> birdRoutineController() {
        return birdRoutineController;
    }

    public BirdEatingController<T> birdEatingController() {
        return birdEatingController;
    }

    public BirdTameController<T> birdTameController() {
        return birdTameController;
    }

    public BirdGoalController<T> birdGoalController() {
        return birdGoalController;
    }

    public BirdFrightController<T> birdFrightController() {
        return birdFrightController;
    }

    public BirdSoundController<T> birdSoundController() {
        return birdSoundController;
    }

    public BirdAnimationController<T> birdAnimationController() {
        return birdAnimationController;
    }

    public BirdSkinController<T> birdModelController() {
        return birdSkinController;
    }

    public BirdBehaviorStateController<T> birdBehaviorStateController() {
        return birdBehaviorStateController;
    }
}