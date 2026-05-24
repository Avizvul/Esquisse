package net.avizvul.esquissemod.client.screen;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.avizvul.esquissemod.component.ModDataComponents;
import net.avizvul.esquissemod.component.SketchData;

public class SketchedPageScreen extends Screen {
    // Ваша новая текстура GUI для оторванного листа
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("esquissemod", "textures/gui/sketched_page_gui.png"); // [8]

    private byte[][] pixels;
    private final int imageWidth = 256; // Замените на размер вашей текстуры GUI
    private final int imageHeight = 256;

    public SketchedPageScreen(ItemStack stack) {
        super(Component.literal("Sketched Page"));

        // Извлекаем рисунок из предмета [9]
        SketchData data = stack.get(ModDataComponents.PAGE_DATA.get());
        if (data != null) {
            // Замените числа на ваши реальные canvasWidth и canvasHeight с учетом множителя
            this.pixels = data.toArray(64, 128);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        int renderX = (this.width - this.imageWidth) / 2;
        int renderY = (this.height - this.imageHeight) / 2;

        // Рисуем фон (сам листок)
        guiGraphics.blit(TEXTURE, renderX, renderY, 0, 0, this.imageWidth, this.imageHeight);

        // Рисуем пиксели, если они есть
        if (this.pixels != null) {
            // Подставьте сюда ваши отступы для отрисовки пикселей холста,
            // точно так же, как вы это делаете в SketchbookScreen!
            int canvasX = renderX + 20;
            int canvasY = renderY + 20;

            for (int x = 0; x < this.pixels.length; x++) {
                for (int y = 0; y < this.pixels[x].length; y++) {
                    if (this.pixels[x][y] != 0) {
                        guiGraphics.fill(canvasX + x, canvasY + y, canvasX + x + 1, canvasY + y + 1, 0xFF000000); // Чёрный цвет
                    }
                }
            }
        }
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}