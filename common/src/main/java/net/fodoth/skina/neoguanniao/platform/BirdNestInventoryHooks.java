package net.fodoth.skina.neoguanniao.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.fodoth.skina.neoguanniao.content.nest.BirdNestBlockEntity;
import net.fodoth.skina.neoguanniao.content.nest.SimpleItemStackHandler;

public final class BirdNestInventoryHooks {
    private BirdNestInventoryHooks() {}

    @ExpectPlatform
    public static SimpleItemStackHandler create(BirdNestBlockEntity nest) { throw new AssertionError(); }
}
