package net.fodoth.skina.neoguanniao.client.guide;

import net.fodoth.skina.neoguanniao.client.guide.layout.BirdGuideLayoutConfig;
import net.fodoth.skina.neoguanniao.client.guide.layout.BirdGuideLayoutHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BirdGuideClient {
    private static ItemStack currentGuideStack;

    public static void open(ItemStack stack) {
        currentGuideStack = stack;
        // 从物品 Data Component 加载布局
        BirdGuideLayoutConfig config = BirdGuideLayoutHelper.loadFromStack(stack);
        if (config == null) {
            // 如果没有自定义布局，使用默认布局
            config = BirdGuideLayoutHelper.createDefaultLayout();
        }
        // 打开 GUI
        Minecraft.getInstance().setScreen(new BirdGuideScreen(config));
    }

    /**
     * 获取当前物品堆
     *
     * @return 当前物品堆
     */
    public static ItemStack getCurrentGuideStack() {
        return currentGuideStack;
    }
}