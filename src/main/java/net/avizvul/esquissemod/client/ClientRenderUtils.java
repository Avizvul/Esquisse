package net.avizvul.esquissemod.client;

import net.minecraft.client.gui.GuiGraphics;

public class ClientRenderUtils {

    /**
     * Универсальный метод для отрисовки массива пикселей на экране.
     */
    public static void renderSketchPixels(GuiGraphics guiGraphics, int[][] pixels, int startX, int startY, int scale) {
        if (pixels == null || pixels.length == 0) return;

        int width = pixels.length;
        int height = pixels.length;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pixelColor = pixels[x][y];

                // Если цвет не прозрачный (0)
                if (pixelColor != 0) {
                    int drawX = startX + (x * scale);
                    int drawY = startY + (y * scale);

                    guiGraphics.fill(drawX, drawY, drawX + scale, drawY + scale, pixelColor);
                }
            }
        }
    }
}
