package net.fodoth.skina.neoguanniao.content.villager;

import net.minecraft.world.item.ItemStack;

public interface MerchantOfferAccessor {

    ItemStack neoguanniao$getDisplayCost();

    void neoguanniao$setDisplayCost(ItemStack stack);


    ItemStack neoguanniao$getCostBDisplay();

    void neoguanniao$setCostBDisplay(ItemStack stack);


    ItemStack neoguanniao$getResultDisplay();

    void neoguanniao$setResultDisplay(ItemStack stack);

}