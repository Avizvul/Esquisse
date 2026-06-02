package net.avizvul.esquissemod.component;

import com.mojang.serialization.Codec;

public class SketchData {
    private final int[][] pixels;
    private final int cachedHashCode; // Кэш для оптимизации сравнения кадров

    // --- ОПТИМИЗИРОВАННЫЙ CODEC ДЛЯ СОХРАНЕНИЯ В МИР И NBT ---
    public static final Codec<SketchData> CODEC = Codec.INT_STREAM.xmap(
            stream -> {
                int[] arr = stream.toArray();
                int w = 126;
                int h = 192;
                int[][] pixels2D = new int[w][h];

                if (arr.length == w * h) {
                    // Поддержка старых рисунков (полный массив из старых версий мода)
                    for (int x = 0; x < w; x++) {
                        System.arraycopy(arr, x * h, pixels2D[x], 0, h);
                    }
                } else {
                    // Новый сжатый формат: читаем парами [индекс, цвет]
                    for (int i = 0; i < arr.length - 1; i += 2) {
                        int index = arr[i];
                        int color = arr[i + 1];
                        int x = index / h;
                        int y = index % h;
                        if (x < w && y < h) {
                            pixels2D[x][y] = color;
                        }
                    }
                }
                return SketchData.fromArray(pixels2D);
            },
            data -> {
                int w = 126;
                int h = 192;
                int[][] pixels2D = data.toArray(w, h);

                java.util.stream.IntStream.Builder builder = java.util.stream.IntStream.builder();
                for (int x = 0; x < w; x++) {
                    for (int y = 0; y < h; y++) {
                        int color = pixels2D[x][y];
                        if (color != 0) {
                            builder.add(x * h + y); // Записываем позицию пикселя
                            builder.add(color);     // Записываем его цвет
                        }
                    }
                }
                return builder.build(); // Пустые страницы вернут пустой массив []
            }
    );

    // --- ОПТИМИЗИРОВАННЫЙ СЕТЕВОЙ КОДЕК ДЛЯ ПАКЕТОВ ---
    public static final net.minecraft.network.codec.StreamCodec<io.netty.buffer.ByteBuf, SketchData> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of(
            (buf, data) -> {
                int w = 126;
                int h = 192;
                int[][] pixels2D = data.toArray(w, h);

                // Считаем количество закрашенных пикселей
                int count = 0;
                for (int x = 0; x < w; x++) {
                    for (int y = 0; y < h; y++) {
                        if (pixels2D[x][y] != 0) count++;
                    }
                }

                buf.writeInt(count); // Пишем количество
                for (int x = 0; x < w; x++) {
                    for (int y = 0; y < h; y++) {
                        if (pixels2D[x][y] != 0) {
                            buf.writeInt(x * h + y);
                            buf.writeInt(pixels2D[x][y]);
                        }
                    }
                }
            },
            buf -> {
                int w = 126;
                int h = 192;
                int[][] pixels2D = new int[w][h];

                int count = buf.readInt();
                for (int i = 0; i < count; i++) {
                    int index = buf.readInt();
                    int color = buf.readInt();
                    int x = index / h;
                    int y = index % h;
                    if (x < w && y < h) {
                        pixels2D[x][y] = color;
                    }
                }
                return SketchData.fromArray(pixels2D);
            }
    );

    private SketchData(int[][] pixels) {
        this.pixels = pixels;
        // Считаем этот огромный массив ТОЛЬКО один раз в момент создания
        this.cachedHashCode = java.util.Arrays.deepHashCode(this.pixels);
    }

    public int[][] getRawPixels() {
        return this.pixels;
    }

    public int[][] toArray(int targetWidth, int targetHeight) {
        int[][] result = new int[targetWidth][targetHeight];
        int copyWidth = Math.min(targetWidth, this.pixels.length);
        if (copyWidth > 0) {
            int copyHeight = Math.min(targetHeight, this.pixels.length);
            for (int x = 0; x < copyWidth; x++) {
                System.arraycopy(this.pixels[x], 0, result[x], 0, copyHeight);
            }
        }
        return result;
    }

    public static SketchData fromArray(int[][] arr) {
        int w = arr.length;
        if (w == 0) return new SketchData(new int[][]{});
        int h = arr.length;
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
        // Мгновенная отбраковка по хэшу (спасает от лагов сравнения в 99% случаев)
        if (this.cachedHashCode != that.cachedHashCode) return false;
        return java.util.Arrays.deepEquals(this.pixels, that.pixels);
    }

    @Override
    public int hashCode() {
        return this.cachedHashCode;
    }
}
