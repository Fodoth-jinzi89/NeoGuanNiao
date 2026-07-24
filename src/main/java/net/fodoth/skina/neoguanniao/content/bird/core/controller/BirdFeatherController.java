package net.fodoth.skina.neoguanniao.content.bird.core.controller;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.data.BirdData;
import net.fodoth.skina.neoguanniao.content.bird.core.data.datum.BirdMiscDatum;
import net.minecraft.util.Mth;

import static net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity.FEATHER_COUNT;
import static net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity.FEATHER_INTERVAL;

public class BirdFeatherController<T extends AbstractBirdEntity<T>> extends AbstractBirdController<T> {

    // ======================== Setter ========================

    public void setFeatherCount(int featherCount) {
        BirdData birdData = bird().getBirdData();
        BirdMiscDatum miscDatum = birdData.misc();
        bird().getEntityData().set(FEATHER_COUNT, Mth.clamp(
                featherCount,
                miscDatum.featherCountMin(),
                miscDatum.featherCountMin() + miscDatum.featherCountVariance()
        ));
    }

    public void setFeatherInterval(int featherInterval) {
        BirdData birdData = bird().getBirdData();
        BirdMiscDatum miscDatum = birdData.misc();
        bird().getEntityData().set(FEATHER_INTERVAL, Mth.clamp(
                featherInterval,
                miscDatum.featherIntervalMiddle() - (miscDatum.featherIntervalVariance() / 2),
                miscDatum.featherIntervalMiddle() + (miscDatum.featherIntervalVariance() / 2)
        ));
    }

    // ======================== Getter ========================

    public int getFeatherCount() {
        BirdData birdData = bird().getBirdData();
        BirdMiscDatum miscDatum = birdData.misc();
        int featherCount = bird().getEntityData().get(FEATHER_COUNT);
        return Mth.clamp(
                featherCount,
                miscDatum.featherCountMin(),
                miscDatum.featherCountMin() + miscDatum.featherCountVariance()
        );
    }

    public int getFeatherInterval() {
        BirdData birdData = bird().getBirdData();
        BirdMiscDatum miscDatum = birdData.misc();
        int featherInterval = bird().getEntityData().get(FEATHER_INTERVAL);
        return Mth.clamp(
                featherInterval,
                miscDatum.featherIntervalMiddle() - (miscDatum.featherIntervalVariance() / 2),
                miscDatum.featherIntervalMiddle() + (miscDatum.featherIntervalVariance() / 2)
        );
    }

    // ======================== Random ========================

    public void randomizeFeatherCount() {
        BirdData birdData = bird().getBirdData();
        BirdMiscDatum miscDatum = birdData.misc();
        int min = miscDatum.featherCountMin();
        int variance = miscDatum.featherCountVariance();
        setFeatherCount(min + bird().getRandom().nextInt(variance + 1));
    }

    public void randomizeFeatherInterval() {
        BirdData birdData = bird().getBirdData();
        BirdMiscDatum miscDatum = birdData.misc();
        int middle = miscDatum.featherIntervalMiddle();
        int variance = miscDatum.featherIntervalVariance();
        int min = middle - variance / 2;
        int max = middle + variance / 2;
        setFeatherInterval(min + bird().getRandom().nextInt(max - min + 1));
    }

    public int getRandomFeatherCount() {
        BirdData birdData = bird().getBirdData();
        BirdMiscDatum miscDatum = birdData.misc();
        int min = miscDatum.featherCountMin();
        int variance = miscDatum.featherCountVariance();
        return min + bird().getRandom().nextInt(variance + 1);
    }

    public int getRandomFeatherInterval() {
        BirdData birdData = bird().getBirdData();
        BirdMiscDatum miscDatum = birdData.misc();
        int middle = miscDatum.featherIntervalMiddle();
        int variance = miscDatum.featherIntervalVariance();
        int min = middle - variance / 2;
        int max = middle + variance / 2;
        return min + bird().getRandom().nextInt(max - min + 1);
    }

    // ======================== Inherit ========================

    public int inheritFeatherCount(AbstractBirdEntity<?> parent, AbstractBirdEntity<?> mate) {
        int parentFeatherCount = parent.getFeatherCount();
        int mateFeatherCount = mate.getFeatherCount();
        var miscDatum = parent.getBirdData().misc();

        // 获取全局范围
        BirdMiscDatum misc = parent.getBirdData().misc();
        BirdMiscDatum misc1 = mate.getBirdData().misc();
        int globalMin = Math.min(misc.featherCountMin(), misc1.featherCountMin());
        int globalMax = Math.min(
                misc.featherCountMin() + misc.featherCountVariance(),
                misc1.featherCountMin() + misc1.featherCountVariance()
        );

        // 计算父母本的平均值作为中心
        double mean = (parentFeatherCount + mateFeatherCount) / 2.0;

        // 计算变异范围：至少为1，确保有变异空间
        int range = Math.max(1, (int)((globalMax - globalMin) * 0.3)); // 30%的全局范围作为变异幅度

        // 随机变异：在平均值附近波动，可以超出父母本范围
        double randomOffset = (parent.getRandom().nextDouble() * 2 - 1) * range; // -range 到 +range

        // 偏斜因子（可选）：让结果偏向更高值
        double skewFactor = parent.getRandom().nextDouble();
        double skewed = skewFactor * skewFactor;
        double resultDouble = mean + randomOffset * (0.5 + 0.5 * skewed); // 有偏斜的变异

        int result = (int) Math.round(resultDouble);
        result = Math.clamp(result, globalMin, globalMax);

        return result;
    }

    public int inheritFeatherInterval(AbstractBirdEntity<?> parent, AbstractBirdEntity<?> mate) {
        int parentFeatherInterval = parent.getFeatherInterval();
        int mateFeatherInterval = mate.getFeatherInterval();

        // 获取全局范围
        BirdMiscDatum misc = parent.getBirdData().misc();
        BirdMiscDatum misc1 = mate.getBirdData().misc();
        int globalMin = Math.min(
                misc.featherIntervalMiddle() - misc.featherIntervalVariance() / 2,
                misc1.featherIntervalMiddle() - misc1.featherIntervalVariance() / 2
        );
        int globalMax = Math.min(
                misc.featherIntervalMiddle() + misc.featherIntervalVariance() / 2,
                misc1.featherIntervalMiddle() + misc1.featherIntervalVariance() / 2
        );

        // 计算父母本的平均值作为中心
        double mean = (parentFeatherInterval + mateFeatherInterval) / 2.0;

        // 计算变异范围
        int range = Math.max(1, (int)((globalMax - globalMin) * 0.3));

        // 随机变异 + 偏向更低值
        double randomOffset = (parent.getRandom().nextDouble() * 2 - 1) * range;
        double skewFactor = parent.getRandom().nextDouble();
        double skewed = 1.0 - (skewFactor * skewFactor); // 偏向低值
        double resultDouble = mean + randomOffset * (0.5 + 0.5 * skewed);

        int result = (int) Math.round(resultDouble);
        result = Math.clamp(result, globalMin, globalMax);

        return result;
    }
}