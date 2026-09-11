package net.fodoth.skina.neoguanniao.mixin;

import net.fodoth.skina.neoguanniao.content.cage.BirdCageBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tschipp.carryon.common.carry.PlacementHandler;

import java.util.function.BiFunction;

/**
 * Carry On（Fabric 端）在 UseBlockCallback 里处理右键：只要玩家抱着东西，它就自己尝试把东西放在
 * 点击的位置上，成功返回 SUCCESS、失败返回 FAIL，两种情况都会吞掉这次交互，方块自己的
 * useWithoutItem 根本不会被调用（NeoForge 端有 RightClickBlock 事件可以抢先处理，Fabric 没有）。
 * 于是在 Carry On 尝试放置之前先看一眼点击的方块是不是鸟笼，是就把抱着的鸟装进笼子并直接返回成功。
 * <p>
 * 该 Mixin 只在安装了 Carry On 时生效，配置见 neoguanniao.fabric.carryon.mixins.json。
 * </p>
 */
@Mixin(PlacementHandler.class)
public abstract class PlacementHandlerMixin {
    @Inject(method = "tryPlaceEntity", at = @At("HEAD"), cancellable = true)
    private static void gt$storeCarriedIntoCage(ServerPlayer player, BlockPos pos, Direction direction,
                                                BiFunction<Vec3, Entity, Boolean> predicate,
                                                CallbackInfoReturnable<Boolean> cir) {
        if (BirdCageBlock.storeCarriedEntity(player.level(), pos, player)) {
            cir.setReturnValue(true);
        }
    }
}
