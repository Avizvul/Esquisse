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

// ВНИМАНИЕ: Замените на ВАШ импорт предметов
// import net.avizvul.esquissemod.item.ModItems;

public class SketchbookScreen extends Screen {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(EsquisseMod.MOD_ID, "textures/gui/sketchbookgui.png");
    private static final ResourceLocation PENCIL_TEX = ResourceLocation.fromNamespaceAndPath(EsquisseMod.MOD_ID, "textures/gui/button_pencil.png");
    private static final ResourceLocation ERASER_TEX = ResourceLocation.fromNamespaceAndPath(EsquisseMod.MOD_ID, "textures/gui/button_eraser.png");
    private static final ResourceLocation ROTATE_BTN_TEX = ResourceLocation.fromNamespaceAndPath(EsquisseMod.MOD_ID, "textures/gui/button_rotate.png");
    private static final ResourceLocation PAGE_B_TEX = ResourceLocation.fromNamespaceAndPath(EsquisseMod.MOD_ID, "textures/gui/button_page_b.png");
    private static final ResourceLocation PAGE_F_TEX = ResourceLocation.fromNamespaceAndPath(EsquisseMod.MOD_ID, "textures/gui/button_page_f.png");
    private static final ResourceLocation EMPTY_PAGE_TEX = ResourceLocation.fromNamespaceAndPath(EsquisseMod.MOD_ID, "textures/item/empty_page.png");

    private enum Tool { PENCIL, ERASER }
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
    private byte currentHardness = 3;
    private byte[][] pixels = new byte[canvasWidth * resolutionMultiplier][canvasHeight * resolutionMultiplier];
    private int brushSize = 1;
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

    // --- ПЕРЕМЕННЫЕ ДЛЯ СТРАНИЦ ---
    private List<SketchData> pages = new ArrayList<>();
    private int currentPageIndex = 0;
    private static int savedPageIndex = 0; // Чтобы запоминать страницу при закрытии


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

                SketchData emptyData = SketchData.fromArray(new byte[this.canvasWidth * this.resolutionMultiplier][this.canvasHeight * this.resolutionMultiplier]);

