package net.fodoth.skina.neoguanniao.content.bird.core.controller.goal;

import net.fodoth.skina.neoguanniao.content.bath.BirdBathAttraction;
import net.fodoth.skina.neoguanniao.content.bath.BirdBathBlockEntity;
import net.fodoth.skina.neoguanniao.content.bath.BirdBathContentType;
import net.fodoth.skina.neoguanniao.content.bath.BirdBathFeedingAnimatable;
import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.BirdBehaviorState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.UUID;

/**
 * 鸟使用浴盆的目标控制器
 * 管理鸟寻找、移动到浴盆顶部、进食动画和跳离的完整流程
 *
 * @param <T> 鸟实体类型
 */
public class BirdBathUseGoalController<T extends AbstractBirdEntity<?>> extends AbstractGoalController<T> {

    // 目标浴盆实体
    private BirdBathBlockEntity targetBath;
    // 是否已消耗食物
    private boolean consumed;
    // 进食动画是否已开始
    private boolean feedingAnimationStarted;
    // 在浴盆顶部停留的刻数
    private int topUseTicks;
    // 跳上浴盆的冷却时间
    private int mountHopCooldown;
    // 总运行刻数
    private int totalTicks;
    // 是否正在离开浴盆
    private boolean leavingBath;
    // 离开冷却时间
    private int leaveCooldown;
    // 浴盆上的站立位置
    private Vec3 bathStandPosition;

    /**
     * 获取该目标被执行的概率
     *
     * @return 概率值
     */
    @Override
    public int chance() {
        return goalDatum().bathUseChance();
    }

    /**
     * 判断是否可以使用该目标
     * 只有当鸟不在使用浴盆且可以进食时才允许使用
     *
     * @return 是否可以使用
     */
    @Override
    public boolean canUse() {
        // 如果已经在使用浴盆则不能再次使用
        if (isUsingBath()) {
            return false;
        }
        // 委托给进食控制器判断是否可进食
        return !bird().isBaby() && bird().getGoalController()
                .getBirdEatFoodGoalController()
                .canUse();
    }

    /**
     * 开始使用目标时的初始化逻辑
     * 查找并尝试占用附近的浴盆
     *
     * @return 是否成功占用浴盆
     */
    @Override
    public boolean onUse() {
        // 在搜索范围内查找可用的浴盆
        Optional<BirdBathBlockEntity> found =
                BirdBathAttraction.findNearbyUsableBath(
                        bird().level(),
                        bird().blockPosition(),
                        goalDatum().bathUseSearchRange(),
                        this::canUseBath
                );

        if (found.isEmpty()) {
            return false;
        }

        BirdBathBlockEntity bath = found.get();

        // 尝试占用浴盆
        if (!BirdBathAttraction.tryClaimUse(
                bath,
                bird(),
                goalDatum().bathUseClaimTicks()
        )) {
            return false;
        }

        // 设置目标浴盆并计算站立位置
        this.targetBath = bath;
        this.bathStandPosition = BirdBathAttraction.edgeStandPosition(
                this.targetBath,
                bird().position());
        return true;
    }

    /**
     * 检查浴盆是否可以被使用
     *
     * @param bath 浴盆实体
     * @return 是否可以使用
     */
    private boolean canUseBath(BirdBathBlockEntity bath) {
        if (bath == null
                || bath.isRemoved()
                || !bird().isAlive()
                || bath.getLevel() != bird().level()) {
            return false;
        }

        UUID uuid = bird().getUUID();
        // 浴盆必须未被占用，或者被当前鸟占用
        return canUseBathPredicates(bath)
                && (!bath.isOccupied() || bath.isOccupiedBy(uuid));
    }

    /**
     * 浴盆的额外使用条件：对小型食籽鸟有吸引力
     *
     * @param bath 浴盆实体
     * @return 是否符合条件
     */
    public boolean canUseBathPredicates(BirdBathBlockEntity bath) {
        return BirdBathAttraction.isAttractiveToSmallSeedBird(bath);
    }

