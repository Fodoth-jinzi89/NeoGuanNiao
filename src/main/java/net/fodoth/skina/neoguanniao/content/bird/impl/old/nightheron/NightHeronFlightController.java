package net.fodoth.skina.neoguanniao.content.bird.impl.old.nightheron;

import net.fodoth.skina.neoguanniao.content.bird.feature.flight.BirdFlightBoids;
import net.fodoth.skina.neoguanniao.content.bird.feature.flight.BirdFlightManager;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 夜鹭飞行控制器
 * 控制夜鹭的所有飞行行为，包括起飞、转向、着陆和各种飞行模式
 */
public final class NightHeronFlightController {

    private NightHeronFlightController() {
    }

    // ============ 起飞 ============

    /**
     * 执行起飞
     */
    public static void takeOff(NightHeronEntity nightHeron, Vec3 direction, double horizontalSpeed, double verticalBoost) {
        Vec3 safeDirection = chooseOpenDirection(nightHeron, normalizeHorizontal(direction, nightHeron.getLookAngle()), 4.5);
        nightHeron.markTakeoffFlapping();
        nightHeron.setBehaviorState(NightHeronBehaviorState.TAKEOFF);

        double safeVerticalBoost = hasVerticalClearance(nightHeron, 2.8) ? verticalBoost : Math.min(verticalBoost, 0.32);
        if (!isAirPathClear(nightHeron, safeDirection, 2.2, 0.25) && hasVerticalClearance(nightHeron, 4.0)) {
            safeVerticalBoost += 0.12;
        }

        Vec3 movement = nightHeron.getDeltaMovement().scale(0.35)
                .add(safeDirection.scale(horizontalSpeed))
                .add(0, safeVerticalBoost, 0);
        applyMovement(nightHeron, movement);
    }

    // ============ 各种飞行模式 ============

    public static void tickLowEscapeFlight(NightHeronEntity nightHeron, Vec3 direction, double speed, double targetHeight, double maxHeight) {
        tickDirectedFlight(nightHeron, direction, Math.max(speed, 0.42), targetHeight, maxHeight, -0.045, false, NightHeronBehaviorState.LOW_FLAP_ESCAPE);
    }

    public static void tickLocalFlight(NightHeronEntity nightHeron, Vec3 direction) {
        tickDirectedFlight(nightHeron, direction, 0.36, 7.0, 13.0, -0.045, false, NightHeronBehaviorState.LOCAL_FLIGHT);
    }

    public static void tickLongEscapeFlight(NightHeronEntity nightHeron, Vec3 direction, double speed, double targetHeight, double maxHeight) {
        tickDirectedFlight(nightHeron, direction, speed, targetHeight, maxHeight, -0.025, true, NightHeronBehaviorState.LONG_FLIGHT_ESCAPE);
    }

    public static void tickHighTransitFlight(NightHeronEntity nightHeron, Vec3 direction) {
        tickDirectedFlight(nightHeron, direction, 0.42, 18.0, 28.0, -0.025, true, NightHeronBehaviorState.HIGH_TRANSIT);
    }

    public static void tickSoaringFlight(NightHeronEntity nightHeron, Vec3 direction) {
        tickDirectedFlight(nightHeron, direction, 0.4, 23.0, 36.0, -0.025, true, NightHeronBehaviorState.SOARING);
    }

    public static boolean tickLanding(NightHeronEntity nightHeron, BlockPos landingTarget) {
        return tickLandingApproach(nightHeron, landingTarget);
    }

    // ============ 着陆 ============

    public static boolean shouldBeginLandingApproach(NightHeronEntity nightHeron, BlockPos landingTarget, int remainingTicks, double approachDistance) {
        if (landingTarget == null) {
            return false;
        }
        return remainingTicks <= 60 || nightHeron.distanceToSqr(Vec3.atCenterOf(landingTarget)) <= approachDistance * approachDistance;
    }

