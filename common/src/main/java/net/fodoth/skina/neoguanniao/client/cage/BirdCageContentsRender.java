package net.fodoth.skina.neoguanniao.client.cage;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.content.cage.BirdCageBlockEntity;
import net.fodoth.skina.neoguanniao.content.cage.BirdCageVariant;
import net.fodoth.skina.neoguanniao.content.cage.SimpleFluidTank;
import net.fodoth.skina.neoguanniao.platform.BirdCageRendererHooks;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoItemTags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;


/**
 * 鸟笼里装着的东西：产量槽（鸟羽）的物品按各规格的网格铺在笼子底部，各规格自己的食盆里再显示
 * 对应食物槽的食物和流体槽的流体。
 * <p>
 * 网格、盆的位置高度、取纹理的像素块大小都由 {@link BirdCageVariant} 提供；食盆的内容物取纹理左上角的
 * 一小块铺在盆底并略微外扩（免得盆底和内沿之间露出缝），再按装满程度从盆底长高，空的时候不渲染。
 * </p>
 */
final class BirdCageContentsRender {

    /** 网格整体以底面中心缩到 0.8，四周一起往中间收，格子之间留出缝。 */
    private static final float GRID_SCALE = 0.8F;

    /** 物品模型的厚度（像素）：原版 generated 物品就是 1 像素厚。 */
    private static final float SLOT_THICKNESS = 1.0F;

    /** 物品离地面抬起一点的高度（像素），免得和地面重合成一条线。 */
    private static final float SLOT_FLOOR_LIFT = 0.05F;

    /** 几何体在模型里是 BlockBench 像素，GeckoLib 统一除以 16 换算成方块。 */
    private static final float PIXEL = 1.0F / 16.0F;

    /** 盆里的内容物每边比盆底内沿多出的宽度（像素）。 */
    private static final float OVERLAP_PIXELS = 0.3F;

    /** 食槽内容物的下限占盆内容深度的比例：浅盆（小型只有 1.5 像素深）也留得住变化。 */
    private static final float MIN_FOOD_LAYER_DEPTH_RATIO = 1.0F / 3.0F;

    /** 水槽内容物的下限占盆内容深度的比例：流体薄一点也看得清，所以比食物再小一半。 */
    private static final float MIN_FLUID_LAYER_DEPTH_RATIO = 1.0F / 6.0F;

    /** 内容物层从盆底上表面再抬起的量（像素）：层贴着盆底面会和它 z-fighting。 */
    private static final float LAYER_LIFT_PIXELS = 0.1F;

    /** 不染色：渲染颜色乘上它等于原样。 */
    private static final int NO_TINT = 0xFFFFFFFF;

    /** 只保留不透明通道：平台给的色调不一定带不透明通道（fabric 的水就没有），统一补上。 */
    private static final int OPAQUE = 0xFF000000;


    private BirdCageContentsRender() {
    }


