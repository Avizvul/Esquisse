package net.avizvul.esquissemod.item;

import net.avizvul.esquissemod.EsquisseMod;
import net.avizvul.esquissemod.component.ModDataComponents; // Убедитесь, что импорт правильный для вашего компонента
import net.avizvul.esquissemod.item.custom.ColorPencilItem;
import net.avizvul.esquissemod.item.custom.PencilCaseItem;
import net.avizvul.esquissemod.item.custom.SketchbookItem;
import net.avizvul.esquissemod.item.custom.SketchedPageItem;
import net.avizvul.esquissemod.item.custom.base.DrawingToolItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(EsquisseMod.MOD_ID);

    public static final DeferredItem<Item> SKETCHBOOK = ITEMS.register("sketchbook",
            () -> new SketchbookItem(new Item.Properties()
                    .stacksTo(1)
                    // --- НОВОЕ: Привязываем 16 пустых страниц к скетчбуку по умолчанию ---
                    .component(ModDataComponents.SKETCHBOOK_PAGES.get(), createBlankPages(16))
            ));

    // Предмет изрисованной страницы (использует кастомный класс, который мы напишем ниже)
    public static final DeferredItem<Item> SKETCHED_PAGE = ITEMS.register("sketched_page",
            () -> new SketchedPageItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> COLOR_PENCIL = ITEMS.register("color_pencil",
            () -> new ColorPencilItem(new Item.Properties()
                    .durability(256)
                    // Инициализируем пустой список цветов и нулевой индекс при выдаче предмета
                    .component(ModDataComponents.STORED_COLORS.get(), new java.util.ArrayList<>())
                    .component(ModDataComponents.ACTIVE_COLOR_INDEX.get(), 0
                    )
            )
    );

    public static final DeferredItem<Item> PENCIL = ITEMS.register("pencil",
            () -> new DrawingToolItem(new Item.Properties().durability(256)) {
                @Override
                public net.minecraft.resources.ResourceLocation getGuiTexture() {
                    return net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(net.avizvul.esquissemod.EsquisseMod.MOD_ID, "textures/gui/button_pencil.png");
                }

                // Если карандаш длиннее/шире, просто переопределяем методы:
                @Override
                public int getGuiWidth() { return 16; } // Например, оставим 16

                @Override
                public int getPeekHeight() { return 12; } // Пусть обычный карандаш выглядывает сильнее!
            });

    public static final DeferredItem<Item> ERASER = ITEMS.register("eraser",
            () -> new DrawingToolItem(new Item.Properties().durability(256)) {
                @Override
                public net.minecraft.resources.ResourceLocation getGuiTexture() {
                    return net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(net.avizvul.esquissemod.EsquisseMod.MOD_ID, "textures/gui/button_eraser.png");
                }

                @Override
                public int getPeekHeight() { return 6; } // А ластик пусть торчит поменьше
            });

    public static final DeferredItem<Item> EMPTY_PAGE = ITEMS.register("empty_page",
            () -> new Item(new Item.Properties().stacksTo(16)));

    public static final DeferredItem<Item> RULER = ITEMS.register("ruler",
            () -> new Item(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> PENCIL_CASE = ITEMS.register("pencil_case",
            () -> new PencilCaseItem(new Item.Properties().stacksTo(1)));

    public net.minecraft.resources.ResourceLocation getGuiTexture() {
        // Базовая текстура без цвета (цвет мы всё так же будем накладывать поверх неё в GUI)
        return net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(net.avizvul.esquissemod.EsquisseMod.MOD_ID, "textures/gui/button_color_pencil.png");
    }


    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }

    // Метод для создания стартового набора пустых страниц
    private static java.util.List<net.avizvul.esquissemod.component.SketchData> createBlankPages(int count) {
        java.util.List<net.avizvul.esquissemod.component.SketchData> list = new java.util.ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            // ВАЖНО: Замените 64 и 128 на ВАШИ реальные размеры холста,
            // иначе при подгрузке пустой страницы игра вылетит из-за несовпадения размеров!
            int[][] emptyPixels = new int[64][128];

            list.add(net.avizvul.esquissemod.component.SketchData.fromArray(emptyPixels));
        }

        return list;
    }
}