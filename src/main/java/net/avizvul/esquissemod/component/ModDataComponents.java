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
                            .build()
            );

    // Компонент пикселей до поломки
    public static final Supplier<DataComponentType<Integer>> UNSPENT_PIXELS =
            DATA_COMPONENT_TYPES.register("unspent_pixels", () ->
                    DataComponentType.<Integer>builder()
                            .persistent(Codec.INT)
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
//test
    public static void register(IEventBus eventBus) {
        DATA_COMPONENT_TYPES.register(eventBus);
    }
}