package net.fodoth.skina.neoguanniao.content.bird.core.skin;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.minecraft.resources.ResourceLocation;

/**
 * 鸟类皮肤记录类
 * 定义了一个鸟类皮肤的所有属性，包括标识符、资源位置、稀有度以及各种获取方式的标志
 *
 * @param id           皮肤的唯一标识符
 * @param location     皮肤纹理的资源位置
 * @param rarity       皮肤的稀有度等级
 * @param natureSpawn  是否可通过自然生成获取
 * @param breed        是否可通过繁殖获取
 * @param baby         是否可通过幼年状态获取
 * @param male         是否可通过雄性状态获取
 * @param female       是否可通过雌性状态获取
 * @param unique       是否可通过独特方式获取
 * @param hidden       是否可通过隐藏方式获取
 */
public record BirdSkin(ResourceLocation id, ResourceLocation location, BirdSkinRarity rarity,
                       boolean natureSpawn, boolean breed, boolean baby,
                       boolean male, boolean female, boolean unique, boolean hidden) {

    // 创建默认实例的静态方法
    public static BirdSkin createDefault() {
        return new BirdSkin(
                ResourceLocation.fromNamespaceAndPath(NeoGuanNiao.MODID, "default"),
                ResourceLocation.fromNamespaceAndPath(NeoGuanNiao.MODID, "default_skin"),
                BirdSkinRarity.COMMON,
                true,  // natureSpawn
                true,  // breed
                true,  // baby
                true,  // male
                true,  // female
                false, // unique
                false  // hidden
        );
    }

    public BirdSkin withId(ResourceLocation id) {
        return new BirdSkin(id, location, rarity, natureSpawn, breed, baby, male, female, unique, hidden);
    }

    public BirdSkin withLocation(ResourceLocation location) {
        return new BirdSkin(id, location, rarity, natureSpawn, breed, baby, male, female, unique, hidden);
    }

    public BirdSkin withRarity(BirdSkinRarity rarity) {
        return new BirdSkin(id, location, rarity, natureSpawn, breed, baby, male, female, unique, hidden);
    }

    public BirdSkin withNatureSpawn(boolean natureSpawn) {
        return new BirdSkin(id, location, rarity, natureSpawn, breed, baby, male, female, unique, hidden);
    }

    public BirdSkin withBreed(boolean breed) {
        return new BirdSkin(id, location, rarity, natureSpawn, breed, baby, male, female, unique, hidden);
    }

    public BirdSkin withBaby(boolean baby) {
        return new BirdSkin(id, location, rarity, natureSpawn, breed, baby, male, female, unique, hidden);
    }

    public BirdSkin withMale(boolean male) {
        return new BirdSkin(id, location, rarity, natureSpawn, breed, baby, male, female, unique, hidden);
    }

    public BirdSkin withFemale(boolean female) {
        return new BirdSkin(id, location, rarity, natureSpawn, breed, baby, male, female, unique, hidden);
    }

    public BirdSkin withUnique(boolean unique) {
        return new BirdSkin(id, location, rarity, natureSpawn, breed, baby, male, female, unique, hidden);
    }

    public BirdSkin withHidden(boolean hidden) {
        return new BirdSkin(id, location, rarity, natureSpawn, breed, baby, male, female, unique, hidden);
    }
}