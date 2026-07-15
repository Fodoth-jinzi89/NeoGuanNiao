package net.fodoth.skina.neoguanniao.content.bird.core.data.datum;

import net.fodoth.skina.neoguanniao.content.bird.feature.scale.BirdModelScaleProfile;
import net.minecraft.resources.ResourceLocation;

// ============ 模型数据 ============
public record BirdModelAndTextureDatum(
        ResourceLocation modelId,
        ResourceLocation[] textureLocations,
        BirdModelScaleProfile modelScaleProfile,
        int skinVariants,
        float shadowRadius,
        float globalScale
) {
    public static BirdModelAndTextureDatum createDefault() {
        return new BirdModelAndTextureDatum(null,null, null, 1, 0.12F, 1.0F);
    }
}