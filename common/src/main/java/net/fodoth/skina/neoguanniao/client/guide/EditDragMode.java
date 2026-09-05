package net.fodoth.skina.neoguanniao.client.guide;

public enum EditDragMode {
    NONE(false, false, false, false),
    MOVE(false, false, false, false),
    RESIZE_LEFT(true, false, false, false),
    RESIZE_RIGHT(false, true, false, false),
    RESIZE_TOP(false, false, true, false),
    RESIZE_BOTTOM(false, false, false, true),
    RESIZE_TOP_LEFT(true, false, true, false),
    RESIZE_TOP_RIGHT(false, true, true, false),
    RESIZE_BOTTOM_LEFT(true, false, false, true),
    RESIZE_BOTTOM_RIGHT(false, true, false, true);

    public final boolean left;
    public final boolean right;
    public final boolean top;
    public final boolean bottom;

    EditDragMode(boolean left, boolean right, boolean top, boolean bottom) {
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
    }
}