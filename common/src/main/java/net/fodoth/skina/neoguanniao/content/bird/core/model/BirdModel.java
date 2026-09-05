package net.fodoth.skina.neoguanniao.content.bird.core.model;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

/**
 * 鸟类模型记录类
 * 定义了一个鸟类模型的所有属性，包括标识符、资源位置、稀有度以及各种获取方式的标志
 *
 * @param id           模型的唯一标识符
 * @param location     模型纹理的资源位置
 * @param rarity       模型的稀有度等级
 * @param natureSpawn  是否可通过自然生成获取
 * @param breed        是否可通过繁殖获取
 * @param baby         是否可通过幼年状态获取
 * @param male         是否可通过雄性状态获取
 * @param female       是否可通过雌性状态获取
 * @param unique       是否可通过独特方式获取
 * @param hidden       是否可通过隐藏方式获取
 */
public record BirdModel(ResourceLocation id, ResourceLocation location, BirdModelRarity rarity,
                        boolean natureSpawn, boolean breed, boolean baby,
                        boolean male, boolean female, boolean unique, boolean hidden,
                        Set<ResourceLocation> whiteList, Set<ResourceLocation> blackList) {

    // 创建默认实例的静态方法
    public static BirdModel createDefault() {
        return new BirdModel(
                ResourceLocation.fromNamespaceAndPath(NeoGuanNiao.MODID, "default"),
                ResourceLocation.fromNamespaceAndPath(NeoGuanNiao.MODID, "default_model"),
                BirdModelRarity.COMMON,
                true,  // natureSpawn
                true,  // breed
                true,  // baby
                true,  // male
                true,  // female
                false, // unique
                false,  // hidden
                Set.of(), // 空集合表示无限制
                Set.of()
        );
    }

    public boolean supportsSkin(ResourceLocation skinId) {
        if (!whiteList.isEmpty()) {
            return whiteList.contains(skinId);
        }
        return !blackList.contains(skinId);
    }

    public BirdModel withId(ResourceLocation id) {
        return new BirdModel(id, location, rarity, natureSpawn, breed, baby, male, female, unique, hidden, whiteList, blackList);
    }

    public BirdModel withLocation(ResourceLocation location) {
        return new BirdModel(id, location, rarity, natureSpawn, breed, baby, male, female, unique, hidden, whiteList, blackList);
    }

    public BirdModel withRarity(BirdModelRarity rarity) {
        return new BirdModel(id, location, rarity, natureSpawn, breed, baby, male, female, unique, hidden, whiteList, blackList);
    }

    public BirdModel withNatureSpawn(boolean natureSpawn) {
        return new BirdModel(id, location, rarity, natureSpawn, breed, baby, male, female, unique, hidden,  whiteList, blackList);
    }

    public BirdModel withBreed(boolean breed) {
        return new BirdModel(id, location, rarity, natureSpawn, breed, baby, male, female, unique, hidden,  whiteList, blackList);
    }

    public BirdModel withBaby(boolean baby) {
        return new BirdModel(id, location, rarity, natureSpawn, breed, baby, male, female, unique, hidden,  whiteList, blackList);
    }

    public BirdModel withMale(boolean male) {
        return new BirdModel(id, location, rarity, natureSpawn, breed, baby, male, female, unique, hidden,  whiteList, blackList);
    }

    public BirdModel withFemale(boolean female) {
        return new BirdModel(id, location, rarity, natureSpawn, breed, baby, male, female, unique, hidden,  whiteList, blackList);
    }

    public BirdModel withUnique(boolean unique) {
        return new BirdModel(id, location, rarity, natureSpawn, breed, baby, male, female, unique, hidden,  whiteList, blackList);
    }

    public BirdModel withHidden(boolean hidden) {
        return new BirdModel(id, location, rarity, natureSpawn, breed, baby, male, female, unique, hidden,  whiteList, blackList);
    }

    public BirdModel withWhiteList(Set<ResourceLocation> skins) {
        return new BirdModel(id, location, rarity, natureSpawn, breed, baby, male, female, unique, hidden, Set.copyOf(skins), blackList);
    }

    public BirdModel withBlackList(Set<ResourceLocation> skins) {
        return new BirdModel(id, location, rarity, natureSpawn, breed, baby, male, female, unique, hidden, whiteList, Set.copyOf(skins));
    }
}