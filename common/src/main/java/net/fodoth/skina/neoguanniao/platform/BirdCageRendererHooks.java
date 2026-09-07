package net.fodoth.skina.neoguanniao.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.fodoth.skina.neoguanniao.client.cage.BirdCageRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import org.jetbrains.annotations.NotNull;

public final class BirdCageRendererHooks {
    private BirdCageRendererHooks() {}

    @ExpectPlatform
    public static @NotNull BirdCageRenderer create(BlockEntityRendererProvider.Context context) {
        throw new AssertionError();
    }
}
