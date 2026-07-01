package com.mineclient.gui.clickgui;

import com.mineclient.module.Category;
import com.mineclient.module.Module;
import com.mineclient.module.ModuleManager;
import com.mineclient.util.RenderUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Painel de uma categoria: cabeçalho arrastável (clique esquerdo) e
 * recolhível (clique direito), com um botão por módulo.
 */
public class Panel {

    public static final int WIDTH = 110;
    public static final int HEADER_HEIGHT = 15;

    private final Category category;
    private final List<ModuleButton> buttons = new ArrayList<ModuleButton>();

    private int x;
    private int y;
    private boolean extended = true;

    private boolean dragging;
    private int dragOffsetX;
    private int dragOffsetY;

    public Panel(Category category, int x, int y) {
        this.category = category;
        this.x = x;
        this.y = y;
        for (Module module : ModuleManager.getByCategory(category)) {
            buttons.add(new ModuleButton(module));
        }
    }

    public void drawScreen(int mouseX, int mouseY) {
        if (dragging) {
            x = mouseX - dragOffsetX;
            y = mouseY - dragOffsetY;
        }

        // cabeçalho
        RenderUtil.drawRect(x, y, x + WIDTH, y + HEADER_HEIGHT, 0xFF181820);
        RenderUtil.drawRect(x, y + HEADER_HEIGHT - 1, x + WIDTH, y + HEADER_HEIGHT, 0xFF4080FF);
        String title = category.getDisplayName();
        RenderUtil.drawString(title,
                x + (WIDTH - RenderUtil.getStringWidth(title)) / 2.0F,
                y + (HEADER_HEIGHT - RenderUtil.getFontHeight()) / 2.0F + 1,
                0xFFFFFFFF, true);

        if (!extended) {
            return;
        }

        int offsetY = y + HEADER_HEIGHT;
        for (ModuleButton button : buttons) {
            offsetY += button.drawScreen(x, offsetY, mouseX, mouseY);
        }
    }

    /** Descrição do módulo sob o mouse (para tooltip), ou null. */
    public String getHoveredDescription(int mouseX, int mouseY) {
        if (!extended || dragging) {
            return null;
        }
        int offsetY = y + HEADER_HEIGHT;
        for (ModuleButton button : buttons) {
            if (RenderUtil.isHovered(mouseX, mouseY, x, offsetY, WIDTH, ModuleButton.HEIGHT)) {
                return button.getModule().getDescription();
            }
            offsetY += button.getHeight();
        }
        return null;
    }

    /** Retorna true se o clique foi consumido pelo painel. */
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (RenderUtil.isHovered(mouseX, mouseY, x, y, WIDTH, HEADER_HEIGHT)) {
            if (mouseButton == 0) {
                dragging = true;
                dragOffsetX = mouseX - x;
                dragOffsetY = mouseY - y;
            } else if (mouseButton == 1) {
                extended = !extended;
            }
            return true;
        }

        if (!extended) {
            return false;
        }

        int offsetY = y + HEADER_HEIGHT;
        for (ModuleButton button : buttons) {
            if (button.mouseClicked(x, offsetY, mouseX, mouseY, mouseButton)) {
                return true;
            }
            offsetY += button.getHeight();
        }
        return false;
    }

    public void mouseReleased(int state) {
        if (state == 0) {
            dragging = false;
        }
        for (ModuleButton button : buttons) {
            button.mouseReleased(state);
        }
    }

    /** Retorna true se a tecla foi consumida (modo de bind). */
    public boolean keyTyped(int keyCode) {
        for (ModuleButton button : buttons) {
            if (button.keyTyped(keyCode)) {
                return true;
            }
        }
        return false;
    }

    public void onGuiClosed() {
        dragging = false;
        for (ModuleButton button : buttons) {
            button.onGuiClosed();
        }
    }
}
