package net.avizvul.esquissemod.item.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.avizvul.esquissemod.client.ClientHooks;
import net.minecraft.world.level.block.state.BlockState;

public class SketchedPageItem extends Item {

    public SketchedPageItem(Properties properties) {
        super(properties);
    }

    @Override
    public net.minecraft.world.InteractionResult useOn(net.minecraft.world.item.context.UseOnContext context) {
        net.minecraft.world.entity.player.Player player = context.getPlayer();

        // Проверяем, что игрок зажал Shift
        if (player != null && player.isShiftKeyDown()) {
            Level level = context.getLevel();
            BlockPos placePos = context.getClickedPos().relative(context.getClickedFace());

            // Проверяем, не занято ли место другим блоком
            if (!level.getBlockState(placePos).canBeReplaced()) {
                return net.minecraft.world.InteractionResult.FAIL;
            }

            // Получаем конкретную сторону блока, по которой кликнули (например, UP - это пол, NORTH - стена и т.д.)
            net.minecraft.core.Direction clickedFace = context.getClickedFace();

            // Прикрепляем рисунок ровно к этой стороне
            BlockState state = net.avizvul.esquissemod.block.ModBlocks.SKETCHED_PAGE_BLOCK.get().defaultBlockState()
                    .setValue(net.avizvul.esquissemod.block.SketchedPageBlock.FACING, clickedFace);

            level.setBlock(placePos, state, 3); // Ставим блок в мире

            // Достаём свежепоставленный BlockEntity и перекачиваем в него рисунок из предмета
            net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(placePos);
            if (be instanceof net.avizvul.esquissemod.block.entity.SketchedPageBlockEntity pageEntity) {
                net.avizvul.esquissemod.component.SketchData data = context.getItemInHand().get(net.avizvul.esquissemod.component.ModDataComponents.PAGE_DATA.get());
                if (data != null) {
                    pageEntity.setSketchData(data); // Передаем данные!
                }
            }

            // Тратим 1 листок из инвентаря
            context.getItemInHand().shrink(1);

            return net.minecraft.world.InteractionResult.sidedSuccess(level.isClientSide());
        }

        // Если Shift НЕ нажат, пропускаем этот шаг (будет вызван метод use для открытия GUI)
        return net.minecraft.world.InteractionResult.PASS;
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