package net.fodoth.skina.neoguanniao.registry;

import com.google.common.collect.ImmutableSet;
import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.block.Block;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.architectury.registry.registries.DeferredRegister;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;


public class NeoGuanNiaoVillagerProfessions {

    private static final Map<String, ProfessionPoiType> POI_TYPES =
            new HashMap<>();


    public static final DeferredRegister<VillagerProfession> PROFESSIONS =
            DeferredRegister.create(NeoGuanNiao.MODID, Registries.VILLAGER_PROFESSION);

    public static final DeferredRegister<PoiType> POI_TYPES_REGISTER =
            DeferredRegister.create(NeoGuanNiao.MODID, Registries.POINT_OF_INTEREST_TYPE);

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
                            holder.value() == POI_TYPES.get(name).poiType;


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



    public static void registerPoi() {
        for (var entry : POI_TYPES.entrySet()) {
            String name = entry.getKey();
            POI_TYPES_REGISTER.register(name, () -> {
                Block block = entry.getValue().block.get();
                PoiType poi = new PoiType(
                        ImmutableSet.copyOf(block.getStateDefinition().getPossibleStates()),
                        1,
                        1
                );
                entry.getValue().poiType = poi;
                return poi;
            });
        }
    }



    private static class ProfessionPoiType {

        private final Supplier<Block> block;

        private PoiType poiType;


        private ProfessionPoiType(
                Supplier<Block> block
        ) {
            this.block = block;
        }
    }
}

