package com.mineclient.input;

import com.mineclient.gui.clickgui.ClickGuiScreen;
import com.mineclient.module.ModuleManager;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

public final class KeybindHandler {

    /** Tecla que abre a ClickGUI (Right Shift). */
    public static final int OPEN_GUI_KEY = Keyboard.KEY_RSHIFT;

    private final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (mc.currentScreen != null) {
            return;
        }
        if (!Keyboard.getEventKeyState()) {
            return;
        }
        int key = Keyboard.getEventKey();
        if (key <= 0) {
            return;
        }
        if (key == OPEN_GUI_KEY) {
            mc.displayGuiScreen(new ClickGuiScreen());
            return;
        }
        ModuleManager.onKeyPress(key);
    }
}
