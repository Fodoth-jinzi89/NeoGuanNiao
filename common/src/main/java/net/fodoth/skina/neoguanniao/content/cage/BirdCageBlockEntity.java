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

    /** 笼中的实体 NBT 列表，下标即捕捉顺序。 */
    public List<CompoundTag> capturedBirds() {
        return capturedBirds;
    }

    /** 最后被装进笼子的实体（后进先出的那一个）。 */
    public CompoundTag lastCapturedBird() {
        return capturedBirds.isEmpty() ? null : capturedBirds.get(capturedBirds.size() - 1);
    }

    public void addCapturedBird(CompoundTag tag) {
        if (tag == null || tag.isEmpty()) return;
        capturedBirds.add(tag.copy());
        setChanged();
    }

    public CompoundTag removeLastCapturedBird() {
        if (capturedBirds.isEmpty()) return null;
        CompoundTag tag = capturedBirds.remove(capturedBirds.size() - 1);
        setChanged();
        return tag;
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
