package net.avizvul.esquissemod.item.custom.base;

import net.minecraft.world.item.Item;

public abstract class DrawingToolItem extends Item {

    public DrawingToolItem(Properties properties) {
        super(properties);
    }

    // Здесь мы закладываем общую логику для всех рисующих предметов.
    // Например, в будущем мы можем добавить сюда общий метод расчета
    // шанса не потратить прочность (как чары Unbreaking),
    // или общие проверки на то, держит ли игрок скетчбук во второй руке.
}