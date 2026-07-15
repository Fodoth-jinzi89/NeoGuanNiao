package net.fodoth.skina.neoguanniao.registry;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.content.bird.core.data.BirdData;
import net.fodoth.skina.neoguanniao.content.bird.core.data.datum.BirdAnimationDatum;
import net.fodoth.skina.neoguanniao.content.bird.core.data.datum.BirdFlyingDatum;
import net.fodoth.skina.neoguanniao.content.bird.core.data.datum.BirdModelAndTextureDatum;
import net.fodoth.skina.neoguanniao.content.bird.core.data.datum.BirdSoundDatum;
import net.fodoth.skina.neoguanniao.content.bird.feature.flight.BirdFlightProfile;
import net.fodoth.skina.neoguanniao.content.bird.feature.scale.BirdModelScaleProfile;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import software.bernie.geckolib.animation.RawAnimation;

import java.util.Map;

public final class NeoGuanNiaoBirdData {


    public static final DeferredRegister<BirdData> BIRD_DATA =
            DeferredRegister.create(
                    ResourceKey.createRegistryKey(
                            resource(
                                    "bird_data"
                            )
                    ),
                    NeoGuanNiao.MODID
            );


    static {
        BIRD_DATA.makeRegistry(builder -> builder
                .sync(true)
                .defaultKey(resource("default_bird"))
                .maxId(Integer.MAX_VALUE));
    }

    public static final DeferredHolder<BirdData, BirdData> BUDGERIGAR =
            BIRD_DATA.register("neo_budgerigar", () ->
                    BirdData.createDefault()
                            .withSound(
                                    new BirdSoundDatum(
                                            0.5F,
                                            180,
                                            NeoGuanNiaoSoundEvents.BUDGERIGAR_AMBIENT.get(),
                                            NeoGuanNiaoSoundEvents.BUDGERIGAR_HURT.get(),
                                            NeoGuanNiaoSoundEvents.BUDGERIGAR_DEATH.get(),
                                            NeoGuanNiaoSoundEvents.BUDGERIGAR_INTERACT.get(),
                                            SoundEvents.PARROT_EAT
                                    )
                            )
                            .withFlying(
                                    BirdFlyingDatum.createDefault()
                                            .withFlightProfile(BirdFlightProfile.BUDGERIGAR)
                            )
                            .withModel(
                                    new BirdModelAndTextureDatum(
                                            resource("geo/budgerigar.geo.json"),
                                            new ResourceLocation[]{
                                                    resource("textures/entity/budgerigar.png"),
                                                    resource("textures/entity/budgerigar/white_lark.png"),
                                                    resource("textures/entity/budgerigar/mystery_green.png"),
                                                    resource("textures/entity/budgerigar/blue_lark.png"),
                                                    resource("textures/entity/budgerigar/blue_porcelain.png"),
                                                    resource("textures/entity/budgerigar/yellow_lark.png"),
                                                    resource("textures/entity/budgerigar/yellow.png"),
                                                    resource("textures/entity/budgerigar/yellow_2.png"),
                                                    resource("textures/entity/budgerigar/yellow_black.png"),
                                                    resource("textures/entity/budgerigar/black_white.png")
                                            },
                                            BirdModelScaleProfile.BUDGERIGAR,
                                            10,
                                            0.12F,
                                            1.0F
                                    )
                            )
                            .withAnimation(
                                    BirdAnimationDatum.withAnimationIdAndMap(
                                            resource("animations/budgerigar.animation.json"),
                                            Map.of(
                                                    "idle", RawAnimation.begin().thenLoop("idle"),
                                                    "preen", RawAnimation.begin().thenPlay("idle_diff_1").thenLoop("idle"),
                                                    "curious", RawAnimation.begin().thenPlay("idle_diff_2").thenLoop("idle"),
                                                    "dance", RawAnimation.begin().thenLoop("idle_diff_3"),
                                                    "eat", RawAnimation.begin().thenPlay("eat").thenLoop("idle"),
                                                    "sleep", RawAnimation.begin().thenPlay("sleep").thenLoop("sleep_loop"),
                                                    "sleep_loop", RawAnimation.begin().thenLoop("sleep_loop"),
                                                    "walk", RawAnimation.begin().thenLoop("walk"),
                                                    "fly", RawAnimation.begin().thenLoop("fly_flapping_wing_loop")
                                            )
                                    )
                            )
            );


    private static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(NeoGuanNiao.MODID, path);
    }

}