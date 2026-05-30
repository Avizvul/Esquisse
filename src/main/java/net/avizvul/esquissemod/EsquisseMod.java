package net.avizvul.esquissemod;

import net.avizvul.esquissemod.block.ModBlocks;
import net.avizvul.esquissemod.block.entity.ModBlockEntities;
import net.avizvul.esquissemod.component.ModDataComponents;
import net.avizvul.esquissemod.item.ModCreativeModeTabs;
import net.avizvul.esquissemod.item.ModItems;
import net.avizvul.esquissemod.menu.ModMenuTypes;
import net.avizvul.esquissemod.network.TearPagePayload;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.fml.event.lifecycle.ModLifecycleEvent;
import org.checkerframework.checker.units.qual.N;
import org.slf4j.Logger;

import net.avizvul.esquissemod.network.SketchbookPayloadHandler;
import net.avizvul.esquissemod.network.SketchbookSavePayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(EsquisseMod.MOD_ID)
public class EsquisseMod {
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "esquissemod";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "esquissemod" namespace


    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public EsquisseMod(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (EsquisseMod) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModCreativeModeTabs.register(modEventBus);
        ModItems.register(modEventBus);
        ModMenuTypes.register(modEventBus);


        // ---> ВЫЗОВ НАШЕГО РЕЕСТРА КОМПОНЕНТОВ ДАННЫХ <---
        ModDataComponents.register(modEventBus);

        modEventBus.addListener(this::registerPayloads);


        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void registerPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(MOD_ID);

        // Создаем ОДИН экземпляр обработчика для всех пакетов этого мода
        SketchbookPayloadHandler handler = new SketchbookPayloadHandler();

        // Регистрируем пакет для сохранения рисунка
        registrar.playToServer(
                SketchbookSavePayload.TYPE,
                SketchbookSavePayload.STREAM_CODEC,
                handler::handleData
        );

        // Регистрируем пакет для отрыва страницы
        registrar.playToServer(
                TearPagePayload.TYPE,
                TearPagePayload.STREAM_CODEC,
                handler::handleTearPage
        );

        registrar.playToServer(
                net.avizvul.esquissemod.network.ChangeColorPayload.TYPE,
                net.avizvul.esquissemod.network.ChangeColorPayload.STREAM_CODEC,
                handler::handleChangeColor
        );
    }

    private void commonSetup(FMLCommonSetupEvent event) {

    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {

    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }
}
