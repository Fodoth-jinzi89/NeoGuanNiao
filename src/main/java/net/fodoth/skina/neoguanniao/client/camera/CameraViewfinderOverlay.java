package net.fodoth.skina.neoguanniao.client.camera;

import net.fodoth.skina.neoguanniao.client.camera.CameraPreviewPostEffect;
import net.fodoth.skina.neoguanniao.content.camera.CameraState;
import java.util.Locale;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;

final class CameraViewfinderOverlay {
    private static final int APERTURE_PERCENT = 72;
    private static final int MASK_COLOR = -1711143158;
    private static final int FRAME_COLOR = -571934977;
    private static final int SOFT_FRAME_COLOR = 1721351376;
    private static final int TEXT_COLOR = -420020225;

    private CameraViewfinderOverlay() {
    }

    static void render(GuiGraphics graphics, CameraState state, double fov) {
        if (!net.fodoth.skina.neoguanniao.config.NeoGuanNiaoClientConfig.SHOW_CAMERA_UI.get()) return;
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();
        Objects.requireNonNull(font);
        int topTextReserve = 9 + 8;
        Objects.requireNonNull(font);
        int bottomTextReserve = 9 * 3 + 12;
        int apertureSize = Math.min(CameraViewfinderOverlay.apertureSize(width, height), Math.max(1, height - topTextReserve - bottomTextReserve));
        int left = (width - apertureSize) / 2;
        int availableHeight = Math.max(1, height - topTextReserve - bottomTextReserve);
        int top = topTextReserve + Math.max(0, (availableHeight - apertureSize) / 2);
        int right = left + apertureSize;
        int bottom = top + apertureSize;
        CameraPreviewPostEffect.drawFilteredLens(graphics, left, top, right, bottom);
        int mask = ((int) (net.fodoth.skina.neoguanniao.config.NeoGuanNiaoClientConfig.VIEWFINDER_OPACITY.get() * 255) << 24) | 0x101010;
        graphics.fill(0, 0, width, top, mask);
        graphics.fill(0, bottom, width, height, mask);
        graphics.fill(0, top, left, bottom, mask);
        graphics.fill(right, top, width, bottom, mask);
        CameraViewfinderOverlay.drawFrame(graphics, left, top, right, bottom);
        CameraViewfinderOverlay.drawGuides(graphics, left, top, right, bottom);
        MutableComponent modeLine = Component.translatable((String)"gui.neoguanniao.camera_viewfinder.focal_line", (Object[])new Object[]{(int)Math.round(state.focalLength()), (int)Math.round(fov)});
        Objects.requireNonNull(font);
        CameraViewfinderOverlay.drawCenteredFitted(graphics, font, (Component)modeLine, width, Math.max(2, top - 9 - 5), -420020225);
        MutableComponent filterLine = Component.translatable((String)"gui.neoguanniao.camera_viewfinder.filter_line", (Object[])new Object[]{Component.translatable((String)state.filter().translationKey())});
        int filterY = bottom + 5;
        CameraViewfinderOverlay.drawCenteredFitted(graphics, font, (Component)filterLine, width, filterY, -420020225);
        String focusDistance = state.hasInfiniteFocus() ? "\u221e" : String.format(Locale.ROOT, "%.1fm", state.focusDistance());
        MutableComponent settingsLine = Component.translatable((String)"gui.neoguanniao.camera_viewfinder.settings_line", (Object[])new Object[]{Component.translatable((String)state.shootingMode().translationKey()), Component.translatable((String)state.lens().translationKey()), state.aperture().label(), state.focusMode().shortName(), focusDistance});
        Objects.requireNonNull(font);
        int settingsY = filterY + 9 + 2;
        CameraViewfinderOverlay.drawCenteredFitted(graphics, font, (Component)settingsLine, width, settingsY, -858069010);
        if (net.fodoth.skina.neoguanniao.config.NeoGuanNiaoClientConfig.SHOW_VIEWFINDER_HINT.get()) {
            MutableComponent hint = Component.translatable((String)"gui.neoguanniao.camera_viewfinder.hint");
            Objects.requireNonNull(font);
            int hintY = settingsY + 9 + 2;
            CameraViewfinderOverlay.drawCenteredFitted(graphics, font, (Component)hint, width, hintY, -1430530849);
        }
    }

    static int apertureSize(int width, int height) {
        return Math.max(1, Math.min(width, height) * 72 / 100);
    }

    private static void drawCenteredFitted(GuiGraphics graphics, Font font, Component text, int screenWidth, int y, int color) {
        int availableWidth = Math.max(1, screenWidth - 12);
        int textWidth = font.width((FormattedText)text);
        float scale = textWidth <= availableWidth ? 1.0f : Math.max(0.55f, (float)availableWidth / (float)Math.max(1, textWidth));
        float scaledWidth = (float)textWidth * scale;
        graphics.pose().pushPose();
        graphics.pose().translate(((float)screenWidth - scaledWidth) / 2.0f, (float)y, 0.0f);
        graphics.pose().scale(scale, scale, 1.0f);
        graphics.drawString(font, text, 0, 0, color, false);
        graphics.pose().popPose();
    }

    private static void drawFrame(GuiGraphics graphics, int left, int top, int right, int bottom) {
        graphics.fill(left - 1, top - 1, right + 1, top + 1, 1721351376);
        graphics.fill(left - 1, bottom - 1, right + 1, bottom + 1, 1721351376);
        graphics.fill(left - 1, top - 1, left + 1, bottom + 1, 1721351376);
        graphics.fill(right - 1, top - 1, right + 1, bottom + 1, 1721351376);
        int corner = Math.max(24, (right - left) / 10);
        graphics.fill(left - 2, top - 2, left + corner, top + 2, -571934977);
        graphics.fill(left - 2, top - 2, left + 2, top + corner, -571934977);
        graphics.fill(right - corner, top - 2, right + 2, top + 2, -571934977);
        graphics.fill(right - 2, top - 2, right + 2, top + corner, -571934977);
        graphics.fill(left - 2, bottom - 2, left + corner, bottom + 2, -571934977);
        graphics.fill(left - 2, bottom - corner, left + 2, bottom + 2, -571934977);
        graphics.fill(right - corner, bottom - 2, right + 2, bottom + 2, -571934977);
        graphics.fill(right - 2, bottom - corner, right + 2, bottom + 2, -571934977);
    }

    private static void drawGuides(GuiGraphics graphics, int left, int top, int right, int bottom) {
        int centerX = (left + right) / 2;
        int centerY = (top + bottom) / 2;
        graphics.fill(centerX - 10, centerY, centerX - 3, centerY + 1, -571934977);
        graphics.fill(centerX + 3, centerY, centerX + 10, centerY + 1, -571934977);
        graphics.fill(centerX, centerY - 10, centerX + 1, centerY - 3, -571934977);
        graphics.fill(centerX, centerY + 3, centerX + 1, centerY + 10, -571934977);
        int third = (right - left) / 3;
        int lineColor = 865713360;
        graphics.fill(left + third, top, left + third + 1, bottom, lineColor);
        graphics.fill(right - third, top, right - third + 1, bottom, lineColor);
        graphics.fill(left, top + third, right, top + third + 1, lineColor);
        graphics.fill(left, bottom - third, right, bottom - third + 1, lineColor);
        int photoSize = 1024;
        String marker = photoSize + "x" + photoSize;
        graphics.drawString(Minecraft.getInstance().font, marker, right - Minecraft.getInstance().font.width(marker) - 6, bottom - 12, 2006564048, false);
    }
}

