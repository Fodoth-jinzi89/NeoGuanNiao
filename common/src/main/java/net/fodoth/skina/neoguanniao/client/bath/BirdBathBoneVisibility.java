package net.fodoth.skina.neoguanniao.client.bath;

import net.fodoth.skina.neoguanniao.content.bath.BirdBathCleanliness;
import net.fodoth.skina.neoguanniao.content.bath.BirdBathContentType;
import software.bernie.geckolib.cache.object.GeoBone;

import java.util.Set;

final class BirdBathBoneVisibility {

    private static final float[] TINT_WHITE = {1F, 1F, 1F};
    private static final float[] TINT_SPOILED_FISH = {0.46F, 0.56F, 0.40F};
    private static final float[] TINT_SPOILED_MEAT = {0.48F, 0.34F, 0.25F};
    private static final float[] TINT_SPOILED_BREAD = {0.50F, 0.44F, 0.25F};
    private static final float[] TINT_SPOILED_DEFAULT = {0.46F, 0.50F, 0.34F};
    private static final float[] TINT_WATER_USED = {0.75F, 0.82F, 0.78F};
    private static final float[] TINT_WATER_DIRTY = {0.56F, 0.54F, 0.43F};
    private static final float[] TINT_WATER_FILTHY = {0.38F, 0.48F, 0.28F};
    private static final float[] TINT_FROZEN = {0.74F, 0.88F, 1F};
    private static final float[] TINT_FLIES = {0.08F, 0.08F, 0.07F};
    private static final float[] TINT_SPOIL_SPOTS = {0.36F, 0.48F, 0.20F};
    private static final float[] TINT_DIRT_USED = {0.62F, 0.58F, 0.48F};
    private static final float[] TINT_DIRT_DIRTY = {0.42F, 0.46F, 0.28F};
    private static final float[] TINT_DIRT_FILTHY = {0.24F, 0.32F, 0.16F};

    private static final Set<String> CONTENT_BONES = Set.of(
            "water_up", "water_middle", "water_down",
            "ice_up", "ice_middle", "ice_down",
            "fish_up", "fish_middle", "fish_down",
            "meat_up", "meat_middle", "meat_down",
            "bread_up", "bread_middle", "bread_down",
            "spoiled_up", "spoiled_middle", "spoiled_down"
    );

    private static final Set<String> DIRT_BONES = Set.of(
            "dirty_spots_light",
            "dirty_spots_medium",
            "dirty_spots_heavy",
            "spoil_spots",
            "flies"
    );


    private BirdBathBoneVisibility() {
    }


    static void apply(
            BirdBathContentType type,
            int level,
            BirdBathCleanliness cleanliness,
            BirdBathContentType spoiledContentType,
            GeoBone bone
    ) {

        String name = bone.getName();

        if (CONTENT_BONES.contains(name)) {

            boolean visible =
                    isContentBoneVisible(
                            type,
                            level,
                            spoiledContentType,
                            name
                    );

            bone.setHidden(!visible);
            bone.setChildrenHidden(!visible);

        } else if (DIRT_BONES.contains(name)) {

            boolean visible =
                    isDirtBoneVisible(
                            type,
                            level,
                            cleanliness,
                            name
                    );

            bone.setHidden(!visible);
            bone.setChildrenHidden(!visible);
        }
    }


    static boolean isContentBoneVisible(
            BirdBathContentType type,
            int level,
            BirdBathContentType spoiledContentType,
            String boneName
    ) {

        if (!CONTENT_BONES.contains(boneName)) {
            return false;
        }

        if (type == null || type.isEmpty() || level <= 0) {
            return false;
        }


        String levelName = switch (level) {
            case 1 -> "down";
            case 2 -> "middle";
            case 3 -> "up";
            default -> "";
        };


        if (levelName.isEmpty()) {
            return false;
        }


        if (type == BirdBathContentType.SPOILED) {

            BirdBathContentType visualType =
                    spoiledContentType != null
                            && spoiledContentType.isFood()
                            ? spoiledContentType
                            : BirdBathContentType.FISH;


            return boneName.equals(
                    visualType.serializedName()
                            + "_"
                            + levelName
            )
                    || boneName.equals(
                    "spoiled_" + levelName
            );
        }


        return switch (type) {

            case WATER ->
                    boneName.equals(
                            "water_" + levelName
                    );

            case FROZEN_WATER ->
                    boneName.equals(
                            "water_" + levelName
                    )
                            ||
                            boneName.equals(
                                    "ice_" + levelName
                            );

            case FISH ->
                    boneName.equals(
                            "fish_" + levelName
                    );

            case MEAT ->
                    boneName.equals(
                            "meat_" + levelName
                    );

            case BREAD ->
                    boneName.equals(
                            "bread_" + levelName
                    );

            default ->
                    false;
        };
    }


