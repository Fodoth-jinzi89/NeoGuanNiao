package net.fodoth.skina.neoguanniao.content.bird.core.controller.goal;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.model.BirdModel;
import net.fodoth.skina.neoguanniao.content.bird.core.skin.BirdSkin;
import net.fodoth.skina.neoguanniao.content.bird.core.skin.BirdSkinUtils;

/**
 * 鸟类皮肤校验目标控制器
 * <p>
 * 负责检查并修正鸟类实体皮肤与年龄/性别状态的匹配性
 */
public class BirdSkinValidateGoalController<T extends AbstractBirdEntity<?>>
        extends AbstractGoalController<T> {


    @Override
    public int chance() {
        return 20;
    }


    @Override
    public boolean canUse() {
        return !isSkinValid();
    }


    /**
     * 检查当前皮肤是否符合实体状态
     */
    private boolean isSkinValid() {

        BirdSkin skin = bird().getSkin();
        BirdModel model = bird().getModel();


        // 模型不支持该皮肤
        if (!model.supportsSkin(skin.id())) {
            return false;
        }


        String family = BirdSkinUtils.getSkinFamily(
                skin.id().getPath()
        );


        /*
         * 如果该家族存在幼年变体，
         * 则必须匹配当前年龄
         */
        if (BirdSkinUtils.hasBabyVariant(
                bird(),
                family
        )) {

            if (bird().isBaby() && !skin.baby()) {
                return false;
            }

            if (!bird().isBaby() && skin.baby()) {
                return false;
            }
        }


        /*
         * 如果该家族存在性别变体，
         * 则必须匹配当前性别
         */
        if (BirdSkinUtils.hasGenderVariant(
                bird(),
                family
        )) {

            if (bird().isMale() && !skin.male()) {
                return false;
            }

            return bird().isMale() || skin.female();
        }


        return true;
    }


    @Override
    public boolean canContinue() {
        return false;
    }


    @Override
    public void onStop() {
        validateSkin();
    }


    /**
     * 修正皮肤
     */
    private void validateSkin() {

        BirdSkin current = bird().getSkin();

        BirdSkin target = BirdSkinUtils.findReplacement(
                bird(),
                current
        );


        if (!target.id().equals(current.id())) {

            bird().getSkinController()
                    .setSkinVariant(target.id());
        }
    }
}