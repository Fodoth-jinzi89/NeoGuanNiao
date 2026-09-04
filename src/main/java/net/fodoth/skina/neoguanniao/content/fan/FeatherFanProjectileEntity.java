package net.fodoth.skina.neoguanniao.content.fan;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.fan.FeatherFanEnchantments;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoEntityTypes;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoItems;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoParticleTypes;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoSoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class FeatherFanProjectileEntity
extends ThrowableItemProjectile {
    private static final EntityDataAccessor<Integer> DATA_STATE = SynchedEntityData.defineId(FeatherFanProjectileEntity.class, (EntityDataSerializer)EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_CHARGE = SynchedEntityData.defineId(FeatherFanProjectileEntity.class, (EntityDataSerializer)EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_RIVEN_TICKS = SynchedEntityData.defineId(FeatherFanProjectileEntity.class, (EntityDataSerializer)EntityDataSerializers.INT);
    private static final int MAX_OWNER_MISSING_TICKS = 200;
    private static final int MAX_TOTAL_LIFE_TICKS = 400;
    private static final int RETURN_COOLDOWN_TICKS = 12;
    private static final int ENTITY_STUCK_DURATION_TICKS = 100;
    private static final int BLOCK_STUCK_DURATION_TICKS = 70;
    private static final int STUCK_DAMAGE_DURATION_TICKS = 50;
    private static final double ENTITY_STUCK_DEPTH = 0.06;
    private static final int PULLOUT_DURATION_TICKS = 5;
    private static final int STUCK_DAMAGE_INTERVAL = 10;
    private static final float STUCK_DAMAGE = 1.0f;
    private static final double STUCK_SLOW_AMOUNT = -0.25;
    private static final ResourceLocation STUCK_SLOWDOWN_ID = ResourceLocation.fromNamespaceAndPath("neoguanniao", "feather_fan_stuck_slowdown");
    private static final int BURIAL_DURATION_TICKS = 40;
    private static final double BURIAL_PULL_RADIUS = 5.0;
    private static final int BURIAL_DAMAGE_INTERVAL_TICKS = 10;
    private static final float BURIAL_DAMAGE = 0.5f;
    private static final double BURIAL_SLASH_RADIUS = 4.0;
    private static final float BURIAL_SLASH_DAMAGE = 6.0f;
    public static final int RIVEN_PREPARE_END = 5;
    public static final int RIVEN_SPLIT_END = 14;
    public static final int RIVEN_LOCK_END = 19;
    public static final int RIVEN_CONVERGE_END = 26;
    public static final int RIVEN_END = 36;
    private static final double RIVEN_BURST_RADIUS = 3.75;
    private static final float RIVEN_MAIN_DAMAGE = 8.0f;
    private static final float RIVEN_MIN_SPLASH_DAMAGE = 3.0f;
    private static final float RIVEN_MAX_SPLASH_DAMAGE = 5.0f;
    private static final float RIVEN_REFORM_DAMAGE = 16.0f;
    private static final int HUNT_MAX_TARGETS = 7;
    public static final float HUNT_SPEED = 1.75f;
    private static final double HUNT_TURN_RATE = 0.24;
    private static final double HUNT_HIT_RANGE = 0.85;
    private static final double HUNT_ABANDON_RANGE = 20.0;
    private final Set<UUID> outboundHits = new HashSet<UUID>();
    private final Set<UUID> returnHits = new HashSet<UUID>();
    private final Set<UUID> huntedTargets = new HashSet<UUID>();
    private final Set<UUID> huntingLockedTargets = new LinkedHashSet<UUID>();
    private final Map<UUID, Boolean> burialTargetPhysics = new HashMap<UUID, Boolean>();
    private Vec3 throwOrigin = Vec3.ZERO;
    private float maxDistance = 6.0f;
    private float attackDamage = 3.0f;
    private float returnSpeed = 1.45f;
    private InteractionHand returnHand = InteractionHand.MAIN_HAND;
    private UUID ownerUuid;
    private UUID stuckEntityUuid;
    private UUID huntingTargetUuid;
    private BlockPos stuckBlockPos = BlockPos.ZERO;
    private Direction stuckFace = Direction.UP;
    private Vec3 stuckPosition = Vec3.ZERO;
    private Vec3 stuckOffset = Vec3.ZERO;
    private Vec3 stuckForward = new Vec3(0.0, 0.0, 1.0);
    private Vec3 stuckLocalForward = new Vec3(0.0, 0.0, 1.0);
    private Vec3 rivenAnchor = Vec3.ZERO;
    private int ownerMissingTicks;
    private int lifeTicks;
    private int returningTicks;
    private int stuckTicks;
    private int pulloutTicks;
    private int rivenTicks;
    private int huntingHop;
    private int clientStuckEntityId = -1;
    private int clientStuckFollowTicks;
    private boolean rivenDamageDone;
    private boolean rivenReformDamageDone;

    public FeatherFanProjectileEntity(EntityType<? extends FeatherFanProjectileEntity> entityType, Level level) {
        super(entityType, level);
    }

    public FeatherFanProjectileEntity(Level level, LivingEntity owner) {
        super((EntityType)NeoGuanNiaoEntityTypes.FEATHER_FAN_PROJECTILE.get(), owner, level);
        this.ownerUuid = owner.getUUID();
        this.throwOrigin = this.position();
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_STATE, FanState.OUTBOUND_SPIN.ordinal());
        builder.define(DATA_CHARGE, 0.0f);
        builder.define(DATA_RIVEN_TICKS, 0);
    }

    public void configureThrow(ItemStack fanStack, InteractionHand hand, float charge) {
        float clampedCharge = Mth.clamp((float)charge, (float)0.0f, (float)1.0f);
        this.setItem(fanStack);
        this.returnHand = hand;
        this.setFanState(FanState.OUTBOUND_SPIN);
        this.entityData.set(DATA_CHARGE, Float.valueOf(clampedCharge));
        this.throwOrigin = this.position();
        this.maxDistance = Mth.lerp((float)clampedCharge, (float)6.0f, (float)16.0f);
        this.attackDamage = Mth.lerp((float)clampedCharge, (float)3.0f, (float)7.0f);
        this.returnSpeed = Mth.lerp((float)clampedCharge, (float)1.45f, (float)1.85f);
    }

    public void configurePiercing(ItemStack fanStack, InteractionHand hand) {
        this.setItem(fanStack);
        this.returnHand = hand;
        this.setFanState(FanState.PIERCING);
        this.entityData.set(DATA_CHARGE, Float.valueOf(1.0f));
        this.throwOrigin = this.position();
        this.maxDistance = 20.0f;
        this.attackDamage = 7.0f;
        this.returnSpeed = 1.95f;
    }

    public void configureHunting(ItemStack fanStack, InteractionHand hand, float charge, List<LivingEntity> targets) {
        float clampedCharge = Mth.clamp((float)charge, (float)0.0f, (float)1.0f);
        this.setItem(fanStack);
        this.returnHand = hand;
        this.setFanState(FanState.HUNTING);
        this.entityData.set(DATA_CHARGE, Float.valueOf(clampedCharge));
        this.throwOrigin = this.position();
        this.maxDistance = 64.0f;
        this.attackDamage = 6.0f;
        this.returnSpeed = Mth.lerp((float)clampedCharge, (float)1.55f, (float)1.95f);
        this.huntingLockedTargets.clear();
        targets.stream().limit(7L).map(Entity::getUUID).forEach(this.huntingLockedTargets::add);
        this.huntingTargetUuid = this.huntingLockedTargets.stream().findFirst().orElse(null);
        this.huntingHop = 0;
        this.huntedTargets.clear();
    }

    @NotNull
    protected Item getDefaultItem() {
        return (Item)NeoGuanNiaoItems.WIND_FEATHER_FAN.get();
    }

    protected double getDefaultGravity() {
        return 0.0;
    }

    public void tick() {
        ServerPlayer owner;
        this.noPhysics = this.isNonCollidingState();
        if (!this.level().isClientSide) {
            if (++this.lifeTicks > 400) {
                this.dropFanAndDiscard();
                return;
            }
            owner = this.findServerOwner();
            if (owner == null) {
                this.releaseBurialTargets();
                this.removeStuckSlowdown();
                this.setFanState(FanState.RETURNING);
                this.noPhysics = true;
                this.setDeltaMovement(Vec3.ZERO);
                if (++this.ownerMissingTicks > 200) {
                    this.dropFanAndDiscard();
                    return;
                }
            } else {
                this.ownerMissingTicks = 0;
                if (!owner.isAlive() || owner.level() != this.level()) {
                    this.dropFanAndDiscard();
                    return;
                }
                this.setOwner((Entity)owner);
                switch (this.getFanState()) {
                    case RETURNING: {
                        if (!this.tickReturning(owner)) break;
                        return;
                    }
                    case STUCK_ENTITY: {
                        this.tickStuckEntity(owner);
                        break;
                    }
                    case STUCK_BLOCK: {
                        this.tickStuckBlock(owner);
                        break;
                    }
                    case BURIAL_VORTEX: {
                        this.tickBurialVortex(owner);
                        break;
                    }
                    case RIVEN_SEQUENCE: {
                        this.tickRivenSequence(owner);
                        break;
                    }
                    case HUNTING: {
                        this.tickHunting(owner);
                        break;
                    }
                    case OUTBOUND_SPIN: 
                    case PIERCING: {
                        this.hitEntitiesAlongMotion();
                    }
                }
                Level level = this.level();
                if (level instanceof ServerLevel) {
                    ServerLevel serverLevel = (ServerLevel)level;
                    FanState currentState = this.getFanState();
                    if (currentState == FanState.OUTBOUND_SPIN || currentState == FanState.PIERCING || currentState == FanState.HUNTING || currentState == FanState.RETURNING) {
                        this.spawnFlightTrail(serverLevel);
                    }
                }
            }
        }
        this.noPhysics = this.isNonCollidingState();
        super.tick();
        if (this.level().isClientSide) {
            this.tickClientStuckAttachment();
        }
        if (!this.level().isClientSide) {
            owner = this.findServerOwner();
            if (this.isFlyingOutbound() && this.position().distanceToSqr(this.throwOrigin) >= (double)(this.maxDistance * this.maxDistance)) {
                this.beginReturn(owner);
            }
            if (this.isReturning() && owner != null && this.position().distanceToSqr(FeatherFanProjectileEntity.returnTarget(owner)) <= 2.25) {
                this.returnFanToOwner(owner);
            }
        }
    }

    protected boolean canHitEntity(Entity target) {
        FanState state = this.getFanState();
        if (state == FanState.STUCK_ENTITY || state == FanState.STUCK_BLOCK || state == FanState.BURIAL_VORTEX || state == FanState.RIVEN_SEQUENCE || !(target instanceof LivingEntity) || target == this.getOwner() || isBird(target) || !super.canHitEntity(target)) {
            return false;
        }
        if (state == FanState.HUNTING) {
            return this.huntingTargetUuid != null && this.huntingTargetUuid.equals(target.getUUID()) && !this.huntedTargets.contains(target.getUUID());
        }
        Set<UUID> hits = state == FanState.RETURNING ? this.returnHits : this.outboundHits;
        return !hits.contains(target.getUUID());
    }

    protected void onHitEntity(EntityHitResult result) {
        Entity hit = result.getEntity();
        if (hit instanceof LivingEntity) {
            LivingEntity living = (LivingEntity)hit;
            this.hitLivingEntity(living, result.getLocation());
        }
    }

    private void hitLivingEntity(LivingEntity living, Vec3 hitLocation) {
        Set<UUID> hits;
        if (isBird(living)) {
            return;
        }
        if (this.getFanState() == FanState.HUNTING) {
            this.hitHuntingTarget(living);
            return;
        }
        if (this.getFanState() == FanState.PIERCING) {
            this.hitPiercingTarget(living, hitLocation);
            return;
        }
        Set<UUID> set = hits = this.isReturning() ? this.returnHits : this.outboundHits;
        if (!hits.add(living.getUUID()) || this.level().isClientSide) {
            return;
        }
        float damage = this.isReturning() ? this.attackDamage * 0.85f : this.attackDamage;
        int previousInvulnerableTime = living.invulnerableTime;
        if (this.isReturning()) {
            living.invulnerableTime = 0;
        }
        boolean damaged = living.hurt(this.damageSources().thrown((Entity)this, this.getOwner()), damage);
        if (this.isReturning()) {
            living.invulnerableTime = Math.max(living.invulnerableTime, previousInvulnerableTime);
        }
        if (!damaged) {
            return;
        }
        Vec3 movement = this.getDeltaMovement();
        living.knockback(0.25, -movement.x, -movement.z);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.7f, this.isReturning() ? 0.85f : 1.1f);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.TRIDENT_HIT, SoundSource.PLAYERS, 0.28f, 1.45f);
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            double hitY = living.getY(0.55);
            serverLevel.sendParticles((ParticleOptions)ParticleTypes.CRIT, living.getX(), hitY, living.getZ(), 6, 0.24, 0.24, 0.24, 0.14);
            serverLevel.sendParticles((ParticleOptions)ParticleTypes.POOF, living.getX(), hitY, living.getZ(), 2, 0.12, 0.12, 0.12, 0.025);
            serverLevel.sendParticles((ParticleOptions)ParticleTypes.WHITE_ASH, living.getX(), hitY, living.getZ(), 4, 0.2, 0.2, 0.2, 0.02);
            serverLevel.sendParticles((ParticleOptions)ParticleTypes.SWEEP_ATTACK, living.getX(), hitY, living.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    private void hitPiercingTarget(LivingEntity living, Vec3 hitLocation) {
        Level level;
        if (!this.outboundHits.add(living.getUUID())) {
            return;
        }
        Vec3 surfaceHit = this.findTargetSurface(living, hitLocation);
        if (this.level().isClientSide) {
            this.clientStuckEntityId = living.getId();
            this.clientStuckFollowTicks = 3;
            this.positionOnEntitySurface(living, surfaceHit);
            return;
        }
        boolean damaged = living.hurt(this.damageSources().thrown((Entity)this, this.getOwner()), this.attackDamage);
        Vec3 movement = this.getDeltaMovement();
        if (movement.lengthSqr() > 1.0E-6) {
            living.knockback(0.18, -movement.x, -movement.z);
        }
        if ((level = this.level()) instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            this.spawnPiercingHitEffects(serverLevel, living, damaged);
        }
        if (living.isAlive()) {
            this.stickToEntity(living, surfaceHit);
        } else {
            this.setPos(surfaceHit.x, surfaceHit.y, surfaceHit.z);
            this.beginReturn(this.findServerOwner());
        }
    }

    private void stickToEntity(LivingEntity target, Vec3 hitLocation) {
        Level level;
        this.stuckEntityUuid = target.getUUID();
        this.positionOnEntitySurface(target, hitLocation);
        PiercingArt art = this.getPiercingArt();
        this.setFanState(switch (art) {
            default -> throw new IncompatibleClassChangeError();
            case PiercingArt.BURIAL -> FanState.BURIAL_VORTEX;
            case PiercingArt.RIVEN -> FanState.RIVEN_SEQUENCE;
            case PiercingArt.NORMAL -> FanState.STUCK_ENTITY;
        });
        this.stuckTicks = 0;
        this.pulloutTicks = 0;
        this.rivenTicks = 0;
        this.rivenDamageDone = false;
        this.rivenReformDamageDone = false;
        this.entityData.set(DATA_RIVEN_TICKS, 0);
        this.rivenAnchor = this.getBurialCenter(target);
        if (art == PiercingArt.NORMAL) {
            this.applyStuckSlowdown(target);
        }
        if ((level = this.level()) instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            if (art == PiercingArt.RIVEN) {
                this.spawnRivenStartEffects(serverLevel, this.rivenAnchor);
            } else {
                this.spawnPinEffects(serverLevel);
            }
            if (art == PiercingArt.BURIAL) {
                this.spawnBurialStartEffects(serverLevel, this.getBurialCenter(target));
            }
        }
    }

    private void positionOnEntitySurface(LivingEntity target, Vec3 hitLocation) {
        this.captureStuckDirection();
        Vec3 attachedPosition = hitLocation.add(this.stuckForward.scale(0.06));
        float bodyYaw = target.yBodyRot * ((float)Math.PI / 180);
        this.stuckOffset = attachedPosition.subtract(target.position()).yRot(bodyYaw);
        this.stuckLocalForward = this.stuckForward.yRot(bodyYaw);
        this.setPos(attachedPosition.x, attachedPosition.y, attachedPosition.z);
        this.setDeltaMovement(Vec3.ZERO);
    }

    private Vec3 findTargetSurface(LivingEntity target, Vec3 fallback) {
        Vec3 movement = this.getDeltaMovement();
        if (movement.lengthSqr() < 1.0E-6) {
            return fallback;
        }
        Vec3 forward = movement.normalize();
        AABB surfaceBox = target.getBoundingBox().inflate(0.02);
        Vec3 rayStart = fallback.subtract(forward.scale(4.0));
        Vec3 rayEnd = fallback.add(forward.scale(4.0));
        return surfaceBox.clip(rayStart, rayEnd).orElseGet(() -> new Vec3(Mth.clamp((double)fallback.x, (double)surfaceBox.minX, (double)surfaceBox.maxX), Mth.clamp((double)fallback.y, (double)surfaceBox.minY, (double)surfaceBox.maxY), Mth.clamp((double)fallback.z, (double)surfaceBox.minZ, (double)surfaceBox.maxZ)));
    }

    private void stickToBlock(BlockHitResult result) {
        this.captureStuckDirection();
        this.stuckBlockPos = result.getBlockPos();
        this.stuckFace = result.getDirection();
        Vec3 surfaceOffset = Vec3.atLowerCornerOf((Vec3i)this.stuckFace.getNormal()).scale(0.035);
        this.stuckPosition = result.getLocation().add(surfaceOffset);
        this.setPos(this.stuckPosition.x, this.stuckPosition.y, this.stuckPosition.z);
        this.setDeltaMovement(Vec3.ZERO);
        this.setFanState(FanState.STUCK_BLOCK);
        this.stuckTicks = 0;
        this.pulloutTicks = 0;
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            this.spawnPinEffects(serverLevel);
        }
    }

    private void tickStuckEntity(ServerPlayer owner) {
        if (this.pulloutTicks > 0) {
            this.tickPullout(owner);
            return;
        }
        LivingEntity target = this.findStuckEntity();
        if (target == null || !target.isAlive() || isBird(target)) {
            this.beginPullout();
            return;
        }
        this.updateStuckAttachment(target);
        this.applyStuckSlowdown(target);
        ++this.stuckTicks;
        if (this.stuckTicks <= 50 && this.stuckTicks % 10 == 0) {
            Level level;
            Vec3 movementBeforeDamage = target.getDeltaMovement();
            int previousInvulnerableTime = target.invulnerableTime;
            target.invulnerableTime = 0;
            boolean damaged = target.hurt(this.damageSources().thrown((Entity)this, this.getOwner()), 1.0f);
            target.invulnerableTime = Math.max(target.invulnerableTime, previousInvulnerableTime);
            target.setDeltaMovement(movementBeforeDamage);
            if (damaged && (level = this.level()) instanceof ServerLevel) {
                ServerLevel serverLevel = (ServerLevel)level;
                this.spawnStuckPulse(serverLevel, target);
            }
        }
        if (!target.isAlive() || this.stuckTicks >= 100) {
            this.beginPullout();
        }
    }

    private void tickBurialVortex(ServerPlayer owner) {
        Level level;
        if (this.pulloutTicks > 0) {
            this.tickPullout(owner);
            return;
        }
        LivingEntity anchor = this.findStuckEntity();
        if (anchor == null || !anchor.isAlive()) {
            this.beginPullout();
            return;
        }
        this.updateStuckAttachment(anchor);
        Vec3 center = this.getBurialCenter(anchor);
        Vec3 collectionPoint = anchor.position();
        ++this.stuckTicks;
        this.captureBurialTargets(center, anchor);
        this.moveBurialTargets(collectionPoint);
        if (this.stuckTicks % 10 == 0) {
            this.damageBurialTargets(anchor);
        }
        if ((level = this.level()) instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            this.spawnBurialVortexEffects(serverLevel, center);
        }
        if (this.stuckTicks >= 40) {
            this.performBurialSlash(center);
            this.beginPullout();
        }
    }

    private void tickRivenSequence(ServerPlayer owner) {
        ServerLevel serverLevel;
        LivingEntity target = this.findStuckEntity();
        if (target != null && target.isAlive()) {
            this.rivenAnchor = target.getBoundingBox().getCenter();
            if (this.rivenTicks + 1 < 5) {
                this.updateStuckAttachment(target);
            } else {
                this.setPos(this.rivenAnchor.x, this.rivenAnchor.y, this.rivenAnchor.z);
            }
        } else {
            this.setPos(this.rivenAnchor.x, this.rivenAnchor.y, this.rivenAnchor.z);
        }
        this.setDeltaMovement(Vec3.ZERO);
        ++this.rivenTicks;
        this.entityData.set(DATA_RIVEN_TICKS, this.rivenTicks);
        Level level = this.level();
        if (level instanceof ServerLevel) {
            serverLevel = (ServerLevel)level;
            this.spawnRivenSequenceEffects(serverLevel, this.rivenAnchor);
        }
        if (this.rivenTicks >= 26 && !this.rivenDamageDone) {
            this.rivenDamageDone = true;
            this.performRivenBurst(this.rivenAnchor);
        }
        if (this.rivenTicks >= 36) {
            if (!this.rivenReformDamageDone) {
                this.rivenReformDamageDone = true;
                this.performRivenReformStrike(target);
                level = this.level();
                if (level instanceof ServerLevel) {
                    serverLevel = (ServerLevel)level;
                    this.spawnRivenReformEffects(serverLevel, this.rivenAnchor);
                }
            }
            this.beginReturn(owner);
        }
    }

    private void tickHunting(ServerPlayer owner) {
        LivingEntity target = this.findHuntingTarget();
        if (target == null || !target.isAlive() || !owner.canAttack(target)) {
            if (this.huntingTargetUuid != null) {
                this.huntedTargets.add(this.huntingTargetUuid);
            }
            Vec3 nextSearchOrigin = target == null ? this.position() : target.getBoundingBox().getCenter();
            this.chooseNextHuntingTargetOrReturn(nextSearchOrigin, owner);
            return;
        }
        Vec3 targetCenter = target.getBoundingBox().getCenter();
        Vec3 toTarget = targetCenter.subtract(this.position());
        double distance = toTarget.length();
        if (distance > 20.0 || !this.hasClearHuntingPath(this.position(), target)) {
            this.huntedTargets.add(target.getUUID());
            this.chooseNextHuntingTargetOrReturn(this.position(), owner);
            return;
        }
        if (distance <= 0.85) {
            this.hitHuntingTarget(target);
            return;
        }
        this.updateHuntingVelocity(targetCenter, distance);
        this.hitEntitiesAlongMotion();
        if (this.getFanState() != FanState.HUNTING || !target.getUUID().equals(this.huntingTargetUuid)) {
            return;
        }
        Vec3 nextPosition = this.position().add(this.getDeltaMovement());
        if (distance < 2.5 && nextPosition.distanceTo(targetCenter) > distance) {
            this.hitHuntingTarget(target);
        }
    }

    private void updateHuntingVelocity(Vec3 targetPosition, double distance) {
        Vec3 desiredDirection = targetPosition.subtract(this.position()).normalize();
        Vec3 current = this.getDeltaMovement();
        if (current.lengthSqr() < 0.001) {
            current = desiredDirection.scale(1.75);
        }
        Vec3 desiredVelocity = desiredDirection.scale(1.75);
        double steering = distance < 3.0 ? 0.48 : 0.24;
        Vec3 newVelocity = current.scale(1.0 - steering).add(desiredVelocity.scale(steering));
        newVelocity = newVelocity.lengthSqr() > 0.001 ? newVelocity.normalize().scale(1.75) : desiredVelocity;
        this.setDeltaMovement(newVelocity);
        this.hasImpulse = true;
    }

    private void hitHuntingTarget(LivingEntity target) {
        Level level;
        if (this.level().isClientSide || this.getFanState() != FanState.HUNTING || this.huntingTargetUuid == null || !this.huntingTargetUuid.equals(target.getUUID())) {
            return;
        }
        UUID targetUuid = target.getUUID();
        if (!this.huntedTargets.add(targetUuid)) {
            this.chooseNextHuntingTargetOrReturn(target.getBoundingBox().getCenter(), this.findServerOwner());
            return;
        }
        float damage = Math.max(3.5f, 6.0f * (1.0f - (float)this.huntingHop * 0.1f));
        boolean damaged = target.hurt(this.damageSources().thrown((Entity)this, this.getOwner()), damage);
        if (damaged) {
            Vec3 movement = this.getDeltaMovement();
            target.knockback(0.18, -movement.x, -movement.z);
        }
        if ((level = this.level()) instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            this.spawnHuntingHitEffects(serverLevel, target, this.huntingHop);
        }
        ++this.huntingHop;
        this.chooseNextHuntingTargetOrReturn(target.getBoundingBox().getCenter(), this.findServerOwner());
    }

    private void chooseNextHuntingTargetOrReturn(Vec3 origin, ServerPlayer owner) {
        if (owner == null || this.huntingHop >= 7) {
            this.beginReturn(owner);
            return;
        }
        LivingEntity next = this.findNextHuntingTarget(origin, owner);
        if (next == null) {
            this.beginReturn(owner);
            return;
        }
        this.setHuntingTarget(next);
    }

    private LivingEntity findNextHuntingTarget(Vec3 origin, ServerPlayer owner) {
        LivingEntity best = null;
        double bestDistance = Double.MAX_VALUE;
        Level level = this.level();
        if (!(level instanceof ServerLevel)) {
            return null;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        for (UUID targetUuid : this.huntingLockedTargets) {
            double distance;
            LivingEntity candidate;
            Entity entity;
            if (this.huntedTargets.contains(targetUuid) || targetUuid.equals(this.huntingTargetUuid) || !((entity = serverLevel.getEntity(targetUuid)) instanceof LivingEntity) || !(candidate = (LivingEntity)entity).isAlive() || candidate.isSpectator() || candidate == owner || isBird(candidate) || !owner.canAttack(candidate) || (distance = candidate.getBoundingBox().getCenter().distanceToSqr(origin)) > 400.0 || !this.hasClearHuntingPath(this.position(), candidate) || !(distance < bestDistance)) continue;
            bestDistance = distance;
            best = candidate;
        }
        return best;
    }

    private void setHuntingTarget(LivingEntity target) {
        Level level;
        this.huntingTargetUuid = target.getUUID();
        Vec3 direction = target.getBoundingBox().getCenter().subtract(this.position());
        if (direction.lengthSqr() > 0.001) {
            Vec3 desired = direction.normalize().scale(1.75);
            Vec3 redirected = this.getDeltaMovement().scale(0.35).add(desired.scale(0.65));
            this.setDeltaMovement(redirected.lengthSqr() > 0.001 ? redirected.normalize().scale(1.75) : desired);
            this.hasImpulse = true;
        }
        if ((level = this.level()) instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            this.spawnHuntingTurnEffects(serverLevel, target);
        }
    }

    private LivingEntity findHuntingTarget() {
        Level level;
        if (this.huntingTargetUuid == null || !((level = this.level()) instanceof ServerLevel)) {
            return null;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        Entity entity = serverLevel.getEntity(this.huntingTargetUuid);
        if (!(entity instanceof LivingEntity living)) {
            return null;
        }
        return isBird(living) ? null : living;
    }

    private boolean hasClearHuntingPath(Vec3 start, LivingEntity target) {
        Vec3 end = target.getBoundingBox().getCenter();
        BlockHitResult hit = this.level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)this));
        return hit.getType() == HitResult.Type.MISS;
    }

    private void tickClientStuckAttachment() {
        LivingEntity living;
        if (this.clientStuckEntityId < 0 || this.clientStuckFollowTicks <= 0) {
            this.clientStuckEntityId = -1;
            return;
        }
        FanState state = this.getFanState();
        boolean followsTarget = state == FanState.PIERCING || state == FanState.STUCK_ENTITY || state == FanState.BURIAL_VORTEX || state == FanState.RIVEN_SEQUENCE;
        Entity target = this.level().getEntity(this.clientStuckEntityId);
        if (!(followsTarget && target instanceof LivingEntity && (living = (LivingEntity)target).isAlive())) {
            this.clientStuckEntityId = -1;
            this.clientStuckFollowTicks = 0;
            return;
        }
        this.updateStuckAttachment(living);
        if (--this.clientStuckFollowTicks <= 0) {
            this.clientStuckEntityId = -1;
        }
    }

    private void updateStuckAttachment(LivingEntity target) {
        float bodyYaw = target.yBodyRot * ((float)Math.PI / 180);
        Vec3 rotatedForward = this.stuckLocalForward.yRot(-bodyYaw);
        if (rotatedForward.lengthSqr() > 1.0E-6) {
            this.stuckForward = rotatedForward.normalize();
            this.updateRotationFromDirection();
        }
        Vec3 position = target.position().add(this.stuckOffset.yRot(-bodyYaw));
        this.setPos(position.x, position.y, position.z);
        this.setDeltaMovement(Vec3.ZERO);
    }

    private Vec3 getBurialCenter(LivingEntity anchor) {
        return anchor.getBoundingBox().getCenter();
    }

    private void captureBurialTargets(Vec3 center, LivingEntity anchor) {
        Entity owner = this.getOwner();
        AABB area = new AABB(center, center).inflate(5.0);
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, area, target -> target.isAlive() && !target.isSpectator() && !target.isPassenger() && !target.isVehicle() && target != owner && target != anchor && !isBird(target));
        for (LivingEntity target2 : targets) {
            Vec3 targetCenter = target2.getBoundingBox().getCenter();
            if (targetCenter.distanceToSqr(center) > 25.0 || !this.hasBurialLineOfSight(center, target2)) continue;
            this.burialTargetPhysics.putIfAbsent(target2.getUUID(), target2.noPhysics);
            target2.noPhysics = true;
        }
    }

    private void moveBurialTargets(Vec3 collectionPoint) {
        Level level = this.level();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        Iterator<Map.Entry<UUID, Boolean>> iterator = this.burialTargetPhysics.entrySet().iterator();
        while (iterator.hasNext()) {
            LivingEntity target;
            Map.Entry<UUID, Boolean> entry = iterator.next();
            Entity entity = serverLevel.getEntity(entry.getKey());
            if (!(entity instanceof LivingEntity) || !(target = (LivingEntity)entity).isAlive()) {
                if (entity != null) {
                    entity.noPhysics = entry.getValue();
                }
                iterator.remove();
                continue;
            }
            target.noPhysics = true;
            Vec3 toPoint = collectionPoint.subtract(target.position());
            double distance = toPoint.length();
            if (distance <= 0.1) {
                target.setPos(collectionPoint.x, collectionPoint.y, collectionPoint.z);
            } else {
                double step = Math.min(distance, Mth.clamp((double)(distance * 0.18), (double)0.1, (double)0.4));
                Vec3 next = target.position().add(toPoint.scale(step / distance));
                target.setPos(next.x, next.y, next.z);
            }
            target.setDeltaMovement(Vec3.ZERO);
            target.fallDistance = 0.0f;
            target.hurtMarked = true;
        }
    }

    private void damageBurialTargets(LivingEntity anchor) {
        this.damageBurialTarget(anchor);
        Level level = this.level();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        for (UUID targetUuid : this.burialTargetPhysics.keySet()) {
            LivingEntity target;
            Entity entity = serverLevel.getEntity(targetUuid);
            if (!(entity instanceof LivingEntity) || !(target = (LivingEntity)entity).isAlive()) continue;
            this.damageBurialTarget(target);
        }
    }

    private void damageBurialTarget(LivingEntity target) {
        if (isBird(target)) {
            return;
        }
        Vec3 movementBeforeDamage = target.getDeltaMovement();
        int previousInvulnerableTime = target.invulnerableTime;
        target.invulnerableTime = 0;
        target.hurt(this.damageSources().thrown((Entity)this, this.getOwner()), 0.5f);
        target.invulnerableTime = Math.max(target.invulnerableTime, previousInvulnerableTime);
        target.setDeltaMovement(movementBeforeDamage);
        target.hurtMarked = true;
    }

    private void releaseBurialTargets() {
        if (this.burialTargetPhysics.isEmpty()) {
            return;
        }
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            for (Map.Entry entry : this.burialTargetPhysics.entrySet()) {
                Entity entity = serverLevel.getEntity((UUID)entry.getKey());
                if (entity == null) continue;
                entity.noPhysics = (Boolean)entry.getValue();
                entity.hurtMarked = true;
            }
        }
        this.burialTargetPhysics.clear();
    }

    private boolean hasBurialLineOfSight(Vec3 center, LivingEntity target) {
        BlockHitResult blockHit = this.level().clip(new ClipContext(center, target.getBoundingBox().getCenter(), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)this));
        return blockHit.getType() == HitResult.Type.MISS;
    }

    private void performBurialSlash(Vec3 center) {
        Entity owner = this.getOwner();
        AABB area = new AABB(center, center).inflate(4.0);
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, area, target -> target.isAlive() && !target.isSpectator() && target != owner && !isBird(target));
        for (LivingEntity target2 : targets) {
            Vec3 targetCenter = target2.getBoundingBox().getCenter();
            Vec3 away = targetCenter.subtract(center);
            if (away.lengthSqr() > 16.0 || !this.hasBurialLineOfSight(center, target2)) continue;
            Vec3 movementBeforeDamage = target2.getDeltaMovement();
            int previousInvulnerableTime = target2.invulnerableTime;
            target2.invulnerableTime = 0;
            boolean damaged = target2.hurt(this.damageSources().thrown((Entity)this, this.getOwner()), 6.0f);
            target2.invulnerableTime = Math.max(target2.invulnerableTime, previousInvulnerableTime);
            target2.setDeltaMovement(movementBeforeDamage);
            if (!damaged || !target2.isAlive()) continue;
            Vec3 horizontalAway = new Vec3(away.x, 0.0, away.z);
            if (horizontalAway.lengthSqr() > 1.0E-6) {
                horizontalAway = horizontalAway.normalize();
            }
            target2.setDeltaMovement(movementBeforeDamage.add(horizontalAway.x * 0.45, 0.18, horizontalAway.z * 0.45));
            target2.hurtMarked = true;
        }
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            this.spawnBurialSlashEffects(serverLevel, center);
        }
    }

    private PiercingArt getPiercingArt() {
        ItemStack fan = this.getItem();
        if (FeatherFanEnchantments.hasRivenPlume(fan)) {
            return PiercingArt.RIVEN;
        }
        if (FeatherFanEnchantments.hasBurialPlume(fan)) {
            return PiercingArt.BURIAL;
        }
        return PiercingArt.NORMAL;
    }

    private void performRivenBurst(Vec3 center) {
        Entity owner = this.getOwner();
        LivingEntity mainTarget = this.findStuckEntity();
        AABB area = new AABB(center, center).inflate(3.75);
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, area, target -> target.isAlive() && !target.isSpectator() && target != owner && !isBird(target));
        for (LivingEntity target2 : targets) {
            float damage;
            Vec3 targetCenter = target2.getBoundingBox().getCenter();
            Vec3 away = targetCenter.subtract(center);
            double distance = away.length();
            if (distance > 3.75 || !this.hasBurialLineOfSight(center, target2)) continue;
            if (target2 == mainTarget) {
                damage = 8.0f;
            } else {
                float factor = 1.0f - (float)(distance / 3.75);
                damage = Mth.lerp((float)factor, (float)3.0f, (float)5.0f);
            }
            Vec3 movementBeforeDamage = target2.getDeltaMovement();
            int previousInvulnerableTime = target2.invulnerableTime;
            target2.invulnerableTime = 0;
            boolean damaged = target2.hurt(this.damageSources().thrown((Entity)this, this.getOwner()), damage);
            target2.invulnerableTime = Math.max(target2.invulnerableTime, previousInvulnerableTime);
            target2.setDeltaMovement(movementBeforeDamage);
            if (!damaged || !target2.isAlive()) continue;
            Vec3 horizontalAway = new Vec3(away.x, 0.0, away.z);
            if (horizontalAway.lengthSqr() < 1.0E-6) {
                target2.setDeltaMovement(movementBeforeDamage.add(0.0, 0.3, 0.0));
            } else {
                horizontalAway = horizontalAway.normalize();
                target2.setDeltaMovement(movementBeforeDamage.add(horizontalAway.x * 0.55, 0.22, horizontalAway.z * 0.55));
            }
            target2.hurtMarked = true;
        }
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            this.spawnRivenBurstEffects(serverLevel, center);
        }
    }

    private void performRivenReformStrike(LivingEntity target) {
        Level level;
        if (target == null || !target.isAlive() || isBird(target)) {
            return;
        }
        Vec3 movementBeforeDamage = target.getDeltaMovement();
        int previousInvulnerableTime = target.invulnerableTime;
        target.invulnerableTime = 0;
        boolean damaged = target.hurt(this.damageSources().thrown((Entity)this, this.getOwner()), 16.0f);
        target.invulnerableTime = Math.max(target.invulnerableTime, previousInvulnerableTime);
        target.setDeltaMovement(movementBeforeDamage);
        target.hurtMarked = true;
        if (damaged && (level = this.level()) instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            Vec3 hitCenter = target.getBoundingBox().getCenter();
            serverLevel.sendParticles((ParticleOptions)ParticleTypes.CRIT, hitCenter.x, hitCenter.y, hitCenter.z, 20, 0.42, 0.42, 0.42, 0.2);
            serverLevel.sendParticles((ParticleOptions)ParticleTypes.ELECTRIC_SPARK, hitCenter.x, hitCenter.y, hitCenter.z, 14, 0.34, 0.34, 0.34, 0.15);
            this.level().playSound(null, hitCenter.x, hitCenter.y, hitCenter.z, SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.15f, 0.92f);
        }
    }

    private void tickStuckBlock(ServerPlayer owner) {
        if (this.pulloutTicks > 0) {
            this.tickPullout(owner);
            return;
        }
        if (this.level().getBlockState(this.stuckBlockPos).isAir()) {
            this.beginPullout();
            return;
        }
        this.setPos(this.stuckPosition.x, this.stuckPosition.y, this.stuckPosition.z);
        this.setDeltaMovement(Vec3.ZERO);
        if (++this.stuckTicks >= 70) {
            this.beginPullout();
        }
    }

    private void beginPullout() {
        if (this.pulloutTicks > 0) {
            return;
        }
        this.releaseBurialTargets();
        this.removeStuckSlowdown();
        this.pulloutTicks = 1;
        this.setPos(this.position().subtract(this.stuckForward.scale(0.05)));
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            this.spawnPulloutEffects(serverLevel);
        }
    }

    private void tickPullout(ServerPlayer owner) {
        this.setPos(this.position().subtract(this.stuckForward.scale(0.05)));
        this.setDeltaMovement(Vec3.ZERO);
        if (++this.pulloutTicks >= 5) {
            this.beginReturn(owner);
        }
    }

    private void captureStuckDirection() {
        Vec3 movement = this.getDeltaMovement();
        if (movement.lengthSqr() > 1.0E-6) {
            this.stuckForward = movement.normalize();
        }
        this.updateRotationFromDirection();
    }

    private void updateRotationFromDirection() {
        double horizontal = Math.sqrt(this.stuckForward.x * this.stuckForward.x + this.stuckForward.z * this.stuckForward.z);
        double radiansToDegrees = 57.29577951308232;
        this.setYRot((float)(Mth.atan2((double)this.stuckForward.x, (double)this.stuckForward.z) * radiansToDegrees));
        this.setXRot((float)(Mth.atan2((double)this.stuckForward.y, (double)horizontal) * radiansToDegrees));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    private LivingEntity findStuckEntity() {
        LivingEntity living;
        Level level;
        if (this.stuckEntityUuid == null || !((level = this.level()) instanceof ServerLevel)) {
            return null;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        Entity target = serverLevel.getEntity(this.stuckEntityUuid);
        return target instanceof LivingEntity ? (living = (LivingEntity)target) : null;
    }

    private void applyStuckSlowdown(LivingEntity target) {
        AttributeInstance movementSpeed = target.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed != null && movementSpeed.getModifier(STUCK_SLOWDOWN_ID) == null) {
            movementSpeed.addTransientModifier(new AttributeModifier(STUCK_SLOWDOWN_ID, -0.25, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    private void removeStuckSlowdown() {
        LivingEntity target = this.findStuckEntity();
        if (target == null) {
            return;
        }
        AttributeInstance movementSpeed = target.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed != null) {
            movementSpeed.removeModifier(STUCK_SLOWDOWN_ID);
        }
    }

    protected void onHitBlock(BlockHitResult result) {
        if (this.level().isClientSide || this.isNonCollidingState()) {
            return;
        }
        if (this.getFanState() == FanState.PIERCING) {
            this.stickToBlock(result);
            return;
        }
        if (this.getFanState() == FanState.HUNTING) {
            Vec3 movement = this.getDeltaMovement();
            if (movement.lengthSqr() > 1.0E-6) {
                this.setPos(result.getLocation().subtract(movement.normalize().scale(0.05)));
            }
            if (this.huntingTargetUuid != null) {
                this.huntedTargets.add(this.huntingTargetUuid);
            }
            this.chooseNextHuntingTargetOrReturn(this.position(), this.findServerOwner());
            return;
        }
        Vec3 movement = this.getDeltaMovement();
        if (movement.lengthSqr() > 1.0E-6) {
            this.setPos(result.getLocation().subtract(movement.normalize().scale(0.05)));
        }
        this.beginReturn(this.findServerOwner());
    }

    private void beginReturn(ServerPlayer owner) {
        Vec3 turn;
        if (this.isReturning()) {
            return;
        }
        FanState previousState = this.getFanState();
        this.releaseBurialTargets();
        this.removeStuckSlowdown();
        this.setFanState(FanState.RETURNING);
        this.noPhysics = true;
        this.returningTicks = 0;
        Vec3 current = this.getDeltaMovement();
        if (current.lengthSqr() < 1.0E-6) {
            current = this.stuckForward.scale(-1.0);
        }
        current = current.normalize();
        Vec3 desired = owner == null ? current.scale(-1.0) : FeatherFanProjectileEntity.returnTarget(owner).subtract(this.position()).normalize();
        Vec3 perpendicular = new Vec3(-current.z, 0.0, current.x);
        if ((this.getId() & 1) == 0) {
            perpendicular = perpendicular.scale(-1.0);
        }
        if ((turn = current.scale(0.45).add(perpendicular.scale(0.65)).add(desired.scale(0.4))).lengthSqr() < 1.0E-6) {
            turn = desired;
        }
        this.setDeltaMovement(turn.normalize().scale((double)this.returnSpeed));
        this.hasImpulse = true;
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            if (previousState == FanState.OUTBOUND_SPIN) {
                this.spawnTurnEffects(serverLevel);
            } else if (previousState == FanState.HUNTING) {
                this.spawnHuntingReturnEffects(serverLevel);
            } else {
                this.spawnPiercingReturnEffects(serverLevel);
            }
        }
        this.huntingTargetUuid = null;
        this.huntedTargets.clear();
        this.huntingLockedTargets.clear();
    }

    private boolean tickReturning(ServerPlayer owner) {
        ++this.returningTicks;
        Vec3 target = FeatherFanProjectileEntity.returnTarget(owner);
        Vec3 toOwner = target.subtract(this.position());
        double distance = toOwner.length();
        if (distance <= 1.6 || this.returningTicks > 100) {
            this.returnFanToOwner(owner);
            return true;
        }
        Vec3 desiredDirection = toOwner.normalize();
        Vec3 current = this.getDeltaMovement();
        if (distance > 5.0) {
            Vec3 desiredVelocity = desiredDirection.scale((double)this.returnSpeed);
            double alignment = current.lengthSqr() < 1.0E-6 ? 1.0 : current.normalize().dot(desiredDirection);
            double desiredWeight = alignment < -0.25 ? 0.65 : 0.45;
            Vec3 steering = current.scale(1.0 - desiredWeight).add(desiredVelocity.scale(desiredWeight));
            if (steering.lengthSqr() > 1.0E-6) {
                this.setDeltaMovement(steering.normalize().scale((double)this.returnSpeed));
            } else {
                this.setDeltaMovement(desiredVelocity);
            }
        } else {
            double speed = Mth.clamp((double)(distance * 0.38), (double)0.35, (double)this.returnSpeed);
            this.setDeltaMovement(desiredDirection.scale(speed));
        }
        this.hasImpulse = true;
        Vec3 next = this.position().add(this.getDeltaMovement());
        if (distance < 3.0 && next.distanceTo(target) > distance) {
            this.returnFanToOwner(owner);
            return true;
        }
        return false;
    }

    private void spawnFlightTrail(ServerLevel serverLevel) {
        Entity owner = this.getOwner();
        if (owner != null && this.distanceToSqr(owner) <= 6.25) {
            return;
        }
        if (this.getFanState() == FanState.PIERCING) {
            this.spawnPiercingTrail(serverLevel);
            return;
        }
        if (this.getFanState() == FanState.HUNTING) {
            this.spawnHuntingTrail(serverLevel);
            return;
        }
        Vec3 movement = this.getDeltaMovement();
        if (movement.lengthSqr() < 0.01) {
            return;
        }
        Vec3 forward = movement.normalize();
        Vec3 side = forward.cross(new Vec3(0.0, 1.0, 0.0));
        side = side.lengthSqr() < 1.0E-4 ? new Vec3(1.0, 0.0, 0.0) : side.normalize();
        Vec3 up = side.cross(forward).normalize();
        double phase = (double)this.tickCount * 0.9;
        double radius = this.isReturning() ? 0.15 : 0.12;
        Vec3 center = this.position().subtract(forward.scale(0.18));
        Vec3 offset = side.scale(Math.cos(phase) * radius).add(up.scale(Math.sin(phase) * radius));
        Vec3 opposite = offset.scale(-1.0);
        if (this.isReturning()) {
            serverLevel.sendParticles((ParticleOptions)ParticleTypes.SNOWFLAKE, center.x + offset.x, center.y + offset.y, center.z + offset.z, 1, 0.0, 0.0, 0.0, 0.0);
            serverLevel.sendParticles((ParticleOptions)ParticleTypes.SNOWFLAKE, center.x + opposite.x, center.y + opposite.y, center.z + opposite.z, 1, 0.0, 0.0, 0.0, 0.0);
        } else {
            serverLevel.sendParticles((ParticleOptions)ParticleTypes.WHITE_ASH, center.x + offset.x, center.y + offset.y, center.z + offset.z, 1, 0.0, 0.0, 0.0, 0.0);
            serverLevel.sendParticles((ParticleOptions)ParticleTypes.WHITE_ASH, center.x + opposite.x, center.y + opposite.y, center.z + opposite.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
        if (this.tickCount % 3 == 0) {
            serverLevel.sendParticles((ParticleOptions)ParticleTypes.SWEEP_ATTACK, center.x, center.y + 0.06, center.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
        if (this.tickCount % 6 == 0) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PHANTOM_FLAP, SoundSource.PLAYERS, 0.14f, this.isReturning() ? 1.55f : 1.8f);
        }
    }

    private void spawnPiercingTrail(ServerLevel serverLevel) {
        Vec3 movement = this.getDeltaMovement();
        if (movement.lengthSqr() < 0.01) {
            return;
        }
        Vec3 forward = movement.normalize();
        Vec3 side = forward.cross(new Vec3(0.0, 1.0, 0.0));
        side = side.lengthSqr() < 1.0E-4 ? new Vec3(1.0, 0.0, 0.0) : side.normalize();
        int segments = Mth.clamp((int)((int)Math.ceil(movement.length() * 2.0)), (int)3, (int)6);
        double spacing = movement.length() / (double)segments;
        for (int segment = 1; segment <= segments; ++segment) {
            Vec3 center = this.position().subtract(forward.scale((double)segment * spacing));
            Vec3 lineOffset = side.scale(0.085);
            serverLevel.sendParticles((ParticleOptions)ParticleTypes.WHITE_ASH, center.x + lineOffset.x, center.y + lineOffset.y, center.z + lineOffset.z, 1, 0.0, 0.0, 0.0, 0.0);
            serverLevel.sendParticles((ParticleOptions)ParticleTypes.WHITE_ASH, center.x - lineOffset.x, center.y - lineOffset.y, center.z - lineOffset.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    private void spawnHuntingTrail(ServerLevel serverLevel) {
        Vec3 movement = this.getDeltaMovement();
        if (movement.lengthSqr() < 0.01) {
            return;
        }
        Vec3 forward = movement.normalize();
        Vec3 center = this.position().subtract(forward.scale(0.24));
        serverLevel.sendParticles((ParticleOptions)((SimpleParticleType)NeoGuanNiaoParticleTypes.HUNTING_STREAK.get()), center.x, center.y, center.z, 1, 0.0, 0.0, 0.0, 0.0);
        if ((this.tickCount & 1) == 0) {
            serverLevel.sendParticles((ParticleOptions)ParticleTypes.WAX_ON, center.x, center.y, center.z, 2, 0.1, 0.1, 0.1, 0.018);
        }
    }

    private void spawnHuntingHitEffects(ServerLevel serverLevel, LivingEntity target, int hop) {
        Vec3 center = target.getBoundingBox().getCenter();
        this.level().playSound(null, center.x, center.y, center.z, (SoundEvent)NeoGuanNiaoSoundEvents.FEATHER_FAN_HUNT_HIT.get(), SoundSource.PLAYERS, 0.92f, 1.05f + (float)hop * 0.08f);
        serverLevel.sendParticles((ParticleOptions)((SimpleParticleType)NeoGuanNiaoParticleTypes.HUNTING_MARK.get()), center.x, center.y, center.z, 1, 0.0, 0.0, 0.0, 0.0);
        serverLevel.sendParticles((ParticleOptions)ParticleTypes.CRIT, center.x, center.y, center.z, 6, 0.22, 0.25, 0.22, 0.13);
        serverLevel.sendParticles((ParticleOptions)ParticleTypes.WAX_ON, center.x, center.y, center.z, 8, 0.28, 0.3, 0.28, 0.08);
    }

    private void spawnHuntingTurnEffects(ServerLevel serverLevel, LivingEntity nextTarget) {
        Vec3 nextCenter = nextTarget.getBoundingBox().getCenter();
        Vec3 direction = nextCenter.subtract(this.position());
        if (direction.lengthSqr() > 1.0E-4) {
            direction = direction.normalize();
        }
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), (SoundEvent)NeoGuanNiaoSoundEvents.FEATHER_FAN_HUNT_TURN.get(), SoundSource.PLAYERS, 0.72f, 1.0f + (float)this.huntingHop * 0.08f);
        for (int i = -1; i <= 1; ++i) {
            double ySpeed = direction.y * 0.08 + (double)i * 0.018;
            serverLevel.sendParticles((ParticleOptions)((SimpleParticleType)NeoGuanNiaoParticleTypes.HUNTING_STREAK.get()), this.getX(), this.getY(), this.getZ(), 0, direction.x * 0.15, ySpeed, direction.z * 0.15, 1.0);
        }
        serverLevel.sendParticles((ParticleOptions)((SimpleParticleType)NeoGuanNiaoParticleTypes.HUNTING_MARK.get()), nextCenter.x, nextCenter.y, nextCenter.z, 1, 0.0, 0.0, 0.0, 0.0);
    }

    private void spawnHuntingReturnEffects(ServerLevel serverLevel) {
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), (SoundEvent)NeoGuanNiaoSoundEvents.FEATHER_FAN_HUNT_TURN.get(), SoundSource.PLAYERS, 0.78f, 0.82f);
        serverLevel.sendParticles((ParticleOptions)((SimpleParticleType)NeoGuanNiaoParticleTypes.HUNTING_MARK.get()), this.getX(), this.getY(), this.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
        serverLevel.sendParticles((ParticleOptions)ParticleTypes.WAX_ON, this.getX(), this.getY(), this.getZ(), 7, 0.24, 0.18, 0.24, 0.055);
    }

    private void spawnTurnEffects(ServerLevel serverLevel) {
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.9f, 0.72f);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PHANTOM_FLAP, SoundSource.PLAYERS, 0.65f, 0.8f);
        for (int i = 0; i < 12; ++i) {
            double angle = Math.PI * 2 * (double)i / 12.0;
            double radius = 0.55;
            double particleX = this.getX() + Math.cos(angle) * radius;
            double particleZ = this.getZ() + Math.sin(angle) * radius;
            if ((i & 1) == 0) {
                serverLevel.sendParticles((ParticleOptions)ParticleTypes.WHITE_ASH, particleX, this.getY(), particleZ, 1, 0.0, 0.0, 0.0, 0.0);
                continue;
            }
            serverLevel.sendParticles((ParticleOptions)ParticleTypes.SNOWFLAKE, particleX, this.getY(), particleZ, 1, 0.0, 0.0, 0.0, 0.0);
        }
        serverLevel.sendParticles((ParticleOptions)ParticleTypes.SNOWFLAKE, this.getX(), this.getY(), this.getZ(), 4, 0.22, 0.08, 0.22, 0.012);
        serverLevel.sendParticles((ParticleOptions)ParticleTypes.SWEEP_ATTACK, this.getX(), this.getY(), this.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
    }

    private void spawnPiercingHitEffects(ServerLevel serverLevel, LivingEntity target, boolean damaged) {
        double hitY = target.getY(0.55);
        this.level().playSound(null, target.getX(), hitY, target.getZ(), SoundEvents.TRIDENT_HIT, SoundSource.PLAYERS, damaged ? 0.65f : 0.35f, 1.55f);
        serverLevel.sendParticles((ParticleOptions)ParticleTypes.CRIT, target.getX(), hitY, target.getZ(), 4, 0.14, 0.14, 0.14, 0.1);
        serverLevel.sendParticles((ParticleOptions)ParticleTypes.WHITE_ASH, target.getX(), hitY, target.getZ(), 3, 0.13, 0.13, 0.13, 0.018);
    }

    private void spawnPinEffects(ServerLevel serverLevel) {
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.TRIDENT_HIT, SoundSource.PLAYERS, 0.9f, 0.72f);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.PLAYERS, 0.55f, 1.35f);
        serverLevel.sendParticles((ParticleOptions)ParticleTypes.POOF, this.getX(), this.getY(), this.getZ(), 3, 0.1, 0.1, 0.1, 0.025);
        serverLevel.sendParticles((ParticleOptions)ParticleTypes.WHITE_ASH, this.getX(), this.getY(), this.getZ(), 5, 0.18, 0.18, 0.18, 0.025);
    }

    private void spawnBurialStartEffects(ServerLevel serverLevel, Vec3 center) {
        this.level().playSound(null, center.x, center.y, center.z, (SoundEvent)NeoGuanNiaoSoundEvents.FEATHER_FAN_BURIAL_VORTEX.get(), SoundSource.PLAYERS, 0.72f, 0.88f);
        this.level().playSound(null, center.x, center.y, center.z, SoundEvents.TRIDENT_RIPTIDE_1, SoundSource.PLAYERS, 0.28f, 0.72f);
        for (int layer = 0; layer < 3; ++layer) {
            double radius = 1.15 + (double)layer * 0.72;
            double y = center.y - 0.32 + (double)layer * 0.36;
            int segments = 8 + layer * 3;
            for (int i = 0; i < segments; ++i) {
                double angle = Math.PI * 2 * (double)i / (double)segments + (double)layer * 0.38;
                double tangentialSpeed = 0.045 + (double)layer * 0.008;
                double xSpeed = -Math.sin(angle) * tangentialSpeed - Math.cos(angle) * 0.018;
                double zSpeed = Math.cos(angle) * tangentialSpeed - Math.sin(angle) * 0.018;
                serverLevel.sendParticles((ParticleOptions)((SimpleParticleType)NeoGuanNiaoParticleTypes.BURIAL_CYCLONE.get()), center.x + Math.cos(angle) * radius, y + Math.sin(angle * 2.0) * 0.1, center.z + Math.sin(angle) * radius, 0, xSpeed, 0.018 + (double)layer * 0.006, zSpeed, 1.0);
            }
        }
        serverLevel.sendParticles((ParticleOptions)ParticleTypes.CLOUD, center.x, center.y - 0.25, center.z, 12, 1.15, 0.12, 1.15, 0.035);
        serverLevel.sendParticles((ParticleOptions)ParticleTypes.SNOWFLAKE, center.x, center.y, center.z, 12, 1.25, 0.55, 1.25, 0.025);
    }

    private void spawnBurialVortexEffects(ServerLevel serverLevel, Vec3 center) {
        double radius;
        if ((this.stuckTicks & 1) != 0) {
            return;
        }
        double progress = Mth.clamp((double)((double)this.stuckTicks / 40.0), (double)0.0, (double)1.0);
        double baseAngle = (double)this.stuckTicks * (0.48 + progress * 0.2);
        for (int layer = 0; layer < 5; ++layer) {
            double layerProgress = (double)layer / 4.0;
            radius = Mth.lerp((double)layerProgress, (double)0.72, (double)2.55) * Mth.lerp((double)progress, (double)1.0, (double)0.86);
            double y = center.y - 0.48 + layerProgress * 2.1;
            for (int arm = 0; arm < 3; ++arm) {
                double angle = baseAngle + Math.PI * 2 * (double)arm / 3.0 + (double)layer * 0.54;
                double x = center.x + Math.cos(angle) * radius;
                double z = center.z + Math.sin(angle) * radius;
                double tangentialSpeed = 0.075 + layerProgress * 0.045;
                double inwardSpeed = 0.018 + progress * 0.012;
                double xSpeed = -Math.sin(angle) * tangentialSpeed - Math.cos(angle) * inwardSpeed;
                double zSpeed = Math.cos(angle) * tangentialSpeed - Math.sin(angle) * inwardSpeed;
                serverLevel.sendParticles((ParticleOptions)((SimpleParticleType)NeoGuanNiaoParticleTypes.BURIAL_CYCLONE.get()), x, y, z, 0, xSpeed, 0.028 + layerProgress * 0.018, zSpeed, 1.0);
            }
        }
        for (int stream = 0; stream < 6; ++stream) {
            double inwardProgress = ((double)this.stuckTicks * 0.095 + (double)stream / 6.0) % 1.0;
            radius = Mth.lerp((double)inwardProgress, (double)4.8, (double)0.55);
            double angle = (double)(-this.stuckTicks) * 0.22 + (double)((float)stream * ((float)Math.PI * 2)) / 6.0 + inwardProgress * 2.2;
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;
            double y = center.y - 0.34 + inwardProgress * 0.7;
            double xSpeed = -Math.cos(angle) * 0.095 - Math.sin(angle) * 0.045;
            double zSpeed = -Math.sin(angle) * 0.095 + Math.cos(angle) * 0.045;
            serverLevel.sendParticles((ParticleOptions)((SimpleParticleType)NeoGuanNiaoParticleTypes.BURIAL_CYCLONE.get()), x, y, z, 0, xSpeed, 0.022, zSpeed, 1.0);
        }
        serverLevel.sendParticles((ParticleOptions)ParticleTypes.CLOUD, center.x, center.y - 0.3, center.z, 4, 1.15, 0.08, 1.15, 0.025);
        serverLevel.sendParticles((ParticleOptions)ParticleTypes.WHITE_ASH, center.x, center.y + 0.45, center.z, 3, 1.35, 0.7, 1.35, 0.018);
        if (this.stuckTicks % 10 == 0 && this.stuckTicks < 40) {
            this.level().playSound(null, center.x, center.y, center.z, (SoundEvent)NeoGuanNiaoSoundEvents.FEATHER_FAN_BURIAL_VORTEX.get(), SoundSource.PLAYERS, 0.64f, (float)(0.92 + progress * 0.14));
        }
    }

    private void spawnBurialSlashEffects(ServerLevel serverLevel, Vec3 center) {
        this.level().playSound(null, center.x, center.y, center.z, (SoundEvent)NeoGuanNiaoSoundEvents.FEATHER_FAN_BURIAL_SLASH.get(), SoundSource.PLAYERS, 1.2f, 1.0f);
        this.level().playSound(null, center.x, center.y, center.z, SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.38f, 1.28f);
        for (int ring = 0; ring < 3; ++ring) {
            double radius = 1.2 + (double)ring * 1.25;
            int segments = 16 + ring * 8;
            for (int i = 0; i < segments; ++i) {
                double angle = Math.PI * 2 * (double)i / (double)segments + (double)ring * 0.22;
                double x = center.x + Math.cos(angle) * radius;
                double z = center.z + Math.sin(angle) * radius;
                serverLevel.sendParticles((ParticleOptions)((SimpleParticleType)NeoGuanNiaoParticleTypes.BURIAL_WIND.get()), x, center.y - 0.08 + (double)ring * 0.11, z, 1, Math.cos(angle) * 0.11, 0.035, Math.sin(angle) * 0.11, 0.0);
                if (ring != 2 || i % 4 != 0) continue;
                serverLevel.sendParticles((ParticleOptions)ParticleTypes.SWEEP_ATTACK, x, center.y + 0.14, z, 1, 0.0, 0.0, 0.0, 0.0);
            }
        }
        serverLevel.sendParticles((ParticleOptions)ParticleTypes.POOF, center.x, center.y - 0.12, center.z, 18, 1.15, 0.24, 1.15, 0.12);
        serverLevel.sendParticles((ParticleOptions)ParticleTypes.CLOUD, center.x, center.y, center.z, 24, 1.45, 0.38, 1.45, 0.16);
        serverLevel.sendParticles((ParticleOptions)ParticleTypes.SNOWFLAKE, center.x, center.y + 0.2, center.z, 32, 2.65, 0.48, 2.65, 0.15);
        serverLevel.sendParticles((ParticleOptions)ParticleTypes.WHITE_ASH, center.x, center.y + 0.2, center.z, 38, 3.45, 0.55, 3.45, 0.2);
    }

    private void spawnRivenStartEffects(ServerLevel serverLevel, Vec3 center) {
        this.level().playSound(null, center.x, center.y, center.z, (SoundEvent)NeoGuanNiaoSoundEvents.FEATHER_FAN_RIVEN_PIN.get(), SoundSource.PLAYERS, 0.9f, 1.0f);
        serverLevel.sendParticles((ParticleOptions)((SimpleParticleType)NeoGuanNiaoParticleTypes.RIVEN_SPLIT.get()), center.x, center.y, center.z, 1, 0.0, 0.0, 0.0, 0.0);
        serverLevel.sendParticles((ParticleOptions)ParticleTypes.ELECTRIC_SPARK, center.x, center.y, center.z, 8, 0.24, 0.24, 0.24, 0.055);
        serverLevel.sendParticles((ParticleOptions)ParticleTypes.END_ROD, center.x, center.y, center.z, 4, 0.14, 0.14, 0.14, 0.025);
    }

    private void spawnRivenSequenceEffects(ServerLevel serverLevel, Vec3 center) {
        if (this.rivenTicks == 5) {
            this.level().playSound(null, center.x, center.y, center.z, (SoundEvent)NeoGuanNiaoSoundEvents.FEATHER_FAN_RIVEN_SPLIT.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
            serverLevel.sendParticles((ParticleOptions)ParticleTypes.ELECTRIC_SPARK, center.x, center.y, center.z, 18, 0.34, 0.34, 0.34, 0.12);
            for (int i = 0; i < 8; ++i) {
                double angle = (double)((float)Math.PI * 2 * (float)i) / 8.0;
                double ySpeed = Math.sin(angle * 2.0) * 0.055;
                serverLevel.sendParticles((ParticleOptions)((SimpleParticleType)NeoGuanNiaoParticleTypes.RIVEN_SPLIT.get()), center.x, center.y, center.z, 0, Math.cos(angle) * 0.21, ySpeed, Math.sin(angle) * 0.21, 1.0);
                serverLevel.sendParticles((ParticleOptions)((SimpleParticleType)NeoGuanNiaoParticleTypes.RIVEN_SPLIT.get()), center.x, center.y, center.z, 0, Math.cos(angle + 0.12) * 0.14, -ySpeed * 0.65, Math.sin(angle + 0.12) * 0.14, 1.0);
            }
            return;
        }
        if (this.rivenTicks > 5 && this.rivenTicks < 14) {
            if ((this.rivenTicks & 1) == 0) {
                float radius = FeatherFanProjectileEntity.getRivenArrayRadius(this.rivenTicks);
                double rotation = FeatherFanProjectileEntity.getRivenRingRotation(this.rivenTicks);
                for (int i = 0; i < 8; ++i) {
                    double baseAngle = (double)((float)Math.PI * 2 * (float)i) / 8.0;
                    double angle = baseAngle + rotation;
                    double y = Math.sin(baseAngle * 2.0) * 0.85 * (double)radius / 3.8;
                    double outward = 0.075 + (double)radius * 0.012;
                    double tangent = 0.028;
                    serverLevel.sendParticles((ParticleOptions)((SimpleParticleType)NeoGuanNiaoParticleTypes.RIVEN_SPLIT.get()), center.x + Math.cos(angle) * (double)radius, center.y + y, center.z + Math.sin(angle) * (double)radius, 0, Math.cos(angle) * outward - Math.sin(angle) * tangent, Math.sin(baseAngle * 2.0) * 0.018, Math.sin(angle) * outward + Math.cos(angle) * tangent, 1.0);
                }
            }
            return;
        }
        if (this.rivenTicks == 14) {
            this.level().playSound(null, center.x, center.y, center.z, (SoundEvent)NeoGuanNiaoSoundEvents.FEATHER_FAN_RIVEN_LOCK.get(), SoundSource.PLAYERS, 0.95f, 1.0f);
            double rotation = FeatherFanProjectileEntity.getRivenRingRotation(this.rivenTicks);
            for (int i = 0; i < 8; ++i) {
                double baseAngle = (double)((float)Math.PI * 2 * (float)i) / 8.0;
                double angle = baseAngle + rotation;
                double y = Math.sin(baseAngle * 2.0) * 0.85;
                serverLevel.sendParticles((ParticleOptions)ParticleTypes.END_ROD, center.x + Math.cos(angle) * 3.8, center.y + y, center.z + Math.sin(angle) * 3.8, 3, 0.08, 0.08, 0.08, 0.015);
                serverLevel.sendParticles((ParticleOptions)((SimpleParticleType)NeoGuanNiaoParticleTypes.RIVEN_SPLIT.get()), center.x + Math.cos(angle) * 3.8, center.y + y, center.z + Math.sin(angle) * 3.8, 1, 0.0, 0.0, 0.0, 0.0);
            }
            return;
        }
        if (this.rivenTicks > 14 && this.rivenTicks < 19) {
            if ((this.rivenTicks & 1) == 0) {
                double rotation = FeatherFanProjectileEntity.getRivenRingRotation(this.rivenTicks);
                for (int i = 0; i < 8; ++i) {
                    double baseAngle = (double)((float)Math.PI * 2 * (float)i) / 8.0;
                    double angle = baseAngle + rotation;
                    double y = Math.sin(baseAngle * 2.0) * 0.85;
                    serverLevel.sendParticles((ParticleOptions)((SimpleParticleType)NeoGuanNiaoParticleTypes.RIVEN_SPLIT.get()), center.x + Math.cos(angle) * 3.8, center.y + y, center.z + Math.sin(angle) * 3.8, 1, 0.0, 0.0, 0.0, 0.0);
                }
            }
            return;
        }
        if (this.rivenTicks >= 19 && this.rivenTicks < 26) {
            float radius = FeatherFanProjectileEntity.getRivenArrayRadius(this.rivenTicks);
            double heightScale = (double)radius / 3.8;
            double rotation = FeatherFanProjectileEntity.getRivenRingRotation(this.rivenTicks);
            for (int i = 0; i < 8; ++i) {
                double baseAngle = (double)((float)Math.PI * 2 * (float)i) / 8.0;
                double angle = baseAngle + rotation;
                double y = Math.sin(baseAngle * 2.0) * 0.85 * heightScale;
                double x = center.x + Math.cos(angle) * (double)radius;
                double z = center.z + Math.sin(angle) * (double)radius;
                serverLevel.sendParticles((ParticleOptions)((SimpleParticleType)NeoGuanNiaoParticleTypes.RIVEN_STREAK.get()), x, center.y + y, z, 0, -Math.cos(angle) * 0.21, -y * 0.065, -Math.sin(angle) * 0.21, 1.0);
            }
            return;
        }
        if (this.rivenTicks > 26 && this.rivenTicks < 36) {
            if ((this.rivenTicks & 1) == 0) {
                double stormProgress = (double)(this.rivenTicks - 26) / 10.0;
                for (int i = 0; i < 8; ++i) {
                    double angle = (double)this.rivenTicks * 0.58 + (double)((float)Math.PI * 2 * (float)i) / 8.0;
                    double radius = Mth.lerp((double)stormProgress, (double)2.15, (double)0.52);
                    double y = Math.sin(angle * 1.7) * 0.72;
                    double tangential = 0.1 + stormProgress * 0.05;
                    serverLevel.sendParticles((ParticleOptions)((SimpleParticleType)NeoGuanNiaoParticleTypes.RIVEN_SPLIT.get()), center.x + Math.cos(angle) * radius, center.y + y, center.z + Math.sin(angle) * radius, 0, -Math.sin(angle) * tangential - Math.cos(angle) * 0.04, 0.025 - y * 0.018, Math.cos(angle) * tangential - Math.sin(angle) * 0.04, 1.0);
                }
            }
            if (this.rivenTicks == 30) {
                for (int i = 0; i < 8; ++i) {
                    double angle = (double)((float)Math.PI * 2 * (float)i) / 8.0 + 0.22;
                    serverLevel.sendParticles((ParticleOptions)((SimpleParticleType)NeoGuanNiaoParticleTypes.RIVEN_SPLIT.get()), center.x, center.y + 0.18, center.z, 0, Math.cos(angle) * 0.13, Math.sin(angle * 2.0) * 0.035, Math.sin(angle) * 0.13, 1.0);
                }
            }
        }
    }

    private void spawnRivenBurstEffects(ServerLevel serverLevel, Vec3 center) {
        this.level().playSound(null, center.x, center.y, center.z, (SoundEvent)NeoGuanNiaoSoundEvents.FEATHER_FAN_RIVEN_BURST.get(), SoundSource.PLAYERS, 1.25f, 1.0f);
        this.level().playSound(null, center.x, center.y, center.z, SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.38f, 1.35f);
        for (int ray = 0; ray < 8; ++ray) {
            double angle = (double)((float)Math.PI * 2 * (float)ray) / 8.0 + 0.18;
            for (int step = 1; step <= 7; ++step) {
                double radius = (double)step * 0.48;
                double y = Math.sin((float)ray * 1.5707964f) * 0.18 * (1.0 - (double)step / 8.0);
                serverLevel.sendParticles((ParticleOptions)((SimpleParticleType)NeoGuanNiaoParticleTypes.RIVEN_STREAK.get()), center.x + Math.cos(angle) * radius, center.y + y, center.z + Math.sin(angle) * radius, 1, 0.0, 0.0, 0.0, 0.0);
            }
            serverLevel.sendParticles((ParticleOptions)((SimpleParticleType)NeoGuanNiaoParticleTypes.RIVEN_SPLIT.get()), center.x + Math.cos(angle) * 2.45, center.y, center.z + Math.sin(angle) * 2.45, 0, Math.cos(angle) * 0.12, Math.sin((float)ray * 1.5707964f) * 0.025, Math.sin(angle) * 0.12, 1.0);
        }
        serverLevel.sendParticles((ParticleOptions)ParticleTypes.ELECTRIC_SPARK, center.x, center.y, center.z, 36, 1.05, 0.82, 1.05, 0.23);
        serverLevel.sendParticles((ParticleOptions)ParticleTypes.CRIT, center.x, center.y, center.z, 28, 1.45, 0.92, 1.45, 0.2);
        serverLevel.sendParticles((ParticleOptions)ParticleTypes.END_ROD, center.x, center.y, center.z, 16, 0.82, 0.62, 0.82, 0.13);
        serverLevel.sendParticles((ParticleOptions)ParticleTypes.ENCHANT, center.x, center.y, center.z, 24, 1.75, 1.05, 1.75, 0.28);
    }

    private void spawnRivenReformEffects(ServerLevel serverLevel, Vec3 center) {
        this.level().playSound(null, center.x, center.y, center.z, SoundEvents.TRIDENT_RETURN, SoundSource.PLAYERS, 0.72f, 1.45f);
        serverLevel.sendParticles((ParticleOptions)ParticleTypes.END_ROD, center.x, center.y, center.z, 8, 0.3, 0.3, 0.3, 0.06);
        for (int i = 0; i < 8; ++i) {
            double angle = (double)((float)Math.PI * 2 * (float)i) / 8.0;
            serverLevel.sendParticles((ParticleOptions)((SimpleParticleType)NeoGuanNiaoParticleTypes.RIVEN_SPLIT.get()), center.x + Math.cos(angle) * 1.35, center.y + Math.sin(angle * 2.0) * 0.28, center.z + Math.sin(angle) * 1.35, 0, -Math.cos(angle) * 0.16, -Math.sin(angle * 2.0) * 0.025, -Math.sin(angle) * 0.16, 1.0);
        }
    }

    public static float getRivenArrayRadius(float age) {
        if (age < 5.0f) {
            return 0.0f;
        }
        if (age < 14.0f) {
            float progress = (age - 5.0f) / 9.0f;
            float eased = 1.0f - (1.0f - progress) * (1.0f - progress);
            return Mth.lerp((float)eased, (float)0.3f, (float)3.8f);
        }
        if (age < 19.0f) {
            return 3.8f;
        }
        if (age < 26.0f) {
            float progress = (age - 19.0f) / 7.0f;
            float eased = progress * progress * progress;
            return Mth.lerp((float)eased, (float)3.8f, (float)0.15f);
        }
        return 0.0f;
    }

    public static float getRivenRingRotation(float age) {
        float progress = Mth.clamp((float)((age - 5.0f) / 9.0f), (float)0.0f, (float)1.0f);
        float eased = 1.0f - (1.0f - progress) * (1.0f - progress) * (1.0f - progress);
        return eased * 0.95f;
    }

    private void spawnStuckPulse(ServerLevel serverLevel, LivingEntity target) {
        double y = target.getY(0.55);
        this.level().playSound(null, target.getX(), y, target.getZ(), SoundEvents.TRIDENT_HIT, SoundSource.PLAYERS, 0.22f, 1.75f);
        serverLevel.sendParticles((ParticleOptions)ParticleTypes.WHITE_ASH, this.getX(), this.getY(), this.getZ(), 2, 0.08, 0.08, 0.08, 0.014);
        serverLevel.sendParticles((ParticleOptions)ParticleTypes.POOF, this.getX(), this.getY(), this.getZ(), 1, 0.04, 0.04, 0.04, 0.008);
    }

    private void spawnPulloutEffects(ServerLevel serverLevel) {
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.TRIDENT_RETURN, SoundSource.PLAYERS, 0.75f, 1.35f);
        serverLevel.sendParticles((ParticleOptions)ParticleTypes.POOF, this.getX(), this.getY(), this.getZ(), 2, 0.09, 0.09, 0.09, 0.018);
        serverLevel.sendParticles((ParticleOptions)ParticleTypes.WHITE_ASH, this.getX(), this.getY(), this.getZ(), 4, 0.14, 0.14, 0.14, 0.018);
    }

    private void spawnPiercingReturnEffects(ServerLevel serverLevel) {
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PHANTOM_FLAP, SoundSource.PLAYERS, 0.55f, 1.45f);
        serverLevel.sendParticles((ParticleOptions)ParticleTypes.SNOWFLAKE, this.getX(), this.getY(), this.getZ(), 4, 0.12, 0.08, 0.12, 0.012);
    }

    private void hitEntitiesAlongMotion() {
        BlockHitResult blockHit;
        FanState motionState = this.getFanState();
        Vec3 movement = this.getDeltaMovement();
        if (movement.lengthSqr() < 1.0E-6) {
            return;
        }
        Vec3 start = this.position();
        Vec3 end = start.add(movement);
        if (!this.isReturning() && (blockHit = this.level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)this))).getType() != HitResult.Type.MISS) {
            end = blockHit.getLocation();
        }
        AABB searchBox = this.getBoundingBox().expandTowards(movement).inflate(0.35);
        List<Entity> targets = this.level().getEntities((Entity)this, searchBox, this::canHitEntity);
        targets.sort(Comparator.comparingDouble(target -> target.position().distanceToSqr(start)));
        for (Entity target2 : targets) {
            AABB targetBox = target2.getBoundingBox().inflate(0.3);
            Vec3 hitLocation = targetBox.contains(start) ? start : (Vec3)targetBox.clip(start, end).orElse(null);
            if (hitLocation == null) continue;
            this.hitLivingEntity((LivingEntity)target2, hitLocation);
            if (motionState != FanState.PIERCING || this.getFanState() == FanState.PIERCING) continue;
            return;
        }
    }

    private ServerPlayer findServerOwner() {
        Entity entity;
        Level level = this.level();
        if (!(level instanceof ServerLevel)) {
            return null;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        if (this.ownerUuid == null && (entity = this.getOwner()) instanceof ServerPlayer) {
            ServerPlayer owner = (ServerPlayer)entity;
            this.ownerUuid = owner.getUUID();
        }
        return this.ownerUuid == null ? null : serverLevel.getServer().getPlayerList().getPlayer(this.ownerUuid);
    }

    private void returnFanToOwner(ServerPlayer owner) {
        if (this.isRemoved()) {
            return;
        }
        this.releaseBurialTargets();
        this.removeStuckSlowdown();
        ItemStack fan = this.getItem().copy();
        fan.setCount(1);
        if (owner.getItemInHand(this.returnHand).isEmpty()) {
            owner.setItemInHand(this.returnHand, fan);
        } else if (!owner.getInventory().add(fan)) {
            owner.drop(fan, false);
        }
        owner.getCooldowns().addCooldown((Item)NeoGuanNiaoItems.WIND_FEATHER_FAN.get(), 12);
        this.level().playSound(null, owner.getX(), owner.getY(), owner.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.7f, 1.35f);
        this.level().playSound(null, owner.getX(), owner.getY(), owner.getZ(), SoundEvents.PHANTOM_FLAP, SoundSource.PLAYERS, 0.22f, 1.7f);
        this.discard();
    }

    private void dropFanAndDiscard() {
        if (this.isRemoved() || this.level().isClientSide) {
            return;
        }
        this.releaseBurialTargets();
        this.removeStuckSlowdown();
        ItemStack fan = this.getItem().copy();
        fan.setCount(1);
        this.spawnAtLocation(fan, 0.1f);
        this.discard();
    }

    private static Vec3 returnTarget(ServerPlayer owner) {
        return owner.getEyePosition().subtract(0.0, 0.25, 0.0);
    }

    public boolean isReturning() {
        return this.getFanState() == FanState.RETURNING;
    }

    public boolean isPiercing() {
        return this.getFanState() == FanState.PIERCING;
    }

    public boolean isHunting() {
        return this.getFanState() == FanState.HUNTING;
    }

    public boolean isStuck() {
        FanState state = this.getFanState();
        return state == FanState.STUCK_ENTITY || state == FanState.STUCK_BLOCK || state == FanState.BURIAL_VORTEX || state == FanState.RIVEN_SEQUENCE;
    }

    public boolean isRivenActive() {
        return this.getFanState() == FanState.RIVEN_SEQUENCE;
    }

    public int getRivenTicks() {
        return (Integer)this.entityData.get(DATA_RIVEN_TICKS);
    }

    public FanState getFanState() {
        return FanState.fromId((Integer)this.entityData.get(DATA_STATE));
    }

    private void setFanState(FanState state) {
        this.entityData.set(DATA_STATE, state.ordinal());
    }

    private boolean isFlyingOutbound() {
        FanState state = this.getFanState();
        return state == FanState.OUTBOUND_SPIN || state == FanState.PIERCING;
    }

    private boolean isNonCollidingState() {
        FanState state = this.getFanState();
        return state == FanState.RETURNING || state == FanState.STUCK_ENTITY || state == FanState.STUCK_BLOCK || state == FanState.BURIAL_VORTEX || state == FanState.RIVEN_SEQUENCE;
    }

    public float getCharge() {
        return ((Float)this.entityData.get(DATA_CHARGE)).floatValue();
    }

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Returning", this.isReturning());
        tag.putInt("FanState", this.getFanState().ordinal());
        tag.putFloat("Charge", this.getCharge());
        tag.putDouble("ThrowOriginX", this.throwOrigin.x);
        tag.putDouble("ThrowOriginY", this.throwOrigin.y);
        tag.putDouble("ThrowOriginZ", this.throwOrigin.z);
        tag.putFloat("MaxDistance", this.maxDistance);
        tag.putFloat("AttackDamage", this.attackDamage);
        tag.putFloat("ReturnSpeed", this.returnSpeed);
        tag.putBoolean("ReturnOffhand", this.returnHand == InteractionHand.OFF_HAND);
        tag.putInt("OwnerMissingTicks", this.ownerMissingTicks);
        tag.putInt("LifeTicks", this.lifeTicks);
        tag.putInt("ReturningTicks", this.returningTicks);
        tag.putInt("StuckTicks", this.stuckTicks);
        tag.putInt("PulloutTicks", this.pulloutTicks);
        tag.putInt("RivenTicks", this.rivenTicks);
        tag.putBoolean("RivenDamageDone", this.rivenDamageDone);
        tag.putBoolean("RivenReformDamageDone", this.rivenReformDamageDone);
        tag.putInt("HuntingHop", this.huntingHop);
        tag.putInt("StuckBlockX", this.stuckBlockPos.getX());
        tag.putInt("StuckBlockY", this.stuckBlockPos.getY());
        tag.putInt("StuckBlockZ", this.stuckBlockPos.getZ());
        tag.putInt("StuckFace", this.stuckFace.get3DDataValue());
        FeatherFanProjectileEntity.putVec3(tag, "StuckPosition", this.stuckPosition);
        FeatherFanProjectileEntity.putVec3(tag, "StuckOffset", this.stuckOffset);
        FeatherFanProjectileEntity.putVec3(tag, "StuckForward", this.stuckForward);
        FeatherFanProjectileEntity.putVec3(tag, "StuckLocalForward", this.stuckLocalForward);
        FeatherFanProjectileEntity.putVec3(tag, "RivenAnchor", this.rivenAnchor);
        tag.put("OutboundHits", (Tag)FeatherFanProjectileEntity.saveHitSet(this.outboundHits));
        tag.put("ReturnHits", (Tag)FeatherFanProjectileEntity.saveHitSet(this.returnHits));
        tag.put("HuntedTargets", (Tag)FeatherFanProjectileEntity.saveHitSet(this.huntedTargets));
        tag.put("HuntingLockedTargets", (Tag)FeatherFanProjectileEntity.saveHitSet(this.huntingLockedTargets));
        if (this.ownerUuid != null) {
            tag.putUUID("FanOwner", this.ownerUuid);
        }
        if (this.stuckEntityUuid != null) {
            tag.putUUID("StuckEntity", this.stuckEntityUuid);
        }
        if (this.huntingTargetUuid != null) {
            tag.putUUID("HuntingTarget", this.huntingTargetUuid);
        }
    }

    public void readAdditionalSaveData(CompoundTag tag) {
        Vec3 savedLocalForward;
        super.readAdditionalSaveData(tag);
        if (tag.contains("FanState", 3)) {
            int savedState = tag.getInt("FanState");
            boolean temporaryReturningState = savedState == FanState.STUCK_BLOCK.ordinal() && !tag.contains("StuckBlockX", 3);
            this.setFanState(temporaryReturningState ? FanState.RETURNING : FanState.fromId(savedState));
        } else {
            this.setFanState(tag.getBoolean("Returning") ? FanState.RETURNING : FanState.OUTBOUND_SPIN);
        }
        this.entityData.set(DATA_CHARGE, Float.valueOf(tag.getFloat("Charge")));
        this.throwOrigin = new Vec3(tag.getDouble("ThrowOriginX"), tag.getDouble("ThrowOriginY"), tag.getDouble("ThrowOriginZ"));
        this.maxDistance = tag.getFloat("MaxDistance");
        this.attackDamage = tag.getFloat("AttackDamage");
        this.returnSpeed = tag.getFloat("ReturnSpeed");
        this.returnHand = tag.getBoolean("ReturnOffhand") ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        this.ownerMissingTicks = tag.getInt("OwnerMissingTicks");
        this.lifeTicks = tag.getInt("LifeTicks");
        this.returningTicks = tag.getInt("ReturningTicks");
        this.stuckTicks = tag.getInt("StuckTicks");
        this.pulloutTicks = tag.getInt("PulloutTicks");
        this.rivenTicks = tag.getInt("RivenTicks");
        this.rivenDamageDone = tag.getBoolean("RivenDamageDone");
        this.rivenReformDamageDone = tag.getBoolean("RivenReformDamageDone");
        this.huntingHop = tag.getInt("HuntingHop");
        this.entityData.set(DATA_RIVEN_TICKS, this.rivenTicks);
        this.stuckBlockPos = new BlockPos(tag.getInt("StuckBlockX"), tag.getInt("StuckBlockY"), tag.getInt("StuckBlockZ"));
        this.stuckFace = Direction.from3DDataValue((int)tag.getInt("StuckFace"));
        this.stuckPosition = FeatherFanProjectileEntity.getVec3(tag, "StuckPosition");
        this.stuckOffset = FeatherFanProjectileEntity.getVec3(tag, "StuckOffset");
        this.rivenAnchor = FeatherFanProjectileEntity.getVec3(tag, "RivenAnchor");
        Vec3 savedForward = FeatherFanProjectileEntity.getVec3(tag, "StuckForward");
        if (savedForward.lengthSqr() > 1.0E-6) {
            this.stuckForward = savedForward.normalize();
        }
        this.stuckLocalForward = (savedLocalForward = FeatherFanProjectileEntity.getVec3(tag, "StuckLocalForward")).lengthSqr() > 1.0E-6 ? savedLocalForward.normalize() : this.stuckForward;
        FeatherFanProjectileEntity.loadHitSet(tag.getList("OutboundHits", 8), this.outboundHits);
        FeatherFanProjectileEntity.loadHitSet(tag.getList("ReturnHits", 8), this.returnHits);
        FeatherFanProjectileEntity.loadHitSet(tag.getList("HuntedTargets", 8), this.huntedTargets);
        FeatherFanProjectileEntity.loadHitSet(tag.getList("HuntingLockedTargets", 8), this.huntingLockedTargets);
        if (tag.hasUUID("FanOwner")) {
            this.ownerUuid = tag.getUUID("FanOwner");
        }
        if (tag.hasUUID("StuckEntity")) {
            this.stuckEntityUuid = tag.getUUID("StuckEntity");
        }
        if (tag.hasUUID("HuntingTarget")) {
            this.huntingTargetUuid = tag.getUUID("HuntingTarget");
        }
        this.noPhysics = this.isNonCollidingState();
    }

    @NotNull


    private static ListTag saveHitSet(Set<UUID> hits) {
        ListTag tag = new ListTag();
        for (UUID uuid : hits) {
            tag.add(StringTag.valueOf(uuid.toString()));
        }
        return tag;
    }

    private static void loadHitSet(ListTag tag, Set<UUID> hits) {
        hits.clear();
        for (int i = 0; i < tag.size(); ++i) {
            hits.add(UUID.fromString(tag.getString(i)));
        }
    }

    private static void putVec3(CompoundTag tag, String key, Vec3 value) {
        tag.putDouble(key + "X", value.x);
        tag.putDouble(key + "Y", value.y);
        tag.putDouble(key + "Z", value.z);
    }

    private static Vec3 getVec3(CompoundTag tag, String key) {
        return new Vec3(tag.getDouble(key + "X"), tag.getDouble(key + "Y"), tag.getDouble(key + "Z"));
    }

    public static enum FanState {
        OUTBOUND_SPIN,
        PIERCING,
        STUCK_ENTITY,
        STUCK_BLOCK,
        RETURNING,
        BURIAL_VORTEX,
        RIVEN_SEQUENCE,
        HUNTING;


        private static FanState fromId(int id) {
            FanState[] values = FanState.values();
            return values[Mth.clamp((int)id, (int)0, (int)(values.length - 1))];
        }
    }

    private static enum PiercingArt {
        NORMAL,
        BURIAL,
        RIVEN;

    }

    private static boolean isBird(Entity entity) {
        return entity instanceof AbstractBirdEntity<?>;
    }

}
