package net.fodoth.skina.neoguanniao.content.guide;

import net.fodoth.skina.neoguanniao.client.guide.BirdGuideClient;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class BirdGuideItem extends Item {
    public BirdGuideItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(
            @NotNull Level level,
            @NotNull Player player,
            @NotNull InteractionHand hand
    ) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            BirdGuideClient.open();
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}