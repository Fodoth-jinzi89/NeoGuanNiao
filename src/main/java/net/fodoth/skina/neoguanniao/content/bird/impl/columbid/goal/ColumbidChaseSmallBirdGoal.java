package net.fodoth.skina.neoguanniao.content.bird.impl.columbid.goal;

import net.fodoth.skina.neoguanniao.content.bird.impl.columbid.AbstractColumbidEntity;
import net.fodoth.skina.neoguanniao.content.bird.impl.columbid.ColumbidBehaviorState;
import net.fodoth.skina.neoguanniao.content.bird.impl.sparrow.SparrowEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;
import java.util.List;

public class ColumbidChaseSmallBirdGoal extends Goal {
    private final AbstractColumbidEntity columbid;
    private LivingEntity target;
    private int chaseTicks;
    private int repathTicks;

    public ColumbidChaseSmallBirdGoal(AbstractColumbidEntity columbid) {
        this.columbid = columbid;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!this.columbid.supportsChasing() || this.columbid.chaseCooldown <= 0
                || !this.columbid.canStartGroundSocialGoal() || this.columbid.getRandom().nextInt(420) != 0) {
            return false;
        }

        List<LivingEntity> candidates = this.columbid.level().getEntitiesOfClass(
                LivingEntity.class,
                this.columbid.getBoundingBox().inflate(8.0, 3.0, 8.0),
                entity -> {
                    if (entity instanceof SparrowEntity) {
                        return true;
                    }
                    if (!(entity instanceof AbstractColumbidEntity other)) {
                        return false;
                    }
                    return other.getClass() == this.columbid.getClass() && other != this.columbid
                            && other.isAlive() && !other.isTame() && other.pairPartnerUUID == null
                            && !this.columbid.isPairedWith(other);
                }
        );

        if (candidates.isEmpty()) {
            return false;
        }

        this.target = candidates.get(this.columbid.getRandom().nextInt(candidates.size()));
        return this.target.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        return this.chaseTicks > 0 && this.target != null && this.target.isAlive()
                && this.columbid.distanceToSqr(this.target) < 196.0
                && !this.columbid.isControlledFlightActive();
    }

    @Override
    public void start() {
        this.chaseTicks = 40 + this.columbid.getRandom().nextInt(61);
        this.repathTicks = 0;
        this.columbid.chaseCooldown = 700 + this.columbid.getRandom().nextInt(600);
        this.columbid.setBehaviorState(ColumbidBehaviorState.CHASING);
    }

    @Override
    public void tick() {
        --this.chaseTicks;
        this.columbid.getLookControl().setLookAt(this.target, 25.0F, 25.0F);

        if (this.columbid.distanceToSqr(this.target) < 3.2) {
            this.columbid.getNavigation().stop();
            this.chaseTicks = Math.min(this.chaseTicks, 12);
        } else {
            if (--this.repathTicks <= 0 || this.columbid.getNavigation().isDone()) {
                this.repathTicks = 8 + this.columbid.getRandom().nextInt(8);
                this.columbid.getNavigation().moveTo(this.target, 0.98);
            }
        }
    }

    @Override
    public void stop() {
        this.target = null;
        this.repathTicks = 0;
        if (this.columbid.getBehaviorState() == ColumbidBehaviorState.CHASING) {
            this.columbid.setBehaviorState(ColumbidBehaviorState.IDLE);
        }
    }
}