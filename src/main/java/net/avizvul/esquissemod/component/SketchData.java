package net.avizvul.esquissemod.component;

import com.mojang.serialization.Codec;
import java.nio.ByteBuffer;

public class SketchData {
    private final int[][] pixels;

    public static final Codec<SketchData> CODEC = Codec.STRING.xmap(
            SketchData::decodeFromString,
            SketchData::encodeToString
    );

    public static final net.minecraft.network.codec.StreamCodec<io.netty.buffer.ByteBuf, SketchData> STREAM_CODEC =
            net.minecraft.network.codec.StreamCodec.composite(
                    net.minecraft.network.codec.ByteBufCodecs.stringUtf8(300000),
                    SketchData::encodeToString,
                    SketchData::decodeFromString
            );

    private static String encodeToString(SketchData data) {
        int w = 126;
        int h = 192;
        int[][] pixels2D = data.toArray(w, h);
        ByteBuffer buffer = ByteBuffer.allocate(w * h * 4); // 4 байта на каждый int
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                buffer.putInt(pixels2D[x][y]);
            }
        }
        return java.util.Base64.getEncoder().encodeToString(buffer.array());
    }

    private static SketchData decodeFromString(String str) {
        byte[] bytes = java.util.Base64.getDecoder().decode(str);
        int w = 126;
        int h = 192;
        int[][] pixels2D = new int[w][h];

        // ОБРАТНАЯ СОВМЕСТИМОСТЬ: Старый формат (1 байт на пиксель)
        if (bytes.length == w * h) {
            for (int i = 0; i < bytes.length; i++) {
                byte val = bytes[i];
                if (val > 0) {
                    int color = 0xFF000000;
                    if (val == 1) color = 0xFFCCCCCC;
                    else if (val == 2) color = 0xFF888888;
                    else if (val == 3) color = 0xFF444444;
                    else color = 0xFF111111;
                    pixels2D[i % w][i / w] = color;
                }
            }
        }
        // НОВЫЙ ФОРМАТ (4 байта на пиксель)
        else if (bytes.length == w * h * 4) {
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    pixels2D[x][y] = buffer.getInt();
                }
            }
        }
        return SketchData.fromArray(pixels2D);
    }

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