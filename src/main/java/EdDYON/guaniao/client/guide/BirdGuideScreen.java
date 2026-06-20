package EdDYON.guaniao.client.guide;

import EdDYON.guaniao.client.gui.layout.GuiLayoutConfig;
import EdDYON.guaniao.client.gui.layout.GuiLayoutLoader;
import EdDYON.guaniao.client.gui.layout.GuiLayoutRect;
import EdDYON.guaniao.content.bird.budgerigar.BudgerigarEntity;
import EdDYON.guaniao.content.bird.columbid.AbstractColumbidEntity;
import EdDYON.guaniao.content.bird.nightheron.NightHeronEntity;
import EdDYON.guaniao.content.bird.sparrow.SparrowEntity;
import EdDYON.guaniao.registry.GuaniaoEntityTypes;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.lwjgl.glfw.GLFW;

public class BirdGuideScreen extends Screen {
    private static final int TEXT_COLOR = 0xFFF2FAFC;
    private static final int MUTED_TEXT_COLOR = 0xFF9CB6C0;
    private static final int ACCENT_TEXT_COLOR = 0xFFB7F0FF;
    private static final int NOTE_TITLE_COLOR = 0xFFD8F7FF;
    private static final int PANEL_DARK = 0x8605131B;
    private static final int PANEL_FAINT = 0x18162932;
    private static final int BLUE_HIGHLIGHT = 0x3A82CFE8;
    private static final int BLUE_HOVER = 0x2482CFE8;
    private static final int BORDER = 0x557FAFC0;
    private static final int BORDER_SOFT = 0x2E8EC8D8;
    private static final int DIVIDER = 0x2E86B7C8;
    private static final int EDIT_BORDER = 0xFFB7F0FF;
    private static final int EDIT_ACTIVE = 0xFFFFFFFF;
    private static final int EDIT_HANDLE = 0xFFB7F0FF;
    private static final int EDIT_MIN_SIZE = 24;
    private static final boolean LAYOUT_EDITING_ENABLED = false;
    private static final List<BirdGuideEntry> ENTRIES = List.of(
            new BirdGuideEntry("night_heron", List.of("intro")),
            new BirdGuideEntry("sparrow", List.of("intro")),
            new BirdGuideEntry("budgerigar", List.of("intro")),
            new BirdGuideEntry("spotted_dove", List.of("intro")),
            new BirdGuideEntry("pigeon", List.of("intro"))
    );
    private static final PoseKind[] POSES = PoseKind.values();
    private static final List<String> LAYOUT_RECT_IDS = List.of(
            "header",
            "main_panel",
            "species_header",
            "species_list",
            "detail_header",
            "tag_area",
            "info_card",
            "preview_box",
            "pose_buttons",
            "close_button");

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
    private PreviewMotion previewMotion = PreviewMotion.PERCH;
    private GuidePreviewAnimation previewAnimation = GuidePreviewAnimation.IDLE;
    private float birdX;
    private float birdY;
    private float birdScale = 1.0F;
    private GuiLayoutConfig externalLayout;
    private boolean debugLayout;
    private boolean layoutEditMode;
    private final Map<String, GuiLayoutRect> editedRects = new LinkedHashMap<>();
    private String activeLayoutRectId;
    private EditDragMode editDragMode = EditDragMode.NONE;
    private GuiLayoutRect editDragStartRect;
    private int editDragStartMouseX;
    private int editDragStartMouseY;
    private Component editMessage = Component.empty();
    private int editMessageTicks;

