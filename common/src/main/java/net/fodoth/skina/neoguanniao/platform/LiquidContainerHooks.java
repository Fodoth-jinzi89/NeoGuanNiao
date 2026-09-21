package net.fodoth.skina.neoguanniao.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.fodoth.skina.neoguanniao.content.cage.SimpleFluidTank;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

/**
 * 把鸟笼水槽里的流体抽进玩家手里的流体容器。
 * <p>
 * 抽取量取「槽里剩下的」和「容器剩余容量」的较小值；容器按自己的单位容量装填（原版桶只能整桶装），
 * 装不下、流体种类对不上、手里的东西根本不是流体容器时都不算数。
 * </p>
 */
public final class LiquidContainerHooks {
    private LiquidContainerHooks() {}

    /**
     * @param simulate 只试算、不改动任何东西（客户端用它决定这次右键要不要吃掉）
     * @return 实际（或试算）抽出的 mB 数，0 表示抽不走
     */
    @ExpectPlatform
    public static int drainIntoContainer(@NotNull SimpleFluidTank tank, @NotNull Player player,
                                         @NotNull InteractionHand hand, boolean simulate) {
        throw new AssertionError();
    }
}
