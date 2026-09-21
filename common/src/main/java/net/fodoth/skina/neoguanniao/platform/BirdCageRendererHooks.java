package net.fodoth.skina.neoguanniao.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.fodoth.skina.neoguanniao.client.cage.BirdCageRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class BirdCageRendererHooks {
    private BirdCageRendererHooks() {}

    @ExpectPlatform
    public static @NotNull BirdCageRenderer create(BlockEntityRendererProvider.Context context) {
        throw new AssertionError();
    }

    /**
     * 流体静止纹理在方块图集里的精灵，供鸟笼食盆显示盆里的流体用。
     * 该流体没有注册渲染信息时返回 null。
     */
    @ExpectPlatform
    public static @Nullable TextureAtlasSprite fluidStillSprite(@NotNull Fluid fluid) {
        throw new AssertionError();
    }

    /**
     * 渲染该流体时要乘上的颜色，不需要染色时返回 {@code -1}；生物群系染色就发生在这里。
     * <p>
     * 水这类流体的颜色取决于所在的生物群系，所以要把流体的位置传进来。
     * </p>
     */
    @ExpectPlatform
    public static int fluidTint(@NotNull Fluid fluid, @NotNull BlockAndTintGetter level, @NotNull BlockPos pos) {
        throw new AssertionError();
    }
}
