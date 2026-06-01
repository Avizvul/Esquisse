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

        // Зазор между креплениями (12 пикселей в масштабе)
        double TOP_GAP = 12.0;

        double dx = pencilX - anchorX;
        double dy = pencilY - anchorY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        double maxRadius = ACTUAL_LEG_LENGTH * 2.0 + TOP_GAP - 0.1;

        if (distance > maxRadius) {
            dx = (dx / distance) * maxRadius;
            dy = (dy / distance) * maxRadius;
            distance = maxRadius;
            pencilX = anchorX + dx;
            pencilY = anchorY + dy;
        }

        if (distance < 5.0) {
            if (distance > 0.01) {
                dx = (dx / distance) * 5.0;
                dy = (dy / distance) * 5.0;
            } else {
                dx = 5.0;
                dy = 0.0;
            }
            distance = 5.0;
            pencilX = anchorX + dx;
            pencilY = anchorY + dy;
        }

        double dirX = dx / distance;
        double dirY = dy / distance;

        // ИСПРАВЛЕНИЕ: Переворачиваем нормаль, чтобы шарнир оказался "с другой стороны".
        // Теперь Рисующая ножка физически окажется слева, а Опорная - справа!
        double nx = -dirY;
        double ny = dirX;

        double halfSpread = (distance - TOP_GAP) / 2.0;
        double height = 0;
        if (halfSpread <= ACTUAL_LEG_LENGTH) {
            height = Math.sqrt(ACTUAL_LEG_LENGTH * ACTUAL_LEG_LENGTH - halfSpread * halfSpread);
        }

        double midTipX = anchorX + dx / 2.0;
        double midTipY = anchorY + dy / 2.0;

        double jointX = midTipX + nx * height;
        double jointY = midTipY + ny * height;

        double midPivX = jointX - nx * 6.0;
        double midPivY = jointY - ny * 6.0;

        // Точки крепления ложатся идеально на текстуру: Карандаш на левое (5ш), Игла на правое (11ш)
        double pencilPivotX = midPivX + dirX * (TOP_GAP / 2.0);
        double pencilPivotY = midPivY + dirY * (TOP_GAP / 2.0);

        double anchorPivotX = midPivX - dirX * (TOP_GAP / 2.0);
        double anchorPivotY = midPivY - dirY * (TOP_GAP / 2.0);

        // Возвращаем целям их законные координаты. Больше никакого скрещивания!
        float anchorRot = (float) Math.toDegrees(Math.atan2(anchorY - anchorPivotY, anchorX - anchorPivotX)) - 90.0f;
        float pencilRot = (float) Math.toDegrees(Math.atan2(pencilY - pencilPivotY, pencilX - pencilPivotX)) - 90.0f;

        // Ось поворачивается так, чтобы отверстия на текстуре совпали с креплениями
        float axisRot = (float) Math.toDegrees(Math.atan2(-dirX, dirY)) - 90.0f;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 150.0f);

        // Отрисовываем правильные текстуры из правильных точек к правильным целям!
        renderPart(guiGraphics, ANCHOR_TEX, anchorPivotX, anchorPivotY, anchorRot, 2.0f, 2.0f, 16, 48, 8, 0);
        renderPart(guiGraphics, PENCIL_TEX, pencilPivotX, pencilPivotY, pencilRot, 2.0f, 2.0f, 16, 48, 8, 0);
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
