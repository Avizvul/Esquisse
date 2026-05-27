package net.avizvul.esquissemod.block.entity;

import net.avizvul.esquissemod.EsquisseMod;
import net.avizvul.esquissemod.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, EsquisseMod.MOD_ID);

    public static final Supplier<BlockEntityType<SketchedPageBlockEntity>> SKETCHED_PAGE_BE = BLOCK_ENTITIES.register("sketched_page_be",
            () -> BlockEntityType.Builder.of(SketchedPageBlockEntity::new, ModBlocks.SKETCHED_PAGE_BLOCK.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}