package net.fodoth.skina.neoguanniao.content.bird.core.controller.tick.ticker;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.BirdBehaviorState;
import net.fodoth.skina.neoguanniao.content.bird.core.data.BirdData;
import net.fodoth.skina.neoguanniao.content.bird.core.data.datum.BirdFlyingDatum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

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
     *
     * @see AbstractBirdTicker#setTicks(int)
     * @see AbstractBirdEntity#getFlyingController()
     * @see AbstractBirdEntity#getBehaviorStateController()
     */
    @Override
    protected void reset() {
        super.reset();
        // 设置检查间隔为 20 个游戏刻（约 1 秒），控制检查频率避免过度消耗性能
        setTicks(20);
        processUnsafeFlying();

    }

    private void processUnsafeFlying() {
        T bird = bird();
        // ---- 不安全飞行修正逻辑 ----
        // 检查鸟儿是否处于需要修正的异常悬空状态
        boolean flag1 = !bird.onGround();
        boolean flag2 = !bird.getFlyingController().isBirdFlightActive();
        boolean flag3 = !bird.isPassenger();
        boolean flag4 = bird.getTickController().getTickTimer().getBirdLandingTicker().getTicks() == 0;
        boolean flag5 = bird.getBehaviorStateController().getBehaviorState().isUnsafeFlyTickerEnabled();
        boolean flag6 = bird.getGoalController().getBirdBathUseGoalController().isRunning();
        boolean flag7 = bird.isBaby();
        boolean isUnsafeFlying = flag1 && flag2 && flag3 && flag4 && flag5 && flag6 && flag7;
        if (!isUnsafeFlying) {
            if (enableLifecycleLog() && bird().getRandom().nextFloat() <= 0.1) {
                NeoGuanNiao.LOGGER.info("[Ticker] UnsafeFly: Early Return with flags: {} {} {} {} {} {} {}", flag1, flag2, flag3, flag4, flag5, flag6, flag7);
            }
            return;
        }

        boolean roosting = bird().getBehaviorStateController().getBehaviorState() == BirdBehaviorState.ROOSTING;
        boolean stuckInAir = bird().getDeltaMovement().length() < 0.2;
        if (roosting) {
            if (!stuckInAir) {
                if (enableLifecycleLog()) {
                    NeoGuanNiao.LOGGER.info("[Ticker] UnsafeFly: Bird roosting properly with movement length: {}, skip check", bird().getDeltaMovement().length());
                }
            }
            return;
        }
        boolean sleeping = bird().getBehaviorStateController().getBehaviorState() == BirdBehaviorState.SLEEPING;
        boolean isSleepingPlaceStillValid = positionStillValid();
        if (sleeping && isSleepingPlaceStillValid) {
            if (enableLifecycleLog() && bird().getTickController().getTickTimer().getDebugLoopTicker().getTicks() < 5) {
                NeoGuanNiao.LOGGER.info("[Ticker] UnsafeFly: Bird sleeping in valid place, raise alertness and skip check");
            }
            setTicks(Math.min(getTicks(), 5));
            return;
        }

        BirdData birdData = bird.getBirdData();
        BirdFlyingDatum flyingDatum = birdData.flying();

        var dataTicks = flyingDatum.ambientAirCruiseMinTicks() + bird.getRandom().nextInt(flyingDatum.ambientAirCruiseRandomTicks());
        var landingTicks = getTicks() + dataTicks;
        // 触发短程飞行，不指定目标位置（自动调整），且不强制持续飞行

        bird.getFlyingController().startShortFlight(null, false);

        NeoGuanNiao.LOGGER.info("[Ticker] UnsafeFly: Start with Data Ticks: {}, LandingTicks: {}", dataTicks, landingTicks);
        setTicks(Math.min((int)(landingTicks * 2.5), dataTicks * 10));
    }

    @Override
    protected void onReset() {
    }

    /**
     * 检查鸟的当前位置是否有效
     * 规则：
     * 1. 如果鸟躲在树叶里面，下方必须为空
     * 2. 如果鸟在空气中，下方必须为实体方块（不能在空中悬浮）
     * 3. 其他情况视为无效
     *
     * @return true 表示位置有效，false 表示位置无效
     */

    //TODO 兼容鸟巢，鸟浴盆等可以休息的方块
    public boolean positionStillValid() {
        BlockPos pos = bird().blockPosition();
        BlockPos belowPos = pos.below();

        BlockState currentState = bird().level().getBlockState(pos);
        BlockState belowState = bird().level().getBlockState(belowPos);

        // 判断是否为空气
        boolean currentIsAir = currentState.isAir() || currentState.is(Blocks.AIR);
        boolean belowIsAir = belowState.isAir() || belowState.is(Blocks.AIR);
        boolean belowIsLeaves = belowState.getBlock() instanceof LeavesBlock;

        // 情况1：站在树叶上 -> 下方必须为空气
        if (currentState.getBlock() instanceof LeavesBlock) {
            return belowIsAir || belowIsLeaves;
        }

        // 情况2：站在空气中 -> 下方必须为实体方块（非空气）
        if (currentIsAir) {
            return !belowIsAir;
        }

        // 其他情况：站在非树叶的实体方块上 -> 无效
        return false;
    }

}