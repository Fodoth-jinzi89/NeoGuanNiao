package net.fodoth.skina.neoguanniao.content.bird.core.controller;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.data.BirdData;
import net.fodoth.skina.neoguanniao.content.bird.core.data.datum.BirdModelSkinDatum;
import net.fodoth.skina.neoguanniao.content.bird.core.data.datum.BirdMiscDatum;
import net.fodoth.skina.neoguanniao.content.bird.core.model.BirdModel;
import net.fodoth.skina.neoguanniao.content.bird.core.model.BirdModelRarity;
import net.fodoth.skina.neoguanniao.content.bird.feature.scale.BirdModelScale;
import net.fodoth.skina.neoguanniao.content.bird.feature.scale.BirdModelScaleProfile;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

import java.util.*;

import static net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity.MODEL_SCALE;
import static net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity.MODEL_VARIANT;

/**
 * 鸟类模型控制器
 * <p>
 * 负责管理鸟实体的模型相关状态，包括：
 * <ul>
 *     <li>个体模型缩放比例（Individual Model Scale）</li>
 *     <li>模型资源获取</li>
 *     <li>模型变体（Model Variant）</li>
 * </ul>
 * 该控制器通过 {@link AbstractBirdEntity} 的实体数据同步系统保存状态，
 * 确保模型属性能够在客户端与服务端之间同步。
 * </p>
 *
 */
public class BirdModelController<T extends AbstractBirdEntity<T>> extends AbstractBirdController<T>{

    /**
     * 随机生成并设置当前鸟个体的模型缩放比例
     * <p>
     * 缩放值会根据当前鸟种的 {@link BirdModelScaleProfile}
     * 进行随机生成，并自动限制在合法范围内。
     * </p>
     */
    public void randomizeModelScale() {
        var random = bird().getRandom();
        var profile = bird().modelScaleProfile();
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
        var profile = bird().modelScaleProfile();
        float sanitizedScale = BirdModelScale.sanitize(scale, profile);
        bird().getEntityData().set(MODEL_SCALE, sanitizedScale);
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
        BirdData birdData = bird().getBirdData();
        BirdModelSkinDatum modelDatum = birdData.model();
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
        float scale = bird().getEntityData().get(MODEL_SCALE);
        return BirdModelScale.sanitize(scale, profile);
    }

    public float getRenderModelScale() {
        var profile = this.modelScaleProfile();
        float scale = bird().getEntityData().get(MODEL_SCALE);
        return BirdModelScale.renderScale(profile, scale);
    }

    /**
     * 获取当前鸟的模型变体编号
     * <p>
     * 返回值会被限制在当前鸟种支持的模型范围内，
     * 避免读取到无效的变体编号。
     * </p>
     *
     * @return 模型变体索引
     */
    public int getModelVariant() {
        BirdData birdData = bird().getBirdData();
        BirdModelSkinDatum modelDatum = birdData.model();
        List<BirdModel> models = modelDatum.birdModel();
        int modelCount = models.size();
        int variant = bird().getEntityData().get(MODEL_VARIANT);
        return Mth.clamp(variant, 0, modelCount - 1);
    }


    /**
     * 设置当前鸟的模型变体
     * <p>
     * 传入编号会自动限制在当前鸟种支持的模型范围内。
     * </p>
     *
     * @param variant 模型变体索引
     */
    public void setModelVariant(int variant) {
        BirdData birdData = bird().getBirdData();
        BirdModelSkinDatum modelDatum = birdData.model();
        List<BirdModel> models = modelDatum.birdModel();
        int modelCount = models.size();
        int clamped = Mth.clamp(variant, 0, modelCount - 1);
        bird().getEntityData().set(MODEL_VARIANT, clamped);
    }

    /**
     * 根据模型ID设置模型
     */
    public void setModelVariant(ResourceLocation modelId) {
        List<BirdModel> models = bird().getBirdData().model().birdModel();

        // 查找匹配的模型
        int index = -1;
        for (int i = 0; i < models.size(); i++) {
            if (models.get(i).id().equals(modelId)) {
                index = i;
                break;
            }
        }

        if (index != -1) {
            setModelVariant(index);
        } else {
            // 找不到则使用默认模型
            setModelVariant(0);
        }
    }


    public void randomizeModelVariant() {
        setModelVariant(getRandomizeModelVariant());
    }

    /**
     * 随机选择一个模型变体
     * <p>
     * 用于鸟生成时初始化外观，
     * 根据当前鸟种支持的模型数量随机选择模型。
     * </p>
     */
    // 完全随机 - 使用所有默认值
    public ResourceLocation getRandomizeModelVariant() {
        return getRandomizeModelVariant(null, true, true, true, true, true, false, false);
    }

