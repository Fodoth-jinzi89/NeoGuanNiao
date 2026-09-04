package net.fodoth.skina.neoguanniao.content.fan;

import java.util.Comparator;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoParticleTypes;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoSoundEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class FeatherFanItem
        extends Item {
    private static final double HUNT_LOCK_MIN_DOT = Math.cos(Math.toRadians(52.0));

    public FeatherFanItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context,
                                @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.neoguanniao.wind_feather_fan.mode").withStyle(ChatFormatting.GOLD)
                .append(Component.translatable("item.neoguanniao.wind_feather_fan.mode." + FeatherFanEnchantments.modeName(stack)).withStyle(ChatFormatting.AQUA)));
        tooltip.add(Component.translatable("tooltip.neoguanniao.wind_feather_fan.attack_damage").withStyle(ChatFormatting.GOLD)
                .append(Component.literal(String.format("%.1f", attackDamage(stack))).withStyle(ChatFormatting.AQUA)));
        tooltip.add(Component.translatable("tooltip.neoguanniao.wind_feather_fan.attack_range").withStyle(ChatFormatting.GOLD)
                .append(Component.literal(String.format("%.1f", attackRange(stack))).withStyle(ChatFormatting.AQUA)));
        tooltip.add(Component.translatable("item.neoguanniao.wind_feather_fan.usage").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public @NotNull ItemAttributeModifiers getDefaultAttributeModifiers(@NotNull ItemStack stack) {
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(ResourceLocation.fromNamespaceAndPath(NeoGuanNiao.MODID, "fan_damage"), attackDamage(stack), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(ResourceLocation.fromNamespaceAndPath(NeoGuanNiao.MODID, "fan_speed"), -2.1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .build();
    }

    private static float attackDamage(ItemStack stack) {
        return 4.0f + rarityBonus(stack);
    }

    public static float attackRange(ItemStack stack) {
        return 6.0f + rarityBonus(stack);
    }

    private static float rarityBonus(ItemStack stack) {
        ListTag feathers = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getList("Feathers", 10);
        float bonus = 0.0f;
        for (int i = 0; i < Math.min(6, feathers.size()); i++) {
            bonus += Math.clamp(feathers.getCompound(i).getInt("rarity"), 0, 3) * 0.5f;
        }
        return bonus;
    }

    public boolean hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity target, @NotNull LivingEntity attacker) {
        if (isBird(target)) {
            return false;
        }
        if (!attacker.level().isClientSide) {
            target.knockback(0.3, attacker.getX() - target.getX(), attacker.getZ() - target.getZ());
        }
        return true;
    }

    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isCrouching()) {
            int mode = (FeatherFanEnchantments.mode(stack) + 1) % 3;
            stack.set(net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoDataComponents.FEATHER_FAN_MODE.get(), mode);
            if (!level.isClientSide)
                player.displayClientMessage(Component.translatable("tooltip.neoguanniao.wind_feather_fan.mode").withStyle(ChatFormatting.GOLD)
                        .append(Component.translatable("item.neoguanniao.wind_feather_fan.mode." + FeatherFanEnchantments.modeName(stack)).withStyle(ChatFormatting.AQUA)), true);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    public void onUseTick(@NotNull Level level, @NotNull LivingEntity living, @NotNull ItemStack stack, int remainingUseDuration) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        int chargeTicks = this.getUseDuration(stack, living) - remainingUseDuration;
        if (living instanceof Player player) {
            int lockCapacity;
            if (FeatherFanEnchantments.hasHuntingReturn(stack) && (lockCapacity = FeatherFanItem.getHuntingLockCapacity(chargeTicks)) > 0 && chargeTicks % 2 == 0) {
                List<LivingEntity> targets = FeatherFanItem.findHuntingTargets(player, lockCapacity);
                for (int index = 0; index < targets.size(); ++index) {
                    this.spawnHuntingLockEffect(serverLevel, targets.get(index), chargeTicks, index);
                }
                if (FeatherFanItem.isHuntingLockMilestone(chargeTicks) && targets.size() >= lockCapacity) {
                    LivingEntity newestTarget = targets.get(lockCapacity - 1);
                    level.playSound(null, newestTarget.getX(), newestTarget.getY(), newestTarget.getZ(), NeoGuanNiaoSoundEvents.FEATHER_FAN_HUNT_LOCK.get(), SoundSource.PLAYERS, 0.52f, 0.96f + (float) lockCapacity * 0.075f);
                }
            }
        }
        float charge = Mth.clamp((float) chargeTicks / 30.0f, 0.0f, 1.0f);
        if (chargeTicks > 0 && chargeTicks % 4 == 0) {
            Vec3 center = living.getEyePosition().add(living.getLookAngle().scale(0.78)).add(0.0, -0.38, 0.0);
            int moteCount = 1 + Mth.floor(charge);
            serverLevel.sendParticles((ParticleOptions) ParticleTypes.WHITE_ASH, center.x, center.y, center.z, moteCount, 0.08 + (double) charge * 0.05, 0.07, 0.08 + (double) charge * 0.05, 0.008);
            if (chargeTicks >= 30) {
                serverLevel.sendParticles((ParticleOptions) ParticleTypes.SNOWFLAKE, center.x, center.y, center.z, 1, 0.12, 0.08, 0.12, 0.008);
            }
        }
        if (chargeTicks == 1) {
            level.playSound(null, living.getX(), living.getY(), living.getZ(), SoundEvents.PHANTOM_FLAP, SoundSource.PLAYERS, 0.2f, 1.45f);
        } else if (chargeTicks == 10 || chargeTicks == 20) {
            level.playSound(null, living.getX(), living.getY(), living.getZ(), SoundEvents.PHANTOM_FLAP, SoundSource.PLAYERS, 0.35f, 1.35f - (float) chargeTicks * 0.012f);
        } else if (chargeTicks == 30) {
            level.playSound(null, living.getX(), living.getY(), living.getZ(), SoundEvents.TRIDENT_RIPTIDE_1, SoundSource.PLAYERS, 0.55f, 1.35f);
            serverLevel.sendParticles((ParticleOptions) ParticleTypes.POOF, living.getX(), living.getEyeY() - 0.2, living.getZ(), 2, 0.12, 0.08, 0.12, 0.018);
            serverLevel.sendParticles((ParticleOptions) ParticleTypes.WHITE_ASH, living.getX(), living.getEyeY() - 0.2, living.getZ(), 5, 0.18, 0.12, 0.18, 0.012);
            serverLevel.sendParticles((ParticleOptions) ParticleTypes.SNOWFLAKE, living.getX(), living.getEyeY() - 0.2, living.getZ(), 3, 0.14, 0.1, 0.14, 0.008);
        }
    }

    public void releaseUsing(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity living, int timeLeft) {
        Player player;
        block6:
        {
            block5:
            {
                if (!(living instanceof Player)) break block5;
                player = (Player) living;
                if (!level.isClientSide) break block6;
            }
            return;
        }
        int chargeTicks = this.getUseDuration(stack, living) - timeLeft;
        if (chargeTicks < 5) {
            return;
        }
        InteractionHand hand = player.getUsedItemHand();
        float charge = FeatherFanItem.getCharge(chargeTicks);
        List<LivingEntity> huntingTargets = List.of();
        boolean fullyCharged = chargeTicks >= 30;
        if (fullyCharged && FeatherFanEnchantments.hasHuntingReturn(stack)) {
            huntingTargets = FeatherFanItem.findHuntingTargets(player, FeatherFanItem.getHuntingLockCapacity(chargeTicks));
            if (huntingTargets.isEmpty()) {
                player.getCooldowns().addCooldown(this, 6);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.35f, 0.72f);
                return;
            }
        }
        this.launchFan(stack, level, player, hand, charge, fullyCharged && !FeatherFanEnchantments.hasHuntingReturn(stack), huntingTargets);
    }

    public static boolean isFullyCharged(LivingEntity living) {
        Item item;
        if (!living.isUsingItem() || !((item = living.getUseItem().getItem()) instanceof FeatherFanItem)) {
            return false;
        }
        FeatherFanItem fan = (FeatherFanItem) item;
        int chargeTicks = fan.getUseDuration(living.getUseItem(), living) - living.getUseItemRemainingTicks();
        return chargeTicks >= 30;
    }

    public void tryLaunchPiercing(ServerPlayer player) {
        if (player.getCooldowns().isOnCooldown(this) || !FeatherFanItem.isFullyCharged(player)) {
            return;
        }
        InteractionHand hand = player.getUsedItemHand();
        ItemStack stack = player.getUseItem();
        player.stopUsingItem();
        this.launchFan(stack, player.level(), player, hand, 1.0f, true, List.of());
    }

    private void launchFan(ItemStack stack, Level level, Player player, InteractionHand hand, float charge, boolean piercing, List<LivingEntity> huntingTargets) {
        LivingEntity primaryHuntingTarget;
        boolean hunting = !piercing && !huntingTargets.isEmpty();
        primaryHuntingTarget = hunting ? huntingTargets.getFirst() : null;
        float speed = piercing ? 2.65f : (hunting ? 1.75f : Mth.lerp(charge, 0.8f, 1.6f));
        ItemStack thrownStack = stack.copy();
        thrownStack.setCount(1);
        FeatherFanProjectileEntity projectile = new FeatherFanProjectileEntity(level, player);
        if (piercing) {
            projectile.configurePiercing(thrownStack, hand);
        } else if (hunting) {
            projectile.configureHunting(thrownStack, hand, charge, huntingTargets);
        } else {
            projectile.configureThrow(thrownStack, hand, charge);
        }
        if (hunting) {
            Vec3 direction = primaryHuntingTarget.getBoundingBox().getCenter().subtract(projectile.position());
            if (direction.lengthSqr() > 1.0E-6) {
                projectile.shoot(direction.x, direction.y, direction.z, speed, 0.0f);
            }
        } else {
            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f, speed, 0.0f);
        }
        if (level.addFreshEntity(projectile)) {
            stack.shrink(1);
            player.awardStat(Stats.ITEM_USED.get(this));
            player.swing(hand, true);
            if (piercing) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0f, 1.55f);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.PLAYERS, 0.85f, 1.25f);
                if (level instanceof ServerLevel serverLevel) {
                    Vec3 launch = player.getEyePosition().add(player.getLookAngle().scale(1.15));
                    serverLevel.sendParticles((ParticleOptions) ParticleTypes.WHITE_ASH, launch.x, launch.y, launch.z, 6, 0.06, 0.05, 0.06, 0.025);
                }
            } else if (hunting) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(), NeoGuanNiaoSoundEvents.FEATHER_FAN_HUNT_START.get(), SoundSource.PLAYERS, 0.95f, 1.0f + charge * 0.12f);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 0.45f, 1.42f);
            } else {
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 0.8f, 1.15f + charge * 0.25f);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.65f, 0.85f + charge * 0.2f);
            }
        }
    }

    private static List<LivingEntity> findHuntingTargets(Player player, int limit) {
        if (limit <= 0) {
            return List.of();
        }
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        AABB searchArea = player.getBoundingBox().inflate(18.0);
        List<LivingEntity> candidates = player.level().getEntitiesOfClass(LivingEntity.class, searchArea, target -> target.isAlive() && !target.isSpectator() && target != player && !isBird(target) && player.canAttack(target));
        candidates.removeIf(target -> {
            Vec3 offset = target.getBoundingBox().getCenter().subtract(eye);
            double distance = offset.length();
            if (distance < 0.001 || distance > 18.0) {
                return true;
            }
            double dot = look.dot(offset.scale(1.0 / distance));
            return dot < HUNT_LOCK_MIN_DOT || !player.hasLineOfSight(target);
        });
        candidates.sort(Comparator.comparingDouble(target -> FeatherFanItem.huntingTargetScore(player, target)));
        return candidates.size() <= limit ? candidates : List.copyOf(candidates.subList(0, limit));
    }

    private static double huntingTargetScore(Player player, LivingEntity target) {
        Vec3 offset = target.getBoundingBox().getCenter().subtract(player.getEyePosition());
        double distance = offset.length();
        double dot = distance < 0.001 ? 1.0 : player.getLookAngle().normalize().dot(offset.scale(1.0 / distance));
        return (1.0 - dot) * 5.0 + distance / 18.0;
    }

    private static int getHuntingLockCapacity(int chargeTicks) {
        if (chargeTicks < 6) {
            return 0;
        }
        return Mth.clamp(1 + (chargeTicks - 6) / 4, 1, 7);
    }

    private static boolean isHuntingLockMilestone(int chargeTicks) {
        return chargeTicks >= 6 && (chargeTicks - 6) % 4 == 0;
    }

    private void spawnHuntingLockEffect(ServerLevel level, LivingEntity target, int ticks, int index) {
        Vec3 center = target.getBoundingBox().getCenter().add(0.0, (double) target.getBbHeight() * 0.08, 0.0);
        double radius = (double) target.getBbWidth() * 0.65 + 0.38;
        double angle = (double) ticks * 0.18 + (double) index * 0.78;
        level.sendParticles((ParticleOptions) NeoGuanNiaoParticleTypes.HUNTING_MARK.get(), center.x + Math.cos(angle) * radius * 0.12, center.y, center.z + Math.sin(angle) * radius * 0.12, 1, 0.0, 0.0, 0.0, 0.0);
        level.sendParticles((ParticleOptions) ParticleTypes.WAX_ON, center.x, center.y, center.z, 2, radius * 0.45, (double) target.getBbHeight() * 0.22, radius * 0.45, 0.012);
    }

    public int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity living) {
        return 72000;
    }

    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.BLOCK;
    }

    private static float getCharge(int chargeTicks) {
        return Mth.clamp((float) (chargeTicks - 5) / 25.0f, 0.0f, 1.0f);
    }

    private static boolean isBird(Entity entity) {
        return entity instanceof AbstractBirdEntity<?>;
    }

}
