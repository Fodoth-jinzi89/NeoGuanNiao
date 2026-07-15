package net.fodoth.skina.neoguanniao.client.old.budgerigar;

import net.fodoth.skina.neoguanniao.content.bird.impl.old.budgerigar.BudgerigarDefinition;
import net.fodoth.skina.neoguanniao.content.bird.impl.old.budgerigar.BudgerigarEntity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.model.GeoModel;

/**
 * 虎皮鹦鹉模型
 * 使用 GeckoLib 加载虎皮鹦鹉的模型、纹理和动画
 */
public class BudgerigarModel extends GeoModel<BudgerigarEntity> {

    public BudgerigarModel() {
    }

    @Override
    public @NotNull ResourceLocation getModelResource(@NotNull BudgerigarEntity animatable) {
        return BudgerigarDefinition.MODEL;
    }

    @Override
    public @NotNull ResourceLocation getTextureResource(@NotNull BudgerigarEntity animatable) {
        return animatable.getTextureResource();
    }

    @Override
    public @NotNull ResourceLocation getAnimationResource(@NotNull BudgerigarEntity animatable) {
        return BudgerigarDefinition.ANIMATION;
    }
}