    public static boolean tickLandingApproach(NightHeronEntity nightHeron, BlockPos landingTarget) {
        Vec3 toTarget = Vec3.atCenterOf(landingTarget).subtract(nightHeron.position());
        Vec3 horizontal = new Vec3(toTarget.x, 0, toTarget.z);
        double horizontalDistance = horizontal.length();

        if (nightHeron.onGround()) {
            nightHeron.finishFlight(NightHeronBehaviorState.IDLE);
            return true;
        }

        nightHeron.setBehaviorState(NightHeronBehaviorState.LANDING);
        Vec3 targetDirection = horizontal.lengthSqr() <= 1.0E-4 ? nightHeron.getLookAngle() : horizontal.normalize();
        double height = nightHeron.heightAboveSurface();

        if (horizontalDistance <= 1.15) {
            // 最终着陆阶段
            Vec3 flareDirection = nightHeron.updateLandingApproachDirection(landingTarget, nightHeron.getDeltaMovement(), 0);
            double flareSpeed = height > 0.55 ? 0.22 : 0.099;
            double verticalSpeed = height > 1.65 ? -0.052 : (height > 0.65 ? -0.034 : -0.018);
            Vec3 desired = flareDirection.scale(flareSpeed).add(0, verticalSpeed, 0);
            Vec3 movement = nightHeron.getDeltaMovement().scale(0.35).add(desired.scale(0.65));
            applyMovement(nightHeron, movement);
            return false;
        }

        // 着陆接近阶段
        Vec3 approachDirection = nightHeron.updateLandingApproachDirection(landingTarget, targetDirection, horizontalDistance > 8.0 ? 0.18 : 0.08);
        boolean blockedApproach = !isAirPathClear(nightHeron, approachDirection, 3.0, 0.1);
        boolean stuckInPlace = nightHeron.tickFlightObstructionProbe(blockedApproach);

        if (blockedApproach || stuckInPlace) {
            Vec3 openDirection = chooseOpenDirection(nightHeron, approachDirection, 3.0);
            if (airPathScore(nightHeron, openDirection, 3.0, 0.1) < 1.25 || stuckInPlace) {
                tickBlockedRecovery(nightHeron, approachDirection);
                return false;
            }
            approachDirection = nightHeron.updateLandingApproachDirection(landingTarget, openDirection, 0.22);
        }

        double glidePathHeight = Mth.clamp(horizontalDistance * 0.18 + 0.55, 0.85, 7.0);
        double verticalSpeed = height > glidePathHeight
                ? -Mth.clamp((height - glidePathHeight) * 0.038, 0.025, 0.11)
                : (height < glidePathHeight - 1.4 && horizontalDistance > 7.0 ? 0.028 : (height > 1.3 ? -0.026 : -0.012));
        double speed = horizontalDistance > 7.0 ? 0.28 : 0.24;
        speed = BirdFlightManager.decelerateNearLanding(speed, horizontalDistance, 5.0, 0.46);

        Vec3 desired = approachDirection.scale(speed).add(0, verticalSpeed, 0);
        Vec3 movement = nightHeron.getDeltaMovement().scale(0.58).add(desired.scale(0.42));
        applyMovement(nightHeron, movement);
        return false;
    }

    public static void tickOpenLanding(NightHeronEntity nightHeron, Vec3 direction) {
        if (nightHeron.onGround()) {
            nightHeron.finishFlight(NightHeronBehaviorState.IDLE);
            return;
        }

        nightHeron.setBehaviorState(NightHeronBehaviorState.LANDING);
        Vec3 safeDirection = chooseOpenDirection(nightHeron, normalizeHorizontal(direction, nightHeron.getLookAngle()), 3.5);
        double descent = nightHeron.heightAboveSurface() > 1.5 ? -0.12 : -0.04;
        Vec3 desired = safeDirection.scale(0.24).add(0, descent, 0);
        Vec3 movement = nightHeron.getDeltaMovement().scale(0.62).add(desired.scale(0.38));
        applyMovement(nightHeron, movement);
    }

