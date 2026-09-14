package net.fodoth.skina.neoguanniao.compat.modonomicon.client.pages;

import com.klikli_dev.modonomicon.client.gui.book.entry.BookEntryScreen;
import com.klikli_dev.modonomicon.client.render.page.BookTextPageRenderer;
import com.klikli_dev.modonomicon.client.render.page.PageRendererRegistry;
import net.fodoth.skina.neoguanniao.compat.modonomicon.pages.BookLinkPage;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.Nullable;

/**
 * {@link BookLinkPage} 的客户端渲染（复刻自 Spectrum 的 BookLinkPageRenderer）：
 * 在页面固定位置画一个按钮，按钮文字自带 OPEN_URL 样式；
 * 点击由 {@link BookEntryScreen} 通过 {@link #getClickedComponentStyleAt} 拿到样式后分发。
 */
public class BookLinkPageRenderer extends BookTextPageRenderer {

    private static final int BUTTON_X = 2;
    private static final int BUTTON_Y = 132;
    private static final int BUTTON_WIDTH = 112;
    private static final int BUTTON_HEIGHT = 20;

    public BookLinkPageRenderer(BookLinkPage page) {
        super(page);
    }

    /** 注册渲染器（仅客户端）。 */
    public static void register() {
        PageRendererRegistry.registerPageRenderer(BookLinkPage.TYPE,
                page -> new BookLinkPageRenderer((BookLinkPage) page));
    }

    @Override
    public void onBeginDisplayPage(BookEntryScreen parentScreen, int left, int top) {
        if (this.getPage() instanceof BookLinkPage linkPage) {
            super.onBeginDisplayPage(parentScreen, left, top);
            this.addButton(Button.builder(linkPage.getLinkText().getComponent(), button -> {})
                    .size(BUTTON_WIDTH, BUTTON_HEIGHT)
                    .pos(BUTTON_X, BUTTON_Y)
                    .build());
        }
    }

    @Nullable
    @Override
    public Style getClickedComponentStyleAt(double mouseX, double mouseY) {
        if (this.getPage() instanceof BookLinkPage linkPage
                && mouseX >= BUTTON_X && mouseY >= BUTTON_Y
                && mouseX < BUTTON_X + BUTTON_WIDTH && mouseY < BUTTON_Y + BUTTON_HEIGHT) {
            return linkPage.getLinkText().getComponent().getStyle();
        }
        return super.getClickedComponentStyleAt(mouseX, mouseY);
    }
}
