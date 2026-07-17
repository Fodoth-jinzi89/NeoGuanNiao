package net.fodoth.skina.neoguanniao.content.bird.core.controller;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.data.BirdData;
import net.fodoth.skina.neoguanniao.content.bird.core.data.datum.BirdSkinDatum;
import net.fodoth.skina.neoguanniao.content.bird.core.data.datum.BirdMiscDatum;
import net.fodoth.skina.neoguanniao.content.bird.core.skin.BirdSkin;
import net.fodoth.skina.neoguanniao.content.bird.core.skin.BirdSkinRarity;
import net.fodoth.skina.neoguanniao.content.bird.feature.scale.BirdModelScale;
import net.fodoth.skina.neoguanniao.content.bird.feature.scale.BirdModelScaleProfile;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity.MODEL_SCALE;
import static net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity.SKIN_VARIANT;

/**
 * 鸟类皮肤控制器
 * <p>
 * 负责管理鸟实体的皮肤相关状态，包括：
 * <ul>
 *     <li>个体模型缩放比例（Individual Model Scale）</li>
 *     <li>模型纹理资源获取</li>
 *     <li>皮肤变体（Skin Variant）</li>
 * </ul>
 * 该控制器通过 {@link AbstractBirdEntity} 的实体数据同步系统保存状态，
 * 确保皮肤属性能够在客户端与服务端之间同步。
 * </p>
 *
 */
