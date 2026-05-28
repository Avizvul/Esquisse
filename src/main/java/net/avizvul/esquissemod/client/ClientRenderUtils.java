package net.avizvul.esquissemod.client;

import net.minecraft.client.gui.GuiGraphics;

public class ClientRenderUtils {

    /**
     * Универсальный метод для отрисовки массива пикселей на экране.
     */
    public static void renderSketchPixels(net.minecraft.client.gui.GuiGraphics guiGraphics, int[][] pixels, int startX, int startY, int scale) {
        if (pixels == null || pixels.length == 0) return;
        int width = pixels.length;
        int height = pixels[0].length;

        // ВАЖНО: Сначала идем по Y (сверху вниз), а внутри по X (слева направо)
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixelColor = pixels[x][y];
                if (pixelColor != 0) {
                    int startPixelX = x;

                    // "Жадное" объединение: пока следующий пиксель имеет ТАКОЙ ЖЕ цвет, расширяем нашу линию
                    while (x + 1 < width && pixels[x + 1][y] == pixelColor) {
                        x++;
                    }

                    int drawX = startX + (startPixelX * scale);
                    int drawY = startY + (y * scale);
                    // Ширина итогового отрезка (от startPixelX до сдвинутого x)
                    int segmentWidth = (x - startPixelX + 1) * scale;

                    guiGraphics.fill(drawX, drawY, drawX + segmentWidth, drawY + scale, pixelColor);
                }
            }
        }
    }

}
