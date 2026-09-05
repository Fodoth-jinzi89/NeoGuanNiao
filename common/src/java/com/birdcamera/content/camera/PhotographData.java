package com.birdcamera.content.camera;

import com.birdcamera.registry.BirdCameraDataComponents;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * 相片数据：使用数据组件（PHOTO_DATA）保存照片引用元数据（迁移自 guaniao-2.1.3，
 * 原版使用物品 NBT，1.21.1 迁移为 Data Component）。
 */
public final class PhotographData {
    public static final int IMAGE_SIZE = 256;
    private static final int MIN_IMAGE_SIZE = 16;
    private static final int MAX_IMAGE_SIZE = 512;
    public static final String TAG_PHOTO_ID = "PhotoId";
    public static final String TAG_PHOTOGRAPHER = "Photographer";
    public static final String TAG_PHOTOGRAPHER_ID = "PhotographerId";
    public static final String TAG_GAME_TIME = "GameTime";
    public static final String TAG_WIDTH = "Width";
    public static final String TAG_HEIGHT = "Height";
    public static final String TAG_CONTENT_HASH = "ContentHash";
    public static final String TAG_PIXELS = "Pixels";

    private PhotographData() {
    }

    private static CompoundTag tag(ItemStack stack) {
        return stack.get(BirdCameraDataComponents.PHOTO_DATA);
    }

    public static boolean hasImage(ItemStack stack) {
        CompoundTag tag = tag(stack);
        return tag != null
                && tag.contains(TAG_PHOTO_ID)
                && PhotoTransferLimits.isValidPhotoId(tag.getString(TAG_PHOTO_ID))
                && imageWidth(tag) > 0
                && imageHeight(tag) > 0;
    }

    public static String id(ItemStack stack) {
        CompoundTag tag = tag(stack);
        return tag == null ? "" : tag.getString(TAG_PHOTO_ID);
    }

    public static String photographer(ItemStack stack) {
        CompoundTag tag = tag(stack);
        return tag == null ? "" : tag.getString(TAG_PHOTOGRAPHER);
    }

    public static UUID photographerId(ItemStack stack) {
        CompoundTag tag = tag(stack);
        return tag != null && tag.contains(TAG_PHOTOGRAPHER_ID) ? tag.getUUID(TAG_PHOTOGRAPHER_ID) : null;
    }

    public static long gameTime(ItemStack stack) {
        CompoundTag tag = tag(stack);
        return tag == null ? 0L : tag.getLong(TAG_GAME_TIME);
    }

    public static int[] pixels(ItemStack stack) {
        CompoundTag tag = tag(stack);
        return tag == null ? new int[0] : tag.getIntArray(TAG_PIXELS);
    }

    public static boolean hasLegacyPixels(ItemStack stack) {
        CompoundTag tag = tag(stack);
        return tag != null && tag.getIntArray(TAG_PIXELS).length == 65536;
    }

    public static String contentHash(ItemStack stack) {
        CompoundTag tag = tag(stack);
        return tag == null ? "" : tag.getString(TAG_CONTENT_HASH);
    }

    public static int width(ItemStack stack) {
        CompoundTag tag = tag(stack);
        return tag == null ? 0 : imageWidth(tag);
    }

    public static int height(ItemStack stack) {
        CompoundTag tag = tag(stack);
        return tag == null ? 0 : imageHeight(tag);
    }

    public static void writeReference(
            ItemStack stack, String id, String photographer, UUID photographerId, long gameTime, int width, int height, String contentHash) {
        if (PhotoTransferLimits.isValidPhotoId(id)
                && width >= MIN_IMAGE_SIZE && height >= MIN_IMAGE_SIZE
                && width <= MAX_IMAGE_SIZE && height <= MAX_IMAGE_SIZE
                && PhotoImageCodec.isSha256(contentHash)) {
            CompoundTag tag = new CompoundTag();
            tag.putString(TAG_PHOTO_ID, id);
            tag.putString(TAG_PHOTOGRAPHER, photographer);
            tag.putUUID(TAG_PHOTOGRAPHER_ID, photographerId);
            tag.putLong(TAG_GAME_TIME, gameTime);
            tag.putInt(TAG_WIDTH, width);
            tag.putInt(TAG_HEIGHT, height);
            tag.putString(TAG_CONTENT_HASH, contentHash);
            stack.set(BirdCameraDataComponents.PHOTO_DATA, tag);
        } else {
            throw new IllegalArgumentException("Invalid photograph reference");
        }
    }

    public static void copyImage(ItemStack from, ItemStack to) {
        CompoundTag source = tag(from);
        if (source != null) {
            to.set(BirdCameraDataComponents.PHOTO_DATA, source.copy());
        }
    }

    public static void finishLegacyMigration(ItemStack stack, String contentHash) {
        CompoundTag tag = tag(stack);
        if (tag != null && PhotoImageCodec.isSha256(contentHash)) {
            CompoundTag updated = tag.copy();
            updated.putInt(TAG_WIDTH, 256);
            updated.putInt(TAG_HEIGHT, 256);
            updated.putString(TAG_CONTENT_HASH, contentHash);
            updated.remove(TAG_PIXELS);
            stack.set(BirdCameraDataComponents.PHOTO_DATA, updated);
        }
    }

    private static int imageWidth(CompoundTag tag) {
        int width = tag.getInt(TAG_WIDTH);
        int height = tag.getInt(TAG_HEIGHT);
        if (validDimensions(width, height)) {
            return width;
        }
        return tag.getIntArray(TAG_PIXELS).length == 65536 ? 256 : 0;
    }

    private static int imageHeight(CompoundTag tag) {
        int width = tag.getInt(TAG_WIDTH);
        int height = tag.getInt(TAG_HEIGHT);
        if (validDimensions(width, height)) {
            return height;
        }
        return tag.getIntArray(TAG_PIXELS).length == 65536 ? 256 : 0;
    }

    private static boolean validDimensions(int width, int height) {
        return width >= MIN_IMAGE_SIZE && height >= MIN_IMAGE_SIZE && width <= MAX_IMAGE_SIZE && height <= MAX_IMAGE_SIZE;
    }
}