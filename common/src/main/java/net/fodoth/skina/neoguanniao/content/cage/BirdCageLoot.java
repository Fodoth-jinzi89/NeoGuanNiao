package net.fodoth.skina.neoguanniao.content.cage;

import net.fodoth.skina.neoguanniao.platform.CageLootHooks;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoItemTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 从鸟笼里往外掏东西的共用逻辑：产量槽里的鸟羽、食盆里的鸟食。
 * <p>
 * 掉落位置和 goldentweaks 对 Lootr 容器的处理保持一致（方块中心沿某方向偏 0.6，也就是那一面再往外 0.1），
 * 生成后如果 goldentweaks 的右键拾取开着就交给它，否则就留在地上。
 * </p>
 */
public final class BirdCageLoot {

    /** 掉落物离方块中心的距离。 */
    private static final double DROP_OFFSET = 0.6D;

    /** 点击判定范围（方块）：盆的半宽乘这个比例再加一点余量，所以大型判得比中型宽、中型比小型宽。 */
    private static final double POT_RADIUS_SCALE = 1.5D;
    private static final double POT_RADIUS_MARGIN = 0.10D;

    /** 产量槽是物品栏里排在食物槽后面的那几格。 */
    private static int outputSlotFirst(BirdCageBlockEntity cage) {
        return cage.outputSlotFirst();
    }

    private static int outputSlotCount(BirdCageBlockEntity cage) {
        return cage.outputSlotCount();
    }


    private BirdCageLoot() {
    }


    /** 产量槽里还有没有东西可掏。 */
    public static boolean hasOutput(BirdCageBlockEntity cage) {
        int first = outputSlotFirst(cage);
        for (int slot = 0; slot < outputSlotCount(cage); slot++) {
            if (!cage.itemHandler().getStackInSlot(first + slot).isEmpty()) return true;
        }
        return false;
    }


    /** 这个食槽里还有没有食物。 */
    public static boolean hasFood(BirdCageBlockEntity cage, int slot) {
        return !cage.itemHandler().getStackInSlot(slot).isEmpty();
    }


    /** 手里的东西算不算鸟食：和自动化进料口用的是同一套标签。 */
    public static boolean isFood(ItemStack stack) {
        return stack.is(NeoGuanNiaoItemTags.BIRD_FOOD) || stack.is(NeoGuanNiaoItemTags.BIRD_FOOD_FISH);
    }


    /**
     * 把手里的鸟食放进食槽。
     * <p>
     * 只动 {@code slot} 这一个槽（点哪个盆就动哪个槽）：槽空着、或装着同一种食物但没满时才放得进去，
     * 单槽上限按规格来（小型 16 / 中型 32 / 大型 64），创造模式不消耗手里的东西。
     * </p>
     *
     * @return 是否放进去了一些
     */
    public static boolean addFood(BirdCageBlockEntity cage, Player player, ItemStack stack, int slot) {
        if (slot < 0 || slot >= cage.foodSlotCount()) return false;
        ItemStack existing = cage.itemHandler().getStackInSlot(slot);
        if (!existing.isEmpty() && !ItemStack.isSameItemSameComponents(existing, stack)) return false;

        int moved = Math.min(cage.foodStackLimit() - existing.getCount(), stack.getCount());
        if (moved <= 0) return false;

        if (existing.isEmpty()) {
            cage.itemHandler().setStackInSlot(slot, stack.copyWithCount(moved));
        } else {
            existing.grow(moved);
        }
        if (!player.getAbilities().instabuild) stack.shrink(moved);
        cage.setChanged();
        return true;
    }


    /**
     * 空手取出产量槽里的鸟羽。
     * <p>
     * 一次最多取 {@link CageLootHooks#pickupGroups()} 组（0 表示不限）；没装 goldentweaks 时就是每次一组。
     * 物品落在点击的那一面外侧。
     * </p>
     */
    public static boolean takeOutput(Level level, BlockPos origin, BirdCageBlockEntity cage,
                                     Player player, Direction face) {
        return takeStacks(level, cage, player, outputSlotFirst(cage), outputSlotCount(cage),
                CageLootHooks.pickupGroups(), dropAtFace(origin, face));
    }


    /** 空手取出某个食槽里的鸟食：一次一组，物品从点中的那只盆那一侧飞到笼子外面。 */
    public static boolean takeFood(Level level, BlockPos origin, BirdCageBlockEntity cage,
                                   Player player, Direction facing, Direction face, int slot) {
        BirdCageVariant.PotLayer pot = potForSlot(cage, slot, true);
        Vec3 drop = pot == null ? dropAtFace(origin, face) : dropOutsidePot(origin, facing, pot);
        return takeStacks(level, cage, player, slot, 1, 1, drop);
    }


    /** 某个槽对应的是哪只盆：大型同一层有两只食盆（或两只水盆），靠 {@code storageIndex} 区分。 */
    private static BirdCageVariant.PotLayer potForSlot(BirdCageBlockEntity cage, int slot, boolean food) {
        for (BirdCageVariant.PotLayer pot : cage.variant().potLayers()) {
            if (pot.food() == food && pot.storageIndex() == slot) return pot;
        }
        return null;
    }


    /** 点到的盆和它对应的槽：{@code food} 选食物盆还是水盆，没点中任何盆时返回 null。 */
    @Nullable
    public static BirdCageVariant.PotLayer potAt(BirdCageBlockEntity cage, Direction facing, BlockPos origin,
                                                 BlockHitResult hit, boolean food) {
        for (BirdCageVariant.PotLayer pot : cage.variant().potLayers()) {
            if (pot.food() == food && hitsPot(facing, origin, hit, pot)) return pot;
        }
        return null;
    }


