package com.birdcamera.mixin;

import com.birdcamera.content.villager.MerchantOfferAccessor;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * MerchantOffer Mixin - 覆盖GUI显示和网络同步
 * 允许自定义交易显示的物品与实际交易物品不同
 * 1.21.1 中 MerchantOffer 使用 CODEC 序列化（无 createTag/CompoundTag 构造器），
 * 因此显示数据通过 writeToStream/createFromStream 在客户端同步，
 * 并由 AbstractVillagerMixin 负责持久化到村民 NBT。
 */
@Mixin(MerchantOffer.class)
public class MerchantOfferMixin implements MerchantOfferAccessor {

    @Unique
    private ItemStack birdcamera$displayCostA = ItemStack.EMPTY;
    @Unique
    private ItemStack birdcamera$displayCostB = ItemStack.EMPTY;
    @Unique
    private ItemStack birdcamera$displayResult = ItemStack.EMPTY;

    @Override
    public ItemStack birdcamera$getDisplayCost() {
        return birdcamera$displayCostA;
    }

    @Override
    public void birdcamera$setDisplayCost(ItemStack stack) {
        this.birdcamera$displayCostA = stack.copy();
    }

    @Override
    public ItemStack birdcamera$getCostBDisplay() {
        return birdcamera$displayCostB;
    }

    @Override
    public void birdcamera$setCostBDisplay(ItemStack stack) {
        this.birdcamera$displayCostB = stack.copy();
    }

    @Override
    public ItemStack birdcamera$getResultDisplay() {
        return birdcamera$displayResult;
    }

    @Override
    public void birdcamera$setResultDisplay(ItemStack stack) {
        this.birdcamera$displayResult = stack.copy();
    }

    /**
     * 覆盖GUI中显示的成本物品A
     */
    @Inject(method = "getCostA", at = @At("HEAD"), cancellable = true)
    private void birdcamera$overrideGetCostA(CallbackInfoReturnable<ItemStack> cir) {
        if (!birdcamera$displayCostA.isEmpty()) {
            cir.setReturnValue(birdcamera$displayCostA);
        }
    }

    /**
     * 覆盖GUI中显示的成本物品B
     */
    @Inject(method = "getCostB", at = @At("HEAD"), cancellable = true)
    private void birdcamera$overrideGetCostB(CallbackInfoReturnable<ItemStack> cir) {
        if (!birdcamera$displayCostB.isEmpty()) {
            cir.setReturnValue(birdcamera$displayCostB);
        }
    }

    /**
     * 覆盖GUI中显示的结果物品
     */
    @Inject(method = "getResult", at = @At("HEAD"), cancellable = true)
    private void birdcamera$overrideGetResult(CallbackInfoReturnable<ItemStack> cir) {
        if (!birdcamera$displayResult.isEmpty()) {
            cir.setReturnValue(birdcamera$displayResult);
        }
    }

    /**
     * 将自定义显示数据写入网络缓冲区（同步到客户端）
     */
    @Inject(method = "writeToStream(Lnet/minecraft/network/RegistryFriendlyByteBuf;Lnet/minecraft/world/item/trading/MerchantOffer;)V", at = @At("TAIL"))
    private static void birdcamera$writeDisplay(RegistryFriendlyByteBuf buffer, MerchantOffer offer, CallbackInfo ci) {
        MerchantOfferAccessor accessor = (MerchantOfferAccessor) offer;
        ItemStack display = accessor.birdcamera$getDisplayCost();
        buffer.writeBoolean(!display.isEmpty());
        if (!display.isEmpty()) {
            ItemStack.STREAM_CODEC.encode(buffer, display);
        }
        ItemStack costBDisplay = accessor.birdcamera$getCostBDisplay();
        buffer.writeBoolean(!costBDisplay.isEmpty());
        if (!costBDisplay.isEmpty()) {
            ItemStack.STREAM_CODEC.encode(buffer, costBDisplay);
        }
        ItemStack resultDisplay = accessor.birdcamera$getResultDisplay();
        buffer.writeBoolean(!resultDisplay.isEmpty());
        if (!resultDisplay.isEmpty()) {
            ItemStack.STREAM_CODEC.encode(buffer, resultDisplay);
        }
    }

    /**
     * 从网络缓冲区恢复自定义显示数据（客户端收到后恢复）
     */
    @Inject(method = "createFromStream(Lnet/minecraft/network/RegistryFriendlyByteBuf;)Lnet/minecraft/world/item/trading/MerchantOffer;", at = @At("RETURN"))
    private static void birdcamera$readDisplay(RegistryFriendlyByteBuf buffer, CallbackInfoReturnable<MerchantOffer> cir) {
        MerchantOffer offer = cir.getReturnValue();
        MerchantOfferAccessor accessor = (MerchantOfferAccessor) offer;
        if (buffer.readBoolean()) {
            accessor.birdcamera$setDisplayCost(ItemStack.STREAM_CODEC.decode(buffer));
        }
        if (buffer.readBoolean()) {
            accessor.birdcamera$setCostBDisplay(ItemStack.STREAM_CODEC.decode(buffer));
        }
        if (buffer.readBoolean()) {
            accessor.birdcamera$setResultDisplay(ItemStack.STREAM_CODEC.decode(buffer));
        }
    }
}