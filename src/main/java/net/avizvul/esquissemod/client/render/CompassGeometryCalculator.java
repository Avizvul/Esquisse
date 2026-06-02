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

        double TOP_GAP = 12.0;

        // Строго задаем: Карандаш слева, Игла (якорь) справа
        double leftTipX = pencilX;
        double leftTipY = pencilY;
        double rightTipX = anchorX;
        double rightTipY = anchorY;

        double dx = rightTipX - leftTipX;
        double dy = rightTipY - leftTipY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        double maxRadius = ACTUAL_LEG_LENGTH * 2.0 + TOP_GAP - 0.1;

        // Ограничитель максимального радиуса
        if (distance > maxRadius) {
            dx = (dx / distance) * maxRadius;
            dy = (dy / distance) * maxRadius;
            distance = maxRadius;
            // Игла зафиксирована, подтягиваем карандаш (левую ножку) к якорю
            leftTipX = rightTipX - dx;
            leftTipY = rightTipY - dy;
        }

        // Сложенное состояние: держим ножки на расстоянии 5 пикселей
        if (distance < 5.0) {
            if (distance > 0.01) {
                dx = (dx / distance) * 5.0;
                dy = (dy / distance) * 5.0;
            } else {
                dx = 5.0;
                dy = 0.0; // По умолчанию карандаш висит ровно слева от иглы
            }
            distance = 5.0;
            // Якорь (игла) остается ровно на курсоре, карандаш отступает влево
            leftTipX = rightTipX - dx;
            leftTipY = rightTipY - dy;
        }

        // Вектор направления от левого к правому кончику
        double dirX = dx / distance;
        double dirY = dy / distance;

        // Нормаль. Всегда направлена "вверх" и "наружу" от линии рисования
        double nx = dirY;
        double ny = -dirX;

        double halfSpread = (distance - TOP_GAP) / 2.0;
        double height = 0;
        if (halfSpread <= ACTUAL_LEG_LENGTH) {
            height = Math.sqrt(ACTUAL_LEG_LENGTH * ACTUAL_LEG_LENGTH - halfSpread * halfSpread);
        }

        // Вычисляем центральные точки
        double midTipX = (leftTipX + rightTipX) / 2.0;
        double midTipY = (leftTipY + rightTipY) / 2.0;

        double midPivotX = midTipX + nx * height;
        double midPivotY = midTipY + ny * height;

        // Центр шарнира сдвинут еще на 6 пикселей выше точек крепления
        double jointX = midPivotX + nx * 6.0;
        double jointY = midPivotY + ny * 6.0;

        // Разносим точки крепления: левая - карандашу (5ш), правая - игле (11ш)
        double leftPivotX = midPivotX - dirX * (TOP_GAP / 2.0);
        double leftPivotY = midPivotY - dirY * (TOP_GAP / 2.0);

        double rightPivotX = midPivotX + dirX * (TOP_GAP / 2.0);
        double rightPivotY = midPivotY + dirY * (TOP_GAP / 2.0);

        // Углы поворота каждой части
        float pencilRot = (float) Math.toDegrees(Math.atan2(leftTipY - leftPivotY, leftTipX - leftPivotX)) - 90.0f;
        float anchorRot = (float) Math.toDegrees(Math.atan2(rightTipY - rightPivotY, rightTipX - rightPivotX)) - 90.0f;
        float axisRot = (float) Math.toDegrees(Math.atan2(ny, nx)) + 90.0f;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 150.0f);

        // Левая сторона (Карандаш)
        renderPart(guiGraphics, PENCIL_TEX, leftPivotX, leftPivotY, pencilRot, 2.0f, 2.0f, 16, 48, 8, 0);
        // Правая сторона (Игла)
        renderPart(guiGraphics, ANCHOR_TEX, rightPivotX, rightPivotY, anchorRot, 2.0f, 2.0f, 16, 48, 8, 0);
        // Шарнир
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