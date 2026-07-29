package net.fodoth.skina.neoguanniao.content.bird.core.skin;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.model.BirdModel;

import java.util.ArrayList;
import java.util.List;

/**
 * 鸟类皮肤工具类，提供皮肤ID解析、查找、筛选等功能
 */
public final class BirdSkinUtils {

    private BirdSkinUtils() {
        // 工具类私有构造，禁止实例化
    }

    // ========== 皮肤ID解析 ==========

    /**
     * 从皮肤ID中提取家族名称（去除年龄、性别、稀有度等修饰词）
     * 例如: "baby_male_blue_jay" -> "blue_jay"
     *
     * @param id 皮肤ID路径
     * @return 家族名称
     */
    public static String getSkinFamily(String id) {
        String[] parts = id.split("_");
        StringBuilder family = new StringBuilder();

        for (String part : parts) {
            if (isModifier(part)) {
                continue; // 跳过修饰词
            }
            if (!family.isEmpty()) {
                family.append("_");
            }
            family.append(part);
        }
        return family.toString();
    }

    /**
     * 判断字符串是否为年龄/性别修饰词
     */
    private static boolean isModifier(String part) {
        return switch (part) {
            case "mature", "baby", "male", "female" -> true;
            default -> false;
        };
    }

    // ========== 皮肤查找 ==========

    /**
     * 查找适合当前鸟类年龄和性别的皮肤（尽量保持当前皮肤不变）
     *
     * @param bird    鸟类实体
     * @param current 当前使用的皮肤
     * @param <T>     鸟类类型
     * @return 替换后的皮肤，若无合适则返回原皮肤
     */
    public static <T extends AbstractBirdEntity<?>> BirdSkin findReplacement(T bird, BirdSkin current) {
        String family = getSkinFamily(current.id().getPath());
        BirdModel model = bird.getModel();
        BirdSkin best = null;

        for (BirdSkin skin : bird.getBirdData().model().birdSkin()) {
            // 必须属于同一家族
            if (!getSkinFamily(skin.id().getPath()).equals(family)) {
                continue;
            }
            // 必须符合年龄/性别
            if (!isSkinAvailable(bird, skin)) {
                continue;
            }
            // 必须模型支持
            if (!model.supportsSkin(skin.id())) {
                continue;
            }

            best = skin;

            // 优先保持当前皮肤（如果可用）
            if (skin.id().equals(current.id())) {
                return skin;
            }
        }

        return best == null ? current : best;
    }

    /**
     * 判断皮肤是否适合当前鸟类的年龄和性别
     * <p>
     * skin.baby():
     * true  -> 幼鸟和成鸟均可使用
     * false -> 仅成鸟可使用
     * <p>
     * skin.male()/female():
     * true -> 对应性别可使用
     *
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static <T extends AbstractBirdEntity<?>> boolean isSkinAvailable(
            T bird,
            BirdSkin skin
    ) {

        /*
         * 年龄限制
         *
         * baby:
         * true  -> 幼鸟可用
         *
         * mature:
         * true  -> 成鸟可用
         */
        if (bird.isBaby() && !skin.baby()) {
            return false;
        }

        if (!bird.isBaby() && !skin.mature()) {
            return false;
        }

        // 雄性限制
        if (bird.isMale() && !skin.male()) {
            return false;
        }


