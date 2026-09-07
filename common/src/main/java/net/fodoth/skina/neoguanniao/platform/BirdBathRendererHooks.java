package net.fodoth.skina.neoguanniao.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.fodoth.skina.neoguanniao.client.bath.BirdBathRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import org.jetbrains.annotations.NotNull;

public final class BirdBathRendererHooks {
    private BirdBathRendererHooks() {}

    @ExpectPlatform
    public static @NotNull BirdBathRenderer create(BlockEntityRendererProvider.Context context) {
        throw new AssertionError();
    }
}
