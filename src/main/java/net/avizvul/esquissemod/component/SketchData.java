package net.avizvul.esquissemod.component;

import com.mojang.serialization.Codec;
import java.nio.ByteBuffer;

public class SketchData {
    private final int[][] pixels;
    private final int cachedHashCode; // НОВОЕ: сохраняем хэш-код здесь

    public static final Codec<SketchData> CODEC = Codec.INT_STREAM.xmap(
            stream -> {
                int[] arr = stream.toArray();
                int w = 126;
                int h = 192;
                int[][] pixels2D = new int[w][h];
                if (arr.length == w * h) {
                    for (int x = 0; x < w; x++) {
                        System.arraycopy(arr, x * h, pixels2D[x], 0, h);
                    }
                }
                return SketchData.fromArray(pixels2D);
            },
            data -> {
                int w = 126;
                int h = 192;
                int[][] pixels2D = data.toArray(w, h);
                int[] arr = new int[w * h];
                for (int x = 0; x < w; x++) {
                    System.arraycopy(pixels2D[x], 0, arr, x * h, h);
                }
                return java.util.Arrays.stream(arr);
            }
    );

    // --- ОПТИМАЛЬНЫЙ СЕТЕВОЙ КОДЕК (Сжатие пустых пикселей) ---
    public static final net.minecraft.network.codec.StreamCodec<io.netty.buffer.ByteBuf, SketchData> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of(
            (buf, data) -> {
                int w = 126;
                int h = 192;
                int[][] pixels2D = data.toArray(w, h);

                // 1. Считаем количество непустых пикселей
                int coloredPixels = 0;
                for (int x = 0; x < w; x++) {
                    for (int y = 0; y < h; y++) {
                        if (pixels2D[x][y] != 0) coloredPixels++;
                    }
                }

                // 2. Записываем их количество
                buf.writeInt(coloredPixels);

                // 3. Записываем координаты и цвет только закрашенных точек
                for (int x = 0; x < w; x++) {
                    for (int y = 0; y < h; y++) {
                        if (pixels2D[x][y] != 0) {
                            buf.writeShort(x);
                            buf.writeShort(y);
                            buf.writeInt(pixels2D[x][y]);
                        }
                    }
                }
            },
            buf -> {
                int w = 126;
                int h = 192;
                int[][] pixels2D = new int[w][h];

                // 1. Читаем количество закрашенных точек
                int coloredPixels = buf.readInt();

                // 2. Восстанавливаем их на холсте
                for (int i = 0; i < coloredPixels; i++) {
                    int x = buf.readShort();
                    int y = buf.readShort();
                    int color = buf.readInt();

                    if (x >= 0 && x < w && y >= 0 && y < h) {
                        pixels2D[x][y] = color;
                    }
                }
                return SketchData.fromArray(pixels2D);
            }
    );

    private SketchData(int[][] pixels) {
        this.pixels = pixels;
        // Считаем этот огромный массив ТОЛЬКО один раз в момент создания страницы:
        this.cachedHashCode = java.util.Arrays.deepHashCode(this.pixels);
    }

    public int[][] getRawPixels() {
        return this.pixels;
    }

    public int[][] toArray(int targetWidth, int targetHeight) {
        int[][] result = new int[targetWidth][targetHeight];
        int copyWidth = Math.min(targetWidth, this.pixels.length);
        if (copyWidth > 0) {
            int copyHeight = Math.min(targetHeight, this.pixels[0].length);
            for (int x = 0; x < copyWidth; x++) {
                System.arraycopy(this.pixels[x], 0, result[x], 0, copyHeight);
            }
        }
        return result;
    }

    public static SketchData fromArray(int[][] arr) {
        int w = arr.length;
        if (w == 0) return new SketchData(new int[][]{});
        // ИСПРАВЛЕНО: arr.length
        int h = arr[0].length;
        int[][] copy = new int[w][h];
        for (int x = 0; x < w; x++) {
            System.arraycopy(arr[x], 0, copy[x], 0, h);
        }
        return new SketchData(copy);
    }

    public boolean isEmpty() {
        for (int[] row : this.pixels) {
            for (int p : row) {
                if (p != 0) return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SketchData that = (SketchData) obj;

        // НОВОЕ: Мгновенная проверка. Если хэши разные, это точно разные рисунки!
        // Это спасет игру от глубокого перебора массива в 99.9% случаев.
        if (this.cachedHashCode != that.cachedHashCode) return false;

        return java.util.Arrays.deepEquals(this.pixels, that.pixels);
    }

    @Override
    public int hashCode() {
        // НОВОЕ: Просто возвращаем посчитанное ранее число, вместо перебора 24 000 пикселей
        return this.cachedHashCode;
    }

}