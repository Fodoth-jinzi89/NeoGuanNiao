package net.fodoth.skina.neoguanniao;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fodoth.skina.neoguanniao.content.bird.impl.BudgerigarEntity;
import net.fodoth.skina.neoguanniao.content.bird.impl.CockatielEntity;
import net.fodoth.skina.neoguanniao.content.bird.impl.CrowEntity;
import net.fodoth.skina.neoguanniao.content.bird.impl.KiwiEntity;
import net.fodoth.skina.neoguanniao.content.bird.impl.LongTailedTitEntity;
import net.fodoth.skina.neoguanniao.content.bird.impl.MacawEntity;
import net.fodoth.skina.neoguanniao.content.bird.impl.MynaEntity;
import net.fodoth.skina.neoguanniao.content.bird.impl.NightHeronEntity;
import net.fodoth.skina.neoguanniao.content.bird.impl.PigeonEntity;
import net.fodoth.skina.neoguanniao.content.bird.impl.SeagullEntity;
import net.fodoth.skina.neoguanniao.content.bird.impl.SparrowEntity;
import net.fodoth.skina.neoguanniao.content.bird.impl.DoveEntity;
import net.fodoth.skina.neoguanniao.content.bird.impl.WoodcockEntity;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoEntityTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.levelgen.Heightmap;

final class NeoGuanNiaoFabricSpawns {
    private NeoGuanNiaoFabricSpawns() {}

    static void register() {
        add("budgerigar", NeoGuanNiaoEntityTypes.NEO_BUDGERIGAR.get(), 3, 1, 3, BudgerigarEntity.createAttributes(), BudgerigarEntity::canSpawn);
        add("cockatiel", NeoGuanNiaoEntityTypes.NEO_COCKATIEL.get(), 3, 1, 2, CockatielEntity.createAttributes(), CockatielEntity::canSpawn);
        add("crow", NeoGuanNiaoEntityTypes.NEO_CROW.get(), 3, 1, 2, CrowEntity.createAttributes(), CrowEntity::canSpawn);
        add("kiwi", NeoGuanNiaoEntityTypes.NEO_KIWI.get(), 3, 1, 2, KiwiEntity.createAttributes(), KiwiEntity::canSpawn);
        add("long_tailed_tit", NeoGuanNiaoEntityTypes.NEO_LONG_TAILED_TIT.get(), 5, 2, 4, LongTailedTitEntity.createAttributes(), LongTailedTitEntity::canSpawn);
        add("macaw", NeoGuanNiaoEntityTypes.NEO_MACAW.get(), 2, 1, 2, MacawEntity.createAttributes(), MacawEntity::canSpawn);
        add("myna", NeoGuanNiaoEntityTypes.NEO_MYNA.get(), 4, 1, 3, MynaEntity.createAttributes(), MynaEntity::canSpawn);
        add("night_heron", NeoGuanNiaoEntityTypes.NEO_NIGHT_HERON.get(), 3, 1, 1, NightHeronEntity.createAttributes(), NightHeronEntity::canSpawn);
        add("pigeon", NeoGuanNiaoEntityTypes.NEO_PIGEON.get(), 3, 1, 3, PigeonEntity.createAttributes(), PigeonEntity::canSpawn);
        add("seagull", NeoGuanNiaoEntityTypes.NEO_SEAGULL.get(), 4, 1, 3, SeagullEntity.createAttributes(), SeagullEntity::canSpawn);
        add("sparrow", NeoGuanNiaoEntityTypes.NEO_SPARROW.get(), 3, 2, 4, SparrowEntity.createAttributes(), SparrowEntity::canSpawn);
        add("spotted_dove", NeoGuanNiaoEntityTypes.NEO_DOVE.get(), 3, 1, 2, DoveEntity.createAttributes(), DoveEntity::canSpawn);
        add("woodcock", NeoGuanNiaoEntityTypes.NEO_WOODCOCK.get(), 3, 1, 2, WoodcockEntity.createAttributes(), WoodcockEntity::canSpawn);
    }

    private static <T extends Mob> void add(String habitat, EntityType<T> type, int weight, int min, int max,
                                                     AttributeSupplier.Builder attributes, SpawnPlacements.SpawnPredicate<T> predicate) {
        FabricDefaultAttributeRegistry.register(type, attributes);
        SpawnPlacements.register(type, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, predicate);
        BiomeModifications.addSpawn(BiomeSelectors.tag(TagKey.create(Registries.BIOME,
                ResourceLocation.fromNamespaceAndPath(NeoGuanNiao.MODID, habitat + "_habitat"))),
                MobCategory.CREATURE, type, weight, min, max);
    }
}
