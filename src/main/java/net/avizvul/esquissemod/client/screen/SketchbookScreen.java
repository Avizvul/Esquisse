package net.avizvul.esquissemod.client.screen;

import net.avizvul.esquissemod.EsquisseMod;
import net.avizvul.esquissemod.component.ModDataComponents;
import net.avizvul.esquissemod.component.SketchData;
import net.avizvul.esquissemod.item.ModItems;
import net.avizvul.esquissemod.network.SketchbookSavePayload;
import net.avizvul.esquissemod.network.TearPagePayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.List;

public class SketchbookScreen extends Screen {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(EsquisseMod.MOD_ID, "textures/gui/sketchbookgui.png");
    private static final ResourceLocation PENCIL_TEX = ResourceLocation.fromNamespaceAndPath(EsquisseMod.MOD_ID, "textures/gui/button_pencil.png");
    private static final ResourceLocation ERASER_TEX = ResourceLocation.fromNamespaceAndPath(EsquisseMod.MOD_ID, "textures/gui/button_eraser.png");
    private static final ResourceLocation ROTATE_BTN_TEX = ResourceLocation.fromNamespaceAndPath(EsquisseMod.MOD_ID, "textures/gui/button_rotate.png");
    private static final ResourceLocation PAGE_B_TEX = ResourceLocation.fromNamespaceAndPath(EsquisseMod.MOD_ID, "textures/gui/button_page_b.png");
    private static final ResourceLocation PAGE_F_TEX = ResourceLocation.fromNamespaceAndPath(EsquisseMod.MOD_ID, "textures/gui/button_page_f.png");
    private static final ResourceLocation COLOR_PENCIL_TEX = ResourceLocation.fromNamespaceAndPath(EsquisseMod.MOD_ID, "textures/gui/button_color_pencil_base.png");
    private static final ResourceLocation COLOR_PENCIL_TINT_TEX = ResourceLocation.fromNamespaceAndPath(EsquisseMod.MOD_ID, "textures/gui/button_color_pencil_tint.png");
    private static final ResourceLocation RULER_BTN_TEX = ResourceLocation.fromNamespaceAndPath(EsquisseMod.MOD_ID, "textures/gui/button_ruler.png");
    private static final ResourceLocation MAGGLASS_TEX = ResourceLocation.fromNamespaceAndPath(EsquisseMod.MOD_ID, "textures/gui/magnifying_glass_gui.png");
    private static final ResourceLocation MAGGLASS_BTN_TEX = ResourceLocation.fromNamespaceAndPath(EsquisseMod.MOD_ID, "textures/gui/button_magnifying_glass.png");
    private static final ResourceLocation SMUDGE_TEX = ResourceLocation.fromNamespaceAndPath(EsquisseMod.MOD_ID, "textures/gui/button_smudge.png");
    private static final ResourceLocation KNEADED_ERASER_TEX = ResourceLocation.fromNamespaceAndPath(EsquisseMod.MOD_ID, "textures/gui/button_kneaded_eraser.png");
    private static final ResourceLocation COMPASS_BTN_TEX = ResourceLocation.fromNamespaceAndPath(EsquisseMod.MOD_ID, "textures/gui/button_drawing_compass.png");
    private static final ResourceLocation COLOR_MARKER_TEX = ResourceLocation.fromNamespaceAndPath(EsquisseMod.MOD_ID, "textures/gui/button_color_marker_base.png");
    private static final ResourceLocation COLOR_MARKER_TINT_TEX = ResourceLocation.fromNamespaceAndPath(EsquisseMod.MOD_ID, "textures/gui/button_color_marker_tint.png");

    private enum Tool { PENCIL, COLOR_PENCIL, ERASER, SMUDGE, KNEADED_ERASER, COLOR_MARKER }

    private Tool activeTool = Tool.PENCIL;
    private final int fileWidth = 74;
    private final int fileHeight = 96;
    private final int frameWidth = 8;
    private final int deadZoneWidth = 3;
    private final int canvasWidth = 63;
    private final int canvasHeight = this.fileHeight;
    private final int tabWidth = 16;
    private final int tabHeight = 16;
    private final int tabScale = 2;

    private int pencilPixelsUsed = 0;
    private int eraserPixelsUsed = 0;

    private final int scale = 3;
    private final int resolutionMultiplier = 2;

    private int[][] pixels = new int[canvasWidth * resolutionMultiplier][canvasHeight * resolutionMultiplier];
    private boolean[][] strokePixels;
    private boolean isPageLoaded = false;
    private boolean isDrawing = false;
    private boolean isErasing = false;

    private double exactGuiLeft;
    private double exactGuiTop;
    private boolean isDragging = false;
    private float rotationAngle = 0.0f;
    private boolean isRotating = false;

    private final int buttonWidth = 16;
    private final int buttonHeight = 16;
    private final int buttonScale = 2;

    private static double savedGuiLeft = -1;
    private static double savedGuiTop = -1;
    private static float savedRotationAngle = 0.0f;
    private static boolean hasSavedState = false;

    private static final ResourceLocation RULER_TEX = ResourceLocation.fromNamespaceAndPath(EsquisseMod.MOD_ID, "textures/gui/ruler.png");
    private final int rulerWidth = 398;
    private final int rulerHeight = 40;

    private boolean isRulerActive = false;
    private double rulerX, rulerY;
    private float rulerAngle = 0.0f;
    private boolean isRulerDragging = false;
    private boolean isRulerRotating = false;
    private double rulerAngleOffset = 0.0;
    private boolean isQuickRulerMode = false;
    private double quickRulerStartX, quickRulerStartY;
    private double lastMouseX, lastMouseY;

    private double lastLogicalX = -1;
    private double lastLogicalY = -1;

    private static double savedRulerX = -1;
    private static double savedRulerY = -1;
    private static float savedRulerAngle = 0.0f;
    private static boolean wasRulerActive = false;

    private boolean isMagnifyingMode = false;
    private boolean isMagnifierLocked = false;

    private enum CompassState { INACTIVE, FOLDED, ANCHORED, LOCKED }
    private CompassState compassState = CompassState.INACTIVE;
    private double compassAnchorX, compassAnchorY, compassRadius;
    private boolean isQuickCompassMode = false;

    private static CompassState savedCompassState = CompassState.INACTIVE;
    private static double savedCompassAnchorX = -1;
    private static double savedCompassAnchorY = -1;
    private static double savedCompassRadius = -1;

    private List<SketchData> pages = new ArrayList<>();
    private int currentPageIndex = 0;
    private static int savedPageIndex = 0;

    private net.minecraft.client.renderer.texture.DynamicTexture activeCanvasTexture;
    private net.minecraft.resources.ResourceLocation activeCanvasId;
    private boolean isCanvasDirty = true;

    private net.minecraft.world.item.ItemStack getColorMarkerStack() {
        return findItemStack(ModItems.COLOR_MARKER.get());
    }

    private int getMarkerRotation() {
        net.minecraft.world.item.ItemStack stack = getActiveToolStack();
        return stack.isEmpty() ? 0 : stack.getOrDefault(ModDataComponents.MARKER_ROTATION.get(), 0);
    }

