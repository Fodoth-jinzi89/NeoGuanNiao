package net.fodoth.skina.neoguanniao.content.bird.core.controller;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;

/**
 * 所有鸟类 Controller 的基类。
 *
 * <p>Controller 不在构造阶段绑定 Entity，
 * 而是在 {@link AbstractBirdEntity} 初始化完成后通过 {@link #attach(AbstractBirdEntity)} 绑定。</p>
 *
 * @param <T> 宿主鸟类实体类型
 */
public abstract class AbstractBirdController<T extends AbstractBirdEntity<?>> {

    protected T bird;


    /**
     * 绑定所属鸟实体。
     *
     * @param bird 宿主实体
     */
    public final void attach(T bird) {
        if (this.bird != null) {
            throw new IllegalStateException(
                    "Controller is already attached to a bird entity"
            );
        }

        this.bird = bird;
        onAttach();
    }


    /**
     * Controller 绑定后的初始化回调。
     *
     * <p>适合需要依赖 Entity 的初始化逻辑。</p>
     */
    protected void onAttach() {
    }


    /**
     * 获取宿主实体。
     *
     * @return 当前绑定的鸟实体
     */
    protected final T bird() {
        if (bird == null) {
            throw new IllegalStateException(
                    "Controller is not attached to a bird entity"
            );
        }

        return bird;
    }


    /**
     * Controller tick。
     *
     * <p>需要每 tick 更新的 Controller 覆盖此方法。</p>
     */
    public void tick() {
    }


    /**
     * Entity 移除时调用。
     */
    public void onRemoved() {
    }
}