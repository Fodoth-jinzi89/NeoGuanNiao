package net.fodoth.skina.neoguanniao.content.bath;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import net.minecraft.world.level.block.Block;

import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;


public class BirdBathItem extends BlockItem implements GeoItem, Equipable {

    private final BirdBathVariant variant;

    private final AnimatableInstanceCache animationCache =
            GeckoLibUtil.createInstanceCache(this);


    public BirdBathItem(
            BirdBathVariant variant,
            Block block,
            Item.Properties properties
    ) {
        super(block, properties);
        this.variant = variant;
    }


    public BirdBathVariant variant() {
        return this.variant;
    }

    @Override public @NotNull EquipmentSlot getEquipmentSlot() { return EquipmentSlot.HEAD; }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        return swapWithEquipmentSlot(this, level, player, hand);
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
