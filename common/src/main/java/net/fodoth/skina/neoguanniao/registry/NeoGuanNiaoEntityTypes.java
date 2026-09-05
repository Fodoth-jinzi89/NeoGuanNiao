package net.fodoth.skina.neoguanniao.registry;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.content.bird.impl.BudgerigarEntity;
import net.fodoth.skina.neoguanniao.content.bird.impl.CockatielEntity;
import net.fodoth.skina.neoguanniao.content.bird.impl.CrowEntity;
import net.fodoth.skina.neoguanniao.content.bird.impl.DoveEntity;
import net.fodoth.skina.neoguanniao.content.bird.impl.LongTailedTitEntity;
import net.fodoth.skina.neoguanniao.content.bird.impl.MacawEntity;
import net.fodoth.skina.neoguanniao.content.bird.impl.KiwiEntity;
import net.fodoth.skina.neoguanniao.content.bird.impl.MynaEntity;
import net.fodoth.skina.neoguanniao.content.bird.impl.NightHeronEntity;
import net.fodoth.skina.neoguanniao.content.bird.impl.PigeonEntity;
import net.fodoth.skina.neoguanniao.content.bird.impl.SeagullEntity;
import net.fodoth.skina.neoguanniao.content.bird.impl.SparrowEntity;
import net.fodoth.skina.neoguanniao.content.camera.PhotographEntity;
import net.fodoth.skina.neoguanniao.content.fan.FeatherFanProjectileEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.architectury.registry.registries.DeferredRegister;

public final class NeoGuanNiaoEntityTypes {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(NeoGuanNiao.MODID, Registries.ENTITY_TYPE);

    public static final RegistrySupplier<EntityType<BudgerigarEntity>> NEO_BUDGERIGAR;
    public static final RegistrySupplier<EntityType<NightHeronEntity>> NEO_NIGHT_HERON;
    public static final RegistrySupplier<EntityType<PigeonEntity>> NEO_PIGEON;
    public static final RegistrySupplier<EntityType<DoveEntity>> NEO_DOVE;
    public static final RegistrySupplier<EntityType<SparrowEntity>> NEO_SPARROW;
    public static final RegistrySupplier<EntityType<CockatielEntity>> NEO_COCKATIEL;
    public static final RegistrySupplier<EntityType<LongTailedTitEntity>> NEO_LONG_TAILED_TIT;
    public static final RegistrySupplier<EntityType<MacawEntity>> NEO_MACAW;
    public static final RegistrySupplier<EntityType<CrowEntity>> NEO_CROW;
    public static final RegistrySupplier<EntityType<SeagullEntity>> NEO_SEAGULL;
    public static final RegistrySupplier<EntityType<KiwiEntity>> NEO_KIWI;
    public static final RegistrySupplier<EntityType<MynaEntity>> NEO_MYNA;
    public static final RegistrySupplier<EntityType<PhotographEntity>> PHOTOGRAPH;
    public static final RegistrySupplier<EntityType<FeatherFanProjectileEntity>> FEATHER_FAN_PROJECTILE;

    private NeoGuanNiaoEntityTypes() {
    }

    private static <T extends Mob> RegistrySupplier<EntityType<T>> registerCreature(
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
        NEO_SPARROW = registerCreature("neo_sparrow", SparrowEntity::new, 0.384F, 0.456F);
        NEO_COCKATIEL = registerCreature("neo_cockatiel", CockatielEntity::new, 0.52F, 0.702F);
        NEO_LONG_TAILED_TIT = registerCreature("neo_long_tailed_tit", LongTailedTitEntity::new, 0.3072F, 0.3648F);
        NEO_MACAW = registerCreature("neo_macaw", MacawEntity::new, 0.7072F, 0.9568F);
        NEO_CROW = registerCreature("neo_crow", CrowEntity::new, 0.416F, 0.576F);
        NEO_SEAGULL = registerCreature("neo_seagull", SeagullEntity::new, 0.72F, 0.81F);
        NEO_KIWI = registerCreature("neo_kiwi", KiwiEntity::new, 0.58F, 0.7F);
        NEO_MYNA = registerCreature("neo_myna", MynaEntity::new, 0.416F, 0.576F);
        PHOTOGRAPH = ENTITY_TYPES.register("photograph", () -> EntityType.Builder
                .<PhotographEntity>of(PhotographEntity::new, MobCategory.MISC)
                .sized(0.5F, 0.5F)
                .clientTrackingRange(10)
                .updateInterval(Integer.MAX_VALUE)
                .build(NeoGuanNiao.resource("photograph").toString()));
        FEATHER_FAN_PROJECTILE = ENTITY_TYPES.register("feather_fan_projectile", () -> EntityType.Builder
                .<FeatherFanProjectileEntity>of(FeatherFanProjectileEntity::new, MobCategory.MISC)
                .sized(0.25F, 0.25F)
                .clientTrackingRange(8)
                .updateInterval(1)
                .build(NeoGuanNiao.resource("feather_fan_projectile").toString()));
    }
}