    /** 三种规格都在笼底画产量槽网格，并按各自的盆布局画盆里的食物/流体。 */
    static void render(BirdCageBlockEntity cage, Direction facing, PoseStack poseStack,
                       MultiBufferSource buffer, int light, int overlay) {
        BirdCageVariant variant = cage.variant();
        Level level = cage.getLevel();

        // 笼子底部的产量槽。
        boolean anySlot = false;
        for (int slot = 0; slot < cage.outputSlotCount(); slot++) {
            if (!cage.itemHandler().getStackInSlot(cage.outputSlotFirst() + slot).isEmpty()) {
                anySlot = true;
                break;
            }
        }

        // 每个盆各自从它对应的食物槽 / 水槽取内容物；一个盆一个盆算，食物槽和两个水槽的内容可以不同。
        List<PotContent> pots = new ArrayList<>();
        for (BirdCageVariant.PotLayer layer : variant.potLayers()) {
            PotContent content = potContent(cage, level, layer);
            if (content != null) pots.add(content);
        }
        if (!anySlot && pots.isEmpty()) return;

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        // 和 GeoBlockRenderer.rotateBlock 一致：模型按朝向绕 Y 轴旋转，各部分的局部坐标才对得上。
        poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));

        for (int slot = 0; slot < cage.outputSlotCount(); slot++) {
            ItemStack stack = cage.itemHandler().getStackInSlot(cage.outputSlotFirst() + slot);
            if (!stack.isEmpty()) renderSlot(stack, slot, variant.slotGrid(), poseStack, buffer, level, light, overlay);
        }

        if (!pots.isEmpty()) {
            VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(InventoryMenu.BLOCK_ATLAS));
            Matrix4f matrix = poseStack.last().pose();
            float sourcePixels = variant.layerPixels();
            for (PotContent content : pots) {
                renderPot(consumer, matrix, content, sourcePixels, light);
            }
        }
        poseStack.popPose();
    }


    /** 一个盆这一帧要画的东西：布局、纹理、色调和装满程度。 */
    private record PotContent(BirdCageVariant.PotLayer layer, TextureAtlasSprite sprite, int tint, float fill) {
    }


    /** 取一个盆的内容物；对应的槽是空的（或问不到颜色）就不画这个盆。 */
    @Nullable
    private static PotContent potContent(BirdCageBlockEntity cage, Level level, BirdCageVariant.PotLayer layer) {
        if (layer.food()) {
            ItemStack food = cage.itemHandler().getStackInSlot(layer.storageIndex());
            if (food.isEmpty()) return null;
            return new PotContent(layer, foodSprite(food), NO_TINT,
                    (float) food.getCount() / cage.foodStackLimit());
        }

        SimpleFluidTank tank = cage.fluidTank(layer.storageIndex());
        Fluid fluid = tank.isEmpty() ? null : fluid(tank);
        // 水这类流体的颜色取自所在生物群系，得有世界才问得出颜色；没有世界就不画这一层。
        if (fluid == null || level == null) return null;
        TextureAtlasSprite sprite = BirdCageRendererHooks.fluidStillSprite(fluid);
        if (sprite == null) return null;
        int tint = BirdCageRendererHooks.fluidTint(fluid, level, cage.getBlockPos()) | OPAQUE;
        return new PotContent(layer, sprite, tint, (float) tank.getAmount() / tank.getCapacity());
    }


    /**
     * 把一个产量槽里的物品摆在笼子底部对应的一格里。
     * <p>
     * 用物品自己的模型渲染，所以厚度、发光、多层纹理都是正常物品的样子；网格按规格分（小型 2x2、中型 2x3、
     * 大型 3x6），槽位按行优先排，整块网格以底面中心缩到 {@link #GRID_SCALE}。
     * </p>
     */
    private static void renderSlot(ItemStack stack, int index, BirdCageVariant.SlotGrid grid,
                                   PoseStack poseStack, MultiBufferSource buffer,
                                   Level level, int light, int overlay) {
        float cellX = grid.halfX() * 2.0F * GRID_SCALE / grid.columns();
        float cellZ = grid.halfZ() * 2.0F * GRID_SCALE / grid.rows();
        int column = index % grid.columns();
        int row = index / grid.columns();
        float x = (-grid.halfX() * GRID_SCALE + (column + 0.5F) * cellX) * PIXEL;
        float z = (-grid.halfZ() * GRID_SCALE + (row + 0.5F) * cellZ) * PIXEL;
        // 物品模型本身就是 1 格（16 像素）宽，缩到比格子短的那一边，免得相邻两格重叠。
        float scale = Math.min(cellX, cellZ) * PIXEL;

        poseStack.pushPose();
        // 物品模型以中心对齐摆放点，所以这里给的是每格中心，纵向让模型正好落在底面上。
        poseStack.translate(x, (grid.floorY() + SLOT_THICKNESS / 2.0F + SLOT_FLOOR_LIFT) * PIXEL, z);
        // 躺平：物品模型正面朝上，图案就朝上。
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poseStack.scale(scale, scale, scale);
        Minecraft.getInstance().getItemRenderer()
                .renderStatic(stack, ItemDisplayContext.FIXED, light, overlay, poseStack, buffer, level, 0);
        poseStack.popPose();
    }


    /** 鱼食显示 fishes，其余食物（种子等）显示 breadcrumbs。 */
    private static TextureAtlasSprite foodSprite(ItemStack food) {
        String texture = food.is(NeoGuanNiaoItemTags.BIRD_FOOD_FISH) ? "fishes" : "breadcrumbs";
        return Minecraft.getInstance().getModelManager()
                .getAtlas(InventoryMenu.BLOCK_ATLAS)
                .getSprite(ResourceLocation.fromNamespaceAndPath(NeoGuanNiao.MODID, "block/" + texture));
    }


    @Nullable
    private static Fluid fluid(SimpleFluidTank tank) {
        ResourceLocation fluidId = tank.getFluidId();
        if (fluidId == null) return null;
        Fluid fluid = BuiltInRegistries.FLUID.get(fluidId);
        return fluid == Fluids.EMPTY ? null : fluid;
    }



    /**
     * 把一个盆里的内容物画出来：底面铺满盆底，顶面随装满程度从盆底升到盆口下方
     * {@link BirdCageVariant.PotLayer#heightRatio()} 处（大型的碗顶面没纹理，直接灌满整碗）。
     * 朝下的底面被盆底挡住，不画。
     * <p>
     * 层的起始高度统一取盆底上表面再向上 {@link #LAYER_LIFT_PIXELS} 像素，不直接压在盆底面上；
     * 高度统一按盆内容深度（{@link BirdCageVariant.PotLayer#rimY()} 减
     * {@link BirdCageVariant.PotLayer#floorY()}）的比例算，三种规格同一套规则。
     * </p>
     */
    private static void renderPot(VertexConsumer consumer, Matrix4f matrix, PotContent content,
                                  float sourcePixels, int light) {
        BirdCageVariant.PotLayer pot = content.layer();
        float overlap = OVERLAP_PIXELS * PIXEL;
        // 烘焙模型时 GeckoLib 把 X 轴取反，所以模型里的 X 是 BlockBench 像素的相反数。
        float minX = -pot.maxX() * PIXEL - overlap + pot.offsetX() * PIXEL;
        float maxX = -pot.minX() * PIXEL + overlap + pot.offsetX() * PIXEL;
        float minZ = pot.minZ() * PIXEL - overlap;
        float maxZ = pot.maxZ() * PIXEL + overlap;
        float bottom = (pot.floorY() + LAYER_LIFT_PIXELS) * PIXEL;
        // 内容物高度统一按盆内容深度的比例算（三种规格同一套规则）：下限也按盆深缩放，
        // 免得浅盆被固定下限占掉大半、看着一直是满的；装满了再按盆深长上去，
        // 所以一看厚度就知道大概装了多少。
        float depth = pot.rimY() - pot.floorY();
        float minLayer = (pot.food() ? MIN_FOOD_LAYER_DEPTH_RATIO : MIN_FLUID_LAYER_DEPTH_RATIO) * depth;
        float layerHeight = Math.max(minLayer, content.fill() * depth * pot.heightRatio());
        float top = bottom + layerHeight * PIXEL;
        TextureAtlasSprite sprite = content.sprite();
        int tint = content.tint();

        float u0 = sprite.getU0();
        float v0 = sprite.getV0();
        float u1 = sprite.getU(sourcePixels / sprite.contents().width());
        float v1 = sprite.getV(sourcePixels / sprite.contents().height());

        // 只画顶面：底面和四周都被盆壁挡住，画了也看不见。
        // 顶点顺序让 u 顺着 X、v 顺着 Z，纹理不会被拧 90°。
        vertex(consumer, matrix, light, tint, minX, top, minZ, u0, v0, 0.0F, 1.0F, 0.0F);
        vertex(consumer, matrix, light, tint, minX, top, maxZ, u0, v1, 0.0F, 1.0F, 0.0F);
        vertex(consumer, matrix, light, tint, maxX, top, maxZ, u1, v1, 0.0F, 1.0F, 0.0F);
        vertex(consumer, matrix, light, tint, maxX, top, minZ, u1, v0, 0.0F, 1.0F, 0.0F);
    }


    private static void vertex(VertexConsumer consumer, Matrix4f matrix, int light, int tint,
                               float x, float y, float z, float u, float v,
                               float normalX, float normalY, float normalZ) {
        consumer.addVertex(matrix, x, y, z)
                .setColor(tint)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(normalX, normalY, normalZ);
    }
}
