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
                        output.accept(ModItems.PENCIL_CASE);
                        output.accept(ModItems.PENCIL);
                        output.accept(ModItems.ERASER);
                        output.accept(ModItems.KNEADED_ERASER);
                        output.accept(ModItems.COLOR_PENCIL);
                        output.accept(ModItems.COLOR_MARKER);
                        output.accept(ModItems.RULER);
                        output.accept(ModItems.MAGNIFYING_GLASS);
                        output.accept(ModItems.SMUDGE);
                        output.accept(ModItems.DRAWING_COMPASS);

                        // --- НОВОЕ: Полностью заполненный цветной карандаш ---
                        ItemStack fullColorPencil = new ItemStack(ModItems.COLOR_PENCIL.get());
                        java.util.List<Integer> allColors = new java.util.ArrayList<>();
                        // Заполняем список всеми 16 ID цветов (от 0 до 15)
                        for (int i = 0; i < 16; i++) {
                            allColors.add(i);
                        }
                        // Применяем список цветов к предмету
                        fullColorPencil.set(net.avizvul.esquissemod.component.ModDataComponents.STORED_COLORS.get(), allColors);
                        fullColorPencil.set(net.avizvul.esquissemod.component.ModDataComponents.ACTIVE_COLOR_INDEX.get(), 0);

                        // Добавляем готовый предмет во вкладку
                        output.accept(fullColorPencil);
                        // -----------------------------------------------------

                        ItemStack fullMarker = new ItemStack(ModItems.COLOR_MARKER.get());
                        fullMarker.set(net.avizvul.esquissemod.component.ModDataComponents.STORED_COLORS.get(), allColors);
                        fullMarker.set(net.avizvul.esquissemod.component.ModDataComponents.ACTIVE_COLOR_INDEX.get(), 0);
                        output.accept(fullMarker);

                    }).build());
    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