    private void setToolSettings(int size, int hardness, int rotation) {
        net.minecraft.world.item.ItemStack stack = getActiveToolStack();
        if (!stack.isEmpty()) {
            stack.set(ModDataComponents.BRUSH_SIZE.get(), size);
            stack.set(ModDataComponents.BRUSH_HARDNESS.get(), hardness);
            if (this.activeTool == Tool.COLOR_MARKER) stack.set(ModDataComponents.MARKER_ROTATION.get(), rotation);
        }
        net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                new net.avizvul.esquissemod.network.ToolSettingsPayload(this.activeTool.ordinal(), size, hardness, rotation)
        );
    }

    public SketchbookScreen() {
        super(Component.literal("Sketchbook"));
    }

    @Override
    protected void init() {
        super.init();
        if (!this.isPageLoaded) {
            net.minecraft.world.item.ItemStack stack = this.minecraft.player.getMainHandItem();
            if (!stack.is(ModItems.SKETCHBOOK.get())) {
                stack = this.minecraft.player.getOffhandItem();
            }
            this.currentPageIndex = stack.getOrDefault(ModDataComponents.LAST_PAGE.get(), 0);
            this.isPageLoaded = true;
        }

        if (hasSavedState) {
            this.exactGuiLeft = savedGuiLeft;
            this.exactGuiTop = savedGuiTop;
            this.rotationAngle = savedRotationAngle;
        } else {
            int scaledWidth = this.fileWidth * this.scale;
            int scaledHeight = this.fileHeight * this.scale;
            this.exactGuiLeft = (this.width - scaledWidth) / 2.0;
            this.exactGuiTop = (this.height - scaledHeight) / 2.0;
        }
        clampSketchbook();

        if (this.minecraft != null && this.minecraft.player != null) {
            ItemStack stack = this.minecraft.player.getMainHandItem();
            if (stack.has(ModDataComponents.SKETCHBOOK_PAGES.get())) {
                this.pages = new ArrayList<>(stack.get(ModDataComponents.SKETCHBOOK_PAGES.get()));
            } else {
                this.pages = new ArrayList<>();
                SketchData emptyData = SketchData.fromArray(new int[this.canvasWidth * this.resolutionMultiplier][this.canvasHeight * this.resolutionMultiplier]);
                for (int i = 0; i < 16; i++) {
                    this.pages.add(emptyData);
                }
            }

            if (savedRulerX != -1) {
                this.rulerX = savedRulerX;
                this.rulerY = savedRulerY;
                this.rulerAngle = savedRulerAngle;
                this.isRulerActive = wasRulerActive;
                this.compassState = savedCompassState;
                this.compassAnchorX = savedCompassAnchorX;
                this.compassAnchorY = savedCompassAnchorY;
                this.compassRadius = savedCompassRadius;
            } else {
                this.rulerX = this.width / 2.0;
                this.rulerY = this.height / 2.0;
            }
            loadPagePixels();
        }
    }

    private void loadPagePixels() {
        if (this.pages != null && this.currentPageIndex >= 0 && this.currentPageIndex < this.pages.size()) {
            SketchData data = this.pages.get(this.currentPageIndex);
            this.pixels = data.toArray(this.canvasWidth * this.resolutionMultiplier, this.canvasHeight * this.resolutionMultiplier);
        }
        this.isCanvasDirty = true;
    }

    private record TabCoords(int tabX, int backTabY, int forwardTabY) {}

    private TabCoords getTabCoords(int renderX, int renderY, int drawWidth) {
        int scaledTabWidth = this.tabWidth * this.tabScale;
        int scaledTabHeight = this.tabHeight * this.tabScale;
        int tabXOffset = 0;
        int topTabYOffset = 1;
        int gapBetweenTabs = 1;
        int tabX = renderX + drawWidth - tabXOffset;
        int backTabY = renderY + (topTabYOffset * this.scale);
        int forwardTabY = backTabY + scaledTabHeight + (gapBetweenTabs * this.scale);
        return new TabCoords(tabX, backTabY, forwardTabY);
    }

    private record ToolButtonCoords(int scaledBtnWidth, int scaledBtnHeight, int pencilX, int colorPencilX, int colorMarkerX, int eraserX, int kneadedX, int smudgeX, int rulerX, int magGlassX, int compassX, int peekY) {}

    private ToolButtonCoords getToolButtonCoords() {
        int scaledBtnWidth = this.buttonWidth * this.buttonScale;
        int scaledBtnHeight = this.buttonHeight * this.buttonScale;
        int peekY = this.height - scaledBtnHeight;
        int startX = (this.width / 2) + 100;
        int currentX = startX;

        boolean hasPencil = hasTool(ModItems.PENCIL.get());
        boolean hasEraser = hasTool(ModItems.ERASER.get());
        boolean hasRuler = hasTool(ModItems.RULER.get());
        boolean hasMagGlass = hasTool(ModItems.MAGNIFYING_GLASS.get());
        boolean hasSmudge = hasTool(ModItems.SMUDGE.get());
        boolean hasKneaded = hasTool(ModItems.KNEADED_ERASER.get());
        boolean hasCompass = hasTool(ModItems.DRAWING_COMPASS.get());

        net.minecraft.world.item.ItemStack colorPencilStack = getColorPencilStack();
        boolean hasColorPencil = !colorPencilStack.isEmpty();
        boolean hasColorMarker = hasTool(ModItems.COLOR_MARKER.get());

        int pencilX = -1000, colorPencilX = -1000, colorMarkerX = -1000, eraserX = -1000, kneadedX = -1000, smudgeX = -1000, rulerX = -1000, magGlassX = -1000, compassX = -1000;

        int leftX = (this.width / 2) - 100 - scaledBtnWidth;
        if (hasRuler) { rulerX = leftX; leftX -= (scaledBtnWidth + 5); }
        if (hasMagGlass) { magGlassX = leftX; leftX -= (scaledBtnWidth + 5); }
        if (hasCompass) { compassX = leftX; }

        if (hasPencil) { pencilX = currentX; currentX += scaledBtnWidth + 5; }
        if (hasColorPencil) { colorPencilX = currentX; currentX += scaledBtnWidth + 5; }
        if (hasColorMarker) { colorMarkerX = currentX; currentX += scaledBtnWidth + 5; }
        if (hasEraser) { eraserX = currentX; currentX += scaledBtnWidth + 5; }
        if (hasKneaded) { kneadedX = currentX; currentX += scaledBtnWidth + 5; }
        if (hasSmudge) { smudgeX = currentX; currentX += scaledBtnWidth + 5; }

        return new ToolButtonCoords(scaledBtnWidth, scaledBtnHeight, pencilX, colorPencilX, colorMarkerX, eraserX, kneadedX, smudgeX, rulerX, magGlassX, compassX, peekY);
    }

    public void turnPage(int newPageIndex) {
        net.minecraft.world.item.ItemStack stack = this.minecraft.player.getMainHandItem();
        if (!stack.is(ModItems.SKETCHBOOK.get())) {
            stack = this.minecraft.player.getOffhandItem();
        }
        java.util.List<net.avizvul.esquissemod.component.SketchData> pages =
                new java.util.ArrayList<>(stack.getOrDefault(ModDataComponents.SKETCHBOOK_PAGES.get(), new java.util.ArrayList<>()));

        if (newPageIndex < 0 || newPageIndex >= pages.size()) {
            return;
        }

        if (this.currentPageIndex >= 0 && this.currentPageIndex < pages.size()) {
            net.avizvul.esquissemod.component.SketchData data = net.avizvul.esquissemod.component.SketchData.fromArray(this.pixels);
            pages.set(this.currentPageIndex, data);
            stack.set(ModDataComponents.SKETCHBOOK_PAGES.get(), pages);
            net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                    new SketchbookSavePayload(this.currentPageIndex, data, this.pencilPixelsUsed, this.eraserPixelsUsed)
            );
        }

        this.currentPageIndex = newPageIndex;
        int w = this.canvasWidth * this.resolutionMultiplier;
        int h = this.canvasHeight * this.resolutionMultiplier;
        this.pixels = pages.get(this.currentPageIndex).toArray(w, h);
        this.isCanvasDirty = true;
    }

    private void clampSketchbook() {
        double cx = this.exactGuiLeft + (this.fileWidth * this.scale) / 2.0;
        double cy = this.exactGuiTop + (this.fileHeight * this.scale) / 2.0;
        double rad = Math.toRadians(this.rotationAngle);
        double absCos = Math.abs(Math.cos(rad));
        double absSin = Math.abs(Math.sin(rad));

        int originalWidth = this.fileWidth * this.scale;
        int originalHeight = this.fileHeight * this.scale;

        double boundingWidth = originalWidth * absCos + originalHeight * absSin;
        double boundingHeight = originalWidth * absSin + originalHeight * absCos;

        cx = Math.max(boundingWidth / 2.0, Math.min(cx, this.width - boundingWidth / 2.0));
        cy = Math.max(boundingHeight / 2.0, Math.min(cy, this.height - boundingHeight / 2.0));

        this.exactGuiLeft = cx - originalWidth / 2.0;
        this.exactGuiTop = cy - originalHeight / 2.0;
    }

    private double[] getLogicalMouse(double mouseX, double mouseY) {
        if (this.rotationAngle == 0.0f) return new double[]{mouseX, mouseY};

        double cx = this.exactGuiLeft + (this.fileWidth * this.scale) / 2.0;
        double cy = this.exactGuiTop + (this.fileHeight * this.scale) / 2.0;

        double dx = mouseX - cx;
        double dy = mouseY - cy;
        double rad = Math.toRadians(-this.rotationAngle);
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);

        double logicalX = cx + (dx * cos - dy * sin);
        double logicalY = cy + (dx * sin + dy * cos);

        return new double[]{logicalX, logicalY};
    }

    private net.minecraft.world.item.ItemStack findItemStack(net.minecraft.world.item.Item targetItem) {
        if (this.minecraft == null || this.minecraft.player == null) return net.minecraft.world.item.ItemStack.EMPTY;
        for (net.minecraft.world.item.ItemStack stack : this.minecraft.player.getInventory().items) {
            if (stack.is(targetItem)) return stack;
            net.minecraft.world.item.ItemStack fromCase = checkPencilCase(stack, targetItem);
            if (!fromCase.isEmpty()) return fromCase;
        }
        for (net.minecraft.world.item.ItemStack stack : this.minecraft.player.getInventory().offhand) {
            if (stack.is(targetItem)) return stack;
            net.minecraft.world.item.ItemStack fromCase = checkPencilCase(stack, targetItem);
            if (!fromCase.isEmpty()) return fromCase;
        }
        return net.minecraft.world.item.ItemStack.EMPTY;
    }

    private net.minecraft.world.item.ItemStack getActiveToolStack() {
        return switch (this.activeTool) {
            case PENCIL -> findItemStack(ModItems.PENCIL.get());
            case COLOR_PENCIL -> getColorPencilStack();
            case ERASER -> findItemStack(ModItems.ERASER.get());
            case KNEADED_ERASER -> findItemStack(ModItems.KNEADED_ERASER.get());
            case SMUDGE -> findItemStack(ModItems.SMUDGE.get());
            case COLOR_MARKER -> findItemStack(ModItems.COLOR_MARKER.get());
        };
    }

    private int getBrushSize() {
        net.minecraft.world.item.ItemStack stack = getActiveToolStack();
        return stack.isEmpty() ? 1 : stack.getOrDefault(ModDataComponents.BRUSH_SIZE.get(), 1);
    }

    private int getHardness() {
        net.minecraft.world.item.ItemStack stack = getActiveToolStack();
        return stack.isEmpty() ? 3 : stack.getOrDefault(ModDataComponents.BRUSH_HARDNESS.get(), 3);
    }

    private net.minecraft.world.item.ItemStack checkPencilCase(net.minecraft.world.item.ItemStack containerStack, net.minecraft.world.item.Item targetItem) {
        if (containerStack.is(net.avizvul.esquissemod.item.ModItems.PENCIL_CASE.get()) && containerStack.has(net.minecraft.core.component.DataComponents.CONTAINER)) {
            net.minecraft.world.item.component.ItemContainerContents contents = containerStack.get(net.minecraft.core.component.DataComponents.CONTAINER);
            if (contents != null) {
                net.minecraft.core.NonNullList<net.minecraft.world.item.ItemStack> items = net.minecraft.core.NonNullList.withSize(9, net.minecraft.world.item.ItemStack.EMPTY);
                contents.copyInto(items);
                for (net.minecraft.world.item.ItemStack innerStack : items) {
                    if (innerStack.is(targetItem)) return innerStack;
                }
            }
        }
        return net.minecraft.world.item.ItemStack.EMPTY;
    }

    private boolean hasTool(net.minecraft.world.item.Item toolItem) {
        return !findItemStack(toolItem).isEmpty();
    }

    private net.minecraft.world.item.ItemStack getColorPencilStack() {
        return findItemStack(net.avizvul.esquissemod.item.ModItems.COLOR_PENCIL.get());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {}

    private void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;

        if (this.isQuickRulerMode) {
            this.rulerX = (this.quickRulerStartX + mouseX) / 2.0;
            this.rulerY = (this.quickRulerStartY + mouseY) / 2.0;
            this.rulerAngle = (float) Math.toDegrees(Math.atan2(mouseY - this.quickRulerStartY, mouseX - this.quickRulerStartX));
        }

        int renderX = (int) this.exactGuiLeft;
        int renderY = (int) this.exactGuiTop;
        int drawWidth = this.fileWidth * this.scale;
        int drawHeight = this.fileHeight * this.scale;

        boolean hasPencil = hasTool(ModItems.PENCIL.get());
        boolean hasEraser = hasTool(ModItems.ERASER.get());
        boolean hasSmudge = hasTool(ModItems.SMUDGE.get());
        boolean hasKneaded = hasTool(ModItems.KNEADED_ERASER.get());
        boolean hasColorMarker = hasTool(ModItems.COLOR_MARKER.get());

        ItemStack colorPencilStack = getColorPencilStack();
        boolean hasColorPencil = !colorPencilStack.isEmpty();

        boolean hasPencilColors = hasColorPencil && !colorPencilStack.getOrDefault(ModDataComponents.STORED_COLORS.get(), new java.util.ArrayList<>()).isEmpty();
        boolean hasMarkerColors = hasColorMarker && !getColorMarkerStack().getOrDefault(ModDataComponents.STORED_COLORS.get(), new java.util.ArrayList<>()).isEmpty();

        double cx = renderX + drawWidth / 2.0;
        double cy = renderY + drawHeight / 2.0;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(cx, cy, 0);
        if (this.rotationAngle != 0.0f) {
            guiGraphics.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(this.rotationAngle));
        }
        guiGraphics.pose().translate(-cx, -cy, 0);

        int scaledTabWidth = this.tabWidth * this.tabScale;
        int scaledTabHeight = this.tabHeight * this.tabScale;
        TabCoords coords = getTabCoords(renderX, renderY, drawWidth);

        int tabX = coords.tabX();
        int backTabY = coords.backTabY();
        int forwardTabY = coords.forwardTabY();

        double[] logicalMouse = getLogicalMouse(mouseX, mouseY);
        double lMouseX = logicalMouse[ 0 ];
        double lMouseY = logicalMouse[ 1 ];

        if (this.currentPageIndex > 0) {
            boolean backHovered = !this.isDragging && !this.isRotating && lMouseX >= tabX && lMouseX < tabX + scaledTabWidth && lMouseY >= backTabY && lMouseY < backTabY + scaledTabHeight;
            float backVOffset = backHovered ? this.tabHeight : 0.0f;
            guiGraphics.blit(PAGE_B_TEX, tabX, backTabY, scaledTabWidth, scaledTabHeight, 0.0f, backVOffset, this.tabWidth, this.tabHeight, this.tabWidth, this.tabHeight * 2);
        }

        if (this.currentPageIndex < 15) {
            boolean forwardHovered = !this.isDragging && !this.isRotating && lMouseX >= tabX && lMouseX < tabX + scaledTabWidth && lMouseY >= forwardTabY && lMouseY < forwardTabY + scaledTabHeight;
            float forwardVOffset = forwardHovered ? this.tabHeight : 0.0f;
            guiGraphics.blit(PAGE_F_TEX, tabX, forwardTabY, scaledTabWidth, scaledTabHeight, 0.0f, forwardVOffset, this.tabWidth, this.tabHeight, this.tabWidth, this.tabHeight * 2);
        }

        guiGraphics.blit(TEXTURE, renderX, renderY, drawWidth, drawHeight, 0.0f, 0.0f, this.fileWidth, this.fileHeight, this.fileWidth, this.fileHeight);

        int blueZoneWidth = this.deadZoneWidth * this.scale;
        int blueZoneLeft = renderX + (this.frameWidth * this.scale);
        int blueZoneRight = blueZoneLeft + blueZoneWidth;
        int blueZoneTop = renderY;
        int blueZoneBottom = renderY + (this.canvasHeight * this.scale);

        if (lMouseX >= blueZoneLeft && lMouseX <= blueZoneRight && lMouseY >= blueZoneTop && lMouseY <= blueZoneBottom) {
            int dashLength = 5;
            int dashGap = 3;
            int lineWidth = 2;
            int color = 0xFFEE0000;
            int lineX = blueZoneLeft + (blueZoneWidth / 2) - (lineWidth / 2);

            for (int y = blueZoneTop; y < blueZoneBottom; y += dashLength + dashGap) {
                int currentDashBottom = Math.min(y + dashLength, blueZoneBottom);
                guiGraphics.fill(lineX, y, lineX + lineWidth, currentDashBottom, color);
            }
        }

        int btnFileWidth = 8;
        int btnFileHeight = 8;
        int btnX = renderX;
        int btnY = renderY + ((this.fileHeight - btnFileHeight) / 2) * this.scale;

        guiGraphics.blit(ROTATE_BTN_TEX, btnX, btnY, btnFileWidth * this.scale, btnFileHeight * this.scale, 0.0f, 0.0f, btnFileWidth, btnFileHeight, btnFileWidth, btnFileHeight);

        int canvasScreenLeft = renderX + ((this.frameWidth + this.deadZoneWidth) * this.scale);
        int canvasScreenTop = renderY;

        if (this.isCanvasDirty) updateActiveCanvasTexture();

        if (this.activeCanvasId != null) {
            com.mojang.blaze3d.systems.RenderSystem.enableBlend();
            int screenWidth = this.canvasWidth * this.scale;
            int screenHeight = this.canvasHeight * this.scale;
            guiGraphics.blit(this.activeCanvasId, canvasScreenLeft, canvasScreenTop, 0.0f, 0.0f, screenWidth, screenHeight, screenWidth, screenHeight);
            com.mojang.blaze3d.systems.RenderSystem.disableBlend();
        }

        guiGraphics.pose().pushPose();
        float resScale = 1.0f / this.resolutionMultiplier;
        guiGraphics.pose().scale(resScale, resScale, 1.0f);

        int scaledCanvasLeft = canvasScreenLeft * this.resolutionMultiplier;
        int scaledCanvasTop = canvasScreenTop * this.resolutionMultiplier;
        int scaledCanvasWidth = this.canvasWidth * this.scale;
        int scaledImageHeight = this.fileHeight * this.scale;

        if (!this.isDragging && !this.isRotating && lMouseX >= canvasScreenLeft && lMouseX < (canvasScreenLeft + scaledCanvasWidth) && lMouseY >= renderY && lMouseY < (renderY + scaledImageHeight)) {

            boolean canDraw = (this.activeTool == Tool.PENCIL && hasPencil) ||
                    (this.activeTool == Tool.COLOR_PENCIL && hasPencilColors) ||
                    (this.activeTool == Tool.COLOR_MARKER && hasMarkerColors) ||
                    (this.activeTool == Tool.ERASER && hasEraser) ||
                    (this.activeTool == Tool.SMUDGE && hasSmudge) ||
                    (this.activeTool == Tool.KNEADED_ERASER && hasKneaded);

            if (canDraw) {
                double physicalCellSize = (double) this.scale / this.resolutionMultiplier;
                double[] magnetMouse = applyRulerMagnet(mouseX, mouseY);
                double[] lMouseMagnet = getLogicalMouse(magnetMouse[ 0 ], magnetMouse[ 1 ]);

                int centerX = (int) ((lMouseMagnet[ 0 ] - canvasScreenLeft) / physicalCellSize);
                int centerY = (int) ((lMouseMagnet[ 1 ] - renderY) / physicalCellSize);

                int currentBrushSize = getBrushSize();
                int actualSize = currentBrushSize;

                if (this.activeTool == Tool.SMUDGE || this.activeTool == Tool.KNEADED_ERASER) {
                    actualSize = (currentBrushSize == 1) ? 2 : (currentBrushSize == 2) ? 5 : 12;
                }

                int offset = actualSize / 2;
                double radius = actualSize / 2.0;

                double exactCX = centerX + (actualSize % 2 == 0 ? -0.5 : 0.0);
                double exactCY = centerY + (actualSize % 2 == 0 ? -0.5 : 0.0);

                boolean isMarker = (this.activeTool == Tool.COLOR_MARKER);
                if (isMarker) {
                    exactCX = centerX + 0.5;
                    exactCY = centerY + 0.5;
                }

                int markerRot = isMarker ? getMarkerRotation() : 0;
                double angleRad = Math.toRadians(markerRot * 15.0);
                double mCos = Math.cos(angleRad);
                double mSin = Math.sin(angleRad);

                double thickness = currentBrushSize + 1.0;
                double length = currentBrushSize * 5.0;

                int bound = isMarker ? (int) Math.ceil(length / 2.0) + 1 : offset;

                int startX = isMarker ? (centerX - bound) : (centerX - offset);
                int endX = isMarker ? (centerX + bound) : (centerX - offset + actualSize - 1);
                int startY = isMarker ? (centerY - bound) : (centerY - offset);
                int endY = isMarker ? (centerY + bound) : (centerY - offset + actualSize - 1);

                int previewColor = (this.activeTool == Tool.ERASER || this.activeTool == Tool.KNEADED_ERASER) ? 0x60FF0000 : 0x60000000;

                if ((this.activeTool == Tool.COLOR_PENCIL && hasPencilColors) || (this.activeTool == Tool.COLOR_MARKER && hasMarkerColors)) {
                    ItemStack activeStack = (this.activeTool == Tool.COLOR_MARKER) ? getColorMarkerStack() : colorPencilStack;
                    int activeIndex = activeStack.getOrDefault(net.avizvul.esquissemod.component.ModDataComponents.ACTIVE_COLOR_INDEX.get(), 0);
                    java.util.List<Integer> toolColors = activeStack.getOrDefault(net.avizvul.esquissemod.component.ModDataComponents.STORED_COLORS.get(), new java.util.ArrayList<>());
                    int colorId = toolColors.get(Math.abs(activeIndex) % toolColors.size());
                    previewColor = net.minecraft.world.item.DyeColor.byId(colorId).getTextureDiffuseColor() | 0x60000000;
                }

                for (int x = startX; x <= endX; x++) {
                    for (int y = startY; y <= endY; y++) {
                        double dx = x - exactCX;
                        double dy = y - exactCY;

                        if (isMarker) {
                            double localX = dx * mCos + dy * mSin;
                            double localY = -dx * mSin + dy * mCos;
                            if (Math.abs(localX) >= length / 2.0 || Math.abs(localY) >= thickness / 2.0) continue;
                        } else {
                            if ((this.activeTool == Tool.SMUDGE || this.activeTool == Tool.KNEADED_ERASER) && Math.sqrt(dx * dx + dy * dy) > radius) continue;
                        }

                        if (x >= 0 && x < this.canvasWidth * this.resolutionMultiplier && y >= 0 && y < this.canvasHeight * this.resolutionMultiplier) {
                            int drawPixelX = scaledCanvasLeft + (x * this.scale);
                            int drawPixelY = scaledCanvasTop + (y * this.scale);
                            guiGraphics.fill(drawPixelX, drawPixelY, drawPixelX + this.scale, drawPixelY + this.scale, previewColor);
                        }
                    }
                }
            }
        }

        guiGraphics.pose().popPose();

        String pageText = String.valueOf(this.currentPageIndex + 1);
        int textX = renderX + (this.fileWidth * this.scale) - this.font.width(pageText) - 10;
        int textY = renderY + (this.fileHeight * this.scale) - 15;
        guiGraphics.drawString(this.font, pageText, textX, textY, 0xFF777777, false);

        if (this.compassState != CompassState.INACTIVE) {
            double screenAnchorX = lMouseX, screenAnchorY = lMouseY;
            double screenPencilX = lMouseX, screenPencilY = lMouseY;

            if (this.compassState != CompassState.FOLDED) {
                screenAnchorX = this.compassAnchorX;
                screenAnchorY = this.compassAnchorY;
                if (this.compassState == CompassState.ANCHORED) {
                    double dx = lMouseX - this.compassAnchorX;
                    double dy = lMouseY - this.compassAnchorY;
                    double dist = Math.sqrt(dx*dx + dy*dy);
                    if (dist > 192.0) {
                        dx = (dx / dist) * 192.0;
                        dy = (dy / dist) * 192.0;
                    }
                    screenPencilX = this.compassAnchorX + dx;
                    screenPencilY = this.compassAnchorY + dy;
                } else if (this.compassState == CompassState.LOCKED) {
                    double angle = Math.atan2(lMouseY - this.compassAnchorY, lMouseX - this.compassAnchorX);
                    screenPencilX = this.compassAnchorX + this.compassRadius * Math.cos(angle);
                    screenPencilY = this.compassAnchorY + this.compassRadius * Math.sin(angle);
                }
            }
            net.avizvul.esquissemod.client.render.CompassGeometryCalculator.renderCompass(guiGraphics, screenAnchorX, screenAnchorY, screenPencilX, screenPencilY);
        }
        guiGraphics.pose().popPose();

        ToolButtonCoords toolCoords = getToolButtonCoords();
        int scaledBtnWidth = toolCoords.scaledBtnWidth();
        int scaledBtnHeight = toolCoords.scaledBtnHeight();
        int peekY = toolCoords.peekY();

        if (hasPencil) renderToolButton(guiGraphics, mouseX, mouseY, this.activeTool == Tool.PENCIL, PENCIL_TEX, toolCoords.pencilX());
        if (hasColorPencil) renderColorToolButton(guiGraphics, mouseX, mouseY, toolCoords.colorPencilX(), colorPencilStack, COLOR_PENCIL_TEX, COLOR_PENCIL_TINT_TEX, Tool.COLOR_PENCIL);
        if (hasColorMarker) renderColorToolButton(guiGraphics, mouseX, mouseY, toolCoords.colorMarkerX(), getColorMarkerStack(), COLOR_MARKER_TEX, COLOR_MARKER_TINT_TEX, Tool.COLOR_MARKER);
        if (hasEraser) renderToolButton(guiGraphics, mouseX, mouseY, this.activeTool == Tool.ERASER, ERASER_TEX, toolCoords.eraserX());
        if (hasKneaded) renderToolButton(guiGraphics, mouseX, mouseY, this.activeTool == Tool.KNEADED_ERASER, KNEADED_ERASER_TEX, toolCoords.kneadedX());
        if (hasSmudge) renderToolButton(guiGraphics, mouseX, mouseY, this.activeTool == Tool.SMUDGE, SMUDGE_TEX, toolCoords.smudgeX());

        boolean hasRuler = hasTool(net.avizvul.esquissemod.item.ModItems.RULER.get());
        if (!hasRuler && this.isRulerActive) {
            this.isRulerActive = false;
            this.isQuickRulerMode = false;
        }
        if (hasRuler && !this.isRulerActive) {
            renderToolButton(guiGraphics, mouseX, mouseY, false, RULER_BTN_TEX, toolCoords.rulerX());
        }

        boolean hasMagGlass = hasTool(ModItems.MAGNIFYING_GLASS.get());
        if (hasMagGlass && !this.isMagnifierLocked) {
            renderToolButton(guiGraphics, mouseX, mouseY, false, MAGGLASS_BTN_TEX, toolCoords.magGlassX());
        }

        boolean hasCompass = hasTool(ModItems.DRAWING_COMPASS.get());
        if (hasCompass && this.compassState == CompassState.INACTIVE) {
            renderToolButton(guiGraphics, mouseX, mouseY, false, COMPASS_BTN_TEX, toolCoords.compassX());
        }

        if (this.activeTool == Tool.PENCIL && hasPencil) {
            renderSizeIndicators(guiGraphics, mouseX, mouseY, toolCoords.pencilX(), peekY);
        } else if ((this.activeTool == Tool.COLOR_PENCIL && hasPencilColors) || (this.activeTool == Tool.COLOR_MARKER && hasMarkerColors)) {
            int activeX = (this.activeTool == Tool.COLOR_MARKER) ? toolCoords.colorMarkerX() : toolCoords.colorPencilX();
            ItemStack activeStack = (this.activeTool == Tool.COLOR_MARKER) ? getColorMarkerStack() : colorPencilStack;
            boolean toolHasColors = !activeStack.getOrDefault(ModDataComponents.STORED_COLORS.get(), new java.util.ArrayList<>()).isEmpty();

            if (toolHasColors) {
                renderSizeIndicators(guiGraphics, mouseX, mouseY, activeX, peekY);
            } else {
                String emptyText = "Empty";
                int emptyX = activeX + (scaledBtnWidth / 2) - (this.font.width(emptyText) / 2);
                guiGraphics.drawString(this.font, emptyText, emptyX, peekY - 24, 0xFFFF0000, false);
            }
            renderPalette(guiGraphics, activeStack);
        } else if (this.activeTool == Tool.ERASER && hasEraser) {
            renderSizeIndicators(guiGraphics, mouseX, mouseY, toolCoords.eraserX(), peekY);
        } else if (this.activeTool == Tool.KNEADED_ERASER && hasKneaded) {
            renderSizeIndicators(guiGraphics, mouseX, mouseY, toolCoords.kneadedX(), peekY);
        } else if (this.activeTool == Tool.SMUDGE && hasSmudge) {
            renderSizeIndicators(guiGraphics, mouseX, mouseY, toolCoords.smudgeX(), peekY);
        }

        if ((this.activeTool == Tool.PENCIL && hasPencil) || (this.activeTool == Tool.COLOR_PENCIL && hasPencilColors) ||
                (this.activeTool == Tool.COLOR_MARKER && hasMarkerColors) ||
                (this.activeTool == Tool.SMUDGE && hasSmudge) || (this.activeTool == Tool.KNEADED_ERASER && hasKneaded)) {

            int currentToolHardness = getHardness();
            String hardnessText = (currentToolHardness == 1) ? "2H" : (currentToolHardness == 2) ? "HB" : "4B";

            if (this.activeTool == Tool.SMUDGE || this.activeTool == Tool.KNEADED_ERASER) {
                hardnessText = (currentToolHardness == 1) ? "S" : (currentToolHardness == 2) ? "M" : "H";
            } else if (this.activeTool == Tool.COLOR_MARKER) {
                hardnessText = (currentToolHardness == 1) ? "L" : (currentToolHardness == 2) ? "M" : "H";
            }

            int hardnessColor = (currentToolHardness == 1) ? 0xFFAAAAAA : (currentToolHardness == 2) ? 0xFF555555 : 0xFF222222;
            int activeX = (this.activeTool == Tool.PENCIL) ? toolCoords.pencilX() :
                    (this.activeTool == Tool.SMUDGE) ? toolCoords.smudgeX() :
                    (this.activeTool == Tool.KNEADED_ERASER) ? toolCoords.kneadedX() :
                    (this.activeTool == Tool.COLOR_MARKER) ? toolCoords.colorMarkerX() : toolCoords.colorPencilX();

            int hX = activeX + (scaledBtnWidth / 2) - (this.font.width(hardnessText) / 2);
            guiGraphics.drawString(this.font, hardnessText, hX, peekY - 24, hardnessColor, false);
        }

        if (this.isRulerActive) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(this.rulerX, this.rulerY, 0.5f);
            guiGraphics.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(this.rulerAngle));
            com.mojang.blaze3d.systems.RenderSystem.enableBlend();
            guiGraphics.blit(RULER_TEX, -this.rulerWidth / 2, 0, 0.0f, 0.0f, this.rulerWidth, this.rulerHeight, this.rulerWidth, this.rulerHeight);
            com.mojang.blaze3d.systems.RenderSystem.disableBlend();
            guiGraphics.pose().popPose();
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        boolean hasMagGlass = hasTool(ModItems.MAGNIFYING_GLASS.get());
        if (!hasMagGlass) {
            this.isMagnifyingMode = false;
            this.isMagnifierLocked = false;
        }

        boolean isMagActive = this.isMagnifyingMode || this.isMagnifierLocked;
        renderContent(guiGraphics, mouseX, mouseY, partialTick);

        if (isMagActive) {
            float glassScale = 2.5f;
            int baseRadius = 13;
            int scaledRadius = (int) (baseRadius * glassScale);

            guiGraphics.enableScissor(mouseX - scaledRadius, mouseY - scaledRadius, mouseX + scaledRadius, mouseY + scaledRadius);
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(mouseX, mouseY, 0);
            guiGraphics.pose().scale(glassScale, glassScale, 1.0f);
            guiGraphics.pose().translate(-mouseX, -mouseY, 0);

            renderContent(guiGraphics, mouseX, mouseY, partialTick);

            guiGraphics.pose().popPose();
            guiGraphics.disableScissor();

            com.mojang.blaze3d.systems.RenderSystem.enableBlend();
            int texWidth = 29;
            int texHeight = 58;
            int destWidth = (int) (texWidth * glassScale);
            int destHeight = (int) (texHeight * glassScale);
            int offsetX = (int) (14 * glassScale);
            int offsetY = (int) (14 * glassScale);
            guiGraphics.blit(MAGGLASS_TEX, mouseX - offsetX, mouseY - offsetY, destWidth, destHeight, 0.0f, 0.0f, texWidth, texHeight, texWidth, texHeight);
            com.mojang.blaze3d.systems.RenderSystem.disableBlend();
        }
    }

    private void renderToolButton(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean isSelected, ResourceLocation texture, int x) {
        int scaledWidth = this.buttonWidth * this.buttonScale;
        int scaledHeight = this.buttonHeight * this.buttonScale;
        int peekY = this.height - scaledHeight;
        int baseY = this.height - (scaledHeight / 2);
        int renderY = isSelected ? peekY : baseY;

        boolean isHovered = mouseX >= x && mouseX < x + scaledWidth && mouseY >= renderY && mouseY < renderY + scaledHeight;
        float vOffset = isHovered ? this.buttonHeight : 0.0f;

        guiGraphics.blit(texture, x, renderY, scaledWidth, scaledHeight, 0.0f, vOffset, this.buttonWidth, this.buttonHeight, this.buttonWidth, this.buttonHeight * 2);
    }

    private void renderColorToolButton(GuiGraphics guiGraphics, int mouseX, int mouseY, int x, net.minecraft.world.item.ItemStack colorStack, ResourceLocation baseTex, ResourceLocation tintTex, Tool tool) {
        boolean isSelected = (this.activeTool == tool);
        int scaledWidth = this.buttonWidth * this.buttonScale;
        int scaledHeight = this.buttonHeight * this.buttonScale;
        int peekY = this.height - scaledHeight;
        int baseY = this.height - (scaledHeight / 2);
        int renderY = isSelected ? peekY : baseY;

        boolean isHovered = mouseX >= x && mouseX < x + scaledWidth && mouseY >= renderY && mouseY < renderY + scaledHeight;
        float vOffset = isHovered ? this.buttonHeight : 0.0f;

        guiGraphics.blit(baseTex, x, renderY, scaledWidth, scaledHeight, 0.0f, vOffset, this.buttonWidth, this.buttonHeight, this.buttonWidth, this.buttonHeight * 2);

        int rgb = 0xFFFFFF;
        java.util.List<Integer> colors = colorStack.getOrDefault(ModDataComponents.STORED_COLORS.get(), new java.util.ArrayList<>());
        if (!colors.isEmpty()) {
            int activeIndex = colorStack.getOrDefault(ModDataComponents.ACTIVE_COLOR_INDEX.get(), 0);
            int colorId = colors.get(Math.abs(activeIndex) % colors.size());
            rgb = net.minecraft.world.item.DyeColor.byId(colorId).getTextureDiffuseColor();
        }

        float r = ((rgb >> 16) & 0xFF) / 255.0f;
        float g = ((rgb >> 8) & 0xFF) / 255.0f;
        float b = (rgb & 0xFF) / 255.0f;

        guiGraphics.setColor(r, g, b, 1.0f);
        guiGraphics.blit(tintTex, x, renderY, scaledWidth, scaledHeight, 0.0f, vOffset, this.buttonWidth, this.buttonHeight, this.buttonWidth, this.buttonHeight * 2);
        guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private record Swatch(int colorId, int x, int y) {}

    private java.util.List<Swatch> getPaletteLayout() {
        java.util.List<Swatch> layout = new java.util.ArrayList<>();
        java.util.List<Integer> grays = java.util.List.of(0, 8, 7, 15);
        java.util.List<Integer> wheelColors = java.util.List.of(14, 1, 4, 5, 13, 9, 3, 11, 10, 2, 6, 12);

        int centerX = this.width - 80;
        int centerY = this.height - 70;
        int radius = 30;
        int swatchSize = 12;

        for (int i = 0; i < wheelColors.size(); i++) {
            int colorId = wheelColors.get(i);
            double angle = 2 * Math.PI * i / wheelColors.size() - Math.PI / 2;
            int x = centerX + (int) (Math.cos(angle) * radius) - (swatchSize / 2);
            int y = centerY + (int) (Math.sin(angle) * radius) - (swatchSize / 2);
            layout.add(new Swatch(colorId, x, y));
        }

        int grayY = centerY + radius + 15;
        int spacing = 4;
        int totalLineWidth = (grays.size() * swatchSize) + ((grays.size() - 1) * spacing);
        int startX = centerX - (totalLineWidth / 2);

        for (int i = 0; i < grays.size(); i++) {
            int colorId = grays.get(i);
            int x = startX + (i * (swatchSize + spacing));
            layout.add(new Swatch(colorId, x, grayY));
        }
        return layout;
    }

    private void renderPalette(GuiGraphics guiGraphics, net.minecraft.world.item.ItemStack activeColorStack) {
        if ((this.activeTool != Tool.COLOR_PENCIL && this.activeTool != Tool.COLOR_MARKER) || activeColorStack.isEmpty()) return;

        java.util.List<Integer> colors = activeColorStack.getOrDefault(ModDataComponents.STORED_COLORS.get(), new java.util.ArrayList<>());
        int activeIndex = Math.abs(activeColorStack.getOrDefault(ModDataComponents.ACTIVE_COLOR_INDEX.get(), 0));
        int activeColorId = colors.isEmpty() ? -1 : colors.get(activeIndex % colors.size());

        int swatchSize = 12;

        for (Swatch swatch : getPaletteLayout()) {
            boolean hasColor = colors.contains(swatch.colorId());
            int outlineColor;

            if (colors.isEmpty()) {
                outlineColor = 0xFFFFFFFF;
            } else {
                outlineColor = (hasColor && swatch.colorId() == activeColorId) ? 0xFFFFFFFF : 0xFF444444;
            }

            guiGraphics.fill(swatch.x() - 1, swatch.y() - 1, swatch.x() + swatchSize + 1, swatch.y() + swatchSize + 1, outlineColor);

            if (hasColor) {
                int rgb = net.minecraft.world.item.DyeColor.byId(swatch.colorId()).getTextureDiffuseColor() | 0xFF000000;
                guiGraphics.fill(swatch.x(), swatch.y(), swatch.x() + swatchSize, swatch.y() + swatchSize, rgb);
            } else {
                guiGraphics.fill(swatch.x(), swatch.y(), swatch.x() + swatchSize, swatch.y() + swatchSize, 0xFF111111);
            }
        }
    }

    private void renderSizeIndicators(GuiGraphics guiGraphics, int mouseX, int mouseY, int toolX, int toolY) {
        int bottomY = toolY - 4;
        int[] sizes = (this.activeTool == Tool.SMUDGE) ? new int[]{2, 5, 12} : new int[]{3, 5, 7};
        int[] xOffsets = (this.activeTool == Tool.SMUDGE) ? new int[]{5, 11, 18} : new int[]{5, 11, 19};

        for (int i = 1; i <= 3; i++) {
            int size = sizes[ i - 1 ];
            int btnX = toolX + xOffsets[ i - 1 ];
            int btnY = bottomY - size;
            int color = (getBrushSize() == i) ? 0xFFFFFFFF : 0xFF555555;

            if (mouseX >= btnX - 2 && mouseX < btnX + size + 2 && mouseY >= btnY - 2 && mouseY < btnY + size + 2) {
                color = 0xFFFFFFAA;
            }
            guiGraphics.fill(btnX, btnY, btnX + size, btnY + size, color);
        }
    }

    private boolean handleSizeIndicatorClick(double mouseX, double mouseY, int toolX, int toolY) {
        int bottomY = toolY - 4;
        int[] sizes = (this.activeTool == Tool.SMUDGE) ? new int[]{2, 5, 12} : new int[]{3, 5, 7};
        int[] xOffsets = (this.activeTool == Tool.SMUDGE) ? new int[]{5, 11, 18} : new int[]{5, 11, 19};

        for (int i = 1; i <= 3; i++) {
            int size = sizes[ i - 1 ];
            int btnX = toolX + xOffsets[ i - 1 ];
            int btnY = bottomY - size;

            if (mouseX >= btnX - 2 && mouseX < btnX + size + 2 && mouseY >= btnY - 2 && mouseY < btnY + size + 2) {
                setToolSettings(i, getHardness(), getMarkerRotation());
                return true;
            }
        }
        return false;
    }

    private double[] applyRulerMagnet(double mX, double mY) {
        if (!this.isRulerActive || this.isQuickRulerMode) return new double[]{mX, mY};

        double dx = mX - this.rulerX;
        double dy = mY - this.rulerY;
        double rad = Math.toRadians(-this.rulerAngle);

        double localX = dx * Math.cos(rad) - dy * Math.sin(rad);
        double localY = dx * Math.sin(rad) + dy * Math.cos(rad);

        if (Math.abs(localY) <= 15 && Math.abs(localX) <= this.rulerWidth / 2.0) {
            localY = 0;
            double radBack = Math.toRadians(this.rulerAngle);
            double snappedX = this.rulerX + (localX * Math.cos(radBack) - localY * Math.sin(radBack));
            double snappedY = this.rulerY + (localX * Math.sin(radBack) + localY * Math.cos(radBack));
            return new double[]{snappedX, snappedY};
        }
        return new double[]{mX, mY};
    }

    private void drawPixel(double lMouseX, double lMouseY) {
        int canvasScreenLeft = (int) this.exactGuiLeft + ((this.frameWidth + this.deadZoneWidth) * this.scale);
        int canvasScreenTop = (int) this.exactGuiTop;
        double physicalCellSize = (double) this.scale / this.resolutionMultiplier;

        int centerX = (int) ((lMouseX - canvasScreenLeft) / physicalCellSize);
        int centerY = (int) ((lMouseY - canvasScreenTop) / physicalCellSize);

        int shiftX = 0, shiftY = 0;
        if (this.lastLogicalX != -1 && this.lastLogicalY != -1) {
            int lastCX = (int) ((this.lastLogicalX - canvasScreenLeft) / physicalCellSize);
            int lastCY = (int) ((this.lastLogicalY - canvasScreenTop) / physicalCellSize);
            shiftX = centerX - lastCX;
            shiftY = centerY - lastCY;
        }

        int currentBrushSize = getBrushSize();
        int currentToolHardness = getHardness();
        int actualSize = currentBrushSize;

        if (this.activeTool == Tool.SMUDGE || this.activeTool == Tool.KNEADED_ERASER) {
            actualSize = (currentBrushSize == 1) ? 2 : (currentBrushSize == 2) ? 5 : 12;
        }

        int offset = actualSize / 2;
        double radius = actualSize / 2.0;

        double exactCX = centerX + (actualSize % 2 == 0 ? -0.5 : 0.0);
        double exactCY = centerY + (actualSize % 2 == 0 ? -0.5 : 0.0);

        boolean isMarker = (this.activeTool == Tool.COLOR_MARKER);
        if (isMarker) {
            exactCX = centerX + 0.5;
            exactCY = centerY + 0.5;
        }

        int markerRot = isMarker ? getMarkerRotation() : 0;
        double angleRad = Math.toRadians(markerRot * 15.0);
        double mCos = Math.cos(angleRad);
        double mSin = Math.sin(angleRad);

        double thickness = currentBrushSize + 1.0;
        double length = currentBrushSize * 5.0;

        int bound = isMarker ? (int) Math.ceil(length / 2.0) + 1 : offset;

        int startX = isMarker ? (centerX - bound) : (centerX - offset);
        int endX = isMarker ? (centerX + bound) : (centerX - offset + actualSize - 1);
        int startY = isMarker ? (centerY - bound) : (centerY - offset);
        int endY = isMarker ? (centerY + bound) : (centerY - offset + actualSize - 1);

        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {

                double dx = x - exactCX;
                double dy = y - exactCY;
                double distance = Math.sqrt(dx * dx + dy * dy);

                if (isMarker) {
                    double localX = dx * mCos + dy * mSin;
                    double localY = -dx * mSin + dy * mCos;
                    if (Math.abs(localX) >= length / 2.0 || Math.abs(localY) >= thickness / 2.0) continue;
                } else {
                    if ((this.activeTool == Tool.SMUDGE || this.activeTool == Tool.KNEADED_ERASER) && distance > radius) continue;
                }

                if (x >= 0 && x < this.canvasWidth * this.resolutionMultiplier && y >= 0 && y < this.canvasHeight * this.resolutionMultiplier) {
                    if (this.activeTool == Tool.ERASER) {
                        if (pixels[x][y] != 0) {
                            pixels[x][y] = 0;
                            this.isCanvasDirty = true;
                            this.eraserPixelsUsed++;
                        }
                    } else {
                        if (this.strokePixels == null) this.strokePixels = new boolean[this.canvasWidth * this.resolutionMultiplier][this.canvasHeight * this.resolutionMultiplier];

                        if (this.strokePixels[x][y] && this.activeTool != Tool.SMUDGE && this.activeTool != Tool.KNEADED_ERASER) continue;

                        if (this.activeTool == Tool.KNEADED_ERASER) {
                            int currentColor = pixels[x][y];
                            if (currentColor != 0) {
                                float eraseStep = (currentToolHardness == 1) ? 5.0f : (currentToolHardness == 2) ? 15.0f : 30.0f;
                                float falloff = 1.0f;
                                float brushHardness = (currentToolHardness == 1) ? 0.4f : (currentToolHardness == 2) ? 0.7f : 0.9f;
                                double softRadius = radius * brushHardness;

                                if (distance > softRadius && radius > softRadius) {
                                    falloff = (float) (1.0 - (distance - softRadius) / (radius - softRadius));
                                }

                                int actualErase = (int)(eraseStep * falloff);
                                if (actualErase > 0) {
                                    int a = (currentColor >> 24) & 0xFF;
                                    a -= actualErase;
                                    if (a <= 5) pixels[x][y] = 0;
                                    else pixels[x][y] = (a << 24) | (currentColor & 0x00FFFFFF);
                                    this.isCanvasDirty = true;
                                    this.eraserPixelsUsed++;
                                    this.strokePixels[x][y] = true;
                                }
                            }
                        } else if (this.activeTool == Tool.SMUDGE) {
                            float shiftRate = (currentToolHardness == 1) ? 0.4f : (currentToolHardness == 2) ? 0.52f : 0.86f;
                            float mixRate = (currentToolHardness == 1) ? 0.65f : (currentToolHardness == 2) ? 0.41f : 0.17f;
                            float falloff = 1.0f;
                            float brushHardness = (currentToolHardness == 1) ? 0.4f : (currentToolHardness == 2) ? 0.59f : 0.89f;
                            double softRadius = radius * brushHardness;

                            if (distance > softRadius && radius > softRadius) falloff = (float) (1.0 - (distance - softRadius) / (radius - softRadius));

                            float finalShiftRate = shiftRate * falloff;
                            float sourceInfluence = (1.0f - mixRate) * falloff;

                            int sourceX = (int) Math.round(x - shiftX * finalShiftRate);
                            int sourceY = (int) Math.round(y - shiftY * finalShiftRate);

                            sourceX = Math.max(0, Math.min(this.canvasWidth * this.resolutionMultiplier - 1, sourceX));
                            sourceY = Math.max(0, Math.min(this.canvasHeight * this.resolutionMultiplier - 1, sourceY));

                            int sourceColor = pixels[sourceX][sourceY];
                            int destColor = pixels[x][y];
                            if (sourceColor == 0 && destColor == 0) continue;

                            int newColor = net.avizvul.esquissemod.util.ColorUtils.lerpColor(destColor, sourceColor, sourceInfluence);
                            if (pixels[x][y] != newColor) {
                                pixels[x][y] = newColor;
                                this.isCanvasDirty = true;
                                this.strokePixels[x][y] = true;
                            }
                        } else {
                            int brushRgb = 0x111111;
                            net.minecraft.world.item.ItemStack activeColorStack = (this.activeTool == Tool.COLOR_MARKER) ? getColorMarkerStack() : getColorPencilStack();

                            if ((this.activeTool == Tool.COLOR_PENCIL || this.activeTool == Tool.COLOR_MARKER) && !activeColorStack.isEmpty()) {
                                java.util.List<Integer> colors = activeColorStack.getOrDefault(net.avizvul.esquissemod.component.ModDataComponents.STORED_COLORS.get(), new java.util.ArrayList<>());
                                if (!colors.isEmpty()) {
                                    int activeIndex = activeColorStack.getOrDefault(net.avizvul.esquissemod.component.ModDataComponents.ACTIVE_COLOR_INDEX.get(), 0);
                                    brushRgb = net.minecraft.world.item.DyeColor.byId(colors.get(Math.abs(activeIndex) % colors.size())).getTextureDiffuseColor();
                                }
                            }

                            int alpha;
                            if (this.activeTool == Tool.COLOR_MARKER) {
                                alpha = (currentToolHardness == 1) ? 76 : ((currentToolHardness == 2) ? 153 : 255);
                            } else {
                                alpha = (currentToolHardness == 1) ? 64 : ((currentToolHardness == 2) ? 128 : 255);
                            }

                            int newColorArgb = (alpha << 24) | (brushRgb & 0xFFFFFF);
                            int blendedColor = net.avizvul.esquissemod.util.ColorUtils.blendColors(pixels[x][y], newColorArgb);

                            if (pixels[x][y] != blendedColor) {
                                this.isCanvasDirty = true;
                                pixels[x][y] = blendedColor;
                                this.pencilPixelsUsed++;
                                this.strokePixels[x][y] = true;
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean hasPencil = hasTool(ModItems.PENCIL.get());
        boolean hasEraser = hasTool(ModItems.ERASER.get());
        boolean hasSmudge = hasTool(ModItems.SMUDGE.get());
        boolean hasKneaded = hasTool(ModItems.KNEADED_ERASER.get());
        boolean hasCompass = hasTool(ModItems.DRAWING_COMPASS.get());
        boolean hasColorMarker = hasTool(ModItems.COLOR_MARKER.get());

        ItemStack colorPencilStack = getColorPencilStack();
        boolean hasColorPencil = !colorPencilStack.isEmpty();
        boolean hasRuler = hasTool(net.avizvul.esquissemod.item.ModItems.RULER.get());
        boolean hasMagGlass = hasTool(ModItems.MAGNIFYING_GLASS.get());

        boolean hasColors = (hasColorPencil && !colorPencilStack.getOrDefault(ModDataComponents.STORED_COLORS.get(), new java.util.ArrayList<>()).isEmpty()) ||
                (hasColorMarker && !getColorMarkerStack().getOrDefault(ModDataComponents.STORED_COLORS.get(), new java.util.ArrayList<>()).isEmpty());

        ToolButtonCoords toolCoords = getToolButtonCoords();
        int scaledBtnWidth = toolCoords.scaledBtnWidth();
        int scaledBtnHeight = toolCoords.scaledBtnHeight();

        int pencilX = toolCoords.pencilX();
        int colorPencilX = toolCoords.colorPencilX();
        int colorMarkerX = toolCoords.colorMarkerX();
        int eraserX = toolCoords.eraserX();
        int smudgeX = toolCoords.smudgeX();
        int kneadedX = toolCoords.kneadedX();
        int rulerX = toolCoords.rulerX();
        int magGlassX = toolCoords.magGlassX();
        int compassX = toolCoords.compassX();
        int peekY = toolCoords.peekY();

        if (button == 1 && this.isMagnifierLocked) {
            this.isMagnifierLocked = false;
            return true;
        }

        if (this.isRulerActive && !this.isQuickRulerMode) {
            double dx = mouseX - this.rulerX;
            double dy = mouseY - this.rulerY;
            double rad = Math.toRadians(-this.rulerAngle);

            double localX = dx * Math.cos(rad) - dy * Math.sin(rad);
            double localY = dx * Math.sin(rad) + dy * Math.cos(rad);

            if (Math.abs(localX) <= this.rulerWidth / 2.0 && localY >= 0 && localY <= this.rulerHeight) {
                if (button == 0) {
                    if (net.minecraft.client.gui.screens.Screen.hasShiftDown()) {
                        this.isRulerRotating = true;
                        double startAngle = Math.toDegrees(Math.atan2(mouseY - this.rulerY, mouseX - this.rulerX));
                        this.rulerAngleOffset = this.rulerAngle - startAngle;
                    } else {
                        this.isRulerDragging = true;
                    }
                    return true;
                } else if (button == 1) {
                    this.isRulerActive = false;
                    return true;
                }
            }
        }

        if (button == 0) {
            int baseY = this.height - (scaledBtnHeight / 2);
            int pencilY = (this.activeTool == Tool.PENCIL) ? peekY : baseY;
            int colorPencilY = (this.activeTool == Tool.COLOR_PENCIL) ? peekY : baseY;
            int colorMarkerY = (this.activeTool == Tool.COLOR_MARKER) ? peekY : baseY;
            int eraserY = (this.activeTool == Tool.ERASER) ? peekY : baseY;
            int smudgeY = (this.activeTool == Tool.SMUDGE) ? peekY : baseY;
            int kneadedY = (this.activeTool == Tool.KNEADED_ERASER) ? peekY : baseY;

            if (hasPencil && mouseX >= pencilX && mouseX < pencilX + scaledBtnWidth && mouseY >= pencilY && mouseY < pencilY + scaledBtnHeight) {
                this.activeTool = Tool.PENCIL; return true;
            }
            if (hasColorPencil && mouseX >= colorPencilX && mouseX < colorPencilX + scaledBtnWidth && mouseY >= colorPencilY && mouseY < colorPencilY + scaledBtnHeight) {
                this.activeTool = Tool.COLOR_PENCIL; return true;
            }
            if (hasColorMarker && mouseX >= colorMarkerX && mouseX < colorMarkerX + scaledBtnWidth && mouseY >= colorMarkerY && mouseY < colorMarkerY + scaledBtnHeight) {
                this.activeTool = Tool.COLOR_MARKER; return true;
            }
            if (hasEraser && mouseX >= eraserX && mouseX < eraserX + scaledBtnWidth && mouseY >= eraserY && mouseY < eraserY + scaledBtnHeight) {
                this.activeTool = Tool.ERASER; return true;
            }
            if (hasSmudge && mouseX >= smudgeX && mouseX < smudgeX + scaledBtnWidth && mouseY >= smudgeY && mouseY < smudgeY + scaledBtnHeight) {
                this.activeTool = Tool.SMUDGE; return true;
            }
            if (hasKneaded && mouseX >= kneadedX && mouseX < kneadedX + scaledBtnWidth && mouseY >= kneadedY && mouseY < kneadedY + scaledBtnHeight) {
                this.activeTool = Tool.KNEADED_ERASER; return true;
            }

            if (hasRuler && !this.isRulerActive && mouseX >= rulerX && mouseX < rulerX + scaledBtnWidth && mouseY >= baseY && mouseY < baseY + scaledBtnHeight) {
                this.isRulerActive = true; return true;
            }
            if (hasMagGlass && !this.isMagnifierLocked && mouseX >= magGlassX && mouseX < magGlassX + scaledBtnWidth && mouseY >= baseY && mouseY < baseY + scaledBtnHeight) {
                this.isMagnifierLocked = true; return true;
            }

            if (hasCompass && this.compassState == CompassState.INACTIVE && mouseX >= compassX && mouseX < compassX + scaledBtnWidth && mouseY >= baseY && mouseY < baseY + scaledBtnHeight) {
                this.compassState = CompassState.FOLDED;
                return true;
            }

            if (this.activeTool == Tool.PENCIL && hasPencil) {
                if (handleSizeIndicatorClick(mouseX, mouseY, pencilX, peekY)) return true;
            } else if ((this.activeTool == Tool.COLOR_PENCIL && hasColorPencil) || (this.activeTool == Tool.COLOR_MARKER && hasColorMarker)) {
                int activeX = (this.activeTool == Tool.COLOR_MARKER) ? colorMarkerX : colorPencilX;
                ItemStack activeStack = (this.activeTool == Tool.COLOR_MARKER) ? getColorMarkerStack() : colorPencilStack;
                boolean toolHasColors = !activeStack.getOrDefault(ModDataComponents.STORED_COLORS.get(), new java.util.ArrayList<>()).isEmpty();

                if (toolHasColors && handleSizeIndicatorClick(mouseX, mouseY, activeX, peekY)) return true;

                int swatchSize = 12;
                java.util.List<Integer> colors = activeStack.getOrDefault(ModDataComponents.STORED_COLORS.get(), new java.util.ArrayList<>());

                for (Swatch swatch : getPaletteLayout()) {
                    if (mouseX >= swatch.x() && mouseX <= swatch.x() + swatchSize && mouseY >= swatch.y() && mouseY <= swatch.y() + swatchSize) {
                        int foundIndex = colors.indexOf(swatch.colorId());
                        if (foundIndex != -1) {
                            activeStack.set(ModDataComponents.ACTIVE_COLOR_INDEX.get(), foundIndex);
                            boolean isMarker = (this.activeTool == Tool.COLOR_MARKER);
                            net.neoforged.neoforge.network.PacketDistributor.sendToServer(new net.avizvul.esquissemod.network.ChangeColorPayload(foundIndex, isMarker));
                            return true;
                        }
                    }
                }
            } else if (this.activeTool == Tool.ERASER && hasEraser) {
                if (handleSizeIndicatorClick(mouseX, mouseY, eraserX, peekY)) return true;
            } else if (this.activeTool == Tool.SMUDGE && hasSmudge) {
                if (handleSizeIndicatorClick(mouseX, mouseY, smudgeX, peekY)) return true;
            } else if (this.activeTool == Tool.KNEADED_ERASER && hasKneaded) {
                if (handleSizeIndicatorClick(mouseX, mouseY, kneadedX, peekY)) return true;
            }

            double[] logicalMouse = getLogicalMouse(mouseX, mouseY);
            double lMouseX = logicalMouse[ 0 ];
            double lMouseY = logicalMouse[ 1 ];

            int renderX = (int) this.exactGuiLeft;
            int renderY = (int) this.exactGuiTop;

            int btnFileWidth = 8;
            int btnFileHeight = 8;
            int btnX = renderX;
            int btnY = renderY + ((this.fileHeight - btnFileHeight) / 2) * this.scale;

            if (lMouseX >= btnX && lMouseX < btnX + (btnFileWidth * this.scale) && lMouseY >= btnY && lMouseY < btnY + (btnFileHeight * this.scale)) {
                this.isRotating = true; return true;
            }

            int drawWidth = this.fileWidth * this.scale;
            TabCoords coords = getTabCoords(renderX, renderY, drawWidth);

            net.minecraft.world.item.ItemStack stack = this.minecraft.player.getMainHandItem();
            if (!stack.is(ModItems.SKETCHBOOK.get())) stack = this.minecraft.player.getOffhandItem();

            java.util.List<net.avizvul.esquissemod.component.SketchData> pages = new java.util.ArrayList<>(stack.getOrDefault(ModDataComponents.SKETCHBOOK_PAGES.get(), new java.util.ArrayList<>()));

            int scaledTabWidth = this.tabWidth * this.tabScale;
            int scaledTabHeight = this.tabHeight * this.tabScale;

            if (this.currentPageIndex > 0 && lMouseX >= coords.tabX() && lMouseX < coords.tabX() + scaledTabWidth && lMouseY >= coords.backTabY() && lMouseY < coords.backTabY() + scaledTabHeight) {
                turnPage(this.currentPageIndex - 1); return true;
            }
            if (this.currentPageIndex < pages.size() - 1 && lMouseX >= coords.tabX() && lMouseX < coords.tabX() + scaledTabWidth && lMouseY >= coords.forwardTabY() && lMouseY < coords.forwardTabY() + scaledTabHeight) {
                turnPage(this.currentPageIndex + 1); return true;
            }

            int scaledFrameWidth = this.frameWidth * this.scale;
            int scaledImageHeight = this.fileHeight * this.scale;

            if (lMouseX >= renderX && lMouseX < (renderX + scaledFrameWidth) && lMouseY >= renderY && lMouseY < (renderY + scaledImageHeight)) {
                this.isDragging = true; return true;
            } else {
                int canvasScreenLeft = renderX + ((this.frameWidth + this.deadZoneWidth) * this.scale);
                int scaledCanvasWidth = this.canvasWidth * this.scale;

                if (lMouseX >= canvasScreenLeft && lMouseX < (canvasScreenLeft + scaledCanvasWidth) && lMouseY >= renderY && lMouseY < (renderY + scaledImageHeight)) {

                    if (this.compassState == CompassState.FOLDED) {
                        this.compassAnchorX = lMouseX;
                        this.compassAnchorY = lMouseY;
                        this.compassState = CompassState.ANCHORED;
                        return true;
                    } else if (this.compassState == CompassState.ANCHORED) {
                        double dx = lMouseX - this.compassAnchorX;
                        double dy = lMouseY - this.compassAnchorY;
                        double dist = Math.sqrt(dx*dx + dy*dy);
                        if (dist > 192.0) dist = 192.0;
                        this.compassRadius = dist;
                        this.compassState = CompassState.LOCKED;
                    }

                    double[] magnetMouse = applyRulerMagnet(mouseX, mouseY);
                    double[] drawLogical = getLogicalMouse(magnetMouse[ 0 ], magnetMouse[ 1 ]);

                    if (this.compassState == CompassState.LOCKED) {
                        double angle = Math.atan2(drawLogical[ 1 ] - this.compassAnchorY, drawLogical[ 0 ] - this.compassAnchorX);
                        drawLogical[ 0 ] = this.compassAnchorX + this.compassRadius * Math.cos(angle);
                        drawLogical[ 1 ] = this.compassAnchorY + this.compassRadius * Math.sin(angle);
                    }

                    if ((this.activeTool == Tool.PENCIL && hasPencil) ||
                            (this.activeTool == Tool.COLOR_PENCIL && hasColorPencil && hasColors) ||
                            (this.activeTool == Tool.COLOR_MARKER && hasColorMarker && hasColors) ||
                            (this.activeTool == Tool.SMUDGE && hasSmudge)) {
                        this.isDrawing = true;
                        this.lastLogicalX = drawLogical[ 0 ];
                        this.lastLogicalY = drawLogical[ 1 ];
                        drawPixel(drawLogical[ 0 ], drawLogical[ 1 ]);
                    } else if ((this.activeTool == Tool.ERASER && hasEraser) || (this.activeTool == Tool.KNEADED_ERASER && hasKneaded)) {
                        this.isErasing = true;
                        this.lastLogicalX = drawLogical[ 0 ];
                        this.lastLogicalY = drawLogical[ 1 ];
                        drawPixel(drawLogical[ 0 ], drawLogical[ 1 ]);
                    }
                    return true;
                }
            }
        }

        if (button == 1) {
            double[] logicalMouse = getLogicalMouse(mouseX, mouseY);
            double lMouseX = logicalMouse[ 0 ];
            double lMouseY = logicalMouse[ 1 ];

            int renderX = (int) this.exactGuiLeft;
            int renderY = (int) this.exactGuiTop;

            int btnFileWidth = 8;
            int btnFileHeight = 8;
            int btnX = renderX;
            int btnY = renderY + ((this.fileHeight - btnFileHeight) / 2) * this.scale;

            if (lMouseX >= btnX && lMouseX < btnX + (btnFileWidth * this.scale) && lMouseY >= btnY && lMouseY < btnY + (btnFileHeight * this.scale)) {
                this.rotationAngle = 0.0f;
                clampSketchbook();
                return true;
            }

            int blueZoneWidth = this.deadZoneWidth * this.scale;
            int blueZoneLeft = renderX + (this.frameWidth * this.scale);
            int blueZoneTop = renderY;
            int blueZoneBottom = renderY + (this.canvasHeight * this.scale);

            if (lMouseX >= blueZoneLeft && lMouseX <= blueZoneLeft + blueZoneWidth && lMouseY >= blueZoneTop && lMouseY <= blueZoneBottom) {
                net.neoforged.neoforge.network.PacketDistributor.sendToServer(new SketchbookSavePayload(this.currentPageIndex, net.avizvul.esquissemod.component.SketchData.fromArray(this.pixels), this.pencilPixelsUsed, this.eraserPixelsUsed));
                net.neoforged.neoforge.network.PacketDistributor.sendToServer(new TearPagePayload(this.currentPageIndex));

                net.minecraft.world.item.ItemStack stack = this.minecraft.player.getMainHandItem();
                if (!stack.is(ModItems.SKETCHBOOK.get())) stack = this.minecraft.player.getOffhandItem();

                java.util.List<net.avizvul.esquissemod.component.SketchData> pages = new java.util.ArrayList<>(stack.getOrDefault(ModDataComponents.SKETCHBOOK_PAGES.get(), new java.util.ArrayList<>()));
                pages.remove(this.currentPageIndex);

                if (pages.isEmpty()) {
                    this.onClose();
                } else {
                    if (this.currentPageIndex >= pages.size()) this.currentPageIndex = pages.size() - 1;
                    stack.set(ModDataComponents.SKETCHBOOK_PAGES.get(), pages);
                    int w = this.canvasWidth * this.resolutionMultiplier;
                    int h = this.canvasHeight * this.resolutionMultiplier;
                    this.pixels = pages.get(this.currentPageIndex).toArray(w, h);
                    this.isCanvasDirty = true;
                }
                return true;
            }

            boolean clickedPencil = hasPencil && mouseX >= pencilX && mouseX < pencilX + scaledBtnWidth && mouseY >= peekY && mouseY < peekY + scaledBtnHeight;
            boolean clickedColorPencil = hasColorPencil && hasColors && mouseX >= colorPencilX && mouseX < colorPencilX + scaledBtnWidth && mouseY >= peekY && mouseY < peekY + scaledBtnHeight;
            boolean clickedColorMarker = hasColorMarker && hasColors && mouseX >= colorMarkerX && mouseX < colorMarkerX + scaledBtnWidth && mouseY >= peekY && mouseY < peekY + scaledBtnHeight;
            boolean clickedSmudge = hasSmudge && mouseX >= smudgeX && mouseX < smudgeX + scaledBtnWidth && mouseY >= peekY && mouseY < peekY + scaledBtnHeight;
            boolean clickedKneaded = hasKneaded && mouseX >= kneadedX && mouseX < kneadedX + scaledBtnWidth && mouseY >= peekY && mouseY < peekY + scaledBtnHeight;

            if (clickedPencil || clickedColorPencil || clickedColorMarker || clickedSmudge || clickedKneaded) {
                if (clickedPencil) this.activeTool = Tool.PENCIL;
                else if (clickedColorPencil) this.activeTool = Tool.COLOR_PENCIL;
                else if (clickedColorMarker) this.activeTool = Tool.COLOR_MARKER;
                else if (clickedSmudge) this.activeTool = Tool.SMUDGE;
                else if (clickedKneaded) this.activeTool = Tool.KNEADED_ERASER;

                int h = getHardness() + 1;
                if (h > 3) h = 1;
                setToolSettings(getBrushSize(), h, getMarkerRotation());
                return true;
            }

            boolean clickedRuler = hasRuler && mouseX >= rulerX && mouseX < rulerX + scaledBtnWidth && mouseY >= peekY && mouseY < peekY + scaledBtnHeight;
            if (clickedRuler && this.isRulerActive) {
                this.isRulerActive = false;
                return true;
            }

            if (this.compassState != CompassState.INACTIVE) {
                if (this.compassState == CompassState.LOCKED) {
                    this.compassState = CompassState.ANCHORED;
                    return true;
                } else if (this.compassState == CompassState.ANCHORED) {
                    this.compassState = CompassState.FOLDED;
                    return true;
                } else if (this.compassState == CompassState.FOLDED) {
                    this.compassState = CompassState.INACTIVE;
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.isRulerDragging) {
            this.rulerX += dragX;
            this.rulerY += dragY;
            return true;
        } else if (this.isRulerRotating) {
            double angleRad = Math.atan2(mouseY - this.rulerY, mouseX - this.rulerX);
            this.rulerAngle = (float) (Math.toDegrees(angleRad) + this.rulerAngleOffset);
            return true;
        } else if (this.isDrawing || this.isErasing) {
            double[] magnetMouse = applyRulerMagnet(mouseX, mouseY);
            double[] lMouse = getLogicalMouse(magnetMouse[ 0 ], magnetMouse[ 1 ]);

            if (this.compassState == CompassState.LOCKED) {
                double angle = Math.atan2(lMouse[ 1 ] - this.compassAnchorY, lMouse[ 0 ] - this.compassAnchorX);
                lMouse[ 0 ] = this.compassAnchorX + this.compassRadius * Math.cos(angle);
                lMouse[ 1 ] = this.compassAnchorY + this.compassRadius * Math.sin(angle);
            }

            drawPixel(lMouse[ 0 ], lMouse[ 1 ]);
            this.lastLogicalX = lMouse[ 0 ];
            this.lastLogicalY = lMouse[ 1 ];
            return true;
        } else if (this.isRotating) {
            double cx = this.exactGuiLeft + (this.fileWidth * this.scale) / 2.0;
            double cy = this.exactGuiTop + (this.fileHeight * this.scale) / 2.0;
            double angleRad = Math.atan2(mouseY - cy, mouseX - cx);
            this.rotationAngle = (float) Math.toDegrees(angleRad) - 180f;
            clampSketchbook();
            return true;
        } else if (this.isDragging) {
            this.exactGuiLeft += dragX;
            this.exactGuiTop += dragY;
            clampSketchbook();
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (this.strokePixels != null) {
                this.strokePixels = new boolean[this.canvasWidth * this.resolutionMultiplier][this.canvasHeight * this.resolutionMultiplier];
            }
            this.lastLogicalX = -1;
            this.lastLogicalY = -1;
            if (this.isRulerDragging) { this.isRulerDragging = false; return true; }
            if (this.isRulerRotating) { this.isRulerRotating = false; return true; }
            if (this.isRotating) { this.isRotating = false; return true; }
            if (this.isDragging) { this.isDragging = false; return true; }
            if (this.isDrawing) { this.isDrawing = false; return true; }
            if (this.isErasing) { this.isErasing = false; return true; }
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        ItemStack colorPencilStack = getColorPencilStack();
        boolean hasColorPencil = !colorPencilStack.isEmpty();
        boolean hasColors = hasColorPencil && !colorPencilStack.getOrDefault(ModDataComponents.STORED_COLORS.get(), new java.util.ArrayList<>()).isEmpty();

        boolean hasMarkerColors = hasTool(ModItems.COLOR_MARKER.get()) && !getColorMarkerStack().getOrDefault(ModDataComponents.STORED_COLORS.get(), new java.util.ArrayList<>()).isEmpty();

        if (net.minecraft.client.gui.screens.Screen.hasAltDown()) {
            if (this.activeTool == Tool.COLOR_MARKER && hasTool(ModItems.COLOR_MARKER.get())) {
                int r = getMarkerRotation();
                if (scrollY > 0) r = (r + 1) % 12;
                else if (scrollY < 0) r = (r - 1 + 12) % 12;
                setToolSettings(getBrushSize(), getHardness(), r);
                return true;
            }
        }
        else if (net.minecraft.client.gui.screens.Screen.hasShiftDown()) {
            if ((this.activeTool == Tool.PENCIL && hasTool(ModItems.PENCIL.get())) ||
                    (this.activeTool == Tool.COLOR_PENCIL && hasColors) ||
                    (this.activeTool == Tool.SMUDGE && hasTool(ModItems.SMUDGE.get())) ||
                    (this.activeTool == Tool.KNEADED_ERASER && hasTool(ModItems.KNEADED_ERASER.get())) ||
                    (this.activeTool == Tool.COLOR_MARKER && hasMarkerColors)) {

                int h = getHardness();
                if (scrollY > 0) h = Math.min(3, h + 1);
                else if (scrollY < 0) h = Math.max(1, h - 1);
                setToolSettings(getBrushSize(), h, getMarkerRotation());
                return true;
            }
        }
        else {
            int s = getBrushSize();
            if (scrollY > 0) s = Math.min(3, s + 1);
            else if (scrollY < 0) s = Math.max(1, s - 1);
            setToolSettings(s, getHardness(), getMarkerRotation());
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean hasPencil = hasTool(ModItems.PENCIL.get());
        boolean hasEraser = hasTool(ModItems.ERASER.get());
        boolean hasSmudge = hasTool(ModItems.SMUDGE.get());
        boolean hasKneadedEraser = hasTool(ModItems.KNEADED_ERASER.get());
        boolean hasColorPencil = !getColorPencilStack().isEmpty();
        boolean hasColorMarker = hasTool(ModItems.COLOR_MARKER.get());

        if (keyCode == GLFW.GLFW_KEY_B && hasPencil) {
            this.activeTool = Tool.PENCIL;
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_C && hasColorPencil) {
            this.activeTool = Tool.COLOR_PENCIL;
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_E && hasEraser) {
            this.activeTool = Tool.ERASER;
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_R) {
            if (hasTool(net.avizvul.esquissemod.item.ModItems.RULER.get())) {
                if (net.minecraft.client.gui.screens.Screen.hasShiftDown()) {
                    if (!this.isQuickRulerMode) {
                        this.isRulerActive = true;
                        this.isQuickRulerMode = true;
                        this.quickRulerStartX = this.lastMouseX;
                        this.quickRulerStartY = this.lastMouseY;
                    }
                } else {
                    this.isRulerActive = !this.isRulerActive;
                }
            }
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_D) {
            boolean hasCompass = hasTool(ModItems.DRAWING_COMPASS.get());
            if (hasCompass) {
                if (net.minecraft.client.gui.screens.Screen.hasShiftDown()) {
                    if (!this.isQuickCompassMode) {
                        this.isQuickCompassMode = true;
                        this.compassState = CompassState.ANCHORED;
                        double[] logicalMouse = getLogicalMouse(this.lastMouseX, this.lastMouseY);
                        this.compassAnchorX = logicalMouse[ 0 ];
                        this.compassAnchorY = logicalMouse[ 1 ];
                    }
                } else {
                    if (this.compassState == CompassState.INACTIVE) {
                        this.compassState = CompassState.FOLDED;
                    } else {
                        this.compassState = CompassState.INACTIVE;
                    }
                }
            }
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_Z) {
            if (hasTool(ModItems.MAGNIFYING_GLASS.get())) {
                this.isMagnifyingMode = true;
                return true;
            }
        }
        else if (keyCode == GLFW.GLFW_KEY_S && hasSmudge) {
            this.activeTool = Tool.SMUDGE;
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_W && hasKneadedEraser) {
            this.activeTool = Tool.KNEADED_ERASER;
            return true;
        }
        else if (keyCode == GLFW.GLFW_KEY_V && hasColorMarker) {
            this.activeTool = Tool.COLOR_MARKER;
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_R && this.isQuickRulerMode) {
            this.isQuickRulerMode = false;
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_D && this.isQuickCompassMode) {
            this.isQuickCompassMode = false;
            this.compassState = CompassState.INACTIVE;
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_Z) {
            this.isMagnifyingMode = false;
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        savedGuiLeft = this.exactGuiLeft;
        savedGuiTop = this.exactGuiTop;
        savedRotationAngle = this.rotationAngle;
        hasSavedState = true;

        SketchData data = SketchData.fromArray(this.pixels);

        if (this.minecraft != null && this.minecraft.player != null) {
            net.minecraft.world.item.ItemStack stack = this.minecraft.player.getMainHandItem();
            if (!stack.is(net.avizvul.esquissemod.item.ModItems.SKETCHBOOK.get())) {
                stack = this.minecraft.player.getOffhandItem();
            }
            if (stack.is(net.avizvul.esquissemod.item.ModItems.SKETCHBOOK.get())) {
                java.util.List<net.avizvul.esquissemod.component.SketchData> pages =
                        new java.util.ArrayList<>(stack.getOrDefault(ModDataComponents.SKETCHBOOK_PAGES.get(), new java.util.ArrayList<>()));
                if (this.currentPageIndex >= 0 && this.currentPageIndex < pages.size()) {
                    pages.set(this.currentPageIndex, data);
                    stack.set(ModDataComponents.SKETCHBOOK_PAGES.get(), pages);
                    stack.set(ModDataComponents.LAST_PAGE.get(), this.currentPageIndex);
                }
            }
        }

        net.neoforged.neoforge.network.PacketDistributor.sendToServer(new SketchbookSavePayload(this.currentPageIndex, data, this.pencilPixelsUsed, this.eraserPixelsUsed));

        if (this.activeCanvasId != null) {
            net.minecraft.client.Minecraft.getInstance().getTextureManager().release(this.activeCanvasId);
            this.activeCanvasTexture.close();
        }

        savedRulerX = this.rulerX;
        savedRulerY = this.rulerY;
        savedRulerAngle = this.rulerAngle;
        wasRulerActive = this.isRulerActive;
        savedCompassState = this.compassState;
        savedCompassAnchorX = this.compassAnchorX;
        savedCompassAnchorY = this.compassAnchorY;
        savedCompassRadius = this.compassRadius;

        super.onClose();
    }

    private void updateActiveCanvasTexture() {
        if (this.activeCanvasTexture == null) {
            com.mojang.blaze3d.platform.NativeImage image = new com.mojang.blaze3d.platform.NativeImage(126, 192, true);
            this.activeCanvasTexture = new net.minecraft.client.renderer.texture.DynamicTexture(image);
            this.activeCanvasId = net.minecraft.client.Minecraft.getInstance().getTextureManager().register("active_canvas", this.activeCanvasTexture);
        }

        com.mojang.blaze3d.platform.NativeImage image = this.activeCanvasTexture.getPixels();
        if (image != null) {
            for (int x = 0; x < 126; x++) {
                for (int y = 0; y < 192; y++) {
                    int argb = this.pixels[x][y];
                    if (argb != 0) {
                        int a = (argb >> 24) & 0xFF;
                        int r = (argb >> 16) & 0xFF;
                        int g = (argb >> 8) & 0xFF;
                        int b = argb & 0xFF;
                        image.setPixelRGBA(x, y, (a << 24) | (b << 16) | (g << 8) | r);
                    } else {
                        image.setPixelRGBA(x, y, 0);
                    }
                }
            }
            this.activeCanvasTexture.upload();
        }
        this.isCanvasDirty = false;
    }
}