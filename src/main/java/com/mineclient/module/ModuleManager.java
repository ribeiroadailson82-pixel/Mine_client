package com.mineclient.module;

import com.mineclient.modules.combat.AutoClicker;
import com.mineclient.modules.combat.Hitboxes;
import com.mineclient.modules.combat.Reach;
import com.mineclient.modules.combat.Velocity;
import com.mineclient.modules.player.FastPlace;
import com.mineclient.modules.player.ToggleSprint;
import com.mineclient.modules.render.Fullbright;
import com.mineclient.modules.render.HUD;
import com.mineclient.modules.render.Keystrokes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ModuleManager {

    private static final List<Module> MODULES = new ArrayList<Module>();

    private ModuleManager() {
    }

    public static void init() {
        MODULES.clear();
        register(
                new Reach(),
                new Hitboxes(),
                new AutoClicker(),
                new Velocity(),
                new ToggleSprint(),
                new FastPlace(),
                new Fullbright(),
                new HUD(),
                new Keystrokes()
        );
    }

    private static void register(Module... modules) {
        Collections.addAll(MODULES, modules);
    }

    public static List<Module> getModules() {
        return Collections.unmodifiableList(MODULES);
    }

    public static List<Module> getByCategory(Category category) {
        List<Module> result = new ArrayList<Module>();
        for (Module module : MODULES) {
            if (module.getCategory() == category) {
                result.add(module);
            }
        }
        return result;
    }

    public static Module getModule(String name) {
        for (Module module : MODULES) {
            if (module.getName().equalsIgnoreCase(name)) {
                return module;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Module> T getModule(Class<T> clazz) {
        for (Module module : MODULES) {
            if (module.getClass() == clazz) {
                return (T) module;
            }
        }
        return null;
    }

    public static void onKeyPress(int keyCode) {
        for (Module module : MODULES) {
            if (module.getKeyCode() == keyCode) {
                module.toggle();
            }
        }
    }
}
