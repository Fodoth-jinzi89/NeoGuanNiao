package net.fodoth.skina.neoguanniao.platform.fabric;

import net.fodoth.skina.neoguanniao.client.bath.BirdBathRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import org.jetbrains.annotations.NotNull;

public final class BirdBathRendererHooksImpl {
    private BirdBathRendererHooksImpl() {}

    public static @NotNull BirdBathRenderer create(BlockEntityRendererProvider.Context context) {
        return new BirdBathRenderer(context);
    }
}
