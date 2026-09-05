package net.fodoth.skina.neoguanniao.content.nest;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.egg.BirdEggData;
import net.fodoth.skina.neoguanniao.content.egg.BirdEggItem;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoBlockEntityTypes;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoCriteriaTriggers;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoDataComponents;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.fodoth.skina.neoguanniao.content.nest.SimpleItemStackHandler;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;

/**
 * 鸟巢方块实体 - 管理鸟巢的库存、动画和鸟蛋孵化
 */
public class BirdNestBlockEntity extends BlockEntity implements Container, GeoBlockEntity {

    // ==================== 库存系统 ====================
    private final SimpleItemStackHandler itemHandler = new SimpleItemStackHandler(4) {

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }
        protected void onContentsChanged(int slot) {
            BirdNestBlockEntity.this.setChanged();
        }
    };

    // ==================== 动画相关 ====================
    private final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
    private final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");

    // ==================== 构造方法 ====================
    public BirdNestBlockEntity(BlockPos pos, BlockState state) {
        super(NeoGuanNiaoBlockEntityTypes.BIRD_NEST.get(), pos, state);
    }

    // ==================== Container 接口实现 ====================

    @Override
    public int getContainerSize() {
        return itemHandler.getSlots();
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            if (!itemHandler.getStackInSlot(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public @NotNull ItemStack getItem(int slot) {
        return itemHandler.getStackInSlot(slot);
    }

    @Override
    public @NotNull ItemStack removeItem(int slot, int amount) {
        ItemStack stack = itemHandler.getStackInSlot(slot);
        if (stack.isEmpty()) return ItemStack.EMPTY;

        ItemStack result = stack.split(amount);
        itemHandler.setStackInSlot(slot, stack);
        return result;
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = itemHandler.getStackInSlot(slot);
        itemHandler.setStackInSlot(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int slot, @NotNull ItemStack stack) {
        itemHandler.setStackInSlot(slot, stack);
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            itemHandler.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true; // 始终允许访问
    }

    @Override
    public int getMaxStackSize(@NotNull ItemStack stack) {
        return itemHandler.getSlotLimit(0);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return stack.is(NeoGuanNiaoItems.BIRD_EGG.get());
    }

    // ==================== GeoBlockEntity 接口实现 ====================

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 0,
                state -> state.setAndContinue(IDLE)
        ));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    // ==================== 辅助方法 ====================

    public SimpleItemStackHandler getItemHandler() {
        return itemHandler;
    }

    public boolean hasEmptySlot() {
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            if (itemHandler.getStackInSlot(i).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public void addEgg(ItemStack egg) {
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            if (itemHandler.getStackInSlot(i).isEmpty()) {
                itemHandler.setStackInSlot(i, egg.copyWithCount(1));
                return;
            }
        }
    }

    // ==================== 孵化逻辑 ====================

    public void tickEggs() {
        if (level == null || level.isClientSide) return;

        for (int i = 0; i < getContainerSize(); i++) {
            ItemStack egg = itemHandler.getStackInSlot(i);
            if (egg.isEmpty()) continue;

            BirdEggData data = BirdEggItem.getEggData(egg);
            if (data == null || !data.alive()) continue;

            BirdEggData newData = data.tickDown(20);

            if (newData.canHatch()) {
                hatchEgg(i, egg, newData);
            } else {
                egg.set(NeoGuanNiaoDataComponents.BIRD_EGG_DATA.get(), newData);
                setItem(i, egg);
            }
        }
    }

    private void hatchEgg(int slot, ItemStack egg, BirdEggData data) {
        if (level == null) return;

        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(data.birdType());
        if (!(type.create(level) instanceof AbstractBirdEntity<?> bird)) return;

        bird.moveTo(
                worldPosition.getX() + 0.5,
                worldPosition.getY() + 0.3,
                worldPosition.getZ() + 0.5,
                level.random.nextFloat() * 360,
                0
        );

        bird.applyEggData(data);
        bird.setAge(-24000);

        Component name = egg.get(DataComponents.CUSTOM_NAME);
        if (name != null) bird.setCustomName(name);

        level.addFreshEntity(bird);
        triggerHatchEggAdvancement();

        removeItemNoUpdate(slot);
        setChanged();
    }

    private void triggerHatchEggAdvancement() {
        if (!(level instanceof ServerLevel server)) return;

        server.getEntitiesOfClass(
                        ServerPlayer.class,
                        new AABB(worldPosition).inflate(16)
                )
                .forEach(player ->
                        NeoGuanNiaoCriteriaTriggers.HATCH_BIRD_EGG.get().trigger(player)
                );
    }

    // ==================== 数据持久化 ====================

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", itemHandler.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Inventory")) {
            itemHandler.deserializeNBT(registries, tag.getCompound("Inventory"));
        }
    }

    // ==================== 网络同步 ====================

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider provider) {
        return saveWithoutMetadata(provider);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
}

