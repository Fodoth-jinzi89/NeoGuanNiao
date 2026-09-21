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


    /**
     * 各规格的存储布局：食物槽个数与单槽上限、水槽个数与单个容量、产量槽个数。
     * <p>
     * 物品栏里食物槽排在前面，后面接着产量槽，所以产量槽从第 {@code foodSlots} 格开始。
     * </p>
     */
    public record Storage(int foodSlots, int foodStackLimit, int tanks, int tankCapacity, int outputSlots) {
    }


    private static final Storage SMALL_STORAGE = new Storage(1, 16, 1, 1000, 4);
    private static final Storage MEDIUM_STORAGE = new Storage(1, 32, 1, 2000, 6);
    private static final Storage LARGE_STORAGE = new Storage(2, 64, 2, 4000, 18);


    public Storage storage() {
        return switch (this) {
            case SMALL -> SMALL_STORAGE;
            case MEDIUM -> MEDIUM_STORAGE;
            case LARGE -> LARGE_STORAGE;
        };
    }


    /** 物品栏总格数：食物槽 + 产量槽。 */
    public int itemSlotCount() {
        Storage storage = storage();
        return storage.foodSlots() + storage.outputSlots();
    }


    /** 产量槽的第一格下标。 */
    public int outputSlotFirst() {
        return storage().foodSlots();
    }


    public int outputSlotCount() {
        return storage().outputSlots();
    }


    public int foodStackLimit() {
        return storage().foodStackLimit();
    }


    public int tankCount() {
        return storage().tanks();
    }


    public int tankCapacity() {
        return storage().tankCapacity();
    }


    /**
     * 笼底展示产量槽用的网格：行数、列数、地面高度（模型像素）、内部地面的半边范围。
     * <p>
     * 地面取笼体底面的内表面（小型 y=2、中型 y=5、大型 y=7），范围比笼体内壁再收 1 像素。
     * </p>
     */
    public record SlotGrid(int rows, int columns, float floorY, float halfX, float halfZ) {

        public int slots() {
            return rows * columns;
        }
    }


    private static final SlotGrid SMALL_GRID = new SlotGrid(2, 2, 2.0F, 7.5F, 7.5F);
    private static final SlotGrid MEDIUM_GRID = new SlotGrid(2, 3, 5.0F, 14.5F, 13.5F);
    private static final SlotGrid LARGE_GRID = new SlotGrid(3, 6, 7.0F, 22.5F, 13.5F);


    public SlotGrid slotGrid() {
        return switch (this) {
            case SMALL -> SMALL_GRID;
            case MEDIUM -> MEDIUM_GRID;
            case LARGE -> LARGE_GRID;
        };
    }


    /** 盆里内容物取纹理左上角的这一小块（像素）：盆越大取越大，大型的碗是 5x5。 */
    public float layerPixels() {
        return this == LARGE ? 5.0F : 3.0F;
    }


    /**
     * 一个盆的内容物展示：盆底内沿范围（BlockBench 像素）、盆底与盆口高度（像素）、装的哪一个槽。
     *
     * @param heightRatio 装满时内容物占盆深度的比例；大型的碗顶面没有纹理，直接灌满整碗，所以是 1
     * @param offsetX     内容物层在模型坐标里额外往哪边偏多少（像素）：挂墙上的盆要往外挪一点才贴住盆壁
     */
    public record PotLayer(float minX, float minZ, float maxX, float maxZ,
                           float floorY, float rimY, boolean food, int storageIndex,
                           float heightRatio, float offsetX) {
    }


    /** 小型：两个盆对称地摆在角落，食物和流体各一个。 */
    private static final PotLayer[] SMALL_POTS = {
            new PotLayer(2.1F, 4.5F, 4.1F, 6.5F, 5.5F, 7.0F, true, 0, 0.8F, 0.0F),
            new PotLayer(-4.1F, -6.5F, -2.1F, -4.5F, 5.5F, 7.0F, false, 0, 0.8F, 0.0F),
    };

    /** 中型：两个盆分别挂在两侧墙上（盆底 y=14、盆口 y=17），内容物层再往外挪 0.35 贴住盆壁。 */
    private static final PotLayer[] MEDIUM_POTS = {
            new PotLayer(-16.0F, -1.25F, -13.5F, 1.25F, 14.0F, 17.0F, false, 0, 0.8F, 0.35F),
            new PotLayer(13.5F, -1.25F, 16.0F, 1.25F, 14.0F, 17.0F, true, 0, 0.8F, -0.35F),
    };

    /**
     * 大型：盆的形状和中小型统一了（外壁 + 内壁 + 盆底 + 盆口的唇），左右各一列、上下两层：
     * 模型 -X 两个食盆、+X 两个水盆（和中型的食物在 -X 一侧一致），同一层的两个盆内容可以不同。
     * 盆比中型大，中型又比小型大。
     * <p>
     * 这里的 Z 是镜像过的（以模型正中面为镜面）：这些盆挂在 climbing_frames 骨头下，渲染时前后被翻了一次，
     * 直接照 geo 里的坐标画会落在盆外面。X 不受影响，左右两边还是照 geo 的位置。
     * </p>
     * <p>
     * 横向范围取盆内壁之间的空腔（x 19.07~23.25），纵向从盆底顶面（下层的 y=23.2、上层的 y=36.2）到盆口唇的下沿
     * （25.2 / 38.2）；大型填满时就是灌满整碗到唇下，所以 heightRatio 是 1，不再乘 80%。
     * </p>
     * <p>
     * 下面的盆是 0 号槽、上面的是 1 号槽。
     * </p>
     */
    private static final PotLayer[] LARGE_POTS = {
            new PotLayer(19.07F, -3.7425F, 23.25F, 0.7575F, 23.2F, 25.2F, true, 0, 1.0F, 0.0F),
            new PotLayer(19.07F, -12.0075F, 23.25F, -7.5075F, 36.2F, 38.2F, true, 1, 1.0F, 0.0F),
            new PotLayer(-23.25F, -3.7425F, -19.07F, 0.7575F, 23.2F, 25.2F, false, 0, 1.0F, 0.0F),
            new PotLayer(-23.25F, -12.0075F, -19.07F, -7.5075F, 36.2F, 38.2F, false, 1, 1.0F, 0.0F),
    };


    public PotLayer[] potLayers() {
        return switch (this) {
            case SMALL -> SMALL_POTS;
            case MEDIUM -> MEDIUM_POTS;
            case LARGE -> LARGE_POTS;
        };
    }


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
        return slots[Math.clamp(slot, 0, slots.length - 1)];
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