    /** 这个盆的点击判定半径：按盆自己的半宽算，盆越大判得越宽（大型 > 中型 > 小型）。 */
    private static double potRadius(BirdCageVariant.PotLayer pot) {
        double halfX = (pot.maxX() - pot.minX()) / 2.0D / 16.0D;
        double halfZ = (pot.maxZ() - pot.minZ()) / 2.0D / 16.0D;
        return Math.max(halfX, halfZ) * POT_RADIUS_SCALE + POT_RADIUS_MARGIN;
    }


    /**
     * 点到的盆：水平方向按盆的大小判，纵向放宽到盆所在那一格方块的范围——盆挂在笼壁上，玩家多半是
     * 从上面隔着笼子点或者从旁边点笼壁，按盆真正的高度严格要求会点不中。
     */
    private static boolean hitsPot(Direction facing, BlockPos origin, BlockHitResult hit, BirdCageVariant.PotLayer pot) {
        Vec3 offset = potCenterOffset(facing, pot);
        double dx = hit.getLocation().x - (origin.getX() + 0.5D + offset.x);
        double dz = hit.getLocation().z - (origin.getZ() + 0.5D + offset.z);
        double radius = potRadius(pot);
        if (dx * dx + dz * dz > radius * radius) return false;

        double halfY = (pot.rimY() - pot.floorY()) / 2.0D / 16.0D;
        double centerY = origin.getY() + (pot.floorY() + pot.rimY()) / 2.0D / 16.0D;
        return Math.abs(hit.getLocation().y - centerY) <= 0.5D + halfY;
    }


    /**
     * 从 {@code slotCount} 个槽里各取一整组，最多取 {@code limit} 组（0 表示不限）。
     * 每取出一组就在 {@code dropPos} 生成一个掉落物，并交给 goldentweaks 的右键拾取（如果它开着）。
     */
    private static boolean takeStacks(Level level, BirdCageBlockEntity cage, Player player,
                                      int firstSlot, int slotCount, int limit, Vec3 dropPos) {
        List<ItemStack> taken = new ArrayList<>();
        for (int slot = 0; slot < slotCount && (limit == 0 || taken.size() < limit); slot++) {
            ItemStack stack = cage.itemHandler().getStackInSlot(firstSlot + slot);
            if (stack.isEmpty()) continue;
            cage.itemHandler().setStackInSlot(firstSlot + slot, ItemStack.EMPTY);
            taken.add(stack);
        }
        if (taken.isEmpty()) return false;

        cage.setChanged();
        for (ItemStack stack : taken) {
            ItemEntity entity = new ItemEntity(level, dropPos.x, dropPos.y, dropPos.z, stack);
            level.addFreshEntity(entity);
            CageLootHooks.tryPickup(player, entity);
        }
        return true;
    }


    /** 点击面中心再往外一点的掉落位置。 */
    private static Vec3 dropAtFace(BlockPos origin, Direction face) {
        return new Vec3(origin.getX() + 0.5D + face.getStepX() * DROP_OFFSET,
                origin.getY() + 0.5D + face.getStepY() * DROP_OFFSET,
                origin.getZ() + 0.5D + face.getStepZ() * DROP_OFFSET);
    }


    /**
     * 盆那一侧、笼子外面的掉落位置：沿「方块中心 → 盆中心」的方向从盆中心再往外 {@link #DROP_OFFSET}，
     * 高度取盆口（大型的盆高一两格）。
     * <p>
     * 不能像 {@link #dropAtFace} 那样只按方块中心偏 0.6：中/大型鸟笼占地 3×3，外墙离中心有一格半，
     * 这点距离还在笼子里面（从外面看就是贴着对面的内壁弹出来）。盆本身已经贴在笼壁内侧，
     * 以盆中心为起点再往外这一段才跨得出外墙。
     * </p>
     */
    private static Vec3 dropOutsidePot(BlockPos origin, Direction facing, BirdCageVariant.PotLayer pot) {
        Vec3 toPot = potCenterOffset(facing, pot);
        double length = toPot.horizontalDistance();
        Vec3 outward = length <= 0.0D ? Vec3.ZERO : toPot.scale((length + DROP_OFFSET) / length);
        return new Vec3(origin.getX() + 0.5D + outward.x,
                origin.getY() + pot.rimY() / 16.0D,
                origin.getZ() + 0.5D + outward.z);
    }


    /**
     * 盆中心在方块内的水平偏移（相对方块中心）。
     * <p>
     * 渲染时 GeckoLib 把模型 X 轴取反（见 {@code BirdCageContentsRender}），所以这里的 X 也要跟着取反；
     * 点中判定和掉落位置共用这一份计算，免得两边算出来的不是同一只盆。
     * </p>
     */
    private static Vec3 potCenterOffset(Direction facing, BirdCageVariant.PotLayer pot) {
        double modelX = (-(pot.minX() + pot.maxX()) / 2.0D + pot.offsetX()) / 16.0D;
        double modelZ = (pot.minZ() + pot.maxZ()) / 2.0D / 16.0D;
        return potOffset(facing, modelX, modelZ);
    }


    /** 把模型坐标里的水平点按鸟笼朝向转成方块内的偏移（相对方块中心）。 */
    private static Vec3 potOffset(Direction facing, double modelX, double modelZ) {
        return new Vec3(modelX, 0.0D, modelZ).yRot((float) Math.toRadians(-facing.toYRot()));
    }
}
