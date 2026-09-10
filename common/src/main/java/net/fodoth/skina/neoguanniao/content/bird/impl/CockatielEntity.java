package net.fodoth.skina.neoguanniao.content.bird.impl;

import net.fodoth.skina.neoguanniao.content.bird.core.SimpleNeoBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoBirdData;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public final class CockatielEntity extends SimpleNeoBirdEntity<CockatielEntity> {
    public CockatielEntity(EntityType<CockatielEntity> type, Level level) {
        super(type, level, NeoGuanNiaoBirdData.COCKATIEL.get());
    }

    @Override protected CockatielEntity getSelf() { return this; }

    public static AttributeSupplier.Builder createAttributes() {
        return SimpleNeoBirdEntity.createAttributes(8.0, 0.24, 0.34, 20.0);
    }

    public static boolean canSpawn(EntityType<? extends AbstractBirdEntity<?>> type,
                                   ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return SimpleNeoBirdEntity.canSpawn(type, level, spawnType, pos, random, NeoGuanNiaoBirdData.COCKATIEL.get());
    }
}
