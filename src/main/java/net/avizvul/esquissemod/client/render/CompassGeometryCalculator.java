package net.avizvul.esquissemod.client.render;

import net.avizvul.esquissemod.EsquisseMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

public class CompassGeometryCalculator {

    private static final ResourceLocation ANCHOR_TEX = ResourceLocation.fromNamespaceAndPath(EsquisseMod.MOD_ID, "textures/gui/drawing_compass_anchor.png");
    private static final ResourceLocation AXIS_TEX = ResourceLocation.fromNamespaceAndPath(EsquisseMod.MOD_ID, "textures/gui/drawing_compass_axis.png");
    private static final ResourceLocation PENCIL_TEX = ResourceLocation.fromNamespaceAndPath(EsquisseMod.MOD_ID, "textures/gui/drawing_compass_pencil.png");

    private static final double ACTUAL_LEG_LENGTH = 96.0;

    public static void renderCompass(GuiGraphics guiGraphics, double anchorX, double anchorY, double pencilX, double pencilY) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        double dx = pencilX - anchorX;
        double dy = pencilY - anchorY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        // --- ИСПРАВЛЕНИЕ 1: Вводим жесткий зазор между точками крепления наверху ---
        double TOP_GAP = 5.0;

        // Максимальный радиус теперь учитывает этот зазор
        double maxRadius = ACTUAL_LEG_LENGTH * 2.0 + TOP_GAP - 0.1;

        if (distance > maxRadius) {
            dx = (dx / distance) * maxRadius;
            dy = (dy / distance) * maxRadius;
            distance = maxRadius;
            pencilX = anchorX + dx;
            pencilY = anchorY + dy;
        }

        // Если радиус меньше зазора - переходим в строго сложенное состояние
        if (distance < TOP_GAP) {
            distance = TOP_GAP;
            pencilX = anchorX + distance;
            pencilY = anchorY;
            dx = distance;
            dy = 0;
        }

        double dirX = dx / distance;
        double dirY = dy / distance;

        // Вектор нормали, который всегда указывает наружу (от радиуса) для идеального 360-вращения
        double nx = dirY;
        double ny = -dirX;

        double halfSpread = (distance - TOP_GAP) / 2.0;

        double height = 0;
        if (halfSpread <= ACTUAL_LEG_LENGTH) {
            height = Math.sqrt(ACTUAL_LEG_LENGTH * ACTUAL_LEG_LENGTH - halfSpread * halfSpread);
        }

        // 1. Находим геометрический центр самого шарнира (оси)
        double jointX = anchorX + dx / 2.0 + nx * height;
        double jointY = anchorY + dy / 2.0 + ny * height;

        // --- ИСПРАВЛЕНИЕ 2: Разносим точки крепления ножек в стороны от центра шарнира ---
        double anchorPivotX = jointX - dirX * (TOP_GAP / 2.0);
        double anchorPivotY = jointY - dirY * (TOP_GAP / 2.0);

        double pencilPivotX = jointX + dirX * (TOP_GAP / 2.0);
        double pencilPivotY = jointY + dirY * (TOP_GAP / 2.0);

        // Углы поворота ножек теперь рассчитываются от их СМЕЩЕННЫХ точек крепления
        float anchorRot = (float) Math.toDegrees(Math.atan2(anchorY - anchorPivotY, anchorX - anchorPivotX)) - 90.0f;
        float pencilRot = (float) Math.toDegrees(Math.atan2(pencilY - pencilPivotY, pencilX - pencilPivotX)) - 90.0f;

        // Ось вращается, смотря строго на биссектрису между концами
        float axisRot = (float) Math.toDegrees(Math.atan2((anchorY + pencilY) / 2.0 - jointY, (anchorX + pencilX) / 2.0 - jointX)) - 90.0f;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 150.0f);

        // Отрисовываем ножки ИЗ ИХ СМЕЩЕННЫХ ТОЧЕК
        renderPart(guiGraphics, ANCHOR_TEX, anchorPivotX, anchorPivotY, anchorRot, 2.0f, 2.0f, 16, 48, 8, 0);
        renderPart(guiGraphics, PENCIL_TEX, pencilPivotX, pencilPivotY, pencilRot, 2.0f, 2.0f, 16, 48, 8, 0);

        // Отрисовываем шарнир по центру, вращая его
        renderPart(guiGraphics, AXIS_TEX, jointX, jointY, axisRot, 2.0f, 2.0f, 16, 16, 8, 8);

        guiGraphics.pose().popPose();
        RenderSystem.disableBlend();
    }

    private static void renderPart(GuiGraphics guiGraphics, ResourceLocation texture, double x, double y, float rot, float scaleX, float scaleY, int texWidth, int texHeight, int pivotX, int pivotY) {
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(x, y, 0);
        poseStack.mulPose(Axis.ZP.rotationDegrees(rot));
        poseStack.scale(scaleX, scaleY, 1.0f);
        guiGraphics.blit(texture, -pivotX, -pivotY, 0, 0, texWidth, texHeight, texWidth, texHeight);
        poseStack.popPose();
    }
}
