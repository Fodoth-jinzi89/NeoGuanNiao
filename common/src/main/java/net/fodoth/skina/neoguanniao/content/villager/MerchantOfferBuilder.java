package net.fodoth.skina.neoguanniao.content.villager;


import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;


/**
 * MerchantOffer 扩展构造工具
 *<p>
 * 用于设置:
 * - 实际交易数据
 * - GUI显示ItemStack
 *<p>
 * 原版 MerchantOffer:
 * costA / costB / result
 * 使用 ItemCost 保存数据，但不会保存 ItemStack 的 DataComponent。
 *<p>
 * 本类通过 Mixin 扩展显示层。
 */
public class MerchantOfferBuilder {


    private final MerchantOffer offer;


    private MerchantOfferBuilder(
            MerchantOffer offer
    ) {
        this.offer = offer;
    }



    public static MerchantOfferBuilder of(
            MerchantOffer offer
    ) {
        return new MerchantOfferBuilder(offer);
    }



    /**
     * 设置主要购买物品显示
     *<p>
     * 对应:
     * MerchantOffer.getCostA()
     */
    public MerchantOfferBuilder displayCost(
            ItemStack stack
    ) {

        ((MerchantOfferAccessor) offer)
                .neoguanniao$setDisplayCost(
                        stack
                );

        return this;
    }



    /**
     * 设置第二购买物品显示
     *<p>
     * 对应:
     * MerchantOffer.getCostB()
     */
    public MerchantOfferBuilder displayCostB(
            ItemStack stack
    ) {

        ((MerchantOfferAccessor) offer)
                .neoguanniao$setCostBDisplay(
                        stack
                );

        return this;
    }



    /**
     * 设置出售物品显示
     *<p>
     * 对应:
     * MerchantOffer.getResult()
     */
    public MerchantOfferBuilder displayResult(
            ItemStack stack
    ) {

        ((MerchantOfferAccessor) offer)
                .neoguanniao$setResultDisplay(
                        stack
                );

        return this;
    }



    /**
     * 设置全部显示层
     */
    public MerchantOfferBuilder display(
            ItemStack cost,
            ItemStack costB,
            ItemStack result
    ) {

        MerchantOfferAccessor accessor =
                (MerchantOfferAccessor) offer;


        accessor.neoguanniao$setDisplayCost(cost);

        accessor.neoguanniao$setCostBDisplay(costB);

        accessor.neoguanniao$setResultDisplay(result);


        return this;
    }



    public MerchantOffer build() {
        return offer;
    }

}