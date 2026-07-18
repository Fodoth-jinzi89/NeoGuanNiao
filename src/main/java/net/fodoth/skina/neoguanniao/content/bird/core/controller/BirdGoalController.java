package net.fodoth.skina.neoguanniao.content.bird.core.controller;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.controller.goal.*;

import java.util.ArrayList;
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

    private final List<AbstractGoalController<T>> controllers;


    public BirdGoalController() {
        super();
        this.birdCuriousFollowGoalController = new BirdCuriousFollowGoalController<>();
        this.birdEatFoodGoalController = new BirdEatFoodGoalController<>();
        this.birdFlockGoalController = new BirdFlockGoalController<>();
        this.birdFollowOwnerGoalController = new BirdFollowOwnerGoalController<>();
        this.birdIdleGoalController = new BirdIdleGoalController<>();
        this.birdMusicDanceGoalController = new BirdMusicDanceGoalController<>();
        this.birdRandomLookAroundGoalController = new BirdRandomLookAroundGoalController<>();
        this.birdRoostGoalController = new BirdRoostGoalController<>();
        this.birdSentinelGoalController = new BirdSentinelGoalController<>();
        this.birdWakeUpGoalController = new BirdWakeUpGoalController<>();
        this.birdBathUseGoalController = new BirdBathUseGoalController<>();

        List<AbstractGoalController<T>> controllers = new ArrayList<>(List.of(
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
                birdBathUseGoalController
        ));

        this.controllers = List.copyOf(controllers);
    }

    @Override
    protected void onAttach() {
        for (AbstractGoalController<T> controller : controllers) {
            controller.attach(bird());
        }
    }


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

}