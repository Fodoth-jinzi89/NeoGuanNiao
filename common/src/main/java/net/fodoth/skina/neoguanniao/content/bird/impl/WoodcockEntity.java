package net.fodoth.skina.neoguanniao.content.bird.impl;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.SimpleNeoBirdEntity;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoBirdData;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

public class WoodcockEntity extends SimpleNeoBirdEntity<WoodcockEntity> {
    private int walkPhaseTicks;
    private boolean walkPause = true;
    private int walkVariant;
    public WoodcockEntity(EntityType<WoodcockEntity> entityType, Level level) {
        super(entityType, level, NeoGuanNiaoBirdData.WOODCOCK.get());
    }

    @Override
    protected WoodcockEntity getSelf() { return this; }

    @Override
    public void travel(Vec3 movementInput) {
        if (onGround() && walkPause && walkPhaseTicks > 0) {
            getNavigation().stop();
            setDeltaMovement(0.0D, getDeltaMovement().y(), 0.0D);
            super.travel(Vec3.ZERO);
            return;
        }
        super.travel(movementInput);
    }

    @Override
    public void tick() {
        super.tick();
        if (!onGround()) return;
        if (walkPhaseTicks > 0) {
            walkPhaseTicks--;
            if (walkPause) {
                getNavigation().stop();
                setDeltaMovement(0.0D, getDeltaMovement().y(), 0.0D);
            }
            return;
        }
        if (!getNavigation().isDone() || getDeltaMovement().horizontalDistanceSqr() > 0.0004D) {
            walkVariant = getRandom().nextInt(2) + 1;
            walkPause = !walkPause;
            walkPhaseTicks = walkPause ? 10 + getRandom().nextInt(11) : 10;
        }
    }

    @Override
    public <E extends AbstractBirdEntity<?>> PlayState movementController(AnimationState<E> state) {
        if (onGround() && walkPhaseTicks > 0 && (walkPause || !getNavigation().isDone()
                || getDeltaMovement().horizontalDistanceSqr() > 0.0004D)) {
            String key = walkPause ? "walk" + walkVariant + "_idle" : "walk" + walkVariant;
            RawAnimation animation = getBirdData().animation().animationMap().get(key);
            if (animation != null) return state.setAndContinue(animation);
        }
        return super.movementController(state);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return SimpleNeoBirdEntity.createAttributes(8.0, 0.22, 0.38, 18.0);
    }

    public static boolean canSpawn(EntityType<? extends AbstractBirdEntity<?>> entityType,
                                   ServerLevelAccessor level, MobSpawnType spawnType,
                                   BlockPos pos, RandomSource random) {
        return SimpleNeoBirdEntity.canSpawn(entityType, level, spawnType, pos, random,
                NeoGuanNiaoBirdData.WOODCOCK.get());
    }
}