public class BirdSkinController<T extends AbstractBirdEntity<T>> extends AbstractBirdController<T>{

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
     * 获取当前鸟种的模型缩放配置
     * <p>
     * 不同鸟种可以通过配置文件定义不同的体型变化范围。
     * </p>
     *
     * @return 模型缩放配置
     */
    public BirdModelScaleProfile modelScaleProfile() {
        BirdData birdData = bird.getBirdData();
        BirdSkinDatum modelDatum = birdData.model();
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
        BirdSkinDatum modelDatum = birdData.model();
        int skinCount = modelDatum.birdSkin().length;
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
        BirdSkinDatum modelDatum = birdData.model();
        int skinCount = modelDatum.birdSkin().length;
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
    // 完全随机 - 使用所有默认值
    public void randomizeSkinVariant() {
        randomizeSkinVariant(null, true, true, true, false);
    }

    // 只指定稀有度，其他使用默认值
    public void randomizeSkinVariant(BirdSkinRarity rarity) {
        randomizeSkinVariant(rarity, true, true, true, false);
    }

    // 指定稀有度和是否自然生成
    public void randomizeSkinVariant(BirdSkinRarity rarity, boolean natureSpawn) {
        randomizeSkinVariant(rarity, natureSpawn, true, true, false);
    }

    // 指定稀有度、自然生成和是否可繁殖
    public void randomizeSkinVariant(BirdSkinRarity rarity, boolean natureSpawn, boolean breed) {
        randomizeSkinVariant(rarity, natureSpawn, breed, true, false);
    }

    public void randomizeSkinVariant(BirdSkinRarity rarity, boolean natureSpawn, boolean breed, boolean baby) {
        randomizeSkinVariant(rarity, natureSpawn, breed, baby, false, false);
    }

    // 指定稀有度、自然生成、可繁殖和是否独特
    public void randomizeSkinVariant(BirdSkinRarity rarity, boolean natureSpawn, boolean breed, boolean baby, boolean unique) {
        randomizeSkinVariant(rarity, natureSpawn, breed, baby, unique, false);
    }

    // 完整版本 - 所有参数，基于权重随机
    public void randomizeSkinVariant(BirdSkinRarity rarity, boolean natureSpawn, boolean breed, boolean baby, boolean unique, boolean hidden) {
        var random = bird.getRandom();
        BirdData birdData = bird.getBirdData();
        BirdSkinDatum modelDatum = birdData.model();
        var skins = modelDatum.birdSkin();

        // 过滤匹配的皮肤
        List<BirdSkin> matchingSkins = new ArrayList<>();
        for (BirdSkin skin : skins) {
            boolean matches = rarity == null || skin.rarity() == rarity;

            if (matches && skin.natureSpawn() != natureSpawn) {
                matches = false;
            }

            if (matches && skin.breed() != breed) {
                matches = false;
            }

            if (matches && skin.baby() != baby) {
                matches = false;
            }

            if (matches && skin.unique() != unique) {
                matches = false;
            }

            if (matches && skin.hidden() != hidden) {
                matches = false;
            }

            if (matches) {
                matchingSkins.add(skin);
            }
        }

        // 如果还是没有匹配的，使用第一个
        if (matchingSkins.isEmpty()) {
            matchingSkins = Collections.singletonList(skins[0]);
        }

        BirdSkin selectedSkin = selectSkinByWeight(matchingSkins, random);

        // 找到原始数组中的索引
        int index = -1;
        for (int i = 0; i < skins.length; i++) {
            if (skins[i].id().equals(selectedSkin.id())) {
                index = i;
                break;
            }
        }

        setSkinVariant(index);
    }

    // 辅助方法：根据权重选择皮肤
    private BirdSkin selectSkinByWeight(List<BirdSkin> skins, RandomSource random) {
        // 计算总权重
        int totalWeight = 0;
        List<Integer> weights = new ArrayList<>();

        for (BirdSkin skin : skins) {
            int weight = skin.rarity().getWeight();
            weights.add(weight);
            totalWeight += weight;
        }

        // 如果所有权重为0，则等概率随机
        if (totalWeight == 0) {
            return skins.get(random.nextInt(skins.size()));
        }

        // 按权重随机选择
        int randomValue = random.nextInt(totalWeight);
        int cumulativeWeight = 0;

        for (int i = 0; i < skins.size(); i++) {
            cumulativeWeight += weights.get(i);
            if (randomValue < cumulativeWeight) {
                return skins.get(i);
            }
        }

        // 保险：返回第一个
        return skins.getFirst();
    }


    /**
     * 遗传父母皮肤
     * 基于父母稀有度的平均值，使用正态分布决定后代稀有度
     *
     * @param parent 父母之一
     * @param mate   父母之二
     */
    public void inheritSkinVariant(
            AbstractBirdEntity<?> parent,
            AbstractBirdEntity<?> mate
    ) {
        var birdData = bird.getBirdData();
        BirdSkinDatum modelDatum = birdData.model();
        BirdMiscDatum miscDatum = birdData.misc();

        int variants = modelDatum.birdSkin().length;
        var random = bird.getRandom();

        // 如果没有多种皮肤，直接设置为0
        if (variants <= 1) {
            setSkinVariant(0);
            return;
        }

        // 获取父母皮肤
        BirdSkin parentSkin = getSkinByIndex(parent.getModelController().getSkinVariant());
        BirdSkin mateSkin = getSkinByIndex(mate.getModelController().getSkinVariant());

        // 如果父母皮肤为null，直接随机
        if (parentSkin == null || mateSkin == null) {
            randomizeSkinVariant(null, false, true, true, false);
            return;
        }

        // 获取父母稀有度的数值
        int parentRarityValue = parentSkin.rarity().getRarity();
        int mateRarityValue = mateSkin.rarity().getRarity();

        // 计算平均值（作为分布中心）
        double baseMean = (parentRarityValue + mateRarityValue) / 2.0;

        // 检查父母是否为繁殖专用皮肤
        float actualMutantChance = getActualMutantChance(parentSkin, mateSkin, miscDatum);

        // 判断是否变异
        boolean isMutant = random.nextFloat() < actualMutantChance;

        // 计算目标稀有度
        int targetRarityInt;
        if (isMutant) {
            // 变异：偏向更高稀有度
            // 基础平均值

            // 随机偏移：有概率向更高稀有度偏移
            double offsetFactor = random.nextDouble();
            double targetMean;
            if (offsetFactor < miscDatum.mutantL1Cap()) {
                targetMean = baseMean;
            } else if (offsetFactor < miscDatum.mutantL2Cap()) {
                targetMean = baseMean + 1.0;
            } else {
                targetMean = baseMean + 2.0;
            }

            // 限制最高到5（ANCIENT）
            targetMean = Math.min(targetMean, 5.0);

            // 使用较小的标准差，让变异皮肤更集中
            double stdDev = Math.abs(parentRarityValue - mateRarityValue) / 4.0 + 0.3;
            stdDev = Math.max(stdDev, 0.3);

            // 使用Box-Muller变换生成正态分布随机数
            double u1 = random.nextDouble();
            double u2 = random.nextDouble();
            double z = Math.sqrt(-2.0 * Math.log(u1)) * Math.cos(2.0 * Math.PI * u2);

            double targetValue = targetMean + z * stdDev;
            targetRarityInt = (int) Math.round(targetValue);
        } else {
            // 正常遗传：使用正态分布
            // 标准差：基于父母稀有度的差距，差距越大，标准差越大
            double stdDev = Math.abs(parentRarityValue - mateRarityValue) / 2.0 + 0.5;
            stdDev = Math.max(stdDev, 0.5);

            // 使用Box-Muller变换生成正态分布随机数
            double u1 = random.nextDouble();
            double u2 = random.nextDouble();
            double z = Math.sqrt(-2.0 * Math.log(u1)) * Math.cos(2.0 * Math.PI * u2);

            double targetValue = baseMean + z * stdDev;
            targetRarityInt = (int) Math.round(targetValue);
        }

        // 限制范围：0-5 (COMMON到ANCIENT)，排除UNIQUE和HIDDEN
        targetRarityInt = Math.clamp(targetRarityInt, 0, 5);

        // 获取对应的稀有度枚举
        BirdSkinRarity targetRarity = BirdSkinRarity.fromValue(targetRarityInt);

        // 获取该稀有度下可用的皮肤列表
        List<BirdSkin> availableSkins = new ArrayList<>();
        for (BirdSkin skin : modelDatum.birdSkin()) {
            if (skin.rarity() == targetRarity) {
                // 如果是变异，只选择可繁殖的皮肤
                if (skin.breed()) {
                    availableSkins.add(skin);
                }
            }
        }

        // 如果没有可用的皮肤，尝试扩大范围：先找相邻稀有度
        if (availableSkins.isEmpty()) {
            // 向相邻稀有度扩展
            for (int offset = 1; offset <= 3; offset++) {
                int lowerValue = targetRarityInt - offset;
                int upperValue = targetRarityInt + offset;

                // 检查下限
                if (lowerValue >= 0) {
                    BirdSkinRarity lowerRarity = BirdSkinRarity.fromValue(lowerValue);
                    for (BirdSkin skin : modelDatum.birdSkin()) {
                        if (skin.rarity() == lowerRarity && skin.breed()) {
                            availableSkins.add(skin);
                        }
                    }
                }

                // 检查上限
                if (upperValue <= 5) {
                    BirdSkinRarity upperRarity = BirdSkinRarity.fromValue(upperValue);
                    for (BirdSkin skin : modelDatum.birdSkin()) {
                        if (skin.rarity() == upperRarity && skin.breed()) {
                            availableSkins.add(skin);
                        }
                    }
                }

                // 如果找到了任何皮肤，停止扩展
                if (!availableSkins.isEmpty()) {
                    break;
                }
            }
        }

        // 如果还是没有可用皮肤，直接设置为0并返回
        if (availableSkins.isEmpty()) {
            setSkinVariant(0);
            return;
        }

        // 从可用皮肤中随机选择一个
        BirdSkin selectedSkin = availableSkins.get(random.nextInt(availableSkins.size()));
        setSkinVariantBySkin(selectedSkin, modelDatum.birdSkin());
    }

    private static float getActualMutantChance(BirdSkin parentSkin, BirdSkin mateSkin, BirdMiscDatum miscDatum) {
        boolean parentIsBreed = parentSkin.breed();
        boolean mateIsBreed = mateSkin.breed();

        // 计算变异概率倍数
        float mutantMultiplier = 1.0f;
        if (parentIsBreed && mateIsBreed) {
            mutantMultiplier = miscDatum.mutantP1Boost(); // 两个均为繁殖专用，x10
        } else if (parentIsBreed || mateIsBreed) {
            mutantMultiplier = miscDatum.mutantP2Boost(); // 至少一个为繁殖专用，x5
        }

        // 计算实际变异概率
        float baseMutantChance = miscDatum.mutantChance();
        return baseMutantChance * mutantMultiplier;
    }

    /**
     * 根据索引获取皮肤
     */
    private BirdSkin getSkinByIndex(int index) {
        BirdData birdData = bird.getBirdData();
        BirdSkinDatum modelDatum = birdData.model();
        BirdSkin[] skins = modelDatum.birdSkin();

        if (index >= 0 && index < skins.length) {
            return skins[index];
        }
        return null;
    }

    /**
     * 根据皮肤对象设置变异体
     */
    private void setSkinVariantBySkin(BirdSkin skin, BirdSkin[] allSkins) {
        for (int i = 0; i < allSkins.length; i++) {
            if (allSkins[i].id().equals(skin.id())) {
                setSkinVariant(i);
                return;
            }
        }
        // 如果找不到，设置为0
        setSkinVariant(0);
    }

    public ResourceLocation textureForVariant(int variant) {
        var l = bird.getBirdData().model().birdSkin();
        return variant >= 0 && variant < l.length
                ? l[variant].location()
                : l[0].location();
    }
}