package com.birdcamera.mixin;

import com.birdcamera.content.bird.core.flight.BirdFlightAware;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * BlockStateBase Mixin - 鸟类树叶碰撞处理（与原版 2.9.1 一致）
 * 鸟类可以停在树叶上（栖枝形状），飞行时穿过树叶不产生碰撞
 */
@Mixin(value = BlockBehaviour.BlockStateBase.class, priority = 500)
public class BlockStateBaseMixin {

    @Unique
    private static final VoxelShape birdcamera$LEAVES_PERCH_SHAPE =
            Shapes.box(
                    0.0F,
                    0.0F,
                    0.0F,
                    1.0F,
                    0.75F,
                    1.0F
            );

    @Inject(method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
            at = @At("HEAD"), cancellable = true)
    private void birdcamera$birdLeavesCollision(BlockGetter level, BlockPos pos, CollisionContext context,
                                                  CallbackInfoReturnable<VoxelShape> cir) {
        BlockState state = (BlockState) (Object) this;

        if (state.getBlock() instanceof LeavesBlock
                && context instanceof EntityCollisionContext entityContext) {

            Entity entity = entityContext.getEntity();

            if (entity instanceof BirdFlightAware bird) {

                boolean activelyFlying =
                        bird.isBirdFlightActive()
                                && (!entity.onGround() || entity.isPassenger());

                if (!activelyFlying
                        && entityContext.isAbove(birdcamera$LEAVES_PERCH_SHAPE, pos, true)) {

                    cir.setReturnValue(birdcamera$LEAVES_PERCH_SHAPE);

                } else {

                    cir.setReturnValue(Shapes.empty());

                }
            }
        }
    }
}