        // 雌性限制
        return bird.isMale() || skin.female();
    }

    /**
     * 检测是否存在该家族支持幼鸟使用的皮肤
     */
    public static <T extends AbstractBirdEntity<?>> boolean hasBabyCompatibleSkin(
            T bird,
            String family
    ) {

        BirdModel model = bird.getModel();

        for (BirdSkin skin : bird.getBirdData().model().birdSkin()) {

            if (!getSkinFamily(skin.id().getPath()).equals(family)) {
                continue;
            }

            if (!model.supportsSkin(skin.id())) {
                continue;
            }

            // baby=true 表示幼鸟可用
            if (skin.baby()) {
                return true;
            }
        }

        return false;
    }

    // ========== 变体检测 ==========

    /**
     * 检测是否存在该家族的性别皮肤变体（同时存在雄性和雌性）
     *
     * @param bird   鸟类实体
     * @param family 家族名称
     * male=true 表示雄性可用
     * female=true 表示雌性可用
     */
    public static <T extends AbstractBirdEntity<?>> boolean hasGenderVariant(T bird, String family) {
        BirdModel model = bird.getModel();
        boolean male = false;
        boolean female = false;

        for (BirdSkin skin : bird.getBirdData().model().birdSkin()) {
            if (!getSkinFamily(skin.id().getPath()).equals(family)) {
                continue;
            }
            if (!model.supportsSkin(skin.id())) {
                continue;
            }
            male |= skin.male();
            female |= skin.female();
            if (male && female) {
                return true;
            }
        }
        return false;
    }

    // ========== 稀有度升级/降级 ==========

    /**
     * 查找比当前稀有度高一阶的皮肤（不包含远古稀有度）
     *
     * @param bird    鸟类实体
     * @param current 当前皮肤
     * @param <T>     鸟类类型
     * @return 高阶皮肤，若不存在则返回null
     */
    public static <T extends AbstractBirdEntity<?>> BirdSkin findUpgradeSkinBeforeAncient(T bird, BirdSkin current) {
        BirdSkinRarity next = BirdSkinRarity.getNextRarityBeforeAncient(current.rarity());
        if (next == null) {
            return null;
        }
        return findSkinByRarity(bird, next);
    }

    /**
     * 查找比当前稀有度低一阶的皮肤（不包含远古稀有度）
     *
     * @param bird    鸟类实体
     * @param current 当前皮肤
     * @param <T>     鸟类类型
     * @return 低阶皮肤，若不存在则返回null
     */
    public static <T extends AbstractBirdEntity<?>> BirdSkin findDowngradeSkinBeforeAncient(T bird, BirdSkin current) {
        BirdSkinRarity previous = BirdSkinRarity.getPreviousRarityBeforeAncient(current.rarity());
        if (previous == null) {
            return null;
        }
        return findSkinByRarity(bird, previous);
    }

    /**
     * 按稀有度查找皮肤（内部方法）
     */
    private static <T extends AbstractBirdEntity<?>> BirdSkin findSkinByRarity(T bird, BirdSkinRarity rarity) {
        BirdModel model = bird.getModel();
        for (BirdSkin skin : bird.getBirdData().model().birdSkin()) {
            if (skin.rarity() != rarity) {
                continue;
            }
            if (!model.supportsSkin(skin.id())) {
                continue;
            }
            if (!BirdSkinUtils.isSkinAvailable(bird, skin)) {
                continue;
            }
            return skin;
        }
        return null;
    }

    // ========== 性别切换 ==========

    /**
     * 查找当前家族中异性别的皮肤（若存在双性皮肤则优先返回）
     *
     * @param bird    鸟类实体
     * @param current 当前皮肤
     * @param <T>     鸟类类型
     * @return 异性皮肤，若不存在则返回null
     */
    public static <T extends AbstractBirdEntity<?>> BirdSkin findOppositeGenderSkin(T bird, BirdSkin current) {
        String family = BirdSkinUtils.getSkinFamily(current.id().getPath());
        boolean targetMale = !bird.isMale();
        BirdModel model = bird.getModel();

        for (BirdSkin skin : bird.getBirdData().model().birdSkin()) {
            if (!BirdSkinUtils.getSkinFamily(skin.id().getPath()).equals(family)) {
                continue;
            }
            if (!model.supportsSkin(skin.id())) {
                continue;
            }
            if (!isSkinAvailable(bird, skin)) {
                continue;
            }
            // 双性皮肤优先
            if (skin.male() && skin.female()) {
                return skin;
            }
            // 匹配目标性别
            if (targetMale && skin.male()) {
                return skin;
            }
            if (!targetMale && skin.female()) {
                return skin;
            }
        }
        return null;
    }

    // ========== 随机皮肤 ==========

    /**
     * 随机返回一个可用的稀有度为UNIQUE的皮肤
     *
     * @param bird 鸟类实体
     * @param <T>  鸟类类型
     * @return 随机独特皮肤，若无则返回null
     */
    public static <T extends AbstractBirdEntity<?>> BirdSkin findRandomUniqueSkin(T bird) {
        BirdModel model = bird.getModel();
        List<BirdSkin> candidates = new ArrayList<>();

        for (BirdSkin skin : bird.getBirdData().model().birdSkin()) {
            if (skin.rarity() != BirdSkinRarity.UNIQUE) {
                continue;
            }
            if (!model.supportsSkin(skin.id())) {
                continue;
            }
            if (!isSkinAvailable(bird, skin)) {
                continue;
            }
            candidates.add(skin);
        }

        return candidates.isEmpty() ? null : candidates.get(bird.getRandom().nextInt(candidates.size()));
    }
}