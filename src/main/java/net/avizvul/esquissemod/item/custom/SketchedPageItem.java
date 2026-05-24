package net.avizvul.esquissemod.item.custom;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.avizvul.esquissemod.client.ClientHooks;

public class SketchedPageItem extends Item {
    public SketchedPageItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Открываем GUI только на стороне клиента
        if (level.isClientSide()) { // [6]
            ClientHooks.openSketchedPageScreen(stack);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide()); // [7]
    }
}