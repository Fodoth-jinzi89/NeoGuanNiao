package net.fodoth.skina.neoguanniao.content.bird.core.navigation;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class BirdPathNavigation extends GroundPathNavigation {
    private final AbstractBirdEntity<?> bird;

    public BirdPathNavigation(AbstractBirdEntity<?> bird, Level level) {
        super(bird, level);
        this.bird = bird;
    }

    @Override
    public boolean moveTo(double x, double y, double z, double speedModifier) {
        if (bird.getFlyingController().isFlightInProgress()) {
            return true;
        }
        if (shouldFlyTo(y)) {
            bird.getFlyingController().startShortFlight(new Vec3(x, y, z), false);
            return true;
        }
        return super.moveTo(x, y, z, speedModifier);
    }

    @Override
    public boolean moveTo(Entity entity, double speedModifier) {
        if (bird.getFlyingController().isFlightInProgress()) {
            return true;
        }
        if (shouldFlyTo(entity.getY())) {
            bird.getFlyingController().startShortFlight(entity.position(), false);
            return true;
        }
        return super.moveTo(entity, speedModifier);
    }

    private boolean shouldFlyTo(double targetY) {
        return bird.canFly() && bird.onGround() && !bird.isLeashed() && targetY > bird.getY() + 1.0D;
    }
}
