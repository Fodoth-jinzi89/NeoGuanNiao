package net.fodoth.skina.neoguanniao.content.bird.core.controller.tick.ticker;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;

/**
 * 鸟类不安全飞行循环处理器
 *
 * <p>该处理器负责检测并修正鸟类实体在非标准移动状态下（如被传送、骑乘、碰撞异常等）
 * 导致的悬空或卡顿情况。当检测到实体处于异常悬空状态时，通过触发短程飞行来使实体
 * 恢复到合理的运动状态，避免实体卡在半空中或出现不符合物理规则的行为。</p>
 *
 * <p>该处理器在服务端以固定频率（每20游戏刻）执行检查，仅在鸟类不在地面且未处于
 * 正常飞行状态时才会触发修正机制。</p>
 *
 * @param <T> 鸟类实体类型，必须继承自 {@link AbstractBirdEntity}
 * @author [您的名字或团队名称]
 * @version 1.0
 * @see AbstractBirdTicker
 * @see AbstractBirdEntity
 */
public class BirdUnsafeFlyLoopTicker<T extends AbstractBirdEntity<T>> extends AbstractBirdTicker<T> {

    /**
     * 构造方法
     *
     * <p>初始化鸟类不安全飞行循环处理器。该处理器仅在服务端执行，
     * 并且作为循环触发器定期检查鸟类实体的飞行状态。</p>
     *
     * <p>参数说明：
     * <ul>
     *   <li>serverSide = true：仅在服务端执行，确保客户端不进行重复检查</li>
     *   <li>clientSide = false：不在客户端执行，避免双端逻辑冲突</li>
     *   <li>loop = true：作为循环触发器，定期执行检查逻辑</li>
     * </ul>
     * </p>
     */
    public BirdUnsafeFlyLoopTicker() {
        super(true, false, true);
    }

    /**
     * 重置并执行安全飞行检查
     *
     * <p>该方法会在每 20 个游戏刻（约 1 秒）被调用一次，用于检测鸟儿是否处于
     * 异常悬空状态。如果是，则强制启动一次短程飞行以修正位置。</p>
     *
     * <p><strong>触发条件（需同时满足）：</strong></p>
     * <ol>
     *   <li>鸟儿不在地面上（{@code onGround == false}）</li>
     *   <li>飞行控制器未处于激活状态（避免干扰正常飞行）</li>
     *   <li>鸟儿不是乘客（未被骑乘）</li>
     *   <li>移动速度平方大于 1.0E-4（即正在移动）</li>
     *   <li>着陆计时器当前 tick 为 0（即不处于待着陆状态）</li>
     *   <li>行为状态允许执行不安全飞行修正（{@code isUnsafeFlyEnabled()} 返回 true）</li>
     * </ol>
     *
     * <p><strong>额外处理：</strong></p>
     * <p>如果鸟儿不在地面上且行为状态允许不安全悬浮修正，则取消无重力状态，
     * 防止鸟儿异常悬浮。</p>
     *
     * @see AbstractBirdTicker#setTicks(int)
     * @see AbstractBirdEntity#getFlyingController()
     * @see AbstractBirdEntity#getBehaviorStateController()
     */
    @Override
    protected void reset() {
        // 设置检查间隔为 20 个游戏刻（约 1 秒），控制检查频率避免过度消耗性能
        setTicks(20);

        // 获取当前鸟类实体实例
        T bird = bird();

        // ---- 不安全飞行修正逻辑 ----
        // 检查鸟儿是否处于需要修正的异常悬空状态
        boolean isUnsafeFlying = !bird.onGround()                                    // 不在地面
                && !bird.getFlyingController().isBirdFlightActive()                  // 飞行未激活
                && !bird.isPassenger()                                              // 不是乘客（未被骑乘）
                && bird.getDeltaMovement().lengthSqr() > 1.0E-4                     // 有移动速度（正在移动）
                && bird.getTickController().getTickTimer().getBirdLandingTicker().getTicks() == 0 // 着陆计时器为0（非着陆延迟状态）
                && bird.getBehaviorStateController().getBehaviorState().isUnsafeFlyEnabled();     // 行为状态允许不安全飞行修正

        // 如果满足所有条件，则启动短程飞行以修正位置
        if (isUnsafeFlying) {
            // 触发短程飞行，不指定目标位置（自动调整），且不强制持续飞行
            bird.getFlyingController().startShortFlight(null, false);
        }

        // ---- 不安全悬浮修正逻辑 ----
        // 检查是否需要处理异常悬浮状态
        boolean isUnsafeFloating = !bird.onGround()
                && bird.getBehaviorStateController().getBehaviorState().isUnsafeFloatEnabled();

        if (isUnsafeFloating) {
            // 取消无重力状态，防止鸟儿异常悬浮在半空中
            bird.setNoGravity(false);
        }
    }
}