    // 只指定稀有度，其他使用默认值
    public ResourceLocation getRandomizeModelVariant(BirdModelRarity rarity) {
        return getRandomizeModelVariant(rarity, true, true, true, true, true, false, false);
    }

    // 指定稀有度和是否自然生成
    public ResourceLocation getRandomizeModelVariant(BirdModelRarity rarity, boolean natureSpawn) {
        return getRandomizeModelVariant(rarity, natureSpawn, true, true, true, true, false, false);
    }

    // 指定稀有度、自然生成和是否可繁殖
    public ResourceLocation getRandomizeModelVariant(BirdModelRarity rarity, boolean natureSpawn, boolean breed) {
        return getRandomizeModelVariant(rarity, natureSpawn, breed, true, true, true, false, false);
    }

    // 指定稀有度、自然生成、可繁殖和是否幼年
    public ResourceLocation getRandomizeModelVariant(BirdModelRarity rarity, boolean natureSpawn, boolean breed, boolean baby) {
        return getRandomizeModelVariant(rarity, natureSpawn, breed, baby, true, true, false, false);
    }

    // 指定稀有度、自然生成、可繁殖、幼年和性别
    public ResourceLocation getRandomizeModelVariant(BirdModelRarity rarity, boolean natureSpawn, boolean breed, boolean baby, boolean male, boolean female) {
        return getRandomizeModelVariant(rarity, natureSpawn, breed, baby, male, female, false, false);
    }

    // 指定稀有度、自然生成、可繁殖、幼年、性别和是否独特
    public ResourceLocation getRandomizeModelVariant(BirdModelRarity rarity, boolean natureSpawn, boolean breed, boolean baby, boolean male, boolean female, boolean unique) {
        return getRandomizeModelVariant(rarity, natureSpawn, breed, baby, male, female, unique, false);
    }

    // 完整版本 - 所有参数，基于权重随机
    @SuppressWarnings("all")
    public ResourceLocation getRandomizeModelVariant(BirdModelRarity rarity, boolean natureSpawn, boolean breed, boolean baby, boolean male, boolean female, boolean unique, boolean hidden) {
        var random = bird().getRandom();
        BirdData birdData = bird().getBirdData();
        BirdModelSkinDatum modelDatum = birdData.model();
        List<BirdModel> models = modelDatum.birdModel();

        // 过滤匹配的模型 - 使用Stream API
        List<BirdModel> matchingModels = models.stream()
                .filter(model -> {
                    // 稀有度必须先匹配（传入null表示匹配所有）
                    if (rarity != null && model.rarity() != rarity) {
                        return false;
                    }

                    // 其他参数是"或"的关系，至少满足一个
                    return model.natureSpawn() == natureSpawn ||
                            model.breed() == breed ||
                            model.baby() == baby ||
                            model.male() == male ||
                            model.female() == female ||
                            model.unique() == unique ||
                            model.hidden() == hidden;
                })
                .toList();

        // 如果还是没有匹配的，使用第一个
        if (matchingModels.isEmpty()) {
            matchingModels = Collections.singletonList(models.getFirst());
        }

        BirdModel selectedModel = selectModelByWeight(matchingModels, random);

        // 返回选中的模型ID
        return selectedModel.id();
    }

    // 辅助方法：根据权重选择模型
    private BirdModel selectModelByWeight(List<BirdModel> models, RandomSource random) {
        // 计算总权重
        int totalWeight = models.stream()
                .mapToInt(model -> model.rarity().getWeight())
                .sum();

        // 如果所有权重为0，则等概率随机
        if (totalWeight == 0) {
            return models.get(random.nextInt(models.size()));
        }

        // 按权重随机选择
        int randomValue = random.nextInt(totalWeight);
        int cumulativeWeight = 0;

        for (BirdModel model : models) {
            cumulativeWeight += model.rarity().getWeight();
            if (randomValue < cumulativeWeight) {
                return model;
            }
        }

        // 保险：返回第一个
        return models.getFirst();
    }


    /**
     * 遗传父母模型 - 基于父母稀有度正态分布决定后代稀有度
     */
    public ResourceLocation inheritModelVariant(
            AbstractBirdEntity<?> parent,
            AbstractBirdEntity<?> mate,
            boolean gender
    ) {
        var birdData = bird().getBirdData();
        BirdModelSkinDatum modelDatum = birdData.model();
        List<BirdModel> models = modelDatum.birdModel();

        if (models.size() <= 1) return models.getFirst().id();

        var random = bird().getRandom();
        BirdModel parentModel = getModelByIndex(parent.getModelController().getModelVariant());
        BirdModel mateModel = getModelByIndex(mate.getModelController().getModelVariant());

        // 父母模型异常或不可繁殖 → 随机筛选
        if (parentModel == null || mateModel == null) {
            return randomSelectModel(models, gender, random);
        }

        int targetRarity = calcTargetRarity(parentModel, mateModel, birdData.misc(), random);
        BirdModel selected = selectModelByRarity(models, targetRarity, gender);

        return selected != null ? selected.id() : fallbackModel(models, gender);
    }

