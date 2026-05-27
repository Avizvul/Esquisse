package net.avizvul.esquissemod.item;

import net.avizvul.esquissemod.EsquisseMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, EsquisseMod.MOD_ID);


    public static final Supplier<CreativeModeTab> ART_SUPPLIES = CREATIVE_MODE_TAB.register("art_supplies",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.SKETCHBOOK.get()))
                    .title(Component.translatable("creativetab.esquissemod.art_supplies"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModItems.SKETCHBOOK);
                        output.accept(ModItems.PENCIL);
                        output.accept(ModItems.ERASER);
                        output.accept(ModItems.COLOR_PENCIL);
                    }).build());
    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