    /**
     * 判断目标是否可以继续执行
     *
     * @return 是否可以继续
     */
    @Override
    public boolean canContinue() {
        // 离开状态允许继续执行
        if (leavingBath) {
            return true;
        }

        // 必须未消耗食物、未超时、浴盆可用且被当前鸟占用或可占用
        return !this.consumed
                && this.totalTicks < goalDatum().bathUseTotalTicks()
                && this.canUseBath(this.targetBath)
                && (this.targetBath.isOccupiedBy(bird().getUUID())
                || BirdBathAttraction.tryClaimUse(
                this.targetBath,
                bird(),
                goalDatum().bathUseClaimTicks()
        ));
    }

    /**
     * 目标开始时的初始化
     */
    @Override
    public void onStart() {
        // 重置所有状态
        this.consumed = false;
        this.topUseTicks = 0;
        this.mountHopCooldown = 0;
        this.totalTicks = 0;
        this.feedingAnimationStarted = false;

        if (bathExists()) {
            // 设置行为状态为沐浴
            bird().getBehaviorStateController()
                    .setBehaviorState(BirdBehaviorState.BATHING);
            // 移动到浴盆顶部
            moveToBathTop();
        }
    }

    /**
     * 检查浴盆是否存在
     *
     * @return 浴盆是否存在
     */
    public boolean bathExists() {
        return this.targetBath != null;
    }

    /**
     * 检查目标是否正在运行
     *
     * @return 是否在运行
     */
    public boolean isRunning() {
        return this.totalTicks > 0;
    }

    /**
     * 每刻更新逻辑
     * 处理移动到浴盆、等待、进食动画和消耗食物
     */
    @Override
    public void onTick() {
        if (this.targetBath == null) {
            return;
        }

        // 处理离开浴盆逻辑
        if (leavingBath) {
            if (leaveCooldown > 0) {
                --leaveCooldown;
                return;
            }
            if (!bird().onGround()) {
                return;
            }
            jumpDownFromBath();
            return;
        }

        ++this.totalTicks;

        if (this.mountHopCooldown > 0) {
            --this.mountHopCooldown;
        }

        // 让鸟看向浴盆使用位置
        Vec3 usePosition = BirdBathAttraction.topUsePosition(this.targetBath);
        bird().getLookControl().setLookAt(
                usePosition.x,
                usePosition.y,
                usePosition.z,
                goalDatum().bathUseLookYaw(),
                goalDatum().bathUseLookPitch()
        );

        // 判断是否已到达浴盆顶部使用位置
        if (this.isAtTopUsePosition()) {
            bird().getNavigation().stop();
            bird().getBehaviorStateController()
                    .setBehaviorState(BirdBehaviorState.USING_BATH);

            ++this.topUseTicks;
            this.startFeedingAnimationIfNeeded();

            // 预热结束后消耗食物
            if (this.topUseTicks >= goalDatum().bathUseConsumeWarmUpTicks()) {
                this.consumeFromBath();
            }
        } else {
            // 未到达位置时重置状态
            this.topUseTicks = 0;
            this.feedingAnimationStarted = false;
            // 定期尝试重新占用浴盆
            if (shouldTick() && this.totalTicks % goalDatum().bathUseTryClaimChance() == 0) {
                BirdBathAttraction.tryClaimUse(
                        this.targetBath,
                        bird(),
                        goalDatum().bathUseClaimTicks()
                );
                onReset();
            }
        }
    }

    /**
     * 判断是否应该执行刻更新
     *
     * @return 是否应该更新
     */
    @Override
    public boolean shouldTick() {
        if (leavingBath) {
            return true;
        }
        // 尝试跳上或安顿到浴盆顶部，或已经在使用浴盆
        return (!this.trySettleOntoTop(this.bathStandPosition)
                && !this.tryHopOntoTop(this.bathStandPosition))
                || isUsingBath();
    }

    /**
     * 重置时的回调
     * 如果导航完成则重新移动到浴盆顶部
     */
    @Override
    public void onReset() {
        if (bird().getNavigation().isDone()) {
            this.moveToBathTop();
        }
    }

