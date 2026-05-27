package net.avizvul.esquissemod.client.tooltip;

import net.avizvul.esquissemod.component.SketchData;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

public class ClientSketchedPageTooltip implements ClientTooltipComponent {

    // --- 1. ДОБАВЛЯЕМ ССЫЛКУ НА ВАШУ ТЕКСТУРУ ---
    private static final net.minecraft.resources.ResourceLocation PAGE_TEX =
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(net.avizvul.esquissemod.EsquisseMod.MOD_ID, "textures/gui/sketched_page_gui.png");

    private final int[][] pixels;
    private final int canvasWidth = 63;
    private final int canvasHeight = 96;
    private final int scale = 1; // Масштаб 1 для миниатюры
    private final int resolutionMultiplier = 2;

    public ClientSketchedPageTooltip(SketchedPageTooltipData data) {
        this.pixels = data.sketchData().toArray(this.canvasWidth * this.resolutionMultiplier, this.canvasHeight * this.resolutionMultiplier);
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

        // --- 2. МЕНЯЕМ ЗАЛИВКУ НА ОТРИСОВКУ ТЕКСТУРЫ ---
        // Было: guiGraphics.fill(x, y, x + drawWidth, y + drawHeight, 0xFFFDF6E3);
        guiGraphics.blit(PAGE_TEX, x, y, drawWidth, drawHeight, 0.0f, 0.0f, this.canvasWidth, this.canvasHeight, this.canvasWidth, this.canvasHeight);

        guiGraphics.pose().pushPose();

        guiGraphics.pose().translate(x, y, 0);

        float resScale = 1.0f / this.resolutionMultiplier;
        guiGraphics.pose().scale(resScale, resScale, 1.0f);

        for (int px = 0; px < this.canvasWidth * this.resolutionMultiplier; px++) {
            for (int py = 0; py < this.canvasHeight * this.resolutionMultiplier; py++) {
                int pixelValue = this.pixels[px][py];

                if (pixelValue > 0) {
                    int drawPixelX = px * this.scale;
                    int drawPixelY = py * this.scale;

                    int pixelColor = 0xFF000000;
                    if (pixelValue == 1) pixelColor = 0xFFCCCCCC;
                    else if (pixelValue == 2) pixelColor = 0xFF888888;
                    else if (pixelValue == 3) pixelColor = 0xFF444444;
                    else if (pixelValue >= 4) pixelColor = 0xFF111111;

                    guiGraphics.fill(drawPixelX, drawPixelY, drawPixelX + this.scale, drawPixelY + this.scale, pixelColor);
                }
            }
        }
        guiGraphics.pose().popPose();
    }
}