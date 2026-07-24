package net.fodoth.skina.neoguanniao.content.bird.core.controller;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.controller.goal.*;

import java.util.List;

/**
 * 鸟类目标控制器
 * <p>
 * 提供各类 AI Goal 的启动条件判断。
 * </p>
 */
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

    private BirdGoalController(Builder<T> builder) {
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
                birdCuriousFollowGoalController,
                birdEatFoodGoalController,
                birdFlockGoalController,
                birdFollowOwnerGoalController,
                birdIdleGoalController,
                birdMusicDanceGoalController,
                birdRandomLookAroundGoalController,
                birdRoostGoalController,
                birdSentinelGoalController,
                birdWakeUpGoalController,
                birdBathUseGoalController,
                birdBreedGoalController,
                birdSkinValidateGoalController,
                birdModelValidateGoalController,
                birdRandomWalkAroundGoalController
        );
    }

    @Override
    protected void onAttach() {
        controllers.forEach(controller -> controller.attach(bird()));
    }

    public static <T extends AbstractBirdEntity<T>> Builder<T> builder() {
        return new Builder<>();
    }

    public static <T extends AbstractBirdEntity<T>> BirdGoalController<T> withBird(T bird) {
        BirdGoalController<T> controller = BirdGoalController.<T>builder().build();
        controller.attach(bird);
        return controller;
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

        public Builder<T> birdCuriousFollowGoalController(BirdCuriousFollowGoalController<T> controller) {
            this.birdCuriousFollowGoalController = controller;
            return this;
        }

        public Builder<T> birdEatFoodGoalController(BirdEatFoodGoalController<T> controller) {
            this.birdEatFoodGoalController = controller;
            return this;
        }

        public Builder<T> birdFlockGoalController(BirdFlockGoalController<T> controller) {
            this.birdFlockGoalController = controller;
            return this;
        }

        public Builder<T> birdFollowOwnerGoalController(BirdFollowOwnerGoalController<T> controller) {
            this.birdFollowOwnerGoalController = controller;
            return this;
        }

        public Builder<T> birdIdleGoalController(BirdIdleGoalController<T> controller) {
            this.birdIdleGoalController = controller;
            return this;
        }

        public Builder<T> birdMusicDanceGoalController(BirdMusicDanceGoalController<T> controller) {
            this.birdMusicDanceGoalController = controller;
            return this;
        }

        public Builder<T> birdRandomLookAroundGoalController(BirdRandomLookAroundGoalController<T> controller) {
            this.birdRandomLookAroundGoalController = controller;
            return this;
        }

        public Builder<T> birdRoostGoalController(BirdRoostGoalController<T> controller) {
            this.birdRoostGoalController = controller;
            return this;
        }

        public Builder<T> birdSentinelGoalController(BirdSentinelGoalController<T> controller) {
            this.birdSentinelGoalController = controller;
            return this;
        }

        public Builder<T> birdWakeUpGoalController(BirdWakeUpGoalController<T> controller) {
            this.birdWakeUpGoalController = controller;
            return this;
        }

        public Builder<T> birdBathUseGoalController(BirdBathUseGoalController<T> controller) {
            this.birdBathUseGoalController = controller;
            return this;
        }

        public Builder<T> birdBreedGoalController(BirdBreedGoalController<T> controller) {
            this.birdBreedGoalController = controller;
            return this;
        }

        public Builder<T> birdSkinValidateGoalController(BirdSkinValidateGoalController<T> controller) {
            this.birdSkinValidateGoalController = controller;
            return this;
        }

        public Builder<T> birdModelValidateGoalController(BirdModelValidateGoalController<T> controller) {
            this.birdModelValidateGoalController = controller;
            return this;
        }

        public Builder<T> birdRandomWalkAroundGoalController(BirdRandomWalkAroundGoalController<T> controller) {
            this.birdRandomWalkAroundGoalController = controller;
            return this;
        }

        public BirdGoalController<T> build() {
            return new BirdGoalController<>(this);
        }
    }

    // Getter 方法
    public BirdCuriousFollowGoalController<T> getBirdCuriousFollowGoalController() {
        return birdCuriousFollowGoalController;
    }

    public BirdEatFoodGoalController<T> getBirdEatFoodGoalController() {
        return birdEatFoodGoalController;
    }

    public BirdFlockGoalController<T> getBirdFlockGoalController() {
        return birdFlockGoalController;
    }

    public BirdFollowOwnerGoalController<T> getBirdFollowOwnerGoalController() {
        return birdFollowOwnerGoalController;
    }

    public BirdIdleGoalController<T> getBirdIdleGoalController() {
        return birdIdleGoalController;
    }

    public BirdMusicDanceGoalController<T> getBirdMusicDanceGoalController() {
        return birdMusicDanceGoalController;
    }

    public BirdRandomLookAroundGoalController<T> getBirdRandomLookAroundGoalController() {
        return birdRandomLookAroundGoalController;
    }

    public BirdRoostGoalController<T> getBirdRoostGoalController() {
        return birdRoostGoalController;
    }

    public BirdSentinelGoalController<T> getBirdSentinelGoalController() {
        return birdSentinelGoalController;
    }

    public BirdWakeUpGoalController<T> getBirdWakeUpGoalController() {
        return birdWakeUpGoalController;
    }

    public BirdBathUseGoalController<T> getBirdBathUseGoalController() {
        return birdBathUseGoalController;
    }

    public BirdBreedGoalController<T> getBirdBreedGoalController() {
        return birdBreedGoalController;
    }

    public BirdSkinValidateGoalController<T> getBirdSkinValidateGoalController() {
        return birdSkinValidateGoalController;
    }

    public BirdModelValidateGoalController<T> getBirdModelValidateGoalController() {
        return birdModelValidateGoalController;
    }

    public BirdRandomWalkAroundGoalController<T> getBirdRandomWalkAroundGoalController() {
        return birdRandomWalkAroundGoalController;
    }

    public List<AbstractGoalController<T>> getControllers() {
        return controllers;
    }

    // 便捷方法（与 getter 相同）
    public BirdCuriousFollowGoalController<T> birdCuriousFollowGoalController() {
        return birdCuriousFollowGoalController;
    }

    public BirdEatFoodGoalController<T> birdEatFoodGoalController() {
        return birdEatFoodGoalController;
    }

    public BirdFlockGoalController<T> birdFlockGoalController() {
        return birdFlockGoalController;
    }

    public BirdFollowOwnerGoalController<T> birdFollowOwnerGoalController() {
        return birdFollowOwnerGoalController;
    }

    public BirdIdleGoalController<T> birdIdleGoalController() {
        return birdIdleGoalController;
    }

    public BirdMusicDanceGoalController<T> birdMusicDanceGoalController() {
        return birdMusicDanceGoalController;
    }

    public BirdRandomLookAroundGoalController<T> birdRandomLookAroundGoalController() {
        return birdRandomLookAroundGoalController;
    }

    public BirdRoostGoalController<T> birdRoostGoalController() {
        return birdRoostGoalController;
    }

    public BirdSentinelGoalController<T> birdSentinelGoalController() {
        return birdSentinelGoalController;
    }

    public BirdWakeUpGoalController<T> birdWakeUpGoalController() {
        return birdWakeUpGoalController;
    }

    public BirdBathUseGoalController<T> birdBathUseGoalController() {
        return birdBathUseGoalController;
    }

    public BirdBreedGoalController<T> birdBreedGoalController() {
        return birdBreedGoalController;
    }

    public BirdSkinValidateGoalController<T> birdSkinValidateGoalController() {
        return birdSkinValidateGoalController;
    }

    public BirdModelValidateGoalController<T> birdModelValidateGoalController() {
        return birdModelValidateGoalController;
    }

    public BirdRandomWalkAroundGoalController<T> birdRandomWalkAroundGoalController() {
        return birdRandomWalkAroundGoalController;
    }
}