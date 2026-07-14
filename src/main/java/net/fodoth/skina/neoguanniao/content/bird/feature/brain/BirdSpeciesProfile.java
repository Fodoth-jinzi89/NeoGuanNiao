package net.fodoth.skina.neoguanniao.content.bird.feature.brain;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

/**
 * 鸟类物种配置文件抽象类
 * 定义不同鸟类的行为参数和感知逻辑
 */
public abstract class BirdSpeciesProfile {

    public BirdSpeciesProfile() {
    }

    // ============ 基础性格参数 ============

    /**
     * 玩家感知半径
     */
    public double playerSenseRadius() {
        return 18.0;
    }

    /**
     * 基础胆量 (0-1)
     */
    public float baseBoldness() {
        return 0.35F;
    }

    /**
     * 基础警惕性 (0-1)
     */
    public float baseWariness() {
        return 0.65F;
    }

    /**
     * 基础活跃度 (0-1)
     */
    public float baseActivity() {
        return 0.5F;
    }

    /**
     * 基础社交性 (0-1)
     */
    public float baseSociability() {
        return 0.45F;
    }

    /**
     * 基础易惊性 (0-1)
     */
    public float baseFlightiness() {
        return 0.55F;
    }

    // ============ 动机变化参数 ============

    /**
     * 每 tick 饥饿值增加量
     */
    public float hungerGainPerTick(BirdSenses senses) {
        return senses.activeTime() ? 1.8E-4F : 8.0E-5F;
    }

    /**
     * 每 tick 飞行疲劳值增加量
     */
    public float flightFatigueGainPerTick() {
        return 0.0015F;
    }

    /**
     * 每 tick 休息疲劳恢复量
     */
    public float restFatigueRecoveryPerTick(BirdSenses senses) {
        return !senses.nearRoost() && !senses.roostTime() ? 0.0011F : 0.0022F;
    }

    /**
     * 恐惧上升速率
     */
    public float fearRiseRate() {
        return 0.08F;
    }

    /**
     * 恐惧下降速率
     */
    public float fearFallRate() {
        return 0.018F;
    }

    // ============ 计算逻辑 ============

    /**
     * 计算舒适度 (0-1)
     */
    public float computeComfort(BirdSenses senses) {
        float comfort = 0.35F;
        if (senses.nearWater()) {
            comfort += 0.18F;
        }
        if (senses.nearCover()) {
            comfort += 0.18F;
        }
        if (senses.nearRoost()) {
            comfort += 0.2F;
        }
        if (senses.hasNearbyThreat()) {
            comfort -= 0.22F;
        }
        return this.clamp(comfort);
    }

    /**
     * 计算风险分数 (0-1)
     */
    public float computeRisk(BirdBrain brain) {
        BirdSenses senses = brain.senses();
        BirdMotivation motivation = brain.motivation();
        BirdPersonality personality = brain.personality();

        float risk = 0.0F;

        // 玩家感知风险
        if (senses.nearestPlayer() != null) {
            double radius = Math.max(1.0, this.playerSenseRadius());
            float closeness = (float) (1.0 - Mth.clamp(senses.nearestPlayerDistance() / radius, 0.0, 1.0));
            risk += closeness * 0.62F;

            if (senses.nearestPlayerSprinting()) {
                risk += 0.18F;
            }
            if (senses.temptingPlayerNearby()) {
                risk -= 0.16F;
            }
        }

        // 环境风险
        if (senses.nearCover()) {
            risk -= 0.08F;
        }

        // 性格影响
        risk += personality.wariness() * 0.22F;
        risk += personality.flightiness() * 0.18F;
        risk -= personality.boldness() * 0.22F;

        // 动机影响
        risk -= motivation.hunger() * 0.08F;
        risk += motivation.fear() * 0.24F;

        return this.clamp(risk);
    }

    // ============ 意图决策 ============

    /**
     * 是否想要觅食
     */
    public boolean wantsForage(BirdBrain brain) {
        return brain.senses().activeTime()
                && brain.motivation().hunger() > 0.45F
                && brain.computeRiskScore() < 0.55F;
    }

    /**
     * 是否想要栖息
     */
    public boolean wantsRoost(BirdBrain brain) {
        return brain.senses().roostTime()
                && (brain.motivation().roostNeed() > 0.35F || brain.motivation().fatigue() > 0.55F)
                && brain.computeRiskScore() < 0.7F;
    }

    /**
     * 是否想要短距离逃跑
     */
    public boolean wantsShortEscape(BirdBrain brain) {
        float risk = brain.computeRiskScore();
        return risk >= 0.58F && risk < 0.78F;
    }

    /**
     * 是否想要长距离逃跑
     */
    public boolean wantsLongEscape(BirdBrain brain) {
        return brain.computeRiskScore() >= 0.78F;
    }

    // ============ 抽象方法 ============

    /**
     * 判断当前时间是否属于活动时间
     */
    public abstract boolean isActiveTime(BirdSenses senses);

    /**
     * 判断当前时间是否属于栖息时间
     */
    public abstract boolean isRoostTime(BirdSenses senses);

    /**
     * 判断是否为偏好猎物
     */
    public abstract boolean isPreferredPrey(LivingEntity entity);

    // ============ 可重写方法（子类可定制） ============

    /**
     * 判断玩家是否具有诱惑力
     */
    public boolean isTemptingPlayer(Player player) {
        return false;
    }

    /**
     * 寻找最近的猎物
     */
    @Nullable
    public LivingEntity findNearestPrey(PathfinderMob bird) {
        return null;
    }

    /**
     * 判断是否靠近水域
     */
    public boolean isNearWater(PathfinderMob bird) {
        return false;
    }

    /**
     * 判断是否在水域边缘
     */
    public boolean isWaterEdge(PathfinderMob bird) {
        return false;
    }

    /**
     * 判断是否靠近遮蔽物
     */
    public boolean isNearCover(PathfinderMob bird) {
        return false;
    }

    /**
     * 判断是否靠近栖息点
     */
    public boolean isNearRoost(PathfinderMob bird) {
        return false;
    }

    // ============ 工具方法 ============

    /**
     * 将值限制在 0.0 到 1.0 之间
     */
    protected float clamp(float value) {
        return Mth.clamp(value, 0.0F, 1.0F);
    }
}