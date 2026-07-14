package net.fodoth.skina.neoguanniao.content.bird.core.controller;

import net.fodoth.skina.neoguanniao.content.bird.core.BirdGuidePreviewAnimation;
import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.feature.flight.BirdFlightAware;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * 鸟类动画控制器。
 * <p>
 * 负责管理 GeckoLib 动画实例、注册动画控制器，并根据鸟类当前状态
 * 判断应播放的动画类型。
 */
public class BirdAnimationController {
    public RawAnimation currentIdleAnimation;
    public final AnimatableInstanceCache cache;
    private final AbstractBirdEntity<?> bird;

    /**
     * 创建鸟类动画控制器。
     *
     * @param entity 鸟类实体
     */
    public BirdAnimationController(AbstractBirdEntity<?> entity){
        this.bird = entity;
        this.cache = GeckoLibUtil.createInstanceCache(bird);
        this.currentIdleAnimation = pickIdleAnimation();
    }


    /**
     * 获取当前待播放的待机动画。
     * <p>
     * 如果当前未指定待机动画，则返回默认空动画。
     *
     * @return 当前待机动画
     */
    public RawAnimation pickIdleAnimation() {
        return currentIdleAnimation == null ? BirdGuidePreviewAnimation.NONE.animation() : currentIdleAnimation;
    }

    /**
     * 注册 GeckoLib 动画控制器。
     *
     * @param controllers 动画控制器注册器
     */
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(bird, "movement", 4, bird::movementController));
    }

    /**
     * 判断当前是否应该播放飞行动画。
     *
     * @return 如果应该播放飞行动画返回 true
     */
    public boolean shouldPlayFlyAnimation() {
        return shouldPlayFlyAnimation(bird,
                bird.getBehaviorStateController().getBehaviorState().isAirborne(), bird.onGround(), bird.isInWater(),
                bird.getDeltaMovement(), bird.getBirdData().airborneGraceTicks());
    }

    /**
     * 判断是否应该播放飞行动画
     *
     * @param bird 鸟类实体（实现了 BirdFlightAware 接口）
     * @param airborneState 空中状态
     * @param onGround 是否在地面
     * @param noGravity 是否无重力
     * @param movement 移动向量
     * @param airborneGraceTicks 空中宽限期
     * @return 如果应该播放飞行动画返回 true
     */
    private static boolean shouldPlayFlyAnimation(
            BirdFlightAware bird,
            boolean airborneState,
            boolean onGround,
            boolean noGravity,
            Vec3 movement,
            int airborneGraceTicks
    ) {
        if (!bird.isBirdFlightActive() && !airborneState) {
            if (onGround) {
                return false;
            }
            if (airborneGraceTicks > 0) {
                return true;
            }
            if (!noGravity && !bird.isBirdLanding() && !bird.isBirdEscaping()) {
                if (movement.y > -0.85) {
                    return true;
                }
                return movement.lengthSqr() > 0.001;
            }
            return true;
        }
        return true;
    }


}
