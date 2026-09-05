package net.fodoth.skina.neoguanniao.registry;

import dev.architectury.registry.registries.RegistrarManager;
import net.fodoth.skina.neoguanniao.NeoGuanNiao;

/** Shared Architectury registry access; platform modules initialize their registries. */
public final class NeoGuanNiaoRegistrar {
    public static final RegistrarManager MANAGER = RegistrarManager.get(NeoGuanNiao.MODID);

    private NeoGuanNiaoRegistrar() {}

}
