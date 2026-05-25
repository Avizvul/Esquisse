package net.avizvul.esquissemod.component;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;

public class SketchData {
    private final byte[][] pixels;

    // Кодек для сохранения рисунка в файлы сохранения мира (на диск)
    public static final com.mojang.serialization.Codec<SketchData> CODEC =
            com.mojang.serialization.Codec.STRING.xmap(
                    // ЧТЕНИЕ: декодируем из Base64 обратно в 2D массив
                    str -> {
                        byte[] data = java.util.Base64.getDecoder().decode(str);

                        // ВАЖНО: Замените 64 и 128 на ВАШИ реальные размеры холста (с учетом resolutionMultiplier)!
                        int w = 126;
                        int h = 192;
                        byte[][] pixels2D = new byte[w][h];
                        for (int i = 0; i < data.length; i++) {
                            pixels2D[i % w][i / w] = data[i];
                        }
                        return SketchData.fromArray(pixels2D); // Ваш метод создания SketchData
                    },
                    // СОХРАНЕНИЕ: кодируем 2D массив в строку Base64
                    sketchData -> {
                        int w = 126; // Замените на ширину
                        int h = 192; // Замените на высоту
                        byte[][] pixels2D = sketchData.toArray(w, h);
                        byte[] data = new byte[w * h];
                        for (int x = 0; x < w; x++) {
                            for (int y = 0; y < h; y++) {
                                data[y * w + x] = pixels2D[x][y];
                            }
                        }
                        return java.util.Base64.getEncoder().encodeToString(data);
                    }
            );

    // StreamCodec для передачи рисунка по сети от сервера к клиенту (чтобы другие игроки тоже его видели)
    public static final net.minecraft.network.codec.StreamCodec<io.netty.buffer.ByteBuf, SketchData> STREAM_CODEC =
            net.minecraft.network.codec.StreamCodec.composite(
                    net.minecraft.network.codec.ByteBufCodecs.STRING_UTF8,
                    // Для отправки - превращаем в строку Base64
                    sketchData -> {
                        int w = 126; // Ширина
                        int h = 192; // Высота
                        byte[][] pixels2D = sketchData.toArray(w, h);
                        byte[] data = new byte[w * h];
                        for (int x = 0; x < w; x++) {
                            for (int y = 0; y < h; y++) {
                                data[y * w + x] = pixels2D[x][y];
                            }
                        }
                        return java.util.Base64.getEncoder().encodeToString(data);
                    },
                    // Для получения - расшифровываем обратно
                    str -> {
                        byte[] data = java.util.Base64.getDecoder().decode(str);
                        int w = 126;
                        int h = 192;
                        byte[][] pixels2D = new byte[w][h];
                        for (int i = 0; i < data.length; i++) {
                            pixels2D[i % w][i / w] = data[i];
                        }
                        return SketchData.fromArray(pixels2D);
                    }
            );

    private SketchData(byte[][] pixels) {
        this.pixels = pixels;
    }

    public byte[][] getRawPixels() {
        return this.pixels;
    }

    // Возвращает копию массива нужного размера
    public byte[][] toArray(int targetWidth, int targetHeight) {
        byte[][] result = new byte[targetWidth][targetHeight];
        // Ширина - это базовая длина массива
        int copyWidth = Math.min(targetWidth, this.pixels.length);
        if (copyWidth > 0) {
            // ПРАВИЛЬНО: Высота - это длина ВЛОЖЕННОГО массива (с индексом ноль)
            int copyHeight = Math.min(targetHeight, this.pixels[0] . length);
            for (int x = 0; x < copyWidth; x++) {
                System.arraycopy(this.pixels[x], 0, result[x], 0, copyHeight);
            }
        }
        return result;
    }

    // Создает объект из существующего массива
    public static SketchData fromArray(byte[][] arr) {
        // ПРАВИЛЬНО: Ширина - это базовая длина массива
        int w = arr.length;
        if (w == 0) return new SketchData(new byte[0][0]);

        // ПРАВИЛЬНО: Высота - это длина ВЛОЖЕННОГО массива (с индексом ноль)
        int h = arr [ 0 ] . length;

        byte[][] copy = new byte[w][h];
        for (int x = 0; x < w; x++) {
            System.arraycopy(arr[x], 0, copy[x], 0, h);
        }
        return new SketchData(copy);
    }

    public boolean isEmpty() {
        // Если ваш массив пикселей хранится в виде byte[][] pixels:
        for (byte[] row : this.pixels) {
            for (byte p : row) {
                if (p != 0) { // Если найден хоть один закрашенный пиксель
                    return false;
                }
            }
        }
        return true; // Все пиксели равны 0 (пусто)
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SketchData that = (SketchData) obj;
        // Глубокое сравнение двумерного массива пикселей
        return java.util.Arrays.deepEquals(this.pixels, that.pixels);
    }

    @Override
    public int hashCode() {
        // Глубокий хэш для двумерного массива
        return java.util.Arrays.deepHashCode(this.pixels);
    }
}