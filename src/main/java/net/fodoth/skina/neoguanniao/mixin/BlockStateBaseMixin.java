package net.fodoth.skina.neoguanniao.mixin;

import net.fodoth.skina.neoguanniao.content.bird.flight.BirdFlightAware;
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


    @Inject(
            method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void neoguanniao$birdLeavesCollision(
            BlockGetter level,
            BlockPos pos,
            CollisionContext context,
            CallbackInfoReturnable<VoxelShape> cir
    ) {
        BlockState state = (BlockState) (Object) this;

        if (state.getBlock() instanceof LeavesBlock
                && context instanceof EntityCollisionContext entityContext) {

            Entity entity = entityContext.getEntity();

            if (entity instanceof BirdFlightAware bird) {

                boolean activelyFlying =
                        bird.isBirdFlightActive()
                                && (!entity.onGround() || entity.isPassenger());

                if (!activelyFlying
                        && entityContext.isAbove(neoguanniao$LEAVES_PERCH_SHAPE, pos, true)) {

                    cir.setReturnValue(neoguanniao$LEAVES_PERCH_SHAPE);

                } else {

                    cir.setReturnValue(Shapes.empty());

                }
            }
        }
    }
}