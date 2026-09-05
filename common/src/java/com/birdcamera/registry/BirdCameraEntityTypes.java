package com.birdcamera.registry;

import com.birdcamera.BirdCameraMod;
import com.birdcamera.content.bird.impl.*;
import com.birdcamera.content.camera.PhotographEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;

public class BirdCameraEntityTypes {

    // 墙上相框相片（相机系统，迁移自 guaniao-2.1.3）
    public static final EntityType<PhotographEntity> PHOTOGRAPH = register("photograph",
            EntityType.Builder.<PhotographEntity>of(PhotographEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F).noSummon().build());

    // 虎皮鹦鹉
    public static final EntityType<BudgerigarEntity> NEO_BUDGERIGAR = register("neo_budgerigar",
            EntityType.Builder.of(BudgerigarEntity::new, MobCategory.CREATURE)
                    .sized(0.4F, 0.54F).build());

    // 夜鹭
    public static final EntityType<NightHeronEntity> NEO_NIGHT_HERON = register("neo_night_heron",
            EntityType.Builder.of(NightHeronEntity::new, MobCategory.CREATURE)
                    .sized(0.8F, 0.9F).build());

    // 鸽子
    public static final EntityType<PigeonEntity> NEO_PIGEON = register("neo_pigeon",
            EntityType.Builder.of(PigeonEntity::new, MobCategory.CREATURE)
                    .sized(0.4F, 0.54F).build());

    // 斑鸠
    public static final EntityType<DoveEntity> NEO_DOVE = register("neo_dove",
            EntityType.Builder.of(DoveEntity::new, MobCategory.CREATURE)
                    .sized(0.4F, 0.54F).build());

    // 麻雀
    public static final EntityType<SparrowEntity> NEO_SPARROW = register("neo_sparrow",
            EntityType.Builder.of(SparrowEntity::new, MobCategory.CREATURE)
                    .sized(0.384F, 0.456F).build());

    // 玄凤鹦鹉
    public static final EntityType<CockatielEntity> NEO_COCKATIEL = register("neo_cockatiel",
            EntityType.Builder.of(CockatielEntity::new, MobCategory.CREATURE)
                    .sized(0.52F, 0.702F).build());

    // 长尾山雀
    public static final EntityType<LongTailedTitEntity> NEO_LONG_TAILED_TIT = register("neo_long_tailed_tit",
            EntityType.Builder.of(LongTailedTitEntity::new, MobCategory.CREATURE)
                    .sized(0.3072F, 0.3648F).build());

    // 金刚鹦鹉
    public static final EntityType<MacawEntity> NEO_MACAW = register("neo_macaw",
            EntityType.Builder.of(MacawEntity::new, MobCategory.CREATURE)
                    .sized(0.7072F, 0.9568F).build());

    // 乌鸦
    public static final EntityType<CrowEntity> NEO_CROW = register("neo_crow",
            EntityType.Builder.of(CrowEntity::new, MobCategory.CREATURE)
                    .sized(0.416F, 0.576F).build());

    // 海鸥
    public static final EntityType<SeagullEntity> NEO_SEAGULL = register("neo_seagull",
            EntityType.Builder.of(SeagullEntity::new, MobCategory.CREATURE)
                    .sized(0.72F, 0.81F).build());

    private static <T extends Entity> EntityType<T> register(String id, EntityType<T> type) {
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, BirdCameraMod.id(id), type);
    }

    public static void register() {
        BirdCameraMod.LOGGER.info("注册实体类型...");
    }
}
