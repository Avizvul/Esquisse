package net.avizvul.esquissemod.client;

import net.minecraft.client.gui.GuiGraphics;

public class ClientRenderUtils {

    public static void renderCachedSketch(net.minecraft.client.gui.GuiGraphics guiGraphics, net.avizvul.esquissemod.component.SketchData data, int startX, int startY, int drawWidth, int drawHeight) {
        net.minecraft.resources.ResourceLocation texture = net.avizvul.esquissemod.client.SketchTextureCache.getOrCreateTexture(data);
        if (texture != null) {
            guiGraphics.blit(texture, startX, startY, 0.0f, 0.0f, drawWidth, drawHeight, drawWidth, drawHeight);
        }
    }


}
