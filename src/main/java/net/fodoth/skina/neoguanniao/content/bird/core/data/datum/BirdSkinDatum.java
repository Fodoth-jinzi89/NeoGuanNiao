package net.fodoth.skina.neoguanniao.content.bird.core.data.datum;

import net.fodoth.skina.neoguanniao.content.bird.core.skin.BirdSkin;
import net.fodoth.skina.neoguanniao.content.bird.feature.scale.BirdModelScaleProfile;
import net.minecraft.resources.ResourceLocation;

// ============ 模型数据 ============
public record BirdSkinDatum(
        ResourceLocation modelLocation,
        BirdSkin[] birdSkin,
        BirdModelScaleProfile modelScaleProfile,
        float shadowRadius,
        float globalScale
) {
    public static BirdSkinDatum createDefault() {
        return new BirdSkinDatum(null, new BirdSkin[]{BirdSkin.createDefault()}, null, 0.12F, 1.0F);
    }
}