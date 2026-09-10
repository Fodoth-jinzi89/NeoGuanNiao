package net.fodoth.skina.neoguanniao.content.cage;


import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoBlockEntityTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

import org.jetbrains.annotations.NotNull;

import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.util.GeckoLibUtil;


public class BirdCageBlockEntity extends BlockEntity implements GeoBlockEntity {
    private CompoundTag capturedBird;


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
        return capturedBird == null;
    }

    public CompoundTag capturedBird() {
        return capturedBird;
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean setCapturedBird(CompoundTag tag) {
        this.capturedBird = tag == null || tag.isEmpty() ? null : tag.copy();
        setChanged();
        return true;
    }

    public CompoundTag removeCapturedBird() {
        CompoundTag tag = capturedBird;
        capturedBird = null;
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
        if (capturedBird != null) tag.put("CapturedBird", capturedBird);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        capturedBird = tag.contains("CapturedBird") ? tag.getCompound("CapturedBird").copy() : null;
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
