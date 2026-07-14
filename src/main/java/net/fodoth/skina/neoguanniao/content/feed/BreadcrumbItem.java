package net.fodoth.skina.neoguanniao.content.feed;

import net.fodoth.skina.neoguanniao.content.bird.impl.sparrow.SparrowEntity;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoBlocks;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BreadcrumbItem extends Item {
    private static final double THROW_DISTANCE = 5.5F;

    public BreadcrumbItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(
            @NotNull Level level,
            @NotNull Player player,
            @NotNull InteractionHand hand
    ) {
        ItemStack stack = player.getItemInHand(hand);
        Vec3 aimedPoint = this.resolveAirAimPoint(level, player);
        if (!level.isClientSide && this.invertedTossBreadcrumbs((ServerLevel) level, player, hand, stack, aimedPoint)) {
            return InteractionResultHolder.fail(stack);
        }
        player.swing(hand, true);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        Vec3 aimedPoint = this.resolveClickAimPoint(context);
        if (aimedPoint == null) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide && this.invertedTossBreadcrumbs((ServerLevel) level, player, context.getHand(), context.getItemInHand(), aimedPoint)) {
            return InteractionResult.PASS;
        }
        player.swing(context.getHand(), true);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private @NotNull Vec3 resolveAirAimPoint(Level level, Player player) {
        HitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            return hitResult.getLocation();
        }
        return player.getEyePosition().add(player.getLookAngle().scale(THROW_DISTANCE));
    }

    @Nullable
    private Vec3 resolveClickAimPoint(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        BlockState clickedState = level.getBlockState(clickedPos);
        Direction face = context.getClickedFace();
        BlockPlaceContext placeContext = new BlockPlaceContext(context);

        if (clickedState.is(NeoGuanNiaoBlocks.BREADCRUMBS.get())) {
            return Vec3.atCenterOf(clickedPos);
        }
        if (clickedState.canBeReplaced(placeContext)) {
            return context.getClickLocation();
        }
        return face == Direction.DOWN ? null : Vec3.atCenterOf(clickedPos.relative(face));
    }

    private boolean invertedTossBreadcrumbs(ServerLevel level, Player player, InteractionHand hand, ItemStack stack, Vec3 aimedPoint) {
        BlockPos targetPos = this.findScatterLanding(level, aimedPoint);
        if (targetPos == null) {
            return true;
        }
        this.scatterBreadcrumbs(level, player, hand, targetPos, stack, aimedPoint);
        return false;
    }

    @Nullable
    private BlockPos findScatterLanding(Level level, Vec3 aimedPoint) {
        int baseX = Mth.floor(aimedPoint.x);
        int baseY = Mth.floor(aimedPoint.y + 0.6);
        int baseZ = Mth.floor(aimedPoint.z);
        int[][] offsets = new int[][]{
                {0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1},
                {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
        };

        for (int[] offset : offsets) {
            for (int yOffset = 2; yOffset >= -6; --yOffset) {
                BlockPos candidate = new BlockPos(baseX + offset[0], baseY + yOffset, baseZ + offset[1]);
                if (this.canScatterAt(level, candidate)) {
                    return candidate;
                }
            }
        }
        return null;
    }

    private boolean canScatterAt(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(NeoGuanNiaoBlocks.BREADCRUMBS.get())) {
            return state.getValue(BreadcrumbPileBlock.LAYERS) < 4;
        }
        if (!state.isAir() && !state.getCollisionShape(level, pos).isEmpty()) {
            return false;
        }
        BlockState breadcrumbs = NeoGuanNiaoBlocks.BREADCRUMBS.get().defaultBlockState();
        return breadcrumbs.canSurvive(level, pos) && level.getFluidState(pos).isEmpty();
    }

    private void scatterBreadcrumbs(ServerLevel level, Player player, InteractionHand hand, BlockPos originPos, ItemStack stack, Vec3 aimedPoint) {
        RandomSource random = level.getRandom();
        if (this.addServing(level, originPos)) {
            if (player == null || !player.getAbilities().instabuild) {
                stack.shrink(1);
            }

            Vec3 center = Vec3.atCenterOf(originPos).add(0, 0.05, 0);
            Vec3 handPos = null;
            if (player != null) {
                handPos = this.handStartPosition(player, hand);
            }
            this.spawnThrowTrail(level, handPos, aimedPoint, random);

            if (player != null) {
                level.playSound(
                        null,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        SoundEvents.ITEM_FRAME_REMOVE_ITEM,
                        SoundSource.PLAYERS,
                        0.28F,
                        Mth.randomBetween(random, 1.15F, 1.35F)
                );
            }
            level.playSound(
                    null,
                    center.x,
                    center.y,
                    center.z,
                    SoundEvents.SAND_PLACE,
                    SoundSource.BLOCKS,
                    0.18F,
                    Mth.randomBetween(random, 1.25F, 1.5F)
            );

            ItemParticleOption particle = new ItemParticleOption(
                    ParticleTypes.ITEM,
                    new ItemStack(NeoGuanNiaoItems.BREADCRUMBS.get())
            );
            level.sendParticles(
                    particle,
                    center.x,
                    center.y + 0.08,
                    center.z,
                    12 + random.nextInt(7),
                    0.34,
                    0.08,
                    0.34,
                    0.02
            );

            SparrowEntity.alertNearbyBreadcrumbs(level, originPos, player);
        }
    }

    private boolean addServing(ServerLevel level, BlockPos pos) {
        if (!this.canScatterAt(level, pos)) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        if (state.is(NeoGuanNiaoBlocks.BREADCRUMBS.get())) {
            level.setBlock(
                    pos,
                    state.setValue(BreadcrumbPileBlock.BITES, 7)
                            .setValue(BreadcrumbPileBlock.LAYERS, 4)
                            .setValue(BreadcrumbPileBlock.AGE, 0),
                    2
            );
        } else {
            level.setBlock(
                    pos,
                    NeoGuanNiaoBlocks.BREADCRUMBS.get().defaultBlockState()
                            .setValue(BreadcrumbPileBlock.BITES, 7)
                            .setValue(BreadcrumbPileBlock.LAYERS, 4)
                            .setValue(BreadcrumbPileBlock.AGE, 0),
                    2
            );
        }
        level.scheduleTick(pos, NeoGuanNiaoBlocks.BREADCRUMBS.get(), 600);
        return true;
    }

    private Vec3 handStartPosition(Player player, InteractionHand hand) {
        double sideOffset = hand == InteractionHand.MAIN_HAND ? 0.24 : -0.24;
        Vec3 look = player.getLookAngle();
        Vec3 right = look.cross(new Vec3(0, 1, 0));
        if (right.length() <= 1.0E-4) {
            right = new Vec3(1, 0, 0);
        } else {
            right = right.normalize();
        }
        return player.getEyePosition()
                .add(right.scale(sideOffset))
                .add(look.scale(0.35))
                .add(0, -0.22, 0);
    }

    private void spawnThrowTrail(ServerLevel level, Vec3 start, Vec3 aimedPoint, RandomSource random) {
        Vec3 end = aimedPoint.add(0, 0.1, 0);
        Vec3 delta = end.subtract(start);
        ItemParticleOption particle = new ItemParticleOption(
                ParticleTypes.ITEM,
                new ItemStack(NeoGuanNiaoItems.BREADCRUMBS.get())
        );
        int steps = 7;

        for (int i = 0; i <= steps; ++i) {
            double progress = (double) i / (double) steps;
            Vec3 point = start.add(delta.scale(progress))
                    .add(0, Math.sin(progress * Math.PI) * 0.14, 0);
            level.sendParticles(
                    particle,
                    point.x,
                    point.y,
                    point.z,
                    1 + random.nextInt(2),
                    0.025,
                    0.02,
                    0.025,
                    0.003
            );
        }
    }
}