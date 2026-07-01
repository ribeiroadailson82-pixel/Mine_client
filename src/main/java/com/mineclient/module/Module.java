package com.mineclient.module;

import com.mineclient.settings.Setting;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Base de todo módulo do client. Enquanto habilitado, o módulo fica
 * registrado no barramento de eventos do Forge; os handlers ficam nos
 * próprios módulos com @SubscribeEvent.
 */
public abstract class Module {

    protected static final Minecraft mc = Minecraft.getMinecraft();

    private final String name;
    private final String description;
    private final Category category;
    private final List<Setting> settings = new ArrayList<Setting>();

    private int keyCode;
    private boolean enabled;

    protected Module(String name, String description, Category category) {
        this(name, description, category, 0);
    }

    protected Module(String name, String description, Category category, int keyCode) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.keyCode = keyCode;
    }

    protected void addSettings(Setting... toAdd) {
        settings.addAll(Arrays.asList(toAdd));
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    public void setEnabled(boolean value) {
        if (enabled == value) {
            return;
        }
        enabled = value;
        if (value) {
            MinecraftForge.EVENT_BUS.register(this);
            onEnable();
        } else {
            MinecraftForge.EVENT_BUS.unregister(this);
            onDisable();
        }
    }

    protected void onEnable() {
    }

    protected void onDisable() {
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Category getCategory() {
        return category;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }

    public List<Setting> getSettings() {
        return Collections.unmodifiableList(settings);
    }

    public Setting getSetting(String settingName) {
        for (Setting setting : settings) {
            if (setting.getName().equalsIgnoreCase(settingName)) {
                return setting;
            }
        }
        return null;
    }

    /** true quando existe jogador/mundo carregados (evita NPE em menus). */
    protected boolean inGame() {
        return mc.thePlayer != null && mc.theWorld != null;
    }
}
