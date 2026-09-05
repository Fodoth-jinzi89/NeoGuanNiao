package net.fodoth.skina.neoguanniao.content.fan;

import net.minecraft.world.item.ItemStack;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoDataComponents;

public final class FeatherFanEnchantments {
    private FeatherFanEnchantments() {
    }

    public static boolean hasBurialPlume(ItemStack s) {
        return mode(s) == 0;
    }

    public static boolean hasRivenPlume(ItemStack s) {
        return mode(s) == 1;
    }

    public static boolean hasHuntingReturn(ItemStack s) {
        return mode(s) == 2;
    }

    public static String modeName(ItemStack s) {
        return switch (mode(s)) { case 1 -> "riven"; case 2 -> "hunting"; default -> "burial"; };
    }

    public static int mode(ItemStack s) {
        return Math.clamp(s.getOrDefault(NeoGuanNiaoDataComponents.FEATHER_FAN_MODE.get(), 0), 0, 2);
    }
}
