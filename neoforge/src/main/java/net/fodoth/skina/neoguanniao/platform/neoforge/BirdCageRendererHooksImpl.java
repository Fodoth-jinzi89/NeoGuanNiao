package net.fodoth.skina.neoguanniao.platform.neoforge;

import net.fodoth.skina.neoguanniao.client.cage.BirdCageRenderer;
import net.fodoth.skina.neoguanniao.content.cage.BirdCageBlockEntity;
import net.fodoth.skina.neoguanniao.content.cage.BirdCageVariant;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    public static @Nullable TextureAtlasSprite fluidStillSprite(@NotNull Fluid fluid) {
        ResourceLocation still = IClientFluidTypeExtensions.of(fluid).getStillTexture();
        if (still == null) return null;
        return Minecraft.getInstance().getModelManager()
                .getAtlas(InventoryMenu.BLOCK_ATLAS)
                .getSprite(still);
    }

    public static int fluidTint(@NotNull Fluid fluid, @NotNull BlockAndTintGetter level, @NotNull BlockPos pos) {
        return IClientFluidTypeExtensions.of(fluid).getTintColor(fluid.defaultFluidState(), level, pos);
    }
}
