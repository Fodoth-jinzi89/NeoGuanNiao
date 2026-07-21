package net.fodoth.skina.neoguanniao.client;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.client.bath.BirdBathItemRenderer;
import net.fodoth.skina.neoguanniao.client.bath.BirdBathRenderer;
import net.fodoth.skina.neoguanniao.client.bird.BirdModelRenderer;
import net.fodoth.skina.neoguanniao.client.cage.BirdCageItemRenderer;
import net.fodoth.skina.neoguanniao.client.cage.BirdCageRenderer;
import net.fodoth.skina.neoguanniao.client.nest.BirdNestItemRenderer;
import net.fodoth.skina.neoguanniao.client.nest.BirdNestRenderer;
import net.fodoth.skina.neoguanniao.client.old.budgerigar.BudgerigarRenderer;
import net.fodoth.skina.neoguanniao.client.old.columbid.PigeonRenderer;
import net.fodoth.skina.neoguanniao.client.old.columbid.SpottedDoveRenderer;
import net.fodoth.skina.neoguanniao.client.old.nightheron.NightHeronRenderer;
import net.fodoth.skina.neoguanniao.client.old.sparrow.SparrowRenderer;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoBlockEntityTypes;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoEntityTypes;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoItems;
import net.fodoth.skina.neoguanniao.util.ClientExtensionHelper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
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
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {

        event.registerEntityRenderer(
                NeoGuanNiaoEntityTypes.NIGHT_HERON.get(),
                NightHeronRenderer::new
        );

        event.registerEntityRenderer(
                NeoGuanNiaoEntityTypes.SPARROW.get(),
                SparrowRenderer::new
        );

        event.registerEntityRenderer(
                NeoGuanNiaoEntityTypes.BUDGERIGAR.get(),
                BudgerigarRenderer::new
        );

        event.registerEntityRenderer(
                NeoGuanNiaoEntityTypes.SPOTTED_DOVE.get(),
                SpottedDoveRenderer::new
        );

        event.registerEntityRenderer(
                NeoGuanNiaoEntityTypes.PIGEON.get(),
                PigeonRenderer::new
        );

        event.registerEntityRenderer(
                NeoGuanNiaoEntityTypes.NEO_BUDGERIGAR.get(),
                BirdModelRenderer::new
        );

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