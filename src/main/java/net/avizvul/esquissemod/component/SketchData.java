package net.avizvul.esquissemod.component;

import com.mojang.serialization.Codec;
import java.nio.ByteBuffer;

public class SketchData {
    private final int[][] pixels;

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

    // --- ОПТИМАЛЬНЫЙ СЕТЕВОЙ КОДЕК (Прямая бинарная запись без строк) ---
    public static final net.minecraft.network.codec.StreamCodec<io.netty.buffer.ByteBuf, SketchData> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of(
            (buf, data) -> {
                int w = 126;
                int h = 192;
                int[][] pixels2D = data.toArray(w, h);
                // Пишем 96 КБ данных напрямую в буфер пакета
                for (int x = 0; x < w; x++) {
                    for (int y = 0; y < h; y++) {
                        buf.writeInt(pixels2D[x][y]);
                    }
                }
            },
            buf -> {
                int w = 126;
                int h = 192;
                int[][] pixels2D = new int[w][h];
                // Читаем напрямую из буфера
                for (int x = 0; x < w; x++) {
                    for (int y = 0; y < h; y++) {
                        pixels2D[x][y] = buf.readInt();
                    }
                }
                return SketchData.fromArray(pixels2D);
            }
    );

    private SketchData(int[][] pixels) {
        this.pixels = pixels;
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
        return java.util.Arrays.deepEquals(this.pixels, that.pixels);
    }

    @Override
    public int hashCode() {
        return java.util.Arrays.deepHashCode(this.pixels);
    }
}