package com.birdcamera.content.cage;

import com.birdcamera.registry.BirdCameraBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * 鸟笼方块实体 - 存储笼内鸟类信息，支持GeckoLib动画渲染
 */
public class BirdCageBlockEntity extends BlockEntity implements GeoBlockEntity, Container, MenuProvider {

    private static final int SLOT_COUNT = 9;
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation OPEN_ANIM = RawAnimation.begin().thenPlay("open").thenLoop("idle_open");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);

    private BirdCageVariant variant = BirdCageVariant.SMALL;
    private boolean isOpen;
    private int birdCount;
    private String occupiedBirdType = "";

    public BirdCageBlockEntity(BlockPos pos, BlockState state) {
        super(BirdCameraBlockEntityTypes.BIRD_CAGE, pos, state);
        if (state.hasProperty(BirdCageBlock.VARIANT)) {
            this.variant = state.getValue(BirdCageBlock.VARIANT);
        }
    }

    // ======== 服务端tick逻辑 ========

    public static void serverTick(Level level, BlockPos pos, BlockState state, BirdCageBlockEntity entity) {
        entity.tickOccupation();
    }

    protected void tickOccupation() {
        int count = 0;
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (!items.get(i).isEmpty()) {
                count++;
            }
        }
        if (count != birdCount) {
            birdCount = count;
            setChanged();
        }
    }

    // ======== GeckoLib动画 ========

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 5, state -> {
            if (isOpen) {
                return state.setAndContinue(OPEN_ANIM);
            }
            return state.setAndContinue(IDLE_ANIM);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    // ======== getter/setter ========

    public BirdCageVariant getVariant() {
        return variant;
    }

    public void setVariant(BirdCageVariant variant) {
        this.variant = variant;
        setChanged();
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        this.isOpen = open;
        setChanged();
    }

    public int getBirdCount() {
        return birdCount;
    }

    public String getOccupiedBirdType() {
        return occupiedBirdType;
    }

    public void setOccupiedBirdType(String type) {
        this.occupiedBirdType = type;
        setChanged();
    }

    // ======== Container接口 ========

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        if (slot >= 0 && slot < SLOT_COUNT) {
            return items.get(slot);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack stack = getItem(slot);
        if (!stack.isEmpty()) {
            ItemStack result = stack.split(amount);
            if (stack.isEmpty()) {
                items.set(slot, ItemStack.EMPTY);
            }
            setChanged();
            return result;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = getItem(slot);
        if (!stack.isEmpty()) {
            items.set(slot, ItemStack.EMPTY);
            return stack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot >= 0 && slot < SLOT_COUNT) {
            items.set(slot, stack);
            setChanged();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        items.clear();
        setChanged();
    }

    // ======== MenuProvider ========

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.birdcamera.bird_cage");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return ChestMenu.threeRows(containerId, playerInventory, this);
    }

    // ======== NBT读写 ========

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        tag.putString("Variant", variant.getSerializedName());
        tag.putBoolean("IsOpen", isOpen);
        tag.putInt("BirdCount", birdCount);
        tag.putString("OccupiedBirdType", occupiedBirdType);

        ListTag itemsList = new ListTag();
        for (int i = 0; i < SLOT_COUNT; i++) {
            ItemStack stack = items.get(i);
            if (!stack.isEmpty()) {
                CompoundTag itemTag = (CompoundTag) stack.save(registries);
                itemTag.putByte("Slot", (byte) i);
                itemsList.add(itemTag);
            }
        }
        tag.put("Items", itemsList);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        variant = BirdCageVariant.byName(tag.getString("Variant"));
        isOpen = tag.getBoolean("IsOpen");
        birdCount = tag.getInt("BirdCount");
        occupiedBirdType = tag.getString("OccupiedBirdType");

        items.clear();
        ListTag itemsList = tag.getList("Items", 10);
        for (int i = 0; i < itemsList.size(); i++) {
            CompoundTag itemTag = itemsList.getCompound(i);
            int slot = itemTag.getByte("Slot") & 255;
            if (slot >= 0 && slot < SLOT_COUNT) {
                items.set(slot, ItemStack.parseOptional(registries, itemTag));
            }
        }
    }

    // ======== 工具方法 ========

    public void scatterContents(Level level, BlockPos pos) {
        Containers.dropContents(level, pos, this);
    }
}
