package net.fodoth.skina.neoguanniao.content.bird.core.data.datum;

import net.fodoth.skina.neoguanniao.content.bird.core.skin.BirdSkin;
import net.fodoth.skina.neoguanniao.content.bird.feature.scale.BirdModelScaleProfile;
import net.minecraft.resources.ResourceLocation;

/**
 * 鸟类皮肤数据记录类
 * 包含鸟类模型的所有相关数据，包括模型位置、皮肤列表、缩放配置等
 *
 * @param modelLocation       模型文件的位置路径
 * @param birdSkin            该模型可用的所有皮肤数组
 * @param modelScaleProfile   模型的缩放配置文件（可为null，表示使用默认缩放）
 * @param shadowRadius        实体阴影的半径大小
 * @param globalScale         模型的全局缩放比例
 * @param babyScale           幼年状态下的模型缩放比例
 */
// ============ 模型数据 ============
public record BirdSkinDatum(
        ResourceLocation modelLocation,
        BirdSkin[] birdSkin,
        BirdModelScaleProfile modelScaleProfile,
        float shadowRadius,
        float globalScale,
        float babyScale
) {
    /**
     * 创建默认的皮肤数据实例
     *
     * @return 返回一个包含默认皮肤配置的BirdSkinDatum实例
     */
    public static BirdSkinDatum createDefault() {
        return new BirdSkinDatum(
                null,
                new BirdSkin[]{BirdSkin.createDefault()},
                null,
                0.12F,
                1.0F,
                0.5F
        );
    }

    // ============ Wither 方法 ============

    /**
     * 设置模型位置
     *
     * @param modelLocation 新的模型位置
     * @return 返回修改后的新BirdSkinDatum实例
     */
    public BirdSkinDatum withModelLocation(ResourceLocation modelLocation) {
        return new BirdSkinDatum(modelLocation, birdSkin, modelScaleProfile, shadowRadius, globalScale, babyScale);
    }

    /**
     * 设置皮肤列表
     *
     * @param birdSkin 新的皮肤数组
     * @return 返回修改后的新BirdSkinDatum实例
     */
    public BirdSkinDatum withBirdSkin(BirdSkin[] birdSkin) {
        return new BirdSkinDatum(modelLocation, birdSkin, modelScaleProfile, shadowRadius, globalScale, babyScale);
    }

    /**
     * 设置模型缩放配置
     *
     * @param modelScaleProfile 新的缩放配置（可为null）
     * @return 返回修改后的新BirdSkinDatum实例
     */
    public BirdSkinDatum withModelScaleProfile(BirdModelScaleProfile modelScaleProfile) {
        return new BirdSkinDatum(modelLocation, birdSkin, modelScaleProfile, shadowRadius, globalScale, babyScale);
    }

    /**
     * 设置阴影半径
     *
     * @param shadowRadius 新的阴影半径
     * @return 返回修改后的新BirdSkinDatum实例
     */
    public BirdSkinDatum withShadowRadius(float shadowRadius) {
        return new BirdSkinDatum(modelLocation, birdSkin, modelScaleProfile, shadowRadius, globalScale, babyScale);
    }

    /**
     * 设置全局缩放比例
     *
     * @param globalScale 新的全局缩放比例
     * @return 返回修改后的新BirdSkinDatum实例
     */
    public BirdSkinDatum withGlobalScale(float globalScale) {
        return new BirdSkinDatum(modelLocation, birdSkin, modelScaleProfile, shadowRadius, globalScale, babyScale);
    }

    /**
     * 设置幼年缩放比例
     *
     * @param babyScale 新的幼年缩放比例
     * @return 返回修改后的新BirdSkinDatum实例
     */
    public BirdSkinDatum withBabyScale(float babyScale) {
        return new BirdSkinDatum(modelLocation, birdSkin, modelScaleProfile, shadowRadius, globalScale, babyScale);
    }
}