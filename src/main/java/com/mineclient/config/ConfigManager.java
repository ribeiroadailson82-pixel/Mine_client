package com.mineclient.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mineclient.MineClient;
import com.mineclient.module.Module;
import com.mineclient.module.ModuleManager;
import com.mineclient.settings.BooleanSetting;
import com.mineclient.settings.ModeSetting;
import com.mineclient.settings.NumberSetting;
import com.mineclient.settings.Setting;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;

/**
 * Salva/carrega o estado dos módulos (habilitado, tecla, settings) em JSON
 * dentro de .minecraft/mineclient/config.json.
 */
public final class ConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private ConfigManager() {
    }

    private static File getConfigFile() {
        File directory = new File(Minecraft.getMinecraft().mcDataDir, "mineclient");
        if (!directory.exists() && !directory.mkdirs()) {
            MineClient.LOGGER.warn("Nao foi possivel criar o diretorio de config: {}", directory);
        }
        return new File(directory, "config.json");
    }

    public static void save() {
        JsonObject root = new JsonObject();
        JsonObject modules = new JsonObject();

        for (Module module : ModuleManager.getModules()) {
            JsonObject moduleJson = new JsonObject();
            moduleJson.addProperty("enabled", module.isEnabled());
            moduleJson.addProperty("key", module.getKeyCode());

            JsonObject settingsJson = new JsonObject();
            for (Setting setting : module.getSettings()) {
                if (setting instanceof BooleanSetting) {
                    settingsJson.addProperty(setting.getName(), ((BooleanSetting) setting).getValue());
                } else if (setting instanceof NumberSetting) {
                    settingsJson.addProperty(setting.getName(), ((NumberSetting) setting).getValue());
                } else if (setting instanceof ModeSetting) {
                    settingsJson.addProperty(setting.getName(), ((ModeSetting) setting).getMode());
                }
            }
            moduleJson.add("settings", settingsJson);
            modules.add(module.getName(), moduleJson);
        }

        root.add("modules", modules);

        Writer writer = null;
        try {
            writer = new FileWriter(getConfigFile());
            GSON.toJson(root, writer);
        } catch (IOException e) {
            MineClient.LOGGER.error("Falha ao salvar config", e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    public static void load() {
        File file = getConfigFile();
        if (!file.exists()) {
            return;
        }

        JsonObject root;
        Reader reader = null;
        try {
            reader = new FileReader(file);
            JsonElement parsed = new JsonParser().parse(reader);
            if (parsed == null || !parsed.isJsonObject()) {
                return;
            }
            root = parsed.getAsJsonObject();
        } catch (Exception e) {
            MineClient.LOGGER.error("Falha ao carregar config", e);
            return;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
            }
        }

        if (!root.has("modules") || !root.get("modules").isJsonObject()) {
            return;
        }

        JsonObject modules = root.getAsJsonObject("modules");
        for (Map.Entry<String, JsonElement> entry : modules.entrySet()) {
            Module module = ModuleManager.getModule(entry.getKey());
            if (module == null || !entry.getValue().isJsonObject()) {
                continue;
            }
            JsonObject moduleJson = entry.getValue().getAsJsonObject();

            if (moduleJson.has("key")) {
                module.setKeyCode(moduleJson.get("key").getAsInt());
            }

            if (moduleJson.has("settings") && moduleJson.get("settings").isJsonObject()) {
                JsonObject settingsJson = moduleJson.getAsJsonObject("settings");
                for (Setting setting : module.getSettings()) {
                    if (!settingsJson.has(setting.getName())) {
                        continue;
                    }
                    try {
                        if (setting instanceof BooleanSetting) {
                            ((BooleanSetting) setting).setValue(settingsJson.get(setting.getName()).getAsBoolean());
                        } else if (setting instanceof NumberSetting) {
                            ((NumberSetting) setting).setValue(settingsJson.get(setting.getName()).getAsDouble());
                        } else if (setting instanceof ModeSetting) {
                            ((ModeSetting) setting).setMode(settingsJson.get(setting.getName()).getAsString());
                        }
                    } catch (Exception e) {
                        MineClient.LOGGER.warn("Setting invalida na config: {}.{}", module.getName(), setting.getName());
                    }
                }
            }

            if (moduleJson.has("enabled") && moduleJson.get("enabled").getAsBoolean()) {
                module.setEnabled(true);
            }
        }
    }
}
