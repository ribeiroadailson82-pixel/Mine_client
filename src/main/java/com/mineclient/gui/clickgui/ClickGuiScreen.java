package com.mineclient.gui.clickgui;

import com.mineclient.config.ConfigManager;
import com.mineclient.module.Category;
import com.mineclient.util.RenderUtil;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Tela principal da ClickGUI. Um painel por categoria; as posições dos
 * painéis são estáticas para persistirem enquanto o jogo estiver aberto.
 */
public class ClickGuiScreen extends GuiScreen {

    private static final List<Panel> PANELS = new ArrayList<Panel>();

    static {
        int x = 20;
        for (Category category : Category.values()) {
            PANELS.add(new Panel(category, x, 20));
            x += Panel.WIDTH + 10;
        }
    }

    public ClickGuiScreen() {
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // fundo levemente escurecido (sem drawDefaultBackground, que escurece demais)
        RenderUtil.drawRect(0, 0, this.width, this.height, 0x50000000);

        String hovered = null;
        for (Panel panel : PANELS) {
            panel.drawScreen(mouseX, mouseY);
            if (hovered == null) {
                hovered = panel.getHoveredDescription(mouseX, mouseY);
            }
        }

        if (hovered != null) {
            drawTooltip(hovered, mouseX, mouseY);
        }
    }

    private void drawTooltip(String text, int mouseX, int mouseY) {
        int textWidth = RenderUtil.getStringWidth(text);
        int x = mouseX + 8;
        int y = mouseY - 4;
        // não deixa o tooltip sair da tela
        if (x + textWidth + 6 > this.width) {
            x = this.width - textWidth - 6;
        }
        if (y + RenderUtil.getFontHeight() + 4 > this.height) {
            y = this.height - RenderUtil.getFontHeight() - 4;
        }
        RenderUtil.drawBorderedRect(x, y, x + textWidth + 6, y + RenderUtil.getFontHeight() + 4,
                1, 0xE0101018, 0xFF303040);
        RenderUtil.drawString(text, x + 3, y + 2, 0xFFDDDDDD, false);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        for (Panel panel : PANELS) {
            if (panel.mouseClicked(mouseX, mouseY, mouseButton)) {
                return;
            }
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        for (Panel panel : PANELS) {
            panel.mouseReleased(state);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        for (Panel panel : PANELS) {
            if (panel.keyTyped(keyCode)) {
                return; // tecla consumida pelo modo de bind (ESC não fecha a GUI)
            }
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void onGuiClosed() {
        for (Panel panel : PANELS) {
            panel.onGuiClosed();
        }
        ConfigManager.save();
    }
}
