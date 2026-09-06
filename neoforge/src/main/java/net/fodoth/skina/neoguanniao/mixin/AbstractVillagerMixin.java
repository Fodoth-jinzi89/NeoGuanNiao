package net.fodoth.skina.neoguanniao.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(AbstractVillager.class)
public abstract class AbstractVillagerMixin {


    @Unique
    private static final String NEOGUANNIAO_DISPLAY =
            "NeoGuanNiaoOfferDisplays";


    /**
     * 保存额外显示ItemStack
     */
    @Inject(
            method = "addAdditionalSaveData",
            at = @At("TAIL")
    )
    private void neoguanniao$saveDisplay(
            CompoundTag tag,
            CallbackInfo ci
    ) {

        AbstractVillager villager =
                (AbstractVillager)(Object)this;

        if (villager.level().isClientSide()) {
            return;
        }

        MerchantOffers offers = villager.getOffers();


        ListTag list = new ListTag();


        for (MerchantOffer offer : offers) {
            if (!hasDisplayAccessors(offer)) continue;

            CompoundTag offerTag = new CompoundTag();

            ItemStack display =
                    (ItemStack) invoke(offer, "neoguanniao$getDisplayCost");

            ItemStack costB =
                    (ItemStack) invoke(offer, "neoguanniao$getCostBDisplay");

            ItemStack result =
                    (ItemStack) invoke(offer, "neoguanniao$getResultDisplay");


            if (display != null) {
                offerTag.put(
                        "DisplayCost",
                        display.save(villager.registryAccess())
                );
            }


            if (costB != null) {
                offerTag.put(
                        "CostBDisplay",
                        costB.save(villager.registryAccess())
                );
            }


            if (result != null) {
                offerTag.put(
                        "ResultDisplay",
                        result.save(villager.registryAccess())
                );
            }


            list.add(offerTag);
        }


        tag.put(
                NEOGUANNIAO_DISPLAY,
                list
        );
    }



    /**
     * 读取额外显示ItemStack
     */
    @Inject(
            method = "readAdditionalSaveData",
            at = @At("TAIL")
    )
    private void neoguanniao$loadDisplay(
            CompoundTag tag,
            CallbackInfo ci
    ) {


        if (!tag.contains(
                NEOGUANNIAO_DISPLAY
        )) {
            return;
        }


        AbstractVillager villager =
                (AbstractVillager)(Object)this;

        if (villager.level().isClientSide()) {
            return;
        }

        MerchantOffers offers =
                villager.getOffers();



        ListTag list =
                tag.getList(
                        NEOGUANNIAO_DISPLAY,
                        CompoundTag.TAG_COMPOUND
                );


        int size =
                Math.min(
                        list.size(),
                        offers.size()
                );


        for (int i = 0; i < size; i++) {



            CompoundTag offerTag =
                    list.getCompound(i);


            MerchantOffer offer =
                    offers.get(i);

            if (!hasDisplayAccessors(offer)) {
                continue;
            }


            if (offerTag.contains("DisplayCost")) {

                ItemStack stack =
                        ItemStack.parseOptional(
                                villager.registryAccess(),
                                offerTag.getCompound("DisplayCost")
                        );

                invoke(offer, "neoguanniao$setDisplayCost", stack);
            }



            if (offerTag.contains("CostBDisplay")) {

                ItemStack stack =
                        ItemStack.parseOptional(
                                villager.registryAccess(),
                                offerTag.getCompound("CostBDisplay")
                        );

                invoke(offer, "neoguanniao$setCostBDisplay", stack);
            }



            if (offerTag.contains("ResultDisplay")) {

                ItemStack stack =
                        ItemStack.parseOptional(
                                villager.registryAccess(),
                                offerTag.getCompound("ResultDisplay")
                        );

                invoke(offer, "neoguanniao$setResultDisplay", stack);
            }
        }
    }

    @Unique
    private static boolean hasDisplayAccessors(MerchantOffer offer) {
        try {
            offer.getClass().getMethod("neoguanniao$getDisplayCost");
            return true;
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    @Unique
    private static Object invoke(MerchantOffer offer, String name, Object... args) {
        try {
            for (var method : offer.getClass().getMethods()) {
                if (method.getName().equals(name)) return method.invoke(offer, args);
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return null;
    }
}
