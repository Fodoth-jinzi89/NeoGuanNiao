package net.fodoth.skina.neoguanniao.content.cage;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.platform.CarryOnHooks;
import net.fodoth.skina.neoguanniao.platform.FluidBucketHooks;
import net.fodoth.skina.neoguanniao.platform.LiquidContainerHooks;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoBlockEntityTypes;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.ArrayList;
import java.util.List;

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


    /**
     * 形状贴着模型：中/大型鸟笼占地 3x3（48 像素），模型比一格大得多，所以每格只取模型真正占到的那一段，
     * 边上的占位方块往内收，选中的框和右键的判定范围就都跟模型对齐了。
     * <p>
     * 收进去的宽度按模型的实际外沿算：中型 x 到 ±16、z 到 ±14.6，大型 x 到 ±24、z 到 ±15.5（像素），
     * 减去中间那格占的 ±8 就是要收的量。纵向仍按整格给，模型每一列都占满这一格的高度。
     * </p>
     */
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
            int localZ = dx * facing.getOpposite().getStepX() + dz * facing.getOpposite().getStepZ();
            int inset = variant == BirdCageVariant.LARGE ? 0 : 8;
            int zInset = variant == BirdCageVariant.LARGE ? 8 : 9;
            double minX = localX == -1 ? inset : 0;
            double maxX = localX == 1 ? 16 - inset : 16;
            double minZ = localZ == -1 ? zInset : 0;
            double maxZ = localZ == 1 ? 16 - zInset : 16;
            double[] bounds = rotateBounds(facing, minX, minZ, maxX, maxZ);
            return Block.box(bounds[0], 0, bounds[1], bounds[2], 16, bounds[3]);
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

    /** 只有原点那个部件带方块实体，所以只有它会拿到这个 ticker。 */
    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            @NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type
    ) {
        return createTickerHelper(type, NeoGuanNiaoBlockEntityTypes.BIRD_CAGE.get(), BirdCageBlock::serverTick);
    }

    private static void serverTick(Level level, BlockPos pos, BlockState state, BirdCageBlockEntity cage) {
        if (level.isClientSide) return;
        BirdCageSimulation.serverTick(level, cage);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        if (!hasRoomFor(context)) return null;
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    /**
     * 放置体积（3×{@link #structureHeight()}×3）是否都能被替换。
     * <p>
     * 这里只做无副作用的判断：{@code getStateForPlacement} 也会在客户端预测等场合被调用，
     * 放置失败提示改由 {@link BirdCageItem#place} 在服务端发出。
     * </p>
     */
    public boolean hasRoomFor(@NotNull BlockPlaceContext context) {
        if (variant == BirdCageVariant.SMALL) return true;
        Direction facing = context.getHorizontalDirection().getOpposite();
        BlockPos origin = context.getClickedPos();
        int height = structureHeight();
        for (int x = -1; x <= 1; x++)
            for (int y = 0; y < height; y++)
                for (int z = -1; z <= 1; z++) {
                    BlockPos pos = offset(origin, facing, x, y, z);
                    if (!context.getLevel().getBlockState(pos).canBeReplaced(context)) return false;
                }
        return true;
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
            List<CompoundTag> birds = BirdCageItem.capturedBirds(stack);
            // 逐个保留物品里记下的笼位，避免放下来之后笼中站位被打乱。
            for (int i = 0; i < birds.size(); i++) {
                cage.addCapturedBird(birds.get(i), BirdCageBlockEntity.slotOf(birds.get(i), i));
            }
            // 还原破坏时存进物品里的物品/流体。
            cage.readStorageFromItem(stack, level.registryAccess());
            // 方块实体会从鸟笼物品继承一份 custom_data 镜像，清掉它，别让鸟数据在方块数据里留两份。
            cage.clearMirroredItemComponents();
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

    /**
     * 破坏鸟笼时把笼中的实体和物品/流体写回鸟笼物品，避免这些内容随方块一起消失或掉一地。
     * <p>
     * 中/大型鸟笼破坏任一部件都会走到原点方块的掉落，因此这里按方块实体统一处理；
     * 笼位信息存在每只实体的 NBT 里，随物品重新放置时会原样恢复。
     * </p>
     */
    @Override
    protected @NotNull List<ItemStack> getDrops(@NotNull BlockState state, @NotNull LootParams.Builder params) {
        BirdCageBlockEntity cage = !state.getValue(PART)
                && params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof BirdCageBlockEntity blockEntity
                ? blockEntity : null;
        List<ItemStack> drops = new ArrayList<>();
        if (cage != null && !cage.isEmpty()) {
            ItemStack stack = new ItemStack(this);
            ListTag list = new ListTag();
            for (CompoundTag bird : cage.capturedBirds()) list.add(bird.copy());
            CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.put("CapturedBirds", list));
            drops.add(stack);
        } else {
            drops.addAll(super.getDrops(state, params));
        }
        // 笼内的物品/流体写进鸟笼物品，破坏时随物品一起带走，而不是掉一地。
        if (cage != null && cage.getLevel() != null) {
            for (ItemStack drop : drops) {
                if (drop.is(this.asItem())) cage.writeStorageToItem(drop, cage.getLevel().registryAccess());
            }
        }
        return drops;
    }

    /**
     * 从结构里任意一格（包括占位方块）取出带方块实体的原点那个鸟笼。
     * <p>
     * 中/大型鸟笼的占位方块没有方块实体，交互和自动化（漏斗、管道）都必须先回到原点那一格。
     * </p>
     */
    public static @Nullable BirdCageBlockEntity cageAt(BlockGetter level, BlockPos pos, BlockState state) {
        if (!(state.getBlock() instanceof BirdCageBlock cageBlock)) return null;
        BlockPos origin = state.getValue(PART) ? cageBlock.findOrigin(level, pos, state.getValue(FACING)) : pos;
        return origin != null && level.getBlockEntity(origin) instanceof BirdCageBlockEntity cage ? cage : null;
    }


    /**
     * 从结构里任意一格找回带方块实体的原点那格。
     * <p>
     * 纵向要上下都找：食槽和水槽在模型上位于原点那一格的上方，点到的往往是原点上面的占位方块，
     * 只往上扫就会漏掉真正的原点。
     * </p>
     */
    private @Nullable BlockPos findOrigin(BlockGetter level, BlockPos pos, Direction facing) {
        int height = structureHeight();
        for (int x = -1; x <= 1; x++)
            for (int y = -(height - 1); y < height; y++)
                for (int z = -1; z <= 1; z++) {
                    BlockPos origin = offset(pos, facing, -x, -y, -z);
                    BlockState originState = level.getBlockState(origin);
                    if (originState.is(this) && !originState.getValue(PART)) return origin;
                }
        return null;
    }

    public int structureHeight() {
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

    /**
     * 手持物品右键鸟笼：
     * <ul>
     *     <li>手里拿着鸟食点食盆：加进食物槽；</li>
     *     <li>空手点食盆：取出一组鸟食，从食盆那一侧飞到笼子外面；</li>
     *     <li>任意流体容器点水槽：把水槽里的流体抽进容器；</li>
     *     <li>桶：把桶里的流体倒进空着或未满的水槽。</li>
     * </ul>
     * 潜行时不处理，把这次右键留给 {@link #useWithoutItem} 放出笼中鸟；空手掏鸟羽也在那边。
     */
    @Override
    public @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, Level level,
                                                   @NotNull BlockPos pos, @NotNull Player player,
                                                   @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        BirdCageBlockEntity cage = cageAt(level, pos, state);
        if (cage == null) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        BlockPos origin = cage.getBlockPos();
        Direction facing = state.getValue(FACING);

        // 食盆：手里拿着鸟食就加料，空手就把鸟食取出来。只认点中的那一个盆，一次只动它对应的那个槽。
        BirdCageVariant.PotLayer foodPot = BirdCageLoot.potAt(cage, facing, origin, hit, true);
        if (foodPot != null) {
            if (!stack.isEmpty() && BirdCageLoot.isFood(stack)) {
                if (!level.isClientSide) {
                    BirdCageLoot.addFood(cage, player, stack, foodPot.storageIndex());
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
            if (stack.isEmpty() && BirdCageLoot.hasFood(cage, foodPot.storageIndex())) {
                if (!level.isClientSide) {
                    BirdCageLoot.takeFood(level, origin, cage, player, facing, hit.getDirection(), foodPot.storageIndex());
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
            // 盆里没东西就往下走，让收鸟羽接手。
        }

        // 流体容器：把点中的那个水盆抽进手里的容器。
        BirdCageVariant.PotLayer fluidPot = BirdCageLoot.potAt(cage, facing, origin, hit, false);
        if (fluidPot != null
                && LiquidContainerHooks.drainIntoContainer(
                        cage.fluidTank(fluidPot.storageIndex()), player, hand, level.isClientSide) > 0) {
            if (!level.isClientSide) {
                cage.setChanged();
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        // 再按原来的规则：桶里的流体倒进空着或未满的水槽。
        return fillTankFromBucket(stack, level, origin, player, hand, cage);
    }

    /**
     * 把手里流体桶的流体倒进鸟笼的水槽，桶换成空桶还给玩家。
     * <p>
     * 多个水槽时优先找空槽，其次找装着同一种流体且没满的槽；一次倒一桶（{@link SimpleFluidTank#BUCKET_VOLUME} mB），
     * 槽装不下的部分就没了。
     * 都装不下（槽里是别的流体）就什么都不做，让交互继续往下走。
     * </p>
     */
    private ItemInteractionResult fillTankFromBucket(ItemStack stack, Level level, BlockPos origin,
                                                     Player player, InteractionHand hand, BirdCageBlockEntity cage) {
        if (!(stack.getItem() instanceof BucketItem)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        ResourceLocation fluidId = FluidBucketHooks.bucketFluid(stack);
        if (fluidId == null) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        SimpleFluidTank target = null;
        boolean sameFluidFull = false;
        for (SimpleFluidTank tank : cage.fluidTanks()) {
            if (tank.isEmpty()) {
                target = tank;
                break;
            }
            if (!fluidId.equals(tank.getFluidId())) continue;
            if (tank.getAmount() < tank.getCapacity()) {
                target = tank;
                break;
            }
            sameFluidFull = true;
        }
        if (target == null) {
            // 装的是同一种流体但已经满了：吃掉这次右键，别让桶接着把流体倒到地上。
            return sameFluidFull
                    ? ItemInteractionResult.sidedSuccess(level.isClientSide)
                    : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!level.isClientSide) {
            target.fill(fluidId, SimpleFluidTank.BUCKET_VOLUME);
            cage.setChanged();
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
                ItemStack emptyBucket = new ItemStack(Items.BUCKET);
                if (stack.isEmpty()) {
                    player.setItemInHand(hand, emptyBucket);
                } else if (!player.getInventory().add(emptyBucket)) {
                    player.drop(emptyBucket, false);
                }
            }
            level.playSound(null, origin, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hit) {
        BirdCageBlockEntity cage = cageAt(level, pos, state);
        if (cage == null) return InteractionResult.PASS;
        BlockPos origin = cage.getBlockPos();
        // 装笼放在去重标记之前：空手右键时 storeCarriedEntity 不会消耗标记，而 Fabric 没有
        // NeoForge 的 RightClickBlock 预处理，抱着实体装笼只能靠这里（NeoForge 侧仍由事件抢先处理）。
        if (!cage.isFull() && storeCarriedEntity(level, pos, player)) {
            return InteractionResult.sidedSuccess(false);
        }
        // 主手和副手会在同一个游戏刻各触发一次交互，避免同一次右键被处理两次。
        if (!cage.tryInteract(level.getGameTime())) return InteractionResult.PASS;
        if (player.isShiftKeyDown() && !cage.isEmpty()) {
            // 优先抱出/放出玩家视线命中的那个笼位上的鸟；没命中（或该笼位是空的）就退回最后装进去的一只。
            int index = cage.indexOfSlot(hitSlot(origin, cage.variant(), state.getValue(FACING), player));
            if (index < 0) index = cage.capturedBirds().size() - 1;
            if (!level.isClientSide) {
                CompoundTag preview = cage.capturedBirds().get(index);
                Entity carriedEntity = EntityType.create(preview, level).orElse(null);
                if (carriedEntity != null && CarryOnHooks.isLoaded()) {
                    // Carry On 会检查实体与玩家的距离，先把实体挪到玩家身上再交给它。
                    carriedEntity.moveTo(player.getX(), player.getY(), player.getZ());
                    if (CarryOnHooks.tryCarryEntity(player, carriedEntity)) {
                        cage.removeCapturedBird(index);
                        return InteractionResult.sidedSuccess(false);
                    }
                }
            }
            if (!level.isClientSide) {
                CompoundTag bird = cage.removeCapturedBird(index);
                Entity entity = EntityType.create(bird, level).orElse(null);
                if (entity != null) {
                    Vec3 spawn = findReleasePos(level, origin, state.getValue(FACING), entity);
                    entity.moveTo(spawn.x, spawn.y, spawn.z, player.getYRot(), 0.0F);
                    level.addFreshEntity(entity);
                    // 刚放出来的鸟先落地站定一段时间，不要一出笼就起飞。
                    if (entity instanceof AbstractBirdEntity<?> releasedBird) {
                        releasedBird.settleAfterRelease();
                    }
                } else {
                    cage.addCapturedBird(bird, BirdCageBlockEntity.slotOf(bird, index));
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        // 收产量槽里的鸟羽，落在点击的那一面外侧；手上拿着东西也照收，潜行时留给上面放鸟。
        if (!player.isShiftKeyDown()) {
            if (level.isClientSide) {
                return BirdCageLoot.hasOutput(cage)
                        ? InteractionResult.sidedSuccess(true)
                        : InteractionResult.PASS;
            }
            return BirdCageLoot.takeOutput(level, origin, cage, player, hit.getDirection())
                    ? InteractionResult.sidedSuccess(false)
                    : InteractionResult.PASS;
        }
        return InteractionResult.PASS;
    }

    /**
     * 找笼子外侧的放出位置。
     * <p>
     * 笼子占地 3×3，实体放在笼内会和占位方块重合，因此从笼子正面开始绕笼一圈，隔一格找一个外侧位置：
     * 用高度图取该列的地面高度，并要求实体的碰撞箱放得下。优先取与笼底高度接近的地面，
     * 一个可用位置都没有时退回笼子中心（旧行为）。
     * </p>
     */
    private Vec3 findReleasePos(Level level, BlockPos origin, Direction facing, Entity entity) {
        // 中/大型鸟笼占地 3×3，外墙在 origin ±1，再多隔一格（±2）才在笼子外面；小型鸟笼只有 origin 一格，贴边即可。
        int distance = variant == BirdCageVariant.SMALL ? 1 : 2;
        Vec3 firstValid = null;
        Direction side = facing;
        for (int i = 0; i < 4; i++) {
            int x = origin.getX() + side.getStepX() * distance;
            int z = origin.getZ() + side.getStepZ() * distance;
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            Vec3 pos = new Vec3(x + 0.5D, y, z + 0.5D);
            if (y > level.getMinBuildHeight()
                    && !level.getBlockState(new BlockPos(x, y - 1, z)).isAir()
                    && level.noCollision(entity, entity.getDimensions(entity.getPose()).makeBoundingBox(pos))) {
                if (Math.abs(y - origin.getY()) <= 2) {
                    return pos;
                }
                if (firstValid == null) {
                    firstValid = pos;
                }
            }
            side = side.getClockWise();
        }
        if (firstValid != null) {
            return firstValid;
        }
        double centerY = origin.getY() + structureHeight() * 0.5D - entity.getBbHeight() * 0.5D;
        return new Vec3(origin.getX() + 0.5D, centerY, origin.getZ() + 0.5D);
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
        if (!(cageBlock.asItem() instanceof BirdCageItem item)) return false;
        Entity carried = CarryOnHooks.carriedEntity(player);
        if (carried == null || !item.canFit(carried) || !BirdCageItem.canCapture(carried)) return false;
        if (!cage.tryInteract(level.getGameTime())) return false;
        CompoundTag bird = new CompoundTag();
        carried.saveWithoutId(bird);
        bird.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(carried.getType()).toString());
        if (carried instanceof LivingEntity living) bird.putFloat("MaxHealth", living.getMaxHealth());
        // 优先放进玩家视线命中的空笼位，没命中就退回追加到末尾。
        cage.addCapturedBird(bird, hitSlot(origin, cageBlock.variant(), state.getValue(FACING), player));
        CarryOnHooks.clearCarriedEntity(player);
        return true;
    }

    /**
     * 玩家视线命中的笼位下标（多个候选取最近的一个）；什么都没命中时返回 -1。
     * 笼位体积按支撑面上方一个 0.6 宽、0.5 高的盒子估算，大致就是笼中实体占据的空间。
     */
    private static int hitSlot(BlockPos origin, BirdCageVariant variant, Direction facing, Player player) {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getViewVector(1.0F).scale(player.blockInteractionRange()));
        int best = -1;
        double bestDistance = Double.MAX_VALUE;
        for (int slot = 0; slot < variant.capacity(); slot++) {
            Vec3 perch = variant.slotPos(origin, facing, slot);
            AABB box = new AABB(perch.x - 0.3D, perch.y, perch.z - 0.3D,
                    perch.x + 0.3D, perch.y + 0.5D, perch.z + 0.3D);
            var hit = box.clip(eye, end);
            if (hit.isEmpty()) continue;
            double distance = hit.get().distanceToSqr(eye);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = slot;
            }
        }
        return best;
    }
}
