package net.fodoth.skina.neoguanniao.content.bird.core.controller.goal;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.model.BirdModel;

/**
 * 鸟类模型校验目标控制器
 * 负责检查并修正鸟类实体模型与年龄/性别状态的匹配性
 */
public class BirdModelValidateGoalController<T extends AbstractBirdEntity<?>> extends AbstractGoalController<T> {

    @Override
    public int chance() {
        return 20; // 执行概率20%
    }

    @Override
    public boolean canUse() {
        boolean valid = isModelValid();
        return !valid; // 模型无效时需要执行校验
    }

    /**
     * 检查当前模型是否与实体的年龄和性别状态匹配
     */
    private boolean isModelValid() {
        BirdModel model = bird().getModel();
        String family = getModelFamily(model.id().getPath());

        // 幼年变体验证：有幼年变体的家族必须匹配当前年龄状态
        if (hasBabyVariant(family)) {
            if (bird().isBaby() && !model.baby()) return false;
            if (!bird().isBaby() && model.baby()) return false;
        }

        // 性别变体验证：有性别变体的家族必须匹配当前性别状态
        if (hasGenderVariant(family)) {
            if (bird().isMale() && !model.male()) return false;
            return bird().isMale() || model.female();
        }

        return true;
    }

    @Override
    public boolean canContinue() {
        return false; // 一次性目标，不持续执行
    }

    @Override
    public void onStop() {
        validateModel(); // 目标停止时执行模型修正
    }

    /**
     * 执行模型修正：查找并切换到匹配当前状态的模型
     */
    private void validateModel() {
        BirdModel current = bird().getModel();
        BirdModel target = findReplacement(current);

        if (!target.id().equals(current.id())) {
            bird().getModelController().setModelVariant(target.id());
        }
    }

    /**
     * 查找匹配当前年龄和性别状态的最佳模型
     */
    private BirdModel findReplacement(BirdModel current) {
        String family = getModelFamily(current.id().getPath());
        BirdModel best = null;

        for (BirdModel model : bird().getBirdData().model().birdModel()) {
            if (!getModelFamily(model.id().getPath()).equals(family)) continue;
            if (!isModelAvailable(model)) continue;

            best = model;
            if (model.id().equals(current.id())) return model; // 优先返回完全相同ID
        }

        return best == null ? current : best; // 无匹配则保留原模型
    }

    /**
     * 判断模型是否适用于当前年龄和性别状态
     */
    private boolean isModelAvailable(BirdModel model) {
        // 年龄匹配
        if (bird().isBaby() && !model.baby()) return false;
        if (!bird().isBaby() && model.baby()) return false;

        // 性别匹配
        if (bird().isMale() && !model.male()) return false;
        return bird().isMale() || model.female();
    }

    /**
     * 从模型ID中提取家族名称（去除baby/male/female修饰后缀）
     */
    private String getModelFamily(String id) {
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
     * 检查指定家族是否存在幼年变体模型
     */
    private boolean hasBabyVariant(String family) {
        for (BirdModel model : bird().getBirdData().model().birdModel()) {
            String modelFamily = getModelFamily(model.id().getPath());
            if (modelFamily.equals(family) && model.baby()) return true;
        }
        return false;
    }

    /**
     * 检查指定家族是否同时存在雄性和雌性变体模型
     */
    private boolean hasGenderVariant(String family) {
        boolean male = false, female = false;

        for (BirdModel model : bird().getBirdData().model().birdModel()) {
            String modelFamily = getModelFamily(model.id().getPath());
            if (!modelFamily.equals(family)) continue;

            if (model.male()) male = true;
            if (model.female()) female = true;

            if (male && female) return true;
        }
        return false;
    }
}