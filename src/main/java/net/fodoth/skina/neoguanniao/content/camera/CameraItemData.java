package net.fodoth.skina.neoguanniao.content.camera;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Compatibility boundary for camera metadata stored on item stacks.
 *
 * <p>Minecraft 1.21 stores custom item NBT in a data component. Keeping all
 * access here prevents camera code from depending on the old mutable tag API
 * and preserves tags written by Guaniao 3.1.4.</p>
 */
public final class CameraItemData {

    private CameraItemData() {
    }

    public static @NotNull CompoundTag read(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    public static void update(ItemStack stack, Consumer<CompoundTag> writer) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, writer);
    }
}
