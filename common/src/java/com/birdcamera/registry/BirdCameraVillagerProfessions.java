package com.birdcamera.registry;

import com.birdcamera.BirdCameraMod;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;
import java.util.function.Predicate;

public class BirdCameraVillagerProfessions {

    public static final ResourceKey<PoiType> BIRD_KEEPER_POI_KEY =
            ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, BirdCameraMod.id("bird_keeper"));

    public static final VillagerProfession BIRD_KEEPER = createProfession("bird_keeper",
            SoundEvents.VILLAGER_WORK_SHEPHERD);

    private static VillagerProfession createProfession(String id, SoundEvent workSound) {
        ResourceLocation identifier = BirdCameraMod.id(id);

        // Register POI using Fabric API
        Set<BlockState> states = ImmutableSet.copyOf(BirdCameraBlocks.SMALL_BIRD_CAGE.getStateDefinition().getPossibleStates());
        PoiType poiType = new PoiType(states, 1, 1);
        Registry.register(BuiltInRegistries.POINT_OF_INTEREST_TYPE, BIRD_KEEPER_POI_KEY, poiType);

        Holder<PoiType> poiHolder = BuiltInRegistries.POINT_OF_INTEREST_TYPE.getHolderOrThrow(BIRD_KEEPER_POI_KEY);
        Predicate<Holder<PoiType>> poiPredicate = h -> h.is(BIRD_KEEPER_POI_KEY);

        return Registry.register(BuiltInRegistries.VILLAGER_PROFESSION, identifier,
                new VillagerProfession(
                        identifier.toString(),
                        poiPredicate,
                        poiPredicate,
                        ImmutableSet.of(),
                        ImmutableSet.of(),
                        workSound
                ));
    }

    public static void register() {
        BirdCameraMod.LOGGER.info("Registering villager professions...");
    }
}