    static float[] tintFor(
            BirdBathContentType type,
            BirdBathContentType visualType,
            BirdBathCleanliness cleanliness
    ) {

        if (type == BirdBathContentType.SPOILED) {

            return switch (
                    visualType == null
                            ? BirdBathContentType.FISH
                            : visualType
                    ) {

                case FISH ->
                        TINT_SPOILED_FISH;

                case MEAT ->
                        TINT_SPOILED_MEAT;

                case BREAD ->
                        TINT_SPOILED_BREAD;

                default ->
                        TINT_SPOILED_DEFAULT;
            };
        }


        if (type == BirdBathContentType.WATER) {

            BirdBathCleanliness clean =
                    cleanliness == null
                            ? BirdBathCleanliness.CLEAN
                            : cleanliness;


            return switch (clean) {

                case CLEAN ->
                        TINT_WHITE;

                case USED ->
                        TINT_WATER_USED;

                case DIRTY ->
                        TINT_WATER_DIRTY;

                case FILTHY ->
                        TINT_WATER_FILTHY;
            };
        }


        return type == BirdBathContentType.FROZEN_WATER
                ? TINT_FROZEN
                : TINT_WHITE;
    }


    static boolean isDirtBone(String boneName) {
        return DIRT_BONES.contains(boneName);
    }


    static float[] dirtTintFor(
            BirdBathContentType type,
            BirdBathCleanliness cleanliness,
            String boneName
    ) {

        if (type == BirdBathContentType.SPOILED) {

            if (boneName.equals("flies")) {
                return TINT_FLIES;
            }

            if (boneName.equals("spoil_spots")) {
                return TINT_SPOIL_SPOTS;
            }
        }


        BirdBathCleanliness clean =
                cleanliness == null
                        ? BirdBathCleanliness.CLEAN
                        : cleanliness;


        return switch (clean) {

            case CLEAN ->
                    TINT_WHITE;

            case USED ->
                    TINT_DIRT_USED;

            case DIRTY ->
                    TINT_DIRT_DIRTY;

            case FILTHY ->
                    TINT_DIRT_FILTHY;
        };
    }


    private static boolean isDirtBoneVisible(
            BirdBathContentType type,
            int level,
            BirdBathCleanliness cleanliness,
            String boneName
    ) {

        if (level < 3) {
            return false;
        }


        BirdBathCleanliness clean =
                cleanliness == null
                        ? BirdBathCleanliness.CLEAN
                        : cleanliness;


        if (type == BirdBathContentType.SPOILED
                && (
                boneName.equals("spoil_spots")
                        ||
                        boneName.equals("flies")
        )) {
            return true;
        }


        return switch (clean) {

            case CLEAN ->
                    false;

            case USED ->
                    boneName.equals(
                            "dirty_spots_light"
                    );

            case DIRTY ->
                    boneName.equals(
                            "dirty_spots_medium"
                    );

            case FILTHY ->
                    boneName.equals(
                            "dirty_spots_heavy"
                    );
        };
    }


    static String visibleBoneFor(
            BirdBathContentType type,
            int level
    ) {

        if (type == null
                || type.isEmpty()
                || level <= 0
                || type == BirdBathContentType.SPOILED) {
            return "";
        }


        String levelName = switch (level) {

            case 1 -> "down";
            case 2 -> "middle";
            case 3 -> "up";
            default -> "";
        };


        if (levelName.isEmpty()) {
            return "";
        }


        return type == BirdBathContentType.FROZEN_WATER
                ? "water_" + levelName
                : type.serializedName()
                + "_"
                + levelName;
    }
}