    /** 随机选择符合性别且可繁殖的模型 */
    private ResourceLocation randomSelectModel(List<BirdModel> models, boolean gender, RandomSource random) {
        List<BirdModel> filtered = models.stream()
                .filter(m -> m.breed() && (gender ? m.male() : m.female()))
                .toList();
        return filtered.isEmpty() ? models.getFirst().id() : filtered.get(random.nextInt(filtered.size())).id();
    }

    /** 计算目标稀有度（含变异逻辑） */
    private int calcTargetRarity(BirdModel parent, BirdModel mate, BirdMiscDatum misc, RandomSource random) {
        int pRarity = parent.rarity().getRarity();
        int mRarity = mate.rarity().getRarity();
        double baseMean = (pRarity + mRarity) / 2.0;

        boolean isMutant = random.nextFloat() < getActualMutantChance(parent, mate, misc);

        if (isMutant) {
            double offset = random.nextDouble();
            double mean = offset < misc.mutantL1Cap() ? baseMean :
                    offset < misc.mutantL2Cap() ? baseMean + 1.0 : baseMean + 2.0;
            mean = Math.min(mean, 5.0);
            double stdDev = Math.max(Math.abs(pRarity - mRarity) / 4.0 + 0.3, 0.3);
            return (int) Math.round(mean + gaussianZ(random) * stdDev);
        } else {
            double stdDev = Math.max(Math.abs(pRarity - mRarity) / 2.0 + 0.5, 0.5);
            return (int) Math.round(baseMean + gaussianZ(random) * stdDev);
        }
    }

    /** Box-Muller 生成标准正态分布随机数 */
    private double gaussianZ(RandomSource random) {
        double u1 = random.nextDouble(), u2 = random.nextDouble();
        return Math.sqrt(-2.0 * Math.log(u1)) * Math.cos(2.0 * Math.PI * u2);
    }

    /** 按稀有度筛选模型，支持 ±3 范围扩大 */
    private BirdModel selectModelByRarity(List<BirdModel> models, int target, boolean gender) {
        target = Math.clamp(target, 0, 5);

        for (int offset = 0; offset <= 3; offset++) {
            List<BirdModel> candidates = new ArrayList<>();
            for (int delta = -offset; delta <= offset; delta += offset == 0 ? 1 : (delta < 0 ? 1 : 2 * offset)) {
                int rarity = target + delta;
                if (rarity < 0 || rarity > 5) continue;
                BirdModelRarity r = BirdModelRarity.fromValue(rarity);
                for (BirdModel model : models) {
                    if (model.rarity() == r && model.breed() && model.baby()
                            && (gender ? model.male() : model.female())) {
                        candidates.add(model);
                    }
                }
            }
            if (!candidates.isEmpty()) {
                return candidates.get(bird().getRandom().nextInt(candidates.size()));
            }
        }
        return null;
    }

    /** 最终保底：任意符合性别的可繁殖模型 */
    private ResourceLocation fallbackModel(List<BirdModel> models, boolean gender) {
        // 使用Stream API查找符合条件的第一项
        Optional<BirdModel> model = models.stream()
                .filter(m -> m.breed() && (gender ? m.male() : m.female()))
                .findFirst();

        return model.map(BirdModel::id).orElse(models.getFirst().id());
    }

    private static float getActualMutantChance(BirdModel parentModel, BirdModel mateModel, BirdMiscDatum miscDatum) {
        boolean parentIsBreed = !parentModel.natureSpawn() && parentModel.breed();
        boolean mateIsBreed = !mateModel.natureSpawn() && mateModel.breed();

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
     * 根据索引获取模型
     */
    private BirdModel getModelByIndex(int index) {
        BirdData birdData = bird().getBirdData();
        BirdModelSkinDatum modelDatum = birdData.model();
        List<BirdModel> models = modelDatum.birdModel();

        if (index >= 0 && index < models.size()) {
            return models.get(index);
        }
        return null;
    }

    /**
     * 根据模型对象设置变体
     */
    private void setModelVariantByModel(BirdModel model, List<BirdModel> allModels) {
        // 遍历查找
        for (int i = 0; i < allModels.size(); i++) {
            if (allModels.get(i).id().equals(model.id())) {
                setModelVariant(i);
                return;
            }
        }
        // 如果找不到，设置为0
        setModelVariant(0);
    }

    public ResourceLocation modelForVariant(int variant) {
        List<BirdModel> models = bird().getBirdData().model().birdModel();
        return variant >= 0 && variant < models.size()
                ? models.get(variant).location()
                : models.getFirst().location();
    }
}