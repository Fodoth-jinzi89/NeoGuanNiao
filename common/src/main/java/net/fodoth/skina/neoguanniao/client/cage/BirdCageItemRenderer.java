package net.fodoth.skina.neoguanniao.client.cage;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fodoth.skina.neoguanniao.client.DefaultBlockItemRenderer;
import net.fodoth.skina.neoguanniao.content.cage.BirdCageItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import software.bernie.geckolib.cache.object.BakedGeoModel;

public class BirdCageItemRenderer extends DefaultBlockItemRenderer<BirdCageItem> {
    public BirdCageItemRenderer() {
        super(new BirdCageItemModel());
    }

    @Override
    public void preRender(PoseStack poseStack, BirdCageItem item, BakedGeoModel model, MultiBufferSource buffers, VertexConsumer vertex, boolean rerender, float partialTick, int light, int overlay, int colour) {
        super.preRender(poseStack, item, model, buffers, vertex, rerender, partialTick, light, overlay, colour);
        CompoundTag data = getCurrentItemStack().getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!data.contains("CapturedBird") || Minecraft.getInstance().level == null) return;
        Entity entity = EntityType.create(data.getCompound("CapturedBird"), Minecraft.getInstance().level).orElse(null);
        if (entity == null) return;
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.45F, 0.5F);
        float scale = 0.35F / Math.max(0.1F, Math.max(entity.getBbWidth(), entity.getBbHeight()));
        poseStack.scale(scale, scale, scale);
        Minecraft.getInstance().getEntityRenderDispatcher().render(entity, 0, 0, 0, 0, partialTick, poseStack, buffers, light);
        poseStack.popPose();
    }
}
