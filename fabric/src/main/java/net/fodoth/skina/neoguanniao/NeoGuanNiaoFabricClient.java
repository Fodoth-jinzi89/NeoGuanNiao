package net.fodoth.skina.neoguanniao;

import net.fabricmc.api.ClientModInitializer;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoItemProperties;
import net.fodoth.skina.neoguanniao.client.NeoGuanNiaoFabricClientParticles;
import net.fodoth.skina.neoguanniao.client.NeoGuanNiaoFabricKeyBindings;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoEntityTypes;
import net.fodoth.skina.neoguanniao.client.NeoGuanNiaoFabricProjectileRenderer;
import net.fodoth.skina.neoguanniao.client.bird.BirdModelRenderer;
import net.fodoth.skina.neoguanniao.client.cage.BirdCageRenderer;
import net.fodoth.skina.neoguanniao.client.bath.BirdBathRenderer;
import net.fodoth.skina.neoguanniao.client.nest.BirdNestRenderer;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoBlockEntityTypes;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public final class NeoGuanNiaoFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        NeoGuanNiaoItemProperties.register();
        NeoGuanNiaoFabricClientParticles.register();
        NeoGuanNiaoFabricKeyBindings.register();
        EntityRendererRegistry.register(NeoGuanNiaoEntityTypes.FEATHER_FAN_PROJECTILE.get(),
                NeoGuanNiaoFabricProjectileRenderer::new);
        EntityRendererRegistry.register(NeoGuanNiaoEntityTypes.NEO_BUDGERIGAR.get(), BirdModelRenderer::new);
        EntityRendererRegistry.register(NeoGuanNiaoEntityTypes.NEO_NIGHT_HERON.get(), BirdModelRenderer::new);
        EntityRendererRegistry.register(NeoGuanNiaoEntityTypes.NEO_PIGEON.get(), BirdModelRenderer::new);
        EntityRendererRegistry.register(NeoGuanNiaoEntityTypes.NEO_DOVE.get(), BirdModelRenderer::new);
        EntityRendererRegistry.register(NeoGuanNiaoEntityTypes.NEO_SPARROW.get(), BirdModelRenderer::new);
        EntityRendererRegistry.register(NeoGuanNiaoEntityTypes.NEO_COCKATIEL.get(), BirdModelRenderer::new);
        EntityRendererRegistry.register(NeoGuanNiaoEntityTypes.NEO_LONG_TAILED_TIT.get(), BirdModelRenderer::new);
        EntityRendererRegistry.register(NeoGuanNiaoEntityTypes.NEO_MACAW.get(), BirdModelRenderer::new);
        EntityRendererRegistry.register(NeoGuanNiaoEntityTypes.NEO_CROW.get(), BirdModelRenderer::new);
        EntityRendererRegistry.register(NeoGuanNiaoEntityTypes.NEO_SEAGULL.get(), BirdModelRenderer::new);
        EntityRendererRegistry.register(NeoGuanNiaoEntityTypes.NEO_KIWI.get(), BirdModelRenderer::new);
        EntityRendererRegistry.register(NeoGuanNiaoEntityTypes.NEO_MYNA.get(), BirdModelRenderer::new);
        BlockEntityRenderers.register(NeoGuanNiaoBlockEntityTypes.BIRD_CAGE.get(), BirdCageRenderer::new);
        BlockEntityRenderers.register(NeoGuanNiaoBlockEntityTypes.BIRD_BATH.get(), BirdBathRenderer::new);
        BlockEntityRenderers.register(NeoGuanNiaoBlockEntityTypes.BIRD_NEST.get(), BirdNestRenderer::new);
    }
}
