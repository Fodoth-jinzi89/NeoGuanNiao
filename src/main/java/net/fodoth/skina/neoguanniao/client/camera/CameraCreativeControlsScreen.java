package net.fodoth.skina.neoguanniao.client.camera;
import net.fodoth.skina.neoguanniao.content.camera.CameraAperture;
import net.fodoth.skina.neoguanniao.content.camera.CameraFocusMode;
import net.fodoth.skina.neoguanniao.content.camera.CameraLens;
import net.fodoth.skina.neoguanniao.content.camera.CameraShootingMode;
import net.fodoth.skina.neoguanniao.content.camera.CameraState;

import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

public final class CameraCreativeControlsScreen
extends Screen {
    private final CameraState original;
    private CameraState working;
    private Page page = Page.LENS;
    private boolean finished;
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
    private int controlsTop;
    private int controlsBottom;
    private double lensScroll;
    private double modeScroll;

    private CameraCreativeControlsScreen(CameraState original) {
                super(Component.translatable("gui.neoguanniao.camera_creative.title"));
        this.original = original;
        this.working = original;
    }

    public static void open() {
        if (!CameraClientCapture.isViewfinderOpen() || CameraClientCapture.isCleanCapturePending()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Screen screen = minecraft.screen;
        if (screen instanceof CameraCreativeControlsScreen controls) {
            controls.confirmAndClose();
            return;
        }
        minecraft.setScreen(new CameraCreativeControlsScreen(CameraClientCapture.currentState()));
    }

    public boolean isPauseScreen() {
        return false;
    }

    protected void init() {
        this.recomputeLayout();
        CameraClientCapture.previewState(this.working);
    }

    public void tick() {
        super.tick();
        if (!CameraClientCapture.isViewfinderOpen() || CameraClientCapture.isCleanCapturePending()) {
            this.finished = true;
            Minecraft.getInstance().setScreen(null);
            return;
        }
        if (this.working.focusMode() == CameraFocusMode.AF_C) {
            this.working = this.working.withFocusDistance(CameraClientCapture.currentState().focusDistance());
        }
    }

    public void removed() {
        if (!this.finished) {
            CameraClientCapture.restorePreviewState(this.original);
        }
        super.removed();
    }

    private void recomputeLayout() {
        int maximumWidth = Math.max(1, this.width - 16);
        int maximumHeight = Math.max(1, this.height - 12);
        int panelWidth = Mth.clamp(Math.round((float)this.width * 0.88f), Math.min(520, maximumWidth), maximumWidth);
        int panelHeight = Mth.clamp(Math.round((float)this.height * 0.82f), Math.min(280, maximumHeight), maximumHeight);
        this.panelLeft = (this.width - panelWidth) / 2;
        this.panelTop = (this.height - panelHeight) / 2;
        this.panelRight = this.panelLeft + panelWidth;
        this.panelBottom = this.panelTop + panelHeight;
        this.footerTop = this.panelBottom - 28;
        int contentLeft = this.panelLeft + 8;
        int contentRight = this.panelRight - 8;
        int contentTop = this.panelTop + 8;
        int contentBottom = this.footerTop - 8;
        int usableWidth = contentRight - contentLeft - 8;
        int leftWidth = Math.round((float)usableWidth * 0.58f);
        this.leftLeft = contentLeft;
        this.leftRight = contentLeft + leftWidth;
        this.rightLeft = this.leftRight + 8;
        this.rightRight = contentRight;
        this.infoTop = Math.max(contentTop + 64, contentBottom - 58);
        this.previewTop = contentTop;
        this.previewBottom = this.infoTop - 5;
        this.tabsTop = contentTop + 27 + 5;
        this.controlsTop = this.tabsTop + 24 + 5;
        this.controlsBottom = contentBottom;
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0x78000000);
        this.drawPanel(graphics, this.panelLeft, this.panelTop, this.panelRight, this.panelBottom, -233301218);
        this.renderPreview(graphics);
        this.renderControls(graphics, mouseX, mouseY);
        this.renderFooter(graphics);
    }

    private void renderPreview(GuiGraphics graphics) {
        this.drawPanel(graphics, this.leftLeft, this.previewTop, this.leftRight, this.previewBottom, -16184563);
        CameraPreviewPostEffect.drawPreview(graphics, this.leftLeft + 3, this.previewTop + 3, this.leftRight - 3, this.previewBottom - 3);
        this.renderPreviewReticle(graphics);
        this.drawPanel(graphics, this.leftLeft, this.infoTop, this.leftRight, this.controlsBottom, -400546776);
        graphics.drawString(this.font, Component.translatable(this.working.shootingMode().translationKey()), this.leftLeft + 10, this.infoTop + 10, -5975188, false);
        graphics.drawString(this.font, Component.translatable(this.working.lens().translationKey()), this.leftLeft + 10, this.infoTop + 31, -5393480, false);
        int dividerX = this.leftLeft + (this.leftRight - this.leftLeft) * 2 / 5;
        graphics.vLine(dividerX, this.infoTop + 7, this.controlsBottom - 7, -12893624);
        int summaryX = dividerX + 9;
                graphics.drawString(this.font, Component.translatable("gui.neoguanniao.camera_creative.current_setup"), summaryX, this.infoTop + 8, -1775897, false);
        String focus = this.working.hasInfiniteFocus() ? "∞" : String.format(Locale.ROOT, "%.1fm", this.working.focusDistance());
        String firstLine = String.format(Locale.ROOT, "%dmm · %s · %s", Math.round(this.working.focalLength()), this.working.aperture().label(), this.working.focusMode().shortName());
        graphics.drawString(this.font, firstLine, summaryX, this.infoTop + 25, -5975188, false);
                graphics.drawString(this.font, Component.translatable("gui.neoguanniao.camera_creative.focus_value", new Object[]{focus}), summaryX, this.infoTop + 39, -5393480, false);
    }

    private void renderPreviewReticle(GuiGraphics graphics) {
        int inset = Math.max(10, (this.previewBottom - this.previewTop) / 12);
        int left = this.leftLeft + inset;
        int right = this.leftRight - inset;
        int top = this.previewTop + inset;
        int bottom = this.previewBottom - inset;
        int corner = Math.max(12, Math.min(right - left, bottom - top) / 9);
        int color = -840370433;
        graphics.fill(left, top, left + corner, top + 2, color);
        graphics.fill(left, top, left + 2, top + corner, color);
        graphics.fill(right - corner, top, right, top + 2, color);
        graphics.fill(right - 2, top, right, top + corner, color);
        graphics.fill(left, bottom - 2, left + corner, bottom, color);
        graphics.fill(left, bottom - corner, left + 2, bottom, color);
        graphics.fill(right - corner, bottom - 2, right, bottom, color);
        graphics.fill(right - 2, bottom - corner, right, bottom, color);
        int centerX = (this.leftLeft + this.leftRight) / 2;
        int centerY = (this.previewTop + this.previewBottom) / 2;
        graphics.fill(centerX - 9, centerY, centerX - 3, centerY + 1, color);
        graphics.fill(centerX + 3, centerY, centerX + 9, centerY + 1, color);
        graphics.fill(centerX, centerY - 9, centerX + 1, centerY - 3, color);
        graphics.fill(centerX, centerY + 3, centerX + 1, centerY + 9, color);
    }

    private void renderControls(GuiGraphics graphics, int mouseX, int mouseY) {
        this.drawPanel(graphics, this.rightLeft, this.panelTop + 8, this.rightRight, this.controlsBottom, -400546776);
        graphics.drawString(this.font, this.title, this.rightLeft + 8, this.panelTop + 8 + 9, -5975188, false);
        this.renderTabs(graphics, mouseX, mouseY);
        switch (this.page) {
            case LENS: {
                this.renderLensList(graphics, mouseX, mouseY);
                break;
            }
            case MODE: {
                this.renderModeList(graphics, mouseX, mouseY);
                break;
            }
            case PARAMETERS: {
                this.renderParameters(graphics, mouseX, mouseY);
            }
        }
    }

    private void renderTabs(GuiGraphics graphics, int mouseX, int mouseY) {
        Page[] pages = Page.values();
        int tabWidth = Math.max(1, (this.rightRight - this.rightLeft - 4) / pages.length);
        for (int i = 0; i < pages.length; ++i) {
            int x0 = this.rightLeft + 2 + i * tabWidth;
            int x1 = i == pages.length - 1 ? this.rightRight - 2 : x0 + tabWidth;
            boolean selected = pages[i] == this.page;
            boolean hovered = CameraCreativeControlsScreen.contains(mouseX, mouseY, x0, this.tabsTop, x1, this.tabsTop + 24);
            graphics.fill(x0, this.tabsTop, x1, this.tabsTop + 24, selected ? -13090253 : (hovered ? -13617605 : -14407379));
            CameraCreativeControlsScreen.drawBorder(graphics, x0, this.tabsTop, x1, this.tabsTop + 24, selected ? -11111109 : -12893624);
            this.drawCentered(graphics, Component.translatable(pages[i].translationKey), x0, this.tabsTop, x1 - x0, 24, selected ? -5975188 : -5393480);
        }
    }

    private void renderLensList(GuiGraphics graphics, int mouseX, int mouseY) {
        int lensIndex;
        CameraLens[] lenses = CameraLens.values();
        int visibleRows = this.visibleChoiceRows(lenses.length);
        int rowHeight = this.choiceRowHeight(visibleRows);
        int maximum = Math.max(0, lenses.length - visibleRows);
        this.lensScroll = Mth.clamp(this.lensScroll, 0.0, maximum);
        int scrollOffset = (int)Math.floor(this.lensScroll);
        int contentRight = lenses.length > visibleRows ? this.rightRight - 12 : this.rightRight - 3;
        graphics.enableScissor(this.rightLeft + 2, this.controlsTop, this.rightRight - 2, this.controlsBottom);
        for (int row = 0; row < visibleRows && (lensIndex = scrollOffset + row) < lenses.length; ++row) {
            CameraLens lens = lenses[lensIndex];
            int y0 = this.controlsTop + row * (rowHeight + 4) - (int)Math.round((this.lensScroll - scrollOffset) * (rowHeight + 4));
            int y1 = Math.min(this.controlsBottom, y0 + rowHeight);
            boolean selected = this.working.lens() == lens;
            this.renderChoiceRow(graphics, mouseX, mouseY, y0, y1, selected, contentRight);
            String focal = Math.round(lens.defaultFocalLength()) + "mm";
            graphics.drawCenteredString(this.font, focal, this.rightLeft + 32, y0 + Math.max(5, (y1 - y0 - 8) / 2), selected ? -5975188 : -5393480);
            this.renderNamedDescription(graphics, lens.translationKey(), lens.descriptionKey(), this.rightLeft + 59, y0, contentRight - this.rightLeft - 65, y1 - y0, selected);
        }
        graphics.disableScissor();
        this.renderChoiceScrollbar(graphics, lenses.length, visibleRows, scrollOffset);
    }

    private void renderModeList(GuiGraphics graphics, int mouseX, int mouseY) {
        int modeIndex;
        CameraShootingMode[] modes = CameraShootingMode.values();
        int visibleRows = this.visibleChoiceRows(modes.length);
        int rowHeight = this.choiceRowHeight(visibleRows);
        int maximum = Math.max(0, modes.length - visibleRows);
        this.modeScroll = Mth.clamp(this.modeScroll, 0.0, maximum);
        int scrollOffset = (int)Math.floor(this.modeScroll);
        int contentRight = modes.length > visibleRows ? this.rightRight - 12 : this.rightRight - 3;
        graphics.enableScissor(this.rightLeft + 2, this.controlsTop, this.rightRight - 2, this.controlsBottom);
        for (int row = 0; row < visibleRows && (modeIndex = scrollOffset + row) < modes.length; ++row) {
            CameraShootingMode mode = modes[modeIndex];
            int y0 = this.controlsTop + row * (rowHeight + 4) - (int)Math.round((this.modeScroll - scrollOffset) * (rowHeight + 4));
            int y1 = Math.min(this.controlsBottom, y0 + rowHeight);
            boolean selected = this.working.shootingMode() == mode;
            this.renderChoiceRow(graphics, mouseX, mouseY, y0, y1, selected, contentRight);
            graphics.drawCenteredString(this.font, String.format(Locale.ROOT, "%02d", modeIndex + 1), this.rightLeft + 25, y0 + Math.max(5, (y1 - y0 - 8) / 2), selected ? -5975188 : -5393480);
            this.renderNamedDescription(graphics, mode.translationKey(), mode.descriptionKey(), this.rightLeft + 48, y0, contentRight - this.rightLeft - 54, y1 - y0, selected);
        }
        graphics.disableScissor();
        this.renderChoiceScrollbar(graphics, modes.length, visibleRows, scrollOffset);
    }

        private void renderChoiceRow(GuiGraphics graphics, int mouseX, int mouseY, int y0, int y1) {
        this.renderChoiceRow(graphics, mouseX, mouseY, y0, y1, false, this.rightRight - 3);
    }

    private void renderChoiceRow(GuiGraphics graphics, int mouseX, int mouseY, int y0, int y1, boolean selected, int x1) {
        int x0 = this.rightLeft + 3;
        boolean hovered = CameraCreativeControlsScreen.contains(mouseX, mouseY, x0, y0, x1, y1);
        graphics.fill(x0, y0, x1, y1, selected ? -231523530 : (hovered ? -298830276 : -433706192));
        CameraCreativeControlsScreen.drawBorder(graphics, x0, y0, x1, y1, selected ? -5975188 : -12893624);
        if (selected) {
            graphics.fill(x0, y0, x0 + 3, y1, -5975188);
        }
    }

    private void renderChoiceScrollbar(GuiGraphics graphics, int totalRows, int visibleRows, int scrollOffset) {
        if (totalRows <= visibleRows) {
            return;
        }
        int trackLeft = this.rightRight - 9;
        int trackRight = this.rightRight - 4;
        graphics.fill(trackLeft, this.controlsTop, trackRight, this.controlsBottom, -15460325);
        int trackHeight = this.controlsBottom - this.controlsTop;
        int thumbHeight = Math.max(12, Math.round((float)trackHeight * ((float)visibleRows / (float)totalRows)));
        int maximum = totalRows - visibleRows;
        int travel = Math.max(0, trackHeight - thumbHeight);
        int thumbTop = this.controlsTop + Math.round((float)travel * ((float)scrollOffset / (float)maximum));
        graphics.fill(trackLeft, thumbTop, trackRight, thumbTop + thumbHeight, -5975188);
    }

    private int visibleChoiceRows(int totalRows) {
        int available = Math.max(1, this.controlsBottom - this.controlsTop);
        int fitByHeight = Math.max(1, (available + 4) / 28);
        return Math.min(totalRows, Math.min(4, fitByHeight));
    }

    private int choiceRowHeight(int visibleRows) {
        int available = Math.max(1, this.controlsBottom - this.controlsTop);
        return Math.max(1, (available - (visibleRows - 1) * 4) / visibleRows);
    }

    private static int choiceScrollOffset(int selectedIndex, int totalRows, int visibleRows) {
        return Mth.clamp(selectedIndex - visibleRows + 1, 0, Math.max(0, totalRows - visibleRows));
    }

    private void renderNamedDescription(GuiGraphics graphics, String nameKey, String descriptionKey, int x, int y, int width, int height, boolean selected) {
        this.drawLeftFitted(graphics, Component.translatable(nameKey), x, y + 7, width, selected ? -5975188 : -1775897);
        if (height < 38) {
            return;
        }
        List<FormattedCharSequence> lines = this.font.split(Component.translatable(descriptionKey), width);
        if (!lines.isEmpty()) {
            graphics.drawString(this.font, lines.getFirst(), x, y + 22, -5393480, false);
        }
    }

    private void renderParameters(GuiGraphics graphics, int mouseX, int mouseY) {
        int rowHeight = this.parameterRowHeight();
        graphics.enableScissor(this.rightLeft + 2, this.controlsTop, this.rightRight - 2, this.controlsBottom);
                this.renderSliderRow(graphics, mouseX, mouseY, 0, Component.translatable("gui.neoguanniao.camera_parameter.focal_length"), Math.round(this.working.focalLength()) + "mm", (this.working.focalLength() - 8.0) / 192.0, rowHeight);
        this.renderApertureRow(graphics, mouseX, mouseY, rowHeight);
        this.renderFocusModeRow(graphics, mouseX, mouseY, rowHeight);
        double focusProgress = Math.log(this.working.focusDistance() / 0.3) / Math.log(426.6666666666667);
        String focusValue = this.working.hasInfiniteFocus() ? "∞" : String.format(Locale.ROOT, "%.1fm", this.working.focusDistance());
                this.renderSliderRow(graphics, mouseX, mouseY, 3, Component.translatable("gui.neoguanniao.camera_parameter.focus_distance"), focusValue, focusProgress, rowHeight);
        graphics.disableScissor();
    }

    private int parameterRowHeight() {
        int available = Math.max(1, this.controlsBottom - this.controlsTop);
        return Math.max(1, (available - 12) / 4);
    }

    private void renderSliderRow(GuiGraphics graphics, int mouseX, int mouseY, int index, Component label, String value, double progress, int rowHeight) {
        int y0 = this.controlsTop + index * (rowHeight + 4);
        int y1 = Math.min(this.controlsBottom, y0 + rowHeight);
        this.renderChoiceRow(graphics, mouseX, mouseY, y0, y1);
        graphics.drawString(this.font, label, this.rightLeft + 10, y0 + 7, -1775897, false);
        graphics.drawString(this.font, value, this.rightRight - 10 - this.font.width(value), y0 + 7, -5975188, false);
        int barLeft = this.rightLeft + 10;
        int barRight = this.rightRight - 10;
        int barY = y1 - 13;
        graphics.fill(barLeft, barY, barRight, barY + 3, -15394532);
        int knobX = Mth.clamp(barLeft + (int)Math.round((double)(barRight - barLeft) * progress), barLeft, barRight);
        graphics.fill(barLeft, barY, knobX, barY + 3, -11111109);
        graphics.fill(knobX - 2, barY - 2, knobX + 2, barY + 5, -5975188);
    }

    private void renderApertureRow(GuiGraphics graphics, int mouseX, int mouseY, int rowHeight) {
        int y0 = this.controlsTop + rowHeight + 4;
        int y1 = Math.min(this.controlsBottom, y0 + rowHeight);
        this.renderChoiceRow(graphics, mouseX, mouseY, y0, y1);
                graphics.drawString(this.font, Component.translatable("gui.neoguanniao.camera_parameter.aperture"), this.rightLeft + 10, y0 + 6, -1775897, false);
        CameraAperture[] apertures = CameraAperture.values();
        int x0 = this.rightLeft + 7;
        int totalWidth = this.rightRight - this.rightLeft - 14;
        int buttonWidth = Math.max(1, totalWidth / apertures.length);
        int buttonTop = y0 + 19;
        for (int i = 0; i < apertures.length; ++i) {
            int left = x0 + i * buttonWidth;
            int right = i == apertures.length - 1 ? this.rightRight - 7 : left + buttonWidth;
            boolean selected = apertures[i] == this.working.aperture();
            graphics.fill(left, buttonTop, right, y1 - 5, selected ? -13090253 : -14670551);
            this.drawCentered(graphics, Component.literal(apertures[i].label()), left, buttonTop, right - left, y1 - 5 - buttonTop, selected ? -5975188 : -5393480);
        }
    }

    private void renderFocusModeRow(GuiGraphics graphics, int mouseX, int mouseY, int rowHeight) {
        int y0 = this.controlsTop + 2 * (rowHeight + 4);
        int y1 = Math.min(this.controlsBottom, y0 + rowHeight);
        this.renderChoiceRow(graphics, mouseX, mouseY, y0, y1);
                graphics.drawString(this.font, Component.translatable("gui.neoguanniao.camera_parameter.focus_mode"), this.rightLeft + 10, y0 + 6, -1775897, false);
        CameraFocusMode[] modes = CameraFocusMode.values();
        int x0 = this.rightLeft + 7;
        int totalWidth = this.rightRight - this.rightLeft - 14;
        int buttonWidth = Math.max(1, totalWidth / modes.length);
        int buttonTop = y0 + 19;
        for (int i = 0; i < modes.length; ++i) {
            int left = x0 + i * buttonWidth;
            int right = i == modes.length - 1 ? this.rightRight - 7 : left + buttonWidth;
            boolean selected = modes[i] == this.working.focusMode();
            graphics.fill(left, buttonTop, right, y1 - 5, selected ? -13090253 : -14670551);
            this.drawCentered(graphics, Component.translatable(modes[i].translationKey()), left, buttonTop, right - left, y1 - 5 - buttonTop, selected ? -5975188 : -5393480);
        }
    }

    private void renderFooter(GuiGraphics graphics) {
        graphics.fill(this.panelLeft, this.footerTop, this.panelRight, this.panelBottom, -216918760);
        graphics.hLine(this.panelLeft, this.panelRight, this.footerTop, -12893624);
                this.drawCentered(graphics, Component.translatable("gui.neoguanniao.camera_creative.help"), this.panelLeft + 8, this.footerTop, this.panelRight - this.panelLeft - 16, 28, -5393480);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        Page[] pages = Page.values();
        int tabWidth = Math.max(1, (this.rightRight - this.rightLeft - 4) / pages.length);
        for (int i = 0; i < pages.length; ++i) {
            int x1;
            int x0 = this.rightLeft + 2 + i * tabWidth;
            x1 = i == pages.length - 1 ? this.rightRight - 2 : x0 + tabWidth;
            if (!CameraCreativeControlsScreen.contains(mouseX, mouseY, x0, this.tabsTop, x1, this.tabsTop + 24)) continue;
            this.page = pages[i];
            return true;
        }
        if (this.page == Page.LENS) {
            return this.clickLens(mouseX, mouseY);
        }
        if (this.page == Page.MODE) {
            return this.clickMode(mouseX, mouseY);
        }
        return this.clickParameter(mouseX, mouseY);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        int row;
        if (button == 0 && this.page == Page.PARAMETERS && ((row = (int)((mouseY - (double)this.controlsTop) / (double)(this.parameterRowHeight() + 4))) == 0 || row == 3)) {
            return this.clickParameter(mouseX, mouseY);
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    private boolean clickLens(double mouseX, double mouseY) {
        CameraLens[] lenses = CameraLens.values();
        int visibleRows = this.visibleChoiceRows(lenses.length);
        int rowHeight = this.choiceRowHeight(visibleRows);
        int scrollOffset = (int)Math.floor(this.lensScroll);
        int contentRight = lenses.length > visibleRows ? this.rightRight - 12 : this.rightRight - 3;
        for (int row = 0; row < visibleRows; ++row) {
            int lensIndex = scrollOffset + row;
            int y0 = this.controlsTop + row * (rowHeight + 4);
            if (lensIndex >= lenses.length || !CameraCreativeControlsScreen.contains(mouseX, mouseY, this.rightLeft + 3, y0, contentRight, y0 + rowHeight)) continue;
            this.setWorking(this.working.withLens(lenses[lensIndex]));
            return true;
        }
        return true;
    }
    private boolean clickMode(double mouseX, double mouseY) {
        CameraShootingMode[] modes = CameraShootingMode.values();
        int visibleRows = this.visibleChoiceRows(modes.length);
        int rowHeight = this.choiceRowHeight(visibleRows);
        int scrollOffset = (int)Math.floor(this.modeScroll);
        int contentRight = modes.length > visibleRows ? this.rightRight - 12 : this.rightRight - 3;
        for (int row = 0; row < visibleRows; ++row) {
            int modeIndex = scrollOffset + row;
            int y0 = this.controlsTop + row * (rowHeight + 4);
            if (modeIndex >= modes.length || !CameraCreativeControlsScreen.contains(mouseX, mouseY, this.rightLeft + 3, y0, contentRight, y0 + rowHeight)) continue;
            this.setWorking(this.working.withShootingMode(modes[modeIndex]));
            return true;
        }
        return true;
    }

    private boolean clickParameter(double mouseX, double mouseY) {
        if (!CameraCreativeControlsScreen.contains(mouseX, mouseY, this.rightLeft + 3, this.controlsTop, this.rightRight - 3, this.controlsBottom)) {
            return false;
        }
        int rowHeight = this.parameterRowHeight();
        int row = (int)((mouseY - (double)this.controlsTop) / (double)(rowHeight + 4));
        if (row < 0 || row > 3) {
            return true;
        }
        int rowTop = this.controlsTop + row * (rowHeight + 4);
        if (mouseY >= (double)Math.min(this.controlsBottom, rowTop + rowHeight)) {
            return true;
        }
        if (row == 0 || row == 3) {
            double progress = Mth.clamp((mouseX - (double)(this.rightLeft + 10)) / Math.max(1.0, (double)(this.rightRight - this.rightLeft) - 20.0), 0.0, 1.0);
            if (row == 0) {
                double focal = Mth.lerp(progress, 8.0, 200.0);
                this.setWorking(this.working.withFocalLength(Math.round(focal)));
            } else {
                double distance = 0.3 * Math.pow(426.6666666666667, progress);
                this.setWorking(this.working.withFocusDistance(distance));
            }
            return true;
        }
        int x0 = this.rightLeft + 7;
        int totalWidth = this.rightRight - this.rightLeft - 14;
        if (row == 1) {
            CameraAperture[] apertures = CameraAperture.values();
            int index = Mth.clamp((int)((mouseX - (double)x0) * (double)apertures.length / (double)Math.max(1, totalWidth)), 0, apertures.length - 1);
            this.setWorking(this.working.withAperture(apertures[index]));
        } else {
            CameraFocusMode[] modes = CameraFocusMode.values();
            int index = Mth.clamp((int)((mouseX - (double)x0) * (double)modes.length / (double)Math.max(1, totalWidth)), 0, modes.length - 1);
            this.setWorking(this.working.withFocusMode(modes[index]));
        }
        return true;
    }
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int direction;
        if (scrollY == 0.0) {
            return true;
        }
        direction = scrollY > 0.0 ? -1 : 1;
        if (CameraCreativeControlsScreen.contains(mouseX, mouseY, this.rightLeft + 2, this.controlsTop, this.rightRight - 2, this.controlsBottom)) {
            if (this.page == Page.LENS) {
                int visible = this.visibleChoiceRows(CameraLens.values().length);
                this.lensScroll = Mth.clamp(this.lensScroll - scrollY * 0.5, 0.0, Math.max(0, CameraLens.values().length - visible));
                return true;
            }
            if (this.page == Page.MODE) {
                int visible = this.visibleChoiceRows(CameraShootingMode.values().length);
                this.modeScroll = Mth.clamp(this.modeScroll - scrollY * 0.5, 0.0, Math.max(0, CameraShootingMode.values().length - visible));
                return true;
            }
        }
        if (this.page == Page.LENS) {
            CameraLens[] values = CameraLens.values();
            int index = Math.floorMod(this.working.lens().ordinal() + direction, values.length);
            this.setWorking(this.working.withLens(values[index]));
        } else if (this.page == Page.MODE) {
            CameraShootingMode[] values = CameraShootingMode.values();
            int index = Math.floorMod(this.working.shootingMode().ordinal() + direction, values.length);
            this.setWorking(this.working.withShootingMode(values[index]));
        } else {
            CameraAperture aperture = direction > 0 ? this.working.aperture().next() : this.working.aperture().previous();
            this.setWorking(this.working.withAperture(aperture));
        }
        return true;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            this.cancelAndClose();
            return true;
        }
        if (keyCode == 67 || keyCode == 257 || keyCode == 335) {
            this.confirmAndClose();
            return true;
        }
        if (keyCode == 81) {
            this.page = this.page.previous();
            return true;
        }
        if (keyCode == 69) {
            this.page = this.page.next();
            return true;
        }
        if (keyCode == 82) {
            CameraClientCapture.focusAtCrosshair();
            this.working = CameraClientCapture.currentState();
            return true;
        }
        if (keyCode == 263 || keyCode == 265) {
            return this.mouseScrolled(0.0, 0.0, 0.0, 1.0);
        }
        if (keyCode == 262 || keyCode == 264) {
            return this.mouseScrolled(0.0, 0.0, 0.0, -1.0);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void setWorking(CameraState next) {
        this.working = next;
        CameraClientCapture.previewState(next);
    }

    private void confirmAndClose() {
        this.finished = true;
        CameraClientCapture.commitState(this.working);
        Minecraft.getInstance().setScreen(null);
    }

    private void cancelAndClose() {
        this.finished = true;
        CameraClientCapture.restorePreviewState(this.original);
        Minecraft.getInstance().setScreen(null);
    }

    private void drawPanel(GuiGraphics graphics, int left, int top, int right, int bottom, int fill) {
        graphics.fill(left, top, right, bottom, fill);
        CameraCreativeControlsScreen.drawBorder(graphics, left, top, right, bottom, -15921391);
        if (right - left > 4 && bottom - top > 4) {
            CameraCreativeControlsScreen.drawBorder(graphics, left + 1, top + 1, right - 1, bottom - 1, -12893624);
        }
    }

    private static void drawBorder(GuiGraphics graphics, int left, int top, int right, int bottom, int color) {
        graphics.hLine(left, right, top, color);
        graphics.hLine(left, right, bottom, color);
        graphics.vLine(left, top, bottom, color);
        graphics.vLine(right, top, bottom, color);
    }

    private void drawCentered(GuiGraphics graphics, Component text, int x, int y, int width, int height, int color) {
        int textWidth = this.font.width(text);
        float scale = textWidth <= width - 4 ? 1.0f : Math.max(0.5f, (float)(width - 4) / (float)Math.max(1, textWidth));
        int scaledWidth = Math.round((float)textWidth * scale);
        graphics.pose().pushPose();
        graphics.pose().translate((float)x + (float)(width - scaledWidth) / 2.0f, (float)y + ((float)height - 8.0f * scale) / 2.0f, 0.0f);
        graphics.pose().scale(scale, scale, 1.0f);
        graphics.drawString(this.font, text, 0, 0, color, false);
        graphics.pose().popPose();
    }

    private void drawLeftFitted(GuiGraphics graphics, Component text, int x, int y, int width, int color) {
        int textWidth = this.font.width(text);
        float scale = textWidth <= width ? 1.0f : Math.max(0.58f, (float)width / (float)Math.max(1, textWidth));
        graphics.pose().pushPose();
        graphics.pose().translate((float)x, (float)y, 0.0f);
        graphics.pose().scale(scale, scale, 1.0f);
        graphics.drawString(this.font, text, 0, 0, color, false);
        graphics.pose().popPose();
    }

    private static boolean contains(double x, double y, int left, int top, int right, int bottom) {
        return x >= (double)left && x < (double)right && y >= (double)top && y < (double)bottom;
    }

    private enum Page {
        LENS("gui.neoguanniao.camera_creative.tab.lens"),
        MODE("gui.neoguanniao.camera_creative.tab.mode"),
        PARAMETERS("gui.neoguanniao.camera_creative.tab.parameters");

        private final String translationKey;

        Page(String translationKey) {
            this.translationKey = translationKey;
        }

        private Page next() {
            Page[] values = Page.values();
            return values[(this.ordinal() + 1) % values.length];
        }

        private Page previous() {
            Page[] values = Page.values();
            return values[(this.ordinal() - 1 + values.length) % values.length];
        }
    }
}

