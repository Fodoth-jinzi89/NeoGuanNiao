package net.fodoth.skina.neoguanniao.client.camera;

import net.fodoth.skina.neoguanniao.client.camera.CameraClientCapture;
import net.fodoth.skina.neoguanniao.client.camera.CameraPreviewPostEffect;
import net.fodoth.skina.neoguanniao.content.camera.CameraFilter;
import net.fodoth.skina.neoguanniao.content.camera.CameraFilterCategory;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

public final class CameraFilterPickerScreen
extends Screen {
    private static final int OUTER_GAP = 8;
    private static final int COLUMN_GAP = 9;
    private static final int SECTION_GAP = 5;
    private static final int FOOTER_HEIGHT = 30;
    private static final int HEADER_HEIGHT = 27;
    private static final int TAB_HEIGHT = 24;
    private static final int ORIGINAL_HEIGHT = 21;
    private static final int INFO_HEIGHT = 54;
    private static final int ROW_GAP = 3;
    private static final int MAX_VISIBLE_ROWS = 7;
    private static final int SCREEN_SHADE = 0x78000000;
    private static final int PANEL_BACKGROUND = -233301218;
    private static final int SECTION_BACKGROUND = -400546776;
    private static final int ITEM_BACKGROUND = -433706192;
    private static final int ITEM_HOVER = -298830276;
    private static final int ITEM_SELECTED = -231523530;
    private static final int BORDER_DARK = -15921391;
    private static final int BORDER_LIGHT = -12893624;
    private static final int TEXT_PRIMARY = -1775897;
    private static final int TEXT_SECONDARY = -5393480;
    private static final int ACCENT = -5975188;
    private static final int ACCENT_DARK = -11111109;
    private final CameraFilter original;
    private CameraFilter highlighted;
    private CameraFilterCategory category;
    private int highlightedIndex;
    private int scrollOffset;
    private int visibleRows;
    private int rowHeight;
    private int panelLeft;
    private int panelTop;
    private int panelRight;
    private int panelBottom;
    private int footerTop;
    private int leftLeft;
    private int leftRight;
    private int previewTop;
    private int previewBottom;
    private int infoTop;
    private int rightLeft;
    private int rightRight;
    private int tabsTop;
    private int originalTop;
    private int listTop;
    private int listBottom;
    private boolean finished;

    private CameraFilterPickerScreen(CameraFilter original) {
        super((Component)Component.translatable((String)"gui.neoguanniao.camera_filter_picker.title"));
        this.original = original;
        this.highlighted = original;
        this.category = original == CameraFilter.NONE ? CameraFilterCategory.NATURAL : original.category();
    }

    public static void open() {
        Minecraft minecraft = Minecraft.getInstance();
        if (!CameraClientCapture.isViewfinderOpen() || CameraClientCapture.isCleanCapturePending()) {
            return;
        }
        Screen screen = minecraft.screen;
        if (screen instanceof CameraFilterPickerScreen) {
            CameraFilterPickerScreen picker = (CameraFilterPickerScreen)screen;
            picker.confirmAndClose();
            return;
        }
        minecraft.setScreen((Screen)new CameraFilterPickerScreen(CameraClientCapture.currentFilter()));
    }

    public boolean isPauseScreen() {
        return false;
    }

    protected void init() {
        if (this.highlighted != CameraFilter.NONE) {
            this.syncIndexToHighlighted();
        }
        this.recomputeLayout();
        this.ensureSelectionVisible();
        CameraClientCapture.previewFilter(this.highlighted);
    }

    public void tick() {
        super.tick();
        if (!CameraClientCapture.isViewfinderOpen() || CameraClientCapture.isCleanCapturePending()) {
            this.finished = true;
            Minecraft.getInstance().setScreen(null);
        }
    }

    public void removed() {
        if (!this.finished) {
            CameraClientCapture.restorePreviewFilter(this.original);
        }
        super.removed();
    }

    private void recomputeLayout() {
        int maximumWidth = Math.max(1, this.width - 16);
        int maximumHeight = Math.max(1, this.height - 12);
        int minimumWidth = Math.min(520, maximumWidth);
        int minimumHeight = Math.min(280, maximumHeight);
        int panelWidth = Mth.clamp((int)Math.round((float)this.width * 0.88f), (int)minimumWidth, (int)maximumWidth);
        int panelHeight = Mth.clamp((int)Math.round((float)this.height * 0.82f), (int)minimumHeight, (int)maximumHeight);
        this.panelLeft = (this.width - panelWidth) / 2;
        this.panelTop = (this.height - panelHeight) / 2;
        this.panelRight = this.panelLeft + panelWidth;
        this.panelBottom = this.panelTop + panelHeight;
        this.footerTop = this.panelBottom - 30;
        int contentLeft = this.panelLeft + 8;
        int contentRight = this.panelRight - 8;
        int contentTop = this.panelTop + 8;
        int contentBottom = this.footerTop - 8;
        int usableWidth = contentRight - contentLeft - 9;
        int leftWidth = Math.round((float)usableWidth * 0.58f);
        this.leftLeft = contentLeft;
        this.leftRight = contentLeft + leftWidth;
        this.rightLeft = this.leftRight + 9;
        this.rightRight = contentRight;
        this.infoTop = Math.max(contentTop + 60, contentBottom - 54);
        this.previewTop = contentTop;
        this.previewBottom = this.infoTop - 5;
        this.tabsTop = contentTop + 27 + 5;
        this.originalTop = this.tabsTop + 24 + 5;
        this.listTop = this.originalTop + 21 + 5;
        this.listBottom = contentBottom;
        int listHeight = Math.max(1, this.listBottom - this.listTop);
        this.visibleRows = Mth.clamp((int)((listHeight + 3) / 35), (int)1, (int)7);
        this.rowHeight = Math.max(24, (listHeight - (this.visibleRows - 1) * 3) / this.visibleRows);
    }

    private List<CameraFilter> currentFilters() {
        return CameraFilter.inCategory(this.category);
    }

    CameraFilterCategory previewCategory() {
        return this.category;
    }

    private void syncIndexToHighlighted() {
        List<CameraFilter> filters = this.currentFilters();
        int index = filters.indexOf((Object)this.highlighted);
        int n = this.highlightedIndex = index >= 0 ? index : 0;
        if (!filters.isEmpty() && index < 0) {
            this.highlighted = filters.get(this.highlightedIndex);
        }
    }

    private void ensureSelectionVisible() {
        List<CameraFilter> filters = this.currentFilters();
        int maximum = Math.max(0, filters.size() - this.visibleRows);
        if (this.highlighted != CameraFilter.NONE) {
            if (this.highlightedIndex < this.scrollOffset) {
                this.scrollOffset = this.highlightedIndex;
            } else if (this.highlightedIndex >= this.scrollOffset + this.visibleRows) {
                this.scrollOffset = this.highlightedIndex - this.visibleRows + 1;
            }
        }
        this.scrollOffset = Mth.clamp((int)this.scrollOffset, (int)0, (int)maximum);
    }

    private void setCategory(CameraFilterCategory next) {
        if (next == null || next == this.category) {
            return;
        }
        this.category = next;
        this.highlightedIndex = 0;
        this.scrollOffset = 0;
        List<CameraFilter> filters = this.currentFilters();
        if (!filters.isEmpty()) {
            this.select(filters.get(0));
        }
    }

    private void select(CameraFilter filter) {
        this.highlighted = filter;
        CameraClientCapture.previewFilter(filter);
        this.ensureSelectionVisible();
    }

    private void selectOriginal() {
        this.select(CameraFilter.NONE);
    }

    private void moveSelection(int delta) {
        List<CameraFilter> filters = this.currentFilters();
        if (filters.isEmpty()) {
            return;
        }
        this.highlightedIndex = this.highlighted == CameraFilter.NONE ? (delta < 0 ? filters.size() - 1 : 0) : Math.floorMod(this.highlightedIndex + delta, filters.size());
        this.select(filters.get(this.highlightedIndex));
    }

    private void randomFilter() {
        this.highlighted = CameraFilter.byId(ThreadLocalRandom.current().nextInt(1, 51));
        this.category = this.highlighted.category();
        this.scrollOffset = 0;
        this.syncIndexToHighlighted();
        this.select(this.highlighted);
    }

    private void confirmAndClose() {
        this.finished = true;
        CameraClientCapture.commitFilter(this.highlighted);
        Minecraft.getInstance().setScreen(null);
    }

    private void cancelAndClose() {
        this.finished = true;
        CameraClientCapture.restorePreviewFilter(this.original);
        Minecraft.getInstance().setScreen(null);
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0x78000000);
        this.drawPanel(graphics, this.panelLeft, this.panelTop, this.panelRight, this.panelBottom, -233301218);
        this.renderLeftPreview(graphics);
        this.renderRightLibrary(graphics, mouseX, mouseY);
        this.renderFooter(graphics);
    }

    private void renderLeftPreview(GuiGraphics graphics) {
        this.drawPanel(graphics, this.leftLeft, this.previewTop, this.leftRight, this.previewBottom, -16184563);
        CameraPreviewPostEffect.drawPreview(graphics, this.leftLeft + 3, this.previewTop + 3, this.leftRight - 3, this.previewBottom - 3);
        this.drawFocusMarks(graphics, this.leftLeft + 3, this.previewTop + 3, this.leftRight - 3, this.previewBottom - 3);
        this.drawPanel(graphics, this.leftLeft, this.infoTop, this.leftRight, this.listBottom, -400546776);
        String filterName = Component.translatable((String)this.highlighted.translationKey()).getString();
        String selected = String.format(Locale.ROOT, "#%02d  %s", CameraFilterPickerScreen.categoryDisplayNumber(this.highlighted), filterName);
        graphics.drawString(this.font, selected, this.leftLeft + 10, this.infoTop + 11, -5975188, false);
        graphics.drawString(this.font, (Component)Component.translatable((String)this.category.translationKey()), this.leftLeft + 10, this.infoTop + 30, -5393480, false);
        int dividerX = Math.min(this.leftRight - 120, this.leftLeft + (this.leftRight - this.leftLeft) * 2 / 5);
        graphics.vLine(dividerX, this.infoTop + 7, this.listBottom - 7, -12893624);
        int noteX = dividerX + 9;
        int noteWidth = Math.max(32, this.leftRight - noteX - 9);
        graphics.drawString(this.font, (Component)Component.translatable((String)"gui.neoguanniao.camera_filter_picker.lens_note"), noteX, this.infoTop + 8, -5975188, false);
        List noteLines = this.font.split((FormattedText)Component.translatable((String)this.highlighted.descriptionKey()), noteWidth);
        int noteY = this.infoTop + 24;
        for (int i = 0; i < Math.min(2, noteLines.size()); ++i) {
            graphics.drawString(this.font, (FormattedCharSequence)noteLines.get(i), noteX, noteY + i * 11, -5393480, false);
        }
    }

    private void renderRightLibrary(GuiGraphics graphics, int mouseX, int mouseY) {
        int contentTop = this.panelTop + 8;
        this.drawPanel(graphics, this.rightLeft, contentTop, this.rightRight, this.listBottom, -400546776);
        graphics.drawString(this.font, this.title, this.rightLeft + 8, contentTop + 9, -5975188, false);
        String count = "50 / 50";
        graphics.drawString(this.font, count, this.rightRight - 8 - this.font.width(count), contentTop + 9, -5393480, false);
        this.renderTabs(graphics, mouseX, mouseY);
        this.renderOriginalButton(graphics, mouseX, mouseY);
        this.renderFilterList(graphics, mouseX, mouseY);
        this.renderScrollBar(graphics);
    }

    private void renderTabs(GuiGraphics graphics, int mouseX, int mouseY) {
        CameraFilterCategory[] categories = CameraFilterCategory.values();
        int tabWidth = Math.max(1, (this.rightRight - this.rightLeft - 4) / categories.length);
        for (int i = 0; i < categories.length; ++i) {
            int x0 = this.rightLeft + 2 + i * tabWidth;
            int x1 = i == categories.length - 1 ? this.rightRight - 2 : x0 + tabWidth;
            CameraFilterCategory candidate = categories[i];
            boolean selected = candidate == this.category;
            boolean hovered = CameraFilterPickerScreen.contains(mouseX, mouseY, x0, this.tabsTop, x1, this.tabsTop + 24);
            int background = selected ? -13090253 : (hovered ? -13617605 : -14407379);
            graphics.fill(x0, this.tabsTop, x1, this.tabsTop + 24, background);
            CameraFilterPickerScreen.drawBorder(graphics, x0, this.tabsTop, x1, this.tabsTop + 24, selected ? -11111109 : -12893624);
            this.drawCenteredFittingString(graphics, (Component)Component.translatable((String)candidate.translationKey()), x0 + 2, this.tabsTop, x1 - x0 - 4, 24, selected ? -5975188 : -5393480);
        }
    }

    private void renderOriginalButton(GuiGraphics graphics, int mouseX, int mouseY) {
        int x0 = this.rightLeft + 3;
        int x1 = this.rightRight - 3;
        boolean selected = this.highlighted == CameraFilter.NONE;
        boolean hovered = CameraFilterPickerScreen.contains(mouseX, mouseY, x0, this.originalTop, x1, this.originalTop + 21);
        graphics.fill(x0, this.originalTop, x1, this.originalTop + 21, selected ? -231523530 : (hovered ? -298830276 : -433706192));
        if (selected) {
            graphics.fill(x0, this.originalTop, x0 + 3, this.originalTop + 21, -5975188);
        }
        CameraFilterPickerScreen.drawBorder(graphics, x0, this.originalTop, x1, this.originalTop + 21, selected ? -11111109 : -12893624);
        String label = "#00  " + Component.translatable((String)CameraFilter.NONE.translationKey()).getString();
        graphics.drawString(this.font, (selected ? "> " : "  ") + label, x0 + 7, this.originalTop + 6, selected ? -5975188 : -1775897, false);
    }

    private void renderFilterList(GuiGraphics graphics, int mouseX, int mouseY) {
        int filterIndex;
        List<CameraFilter> filters = this.currentFilters();
        int contentRight = this.rightRight - 12;
        graphics.enableScissor(this.rightLeft + 2, this.listTop, this.rightRight - 2, this.listBottom);
        for (int row = 0; row < this.visibleRows && (filterIndex = this.scrollOffset + row) < filters.size(); ++row) {
            int thumbnailWidth;
            int thumbnailHeight;
            int thumbnailY;
            int thumbnailX;
            boolean thumbnailDrawn;
            CameraFilter filter = filters.get(filterIndex);
            int y0 = this.listTop + row * (this.rowHeight + 3);
            int y1 = Math.min(this.listBottom, y0 + this.rowHeight);
            boolean selected = filter == this.highlighted;
            boolean hovered = CameraFilterPickerScreen.contains(mouseX, mouseY, this.rightLeft + 3, y0, contentRight, y1);
            graphics.fill(this.rightLeft + 3, y0, contentRight, y1, selected ? -231523530 : (hovered ? -298830276 : -433706192));
            CameraFilterPickerScreen.drawBorder(graphics, this.rightLeft + 3, y0, contentRight, y1, selected ? -5975188 : -12893624);
            if (selected) {
                graphics.fill(this.rightLeft + 3, y0, this.rightLeft + 6, y1, -5975188);
            }
            if (!(thumbnailDrawn = CameraPreviewPostEffect.drawFilterThumbnail(graphics, filter, thumbnailX = this.rightLeft + 10, thumbnailY = y0 + (y1 - y0 - (thumbnailHeight = Math.max(12, y1 - y0 - 8))) / 2, thumbnailX + (thumbnailWidth = Math.min(72, Math.max(24, Math.round((float)thumbnailHeight * 1.6f)))), thumbnailY + thumbnailHeight))) {
                this.renderPaletteThumbnail(graphics, filter, thumbnailX, thumbnailY, thumbnailWidth, thumbnailHeight);
            }
            CameraFilterPickerScreen.drawBorder(graphics, thumbnailX, thumbnailY, thumbnailX + thumbnailWidth, thumbnailY + thumbnailHeight, -15921391);
            int textX = thumbnailX + thumbnailWidth + 9;
            int textWidth = Math.max(8, contentRight - textX - 6);
            String name = (selected ? "> " : "  ") + String.format(Locale.ROOT, "%02d  %s", filterIndex + 1, Component.translatable((String)filter.translationKey()).getString());
            if (this.font.width(name) > textWidth) {
                name = this.font.plainSubstrByWidth(name, Math.max(6, textWidth - 6)) + "\u2026";
            }
            graphics.drawString(this.font, name, textX, y0 + Math.max(4, (y1 - y0 - 8) / 2), selected ? -5975188 : -1775897, false);
        }
        graphics.disableScissor();
    }

    private static int categoryDisplayNumber(CameraFilter filter) {
        if (filter == CameraFilter.NONE) {
            return 0;
        }
        int index = CameraFilter.inCategory(filter.category()).indexOf((Object)filter);
        return index < 0 ? 0 : index + 1;
    }

    private void renderPaletteThumbnail(GuiGraphics graphics, CameraFilter filter, int x, int y, int width, int height) {
        float saturation;
        float hue;
        float position = (float)(filter.id() % 10) / 10.0f;
        float brightness = switch (filter.category()) {
            case CameraFilterCategory.NATURAL -> {
                hue = 0.28f + position * 0.3f;
                saturation = 0.58f;
                yield 0.82f;
            }
            case CameraFilterCategory.FILM -> {
                hue = 0.06f + position * 0.1f;
                saturation = 0.52f;
                yield 0.73f;
            }
            case CameraFilterCategory.MONO -> {
                hue = 0.0f;
                saturation = 0.04f;
                yield 0.42f + position * 0.42f;
            }
            case CameraFilterCategory.MOOD -> {
                hue = 0.72f + position * 0.34f;
                saturation = 0.46f;
                yield 0.86f;
            }
            case CameraFilterCategory.CREATIVE -> {
                hue = 0.48f + position * 0.58f;
                saturation = 0.88f;
                yield 0.88f;
            }
            default -> throw new IllegalStateException("Unexpected filter category");
        };
        hue -= (float)Math.floor(hue);
        int sky = 0xFF000000 | Mth.hsvToRgb((float)hue, (float)(saturation * 0.72f), (float)brightness);
        int groundHueOffset = Mth.hsvToRgb((float)(hue + 0.08f >= 1.0f ? hue - 0.92f : hue + 0.08f), (float)saturation, (float)(brightness * 0.58f));
        int highlightHueOffset = Mth.hsvToRgb((float)(hue + 0.16f >= 1.0f ? hue - 0.84f : hue + 0.16f), (float)Math.max(0.0f, saturation - 0.12f), (float)Math.min(1.0f, brightness + 0.1f));
        int ground = 0xFF000000 | groundHueOffset;
        int highlight = 0xFF000000 | highlightHueOffset;
        int horizon = y + Math.max(4, height * 5 / 9);
        graphics.fill(x, y, x + width, horizon, sky);
        graphics.fill(x, horizon, x + width, y + height, ground);
        graphics.fill(x + width / 7, horizon - height / 5, x + width / 3, horizon, highlight);
        graphics.fill(x + width / 3, horizon - height / 3, x + width * 3 / 5, horizon, highlight);
        graphics.fill(x + width * 3 / 5, horizon - height / 6, x + width * 6 / 7, horizon, highlight);
        CameraFilterPickerScreen.drawBorder(graphics, x, y, x + width, y + height, -15921391);
    }

    private void renderScrollBar(GuiGraphics graphics) {
        List<CameraFilter> filters = this.currentFilters();
        int x0 = this.rightRight - 8;
        int x1 = this.rightRight - 4;
        graphics.fill(x0, this.listTop, x1, this.listBottom, -15657962);
        int maximum = Math.max(0, filters.size() - this.visibleRows);
        if (maximum <= 0) {
            graphics.fill(x0 + 1, this.listTop + 1, x1 - 1, this.listBottom - 1, -12893624);
            return;
        }
        int trackHeight = Math.max(1, this.listBottom - this.listTop);
        int thumbHeight = Math.max(16, trackHeight * this.visibleRows / filters.size());
        int thumbTravel = trackHeight - thumbHeight;
        int thumbTop = this.listTop + Math.round((float)thumbTravel * ((float)this.scrollOffset / (float)maximum));
        graphics.fill(x0, thumbTop, x1, thumbTop + thumbHeight, -11111109);
        graphics.fill(x0 + 1, thumbTop + 1, x1 - 1, thumbTop + thumbHeight - 1, -5975188);
    }

    private void renderFooter(GuiGraphics graphics) {
        graphics.fill(this.panelLeft, this.footerTop, this.panelRight, this.panelBottom, -216918760);
        graphics.hLine(this.panelLeft, this.panelRight, this.footerTop, -12893624);
        this.drawCenteredFittingString(graphics, (Component)Component.translatable((String)"gui.neoguanniao.camera_filter_picker.help"), this.panelLeft + 8, this.footerTop, this.panelRight - this.panelLeft - 16, 30, -5393480);
    }

    private void drawFocusMarks(GuiGraphics graphics, int left, int top, int right, int bottom) {
        int length = Mth.clamp((int)(Math.min(right - left, bottom - top) / 9), (int)9, (int)22);
        int inset = 12;
        int x0 = left + inset;
        int y0 = top + inset;
        int x1 = right - inset;
        int y1 = bottom - inset;
        int color = -572201241;
        graphics.fill(x0, y0, x0 + length, y0 + 2, color);
        graphics.fill(x0, y0, x0 + 2, y0 + length, color);
        graphics.fill(x1 - length, y0, x1, y0 + 2, color);
        graphics.fill(x1 - 2, y0, x1, y0 + length, color);
        graphics.fill(x0, y1 - 2, x0 + length, y1, color);
        graphics.fill(x0, y1 - length, x0 + 2, y1, color);
        graphics.fill(x1 - length, y1 - 2, x1, y1, color);
        graphics.fill(x1 - 2, y1 - length, x1, y1, color);
        int centerX = (left + right) / 2;
        int centerY = (top + bottom) / 2;
        graphics.fill(centerX - 8, centerY, centerX - 2, centerY + 1, color);
        graphics.fill(centerX + 3, centerY, centerX + 9, centerY + 1, color);
        graphics.fill(centerX, centerY - 8, centerX + 1, centerY - 2, color);
        graphics.fill(centerX, centerY + 3, centerX + 1, centerY + 9, color);
    }

    private void drawPanel(GuiGraphics graphics, int left, int top, int right, int bottom, int fill) {
        graphics.fill(left, top, right, bottom, fill);
        CameraFilterPickerScreen.drawBorder(graphics, left, top, right, bottom, -15921391);
        if (right - left > 4 && bottom - top > 4) {
            CameraFilterPickerScreen.drawBorder(graphics, left + 1, top + 1, right - 1, bottom - 1, -12893624);
        }
    }

    private static void drawBorder(GuiGraphics graphics, int left, int top, int right, int bottom, int color) {
        graphics.hLine(left, right, top, color);
        graphics.hLine(left, right, bottom, color);
        graphics.vLine(left, top, bottom, color);
        graphics.vLine(right, top, bottom, color);
    }

    private void drawCenteredFittingString(GuiGraphics graphics, Component text, int x, int y, int width, int height, int color) {
        int textWidth = this.font.width((FormattedText)text);
        if (textWidth <= 0 || width <= 0) {
            return;
        }
        float scale = Math.min(1.0f, Math.max(0.42f, (float)(width - 6) / (float)textWidth));
        int scaledWidth = Math.round((float)textWidth * scale);
        int scaledHeight = Math.round(8.0f * scale);
        graphics.pose().pushPose();
        graphics.pose().translate((float)x + (float)(width - scaledWidth) / 2.0f, (float)y + (float)(height - scaledHeight) / 2.0f, 0.0f);
        graphics.pose().scale(scale, scale, 1.0f);
        graphics.drawString(this.font, text, 0, 0, color, false);
        graphics.pose().popPose();
    }

    private static boolean contains(double x, double y, int left, int top, int right, int bottom) {
        return x >= (double)left && x < (double)right && y >= (double)top && y < (double)bottom;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int filterIndex;
        if (button != 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        CameraFilterCategory[] categories = CameraFilterCategory.values();
        int tabWidth = Math.max(1, (this.rightRight - this.rightLeft - 4) / categories.length);
        for (int i = 0; i < categories.length; ++i) {
            int x1;
            int x0 = this.rightLeft + 2 + i * tabWidth;
            int n = x1 = i == categories.length - 1 ? this.rightRight - 2 : x0 + tabWidth;
            if (!CameraFilterPickerScreen.contains(mouseX, mouseY, x0, this.tabsTop, x1, this.tabsTop + 24)) continue;
            this.setCategory(categories[i]);
            return true;
        }
        if (CameraFilterPickerScreen.contains(mouseX, mouseY, this.rightLeft + 3, this.originalTop, this.rightRight - 3, this.originalTop + 21)) {
            this.selectOriginal();
            return true;
        }
        if (CameraFilterPickerScreen.contains(mouseX, mouseY, this.rightRight - 10, this.listTop, this.rightRight, this.listBottom)) {
            this.scrollFromTrack(mouseY);
            return true;
        }
        List<CameraFilter> filters = this.currentFilters();
        for (int row = 0; row < this.visibleRows && (filterIndex = this.scrollOffset + row) < filters.size(); ++row) {
            int y0 = this.listTop + row * (this.rowHeight + 3);
            int y1 = Math.min(this.listBottom, y0 + this.rowHeight);
            if (!CameraFilterPickerScreen.contains(mouseX, mouseY, this.rightLeft + 3, y0, this.rightRight - 12, y1)) continue;
            this.highlightedIndex = filterIndex;
            this.select(filters.get(filterIndex));
            return true;
        }
        return true;
    }

    private void scrollFromTrack(double mouseY) {
        List<CameraFilter> filters = this.currentFilters();
        int maximum = Math.max(0, filters.size() - this.visibleRows);
        if (maximum <= 0) {
            return;
        }
        double progress = Mth.clamp((double)((mouseY - (double)this.listTop) / Math.max(1.0, (double)(this.listBottom - this.listTop))), (double)0.0, (double)1.0);
        this.scrollOffset = Mth.clamp((int)((int)Math.round(progress * (double)maximum)), (int)0, (int)maximum);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (delta == 0.0) {
            return true;
        }
        this.moveSelection(delta > 0.0 ? -1 : 1);
        return true;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            this.cancelAndClose();
            return true;
        }
        if (keyCode == 86 || keyCode == 257 || keyCode == 335) {
            this.confirmAndClose();
            return true;
        }
        if (keyCode == 82) {
            this.randomFilter();
            return true;
        }
        if (keyCode == 81) {
            this.setCategory(this.category.previous());
            return true;
        }
        if (keyCode == 69) {
            this.setCategory(this.category.next());
            return true;
        }
        if (keyCode == 48 || keyCode == 320) {
            this.selectOriginal();
            return true;
        }
        if (keyCode >= 49 && keyCode <= 53) {
            this.setCategory(CameraFilterCategory.values()[keyCode - 49]);
            return true;
        }
        if (keyCode == 268) {
            this.highlightedIndex = 0;
            this.select(this.currentFilters().get(0));
            return true;
        }
        if (keyCode == 269) {
            List<CameraFilter> filters = this.currentFilters();
            this.highlightedIndex = filters.size() - 1;
            this.select(filters.get(this.highlightedIndex));
            return true;
        }
        if (keyCode == 263 || keyCode == 265) {
            this.moveSelection(-1);
            return true;
        }
        if (keyCode == 262 || keyCode == 264) {
            this.moveSelection(1);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}

