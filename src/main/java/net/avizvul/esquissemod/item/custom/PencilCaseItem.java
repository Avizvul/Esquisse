package net.avizvul.esquissemod.item.custom;

import net.avizvul.esquissemod.menu.PencilCaseMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class PencilCaseItem extends Item {
    public PencilCaseItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Открываем GUI только на сервере (клиент откроет свой Screen автоматически при получении пакета от сервера)
        if (!level.isClientSide()) {
            player.openMenu(new SimpleMenuProvider(
                    (containerId, playerInventory, playerEntity) -> new PencilCaseMenu(containerId, playerInventory, stack),
                    Component.translatable("item.esquissemod.pencil_case")
            ));
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
