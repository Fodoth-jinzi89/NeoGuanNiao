package net.fodoth.skina.neoguanniao.network;

import net.minecraft.world.entity.player.Player;

/** Platform context for packet handlers; work must execute on the game thread. */
public interface PayloadContext {
    Player player();
    void enqueueWork(Runnable work);
}
