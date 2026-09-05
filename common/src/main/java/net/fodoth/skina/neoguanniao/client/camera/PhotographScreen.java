package net.fodoth.skina.neoguanniao.client.camera;
import net.fodoth.skina.neoguanniao.content.camera.PhotographData;

import java.io.IOException;
import java.nio.file.Path;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PhotographScreen
extends Screen {
    private final ItemStack photograph;

    public PhotographScreen(ItemStack photograph) {
        super(Component.translatable("gui.neoguanniao.photograph.title"));
        this.photograph = photograph;
    }

    protected void init() {
        int buttonY = this.height - 34;
        this.addRenderableWidget(Button.builder(Component.translatable("gui.neoguanniao.photograph.export"), button -> this.export()).bounds(this.width / 2 - 94, buttonY, 88, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.neoguanniao.photograph.close"), button -> this.onClose()).bounds(this.width / 2 + 6, buttonY, 88, 20).build());
    }

    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0x78000000);
        super.render(graphics, mouseX, mouseY, partialTick);
        int textureWidth = Math.max(1, PhotographData.width(this.photograph));
        int textureHeight = Math.max(1, PhotographData.height(this.photograph));
        int imageSize = Math.min(1024, Math.min(this.width - 48, this.height - 96));
        int x = (this.width - imageSize) / 2;
        int y = 36;
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 14, 15267327);
        graphics.fill(x - 8, y - 8, x + imageSize + 8, y + imageSize + 8, -1449519);
        graphics.fill(x - 4, y - 4, x + imageSize + 4, y + imageSize + 4, -14013910);
        PhotographTextureCache.pumpUploads();
        ResourceLocation texture = PhotographTextureCache.textureFor(this.photograph);
        PhotographScreen.blitFullTexture(graphics, texture, x, y, imageSize, textureWidth, textureHeight);
        String photographer = PhotographData.photographer(this.photograph);
        if (!photographer.isEmpty()) {
            graphics.drawCenteredString(this.font, Component.translatable("item.neoguanniao.photograph.tooltip.photographer", new Object[]{photographer}), this.width / 2, y + imageSize + 12, 12113894);
        }
    }

    public boolean isPauseScreen() {
        return false;
    }


    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void blitFullTexture(GuiGraphics graphics, ResourceLocation texture, int x, int y, int imageSize, int textureWidth, int textureHeight) {
        graphics.pose().pushPose();
        try {
            graphics.pose().translate((float)x, (float)y, 0.0f);
            graphics.pose().scale((float)imageSize / (float)textureWidth, (float)imageSize / (float)textureHeight, 1.0f);
            Minecraft.getInstance().getTextureManager().getTexture(texture).setFilter(false, false);
            graphics.blit(texture, 0, 0, 0.0f, 0.0f, textureWidth, textureHeight, textureWidth, textureHeight);
        }
        finally {
            graphics.pose().popPose();
        }
    }

    private void export() {
        if (this.minecraft == null || this.minecraft.player == null) {
            return;
        }
        try {
            Path file = PhotographTextureCache.export(this.photograph);
            this.minecraft.player.displayClientMessage(Component.translatable("gui.neoguanniao.photograph.exported", new Object[]{file.toString()}).withStyle(ChatFormatting.GREEN), false);
        }
        catch (IOException exception) {
            this.minecraft.player.displayClientMessage(Component.translatable("gui.neoguanniao.photograph.export_failed").withStyle(ChatFormatting.RED), false);
        }
    }
}

