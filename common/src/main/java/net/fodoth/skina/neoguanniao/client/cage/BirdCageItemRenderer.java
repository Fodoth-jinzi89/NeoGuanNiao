package net.fodoth.skina.neoguanniao.client.cage;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fodoth.skina.neoguanniao.client.DefaultBlockItemRenderer;
import net.fodoth.skina.neoguanniao.content.cage.BirdCageItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.cache.object.BakedGeoModel;

public class BirdCageItemRenderer extends DefaultBlockItemRenderer<BirdCageItem> {

    /**
     * 物品栏中同时只会展示一个鸟笼，这里只缓存最近使用的预览实体
     */
    private final CageBirdPreview preview = new CageBirdPreview();

    @Override
    protected float additionalOffsetX(BirdCageItem item, ItemDisplayContext context) {
        if (context == ItemDisplayContext.GUI)
            return item.variant().ordinal() == 1 ? 0.75F : item.variant().ordinal() == 2 ? 0.85F : 0.0F;
        if (context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                || context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)
            return item.variant().ordinal() == 1 ? 0.55F : item.variant().ordinal() == 2 ? 0.8F : 0.0F;
        return 0.0F;
    }

    @Override
    protected float additionalOffsetY(BirdCageItem item, ItemDisplayContext context) {
        if (context == ItemDisplayContext.HEAD && item.variant().ordinal() == 0) return -0.82F;
        return 0.0F;
    }

    @Override
    protected float additionalOffsetZ(BirdCageItem item, ItemDisplayContext context) {
        if (context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                || context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)
            return item.variant().ordinal() == 1 ? 0.20F : item.variant().ordinal() == 2 ? 0.25F : 0.0F;
        return 0.0F;
    }

    public BirdCageItemRenderer(float additionalScale) {
        super(new BirdCageItemModel(), additionalScale);
    }

    @Override
    public void preRender(PoseStack poseStack, BirdCageItem item, BakedGeoModel model, MultiBufferSource buffers, VertexConsumer vertex, boolean rerender, float partialTick, int light, int overlay, int colour) {
        super.preRender(poseStack, item, model, buffers, vertex, rerender, partialTick, light, overlay, colour);
        Level level = Minecraft.getInstance().level;
        if (level == null) return;
        CompoundTag data = getCurrentItemStack().getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!data.contains("CapturedBird")) return;
        Entity entity = preview.entity(level, data.getCompound("CapturedBird"));
        if (entity == null) return;
        preview.tick(level);
        BirdCageEntityRender.resetRotation(entity);
        poseStack.pushPose();
        // 物品模型空间的原点已经是笼子中心，这里只需要抬到笼子几何中心的高度。
        poseStack.translate(0.0F, (float) BirdCageEntityRender.centerY(item.variant()), 0.0F);
        BirdCageEntityRender.render(entity, item.variant(), 0.0F, partialTick, poseStack, buffers, light);
        poseStack.popPose();
    }
}
