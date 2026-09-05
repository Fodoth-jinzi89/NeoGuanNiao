package net.fodoth.skina.neoguanniao;

import net.fabricmc.api.ModInitializer;
import net.fodoth.skina.neoguanniao.registry.*;

public final class NeoGuanNiaoFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        NeoGuanNiaoBlocks.BLOCKS.register();
        NeoGuanNiaoItems.ITEMS.register();
        NeoGuanNiaoSoundEvents.SOUND_EVENTS.register();
        NeoGuanNiaoRecipeSerializers.RECIPE_SERIALIZERS.register();
        NeoGuanNiaoEntityTypes.ENTITY_TYPES.register();
        NeoGuanNiaoBlockEntityTypes.BLOCK_ENTITY_TYPES.register();
        NeoGuanNiaoParticleTypes.PARTICLE_TYPES.register();
        NeoGuanNiaoCreativeTabs.CREATIVE_MODE_TABS.register();
        NeoGuanNiaoFabricSpawns.register();
    }
}
