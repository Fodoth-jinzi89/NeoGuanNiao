package net.fodoth.skina.neoguanniao;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoEntityTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

final class NeoGuanNiaoFabricSpawns {
    private NeoGuanNiaoFabricSpawns() {}

    static void register() {
        add("budgerigar", NeoGuanNiaoEntityTypes.NEO_BUDGERIGAR.get(), 3, 1, 3);
        add("cockatiel", NeoGuanNiaoEntityTypes.NEO_COCKATIEL.get(), 3, 1, 2);
        add("crow", NeoGuanNiaoEntityTypes.NEO_CROW.get(), 3, 1, 2);
        add("kiwi", NeoGuanNiaoEntityTypes.NEO_KIWI.get(), 3, 1, 2);
        add("long_tailed_tit", NeoGuanNiaoEntityTypes.NEO_LONG_TAILED_TIT.get(), 5, 2, 4);
        add("macaw", NeoGuanNiaoEntityTypes.NEO_MACAW.get(), 2, 1, 2);
        add("myna", NeoGuanNiaoEntityTypes.NEO_MYNA.get(), 4, 1, 3);
        add("night_heron", NeoGuanNiaoEntityTypes.NEO_NIGHT_HERON.get(), 3, 1, 1);
        add("pigeon", NeoGuanNiaoEntityTypes.NEO_PIGEON.get(), 3, 1, 3);
        add("seagull", NeoGuanNiaoEntityTypes.NEO_SEAGULL.get(), 4, 1, 3);
        add("sparrow", NeoGuanNiaoEntityTypes.NEO_SPARROW.get(), 3, 2, 4);
        add("spotted_dove", NeoGuanNiaoEntityTypes.NEO_DOVE.get(), 3, 1, 2);
    }

    private static <T extends Entity> void add(String habitat, EntityType<T> type, int weight, int min, int max) {
        BiomeModifications.addSpawn(BiomeSelectors.tag(net.minecraft.tags.TagKey.create(Registries.BIOME,
                ResourceLocation.fromNamespaceAndPath(NeoGuanNiao.MODID, habitat + "_habitat"))),
                MobCategory.CREATURE, type, weight, min, max);
    }
}
