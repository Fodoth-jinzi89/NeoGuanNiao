package com.birdcamera.content.guide;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class BirdGuideItem extends Item {

    public BirdGuideItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            // 打开观鸟指南GUI（由客户端桥接实现，避免服务端加载 client-only 类）
            BirdGuideOpener.get().openGuide(stack);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}