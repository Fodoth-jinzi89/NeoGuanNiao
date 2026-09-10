package net.fodoth.skina.neoguanniao.content.camera;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class PhotographData {


    public static final int IMAGE_SIZE = 1024;
    private static final int MIN_IMAGE_SIZE = 16;
    private static final int MAX_IMAGE_SIZE = 1024;


    public static final String TAG_PHOTO_ID = "PhotoId";
    public static final String TAG_PHOTOGRAPHER = "Photographer";
    public static final String TAG_PHOTOGRAPHER_ID = "PhotographerId";
    public static final String TAG_GAME_TIME = "GameTime";
    public static final String TAG_CAPTURE_TIME = "CaptureTime";
    public static final String TAG_WIDTH = "Width";
    public static final String TAG_HEIGHT = "Height";
    public static final String TAG_CONTENT_HASH = "ContentHash";
    public static final String TAG_PIXELS = "Pixels";
    public static final String TAG_DIMENSION = "Dimension";
    public static final String TAG_X = "X";
    public static final String TAG_Y = "Y";
    public static final String TAG_Z = "Z";
    public static final String TAG_FRAME_BLOCK = "FrameBlock";

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

    public static long captureTime(ItemStack stack) {
        CompoundTag tag = CameraItemData.read(stack);
        return tag.contains(TAG_CAPTURE_TIME) ? tag.getLong(TAG_CAPTURE_TIME) : 0L;
    }

    public static int[] pixels(ItemStack stack) {
        return CameraItemData.read(stack).getIntArray(TAG_PIXELS);
    }

    public static String contentHash(ItemStack stack) {
        return CameraItemData.read(stack).getString(TAG_CONTENT_HASH);
    }

    public static String dimension(ItemStack stack) { return CameraItemData.read(stack).getString(TAG_DIMENSION); }
    public static int x(ItemStack stack) { return CameraItemData.read(stack).getInt(TAG_X); }
    public static int y(ItemStack stack) { return CameraItemData.read(stack).getInt(TAG_Y); }
    public static int z(ItemStack stack) { return CameraItemData.read(stack).getInt(TAG_Z); }
    public static ResourceLocation frameBlock(ItemStack stack) {
        String id = CameraItemData.read(stack).getString(TAG_FRAME_BLOCK);
        return id.isEmpty() ? ResourceLocation.withDefaultNamespace("oak_planks") : ResourceLocation.parse(id);
    }
    public static void setFrameBlock(ItemStack stack, ResourceLocation blockId) {
        CameraItemData.update(stack, tag -> tag.putString(TAG_FRAME_BLOCK, blockId.toString()));
    }

    public static int width(ItemStack stack) {
        return PhotographData.imageWidth(CameraItemData.read(stack));
    }

    public static int height(ItemStack stack) {
        return PhotographData.imageHeight(CameraItemData.read(stack));
    }

    public static void writeReference(ItemStack stack, String id, String photographer, UUID photographerId, long gameTime, int width, int height, String contentHash) {
        writeReference(stack, id, photographer, photographerId, gameTime, width, height, contentHash, "", 0, 0, 0);
    }

    public static void writeReference(ItemStack stack, String id, String photographer, UUID photographerId, long gameTime, int width, int height, String contentHash, String dimension, int x, int y, int z) {
        if (!PhotoTransferLimits.isValidPhotoId(id) || width < MIN_IMAGE_SIZE || height < MIN_IMAGE_SIZE || width > MAX_IMAGE_SIZE || height > MAX_IMAGE_SIZE || !PhotoImageCodec.isSha256(contentHash)) {
            throw new IllegalArgumentException("Invalid photograph reference");
        }
        CameraItemData.update(stack, tag -> {
            tag.putString(TAG_PHOTO_ID, id);
            tag.putString(TAG_PHOTOGRAPHER, photographer);
            tag.putUUID(TAG_PHOTOGRAPHER_ID, photographerId);
            tag.putLong(TAG_GAME_TIME, gameTime);
            if (!tag.contains(TAG_CAPTURE_TIME)) {
                tag.putLong(TAG_CAPTURE_TIME, System.currentTimeMillis());
            }
            tag.putInt(TAG_WIDTH, width);
            tag.putInt(TAG_HEIGHT, height);
            tag.putString(TAG_CONTENT_HASH, contentHash);
            tag.putString(TAG_DIMENSION, dimension == null ? "" : dimension);
            tag.putInt(TAG_X, x);
            tag.putInt(TAG_Y, y);
            tag.putInt(TAG_Z, z);
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
        target.merge(source.copy());
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

