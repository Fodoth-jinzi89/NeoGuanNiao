package net.fodoth.skina.neoguanniao.content.bath;

import java.util.Locale;

public enum BirdBathContentType {

    EMPTY,
    WATER,
    FISH,
    MEAT,
    BREAD,
    FROZEN_WATER,
    SPOILED;


    public String serializedName() {
        return name().toLowerCase(Locale.ROOT);
    }


    public boolean isEmpty() {
        return this == EMPTY;
    }


    /**
     * 是否属于水类内容
     */
    public boolean isWaterLike() {
        return this == WATER || this == FROZEN_WATER;
    }


    /**
     * 是否属于食物
     */
    public boolean isFood() {
        return this == FISH
                || this == MEAT
                || this == BREAD;
    }


    public static BirdBathContentType fromOrdinal(int ordinal) {

        BirdBathContentType[] values = values();

        return ordinal >= 0 && ordinal < values.length
                ? values[ordinal]
                : EMPTY;
    }
}