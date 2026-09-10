package net.fodoth.skina.neoguanniao.platform.neoforge;

import net.fodoth.skina.neoguanniao.client.cage.BirdCageRenderer;
import net.fodoth.skina.neoguanniao.content.cage.BirdCageBlockEntity;
import net.fodoth.skina.neoguanniao.content.cage.BirdCageVariant;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

public final class BirdCageRendererHooksImpl {
    private BirdCageRendererHooksImpl() {}

    public static @NotNull BirdCageRenderer create(BlockEntityRendererProvider.Context context) {
        // Compile the override on NeoForge so its generic bridge is generated.
        return new BirdCageRenderer(context) {
            @Override
            public @NotNull AABB getRenderBoundingBox(@NotNull BirdCageBlockEntity birdCage) {
                var pos = birdCage.getBlockPos();
                double height = switch (birdCage.variant()) {
                    case SMALL -> 1.0D;
                    case MEDIUM -> 3.0D;
                    case LARGE -> 4.0D;
                };
                return new AABB(
                        pos.getX(), pos.getY(), pos.getZ(),
                        pos.getX() + (birdCage.variant() == BirdCageVariant.SMALL ? 1.0D : 3.0D),
                        pos.getY() + height,
                        pos.getZ() + (birdCage.variant() == BirdCageVariant.SMALL ? 1.0D : 3.0D)
                );
            }
        };
    }
}
