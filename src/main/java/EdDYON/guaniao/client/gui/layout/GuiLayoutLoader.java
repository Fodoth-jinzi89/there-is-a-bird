package EdDYON.guaniao.client.gui.layout;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraftforge.fml.loading.FMLPaths;

public final class GuiLayoutLoader {
    private static final Path BIRD_GUIDE_LAYOUT_PATH = FMLPaths.CONFIGDIR.get().resolve("guaniao/gui/bird_guide_layout.json");

    private GuiLayoutLoader() {
    }

    public static GuiLayoutConfig loadBirdGuideLayout() {
        if (!Files.isRegularFile(BIRD_GUIDE_LAYOUT_PATH)) {
            return null;
        }

        try (BufferedReader reader = Files.newBufferedReader(BIRD_GUIDE_LAYOUT_PATH, StandardCharsets.UTF_8)) {
            JsonElement rootElement = JsonParser.parseReader(reader);
            if (!rootElement.isJsonObject()) {
                return null;
            }

            JsonObject root = rootElement.getAsJsonObject();
            int baseWidth = readInt(root, "baseWidth", 0);
            int baseHeight = readInt(root, "baseHeight", 0);
            if (baseWidth <= 0 || baseHeight <= 0 || !root.has("rects") || !root.get("rects").isJsonObject()) {
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
                        readInt(rect, "h", 0));
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
        } catch (Exception ignored) {
            return null;
        }
    }

    public static boolean saveBirdGuideLayout(int baseWidth, int baseHeight, Map<String, GuiLayoutRect> rects) {
        if (baseWidth <= 0 || baseHeight <= 0 || rects.isEmpty()) {
            return false;
        }

        JsonObject root = new JsonObject();
        root.addProperty("screen", "bird_guide");
        root.addProperty("baseWidth", baseWidth);
        root.addProperty("baseHeight", baseHeight);

        JsonObject rectRoot = new JsonObject();
        for (Map.Entry<String, GuiLayoutRect> entry : rects.entrySet()) {
            GuiLayoutRect rect = entry.getValue();
            if (rect == null || !rect.isValid()) {
                continue;
            }
            JsonObject rectJson = new JsonObject();
            rectJson.addProperty("x", rect.x());
            rectJson.addProperty("y", rect.y());
            rectJson.addProperty("w", rect.w());
            rectJson.addProperty("h", rect.h());
            rectRoot.add(entry.getKey(), rectJson);
        }
        root.add("rects", rectRoot);

        try {
            Files.createDirectories(BIRD_GUIDE_LAYOUT_PATH.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(BIRD_GUIDE_LAYOUT_PATH, StandardCharsets.UTF_8)) {
                writer.write(root.toString());
            }
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static int readInt(JsonObject object, String key, int fallback) {
        if (!object.has(key) || !object.get(key).isJsonPrimitive()) {
            return fallback;
        }
        try {
            return object.get(key).getAsInt();
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }
}
