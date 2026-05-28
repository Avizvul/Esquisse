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

        // 1. Идем в самый центр блока
        poseStack.translate(0.5f, 0.5f, 0.5f);

        // 2. Строго задаем поворот для каждой из 6 граней.
        // Теперь локальные оси всегда будут: +X = Вправо, +Y = Вниз, -Z = Лицо (к игроку)
        switch (facing) {
            case NORTH:
                poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(180f));
                break;
            case SOUTH:
                poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(180f));
                break;
            case EAST:
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(90f));
                poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(180f));
                break;
            case WEST:
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-90f));
                poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(180f));
                break;
            case UP: // Пол
                poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90f));
                break;
            case DOWN: // Потолок
                poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-90f));
                poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(180f));
                break;
        }

        // 3. Сдвигаемся к нужной грани.
        // Локальная ось +Z теперь смотрит ВНУТРЬ блока (в стену/пол). Сдвигаем на 0.4375, чтобы прижать бумагу к внутренней поверхности хитбокса.
        poseStack.translate(0.0f, 0.0f, 0.4375f);

        // 4. Вращение от кликов игрока (ПКМ по блоку)
        poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(blockEntity.getRotation() * 90f));

        // 5. Масштаб и центрирование рисунка
        float scale = 0.8f / 192f;
        poseStack.scale(scale, scale, scale);
        poseStack.translate(-63.0f, -96.0f, 0.0f);

        PoseStack.Pose pose = poseStack.last();

        // --- ФОН (Бумага) ---
        VertexConsumer bgConsumer = bufferSource.getBuffer(RenderType.entityCutout(PAGE_TEX));
        // Фон на Z = 0.0f
        drawQuad(pose, bgConsumer, 0, 0, 0.0f, 126, 192, 0.0f, 0.0f, 1.0f, 1.0f, 0xFFFFFFFF, packedLight);

        // --- ПИКСЕЛИ (Рисунок) ---
        net.minecraft.resources.ResourceLocation sketchTexture = net.avizvul.esquissemod.client.SketchTextureCache.getOrCreateTexture(data);
        if (sketchTexture != null) {
            VertexConsumer pixelConsumer = bufferSource.getBuffer(RenderType.entityTranslucentCull(sketchTexture));
            // ИСПРАВЛЕНИЕ Z-FIGHTING: Рисуем пиксели с Z = -1.0f (выдвигаем на 1 локальный пиксель вперед от бумаги)
            drawQuad(pose, pixelConsumer, 0, 0, -1.0f, 126, 192, 0.0f, 0.0f, 1.0f, 1.0f, 0xFFFFFFFF, packedLight);
        }

        poseStack.popPose();
    }

    private void drawQuad(
            PoseStack.Pose pose, VertexConsumer consumer,
            float x, float y, float z, float width, float height, float u0, float v0, float u1, float v1,
            int argb, int light) {

        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        int a = (argb >> 24) & 0xFF;
        int overlay = net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY;

        // ИСПРАВЛЕНИЕ: Меняем нормаль с 1.0f на -1.0f, чтобы свет падал с лица, а не изнутри блока
        consumer.addVertex(pose, x, y, z).setColor(r, g, b, a).setUv(u0, v0).setOverlay(overlay).setLight(light).setNormal(pose, 0.0f, 0.0f, -1.0f);
        consumer.addVertex(pose, x, y + height, z).setColor(r, g, b, a).setUv(u0, v1).setOverlay(overlay).setLight(light).setNormal(pose, 0.0f, 0.0f, -1.0f);
        consumer.addVertex(pose, x + width, y + height, z).setColor(r, g, b, a).setUv(u1, v1).setOverlay(overlay).setLight(light).setNormal(pose, 0.0f, 0.0f, -1.0f);
        consumer.addVertex(pose, x + width, y, z).setColor(r, g, b, a).setUv(u1, v0).setOverlay(overlay).setLight(light).setNormal(pose, 0.0f, 0.0f, -1.0f);
    }
}