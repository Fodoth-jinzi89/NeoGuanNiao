package net.fodoth.skina.neoguanniao.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.fodoth.skina.neoguanniao.content.bird.core.flight.BirdFlightAware;
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


@Mixin(
        value = BlockBehaviour.BlockStateBase.class,
        priority = 500
)
public class BlockStateBaseMixin {

    @Unique
    private static final VoxelShape neoguanniao$LEAVES_PERCH_SHAPE =
            Shapes.box(
                    0.0F,
                    0.0F,
                    0.0F,
                    1.0F,
                    0.75F,
                    1.0F
            );


    @ModifyReturnValue(
            method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
            at = @At("RETURN")
    )
    private VoxelShape neoguanniao$birdLeavesCollision(
            VoxelShape original,
            BlockGetter blockGetter,
            BlockPos blockPos,
            CollisionContext collisionContext
    ) {
        @SuppressWarnings("DataFlowIssue") BlockState state = (BlockState) (Object) this;

        if (state.getBlock() instanceof LeavesBlock
                && collisionContext instanceof EntityCollisionContext entityContext) {

            Entity entity = entityContext.getEntity();

            if (entity instanceof BirdFlightAware bird) {

                boolean activelyFlying =
                        bird.isBirdFlightActive()
                                && (!entity.onGround() || entity.isPassenger());

                if (!activelyFlying
                        && entityContext.isAbove(neoguanniao$LEAVES_PERCH_SHAPE, blockPos, true)) {

                    return neoguanniao$LEAVES_PERCH_SHAPE;

                } else {

                    return Shapes.empty();

                }
            }
        }
        return original;
    }
}
