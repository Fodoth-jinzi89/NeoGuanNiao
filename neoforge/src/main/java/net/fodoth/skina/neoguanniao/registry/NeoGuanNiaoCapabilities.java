package net.fodoth.skina.neoguanniao.registry;

import net.fodoth.skina.neoguanniao.content.cage.BirdCageBlock;
import net.fodoth.skina.neoguanniao.content.cage.BirdCageBlockEntity;
import net.fodoth.skina.neoguanniao.content.nest.BirdNestBlockEntity;
import net.fodoth.skina.neoguanniao.platform.neoforge.BirdCageFluidHandler;
import net.fodoth.skina.neoguanniao.platform.neoforge.BirdCageItemHandler;
import net.fodoth.skina.neoguanniao.platform.neoforge.BirdNestInventoryHooksImpl;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class NeoGuanNiaoCapabilities {

    public static void register(RegisterCapabilitiesEvent event) {

        if (NeoGuanNiaoBlockEntityTypes.BIRD_NEST.isPresent()) {
            event.registerBlockEntity(
                    Capabilities.ItemHandler.BLOCK,
                    NeoGuanNiaoBlockEntityTypes.BIRD_NEST.get(),
                    (blockEntity, side) -> {
                        if (blockEntity instanceof BirdNestBlockEntity nest) {
                            return BirdNestInventoryHooksImpl.itemHandler(nest);
                        }
                        return null;
                    }
            );
        }

        // 鸟笼：三种规格都有容器槽位。注册在方块上而不是方块实体上，因为中/大型鸟笼的占位方块没有
        // 方块实体，接在占位方块上的漏斗/管道也要能查到存储——这里统一先回到原点那一格。
        if (NeoGuanNiaoBlockEntityTypes.BIRD_CAGE.isPresent()) {
            Block[] cageBlocks = {
                    NeoGuanNiaoBlocks.SMALL_BIRD_CAGE.get(),
                    NeoGuanNiaoBlocks.MEDIUM_BIRD_CAGE.get(),
                    NeoGuanNiaoBlocks.LARGE_BIRD_CAGE.get()
            };
            event.registerBlock(
                    Capabilities.ItemHandler.BLOCK,
                    (level, pos, state, blockEntity, side) -> {
                        BirdCageBlockEntity cage = BirdCageBlock.cageAt(level, pos, state);
                        return cage != null && cage.supportsCageStorage() ? new BirdCageItemHandler(cage) : null;
                    },
                    cageBlocks
            );
            event.registerBlock(
                    Capabilities.FluidHandler.BLOCK,
                    (level, pos, state, blockEntity, side) -> {
                        BirdCageBlockEntity cage = BirdCageBlock.cageAt(level, pos, state);
                        return cage != null && cage.supportsCageStorage() ? new BirdCageFluidHandler(cage) : null;
                    },
                    cageBlocks
            );
        }
    }
}
