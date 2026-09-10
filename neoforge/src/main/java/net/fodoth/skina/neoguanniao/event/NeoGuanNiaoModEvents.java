package net.fodoth.skina.neoguanniao.event;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.content.bird.core.data.BirdData;
import net.fodoth.skina.neoguanniao.content.bird.core.model.BirdModel;
import net.fodoth.skina.neoguanniao.content.bird.core.skin.BirdSkin;
import net.fodoth.skina.neoguanniao.content.bird.impl.BudgerigarEntity;
import net.fodoth.skina.neoguanniao.content.bird.impl.CockatielEntity;
import net.fodoth.skina.neoguanniao.content.bird.impl.CrowEntity;
import net.fodoth.skina.neoguanniao.content.bird.impl.DoveEntity;
import net.fodoth.skina.neoguanniao.content.bird.impl.LongTailedTitEntity;
import net.fodoth.skina.neoguanniao.content.bird.impl.MacawEntity;
import net.fodoth.skina.neoguanniao.content.bird.impl.NightHeronEntity;
import net.fodoth.skina.neoguanniao.content.bird.impl.PigeonEntity;
import net.fodoth.skina.neoguanniao.content.bird.impl.SeagullEntity;
import net.fodoth.skina.neoguanniao.content.bird.impl.SparrowEntity;
import net.fodoth.skina.neoguanniao.content.bird.impl.KiwiEntity;
import net.fodoth.skina.neoguanniao.content.bird.impl.MynaEntity;
import net.fodoth.skina.neoguanniao.registry.*;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import dev.architectury.registry.registries.RegistrySupplier;


@EventBusSubscriber(
        modid = NeoGuanNiao.MODID
)
public final class NeoGuanNiaoModEvents {

    private NeoGuanNiaoModEvents() {
    }

    @SubscribeEvent
    public static void registerBirdSkins(RegisterEvent event) {

        if (event.getRegistryKey().location().equals(NeoGuanNiao.resource("bird_data"))) {


            for (var holder : java.util.List.of(
                    NeoGuanNiaoBirdData.BUDGERIGAR,
                    NeoGuanNiaoBirdData.NIGHT_HERON,
                    NeoGuanNiaoBirdData.PIGEON,
                    NeoGuanNiaoBirdData.DOVE,
                    NeoGuanNiaoBirdData.SPARROW,
                    NeoGuanNiaoBirdData.COCKATIEL,
                    NeoGuanNiaoBirdData.LONG_TAILED_TIT,
                    NeoGuanNiaoBirdData.MACAW,
                    NeoGuanNiaoBirdData.CROW,
                    NeoGuanNiaoBirdData.SEAGULL,
                    NeoGuanNiaoBirdData.KIWI,
                    NeoGuanNiaoBirdData.MYNA)) {

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

        putAttributes(event, NeoGuanNiaoEntityTypes.NEO_BUDGERIGAR, BudgerigarEntity.createAttributes().build());
        putAttributes(event, NeoGuanNiaoEntityTypes.NEO_NIGHT_HERON, NightHeronEntity.createAttributes().build());
        putAttributes(event, NeoGuanNiaoEntityTypes.NEO_PIGEON, PigeonEntity.createAttributes().build());
        putAttributes(event, NeoGuanNiaoEntityTypes.NEO_DOVE, DoveEntity.createAttributes().build());
        putAttributes(event, NeoGuanNiaoEntityTypes.NEO_SPARROW, SparrowEntity.createAttributes().build());
        putAttributes(event, NeoGuanNiaoEntityTypes.NEO_COCKATIEL, CockatielEntity.createAttributes().build());
        putAttributes(event, NeoGuanNiaoEntityTypes.NEO_LONG_TAILED_TIT, LongTailedTitEntity.createAttributes().build());
        putAttributes(event, NeoGuanNiaoEntityTypes.NEO_MACAW, MacawEntity.createAttributes().build());
        putAttributes(event, NeoGuanNiaoEntityTypes.NEO_CROW, CrowEntity.createAttributes().build());
        putAttributes(event, NeoGuanNiaoEntityTypes.NEO_SEAGULL, SeagullEntity.createAttributes().build());
        putAttributes(event, NeoGuanNiaoEntityTypes.NEO_KIWI, KiwiEntity.createAttributes().build());
        putAttributes(event, NeoGuanNiaoEntityTypes.NEO_MYNA, MynaEntity.createAttributes().build());
    }

    private static <T extends net.minecraft.world.entity.LivingEntity> void putAttributes(
            EntityAttributeCreationEvent event,
            dev.architectury.registry.registries.RegistrySupplier<net.minecraft.world.entity.EntityType<T>> type,
            net.minecraft.world.entity.ai.attributes.AttributeSupplier attributes) {
        if (type.isPresent()) {
            event.put(type.get(), attributes);
        }
    }


    @SubscribeEvent
    public static void onRegisterSpawnPlacements(RegisterSpawnPlacementsEvent event) {

        registerSpawnPlacement(event, NeoGuanNiaoEntityTypes.NEO_BUDGERIGAR, BudgerigarEntity::canSpawn);
        registerSpawnPlacement(event, NeoGuanNiaoEntityTypes.NEO_NIGHT_HERON, NightHeronEntity::canSpawn);
        registerSpawnPlacement(event, NeoGuanNiaoEntityTypes.NEO_PIGEON, PigeonEntity::canSpawn);
        registerSpawnPlacement(event, NeoGuanNiaoEntityTypes.NEO_DOVE, DoveEntity::canSpawn);
        registerSpawnPlacement(event, NeoGuanNiaoEntityTypes.NEO_SPARROW, SparrowEntity::canSpawn);
        registerSpawnPlacement(event, NeoGuanNiaoEntityTypes.NEO_COCKATIEL, CockatielEntity::canSpawn);
        registerSpawnPlacement(event, NeoGuanNiaoEntityTypes.NEO_LONG_TAILED_TIT, LongTailedTitEntity::canSpawn);
        registerSpawnPlacement(event, NeoGuanNiaoEntityTypes.NEO_MACAW, MacawEntity::canSpawn);
        registerSpawnPlacement(event, NeoGuanNiaoEntityTypes.NEO_CROW, CrowEntity::canSpawn);
        registerSpawnPlacement(event, NeoGuanNiaoEntityTypes.NEO_SEAGULL, SeagullEntity::canSpawn);
        registerSpawnPlacement(event, NeoGuanNiaoEntityTypes.NEO_KIWI, KiwiEntity::canSpawn);
        registerSpawnPlacement(event, NeoGuanNiaoEntityTypes.NEO_MYNA, MynaEntity::canSpawn);
    }

    private static <T extends Entity> void registerSpawnPlacement(
            RegisterSpawnPlacementsEvent event,
            RegistrySupplier<EntityType<T>> type,
            SpawnPlacements.SpawnPredicate<T> predicate) {
        if (type.isPresent()) {
            event.register(type.get(), SpawnPlacementTypes.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, predicate,
                    RegisterSpawnPlacementsEvent.Operation.REPLACE);
        }
    }


    @SubscribeEvent
    public static void registerCapabilities(
            RegisterCapabilitiesEvent event
    ) {
        NeoGuanNiaoCapabilities.register(event);
    }



}
