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

public final class CrowEntity extends SimpleNeoBirdEntity<CrowEntity> {
    public CrowEntity(EntityType<CrowEntity> type, Level level) {
        super(type, level, NeoGuanNiaoBirdData.CROW.get());
    }

    @Override protected CrowEntity getSelf() { return this; }

    public static AttributeSupplier.Builder createAttributes() {
        return SimpleNeoBirdEntity.createAttributes(8.0, 0.25, 0.42, 18.0);
    }

    public static boolean canSpawn(EntityType<? extends AbstractBirdEntity<?>> type,
                                   ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return SimpleNeoBirdEntity.canSpawn(type, level, spawnType, pos, random, NeoGuanNiaoBirdData.CROW.get());
    }
}
