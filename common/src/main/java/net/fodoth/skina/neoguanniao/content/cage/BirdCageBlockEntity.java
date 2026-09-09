package net.fodoth.skina.neoguanniao.content.cage;


import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoBlockEntityTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

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
    public void registerControllers(
            AnimatableManager.ControllerRegistrar controllers
    ) {

    }


    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animationCache;
    }
}
