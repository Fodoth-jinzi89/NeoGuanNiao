package net.fodoth.skina.neoguanniao.content.bird.impl;

import net.fodoth.skina.neoguanniao.content.bird.core.SimpleNeoBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bath.BirdBathAttraction;
import net.fodoth.skina.neoguanniao.content.bath.BirdBathBlockEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.controller.BirdBreedController;
import net.fodoth.skina.neoguanniao.content.bird.core.controller.BirdControllers;
import net.fodoth.skina.neoguanniao.content.bird.core.controller.BirdEatingController;
import net.fodoth.skina.neoguanniao.content.bird.core.controller.BirdGoalController;
import net.fodoth.skina.neoguanniao.content.bird.core.controller.goal.BirdBathUseGoalController;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoBirdData;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoItemTags;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public final class SeagullEntity extends SimpleNeoBirdEntity<SeagullEntity> {
    public SeagullEntity(EntityType<SeagullEntity> type, Level level) {
        super(type, level, NeoGuanNiaoBirdData.SEAGULL.get(),
                BirdControllers.<SeagullEntity>builder()
                        .birdEatingController(new BirdEatingController<>() {
                            @Override
                            public boolean isEdibleFood(ItemStack stack) {
                                return stack.is(NeoGuanNiaoItemTags.BIRD_FOOD_FISH)
                                        || stack.is(NeoGuanNiaoItemTags.SEAGULL_EXTRA_FOOD);
                            }
                        })
                        .birdBreedController(new BirdBreedController<>() {
                            @Override
                            public boolean isBreedingFood(ItemStack stack) {
                                return !stack.isEmpty() && stack.is(NeoGuanNiaoItemTags.BIRD_BREED_FOOD_FISH);
                            }
                        })
                        .birdGoalController(BirdGoalController.<SeagullEntity>builder()
                                .birdBathUseGoalController(new BirdBathUseGoalController<>() {
                                    @Override
                                    public boolean canUseBathPredicates(BirdBathBlockEntity bath) {
                                        return BirdBathAttraction.isAttractiveToNightHeron(bath);
                                    }
                                }).build())
                        .build());
    }

    @Override protected SeagullEntity getSelf() { return this; }

    public static AttributeSupplier.Builder createAttributes() {
        return SimpleNeoBirdEntity.createAttributes(8.0, 0.27, 0.62, 22.0);
    }

    public static boolean canSpawn(EntityType<? extends AbstractBirdEntity<?>> type,
                                   ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return SimpleNeoBirdEntity.canSpawn(type, level, spawnType, pos, random, NeoGuanNiaoBirdData.SEAGULL.get());
    }
}