                for (int i = 0; i < 16; i++) {
                    this.pages.add(emptyData);
                }
            }

            // 2. Вызываем помощник для загрузки пикселей текущей страницы на холст
            loadPagePixels();
        }
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
    private record ToolButtonCoords(int scaledBtnWidth, int scaledBtnHeight, int pencilX, int eraserX, int peekY) {}

    // Вспомогательный метод для расчета
    private ToolButtonCoords getToolButtonCoords() {
        int scaledBtnWidth = this.buttonWidth * this.buttonScale;
        int scaledBtnHeight = this.buttonHeight * this.buttonScale;
        int pencilX = (this.width / 2) + 100;
        int eraserX = pencilX + scaledBtnWidth + 10;
        int peekY = this.height - scaledBtnHeight;

        return new ToolButtonCoords(scaledBtnWidth, scaledBtnHeight, pencilX, eraserX, peekY);
    }

    private void loadPagePixels() {
        if (this.pages != null && this.currentPageIndex >= 0 && this.currentPageIndex < this.pages.size()) {
            SketchData data = this.pages.get(this.currentPageIndex);
            this.pixels = data.toArray(this.canvasWidth * this.resolutionMultiplier, this.canvasHeight * this.resolutionMultiplier);
        }
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

    private boolean hasTool(Item toolItem) {
        if (this.minecraft == null || this.minecraft.player == null) return false;
        for (ItemStack stack : this.minecraft.player.getInventory().items) {
            if (stack.is(toolItem)) return true;
        }
        for (ItemStack stack : this.minecraft.player.getInventory().offhand) {
            if (stack.is(toolItem)) return true;
        }
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {}

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // --- 1. БАЗОВЫЕ ПЕРЕМЕННЫЕ ---
        int renderX = (int) this.exactGuiLeft;
        int renderY = (int) this.exactGuiTop;
        int drawWidth = this.fileWidth * this.scale;
        int drawHeight = this.fileHeight * this.scale;

        // ВНИМАНИЕ: Проверьте, что здесь указаны ваши правильные предметы из ModItems!
        boolean hasPencil = hasTool(ModItems.PENCIL.get());
        boolean hasEraser = hasTool(ModItems.ERASER.get());

        double cx = renderX + drawWidth / 2.0;
        double cy = renderY + drawHeight / 2.0;

        // --- 2. НАЧАЛО БЛОКА ВРАЩЕНИЯ ---
        guiGraphics.pose().pushPose();

        guiGraphics.pose().translate(cx, cy, 0);
        if (this.rotationAngle != 0.0f) {
            guiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(this.rotationAngle));
        }
        guiGraphics.pose().translate(-cx, -cy, 0);

        // --- 3. ЗАКЛАДКИ ПОД БЛОКНОТОМ ---
        int scaledTabWidth = this.tabWidth * this.tabScale;
        int scaledTabHeight = this.tabHeight * this.tabScale;

        // Вызываем наш метод и получаем готовые координаты
        TabCoords coords = getTabCoords(renderX, renderY, drawWidth);
        int tabX = coords.tabX();
        int backTabY = coords.backTabY();
        int forwardTabY = coords.forwardTabY();

        // Получаем координаты с учетом вращения (индексы 0 и 1 обязательны)
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

        // --- 4. ФОН БЛОКНОТА (Накладывается поверх левой половины закладок) ---
        guiGraphics.blit(TEXTURE, renderX, renderY, drawWidth, drawHeight, 0.0f, 0.0f, this.fileWidth, this.fileHeight, this.fileWidth, this.fileHeight);

        // --- ПОДСВЕТКА СИНЕЙ ЗОНЫ (ПЕРФОРАЦИИ) ---
        int blueZoneWidth = this.deadZoneWidth * this.scale;
        int blueZoneLeft = renderX + (this.frameWidth * this.scale);
        int blueZoneRight = blueZoneLeft + blueZoneWidth;
        int blueZoneTop = renderY;
        int blueZoneBottom = renderY + (this.canvasHeight * this.scale);

        // Если курсор мыши находится в пределах синей зоны
        if (lMouseX >= blueZoneLeft && lMouseX <= blueZoneRight && lMouseY >= blueZoneTop && lMouseY <= blueZoneBottom) {

            // Настройки нашего пунктира
            int dashLength = 5; // Длина одного красного штриха в пикселях
            int dashGap = 3;    // Расстояние между штрихами
            int lineWidth = 2;  // Толщина линии

            // Цвет ARGB (Альфа, Красный, Зеленый, Синий)
            // 0xFF - полная непрозрачность, FF0000 - чистый красный цвет [1]
            int color = 0xFFEE0000;

            // Рисуем линию ровно по центру синей зоны
            int lineX = blueZoneLeft + (blueZoneWidth / 2) - (lineWidth / 2);

            // Проходимся циклом сверху вниз по высоте блокнота
            for (int y = blueZoneTop; y < blueZoneBottom; y += dashLength + dashGap) {
                // Вычисляем нижнюю координату текущего штриха
                // Math.min нужен, чтобы последний штрих не вылез за границу блокнота
                int currentDashBottom = Math.min(y + dashLength, blueZoneBottom);

                // Рисуем один штрих с помощью метода fill (закрашенный прямоугольник) [2]
                guiGraphics.fill(lineX, y, lineX + lineWidth, currentDashBottom, color);
            }
        }

        // --- 5. КНОПКА ПОВОРОТА ---
        int btnFileWidth = 8;
        int btnFileHeight = 8;
        int btnX = renderX;
        int btnY = renderY + ((this.fileHeight - btnFileHeight) / 2) * this.scale;
        guiGraphics.blit(ROTATE_BTN_TEX, btnX, btnY, btnFileWidth * this.scale, btnFileHeight * this.scale, 0.0f, 0.0f, btnFileWidth, btnFileHeight, btnFileWidth, btnFileHeight);

        // --- 6. ОТРИСОВКА ХОЛСТА (ПИКСЕЛЕЙ) ---
        int canvasScreenLeft = renderX + ((this.frameWidth + this.deadZoneWidth) * this.scale);
        int canvasScreenTop = renderY;

        guiGraphics.pose().pushPose();
        float resScale = 1.0f / this.resolutionMultiplier;
        guiGraphics.pose().scale(resScale, resScale, 1.0f);

        int scaledCanvasLeft = canvasScreenLeft * this.resolutionMultiplier;
        int scaledCanvasTop = canvasScreenTop * this.resolutionMultiplier;

        for (int x = 0; x < this.canvasWidth * this.resolutionMultiplier; x++) {
            for (int y = 0; y < this.canvasHeight * this.resolutionMultiplier; y++) {
                byte pixelValue = pixels[x][y];

                // ВАЖНО: Рисуем только в том случае, если на бумаге есть след от карандаша
                if (pixelValue > 0) {
                    // Восстанавливаем расчет координат для конкретного пикселя
                    int drawPixelX = scaledCanvasLeft + (x * this.scale);
                    int drawPixelY = scaledCanvasTop + (y * this.scale);

                    // Выбираем цвет в зависимости от плотности графита (от 1 до 4)
                    int pixelColor = 0xFF000000;

                    if (pixelValue == 1) {
                        pixelColor = 0xFFCCCCCC; // 1 слой (25% плотности)
                    } else if (pixelValue == 2) {
                        pixelColor = 0xFF888888; // 2 слоя (50% плотности)
                    } else if (pixelValue == 3) {
                        pixelColor = 0xFF444444; // 3 слоя (75% плотности)
                    } else if (pixelValue >= 4) {
                        pixelColor = 0xFF111111; // 4 слоя (100% черный)
                    }

                    // ВАЖНО: Сама команда, которая физически рисует этот пиксель на экране
                    guiGraphics.fill(drawPixelX, drawPixelY, drawPixelX + this.scale, drawPixelY + this.scale, pixelColor);
                }
            }
        }

        // --- 7. ПРЕДПРОСМОТР КИСТИ ---
        int scaledCanvasWidth = this.canvasWidth * this.scale;
        int scaledImageHeight = this.fileHeight * this.scale;

        if (!this.isDragging && !this.isRotating &&
                lMouseX >= canvasScreenLeft && lMouseX < (canvasScreenLeft + scaledCanvasWidth) &&
                lMouseY >= renderY && lMouseY < (renderY + scaledImageHeight)) {

            boolean canDraw = (this.activeTool == Tool.PENCIL && hasPencil) || (this.activeTool == Tool.ERASER && hasEraser);

            if (canDraw) {
                double physicalCellSize = (double) this.scale / this.resolutionMultiplier;
                int centerX = (int) ((lMouseX - canvasScreenLeft) / physicalCellSize);
                int centerY = (int) ((lMouseY - renderY) / physicalCellSize);

                int offset = this.brushSize / 2;
                int previewColor = (this.activeTool == Tool.ERASER) ? 0x60FF0000 : 0x60000000;

                for (int x = centerX - offset; x < centerX - offset + this.brushSize; x++) {
                    for (int y = centerY - offset; y < centerY - offset + this.brushSize; y++) {
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
        guiGraphics.pose().popPose(); // Конец скейла для пикселей

        // --- 8. НУМЕРАЦИЯ СТРАНИЦ ---
        String pageText = String.valueOf(this.currentPageIndex + 1);

        // Берем правый край бумаги (ширина * масштаб), отнимаем ширину текста и отнимаем 15 пикселей для красивого отступа от края
        int textX = renderX + (this.fileWidth * this.scale) - this.font.width(pageText) - 10;
        int textY = renderY + (this.fileHeight * this.scale) - 15; // Высота (Y) остается прежней - внизу страницы

        guiGraphics.drawString(this.font, pageText, textX, textY, 0xFF777777, false);

        guiGraphics.pose().popPose(); // КОНЕЦ БЛОКА ВРАЩЕНИЯ

        // --- 9. ИНСТРУМЕНТЫ И ИНДИКАТОРЫ ---
        ToolButtonCoords toolCoords = getToolButtonCoords();
        int scaledBtnWidth = toolCoords.scaledBtnWidth();
        int pencilX = toolCoords.pencilX();
        int eraserX = toolCoords.eraserX();
        int peekY = toolCoords.peekY();

        if (hasPencil) {
            renderToolButton(guiGraphics, mouseX, mouseY, Tool.PENCIL, PENCIL_TEX, pencilX);

            // Показывать текст ТОЛЬКО если карандаш сейчас выбран
            if (this.activeTool == Tool.PENCIL) {
                String hardnessText = "";
                int hardnessColor = 0;

                if (this.currentHardness == 1) { hardnessText = "2H"; hardnessColor = 0xFFAAAAAA; }
                else if (this.currentHardness == 2) { hardnessText = "HB"; hardnessColor = 0xFF555555; }
                else if (this.currentHardness == 3) { hardnessText = "4B"; hardnessColor = 0xFF222222; }

                int hardnessX = pencilX + (scaledBtnWidth / 2) - (this.font.width(hardnessText) / 2);
                int hardnessY = peekY - 24;

                guiGraphics.drawString(this.font, hardnessText, hardnessX, hardnessY, hardnessColor, false);
            }
        }

        if (hasEraser) {
            renderToolButton(guiGraphics, mouseX, mouseY, Tool.ERASER, ERASER_TEX, eraserX);
        }

        // Отрисовка индикаторов размера кисти (точки)
        if (this.activeTool == Tool.PENCIL && hasPencil) {
            renderSizeIndicators(guiGraphics, mouseX, mouseY, pencilX, peekY);
        } else if (this.activeTool == Tool.ERASER && hasEraser) {
            renderSizeIndicators(guiGraphics, mouseX, mouseY, eraserX, peekY);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void renderToolButton(GuiGraphics guiGraphics, int mouseX, int mouseY, Tool tool, ResourceLocation texture, int x) {
        boolean isSelected = (this.activeTool == tool);
        int scaledWidth = this.buttonWidth * this.buttonScale;
        int scaledHeight = this.buttonHeight * this.buttonScale;

        int peekY = this.height - scaledHeight;
        int baseY = this.height - (scaledHeight / 2);
        int renderY = isSelected ? peekY : baseY;

        boolean isHovered = mouseX >= x && mouseX < x + scaledWidth && mouseY >= renderY && mouseY < renderY + scaledHeight;
        float vOffset = isHovered ? this.buttonHeight : 0.0f;

        guiGraphics.blit(texture, x, renderY, scaledWidth, scaledHeight, 0.0f, vOffset, this.buttonWidth, this.buttonHeight, this.buttonWidth, this.buttonHeight * 2);
    }

    // Отрисовка индикаторов размера (лесенка: 3x3, 5x5, 7x7)
    private void renderSizeIndicators(GuiGraphics guiGraphics, int mouseX, int mouseY, int toolX, int toolY) {
        int bottomY = toolY - 4; // Отступ от верхней границы кнопки инструмента
        int[] sizes = {3, 5, 7}; // Размеры квадратов
        int[] xOffsets = {5, 11, 19}; // Смещение по X для выравнивания по центру кнопки (ширина кнопки 32)

        for (int i = 1; i <= 3; i++) {
            int size = sizes[i - 1];
            int btnX = toolX + xOffsets[i - 1];
            int btnY = bottomY - size; // Чем больше размер, тем выше он "растет" вверх

            // Активный размер светится белым, неактивные - тёмно-серые
            int color = (this.brushSize == i) ? 0xFFFFFFFF : 0xFF555555;

            // Расширенная зона для подсветки при наведении (хитбокс +2 пикселя)
            if (mouseX >= btnX - 2 && mouseX < btnX + size + 2 && mouseY >= btnY - 2 && mouseY < btnY + size + 2) {
                color = 0xFFFFFFAA;
            }

            guiGraphics.fill(btnX, btnY, btnX + size, btnY + size, color);
        }
    }

    // Обработка клика по индикаторам
    private boolean handleSizeIndicatorClick(double mouseX, double mouseY, int toolX, int toolY) {
        int bottomY = toolY - 4;
        int[] sizes = {3, 5, 7};
        int[] xOffsets = {5, 11, 19};

        for (int i = 1; i <= 3; i++) {
            int size = sizes[i - 1];
            int btnX = toolX + xOffsets[i - 1];
            int btnY = bottomY - size;

            // Проверяем клик с учетом расширенного хитбокса
            if (mouseX >= btnX - 2 && mouseX < btnX + size + 2 && mouseY >= btnY - 2 && mouseY < btnY + size + 2) {
                this.brushSize = i; // Меняем толщину кисти
                return true;
            }
        }
        return false;
    }

    private void drawPixel(double lMouseX, double lMouseY, boolean isEraser) {
        int canvasScreenLeft = (int) this.exactGuiLeft + ((this.frameWidth + this.deadZoneWidth) * this.scale);
        int canvasScreenTop = (int) this.exactGuiTop;

        double physicalCellSize = (double) this.scale / this.resolutionMultiplier;
        int centerX = (int) ((lMouseX - canvasScreenLeft) / physicalCellSize);
        int centerY = (int) ((lMouseY - canvasScreenTop) / physicalCellSize);

        int offset = this.brushSize / 2;

        for (int x = centerX - offset; x < centerX - offset + this.brushSize; x++) {
            for (int y = centerY - offset; y < centerY - offset + this.brushSize; y++) {
                if (x >= 0 && x < this.canvasWidth * this.resolutionMultiplier &&
                        y >= 0 && y < this.canvasHeight * this.resolutionMultiplier) {

                    // Если мы используем ластик
                    if (isEraser) {
                        if (pixels[x][y] != 0) {
                            pixels[x][y] = 0;
                            this.eraserPixelsUsed++;
                        }
                    } else {
                        // Инициализируем память штриха, если она пустая
                        if (this.strokePixels == null) {
                            this.strokePixels = new boolean[this.canvasWidth * this.resolutionMultiplier][this.canvasHeight * this.resolutionMultiplier];
                        }

                        // --- ПРАВИЛЬНОЕ НАСЛОЕНИЕ ---
                        // Проверяем только то, не красили ли мы этот пиксель в ТЕКУЩЕМ штрихе
                        if (!this.strokePixels[x][y]) {
                            int increment = (this.currentHardness == 3) ? 4 : this.currentHardness;

                            if (pixels[x][y] < 4) {
                                int newValue = pixels[x][y] + increment;
                                // Прибавляем цвет. Math.min гарантирует, что мы не уйдем выше 4 (черный цвет)
                                pixels[x][y] = (byte) Math.min(4, newValue);
                                this.pencilPixelsUsed++;
                            }

                            // Запоминаем, что в этом движении мыши мы пиксель уже затемнили
                            this.strokePixels[x][y] = true;
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

        ToolButtonCoords toolCoords = getToolButtonCoords();
        int scaledBtnWidth = toolCoords.scaledBtnWidth();
        int scaledBtnHeight = toolCoords.scaledBtnHeight();
        int pencilX = toolCoords.pencilX();
        int peekY = toolCoords.peekY();
        int eraserX = toolCoords.eraserX;

        // 1. Проверяем клики по инструментам интерфейса (только ЛКМ)
        if (button == 0) {

            int baseY = this.height - (scaledBtnHeight / 2);

            int pencilY = (this.activeTool == Tool.PENCIL) ? peekY : baseY;
            int eraserY = (this.activeTool == Tool.ERASER) ? peekY : baseY;

            if (hasPencil && mouseX >= pencilX && mouseX < pencilX + scaledBtnWidth && mouseY >= pencilY && mouseY < pencilY + scaledBtnHeight) {
                this.activeTool = Tool.PENCIL;
                return true;
            }
            if (hasEraser && mouseX >= eraserX && mouseX < eraserX + scaledBtnWidth && mouseY >= eraserY && mouseY < eraserY + scaledBtnHeight) {
                this.activeTool = Tool.ERASER;
                return true;
            }

            // --- Проверка клика по индикаторам размера активного инструмента ---
            if (this.activeTool == Tool.PENCIL && hasPencil) {
                if (handleSizeIndicatorClick(mouseX, mouseY, pencilX, peekY)) return true;
            } else if (this.activeTool == Tool.ERASER && hasEraser) {
                if (handleSizeIndicatorClick(mouseX, mouseY, eraserX, peekY)) return true;
            }
        }

        // 2. Получаем логические координаты для холста и кнопок на нём
        double[] logicalMouse = getLogicalMouse(mouseX, mouseY);
        double lMouseX = logicalMouse[0];
        double lMouseY = logicalMouse[1];

        int renderX = (int) this.exactGuiLeft;
        int renderY = (int) this.exactGuiTop;
        int drawWidth = this.fileWidth * this.scale;

        // --- ДОБАВЛЕНО: Читаем текущие страницы ОДИН РАЗ для всех проверок ниже ---
        net.minecraft.world.item.ItemStack stack = this.minecraft.player.getMainHandItem();
        if (!stack.is(ModItems.SKETCHBOOK.get())) {
            stack = this.minecraft.player.getOffhandItem();
        }
        java.util.List<net.avizvul.esquissemod.component.SketchData> pages =
                new java.util.ArrayList<>(stack.getOrDefault(ModDataComponents.SKETCHBOOK_PAGES.get(), new java.util.ArrayList<>()));
        // -------------------------------------------------------------------------

        // Расчет координат синей зоны
        int blueZoneWidth = this.deadZoneWidth * this.scale;
        int blueZoneLeft = renderX + (this.frameWidth * this.scale);
        int blueZoneRight = blueZoneLeft + blueZoneWidth;
        int blueZoneTop = renderY;
        int blueZoneBottom = renderY + (this.canvasHeight * this.scale);


        // --- ЛОГИКА ОТРЫВА СТРАНИЦЫ ---
        if (button == 0 && lMouseX >= blueZoneLeft && lMouseX <= blueZoneRight && lMouseY >= blueZoneTop && lMouseY <= blueZoneBottom) {

            // --- ИСПРАВЛЕНИЕ 1: Оборачиваем сырой массив пикселей в объект SketchData ---
            net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                    new SketchbookSavePayload(
                            this.currentPageIndex,                                                // 1: int (индекс страницы)
                            net.avizvul.esquissemod.component.SketchData.fromArray(this.pixels),  // 2: SketchData (сам рисунок)
                            this.pencilPixelsUsed,                                             // 3: int (ваша переменная пикселей карандаша)
                            this.eraserPixelsUsed                                              // 4: int (ваша переменная пикселей ластика)
                    )
            );

            // Отправляем пакет на отрыв (сервер уже будет знать о нашем рисунке!)
            net.neoforged.neoforge.network.PacketDistributor.sendToServer(new TearPagePayload(this.currentPageIndex));

            // --- ИСПРАВЛЕНИЕ 2: Оставлено строго ОДНО удаление страницы ---
            // Локально удаляем страницу из списка на клиенте, чтобы не было рассинхрона
            if (this.currentPageIndex >= 0 && this.currentPageIndex < pages.size()) {
                pages.remove(this.currentPageIndex);
            }

            if (pages.isEmpty()) {
                // Если мы оторвали самую последнюю существующую страницу — закрываем скетчбук
                this.onClose();
            } else {
                // Корректируем индекс, если мы оторвали страницу в самом конце
                if (this.currentPageIndex >= pages.size()) {
                    this.currentPageIndex = pages.size() - 1;
                }

                // ВАЖНО: сохраняем уменьшенный список обратно в предмет на клиенте
                stack.set(ModDataComponents.SKETCHBOOK_PAGES.get(), pages);

                // Подгружаем на холст ту страницу, которая теперь находится под этим индексом
                int w = this.canvasWidth * this.resolutionMultiplier;
                int h = this.canvasHeight * this.resolutionMultiplier;
                this.pixels = pages.get(this.currentPageIndex).toArray(w, h);
            }

            return true;
        }

        // 3. Проверка клика по кнопке поворота (и ЛКМ, и ПКМ)
        int btnFileWidth = 8;
        int btnFileHeight = 8;
        int btnX = renderX;
        int btnY = renderY + ((this.fileHeight - btnFileHeight) / 2) * this.scale;

        if (lMouseX >= btnX && lMouseX < btnX + (btnFileWidth * this.scale) &&
                lMouseY >= btnY && lMouseY < btnY + (btnFileHeight * this.scale)) {

            if (button == 0) {
                this.isRotating = true; // ЛКМ - Вращать
                return true;
            } else if (button == 1) { // ПКМ - Сброс угла
                this.rotationAngle = 0.0f;
                clampSketchbook();
                return true;
            }
        }

        // 4. Логика, работающая только на ЛКМ (перетаскивание и рисование)
        if (button == 0) {
            int scaledFrameWidth = this.frameWidth * this.scale;
            int scaledImageHeight = this.fileHeight * this.scale;

            // --- ЗАКЛАДКИ ПОД БЛОКНОТОМ ---
            int scaledTabWidth = this.tabWidth * this.tabScale;
            int scaledTabHeight = this.tabHeight * this.tabScale;

            // Вызываем наш метод и получаем готовые координаты
            TabCoords coords = getTabCoords(renderX, renderY, drawWidth);
            int tabX = coords.tabX();
            int backTabY = coords.backTabY();
            int forwardTabY = coords.forwardTabY();

            // Клик "Назад"
            if (this.currentPageIndex > 0 &&
                    lMouseX >= tabX && lMouseX < tabX + scaledTabWidth &&
                    lMouseY >= backTabY && lMouseY < backTabY + scaledTabHeight) {

                // ИСПРАВЛЕНО: передаем число (индекс предыдущей страницы)
                turnPage(this.currentPageIndex - 1);
                return true;
            }

            // Клик "Вперед"
            if (this.currentPageIndex < pages.size() - 1 &&
                    lMouseX >= tabX && lMouseX < tabX + scaledTabWidth &&
                    lMouseY >= forwardTabY && lMouseY < forwardTabY + scaledTabHeight) {

                // ИСПРАВЛЕНО: передаем число (индекс следующей страницы)
                turnPage(this.currentPageIndex + 1);
                return true;
            }

            // Проверка перетаскивания (рамка)
            if (lMouseX >= renderX && lMouseX < (renderX + scaledFrameWidth)
                    && lMouseY >= renderY && lMouseY < (renderY + scaledImageHeight)) {
                this.isDragging = true;
                return true;
            } else {
                // Проверка рисования (холст)
                int canvasScreenLeft = renderX + ((this.frameWidth + this.deadZoneWidth) * this.scale);
                int scaledCanvasWidth = this.canvasWidth * this.scale;

                if (lMouseX >= canvasScreenLeft && lMouseX < (canvasScreenLeft + scaledCanvasWidth)
                        && lMouseY >= renderY && lMouseY < (renderY + scaledImageHeight)) {
                    if (this.activeTool == Tool.PENCIL && hasPencil) {
                        this.isDrawing = true;
                        drawPixel(lMouseX, lMouseY, false);
                    } else if (this.activeTool == Tool.ERASER && hasEraser) {
                        this.isErasing = true;
                        drawPixel(lMouseX, lMouseY, true);
                    }
                    return true;
                }
            }
        }

        // --- НОВОЕ: Обработка ПРАВОГО клика (ПКМ) по инструментам ---
        if (button == 1) {
            if (hasTool(ModItems.PENCIL.get()) &&
                    mouseX >= pencilX && mouseX < pencilX + scaledBtnWidth &&
                    mouseY >= peekY && mouseY < peekY + scaledBtnHeight) {

                // Меняем твердость по кругу: 1 -> 2 -> 3 -> 1
                this.currentHardness++;
                if (this.currentHardness > 3) {
                    this.currentHardness = 1;
                }
                return true; // Прерываем дальнейшую проверку клика
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.isRotating) {
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
        } else if (this.isDrawing) {
            double[] lMouse = getLogicalMouse(mouseX, mouseY);
            drawPixel(lMouse[0], lMouse[1], false);
            return true;
        } else if (this.isErasing) {
            double[] lMouse = getLogicalMouse(mouseX, mouseY);
            drawPixel(lMouse[0], lMouse[1], true);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            // При отпускании левой кнопки мыши (0) мы обнуляем память о штрихе
            if (this.strokePixels != null) {
                this.strokePixels = new boolean[this.canvasWidth * this.resolutionMultiplier][this.canvasHeight * this.resolutionMultiplier];
            }

            if (this.isRotating) { this.isRotating = false; return true; }
            if (this.isDragging) { this.isDragging = false; return true; }
            if (this.isDrawing) { this.isDrawing = false; return true; }
            if (this.isErasing) { this.isErasing = false; return true; }
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        // Проверяем, зажат ли Shift
        if (Screen.hasShiftDown()) {
            // Только для карандаша меняем твердость
            if (this.activeTool == Tool.PENCIL && hasTool(ModItems.PENCIL.get())) {
                if (scrollY > 0) { // Скролл ВВЕРХ (увеличиваем твердость)
                    this.currentHardness++;
                    if (this.currentHardness > 3) this.currentHardness = 3; // Ограничитель
                } else if (scrollY < 0) { // Скролл ВНИЗ (уменьшаем твердость)
                    this.currentHardness--;
                    if (this.currentHardness < 1) this.currentHardness = 1; // Ограничитель
                }
                return true;
            }
        } else {
            // Старое поведение: меняем размер кисти
            if (scrollY > 0) {
                this.brushSize = Math.min(3, this.brushSize + 1);
                return true;
            } else if (scrollY < 0) {
                this.brushSize = Math.max(1, this.brushSize - 1);
                return true;
            }
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean hasPencil = hasTool(ModItems.PENCIL.get());
        boolean hasEraser = hasTool(ModItems.ERASER.get());

        if (keyCode == GLFW.GLFW_KEY_B && hasPencil) {
            this.activeTool = Tool.PENCIL;
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_E && hasEraser) {
            this.activeTool = Tool.ERASER;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        savedGuiLeft = this.exactGuiLeft;
        savedGuiTop = this.exactGuiTop;
        savedRotationAngle = this.rotationAngle;

        // ЭТУ СТРОКУ УДАЛЯЕМ: savedPageIndex = this.currentPageIndex;

        hasSavedState = true;

        SketchData data = SketchData.fromArray(this.pixels);
        // Вы уже правильно передаете this.currentPageIndex в пакет!
        PacketDistributor.sendToServer(new SketchbookSavePayload(this.currentPageIndex, data, this.pencilPixelsUsed, this.eraserPixelsUsed));
        super.onClose();
    }
}