package net.fodoth.skina.neoguanniao.registry;

import com.google.common.collect.ImmutableSet;
import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.architectury.registry.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;


@EventBusSubscriber(
        modid = NeoGuanNiao.MODID
)
public class NeoGuanNiaoVillagerProfessions {

    private static final Map<String, ProfessionPoiType> POI_TYPES =
            new HashMap<>();


    public static final DeferredRegister<VillagerProfession> PROFESSIONS =
            DeferredRegister.create(NeoGuanNiao.MODID, Registries.VILLAGER_PROFESSION);

    public static final RegistrySupplier<VillagerProfession>
            BIRD_KEEPER;

    static {
        BIRD_KEEPER =
                registerProfession(
                        "bird_keeper",
                        NeoGuanNiaoBlocks.BIRD_NEST,
                        () -> SoundEvents.NOTE_BLOCK_CHIME
                );
    }


    @SuppressWarnings("SameParameterValue")
    private static RegistrySupplier<VillagerProfession>
    registerProfession(
            String name,
            Supplier<Block> block,
            Supplier<Holder<SoundEvent>> sound
    ) {

        POI_TYPES.put(
                name,
                new ProfessionPoiType(block)
        );


        return PROFESSIONS.register(name, () -> {

            Predicate<Holder<PoiType>> predicate =
                    holder ->
                            POI_TYPES.get(name).poiType != null
                                    &&
                                    holder.value()
                                            ==
                                            POI_TYPES.get(name)
                                                    .poiType
                                                    .value();


            return new VillagerProfession(
                    ResourceLocation.fromNamespaceAndPath(
                            NeoGuanNiao.MODID,
                            name
                    ).toString(),

                    predicate,
                    predicate,

                    ImmutableSet.of(),
                    ImmutableSet.of(),

                    sound.get().value()
            );
        });
    }



    /**
     * 注册职业工作站 POI
     */
    @SubscribeEvent
    public static void registerPoi(RegisterEvent event) {

        event.register(
                Registries.POINT_OF_INTEREST_TYPE,
                helper -> {

                    for (var entry : POI_TYPES.entrySet()) {

                        String name = entry.getKey();

                        Block block =
                                entry.getValue()
                                        .block
                                        .get();


                        Optional<Holder<PoiType>> existing =
                                PoiTypes.forState(
                                        block.defaultBlockState()
                                );


                        if (existing.isPresent()) {

                            NeoGuanNiao.LOGGER.error(
                                    "Skipping villager profession {} because block {} already has POI",
                                    name,
                                    block
                            );

                            continue;
                        }


                        PoiType poi =
                                new PoiType(
                                        ImmutableSet.copyOf(
                                                block.getStateDefinition()
                                                        .getPossibleStates()
                                        ),
                                        1,
                                        1
                                );


                        helper.register(
                                ResourceLocation.fromNamespaceAndPath(
                                        NeoGuanNiao.MODID,
                                        name
                                ),
                                poi
                        );


                        entry.getValue().poiType =
                                BuiltInRegistries
                                        .POINT_OF_INTEREST_TYPE
                                        .wrapAsHolder(poi);
                    }
                }
        );
    }



    private static class ProfessionPoiType {

        private final Supplier<Block> block;

        private Holder<PoiType> poiType;


        private ProfessionPoiType(
                Supplier<Block> block
        ) {
            this.block = block;
        }
    }
}

