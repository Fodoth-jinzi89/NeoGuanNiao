package net.fodoth.skina.neoguanniao.event;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.content.bird.impl.old.budgerigar.BudgerigarEntity;
import net.fodoth.skina.neoguanniao.content.bird.impl.old.columbid.PigeonEntity;
import net.fodoth.skina.neoguanniao.content.bird.impl.old.columbid.SpottedDoveEntity;
import net.fodoth.skina.neoguanniao.content.bird.impl.old.nightheron.NightHeronEntity;
import net.fodoth.skina.neoguanniao.content.bird.impl.old.sparrow.SparrowEntity;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoCapabilities;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoEntityTypes;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoItems;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;


@EventBusSubscriber(
        modid = NeoGuanNiao.MODID
)
public final class NeoGuanNiaoModEvents {

    private NeoGuanNiaoModEvents() {
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