package net.fodoth.skina.neoguanniao.content.bird.impl.neo.budgerigar;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.BirdBehaviorState;
import net.fodoth.skina.neoguanniao.content.bird.core.controller.BirdTameController;
import net.fodoth.skina.neoguanniao.content.bird.core.data.BirdControllers;
import net.fodoth.skina.neoguanniao.content.bird.core.goal.goals.BirdMusicDanceGoal;
import net.fodoth.skina.neoguanniao.content.bird.feature.brain.BirdBrain;
import net.fodoth.skina.neoguanniao.content.bird.feature.species.BudgerigarProfile;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoBirdData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class BudgerigarEntity extends AbstractBirdEntity<BudgerigarEntity> {

    private static final ResourceLocation CHIRPY_PARTNER_ADVANCEMENT =
            ResourceLocation.fromNamespaceAndPath(NeoGuanNiao.MODID, "husbandry/chirpy_partner");

    public BudgerigarEntity(EntityType<BudgerigarEntity> entityType, Level level) {
        super(
                entityType,
                level,
                NeoGuanNiaoBirdData.BUDGERIGAR.get(),
                // 使用 Builder 传入自定义的 BirdTameController
                BirdControllers.<BudgerigarEntity>builder()
                        .birdTameController(new BirdTameController<>() {
                            @Override
                            public void triggerTameSideEffects(Player player) {
                                if (!(player instanceof ServerPlayer serverPlayer)) {
                                    return;
                                }

                                var advancements = serverPlayer.server.getAdvancements();
                                var chirpyPartnerAdvancement = advancements.get(CHIRPY_PARTNER_ADVANCEMENT);

                                if (chirpyPartnerAdvancement != null) {
                                    serverPlayer.getAdvancements().award(chirpyPartnerAdvancement, "tame_budgerigar");
                                }
                            }
                        })
                        .build()
        );
        initControllers();
    }


    @Override
    protected BudgerigarEntity getSelf() {
        return this;
    }

    @Override
    protected void initFeatures() {
        birdBrain = new BirdBrain(
                this,
                BudgerigarProfile.INSTANCE
        );
    }

    @Override
    protected void initControllers() {
        super.initControllers();
        getBirdControllers().getBirdTickController().getTickTimer().getBirdFindNearbyMusicLoopTicker().setTicks(10 + this.getRandom().nextInt(20));
    }


    public static AttributeSupplier.Builder createAttributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 6.0)
                .add(Attributes.MOVEMENT_SPEED, 0.24)
                .add(Attributes.FLYING_SPEED, 0.32)
                .add(Attributes.FOLLOW_RANGE, 18.0);
    }

    public static boolean canSpawn(
            EntityType<? extends AbstractBirdEntity<?>> entityType,
            ServerLevelAccessor level,
            MobSpawnType spawnType,
            BlockPos pos,
            RandomSource random
    ) {
        return AbstractBirdEntity.canSpawn(
                entityType,
                level,
                spawnType,
                pos,
                random,
                NeoGuanNiaoBirdData.BUDGERIGAR.get()
        );
    }
    
    // ============ AI 注册 ============
    @Override
    protected List<Goal> buildGoals() {
        List<Goal> goals = new ArrayList<>(super.buildGoals());
        goals.add(1, new BirdMusicDanceGoal(this));
        return goals;
    }

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (isCocoa(stack)) {
            if (!this.level().isClientSide) {
                this.getBirdControllers().getBirdBehaviorStateController().setBehaviorStateFor(
                        BirdBehaviorState.CURIOUS,
                        NeoGuanNiaoBirdData.BUDGERIGAR.get().eating().curiousTicksLimitForDroppedFood()
                );
                SoundEvent sound = this.getBirdControllers().getBirdSoundController().getInteractionSound();
                if (sound != null) {
                    playSound(
                            this.getBirdControllers().getBirdSoundController().getInteractionSound(),
                            NeoGuanNiaoBirdData.BUDGERIGAR.get().eating().eatSoundVolume(),
                            NeoGuanNiaoBirdData.BUDGERIGAR.get().eating().eatSoundPitch()
                    );
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    private boolean isCocoa(ItemStack stack) {
        return !stack.isEmpty() && stack.is(Items.COCOA_BEANS);
    }

    public boolean isBusyWithMusicOrSleep() {
        return this.isDancing() || this.getBirdControllers().getBirdRoutineController().isSleepingOrRoosting();
    }

    public void triggerMusic(int ticks) {
        this.getBirdControllers().getBirdTickController().getTickTimer().getBirdMusicTicker().setTicks(
                Math.max(
                        this.getBirdControllers().getBirdTickController().getTickTimer().getBirdMusicTicker().getTicks(),
                        ticks
                )
        );
        if (!this.getBirdControllers().getBirdEatingController().isEating()
                && !this.getBirdControllers().getBirdBehaviorStateController().getBehaviorState().isEscape()) {
            this.getBirdControllers().getBirdBehaviorStateController().setBehaviorStateFor(
                    BirdBehaviorState.DANCING,
                    Math.min(ticks, 80)
            );
        }
    }

    public BlockPos findNearbyJukebox() {
        BlockPos origin = this.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(
                origin.offset(-8, -3, -8), origin.offset(8, 3, 8))) {
            BlockState state = this.level().getBlockState(pos);
            if (state.is(Blocks.JUKEBOX) && state.hasProperty(JukeboxBlock.HAS_RECORD)
                    && state.getValue(JukeboxBlock.HAS_RECORD)) {
                return pos.immutable();
            }
        }
        return null;
    }

    @Override
    protected void applyTamingSideEffects() {
        super.applyTamingSideEffects();

        for (int i = 0; i < 9; ++i) {
            double xOffset = this.getRandom().nextGaussian() * 0.03;
            double yOffset = this.getRandom().nextGaussian() * 0.04;
            double zOffset = this.getRandom().nextGaussian() * 0.03;

            this.level().addParticle(
                    ParticleTypes.HEART,
                    this.getX(0.7),
                    this.getY() + 0.22,
                    this.getZ(0.7),
                    xOffset,
                    yOffset + 0.035,
                    zOffset
            );
        }
    }

}