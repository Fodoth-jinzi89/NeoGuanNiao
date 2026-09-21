package net.fodoth.skina.neoguanniao;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fodoth.skina.neoguanniao.content.cage.BirdCageBlock;
import net.fodoth.skina.neoguanniao.content.cage.BirdCageBlockEntity;
import net.fodoth.skina.neoguanniao.platform.fabric.BirdCageFluidStorage;
import net.fodoth.skina.neoguanniao.platform.fabric.BirdCageItemStorage;
import net.fodoth.skina.neoguanniao.registry.*;
import net.minecraft.world.level.block.Block;
import net.fodoth.skina.neoguanniao.network.NeoGuanNiaoFabricNetwork;
import net.fabricmc.loader.api.FabricLoader;
import net.fodoth.skina.neoguanniao.compat.modonomicon.pages.BookLinkPage;
import net.fodoth.skina.neoguanniao.config.NeoGuanNiaoFabricConfig;
import net.fodoth.skina.neoguanniao.platform.ModonomiconHooks;
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
        registerCageStorages();
        NeoGuanNiaoVillagerProfessions.PROFESSIONS.register();
        NeoGuanNiaoFabricVillagerTrades.register();
        NeoGuanNiaoFabricSpawns.register();
        NeoGuanNiaoFabricNetwork.register();
        NeoGuanNiaoFabricServerEvents.register();
        if (ModonomiconHooks.isLoaded()) {
            // Modonomicon 是可选依赖：装了才注册观鸟手册的自定义页面类型。
            BookLinkPage.register();
        }
    }

    // 漏斗/自动化：把鸟巢暴露为 fabric-transfer 的物品存储（对应 NeoForge 的 Capabilities.ItemHandler.BLOCK）。
    private static void registerNestItemStorage() {
        ItemStorage.SIDED.registerForBlockEntity(InventoryStorage::of,
                NeoGuanNiaoBlockEntityTypes.BIRD_NEST.get());
    }

    // 漏斗/管道：把鸟笼暴露为 fabric-transfer 的物品/流体存储（对应 NeoForge 的 Capabilities）。三种规格都参与，
    // 而且注册在方块上：中/大型鸟笼的占位方块没有方块实体，接在占位方块上的漏斗也要查得到，这里统一回到原点。
    private static void registerCageStorages() {
        Block[] cageBlocks = {
                NeoGuanNiaoBlocks.SMALL_BIRD_CAGE.get(),
                NeoGuanNiaoBlocks.MEDIUM_BIRD_CAGE.get(),
                NeoGuanNiaoBlocks.LARGE_BIRD_CAGE.get()
        };
        ItemStorage.SIDED.registerForBlocks(
                (level, pos, state, blockEntity, direction) -> {
                    BirdCageBlockEntity cage = BirdCageBlock.cageAt(level, pos, state);
                    return cage != null && cage.supportsCageStorage() ? new BirdCageItemStorage(cage) : null;
                },
                cageBlocks
        );
        FluidStorage.SIDED.registerForBlocks(
                (level, pos, state, blockEntity, direction) -> {
                    BirdCageBlockEntity cage = BirdCageBlock.cageAt(level, pos, state);
                    return cage != null && cage.supportsCageStorage() ? new BirdCageFluidStorage(cage) : null;
                },
                cageBlocks
        );
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
