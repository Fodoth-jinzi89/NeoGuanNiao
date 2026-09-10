package net.fodoth.skina.neoguanniao;

import net.minecraft.resources.ResourceLocation;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

public final class NeoGuanNiao {
    public static final String MODID = "neoguanniao";
    public static final Logger LOGGER = LogUtils.getLogger();
    private NeoGuanNiao() {}
    public static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
