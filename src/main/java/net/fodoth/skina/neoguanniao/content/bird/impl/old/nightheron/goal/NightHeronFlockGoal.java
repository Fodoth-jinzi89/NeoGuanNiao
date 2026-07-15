package net.fodoth.skina.neoguanniao.content.bird.impl.old.nightheron.goal;

import net.fodoth.skina.neoguanniao.content.bird.impl.old.nightheron.NightHeronBehaviorState;
import net.fodoth.skina.neoguanniao.content.bird.impl.old.nightheron.NightHeronEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

/**
 * 夜鹭群体目标
 * 控制夜鹭在群体中的社交距离，避免过于拥挤或离群太远
 */
public class NightHeronFlockGoal extends Goal {
    private final NightHeronEntity nightHeron;
    private NightHeronEntity neighbor;
    private int remainingTicks;

    public NightHeronFlockGoal(NightHeronEntity nightHeron) {
        this.nightHeron = nightHeron;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.nightHeron.onGround() && !this.nightHeron.getBehaviorState().isEscape()
                && !this.nightHeron.getBehaviorState().isAirborne()
                && this.nightHeron.getRandom().nextInt(this.nightHeron.shouldRoost() ? 12 : 30) == 0) {

            Optional<NightHeronEntity> nearest = this.nearbyNightHerons().stream()
                    .min(Comparator.comparingDouble(this.nightHeron::distanceToSqr));

            if (nearest.isEmpty()) {
                return false;
            }

            this.neighbor = nearest.get();
            double distance = Math.sqrt(this.nightHeron.distanceToSqr(this.neighbor));
            return distance < 2.25 || (this.nightHeron.shouldRoost() && distance > 5.0);
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return this.remainingTicks > 0 && this.neighbor != null && this.neighbor.isAlive()
                && this.nightHeron.onGround() && !this.nightHeron.getBehaviorState().isEscape();
    }

    @Override
    public void start() {
        this.remainingTicks = 30 + this.nightHeron.getRandom().nextInt(45);
        this.nightHeron.setBehaviorState(NightHeronBehaviorState.SOCIAL_SPACING);
        this.moveForSpacing();
    }

    @Override
    public void stop() {
        this.remainingTicks = 0;
        this.neighbor = null;
        if (!this.nightHeron.getBehaviorState().isEscape()) {
            this.nightHeron.setBehaviorState(NightHeronBehaviorState.IDLE);
        }
    }

    @Override
    public void tick() {
        --this.remainingTicks;
        if (this.remainingTicks % 16 == 0 || this.nightHeron.getNavigation().isDone()) {
            this.moveForSpacing();
        }
    }

    /**
     * 移动以保持适当的社交距离
     */
    private void moveForSpacing() {
        if (this.neighbor == null) {
            return;
        }

        double distance = Math.sqrt(this.nightHeron.distanceToSqr(this.neighbor));
        Vec3 target;

        if (distance < 2.25) {
            // 太近了，远离邻居
            target = LandRandomPos.getPosAway(this.nightHeron, 6, 3, this.neighbor.position());
        } else {
            // 太远了，靠近邻居
            Vec3 toward = this.neighbor.position().subtract(this.nightHeron.position());
            target = this.nightHeron.position().add(toward.normalize().scale(Math.min(4.0, distance - 3.5)));
        }

        if (target != null) {
            this.nightHeron.getNavigation().moveTo(target.x, target.y, target.z, 0.17);
        }
    }

    /**
     * 获取附近的夜鹭列表
     */
    private List<NightHeronEntity> nearbyNightHerons() {
        return this.nightHeron.level().getEntitiesOfClass(
                NightHeronEntity.class,
                this.nightHeron.getBoundingBox().inflate(8.0),
                other -> other != this.nightHeron && other.isAlive()
        );
    }
}