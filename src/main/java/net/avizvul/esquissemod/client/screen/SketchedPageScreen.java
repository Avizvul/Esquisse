package net.avizvul.esquissemod.client.screen;

import net.avizvul.esquissemod.component.ModDataComponents;
import net.avizvul.esquissemod.component.SketchData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class SketchedPageScreen extends Screen {

    private static final net.minecraft.resources.ResourceLocation PAGE_TEX =
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(net.avizvul.esquissemod.EsquisseMod.MOD_ID,
                    "textures/gui/sketched_page_gui.png"
            );

    // --- ПРАВИЛЬНЫЕ РАЗМЕРЫ (Как в SketchbookScreen) ---
    private final int canvasWidth = 63;
    private final int canvasHeight = 96; // Высота должна быть больше ширины!
    private final int scale = 3;
    private final int resolutionMultiplier = 2;

    private int[][] pixels;

    public SketchedPageScreen(ItemStack stack) {
        super(Component.literal("Sketched Page"));

        // 1. ИЗВЛЕКАЕМ РИСУНОК ИЗ ПРЕДМЕТА
        SketchData data = stack.get(ModDataComponents.PAGE_DATA.get());
        if (data != null) {
            // Обязательно передаем правильные прямоугольные размеры!
            this.pixels = data.toArray(this.canvasWidth * this.resolutionMultiplier, this.canvasHeight * this.resolutionMultiplier);
        } else {
            this.pixels = new int[this.canvasWidth * this.resolutionMultiplier][this.canvasHeight * this.resolutionMultiplier];
        }
    }

    // --- РЕШЕНИЕ ПРОБЛЕМЫ 1: Убираем блюр и паузу ---
    @Override
    public boolean isPauseScreen() {
        return false; // Чтобы одиночная игра не ставилась на паузу
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // ВАЖНО: Оставляем этот метод АБСОЛЮТНО ПУСТЫМ!
        // Мы не вызываем super.renderBackground(...), благодаря чему
        // ванильный блюр и темный полупрозрачный фон не будут отрисовываться.
    }

    // --- РЕШЕНИЕ ПРОБЛЕМ 2 И 3: Правильные пропорции и циклы ---
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Рассчитываем физические размеры листа на экране (189x288 при scale=3)
        int drawWidth = this.canvasWidth * this.scale;
        int drawHeight = this.canvasHeight * this.scale;

        // Центрируем лист ровно по центру экрана
        int renderX = (this.width - drawWidth) / 2;
        int renderY = (this.height - drawHeight) / 2;

        // 1. Отрисовка фона самой бумаги
        // Используем вашу новую текстуру вместо сплошной заливки
        guiGraphics.blit(PAGE_TEX, renderX, renderY, drawWidth, drawHeight,
                0.0f, 0.0f, this.canvasWidth, this.canvasHeight, this.canvasWidth, this.canvasHeight
        );

        // 2. Отрисовка пикселей рисунка
        guiGraphics.pose().pushPose();
        float resScale = 1.0f / this.resolutionMultiplier;
        guiGraphics.pose().scale(resScale, resScale, 1.0f);

        int scaledCanvasLeft = renderX * this.resolutionMultiplier;
        int scaledCanvasTop = renderY * this.resolutionMultiplier;

        net.avizvul.esquissemod.client.ClientRenderUtils.renderSketchPixels(guiGraphics, this.pixels, scaledCanvasLeft, scaledCanvasTop, this.scale);

        guiGraphics.pose().popPose();

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
}