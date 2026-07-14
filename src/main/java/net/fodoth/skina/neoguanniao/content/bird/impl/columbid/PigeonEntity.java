package net.fodoth.skina.neoguanniao.content.bird.impl.columbid;

import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoEntityTypes;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 鸽子实体类
 * 继承自 AbstractColumbidEntity，实现鸽子特有的行为
 *
 * <p>鸽子是高度适应城市环境的鸟类，具有两种颜色变体：灰色和白色
 */
public class PigeonEntity extends AbstractColumbidEntity {

    private static final EntityDataAccessor<Integer> VARIANT;

    public PigeonEntity(EntityType<? extends PigeonEntity> entityType, Level level) {
        super(entityType, level, PigeonProfile.INSTANCE);
    }

    // ============ 属性 ============

    public static AttributeSupplier.Builder createAttributes() {
        return createColumbidAttributes(
                PigeonDefinition.MAX_HEALTH,
                PigeonDefinition.WALK_SPEED,
                PigeonDefinition.FLYING_SPEED,
                PigeonDefinition.FOLLOW_RANGE
        );
    }

    // ============ 生成 ============

    public static boolean canSpawn(EntityType<PigeonEntity> entityType, ServerLevelAccessor level,
                                   MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return canColumbidSpawn(level, pos, random, true);
    }

    // ============ 数据序列化 ============

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(VARIANT, ColumbidVariant.GRAY_PIGEON.ordinal());
    }

    @Override
    public @NotNull SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level,
                                                 @NotNull DifficultyInstance difficulty,
                                                 @NotNull MobSpawnType spawnType,
                                                 @Nullable SpawnGroupData spawnGroupData) {
        SpawnGroupData data = super.finalizeSpawn(
                level,
                difficulty,
                spawnType,
                spawnGroupData
        );

        this.randomizeModelScale();

        return data;
    }

    // ============ NBT ============

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("PigeonVariant", this.getColumbidVariant().ordinal());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.setPigeonVariant(ColumbidVariant.pigeonByOrdinal(compoundTag.getInt("PigeonVariant")));
    }

    // ============ 变体 ============

    @Override
    public ColumbidVariant getColumbidVariant() {
        return ColumbidVariant.pigeonByOrdinal(this.getEntityData().get(VARIANT));
    }

    public void setPigeonVariant(ColumbidVariant variant) {
        ColumbidVariant pigeonVariant = variant == ColumbidVariant.WHITE_PIGEON
                ? ColumbidVariant.WHITE_PIGEON
                : ColumbidVariant.GRAY_PIGEON;
        this.getEntityData().set(VARIANT, pigeonVariant.ordinal());
    }

    // ============ 可重写方法 ============

    @Override
    public boolean prefersHumanSettlements() {
        return true;  // 鸽子偏好人类居住区
    }

    @Override
    public boolean supportsPairBond() {
        return true;  // 鸽子支持配对
    }

    // ============ 声音 ============

    @Override
    @SuppressWarnings("all")
    protected SoundEvent getAmbientSound() {
        return NeoGuanNiaoSoundEvents.PIGEON_AMBIENT.get();
    }

    // ============ 繁殖 ============

    @Override
    protected AbstractColumbidEntity createChildEntity(ServerLevel level) {
        PigeonEntity child = NeoGuanNiaoEntityTypes.PIGEON.get().create(level);
        if (child != null) {
            child.setPigeonVariant(this.getColumbidVariant());
        }
        return child;
    }

    static {
        VARIANT = SynchedEntityData.defineId(PigeonEntity.class, EntityDataSerializers.INT);
    }

    @Override
    public boolean isFlying() {
        return false;
    }
}