    public static void tickBlockedRecovery(NightHeronEntity nightHeron, Vec3 preferredDirection) {
        if (nightHeron.onGround()) {
            nightHeron.finishFlight(NightHeronBehaviorState.IDLE);
            return;
        }

        nightHeron.clearLandingApproach();
        nightHeron.markBlockedFlightRecovery();
        nightHeron.setBehaviorState(NightHeronBehaviorState.LANDING);

        Vec3 recoveryDirection = nightHeron.getBlockedFlightRecoveryDirection();
        if (recoveryDirection.lengthSqr() <= 1.0E-4) {
            recoveryDirection = chooseRecoveryDirection(nightHeron, preferredDirection, 3.4);
            nightHeron.lockBlockedFlightRecoveryDirection(recoveryDirection, 28);
        }

        double horizontalSpeed = airPathScore(nightHeron, recoveryDirection, 3.2, 0.1) >= 0.55 ? 0.18 : 0.08;
        double height = nightHeron.heightAboveSurface();
        double descent = height > 3.0 ? -0.105 : (height > 1.1 ? -0.075 : -0.035);
        Vec3 desired = recoveryDirection.scale(horizontalSpeed).add(0, descent, 0);
        Vec3 movement = nightHeron.getDeltaMovement().scale(0.25).add(desired.scale(0.75));
        applyMovement(nightHeron, movement);
    }

    public static boolean shouldGlide(NightHeronEntity nightHeron) {
        Vec3 movement = nightHeron.getDeltaMovement();
        return nightHeron.heightAboveSurface() >= 8.5 && movement.horizontalDistance() >= 0.15 && movement.y <= 0.09;
    }

    // ============ 核心飞行控制 ============
    @SuppressWarnings("all")
    private static void tickDirectedFlight(NightHeronEntity nightHeron, Vec3 direction, double speed, double targetHeight, double maxHeight, double cruiseDescent, boolean allowGlide, NightHeronBehaviorState defaultFlightState) {
        Vec3 requestedDirection = normalizeHorizontal(direction, nightHeron.getLookAngle());

        if (defaultFlightState != NightHeronBehaviorState.LOCAL_FLIGHT) {
            Vec3 flockHeading = BirdFlightBoids.sameTypeHeading(nightHeron, 26.0, 5.0, 0.018, 0.34, 0.06, defaultFlightState.isEscape() ? 0.14 : 0.06);
            if (flockHeading.lengthSqr() > 1.0E-4) {
                requestedDirection = normalizeHorizontal(requestedDirection.add(flockHeading), requestedDirection);
            }
        }

        Vec3 safeDirection = chooseOpenDirection(nightHeron, requestedDirection, defaultFlightState == NightHeronBehaviorState.LOCAL_FLIGHT ? 4.0 : 6.0);
        double height = nightHeron.heightAboveSurface();
        Vec3 currentMovement = nightHeron.getDeltaMovement();
        double clearScore = airPathScore(nightHeron, safeDirection, 3.2, 0.25);
        boolean blockedAhead = clearScore < 1.25;
        boolean hasClimbRoom = hasVerticalClearance(nightHeron, 3.6) && height < maxHeight - 1.0;
        boolean stuckInPlace = nightHeron.tickFlightObstructionProbe(blockedAhead);
        boolean cruiseBand = height >= targetHeight - 0.8 && height <= maxHeight - 0.75;
        boolean glideWindow = allowGlide && cruiseBand && clearScore >= 2.2 && !blockedAhead;

        double lift;
        if (!nightHeron.isInWater() && !blockedAhead && !stuckInPlace) {
            if (height < targetHeight) {
                lift = height < targetHeight - 5.0 ? 0.24 : 0.15;
                nightHeron.setBehaviorState(NightHeronBehaviorState.CLIMB);
            } else if (height > maxHeight) {
                lift = cruiseDescent;
                nightHeron.setBehaviorState(allowGlide ? NightHeronBehaviorState.GLIDE : defaultFlightState);
            } else if (glideWindow) {
                lift = Math.max(cruiseDescent - 0.008, -0.055);
                nightHeron.setBehaviorState(NightHeronBehaviorState.GLIDE);
            } else {
                lift = allowGlide ? Math.max(cruiseDescent, -0.035) : 0.11;
                nightHeron.setBehaviorState(allowGlide && shouldGlide(nightHeron) ? NightHeronBehaviorState.GLIDE : defaultFlightState);
            }
        } else {
            if (stuckInPlace || !hasClimbRoom || clearScore < 0.75) {
                tickBlockedRecovery(nightHeron, safeDirection);
                return;
            }
            lift = 0.19;
            nightHeron.setBehaviorState(NightHeronBehaviorState.CLIMB);
        }

        boolean fastFlap = shouldFastFlap(nightHeron, height, targetHeight, maxHeight, allowGlide);
        double flightSpeed = speed;
        if (fastFlap && height < maxHeight - 0.45) {
            flightSpeed = speed * 1.22;
            lift = Math.max(lift, 0.08) + 0.09;
            if (allowGlide || height < targetHeight) {
                nightHeron.setBehaviorState(NightHeronBehaviorState.CLIMB);
            }
        } else if (glideWindow) {
            flightSpeed = speed * 1.05;
            lift = Math.min(lift, -0.012);
        }

        Vec3 desired = safeDirection.scale(flightSpeed).add(0, lift, 0);
        double weight = glideWindow && !fastFlap ? 0.78 : 0.62;
        Vec3 movement = currentMovement.scale(weight).add(desired.scale(1.0 - weight));
        applyMovement(nightHeron, movement);
    }

