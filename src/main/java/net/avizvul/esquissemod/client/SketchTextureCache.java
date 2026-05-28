package net.avizvul.esquissemod.client;

import net.avizvul.esquissemod.component.SketchData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.platform.NativeImage;

import java.util.LinkedHashMap;
import java.util.Map;

public class SketchTextureCache {
    // Кэш на 50 текстур, чтобы видеопамять не переполнялась
    private static final int MAX_CACHE_SIZE = 50;
    private static final Map<Integer, ResourceLocation> CACHE = new LinkedHashMap<>(MAX_CACHE_SIZE + 1, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Integer, ResourceLocation> eldest) {
            if (size() > MAX_CACHE_SIZE) {
                // Удаляем самую старую текстуру из видеокарты
                Minecraft.getInstance().getTextureManager().release(eldest.getValue());
                return true;
            }
            return false;
        }
    };

    public static ResourceLocation getOrCreateTexture(SketchData data) {
        if (data == null || data.isEmpty()) return null;

        int hash = data.hashCode();
        if (CACHE.containsKey(hash)) {
            return CACHE.get(hash); // Если текстура уже сгенерирована, возвращаем её
        }

        int[][] pixels = data.getRawPixels();
        int width = pixels.length;
        if (width == 0) return null;

        // ИСПРАВЛЕНИЕ: Берем pixels.length для правильной высоты (192 вместо 126)
        int height = pixels[0].length;

        NativeImage image = new NativeImage(width, height, true);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int argb = pixels[x][y];
                if (argb != 0) {
                    // Переводим ARGB в ABGR (формат, который требует NativeImage)
                    int a = (argb >> 24) & 0xFF;
                    int r = (argb >> 16) & 0xFF;
                    int g = (argb >> 8) & 0xFF;
                    int b = argb & 0xFF;
                    image.setPixelRGBA(x, y, (a << 24) | (b << 16) | (g << 8) | r);
                }
            }
        }

        DynamicTexture texture = new DynamicTexture(image);

        // ИСПРАВЛЕНИЕ: Используем встроенный генератор ID Майнкрафта
        // Он создаст безопасный путь вида minecraft:dynamic/sketch_cache_...
        ResourceLocation id = Minecraft.getInstance().getTextureManager().register("sketch_cache", texture);

        CACHE.put(hash, id);
        return id;
    }
}
