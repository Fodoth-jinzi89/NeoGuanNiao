package net.fodoth.skina.neoguanniao.platform.fabric;

import net.fodoth.skina.neoguanniao.content.nest.BirdNestBlockEntity;
import net.fodoth.skina.neoguanniao.content.nest.SimpleItemStackHandler;

public final class BirdNestInventoryHooksImpl {
    private BirdNestInventoryHooksImpl() {}

    public static SimpleItemStackHandler create(BirdNestBlockEntity nest) {
        return new SimpleItemStackHandler(4) {
            @Override
            public int getSlotLimit(int slot) { return 1; }
        };
    }
}
