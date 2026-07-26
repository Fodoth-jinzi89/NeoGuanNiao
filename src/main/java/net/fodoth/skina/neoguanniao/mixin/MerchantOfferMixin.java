package net.fodoth.skina.neoguanniao.mixin;

import net.fodoth.skina.neoguanniao.content.villager.MerchantOfferAccessor;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;


@Mixin(MerchantOffer.class)
public class MerchantOfferMixin implements MerchantOfferAccessor {

    @Unique
    private ItemStack neoguanniao$displayCost;

    @Unique
    private ItemStack neoguanniao$costBDisplay;

    @Unique
    private ItemStack neoguanniao$resultDisplay;

    @Override
    public ItemStack neoguanniao$getDisplayCost() {
        return neoguanniao$displayCost;
    }

    @Override
    public void neoguanniao$setDisplayCost(ItemStack stack) {
        this.neoguanniao$displayCost = stack;
    }

    @Override
    public ItemStack neoguanniao$getCostBDisplay() {
        return neoguanniao$costBDisplay;
    }

    @Override
    public void neoguanniao$setCostBDisplay(ItemStack stack) {
        this.neoguanniao$costBDisplay = stack;
    }

    @Override
    public ItemStack neoguanniao$getResultDisplay() {
        return neoguanniao$resultDisplay;
    }

    @Override
    public void neoguanniao$setResultDisplay(ItemStack stack) {
        this.neoguanniao$resultDisplay = stack;
    }

    @SuppressWarnings("all")
    @Inject(
            method = "<init>(Lnet/minecraft/world/item/trading/ItemCost;Ljava/util/Optional;Lnet/minecraft/world/item/ItemStack;IIZIIFI)V",
            at = @At("TAIL")
    )
    private void neoguanniao$init(
            ItemCost baseCostA, Optional costB, ItemStack result, int _uses, int maxUses, boolean rewardExp, int specialPriceDiff, int demand, float priceMultiplier, int xp, CallbackInfo ci
    ) {
        this.neoguanniao$displayCost = null;
        this.neoguanniao$costBDisplay = null;
        this.neoguanniao$resultDisplay = null;
    }

    @Inject(
            method = "<init>(Lnet/minecraft/world/item/trading/MerchantOffer;)V",
            at = @At("TAIL")
    )
    private void neoguanniao$copy(
            MerchantOffer other,
            CallbackInfo ci
    ) {
        MerchantOfferAccessor accessor = (MerchantOfferAccessor) other;
        this.neoguanniao$displayCost = accessor.neoguanniao$getDisplayCost();
        this.neoguanniao$costBDisplay = accessor.neoguanniao$getCostBDisplay();
        this.neoguanniao$resultDisplay = accessor.neoguanniao$getResultDisplay();
    }

    /*
     * 覆盖GUI显示
     */

    @Inject(
            method = "getBaseCostA",
            at = @At("HEAD"),
            cancellable = true
    )
    private void neoguanniao$getBaseCostA(
            CallbackInfoReturnable<ItemStack> cir
    ) {
        if (neoguanniao$displayCost != null) {
            cir.setReturnValue(neoguanniao$displayCost.copy());
        }
    }

    @Inject(
            method = "getCostA",
            at = @At("HEAD"),
            cancellable = true
    )
    private void neoguanniao$getCostA(
            CallbackInfoReturnable<ItemStack> cir
    ) {
        if (neoguanniao$displayCost != null) {
            cir.setReturnValue(neoguanniao$displayCost.copy());
        }
    }

    @Inject(
            method = "getCostB",
            at = @At("HEAD"),
            cancellable = true
    )
    private void neoguanniao$getCostB(
            CallbackInfoReturnable<ItemStack> cir
    ) {
        if (neoguanniao$costBDisplay != null) {
            cir.setReturnValue(neoguanniao$costBDisplay.copy());
        }
    }

    @Inject(
            method = "getResult",
            at = @At("HEAD"),
            cancellable = true
    )
    private void neoguanniao$getResult(
            CallbackInfoReturnable<ItemStack> cir
    ) {
        if (neoguanniao$resultDisplay != null) {
            cir.setReturnValue(neoguanniao$resultDisplay.copy());
        }
    }

    @Inject(
            method = "writeToStream",
            at = @At("TAIL")
    )
    private static void neoguanniao$writeDisplay(
            RegistryFriendlyByteBuf buffer,
            MerchantOffer offer,
            CallbackInfo ci
    ) {
        MerchantOfferAccessor accessor = (MerchantOfferAccessor) offer;

        ItemStack display = accessor.neoguanniao$getDisplayCost();
        buffer.writeBoolean(display != null);
        if (display != null) {
            ItemStack.STREAM_CODEC.encode(buffer, display);
        }

        ItemStack costBDisplay = accessor.neoguanniao$getCostBDisplay();
        buffer.writeBoolean(costBDisplay != null);
        if (costBDisplay != null) {
            ItemStack.STREAM_CODEC.encode(buffer, costBDisplay);
        }

        ItemStack resultDisplay = accessor.neoguanniao$getResultDisplay();
        buffer.writeBoolean(resultDisplay != null);
        if (resultDisplay != null) {
            ItemStack.STREAM_CODEC.encode(buffer, resultDisplay);
        }
    }

    @Inject(
            method = "createFromStream",
            at = @At("RETURN")
    )
    private static void neoguanniao$readDisplay(
            RegistryFriendlyByteBuf buffer,
            CallbackInfoReturnable<MerchantOffer> cir
    ) {
        MerchantOffer offer = cir.getReturnValue();
        MerchantOfferAccessor accessor = (MerchantOfferAccessor) offer;

        if (buffer.readBoolean()) {
            ItemStack display = ItemStack.STREAM_CODEC.decode(buffer);
            accessor.neoguanniao$setDisplayCost(display);
        }

        if (buffer.readBoolean()) {
            ItemStack costBDisplay = ItemStack.STREAM_CODEC.decode(buffer);
            accessor.neoguanniao$setCostBDisplay(costBDisplay);
        }

        if (buffer.readBoolean()) {
            ItemStack resultDisplay = ItemStack.STREAM_CODEC.decode(buffer);
            accessor.neoguanniao$setResultDisplay(resultDisplay);
        }
    }
}