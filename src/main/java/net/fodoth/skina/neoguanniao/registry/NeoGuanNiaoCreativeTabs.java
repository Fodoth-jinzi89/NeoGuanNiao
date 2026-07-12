package net.fodoth.skina.neoguanniao.registry;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
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
                            })
                            .build()
            );

    private NeoGuanNiaoCreativeTabs() {}
}
