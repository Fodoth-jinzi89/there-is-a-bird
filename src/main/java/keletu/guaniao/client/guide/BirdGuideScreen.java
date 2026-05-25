package keletu.guaniao.client.guide;

import java.util.List;
import keletu.guaniao.content.bird.nightheron.NightHeronEntity;
import keletu.guaniao.content.bird.sparrow.SparrowEntity;
import keletu.guaniao.registry.GuaniaoEntityTypes;
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
import net.minecraft.world.level.Level;

public class BirdGuideScreen extends Screen {
    private static final int MARGIN = 18;
    private static final int ROW_HEIGHT = 24;
    private static final int TEXT_COLOR = -1446411;
    private static final int MUTED_TEXT_COLOR = -5457722;
    private static final int ACCENT_TEXT_COLOR = -992871;
    private static final List<BirdGuideEntry> ENTRIES = List.of(
            new BirdGuideEntry("night_heron", List.of("intro")),
            new BirdGuideEntry("sparrow", List.of("intro"))
    );

    private int selectedIndex;
    private int textScroll;
    private LivingEntity previewEntity;
    private final RandomSource previewRandom = RandomSource.create();
    private float previewDragX = 16.0f;
    private float previewDragY = -8.0f;
    private boolean draggingPreview;
    private int manualLookTicks;
    private int motionTicks;
    private int motionDuration = 90;
    private PreviewMotion previewMotion = PreviewMotion.PERCH;
    private GuidePreviewAnimation previewAnimation = GuidePreviewAnimation.IDLE;
    private float birdX;
    private float birdY;
    private float birdScale = 1.0f;

