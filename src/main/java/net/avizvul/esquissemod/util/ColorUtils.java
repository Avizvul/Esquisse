package net.avizvul.esquissemod.util;

public class ColorUtils {

    /**
     * Смешивает новый полупрозрачный цвет с фоновым пикселем (Alpha Blending).
     * @param bg Фоновый цвет (уже нарисованный пиксель) в формате ARGB
     * @param fg Новый цвет (карандаш) в формате ARGB
     * @return Смешанный цвет в формате ARGB
     */
    public static int blendColors(int bg, int fg) {
        if (bg == 0) return fg; // Если фона нет, просто кладем чистый цвет

        int fgA = (fg >> 24) & 0xFF;
        int fgR = (fg >> 16) & 0xFF;
        int fgG = (fg >> 8) & 0xFF;
        int fgB = fg & 0xFF;

        int bgA = (bg >> 24) & 0xFF;
        int bgR = (bg >> 16) & 0xFF;
        int bgG = (bg >> 8) & 0xFF;
        int bgB = bg & 0xFF;

        int outA = fgA + (bgA * (255 - fgA) / 255);
        if (outA == 0) return 0;

        int outR = (fgR * fgA + bgR * bgA * (255 - fgA) / 255) / outA;
        int outG = (fgG * fgA + bgG * bgA * (255 - fgA) / 255) / outA;
        int outB = (fgB * fgA + bgB * bgA * (255 - fgA) / 255) / outA;

        return (outA << 24) | (outR << 16) | (outG << 8) | outB;
    }
}