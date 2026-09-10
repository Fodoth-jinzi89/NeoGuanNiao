package net.fodoth.skina.neoguanniao;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fodoth.skina.neoguanniao.registry.*;
import net.fodoth.skina.neoguanniao.network.NeoGuanNiaoFabricNetwork;
import net.fabricmc.loader.api.FabricLoader;
import net.fodoth.skina.neoguanniao.config.NeoGuanNiaoFabricConfig;
import java.io.IOException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import java.lang.reflect.Method;
import java.util.Set;

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
        registerPoiBlockStates();
        registerNestItemStorage();
        NeoGuanNiaoVillagerProfessions.PROFESSIONS.register();
        NeoGuanNiaoFabricVillagerTrades.register();
        NeoGuanNiaoFabricSpawns.register();
        NeoGuanNiaoFabricNetwork.register();
        NeoGuanNiaoFabricServerEvents.register();
    }

    // 漏斗/自动化：把鸟巢暴露为 fabric-transfer 的物品存储（对应 NeoForge 的 Capabilities.ItemHandler.BLOCK）。
    private static void registerNestItemStorage() {
        ItemStorage.SIDED.registerForBlockEntity(InventoryStorage::of,
                NeoGuanNiaoBlockEntityTypes.BIRD_NEST.get());
    }

    private static void registerPoiBlockStates() {
        try {
            Method method = PoiTypes.class.getDeclaredMethod("registerBlockStates", Holder.class, Set.class);
            method.setAccessible(true);
            var holder = BuiltInRegistries.POINT_OF_INTEREST_TYPE.getHolderOrThrow(
                    ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE,
                            NeoGuanNiao.resource("bird_keeper")));
            Set<BlockState> states = Set.copyOf(NeoGuanNiaoBlocks.BIRD_NEST.get().getStateDefinition().getPossibleStates());
            method.invoke(null, holder, (Object) states);
        } catch (ReflectiveOperationException exception) {
            NeoGuanNiao.LOGGER.error("Unable to register bird nest POI block states", exception);
        }
    }
}
