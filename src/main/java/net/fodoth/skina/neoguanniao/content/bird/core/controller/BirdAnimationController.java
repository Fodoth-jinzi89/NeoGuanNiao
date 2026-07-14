package net.fodoth.skina.neoguanniao.content.bird.core.controller;

import net.fodoth.skina.neoguanniao.content.bird.core.BirdGuidePreviewAnimation;
import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.feature.flight.BirdFlightAware;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * 鸟类动画控制器
 * <p>
 * 负责管理 GeckoLib 动画实例、注册动画控制器，并根据鸟类当前状态
 * 判断应播放的动画类型。
 * </p>
 *
 */
public class BirdAnimationController {

    /**
     * 当前待播放的待机动画
     */
    public RawAnimation currentIdleAnimation;

    /**
     * GeckoLib 动画实例缓存
     */
    public final AnimatableInstanceCache cache;

    /**
     * 关联的鸟类实体
     */
    private final AbstractBirdEntity<?> bird;

    /**
     * 构造鸟类动画控制器
     *
     * @param entity 鸟类实体，不能为 null
     */
    public BirdAnimationController(AbstractBirdEntity<?> entity) {
        this.bird = entity;
        this.cache = GeckoLibUtil.createInstanceCache(bird);
        this.currentIdleAnimation = pickIdleAnimation();
    }

    /**
     * 获取当前待播放的待机动画
     * <p>
     * 如果当前未指定待机动画，则返回默认空动画。
     * </p>
     *
     * @return 当前待机动画，不为 null
     */
    public RawAnimation pickIdleAnimation() {
        return currentIdleAnimation == null
                ? BirdGuidePreviewAnimation.NONE.animation()
                : currentIdleAnimation;
    }

    /**
     * 注册 GeckoLib 动画控制器
     * <p>
     * 为鸟类实体注册名为 "movement" 的动画控制器，
     * 过渡时间为 4 帧。
     * </p>
     *
     * @param controllers 动画控制器注册器，用于接收注册的控制器
     */
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(bird, "movement", 4, bird::movementController));
    }

    /**
     * 判断当前是否应该播放飞行动画
     * <p>
     * 综合考虑鸟类的空中状态、地面状态、移动速度等因素，
     * 决定是否应该播放飞行动画。
     * </p>
     *
     * @return 如果应该播放飞行动画返回 true，否则返回 false
     */
    public boolean shouldPlayFlyAnimation() {
        return shouldPlayFlyAnimation(bird,
                bird.getBehaviorStateController().getBehaviorState().isAirborne(),
                bird.onGround(),
                bird.isInWater(),
                bird.getDeltaMovement(),
                bird.getBirdData().flying().airborneGraceTicks());
    }

    /**
     * 判断是否应该播放飞行动画（静态方法）
     * <p>
     * 核心逻辑：
     * <ul>
     *   <li>如果飞行功能未激活且未处于空中状态：</li>
     *   <ul>
     *     <li>在地面时不播放飞行</li>
     *     <li>处于空中宽限期则播放</li>
     *     <li>非无重力且未着陆/未逃离时，根据下落速度和水平移动判断</li>
     *   </ul>
     *   <li>其他情况默认播放飞行动画</li>
     * </ul>
     * </p>
     *
     * @param bird 鸟类实体，必须实现 {@link BirdFlightAware} 接口
     * @param airborneState 是否处于空中状态
     * @param onGround 是否在地面上
     * @param noGravity 是否处于无重力状态（如水中）
     * @param movement 当前移动向量
     * @param airborneGraceTicks 空中宽限期（刻数），用于平滑过渡
     * @return 如果应该播放飞行动画返回 true，否则返回 false
     */
    private static boolean shouldPlayFlyAnimation(
            BirdFlightAware bird,
            boolean airborneState,
            boolean onGround,
            boolean noGravity,
            Vec3 movement,
            int airborneGraceTicks
    ) {
        // 飞行功能未激活且未处于空中状态
        if (!bird.isBirdFlightActive() && !airborneState) {
            // 在地面上不播放飞行动画
            if (onGround) {
                return false;
            }

            // 空中宽限期允许继续播放飞行动画
            if (airborneGraceTicks > 0) {
                return true;
            }

            // 非无重力状态且未着陆未逃离时，根据移动判断
            if (!noGravity && !bird.isBirdLanding() && !bird.isBirdEscaping()) {
                // 下落速度不是特别快时播放飞行
                if (movement.y > -0.85) {
                    return true;
                }
                // 有水平移动时播放飞行
                return movement.lengthSqr() > 0.001;
            }

            // 其他情况默认播放飞行
            return true;
        }

        // 飞行功能激活或处于空中状态，默认播放飞行
        return true;
    }
}