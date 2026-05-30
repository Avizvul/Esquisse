package net.avizvul.esquissemod.client.screen;

import net.avizvul.esquissemod.EsquisseMod;
import net.avizvul.esquissemod.menu.PencilCaseMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class PencilCaseScreen extends AbstractContainerScreen<PencilCaseMenu> {
    // ВАМ ПОНАДОБИТСЯ ЭТА ТЕКСТУРА (размер должен быть строго 176x133, нарисуйте в стиле инвентаря печки/сундука)
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(EsquisseMod.MOD_ID, "textures/gui/pencil_case_gui.png");

    public PencilCaseScreen(PencilCaseMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 181;
        this.inventoryLabelY = this.imageHeight - 94; // Оставляем дефолтное смещение для слова "Инвентарь"

        // Настраиваем позицию названия пенала
        this.titleLabelX = 61;
        // Так как шрифт рисуется от верхнего левого угла, а его высота 9 пикселей: 16 - 9 = 7
        this.titleLabelY = 9;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY); // Рисуем тултипы предметов
    }
}
