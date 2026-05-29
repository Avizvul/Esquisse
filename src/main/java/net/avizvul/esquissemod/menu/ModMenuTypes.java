package net.avizvul.esquissemod.menu;

import net.avizvul.esquissemod.EsquisseMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, EsquisseMod.MOD_ID);

    public static final Supplier<MenuType<PencilCaseMenu>> PENCIL_CASE_MENU = MENUS.register("pencil_case_menu",
            () -> new MenuType<>(PencilCaseMenu::new, net.minecraft.world.flag.FeatureFlags.DEFAULT_FLAGS));

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}