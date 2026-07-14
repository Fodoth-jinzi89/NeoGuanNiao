package net.fodoth.skina.neoguanniao.content.bird.impl.sparrow.goal;

import net.fodoth.skina.neoguanniao.content.bird.impl.sparrow.SparrowBehaviorState;
import net.fodoth.skina.neoguanniao.content.bird.impl.sparrow.SparrowEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class SparrowFleePlayerGoal extends Goal {
    private final SparrowEntity sparrow;
    private Player player;
    private Vec3 fleeTarget;

    public SparrowFleePlayerGoal(SparrowEntity sparrow) {
        this.sparrow = sparrow;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.sparrow.pendingScareTicks > 0) {
            return false;
        }

        double senseRange = this.sparrow.hasDistrustMemory() ? 11.0 : 6.5;
        if (this.sparrow.brainWantsEscapeOrAlert()) {
            senseRange += 2.0;
        }

        this.player = this.sparrow.level().getNearestPlayer(this.sparrow, senseRange);
        if (this.player != null && !this.sparrow.isComfortableNear(this.player)) {
            boolean strongFlee = this.sparrow.isDistrusted(this.player) || this.sparrow.birdBrain().wantsLongEscape();
            this.fleeTarget = DefaultRandomPos.getPosAway(this.sparrow, strongFlee ? 14 : 9, strongFlee ? 7 : 5, this.player.position());
            return this.fleeTarget != null;
        }
        return false;
    }

    @Override
    public void start() {
        this.sparrow.setBehaviorStateFor(SparrowBehaviorState.FLEEING, 60);
        if (this.sparrow.isDistrusted(this.player)) {
            this.sparrow.alertNearbySparrows(this.player);
        }

        if (!this.sparrow.startEscapeFlight(this.player.position())) {
            this.sparrow.getNavigation().moveTo(this.fleeTarget.x, this.fleeTarget.y, this.fleeTarget.z, 1.18);
            this.sparrow.shortHop();
        }
    }

    @Override
    public boolean canContinueToUse() {
        return this.sparrow.isControlledFlightActive() || !this.sparrow.getNavigation().isDone();
    }

    @Override
    public void stop() {
        if (this.sparrow.getBehaviorState().isEscape() && !this.sparrow.isControlledFlightActive()) {
            this.sparrow.behaviorStateLockTicks = 0;
            this.sparrow.setBehaviorState(SparrowBehaviorState.IDLE);
        }
    }
}