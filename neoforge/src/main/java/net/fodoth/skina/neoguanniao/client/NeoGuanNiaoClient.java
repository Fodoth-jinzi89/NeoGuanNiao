package net.fodoth.skina.neoguanniao.client;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoItemProperties;
import net.fodoth.skina.neoguanniao.config.NeoGuanNiaoConfigScreen;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.fml.common.Mod;
import net.neoforged.api.distmarker.Dist;

@Mod(value = NeoGuanNiao.MODID, dist = Dist.CLIENT)
public class NeoGuanNiaoClient {

    public NeoGuanNiaoClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, (mc, parent) -> NeoGuanNiaoConfigScreen.create(parent));
    }

    public static void init() {
        NeoGuanNiaoItemProperties.register();
    }
}