    // ============ 辅助方法 ============

    private static boolean shouldFastFlap(NightHeronEntity nightHeron, double height, double targetHeight, double maxHeight, boolean allowGlide) {
        if (nightHeron.isTakeoffFlapping()) {
            return true;
        }
        if (height < targetHeight - 1.2) {
            return true;
        }
        if (height >= maxHeight - 0.75) {
            return false;
        }
        if (allowGlide && nightHeron.getDeltaMovement().horizontalDistance() < 0.19) {
            return true;
        }

        int ticks = Math.max(0, nightHeron.getControlledFlightTicks());
        int phase = Math.floorMod(ticks + nightHeron.getId() * 7, allowGlide ? 56 : 42);
        if (!allowGlide) {
            return phase < 18;
        }
        if (height < targetHeight + 1.5) {
            return phase < 18;
        }
        return phase < 12;
    }

    private static Vec3 normalizeHorizontal(Vec3 direction, Vec3 fallback) {
        Vec3 horizontal = new Vec3(direction.x, 0, direction.z);
        if (horizontal.lengthSqr() <= 1.0E-4) {
            horizontal = new Vec3(fallback.x, 0, fallback.z);
        }
        return horizontal.lengthSqr() <= 1.0E-4 ? new Vec3(1, 0, 0) : horizontal.normalize();
    }

    private static Vec3 chooseOpenDirection(NightHeronEntity nightHeron, Vec3 preferred, double lookAhead) {
        Vec3 baseDirection = normalizeHorizontal(preferred, nightHeron.getLookAngle());
        Vec3 bestDirection = baseDirection;
        double bestScore = airPathScore(nightHeron, baseDirection, lookAhead, 0.25);

        for (double angle : new double[]{0.32, -0.32, 0.68, -0.68, 1.05, -1.05, 1.55, -1.55}) {
            Vec3 candidate = rotateHorizontal(baseDirection, angle);
            double score = airPathScore(nightHeron, candidate, lookAhead, 0.25);
            if (score > bestScore) {
                bestScore = score;
                bestDirection = candidate;
            }
        }
        return bestDirection;
    }

