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

        // 计算平均值作为中心值
        double mean = (parentFeatherCount + mateFeatherCount) / 2.0;

        // 偏斜分布：偏向更高值（使用指数偏斜）
        // 生成一个0-1之间的随机数，平方后使得结果偏向更高值
        double skewFactor = parent.getRandom().nextDouble();
        // 使用平方使分布偏向1（更高值）
        double skewed = skewFactor * skewFactor;
        // 将偏斜因子映射到范围：从平均值到最大值之间
        int max = Math.max(parentFeatherCount, mateFeatherCount);
        // 结果在 mean 和 max 之间偏向 max
        int result = (int) Math.round(mean + (max - mean) * skewed);

        result = Math.clamp(result, miscDatum.featherCountMin(), miscDatum.featherCountMin() + miscDatum.featherCountVariance());

        BirdMiscDatum misc = parent.getBirdData().misc();
        BirdMiscDatum misc1 = mate.getBirdData().misc();
        int min = Math.min(misc.featherCountMin(), misc1.featherCountMin());
        int maxLimit = Math.min(
                misc.featherCountMin() + misc.featherCountVariance(),
                misc1.featherCountMin() + misc1.featherCountVariance()
        );

        result = Math.clamp(result, min, maxLimit);

        return result;
    }

    public int inheritFeatherInterval(AbstractBirdEntity<?> parent, AbstractBirdEntity<?> mate) {
        int parentFeatherInterval = parent.getFeatherInterval();
        int mateFeatherInterval = mate.getFeatherInterval();
        var miscDatum = parent.getBirdData().misc();

        // 计算平均值作为中心值
        double mean = (parentFeatherInterval + mateFeatherInterval) / 2.0;

        // 偏斜分布：偏向更低值（使用指数偏斜）
        // 生成一个0-1之间的随机数，平方后使得结果偏向更低值（取反）
        double skewFactor = parent.getRandom().nextDouble();
        // 使用平方使分布偏向0（更低值），然后取反映射到范围
        double skewed = 1.0 - (skewFactor * skewFactor);
        // 将偏斜因子映射到范围：从最小值到平均值之间
        int min = Math.min(parentFeatherInterval, mateFeatherInterval);
        // 结果在 min 和 mean 之间偏向 min
        int result = (int) Math.round(min + (mean - min) * skewed);

        // 确保在合理范围内
        result = Math.clamp(result, miscDatum.featherIntervalMiddle() - (miscDatum.featherIntervalVariance() / 2), miscDatum.featherIntervalMiddle() + (miscDatum.featherIntervalVariance() / 2));

        BirdMiscDatum misc = parent.getBirdData().misc();
        BirdMiscDatum misc1 = mate.getBirdData().misc();
        int minLimit = Math.min(
                misc.featherIntervalMiddle() - misc.featherIntervalVariance() / 2,
                misc1.featherIntervalMiddle() - misc1.featherIntervalVariance() / 2
        );
        int maxLimit = Math.min(
                misc.featherIntervalMiddle() + misc.featherIntervalVariance() / 2,
                misc1.featherIntervalMiddle() + misc1.featherIntervalVariance() / 2
        );

        result = Math.clamp(result, minLimit, maxLimit);

        return result;
    }
}