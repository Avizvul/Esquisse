package net.avizvul.esquissemod.network;

import net.avizvul.esquissemod.component.ModDataComponents;
import net.avizvul.esquissemod.component.SketchData;
import net.avizvul.esquissemod.item.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class SketchbookPayloadHandler {

    public void handleData(final SketchbookSavePayload payload, final net.neoforged.neoforge.network.handling.IPayloadContext context) {
        context.enqueueWork(() -> {
            net.minecraft.world.entity.player.Player player = context.player();
            net.minecraft.world.item.ItemStack stack = player.getMainHandItem();

            if (!stack.is(net.avizvul.esquissemod.item.ModItems.SKETCHBOOK.get())) {
                stack = player.getOffhandItem();
            }

            if (stack.is(net.avizvul.esquissemod.item.ModItems.SKETCHBOOK.get())) {
                // 1. Получаем список текущих страниц
                java.util.List<net.avizvul.esquissemod.component.SketchData> pages =
                        new java.util.ArrayList<>(stack.getOrDefault(net.avizvul.esquissemod.component.ModDataComponents.SKETCHBOOK_PAGES.get(), new java.util.ArrayList<>()));

                // 2. Обновляем страницу (теперь проверяем через pages.size() вместо жесткого числа 16)
                if (payload.pageIndex() >= 0 && payload.pageIndex() < pages.size()) {
                    pages.set(payload.pageIndex(), payload.sketchData());

                    // 3. Сохраняем обновленный список страниц и последнюю открытую страницу
                    stack.set(net.avizvul.esquissemod.component.ModDataComponents.SKETCHBOOK_PAGES.get(), pages);
                    stack.set(net.avizvul.esquissemod.component.ModDataComponents.LAST_PAGE.get(), payload.pageIndex());
                }
            }

            // 4. Наносим урон инструментам
            if (player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                damageTool(player, serverLevel, net.avizvul.esquissemod.item.ModItems.PENCIL.get(), payload.pencilPixels() / 100);
                damageTool(player, serverLevel, net.avizvul.esquissemod.item.ModItems.ERASER.get(), payload.eraserPixels() / 100);
            }
        });
    }

    public void handleTearPage(final TearPagePayload payload, final net.neoforged.neoforge.network.handling.IPayloadContext context) {
        // Обязательно выполняем всё в основном потоке сервера
        context.enqueueWork(() -> {
            net.minecraft.world.entity.player.Player player = context.player();
            net.minecraft.world.item.ItemStack stack = player.getMainHandItem();

            if (!stack.is(ModItems.SKETCHBOOK.get())) {
                stack = player.getOffhandItem();
            }

            if (stack.is(ModItems.SKETCHBOOK.get())) {
                java.util.List<net.avizvul.esquissemod.component.SketchData> pages =
                        new java.util.ArrayList<>(stack.getOrDefault(ModDataComponents.SKETCHBOOK_PAGES.get(), new java.util.ArrayList<>()));

                if (payload.pageIndex() >= 0 && payload.pageIndex() < pages.size()) {

                    // 1. ИЗВЛЕКАЕМ удаляемую страницу (делаем это ровно один раз!)
                    net.avizvul.esquissemod.component.SketchData tornData = pages.remove(payload.pageIndex());

                    // 2. Объявляем предмет вырванной страницы
                    net.minecraft.world.item.ItemStack tornPage;

                    // 3. Проверяем, есть ли на странице рисунок
                    if (tornData.isEmpty()) {
                        // Выдаем обычную пустую страницу
                        tornPage = new net.minecraft.world.item.ItemStack(ModItems.EMPTY_PAGE.get());
                    } else {
                        // Выдаем изрисованную страницу
                        tornPage = new net.minecraft.world.item.ItemStack(ModItems.SKETCHED_PAGE.get());
                        // Сохраняем рисунок внутрь предмета
                        tornPage.set(ModDataComponents.PAGE_DATA.get(), tornData);
                    }

                    // 4. Выдаем игроку вырванный лист
                    if (!player.getInventory().add(tornPage)) {
                        player.drop(tornPage, false);
                    }

                    // 5. Проверка на пустой скетчбук
                    if (pages.isEmpty()) {
                        // Если листов не осталось, удаляем скетчбук из руки игрока
                        stack.shrink(1);
                    } else {
                        // Если листы еще есть, сохраняем обновленный список
                        stack.set(ModDataComponents.SKETCHBOOK_PAGES.get(), pages);

                        // Смещаем индекс последней открытой страницы в компоненте предмета
                        int lastPage = stack.getOrDefault(ModDataComponents.LAST_PAGE.get(), 0);
                        if (lastPage >= pages.size()) {
                            stack.set(ModDataComponents.LAST_PAGE.get(), pages.size() - 1);
                        }
                    }
                }
            }
        });
    }

    // Вспомогательный метод для генерации 16 пустых страниц
    private static List<SketchData> createEmptyPages() {
        List<SketchData> pages = new ArrayList<>();

        SketchData emptyData = SketchData.fromArray(new int[1][2]); // Создаем пустой холст

        for (int i = 0; i < 16; i++) {
            pages.add(emptyData);
        }
        return pages;
    }

    // Вспомогательный метод для поиска и нанесения урона
    private static void damageTool(Player player, ServerLevel level, Item toolItem, int damageAmount) {
        if (damageAmount <= 0) return;

        // Ищем в основном инвентаре
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(toolItem)) {
                stack.hurtAndBreak(damageAmount, level, (ServerPlayer) player, p -> {});
                return;
            }
            if (damageInPencilCase(stack, toolItem, damageAmount, level, player)) return;
        }

        // Ищем во второй руке
        for (ItemStack stack : player.getInventory().offhand) {
            if (stack.is(toolItem)) {
                stack.hurtAndBreak(damageAmount, level, (ServerPlayer) player, p -> {});
                return;
            }
            if (damageInPencilCase(stack, toolItem, damageAmount, level, player)) return;
        }
    }

    // НОВОЕ: Нанесение урона инструменту внутри компонента пенала
    private static boolean damageInPencilCase(ItemStack containerStack, Item toolItem, int damageAmount, ServerLevel level, Player player) {
        if (containerStack.is(ModItems.PENCIL_CASE.get()) && containerStack.has(net.minecraft.core.component.DataComponents.CONTAINER)) {
            net.minecraft.world.item.component.ItemContainerContents contents = containerStack.get(net.minecraft.core.component.DataComponents.CONTAINER);
            if (contents != null) {
                net.minecraft.core.NonNullList<ItemStack> items = net.minecraft.core.NonNullList.withSize(9, ItemStack.EMPTY);
                contents.copyInto(items);

                boolean foundAndDamaged = false;

                for (int i = 0; i < items.size(); i++) {
                    ItemStack innerStack = items.get(i);
                    if (innerStack.is(toolItem)) {
                        // Наносим урон предмету (игра сама удалит предмет, если он сломался окончательно)
                        innerStack.hurtAndBreak(damageAmount, level, (ServerPlayer) player, p -> {});
                        items.set(i, innerStack);
                        foundAndDamaged = true;
                        break;
                    }
                }

                if (foundAndDamaged) {
                    // Важно: упаковываем измененный массив предметов обратно в компонент пенала!
                    containerStack.set(net.minecraft.core.component.DataComponents.CONTAINER, net.minecraft.world.item.component.ItemContainerContents.fromItems(items));
                    return true;
                }
            }
        }
        return false;
    }

    public void handleChangeColor(final ChangeColorPayload payload, final net.neoforged.neoforge.network.handling.IPayloadContext context) {
        context.enqueueWork(() -> {
            net.minecraft.world.entity.player.Player player = context.player();

            // Ищем цветной карандаш везде: в руках, в инвентаре и внутри пенала
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                net.minecraft.world.item.ItemStack stack = player.getInventory().getItem(i);

                // Если нашли просто в инвентаре:
                if (stack.is(net.avizvul.esquissemod.item.ModItems.COLOR_PENCIL.get())) {
                    stack.set(net.avizvul.esquissemod.component.ModDataComponents.ACTIVE_COLOR_INDEX.get(), payload.colorIndex());
                    return;
                }

                // Если нашли пенал, лезем внутрь:
                if (stack.is(net.avizvul.esquissemod.item.ModItems.PENCIL_CASE.get()) && stack.has(net.minecraft.core.component.DataComponents.CONTAINER)) {
                    net.minecraft.world.item.component.ItemContainerContents contents = stack.get(net.minecraft.core.component.DataComponents.CONTAINER);
                    if (contents != null) {
                        net.minecraft.core.NonNullList<net.minecraft.world.item.ItemStack> items = net.minecraft.core.NonNullList.withSize(9, net.minecraft.world.item.ItemStack.EMPTY);
                        contents.copyInto(items);
                        for (int j = 0; j < items.size(); j++) {
                            net.minecraft.world.item.ItemStack innerStack = items.get(j);
                            if (innerStack.is(net.avizvul.esquissemod.item.ModItems.COLOR_PENCIL.get())) {
                                innerStack.set(net.avizvul.esquissemod.component.ModDataComponents.ACTIVE_COLOR_INDEX.get(), payload.colorIndex());
                                // Упаковываем обновленный инвентарь обратно в пенал!
                                stack.set(net.minecraft.core.component.DataComponents.CONTAINER, net.minecraft.world.item.component.ItemContainerContents.fromItems(items));
                                return;
                            }
                        }
                    }
                }
            }
        });
    }

    public void handleToolSettings(final ToolSettingsPayload payload, final net.neoforged.neoforge.network.handling.IPayloadContext context) {
        context.enqueueWork(() -> {
            net.minecraft.world.entity.player.Player player = context.player();

            // Определяем, какой инструмент обновляем
            net.minecraft.world.item.Item targetItem = switch (payload.toolType()) {
                case 0 -> ModItems.PENCIL.get();
                case 1 -> ModItems.COLOR_PENCIL.get();
                case 2 -> ModItems.ERASER.get();
                case 3 -> ModItems.SMUDGE.get();
                case 4 -> ModItems.KNEADED_ERASER.get();
                case 5 -> ModItems.COLOR_MARKER.get(); // МАРКЕР
                default -> null;
            };

            if (targetItem == null) return;

            // Ищем инструмент (как мы делали с цветом)
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                net.minecraft.world.item.ItemStack stack = player.getInventory().getItem(i);

                // 1. Нашли напрямую в инвентаре (переменная называется stack)
                if (stack.is(targetItem)) {
                    stack.set(ModDataComponents.BRUSH_SIZE.get(), payload.size());
                    stack.set(ModDataComponents.BRUSH_HARDNESS.get(), payload.hardness());
                    // Сохраняем угол поворота, если это маркер
                    if (targetItem == ModItems.COLOR_MARKER.get()) {
                        stack.set(ModDataComponents.MARKER_ROTATION.get(), payload.rotation());
                    }
                    return;
                }

                // 2. Проверка внутри пенала (а вот тут переменная называется innerStack)
                if (stack.is(ModItems.PENCIL_CASE.get()) && stack.has(net.minecraft.core.component.DataComponents.CONTAINER)) {
                    net.minecraft.world.item.component.ItemContainerContents contents = stack.get(net.minecraft.core.component.DataComponents.CONTAINER);
                    if (contents != null) {
                        net.minecraft.core.NonNullList<net.minecraft.world.item.ItemStack> items = net.minecraft.core.NonNullList.withSize(9, net.minecraft.world.item.ItemStack.EMPTY);
                        contents.copyInto(items);
                        for (int j = 0; j < items.size(); j++) {
                            net.minecraft.world.item.ItemStack innerStack = items.get(j);

                            if (innerStack.is(targetItem)) {
                                innerStack.set(ModDataComponents.BRUSH_SIZE.get(), payload.size());
                                innerStack.set(ModDataComponents.BRUSH_HARDNESS.get(), payload.hardness());
                                // Сохраняем угол поворота, если это маркер
                                if (targetItem == ModItems.COLOR_MARKER.get()) {
                                    innerStack.set(ModDataComponents.MARKER_ROTATION.get(), payload.rotation());
                                }
                                // Перепаковываем обновленный список в пенал
                                stack.set(net.minecraft.core.component.DataComponents.CONTAINER, net.minecraft.world.item.component.ItemContainerContents.fromItems(items));
                                return;
                            }
                        }
                    }
                }
            }
        });
    }
}
