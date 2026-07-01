package com.mineclient.modules.player;

import com.mineclient.module.Category;
import com.mineclient.module.Module;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

/**
 * Mantém a tecla de sprint pressionada; o próprio vanilla decide se
 * pode correr (comida, colisão, direção do movimento etc.).
 */
public class ToggleSprint extends Module {

    public ToggleSprint() {
        super("ToggleSprint", "Corre automaticamente sem segurar a tecla.", Category.PLAYER);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !inGame()) {
            return;
        }
        if (mc.currentScreen == null) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), true);
        }
    }

    @Override
    protected void onDisable() {
        int keyCode = mc.gameSettings.keyBindSprint.getKeyCode();
        boolean held = false;
        // Keyboard pode lançar para códigos inválidos (ex.: sprint no mouse)
        try {
            held = Keyboard.isKeyDown(keyCode);
        } catch (Throwable ignored) {
        }
        // só solta se o jogador não estiver segurando a tecla física
        if (!held) {
            KeyBinding.setKeyBindState(keyCode, false);
        }
    }
}
