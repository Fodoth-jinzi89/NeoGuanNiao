package net.fodoth.skina.neoguanniao.client.guide.layout;

import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoDataComponents;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 鸟指南布局 Data Component 工具类
 */
public final class BirdGuideLayoutHelper {

    private BirdGuideLayoutHelper() {
    }

    /**
     * 从 ItemStack 加载布局配置
     *
     * @param stack 物品堆
     * @return 布局配置对象，如果不存在或无效则返回 null
     */
    public static BirdGuideLayoutConfig loadFromStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }

        BirdGuideLayoutData data = stack.get(NeoGuanNiaoDataComponents.BIRD_GUIDE_LAYOUT.get());
        if (data == null || !data.isValid()) {
            return null;
        }

        return new BirdGuideLayoutConfig(data.screen(), data.baseWidth(), data.baseHeight(), data.rects());
    }

    /**
     * 保存布局配置到 ItemStack
     *
     * @param stack  物品堆
     * @param config 布局配置
     * @return 是否保存成功
     */
    @SuppressWarnings("all")
    public static boolean saveToStack(ItemStack stack, BirdGuideLayoutConfig config) {
        if (stack == null || stack.isEmpty() || config == null) {
            return false;
        }

        if (config.baseWidth() <= 0 || config.baseHeight() <= 0 || config.rects().isEmpty()) {
            return false;
        }

        BirdGuideLayoutData data = new BirdGuideLayoutData(
                BirdGuideLayoutData.CURRENT_VERSION,
                config.baseWidth(),
                config.baseHeight(),
                config.screen(),
                config.rects()
        );

        stack.set(NeoGuanNiaoDataComponents.BIRD_GUIDE_LAYOUT.get(), data);
        return true;
    }

    /**
     * 从 ItemStack 移除布局配置
     *
     * @param stack 物品堆
     * @return 是否移除成功
     */
    public static boolean removeFromStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        return stack.remove(NeoGuanNiaoDataComponents.BIRD_GUIDE_LAYOUT.get()) != null;
    }

    /**
     * 检查 ItemStack 是否有布局配置
     *
     * @param stack 物品堆
     * @return 是否有配置
     */
    public static boolean hasLayoutData(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        return stack.has(NeoGuanNiaoDataComponents.BIRD_GUIDE_LAYOUT.get());
    }

    /**
     * 获取布局数据
     *
     * @param stack 物品堆
     * @return 布局数据，如果不存在则返回 null
     */
    public static BirdGuideLayoutData getLayoutData(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        return stack.get(NeoGuanNiaoDataComponents.BIRD_GUIDE_LAYOUT.get());
    }

    /**
     * 复制布局数据从一个物品到另一个物品
     *
     * @param fromStack 源物品堆
     * @param toStack   目标物品堆
     * @return 是否复制成功
     */
    public static boolean copyLayout(ItemStack fromStack, ItemStack toStack) {
        if (fromStack == null || fromStack.isEmpty() || toStack == null || toStack.isEmpty()) {
            return false;
        }

        BirdGuideLayoutData data = fromStack.get(NeoGuanNiaoDataComponents.BIRD_GUIDE_LAYOUT.get());
        if (data == null || !data.isValid()) {
            return false;
        }

        toStack.set(NeoGuanNiaoDataComponents.BIRD_GUIDE_LAYOUT.get(), data);
        return true;
    }

    /**
     * 创建默认布局配置
     *
     * @return 默认布局配置
     */
    public static BirdGuideLayoutConfig createDefaultLayout() {
        Map<String, BirdGuideLayoutRect> rects = new LinkedHashMap<>();

        // 默认布局区域（基于 1600x900 基础尺寸）
        rects.put("header", new BirdGuideLayoutRect(41, 21, 1520, 48));
        rects.put("main_panel", new BirdGuideLayoutRect(40, 90, 1520, 760));
        rects.put("species_header", new BirdGuideLayoutRect(70, 120, 312, 42));
        rects.put("species_list", new BirdGuideLayoutRect(64, 170, 315, 660));
        rects.put("detail_header", new BirdGuideLayoutRect(456, 116, 448, 72));
        rects.put("tag_area", new BirdGuideLayoutRect(456, 198, 448, 58));
        rects.put("info_card", new BirdGuideLayoutRect(455, 267, 450, 555));
        rects.put("preview_box", new BirdGuideLayoutRect(986, 172, 548, 430));
        rects.put("pose_buttons", new BirdGuideLayoutRect(986, 620, 548, 72));
        rects.put("close_button", new BirdGuideLayoutRect(1380, 790, 150, 42));

        return new BirdGuideLayoutConfig("bird_guide", 1600, 900, rects);
    }
}