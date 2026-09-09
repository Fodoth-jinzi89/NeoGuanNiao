package net.fodoth.skina.neoguanniao;

import net.fabricmc.api.ModInitializer;
import net.fodoth.skina.neoguanniao.registry.*;
import net.fodoth.skina.neoguanniao.network.NeoGuanNiaoFabricNetwork;
import net.fabricmc.loader.api.FabricLoader;
import net.fodoth.skina.neoguanniao.config.NeoGuanNiaoFabricConfig;
import java.io.IOException;

public final class NeoGuanNiaoFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        var configPath = FabricLoader.getInstance().getConfigDir().resolve("neoguanniao.properties");
        try {
            NeoGuanNiaoFabricConfig.load(configPath);
            NeoGuanNiaoFabricConfig.save(configPath);
        } catch (IOException | IllegalArgumentException exception) {
            NeoGuanNiao.LOGGER.error("Unable to load Fabric camera configuration; using defaults", exception);
        }
        NeoGuanNiaoDataComponents.DATA_COMPONENTS.register();
        NeoGuanNiaoBlocks.BLOCKS.register();
        NeoGuanNiaoItems.ITEMS.register();
        NeoGuanNiaoSoundEvents.SOUND_EVENTS.register();
        NeoGuanNiaoBirdData.BIRD_DATA.register();
        for (var holder : NeoGuanNiaoBirdData.BIRD_DATA) {
            var model = holder.get().model();
            model.birdSkin().forEach(NeoGuanNiaoBirdSkins::register);
            model.birdModel().forEach(NeoGuanNiaoBirdModels::register);
        }
        NeoGuanNiaoCriteriaTriggers.register();
        NeoGuanNiaoRecipeSerializers.RECIPE_SERIALIZERS.register();
        NeoGuanNiaoEntityTypes.ENTITY_TYPES.register();
        NeoGuanNiaoBlockEntityTypes.BLOCK_ENTITY_TYPES.register();
        NeoGuanNiaoParticleTypes.PARTICLE_TYPES.register();
        NeoGuanNiaoCreativeTabs.CREATIVE_MODE_TABS.register();
        NeoGuanNiaoVillagerProfessions.registerPoi();
        NeoGuanNiaoVillagerProfessions.POI_TYPES_REGISTER.register();
        NeoGuanNiaoVillagerProfessions.PROFESSIONS.register();
        NeoGuanNiaoFabricVillagerTrades.register();
        NeoGuanNiaoFabricSpawns.register();
        NeoGuanNiaoFabricNetwork.register();
        NeoGuanNiaoFabricServerEvents.register();
    }
}
