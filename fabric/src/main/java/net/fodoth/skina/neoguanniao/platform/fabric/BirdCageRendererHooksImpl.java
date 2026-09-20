package net.fodoth.skina.neoguanniao.platform.fabric;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fodoth.skina.neoguanniao.client.cage.BirdCageRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class BirdCageRendererHooksImpl {
    private BirdCageRendererHooksImpl() {}

    public static @NotNull BirdCageRenderer create(BlockEntityRendererProvider.Context context) {
        return new BirdCageRenderer(context);
    }

    public static @Nullable TextureAtlasSprite fluidStillSprite(@NotNull Fluid fluid) {
        FluidRenderHandler handler = FluidRenderHandlerRegistry.INSTANCE.get(fluid);
        if (handler == null) return null;
        // fabric 的处理器按流体状态给精灵；这里只是取静态纹理，不需要世界与环境。
        TextureAtlasSprite[] sprites = handler.getFluidSprites(null, null, fluid.defaultFluidState());
        return sprites == null || sprites.length == 0 ? null : sprites[0];
    }

    public static int fluidTint(@NotNull Fluid fluid, @NotNull BlockAndTintGetter level, @NotNull BlockPos pos) {
        FluidRenderHandler handler = FluidRenderHandlerRegistry.INSTANCE.get(fluid);
        // 处理器拿所在位置去取生物群系颜色；没有处理器的流体不上色。
        return handler == null ? -1 : handler.getFluidColor(level, pos, fluid.defaultFluidState());
    }
}
