package net.avizvul.esquissemod.client;

import net.avizvul.esquissemod.EsquisseMod;
import net.avizvul.esquissemod.client.tooltip.ClientSketchedPageTooltip;
import net.avizvul.esquissemod.client.tooltip.SketchedPageTooltipData;
import net.avizvul.esquissemod.item.ModItems;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;

// Аннотация @EventBusSubscriber автоматически зарегистрирует этот класс на клиентской шине мода [3]
@EventBusSubscriber(modid = EsquisseMod.MOD_ID, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
        // Говорим игре: "Когда встретишь SketchedPageTooltipData, используй ClientSketchedPageTooltip для отрисовки"
        event.register(SketchedPageTooltipData.class, ClientSketchedPageTooltip::new);
    }

    @SubscribeEvent
    public static void registerItemColors(net.neoforged.neoforge.client.event.RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> {
            // tintIndex соответствует слоям текстуры в JSON модели.
            // layer0 (корпус) имеет индекс 0, layer1 (грифель) имеет индекс 1.

            if (tintIndex == 1) { // Красим ТОЛЬКО слой грифеля (layer1)
                if (stack.has(net.avizvul.esquissemod.component.ModDataComponents.STORED_COLORS.get())) {
                    java.util.List<Integer> colors = stack.getOrDefault(net.avizvul.esquissemod.component.ModDataComponents.STORED_COLORS.get(), new java.util.ArrayList<>());

                    if (!colors.isEmpty()) {
                        int activeIndex = stack.getOrDefault(net.avizvul.esquissemod.component.ModDataComponents.ACTIVE_COLOR_INDEX.get(), 0);
                        int colorId = colors.get(activeIndex % colors.size());

                        // Возвращаем цвет красителя в формате RGB.
                        // Minecraft сам наложит этот цвет на белую текстуру грифеля.
                        return net.minecraft.world.item.DyeColor.byId(colorId).getTextColor();
                    }
                }
                // Если красителей внутри нет, возвращаем белый (или серый) цвет по умолчанию
                return 0xFFDDDDDD;
            }

            // Для слоя 0 (деревянный корпус) возвращаем -1, чтобы игра отрисовала его как есть, без фильтров
            return -1;
        }, ModItems.COLOR_PENCIL.get());
    }

    @SubscribeEvent
    public static void registerBER(net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(net.avizvul.esquissemod.block.entity.ModBlockEntities.SKETCHED_PAGE_BE.get(),
                net.avizvul.esquissemod.client.render.SketchedPageBlockEntityRenderer::new);
    }
}