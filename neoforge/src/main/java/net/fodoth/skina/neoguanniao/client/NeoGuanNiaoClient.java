package net.fodoth.skina.neoguanniao.client;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.compat.modonomicon.client.pages.BookLinkPageRenderer;
import net.fodoth.skina.neoguanniao.config.NeoGuanNiaoConfigScreen;
import net.fodoth.skina.neoguanniao.platform.ModonomiconHooks;
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
        if (ModonomiconHooks.isLoaded()) {
            // 观鸟手册链接页的渲染器（仅客户端）。
            BookLinkPageRenderer.register();
        }
    }
}
