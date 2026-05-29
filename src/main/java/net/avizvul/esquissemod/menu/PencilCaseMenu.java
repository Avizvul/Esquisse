package net.avizvul.esquissemod.menu;

import net.avizvul.esquissemod.item.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

public class PencilCaseMenu extends AbstractContainerMenu {
    private final SimpleContainer container;
    private final ItemStack pencilCaseStack;

    // Конструктор для клиента
    public PencilCaseMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, ItemStack.EMPTY);
    }

    // Конструктор для сервера
    public PencilCaseMenu(int containerId, Inventory playerInventory, ItemStack pencilCaseStack) {
        super(ModMenuTypes.PENCIL_CASE_MENU.get(), containerId);
        this.pencilCaseStack = pencilCaseStack;

        // Создаем инвентарь на 9 слотов. При любом изменении сохраняем его в предмет!
        this.container = new SimpleContainer(9) {
            @Override
            public void setChanged() {
                super.setChanged();
                saveToItem();
            }
        };

        // Загружаем сохраненные предметы из компонента предмета (если они там есть)
        if (!pencilCaseStack.isEmpty() && pencilCaseStack.has(DataComponents.CONTAINER)) {
            ItemContainerContents contents = pencilCaseStack.get(DataComponents.CONTAINER);
            contents.copyInto(this.container.getItems());
        }

        // 1. Добавляем 9 слотов самого пенала
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(this.container, i, 8 + i * 18, 20) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    // РАЗРЕШАЕМ КЛАСТЬ ТОЛЬКО ИНСТРУМЕНТЫ (защищает от рекурсии пенала в пенале)
                    return stack.is(ModItems.PENCIL.get()) ||
                            stack.is(ModItems.COLOR_PENCIL.get()) ||
                            stack.is(ModItems.ERASER.get()) ||
                            stack.is(ModItems.RULER.get());
                }
            });
        }

        // 2. Добавляем 27 слотов инвентаря игрока
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, row * 18 + 51));
            }
        }

        // 3. Добавляем 9 слотов хотбара игрока
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 109));
        }
    }

    // Метод для записи измененного инвентаря обратно в NBT компоненты пенала
    private void saveToItem() {
        if (!this.pencilCaseStack.isEmpty()) {
            this.pencilCaseStack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(this.container.getItems()));
        }
    }

    // Логика Shift-клика (быстрое перемещение предметов)
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            itemstack = stackInSlot.copy();

            // Если мы шифт-кликаем из пенала (первые 9 слотов) -> в инвентарь
            if (index < 9) {
                if (!this.moveItemStackTo(stackInSlot, 9, this.slots.size(), true)) return ItemStack.EMPTY;
            }
            // Если шифт-кликаем из инвентаря -> в пенал
            else if (!this.moveItemStackTo(stackInSlot, 0, 9, false)) {
                return ItemStack.EMPTY;
            }

            if (stackInSlot.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        // Проверяем, что игрок не выкинул пенал из рук, пока GUI открыт
        return player.getMainHandItem() == this.pencilCaseStack || player.getOffhandItem() == this.pencilCaseStack || this.pencilCaseStack.isEmpty();
    }
}
