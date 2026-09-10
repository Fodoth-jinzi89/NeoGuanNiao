package net.fodoth.skina.neoguanniao.content.bath;

public enum BirdBathCleanliness {

    CLEAN,
    USED,
    DIRTY,
    FILTHY;


    public static BirdBathCleanliness fromOrdinal(int ordinal) {
        BirdBathCleanliness[] values = values();

        return ordinal >= 0 && ordinal < values.length
                ? values[ordinal]
                : CLEAN;
    }


    public boolean isDirty() {
        return this != CLEAN;
    }


    /**
     * 变脏一级
     */
    public BirdBathCleanliness nextDirtier() {

        return switch (this) {
            case CLEAN -> USED;
            case USED -> DIRTY;
            case DIRTY, FILTHY -> FILTHY;
        };
    }


    /**
     * 清洁一级
     */
    public BirdBathCleanliness cleanOneStep() {

        return switch (this) {
            case CLEAN, USED -> CLEAN;
            case DIRTY -> USED;
            case FILTHY -> DIRTY;
        };
    }


    /**
     * 用于粒子效果强度
     */
    public int particleIntensity() {

        return switch (this) {
            case CLEAN -> 0;
            case USED -> 2;
            case DIRTY -> 5;
            case FILTHY -> 8;
        };
    }
}