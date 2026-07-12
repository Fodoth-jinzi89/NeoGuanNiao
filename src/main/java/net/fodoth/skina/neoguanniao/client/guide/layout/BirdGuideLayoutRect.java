package net.fodoth.skina.neoguanniao.client.guide.layout;

/**
 * GUI 布局矩形区域
 * 使用 Record 类型存储不可变的矩形数据
 *
 * @param x 左上角 X 坐标
 * @param y 左上角 Y 坐标
 * @param w 宽度
 * @param h 高度
 */
public record BirdGuideLayoutRect(int x, int y, int w, int h) {

    /**
     * 获取右边界坐标
     *
     * @return 右边界 X 坐标
     */
    public int right() {
        return this.x + this.w;
    }

    /**
     * 获取下边界坐标
     *
     * @return 下边界 Y 坐标
     */
    public int bottom() {
        return this.y + this.h;
    }

    /**
     * 获取中心点 X 坐标
     *
     * @return 中心点 X 坐标
     */
    public int centerX() {
        return this.x + this.w / 2;
    }

    /**
     * 获取中心点 Y 坐标
     *
     * @return 中心点 Y 坐标
     */
    public int centerY() {
        return this.y + this.h / 2;
    }

    /**
     * 缩放矩形
     *
     * @param scaleX X 轴缩放比例
     * @param scaleY Y 轴缩放比例
     * @return 缩放后的新矩形
     */
    public BirdGuideLayoutRect scale(float scaleX, float scaleY) {
        return new BirdGuideLayoutRect(
                Math.round((float) this.x * scaleX),
                Math.round((float) this.y * scaleY),
                Math.round((float) this.w * scaleX),
                Math.round((float) this.h * scaleY)
        );
    }

    /**
     * 向内缩进矩形
     *
     * @param amount 缩进量（正值向内缩进，负值向外扩展）
     * @return 缩进后的新矩形
     */
    public BirdGuideLayoutRect inset(int amount) {
        int newX = this.x + amount;
        int newY = this.y + amount;
        int newW = Math.max(0, this.w - amount * 2);
        int newH = Math.max(0, this.h - amount * 2);
        return new BirdGuideLayoutRect(newX, newY, newW, newH);
    }

    /**
     * 判断点是否在矩形内
     *
     * @param mouseX 鼠标 X 坐标
     * @param mouseY 鼠标 Y 坐标
     * @return 如果点在矩形内则返回 true
     */
    public boolean contains(double mouseX, double mouseY) {
        return mouseX >= (double) this.x
                && mouseX <= (double) this.right()
                && mouseY >= (double) this.y
                && mouseY <= (double) this.bottom();
    }

    /**
     * 检查矩形是否有效（宽度和高度大于 0）
     *
     * @return 如果矩形有效则返回 true
     */
    public boolean isValid() {
        return this.w > 0 && this.h > 0;
    }

    /**
     * 创建从另一个矩形复制的新矩形
     *
     * @param other 要复制的矩形
     * @return 新的矩形实例
     */
    public static BirdGuideLayoutRect copyOf(BirdGuideLayoutRect other) {
        return new BirdGuideLayoutRect(other.x, other.y, other.w, other.h);
    }

    /**
     * 创建宽高为 0 的空矩形
     *
     * @return 空矩形
     */
    public static BirdGuideLayoutRect empty() {
        return new BirdGuideLayoutRect(0, 0, 0, 0);
    }

    /**
     * 创建指定位置和大小的矩形
     *
     * @param x 左上角 X 坐标
     * @param y 左上角 Y 坐标
     * @param w 宽度
     * @param h 高度
     * @return 新矩形
     */
    public static BirdGuideLayoutRect of(int x, int y, int w, int h) {
        return new BirdGuideLayoutRect(x, y, w, h);
    }
}