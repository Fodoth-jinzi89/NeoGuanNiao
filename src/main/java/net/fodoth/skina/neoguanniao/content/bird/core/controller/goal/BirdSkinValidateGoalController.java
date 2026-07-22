package net.fodoth.skina.neoguanniao.content.bird.core.controller.goal;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.skin.BirdSkin;

/**
 * 鸟类皮肤校验目标控制器
 * 负责检查并修正鸟类实体皮肤与年龄/性别状态的匹配性
 */
public class BirdSkinValidateGoalController<T extends AbstractBirdEntity<?>> extends AbstractGoalController<T> {

    @Override
    public int chance() {
        return 20; // 执行概率20%
    }

    @Override
    public boolean canUse() {
        boolean valid = isSkinValid();
        return !valid; // 皮肤无效时需要执行校验
    }

    /**
     * 检查当前皮肤是否与实体的年龄和性别状态匹配
     */
    private boolean isSkinValid() {
        BirdSkin skin = bird().getSkin();
        String family = getSkinFamily(skin.id().getPath());

        // 幼年变体验证：有幼年变体的家族必须匹配当前年龄状态
        if (hasBabyVariant(family)) {
            if (bird().isBaby() && !skin.baby()) return false;
            if (!bird().isBaby() && skin.baby()) return false;
        }

        // 性别变体验证：有性别变体的家族必须匹配当前性别状态
        if (hasGenderVariant(family)) {
            if (bird().isMale() && !skin.male()) return false;
            return bird().isMale() || skin.female();
        }

        return true;
    }

    @Override
    public boolean canContinue() {
        return false; // 一次性目标，不持续执行
    }

    @Override
    public void onStop() {
        validateSkin(); // 目标停止时执行皮肤修正
    }

    /**
     * 执行皮肤修正：查找并切换到匹配当前状态的皮肤
     */
    private void validateSkin() {
        BirdSkin current = bird().getSkin();
        BirdSkin target = findReplacement(current);

        if (!target.id().equals(current.id())) {
            bird().getSkinController().setSkinVariant(target.id());
        }
    }

    /**
     * 查找匹配当前年龄和性别状态的最佳皮肤
     */
    private BirdSkin findReplacement(BirdSkin current) {
        String family = getSkinFamily(current.id().getPath());
        BirdSkin best = null;

        for (BirdSkin skin : bird().getBirdData().model().birdSkin()) {
            if (!getSkinFamily(skin.id().getPath()).equals(family)) continue;
            if (!isSkinAvailable(skin)) continue;

            best = skin;
            if (skin.id().equals(current.id())) return skin; // 优先返回完全相同ID
        }

        return best == null ? current : best; // 无匹配则保留原皮肤
    }

    /**
     * 判断皮肤是否适用于当前年龄和性别状态
     */
    private boolean isSkinAvailable(BirdSkin skin) {
        // 年龄匹配
        if (bird().isBaby() && !skin.baby()) return false;
        if (!bird().isBaby() && skin.baby()) return false;

        // 性别匹配
        if (bird().isMale() && !skin.male()) return false;
        return bird().isMale() || skin.female();
    }

    /**
     * 从皮肤ID中提取家族名称（去除baby/male/female修饰后缀）
     */
    private String getSkinFamily(String id) {
        String[] parts = id.split("_");
        StringBuilder family = new StringBuilder();

        for (String part : parts) {
            if (part.equals("baby") || part.equals("male") || part.equals("female")) continue;
            if (!family.isEmpty()) family.append("_");
            family.append(part);
        }

        return family.toString();
    }

    /**
     * 检查指定家族是否存在幼年变体皮肤
     */
    private boolean hasBabyVariant(String family) {
        for (BirdSkin skin : bird().getBirdData().model().birdSkin()) {
            String skinFamily = getSkinFamily(skin.id().getPath());
            if (skinFamily.equals(family) && skin.baby()) return true;
        }
        return false;
    }

    /**
     * 检查指定家族是否同时存在雄性和雌性变体皮肤
     */
    private boolean hasGenderVariant(String family) {
        boolean male = false, female = false;

        for (BirdSkin skin : bird().getBirdData().model().birdSkin()) {
            String skinFamily = getSkinFamily(skin.id().getPath());
            if (!skinFamily.equals(family)) continue;

            if (skin.male()) male = true;
            if (skin.female()) female = true;

            if (male && female) return true;
        }
        return false;
    }
}