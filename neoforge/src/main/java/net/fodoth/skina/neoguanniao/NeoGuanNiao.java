package net.fodoth.skina.neoguanniao;

import com.mojang.logging.LogUtils;
import net.fodoth.skina.neoguanniao.config.NeoGuanNiaoClientConfig;
import net.fodoth.skina.neoguanniao.config.NeoGuanNiaoCommonConfig;
import net.fodoth.skina.neoguanniao.registry.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.common.Mod;
import net.fodoth.skina.neoguanniao.client.fan.FeatherFanParticleProviders;
import org.slf4j.Logger;

@Mod(NeoGuanNiao.MODID)
public class NeoGuanNiao {

    public static final String MODID = "neoguanniao";

    public static final Logger LOGGER = LogUtils.getLogger();

    public NeoGuanNiao(IEventBus modEventBus, ModContainer container) {
        NeoGuanNiaoBlocks.BLOCKS.register();
        NeoGuanNiaoBlockEntityTypes.BLOCK_ENTITY_TYPES.register();
        NeoGuanNiaoItems.ITEMS.register();
        NeoGuanNiaoItemTags.register();
        modEventBus.addListener(FeatherFanParticleProviders::register);
        NeoGuanNiaoEntityTypes.ENTITY_TYPES.register();
        NeoGuanNiaoRecipeSerializers.RECIPE_SERIALIZERS.register();
        NeoGuanNiaoSoundEvents.SOUND_EVENTS.register();
        NeoGuanNiaoCreativeTabs.CREATIVE_MODE_TABS.register();
        NeoGuanNiaoBirdData.BIRD_DATA.register(modEventBus);
        NeoGuanNiaoDataComponents.DATA_COMPONENTS.register(modEventBus);
        NeoGuanNiaoParticleTypes.PARTICLE_TYPES.register(modEventBus);
        NeoGuanNiaoVillagerProfessions.PROFESSIONS.register(modEventBus);
        NeoGuanNiaoCriteriaTriggers.register();
        container.registerConfig(ModConfig.Type.COMMON, NeoGuanNiaoCommonConfig.SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, NeoGuanNiaoClientConfig.SPEC);
    }

    public static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(NeoGuanNiao.MODID, path);
    }

}
