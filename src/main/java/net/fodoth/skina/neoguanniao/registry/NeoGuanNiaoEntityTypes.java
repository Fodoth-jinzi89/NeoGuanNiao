package net.fodoth.skina.neoguanniao.registry;
import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.content.bird.impl.old.budgerigar.BudgerigarEntity;
import net.fodoth.skina.neoguanniao.content.bird.impl.old.columbid.PigeonEntity;
import net.fodoth.skina.neoguanniao.content.bird.impl.old.columbid.SpottedDoveEntity;
import net.fodoth.skina.neoguanniao.content.bird.impl.old.nightheron.NightHeronEntity;
import net.fodoth.skina.neoguanniao.content.bird.impl.old.sparrow.SparrowEntity;
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

    public static final DeferredHolder<EntityType<?>, EntityType<NightHeronEntity>> NIGHT_HERON;
    public static final DeferredHolder<EntityType<?>, EntityType<SparrowEntity>> SPARROW;
    public static final DeferredHolder<EntityType<?>, EntityType<BudgerigarEntity>> BUDGERIGAR;
    public static final DeferredHolder<EntityType<?>, EntityType<SpottedDoveEntity>> SPOTTED_DOVE;
    public static final DeferredHolder<EntityType<?>, EntityType<PigeonEntity>> PIGEON;


    public static final DeferredHolder<EntityType<?>, EntityType<net.fodoth.skina.neoguanniao.content.bird.impl.neo.budgerigar.BudgerigarEntity>> NEO_BUDGERIGAR;

    private NeoGuanNiaoEntityTypes() {
    }

    private static <T extends Mob> DeferredHolder<EntityType<?>, EntityType<T>> registerCreature(
            String id,
            EntityType.EntityFactory<T> factory,
            float width,
            float height
    ) {
        return ENTITY_TYPES.register(id, () ->
                EntityType.Builder.of(factory, MobCategory.CREATURE)
                        .sized(width, height)
                        .clientTrackingRange(8)
                        .build(String.valueOf(ResourceLocation.fromNamespaceAndPath(NeoGuanNiao.MODID, id)))
        );
    }

    static {
        NIGHT_HERON = registerCreature(
                "night_heron",
                NightHeronEntity::new,
                0.8F,
                1.9F
        );

        SPARROW = registerCreature(
                "sparrow",
                SparrowEntity::new,
                0.32F,
                0.38F
        );

        BUDGERIGAR = registerCreature(
                "budgerigar",
                BudgerigarEntity::new,
                0.204F,
                0.252F
        );

        SPOTTED_DOVE = registerCreature(
                "spotted_dove",
                SpottedDoveEntity::new,
                0.42F,
                0.58F
        );

        PIGEON = registerCreature(
                "pigeon",
                PigeonEntity::new,
                0.4F,
                0.54F
        );

        NEO_BUDGERIGAR = registerCreature(
                "neo_budgerigar",
                net.fodoth.skina.neoguanniao.content.bird.impl.neo.budgerigar.BudgerigarEntity::new,
                0.4F,
                0.54F
        );

    }
}