package net.fodoth.skina.neoguanniao.content.camera;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/** Accessor for camera metadata stored in the 1.21.1 custom-data component. */
public final class CameraItemData {

    private CameraItemData() {
    }

    public static @NotNull CompoundTag read(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? new CompoundTag() : data.copyTag();
    }

    public static void update(ItemStack stack, Consumer<CompoundTag> writer) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, writer);
    }
}
