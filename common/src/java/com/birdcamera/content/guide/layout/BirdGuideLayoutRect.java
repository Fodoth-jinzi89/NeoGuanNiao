package com.birdcamera.content.guide.layout;

/**
 * GUI 布局矩形区域
 *
 * @param x 左上角 X 坐标
 * @param y 左上角 Y 坐标
 * @param w 宽度
 * @param h 高度
 */
public record BirdGuideLayoutRect(int x, int y, int w, int h) {

    public int right() {
        return this.x + this.w;
    }

    public int bottom() {
        return this.y + this.h;
    }

    public int centerX() {
        return this.x + this.w / 2;
    }

    public int centerY() {
        return this.y + this.h / 2;
    }

    public BirdGuideLayoutRect scale(float scaleX, float scaleY) {
        return new BirdGuideLayoutRect(
                Math.round((float) this.x * scaleX),
                Math.round((float) this.y * scaleY),
                Math.round((float) this.w * scaleX),
                Math.round((float) this.h * scaleY)
        );
    }

    public BirdGuideLayoutRect inset(int amount) {
        int newX = this.x + amount;
        int newY = this.y + amount;
        int newW = Math.max(0, this.w - amount * 2);
        int newH = Math.max(0, this.h - amount * 2);
        return new BirdGuideLayoutRect(newX, newY, newW, newH);
    }

    public boolean contains(double mouseX, double mouseY) {
        return mouseX >= (double) this.x
                && mouseX <= (double) this.right()
                && mouseY >= (double) this.y
                && mouseY <= (double) this.bottom();
    }

    public boolean isValid() {
        return this.w > 0 && this.h > 0;
    }

    public static BirdGuideLayoutRect copyOf(BirdGuideLayoutRect other) {
        return new BirdGuideLayoutRect(other.x, other.y, other.w, other.h);
    }

    public static BirdGuideLayoutRect empty() {
        return new BirdGuideLayoutRect(0, 0, 0, 0);
    }

    public static BirdGuideLayoutRect of(int x, int y, int w, int h) {
        return new BirdGuideLayoutRect(x, y, w, h);
    }
}