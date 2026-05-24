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