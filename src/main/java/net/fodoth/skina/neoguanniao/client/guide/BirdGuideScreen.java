package net.fodoth.skina.neoguanniao.client.guide;

import net.fodoth.skina.neoguanniao.client.gui.layout.GuiLayoutConfig;
import net.fodoth.skina.neoguanniao.client.gui.layout.GuiLayoutLoader;
import net.fodoth.skina.neoguanniao.client.gui.layout.GuiLayoutRect;
import net.fodoth.skina.neoguanniao.content.bird.budgerigar.BudgerigarEntity;
import net.fodoth.skina.neoguanniao.content.bird.budgerigar.BudgerigarGuidePreviewAnimation;
import net.fodoth.skina.neoguanniao.content.bird.columbid.AbstractColumbidEntity;
import net.fodoth.skina.neoguanniao.content.bird.columbid.ColumbidGuidePreviewAnimation;
import net.fodoth.skina.neoguanniao.content.bird.nightheron.NightHeronEntity;
import net.fodoth.skina.neoguanniao.content.bird.nightheron.NightHeronGuidePreviewAnimation;
import net.fodoth.skina.neoguanniao.content.bird.sparrow.SparrowEntity;
import net.fodoth.skina.neoguanniao.content.bird.sparrow.SparrowGuidePreviewAnimation;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BirdGuideScreen extends Screen {

    // 颜色常量
    private static final int TEXT_COLOR = -853252;
    private static final int MUTED_TEXT_COLOR = -6506816;
    private static final int ACCENT_TEXT_COLOR = -4722433;
    private static final int NOTE_TITLE_COLOR = -2557953;
    private static final int PANEL_DARK = -2046487781;
    private static final int PANEL_FAINT = 404105522;
    private static final int BLUE_HIGHLIGHT = 981651432;
    private static final int BLUE_HOVER = 612552680;
    private static final int BORDER = 1434431424;
    private static final int BORDER_SOFT = 781109464;
    private static final int DIVIDER = 780580808;
    private static final int EDIT_BORDER = -4722433;
    private static final int EDIT_ACTIVE = -1;
    private static final int EDIT_HANDLE = -4722433;
    private static final int EDIT_MIN_SIZE = 24;
    private static final boolean LAYOUT_EDITING_ENABLED = false;

    // 数据
    private static final List<BirdGuideEntry> ENTRIES = List.of(
            new BirdGuideEntry("night_heron", List.of("intro")),
            new BirdGuideEntry("sparrow", List.of("intro")),
            new BirdGuideEntry("budgerigar", List.of("intro")),
            new BirdGuideEntry("spotted_dove", List.of("intro")),
            new BirdGuideEntry("pigeon", List.of("intro"))
    );
    private static final PoseKind[] POSES = PoseKind.values();
    private static final List<String> LAYOUT_RECT_IDS = List.of(
            "header", "main_panel", "species_header", "species_list",
            "detail_header", "tag_area", "info_card", "preview_box",
            "pose_buttons", "close_button"
    );

    // 状态变量
    private int selectedIndex;
    private int selectedPoseIndex;
    private int textScroll;
    private LivingEntity previewEntity;
    private final RandomSource previewRandom = RandomSource.create();
    private float previewDragX = 16.0F;
    private float previewDragY = -8.0F;
    private boolean draggingPreview;
    private boolean manualPoseLocked;
    private int manualLookTicks;
    private int motionTicks;
    private int motionDuration = 90;
    private PreviewMotion previewMotion;
    private GuidePreviewAnimation previewAnimation;
    private float birdX;
    private float birdY;
    private float birdScale;

    // 布局相关
    private GuiLayoutConfig externalLayout;
    private boolean debugLayout;
    private boolean layoutEditMode;
    private final Map<String, GuiLayoutRect> editedRects;
    private String activeLayoutRectId;
    private EditDragMode editDragMode;
    private GuiLayoutRect editDragStartRect;
    private int editDragStartMouseX;
    private int editDragStartMouseY;
    private Component editMessage;
    private int editMessageTicks;

    public BirdGuideScreen() {
        super(Component.translatable("gui.neoguanniao.bird_guide.title"));
        this.previewMotion = PreviewMotion.PERCH;
        this.previewAnimation = GuidePreviewAnimation.IDLE;
        this.birdScale = 1.0F;
        this.editedRects = new LinkedHashMap<>();
        this.editDragMode = EditDragMode.NONE;
        this.editMessage = Component.empty();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        this.externalLayout = GuiLayoutLoader.loadBirdGuideLayout();
        GuiLayoutRect closeButton = this.closeButtonRect();
        this.addRenderableWidget(
                Button.builder(
                                Component.translatable("gui.neoguanniao.bird_guide.close"),
                                (button) -> this.onClose()
                        )
                        .bounds(closeButton.x(), closeButton.y(), closeButton.w(), closeButton.h())
                        .build()
        );
    }

    @Override
    public void tick() {
        super.tick();
        if (this.editMessageTicks > 0) {
            --this.editMessageTicks;
        }
        this.tickPreviewMotion();
        if (this.previewEntity != null) {
            ++this.previewEntity.tickCount;
            this.applyPreviewAnimation(this.previewEntity);
        }
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics, mouseX, mouseY, partialTicks);
        this.renderShell(graphics);
        this.renderEntryList(graphics, mouseX, mouseY);
        BirdGuideEntry entry = this.selectedEntry(this.selectedIndex);
        this.renderCenterDetails(graphics, entry);
        this.renderPreviewPanel(graphics, mouseX, mouseY);
        super.render(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (this.minecraft != null && this.minecraft.level != null) {
            graphics.fillGradient(0, 0, this.width, this.height, -1224338404, -821950709);
            graphics.fillGradient(0, 0, this.width, this.height, 607149424, 100663296);
        } else {
            super.renderBackground(graphics, mouseX, mouseY, partialTick);
            graphics.fillGradient(0, 0, this.width, this.height, -1224338404, -821950709);
        }
    }

    // ============ 交互方法 ============

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (button != 0) {
            return false;
        }

        int pose = this.poseButtonIndexAt(mouseX, mouseY);
        if (pose >= 0) {
            this.selectPose(pose);
            return true;
        }

        if (this.isInPreview(mouseX, mouseY)) {
            this.draggingPreview = true;
            return true;
        }

        GuiLayoutRect list = this.layoutRect("species_list");
        int listY = this.listRowsY(list);
        int stride = this.listRowStride(list);

        if (list.contains(mouseX, mouseY)) {
            int localY = (int) mouseY - listY;
            int row = localY / stride;
            if (row >= 0 && row < ENTRIES.size() && localY >= 0 && localY % stride < this.listRowH(list)) {
                if (this.selectedIndex != row) {
                    this.previewEntity = null;
                }
                this.selectedIndex = row;
                this.textScroll = 0;
                this.selectedPoseIndex = 0;
                this.manualPoseLocked = false;
                this.resetPreviewMotion();
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0 && this.draggingPreview) {
            this.previewDragX = Mth.clamp(this.previewDragX + (float) dragX * 1.7F, -85.0F, 85.0F);
            this.previewDragY = Mth.clamp(this.previewDragY + (float) dragY * 1.25F, -45.0F, 45.0F);
            this.manualLookTicks = 60;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && this.draggingPreview) {
            this.draggingPreview = false;
            this.manualLookTicks = 50;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.isInNotes(mouseX, mouseY)) {
            int maxScroll = this.maxTextScroll(this.selectedEntry(this.selectedIndex));
            if (maxScroll > 0) {
                // 使用 scrollY 作为滚动量，因为垂直滚动通常使用 Y 轴
                this.textScroll = Mth.clamp(this.textScroll - (int) Math.signum(scrollY) * 18, 0, maxScroll);
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // 布局编辑模式快捷键
        if (LAYOUT_EDITING_ENABLED && (keyCode == 67 && (modifiers & 1) != 0)) { // Ctrl+E
            this.toggleLayoutEditMode();
            return true;
        }
        if (this.layoutEditMode) {
            if (keyCode == 83 && (modifiers & 1) != 0) { // Ctrl+S
                this.saveEditedLayout();
                return true;
            }
            if (keyCode == 82 && (modifiers & 1) != 0) { // Ctrl+R
                this.externalLayout = GuiLayoutLoader.loadBirdGuideLayout();
                this.editedRects.clear();
                this.showEditMessage(Component.literal("Layout reloaded"));
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    // ============ 辅助方法 ============

    private BirdGuideEntry selectedEntry(int index) {
        return ENTRIES.get(Mth.clamp(index, 0, ENTRIES.size() - 1));
    }

    private boolean isInPreview(double mouseX, double mouseY) {
        return this.layoutRect("preview_box").contains(mouseX, mouseY);
    }

    private boolean isInNotes(double mouseX, double mouseY) {
        return this.infoCardRect().contains(mouseX, mouseY);
    }

    private boolean isNightHeronSelected() {
        return "night_heron".equals(this.selectedEntry(this.selectedIndex).id());
    }

    private int randomBetween(int min, int max) {
        return min + this.previewRandom.nextInt(max - min + 1);
    }

    // ============ 主要渲染方法 ============

    private void renderShell(GuiGraphics graphics) {
        GuiLayoutRect header = this.layoutRect("header");
        GuiLayoutRect main = this.layoutRect("main_panel");
        GuiLayoutRect speciesList = this.layoutRect("species_list");
        GuiLayoutRect detailHeader = this.layoutRect("detail_header");
        GuiLayoutRect preview = this.layoutRect("preview_box");

        int titleY = header.y() + Math.max(0, (header.h() - 16) / 2);
        graphics.renderItem(new ItemStack(Items.BOOK), header.x() + 4, titleY - 4);
        this.drawScaledString(graphics, this.title, header.x() + 28, titleY, 1.0F, TEXT_COLOR);

        graphics.hLine(header.x(), header.right(), header.bottom() - 2, BORDER);
        graphics.fill(main.x(), main.y(), main.right(), main.bottom(), PANEL_DARK);
        this.drawThinBorder(graphics, main.x(), main.y(), main.w(), main.h(), BORDER);

        int firstDivider = speciesList.right() + Math.max(8, (detailHeader.x() - speciesList.right()) / 2);
        int secondDivider = preview.x() - Math.max(8, (preview.x() - detailHeader.right()) / 2);
        int dividerTop = main.y() + 14;
        int dividerBottom = main.bottom() - 14;

        if (firstDivider > main.x() && firstDivider < main.right()) {
            graphics.vLine(firstDivider, dividerTop, dividerBottom, DIVIDER);
        }
        if (secondDivider > main.x() && secondDivider < main.right()) {
            graphics.vLine(secondDivider, dividerTop, dividerBottom, DIVIDER);
        }
    }

    private void renderEntryList(GuiGraphics graphics, int mouseX, int mouseY) {
        GuiLayoutRect header = this.layoutRect("species_header");
        GuiLayoutRect list = this.layoutRect("species_list");

        int x = header.x() + 12;
        int y = header.y() + Math.max(0, (header.h() - 18) / 2);
        graphics.drawString(this.font, Component.translatable("gui.neoguanniao.bird_guide.species"), x, y, TEXT_COLOR, false);

        String count = ENTRIES.size() + "/" + ENTRIES.size();
        graphics.drawString(this.font, count, header.right() - 12 - this.font.width(count), y, MUTED_TEXT_COLOR, false);
        graphics.hLine(x, header.right() - 12, header.bottom() - 4, DIVIDER);

        graphics.enableScissor(list.x(), list.y(), list.right(), list.bottom());

        int listX = this.listContentX(list);
        int listW = this.listContentW(list);
        int rowH = this.listRowH(list);
        int stride = this.listRowStride(list);

        for (int i = 0; i < ENTRIES.size(); ++i) {
            BirdGuideEntry entry = this.selectedEntry(i);
            int rowY = this.listRowsY(list) + i * stride;
            boolean selected = this.selectedIndex == i;
            boolean hovered = mouseX >= listX && mouseX <= listX + listW && mouseY >= rowY && mouseY < rowY + rowH;

            graphics.fill(listX, rowY, listX + listW, rowY + rowH, selected ? BLUE_HIGHLIGHT : (hovered ? BLUE_HOVER : PANEL_FAINT));
            if (selected) {
                graphics.fill(listX, rowY, listX + 2, rowY + rowH, ACCENT_TEXT_COLOR);
            }

            this.drawColorDot(graphics, listX + 12, rowY + rowH / 2 - 2, this.speciesColor(entry));
            graphics.drawString(this.font, entry.title(), listX + 28, rowY + rowH / 2 - 4, selected ? TEXT_COLOR : -2892576, false);
        }

        graphics.disableScissor();
    }

    private void renderCenterDetails(GuiGraphics graphics, BirdGuideEntry entry) {
        GuiLayoutRect detailHeader = this.layoutRect("detail_header");
        GuiLayoutRect tagArea = this.layoutRect("tag_area");
        GuiLayoutRect infoCard = this.infoCardRect();

        graphics.enableScissor(detailHeader.x(), detailHeader.y(), detailHeader.right(), detailHeader.bottom());
        int x = detailHeader.x() + 12;
        int w = detailHeader.w() - 24;
        int titleY = detailHeader.y() + Math.max(4, (detailHeader.h() - 30) / 2);

        this.drawScaledString(graphics, entry.title(), x, titleY, 1.0F, TEXT_COLOR);
        graphics.drawString(this.font, entry.subtitle(), x, titleY + 16, ACCENT_TEXT_COLOR, false);
        graphics.hLine(x, x + w, detailHeader.bottom() - 5, DIVIDER);
        graphics.disableScissor();

        graphics.enableScissor(tagArea.x(), tagArea.y(), tagArea.right(), tagArea.bottom());
        this.renderTagChips(graphics, entry, tagArea.x() + 12, tagArea.y() + 6, tagArea.w() - 24);
        graphics.disableScissor();

        this.renderNotes(graphics, entry, infoCard);
    }

    // ============ 标签和笔记渲染 ============

    private void renderTagChips(GuiGraphics graphics, BirdGuideEntry entry, int x, int y, int w) {
        int chipX = x;
        int chipY = y;
        int row = 0;

        for (String key : this.tagsFor(entry)) {
            Component text = Component.translatable("gui.neoguanniao.bird_guide.tag." + key);
            int chipW = this.font.width(text) + 14;

            if (chipX + chipW > x + w) {
                chipX = x;
                chipY += 21;
                ++row;
            }
            if (row >= 2) {
                break;
            }

            graphics.fill(chipX, chipY, chipX + chipW, chipY + 16, 605895237);
            this.drawThinBorder(graphics, chipX, chipY, chipW, 16, 797886680);
            graphics.drawString(this.font, text, chipX + 7, chipY + 4, -2562068, false);
            chipX += chipW + 5;
        }
    }

    private void renderNotes(GuiGraphics graphics, BirdGuideEntry entry, GuiLayoutRect rect) {
        int x = rect.x();
        int y = rect.y();
        int w = rect.w();
        int h = rect.h();

        this.drawSoftRect(graphics, x, y, w, h, 604641826, BORDER_SOFT);

        MutableComponent title = Component.translatable("gui.neoguanniao.bird_guide.entry." + entry.id() + ".intro.title");
        int titleX = x + 14;
        int titleY = y + 12;
        graphics.drawString(this.font, title, titleX, titleY, NOTE_TITLE_COLOR, false);
        graphics.hLine(titleX, x + w - 14, titleY + 16, 578793408);

        int textX = x + 14;
        int textY = titleY + 26;
        int textW = w - 28;
        int textBottom = y + h - 14;
        int maxScroll = this.maxTextScroll(entry);
        this.textScroll = Mth.clamp(this.textScroll, 0, maxScroll);

        graphics.enableScissor(textX, textY, textX + textW, textBottom);
        int lineY = textY - this.textScroll;

        for (String section : entry.sections()) {
            MutableComponent body = Component.translatable("gui.neoguanniao.bird_guide.entry." + entry.id() + "." + section + ".body");
            for (FormattedCharSequence line : this.font.split(body, textW)) {
                if (lineY >= textY - 10 && lineY < textBottom) {
                    graphics.drawString(this.font, line, textX, lineY, TEXT_COLOR, false);
                }
                lineY += 12;
            }
            lineY += 7;
        }

        graphics.disableScissor();

        if (maxScroll > 0) {
            int barX = x + w - 9;
            int totalHeight = this.detailTextHeight(entry, textW);
            int thumbH = Math.max(16, (textBottom - textY) * (textBottom - textY) / Math.max(textBottom - textY, totalHeight));
            int thumbY = textY + (textBottom - textY - thumbH) * this.textScroll / maxScroll;
            graphics.fill(barX, textY, barX + 1, textBottom, 864006080);
            graphics.fill(barX - 1, thumbY, barX + 2, thumbY + thumbH, ACCENT_TEXT_COLOR);
        }
    }

    // ============ 预览面板渲染 ============

    private void renderPreviewPanel(GuiGraphics graphics, int mouseX, int mouseY) {
        GuiLayoutRect main = this.layoutRect("main_panel");
        GuiLayoutRect preview = this.layoutRect("preview_box");

        int titleY = Math.max(main.y() + 8, preview.y() - 28);
        graphics.drawString(this.font, Component.translatable("gui.neoguanniao.bird_guide.observation_pose"), preview.x(), titleY, TEXT_COLOR, false);
        graphics.hLine(preview.x(), preview.right(), titleY + 15, DIVIDER);

        this.drawSoftRect(graphics, preview.x(), preview.y(), preview.w(), preview.h(), 604444192, BORDER_SOFT);
        graphics.enableScissor(preview.x() + 1, preview.y() + 1, preview.right() - 1, preview.bottom() - 1);
        this.renderHabitatStage(graphics, preview.x(), preview.y(), preview.w(), preview.h());

        LivingEntity entity = this.previewEntity();
        if (entity != null) {
            // 计算预览实体的位置和缩放
            int scale = this.previewRenderScale(preview);

            // 使用新的 renderEntityInInventory 方法签名
            InventoryScreen.renderEntityInInventory(
                    graphics,
                    this.birdX,                           // 位置 X (float)
                    this.birdY,                           // 位置 Y (float)
                    scale,                                // 缩放 (float)
                    new Vector3f(0, 0, 0),       // 位置偏移 (Vector3f)
                    new Quaternionf()            // 旋转 (Quaternionf)
                            .rotateZ((float) Math.PI).rotateY((float) Math.PI)
                            .rotateX(this.previewDragY * 0.017453292F)  // 转换角度为弧度
                            .rotateY(this.previewDragX * 0.017453292F),
                    null,                                 // 相机旋转 (可空)
                    entity
            );
        }

        graphics.disableScissor();
        this.renderPoseButtons(graphics, mouseX, mouseY);
    }

    private void renderHabitatStage(GuiGraphics graphics, int x, int y, int w, int h) {
        graphics.fillGradient(x, y, x + w, y + h, 840116788, 1678051853);
        graphics.fillGradient(x + 1, y + 1, x + w - 1, y + h - 1, 270223960, 33554432);
    }

    private void renderPoseButtons(GuiGraphics graphics, int mouseX, int mouseY) {
        GuiLayoutRect poseButtons = this.layoutRect("pose_buttons");
        int y = poseButtons.y();
        int h = this.poseButtonH(poseButtons);

        for (int i = 0; i < POSES.length; ++i) {
            int x = this.poseButtonX(poseButtons, i);
            int w = this.poseButtonW(poseButtons);
            boolean selected = this.selectedPoseIndex == i && this.manualPoseLocked;
            boolean hovered = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;

            this.drawSoftRect(graphics, x, y, w, h,
                    selected ? BLUE_HIGHLIGHT : (hovered ? BLUE_HOVER : 571480096),
                    selected ? ACCENT_TEXT_COLOR : BORDER_SOFT);
            this.drawCenteredFittingString(graphics,
                    Component.translatable(POSES[i].translationKey()),
                    x, y, w, h,
                    selected ? TEXT_COLOR : -3090213);
        }
    }

    // ============ 预览实体和动画 ============

    @Nullable
    private LivingEntity previewEntity() {
        if (this.previewEntity == null && this.minecraft != null && this.minecraft.level != null) {
            this.previewEntity = this.selectedEntry(this.selectedIndex).entityType().create(this.minecraft.level);
            if (this.previewEntity != null) {
                if (this.previewEntity instanceof Mob mob) {
                    mob.setPersistenceRequired();
                }
                this.previewEntity.setNoGravity(true);
                this.previewEntity.setInvulnerable(true);
                this.previewEntity.setSilent(true);
                this.resetPreviewMotion();
            }
        }
        return this.previewEntity;
    }

    private void resetPreviewMotion() {
        this.manualLookTicks = 0;
        this.motionTicks = 0;
        this.motionDuration = 1;
        this.previewMotion = PreviewMotion.PERCH;
        this.previewAnimation = GuidePreviewAnimation.IDLE;

        GuiLayoutRect preview = this.layoutRect("preview_box");
        this.birdScale = this.basePreviewScale();
        int scale = this.previewRenderScale(preview);
        this.birdX = this.defaultStageX(preview, scale);
        this.birdY = this.defaultStageY(preview, scale);

        if (this.previewEntity != null) {
            this.applyPreviewAnimation(this.previewEntity);
        }
        if (!this.manualPoseLocked) {
            this.chooseNextPreviewMotion();
        } else {
            this.applySelectedPose();
        }
    }

    private void tickPreviewMotion() {
        if (this.manualLookTicks > 0) {
            --this.manualLookTicks;
        }
        if (this.manualPoseLocked) {
            ++this.motionTicks;
            this.applyPreviewMotion();
        } else {
            if (++this.motionTicks >= this.motionDuration) {
                this.chooseNextPreviewMotion();
            }
            this.applyPreviewMotion();
        }
    }

    private void chooseNextPreviewMotion() {
        float roll = this.previewRandom.nextFloat();
        if (this.isNightHeronSelected()) {
            if (roll < 0.34F) {
                this.planPerch();
            } else if (roll < 0.48F) {
                this.planWalk();
            } else if (roll < 0.58F) {
                this.planRun();
            } else if (roll < 0.78F) {
                this.planTakeoff();
            } else {
                this.planGlide();
            }
        } else if (roll < 0.46F) {
            this.planPerch();
        } else if (roll < 0.72F) {
            this.planWalk();
        } else if (roll < 0.83F) {
            this.planRun();
        } else if (roll < 0.92F) {
            this.planTakeoff();
        } else {
            this.planGlide();
        }
    }

    private void planPerch() {
        this.setPreviewMotion(PreviewMotion.PERCH, this.randomBetween(72, 118), this.randomIdleGuideAnimation());
    }

    private void planWalk() {
        this.setPreviewMotion(PreviewMotion.WALK, this.randomBetween(50, 88), GuidePreviewAnimation.WALK);
    }

    private void planRun() {
        this.setPreviewMotion(PreviewMotion.RUN, this.randomBetween(28, 48), GuidePreviewAnimation.RUN);
    }

    private void planTakeoff() {
        this.setPreviewMotion(PreviewMotion.TAKEOFF, this.randomBetween(28, 42), GuidePreviewAnimation.FLY_FLAP);
    }

    private void planGlide() {
        this.setPreviewMotion(PreviewMotion.GLIDE, this.randomBetween(54, 84), GuidePreviewAnimation.GLIDE);
    }

    private void setPreviewMotion(PreviewMotion motion, int duration, GuidePreviewAnimation animation) {
        this.previewMotion = motion;
        this.previewAnimation = animation;
        this.motionTicks = 0;
        this.motionDuration = Math.max(1, duration);
        this.lockPreviewModelPosition();
        if (this.previewEntity != null) {
            this.applyPreviewAnimation(this.previewEntity);
        }
    }

    private void selectPose(int poseIndex) {
        this.selectedPoseIndex = Mth.clamp(poseIndex, 0, POSES.length - 1);
        this.manualPoseLocked = true;
        this.applySelectedPose();
    }

    private void applySelectedPose() {
        PoseKind pose = POSES[this.selectedPoseIndex];
        switch (pose) {
            case IDLE -> this.setPreviewMotion(PreviewMotion.PERCH, 120, GuidePreviewAnimation.IDLE);
            case FORAGE -> this.setPreviewMotion(PreviewMotion.PERCH, 120, this.forageAnimationForSelected());
            case FLY -> this.setPreviewMotion(PreviewMotion.GLIDE, 120, GuidePreviewAnimation.GLIDE);
            case ALERT -> this.setPreviewMotion(PreviewMotion.PERCH, 120, GuidePreviewAnimation.LOOK_3);
        }
    }

    private GuidePreviewAnimation forageAnimationForSelected() {
        String id = this.selectedEntry(this.selectedIndex).id();
        if ("budgerigar".equals(id)) {
            return GuidePreviewAnimation.LOOK_2;
        }
        return "night_heron".equals(id) ? GuidePreviewAnimation.SCRATCH : GuidePreviewAnimation.LOOK_2;
    }

    private void applyPreviewMotion() {
        this.lockPreviewModelPosition();
        if (!this.draggingPreview && this.manualLookTicks <= 0) {
            float targetDragX = switch (this.previewMotion) {
                case GLIDE -> 26.0F;
                case TAKEOFF -> 20.0F;
                case RUN -> 12.0F;
                default -> 9.0F;
            };
            float targetDragY = switch (this.previewMotion) {
                case GLIDE -> -18.0F;
                case TAKEOFF -> -12.0F;
                case RUN -> -7.0F;
                default -> -4.0F;
            };
            this.previewDragX = Mth.lerp(0.12F, this.previewDragX, targetDragX);
            this.previewDragY = Mth.lerp(0.12F, this.previewDragY, targetDragY);
        }
    }

    private void lockPreviewModelPosition() {
        GuiLayoutRect preview = this.layoutRect("preview_box");
        this.birdScale = this.basePreviewScale();
        int scale = this.previewRenderScale(preview);
        this.birdX = this.defaultStageX(preview, scale);
        this.birdY = this.defaultStageY(preview, scale);
    }

    // ============ 动画转换方法 ============

    private void applyPreviewAnimation(LivingEntity entity) {
        if (entity instanceof NightHeronEntity nightHeron) {
            nightHeron.setGuidePreviewAnimation(this.toNightHeronPreviewAnimation(this.previewAnimation));
        } else if (entity instanceof SparrowEntity sparrow) {
            sparrow.setGuidePreviewAnimation(this.toSparrowPreviewAnimation(this.previewAnimation));
        } else if (entity instanceof BudgerigarEntity budgerigar) {
            budgerigar.setGuidePreviewAnimation(this.toBudgerigarPreviewAnimation(this.previewAnimation));
        } else if (entity instanceof AbstractColumbidEntity columbid) {
            columbid.setGuidePreviewAnimation(this.toColumbidPreviewAnimation(this.previewAnimation));
        }
    }

    private NightHeronGuidePreviewAnimation toNightHeronPreviewAnimation(GuidePreviewAnimation animation) {
        return switch (animation) {
            case IDLE -> NightHeronGuidePreviewAnimation.IDLE;
            case LOOK_1 -> NightHeronGuidePreviewAnimation.LOOK_1;
            case LOOK_2 -> NightHeronGuidePreviewAnimation.LOOK_2;
            case LOOK_3 -> NightHeronGuidePreviewAnimation.LOOK_3;
            case SCRATCH -> NightHeronGuidePreviewAnimation.SCRATCH;
            case LOOK_5 -> NightHeronGuidePreviewAnimation.LOOK_5;
            case WALK -> NightHeronGuidePreviewAnimation.WALK;
            case RUN -> NightHeronGuidePreviewAnimation.RUN;
            case FLY_FLAP -> NightHeronGuidePreviewAnimation.FLY_FLAP;
            case GLIDE -> NightHeronGuidePreviewAnimation.GLIDE;
        };
    }

    private SparrowGuidePreviewAnimation toSparrowPreviewAnimation(GuidePreviewAnimation animation) {
        return switch (animation) {
            case IDLE -> SparrowGuidePreviewAnimation.IDLE;
            case LOOK_1, LOOK_5 -> SparrowGuidePreviewAnimation.TAIL;
            case LOOK_2, SCRATCH -> SparrowGuidePreviewAnimation.PECK;
            case LOOK_3 -> SparrowGuidePreviewAnimation.LOOK_AROUND;
            case WALK, RUN -> SparrowGuidePreviewAnimation.WALK;
            case FLY_FLAP, GLIDE -> SparrowGuidePreviewAnimation.FLY;
        };
    }

    private BudgerigarGuidePreviewAnimation toBudgerigarPreviewAnimation(GuidePreviewAnimation animation) {
        return switch (animation) {
            case IDLE -> BudgerigarGuidePreviewAnimation.IDLE;
            case LOOK_1, SCRATCH -> BudgerigarGuidePreviewAnimation.PREEN;
            case LOOK_2, LOOK_5 -> BudgerigarGuidePreviewAnimation.CURIOUS;
            case LOOK_3 -> BudgerigarGuidePreviewAnimation.DANCE;
            case WALK, RUN -> BudgerigarGuidePreviewAnimation.WALK;
            case FLY_FLAP, GLIDE -> BudgerigarGuidePreviewAnimation.FLY;
        };
    }

    private ColumbidGuidePreviewAnimation toColumbidPreviewAnimation(GuidePreviewAnimation animation) {
        return switch (animation) {
            case IDLE -> ColumbidGuidePreviewAnimation.IDLE;
            case LOOK_1, SCRATCH -> ColumbidGuidePreviewAnimation.LOOK_1;
            case LOOK_2 -> ColumbidGuidePreviewAnimation.LOOK_2;
            case LOOK_3, LOOK_5 -> ColumbidGuidePreviewAnimation.LOOK_3;
            case WALK, RUN -> ColumbidGuidePreviewAnimation.WALK;
            case FLY_FLAP -> ColumbidGuidePreviewAnimation.FLY_FLAP;
            case GLIDE -> ColumbidGuidePreviewAnimation.GLIDE;
        };
    }

    private GuidePreviewAnimation randomIdleGuideAnimation() {
        return switch (this.previewRandom.nextInt(6)) {
            case 0 -> GuidePreviewAnimation.IDLE;
            case 1 -> GuidePreviewAnimation.LOOK_1;
            case 2 -> GuidePreviewAnimation.LOOK_2;
            case 3 -> GuidePreviewAnimation.LOOK_3;
            case 4 -> GuidePreviewAnimation.LOOK_5;
            default -> GuidePreviewAnimation.SCRATCH;
        };
    }

    // ============ 尺寸计算方法 ============

    private int previewRenderScale(GuiLayoutRect preview) {
        float baseScale = Math.min((float) preview.w() * 0.072F, (float) preview.h() * 0.18F);
        return Math.max(34, Math.round(baseScale * this.birdScale));
    }

    private float basePreviewScale() {
        return this.isNightHeronSelected() ? 0.86F : 0.96F;
    }

    private float defaultStageX(GuiLayoutRect preview, int scale) {
        return this.clampStageX(preview, (float) preview.centerX(), scale);
    }

    private float defaultStageY(GuiLayoutRect preview, int scale) {
        float top = this.stageSafeTop(preview, scale);
        float bottom = this.stageSafeBottom(preview, scale);
        return top > bottom ? (float) preview.y() + (float) preview.h() * 0.58F : Mth.lerp(0.52F, top, bottom);
    }

    private float stageSafeLeft(GuiLayoutRect preview, int scale) {
        return (float) preview.x() + 22.0F + (float) scale * 0.7F;
    }

    private float stageSafeRight(GuiLayoutRect preview, int scale) {
        return (float) preview.right() - 22.0F - (float) scale * 0.7F;
    }

    private float stageSafeTop(GuiLayoutRect preview, int scale) {
        return (float) preview.y() + 20.0F + (float) scale * 0.94F;
    }

    private float stageSafeBottom(GuiLayoutRect preview, int scale) {
        return (float) preview.bottom() - 24.0F - (float) scale * 0.08F;
    }

    private float clampStageX(GuiLayoutRect preview, float x, int scale) {
        float left = this.stageSafeLeft(preview, scale);
        float right = this.stageSafeRight(preview, scale);
        return left > right ? (float) preview.centerX() : Mth.clamp(x, left, right);
    }

    // ============ 布局管理 ============

    private GuiLayoutRect layoutRect(String id) {
        return this.layoutRect(id, this.fallbackRect(id));
    }

    private GuiLayoutRect layoutRect(String id, GuiLayoutRect fallback) {
        GuiLayoutRect edited = this.editedRects.get(id);
        if (edited != null) {
            return edited;
        }
        return this.externalLayout == null ? fallback : this.externalLayout.rect(id, fallback, this.width, this.height);
    }

    private GuiLayoutRect infoCardRect() {
        return this.infoCardRectFrom(this.layoutRect("info_card"));
    }

    private GuiLayoutRect infoCardRectFrom(GuiLayoutRect raw) {
        GuiLayoutRect tagArea = this.layoutRect("tag_area");
        GuiLayoutRect main = this.layoutRect("main_panel");
        int targetY = Math.max(raw.y(), tagArea.bottom() + 20);
        int maxBottom = main.bottom() - 24;
        int shifted = Math.max(0, targetY - raw.y());
        int h = Math.max(72, raw.h() - shifted);
        if (targetY + h > maxBottom) {
            h = Math.max(72, maxBottom - targetY);
        }
        return new GuiLayoutRect(raw.x(), targetY, raw.w(), h);
    }

    private GuiLayoutRect closeButtonRect() {
        GuiLayoutRect raw = this.layoutRect("close_button");
        int minW = Math.max(48, this.font.width(Component.translatable("gui.neoguanniao.bird_guide.close")) + 16);
        int minH = 20;
        int w = Mth.clamp(raw.w(), minW, minW + 18);
        int h = Mth.clamp(raw.h(), minH, minH + 6);
        int x = Mth.clamp(raw.centerX() - w / 2, 0, Math.max(0, this.width - w));
        int y = Mth.clamp(raw.centerY() - h / 2, 0, Math.max(0, this.height - h));
        return new GuiLayoutRect(x, y, w, h);
    }

    private GuiLayoutRect fallbackRect(String id) {
        return switch (id) {
            case "header" -> this.scaleBaseRect(41, 21, 1520, 48);
            case "main_panel" -> this.scaleBaseRect(40, 90, 1520, 760);
            case "species_header" -> this.scaleBaseRect(70, 120, 312, 42);
            case "species_list" -> this.scaleBaseRect(64, 170, 315, 660);
            case "detail_header" -> this.scaleBaseRect(456, 116, 448, 72);
            case "tag_area" -> this.scaleBaseRect(456, 198, 448, 58);
            case "info_card" -> this.scaleBaseRect(455, 267, 450, 555);
            case "preview_box" -> this.scaleBaseRect(986, 172, 548, 430);
            case "pose_buttons" -> this.scaleBaseRect(986, 620, 548, 72);
            case "close_button" -> this.scaleBaseRect(1380, 790, 150, 42);
            default -> new GuiLayoutRect(0, 0, Math.max(1, this.width), Math.max(1, this.height));
        };
    }

    private GuiLayoutRect scaleBaseRect(int x, int y, int w, int h) {
        return new GuiLayoutRect(x, y, w, h).scale((float) this.width / 1600.0F, (float) this.height / 900.0F);
    }

    // ============ 列表尺寸计算 ============

    private int listContentX(GuiLayoutRect rect) {
        return rect.x() + 10;
    }

    private int listContentW(GuiLayoutRect rect) {
        return Math.max(20, rect.w() - 20);
    }

    private int listRowsY(GuiLayoutRect rect) {
        return rect.y() + 4;
    }

    private int listRowH(GuiLayoutRect rect) {
        if (ENTRIES.isEmpty()) {
            return 28;
        }
        int gap = 4;
        return Mth.clamp((rect.h() - gap * Math.max(0, ENTRIES.size() - 1)) / ENTRIES.size(), 28, 46);
    }

    private int listRowStride(GuiLayoutRect rect) {
        return this.listRowH(rect) + 4;
    }

    private int detailTextHeight(BirdGuideEntry entry, int textW) {
        int height = 0;
        for (String section : entry.sections()) {
            MutableComponent body = Component.translatable("gui.neoguanniao.bird_guide.entry." + entry.id() + "." + section + ".body");
            height += this.font.split(body, textW).size() * 12 + 7;
        }
        return height;
    }

    private int maxTextScroll(BirdGuideEntry entry) {
        GuiLayoutRect note = this.infoCardRect();
        int visibleHeight = note.h() - 52;
        return Math.max(0, this.detailTextHeight(entry, note.w() - 28) - visibleHeight + 8);
    }

    // ============ 按钮计算 ============

    private int poseButtonH(GuiLayoutRect rect) {
        return Math.clamp(rect.h(), 22, 34);
    }

    private int poseButtonGap() {
        return 6;
    }

    private int poseButtonW(GuiLayoutRect rect) {
        return Math.max(36, (rect.w() - this.poseButtonGap() * (POSES.length - 1)) / POSES.length);
    }

    private int poseButtonX(GuiLayoutRect rect, int index) {
        return rect.x() + index * (this.poseButtonW(rect) + this.poseButtonGap());
    }

    private int poseButtonIndexAt(double mouseX, double mouseY) {
        GuiLayoutRect rect = this.layoutRect("pose_buttons");
        int buttonH = this.poseButtonH(rect);
        if (mouseY < rect.y() || mouseY > rect.y() + buttonH) {
            return -1;
        }
        for (int i = 0; i < POSES.length; ++i) {
            int x = this.poseButtonX(rect, i);
            if (mouseX >= x && mouseX <= x + this.poseButtonW(rect)) {
                return i;
            }
        }
        return -1;
    }

    // ============ 数据获取 ============

    private List<String> tagsFor(BirdGuideEntry entry) {
        return switch (entry.id()) {
            case "night_heron" -> List.of("nocturnal", "wetland", "fish_eater", "alert");
            case "sparrow" -> List.of("diurnal", "village", "seed_eater", "social", "tameable");
            case "budgerigar" -> List.of("diurnal", "social", "music", "seed_eater", "curious");
            case "spotted_dove" -> List.of("diurnal", "farmland", "pair_bond", "weather_sense", "calm");
            case "pigeon" -> List.of("diurnal", "urban", "social", "seed_eater");
            default -> List.of();
        };
    }

    private int speciesColor(BirdGuideEntry entry) {
        return switch (entry.id()) {
            case "night_heron" -> -7353370;
            case "sparrow" -> -3039131;
            case "budgerigar" -> -2696606;
            case "spotted_dove" -> -6583634;
            case "pigeon" -> -6638652;
            default -> ACCENT_TEXT_COLOR;
        };
    }

    // ============ 布局编辑模式 ============

    private void toggleLayoutEditMode() {
        if (!this.layoutEditMode) {
            this.captureEditableLayout();
            this.layoutEditMode = true;
            this.debugLayout = false;
            this.showEditMessage(Component.literal("Layout edit mode on"));
        } else {
            this.layoutEditMode = false;
            this.editDragMode = EditDragMode.NONE;
            this.showEditMessage(Component.literal("Layout edit mode off"));
        }
    }

    private void captureEditableLayout() {
        this.editedRects.clear();
        for (String id : LAYOUT_RECT_IDS) {
            GuiLayoutRect rect = "info_card".equals(id) ? this.infoCardRect() : this.layoutRect(id);
            this.editedRects.put(id, rect);
        }
    }

    private boolean startLayoutEditDrag(double mouseX, double mouseY) {
        for (int i = LAYOUT_RECT_IDS.size() - 1; i >= 0; --i) {
            String id = LAYOUT_RECT_IDS.get(i);
            GuiLayoutRect rect = this.editorRect(id);
            EditDragMode mode = this.editModeAt(rect, mouseX, mouseY);
            if (mode != EditDragMode.NONE) {
                this.activeLayoutRectId = id;
                this.editDragMode = mode;
                this.editDragStartRect = rect;
                this.editDragStartMouseX = (int) Math.round(mouseX);
                this.editDragStartMouseY = (int) Math.round(mouseY);
                return true;
            }
        }
        this.activeLayoutRectId = null;
        return false;
    }

    private void updateLayoutEditDrag(double mouseX, double mouseY) {
        if (this.activeLayoutRectId != null && this.editDragStartRect != null && this.editDragMode != EditDragMode.NONE) {
            int dx = (int) Math.round(mouseX) - this.editDragStartMouseX;
            int dy = (int) Math.round(mouseY) - this.editDragStartMouseY;
            GuiLayoutRect next = this.editDragMode == EditDragMode.MOVE
                    ? this.moveEditedRect(this.editDragStartRect, dx, dy)
                    : this.resizeEditedRect(this.editDragStartRect, dx, dy, this.editDragMode);
            this.editedRects.put(this.activeLayoutRectId, next);
            if ("preview_box".equals(this.activeLayoutRectId)) {
                this.lockPreviewModelPosition();
            }
        }
    }

    private GuiLayoutRect moveEditedRect(GuiLayoutRect rect, int dx, int dy) {
        int x = Mth.clamp(rect.x() + dx, 0, Math.max(0, this.width - rect.w()));
        int y = Mth.clamp(rect.y() + dy, 0, Math.max(0, this.height - rect.h()));
        return new GuiLayoutRect(x, y, rect.w(), rect.h());
    }

    private GuiLayoutRect resizeEditedRect(GuiLayoutRect rect, int dx, int dy, EditDragMode mode) {
        int left = rect.x();
        int right = rect.right();
        int top = rect.y();
        int bottom = rect.bottom();

        if (mode.left) left += dx;
        if (mode.right) right += dx;
        if (mode.top) top += dy;
        if (mode.bottom) bottom += dy;

        left = Mth.clamp(left, 0, Math.max(0, this.width - EDIT_MIN_SIZE));
        right = Mth.clamp(right, EDIT_MIN_SIZE, this.width);
        top = Mth.clamp(top, 0, Math.max(0, this.height - EDIT_MIN_SIZE));
        bottom = Mth.clamp(bottom, EDIT_MIN_SIZE, this.height);

        if (right - left < EDIT_MIN_SIZE) {
            if (mode.left) {
                left = Math.max(0, right - EDIT_MIN_SIZE);
            } else {
                right = Math.min(this.width, left + EDIT_MIN_SIZE);
            }
        }
        if (bottom - top < EDIT_MIN_SIZE) {
            if (mode.top) {
                top = Math.max(0, bottom - EDIT_MIN_SIZE);
            } else {
                bottom = Math.min(this.height, top + EDIT_MIN_SIZE);
            }
        }

        return new GuiLayoutRect(left, top, right - left, bottom - top);
    }

    private EditDragMode editModeAt(GuiLayoutRect rect, double mouseX, double mouseY) {
        int handle = 5;
        boolean inExpanded = mouseX >= rect.x() - handle && mouseX <= rect.right() + handle
                && mouseY >= rect.y() - handle && mouseY <= rect.bottom() + handle;
        if (!inExpanded) {
            return EditDragMode.NONE;
        }

        boolean left = Math.abs(mouseX - rect.x()) <= handle;
        boolean right = Math.abs(mouseX - rect.right()) <= handle;
        boolean top = Math.abs(mouseY - rect.y()) <= handle;
        boolean bottom = Math.abs(mouseY - rect.bottom()) <= handle;

        if (left && top) return EditDragMode.RESIZE_TOP_LEFT;
        if (right && top) return EditDragMode.RESIZE_TOP_RIGHT;
        if (left && bottom) return EditDragMode.RESIZE_BOTTOM_LEFT;
        if (right && bottom) return EditDragMode.RESIZE_BOTTOM_RIGHT;
        if (left) return EditDragMode.RESIZE_LEFT;
        if (right) return EditDragMode.RESIZE_RIGHT;
        if (top) return EditDragMode.RESIZE_TOP;
        if (bottom) return EditDragMode.RESIZE_BOTTOM;
        if (rect.contains(mouseX, mouseY)) return EditDragMode.MOVE;
        return EditDragMode.NONE;
    }

    private void saveEditedLayout() {
        if (this.editedRects.isEmpty()) {
            this.captureEditableLayout();
        }
        Map<String, GuiLayoutRect> rects = new LinkedHashMap<>();
        for (String id : LAYOUT_RECT_IDS) {
            rects.put(id, this.editorRect(id));
        }
        boolean saved = GuiLayoutLoader.saveBirdGuideLayout(this.width, this.height, rects);
        this.externalLayout = GuiLayoutLoader.loadBirdGuideLayout();
        this.editedRects.clear();
        this.editedRects.putAll(rects);
        this.showEditMessage(Component.literal(saved ? "Layout saved" : "Layout save failed"));
    }

    private GuiLayoutRect editorRect(String id) {
        GuiLayoutRect edited = this.editedRects.get(id);
        if (edited != null) {
            return "info_card".equals(id) ? this.infoCardRectFrom(edited) : edited;
        }
        return "info_card".equals(id) ? this.infoCardRect() : this.layoutRect(id);
    }

    private void showEditMessage(Component message) {
        this.editMessage = message;
        this.editMessageTicks = 80;
    }

    // ============ 绘制辅助方法 ============

    private void drawSoftRect(GuiGraphics graphics, int x, int y, int w, int h, int fill, int border) {
        graphics.fill(x, y, x + w, y + h, fill);
        this.drawThinBorder(graphics, x, y, w, h, border);
    }

    private void drawThinBorder(GuiGraphics graphics, int x, int y, int w, int h, int color) {
        graphics.hLine(x, x + w, y, color);
        graphics.hLine(x, x + w, y + h, color);
        graphics.vLine(x, y, y + h, color);
        graphics.vLine(x + w, y, y + h, color);
    }

    private void drawColorDot(GuiGraphics graphics, int x, int y, int color) {
        graphics.fill(x, y + 1, x + 4, y + 3, color);
        graphics.fill(x + 1, y, x + 3, y + 4, color);
    }

    private void drawCenteredFittingString(GuiGraphics graphics, Component component, int x, int y, int w, int h, int color) {
        int textW = this.font.width(component);
        if (textW > 0) {
            float scale = Math.min(1.0F, Math.max(1.0F, (float) (w - 10)) / (float) textW);
            int scaledW = Math.round((float) textW * scale);
            int scaledH = Math.round(8.0F * scale);
            int drawX = x + Math.max(0, (w - scaledW) / 2);
            int drawY = y + Math.max(0, (h - scaledH) / 2);
            graphics.enableScissor(x + 1, y + 1, x + w - 1, y + h - 1);
            this.drawScaledString(graphics, component, drawX, drawY, scale, color);
            graphics.disableScissor();
        }
    }

    private void drawFittingString(GuiGraphics graphics, Component component, int x, int y, int maxW, float maxScale, int color) {
        int textW = this.font.width(component);
        if (textW > 0 && maxW > 0) {
            float scale = Math.min(maxScale, (float) maxW / (float) textW);
            this.drawScaledString(graphics, component, x, y, scale, color);
        }
    }

    private void drawScaledString(GuiGraphics graphics, Component component, int x, int y, float scale, int color) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.drawString(this.font, component, 0, 0, color, false);
        graphics.pose().popPose();
    }

    private void renderLayoutDebug(GuiGraphics graphics) {
        // 布局调试渲染（如果启用）
    }

    private void renderLayoutEditHelp(GuiGraphics graphics) {
        // 布局编辑帮助渲染（如果启用）
    }

    private void drawEditHandles(GuiGraphics graphics, GuiLayoutRect rect, int color) {
        // 编辑手柄渲染
    }

    private void drawHandle(GuiGraphics graphics, int centerX, int centerY, int size, int color) {
        // 单个手柄渲染
    }

    public boolean isDebugLayout() {
        return debugLayout;
    }
}
