package net.fodoth.skina.neoguanniao.content.nest;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.egg.BirdEggData;
import net.fodoth.skina.neoguanniao.content.egg.BirdEggItem;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoBlockEntityTypes;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoDataComponents;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
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
public class BirdNestBlockEntity extends BlockEntity implements GeoBlockEntity {

    // ==================== 动画相关 ====================

    /** 动画实例缓存 */
    private final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);

    /** 空闲动画循环 */
    private final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");

    // ==================== 库存系统 ====================

    /**
     * 4格鸟蛋库存处理器
     * - 每格最多1个物品
     * - 仅允许放入 BIRD_EGG
     */
    private final ItemStackHandler inventory = new ItemStackHandler(4) {
        @Override
        protected int getStackLimit(int slot, @NotNull ItemStack stack) {
            return 1; // 每格限1个
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.is(NeoGuanNiaoItems.BIRD_EGG.get()); // 仅接受鸟蛋
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged(); // 标记数据变更
        }
    };

    // ==================== 构造方法 ====================

    public BirdNestBlockEntity(BlockPos pos, BlockState state) {
        super(NeoGuanNiaoBlockEntityTypes.BIRD_NEST.get(), pos, state);
    }

    // ==================== 库存访问方法 ====================

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public ItemStack getItem(int slot) {
        return inventory.getStackInSlot(slot);
    }

    public void setItem(int slot, ItemStack stack) {
        inventory.setStackInSlot(slot, stack);
    }

    public int getContainerSize() {
        return inventory.getSlots();
    }

    public void clearContent() {
        for (int i = 0; i < inventory.getSlots(); i++) {
            inventory.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    // ==================== GeoBlockEntity 接口实现 ====================

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 0,
                state -> state.setAndContinue(IDLE) // 持续播放空闲动画
        ));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    // ==================== 数据持久化 ====================

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        tag.put("Inventory", inventory.serializeNBT(provider)); // 保存库存
        super.saveAdditional(tag, provider);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        super.loadAdditional(tag, provider);
        inventory.deserializeNBT(provider, tag.getCompound("Inventory")); // 加载库存
    }

    // ==================== 网络同步 ====================

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider provider) {
        CompoundTag tag = super.getUpdateTag(provider);
        tag.put("Inventory", inventory.serializeNBT(provider));
        return tag;
    }

    @Override
    public void handleUpdateTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        super.handleUpdateTag(tag, provider);
        if (tag.contains("Inventory")) {
            inventory.deserializeNBT(provider, tag.getCompound("Inventory"));
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    // ==================== 变更通知 ====================

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide) {
            // 通知客户端方块状态更新（渲染同步）
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    // ==================== 孵化逻辑 ====================

    /**
     * 每秒（20 tick）调用一次，更新所有鸟蛋的孵化进度
     * 由 BirdNestBlock.getTicker() 触发
     */
    public void tickEggs() {
        if (level == null || level.isClientSide) return;

        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack egg = inventory.getStackInSlot(i);
            if (egg.isEmpty()) continue;

            // 获取鸟蛋数据
            BirdEggData data = BirdEggItem.getEggData(egg);
            if (data == null || !data.alive()) continue;

            // 减少20 tick（1秒）的孵化时间
            BirdEggData newData = data.tickDown(20);

            if (newData.canHatch()) {
                // 孵化条件满足 → 生成小鸟
                hatchEgg(i, newData);
            } else {
                // 更新蛋的NBT数据
                egg.set(NeoGuanNiaoDataComponents.BIRD_EGG_DATA.get(), newData);
                inventory.setStackInSlot(i, egg);
            }
        }
    }

    /**
     * 孵化鸟蛋，生成小鸟实体
     * @param slot 鸟蛋所在的格子
     * @param data 鸟蛋数据（包含鸟类类型、皮肤、性别等信息）
     */
    private void hatchEgg(int slot, BirdEggData data) {
        if (level == null) return;

        // 根据鸟类类型ID获取实体类型
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(data.birdType());
        if (!(type.create(level) instanceof AbstractBirdEntity<?> bird)) return;

        // 设置出生位置（巢穴中心偏上）
        bird.moveTo(
                worldPosition.getX() + 0.5,
                worldPosition.getY() + 0.3,
                worldPosition.getZ() + 0.5,
                level.random.nextFloat() * 360, // 随机朝向
                0
        );

        // 应用蛋里保存的遗传数据（皮肤、体型、性别等）
        bird.applyEggData(data);

        // 设置为幼年（-24000 tick = 20分钟成长时间）
        bird.setAge(-24000);

        // 生成到世界
        level.addFreshEntity(bird);

        // 清除该格的蛋
        inventory.setStackInSlot(slot, ItemStack.EMPTY);
        setChanged();

        // 通知客户端更新
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }
}