package net.fodoth.skina.neoguanniao.content.bird.core.controller;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.data.BirdData;
import net.fodoth.skina.neoguanniao.content.bird.core.data.datum.BirdModelSkinDatum;
import net.fodoth.skina.neoguanniao.content.bird.core.data.datum.BirdMiscDatum;
import net.fodoth.skina.neoguanniao.content.bird.core.skin.BirdSkin;
import net.fodoth.skina.neoguanniao.content.bird.core.skin.BirdSkinRarity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

import java.util.*;

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
        BirdModelSkinDatum modelDatum = birdData.model();
        List<BirdSkin> skins = modelDatum.birdSkin();
        int skinCount = skins.size();
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
        BirdModelSkinDatum modelDatum = birdData.model();
        List<BirdSkin> skins = modelDatum.birdSkin();
        int skinCount = skins.size();
        int clamped = Mth.clamp(variant, 0, skinCount - 1);
        bird.getEntityData().set(SKIN_VARIANT, clamped);
    }

    /**
     * 根据皮肤ID设置皮肤
     */
    public void setSkinVariant(ResourceLocation skinId) {
        List<BirdSkin> skins = bird.getBirdData().model().birdSkin();

        // 使用List的indexOf方法查找匹配的皮肤
        int index = -1;
        for (int i = 0; i < skins.size(); i++) {
            if (skins.get(i).id().equals(skinId)) {
                index = i;
                break;
            }
        }

        if (index != -1) {
            setSkinVariant(index);
        } else {
            // 找不到则使用默认皮肤
            setSkinVariant(0);
        }
    }


    public void randomizeSkinVariant() {
        setSkinVariant(getRandomizeSkinVariant());
    }

    /**
     * 随机选择一个皮肤变体
     * <p>
     * 用于鸟生成时初始化外观，
     * 根据当前鸟种支持的纹理数量随机选择皮肤。
     * </p>
     */
    // 完全随机 - 使用所有默认值
    public ResourceLocation getRandomizeSkinVariant() {
        return getRandomizeSkinVariant(null, true, true, true, true, true, false, false);
    }

    // 只指定稀有度，其他使用默认值
    public ResourceLocation getRandomizeSkinVariant(BirdSkinRarity rarity) {
        return getRandomizeSkinVariant(rarity, true, true, true, true, true, false, false);
    }

    // 指定稀有度和是否自然生成
    public ResourceLocation getRandomizeSkinVariant(BirdSkinRarity rarity, boolean natureSpawn) {
        return getRandomizeSkinVariant(rarity, natureSpawn, true, true, true, true, false, false);
    }

    // 指定稀有度、自然生成和是否可繁殖
    public ResourceLocation getRandomizeSkinVariant(BirdSkinRarity rarity, boolean natureSpawn, boolean breed) {
        return getRandomizeSkinVariant(rarity, natureSpawn, breed, true, true, true, false, false);
    }

    // 指定稀有度、自然生成、可繁殖和是否幼年
    public ResourceLocation getRandomizeSkinVariant(BirdSkinRarity rarity, boolean natureSpawn, boolean breed, boolean baby) {
        return getRandomizeSkinVariant(rarity, natureSpawn, breed, baby, true, true, false, false);
    }

    // 指定稀有度、自然生成、可繁殖、幼年和性别
    public ResourceLocation getRandomizeSkinVariant(BirdSkinRarity rarity, boolean natureSpawn, boolean breed, boolean baby, boolean male, boolean female) {
        return getRandomizeSkinVariant(rarity, natureSpawn, breed, baby, male, female, false, false);
    }

    // 指定稀有度、自然生成、可繁殖、幼年、性别和是否独特
    public ResourceLocation getRandomizeSkinVariant(BirdSkinRarity rarity, boolean natureSpawn, boolean breed, boolean baby, boolean male, boolean female, boolean unique) {
        return getRandomizeSkinVariant(rarity, natureSpawn, breed, baby, male, female, unique, false);
    }

    // 完整版本 - 所有参数，基于权重随机
    @SuppressWarnings("all")
    public ResourceLocation getRandomizeSkinVariant(BirdSkinRarity rarity, boolean natureSpawn, boolean breed, boolean baby, boolean male, boolean female, boolean unique, boolean hidden) {
        var random = bird.getRandom();
        BirdData birdData = bird.getBirdData();
        BirdModelSkinDatum modelDatum = birdData.model();
        List<BirdSkin> skins = modelDatum.birdSkin();

        // 过滤匹配的皮肤 - 使用Stream API
        List<BirdSkin> matchingSkins = skins.stream()
                .filter(skin -> {
                    // 稀有度必须先匹配（传入null表示匹配所有）
                    if (rarity != null && skin.rarity() != rarity) {
                        return false;
                    }

                    // 其他参数是"或"的关系，至少满足一个
                    return skin.natureSpawn() == natureSpawn ||
                            skin.breed() == breed ||
                            skin.baby() == baby ||
                            skin.male() == male ||
                            skin.female() == female ||
                            skin.unique() == unique ||
                            skin.hidden() == hidden;
                })
                .toList();

        // 如果还是没有匹配的，使用第一个
        if (matchingSkins.isEmpty()) {
            matchingSkins = Collections.singletonList(skins.getFirst());
        }

        BirdSkin selectedSkin = selectSkinByWeight(matchingSkins, random);

        // 返回选中的皮肤ID
        return selectedSkin.id();
    }

    // 辅助方法：根据权重选择皮肤
    private BirdSkin selectSkinByWeight(List<BirdSkin> skins, RandomSource random) {
        // 计算总权重
        int totalWeight = skins.stream()
                .mapToInt(skin -> skin.rarity().getWeight())
                .sum();

        // 如果所有权重为0，则等概率随机
        if (totalWeight == 0) {
            return skins.get(random.nextInt(skins.size()));
        }

        // 按权重随机选择
        int randomValue = random.nextInt(totalWeight);
        int cumulativeWeight = 0;

        for (BirdSkin skin : skins) {
            cumulativeWeight += skin.rarity().getWeight();
            if (randomValue < cumulativeWeight) {
                return skin;
            }
        }

        // 保险：返回第一个
        return skins.getFirst();
    }


    /**
     * 遗传父母皮肤 - 基于父母稀有度正态分布决定后代稀有度
     */
    public ResourceLocation inheritSkinVariant(
            AbstractBirdEntity<?> parent,
            AbstractBirdEntity<?> mate,
            boolean gender
    ) {
        var birdData = bird.getBirdData();
        BirdModelSkinDatum modelDatum = birdData.model();
        List<BirdSkin> skins = modelDatum.birdSkin();

        if (skins.size() <= 1) return skins.getFirst().id();

        var random = bird.getRandom();
        BirdSkin parentSkin = getSkinByIndex(parent.getSkinController().getSkinVariant());
        BirdSkin mateSkin = getSkinByIndex(mate.getSkinController().getSkinVariant());

        // 父母皮肤异常或不可繁殖 → 随机筛选
        if (parentSkin == null || mateSkin == null) {
            return randomSelectSkin(skins, gender, random);
        }

        int targetRarity = calcTargetRarity(parentSkin, mateSkin, birdData.misc(), random);
        BirdSkin selected = selectSkinByRarity(skins, targetRarity, gender);

        return selected != null ? selected.id() : fallbackSkin(skins, gender);
    }

    /** 随机选择符合性别且可繁殖的皮肤 */
    private ResourceLocation randomSelectSkin(List<BirdSkin> skins, boolean gender, RandomSource random) {
        List<BirdSkin> filtered = skins.stream()
                .filter(s -> s.breed() && (gender ? s.male() : s.female()))
                .toList();
        return filtered.isEmpty() ? skins.getFirst().id() : filtered.get(random.nextInt(filtered.size())).id();
    }

    /** 计算目标稀有度（含变异逻辑） */
    private int calcTargetRarity(BirdSkin parent, BirdSkin mate, BirdMiscDatum misc, RandomSource random) {
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

    /** 按稀有度筛选皮肤，支持 ±3 范围扩大 */
    private BirdSkin selectSkinByRarity(List<BirdSkin> skins, int target, boolean gender) {
        target = Math.clamp(target, 0, 5);

        for (int offset = 0; offset <= 3; offset++) {
            List<BirdSkin> candidates = new ArrayList<>();
            for (int delta = -offset; delta <= offset; delta += offset == 0 ? 1 : (delta < 0 ? 1 : 2 * offset)) {
                int rarity = target + delta;
                if (rarity < 0 || rarity > 5) continue;
                BirdSkinRarity r = BirdSkinRarity.fromValue(rarity);
                for (BirdSkin skin : skins) {
                    if (skin.rarity() == r && skin.breed() && skin.baby()
                            && (gender ? skin.male() : skin.female())) {
                        candidates.add(skin);
                    }
                }
            }
            if (!candidates.isEmpty()) {
                return candidates.get(bird.getRandom().nextInt(candidates.size()));
            }
        }
        return null;
    }

    /** 最终保底：任意符合性别的可繁殖皮肤 */
    private ResourceLocation fallbackSkin(List<BirdSkin> skins, boolean gender) {
        // 使用Stream API查找符合条件的第一项
        Optional<BirdSkin> skin = skins.stream()
                .filter(s -> s.breed() && (gender ? s.male() : s.female()))
                .findFirst();

        return skin.map(BirdSkin::id).orElse(skins.getFirst().id());
    }

    private static float getActualMutantChance(BirdSkin parentSkin, BirdSkin mateSkin, BirdMiscDatum miscDatum) {
        boolean parentIsBreed = !parentSkin.natureSpawn() && parentSkin.breed();
        boolean mateIsBreed = !mateSkin.natureSpawn() && mateSkin.breed();

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
        BirdModelSkinDatum modelDatum = birdData.model();
        List<BirdSkin> skins = modelDatum.birdSkin();

        if (index >= 0 && index < skins.size()) {
            return skins.get(index);
        }
        return null;
    }

    /**
     * 根据皮肤对象设置变异体
     */
    private void setSkinVariantBySkin(BirdSkin skin, List<BirdSkin> allSkins) {
        // 使用List的indexOf或遍历查找
        for (int i = 0; i < allSkins.size(); i++) {
            if (allSkins.get(i).id().equals(skin.id())) {
                setSkinVariant(i);
                return;
            }
        }
        // 如果找不到，设置为0
        setSkinVariant(0);
    }

    public ResourceLocation textureForVariant(int variant) {
        List<BirdSkin> skins = bird.getBirdData().model().birdSkin();
        return variant >= 0 && variant < skins.size()
                ? skins.get(variant).location()
                : skins.getFirst().location();
    }
}