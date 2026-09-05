package com.birdcamera.content.guide;

import net.minecraft.world.item.ItemStack;

/**
 * 图鉴打开桥接 - 由客户端实现并注册，避免服务端加载 client-only 类
 */
public interface BirdGuideOpener {

    Holder HOLDER = new Holder();

    void openGuide(ItemStack stack);

    class Holder {
        public BirdGuideOpener opener = stack -> {
        };
    }

    static void register(BirdGuideOpener opener) {
        if (opener != null) {
            HOLDER.opener = opener;
        }
    }

    static BirdGuideOpener get() {
        return HOLDER.opener;
    }
}