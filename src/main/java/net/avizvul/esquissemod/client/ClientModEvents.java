package net.avizvul.esquissemod.client;

import net.avizvul.esquissemod.EsquisseMod;
import net.avizvul.esquissemod.client.tooltip.ClientSketchedPageTooltip;
import net.avizvul.esquissemod.client.tooltip.SketchedPageTooltipData;
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
}