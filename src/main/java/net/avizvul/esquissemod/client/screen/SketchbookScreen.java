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
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;
import com.mojang.math.Axis;

import java.util.ArrayList;
import java.util.List;

// ВНИМАНИЕ: Замените на ВАШ импорт предметов
// import net.avizvul.esquissemod.item.ModItems;

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

    // НОВОЕ: Переменные подсчета потраченных пикселей в этой сессии
    private int pencilPixelsUsed = 0;
    private int eraserPixelsUsed = 0;

    private final int scale = 3;
    private final int resolutionMultiplier = 2;

    // 1 = 2H (Светлый), 2 = HB (Средний), 3 = 4B (Черный)
    private int[][] pixels = new int[canvasWidth * resolutionMultiplier][canvasHeight * resolutionMultiplier];
    // Массив для запоминания пикселей текущего штриха
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

    // --- СТАТИЧЕСКИЕ ПЕРЕМЕННЫЕ ДЛЯ СОХРАНЕНИЯ СОСТОЯНИЯ ---
    private static double savedGuiLeft = -1;
    private static double savedGuiTop = -1;
    private static float savedRotationAngle = 0.0f;
    private static boolean hasSavedState = false;

    // Текстура линейки
    private static final ResourceLocation RULER_TEX = ResourceLocation.fromNamespaceAndPath(EsquisseMod.MOD_ID, "textures/gui/ruler.png");

    // Размеры линейки (увеличили в 2 раза)
    private final int rulerWidth = 398;
    private final int rulerHeight = 40;

    // Переменные текущего состояния
    private boolean isRulerActive = false;
    private double rulerX, rulerY; // Якорная точка (теперь это ЦЕНТР РАБОЧЕЙ ГРАНИ)
    private float rulerAngle = 0.0f;
    private boolean isRulerDragging = false;
    private boolean isRulerRotating = false;

    // НОВОЕ: Переменные для расширенного функционала
    private double rulerAngleOffset = 0.0; // Для плавного вращения без рывков
    private boolean isQuickRulerMode = false;
    private double quickRulerStartX, quickRulerStartY;
    private double lastMouseX, lastMouseY;

    // Переменные для растушевки
    private double lastLogicalX = -1;
    private double lastLogicalY = -1;

    // Статические переменные для сохранения позиции
    private static double savedRulerX = -1;
    private static double savedRulerY = -1;
    private static float savedRulerAngle = 0.0f;
    private static boolean wasRulerActive = false;

    private boolean isMagnifyingMode = false; // Для зажатия Z
    private boolean isMagnifierLocked = false; // Для клика по кнопке

    private enum CompassState { INACTIVE, FOLDED, ANCHORED, LOCKED }
    private CompassState compassState = CompassState.INACTIVE;
    private double compassAnchorX, compassAnchorY, compassRadius;
    private boolean isQuickCompassMode = false;

    private static CompassState savedCompassState = CompassState.INACTIVE;
    private static double savedCompassAnchorX = -1;
    private static double savedCompassAnchorY = -1;
    private static double savedCompassRadius = -1;

    // --- ПЕРЕМЕННЫЕ ДЛЯ СТРАНИЦ ---
    private List<SketchData> pages = new ArrayList<>();
    private int currentPageIndex = 0;
    private static int savedPageIndex = 0; // Чтобы запоминать страницу при закрытии
    private net.minecraft.client.renderer.texture.DynamicTexture activeCanvasTexture;
    private net.minecraft.resources.ResourceLocation activeCanvasId;
    private boolean isCanvasDirty = true;

    // --- Маркер ---
    private net.minecraft.world.item.ItemStack getColorMarkerStack() {
        return findItemStack(ModItems.COLOR_MARKER.get());
    }

    private int getMarkerRotation() {
        net.minecraft.world.item.ItemStack stack = getActiveToolStack();
        return stack.isEmpty() ? 0 : stack.getOrDefault(ModDataComponents.MARKER_ROTATION.get(), 0);
    }

    private void setToolSettings(int size, int hardness, int rotation) { // ОБНОВИТЕ СИГНАТУРУ
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

        // --- ЗАГРУЗКА ПОСЛЕДНЕЙ СТРАНИЦЫ ИЗ КОМПОНЕНТА ПРЕДМЕТА ---
        if (!this.isPageLoaded) {
            net.minecraft.world.item.ItemStack stack = this.minecraft.player.getMainHandItem();
            if (!stack.is(ModItems.SKETCHBOOK.get())) {
                stack = this.minecraft.player.getOffhandItem();
            }

            // Достаем сохраненную страницу. Если её еще нет, вернется 0
            // Используем вашу переменную currentPageIndex!
            this.currentPageIndex = stack.getOrDefault(ModDataComponents.LAST_PAGE.get(), 0);

            this.isPageLoaded = true;
        }
        // ----------------------------------------------------------

        // Если интерфейс уже открывали ранее - восстанавливаем сохранённые значения
        if (hasSavedState) {
            this.exactGuiLeft = savedGuiLeft;
            this.exactGuiTop = savedGuiTop;
            this.rotationAngle = savedRotationAngle;
        } else {
            // Иначе центрируем по умолчанию
            int scaledWidth = this.fileWidth * this.scale;
            int scaledHeight = this.fileHeight * this.scale;

            this.exactGuiLeft = (this.width - scaledWidth) / 2.0;
            this.exactGuiTop = (this.height - scaledHeight) / 2.0;
        }

        clampSketchbook(); // Обязательно вызываем на случай, если игрок изменил размер окна игры

        if (this.minecraft != null && this.minecraft.player != null) {
            ItemStack stack = this.minecraft.player.getMainHandItem();

            // 1. Проверяем, есть ли уже компонент со страницами в предмете
            if (stack.has(ModDataComponents.SKETCHBOOK_PAGES.get())) {
                this.pages = new ArrayList<>(stack.get(ModDataComponents.SKETCHBOOK_PAGES.get()));
            } else {
                // Если нет (скетчбук новый), создаем 16 пустых страниц
                this.pages = new ArrayList<>();

                SketchData emptyData = SketchData.fromArray(new int[this.canvasWidth * this.resolutionMultiplier][this.canvasHeight * this.resolutionMultiplier]);

                for (int i = 0; i < 16; i++) {
                    this.pages.add(emptyData);
                }
            }

            // --- ЗАГРУЗКА ЛИНЕЙКИ ---
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
                // Если открываем впервые, спавним линейку по центру экрана
                this.rulerX = this.width / 2.0;
                this.rulerY = this.height / 2.0;
            }

            // 2. Вызываем помощник для загрузки пикселей текущей страницы на холст
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

    // Компактная структура для хранения вычисленных координат
    private record TabCoords(int tabX, int backTabY, int forwardTabY) {}
    // Метод, который мы будем вызывать для расчетов
    private TabCoords getTabCoords(int renderX, int renderY, int drawWidth) {
        int scaledTabWidth = this.tabWidth * this.tabScale;
        int scaledTabHeight = this.tabHeight * this.tabScale;

        // --- НАСТРОЙКИ СМЕЩЕНИЯ (Менять только здесь!) ---
        int tabXOffset = 0;
        int topTabYOffset = 1;
        int gapBetweenTabs = 1;
        // -------------------------------------------------

        int tabX = renderX + drawWidth - tabXOffset;
        int backTabY = renderY + (topTabYOffset * this.scale);
        int forwardTabY = backTabY + scaledTabHeight + (gapBetweenTabs * this.scale);

        return new TabCoords(tabX, backTabY, forwardTabY);
    }

    // Компактная структура для хранения координат кнопок инструментов
    private record ToolButtonCoords(int scaledBtnWidth, int scaledBtnHeight, int pencilX, int colorPencilX, int colorMarkerX, int eraserX, int kneadedX, int smudgeX, int rulerX, int magGlassX, int compassX, int peekY) {}

    // Вспомогательный метод для расчета
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
        boolean hasColorMarker = hasTool(ModItems.COLOR_MARKER.get()); // ДОБАВЛЕНО

        int pencilX = -1000, colorPencilX = -1000, colorMarkerX = -1000, eraserX = -1000, kneadedX = -1000, smudgeX = -1000, rulerX = -1000, magGlassX = -1000, compassX = -1000;

        int leftX = (this.width / 2) - 100 - scaledBtnWidth;
        if (hasRuler) { rulerX = leftX; leftX -= (scaledBtnWidth + 5); }
        if (hasMagGlass) { magGlassX = leftX; leftX -= (scaledBtnWidth + 5); }
        if (hasCompass) { compassX = leftX; }

        if (hasPencil) { pencilX = currentX; currentX += scaledBtnWidth + 5; }
        if (hasColorPencil) { colorPencilX = currentX; currentX += scaledBtnWidth + 5; }
        if (hasColorMarker) { colorMarkerX = currentX; currentX += scaledBtnWidth + 5; } // ДОБАВЛЕНО
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

        // --- ЗАЩИТА №1: Игнорируем клик, если кнопка пытается увести нас за пределы блокнота
        if (newPageIndex < 0 || newPageIndex >= pages.size()) {
            return;
        }

        // --- ЗАЩИТА №2: Сохраняем текущую страницу ТОЛЬКО если она всё ещё существует (т.е. не была вырвана)
        if (this.currentPageIndex >= 0 && this.currentPageIndex < pages.size()) {
            net.avizvul.esquissemod.component.SketchData data = net.avizvul.esquissemod.component.SketchData.fromArray(this.pixels);
            pages.set(this.currentPageIndex, data);
            stack.set(ModDataComponents.SKETCHBOOK_PAGES.get(), pages); // локально обновляем предмет

            // --- ИСПРАВЛЕНИЕ РАССИНХРОНА: Отправляем рисунок на сервер при перелистывании! ---
            net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                    new SketchbookSavePayload(this.currentPageIndex, data, this.pencilPixelsUsed, this.eraserPixelsUsed)
            );
        }

        // --- ЗАГРУЗКА НОВОЙ СТРАНИЦЫ ---
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

    // Единый метод для поиска инструмента в инвентаре игрока ИЛИ внутри пенала
    private net.minecraft.world.item.ItemStack findItemStack(net.minecraft.world.item.Item targetItem) {
        if (this.minecraft == null || this.minecraft.player == null) return net.minecraft.world.item.ItemStack.EMPTY;

        // Ищем в основной руке и слотах инвентаря
        for (net.minecraft.world.item.ItemStack stack : this.minecraft.player.getInventory().items) {
            if (stack.is(targetItem)) return stack;
            net.minecraft.world.item.ItemStack fromCase = checkPencilCase(stack, targetItem);
            if (!fromCase.isEmpty()) return fromCase;
        }
        // Ищем во второй руке
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

    private void setToolSettings(int size, int hardness) {
        net.minecraft.world.item.ItemStack stack = getActiveToolStack();
        if (!stack.isEmpty()) {
            stack.set(ModDataComponents.BRUSH_SIZE.get(), size);
            stack.set(ModDataComponents.BRUSH_HARDNESS.get(), hardness);
        }
        net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                new net.avizvul.esquissemod.network.ToolSettingsPayload(this.activeTool.ordinal(), size, hardness, getMarkerRotation())
        );
    }

    // Проверяем, является ли предмет пеналом, и заглядываем внутрь него
    private net.minecraft.world.item.ItemStack checkPencilCase(net.minecraft.world.item.ItemStack containerStack, net.minecraft.world.item.Item targetItem) {
        if (containerStack.is(net.avizvul.esquissemod.item.ModItems.PENCIL_CASE.get()) && containerStack.has(net.minecraft.core.component.DataComponents.CONTAINER)) {
            net.minecraft.world.item.component.ItemContainerContents contents = containerStack.get(net.minecraft.core.component.DataComponents.CONTAINER);
            if (contents != null) {
                // Копируем содержимое пенала в список для перебора
                net.minecraft.core.NonNullList<net.minecraft.world.item.ItemStack> items = net.minecraft.core.NonNullList.withSize(9, net.minecraft.world.item.ItemStack.EMPTY);
                contents.copyInto(items);
                for (net.minecraft.world.item.ItemStack innerStack : items) {
                    if (innerStack.is(targetItem)) return innerStack;
                }
            }
        }
        return net.minecraft.world.item.ItemStack.EMPTY;
    }

    // Обновленные старые методы теперь просто используют универсальный поисковик
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
        boolean hasKneaded = hasTool(ModItems.KNEADED_ERASER.get()); // ДОБАВЛЕНО
        boolean hasColorMarker = hasTool(ModItems.COLOR_MARKER.get());

        ItemStack colorPencilStack = getColorPencilStack();
        boolean hasColorPencil = !colorPencilStack.isEmpty();
        boolean hasColors = hasColorPencil && !colorPencilStack.getOrDefault(ModDataComponents.STORED_COLORS.get(), new java.util.ArrayList<>()).isEmpty();

        double cx = renderX + drawWidth / 2.0;
        double cy = renderY + drawHeight / 2.0;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(cx, cy, 0);
        if (this.rotationAngle != 0.0f) {
            guiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(this.rotationAngle));
        }
        guiGraphics.pose().translate(-cx, -cy, 0);

        int scaledTabWidth = this.tabWidth * this.tabScale;
        int scaledTabHeight = this.tabHeight * this.tabScale;

        TabCoords coords = getTabCoords(renderX, renderY, drawWidth);
        int tabX = coords.tabX();
        int backTabY = coords.backTabY();
        int forwardTabY = coords.forwardTabY();

        double[] logicalMouse = getLogicalMouse(mouseX, mouseY);
        double lMouseX = logicalMouse[0];
        double lMouseY = logicalMouse[1];

        if (this.currentPageIndex > 0) {
            boolean backHovered = !this.isDragging && !this.isRotating &&
                    lMouseX >= tabX && lMouseX < tabX + scaledTabWidth &&
                    lMouseY >= backTabY && lMouseY < backTabY + scaledTabHeight;
            float backVOffset = backHovered ? this.tabHeight : 0.0f;
            guiGraphics.blit(PAGE_B_TEX, tabX, backTabY, scaledTabWidth, scaledTabHeight, 0.0f, backVOffset, this.tabWidth, this.tabHeight, this.tabWidth, this.tabHeight * 2);
        }

        if (this.currentPageIndex < 15) {
            boolean forwardHovered = !this.isDragging && !this.isRotating &&
                    lMouseX >= tabX && lMouseX < tabX + scaledTabWidth &&
                    lMouseY >= forwardTabY && lMouseY < forwardTabY + scaledTabHeight;
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

        if (!this.isDragging && !this.isRotating &&
                lMouseX >= canvasScreenLeft && lMouseX < (canvasScreenLeft + scaledCanvasWidth) &&
                lMouseY >= renderY && lMouseY < (renderY + scaledImageHeight)) {

            // ДОБАВЛЕНО: Клячка в условии предпросмотра
            boolean canDraw = (this.activeTool == Tool.PENCIL && hasPencil) ||
                    (this.activeTool == Tool.COLOR_PENCIL && hasColors) ||
                    (this.activeTool == Tool.ERASER && hasEraser) ||
                    (this.activeTool == Tool.SMUDGE && hasSmudge) ||
                    (this.activeTool == Tool.KNEADED_ERASER && hasKneaded);

            if (canDraw) {
                double physicalCellSize = (double) this.scale / this.resolutionMultiplier;
                double[] magnetMouse = applyRulerMagnet(mouseX, mouseY);
                double[] lMouseMagnet = getLogicalMouse(magnetMouse[0], magnetMouse[1]);

                int centerX = (int) ((lMouseMagnet[0] - canvasScreenLeft) / physicalCellSize);
                int centerY = (int) ((lMouseMagnet[1] - renderY) / physicalCellSize);

                int currentBrushSize = getBrushSize();
                int actualSize = currentBrushSize;

                // ДОБАВЛЕНО: Клячка тоже имеет размеры 2, 5, 12
                if (this.activeTool == Tool.SMUDGE || this.activeTool == Tool.KNEADED_ERASER) {
                    actualSize = (currentBrushSize == 1) ? 2 : (currentBrushSize == 2) ? 5 : 12;
                }

                int offset = actualSize / 2;
                double radius = actualSize / 2.0;
                double exactCX = centerX + (actualSize % 2 == 0 ? -0.5 : 0.0);
                double exactCY = centerY + (actualSize % 2 == 0 ? -0.5 : 0.0);

                int previewColor = (this.activeTool == Tool.ERASER || this.activeTool == Tool.KNEADED_ERASER) ? 0x60FF0000 : 0x60000000;

                if (this.activeTool == Tool.COLOR_PENCIL && hasColorPencil && hasColors) {
                    int activeIndex = colorPencilStack.getOrDefault(net.avizvul.esquissemod.component.ModDataComponents.ACTIVE_COLOR_INDEX.get(), 0);
                    java.util.List<Integer> colors = colorPencilStack.getOrDefault(net.avizvul.esquissemod.component.ModDataComponents.STORED_COLORS.get(), new java.util.ArrayList<>());
                    int colorId = colors.get(Math.abs(activeIndex) % colors.size());
                    previewColor = net.minecraft.world.item.DyeColor.byId(colorId).getTextureDiffuseColor() | 0x60000000;
                }

                for (int x = centerX - offset; x < centerX - offset + actualSize; x++) {
                    for (int y = centerY - offset; y < centerY - offset + actualSize; y++) {

                        double dx = x - exactCX;
                        double dy = y - exactCY;

                        // ДОБАВЛЕНО: Клячка тоже круглая
                        if ((this.activeTool == Tool.SMUDGE || this.activeTool == Tool.KNEADED_ERASER) && Math.sqrt(dx * dx + dy * dy) > radius) continue;

                        if (x >= 0 && x < this.canvasWidth * this.resolutionMultiplier &&
                                y >= 0 && y < this.canvasHeight * this.resolutionMultiplier) {

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

        // --- ОТРИСОВКА ЦИРКУЛЯ ---
        // ИСПРАВЛЕНИЕ: Рисуем ДО popPose(), чтобы циркуль вращался вместе с блокнотом
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
                    if (dist > 192.0) { // 192 - это наш максимальный радиус
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

        // --- 9. ИНСТРУМЕНТЫ И ИНДИКАТОРЫ ---
        ToolButtonCoords toolCoords = getToolButtonCoords();
        int scaledBtnWidth = toolCoords.scaledBtnWidth();
        int scaledBtnHeight = toolCoords.scaledBtnHeight();
        int peekY = toolCoords.peekY();

        if (hasPencil) renderToolButton(guiGraphics, mouseX, mouseY, this.activeTool == Tool.PENCIL, PENCIL_TEX, toolCoords.pencilX());
        if (hasColorPencil) renderColorToolButton(guiGraphics, mouseX, mouseY, toolCoords.colorPencilX(), colorPencilStack, COLOR_PENCIL_TEX, COLOR_PENCIL_TINT_TEX, Tool.COLOR_PENCIL);
        if (hasColorMarker) renderColorToolButton(guiGraphics, mouseX, mouseY, toolCoords.colorMarkerX(), getColorMarkerStack(), COLOR_MARKER_TEX, COLOR_MARKER_TINT_TEX, Tool.COLOR_MARKER);
        if (hasEraser) renderToolButton(guiGraphics, mouseX, mouseY, this.activeTool == Tool.ERASER, ERASER_TEX, toolCoords.eraserX());
        if (hasKneaded) renderToolButton(guiGraphics, mouseX, mouseY, this.activeTool == Tool.KNEADED_ERASER, KNEADED_ERASER_TEX, toolCoords.kneadedX()); // ДОБАВЛЕНО
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
        if (hasCompass) {
            renderToolButton(guiGraphics, mouseX, mouseY, this.compassState != CompassState.INACTIVE, COMPASS_BTN_TEX, toolCoords.compassX());
        }

        if (this.activeTool == Tool.PENCIL && hasPencil) {
            renderSizeIndicators(guiGraphics, mouseX, mouseY, toolCoords.pencilX(), peekY);
        } else if (this.activeTool == Tool.COLOR_PENCIL && hasColorPencil) {
            if (hasColors) renderSizeIndicators(guiGraphics, mouseX, mouseY, toolCoords.colorPencilX(), peekY);
            else {
                String emptyText = "Empty";
                int emptyX = toolCoords.colorPencilX() + (scaledBtnWidth / 2) - (this.font.width(emptyText) / 2);
                guiGraphics.drawString(this.font, emptyText, emptyX, peekY - 24, 0xFFFF0000, false);
            }
            renderPalette(guiGraphics, colorPencilStack);
        } else if (this.activeTool == Tool.ERASER && hasEraser) {
            renderSizeIndicators(guiGraphics, mouseX, mouseY, toolCoords.eraserX(), peekY);
        } else if (this.activeTool == Tool.KNEADED_ERASER && hasKneaded) { // ДОБАВЛЕНО
            renderSizeIndicators(guiGraphics, mouseX, mouseY, toolCoords.kneadedX(), peekY);
        } else if (this.activeTool == Tool.SMUDGE && hasSmudge) {
            renderSizeIndicators(guiGraphics, mouseX, mouseY, toolCoords.smudgeX(), peekY);
        }

        // ДОБАВЛЕНО: Текст жесткости для клячки
        if ((this.activeTool == Tool.PENCIL && hasPencil) || (this.activeTool == Tool.COLOR_PENCIL && hasColors) ||
                (this.activeTool == Tool.SMUDGE && hasSmudge) || (this.activeTool == Tool.KNEADED_ERASER && hasKneaded)) {

            int currentToolHardness = getHardness();
            String hardnessText;

            if (this.activeTool == Tool.SMUDGE || this.activeTool == Tool.KNEADED_ERASER) {
                hardnessText = (currentToolHardness == 1) ? "L" : (currentToolHardness == 2) ? "M" : "H";
            } else {
                hardnessText = (currentToolHardness == 1) ? "2H" : (currentToolHardness == 2) ? "HB" : "4B";
            }

            int hardnessColor = (currentToolHardness == 1) ? 0xFFAAAAAA : (currentToolHardness == 2) ? 0xFF555555 : 0xFF222222;

            int activeX = (this.activeTool == Tool.PENCIL) ? toolCoords.pencilX() :
                    (this.activeTool == Tool.SMUDGE) ? toolCoords.smudgeX() :
                    (this.activeTool == Tool.KNEADED_ERASER) ? toolCoords.kneadedX() : toolCoords.colorPencilX();

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
        // Сначала отрисовываем темный фон (затемнение мира)
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        boolean hasMagGlass = hasTool(ModItems.MAGNIFYING_GLASS.get());
        if (!hasMagGlass) {
            this.isMagnifyingMode = false;
            this.isMagnifierLocked = false;
        }

        boolean isMagActive = this.isMagnifyingMode || this.isMagnifierLocked;

        // 1. Отрисовываем весь интерфейс обычного размера
        renderContent(guiGraphics, mouseX, mouseY, partialTick);

        // 2. Если лупа активна - отрисовываем интерфейс ПОВТОРНО в увеличенном масштабе внутри маски!
        if (isMagActive) {
            float glassScale = 2.5f; // Масштаб лупы и самого увеличения (х2.5)

            // Если ваше стекло всё так же от 2 до 26 пикселей, то радиус остается 12
            int baseRadius = 13;
            int scaledRadius = (int) (baseRadius * glassScale); // Радиус обрезки = 30 пикселей

            // Обрезаем область отрисовки до квадрата 60х60 вокруг мыши
            guiGraphics.enableScissor(mouseX - scaledRadius, mouseY - scaledRadius, mouseX + scaledRadius, mouseY + scaledRadius);

            guiGraphics.pose().pushPose();
            // Сдвигаемся к курсору, увеличиваем масштаб интерфейса в 2.5 раза и возвращаемся
            guiGraphics.pose().translate(mouseX, mouseY, 0);
            guiGraphics.pose().scale(glassScale, glassScale, 1.0f);
            guiGraphics.pose().translate(-mouseX, -mouseY, 0);

            // Заново вызываем наш рендер контента.
            renderContent(guiGraphics, mouseX, mouseY, partialTick);

            guiGraphics.pose().popPose();
            guiGraphics.disableScissor(); // Выключаем обрезку

            // 3. Рисуем саму графику лупы поверх всего этого
            com.mojang.blaze3d.systems.RenderSystem.enableBlend();

            // ИСПРАВЛЕНИЕ: Новые размеры текстуры лупы
            int texWidth = 29;
            int texHeight = 58;

            // Пересчет размеров с учетом масштаба 2.5x (будет 72x145 пикселей на экране)
            int destWidth = (int) (texWidth * glassScale);
            int destHeight = (int) (texHeight * glassScale);

            // Так как стекло находится в верхней части текстуры, его центр по-прежнему 14х14
            int offsetX = (int) (14 * glassScale); // Будет 35
            int offsetY = (int) (14 * glassScale); // Будет 35

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

        // 1. Отрисовка базовой части (дерево/пластик)
        guiGraphics.blit(baseTex, x, renderY, scaledWidth, scaledHeight, 0.0f, vOffset, this.buttonWidth, this.buttonHeight, this.buttonWidth, this.buttonHeight * 2);

        // 2. Получаем активный цвет из переданного предмета
        int rgb = 0xFFFFFF; // Белый по умолчанию
        java.util.List<Integer> colors = colorStack.getOrDefault(ModDataComponents.STORED_COLORS.get(), new java.util.ArrayList<>());
        if (!colors.isEmpty()) {
            int activeIndex = colorStack.getOrDefault(ModDataComponents.ACTIVE_COLOR_INDEX.get(), 0);
            int colorId = colors.get(Math.abs(activeIndex) % colors.size());
            rgb = net.minecraft.world.item.DyeColor.byId(colorId).getTextureDiffuseColor();
        }

        float r = ((rgb >> 16) & 0xFF) / 255.0f;
        float g = ((rgb >> 8) & 0xFF) / 255.0f;
        float b = (rgb & 0xFF) / 255.0f;

        // 3. Задаем цвет рендера и рисуем слой маски (грифель/стержень)
        guiGraphics.setColor(r, g, b, 1.0f);
        guiGraphics.blit(tintTex, x, renderY, scaledWidth, scaledHeight, 0.0f, vOffset, this.buttonWidth, this.buttonHeight, this.buttonWidth, this.buttonHeight * 2);
        guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
    // Структура для хранения координат одного квадратика цвета
    private record Swatch(int colorId, int x, int y) {}

    // Метод, рассчитывающий круг и линию градиента (теперь всегда для всех 16 цветов!)
    private java.util.List<Swatch> getPaletteLayout() {
        java.util.List<Swatch> layout = new java.util.ArrayList<>();

        // Серые цвета (0=Белый, 8=Светло-серый, 7=Серый, 15=Черный)
        java.util.List<Integer> grays = java.util.List.of(0, 8, 7, 15);
        // Цветные в круг (в порядке красивой радуги)
        java.util.List<Integer> wheelColors = java.util.List.of(14, 1, 4, 5, 13, 9, 3, 11, 10, 2, 6, 12);

        int centerX = this.width - 80;
        int centerY = this.height - 70;
        int radius = 30;
        int swatchSize = 12;

        // 1. Строим цветовой круг
        for (int i = 0; i < wheelColors.size(); i++) {
            int colorId = wheelColors.get(i);
            double angle = 2 * Math.PI * i / wheelColors.size() - Math.PI / 2;
            int x = centerX + (int) (Math.cos(angle) * radius) - (swatchSize / 2);
            int y = centerY + (int) (Math.sin(angle) * radius) - (swatchSize / 2);
            layout.add(new Swatch(colorId, x, y));
        }

        // 2. Строим линию серых оттенков
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

    // Метод отрисовки палитры
// Метод отрисовки палитры
    private void renderPalette(GuiGraphics guiGraphics, net.minecraft.world.item.ItemStack colorPencilStack) {
        if (this.activeTool != Tool.COLOR_PENCIL || colorPencilStack.isEmpty()) return;

        java.util.List<Integer> colors = colorPencilStack.getOrDefault(ModDataComponents.STORED_COLORS.get(), new java.util.ArrayList<>());
        int activeIndex = Math.abs(colorPencilStack.getOrDefault(ModDataComponents.ACTIVE_COLOR_INDEX.get(), 0));
        int activeColorId = colors.isEmpty() ? -1 : colors.get(activeIndex % colors.size());

        int swatchSize = 12;

        // ОШИБКА БЫЛА ЗДЕСЬ: Вызов строго без аргументов!
        for (Swatch swatch : getPaletteLayout()) {
            boolean hasColor = colors.contains(swatch.colorId());

            int outlineColor;
            if (colors.isEmpty()) {
                outlineColor = 0xFFFFFFFF; // Белая обводка для всех, если карандаш пуст
            } else {
                // ОШИБКА БЫЛА ЗДЕСЬ: Мы убрали index, теперь сравниваем по colorId!
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

    // Отрисовка индикаторов размера
    private void renderSizeIndicators(GuiGraphics guiGraphics, int mouseX, int mouseY, int toolX, int toolY) {
        int bottomY = toolY - 4;

        // --- ИСПРАВЛЕНИЕ: Разные размеры кнопочек и отступов для растушевки ---
        int[] sizes = (this.activeTool == Tool.SMUDGE) ? new int[]{2, 5, 12} : new int[]{3, 5, 7};
        int[] xOffsets = (this.activeTool == Tool.SMUDGE) ? new int[]{5, 11, 18} : new int[]{5, 11, 19};

        for (int i = 1; i <= 3; i++) {
            int size = sizes[i - 1];
            int btnX = toolX + xOffsets[i - 1];
            int btnY = bottomY - size;

            int color = (getBrushSize() == i) ? 0xFFFFFFFF : 0xFF555555;

            if (mouseX >= btnX - 2 && mouseX < btnX + size + 2 && mouseY >= btnY - 2 && mouseY < btnY + size + 2) {
                color = 0xFFFFFFAA;
            }
            guiGraphics.fill(btnX, btnY, btnX + size, btnY + size, color);
        }
    }

    // Обработка клика по индикаторам
    private boolean handleSizeIndicatorClick(double mouseX, double mouseY, int toolX, int toolY) {
        int bottomY = toolY - 4;

        // --- ИСПРАВЛЕНИЕ: Те же размеры и отступы, что и в рендере ---
        int[] sizes = (this.activeTool == Tool.SMUDGE) ? new int[]{2, 5, 12} : new int[]{3, 5, 7};
        int[] xOffsets = (this.activeTool == Tool.SMUDGE) ? new int[]{5, 11, 18} : new int[]{5, 11, 19};

        for (int i = 1; i <= 3; i++) {
            int size = sizes[i - 1];
            int btnX = toolX + xOffsets[i - 1];
            int btnY = bottomY - size;

            if (mouseX >= btnX - 2 && mouseX < btnX + size + 2 && mouseY >= btnY - 2 && mouseY < btnY + size + 2) {
                setToolSettings(i, getHardness());
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
        // Переводим курсор в локальные координаты линейки
        double localX = dx * Math.cos(rad) - dy * Math.sin(rad);
        double localY = dx * Math.sin(rad) + dy * Math.cos(rad);

        // Дистанция захвата - 15 пикселей от рабочей грани
        // Проверяем, что мышь рядом с гранью (localY ~ 0) и не выходит за длину линейки
        if (Math.abs(localY) <= 15 && Math.abs(localX) <= this.rulerWidth / 2.0) {
            localY = 0; // Строго привязываем к верхней кромке (рабочей стороне)

            // Возвращаем примагниченные координаты обратно в глобальные
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

        // --- Вычисляем вектор сдвига для растушевки ---
        int shiftX = 0, shiftY = 0;
        if (this.lastLogicalX != -1 && this.lastLogicalY != -1) {
            int lastCX = (int) ((this.lastLogicalX - canvasScreenLeft) / physicalCellSize);
            int lastCY = (int) ((this.lastLogicalY - canvasScreenTop) / physicalCellSize);
            shiftX = centerX - lastCX;
            shiftY = centerY - lastCY;
        }

        // --- УНИКАЛЬНЫЕ РАЗМЕРЫ ДЛЯ ИНСТРУМЕНТОВ ---
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

        // --- НОВАЯ МАТЕМАТИКА ДЛЯ МАРКЕРА ---
        boolean isMarker = (this.activeTool == Tool.COLOR_MARKER);
        int markerRot = isMarker ? getMarkerRotation() : 0;
        double angleRad = Math.toRadians(markerRot * 45.0); // 0, 45, 90, 135 градусов
        double mCos = Math.cos(angleRad);
        double mSin = Math.sin(angleRad);

        // Длина маркера больше его толщины
        double length = actualSize * 1.5;
        double thickness = Math.max(1.0, actualSize * 0.4);

        int startX = isMarker ? (centerX - actualSize) : (centerX - offset);
        int endX = isMarker ? (centerX + actualSize) : (centerX - offset + actualSize - 1);
        int startY = isMarker ? (centerY - actualSize) : (centerY - offset);
        int endY = isMarker ? (centerY + actualSize) : (centerY - offset + actualSize - 1);

        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {

                double dx = x - exactCX;
                double dy = y - exactCY;
                double distance = Math.sqrt(dx * dx + dy * dy);

                if (isMarker) {
                    // Поворачиваем координату вокруг центра кисти
                    double localX = dx * mCos + dy * mSin;
                    double localY = -dx * mSin + dy * mCos;
                    // Отрезаем всё, что за пределами прямоугольника маркера
                    if (Math.abs(localX) > length / 2.0 || Math.abs(localY) > thickness / 2.0) continue;
                } else {
                    // Отсекаем углы (делаем круг) для растушевки и клячки
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

                        // Растушевка игнорирует блокировку штриха и может мазать по одному месту бесконечно
                        if (this.strokePixels[x][y] && this.activeTool != Tool.SMUDGE) continue;

                        // --- ЛОГИКА КЛЯЧКИ (KNEADED ERASER) ---
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
                                    if (a <= 5) {
                                        pixels[x][y] = 0; // Стираем полностью
                                    } else {
                                        pixels[x][y] = (a << 24) | (currentColor & 0x00FFFFFF); // Оставляем цвет, уменьшаем альфу
                                    }
                                    this.isCanvasDirty = true;
                                    this.eraserPixelsUsed++;
                                    this.strokePixels[x][y] = true;
                                }
                            }
                        }
                        // --- ЛОГИКА РАСТУШЕВКИ (SMUDGE) ---
                        else if (this.activeTool == Tool.SMUDGE) {
                            float shiftRate = (currentToolHardness == 1) ? 0.4f : (currentToolHardness == 2) ? 0.7f : 0.95f;
                            float mixRate = (currentToolHardness == 1) ? 0.65f : (currentToolHardness == 2) ? 0.35f : 0.15f;
                            float falloff = 1.0f;
                            float brushHardness = (currentToolHardness == 1) ? 0.6f : (currentToolHardness == 2) ? 0.85f : 1.0f;
                            double softRadius = radius * brushHardness;

                            if (distance > softRadius && radius > softRadius) {
                                falloff = (float) (1.0 - (distance - softRadius) / (radius - softRadius));
                            }

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
                        }
                        // --- ЛОГИКА ОБЫЧНЫХ КАРАНДАШЕЙ И ЦВЕТНОГО МАРКЕРА ---
                        else {
                            int brushRgb = 0x111111;
                            net.minecraft.world.item.ItemStack activeColorStack = (this.activeTool == Tool.COLOR_MARKER) ? getColorMarkerStack() : getColorPencilStack();

                            if ((this.activeTool == Tool.COLOR_PENCIL || this.activeTool == Tool.COLOR_MARKER) && !activeColorStack.isEmpty()) {
                                java.util.List<Integer> colors = activeColorStack.getOrDefault(net.avizvul.esquissemod.component.ModDataComponents.STORED_COLORS.get(), new java.util.ArrayList<>());
                                if (!colors.isEmpty()) {
                                    int activeIndex = activeColorStack.getOrDefault(net.avizvul.esquissemod.component.ModDataComponents.ACTIVE_COLOR_INDEX.get(), 0);
                                    brushRgb = net.minecraft.world.item.DyeColor.byId(colors.get(Math.abs(activeIndex) % colors.size())).getTextureDiffuseColor();
                                }
                            }

                            // Для маркера альфа-канал всегда 255 (максимально плотный цвет)
                            int alpha = (this.activeTool == Tool.COLOR_MARKER) ? 255 : ((currentToolHardness == 1) ? 64 : (currentToolHardness == 2) ? 128 : 255);
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
        boolean hasColorMarker = hasTool(ModItems.COLOR_MARKER.get()); // ДОБАВЛЕНО

        ItemStack colorPencilStack = getColorPencilStack();
        boolean hasColorPencil = !colorPencilStack.isEmpty();
        boolean hasRuler = hasTool(net.avizvul.esquissemod.item.ModItems.RULER.get());
        boolean hasMagGlass = hasTool(ModItems.MAGNIFYING_GLASS.get());

        // Проверяем наличие заправленных цветов для обоих цветных инструментов
        boolean hasColors = (hasColorPencil && !colorPencilStack.getOrDefault(ModDataComponents.STORED_COLORS.get(), new java.util.ArrayList<>()).isEmpty()) ||
                (hasColorMarker && !getColorMarkerStack().getOrDefault(ModDataComponents.STORED_COLORS.get(), new java.util.ArrayList<>()).isEmpty());

        ToolButtonCoords toolCoords = getToolButtonCoords();
        int scaledBtnWidth = toolCoords.scaledBtnWidth();
        int scaledBtnHeight = toolCoords.scaledBtnHeight();

        int pencilX = toolCoords.pencilX();
        int colorPencilX = toolCoords.colorPencilX();
        int colorMarkerX = toolCoords.colorMarkerX(); // ДОБАВЛЕНО
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
            int colorMarkerY = (this.activeTool == Tool.COLOR_MARKER) ? peekY : baseY; // ДОБАВЛЕНО
            int eraserY = (this.activeTool == Tool.ERASER) ? peekY : baseY;
            int smudgeY = (this.activeTool == Tool.SMUDGE) ? peekY : baseY;
            int kneadedY = (this.activeTool == Tool.KNEADED_ERASER) ? peekY : baseY;

            // 1. Выбор инструментов
            if (hasPencil && mouseX >= pencilX && mouseX < pencilX + scaledBtnWidth && mouseY >= pencilY && mouseY < pencilY + scaledBtnHeight) {
                this.activeTool = Tool.PENCIL; return true;
            }
            if (hasColorPencil && mouseX >= colorPencilX && mouseX < colorPencilX + scaledBtnWidth && mouseY >= colorPencilY && mouseY < colorPencilY + scaledBtnHeight) {
                this.activeTool = Tool.COLOR_PENCIL; return true;
            }
            if (hasColorMarker && mouseX >= colorMarkerX && mouseX < colorMarkerX + scaledBtnWidth && mouseY >= colorMarkerY && mouseY < colorMarkerY + scaledBtnHeight) { // ДОБАВЛЕНО
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

            // Активация компаса на ЛКМ
            if (hasCompass && this.compassState == CompassState.INACTIVE && mouseX >= compassX && mouseX < compassX + scaledBtnWidth && mouseY >= baseY && mouseY < baseY + scaledBtnHeight) {
                this.compassState = CompassState.FOLDED;
                return true;
            }

            // 2. Индикаторы размера кисти и палитра
            if (this.activeTool == Tool.PENCIL && hasPencil) {
                if (handleSizeIndicatorClick(mouseX, mouseY, pencilX, peekY)) return true;
            } else if ((this.activeTool == Tool.COLOR_PENCIL && hasColorPencil) || (this.activeTool == Tool.COLOR_MARKER && hasColorMarker)) {
                // ИСПРАВЛЕНИЕ: Палитра и размеры работают корректно и для карандаша, и для маркера
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
                            net.neoforged.neoforge.network.PacketDistributor.sendToServer(new net.avizvul.esquissemod.network.ChangeColorPayload(foundIndex));
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

            // ИСПРАВЛЕНИЕ: Берем координаты из массива правильно
            double[] logicalMouse = getLogicalMouse(mouseX, mouseY);
            double lMouseX = logicalMouse[0];
            double lMouseY = logicalMouse[1];

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

                    // Установка якоря и блокировка радиуса для компаса
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
                    double[] drawLogical = getLogicalMouse(magnetMouse[0], magnetMouse[1]);

                    // МАГИЯ ЦИРКУЛЯ: Перехватываем координаты кисти
                    if (this.compassState == CompassState.LOCKED) {
                        double angle = Math.atan2(drawLogical[1] - this.compassAnchorY, drawLogical[0] - this.compassAnchorX);
                        drawLogical[0] = this.compassAnchorX + this.compassRadius * Math.cos(angle);
                        drawLogical[1] = this.compassAnchorY + this.compassRadius * Math.sin(angle);
                    }

                    // Стандартное рисование с добавленным Маркером
                    if ((this.activeTool == Tool.PENCIL && hasPencil) ||
                            (this.activeTool == Tool.COLOR_PENCIL && hasColorPencil && hasColors) ||
                            (this.activeTool == Tool.COLOR_MARKER && hasColorMarker && hasColors) ||
                            (this.activeTool == Tool.SMUDGE && hasSmudge)) {
                        this.isDrawing = true;
                        this.lastLogicalX = drawLogical[0];
                        this.lastLogicalY = drawLogical[1];
                        drawPixel(drawLogical[0], drawLogical[1]);
                    } else if ((this.activeTool == Tool.ERASER && hasEraser) || (this.activeTool == Tool.KNEADED_ERASER && hasKneaded)) {
                        this.isErasing = true;
                        this.lastLogicalX = drawLogical[0];
                        this.lastLogicalY = drawLogical[1];
                        drawPixel(drawLogical[0], drawLogical[1]);
                    }
                    return true;
                }
            }
        }

        if (button == 1) {
            double[] logicalMouse = getLogicalMouse(mouseX, mouseY);
            double lMouseX = logicalMouse[0];
            double lMouseY = logicalMouse[1];

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

            // 3. Изменение твердости карандаша (Правый клик по инструментам)
            // Маркер пропускаем, так как твердость у него не меняется
            boolean clickedPencil = hasPencil && mouseX >= pencilX && mouseX < pencilX + scaledBtnWidth && mouseY >= peekY && mouseY < peekY + scaledBtnHeight;
            boolean clickedColorPencil = hasColorPencil && hasColors && mouseX >= colorPencilX && mouseX < colorPencilX + scaledBtnWidth && mouseY >= peekY && mouseY < peekY + scaledBtnHeight;
            boolean clickedSmudge = hasSmudge && mouseX >= smudgeX && mouseX < smudgeX + scaledBtnWidth && mouseY >= peekY && mouseY < peekY + scaledBtnHeight;
            boolean clickedKneaded = hasKneaded && mouseX >= kneadedX && mouseX < kneadedX + scaledBtnWidth && mouseY >= peekY && mouseY < peekY + scaledBtnHeight;

            if (clickedPencil || clickedColorPencil || clickedSmudge || clickedKneaded) {
                if (clickedPencil) this.activeTool = Tool.PENCIL;
                else if (clickedColorPencil) this.activeTool = Tool.COLOR_PENCIL;
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

            // Отмена шагов циркуля и убирание его в тулбар
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
            double[] lMouse = getLogicalMouse(magnetMouse[0], magnetMouse[1]);

            // Если циркуль активен, заставляем кисть рисовать ровно по окружности
            if (this.compassState == CompassState.LOCKED) {
                double angle = Math.atan2(lMouse[1] - this.compassAnchorY, lMouse[0] - this.compassAnchorX);
                lMouse[0] = this.compassAnchorX + this.compassRadius * Math.cos(angle);
                lMouse[1] = this.compassAnchorY + this.compassRadius * Math.sin(angle);
            }
            drawPixel(lMouse[0], lMouse[1]);
            this.lastLogicalX = lMouse[0];
            this.lastLogicalY = lMouse[1];
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

        // Проверяем, зажат ли Shift (для изменения нажима/жесткости)
        if (Screen.hasShiftDown()) {
            if ((this.activeTool == Tool.PENCIL && hasTool(ModItems.PENCIL.get())) ||
                    (this.activeTool == Tool.COLOR_PENCIL && hasColors) ||
                    (this.activeTool == Tool.SMUDGE && hasTool(ModItems.SMUDGE.get())) ||
                    (this.activeTool == Tool.KNEADED_ERASER && hasTool(ModItems.KNEADED_ERASER.get()))) { // <-- ДОБАВЛЕНА КЛЯЧКА ЗДЕСЬ

                int h = getHardness();
                if (scrollY > 0) h = Math.min(3, h + 1);
                else if (scrollY < 0) h = Math.max(1, h - 1);

                setToolSettings(getBrushSize(), h);
                return true;
            } else if (this.activeTool == Tool.COLOR_MARKER && hasTool(ModItems.COLOR_MARKER.get())) {
                int r = getMarkerRotation();
                if (scrollY > 0) r = (r + 1) % 4; // 4 угла: 0, 45, 90, 135 градусов
                else if (scrollY < 0) r = (r - 1 + 4) % 4;
                setToolSettings(getBrushSize(), getHardness(), r);
                return true;
            }
        } else {
            // Без Shift меняем размер кисти
            int s = getBrushSize();
            if (scrollY > 0) s = Math.min(3, s + 1);
            else if (scrollY < 0) s = Math.max(1, s - 1);

            setToolSettings(s, getHardness());
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
        // НОВОЕ: Проверяем наличие цветного карандаша (используем уже готовый метод)
        boolean hasColorPencil = !getColorPencilStack().isEmpty();
        boolean hasColorMarker = hasTool(ModItems.COLOR_MARKER.get());

        if (keyCode == GLFW.GLFW_KEY_B && hasPencil) {
            this.activeTool = Tool.PENCIL;
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_C && hasColorPencil) {
            // НОВОЕ: При нажатии на 'C' берем цветной карандаш
            this.activeTool = Tool.COLOR_PENCIL;
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_E && hasEraser) {
            this.activeTool = Tool.ERASER;
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_R) {
            // Проверяем наличие линейки в инвентаре!
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
                    // Быстрый вызов (Shift + D)
                    if (!this.isQuickCompassMode) {
                        this.isQuickCompassMode = true;
                        this.compassState = CompassState.ANCHORED;
                        // Моментально ставим иглу туда, где сейчас мышь
                        double[] logicalMouse = getLogicalMouse(this.lastMouseX, this.lastMouseY);
                        this.compassAnchorX = logicalMouse[0];
                        this.compassAnchorY = logicalMouse[1];
                    }
                } else {
                    // Обычный вызов (просто D) - работает как кнопка в тулбаре
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
        // При отпускании клавиши R отключаем режим "быстрого позиционирования", фиксируя линейку
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

        // --- ИСПРАВЛЕНИЕ: Локально сохраняем рисунок в предмет перед закрытием ---
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

        // Теперь пакет весит считанные байты и отправится без крашей
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

            // ИСПРАВЛЕНИЕ: Точно так же отдаем регистрацию на откуп самой игре
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
                        image.setPixelRGBA(x, y, 0); // Чистим стертые пиксели
                    }
                }
            }
            this.activeCanvasTexture.upload();
        }
        this.isCanvasDirty = false;
    }
}