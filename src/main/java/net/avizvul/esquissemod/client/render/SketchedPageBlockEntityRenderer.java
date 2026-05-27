package net.avizvul.esquissemod.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.avizvul.esquissemod.EsquisseMod;
import net.avizvul.esquissemod.block.SketchedPageBlock;
import net.avizvul.esquissemod.block.entity.SketchedPageBlockEntity;
import net.avizvul.esquissemod.component.SketchData;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class SketchedPageBlockEntityRenderer implements BlockEntityRenderer<SketchedPageBlockEntity> {

    // Ссылка на текстуру листа
    private static final ResourceLocation PAGE_TEX =
            ResourceLocation.fromNamespaceAndPath(EsquisseMod.MOD_ID, "textures/gui/sketched_page_gui.png");

    public SketchedPageBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(SketchedPageBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        SketchData data = blockEntity.getSketchData();
        if (data == null || data.isEmpty()) return;

        Direction facing = blockEntity.getBlockState().getValue(SketchedPageBlock.FACING);

        poseStack.pushPose();

        // 1. Центрируем матрицу
        poseStack.translate(0.5f, 0.5f, 0.5f);

        // 2. Поворачиваем систему координат.
        // getRotation() делает так, что локальная ось +Z всегда смотрит ОТ стены на игрока.
        // Сама стена, к которой прикреплен блок, находится в направлении -Z.
        poseStack.mulPose(facing.getRotation());

        // 3. ИСПРАВЛЕНИЕ: Прижимаем холст к стене (сдвиг в локальный МИНУС Z).
        poseStack.translate(0.0f, 0.0f, -0.49f);

        // 4. Вращение рисунка вокруг своей оси (при кликах)
        poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(blockEntity.getRotation() * 90f));

        // 5. Разворот осей X и Y, чтобы они шли слева-направо и сверху-вниз (как в 2D).
        // Поворот на 180 по Z идеально переворачивает плоскость, не ломая 3D нормали!
        poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(180f));

        // 6. Масштаб (теперь строго положительный)
        float scale = 0.8f / 192f;
        poseStack.scale(scale, scale, scale);

        // 7. Сдвигаем левый верхний угол в нулевую точку, чтобы рисунок отцентровался
        poseStack.translate(-63.0f, -96.0f, 0.0f);

        Matrix4f matrix = poseStack.last().pose();

        // --- ИСПРАВЛЕНИЕ ПРОЗРАЧНОСТИ (ДЫРОК) ---
        // Фон рисуем как Cutout. Он запишется в буфер глубины как 100% плотный объект.
        VertexConsumer bgConsumer = bufferSource.getBuffer(RenderType.entityCutout(PAGE_TEX));
        drawQuad(matrix, bgConsumer, 0, 0, 0.0f, 126, 192, 0.0f, 0.0f, 1.0f, 1.0f, 0xFFFFFFFF, packedLight);

        // А пиксели рисуем как Translucent. Теперь они будут мягко наслаиваться поверх плотного фона.
        VertexConsumer pixelConsumer = bufferSource.getBuffer(RenderType.entityTranslucentCull(PAGE_TEX));
        int[][] pixels = data.toArray(126, 192);

        for (int x = 0; x < 126; x++) {
            for (int y = 0; y < 192; y++) {
                int pixelColor = pixels[x][y];
                if (pixelColor != 0) {
                    // ИСПРАВЛЕНИЕ ГЛУБИНЫ: Сдвигаем пиксели на +0.01f.
                    // Поскольку +Z смотрит на игрока, пиксели будут чуть ближе, чем фон (Z=0.0f)
                    drawQuad(matrix, pixelConsumer, x, y, 0.01f, 1, 1, 0.50f, 0.50f, 0.51f, 0.51f, pixelColor, packedLight);
                }
            }
        }

        poseStack.popPose();
    }

    private void drawQuad(Matrix4f matrix, VertexConsumer consumer, float x, float y, float z, float width, float height, float u0, float v0, float u1, float v1, int argb, int light) {
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        int a = (argb >> 24) & 0xFF;

        int overlay = net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY;

        // Нормаль обязательно должна смотреть на игрока (0, 0, 1), чтобы свет падал правильно
        consumer.addVertex(matrix, x, y, z).setColor(r, g, b, a).setUv(u0, v0).setOverlay(overlay).setLight(light).setNormal(0.0f, 0.0f, 1.0f);
        consumer.addVertex(matrix, x, y + height, z).setColor(r, g, b, a).setUv(u0, v1).setOverlay(overlay).setLight(light).setNormal(0.0f, 0.0f, 1.0f);
        consumer.addVertex(matrix, x + width, y + height, z).setColor(r, g, b, a).setUv(u1, v1).setOverlay(overlay).setLight(light).setNormal(0.0f, 0.0f, 1.0f);
        consumer.addVertex(matrix, x + width, y, z).setColor(r, g, b, a).setUv(u1, v0).setOverlay(overlay).setLight(light).setNormal(0.0f, 0.0f, 1.0f);
    }
}