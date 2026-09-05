package net.fodoth.skina.neoguanniao.mixin;

import net.fodoth.skina.neoguanniao.content.villager.MerchantOfferAccessor;
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
            CompoundTag compoundTag,
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
            if (!(offer instanceof MerchantOfferAccessor accessor)) {
                continue;
            }

            CompoundTag offerTag = new CompoundTag();

            ItemStack display =
                    accessor.neoguanniao$getDisplayCost();

            ItemStack costB =
                    accessor.neoguanniao$getCostBDisplay();

            ItemStack result =
                    accessor.neoguanniao$getResultDisplay();


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


        compoundTag.put(
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
            CompoundTag compoundTag,
            CallbackInfo ci
    ) {


        if (!compoundTag.contains(
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
                compoundTag.getList(
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

            if (!(offer instanceof MerchantOfferAccessor accessor)) {
                continue;
            }


            if (offerTag.contains("DisplayCost")) {

                ItemStack stack =
                        ItemStack.parseOptional(
                                villager.registryAccess(),
                                offerTag.getCompound("DisplayCost")
                        );

                accessor.neoguanniao$setDisplayCost(stack);
            }



            if (offerTag.contains("CostBDisplay")) {

                ItemStack stack =
                        ItemStack.parseOptional(
                                villager.registryAccess(),
                                offerTag.getCompound("CostBDisplay")
                        );

                accessor.neoguanniao$setCostBDisplay(stack);
            }



            if (offerTag.contains("ResultDisplay")) {

                ItemStack stack =
                        ItemStack.parseOptional(
                                villager.registryAccess(),
                                offerTag.getCompound("ResultDisplay")
                        );

                accessor.neoguanniao$setResultDisplay(stack);
            }
        }
    }
}