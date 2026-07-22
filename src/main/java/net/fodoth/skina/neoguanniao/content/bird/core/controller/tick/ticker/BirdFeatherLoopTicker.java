package net.fodoth.skina.neoguanniao.content.bird.core.controller.tick.ticker;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.feather.BirdFeatherData;
import net.fodoth.skina.neoguanniao.content.feather.BirdFeatherItem;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

public class BirdFeatherLoopTicker<T extends AbstractBirdEntity<T>> extends AbstractBirdTicker<T>  {

    private boolean initialized = false;

    public BirdFeatherLoopTicker() {
        super(true, false, true);
    }


    @Override
    protected void reset() {
        super.reset();
        setTicks(bird().getFeatherInterval());
        if (!initialized) {
            initialized = true;
            return;
        }
        dropFeather();
    }

    private void dropFeather() {
        if (bird().level().isClientSide) {
            return;
        }

        // 成鸟才会掉毛
        if (bird().isBaby()) {
            return;
        }

        int featherCount = bird().getFeatherCount();
        if (featherCount <= 0) {
            return;
        }

        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(bird().getType());

        // 获取羽毛数据
        BirdFeatherData featherData = BirdFeatherData.create(
                id,
                bird().getSkin().rarity().getRarity()
        );

        // 创建羽毛物品（一个 ItemStack，多数量）
        ItemStack featherStack = new ItemStack(
                NeoGuanNiaoItems.BIRD_FEATHER.get(),
                featherCount
        );
        BirdFeatherItem.setFeatherData(featherStack, featherData);

        // 生成一个掉落物
        ItemEntity itemEntity = new ItemEntity(
                bird().level(),
                bird().getX(),
                bird().getY(),
                bird().getZ(),
                featherStack
        );

        // 添加一点随机弹出速度
        itemEntity.setDeltaMovement(
                (bird().getRandom().nextDouble() - 0.5) * 0.2,
                bird().getRandom().nextDouble() * 0.1 + 0.05,
                (bird().getRandom().nextDouble() - 0.5) * 0.2
        );

        itemEntity.setPickUpDelay(40);

        bird().level().addFreshEntity(itemEntity);
    }
}