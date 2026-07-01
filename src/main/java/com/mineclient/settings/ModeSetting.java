package com.mineclient.settings;

import java.util.Arrays;
import java.util.List;

public class ModeSetting extends Setting {

    private final List<String> modes;
    private int index;

    public ModeSetting(String name, String defaultMode, String... modes) {
        super(name);
        this.modes = Arrays.asList(modes);
        int idx = this.modes.indexOf(defaultMode);
        this.index = Math.max(0, idx);
    }

    public String getMode() {
        return modes.get(index);
    }

    public boolean is(String mode) {
        return getMode().equalsIgnoreCase(mode);
    }

    public void setMode(String mode) {
        for (int i = 0; i < modes.size(); i++) {
            if (modes.get(i).equalsIgnoreCase(mode)) {
                index = i;
                return;
            }
        }
    }

    public void cycle() {
        index = (index + 1) % modes.size();
    }

    public List<String> getModes() {
        return modes;
    }
}
