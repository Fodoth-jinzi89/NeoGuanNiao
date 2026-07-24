package net.fodoth.skina.neoguanniao.content.bird.impl.neo.sparrow;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.controller.BirdControllers;
import net.fodoth.skina.neoguanniao.content.bird.feature.brain.BirdBrain;
import net.fodoth.skina.neoguanniao.content.bird.feature.species.SparrowProfile;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoBirdData;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import java.util.ArrayList;
import java.util.List;

public class NeoSparrowEntity extends AbstractBirdEntity<NeoSparrowEntity> {

    public NeoSparrowEntity(EntityType<NeoSparrowEntity> entityType, Level level) {
        super(
                entityType,
                level,
                NeoGuanNiaoBirdData.SPARROW.get(),
                BirdControllers.<NeoSparrowEntity>builder().build()
        );
        initControllers();
    }

    @Override
    protected NeoSparrowEntity getSelf() {
        return this;
    }

    @Override
    protected void initFeatures() {
        birdBrain = new BirdBrain(
                this,
                SparrowProfile.INSTANCE
        );
    }

    public static AttributeSupplier.Builder createAttributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 6.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.FLYING_SPEED, 0.22)
                .add(Attributes.FOLLOW_RANGE, 18.0);
    }

    public static boolean canSpawn(
            EntityType<? extends AbstractBirdEntity<?>> entityType,
            ServerLevelAccessor level,
            MobSpawnType spawnType,
            BlockPos pos,
            RandomSource random
    ) {
        return AbstractBirdEntity.canSpawn(
                entityType,
                level,
                spawnType,
                pos,
                random,
                NeoGuanNiaoBirdData.SPARROW.get()
        );
    }

    @Override
    protected List<Goal> buildGoals() {
        return new ArrayList<>(super.buildGoals());
    }
}
