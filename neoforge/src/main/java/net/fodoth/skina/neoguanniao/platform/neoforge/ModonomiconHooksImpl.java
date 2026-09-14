package net.fodoth.skina.neoguanniao.platform.neoforge;

import com.klikli_dev.modonomicon.item.ModonomiconItem;
import com.klikli_dev.modonomicon.registry.DataComponentRegistry;
import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.minecraft.world.item.Item;

public final class ModonomiconHooksImpl {
    private ModonomiconHooksImpl() {}

    public static Item birdGuide() {
        return new ModonomiconItem(new Item.Properties()
                .stacksTo(1)
                .component(DataComponentRegistry.BOOK_ID.get(), NeoGuanNiao.resource("bird_guide")));
    }
}
