package net.fodoth.skina.neoguanniao.client;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.client.bath.BirdBathItemRenderer;
import net.fodoth.skina.neoguanniao.client.bath.BirdBathRenderer;
import net.fodoth.skina.neoguanniao.client.bird.BirdModelRenderer;
import net.fodoth.skina.neoguanniao.client.cage.BirdCageItemRenderer;
import net.fodoth.skina.neoguanniao.client.cage.BirdCageRenderer;
import net.fodoth.skina.neoguanniao.client.nest.BirdNestItemRenderer;
import net.fodoth.skina.neoguanniao.client.nest.BirdNestRenderer;
import net.fodoth.skina.neoguanniao.client.camera.FilmItemRenderer;
import net.fodoth.skina.neoguanniao.client.camera.NikonD750ItemRenderer;
import net.fodoth.skina.neoguanniao.client.camera.PhotographEntityRenderer;
import net.fodoth.skina.neoguanniao.client.camera.PhotographItemRenderer;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoBlockEntityTypes;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoEntityTypes;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoItems;
import net.fodoth.skina.neoguanniao.util.ClientExtensionHelper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(
        modid = NeoGuanNiao.MODID,
        value = Dist.CLIENT
)
public final class NeoGuanNiaoClientModEvents {

    private NeoGuanNiaoClientModEvents() {
    }


    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(
                NeoGuanNiaoClient::init
        );
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {

        event.registerEntityRenderer(
                NeoGuanNiaoEntityTypes.NEO_BUDGERIGAR.get(),
                BirdModelRenderer::new
        );

        event.registerEntityRenderer(
                NeoGuanNiaoEntityTypes.NEO_NIGHT_HERON.get(),
                BirdModelRenderer::new
        );

        event.registerEntityRenderer(
                NeoGuanNiaoEntityTypes.NEO_PIGEON.get(),
                BirdModelRenderer::new
        );

        event.registerEntityRenderer(
                NeoGuanNiaoEntityTypes.NEO_DOVE.get(),
                BirdModelRenderer::new
        );

        event.registerEntityRenderer(
                NeoGuanNiaoEntityTypes.NEO_SPARROW.get(),
                BirdModelRenderer::new
        );
        event.registerEntityRenderer(NeoGuanNiaoEntityTypes.NEO_COCKATIEL.get(), BirdModelRenderer::new);
        event.registerEntityRenderer(NeoGuanNiaoEntityTypes.NEO_LONG_TAILED_TIT.get(), BirdModelRenderer::new);
        event.registerEntityRenderer(NeoGuanNiaoEntityTypes.NEO_MACAW.get(), BirdModelRenderer::new);
        event.registerEntityRenderer(NeoGuanNiaoEntityTypes.NEO_CROW.get(), BirdModelRenderer::new);
        event.registerEntityRenderer(NeoGuanNiaoEntityTypes.NEO_SEAGULL.get(), BirdModelRenderer::new);
        event.registerEntityRenderer(NeoGuanNiaoEntityTypes.NEO_KIWI.get(), BirdModelRenderer::new);
        event.registerEntityRenderer(NeoGuanNiaoEntityTypes.NEO_MYNA.get(), BirdModelRenderer::new);
        event.registerEntityRenderer(NeoGuanNiaoEntityTypes.PHOTOGRAPH.get(), PhotographEntityRenderer::new);
        event.registerEntityRenderer(NeoGuanNiaoEntityTypes.FEATHER_FAN_PROJECTILE.get(), net.minecraft.client.renderer.entity.ThrownItemRenderer::new);

        event.registerBlockEntityRenderer(
                NeoGuanNiaoBlockEntityTypes.BIRD_CAGE.get(),
                BirdCageRenderer::new
        );

        event.registerBlockEntityRenderer(
                NeoGuanNiaoBlockEntityTypes.BIRD_BATH.get(),
                BirdBathRenderer::new
        );

        event.registerBlockEntityRenderer(
                NeoGuanNiaoBlockEntityTypes.BIRD_NEST.get(),
                BirdNestRenderer::new
        );
    }

    @SubscribeEvent
    public static void registerItemExtensions(
            RegisterClientExtensionsEvent event
    ) {

        ClientExtensionHelper.registerGeoItemRenderer(
                event,
                NeoGuanNiaoItems.BIRD_BATH.get(),
                BirdBathItemRenderer::new
        );

        ClientExtensionHelper.registerGeoItemRenderer(event, NeoGuanNiaoItems.NIKON_D750.get(), NikonD750ItemRenderer::new);
        ClientExtensionHelper.registerItemRenderer(event, NeoGuanNiaoItems.FILM.get(), FilmItemRenderer::new);
        ClientExtensionHelper.registerItemRenderer(event, NeoGuanNiaoItems.PHOTOGRAPH.get(), PhotographItemRenderer::new);

        ClientExtensionHelper.registerGeoItemRenderer(
                event,
                NeoGuanNiaoItems.WOODEN_BIRD_BATH.get(),
                BirdBathItemRenderer::new
        );

        ClientExtensionHelper.registerGeoItemRenderer(
                event,
                NeoGuanNiaoItems.STONE_BIRD_BATH.get(),
                BirdBathItemRenderer::new
        );

        ClientExtensionHelper.registerGeoItemRenderer(
                event,
                NeoGuanNiaoItems.BIRD_BATH_2.get(),
                BirdBathItemRenderer::new
        );

        ClientExtensionHelper.registerGeoItemRenderer(
                event,
                NeoGuanNiaoItems.WOODEN_BIRD_BATH_2.get(),
                BirdBathItemRenderer::new
        );

        ClientExtensionHelper.registerGeoItemRenderer(
                event,
                NeoGuanNiaoItems.STONE_BIRD_BATH_2.get(),
                BirdBathItemRenderer::new
        );

        // Bird cages
        ClientExtensionHelper.registerGeoItemRenderer(
                event,
                NeoGuanNiaoItems.SMALL_BIRD_CAGE.get(),
                BirdCageItemRenderer::new
        );

        ClientExtensionHelper.registerGeoItemRenderer(
                event,
                NeoGuanNiaoItems.MEDIUM_BIRD_CAGE.get(),
                BirdCageItemRenderer::new
        );

        ClientExtensionHelper.registerGeoItemRenderer(
                event,
                NeoGuanNiaoItems.LARGE_BIRD_CAGE.get(),
                BirdCageItemRenderer::new
        );

        ClientExtensionHelper.registerGeoItemRenderer(
                event,
                NeoGuanNiaoItems.BIRD_NEST.get(),
                BirdNestItemRenderer::new
        );

    }
}
