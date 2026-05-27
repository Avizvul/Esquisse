package net.avizvul.esquissemod.item.custom;

import net.avizvul.esquissemod.client.screen.SketchbookScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SketchbookItem extends Item {

    public SketchbookItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        // Показываем полоску, только если в скетчбуке меньше 16 страниц (не полный)
        java.util.List<net.avizvul.esquissemod.component.SketchData> pages =
                stack.getOrDefault(net.avizvul.esquissemod.component.ModDataComponents.SKETCHBOOK_PAGES.get(), new java.util.ArrayList<>());
        return pages.size() < 16 && pages.size() > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        java.util.List<net.avizvul.esquissemod.component.SketchData> pages =
                stack.getOrDefault(net.avizvul.esquissemod.component.ModDataComponents.SKETCHBOOK_PAGES.get(), new java.util.ArrayList<>());
        // 13.0f — это максимальная длина полоски в пикселях. Заполняем пропорционально кол-ву страниц.
        return Math.round(13.0f * pages.size() / 16.0f);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x6C8ABA; // Светло-серый цвет бумаги (можно заменить на 0x00FF00 для зеленого, как у инструментов)
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack other, net.minecraft.world.inventory.Slot slot, net.minecraft.world.inventory.ClickAction action, Player player, net.minecraft.world.entity.SlotAccess access) {

        // Проверяем, что это клик Правой Кнопкой Мыши (SECONDARY) по скетчбуку
        if (action == net.minecraft.world.inventory.ClickAction.SECONDARY && slot.allowModification(player)) {

            // Проверяем, что в этот момент на курсоре висит "Пустая страница" (или обычная бумага)
            if (other.is(net.avizvul.esquissemod.item.ModItems.EMPTY_PAGE.get()) || other.is(net.minecraft.world.item.Items.PAPER)) {

                // Достаем текущие страницы скетчбука
                java.util.List<net.avizvul.esquissemod.component.SketchData> pages =
                        new java.util.ArrayList<>(stack.getOrDefault(net.avizvul.esquissemod.component.ModDataComponents.SKETCHBOOK_PAGES.get(), new java.util.ArrayList<>()));

                // Проверяем лимит (не больше 16 страниц)
                if (pages.size() < 16) {

                    // Создаем чистый холст ВАЖНО: Размеры строго как в вашем SketchbookScreen (63*2=126 и 96*2=192)
                    byte[][] emptyPixels = new byte[1][2];
                    pages.add(net.avizvul.esquissemod.component.SketchData.fromArray(emptyPixels));

                    // Обновляем список страниц в предмете скетчбука
                    stack.set(net.avizvul.esquissemod.component.ModDataComponents.SKETCHBOOK_PAGES.get(), pages);

                    // Забираем (тратим) 1 листок с курсора
                    other.shrink(1);

                    // Воспроизводим приятный звук добавления страницы
                    player.playSound(net.minecraft.sounds.SoundEvents.BOOK_PAGE_TURN, 1.0f, 1.0f);

                    // Возвращаем true, говоря игре, что мы успешно обработали клик и предметы не нужно менять местами
                    return true;
                }
            }
        }

        // Для всех остальных случаев вызываем стандартное поведение
        return super.overrideOtherStackedOnMe(stack, other, slot, action, player, access);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        // Проверяем, что находимся на логическом клиенте
        if (level.isClientSide()) {
            // Открываем созданный экран
            Minecraft.getInstance().setScreen(new SketchbookScreen());
        }

        // Сообщаем, что действие прошло успешно, и прерываем дальнейшие проверки
        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }
}