package net.fodoth.skina.neoguanniao.content.bird.core.data.datum;

import net.fodoth.skina.neoguanniao.content.bird.core.model.BirdModel;
import net.fodoth.skina.neoguanniao.content.bird.core.skin.BirdSkin;
import net.fodoth.skina.neoguanniao.content.bird.feature.scale.BirdModelScaleProfile;

import java.util.List;

/**
 * 鸟类皮肤数据记录类
 * 包含鸟类模型的所有相关数据，包括模型列表、皮肤列表、缩放配置等
 *
 * @param birdModel           该模型可用的所有模型列表
 * @param birdSkin            该模型可用的所有皮肤列表
 * @param modelScaleProfile   模型的缩放配置文件（可为null，表示使用默认缩放）
 * @param shadowRadius        实体阴影的半径大小
 * @param globalScale         模型的全局缩放比例
 * @param babyScale           幼年状态下的模型缩放比例
 * @param maleScale           雄性状态下的模型缩放比例
 */
// ============ 模型数据 ============
public record BirdModelSkinDatum(
        List<BirdModel> birdModel,
        List<BirdSkin> birdSkin,
        BirdModelScaleProfile modelScaleProfile,
        float shadowRadius,
        float globalScale,
        float babyScale,
        float maleScale
) {
    /**
     * 创建默认的皮肤数据实例
     *
     * @return 返回一个包含默认配置的BirdSkinDatum实例
     */
    public static BirdModelSkinDatum createDefault() {
        return new BirdModelSkinDatum(
                List.of(BirdModel.createDefault()),
                List.of(BirdSkin.createDefault()),
                null,
                0.12F,
                1.0F,
                0.75F,
                1.1F
        );
    }

    // ============ Wither 方法 ============

    /**
     * 设置模型列表
     *
     * @param birdModel 新的模型列表
     * @return 返回修改后的新BirdSkinDatum实例
     */
    public BirdModelSkinDatum withBirdModel(List<BirdModel> birdModel) {
        return new BirdModelSkinDatum(birdModel, birdSkin, modelScaleProfile, shadowRadius, globalScale, babyScale, maleScale);
    }

    /**
     * 设置皮肤列表
     *
     * @param birdSkin 新的皮肤列表
     * @return 返回修改后的新BirdSkinDatum实例
     */
    public BirdModelSkinDatum withBirdSkin(List<BirdSkin> birdSkin) {
        return new BirdModelSkinDatum(birdModel, birdSkin, modelScaleProfile, shadowRadius, globalScale, babyScale, maleScale);
    }

    /**
     * 设置模型缩放配置
     *
     * @param modelScaleProfile 新的缩放配置（可为null）
     * @return 返回修改后的新BirdSkinDatum实例
     */
    public BirdModelSkinDatum withModelScaleProfile(BirdModelScaleProfile modelScaleProfile) {
        return new BirdModelSkinDatum(birdModel, birdSkin, modelScaleProfile, shadowRadius, globalScale, babyScale, maleScale);
    }

    /**
     * 设置阴影半径
     *
     * @param shadowRadius 新的阴影半径
     * @return 返回修改后的新BirdSkinDatum实例
     */
    public BirdModelSkinDatum withShadowRadius(float shadowRadius) {
        return new BirdModelSkinDatum(birdModel, birdSkin, modelScaleProfile, shadowRadius, globalScale, babyScale, maleScale);
    }

    /**
     * 设置全局缩放比例
     *
     * @param globalScale 新的全局缩放比例
     * @return 返回修改后的新BirdSkinDatum实例
     */
    public BirdModelSkinDatum withGlobalScale(float globalScale) {
        return new BirdModelSkinDatum(birdModel, birdSkin, modelScaleProfile, shadowRadius, globalScale, babyScale, maleScale);
    }

    /**
     * 设置幼年缩放比例
     *
     * @param babyScale 新的幼年缩放比例
     * @return 返回修改后的新BirdSkinDatum实例
     */
    public BirdModelSkinDatum withBabyScale(float babyScale) {
        return new BirdModelSkinDatum(birdModel, birdSkin, modelScaleProfile, shadowRadius, globalScale, babyScale, maleScale);
    }

    /**
     * 设置雄性缩放比例
     *
     * @param maleScale 新的雄性缩放比例
     * @return 返回修改后的新BirdSkinDatum实例
     */
    public BirdModelSkinDatum withMaleScale(float maleScale) {
        return new BirdModelSkinDatum(birdModel, birdSkin, modelScaleProfile, shadowRadius, globalScale, babyScale, maleScale);
    }

    // ============ 便捷访问方法 ============

    /**
     * 获取第一个模型（如果存在）
     *
     * @return 返回第一个模型，如果列表为空则返回null
     */
    public BirdModel getFirstModel() {
        return birdModel.isEmpty() ? null : birdModel.getFirst();
    }

    /**
     * 根据索引获取模型
     *
     * @param index 模型索引
     * @return 返回指定索引的模型，如果索引无效则返回null
     */
    public BirdModel getModel(int index) {
        return index >= 0 && index < birdModel.size() ? birdModel.get(index) : null;
    }

    /**
     * 获取模型数量
     *
     * @return 模型列表的大小
     */
    public int getModelCount() {
        return birdModel.size();
    }

    /**
     * 获取第一个皮肤（如果存在）
     *
     * @return 返回第一个皮肤，如果列表为空则返回null
     */
    public BirdSkin getFirstSkin() {
        return birdSkin.isEmpty() ? null : birdSkin.getFirst();
    }

    /**
     * 根据索引获取皮肤
     *
     * @param index 皮肤索引
     * @return 返回指定索引的皮肤，如果索引无效则返回null
     */
    public BirdSkin getSkin(int index) {
        return index >= 0 && index < birdSkin.size() ? birdSkin.get(index) : null;
    }

    /**
     * 获取皮肤数量
     *
     * @return 皮肤列表的大小
     */
    public int getSkinCount() {
        return birdSkin.size();
    }
}