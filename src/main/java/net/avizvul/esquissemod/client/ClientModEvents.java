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
            // Красим ТОЛЬКО слой грифеля (layer1)
            if (tintIndex == 1) {
                if (stack.has(net.avizvul.esquissemod.component.ModDataComponents.STORED_COLORS.get())) {
                    java.util.List<Integer> colors = stack.getOrDefault(net.avizvul.esquissemod.component.ModDataComponents.STORED_COLORS.get(), new java.util.ArrayList<>());

                    if (!colors.isEmpty()) {
                        int activeIndex = stack.getOrDefault(net.avizvul.esquissemod.component.ModDataComponents.ACTIVE_COLOR_INDEX.get(), 0);

                        // Защита от сбоя рендера (Math.abs исключает отрицательные индексы)
                        int safeIndex = Math.abs(activeIndex) % colors.size();
                        int colorId = colors.get(safeIndex);

                        // ИСПРАВЛЕНИЕ 1: ПРАВИЛЬНЫЙ МЕТОД - getTextureDiffuseColor()
                        // ИСПРАВЛЕНИЕ 2: Обязательно накладываем маску | 0xFF000000 для полной непрозрачности
                        return net.minecraft.world.item.DyeColor.byId(colorId).getTextureDiffuseColor() | 0xFF000000;
                    }
                }
                // Цвет по умолчанию для пустого карандаша (непрозрачный серый)
                return 0xFFDDDDDD;
            }

            // Для деревянного корпуса (layer0) возвращаем -1, чтобы игра его не фильтровала
            return -1;
        }, net.avizvul.esquissemod.item.ModItems.COLOR_PENCIL.get());
    }

    @SubscribeEvent
    public static void registerBER(net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(net.avizvul.esquissemod.block.entity.ModBlockEntities.SKETCHED_PAGE_BE.get(),
                net.avizvul.esquissemod.client.render.SketchedPageBlockEntityRenderer::new);
    }

    @SubscribeEvent
    public static void registerScreens(net.neoforged.neoforge.client.event.RegisterMenuScreensEvent event) {
        event.register(net.avizvul.esquissemod.menu.ModMenuTypes.PENCIL_CASE_MENU.get(), net.avizvul.esquissemod.client.screen.PencilCaseScreen::new);
    }
}