    @SuppressWarnings("SameParameterValue")
    private static Vec3 chooseRecoveryDirection(NightHeronEntity nightHeron, Vec3 preferred, double lookAhead) {
        Vec3 baseDirection = normalizeHorizontal(preferred.scale(-1), nightHeron.getDeltaMovement().scale(-1));
        Vec3 bestDirection = baseDirection;
        double bestScore = airPathScore(nightHeron, baseDirection, lookAhead, 0.1);

        for (double angle : new double[]{0.45, -0.45, 0.95, -0.95, 1.45, -1.45, 2.15, -2.15}) {
            Vec3 candidate = rotateHorizontal(baseDirection, angle);
            double score = airPathScore(nightHeron, candidate, lookAhead, 0.1);
            if (score > bestScore) {
                bestScore = score;
                bestDirection = candidate;
            }
        }
        return bestDirection;
    }

    @SuppressWarnings("all")
    private static boolean isAirPathClear(NightHeronEntity nightHeron, Vec3 direction, double distance, double verticalOffset) {
        return airPathScore(nightHeron, direction, distance, verticalOffset) >= distance;
    }

    private static double airPathScore(NightHeronEntity nightHeron, Vec3 direction, double distance, double verticalOffset) {
        Vec3 safeDirection = normalizeHorizontal(direction, nightHeron.getLookAngle());
        double score = 0;
        int samples = Math.max(2, Mth.floor(distance));

        for (int step = 1; step <= samples; step++) {
            Vec3 offset = safeDirection.scale(distance * step / samples).add(0, verticalOffset, 0);
            if (isAirSpaceClear(nightHeron, offset)) {
                score += distance / samples;
            }
        }
        return score;
    }

    private static boolean hasVerticalClearance(NightHeronEntity nightHeron, double height) {
        for (double yOffset = 0.45; yOffset <= height; yOffset += 0.75) {
            if (!isAirSpaceClear(nightHeron, new Vec3(0, yOffset, 0))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isAirSpaceClear(NightHeronEntity nightHeron, Vec3 offset) {
        Level level = nightHeron.level();
        AABB box = nightHeron.getBoundingBox().move(offset).inflate(-0.06, -0.02, -0.06);
        if (!canReadBox(level, box)) {
            return false;
        }
        return level.noCollision(nightHeron, box) && !containsBlockedFluid(level, box);
    }

    private static boolean canReadBox(Level level, AABB box) {
        int minX = Mth.floor(box.minX);
        int maxX = Mth.floor(box.maxX);
        int minZ = Mth.floor(box.minZ);
        int maxZ = Mth.floor(box.maxZ);
        int y = Mth.floor(box.minY);
        return NightHeronEntity.canReadChunk(level, new BlockPos(minX, y, minZ))
                && NightHeronEntity.canReadChunk(level, new BlockPos(minX, y, maxZ))
                && NightHeronEntity.canReadChunk(level, new BlockPos(maxX, y, minZ))
                && NightHeronEntity.canReadChunk(level, new BlockPos(maxX, y, maxZ));
    }

    private static boolean containsBlockedFluid(Level level, AABB box) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        int minX = Mth.floor(box.minX);
        int maxX = Mth.floor(box.maxX);
        int minY = Mth.floor(box.minY);
        int maxY = Mth.floor(box.maxY);
        int minZ = Mth.floor(box.minZ);
        int maxZ = Mth.floor(box.maxZ);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    mutablePos.set(x, y, z);
                    if (level.getFluidState(mutablePos).is(FluidTags.WATER) || level.getFluidState(mutablePos).is(FluidTags.LAVA)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static Vec3 rotateHorizontal(Vec3 direction, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new Vec3(
                direction.x * cos - direction.z * sin,
                0,
                direction.x * sin + direction.z * cos
        ).normalize();
    }

    private static void applyMovement(NightHeronEntity nightHeron, Vec3 movement) {
        nightHeron.setDeltaMovement(movement);
        nightHeron.hasImpulse = true;
        nightHeron.xxa = 0.0F;
        nightHeron.faceMovementDirection(movement);
    }
}