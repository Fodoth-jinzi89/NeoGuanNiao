package net.fodoth.skina.neoguanniao;

import net.fabricmc.api.ClientModInitializer;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoItemProperties;
import net.fodoth.skina.neoguanniao.client.NeoGuanNiaoFabricClientParticles;
import net.fodoth.skina.neoguanniao.client.NeoGuanNiaoFabricKeyBindings;
import net.fodoth.skina.neoguanniao.client.NeoGuanNiaoFabricClientNetwork;
import net.fodoth.skina.neoguanniao.client.NeoGuanNiaoFabricItemRenderers;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoEntityTypes;
import net.fodoth.skina.neoguanniao.client.fan.FeatherFanProjectileRenderer;
import net.fodoth.skina.neoguanniao.client.bird.BirdModelRenderer;
import net.fodoth.skina.neoguanniao.platform.BirdCageRendererHooks;
import net.fodoth.skina.neoguanniao.platform.BirdBathRendererHooks;
import net.fodoth.skina.neoguanniao.client.nest.BirdNestRenderer;
import net.fodoth.skina.neoguanniao.client.camera.PhotographEntityRenderer;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoBlockEntityTypes;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fodoth.skina.neoguanniao.client.camera.CameraClientEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.fodoth.skina.neoguanniao.client.camera.CameraOpticsShader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.resources.ResourceLocation;
import net.fodoth.skina.neoguanniao.config.NeoGuanNiaoFabricConfigCommand;

public final class NeoGuanNiaoFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        NeoGuanNiaoFabricClientNetwork.register();
        NeoGuanNiaoFabricConfigCommand.register();
        NeoGuanNiaoItemProperties.register();
        NeoGuanNiaoFabricItemRenderers.register();
        NeoGuanNiaoFabricClientParticles.register();
        NeoGuanNiaoFabricKeyBindings.register();
        ClientTickEvents.END_CLIENT_TICK.register(client -> CameraClientEvents.onClientTick(false));
        CoreShaderRegistrationCallback.EVENT.register(context -> context.register(
                ResourceLocation.fromNamespaceAndPath("neoguanniao", "camera_optics"),
                DefaultVertexFormat.POSITION_TEX, CameraOpticsShader::setShader));
        HudRenderCallback.EVENT.register((graphics, tickDelta) -> CameraClientEvents.onRenderGui(graphics, tickDelta.getGameTimeDeltaPartialTick(false)));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> CameraClientEvents.onClientLogout());
        EntityRendererRegistry.register(NeoGuanNiaoEntityTypes.FEATHER_FAN_PROJECTILE.get(),
                FeatherFanProjectileRenderer::new);
        EntityRendererRegistry.register(NeoGuanNiaoEntityTypes.PHOTOGRAPH.get(), PhotographEntityRenderer::new);
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
        EntityRendererRegistry.register(NeoGuanNiaoEntityTypes.NEO_WOODCOCK.get(), BirdModelRenderer::new);
        BlockEntityRenderers.register(NeoGuanNiaoBlockEntityTypes.BIRD_CAGE.get(), BirdCageRendererHooks::create);
        BlockEntityRenderers.register(NeoGuanNiaoBlockEntityTypes.BIRD_BATH.get(), BirdBathRendererHooks::create);
        BlockEntityRenderers.register(NeoGuanNiaoBlockEntityTypes.BIRD_NEST.get(), BirdNestRenderer::new);
    }
}
