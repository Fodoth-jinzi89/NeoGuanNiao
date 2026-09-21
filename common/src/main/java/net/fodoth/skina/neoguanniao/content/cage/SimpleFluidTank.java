package net.fodoth.skina.neoguanniao.content.cage;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

/**
 * 平台中立的单槽流体储罐，容量与存量都以 mB 计。
 * <p>
 * 只依赖原版类型，fabric / neoforge 各自把它包装成自己的流体传输对象。
 * </p>
 */
public class SimpleFluidTank {

    /** 一桶的容积（mB）：容量与存量都以 mB 计，各平台自己的流体单位往这里换算也以它为准。 */
    public static final int BUCKET_VOLUME = 1000;

    private static final String FLUID_KEY = "Fluid";
    private static final String AMOUNT_KEY = "Amount";


    private final int capacity;
    private ResourceLocation fluidId;
    private int amount;


    public SimpleFluidTank(int capacity) {
        this.capacity = capacity;
    }


    public int getCapacity() {
        return capacity;
    }

    public int getAmount() {
        return amount;
    }

    public ResourceLocation getFluidId() {
        return fluidId;
    }

    public boolean isEmpty() {
        return fluidId == null || amount <= 0;
    }

    /** 本次最多还能注入多少 mB（不修改状态）；流体种类不同或参数非法时返回 0。 */
    public int simulateFill(ResourceLocation id, int maxAmount) {
        if (id == null || maxAmount <= 0) return 0;
        if (isEmpty()) return Math.min(maxAmount, capacity);
        if (!fluidId.equals(id)) return 0;
        return Math.min(maxAmount, capacity - amount);
    }

    /** 注入流体，返回实际注入的 mB 数。 */
    public int fill(ResourceLocation id, int maxAmount) {
        int filled = simulateFill(id, maxAmount);
        if (filled <= 0) return 0;
        if (isEmpty()) fluidId = id;
        amount += filled;
        return filled;
    }

    /** 抽出流体，返回实际抽出的 mB 数；抽空后流体种类也一并清掉。 */
    public int drain(int maxAmount) {
        int drained = Math.min(maxAmount, amount);
        if (drained <= 0) return 0;
        amount -= drained;
        if (amount == 0) clear();
        return drained;
    }

    public void clear() {
        fluidId = null;
        amount = 0;
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        if (!isEmpty()) {
            tag.putString(FLUID_KEY, fluidId.toString());
            tag.putInt(AMOUNT_KEY, amount);
        }
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        clear();
        if (!tag.contains(FLUID_KEY)) return;
        ResourceLocation id = ResourceLocation.tryParse(tag.getString(FLUID_KEY));
        int stored = Math.min(tag.getInt(AMOUNT_KEY), capacity);
        if (id != null && stored > 0) {
            fluidId = id;
            amount = stored;
        }
    }
}
