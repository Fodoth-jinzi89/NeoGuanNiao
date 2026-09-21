package net.fodoth.skina.neoguanniao.content.cage;


import net.fodoth.skina.neoguanniao.content.nest.SimpleItemStackHandler;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoBlockEntityTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
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

    /** 产量槽的单格上限。 */
    public static final int OUTPUT_STACK_LIMIT = 64;
    /** 物品栏在方块实体 / 鸟笼物品 NBT 里的键。 */
    public static final String INVENTORY_KEY = "Inventory";
    /** 水槽在方块实体 / 鸟笼物品 NBT 里的键前缀，后面接槽位下标（多个水槽时用）。 */
    public static final String TANK_KEY = "FluidTank";

    private final SimpleItemStackHandler itemHandler;
    private final List<SimpleFluidTank> fluidTanks;

    /** 下一次检查笼中鸟（产毛、成长）的游戏刻；见 {@link #nextBirdCheck()}。 */
    private long nextBirdCheck;


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
        BirdCageVariant.Storage storage = variant().storage();
        this.itemHandler = new SimpleItemStackHandler(storage.foodSlots() + storage.outputSlots());
        this.fluidTanks = new ArrayList<>(storage.tanks());
        for (int tank = 0; tank < storage.tanks(); tank++) {
            this.fluidTanks.add(new SimpleFluidTank(storage.tankCapacity()));
        }
    }


    public BirdCageVariant variant() {

        Block block =
                getBlockState().getBlock();


        if (block instanceof BirdCageBlock birdCageBlock) {
            return birdCageBlock.variant();
        }


        return BirdCageVariant.SMALL;
    }

    /** 物品栏：前面的食物槽只能输入，后面的产量槽只能输出。 */
    public SimpleItemStackHandler itemHandler() {
        return itemHandler;
    }

    /** 水槽；小型/中型各 1 个，大型 2 个。 */
    public List<SimpleFluidTank> fluidTanks() {
        return fluidTanks;
    }

    /** 第 {@code index} 个水槽；下标越界时退回第一个。 */
    public SimpleFluidTank fluidTank(int index) {
        return fluidTanks.get(Math.min(Math.max(index, 0), fluidTanks.size() - 1));
    }

    /** 产量槽的第一格（也就是食物槽的个数）与个数。 */
    public int outputSlotFirst() {
        return variant().outputSlotFirst();
    }

    public int outputSlotCount() {
        return variant().outputSlotCount();
    }

    public int foodStackLimit() {
        return variant().foodStackLimit();
    }

    /** 食物槽是物品栏里排在产量槽前面的那几格。 */
    public int foodSlotCount() {
        return variant().outputSlotFirst();
    }

    /** 每只鸟的产毛倒计时和年龄都存在它自己的 NBT 里，逻辑见 {@link BirdCageSimulation}。 */
    public void markTimersDirty() {
        // 倒计时每刻都在变，但它只写进 NBT、不用同步给客户端，所以只标脏、不发方块更新。
        super.setChanged();
    }

    /**
     * 下一次检查笼中鸟（产毛、成长）的游戏刻。
     * <p>
     * 只是调度用的，不用存盘：重载后从 0 开始，下一刻照常检查一次。
     * </p>
     */
    public long nextBirdCheck() {
        return nextBirdCheck;
    }

    public void setNextBirdCheck(long gameTime) {
        this.nextBirdCheck = gameTime;
    }

    /** 三种规格都有对外暴露的物品/流体存储，布局见 {@link BirdCageVariant#storage()}。 */
    public boolean supportsCageStorage() {
        return true;
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
        return capturedBirds.isEmpty() ? null : capturedBirds.getLast();
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
        // 被取走这只鸟的物品镜像也要一起清掉，否则方块数据里还会留着它。
        clearMirroredItemComponents();
        setChanged();
        return tag;
    }

    /**
     * 清掉方块实体从鸟笼物品继承来的数据组件。
     * <p>
     * 放置方块时 {@code BlockItem} 会调用 {@code BlockEntity.applyComponentsFromItemStack}，把鸟笼物品的
     * {@code minecraft:custom_data}（里面是 CapturedBirds）镜像进方块数据的 {@code components} 里。
     * 这份镜像是多余的——笼中鸟的唯一数据源是 {@link #capturedBirds}——而且不随鸟被取走而清除，
     * 所以这里直接清空继承来的组件，保证鸟离开后方块数据里不会再出现它的 NBT。
     * </p>
     */
    public void clearMirroredItemComponents() {
        setComponents(DataComponentMap.EMPTY);
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

    /**
     * 破坏鸟笼时把笼内的物品/流体写进鸟笼物品，随物品一起带走而不是掉一地。
     * 内容为空时什么都不写，保持物品 NBT 干净。
     */
    public void writeStorageToItem(ItemStack stack, HolderLookup.Provider registries) {
        CompoundTag inventory = itemHandler.serializeNBT(registries);
        List<CompoundTag> tanks = new ArrayList<>(fluidTanks.size());
        boolean anyTank = false;
        for (SimpleFluidTank tank : fluidTanks) {
            CompoundTag serialized = tank.serializeNBT();
            anyTank |= !serialized.isEmpty();
            tanks.add(serialized);
        }
        if (inventory.isEmpty() && !anyTank) return;
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            if (!inventory.isEmpty()) tag.put(INVENTORY_KEY, inventory);
            for (int index = 0; index < tanks.size(); index++) {
                if (!tanks.get(index).isEmpty()) tag.put(TANK_KEY + index, tanks.get(index));
            }
        });
    }

    /** 放置鸟笼时把鸟笼物品里存的物品/流体还原回笼内。 */
    public void readStorageFromItem(ItemStack stack, HolderLookup.Provider registries) {
        CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (data.contains(INVENTORY_KEY)) itemHandler.deserializeNBT(registries, data.getCompound(INVENTORY_KEY));
        for (int index = 0; index < fluidTanks.size(); index++) {
            if (data.contains(TANK_KEY + index)) {
                fluidTanks.get(index).deserializeNBT(data.getCompound(TANK_KEY + index));
            }
        }
    }

    /**
     * 笼中最后一只鸟被取走后不会再写入任何数据，更新标签变为空。
     * NeoForge 的 {@code IBlockEntityExtension.onDataPacket} 默认实现会跳过空标签，
     * 客户端就会一直保留已取走那只鸟的渲染，所以这里始终写入 {@code CapturedBirds}
     * （允许为空列表），保证同步标签永远非空。
     */
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ListTag list = new ListTag();
        for (CompoundTag bird : capturedBirds) list.add(bird.copy());
        tag.put("CapturedBirds", list);
        tag.put(INVENTORY_KEY, itemHandler.serializeNBT(registries));
        for (int index = 0; index < fluidTanks.size(); index++) {
            tag.put(TANK_KEY + index, fluidTanks.get(index).serializeNBT());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        itemHandler.deserializeNBT(registries, tag.getCompound(INVENTORY_KEY));
        for (int index = 0; index < fluidTanks.size(); index++) {
            fluidTanks.get(index).deserializeNBT(tag.getCompound(TANK_KEY + index));
        }
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
