package net.fodoth.skina.neoguanniao.client.camera;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid= NeoGuanNiao.MODID, value=Dist.CLIENT)
public final class CameraKeyMappings {
    public static final KeyMapping OPEN_FILTER_LIBRARY = new KeyMapping("key.neoguanniao.camera.cycle_filter", 86, "key.categories.neoguanniao");
    public static final KeyMapping OPEN_CREATIVE_CONTROLS = new KeyMapping("key.neoguanniao.camera.open_creative_controls", 67, "key.categories.neoguanniao");
    public static final KeyMapping FOCUS = new KeyMapping("key.neoguanniao.camera.focus", 82, "key.categories.neoguanniao");

    private CameraKeyMappings() {
    }

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(OPEN_FILTER_LIBRARY);
        event.register(OPEN_CREATIVE_CONTROLS);
        event.register(FOCUS);
    }
}

