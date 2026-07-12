package net.fodoth.skina.neoguanniao.content.bird.sparrow.goal;

import net.fodoth.skina.neoguanniao.content.bird.sparrow.SparrowEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

public class SparrowFlockGoal extends Goal {
    private final SparrowEntity sparrow;
    private Vec3 target;

    public SparrowFlockGoal(SparrowEntity sparrow) {
        this.sparrow = sparrow;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        float sociability = this.sparrow.birdBrain().personality().sociability();
        int chance = Mth.clamp((int) (70.0F - sociability * 55.0F), 12, 70);

        if (!this.sparrow.isTame() && this.sparrow.getRandom().nextInt(chance) == 0) {
            List<SparrowEntity> flock = this.sparrow.level().getEntitiesOfClass(
                    SparrowEntity.class,
                    this.sparrow.getBoundingBox().inflate(9.0),
                    other -> other != this.sparrow && !other.isTame()
            );

            if (flock.isEmpty()) {
                return false;
            }

            double x = 0, y = 0, z = 0;
            for (SparrowEntity other : flock) {
                x += other.getX();
                y += other.getY();
                z += other.getZ();
            }

            Vec3 center = new Vec3(x / flock.size(), y / flock.size(), z / flock.size());
            if (this.sparrow.position().distanceToSqr(center) < 9.0) {
                return false;
            }
            if (sociability < 0.4F && this.sparrow.position().distanceToSqr(center) < 25.0
                    && this.sparrow.getRandom().nextBoolean()) {
                return false;
            }

            this.target = center;
            return true;
        }
        return false;
    }

    @Override
    public void start() {
        this.sparrow.getNavigation().moveTo(this.target.x, this.target.y, this.target.z, 0.82);
    }

    @Override
    public boolean canContinueToUse() {
        return !this.sparrow.getNavigation().isDone();
    }
}