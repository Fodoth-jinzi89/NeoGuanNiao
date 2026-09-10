package net.fodoth.skina.neoguanniao.content.cage;


import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoBlockEntityTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import org.jetbrains.annotations.NotNull;

import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;


public class BirdCageBlockEntity extends BlockEntity implements GeoBlockEntity {
    /** 笼中的实体 NBT，按捕捉顺序排列；取出时从末尾拿（后进先出）。 */
    private final List<CompoundTag> capturedBirds = new ArrayList<>();


    private final AnimatableInstanceCache animationCache =
            GeckoLibUtil.createInstanceCache(this);


    public BirdCageBlockEntity(
            BlockPos pos,
            BlockState state
    ) {
        super(
                NeoGuanNiaoBlockEntityTypes.BIRD_CAGE.get(),
                pos,
                state
        );
    }


    public BirdCageVariant variant() {

        Block block =
                getBlockState().getBlock();


        if (block instanceof BirdCageBlock birdCageBlock) {
            return birdCageBlock.variant();
        }


        return BirdCageVariant.SMALL;
    }

    public boolean isEmpty() {
        return capturedBirds.isEmpty();
    }

    public boolean isFull() {
        return capturedBirds.size() >= variant().capacity();
    }

    /** 笼中的实体 NBT 列表，下标即捕捉顺序；每只实体占哪个笼位存在 {@link #SLOT_KEY} 里。 */
    public List<CompoundTag> capturedBirds() {
        return capturedBirds;
    }

    /** 最后被装进笼子的实体（后进先出的那一个）。 */
    public CompoundTag lastCapturedBird() {
        return capturedBirds.isEmpty() ? null : capturedBirds.get(capturedBirds.size() - 1);
    }

    public void addCapturedBird(CompoundTag tag) {
        addCapturedBird(tag, -1);
    }

    /**
     * 追加一只实体，并让它优先占用 {@code preferredSlot} 号笼位；该笼位已被占用（或下标不合法）
     * 时退回编号最小的空闲笼位。列表顺序始终是捕捉顺序，取出时的“最后一只”不受笼位影响。
     */
    public void addCapturedBird(CompoundTag tag, int preferredSlot) {
        if (tag == null || tag.isEmpty()) return;
        CompoundTag copy = tag.copy();
        copy.putInt(SLOT_KEY, freeSlot(capturedBirds, variant().capacity(), preferredSlot));
        capturedBirds.add(copy);
        setChanged();
    }

    public CompoundTag removeLastCapturedBird() {
        return removeCapturedBird(capturedBirds.size() - 1);
    }

    /** 取出列表下标 {@code index} 处的实体；下标越界（含笼子为空）时返回 null。 */
    public CompoundTag removeCapturedBird(int index) {
        if (index < 0 || index >= capturedBirds.size()) return null;
        CompoundTag tag = capturedBirds.remove(index);
        setChanged();
        return tag;
    }

    /** 占着 {@code slot} 号笼位的实体在列表里的下标；该笼位是空的时返回 -1。 */
    public int indexOfSlot(int slot) {
        if (slot < 0) return -1;
        for (int i = 0; i < capturedBirds.size(); i++) {
            if (slotOf(capturedBirds.get(i), i) == slot) return i;
        }
        return -1;
    }

    /** 实体 NBT 里记录笼位的键；旧存档没有这个键，按下标处理。 */
    public static final String SLOT_KEY = "CageSlot";

    /** 实体所在的笼位下标；没有 {@link #SLOT_KEY} 的旧数据按列表下标处理。 */
    public static int slotOf(CompoundTag bird, int index) {
        return bird.contains(SLOT_KEY) ? bird.getInt(SLOT_KEY) : index;
    }

    /** 优先使用 {@code preferredSlot} 号笼位；它不空闲时退回编号最小的空闲笼位。 */
    public static int freeSlot(List<CompoundTag> birds, int capacity, int preferredSlot) {
        if (preferredSlot >= 0 && preferredSlot < capacity && !isSlotTaken(birds, preferredSlot)) return preferredSlot;
        for (int slot = 0; slot < capacity; slot++) {
            if (!isSlotTaken(birds, slot)) return slot;
        }
        return Math.max(capacity - 1, 0);
    }

    private static boolean isSlotTaken(List<CompoundTag> birds, int slot) {
        for (int i = 0; i < birds.size(); i++) {
            if (slotOf(birds.get(i), i) == slot) return true;
        }
        return false;
    }

    private long lastInteractTick = Long.MIN_VALUE;

    /**
     * 主手和副手会在同一个游戏刻各触发一次右键交互，这里保证每个游戏刻只处理一次。
     *
     * @return 本次交互是否应当生效
     */
    public boolean tryInteract(long gameTime) {
        if (lastInteractTick == gameTime) return false;
        lastInteractTick = gameTime;
        return true;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!capturedBirds.isEmpty()) {
            ListTag list = new ListTag();
            for (CompoundTag bird : capturedBirds) list.add(bird.copy());
            tag.put("CapturedBirds", list);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        capturedBirds.clear();
        if (tag.contains("CapturedBirds", Tag.TAG_LIST)) {
            ListTag list = tag.getList("CapturedBirds", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) capturedBirds.add(list.getCompound(i).copy());
        } else if (tag.contains("CapturedBird")) {
            // 兼容只存了一只鸟的旧存档。
            capturedBirds.add(tag.getCompound("CapturedBird").copy());
        }
        for (int i = 0; i < capturedBirds.size(); i++) {
            // 旧存档没有笼位信息，按列表下标补一份，之后取出中间的鸟也不会让其他鸟换笼位。
            CompoundTag bird = capturedBirds.get(i);
            if (!bird.contains(SLOT_KEY)) bird.putInt(SLOT_KEY, i);
        }
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        return saveWithoutMetadata(registries);
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


    @Override
    public void registerControllers(
            AnimatableManager.ControllerRegistrar controllers
    ) {

    }


    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animationCache;
    }
}
