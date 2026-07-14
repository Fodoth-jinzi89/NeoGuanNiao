package net.fodoth.skina.neoguanniao.content.bird.impl.columbid;

import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoEntityTypes;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.NotNull;

/**
 * 斑鸠实体类
 * 继承自 AbstractColumbidEntity，实现斑鸠特有的行为
 *
 * <p>斑鸠是温和的鸽形目鸟类，具有天气感知能力，支持配对和追逐行为
 */
public class SpottedDoveEntity extends AbstractColumbidEntity {

    public SpottedDoveEntity(EntityType<? extends SpottedDoveEntity> entityType, Level level) {
        super(entityType, level, SpottedDoveProfile.INSTANCE);
    }

    // ============ 属性 ============

    public static AttributeSupplier.Builder createAttributes() {
        return createColumbidAttributes(
                SpottedDoveDefinition.MAX_HEALTH,
                SpottedDoveDefinition.WALK_SPEED,
                SpottedDoveDefinition.FLYING_SPEED,
                SpottedDoveDefinition.FOLLOW_RANGE
        );
    }

    // ============ 生成 ============

    public static boolean canSpawn(EntityType<SpottedDoveEntity> entityType, ServerLevelAccessor level,
                                   MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        // 斑鸠偏好自然区域，而非城市
        return canColumbidSpawn(level, pos, random, false);
    }

    // ============ 变体 ============

    @Override
    @SuppressWarnings("all")
    public ColumbidVariant getColumbidVariant() {
        return ColumbidVariant.SPOTTED_DOVE;
    }

    // ============ 可重写方法 ============

    @Override
    protected boolean usesWeatherSense() {
        return true;  // 斑鸠能感知天气变化
    }

    @Override
    public boolean supportsPairBond() {
        return true;  // 斑鸠支持配对
    }

    @Override
    public boolean supportsChasing() {
        return true;  // 斑鸠有追逐行为
    }

    // ============ 声音 ============

    @Override
    protected SoundEvent getAmbientSound() {
        return NeoGuanNiaoSoundEvents.SPOTTED_DOVE_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return NeoGuanNiaoSoundEvents.SPOTTED_DOVE_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return NeoGuanNiaoSoundEvents.SPOTTED_DOVE_DEATH.get();
    }

    // ============ 繁殖 ============

    @Override
    protected AbstractColumbidEntity createChildEntity(ServerLevel level) {
        return NeoGuanNiaoEntityTypes.SPOTTED_DOVE.get().create(level);
    }

    @Override
    public boolean isFlying() {
        return false;
    }
}