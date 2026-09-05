package net.fodoth.skina.neoguanniao.platform;
import net.fodoth.skina.neoguanniao.client.keybind.NeoGuanNiaoClientKeyBindings;
public final class GuideKeyHooksImpl {
    private GuideKeyHooksImpl() {}
    public static String toggleLayoutEdit() { return NeoGuanNiaoClientKeyBindings.TOGGLE_LAYOUT_EDIT.getKey().getDisplayName().getString(); }
    public static String saveLayout() { return NeoGuanNiaoClientKeyBindings.SAVE_LAYOUT.getKey().getDisplayName().getString(); }
    public static String reloadLayout() { return NeoGuanNiaoClientKeyBindings.RELOAD_LAYOUT.getKey().getDisplayName().getString(); }
}