    public BirdGuideScreen() {
        super(Component.translatable("gui.guaniao.bird_guide.title"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        this.externalLayout = GuiLayoutLoader.loadBirdGuideLayout();
        GuiLayoutRect closeButton = this.closeButtonRect();
        this.addRenderableWidget(Button.builder(Component.translatable("gui.guaniao.bird_guide.close"), button -> this.onClose())
                .bounds(closeButton.x(), closeButton.y(), closeButton.w(), closeButton.h())
                .build());
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
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);
        this.renderShell(graphics);
        this.renderEntryList(graphics, mouseX, mouseY);
        BirdGuideEntry entry = this.selectedEntry(this.selectedIndex);
        this.renderCenterDetails(graphics, entry);
        this.renderPreviewPanel(graphics, mouseX, mouseY);
        if (LAYOUT_EDITING_ENABLED && (this.debugLayout || this.layoutEditMode)) {
            this.renderLayoutDebug(graphics);
        }
        if (LAYOUT_EDITING_ENABLED && this.layoutEditMode) {
            this.renderLayoutEditHelp(graphics);
        }
        super.render(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public void renderBackground(GuiGraphics graphics) {
        if (this.minecraft != null && this.minecraft.level != null) {
            graphics.fillGradient(0, 0, this.width, this.height, 0xB706141C, 0xCF02070B);
            graphics.fillGradient(0, 0, this.width, this.height, 0x24305D70, 0x06000000);
        } else {
            this.renderDirtBackground(graphics);
            graphics.fillGradient(0, 0, this.width, this.height, 0xB706141C, 0xCF02070B);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (LAYOUT_EDITING_ENABLED && this.layoutEditMode && button == 0 && this.startLayoutEditDrag(mouseX, mouseY)) {
            return true;
        }
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
        int listX = this.listContentX(list);
        int listY = this.listRowsY(list);
        int stride = this.listRowStride(list);
        if (list.contains(mouseX, mouseY)) {
            int localY = (int)mouseY - listY;
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
        if (button == 0 && LAYOUT_EDITING_ENABLED && this.layoutEditMode && this.editDragMode != EditDragMode.NONE) {
            this.updateLayoutEditDrag(mouseX, mouseY);
            return true;
        }
        if (button == 0 && this.draggingPreview) {
            this.previewDragX = Mth.clamp(this.previewDragX + (float)dragX * 1.7F, -85.0F, 85.0F);
            this.previewDragY = Mth.clamp(this.previewDragY + (float)dragY * 1.25F, -45.0F, 45.0F);
            this.manualLookTicks = 60;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && LAYOUT_EDITING_ENABLED && this.layoutEditMode && this.editDragMode != EditDragMode.NONE) {
            this.editDragMode = EditDragMode.NONE;
            this.editDragStartRect = null;
            return true;
        }
        if (button == 0 && this.draggingPreview) {
            this.draggingPreview = false;
            this.manualLookTicks = 50;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (this.isInNotes(mouseX, mouseY)) {
            int maxScroll = this.maxTextScroll(this.selectedEntry(this.selectedIndex));
            if (maxScroll > 0) {
                this.textScroll = Mth.clamp(this.textScroll - (int)Math.signum(delta) * 18, 0, maxScroll);
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!LAYOUT_EDITING_ENABLED) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        if (keyCode == GLFW.GLFW_KEY_E && Screen.hasControlDown()) {
            this.toggleLayoutEditMode();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_S && Screen.hasControlDown() && this.layoutEditMode) {
            this.saveEditedLayout();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && this.layoutEditMode) {
            this.layoutEditMode = false;
            this.editDragMode = EditDragMode.NONE;
            this.showEditMessage(Component.literal("Layout edit mode off"));
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_L) {
            this.debugLayout = !this.debugLayout;
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_R && Screen.hasControlDown()) {
            this.editedRects.clear();
            this.layoutEditMode = false;
            this.editDragMode = EditDragMode.NONE;
            this.clearWidgets();
            this.init();
            this.resetPreviewMotion();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void renderShell(GuiGraphics graphics) {
        GuiLayoutRect header = this.layoutRect("header");
        GuiLayoutRect main = this.layoutRect("main_panel");
        GuiLayoutRect speciesList = this.layoutRect("species_list");
        GuiLayoutRect detailHeader = this.layoutRect("detail_header");
        GuiLayoutRect preview = this.layoutRect("preview_box");

        int titleY = header.y() + Math.max(0, (header.h() - 16) / 2);
        graphics.renderItem(new ItemStack(Items.FEATHER), header.x() + 4, titleY - 4);
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
        graphics.drawString(this.font, Component.translatable("gui.guaniao.bird_guide.species"), x, y, TEXT_COLOR, false);
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
            graphics.fill(listX, rowY, listX + listW, rowY + rowH, selected ? BLUE_HIGHLIGHT : hovered ? BLUE_HOVER : PANEL_FAINT);
            if (selected) {
                graphics.fill(listX, rowY, listX + 2, rowY + rowH, ACCENT_TEXT_COLOR);
            }
            this.drawColorDot(graphics, listX + 12, rowY + rowH / 2 - 2, this.speciesColor(entry));
            graphics.drawString(this.font, entry.title(), listX + 28, rowY + rowH / 2 - 4, selected ? TEXT_COLOR : 0xFFD3DCE0, false);
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

    private void renderTagChips(GuiGraphics graphics, BirdGuideEntry entry, int x, int y, int w) {
        int chipX = x;
        int chipY = y;
        int row = 0;
        for (String key : this.tagsFor(entry)) {
            Component text = Component.translatable("gui.guaniao.bird_guide.tag." + key);
            int chipW = this.font.width(text) + 14;
            if (chipX + chipW > x + w) {
                chipX = x;
                chipY += 21;
                row++;
            }
            if (row >= 2) {
                break;
            }
            graphics.fill(chipX, chipY, chipX + chipW, chipY + 16, 0x241D3A45);
            this.drawThinBorder(graphics, chipX, chipY, chipW, 16, 0x2F8EC8D8);
            graphics.drawString(this.font, text, chipX + 7, chipY + 4, 0xFFD8E7EC, false);
            chipX += chipW + 5;
        }
    }

    private void renderNotes(GuiGraphics graphics, BirdGuideEntry entry, GuiLayoutRect rect) {
        int x = rect.x();
        int y = rect.y();
        int w = rect.w();
        int h = rect.h();
        this.drawSoftRect(graphics, x, y, w, h, 0x240A1A22, BORDER_SOFT);
        MutableComponent title = Component.translatable("gui.guaniao.bird_guide.entry." + entry.id() + ".intro.title");
        int titleX = x + 14;
        int titleY = y + 12;
        graphics.drawString(this.font, title, titleX, titleY, NOTE_TITLE_COLOR, false);
        graphics.hLine(titleX, x + w - 14, titleY + 16, 0x227FAFC0);

        int textX = x + 14;
        int textY = titleY + 26;
        int textW = w - 28;
        int textBottom = y + h - 14;
        int maxScroll = this.maxTextScroll(entry);
        this.textScroll = Mth.clamp(this.textScroll, 0, maxScroll);
        graphics.enableScissor(textX, textY, textX + textW, textBottom);
        int lineY = textY - this.textScroll;
        for (String section : entry.sections()) {
            MutableComponent body = Component.translatable("gui.guaniao.bird_guide.entry." + entry.id() + "." + section + ".body");
            for (FormattedCharSequence line : this.font.split((FormattedText)body, textW)) {
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
            int barTop = textY;
            int barBottom = textBottom;
            int totalHeight = this.detailTextHeight(entry, textW);
            int thumbH = Math.max(16, (barBottom - barTop) * (barBottom - barTop) / Math.max(barBottom - barTop, totalHeight));
            int thumbY = barTop + (barBottom - barTop - thumbH) * this.textScroll / maxScroll;
            graphics.fill(barX, barTop, barX + 1, barBottom, 0x337FAFC0);
            graphics.fill(barX - 1, thumbY, barX + 2, thumbY + thumbH, 0xFFB7F0FF);
        }
    }

    private void renderPreviewPanel(GuiGraphics graphics, int mouseX, int mouseY) {
        GuiLayoutRect main = this.layoutRect("main_panel");
        GuiLayoutRect preview = this.layoutRect("preview_box");
        int titleY = Math.max(main.y() + 8, preview.y() - 28);
        graphics.drawString(this.font, Component.translatable("gui.guaniao.bird_guide.observation_pose"), preview.x(), titleY, TEXT_COLOR, false);
        graphics.hLine(preview.x(), preview.right(), titleY + 15, DIVIDER);

        this.drawSoftRect(graphics, preview.x(), preview.y(), preview.w(), preview.h(), 0x24071620, BORDER_SOFT);
        graphics.enableScissor(preview.x() + 1, preview.y() + 1, preview.right() - 1, preview.bottom() - 1);
        this.renderHabitatStage(graphics, preview.x(), preview.y(), preview.w(), preview.h());
        LivingEntity entity = this.previewEntity();
        if (entity != null) {
            InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, Math.round(this.birdX), Math.round(this.birdY), this.previewRenderScale(preview), this.previewDragX, this.previewDragY, entity);
        }
        graphics.disableScissor();
        this.renderPoseButtons(graphics, mouseX, mouseY);
    }

    private void renderHabitatStage(GuiGraphics graphics, int x, int y, int w, int h) {
        graphics.fillGradient(x, y, x + w, y + h, 0x32132A34, 0x64050A0D);
        graphics.fillGradient(x + 1, y + 1, x + w - 1, y + h - 1, 0x101B4A58, 0x02000000);
    }

    private void renderPoseButtons(GuiGraphics graphics, int mouseX, int mouseY) {
        GuiLayoutRect poseButtons = this.layoutRect("pose_buttons");
        int y = poseButtons.y();
        int h = this.poseButtonH(poseButtons);
        for (int i = 0; i < POSES.length; i++) {
            int x = this.poseButtonX(poseButtons, i);
            int w = this.poseButtonW(poseButtons);
            boolean selected = this.selectedPoseIndex == i && this.manualPoseLocked;
            boolean hovered = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
            this.drawSoftRect(graphics, x, y, w, h, selected ? BLUE_HIGHLIGHT : hovered ? BLUE_HOVER : 0x22101820, selected ? ACCENT_TEXT_COLOR : BORDER_SOFT);
            this.drawCenteredFittingString(graphics, Component.translatable(POSES[i].translationKey()), x, y, w, h, selected ? TEXT_COLOR : 0xFFD0D8DB);
        }
    }

    private LivingEntity previewEntity() {
        if (this.previewEntity == null && this.minecraft != null && this.minecraft.level != null) {
            this.previewEntity = this.selectedEntry(this.selectedIndex).entityType().create((Level)this.minecraft.level);
            if (this.previewEntity != null) {
                if (this.previewEntity instanceof Mob mob) {
                    mob.setNoAi(true);
                }
                this.previewEntity.setNoGravity(true);
                this.previewEntity.setSilent(true);
                this.previewEntity.setOnGround(true);
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
            return;
        }
        if (++this.motionTicks >= this.motionDuration) {
            this.chooseNextPreviewMotion();
        }
        this.applyPreviewMotion();
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
        if ("night_heron".equals(id)) {
            return GuidePreviewAnimation.SCRATCH;
        }
        return GuidePreviewAnimation.LOOK_2;
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

    private NightHeronEntity.GuidePreviewAnimation toNightHeronPreviewAnimation(GuidePreviewAnimation animation) {
        return switch (animation) {
            case IDLE -> NightHeronEntity.GuidePreviewAnimation.IDLE;
            case LOOK_1 -> NightHeronEntity.GuidePreviewAnimation.LOOK_1;
            case LOOK_2 -> NightHeronEntity.GuidePreviewAnimation.LOOK_2;
            case LOOK_3 -> NightHeronEntity.GuidePreviewAnimation.LOOK_3;
            case SCRATCH -> NightHeronEntity.GuidePreviewAnimation.SCRATCH;
            case LOOK_5 -> NightHeronEntity.GuidePreviewAnimation.LOOK_5;
            case WALK -> NightHeronEntity.GuidePreviewAnimation.WALK;
            case RUN -> NightHeronEntity.GuidePreviewAnimation.RUN;
            case FLY_FLAP -> NightHeronEntity.GuidePreviewAnimation.FLY_FLAP;
            case GLIDE -> NightHeronEntity.GuidePreviewAnimation.GLIDE;
        };
    }

    private SparrowEntity.GuidePreviewAnimation toSparrowPreviewAnimation(GuidePreviewAnimation animation) {
        return switch (animation) {
            case IDLE -> SparrowEntity.GuidePreviewAnimation.IDLE;
            case LOOK_1, LOOK_5 -> SparrowEntity.GuidePreviewAnimation.TAIL;
            case LOOK_2, SCRATCH -> SparrowEntity.GuidePreviewAnimation.PECK;
            case LOOK_3 -> SparrowEntity.GuidePreviewAnimation.LOOK_AROUND;
            case WALK, RUN -> SparrowEntity.GuidePreviewAnimation.WALK;
            case FLY_FLAP, GLIDE -> SparrowEntity.GuidePreviewAnimation.FLY;
        };
    }

    private BudgerigarEntity.GuidePreviewAnimation toBudgerigarPreviewAnimation(GuidePreviewAnimation animation) {
        return switch (animation) {
            case IDLE -> BudgerigarEntity.GuidePreviewAnimation.IDLE;
            case LOOK_1, SCRATCH -> BudgerigarEntity.GuidePreviewAnimation.PREEN;
            case LOOK_2, LOOK_5 -> BudgerigarEntity.GuidePreviewAnimation.CURIOUS;
            case LOOK_3 -> BudgerigarEntity.GuidePreviewAnimation.DANCE;
            case WALK, RUN -> BudgerigarEntity.GuidePreviewAnimation.WALK;
            case FLY_FLAP, GLIDE -> BudgerigarEntity.GuidePreviewAnimation.FLY;
        };
    }

    private AbstractColumbidEntity.GuidePreviewAnimation toColumbidPreviewAnimation(GuidePreviewAnimation animation) {
        return switch (animation) {
            case IDLE -> AbstractColumbidEntity.GuidePreviewAnimation.IDLE;
            case LOOK_1, SCRATCH -> AbstractColumbidEntity.GuidePreviewAnimation.LOOK_1;
            case LOOK_2 -> AbstractColumbidEntity.GuidePreviewAnimation.LOOK_2;
            case LOOK_3, LOOK_5 -> AbstractColumbidEntity.GuidePreviewAnimation.LOOK_3;
            case WALK, RUN -> AbstractColumbidEntity.GuidePreviewAnimation.WALK;
            case FLY_FLAP -> AbstractColumbidEntity.GuidePreviewAnimation.FLY_FLAP;
            case GLIDE -> AbstractColumbidEntity.GuidePreviewAnimation.GLIDE;
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

    private int randomBetween(int min, int max) {
        return min + this.previewRandom.nextInt(max - min + 1);
    }

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
            case "night_heron" -> 0xFF8FCBE6;
            case "sparrow" -> 0xFFD1A065;
            case "budgerigar" -> 0xFFD6DA62;
            case "spotted_dove" -> 0xFF9B8AAE;
            case "pigeon" -> 0xFF9AB3C4;
            default -> ACCENT_TEXT_COLOR;
        };
    }

    private int previewRenderScale(GuiLayoutRect preview) {
        float baseScale = Math.min((float)preview.w() * 0.072F, (float)preview.h() * 0.18F);
        return Math.max(34, Math.round(baseScale * this.birdScale));
    }

    private float basePreviewScale() {
        return this.isNightHeronSelected() ? 0.86F : 0.96F;
    }

    private float defaultStageX(GuiLayoutRect preview, int scale) {
        return this.clampStageX(preview, (float)preview.centerX(), scale);
    }

    private float defaultStageY(GuiLayoutRect preview, int scale) {
        float top = this.stageSafeTop(preview, scale);
        float bottom = this.stageSafeBottom(preview, scale);
        if (top > bottom) {
            return preview.y() + preview.h() * 0.58F;
        }
        return Mth.lerp(0.52F, top, bottom);
    }

    private float stageSafeLeft(GuiLayoutRect preview, int scale) {
        return preview.x() + 22.0F + (float)scale * 0.7F;
    }

    private float stageSafeRight(GuiLayoutRect preview, int scale) {
        return preview.right() - 22.0F - (float)scale * 0.7F;
    }

    private float stageSafeTop(GuiLayoutRect preview, int scale) {
        return preview.y() + 20.0F + (float)scale * 0.94F;
    }

    private float stageSafeBottom(GuiLayoutRect preview, int scale) {
        return preview.bottom() - 24.0F - (float)scale * 0.08F;
    }

    private float clampStageX(GuiLayoutRect preview, float x, int scale) {
        float left = this.stageSafeLeft(preview, scale);
        float right = this.stageSafeRight(preview, scale);
        if (left > right) {
            return preview.centerX();
        }
        return Mth.clamp(x, left, right);
    }

    private boolean isInPreview(double mouseX, double mouseY) {
        return this.layoutRect("preview_box").contains(mouseX, mouseY);
    }

    private boolean isInNotes(double mouseX, double mouseY) {
        return this.infoCardRect().contains(mouseX, mouseY);
    }

    private BirdGuideEntry selectedEntry(int index) {
        return ENTRIES.get(Mth.clamp(index, 0, ENTRIES.size() - 1));
    }

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
                this.editDragStartMouseX = (int)Math.round(mouseX);
                this.editDragStartMouseY = (int)Math.round(mouseY);
                return true;
            }
        }
        this.activeLayoutRectId = null;
        return false;
    }

    private void updateLayoutEditDrag(double mouseX, double mouseY) {
        if (this.activeLayoutRectId == null || this.editDragStartRect == null || this.editDragMode == EditDragMode.NONE) {
            return;
        }

        int dx = (int)Math.round(mouseX) - this.editDragStartMouseX;
        int dy = (int)Math.round(mouseY) - this.editDragStartMouseY;
        GuiLayoutRect next = this.editDragMode == EditDragMode.MOVE
                ? this.moveEditedRect(this.editDragStartRect, dx, dy)
                : this.resizeEditedRect(this.editDragStartRect, dx, dy, this.editDragMode);
        this.editedRects.put(this.activeLayoutRectId, next);
        if ("preview_box".equals(this.activeLayoutRectId)) {
            this.lockPreviewModelPosition();
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

        if (mode.left) {
            left += dx;
        }
        if (mode.right) {
            right += dx;
        }
        if (mode.top) {
            top += dy;
        }
        if (mode.bottom) {
            bottom += dy;
        }

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
        if (left && top) {
            return EditDragMode.RESIZE_TOP_LEFT;
        }
        if (right && top) {
            return EditDragMode.RESIZE_TOP_RIGHT;
        }
        if (left && bottom) {
            return EditDragMode.RESIZE_BOTTOM_LEFT;
        }
        if (right && bottom) {
            return EditDragMode.RESIZE_BOTTOM_RIGHT;
        }
        if (left) {
            return EditDragMode.RESIZE_LEFT;
        }
        if (right) {
            return EditDragMode.RESIZE_RIGHT;
        }
        if (top) {
            return EditDragMode.RESIZE_TOP;
        }
        if (bottom) {
            return EditDragMode.RESIZE_BOTTOM;
        }
        return rect.contains(mouseX, mouseY) ? EditDragMode.MOVE : EditDragMode.NONE;
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

    private GuiLayoutRect layoutRect(String id) {
        return this.layoutRect(id, this.fallbackRect(id));
    }

    private GuiLayoutRect layoutRect(String id, GuiLayoutRect fallback) {
        GuiLayoutRect edited = this.editedRects.get(id);
        if (edited != null) {
            return edited;
        }
        if (this.externalLayout == null) {
            return fallback;
        }
        return this.externalLayout.rect(id, fallback, this.width, this.height);
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
        int minW = Math.max(48, this.font.width(Component.translatable("gui.guaniao.bird_guide.close")) + 16);
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
        return new GuiLayoutRect(x, y, w, h).scale(this.width / 1600.0F, this.height / 900.0F);
    }

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
            MutableComponent body = Component.translatable("gui.guaniao.bird_guide.entry." + entry.id() + "." + section + ".body");
            height += this.font.split((FormattedText)body, textW).size() * 12 + 7;
        }
        return height;
    }

    private int maxTextScroll(BirdGuideEntry entry) {
        GuiLayoutRect note = this.infoCardRect();
        int visibleHeight = note.h() - 52;
        return Math.max(0, this.detailTextHeight(entry, note.w() - 28) - visibleHeight + 8);
    }

    private int poseButtonH(GuiLayoutRect rect) {
        return Math.max(22, Math.min(34, rect.h()));
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
        for (int i = 0; i < POSES.length; i++) {
            int x = this.poseButtonX(rect, i);
            if (mouseX >= x && mouseX <= x + this.poseButtonW(rect)) {
                return i;
            }
        }
        return -1;
    }

    private boolean isNightHeronSelected() {
        return "night_heron".equals(this.selectedEntry(this.selectedIndex).id());
    }

    private void renderLayoutDebug(GuiGraphics graphics) {
        for (String id : LAYOUT_RECT_IDS) {
            GuiLayoutRect rect = "close_button".equals(id) ? this.closeButtonRect() : this.editorRect(id);
            boolean active = id.equals(this.activeLayoutRectId);
            int color = active ? EDIT_ACTIVE : "main_panel".equals(id) ? 0xAAB7F0FF : 0xAA9DD6E8;
            this.drawThinBorder(graphics, rect.x(), rect.y(), rect.w(), rect.h(), color);
            this.drawFittingString(graphics, Component.literal(id), rect.x() + 3, rect.y() + 3, rect.w() - 6, 0.55F, color);
            if (this.layoutEditMode) {
                this.drawEditHandles(graphics, rect, active ? EDIT_ACTIVE : EDIT_HANDLE);
            }
        }
    }

    private void renderLayoutEditHelp(GuiGraphics graphics) {
        Component help = Component.literal("Layout Edit  Ctrl+E exit  Drag move  Drag edge resize  Ctrl+S save  Ctrl+R reload");
        int x = 8;
        int y = this.height - 19;
        int w = Math.min(this.width - 16, this.font.width(help) + 14);
        graphics.fill(x, y, x + w, y + 14, 0xAA06131B);
        this.drawThinBorder(graphics, x, y, w, 14, BORDER_SOFT);
        this.drawFittingString(graphics, help, x + 7, y + 3, w - 14, 1.0F, ACCENT_TEXT_COLOR);

        if (this.editMessageTicks > 0) {
            int messageW = Math.min(this.width - 16, this.font.width(this.editMessage) + 14);
            int messageX = this.width - messageW - 8;
            graphics.fill(messageX, y - 17, messageX + messageW, y - 3, 0xAA06131B);
            this.drawThinBorder(graphics, messageX, y - 17, messageW, 14, BORDER_SOFT);
            this.drawFittingString(graphics, this.editMessage, messageX + 7, y - 14, messageW - 14, 1.0F, TEXT_COLOR);
        }
    }

    private void drawEditHandles(GuiGraphics graphics, GuiLayoutRect rect, int color) {
        int size = 4;
        this.drawHandle(graphics, rect.x(), rect.y(), size, color);
        this.drawHandle(graphics, rect.centerX(), rect.y(), size, color);
        this.drawHandle(graphics, rect.right(), rect.y(), size, color);
        this.drawHandle(graphics, rect.x(), rect.centerY(), size, color);
        this.drawHandle(graphics, rect.right(), rect.centerY(), size, color);
        this.drawHandle(graphics, rect.x(), rect.bottom(), size, color);
        this.drawHandle(graphics, rect.centerX(), rect.bottom(), size, color);
        this.drawHandle(graphics, rect.right(), rect.bottom(), size, color);
    }

    private void drawHandle(GuiGraphics graphics, int centerX, int centerY, int size, int color) {
        graphics.fill(centerX - size / 2, centerY - size / 2, centerX + size / 2 + 1, centerY + size / 2 + 1, color);
    }

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
        if (textW <= 0) {
            return;
        }
        float scale = Math.min(1.0F, Math.max(1.0F, w - 10) / (float)textW);
        int scaledW = Math.round(textW * scale);
        int scaledH = Math.round(8.0F * scale);
        int drawX = x + Math.max(0, (w - scaledW) / 2);
        int drawY = y + Math.max(0, (h - scaledH) / 2);
        graphics.enableScissor(x + 1, y + 1, x + w - 1, y + h - 1);
        this.drawScaledString(graphics, component, drawX, drawY, scale, color);
        graphics.disableScissor();
    }

    private void drawFittingString(GuiGraphics graphics, Component component, int x, int y, int maxW, float maxScale, int color) {
        int textW = this.font.width(component);
        if (textW <= 0 || maxW <= 0) {
            return;
        }
        float scale = Math.min(maxScale, maxW / (float)textW);
        this.drawScaledString(graphics, component, x, y, scale, color);
    }

    private void drawScaledString(GuiGraphics graphics, Component component, int x, int y, float scale, int color) {
        graphics.pose().pushPose();
        graphics.pose().translate((float)x, (float)y, 0.0F);
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.drawString(this.font, component, 0, 0, color, false);
        graphics.pose().popPose();
    }

    private enum PreviewMotion {
        PERCH,
        WALK,
        RUN,
        TAKEOFF,
        GLIDE
    }

    private enum GuidePreviewAnimation {
        IDLE,
        LOOK_1,
        LOOK_2,
        LOOK_3,
        SCRATCH,
        LOOK_5,
        WALK,
        RUN,
        FLY_FLAP,
        GLIDE
    }

    private enum PoseKind {
        IDLE("idle"),
        FORAGE("forage"),
        FLY("fly"),
        ALERT("alert");

        private final String key;

        PoseKind(String key) {
            this.key = key;
        }

        private String translationKey() {
            return "gui.guaniao.bird_guide.pose." + this.key;
        }
    }

    private enum EditDragMode {
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

        private final boolean left;
        private final boolean right;
        private final boolean top;
        private final boolean bottom;

        EditDragMode(boolean left, boolean right, boolean top, boolean bottom) {
            this.left = left;
            this.right = right;
            this.top = top;
            this.bottom = bottom;
        }
    }

    private record BirdGuideEntry(String id, List<String> sections) {
        private Component title() {
            return Component.translatable("gui.guaniao.bird_guide.entry." + this.id + ".title");
        }

        private Component subtitle() {
            return Component.translatable("gui.guaniao.bird_guide.entry." + this.id + ".subtitle");
        }

        private EntityType<? extends LivingEntity> entityType() {
            return switch (this.id) {
                case "budgerigar" -> GuaniaoEntityTypes.BUDGERIGAR.get();
                case "sparrow" -> GuaniaoEntityTypes.SPARROW.get();
                case "spotted_dove" -> GuaniaoEntityTypes.SPOTTED_DOVE.get();
                case "pigeon" -> GuaniaoEntityTypes.PIGEON.get();
                default -> GuaniaoEntityTypes.NIGHT_HERON.get();
            };
        }
    }

}
