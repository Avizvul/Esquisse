package net.avizvul.esquissemod.item.custom.base;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public abstract class DrawingToolItem extends Item {

    public DrawingToolItem(Properties properties) {
        super(properties);
    }

    // 1. Обязательный метод: возвращает путь к текстуре кнопки
    public abstract ResourceLocation getGuiTexture();

    // 2. Ширина кнопки инструмента по умолчанию
    public int getGuiWidth() {
        return 16;
    }

    // 3. Высота кнопки инструмента по умолчанию
    public int getGuiHeight() {
        return 16;
    }

    // 4. Смещение (на сколько пикселей текстура сдвигается вверх при наведении)
    public int getHoverOffset() {
        return 16;
    }

    // 5. На сколько пикселей инструмент "выглядывает" из-за экрана, когда он ВЫБРАН
    public int getPeekHeight() {
        return 8;
    }
}