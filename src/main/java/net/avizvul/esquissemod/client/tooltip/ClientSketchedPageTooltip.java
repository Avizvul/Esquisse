package net.avizvul.esquissemod.client.tooltip;

import net.avizvul.esquissemod.component.SketchData;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

public class ClientSketchedPageTooltip implements ClientTooltipComponent {

    private static final net.minecraft.resources.ResourceLocation PAGE_TEX =
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(net.avizvul.esquissemod.EsquisseMod.MOD_ID, "textures/gui/sketched_page_gui.png");

    private final int canvasWidth = 63;
    private final int canvasHeight = 96;
    private final int scale = 1; // Масштаб 1 для миниатюры

    // НОВОЕ: Храним только объект SketchData, массив пикселей нам больше не нужен
    private final SketchData data;

    public ClientSketchedPageTooltip(SketchedPageTooltipData tooltipData) {
        // Извлекаем рисунок напрямую из переданных данных тултипа
        this.data = tooltipData.sketchData();
    }

    @Override
    public int getHeight() {
        return this.canvasHeight * this.scale;
    }

    @Override
    public int getWidth(Font font) {
        return this.canvasWidth * this.scale;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
        int drawWidth = this.canvasWidth * this.scale;
        int drawHeight = this.canvasHeight * this.scale;

        // 1. Отрисовка текстуры самой бумаги
        guiGraphics.blit(PAGE_TEX, x, y, drawWidth, drawHeight, 0.0f, 0.0f, this.canvasWidth, this.canvasHeight, this.canvasWidth, this.canvasHeight);

        // 2. Отрисовка рисунка через кэшированную текстуру видеокарты
        if (this.data != null && !this.data.isEmpty()) {
            com.mojang.blaze3d.systems.RenderSystem.enableBlend();
            net.avizvul.esquissemod.client.ClientRenderUtils.renderCachedSketch(guiGraphics, this.data, x, y, drawWidth, drawHeight);
            com.mojang.blaze3d.systems.RenderSystem.disableBlend();
        }
    }
}
