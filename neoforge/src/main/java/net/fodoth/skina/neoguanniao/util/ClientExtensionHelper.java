package net.fodoth.skina.neoguanniao.util;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.Item;
import dev.architectury.registry.registries.RegistrySupplier;

import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;


public class ClientExtensionHelper {


    public static void registerGeoItemRenderer(
            RegisterClientExtensionsEvent event,
            RegistrySupplier<Item> item,
            Supplier<BlockEntityWithoutLevelRenderer> rendererSupplier
    ) {

        if (!item.isPresent()) {
            return;
        }
        event.registerItem(
                new IClientItemExtensions() {

                    private final Supplier<BlockEntityWithoutLevelRenderer> renderer =
                            rendererSupplier;


                    @Override
                    public @NotNull BlockEntityWithoutLevelRenderer getCustomRenderer() {
                        return renderer.get();
                    }
                },

                item.get()
        );
    }

    public static void registerItemRenderer(
            RegisterClientExtensionsEvent event,
            RegistrySupplier<Item> item,
            Supplier<? extends BlockEntityWithoutLevelRenderer> rendererFactory
    ) {
        if (!item.isPresent()) {
            return;
        }
        event.registerItem(new IClientItemExtensions() {
            private BlockEntityWithoutLevelRenderer renderer;

            @Override
            public @NotNull BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = rendererFactory.get();
                }
                return renderer;
            }
        }, item.get());
    }
}
