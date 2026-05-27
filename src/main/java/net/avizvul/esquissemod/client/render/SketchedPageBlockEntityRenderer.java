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

        // 1. Центрируем внутри блока
        poseStack.translate(0.5f, 0.5f, 0.5f);

        // 2. ВАНИЛЬНАЯ МАГИЯ (Математика Рамки).
        // Вращение по оси Y
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180.0f - facing.toYRot()));

        // Вращение по оси X (т.к. метода toXRot() нет, задаем углы для пола и потолка вручную)
        float xRot = 0.0f;
        if (facing == Direction.UP) {
            xRot = -90.0f;
        } else if (facing == Direction.DOWN) {
            xRot = 90.0f;
        }
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(xRot));

        // 3. Прижимаем холст вплотную к стене (сдвигаем вглубь на 0.49f)
        poseStack.translate(0.0f, 0.0f, 0.49f);

        // 4. Пользовательское вращение (при кликах игрока)
        poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(blockEntity.getRotation() * 90f));

        // 5. Переворачиваем плоскость по Z, чтобы ось Y пошла вниз (как в 2D-координатах)
        poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(180f));

        // 6. Масштаб
        float scale = 0.8f / 192f;
        poseStack.scale(scale, scale, scale);

        // 7. Сдвигаем холст так, чтобы он был по центру
        poseStack.translate(-63.0f, -96.0f, 0.0f);

        // ИСПРАВЛЕНИЕ: Берем один общий объект Pose (содержит в себе и позиции, и нормали света)
        PoseStack.Pose pose = poseStack.last();

        // --- ФОН ---
        VertexConsumer bgConsumer = bufferSource.getBuffer(RenderType.entityCutout(PAGE_TEX));
        drawQuad(pose, bgConsumer, 0, 0, 0.0f, 126, 192, 0.0f, 0.0f, 1.0f, 1.0f, 0xFFFFFFFF, packedLight);

        // --- ПИКСЕЛИ ---
        VertexConsumer pixelConsumer = bufferSource.getBuffer(RenderType.entityTranslucentCull(PAGE_TEX));
        int[][] pixels = data.toArray(126, 192);

        for (int x = 0; x < 126; x++) {
            for (int y = 0; y < 192; y++) {
                int pixelColor = pixels[x][y];
                if (pixelColor != 0) {
                    drawQuad(pose, pixelConsumer, x, y, -0.01f, 1, 1, 0.1f, 0.1f, 0.11f, 0.11f, pixelColor, packedLight);
                }
            }
        }

        poseStack.popPose();
    }

    // ИСПРАВЛЕНИЕ: Метод теперь принимает PoseStack.Pose вместо раздельных матриц
    private void drawQuad(PoseStack.Pose pose, VertexConsumer consumer, float x, float y, float z, float width, float height, float u0, float v0, float u1, float v1, int argb, int light) {
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        int a = (argb >> 24) & 0xFF;

        int overlay = net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY;

        // Передаем единый объект 'pose' и в addVertex (для координат), и в setNormal (для теней)
        consumer.addVertex(pose, x, y, z).setColor(r, g, b, a).setUv(u0, v0).setOverlay(overlay).setLight(light).setNormal(pose, 0.0f, 0.0f, -1.0f);
        consumer.addVertex(pose, x, y + height, z).setColor(r, g, b, a).setUv(u0, v1).setOverlay(overlay).setLight(light).setNormal(pose, 0.0f, 0.0f, -1.0f);
        consumer.addVertex(pose, x + width, y + height, z).setColor(r, g, b, a).setUv(u1, v1).setOverlay(overlay).setLight(light).setNormal(pose, 0.0f, 0.0f, -1.0f);
        consumer.addVertex(pose, x + width, y, z).setColor(r, g, b, a).setUv(u1, v0).setOverlay(overlay).setLight(light).setNormal(pose, 0.0f, 0.0f, -1.0f);
    }
}