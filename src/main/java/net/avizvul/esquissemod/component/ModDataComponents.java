package net.avizvul.esquissemod.component;

import com.mojang.serialization.Codec;
import net.avizvul.esquissemod.EsquisseMod;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Supplier;

public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, EsquisseMod.MOD_ID);

    // НОВЫЙ КОМПОНЕНТ ДЛЯ 16 СТРАНИЦ
    public static final Supplier<DataComponentType<List<SketchData>>> SKETCHBOOK_PAGES =
            DATA_COMPONENT_TYPES.register("sketchbook_pages", () ->
                    DataComponentType.<List<SketchData>>builder()
                            // Используем .listOf(), чтобы игра поняла, что это список объектов SketchData [1]
                            .persistent(SketchData.CODEC.listOf())
                            .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.collection(java.util.ArrayList::new, SketchData.STREAM_CODEC))
                            .build()
            );

    // Компонент пикселей до поломки
    public static final Supplier<DataComponentType<Integer>> UNSPENT_PIXELS = DATA_COMPONENT_TYPES.register("unspent_pixels", () ->
                    DataComponentType.<Integer>builder()
                            .persistent(Codec.INT)
                            .build()
            );

    // --- КОМПОНЕНТЫ НАСТРОЕК ИНСТРУМЕНТА (Размер и Жесткость) ---
    public static final Supplier<DataComponentType<Integer>> BRUSH_SIZE =
            DATA_COMPONENT_TYPES.register("brush_size", () ->
                    DataComponentType.<Integer>builder()
                            .persistent(com.mojang.serialization.Codec.INT)
                            .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.INT)
                            .build()
            );

    public static final Supplier<DataComponentType<Integer>> BRUSH_HARDNESS =
            DATA_COMPONENT_TYPES.register("brush_hardness", () ->
                    DataComponentType.<Integer>builder()
                            .persistent(com.mojang.serialization.Codec.INT)
                            .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.INT)
                            .build()
            );

    // Хранит список ID загруженных красителей (от 0 до 15)
    public static final Supplier<net.minecraft.core.component.DataComponentType<java.util.List<Integer>>> STORED_COLORS =
            DATA_COMPONENT_TYPES.register("stored_colors", () ->
                    net.minecraft.core.component.DataComponentType.<java.util.List<Integer>>builder()
                            .persistent(com.mojang.serialization.Codec.INT.listOf()) // Автоматически создаст и сетевой кодек [3]
                            .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.collection(java.util.ArrayList::new, net.minecraft.network.codec.ByteBufCodecs.INT))
                            .build()
            );

    // Хранит индекс текущего выбранного цвета из списка STORED_COLORS
    public static final Supplier<net.minecraft.core.component.DataComponentType<Integer>> ACTIVE_COLOR_INDEX =
            DATA_COMPONENT_TYPES.register("active_color_index", () ->
                    net.minecraft.core.component.DataComponentType.<Integer>builder()
                            .persistent(com.mojang.serialization.Codec.INT)
                            .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.INT)
                            .build()
            );

    // --- КОМПОНЕНТЫ ЦИРКУЛЯ ---
    public static final Supplier<DataComponentType<Integer>> COMPASS_ANCHOR_X =
            DATA_COMPONENT_TYPES.register("compass_anchor_x", () ->
                    DataComponentType.<Integer>builder()
                            .persistent(com.mojang.serialization.Codec.INT)
                            .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.INT)
                            .build()
            );

    public static final Supplier<DataComponentType<Integer>> COMPASS_ANCHOR_Y =
            DATA_COMPONENT_TYPES.register("compass_anchor_y", () ->
                    DataComponentType.<Integer>builder()
                            .persistent(com.mojang.serialization.Codec.INT)
                            .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.INT)
                            .build()
            );

    public static final Supplier<DataComponentType<Boolean>> COMPASS_IS_DRAWING =
            DATA_COMPONENT_TYPES.register("compass_is_drawing", () ->
                    DataComponentType.<Boolean>builder()
                            .persistent(com.mojang.serialization.Codec.BOOL)
                            .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.BOOL)
                            .build()
            );

    // --- КОМПОНЕНТ ДЛЯ ЗАПОМИНАНИЯ ПОСЛЕДНЕЙ СТРАНИЦЫ ---
    public static final Supplier<DataComponentType<Integer>> LAST_PAGE =
            DATA_COMPONENT_TYPES.register("last_page", () ->
                    DataComponentType.<Integer>builder()
                            .persistent(Codec.INT) // Сохранение в файл мира
                            .networkSynchronized(ByteBufCodecs.INT) // Синхронизация между сервером и клиентом
                            .build()
            );

    // ---  Компонент для хранения рисунка на оторванной странице ---
    public static final Supplier<DataComponentType<SketchData>> PAGE_DATA =
            DATA_COMPONENT_TYPES.register("page_data", () ->
                    DataComponentType.<SketchData>builder()
                            .persistent(SketchData.CODEC)
                            .networkSynchronized(SketchData.STREAM_CODEC)
                            .build()
            );

    public static void register(IEventBus eventBus) {
        DATA_COMPONENT_TYPES.register(eventBus);
    }
}