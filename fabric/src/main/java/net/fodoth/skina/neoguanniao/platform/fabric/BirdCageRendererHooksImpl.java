package net.fodoth.skina.neoguanniao.platform.fabric;

import net.fodoth.skina.neoguanniao.client.cage.BirdCageRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import org.jetbrains.annotations.NotNull;

public final class BirdCageRendererHooksImpl {
    private BirdCageRendererHooksImpl() {}

    public static @NotNull BirdCageRenderer create(BlockEntityRendererProvider.Context context) {
        return new BirdCageRenderer(context);
    }
}
