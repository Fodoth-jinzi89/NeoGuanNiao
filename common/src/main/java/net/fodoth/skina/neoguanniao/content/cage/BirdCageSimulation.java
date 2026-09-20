package net.fodoth.skina.neoguanniao.content.cage;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.skin.BirdSkin;
import net.fodoth.skina.neoguanniao.content.bird.core.skin.BirdSkinUtils;
import net.fodoth.skina.neoguanniao.content.feather.BirdFeatherData;
import net.fodoth.skina.neoguanniao.content.feather.BirdFeatherItem;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 笼中鸟的持续状态：产毛和成长。
 * <p>
 * 产毛周期和笼外一样，取自每只鸟自己的产毛间隔；周期开始时食槽里如果放着这只鸟能吃的食物就吃掉一份、
 * 水槽里如果有水就喝掉 {@link #WATER_PER_CYCLE} mB，各自把这一周期再缩短 10%。产出的羽毛按
 * {@code bird_feather} 的样式（鸟种 + 皮肤稀有度）放进产量槽。放不下时这一份直接丢掉（不会掉在地上），
 * 并且暂停这只鸟的生产，每 {@link #RETRY_TICKS} tick 再看一次，放得下了才重新起周期。
 * </p>
 * <p>
 * 成长用原版 {@code Age}：幼鸟（负数）往 0 长，成鸟的繁殖冷却（正数）往 0 缩，长成时换成成鸟能用的皮肤并
 * 同步给客户端重画。这些状态都存在每只鸟自己的 NBT 里，所以各算各的，取出或放进别的鸟都不会串。
 * </p>
 * <p>
 * 每只鸟都按自己的剩余量跳步推进：剩 10 秒以上就 10 秒一步、剩 1 秒以上就 1 秒一步、最后一秒才每刻推进，
 * 所以一个默认 24000 tick 的周期只会被检查上百次，而不是两万多次。
 * </p>
 */
public final class BirdCageSimulation {

    /** 每只鸟的产毛倒计时在它自己 NBT 里的键；正数在倒计时，负数在等产量槽腾地方。 */
    public static final String TIMER_KEY = "FeatherTimer";

    /** 年龄：就是原版 {@code Mob} 用的那个 Age，负数是幼鸟、正数是繁殖冷却。 */
    private static final String AGE_KEY = "Age";

    /** 成长被暂停的标记（绿食袋那些道具写的）。 */
    private static final String GROWTH_STOPPED_KEY = "BirdGrowthStopped";

    /** 皮肤变体在 NBT 里的键。 */
    private static final String SKIN_VARIANT_KEY = "BirdSkinVariant";

    /** 一份对应食物让这一周期缩短 10%。 */
    private static final double FOOD_SPEEDUP = 0.9D;

    /** 一次喝的水让这一周期再缩短 10%。 */
    private static final double WATER_SPEEDUP = 0.9D;

    /** 一个周期喝掉的水量（mB）。 */
    private static final int WATER_PER_CYCLE = 100;

    /** 产量槽放不下时，隔多久再看一次（10 秒）。 */
    private static final int RETRY_TICKS = 200;

    /** 剩余 10 秒以上时的推进步长（tick）。 */
    private static final int STEP_COARSE = 200;

    /** 剩余 1 秒以上时的推进步长（tick）。 */
    private static final int STEP_FINE = 20;

    private BirdCageSimulation() {
    }


    /** 服务端每刻调用；没到检查点就直接返回，真正的工作按剩余时间的粗细间隔分摊。 */
    public static void serverTick(Level level, BirdCageBlockEntity cage) {
        List<CompoundTag> birds = cage.capturedBirds();
        if (birds.isEmpty()) return;

        long gameTime = level.getGameTime();
        if (gameTime < cage.nextBirdCheck()) return;

        int nextCheck = STEP_COARSE;
        for (CompoundTag bird : birds) {
            // 下一次检查按最急的那只鸟来定，别错过任何一个到点。
            nextCheck = Math.min(nextCheck, tickFeathers(level, cage, bird));
            int growthStep = tickAge(level, cage, bird);
            if (growthStep > 0) nextCheck = Math.min(nextCheck, growthStep);
        }

        // 倒计时只存在 NBT 里，不用同步给客户端，所以只标脏、不发方块更新。
        cage.markTimersDirty();
        cage.setNextBirdCheck(gameTime + Math.max(1, nextCheck));
    }


    /** 按剩余量选推进步长：10 秒以上 10 秒一步，1 秒以上 1 秒一步，最后一秒每刻推进。 */
    private static int stepFor(int remaining) {
        if (remaining >= STEP_COARSE) return STEP_COARSE;
        if (remaining >= STEP_FINE) return STEP_FINE;
        return 1;
    }


    /** 推进一只鸟的产毛倒计时，到点了就产毛；返回这只鸟下一次该隔多久看一次。 */
    private static int tickFeathers(Level level, BirdCageBlockEntity cage, CompoundTag bird) {
        int timer = bird.getInt(TIMER_KEY);
        if (timer > 0) {
            timer -= stepFor(timer);
            if (timer <= 0) {
                // 倒计时归零：产毛；放不下就把这一份丢掉并停下来等产量槽腾地方。
                timer = produce(level, cage, bird) ? arm(level, cage, bird) : -RETRY_TICKS;
            }
        } else if (timer < 0) {
            timer += stepFor(-timer);
            if (timer >= 0) {
                // 到点了再看一次：放得下就重新起周期，放不下接着等。
                timer = hasRoom(level, cage, bird) ? arm(level, cage, bird) : -RETRY_TICKS;
            }
        } else {
            // 刚进笼的鸟：先走一个完整周期。
            timer = arm(level, cage, bird);
        }
        bird.putInt(TIMER_KEY, timer);
        return stepFor(Math.abs(timer));
    }


    /**
     * 推进一只鸟的年龄：幼鸟往 0 长，成鸟的繁殖冷却往 0 缩，长成时换一身成鸟能用的皮肤。
     *
     * @return 这只鸟下一次该隔多久看一次；不用再看的（已经成年、成长被暂停）返回 0
     */
    private static int tickAge(Level level, BirdCageBlockEntity cage, CompoundTag tag) {
        int age = tag.getInt(AGE_KEY);
        if (age < 0) {
            // 成长被绿食袋停住的鸟就一直停在幼鸟。
            if (tag.getBoolean(GROWTH_STOPPED_KEY)) return 0;
            age = Math.min(0, age + stepFor(-age));
            tag.putInt(AGE_KEY, age);
            if (age == 0) {
                // 长大了：原来的皮肤可能只给幼鸟用，换一张，并让客户端重新生成预览实体。
                validateSkin(level, tag);
                cage.setChanged();
            }
        } else if (age > 0) {
            age = Math.max(0, age - stepFor(age));
            tag.putInt(AGE_KEY, age);
        }
        return age == 0 ? 0 : stepFor(Math.abs(age));
    }


    /** 长大了：如果当前皮肤这只成鸟用不了，就换成能用的那张，并把变体号写回鸟的 NBT。 */
    private static void validateSkin(Level level, CompoundTag tag) {
        AbstractBirdEntity<?> bird = createBird(level, tag);
        if (bird == null) return;

        BirdSkin current = bird.getSkin();
        if (BirdSkinUtils.isSkinAvailable(bird, current)) return;

        BirdSkin replacement = BirdSkinUtils.findReplacement(bird, current);
        if (replacement == null || replacement.id().equals(current.id())) return;
        bird.getSkinController().setSkinVariant(replacement.id());
        tag.putInt(SKIN_VARIANT_KEY, bird.getSkinController().getSkinVariant());
    }


    /**
     * 起一个产毛周期：能吃就吃一份对应食物、能喝就喝 100mB 水，每样都把这一周期缩短 10%（两样叠加就是 20%）。
     *
     * @return 这一周期的长度（tick）
     */
    private static int arm(Level level, BirdCageBlockEntity cage, CompoundTag tag) {
        AbstractBirdEntity<?> bird = createBird(level, tag);
        if (bird == null) return 0;
        double factor = 1.0D;
        if (consumeMatchingFood(cage, bird)) factor *= FOOD_SPEEDUP;
        if (consumeWater(cage)) factor *= WATER_SPEEDUP;
        return Math.max(1, (int) Math.round(bird.getFeatherInterval() * factor));
    }


    /**
     * 产毛并放进产量槽。
     *
     * @return 这一轮是否算完成（放不下才返回 false，此时羽毛被丢掉并且要暂停生产）
     */
    private static boolean produce(Level level, BirdCageBlockEntity cage, CompoundTag tag) {
        ItemStack feathers = featherStack(level, tag);
        if (feathers == null) return true;
        if (!canStore(cage, feathers)) return false;
        addToOutput(cage, feathers);
        return true;
    }


    /** 暂停之后再看一次：这只鸟的羽毛放得下了吗。 */
    private static boolean hasRoom(Level level, BirdCageBlockEntity cage, CompoundTag tag) {
        ItemStack feathers = featherStack(level, tag);
        return feathers == null || canStore(cage, feathers);
    }


    /** 这只鸟这一轮该产的羽毛；幼鸟或产毛量为零时返回 null。 */
    @Nullable
    private static ItemStack featherStack(Level level, CompoundTag tag) {
        AbstractBirdEntity<?> bird = createBird(level, tag);
        if (bird == null || bird.isBaby()) return null;
        int count = bird.getFeatherCount();
        if (count <= 0) return null;

        ResourceLocation birdId = BuiltInRegistries.ENTITY_TYPE.getKey(bird.getType());
        ItemStack feathers = new ItemStack(NeoGuanNiaoItems.BIRD_FEATHER.get(), count);
        BirdFeatherItem.setFeatherData(feathers, BirdFeatherData.create(birdId, bird.getSkin().rarity().getRarity()));
        return feathers;
    }


    /** 食槽里有这只鸟能吃的食物就吃掉一份（多个食槽时从前往后找）。 */
    private static boolean consumeMatchingFood(BirdCageBlockEntity cage, AbstractBirdEntity<?> bird) {
        for (int slot = 0; slot < cage.foodSlotCount(); slot++) {
            ItemStack food = cage.itemHandler().getStackInSlot(slot);
            if (food.isEmpty() || !bird.isFood(food)) continue;
            food.shrink(1);
            if (food.isEmpty()) {
                cage.itemHandler().setStackInSlot(slot, ItemStack.EMPTY);
            }
            cage.setChanged();
            return true;
        }
        return false;
    }


    /** 水槽里有水就抽出一次喝的量（多个水槽时从前往后找，够一次喝的就用）。 */
    private static boolean consumeWater(BirdCageBlockEntity cage) {
        for (SimpleFluidTank tank : cage.fluidTanks()) {
            if (tank.getAmount() < WATER_PER_CYCLE || !isWater(tank)) continue;
            tank.drain(WATER_PER_CYCLE);
            cage.setChanged();
            return true;
        }
        return false;
    }


    /** 水槽里装的是不是水（按流体标签判断，流水之类也算）。 */
    private static boolean isWater(SimpleFluidTank tank) {
        if (tank.isEmpty()) return false;
        Fluid fluid = BuiltInRegistries.FLUID.get(tank.getFluidId());
        return !fluid.isSame(Fluids.EMPTY) && fluid.defaultFluidState().is(FluidTags.WATER);
    }


    /** 产量槽里有没有地方整组放下这些羽毛：并进同种羽毛，或者有个空槽。 */
    private static boolean canStore(BirdCageBlockEntity cage, ItemStack feathers) {
        int first = cage.outputSlotFirst();
        for (int slot = first; slot < first + cage.outputSlotCount(); slot++) {
            ItemStack existing = cage.itemHandler().getStackInSlot(slot);
            if (existing.isEmpty()) return true;
            if (ItemStack.isSameItemSameComponents(existing, feathers)
                    && BirdCageBlockEntity.OUTPUT_STACK_LIMIT - existing.getCount() >= feathers.getCount()) {
                return true;
            }
        }
        return false;
    }


    /** 把羽毛放进产量槽：优先并进同种羽毛，其次放进空槽。调用前先确认 {@link #canStore}。 */
    private static void addToOutput(BirdCageBlockEntity cage, ItemStack feathers) {
        int first = cage.outputSlotFirst();
        int end = first + cage.outputSlotCount();
        for (int slot = first; slot < end; slot++) {
            ItemStack existing = cage.itemHandler().getStackInSlot(slot);
            if (existing.isEmpty() || !ItemStack.isSameItemSameComponents(existing, feathers)) continue;
            if (BirdCageBlockEntity.OUTPUT_STACK_LIMIT - existing.getCount() < feathers.getCount()) continue;
            existing.grow(feathers.getCount());
            cage.setChanged();
            return;
        }
        for (int slot = first; slot < end; slot++) {
            if (cage.itemHandler().getStackInSlot(slot).isEmpty()) {
                cage.itemHandler().setStackInSlot(slot, feathers);
                cage.setChanged();
                return;
            }
        }
    }


    /** 用捕捉时存下的 NBT 还原一只鸟（不放进世界），只为了读它的产毛间隔、皮肤和食性。 */
    @Nullable
    private static AbstractBirdEntity<?> createBird(Level level, CompoundTag tag) {
        return EntityType.create(tag, level).orElse(null) instanceof AbstractBirdEntity<?> bird ? bird : null;
    }
}
