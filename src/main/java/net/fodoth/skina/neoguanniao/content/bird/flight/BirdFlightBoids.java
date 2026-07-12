package net.fodoth.skina.neoguanniao.content.bird.flight;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 鸟类群体行为（Boids）算法
 * 模拟鸟群飞行时的群体行为，包括分离、对齐和凝聚
 */
public final class BirdFlightBoids {

    private BirdFlightBoids() {
    }

    /**
     * 计算同类型鸟类的群体行为向量
     *
     * @param bird 当前鸟类实体
     * @param radius 搜索半径
     * @param separationRadius 分离半径
     * @param cohesionWeight 凝聚权重
     * @param alignmentWeight 对齐权重
     * @param separationWeight 分离权重
     * @param randomnessWeight 随机性权重
     * @return 群体行为方向向量
     */
    @NotNull
    public static Vec3 sameTypeHeading(
            PathfinderMob bird,
            double radius,
            double separationRadius,
            double cohesionWeight,
            double alignmentWeight,
            double separationWeight,
            double randomnessWeight
    ) {
        List<PathfinderMob> nearby = bird.level().getEntitiesOfClass(
                PathfinderMob.class,
                bird.getBoundingBox().inflate(radius),
                other -> other != bird
                        && other.isAlive()
                        && other.getType() == bird.getType()
                        && other instanceof BirdFlightAware aware
                        && aware.isBirdFlightActive()
        );
        return headingFrom(bird, nearby, separationRadius, cohesionWeight, alignmentWeight, separationWeight, randomnessWeight);
    }

    /**
     * 计算群体行为向量
     *
     * @param bird 当前鸟类实体
     * @param nearby 附近实体列表
     * @param separationRadius 分离半径
     * @param cohesionWeight 凝聚权重
     * @param alignmentWeight 对齐权重
     * @param separationWeight 分离权重
     * @param randomnessWeight 随机性权重
     * @return 群体行为方向向量
     */
    @NotNull
    public static Vec3 headingFrom(
            PathfinderMob bird,
            List<? extends PathfinderMob> nearby,
            double separationRadius,
            double cohesionWeight,
            double alignmentWeight,
            double separationWeight,
            double randomnessWeight
    ) {
        if (nearby.isEmpty()) {
            return randomHeading(bird, randomnessWeight);
        }

        Vec3 separation = Vec3.ZERO;
        Vec3 alignment = Vec3.ZERO;
        Vec3 center = Vec3.ZERO;
        int alignmentCount = 0;
        int centerCount = 0;
        double separationSqr = separationRadius * separationRadius;

        for (PathfinderMob other : nearby) {
            Vec3 offset = bird.position().subtract(other.position());
            double distanceSqr = offset.lengthSqr();

            // 分离：远离过近的个体
            if (distanceSqr > 1.0E-4 && distanceSqr < separationSqr) {
                double distance = Math.sqrt(distanceSqr);
                separation = separation.add(
                        offset.normalize().scale((separationRadius - distance) / separationRadius)
                );
            }

            // 对齐：与周围个体的移动方向对齐
            Vec3 otherMovement = other.getDeltaMovement().multiply(1.0, 0.0, 1.0);
            if (otherMovement.lengthSqr() > 1.0E-4) {
                alignment = alignment.add(otherMovement.normalize());
                ++alignmentCount;
            }

            // 凝聚：向群体中心移动
            center = center.add(other.position());
            ++centerCount;
        }

        Vec3 heading = Vec3.ZERO;

        // 应用分离
        if (separation.lengthSqr() > 1.0E-4) {
            heading = heading.add(separation.normalize().scale(separationWeight));
        }

        // 应用对齐
        if (alignmentCount > 0 && alignment.lengthSqr() > 1.0E-4) {
            heading = heading.add(alignment.normalize().scale(alignmentWeight));
        }

        // 应用凝聚
        if (centerCount > 0) {
            Vec3 cohesion = center.scale(1.0 / centerCount)
                    .subtract(bird.position())
                    .multiply(1.0, 0.0, 1.0);
            if (cohesion.lengthSqr() > 1.0E-4) {
                heading = heading.add(cohesion.normalize().scale(cohesionWeight));
            }
        }

        // 添加随机性
        return heading.add(randomHeading(bird, randomnessWeight));
    }

    /**
     * 生成随机方向向量
     *
     * @param bird 鸟类实体
     * @param randomnessWeight 随机性权重
     * @return 随机方向向量
     */
    @NotNull
    private static Vec3 randomHeading(PathfinderMob bird, double randomnessWeight) {
        if (randomnessWeight <= 0.0) {
            return Vec3.ZERO;
        }
        return BirdFlightTargeting.randomHorizontalDirection(bird.getRandom())
                .scale(randomnessWeight);
    }
}