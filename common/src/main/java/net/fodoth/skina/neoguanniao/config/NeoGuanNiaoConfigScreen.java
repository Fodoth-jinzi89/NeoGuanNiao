package net.fodoth.skina.neoguanniao.config;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.fodoth.skina.neoguanniao.platform.ConfigScreenHooks;
import java.util.function.Consumer;
import java.util.function.Supplier;

import java.lang.reflect.Method;

/** Optional Cloth Config screen; safely falls back when the dependency is absent. */
public final class NeoGuanNiaoConfigScreen {
    private NeoGuanNiaoConfigScreen() {}

    public static Screen create(Screen parent) {
        try {
            Class<?> type = Class.forName("me.shedaniel.clothconfig2.api.ConfigBuilder");
            Object builder = type.getMethod("create").invoke(null);
            type.getMethod("setParentScreen", Screen.class).invoke(builder, parent);
            type.getMethod("setTitle", Component.class).invoke(builder, Component.translatable("config.neoguanniao.title"));
            Object entries = type.getMethod("entryBuilder").invoke(builder);
            Object category = type.getMethod("getOrCreateCategory", Component.class).invoke(builder, Component.translatable("config.neoguanniao.camera"));
            for (Entry entry : ConfigScreenHooks.entries()) {
                add(entries, category, entry);
            }
            type.getMethod("setSavingRunnable", Runnable.class).invoke(builder, (Runnable) ConfigScreenHooks::save);
            return (Screen) type.getMethod("build").invoke(builder);
        } catch (Exception e) {
            NeoGuanNiao.LOGGER.warn("Cloth Config is unavailable; using default config screen", e);
            return parent;
        }
    }

    private static void add(Object entries, Object category, Entry config) throws Exception {
        boolean bool = config.valueType() == boolean.class;
        boolean integer = config.valueType() == int.class;
        boolean decimal = config.valueType() == double.class;
        String method = bool ? "startBooleanToggle" : integer ? "startIntField" : decimal ? "startDoubleField" : "startLongField";
        Class<?> valueType = bool ? boolean.class : integer ? int.class : decimal ? double.class : long.class;
        Object value = config.getter().get();
        Object field = entries.getClass().getMethod(method, Component.class, valueType).invoke(entries, Component.translatable(config.key()), value);
        field.getClass().getMethod("setSaveConsumer", Consumer.class).invoke(field, config.setter());
        Object entry = field.getClass().getMethod("build").invoke(field);
        for (Method m : category.getClass().getMethods()) if (m.getName().equals("addEntry") && m.getParameterCount() == 1) { m.invoke(category, entry); return; }
    }
    public record Entry(String key, Class<?> valueType, Supplier<?> getter, Consumer<Object> setter) {}
}
