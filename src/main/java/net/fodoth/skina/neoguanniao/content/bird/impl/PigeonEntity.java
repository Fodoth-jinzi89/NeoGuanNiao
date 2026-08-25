package net.fodoth.skina.neoguanniao.content.bird.impl;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.SimpleNeoBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.controller.BirdAnimationController;
import net.fodoth.skina.neoguanniao.content.bird.core.controller.BirdControllers;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoBirdData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import software.bernie.geckolib.animation.RawAnimation;

import java.util.ArrayList;
import java.util.List;

public class PigeonEntity extends SimpleNeoBirdEntity<PigeonEntity> {

    public PigeonEntity(EntityType<PigeonEntity> entityType, Level level) {
        super(
                entityType,
                level,
                NeoGuanNiaoBirdData.PIGEON.get(),
                BirdControllers.<PigeonEntity>builder().birdAnimationController(new BirdAnimationController<>(){
                    @Override
                    public RawAnimation pickFlyAnimation() {
                        var animationMap = bird().getBirdData().animation().animationMap();
                        if (bird().getDeltaMovement().y() > 0.05) {
                            return animationMap.get("fly");
                        }
                        ClipContext context = new ClipContext(
                                bird().position(),
                                bird().position().subtract(0, 5, 0),
                                ClipContext.Block.COLLIDER,
                                ClipContext.Fluid.NONE,
                                bird()
                        );

                        BlockHitResult hit = bird().level().clip(context);

                        if (hit.getType() != HitResult.Type.MISS) {
                            return animationMap.get("fly");
                        }
                        return animationMap.get("fly_glide");
                    }
                }).build()
        );
    }

    @Override
    protected PigeonEntity getSelf() {
        return this;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return SimpleNeoBirdEntity.createAttributes(8.0, 0.22, 0.42, 18.0);
    }

    public static boolean canSpawn(
            EntityType<? extends AbstractBirdEntity<?>> entityType,
            ServerLevelAccessor level,
            MobSpawnType spawnType,
            BlockPos pos,
            RandomSource random
    ) {
        BlockState below = level.getBlockState(pos.below());
        boolean validGround = below.is(BlockTags.DIRT) || below.is(Blocks.GRASS_BLOCK)
                || below.is(BlockTags.SAND) || below.is(Blocks.FARMLAND);

        if (!validGround) {
            return false;
        }
        AABB searchBox = new AABB(
                pos.getX() - 8, pos.getY() - 4, pos.getZ() - 8,
                pos.getX() + 8, pos.getY() + 4, pos.getZ() + 8
        );
        int entityCount = level.getEntitiesOfClass(PigeonEntity.class, searchBox).size();
        // 如果附近同类型实体超过 12 个，不允许生成
        if (entityCount > 12) {
            return false;
        }
        // 计算栖息地分数
        int score = habitatScore(level, pos);
        return score >= 12 || (score >= 7 && random.nextFloat() < 0.55F);
    }

    private static int habitatScore(LevelReader level, BlockPos origin) {
        int score = 0;
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-7, -2, -7), origin.offset(7, 5, 7))) {
            if (canReadChunk(level, pos)) {
                BlockState state = level.getBlockState(pos);
                if (!state.is(Blocks.FARMLAND) && !state.is(Blocks.DIRT_PATH) && !(state.getBlock() instanceof CropBlock)) {
                    if (!state.is(Blocks.PODZOL) && !state.is(Blocks.GRASS_BLOCK) && !(state.getBlock() instanceof ComposterBlock)) {
                        if (!state.is(BlockTags.WALLS) && !state.is(BlockTags.LEAVES)) {
                            if (!(state.getBlock() instanceof FenceBlock) && !(state.getBlock() instanceof FenceGateBlock)) {
                                if (state.getBlock() instanceof DoorBlock || state.getBlock() instanceof BedBlock) {
                                    score += 4;
                                }
                            } else {
                                score += 2;
                            }
                        } else {
                            score += 1;
                        }
                    } else {
                        score += 3;
                    }
                } else {
                    score += 2;
                }
                if (score >= 24) return score;
            }
        }
        return score;
    }

    @SuppressWarnings("deprecation")
    private static boolean canReadChunk(LevelReader level, BlockPos pos) {
        return level.hasChunk(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
    }

    @Override
    protected List<Goal> buildGoals() {
        return new ArrayList<>(super.buildGoals());
    }

}