    /**
     * 目标停止时的清理逻辑
     * 释放浴盆占用并重置状态
     */
    @Override
    public void onStop() {
        BirdBathBlockEntity bath = this.targetBath;

        if (bath != null) {
            bath.releaseUse(bird().getUUID());

            BirdBehaviorState state = bird().getBehaviorStateController().getBehaviorState();
            if (state == BirdBehaviorState.BATHING || state == BirdBehaviorState.USING_BATH) {
                bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.IDLE);
            }
        }

        // 清空所有状态
        this.targetBath = null;
        this.bathStandPosition = null;
        this.topUseTicks = 0;
        this.mountHopCooldown = 0;
        this.totalTicks = 0;
        this.leavingBath = false;
        this.leaveCooldown = 0;
        this.feedingAnimationStarted = false;
        this.consumed = false;
    }

    /**
     * 移动鸟到浴盆顶部站立位置
     */
    private void moveToBathTop() {
        if (this.targetBath == null) {
            return;
        }

        Vec3 top = BirdBathAttraction.topStandPosition(this.targetBath);
        bird().getNavigation().moveTo(
                top.x,
                top.y,
                top.z,
                goalDatum().bathUseSpeedModifier()
        );
    }

    /**
     * 尝试跳上浴盆顶部
     * 当鸟离浴盆较近时触发跳跃动作
     *
     * @param standPosition 站立位置
     * @return 是否成功触发跳跃
     */
    private boolean tryHopOntoTop(Vec3 standPosition) {
        if (this.targetBath == null || this.mountHopCooldown > 0) {
            return false;
        }

        Vec3 feet = bird().position();
        double horizontalDistanceSqr = feet.subtract(standPosition)
                .multiply(1, 0, 1)
                .lengthSqr();

        double feetOffset = feet.y - this.targetBath.getBlockPos().getY();

        // 检查距离和高度条件
        if (feetOffset >= goalDatum().bathUseTopMinYFeetOffset()
                || horizontalDistanceSqr > goalDatum().bathUseTopMountRangeSqr()) {
            return false;
        }

        // 尝试使用飞行方式跳上浴盆
        if (bird().startBirdBathMountFlight(standPosition)) {
            this.mountHopCooldown = goalDatum().bathUseMountHopCooldown();
            return true;
        }

        // 计算水平方向
        Vec3 horizontal = standPosition.subtract(feet).multiply(1, 0, 1);
        if (horizontal.lengthSqr() <= 1.0E-4) {
            horizontal = Vec3.ZERO;
        } else {
            horizontal = horizontal.normalize().scale(goalDatum().bathUseHopHorizontalScale());
        }

        // 应用跳跃速度
        bird().getNavigation().stop();
        bird().setDeltaMovement(
                horizontal.x,
                goalDatum().bathUseHopVerticalScale(),
                horizontal.z
        );
        bird().fallDistance = 0;

        this.mountHopCooldown = goalDatum().bathUseMountHopCooldown();
        return true;
    }

    /**
     * 尝试直接安顿到浴盆顶部
     * 当鸟已经在浴盆顶部附近时直接设置位置
     *
     * @param standPosition 站立位置
     * @return 是否成功安顿
     */
    private boolean trySettleOntoTop(Vec3 standPosition) {
        if (this.targetBath == null) {
            return false;
        }

        Vec3 feet = bird().position();
        double horizontalDistanceSqr = feet.subtract(standPosition)
                .multiply(1, 0, 1)
                .lengthSqr();

        double feetOffset = feet.y - this.targetBath.getBlockPos().getY();

        // 检查是否在安顿范围内
        if (horizontalDistanceSqr > goalDatum().bathUseTopSettleHorizontalSqr()
                || feetOffset < goalDatum().bathUseTopSettleMinYFeetOffset()
                || feetOffset > goalDatum().bathUseTopMaxYFeetOffset()) {
            return false;
        }

        // 直接设置位置到浴盆顶部
        bird().getNavigation().stop();
        bird().setPos(standPosition.x, standPosition.y, standPosition.z);
        bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.USING_BATH);
        bird().setDeltaMovement(Vec3.ZERO);
        bird().fallDistance = 0;

        this.mountHopCooldown = goalDatum().bathUseMountHopCooldownShort();
        return true;
    }

    /**
     * 检查鸟是否在浴盆顶部的使用位置
     *
     * @return 是否在正确位置
     */
    private boolean isAtTopUsePosition() {
        Vec3 feet = bird().position();
        double horizontalDistanceSqr = feet.subtract(bathStandPosition)
                .multiply(1, 0, 1)
                .lengthSqr();

        double feetOffset = feet.y - this.targetBath.getBlockPos().getY();

        return horizontalDistanceSqr <= goalDatum().bathUseTopHorizontalUseSqr()
                && feetOffset >= goalDatum().bathUseTopMinYFeetOffset()
                && feetOffset <= goalDatum().bathUseTopMaxYFeetOffset();
    }

    /**
     * 在需要时开始进食动画
     */
    private void startFeedingAnimationIfNeeded() {
        if (!this.feedingAnimationStarted && this.targetBath != null) {
            this.feedingAnimationStarted = true;

            if (bird() instanceof BirdBathFeedingAnimatable animatable) {
                animatable.startBirdBathFeedingAnimation(
                        this.targetBath.getContentType(),
                        goalDatum().bathUseConsumeWarmUpTicks()
                );
            }
        }
    }

    /**
     * 从浴盆中消耗食物
     * 消耗后决定是否跳离浴盆
     */
    private void consumeFromBath() {
        if (this.targetBath == null || this.consumed) {
            return;
        }

        BirdBathContentType consumedType = this.targetBath.getContentType();

        if (BirdBathAttraction.consumeServingForBird(this.targetBath)) {
            this.consumed = true;

            // 根据概率决定是否跳离浴盆
            if (bird().getRandom().nextFloat() < goalDatum().bathUseJumpDownChance()) {
                this.leavingBath = true;
                this.leaveCooldown = goalDatum().bathUseJumpDownTicks();
            } else {
                this.leavingBath = false;
            }

            // 通知进食控制器
            bird().getBirdControllers()
                    .getBirdEatingController()
                    .consumeBirdBathServing(targetBath, consumedType);
        }
    }

    /**
     * 检查鸟是否正在使用浴盆
     * 通过位置和状态双重判断
     *
     * @return 是否在使用浴盆
     */
    public boolean isUsingBath() {
        BlockPos pos = bird().blockPosition();
        BlockPos belowPos = bird().blockPosition().below();

        // 检查当前位置或下方是否有浴盆
        if (bird().level().getBlockEntity(pos) instanceof BirdBathBlockEntity
                || bird().level().getBlockEntity(belowPos) instanceof BirdBathBlockEntity) {
            return true;
        }

        if (targetBath == null) {
            return false;
        }

        // 通过行为状态判断
        BirdBehaviorState state = bird().getBehaviorStateController().getBehaviorState();
        return state == BirdBehaviorState.BATHING || state == BirdBehaviorState.USING_BATH;
    }

    /**
     * 从浴盆跳下
     * 计算跳跃方向并执行跳跃动作
     */
    private void jumpDownFromBath() {
        if (targetBath == null) {
            return;
        }

        Vec3 pos = bird().position();
        Vec3 center = Vec3.atCenterOf(targetBath.getBlockPos());

        // 计算从浴盆中心指向鸟的方向（即向外方向）
        Vec3 direction = pos.subtract(center).multiply(1, 0, 1);

        if (direction.lengthSqr() < 1.0E-4) {
            // 极端情况：随机生成一个方向
            float yaw = bird().getRandom().nextFloat() * ((float) Math.PI * 2);
            direction = new Vec3(Math.cos(yaw), 0, Math.sin(yaw));
        } else {
            direction = direction.normalize();
        }

        // 让鸟面向跳跃方向
        float yaw = (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
        bird().setYRot(yaw);
        bird().yBodyRot = yaw;
        bird().yHeadRot = yaw;

        bird().getNavigation().stop();

        // 计算跳跃速度
        Vec3 jump = direction.scale(goalDatum().bathUseHopHorizontalScale() * 0.5);
        bird().setDeltaMovement(
                jump.x,
                goalDatum().bathUseHopVerticalScale() * 0.5,
                jump.z
        );
        bird().fallDistance = 0;

        // 重置行为状态为空闲
        bird().getBehaviorStateController().setBehaviorState(BirdBehaviorState.IDLE);
        leavingBath = false;
    }
}