package net.fodoth.skina.neoguanniao;

import com.mojang.logging.LogUtils;
import net.fodoth.skina.neoguanniao.registry.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(NeoGuanNiao.MODID)
public class NeoGuanNiao {

    public static final String MODID = "neoguanniao";

    public static final Logger LOGGER = LogUtils.getLogger();

    public NeoGuanNiao(IEventBus modEventBus, ModContainer container) {
        NeoGuanNiaoBlocks.BLOCKS.register(modEventBus);
        NeoGuanNiaoBlockEntityTypes.BLOCK_ENTITY_TYPES.register(modEventBus);
        NeoGuanNiaoItems.ITEMS.register(modEventBus);
        NeoGuanNiaoEntityTypes.ENTITY_TYPES.register(modEventBus);
        NeoGuanNiaoRecipeSerializers.RECIPE_SERIALIZERS.register(modEventBus);
        NeoGuanNiaoSoundEvents.SOUND_EVENTS.register(modEventBus);
        NeoGuanNiaoCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        NeoGuanNiaoComponents.DATA_COMPONENTS.register(modEventBus);
    }

}
