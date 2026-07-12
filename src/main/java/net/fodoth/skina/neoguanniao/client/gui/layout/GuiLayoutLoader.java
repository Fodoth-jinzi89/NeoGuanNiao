package net.fodoth.skina.neoguanniao.client.gui.layout;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * GUI 布局加载器
 * 负责从 JSON 文件加载和保存布局配置
 */
public final class GuiLayoutLoader {
    private static final String BIRD_GUIDE_LAYOUT_RESOURCE = "/assets/neoguanniao/gui/bird_guide_layout.json";

    private GuiLayoutLoader() {
    }

    /**
     * 加载鸟指南布局配置
     *
     * @return 布局配置对象，如果加载失败则返回 null
     */
    public static GuiLayoutConfig loadBirdGuideLayout() {
        try (InputStream stream = GuiLayoutLoader.class.getResourceAsStream(BIRD_GUIDE_LAYOUT_RESOURCE)) {
            if (stream == null) {
                return null;
            }
            return parseLayout(new InputStreamReader(stream, StandardCharsets.UTF_8));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 保存鸟指南布局配置
     *
     * @param baseWidth 基础宽度
     * @param baseHeight 基础高度
     * @param rects 矩形区域映射
     * @return 是否保存成功
     */
    public static boolean saveBirdGuideLayout(int baseWidth, int baseHeight, Map<String, GuiLayoutRect> rects) {
        // TODO: 实现保存功能
        return false;
    }

    /**
     * 从 Reader 解析布局配置
     *
     * @param reader JSON 读取器
     * @return 布局配置对象，如果解析失败则返回 null
     */
    private static GuiLayoutConfig parseLayout(Reader reader) {
        try {
            JsonElement rootElement = JsonParser.parseReader(reader);
            if (!rootElement.isJsonObject()) {
                return null;
            }

            JsonObject root = rootElement.getAsJsonObject();
            int baseWidth = readInt(root, "baseWidth", 0);
            int baseHeight = readInt(root, "baseHeight", 0);

            if (baseWidth <= 0 || baseHeight <= 0) {
                return null;
            }

            if (!root.has("rects") || !root.get("rects").isJsonObject()) {
                return null;
            }

            Map<String, GuiLayoutRect> rects = new LinkedHashMap<>();
            JsonObject rectRoot = root.getAsJsonObject("rects");

            for (Map.Entry<String, JsonElement> entry : rectRoot.entrySet()) {
                if (!entry.getValue().isJsonObject()) {
                    continue;
                }

                JsonObject rect = entry.getValue().getAsJsonObject();
                GuiLayoutRect parsed = new GuiLayoutRect(
                        readInt(rect, "x", 0),
                        readInt(rect, "y", 0),
                        readInt(rect, "w", 0),
                        readInt(rect, "h", 0)
                );

                if (parsed.isValid()) {
                    rects.put(entry.getKey(), parsed);
                }
            }

            if (rects.isEmpty()) {
                return null;
            }

            String screen = root.has("screen") && root.get("screen").isJsonPrimitive()
                    ? root.get("screen").getAsString()
                    : "";

            return new GuiLayoutConfig(screen, baseWidth, baseHeight, rects);

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从 JsonObject 中读取整数值
     *
     * @param object JSON 对象
     * @param key 键名
     * @param fallback 默认值
     * @return 整数值，如果不存在或无效则返回默认值
     */
    @SuppressWarnings("SameParameterValue")
    private static int readInt(JsonObject object, String key, int fallback) {
        if (object.has(key) && object.get(key).isJsonPrimitive()) {
            try {
                return object.get(key).getAsInt();
            } catch (NumberFormatException e) {
                return fallback;
            }
        }
        return fallback;
    }
}