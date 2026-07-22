package net.fodoth.skina.neoguanniao.event;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.content.bird.core.data.BirdData;
import net.fodoth.skina.neoguanniao.content.bird.core.model.BirdModel;
import net.fodoth.skina.neoguanniao.content.bird.core.skin.BirdSkin;
import net.fodoth.skina.neoguanniao.content.bird.impl.old.budgerigar.BudgerigarEntity;
import net.fodoth.skina.neoguanniao.content.bird.impl.old.columbid.PigeonEntity;
import net.fodoth.skina.neoguanniao.content.bird.impl.old.columbid.SpottedDoveEntity;
import net.fodoth.skina.neoguanniao.content.bird.impl.old.nightheron.NightHeronEntity;
import net.fodoth.skina.neoguanniao.content.bird.impl.old.sparrow.SparrowEntity;
import net.fodoth.skina.neoguanniao.registry.*;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;


@EventBusSubscriber(
        modid = NeoGuanNiao.MODID
)
public final class NeoGuanNiaoModEvents {

    private NeoGuanNiaoModEvents() {
    }

    @SubscribeEvent
    public static void registerBirdSkins(RegisterEvent event) {

        if (event.getRegistryKey()
                .equals(NeoGuanNiaoBirdData.BIRD_DATA.getRegistryKey())) {


            for (var holder : NeoGuanNiaoBirdData.BIRD_DATA.getEntries()) {

                BirdData birdData = holder.get();

                for (BirdSkin skin : birdData.model().birdSkin()) {
                    NeoGuanNiaoBirdSkins.register(skin);
                }
                for (BirdModel model : birdData.model().birdModel()) {
                    NeoGuanNiaoBirdModels.register(model);
                }
            }
        }
    }


    @SubscribeEvent
    public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {

        event.put(
                NeoGuanNiaoEntityTypes.NIGHT_HERON.get(),
                NightHeronEntity.createAttributes().build()
        );

        event.put(
                NeoGuanNiaoEntityTypes.SPARROW.get(),
                SparrowEntity.createAttributes().build()
        );

        event.put(
                NeoGuanNiaoEntityTypes.BUDGERIGAR.get(),
                BudgerigarEntity.createAttributes().build()
        );

        event.put(
                NeoGuanNiaoEntityTypes.SPOTTED_DOVE.get(),
                SpottedDoveEntity.createAttributes().build()
        );

        event.put(
                NeoGuanNiaoEntityTypes.PIGEON.get(),
                PigeonEntity.createAttributes().build()
        );


        event.put(
                NeoGuanNiaoEntityTypes.NEO_BUDGERIGAR.get(),
                net.fodoth.skina.neoguanniao.content.bird.impl.neo.budgerigar.BudgerigarEntity.createAttributes().build()
        );

        event.put(
                NeoGuanNiaoEntityTypes.NEO_NIGHT_HERON.get(),
                net.fodoth.skina.neoguanniao.content.bird.impl.neo.night_heron.NightHeronEntity.createAttributes().build()
        );
    }


    @SubscribeEvent
    public static void onRegisterSpawnPlacements(RegisterSpawnPlacementsEvent event) {

        event.register(
                NeoGuanNiaoEntityTypes.NIGHT_HERON.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                NightHeronEntity::canSpawn,
                RegisterSpawnPlacementsEvent.Operation.REPLACE
        );

        event.register(
                NeoGuanNiaoEntityTypes.SPARROW.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SparrowEntity::canSpawn,
                RegisterSpawnPlacementsEvent.Operation.REPLACE
        );

        event.register(
                NeoGuanNiaoEntityTypes.BUDGERIGAR.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                BudgerigarEntity::canSpawn,
                RegisterSpawnPlacementsEvent.Operation.REPLACE
        );

        event.register(
                NeoGuanNiaoEntityTypes.SPOTTED_DOVE.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SpottedDoveEntity::canSpawn,
                RegisterSpawnPlacementsEvent.Operation.REPLACE
        );

        event.register(
                NeoGuanNiaoEntityTypes.PIGEON.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                PigeonEntity::canSpawn,
                RegisterSpawnPlacementsEvent.Operation.REPLACE
        );

        event.register(
                NeoGuanNiaoEntityTypes.NEO_BUDGERIGAR.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                net.fodoth.skina.neoguanniao.content.bird.impl.neo.budgerigar.BudgerigarEntity::canSpawn,
                RegisterSpawnPlacementsEvent.Operation.REPLACE
        );

        event.register(
                NeoGuanNiaoEntityTypes.NEO_NIGHT_HERON.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                net.fodoth.skina.neoguanniao.content.bird.impl.neo.night_heron.NightHeronEntity::canSpawn,
                RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
    }


    @SubscribeEvent
    public static void onBuildCreativeModeTabContents(BuildCreativeModeTabContentsEvent event) {

        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(NeoGuanNiaoItems.NIGHT_HERON_SPAWN_EGG.get());
            event.accept(NeoGuanNiaoItems.SPARROW_SPAWN_EGG.get());
            event.accept(NeoGuanNiaoItems.BUDGERIGAR_SPAWN_EGG.get());
            event.accept(NeoGuanNiaoItems.SPOTTED_DOVE_SPAWN_EGG.get());
            event.accept(NeoGuanNiaoItems.PIGEON_SPAWN_EGG.get());
        }
    }

    @SubscribeEvent
    public static void registerCapabilities(
            RegisterCapabilitiesEvent event
    ) {
        NeoGuanNiaoCapabilities.register(event);
    }


}