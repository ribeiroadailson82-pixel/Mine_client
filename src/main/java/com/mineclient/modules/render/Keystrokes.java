package com.mineclient.modules.render;

import com.mineclient.module.Category;
import com.mineclient.module.Module;
import com.mineclient.settings.NumberSetting;
import com.mineclient.util.RenderUtil;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

/**
 * Overlay clássico de keystrokes: W em cima, A S D embaixo e as
 * caixas LMB/RMB abaixo. Não desenha com o F3 aberto.
 */
public class Keystrokes extends Module {

    private static final int KEY_SIZE = 20;
    private static final int MOUSE_WIDTH = 31;
    private static final int MOUSE_HEIGHT = 14;
    private static final int GAP = 1;

    private static final int BG_RELEASED = 0x80000000;
    private static final int BG_PRESSED = 0x80FFFFFF;
    private static final int TEXT_RELEASED = 0xFFFFFFFF;
    private static final int TEXT_PRESSED = 0xFF000000;

    private final NumberSetting x = new NumberSetting("X", 5, 0, 500, 1);
    private final NumberSetting y = new NumberSetting("Y", 60, 0, 500, 1);

    public Keystrokes() {
        super("Keystrokes", "Mostra as teclas de movimento e cliques do mouse.", Category.RENDER);
        addSettings(x, y);
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) {
            return;
        }
        if (!inGame() || mc.gameSettings.showDebugInfo) {
            return;
        }

        double baseX = x.getValue();
        double baseY = y.getValue();
        double column = KEY_SIZE + GAP;

        // linha de cima: W centralizado (acima do S)
        drawKey(baseX + column, baseY, KEY_SIZE, KEY_SIZE,
                keyLabel(mc.gameSettings.keyBindForward), mc.gameSettings.keyBindForward.isKeyDown());

        // linha do meio: A S D
        double rowAsd = baseY + column;
        drawKey(baseX, rowAsd, KEY_SIZE, KEY_SIZE,
                keyLabel(mc.gameSettings.keyBindLeft), mc.gameSettings.keyBindLeft.isKeyDown());
        drawKey(baseX + column, rowAsd, KEY_SIZE, KEY_SIZE,
                keyLabel(mc.gameSettings.keyBindBack), mc.gameSettings.keyBindBack.isKeyDown());
        drawKey(baseX + column * 2.0D, rowAsd, KEY_SIZE, KEY_SIZE,
                keyLabel(mc.gameSettings.keyBindRight), mc.gameSettings.keyBindRight.isKeyDown());

        // linha de baixo: botões do mouse
        double rowMouse = rowAsd + column;
        drawKey(baseX, rowMouse, MOUSE_WIDTH, MOUSE_HEIGHT, "LMB", Mouse.isButtonDown(0));
        drawKey(baseX + MOUSE_WIDTH + GAP, rowMouse, MOUSE_WIDTH, MOUSE_HEIGHT, "RMB", Mouse.isButtonDown(1));
    }

    private void drawKey(double left, double top, int width, int height, String label, boolean pressed) {
        RenderUtil.drawRect(left, top, left + width, top + height, pressed ? BG_PRESSED : BG_RELEASED);

        FontRenderer font = mc.fontRendererObj;
        float textX = (float) (left + (width - font.getStringWidth(label)) / 2.0D);
        float textY = (float) (top + (height - font.FONT_HEIGHT) / 2.0D + 1.0D);
        font.drawString(label, textX, textY, pressed ? TEXT_PRESSED : TEXT_RELEASED, false);
    }

    /** Nome real da tecla configurada; "?" quando não há nome (ex.: bind de mouse). */
    private String keyLabel(KeyBinding binding) {
        String name = Keyboard.getKeyName(binding.getKeyCode());
        if (name == null || name.isEmpty()) {
            return "?";
        }
        return name;
    }
}
