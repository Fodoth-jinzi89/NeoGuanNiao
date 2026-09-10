package net.fodoth.skina.neoguanniao.content.camera;

public enum CameraFilterCategory {
    NATURAL("gui.neoguanniao.camera_filter_category.natural"),
    FILM("gui.neoguanniao.camera_filter_category.film"),
    MONO("gui.neoguanniao.camera_filter_category.mono"),
    MOOD("gui.neoguanniao.camera_filter_category.mood"),
    CREATIVE("gui.neoguanniao.camera_filter_category.creative");

    private final String translationKey;

    private CameraFilterCategory(String translationKey) {
        this.translationKey = translationKey;
    }

    public String translationKey() {
        return this.translationKey;
    }

    public CameraFilterCategory next() {
        CameraFilterCategory[] categories = CameraFilterCategory.values();
        return categories[(this.ordinal() + 1) % categories.length];
    }

    public CameraFilterCategory previous() {
        CameraFilterCategory[] categories = CameraFilterCategory.values();
        return categories[(this.ordinal() - 1 + categories.length) % categories.length];
    }
}

