package net.fodoth.skina.neoguanniao.registry;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
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
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NeoGuanNiaoEntityTypes {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, NeoGuanNiao.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<BudgerigarEntity>> NEO_BUDGERIGAR;
    public static final DeferredHolder<EntityType<?>, EntityType<NightHeronEntity>> NEO_NIGHT_HERON;
    public static final DeferredHolder<EntityType<?>, EntityType<PigeonEntity>> NEO_PIGEON;
    public static final DeferredHolder<EntityType<?>, EntityType<DoveEntity>> NEO_DOVE;
    public static final DeferredHolder<EntityType<?>, EntityType<SparrowEntity>> NEO_SPARROW;
    public static final DeferredHolder<EntityType<?>, EntityType<CockatielEntity>> NEO_COCKATIEL;
    public static final DeferredHolder<EntityType<?>, EntityType<LongTailedTitEntity>> NEO_LONG_TAILED_TIT;
    public static final DeferredHolder<EntityType<?>, EntityType<MacawEntity>> NEO_MACAW;
    public static final DeferredHolder<EntityType<?>, EntityType<CrowEntity>> NEO_CROW;
    public static final DeferredHolder<EntityType<?>, EntityType<SeagullEntity>> NEO_SEAGULL;

    private NeoGuanNiaoEntityTypes() {
    }

    private static <T extends Mob> DeferredHolder<EntityType<?>, EntityType<T>> registerCreature(
            String id, EntityType.EntityFactory<T> factory, float width, float height) {
        return ENTITY_TYPES.register(id, () -> EntityType.Builder.of(factory, MobCategory.CREATURE)
                .sized(width, height)
                .clientTrackingRange(8)
                .build(String.valueOf(ResourceLocation.fromNamespaceAndPath(NeoGuanNiao.MODID, id))));
    }

    static {
        NEO_BUDGERIGAR = registerCreature("neo_budgerigar", BudgerigarEntity::new, 0.4F, 0.54F);
        NEO_NIGHT_HERON = registerCreature("neo_night_heron", NightHeronEntity::new, 0.8F, 0.9F);
        NEO_PIGEON = registerCreature("neo_pigeon", PigeonEntity::new, 0.4F, 0.54F);
        NEO_DOVE = registerCreature("neo_dove", DoveEntity::new, 0.4F, 0.54F);
        NEO_SPARROW = registerCreature("neo_sparrow", SparrowEntity::new, 0.32F, 0.38F);
        NEO_COCKATIEL = registerCreature("neo_cockatiel", CockatielEntity::new, 0.4F, 0.54F);
        NEO_LONG_TAILED_TIT = registerCreature("neo_long_tailed_tit", LongTailedTitEntity::new, 0.34F, 0.36F);
        NEO_MACAW = registerCreature("neo_macaw", MacawEntity::new, 0.68F, 0.92F);
        NEO_CROW = registerCreature("neo_crow", CrowEntity::new, 0.52F, 0.72F);
        NEO_SEAGULL = registerCreature("neo_seagull", SeagullEntity::new, 0.56F, 0.72F);
    }
}
