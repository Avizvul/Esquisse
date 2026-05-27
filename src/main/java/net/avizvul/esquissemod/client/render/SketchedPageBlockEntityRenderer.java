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

public class SketchedPageBlockEntityRenderer implements BlockEntityRenderer<SketchedPageBlockEntity> {
    private static final ResourceLocation PAGE_TEX = ResourceLocation.fromNamespaceAndPath(EsquisseMod.MOD_ID, "textures/gui/sketched_page_gui.png");

    public SketchedPageBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(SketchedPageBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        SketchData data = blockEntity.getSketchData();
        if (data == null || data.isEmpty()) return;

        Direction facing = blockEntity.getBlockState().getValue(SketchedPageBlock.FACING);

        poseStack.pushPose();

        // 1. ЖЕСТКАЯ АБСОЛЮТНАЯ ПРИВЯЗКА К ПОВЕРХНОСТИ
        switch (facing) {
            case UP: // Пол: абсолютный центр на Y=0.01
                poseStack.translate(0.5f, 0.01f, 0.5f);
                poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90f));
                break;
            case DOWN: // Потолок: абсолютный центр на Y=0.99
                poseStack.translate(0.5f, 0.99f, 0.5f);
                poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-90f));
                break;
            case NORTH: // Южная стена (блок смотрит на Север)
                poseStack.translate(0.5f, 0.5f, 0.99f);
                break;
            case SOUTH: // Северная стена (блок смотрит на Юг)
                poseStack.translate(0.5f, 0.5f, 0.01f);
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180f));
                break;
            case WEST: // Восточная стена
                poseStack.translate(0.99f, 0.5f, 0.5f);
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(90f));
                break;
            case EAST: // Западная стена
                poseStack.translate(0.01f, 0.5f, 0.5f);
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-90f));
                break;
        }

        // 2. Вращение от кликов игрока (ПКМ по блоку)
        poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(blockEntity.getRotation() * 90f));

        // 3. Выравниваем оси X и Y, чтобы они соответствовали 2D
        poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(180f));

        // 4. Масштаб
        float scale = 0.8f / 192f;
        poseStack.scale(scale, scale, scale);

        // 5. Сдвигаем левый верхний угол (126x192) ровно в геометрический центр
        poseStack.translate(-63.0f, -96.0f, 0.0f);

        PoseStack.Pose pose = poseStack.last();

        // --- ФОН (Бумага) ---
        VertexConsumer bgConsumer = bufferSource.getBuffer(RenderType.entityCutout(PAGE_TEX));
        drawQuad(pose, bgConsumer, 0, 0, 0.0f, 126, 192, 0.0f, 0.0f, 1.0f, 1.0f, 0xFFFFFFFF, packedLight);

        // --- ПИКСЕЛИ (Рисунок) ---
        VertexConsumer pixelConsumer = bufferSource.getBuffer(RenderType.entityTranslucentCull(PAGE_TEX));
        int[][] pixels = data.toArray(126, 192);

        for (int x = 0; x < 126; x++) {
            for (int y = 0; y < 192; y++) {
                int pixelColor = pixels[x][y];
                if (pixelColor != 0) {
                    // Z = +0.01f. Пиксели выдвигаются БЛИЖЕ к игроку поверх бумаги!
                    drawQuad(pose, pixelConsumer, x, y, 0.01f, 1, 1, 0.1f, 0.1f, 0.11f, 0.11f, pixelColor, packedLight);
                }
            }
        }

        poseStack.popPose();
    }

    private void drawQuad(PoseStack.Pose pose, VertexConsumer consumer, float x, float y, float z, float width, float height, float u0, float v0, float u1, float v1, int argb, int light) {
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        int a = (argb >> 24) & 0xFF;

        int overlay = net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY;

        // Восстановлен правильный порядок вершин: Верх-Лево -> Низ-Лево -> Низ-Право -> Верх-Право
        consumer.addVertex(pose, x, y, z).setColor(r, g, b, a).setUv(u0, v0).setOverlay(overlay).setLight(light).setNormal(pose, 0.0f, 0.0f, 1.0f);
        consumer.addVertex(pose, x, y + height, z).setColor(r, g, b, a).setUv(u0, v1).setOverlay(overlay).setLight(light).setNormal(pose, 0.0f, 0.0f, 1.0f);
        consumer.addVertex(pose, x + width, y + height, z).setColor(r, g, b, a).setUv(u1, v1).setOverlay(overlay).setLight(light).setNormal(pose, 0.0f, 0.0f, 1.0f);
        consumer.addVertex(pose, x + width, y, z).setColor(r, g, b, a).setUv(u1, v0).setOverlay(overlay).setLight(light).setNormal(pose, 0.0f, 0.0f, 1.0f);
    }
}