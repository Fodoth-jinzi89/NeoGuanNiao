package net.fodoth.skina.neoguanniao;

import com.mojang.logging.LogUtils;
import net.fodoth.skina.neoguanniao.config.NeoGuanNiaoNeoForgeClientConfig;
import net.fodoth.skina.neoguanniao.config.NeoGuanNiaoNeoForgeCommonConfig;
import net.fodoth.skina.neoguanniao.registry.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.common.Mod;
import net.fodoth.skina.neoguanniao.client.fan.FeatherFanParticleProviders;
import org.slf4j.Logger;

@Mod(NeoGuanNiao.MODID)
public class NeoGuanNiaoNeoForge {

    public static final String MODID = NeoGuanNiao.MODID;

    public static final Logger LOGGER = LogUtils.getLogger();

    public NeoGuanNiaoNeoForge(IEventBus modEventBus, ModContainer container) {
        LOGGER.info("NeoGuanNiao constructor: registering Architectury registries");
        NeoGuanNiaoBlocks.BLOCKS.register();
        NeoGuanNiaoBlockEntityTypes.BLOCK_ENTITY_TYPES.register();
        NeoGuanNiaoItems.ITEMS.register();
        NeoGuanNiaoItemTags.register();
        modEventBus.addListener(FeatherFanParticleProviders::register);
        NeoGuanNiaoEntityTypes.ENTITY_TYPES.register();
        NeoGuanNiaoRecipeSerializers.RECIPE_SERIALIZERS.register();
        NeoGuanNiaoSoundEvents.SOUND_EVENTS.register();
        NeoGuanNiaoCreativeTabs.CREATIVE_MODE_TABS.register();
        NeoGuanNiaoBirdData.BIRD_DATA.register();
        NeoGuanNiaoDataComponents.DATA_COMPONENTS.register();
        NeoGuanNiaoParticleTypes.PARTICLE_TYPES.register();
        NeoGuanNiaoVillagerProfessions.registerPoi();
        NeoGuanNiaoVillagerProfessions.POI_TYPES_REGISTER.register();
        NeoGuanNiaoVillagerProfessions.PROFESSIONS.register();
        NeoGuanNiaoCriteriaTriggers.register();
        LOGGER.info("NeoGuanNiao Architectury registries registered");
        container.registerConfig(ModConfig.Type.COMMON, NeoGuanNiaoNeoForgeCommonConfig.SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, NeoGuanNiaoNeoForgeClientConfig.SPEC);
    }

    public static ResourceLocation resource(String path) {
        return NeoGuanNiao.resource(path);
    }
}
