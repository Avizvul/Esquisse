package net.avizvul.esquissemod.item.custom;

import net.avizvul.esquissemod.component.ModDataComponents;
import net.avizvul.esquissemod.item.custom.base.DrawingToolItem;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ColorPencilItem extends DrawingToolItem {

    public ColorPencilItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
        // Проверяем, что клик был ПКМ (SECONDARY) [1]
        if (action == ClickAction.SECONDARY && slot.allowModification(player)) {

            // Самый надежный способ проверить краситель в 1.21 — проверить инстанс класса DyeItem
            if (other.getItem() instanceof DyeItem dye) {
                int colorId = dye.getDyeColor().getId(); // Получаем ID цвета (0-15)

                // Достаем текущий список цветов из карандаша
                List<Integer> colors = new ArrayList<>(stack.getOrDefault(ModDataComponents.STORED_COLORS.get(), new ArrayList<>()));

                // Проверяем, что карандаш еще не содержит этот цвет и лимит в 16 цветов не превышен
                if (!colors.contains(colorId) && colors.size() < 16) {
                    colors.add(colorId);

                    // Обновляем компонент предмета
                    stack.set(ModDataComponents.STORED_COLORS.get(), colors);

                    // СРАЗУ переключаем активный цвет на только что добавленный (он теперь последний в списке)
                    stack.set(ModDataComponents.ACTIVE_COLOR_INDEX.get(), colors.size() - 1);

                    // Тратим 1 краситель с курсора [3]
                    other.shrink(1);

                    // Воспроизводим звук (как при покраске ошейника или брони)
                    player.playSound(SoundEvents.DYE_USE, 1.0f, 1.0f);

                    return true; // Успешно "впитали" цвет
                }
            }
        }
        return super.overrideOtherStackedOnMe(stack, other, slot, action, player, access);
    }
}