    public BirdGuideScreen() {
        super(Component.translatable("gui.guaniao.bird_guide.title"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        int closeWidth = 54;
        this.addRenderableWidget(Button.builder(Component.translatable("gui.guaniao.bird_guide.close"), button -> this.onClose())
                .bounds(this.width - MARGIN - closeWidth, this.height - MARGIN - 20, closeWidth, 20)
                .build());
    }

    @Override
    public void tick() {
        super.tick();
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
        this.renderDetails(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public void renderBackground(GuiGraphics graphics) {
        if (this.minecraft != null && this.minecraft.level != null) {
            graphics.fillGradient(0, 0, this.width, this.height, -300869600, -300013006);
        } else {
            this.renderDirtBackground(graphics);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (button == 0 && this.isInPreview(mouseX, mouseY)) {
            this.draggingPreview = true;
            return true;
        }
        if (button == 0 && mouseX >= this.listX() && mouseX <= this.listX() + this.listW()
                && mouseY >= this.listY() && mouseY < this.listY() + ENTRIES.size() * ROW_HEIGHT) {
            int row = ((int)mouseY - this.listY()) / ROW_HEIGHT;
            if (row >= 0 && row < ENTRIES.size()) {
                if (this.selectedIndex != row) {
                    this.previewEntity = null;
                }
                this.selectedIndex = row;
                this.textScroll = 0;
                this.resetPreviewMotion();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0 && this.draggingPreview) {
            this.previewDragX = Mth.clamp(this.previewDragX + (float)dragX * 1.7f, -85.0f, 85.0f);
            this.previewDragY = Mth.clamp(this.previewDragY + (float)dragY * 1.25f, -45.0f, 45.0f);
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
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (this.isInNotes(mouseX, mouseY)) {
            int maxScroll = this.maxTextScroll();
            if (maxScroll > 0) {
                this.textScroll = Mth.clamp(this.textScroll - (int)Math.signum(delta) * 18, 0, maxScroll);
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    private void renderShell(GuiGraphics graphics) {
        graphics.drawString(this.font, this.title, MARGIN, 14, -1, false);
        graphics.fill(this.panelX(), this.panelY(), this.panelX() + this.panelW(), this.panelY() + this.panelH(), 1712461855);
        graphics.fill(this.panelX(), this.panelY(), this.panelX() + this.panelW(), this.panelY() + 1, -1713839380);
        graphics.fill(this.detailX(), this.panelY() + 18, this.detailX() + 1, this.panelY() + this.panelH() - 18, 1147631747);
    }

    private void renderEntryList(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, Component.translatable("gui.guaniao.bird_guide.species"), this.listX(), this.listY() - 16, MUTED_TEXT_COLOR, false);
        for (int i = 0; i < ENTRIES.size(); ++i) {
            int y = this.listY() + i * ROW_HEIGHT;
            boolean selected = this.selectedIndex == i;
            boolean hovered = mouseX >= this.listX() && mouseX <= this.listX() + this.listW() && mouseY >= y && mouseY < y + ROW_HEIGHT - 3;
            graphics.fill(this.listX(), y, this.listX() + this.listW(), y + ROW_HEIGHT - 3, selected ? -1439278218 : (hovered ? 1714438735 : 572269611));
            if (selected) {
                graphics.fill(this.listX(), y, this.listX() + 3, y + ROW_HEIGHT - 3, -6502440);
            }
            graphics.drawString(this.font, this.selectedEntry(i).title(), this.listX() + 10, y + 7, selected ? -1 : TEXT_COLOR, false);
        }
    }

    private void renderDetails(GuiGraphics graphics) {
        BirdGuideEntry entry = this.selectedEntry(this.selectedIndex);
        int x = this.detailX() + 18;
        int y = this.panelY() + 18;
        graphics.drawString(this.font, entry.title(), x, y, -1, false);
        graphics.drawString(this.font, entry.subtitle(), x, y + 13, -7092522, false);
        this.renderPreview(graphics);
        this.renderNotes(graphics, entry);
    }

    private void renderNotes(GuiGraphics graphics, BirdGuideEntry entry) {
        int notesX = this.notesX();
        int notesY = this.notesY();
        int notesW = this.notesW();
        int notesBottom = this.notesBottom();
        graphics.fill(notesX - 8, notesY - 8, notesX + notesW + 8, notesBottom + 8, 571479066);

        int maxScroll = this.maxTextScroll();
        this.textScroll = Mth.clamp(this.textScroll, 0, maxScroll);
        graphics.enableScissor(notesX - 8, notesY - 8, notesX + notesW + 8, notesBottom + 8);
        int lineY = notesY - this.textScroll;
        for (String section : entry.sections()) {
            MutableComponent title = Component.translatable("gui.guaniao.bird_guide.entry." + entry.id() + "." + section + ".title");
            MutableComponent body = Component.translatable("gui.guaniao.bird_guide.entry." + entry.id() + "." + section + ".body");
            if (lineY >= notesY - 12 && lineY < notesBottom) {
                graphics.drawString(this.font, title, notesX, lineY, ACCENT_TEXT_COLOR, false);
            }
            lineY += 11;
            List<FormattedCharSequence> lines = this.font.split((FormattedText)body, notesW - 10);
            for (FormattedCharSequence line : lines) {
                if (lineY >= notesY - 9 && lineY < notesBottom) {
                    graphics.drawString(this.font, line, notesX + 8, lineY, TEXT_COLOR, false);
                }
                lineY += 10;
            }
            lineY += 8;
        }
        graphics.disableScissor();

        if (maxScroll > 0) {
            int barX = notesX + notesW + 5;
            int barTop = notesY;
            int barBottom = notesBottom;
            int thumbH = Math.max(18, (barBottom - barTop) * (barBottom - barTop) / Math.max(barBottom - barTop, this.detailTextHeight(entry, notesW)));
            int thumbY = barTop + (barBottom - barTop - thumbH) * this.textScroll / maxScroll;
            graphics.fill(barX, barTop, barX + 2, barBottom, 572662306);
            graphics.fill(barX - 1, thumbY, barX + 3, thumbY + thumbH, -6502440);
        }
    }

    private void renderPreview(GuiGraphics graphics) {
        LivingEntity entity = this.previewEntity();
        if (entity == null) {
            return;
        }
        int scale = this.previewRenderScale();
        int centerX = Math.round(this.birdX);
        int centerY = Math.round(this.birdY);
        graphics.fill(this.previewBoxX(), this.previewBoxY(), this.previewBoxX() + this.previewBoxW(), this.previewBoxY() + this.previewBoxH(), 856692768);
        graphics.fill(this.previewBoxX(), this.previewBoxY(), this.previewBoxX() + this.previewBoxW(), this.previewBoxY() + 1, 1147631747);
        graphics.enableScissor(this.previewBoxX() + 1, this.previewBoxY() + 1, this.previewBoxX() + this.previewBoxW() - 1, this.previewBoxY() + this.previewBoxH() - 1);
        InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, centerX, centerY, scale, this.previewDragX, this.previewDragY, entity);
        graphics.disableScissor();
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
        this.birdScale = this.basePreviewScale();
        int scale = this.previewRenderScale();
        this.birdX = this.defaultStageX(scale);
        this.birdY = this.defaultStageY(scale);
        if (this.previewEntity != null) {
            this.applyPreviewAnimation(this.previewEntity);
        }
        this.chooseNextPreviewMotion();
    }

    private void tickPreviewMotion() {
        if (this.manualLookTicks > 0) {
            --this.manualLookTicks;
        }
        if (++this.motionTicks >= this.motionDuration) {
            this.chooseNextPreviewMotion();
        }
        this.applyPreviewMotion();
    }

    private void chooseNextPreviewMotion() {
        float roll = this.previewRandom.nextFloat();
        if (this.isNightHeronSelected()) {
            if (roll < 0.34f) {
                this.planPerch();
            } else if (roll < 0.48f) {
                this.planWalk();
            } else if (roll < 0.58f) {
                this.planRun();
            } else if (roll < 0.78f) {
                this.planTakeoff();
            } else {
                this.planGlide();
            }
        } else if (roll < 0.46f) {
            this.planPerch();
        } else if (roll < 0.72f) {
            this.planWalk();
        } else if (roll < 0.83f) {
            this.planRun();
        } else if (roll < 0.92f) {
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

    private void applyPreviewMotion() {
        this.lockPreviewModelPosition();
        if (!this.draggingPreview && this.manualLookTicks <= 0) {
            float targetDragX = switch (this.previewMotion) {
                case GLIDE -> 26.0f;
                case TAKEOFF -> 20.0f;
                case RUN -> 12.0f;
                default -> 9.0f;
            };
            float targetDragY = switch (this.previewMotion) {
                case GLIDE -> -18.0f;
                case TAKEOFF -> -12.0f;
                case RUN -> -7.0f;
                default -> -4.0f;
            };
            this.previewDragX = Mth.lerp(0.12f, this.previewDragX, targetDragX);
            this.previewDragY = Mth.lerp(0.12f, this.previewDragY, targetDragY);
        }
    }

    private void lockPreviewModelPosition() {
        this.birdScale = this.basePreviewScale();
        int scale = this.previewRenderScale();
        this.birdX = this.defaultStageX(scale);
        this.birdY = this.defaultStageY(scale);
    }

    private void applyPreviewAnimation(LivingEntity entity) {
        if (entity instanceof NightHeronEntity nightHeron) {
            nightHeron.setGuidePreviewAnimation(this.toNightHeronPreviewAnimation(this.previewAnimation));
        } else if (entity instanceof SparrowEntity sparrow) {
            sparrow.setGuidePreviewAnimation(this.toSparrowPreviewAnimation(this.previewAnimation));
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

    private int previewRenderScale() {
        return this.previewRenderScale(this.birdScale);
    }

    private int previewRenderScale(float scaleMultiplier) {
        float baseScale = Math.min((float)this.previewBoxW() * 0.095f, (float)this.previewBoxH() * 0.24f);
        return Math.max(38, Math.round(baseScale * scaleMultiplier));
    }

    private float basePreviewScale() {
        return this.isNightHeronSelected() ? 1.14f : 1.3f;
    }

    private float stageCenterX() {
        return (float)this.previewBoxX() + (float)this.previewBoxW() * 0.5f;
    }

    private float defaultStageX(int scale) {
        return this.clampStageX(this.stageCenterX(), scale);
    }

    private float defaultStageY(int scale) {
        float top = this.stageSafeTop(scale);
        float bottom = this.stageSafeBottom(scale);
        if (top > bottom) {
            return (float)this.previewBoxY() + (float)this.previewBoxH() * 0.56f;
        }
        return Mth.lerp(0.46f, top, bottom);
    }

    private float stageSafeLeft(int scale) {
        return (float)this.previewBoxX() + 24.0f + (float)scale * 0.7f;
    }

    private float stageSafeRight(int scale) {
        return (float)(this.previewBoxX() + this.previewBoxW()) - 24.0f - (float)scale * 0.7f;
    }

    private float stageSafeTop(int scale) {
        return (float)this.previewBoxY() + 24.0f + (float)scale * 0.94f;
    }

    private float stageSafeBottom(int scale) {
        return (float)(this.previewBoxY() + this.previewBoxH()) - 28.0f - (float)scale * 0.08f;
    }

    private float clampStageX(float x, int scale) {
        float left = this.stageSafeLeft(scale);
        float right = this.stageSafeRight(scale);
        if (left > right) {
            return this.stageCenterX();
        }
        return Mth.clamp(x, left, right);
    }

    private float clampStageY(float y, int scale) {
        float top = this.stageSafeTop(scale);
        float bottom = this.stageSafeBottom(scale);
        if (top > bottom) {
            return this.defaultStageY(scale);
        }
        return Mth.clamp(y, top, bottom);
    }

    private boolean isInPreview(double mouseX, double mouseY) {
        return mouseX >= this.previewBoxX() && mouseX <= this.previewBoxX() + this.previewBoxW()
                && mouseY >= this.previewBoxY() && mouseY <= this.previewBoxY() + this.previewBoxH();
    }

    private boolean isInNotes(double mouseX, double mouseY) {
        return mouseX >= this.notesX() - 8 && mouseX <= this.notesX() + this.notesW() + 8
                && mouseY >= this.notesY() - 8 && mouseY <= this.notesBottom() + 8;
    }

    private BirdGuideEntry selectedEntry(int index) {
        return ENTRIES.get(Mth.clamp(index, 0, ENTRIES.size() - 1));
    }

    private int panelX() {
        return MARGIN;
    }

    private int panelY() {
        return 38;
    }

    private int panelW() {
        return this.width - MARGIN * 2;
    }

    private int panelH() {
        return this.height - 68;
    }

    private int listX() {
        return this.panelX() + 16;
    }

    private int listY() {
        return this.panelY() + 44;
    }

    private int listW() {
        return Math.max(112, Math.min(150, this.width / 4));
    }

    private int detailX() {
        return this.listX() + this.listW() + 20;
    }

    private int detailRight() {
        return this.panelX() + this.panelW() - 18;
    }

    private int notesX() {
        return this.detailX() + 18;
    }

    private int notesY() {
        return this.panelY() + 62;
    }

    private int notesW() {
        return Math.max(110, this.previewBoxX() - this.notesX() - 18);
    }

    private int notesBottom() {
        return this.panelY() + this.panelH() - 44;
    }

    private int detailTextHeight(BirdGuideEntry entry, int notesW) {
        int height = 0;
        for (String section : entry.sections()) {
            MutableComponent body = Component.translatable("gui.guaniao.bird_guide.entry." + entry.id() + "." + section + ".body");
            height += 11 + this.font.split((FormattedText)body, notesW - 10).size() * 10 + 8;
        }
        return height;
    }

    private int maxTextScroll() {
        BirdGuideEntry entry = this.selectedEntry(this.selectedIndex);
        int visibleHeight = this.notesBottom() - this.notesY();
        return Math.max(0, this.detailTextHeight(entry, this.notesW()) - visibleHeight + 20);
    }

    private int previewBoxW() {
        int detailWidth = Math.max(180, this.detailRight() - this.detailX());
        return Mth.clamp(detailWidth / 3, 96, 220);
    }

    private int previewBoxH() {
        return Mth.clamp(this.panelH() - 126, 86, 220);
    }

    private int previewBoxX() {
        return this.detailRight() - this.previewBoxW();
    }

    private int previewBoxY() {
        return this.panelY() + 62;
    }

    private int previewCenterX() {
        return this.previewBoxX() + this.previewBoxW() / 2;
    }

    private boolean isNightHeronSelected() {
        return "night_heron".equals(this.selectedEntry(this.selectedIndex).id());
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

    private record BirdGuideEntry(String id, List<String> sections) {
        private Component title() {
            return Component.translatable("gui.guaniao.bird_guide.entry." + this.id + ".title");
        }

        private Component subtitle() {
            return Component.translatable("gui.guaniao.bird_guide.entry." + this.id + ".subtitle");
        }

        private EntityType<? extends LivingEntity> entityType() {
            return switch (this.id) {
                case "sparrow" -> GuaniaoEntityTypes.SPARROW.get();
                default -> GuaniaoEntityTypes.NIGHT_HERON.get();
            };
        }
    }
}
