package net.fodoth.skina.neoguanniao.content.cage;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;


public enum BirdCageVariant {

    SMALL(
            "small_bird_cage",
            "geo/small_bird_cage.geo.json",
            "textures/block/small_bird_cage.png",
            Block.box(1, 0, 1, 15, 16, 15), 0.55F
    ),

    MEDIUM(
            "medium_bird_cage",
            "geo/medium_bird_cage.geo.json",
            "textures/block/medium_bird_cage.png",
            Block.box(0, 0, 0, 16, 32, 16), 1.0F
    ),

    LARGE(
            "large_bird_cage",
            "geo/large_bird_cage.geo.json",
            "textures/block/large_bird_cage.png",
            Block.box(0, 0, 0, 16, 48, 16), 2.0F
    );


    public static final ResourceLocation ANIMATION =
            ResourceLocation.fromNamespaceAndPath(
                    NeoGuanNiao.MODID,
                    "animations/bird_cage.animation.json"
            );


    private final String id;
    private final ResourceLocation model;
    private final ResourceLocation texture;
    private final VoxelShape shape;
    private final float maxEntitySize;


    BirdCageVariant(
            String id,
            String modelPath,
            String texturePath,
            VoxelShape shape, float maxEntitySize
    ) {
        this.id = id;

        this.model = ResourceLocation.fromNamespaceAndPath(
                NeoGuanNiao.MODID,
                modelPath
        );

        this.texture = ResourceLocation.fromNamespaceAndPath(
                NeoGuanNiao.MODID,
                texturePath
        );

        this.shape = shape;
        this.maxEntitySize = maxEntitySize;
    }


    public String id() {
        return id;
    }


    public ResourceLocation model() {
        return model;
    }


    public ResourceLocation texture() {
        return texture;
    }


    public VoxelShape shape() {
        return shape;
    }

    public float maxEntitySize() { return maxEntitySize; }


    /** 该规格鸟笼最多能装下的实体数量。 */
    public int capacity() {
        return switch (this) {
            case SMALL -> 1;
            case MEDIUM -> 2;
            case LARGE -> 4;
        };
    }


    /**
     * 每个笼位的落点，单位为像素：{左右偏移, 脚踩的支撑面高度, 前后偏移}，下标与捕捉顺序一致。
     *
     * <p>GeoBlockRenderer 用 {@code 180 - FACING.toYRot()} 旋转笼子模型，而实体渲染用的 yaw 是
     * {@code -FACING.toYRot()}，两套旋转相差 180°，所以本表的水平坐标与模型坐标互为反号：
     * 表中 z 为正对应模型里的前方（-Z）。中型/大型鸟笼分别站在下（前）杠与上（后）杠上。</p>
     */
    private static final double[][][] SLOT_OFFSETS = {
            {{0, 2, 0}},
            {{5, 14.525, 8.5}, {-5, 19.525, -4.5}},
            {{7, 24.525, 4.5}, {-7, 24.525, 4.5}, {7, 35.425, -3.2}, {-7, 35.425, -3.2}},
    };


    /** 取出某个笼位的落点；下标越界时退回最后一个笼位。 */
    public double[] slotOffset(int slot) {
        double[][] slots = SLOT_OFFSETS[ordinal()];
        return slots[Math.min(Math.max(slot, 0), slots.length - 1)];
    }


    /** 某个笼位落点（实体脚踩的支撑面中心）在世界中的位置，水平偏移已按鸟笼朝向旋转。 */
    public Vec3 slotPos(BlockPos origin, Direction facing, int slot) {
        double[] offset = slotOffset(slot);
        Vec3 horizontal = new Vec3(offset[0] / 16.0D, 0.0D, offset[2] / 16.0D)
                .yRot((float) Math.toRadians(-facing.toYRot()));
        return new Vec3(origin.getX() + 0.5D + horizontal.x,
                origin.getY() + offset[1] / 16.0D,
                origin.getZ() + 0.5D + horizontal.z);
    }
}
