package net.fodoth.skina.neoguanniao.content.bird.core.controller.goal;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.model.BirdModel;
import net.fodoth.skina.neoguanniao.content.bird.core.skin.BirdSkin;
import net.fodoth.skina.neoguanniao.content.bird.core.skin.BirdSkinUtils;

/**
 * 鸟类皮肤校验目标控制器
 * <p>
 * 负责检查并修正鸟类皮肤是否符合：
 * - 模型支持
 * - 年龄限制
 * - 性别限制
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
     * 检查当前皮肤是否合法
     */
    private boolean isSkinValid() {

        BirdSkin skin = bird().getSkin();

        BirdModel model = bird().getModel();


        // 模型不支持
        if (!model.supportsSkin(skin.id())) {
            return false;
        }


        // 年龄和性别限制
        return BirdSkinUtils.isSkinAvailable(
                bird(),
                skin
        );
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