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

public final class MacawEntity extends SimpleNeoBirdEntity<MacawEntity> {
    public MacawEntity(EntityType<MacawEntity> type, Level level) {
        super(type, level, NeoGuanNiaoBirdData.MACAW.get());
    }

    @Override protected MacawEntity getSelf() { return this; }

    public static AttributeSupplier.Builder createAttributes() {
        return SimpleNeoBirdEntity.createAttributes(14.0, 0.22, 0.38, 28.0);
    }

    public static boolean canSpawn(EntityType<? extends AbstractBirdEntity<?>> type,
                                   ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return SimpleNeoBirdEntity.canSpawn(type, level, spawnType, pos, random, NeoGuanNiaoBirdData.MACAW.get());
    }
}
