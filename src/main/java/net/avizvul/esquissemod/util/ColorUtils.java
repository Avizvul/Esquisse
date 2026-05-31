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

        // 1. Прямое (линейное) сложение непрозрачности с лимитом в 255 (100%)
        int outA = Math.min(255, bgA + fgA);
        if (outA == 0) return 0;

        // 2. Вычисляем "вес" старого цвета для правильного смешивания RGB
        int bgWeight = outA - fgA;

        // 3. Пропорционально смешиваем цвета в зависимости от их плотности
        int outR = (fgR * fgA + bgR * bgWeight) / outA;
        int outG = (fgG * fgA + bgG * bgWeight) / outA;
        int outB = (fgB * fgA + bgB * bgWeight) / outA;

        return (outA << 24) | (outR << 16) | (outG << 8) | outB;
    }

    /**
     * Линейная интерполяция (Lerp) между двумя ARGB цветами.
     * @param dest Цвет назначения (куда сдвигаем)
     * @param source Взятый цвет (откуда сдвигаем)
     * @param t Коэффициент смешивания (0.0 = только dest, 1.0 = только source)
     * @return Смешанный ARGB цвет
     */
    public static int lerpColor(int dest, int source, float t) {
        int dA = (dest >> 24) & 0xFF;
        int dR = (dest >> 16) & 0xFF;
        int dG = (dest >> 8) & 0xFF;
        int dB = dest & 0xFF;

        int sA = (source >> 24) & 0xFF;
        int sR = (source >> 16) & 0xFF;
        int sG = (source >> 8) & 0xFF;
        int sB = source & 0xFF;

        // Применяем формулу: Dest + (Source - Dest) * T для каждого канала
        int nA = (int) (dA + (sA - dA) * t);
        int nR = (int) (dR + (sR - dR) * t);
        int nG = (int) (dG + (sG - dG) * t);
        int nB = (int) (dB + (sB - dB) * t);

        return (nA << 24) | (nR << 16) | (nG << 8) | nB;
    }
}