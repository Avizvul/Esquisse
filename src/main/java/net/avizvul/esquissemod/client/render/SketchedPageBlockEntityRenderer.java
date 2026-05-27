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

        // 1. Сдвигаем матрицу в центр физического блока
        poseStack.translate(0.5f, 0.5f, 0.5f);

        // 2. Поворачиваем холст так, чтобы он лег на нужную плоскость (пол, потолок или стены)
        switch (facing) {
            case NORTH -> poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180f));
            case SOUTH -> poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(0f));
            case WEST -> poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(90f));
            case EAST -> poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-90f));
            case UP -> poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-90f));
            case DOWN -> poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90f));
        }

        // 3. Прижимаем лист бумаги к самой поверхности стены/пола/потолка (-0.49f)
        poseStack.translate(0.0f, 0.0f, -0.49f);

        // --- 4. ВРАЩЕНИЕ ВОКРУГ СВОЕЙ ОСИ ПО Z (При кликах игрока) ---
        poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(blockEntity.getRotation() * 90f));

        // 5. Масштабируем: холст 126x192...
        float scale = 0.8f / 192f;
        poseStack.scale(scale, -scale, scale);

        // 5. Смещаем координаты в левый верхний угол
        poseStack.translate(-126 / 2.0f, -192 / 2.0f, 0.0f);

        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.text(PAGE_TEX));

        // --- ОТРИСОВКА ФОНА БУМАГИ ---
        // Передаем z = 0.0f
        drawQuad(matrix, consumer, 0, 0, 0.0f, 126, 192, 0.0f, 0.0f, 1.0f, 1.0f, 0xFFFFFFFF, packedLight);

        // --- ОТРИСОВКА ПИКСЕЛЕЙ ---
        int[][] pixels = data.toArray(126, 192);

        for (int x = 0; x < 126; x++) {
            for (int y = 0; y < 192; y++) {
                int pixelColor = pixels[x][y];

                if (pixelColor != 0) {
                    // Передаем z = 0.1f и готовый pixelColor для 3D полигона
                    drawQuad(matrix, consumer, x, y, 0.1f, 1, 1, 0.50f, 0.50f, 0.51f, 0.51f, pixelColor, packedLight);
                }
            }
        }

        poseStack.popPose();
    }

    // Вспомогательный метод для рисования 3D-квадратов (полигонов)
    private void drawQuad(Matrix4f matrix, VertexConsumer consumer, float x, float y, float z, float width, float height, float u0, float v0, float u1, float v1, int argb, int light) {
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        int a = (argb >> 24) & 0xFF;

        // Передаем z вместо 0
        consumer.addVertex(matrix, x, y, z).setColor(r, g, b, a).setUv(u0, v0).setLight(light);
        consumer.addVertex(matrix, x, y + height, z).setColor(r, g, b, a).setUv(u0, v1).setLight(light);
        consumer.addVertex(matrix, x + width, y + height, z).setColor(r, g, b, a).setUv(u1, v1).setLight(light);
        consumer.addVertex(matrix, x + width, y, z).setColor(r, g, b, a).setUv(u1, v0).setLight(light);
    }
}