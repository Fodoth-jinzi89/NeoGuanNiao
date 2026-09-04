package net.fodoth.skina.neoguanniao.content.camera;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public final class PhotographData {


    public static final int IMAGE_SIZE = 1024;
    private static final int MIN_IMAGE_SIZE = 16;
    private static final int MAX_IMAGE_SIZE = 1024;


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

    public static boolean hasImage(ItemStack stack) {
        CompoundTag tag = CameraItemData.read(stack);
        return tag.contains(TAG_PHOTO_ID) && PhotoTransferLimits.isValidPhotoId(tag.getString(TAG_PHOTO_ID)) && PhotographData.imageWidth(tag) > 0 && PhotographData.imageHeight(tag) > 0;
    }

    public static String id(ItemStack stack) {
        return CameraItemData.read(stack).getString(TAG_PHOTO_ID);
    }

    public static String photographer(ItemStack stack) {
        return CameraItemData.read(stack).getString(TAG_PHOTOGRAPHER);
    }

    public static UUID photographerId(ItemStack stack) {
        CompoundTag tag = CameraItemData.read(stack);
        return tag.hasUUID(TAG_PHOTOGRAPHER_ID) ? tag.getUUID(TAG_PHOTOGRAPHER_ID) : null;
    }

    public static long gameTime(ItemStack stack) {
        return CameraItemData.read(stack).getLong(TAG_GAME_TIME);
    }

    public static int[] pixels(ItemStack stack) {
        return CameraItemData.read(stack).getIntArray(TAG_PIXELS);
    }

    public static String contentHash(ItemStack stack) {
        return CameraItemData.read(stack).getString(TAG_CONTENT_HASH);
    }

    public static int width(ItemStack stack) {
        return PhotographData.imageWidth(CameraItemData.read(stack));
    }

    public static int height(ItemStack stack) {
        return PhotographData.imageHeight(CameraItemData.read(stack));
    }

    public static void writeReference(ItemStack stack, String id, String photographer, UUID photographerId, long gameTime, int width, int height, String contentHash) {
        if (!PhotoTransferLimits.isValidPhotoId(id) || width < MIN_IMAGE_SIZE || height < MIN_IMAGE_SIZE || width > MAX_IMAGE_SIZE || height > MAX_IMAGE_SIZE || !PhotoImageCodec.isSha256(contentHash)) {
            throw new IllegalArgumentException("Invalid photograph reference");
        }
        CameraItemData.update(stack, tag -> {
            tag.putString(TAG_PHOTO_ID, id);
            tag.putString(TAG_PHOTOGRAPHER, photographer);
            tag.putUUID(TAG_PHOTOGRAPHER_ID, photographerId);
            tag.putLong(TAG_GAME_TIME, gameTime);
            tag.putInt(TAG_WIDTH, width);
            tag.putInt(TAG_HEIGHT, height);
            tag.putString(TAG_CONTENT_HASH, contentHash);
            tag.remove(TAG_PIXELS);
        });
    }

    public static void copyImage(ItemStack from, ItemStack to) {
        CompoundTag source = CameraItemData.read(from);
        if (source.isEmpty()) {
            return;
        }
        CameraItemData.update(to, target -> copyImageTag(source, target));
    }

    private static void copyImageTag(CompoundTag source, CompoundTag target) {
        copyString(source, target, TAG_PHOTO_ID);
        copyString(source, target, TAG_PHOTOGRAPHER);
        if (source.hasUUID(TAG_PHOTOGRAPHER_ID)) {
            target.putUUID(TAG_PHOTOGRAPHER_ID, source.getUUID(TAG_PHOTOGRAPHER_ID));
        }
        if (source.contains(TAG_GAME_TIME)) {
            target.putLong(TAG_GAME_TIME, source.getLong(TAG_GAME_TIME));
        }
        copyString(source, target, TAG_CONTENT_HASH);
        int width = imageWidth(source);
        int height = imageHeight(source);
        target.putInt(TAG_WIDTH, width);
        target.putInt(TAG_HEIGHT, height);
    }

    private static void copyString(CompoundTag source, CompoundTag target, String key) {
        if (source.contains(key)) {
            target.putString(key, source.getString(key));
        }
    }

    private static int imageWidth(CompoundTag tag) {
        int width = tag.getInt(TAG_WIDTH);
        if (PhotographData.validDimensions(width, tag.getInt(TAG_HEIGHT))) {
            return width;
        }
        return tag.getIntArray(TAG_PIXELS).length == 65536 ? 256 : 0;
    }

    private static int imageHeight(CompoundTag tag) {
        int height;
        int width = tag.getInt(TAG_WIDTH);
        if (PhotographData.validDimensions(width, height = tag.getInt(TAG_HEIGHT))) {
            return height;
        }
        return tag.getIntArray(TAG_PIXELS).length == 65536 ? 256 : 0;
    }

    private static boolean validDimensions(int width, int height) {
        return width >= 16 && height >= 16 && width <= 1024 && height <= 1024;
    }
}

