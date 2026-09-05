package com.birdcamera.content.guide.layout;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * GUI 布局配置类
 * 存储屏幕布局的配置信息，包括基础尺寸和各个矩形区域的位置
 */
public record BirdGuideLayoutConfig(String screen, int baseWidth, int baseHeight, Map<String, BirdGuideLayoutRect> rects) {
    public BirdGuideLayoutConfig(String screen, int baseWidth, int baseHeight, Map<String, BirdGuideLayoutRect> rects) {
        this.screen = screen;
        this.baseWidth = baseWidth;
        this.baseHeight = baseHeight;
        this.rects = Map.copyOf(rects);
    }

    /**
     * 获取指定 ID 的矩形区域，并缩放到当前屏幕尺寸
     */
    public BirdGuideLayoutRect rect(String id, BirdGuideLayoutRect fallback, int screenWidth, int screenHeight) {
        if (this.baseWidth > 0 && this.baseHeight > 0) {
            BirdGuideLayoutRect raw = this.rects.get(id);
            if (raw != null && raw.isValid()) {
                float scaleX = (float) screenWidth / (float) this.baseWidth;
                float scaleY = (float) screenHeight / (float) this.baseHeight;
                BirdGuideLayoutRect scaled = raw.scale(scaleX, scaleY);
                return scaled.isValid() ? scaled : fallback;
            }
        }
        return fallback;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof BirdGuideLayoutConfig(String screen1, int width, int height, Map<String, BirdGuideLayoutRect> rects1))) return false;

        return baseWidth == width
                && baseHeight == height
                && screen.equals(screen1)
                && rects.equals(rects1);
    }

    @Override
    public @NotNull String toString() {
        return "BirdGuideLayoutConfig{" +
                "screen='" + screen + '\'' +
                ", baseWidth=" + baseWidth +
                ", baseHeight=" + baseHeight +
                ", rects=" + rects +
                '}';
    }
}