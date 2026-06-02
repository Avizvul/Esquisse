package net.avizvul.esquissemod.component;

import com.mojang.serialization.Codec;

public class SketchData {
    private final int[][] pixels;
    private final int cachedHashCode;

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

    // --- ОПТИМИЗИРОВАННЫЙ СЕТЕВОЙ КОДЕК С GZIP СЖАТИЕМ ---
    public static final net.minecraft.network.codec.StreamCodec<io.netty.buffer.ByteBuf, SketchData> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of(
            (buf, data) -> {
                int w = 126;
                int h = 192;
                int[][] pixels2D = data.toArray(w, h);
                try {
                    java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                    java.util.zip.GZIPOutputStream gzip = new java.util.zip.GZIPOutputStream(baos);
                    java.io.DataOutputStream dos = new java.io.DataOutputStream(gzip);

                    for (int x = 0; x < w; x++) {
                        for (int y = 0; y < h; y++) {
                            dos.writeInt(pixels2D[x][y]);
                        }
                    }

                    dos.close();
                    byte[] compressed = baos.toByteArray();

                    // Отправляем длину архива и сам архив
                    buf.writeInt(compressed.length);
                    buf.writeBytes(compressed);
                } catch (Exception e) {
                    buf.writeInt(0);
                    e.printStackTrace();
                }
            },
            buf -> {
                int w = 126;
                int h = 192;
                int[][] pixels2D = new int[w][h];
                int len = buf.readInt();

                if (len > 0) {
                    byte[] compressed = new byte[len];
                    buf.readBytes(compressed);
                    try {
                        java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(compressed);
                        java.util.zip.GZIPInputStream gzip = new java.util.zip.GZIPInputStream(bais);
                        java.io.DataInputStream dis = new java.io.DataInputStream(gzip);

                        for (int x = 0; x < w; x++) {
                            for (int y = 0; y < h; y++) {
                                pixels2D[x][y] = dis.readInt();
                            }
                        }
                        dis.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return SketchData.fromArray(pixels2D);
            }
    );

    private SketchData(int[][] pixels) {
        this.pixels = pixels;
        this.cachedHashCode = java.util.Arrays.deepHashCode(this.pixels);
    }

    public int[][] getRawPixels() {
        return this.pixels;
    }

    public int[][] toArray(int targetWidth, int targetHeight) {
        int[][] result = new int[targetWidth][targetHeight];
        int copyWidth = Math.min(targetWidth, this.pixels.length);
        if (copyWidth > 0) {
            // ИСПРАВЛЕНИЕ: Пробел в [ 0 ]
            int copyHeight = Math.min(targetHeight, this.pixels[ 0 ].length);
            for (int x = 0; x < copyWidth; x++) {
                System.arraycopy(this.pixels[x], 0, result[x], 0, copyHeight);
            }
        }
        return result;
    }

    public static SketchData fromArray(int[][] arr) {
        int w = arr.length;
        if (w == 0) return new SketchData(new int[][]{});

        // ИСПРАВЛЕНИЕ: Пробел в [ 0 ]
        int h = arr[ 0 ].length;

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
        if (this.cachedHashCode != that.cachedHashCode) return false;
        return java.util.Arrays.deepEquals(this.pixels, that.pixels);
    }

    @Override
    public int hashCode() {
        return this.cachedHashCode;
    }
}
