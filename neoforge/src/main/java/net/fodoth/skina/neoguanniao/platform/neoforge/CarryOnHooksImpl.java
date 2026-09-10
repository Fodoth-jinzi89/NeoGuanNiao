package net.fodoth.skina.neoguanniao.platform.neoforge;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import tschipp.carryon.Constants;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnDataManager;
import tschipp.carryon.common.carry.PickupHandler;

public final class CarryOnHooksImpl {
    private CarryOnHooksImpl() {}

    public static boolean tryCarryEntity(Player player, Entity entity) {
        return player instanceof ServerPlayer serverPlayer
                && PickupHandler.tryPickupEntity(serverPlayer, entity, picked -> true);
    }

    public static @Nullable Entity carriedEntity(Player player) {
        if (!(player instanceof ServerPlayer)) return null;
        CarryOnData data = CarryOnDataManager.getCarryData(player);
        // 只有抱着实体时才能调用 getEntity，抱着方块/玩家时它会抛 IllegalStateException。
        return data.isCarrying(CarryOnData.CarryType.ENTITY) ? data.getEntity(player.level()) : null;
    }

    public static void clearCarriedEntity(Player player) {
        if (!(player instanceof ServerPlayer)) return;
        CarryOnData data = CarryOnDataManager.getCarryData(player);
        data.clear();
        // clear() 只改服务端数据，必须再 setCarryData 一次才会给客户端发包，
        // 否则客户端手里的实体要等下一次 Carry On 同步才消失。
        CarryOnDataManager.setCarryData(player, data);
        // Carry On 抱起时会挂 1e8 tick 的缓慢，正常放下时才会移除；装笼绕过了那段逻辑，这里按同样的条件补一次。
        if (!player.isCreative() || Constants.COMMON_CONFIG.settings.slownessInCreative) {
            player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        }
    }
}
