package net.avizvul.esquissemod.client.screen;

import net.avizvul.esquissemod.component.ModDataComponents;
import net.avizvul.esquissemod.component.SketchData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class SketchedPageScreen extends Screen {

    private static final net.minecraft.resources.ResourceLocation PAGE_TEX =
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(net.avizvul.esquissemod.EsquisseMod.MOD_ID,
                    "textures/gui/sketched_page_gui.png"
            );

    private final int canvasWidth = 63;
    private final int canvasHeight = 96;
    private final int scale = 3;

    // НОВОЕ: Теперь мы храним сам объект SketchData на уровне класса, а не массив пикселей
    private final SketchData data;

    public SketchedPageScreen(ItemStack stack) {
        super(Component.literal("Sketched Page"));

        // Сохраняем рисунок из предмета в поле класса, чтобы он был доступен в методе render
        this.data = stack.get(ModDataComponents.PAGE_DATA.get());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Оставляем пустым, чтобы не было стандартного затемнения
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int drawWidth = this.canvasWidth * this.scale;
        int drawHeight = this.canvasHeight * this.scale;

        int renderX = (this.width - drawWidth) / 2;
        int renderY = (this.height - drawHeight) / 2;

        // 1. Отрисовка фона самой бумаги
        guiGraphics.blit(PAGE_TEX, renderX, renderY, drawWidth, drawHeight,
                0.0f, 0.0f, this.canvasWidth, this.canvasHeight, this.canvasWidth, this.canvasHeight
        );

        // 2. Отрисовка рисунка из кэшированной текстуры
        // Теперь this.data распознается без проблем!
        if (this.data != null && !this.data.isEmpty()) {
            com.mojang.blaze3d.systems.RenderSystem.enableBlend();
            net.avizvul.esquissemod.client.ClientRenderUtils.renderCachedSketch(guiGraphics, this.data, renderX, renderY, drawWidth, drawHeight);
            com.mojang.blaze3d.systems.RenderSystem.disableBlend();
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
}
