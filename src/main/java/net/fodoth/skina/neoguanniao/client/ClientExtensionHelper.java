package net.fodoth.skina.neoguanniao.client;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.Item;

import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;


public class ClientExtensionHelper {


    public static void registerGeoItemRenderer(
            RegisterClientExtensionsEvent event,
            Item item,
            Supplier<BlockEntityWithoutLevelRenderer> rendererSupplier
    ) {

        event.registerItem(
                new IClientItemExtensions() {

                    private final Supplier<BlockEntityWithoutLevelRenderer> renderer =
                            rendererSupplier;


                    @Override
                    public @NotNull BlockEntityWithoutLevelRenderer getCustomRenderer() {
                        return renderer.get();
                    }
                },

                item
        );
    }
}