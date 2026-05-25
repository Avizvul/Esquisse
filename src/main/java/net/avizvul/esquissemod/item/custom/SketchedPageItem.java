package net.avizvul.esquissemod.item.custom;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.avizvul.esquissemod.client.ClientHooks;

public class SketchedPageItem extends Item {

    public SketchedPageItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // ВАЖНО: Вызываем код графического интерфейса ТОЛЬКО если мы на клиенте (isClientSide = true)
        if (level.isClientSide()) {
            // Безопасный вызов изолированного клиентского кода [5, 6]
            ClientHooks.openSketchedPageScreen(stack);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    // 1. Метод для обычного текста (как в уроках)
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<net.minecraft.network.chat.Component> tooltipComponents, net.minecraft.world.item.TooltipFlag tooltipFlag) {
        // Добавляем серый текст-подсказку
        tooltipComponents.add(net.minecraft.network.chat.Component.translatable("item.esquissemod.sketched_page.tooltip").withStyle(net.minecraft.ChatFormatting.GRAY));
    }

    // 2. Метод для передачи картинки (Возвращаем наш новый класс)
    @Override
    public java.util.Optional<net.minecraft.world.inventory.tooltip.TooltipComponent> getTooltipImage(ItemStack stack) {
        net.avizvul.esquissemod.component.SketchData data = stack.get(net.avizvul.esquissemod.component.ModDataComponents.PAGE_DATA.get());

        // Если рисунок есть и он не пустой, передаем его в тултип
        if (data != null && !data.isEmpty()) {
            return java.util.Optional.of(new net.avizvul.esquissemod.client.tooltip.SketchedPageTooltipData(data));
        }
        return java.util.Optional.empty();
    }
}