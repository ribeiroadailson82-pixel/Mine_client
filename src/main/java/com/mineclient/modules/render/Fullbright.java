package com.mineclient.modules.render;

import com.mineclient.module.Category;
import com.mineclient.module.Module;

public class Fullbright extends Module {

    private float previousGamma = 1.0F;

    public Fullbright() {
        super("Fullbright", "Ilumina o mundo ao maximo (gamma).", Category.RENDER);
    }

    @Override
    protected void onEnable() {
        previousGamma = mc.gameSettings.gammaSetting;
        mc.gameSettings.gammaSetting = 100.0F;
    }

    @Override
    protected void onDisable() {
        mc.gameSettings.gammaSetting = previousGamma;
    }
}
