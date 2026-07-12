package net.fodoth.skina.neoguanniao.content.bird.brain;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

/**
 * 鸟类感知系统
 * 管理鸟类的各种感官信息，包括视觉、听觉和环境感知
 */
public class BirdSenses {
    private Player nearestPlayer;
    private double nearestPlayerDistance = Double.MAX_VALUE;
    private boolean nearestPlayerSprinting;
    private boolean temptingPlayerNearby;
    private LivingEntity nearestPrey;
    private double nearestPreyDistance = Double.MAX_VALUE;
    private boolean nearWater;
    private boolean waterEdge;
    private boolean nearCover;
    private boolean nearRoost;
    private boolean airborne;
    private boolean onGround;
    private boolean activeTime;
    private boolean roostTime;
    private long dayTime;
    private BlockPos lastKnownWaterEdge;
    private BlockPos lastKnownRoost;

    public BirdSenses() {
    }

    /**
     * 每 tick 更新感知信息
     *
     * @param brain 鸟类大脑实例
     */
    public void tick(BirdBrain brain) {
        PathfinderMob bird = brain.bird();
        BirdSpeciesProfile profile = brain.profile();

        // 更新时间
        this.dayTime = bird.level().getDayTime() % 24000L;
        this.onGround = bird.onGround();
        this.airborne = !this.onGround;

        // 感知最近玩家
        this.nearestPlayer = bird.level().getNearestPlayer(bird, profile.playerSenseRadius());
        if (this.nearestPlayer != null && !this.nearestPlayer.isSpectator()) {
            this.nearestPlayerDistance = Math.sqrt(bird.distanceToSqr(this.nearestPlayer));
            this.nearestPlayerSprinting = this.nearestPlayer.isSprinting();
            this.temptingPlayerNearby = profile.isTemptingPlayer(this.nearestPlayer);
        } else {
            this.nearestPlayer = null;
            this.nearestPlayerDistance = Double.MAX_VALUE;
            this.nearestPlayerSprinting = false;
            this.temptingPlayerNearby = false;
        }

        // 感知最近猎物
        this.nearestPrey = profile.findNearestPrey(bird);
        this.nearestPreyDistance = this.nearestPrey == null
                ? Double.MAX_VALUE
                : Math.sqrt(bird.distanceToSqr(this.nearestPrey));

        // 环境感知
        this.nearWater = profile.isNearWater(bird);
        this.waterEdge = profile.isWaterEdge(bird);
        this.nearCover = profile.isNearCover(bird);
        this.nearRoost = profile.isNearRoost(bird);
        this.activeTime = profile.isActiveTime(this);
        this.roostTime = profile.isRoostTime(this);

        // 记录已知位置
        if (this.waterEdge) {
            this.lastKnownWaterEdge = bird.blockPosition();
        }
        if (this.nearRoost) {
            this.lastKnownRoost = bird.blockPosition();
        }
    }

    // ============ Getters ============

    @Nullable
    public Player nearestPlayer() {
        return this.nearestPlayer;
    }

    public double nearestPlayerDistance() {
        return this.nearestPlayerDistance;
    }

    public boolean nearestPlayerSprinting() {
        return this.nearestPlayerSprinting;
    }

    public boolean temptingPlayerNearby() {
        return this.temptingPlayerNearby;
    }

    @Nullable
    public LivingEntity nearestPrey() {
        return this.nearestPrey;
    }

    public double nearestPreyDistance() {
        return this.nearestPreyDistance;
    }

    public boolean nearWater() {
        return this.nearWater;
    }

    public boolean waterEdge() {
        return this.waterEdge;
    }

    public boolean nearCover() {
        return this.nearCover;
    }

    public boolean nearRoost() {
        return this.nearRoost;
    }

    public boolean isAirborne() {
        return this.airborne;
    }

    public boolean isOnGround() {
        return this.onGround;
    }

    public boolean activeTime() {
        return this.activeTime;
    }

    public boolean roostTime() {
        return this.roostTime;
    }

    public long dayTime() {
        return this.dayTime;
    }

    @Nullable
    public BlockPos lastKnownWaterEdge() {
        return this.lastKnownWaterEdge;
    }

    @Nullable
    public BlockPos lastKnownRoost() {
        return this.lastKnownRoost;
    }

    /**
     * 检查附近是否有威胁
     * 默认检测距离为 16 格内的玩家
     *
     * @return 如果有威胁则返回 true
     */
    public boolean hasNearbyThreat() {
        return this.nearestPlayer != null && this.nearestPlayerDistance < 16.0;
    }

    @Override
    public String toString() {
        return "BirdSenses{" +
                "nearestPlayer=" + (nearestPlayer != null ? nearestPlayer.getName() : "null") +
                ", nearestPlayerDistance=" + nearestPlayerDistance +
                ", nearestPlayerSprinting=" + nearestPlayerSprinting +
                ", temptingPlayerNearby=" + temptingPlayerNearby +
                ", nearWater=" + nearWater +
                ", waterEdge=" + waterEdge +
                ", nearCover=" + nearCover +
                ", nearRoost=" + nearRoost +
                ", airborne=" + airborne +
                ", onGround=" + onGround +
                ", activeTime=" + activeTime +
                ", roostTime=" + roostTime +
                ", dayTime=" + dayTime +
                '}';
    }
}