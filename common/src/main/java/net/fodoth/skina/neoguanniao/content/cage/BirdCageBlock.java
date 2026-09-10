package net.fodoth.skina.neoguanniao.content.cage;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.fodoth.skina.neoguanniao.platform.CarryOnHooks;
import net.minecraft.core.registries.BuiltInRegistries;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class BirdCageBlock extends BaseEntityBlock {

    public static final DirectionProperty FACING =
            HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty PART = BooleanProperty.create("part");
    public static final IntegerProperty LAYERS = IntegerProperty.create("layers", 1, 8);


    private final BirdCageVariant variant;


    public BirdCageBlock(
            BirdCageVariant variant,
            BlockBehaviour.Properties properties
    ) {
        super(properties);
        this.variant = variant;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(PART, false).setValue(LAYERS, 8));
    }


    public BirdCageVariant variant() {
        return variant;
    }


    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public final MapCodec<BirdCageBlock> CODEC =
            simpleCodec(properties ->
                    new BirdCageBlock(
                            this.variant(),
                            properties
                    )
            );

    @Override
    public @NotNull RenderShape getRenderShape(
            @NotNull BlockState state
    ) {
        return state.getValue(PART) ? RenderShape.INVISIBLE : RenderShape.ENTITYBLOCK_ANIMATED;
    }


    @Override
    public @NotNull VoxelShape getShape(
            @NotNull BlockState state,
            @NotNull BlockGetter level,
            @NotNull BlockPos pos,
            @NotNull CollisionContext context
    ) {
        if (variant == BirdCageVariant.MEDIUM || variant == BirdCageVariant.LARGE) {
            BlockPos origin = state.getValue(PART) ? findOrigin(level, pos, state.getValue(FACING)) : pos;
            if (origin == null) return Block.box(0, 0, 0, 16, 16, 16);
            Direction facing = state.getValue(FACING);
            int dx = pos.getX() - origin.getX();
            int dz = pos.getZ() - origin.getZ();
            int localX = dx * facing.getClockWise().getStepX() + dz * facing.getClockWise().getStepZ();
            int localY = pos.getY() - origin.getY();
            int localZ = dx * facing.getOpposite().getStepX() + dz * facing.getOpposite().getStepZ();
            int inset = variant == BirdCageVariant.LARGE ? 0 : 6;
            double minX = localX == -1 ? inset : 0;
            double maxX = localX == 1 ? 16 - inset : 16;
            int zInset = variant == BirdCageVariant.LARGE ? 8 : inset;
            double minZ = localZ == -1 ? zInset : 0;
            double maxZ = localZ == 1 ? 16 - zInset : 16;
            double maxY = 16;
            double[] bounds = rotateBounds(facing, minX, minZ, maxX, maxZ);
            return Block.box(bounds[0], 0, bounds[1], bounds[2], maxY, bounds[3]);
        }
        return variant.shape();
    }

    @Override
    public @NotNull VoxelShape getVisualShape(@NotNull BlockState state, @NotNull BlockGetter level,
                                              @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public @NotNull VoxelShape getOcclusionShape(@NotNull BlockState state, @NotNull BlockGetter level,
                                                 @NotNull BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public int getLightBlock(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        return 0;
    }

    @Override
    protected float getShadeBrightness(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        return 1.0F;
    }

    @Override
    public boolean useShapeForLightOcclusion(@NotNull BlockState state) {
        return false;
    }


    @Override
    public @Nullable BlockEntity newBlockEntity(
            @NotNull BlockPos pos,
            @NotNull BlockState state
    ) {
        if (state.getValue(PART)) return null;
        return new BirdCageBlockEntity(
                pos,
                state
        );
    }

    @Override
    public @Nullable BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        if (variant == BirdCageVariant.SMALL) return defaultBlockState().setValue(FACING, facing);
        BlockPos origin = context.getClickedPos();
        int height = structureHeight();
        for (int x = -1; x <= 1; x++)
            for (int y = 0; y < height; y++)
                for (int z = -1; z <= 1; z++) {
                    BlockPos pos = offset(origin, facing, x, y, z);
                    if (!context.getLevel().getBlockState(pos).canBeReplaced(context)) {
                        if (context.getPlayer() != null) context.getPlayer().displayClientMessage(
                                Component.translatable("message.neoguanniao.bird_cage.place_failed", 3, height, 3), true);
                        return null;
                    }
                }
        return defaultBlockState().setValue(FACING, facing);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART, LAYERS);
    }

    @Override
    public @NotNull BlockState rotate(@NotNull BlockState state, @NotNull Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public @NotNull BlockState mirror(@NotNull BlockState state, @NotNull Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state,
                            @Nullable LivingEntity placer, @NotNull ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.getBlockEntity(pos) instanceof BirdCageBlockEntity cage) {
            for (CompoundTag bird : BirdCageItem.capturedBirds(stack)) cage.addCapturedBird(bird);
        }
        if (state.getValue(PART) || variant == BirdCageVariant.SMALL) return;
        int height = structureHeight();
        BlockState part = state.setValue(PART, true).setValue(LAYERS, 8);
        for (int x = -1; x <= 1; x++)
            for (int y = 0; y < height; y++)
                for (int z = -1; z <= 1; z++) {
                    if (x != 0 || y != 0 || z != 0)
                        level.setBlock(offset(pos, state.getValue(FACING), x, y, z), part, 3);
                }
    }

    @Override
    public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                         @NotNull BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && state.getValue(PART) && !level.isClientSide) {
            BlockPos origin = findOrigin(level, pos, state.getValue(FACING));
            if (origin != null) level.destroyBlock(origin, true);
        } else if (!state.is(newState.getBlock()) && !state.getValue(PART) && variant != BirdCageVariant.SMALL) {
            int height = structureHeight();
            for (int x = -1; x <= 1; x++)
                for (int y = 0; y < height; y++)
                    for (int z = -1; z <= 1; z++) {
                        if (x != 0 || y != 0 || z != 0) {
                            BlockPos part;
                            part = offset(pos, state.getValue(FACING), x, y, z);
                            if (level.getBlockState(part).is(this)) level.removeBlock(part, false);
                        }
                    }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public @NotNull BlockState playerWillDestroy(@NotNull Level level, @NotNull BlockPos pos,
                                                 @NotNull BlockState state, @NotNull Player player) {
        if (player.isCreative() && state.getValue(PART)) {
            BlockPos origin = findOrigin(level, pos, state.getValue(FACING));
            if (origin != null) level.removeBlock(origin, false);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    private @Nullable BlockPos findOrigin(BlockGetter level, BlockPos pos, Direction facing) {
        int height = structureHeight();
        for (int x = -1; x <= 1; x++)
            for (int y = 0; y < height; y++)
                for (int z = -1; z <= 1; z++) {
                    BlockPos origin = offset(pos, facing, -x, -y, -z);
                    BlockState originState = level.getBlockState(origin);
                    if (originState.is(this) && !originState.getValue(PART)) return origin;
                }
        return null;
    }

    private int structureHeight() {
        return switch (variant) {
            case SMALL, MEDIUM -> variant == BirdCageVariant.SMALL ? 1 : 3;
            case LARGE -> 4;
        };
    }

    private static BlockPos offset(BlockPos origin, Direction facing, int x, int y, int z) {
        Direction right = facing.getClockWise();
        Direction back = facing.getOpposite();
        return origin.relative(right, x).relative(Direction.UP, y).relative(back, z);
    }

    private static double[] rotateBounds(Direction facing, double minX, double minZ, double maxX, double maxZ) {
        return switch (facing) {
            case EAST -> new double[]{16 - maxZ, minX, 16 - minZ, maxX};
            case SOUTH -> new double[]{16 - maxX, 16 - maxZ, 16 - minX, 16 - minZ};
            case WEST -> new double[]{minZ, 16 - maxX, maxZ, 16 - minX};
            default -> new double[]{minX, minZ, maxX, maxZ};
        };
    }

    @Override
    public @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hit) {
        BlockPos origin = state.getValue(PART) ? findOrigin(level, pos, state.getValue(FACING)) : pos;
        if (origin == null || !(level.getBlockEntity(origin) instanceof BirdCageBlockEntity cage))
            return InteractionResult.PASS;
        // 主手和副手会在同一个游戏刻各触发一次交互，避免同一次右键被处理两次。
        if (!cage.tryInteract(level.getGameTime())) return InteractionResult.PASS;
        if (!cage.isFull() && storeCarriedEntity(level, pos, player)) {
            return InteractionResult.sidedSuccess(false);
        }
        if (player.isShiftKeyDown() && !cage.isEmpty()) {
            if (!level.isClientSide) {
                CompoundTag preview = cage.lastCapturedBird();
                Entity carriedEntity = EntityType.create(preview, level).orElse(null);
                if (carriedEntity != null && CarryOnHooks.isLoaded()) {
                    // Carry On 会检查实体与玩家的距离，先把实体挪到玩家身上再交给它。
                    carriedEntity.moveTo(player.getX(), player.getY(), player.getZ());
                    if (CarryOnHooks.tryCarryEntity(player, carriedEntity)) {
                        cage.removeLastCapturedBird();
                        return InteractionResult.sidedSuccess(false);
                    }
                }
            }
            if (!level.isClientSide) {
                CompoundTag bird = cage.removeLastCapturedBird();
                Entity entity = EntityType.create(bird, level).orElse(null);
                if (entity != null) {
                    double centerX = origin.getX() + 0.5D;
                    double centerY = origin.getY() + structureHeight() * 0.5D;
                    double centerZ = origin.getZ() + 0.5D;
                    entity.moveTo(centerX, centerY - entity.getBbHeight() * 0.5D, centerZ, player.getYRot(), 0.0F);
                    level.addFreshEntity(entity);
                } else {
                    cage.addCapturedBird(bird);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    /**
     * 尝试把玩家（Carry On）抱着的实体放进 {@code pos} 处的鸟笼。
     * <p>
     * Carry On 以 HIGH 优先级处理右键事件，会把抱着的实体直接放到地上并取消事件，
     * 方块自身的 {@link #useWithoutItem} 因此不会执行；平台事件需要在它之前调用本方法。
     * </p>
     *
     * @return 是否成功把实体装进鸟笼
     */
    public static boolean storeCarriedEntity(Level level, BlockPos pos, Player player) {
        if (level.isClientSide || !CarryOnHooks.isLoaded()) return false;
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof BirdCageBlock cageBlock)) return false;
        BlockPos origin = state.getValue(PART) ? cageBlock.findOrigin(level, pos, state.getValue(FACING)) : pos;
        if (origin == null || !(level.getBlockEntity(origin) instanceof BirdCageBlockEntity cage) || cage.isFull())
            return false;
        if (!cage.tryInteract(level.getGameTime())) return false;
        if (!(cageBlock.asItem() instanceof BirdCageItem item)) return false;
        Entity carried = CarryOnHooks.carriedEntity(player);
        if (carried == null || !item.canFit(carried) || !BirdCageItem.canCapture(carried)) return false;
        CompoundTag bird = new CompoundTag();
        carried.saveWithoutId(bird);
        bird.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(carried.getType()).toString());
        if (carried instanceof LivingEntity living) bird.putFloat("MaxHealth", living.getMaxHealth());
        cage.addCapturedBird(bird);
        CarryOnHooks.clearCarriedEntity(player);
        return true;
    }
}
