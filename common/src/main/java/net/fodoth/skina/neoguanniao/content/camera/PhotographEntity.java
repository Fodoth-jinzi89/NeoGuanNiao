package net.fodoth.skina.neoguanniao.content.camera;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PhotographEntity
extends HangingEntity {
    /** 相框每格 16 像素（1 格），与原版画一致。 */
    public static final int FRAME_CELL_PIXELS = 16;

    private static final EntityDataAccessor<ItemStack> DATA_ITEM = SynchedEntityData.defineId(PhotographEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Integer> DATA_ROTATION = SynchedEntityData.defineId(PhotographEntity.class, EntityDataSerializers.INT);

    public PhotographEntity(EntityType<? extends PhotographEntity> entityType, Level level) {
        super(entityType, level);
    }

    public PhotographEntity(Level level, BlockPos pos, Direction direction, ItemStack photograph) {
        super(NeoGuanNiaoEntityTypes.PHOTOGRAPH.get(), level, pos);
        this.setDirection(direction);
        this.setItem(photograph);
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_ITEM, ItemStack.EMPTY);
        builder.define(DATA_ROTATION, 0);
    }

    public void onSyncedDataUpdated(@NotNull EntityDataAccessor<?> key) {
        if (DATA_ITEM.equals(key)) {
            this.onItemChanged(this.getItem());
        }
        super.onSyncedDataUpdated(key);
    }

    public void recreateFromPacket(@NotNull ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        int data = packet.getData();
        Direction direction = Direction.from3DDataValue(data);
        if (!direction.getAxis().isHorizontal()) {
            NeoGuanNiao.LOGGER.warn("Ignoring invalid photograph facing {} in spawn packet", data);
            direction = Direction.SOUTH;
        }
        this.setDirection(direction);
    }

    @Override
    @NotNull
    public Packet<ClientGamePacketListener> getAddEntityPacket(@NotNull ServerEntity serverEntity) {
        return new ClientboundAddEntityPacket((Entity)this, this.direction.get3DDataValue(), this.getPos());
    }

    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (!this.getItem().isEmpty()) {
            tag.put("Item", this.getItem().save(this.registryAccess()));
        }
        tag.putByte("Facing", (byte)this.direction.get3DDataValue());
        tag.putByte("Rotation", (byte)this.getRotation());
    }

    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        ItemStack item = ItemStack.parseOptional(this.registryAccess(), tag.getCompound("Item"));
        if (!item.isEmpty()) {
            this.setItem(item);
        }
        this.setDirection(Direction.from3DDataValue((int)tag.getByte("Facing")));
        this.setRotation(tag.getByte("Rotation"));
    }

    protected float getEyeHeight(@NotNull Pose pose, @NotNull EntityDimensions dimensions) {
        return 0.0f;
    }

    public int getWidth() {
        return PhotographData.frameSize(this.getItem()) * FRAME_CELL_PIXELS;
    }

    public int getHeight() {
        return PhotographData.frameSize(this.getItem()) * FRAME_CELL_PIXELS;
    }

    @Nullable
    public ItemStack getPickResult() {
        return this.getItem().copy();
    }

    /**
     * 挂载几何：相框占 size×size 格，玩家点击的方块是整张相片的左下角（向视线右侧与上方展开）。
     * 是否放得下（空间与支撑面）交给继承自 {@link HangingEntity} 的原版画判定。
     */
    protected @NotNull AABB calculateBoundingBox(BlockPos pos, Direction direction) {
        int size = PhotographData.frameSize(this.getItem());
        // 几何中心相对锚点（左下角方块的中心）偏移 size/2 - 0.5 格。
        double offset = size / 2.0 - 0.5;
        Vec3 center = Vec3.atCenterOf(pos).relative(direction, -0.46875);
        Vec3 origin = center.relative(direction.getCounterClockWise(), offset).relative(Direction.UP, offset);
        double width = direction.getAxis() == Direction.Axis.X ? 0.0625 : size;
        double depth = direction.getAxis() == Direction.Axis.Z ? 0.0625 : size;
        return AABB.ofSize(origin, width, size, depth);
    }

    public ItemStack getItem() {
        return (ItemStack)this.getEntityData().get(DATA_ITEM);
    }

    public void setItem(ItemStack stack) {
        this.getEntityData().set(DATA_ITEM, stack);
    }

    private void onItemChanged(ItemStack stack) {
        if (!stack.isEmpty()) {
            stack.setEntityRepresentation((Entity)this);
        }
        this.recalculateBoundingBox();
    }

    public int getRotation() {
        return (Integer)this.getEntityData().get(DATA_ROTATION);
    }

    public void setRotation(int rotation) {
        this.getEntityData().set(DATA_ROTATION, rotation & 3);
    }

    @NotNull
    public InteractionResult interact(@NotNull Player player, @NotNull InteractionHand hand) {
        if (!this.level().isClientSide) {
            this.setRotation(this.getRotation() + 1);
            this.playSound(SoundEvents.ITEM_FRAME_ROTATE_ITEM, 0.8f, 1.0f);
        }
        return InteractionResult.SUCCESS;
    }

    public boolean hurt(@NotNull DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        }
        if (!this.isRemoved() && !this.level().isClientSide) {
            this.dropItem(source.getEntity());
            this.kill();
            this.markHurt();
        }
        return true;
    }

    public void dropItem(@Nullable Entity breaker) {
        Player player;
        this.playSound(SoundEvents.ITEM_FRAME_BREAK, 0.8f, 1.0f);
        if (breaker instanceof Player && (player = (Player)breaker).isCreative()) {
            return;
        }
        ItemStack item = this.getItem();
        if (!item.isEmpty()) {
            ItemStack drop = item.copy();
            drop.setCount(1);
            this.spawnAtLocation(drop);
        }
    }

    public void playPlacementSound() {
        this.playSound(SoundEvents.ITEM_FRAME_PLACE, 0.8f, 1.0f);
    }
}

