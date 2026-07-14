package net.fodoth.skina.neoguanniao.content.bird.core.data.datum;

import net.fodoth.skina.neoguanniao.content.bird.feature.scale.BirdModelScaleProfile;
import net.minecraft.resources.ResourceLocation;

// ============ 模型数据 ============
public record BirdModelDatum(
        ResourceLocation location,
        BirdModelScaleProfile modelScaleProfile,
        int skinVariants
) {
    public static BirdModelDatum createDefault() {
        return new BirdModelDatum(null, null, 1);
    }
}