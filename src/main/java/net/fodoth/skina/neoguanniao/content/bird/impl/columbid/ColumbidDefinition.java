package net.fodoth.skina.neoguanniao.content.bird.impl.columbid;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.minecraft.resources.ResourceLocation;

/**
 * 鸽形目鸟类定义类
 * 包含鸽形目鸟类的模型和动画资源定义
 */
public final class ColumbidDefinition {

    // ============ 模型资源 ============
    public static final ResourceLocation MODEL = resource("geo/columbid.geo.json");
    public static final ResourceLocation ANIMATION = resource("animations/columbid.animation.json");

    private ColumbidDefinition() {
    }

    /**
     * 创建资源位置
     *
     * @param path 资源路径
     * @return 资源位置
     */
    static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(NeoGuanNiao.MODID, path);
    }
}