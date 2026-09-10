package net.fodoth.skina.neoguanniao.platform.fabric;

import net.fodoth.skina.neoguanniao.client.NeoGuanNiaoFabricKeyBindings;

public final class GuideKeyHooksImpl {
    private GuideKeyHooksImpl() {
    }

    public static String toggleLayoutEdit() {
        return NeoGuanNiaoFabricKeyBindings.TOGGLE_LAYOUT_EDIT.getTranslatedKeyMessage().getString();
    }

    public static String saveLayout() {
        return NeoGuanNiaoFabricKeyBindings.SAVE_LAYOUT.getTranslatedKeyMessage().getString();
    }

    public static String reloadLayout() {
        return NeoGuanNiaoFabricKeyBindings.RELOAD_LAYOUT.getTranslatedKeyMessage().getString();
    }
}
