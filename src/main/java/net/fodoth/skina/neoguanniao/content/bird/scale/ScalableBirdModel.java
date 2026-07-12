package net.fodoth.skina.neoguanniao.content.bird.scale;

/**
 * 可缩放鸟类模型接口
 *
 * <p>实现此接口的鸟类实体将支持个体缩放功能，
 * 使同一种鸟类在视觉上有个体大小差异。
 *
 * <p>缩放机制：
 * <ul>
 *   <li>每个物种有一个 {@link BirdModelScaleProfile} 定义基础缩放和个体范围</li>
 *   <li>每个个体有一个独立的缩放值，在出生时通过遗传或随机确定</li>
 *   <li>最终渲染缩放 = 基础渲染缩放 × 个体缩放</li>
 * </ul>
 */
public interface ScalableBirdModel {

    /**
     * 获取该物种的模型缩放配置文件
     *
     * @return 缩放配置文件
     */
    BirdModelScaleProfile modelScaleProfile();

    /**
     * 获取个体的模型缩放值
     * 范围由 {@link BirdModelScaleProfile#minIndividualScale()} 和
     * {@link BirdModelScaleProfile#maxIndividualScale()} 定义
     *
     * @return 个体缩放值
     */
    float getIndividualModelScale();

    /**
     * 设置个体的模型缩放值
     * 传入的值会被 {@link BirdModelScale#sanitize(float, BirdModelScaleProfile)} 清理
     *
     * @param scale 新的缩放值
     */
    void setIndividualModelScale(float scale);

    /**
     * 获取最终的模型渲染缩放值
     * 计算方式：基础渲染缩放 × 个体缩放
     *
     * @return 最终渲染缩放值
     */
    default float getModelRenderScale() {
        return BirdModelScale.renderScale(this.modelScaleProfile(), this.getIndividualModelScale());
    }

    /**
     * 检查当前个体缩放是否有效
     *
     * @return 如果缩放值在有效范围内返回 true
     */
    default boolean isIndividualScaleValid() {
        return this.modelScaleProfile().isScaleValid(this.getIndividualModelScale());
    }

    /**
     * 重置个体缩放到随机值
     *
     * @param random 随机源
     */
    default void randomizeIndividualScale(net.minecraft.util.RandomSource random) {
        this.setIndividualModelScale(BirdModelScale.randomIndividualScale(random, this.modelScaleProfile()));
    }

    /**
     * 继承父母的缩放值
     *
     * @param random 随机源
     * @param firstParent 第一个父母
     * @param secondParent 第二个父母
     */
    default void inheritIndividualScale(
            net.minecraft.util.RandomSource random,
            ScalableBirdModel firstParent,
            ScalableBirdModel secondParent
    ) {
        float childScale = BirdModelScale.inheritIndividualScale(
                random,
                firstParent.getIndividualModelScale(),
                secondParent.getIndividualModelScale(),
                this.modelScaleProfile()
        );
        this.setIndividualModelScale(childScale);
    }

    /**
     * 获取个体缩放范围的中间值
     *
     * @return 中间缩放值
     */
    default float getMidIndividualScale() {
        return this.modelScaleProfile().midIndividualScale();
    }

    /**
     * 获取个体缩放范围大小
     *
     * @return 范围大小 (max - min)
     */
    default float getIndividualScaleRange() {
        return this.modelScaleProfile().individualScaleRange();
    }
}