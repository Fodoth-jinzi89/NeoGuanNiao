package net.fodoth.skina.neoguanniao.registry;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.content.bird.core.data.BirdData;
import net.fodoth.skina.neoguanniao.content.bird.core.data.datum.BirdSkinDatum;
import net.fodoth.skina.neoguanniao.content.bird.core.skin.BirdSkin;
import net.fodoth.skina.neoguanniao.content.egg.BirdEggData;
import net.fodoth.skina.neoguanniao.content.egg.BirdEggItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class NeoGuanNiaoCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, NeoGuanNiao.MODID);

    public static final Supplier<CreativeModeTab> MAIN_TAB =
            CREATIVE_MODE_TABS.register("main", () ->
                    CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup.neoguanniao.main"))
                            .icon(() -> new ItemStack(NeoGuanNiaoItems.BIRD_GUIDE.get()))
                            .displayItems((parameters, output) -> {
                                output.accept(NeoGuanNiaoItems.BIRD_GUIDE.get());
                                output.accept(NeoGuanNiaoItems.BREADCRUMBS.get());

                                output.accept(NeoGuanNiaoItems.SMALL_BIRD_CAGE.get());
                                output.accept(NeoGuanNiaoItems.MEDIUM_BIRD_CAGE.get());
                                output.accept(NeoGuanNiaoItems.LARGE_BIRD_CAGE.get());

                                output.accept(NeoGuanNiaoItems.WOODEN_BIRD_BATH.get());
                                output.accept(NeoGuanNiaoItems.STONE_BIRD_BATH.get());
                                output.accept(NeoGuanNiaoItems.BIRD_BATH.get());

                                output.accept(NeoGuanNiaoItems.WOODEN_BIRD_BATH_2.get());
                                output.accept(NeoGuanNiaoItems.STONE_BIRD_BATH_2.get());
                                output.accept(NeoGuanNiaoItems.BIRD_BATH_2.get());

                                output.accept(NeoGuanNiaoItems.NIGHT_HERON_SPAWN_EGG.get());
                                output.accept(NeoGuanNiaoItems.SPARROW_SPAWN_EGG.get());
                                output.accept(NeoGuanNiaoItems.BUDGERIGAR_SPAWN_EGG.get());
                                output.accept(NeoGuanNiaoItems.SPOTTED_DOVE_SPAWN_EGG.get());
                                output.accept(NeoGuanNiaoItems.PIGEON_SPAWN_EGG.get());

                                output.accept(NeoGuanNiaoItems.BIRD_FOOD_BAG.get());
                                output.accept(NeoGuanNiaoItems.BIRD_FOOD_BAG_SEED.get());
                                output.accept(NeoGuanNiaoItems.BIRD_FOOD_BAG_FISH.get());


                                generateBirdEggs(output);
                            })
                            .build()
            );

    private NeoGuanNiaoCreativeTabs() {
    }

    private static void generateBirdEggs(
            CreativeModeTab.Output output
    ) {

        for (var holder : NeoGuanNiaoBirdData.BIRD_DATA.getEntries()) {

            BirdData birdData = holder.get();

            // 鸟种类
            ResourceLocation birdType = holder.getId();


            BirdSkinDatum modelDatum = birdData.model();


            for (BirdSkin skin : modelDatum.birdSkin()) {


                ItemStack egg =
                        new ItemStack(
                                NeoGuanNiaoItems.BIRD_EGG.get()
                        );


                BirdEggItem.setEggData(
                        egg,
                        BirdEggData.create(
                                birdType,
                                modelDatum.modelLocation(),
                                skin.id(),
                                1.0F,
                                6000,
                                true
                        )
                );


                output.accept(egg);
            }
        }
    }
}
