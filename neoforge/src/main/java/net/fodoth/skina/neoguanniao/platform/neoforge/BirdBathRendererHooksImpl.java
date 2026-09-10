package net.fodoth.skina.neoguanniao.platform.neoforge;

import net.fodoth.skina.neoguanniao.client.bath.BirdBathRenderer;
import net.fodoth.skina.neoguanniao.content.bath.BirdBathBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

public final class BirdBathRendererHooksImpl {
    private BirdBathRendererHooksImpl() {}

    public static @NotNull BirdBathRenderer create(BlockEntityRendererProvider.Context context) {
        // Compile the override on NeoForge so its generic bridge is generated.
        return new BirdBathRenderer(context) {
            @Override
            public @NotNull AABB getRenderBoundingBox(BirdBathBlockEntity birdBath) {
                var pos = birdBath.getBlockPos();
                return new AABB(
                        pos.getX(), pos.getY(), pos.getZ(),
                        pos.getX() + 1.0D, pos.getY() + 1.5D, pos.getZ() + 1.0D
                );
            }
        };
    }
}
