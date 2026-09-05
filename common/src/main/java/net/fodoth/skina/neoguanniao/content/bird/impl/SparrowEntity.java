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

public class SparrowEntity extends SimpleNeoBirdEntity<SparrowEntity> {

    public SparrowEntity(EntityType<SparrowEntity> entityType, Level level) {
        super(entityType, level, NeoGuanNiaoBirdData.SPARROW.get());
    }

    @Override
    protected SparrowEntity getSelf() {
        return this;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return SimpleNeoBirdEntity.createAttributes(6.0, 0.25, 0.22, 18.0);
    }

    public static boolean canSpawn(
            EntityType<? extends AbstractBirdEntity<?>> entityType,
            ServerLevelAccessor level,
            MobSpawnType spawnType,
            BlockPos pos,
            RandomSource random
    ) {
        return SimpleNeoBirdEntity.canSpawn(entityType, level, spawnType, pos, random,
                NeoGuanNiaoBirdData.SPARROW.get());
    }
}
