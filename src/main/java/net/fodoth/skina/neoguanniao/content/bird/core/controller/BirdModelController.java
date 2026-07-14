package net.fodoth.skina.neoguanniao.content.bird.core.controller;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.data.BirdData;
import net.fodoth.skina.neoguanniao.content.bird.core.data.datum.BirdModelDatum;
import net.fodoth.skina.neoguanniao.content.bird.core.data.datum.BirdMiscDatum;
import net.fodoth.skina.neoguanniao.content.bird.feature.scale.BirdModelScale;
import net.fodoth.skina.neoguanniao.content.bird.feature.scale.BirdModelScaleProfile;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import static net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity.MODEL_SCALE;
import static net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity.SKIN_VARIANT;

/**
 * 鸟类模型控制器
 * <p>
 * 负责管理鸟实体的模型相关状态，包括：
 * <ul>
 *     <li>个体模型缩放比例（Individual Model Scale）</li>
 *     <li>模型纹理资源获取</li>
 *     <li>皮肤变体（Skin Variant）</li>
 * </ul>
 * 该控制器通过 {@link AbstractBirdEntity} 的实体数据同步系统保存状态，
 * 确保模型属性能够在客户端与服务端之间同步。
 * </p>
 *
 * @param bird 当前控制的鸟实体
 */
public record BirdModelController(AbstractBirdEntity<?> bird) {

    /**
     * 随机生成并设置当前鸟个体的模型缩放比例
     * <p>
     * 缩放值会根据当前鸟种的 {@link BirdModelScaleProfile}
     * 进行随机生成，并自动限制在合法范围内。
     * </p>
     */
    public void randomizeModelScale() {
        var random = bird.getRandom();
        var profile = bird.modelScaleProfile();
        float scale = BirdModelScale.randomIndividualScale(random, profile);
        setIndividualModelScale(scale);
    }

    /**
     * 设置当前鸟个体的模型缩放比例
     * <p>
     * 传入的缩放值会经过 {@link BirdModelScale#sanitize(float, BirdModelScaleProfile)}
     * 处理，避免超出当前鸟种允许范围。
     * </p>
     *
     * @param scale 目标模型缩放比例
     */
    public void setIndividualModelScale(float scale) {
        var profile = bird.modelScaleProfile();
        float sanitizedScale = BirdModelScale.sanitize(scale, profile);
        bird.getEntityData().set(MODEL_SCALE, sanitizedScale);
    }

    /**
     * 获取当前鸟使用的纹理资源路径
     *
     * @return 鸟模型对应的纹理资源位置
     */
    public ResourceLocation getTextureResource() {
        BirdData birdData = bird.getBirdData();
        BirdModelDatum modelDatum = birdData.model();
        return modelDatum.location();
    }

    /**
     * 获取当前鸟种的模型缩放配置
     * <p>
     * 不同鸟种可以通过配置文件定义不同的体型变化范围。
     * </p>
     *
     * @return 模型缩放配置
     */
    public BirdModelScaleProfile modelScaleProfile() {
        BirdData birdData = bird.getBirdData();
        BirdModelDatum modelDatum = birdData.model();
        return modelDatum.modelScaleProfile();
    }

    /**
     * 获取当前鸟个体的模型缩放比例
     * <p>
     * 读取实体同步数据后，会再次进行合法性校验，
     * 防止存档数据异常导致模型比例错误。
     * </p>
     *
     * @return 当前个体模型缩放比例
     */
    public float getIndividualModelScale() {
        var profile = this.modelScaleProfile();
        float scale = bird.getEntityData().get(MODEL_SCALE);
        return BirdModelScale.sanitize(scale, profile);
    }

    /**
     * 获取当前鸟的皮肤变体编号
     * <p>
     * 返回值会被限制在当前鸟种支持的皮肤范围内，
     * 避免读取到无效的变体编号。
     * </p>
     *
     * @return 皮肤变体索引
     */
    public int getSkinVariant() {
        BirdData birdData = bird.getBirdData();
        BirdModelDatum modelDatum = birdData.model();
        int skinCount = modelDatum.skinVariants();
        int variant = bird.getEntityData().get(SKIN_VARIANT);
        return Mth.clamp(variant, 0, skinCount - 1);
    }

    /**
     * 设置当前鸟的皮肤变体
     * <p>
     * 传入编号会自动限制在当前鸟种支持的皮肤范围内。
     * </p>
     *
     * @param variant 皮肤变体索引
     */
    public void setSkinVariant(int variant) {
        BirdData birdData = bird.getBirdData();
        BirdModelDatum modelDatum = birdData.model();
        int skinCount = modelDatum.skinVariants();
        int clamped = Mth.clamp(variant, 0, skinCount - 1);
        bird.getEntityData().set(SKIN_VARIANT, clamped);
    }

    /**
     * 随机选择一个皮肤变体
     * <p>
     * 用于鸟生成时初始化外观，
     * 根据当前鸟种支持的纹理数量随机选择皮肤。
     * </p>
     */
    public void randomizeSkinVariant() {
        var random = bird.getRandom();
        BirdData birdData = bird.getBirdData();
        BirdModelDatum modelDatum = birdData.model();
        int skinCount = modelDatum.skinVariants();
        int variant = random.nextInt(skinCount);
        setSkinVariant(variant);
    }

    /**
     * 遗传父母皮肤
     *
     * @param parent 父母之一
     * @param mate   父母之二
     */
    public void inheritSkinVariant(
            AbstractBirdEntity<?> parent,
            AbstractBirdEntity<?> mate
    ) {
        var birdData = bird.getBirdData();
        BirdModelDatum modelDatum = birdData.model();
        BirdMiscDatum miscDatum = birdData.misc();

        int variants = modelDatum.skinVariants();
        var random = bird.getRandom();

        // 如果没有多种皮肤，直接设置为0
        if (variants <= 1) {
            setSkinVariant(0);
            return;
        }

        // 概率变异
        float mutantChance = miscDatum.mutantChance();
        if (random.nextFloat() < mutantChance) {
            int variant = random.nextInt(variants);
            setSkinVariant(variant);
            return;
        }

        // 从父母处继承皮肤
        int parentSkin = parent.getModelController().getSkinVariant();
        int mateSkin = mate.getModelController().getSkinVariant();
        int inheritedSkin = random.nextBoolean() ? parentSkin : mateSkin;
        setSkinVariant(inheritedSkin);
    }
}