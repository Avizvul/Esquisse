package net.avizvul.esquissemod.block;

import net.avizvul.esquissemod.EsquisseMod;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(EsquisseMod.MOD_ID);

    // Регистрируем блок: без коллизии, без затенения света, мгновенно ломается
    public static final DeferredBlock<Block> SKETCHED_PAGE_BLOCK = BLOCKS.register("sketched_page_block",
            () -> new SketchedPageBlock(BlockBehaviour.Properties.of()
                    .noCollission()
                    .noOcclusion()
                    .instabreak()
                    .sound(net.minecraft.world.level.block.SoundType.WOOL
                    )
            )
    );

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}