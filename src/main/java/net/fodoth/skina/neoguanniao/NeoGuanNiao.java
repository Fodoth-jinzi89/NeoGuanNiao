package net.fodoth.skina.neoguanniao;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(NeoGuanNiao.MODID)
public class NeoGuanNiao {

    public static final String MODID = "neoguanniao";

    public static final Logger LOGGER = LogUtils.getLogger();

    public NeoGuanNiao(IEventBus modEventBus, ModContainer container) {
    }

}
