package net.fodoth.skina.neoguanniao.client.gui.layout;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * GUI 布局配置类
 * 存储屏幕布局的配置信息，包括基础尺寸和各个矩形区域的位置
 */
public record GuiLayoutConfig(String screen, int baseWidth, int baseHeight, Map<String, GuiLayoutRect> rects) {
    public GuiLayoutConfig(String screen, int baseWidth, int baseHeight, Map<String, GuiLayoutRect> rects) {
        this.screen = screen;
        this.baseWidth = baseWidth;
        this.baseHeight = baseHeight;
        this.rects = Map.copyOf(rects);
    }

    /**
     * 获取指定 ID 的矩形区域，并缩放到当前屏幕尺寸
     *
     * @param id           矩形区域标识符
     * @param fallback     如果找不到或无效时的默认值
     * @param screenWidth  当前屏幕宽度
     * @param screenHeight 当前屏幕高度
     * @return 缩放后的矩形区域
     */
    public GuiLayoutRect rect(String id, GuiLayoutRect fallback, int screenWidth, int screenHeight) {
        if (this.baseWidth > 0 && this.baseHeight > 0) {
            GuiLayoutRect raw = this.rects.get(id);
            if (raw != null && raw.isValid()) {
                float scaleX = (float) screenWidth / (float) this.baseWidth;
                float scaleY = (float) screenHeight / (float) this.baseHeight;
                GuiLayoutRect scaled = raw.scale(scaleX, scaleY);
                return scaled.isValid() ? scaled : fallback;
            }
        }
        return fallback;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof GuiLayoutConfig(String screen1, int width, int height, Map<String, GuiLayoutRect> rects1))) return false;

        return baseWidth == width
                && baseHeight == height
                && screen.equals(screen1)
                && rects.equals(rects1);
    }

    @Override
    public @NotNull String toString() {
        return "GuiLayoutConfig{" +
                "screen='" + screen + '\'' +
                ", baseWidth=" + baseWidth +
                ", baseHeight=" + baseHeight +
                ", rects=" + rects +
                '}';
    }
}