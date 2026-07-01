package com.mineclient.gui.clickgui;

import com.mineclient.module.Module;
import com.mineclient.settings.BooleanSetting;
import com.mineclient.settings.ModeSetting;
import com.mineclient.settings.NumberSetting;
import com.mineclient.settings.Setting;
import com.mineclient.util.RenderUtil;
import org.lwjgl.input.Keyboard;

import java.util.Locale;

/**
 * Botão de um módulo dentro do painel: clique esquerdo alterna o módulo,
 * clique direito expande as settings (caixinhas, sliders, modos e bind).
 */
public class ModuleButton {

    public static final int HEIGHT = 14;
    private static final int SETTING_HEIGHT = 12;
    private static final int SLIDER_HEIGHT = 16;
    private static final int BIND_HEIGHT = 12;

    private static final int COLOR_MODULE = 0xFF202028;
    private static final int COLOR_MODULE_HOVER = 0xFF2A2A34;
    private static final int COLOR_MODULE_ON = 0xFF3564C8;
    private static final int COLOR_SETTING_BG = 0xFF16161C;
    private static final int COLOR_ACCENT = 0xFF4080FF;
    private static final int COLOR_TEXT = 0xFFE0E0E0;
    private static final int COLOR_TEXT_DIM = 0xFFA0A0A0;

    private final Module module;

    private boolean expanded;
    private boolean binding;
    private NumberSetting draggingSlider;

    public ModuleButton(Module module) {
        this.module = module;
    }

    public Module getModule() {
        return module;
    }

    /** Altura total do botão (linha do módulo + settings, se expandido). */
    public int getHeight() {
        int height = HEIGHT;
        if (expanded) {
            for (Setting setting : module.getSettings()) {
                height += settingHeight(setting);
            }
            height += BIND_HEIGHT;
        }
        return height;
    }

    private int settingHeight(Setting setting) {
        return setting instanceof NumberSetting ? SLIDER_HEIGHT : SETTING_HEIGHT;
    }

    /** Desenha e retorna a altura ocupada. Arrasto do slider é tratado aqui. */
    public int drawScreen(int panelX, int drawY, int mouseX, int mouseY) {
        boolean hovered = RenderUtil.isHovered(mouseX, mouseY, panelX, drawY, Panel.WIDTH, HEIGHT);
        int background = module.isEnabled() ? COLOR_MODULE_ON
                : hovered ? COLOR_MODULE_HOVER : COLOR_MODULE;
        RenderUtil.drawRect(panelX, drawY, panelX + Panel.WIDTH, drawY + HEIGHT, background);
        RenderUtil.drawString(module.getName(), panelX + 5,
                drawY + (HEIGHT - RenderUtil.getFontHeight()) / 2.0F + 1, COLOR_TEXT, true);
        // marcador de expansão (todo módulo tem ao menos a linha de bind)
        String marker = expanded ? "-" : "+";
        RenderUtil.drawString(marker,
                panelX + Panel.WIDTH - 5 - RenderUtil.getStringWidth(marker),
                drawY + (HEIGHT - RenderUtil.getFontHeight()) / 2.0F + 1, COLOR_TEXT_DIM, false);

        if (!expanded) {
            return HEIGHT;
        }

        int offsetY = drawY + HEIGHT;
        for (Setting setting : module.getSettings()) {
            int rowHeight = settingHeight(setting);
            RenderUtil.drawRect(panelX, offsetY, panelX + Panel.WIDTH, offsetY + rowHeight, COLOR_SETTING_BG);

            if (setting instanceof BooleanSetting) {
                drawBoolean((BooleanSetting) setting, panelX, offsetY);
            } else if (setting instanceof NumberSetting) {
                drawNumber((NumberSetting) setting, panelX, offsetY, mouseX);
            } else if (setting instanceof ModeSetting) {
                drawMode((ModeSetting) setting, panelX, offsetY);
            }
            offsetY += rowHeight;
        }

        // linha de bind
        RenderUtil.drawRect(panelX, offsetY, panelX + Panel.WIDTH, offsetY + BIND_HEIGHT, COLOR_SETTING_BG);
        RenderUtil.drawString("Bind: " + bindText(), panelX + 8, offsetY + 2,
                binding ? COLOR_ACCENT : COLOR_TEXT_DIM, false);
        offsetY += BIND_HEIGHT;

        // linha lateral marcando a área expandida
        RenderUtil.drawRect(panelX, drawY + HEIGHT, panelX + 1, offsetY, COLOR_ACCENT);

        return offsetY - drawY;
    }

    private String bindText() {
        if (binding) {
            return "...";
        }
        int keyCode = module.getKeyCode();
        if (keyCode <= 0) {
            return "Nenhum";
        }
        String name = Keyboard.getKeyName(keyCode);
        return name != null ? name : String.valueOf(keyCode);
    }

    private void drawBoolean(BooleanSetting setting, int panelX, int rowY) {
        int boxX = panelX + 6;
        int boxY = rowY + 2;
        RenderUtil.drawBorderedRect(boxX, boxY, boxX + 8, boxY + 8, 1,
                setting.getValue() ? COLOR_ACCENT : 0xFF101014, 0xFF505060);
        RenderUtil.drawString(setting.getName(), boxX + 12, rowY + 2, COLOR_TEXT, false);
    }

    private void drawNumber(NumberSetting setting, int panelX, int rowY, int mouseX) {
        int trackX = panelX + 6;
        int trackWidth = Panel.WIDTH - 12;

        // arrasto ativo: atualiza o valor a partir do mouse (não usa mouseClickMove)
        if (draggingSlider == setting) {
            double fraction = (mouseX - trackX) / (double) trackWidth;
            fraction = Math.max(0.0D, Math.min(1.0D, fraction));
            setting.setValue(setting.getMin() + fraction * (setting.getMax() - setting.getMin()));
        }

        RenderUtil.drawString(setting.getName() + ": " + formatValue(setting),
                trackX, rowY + 2, COLOR_TEXT, false);

        int trackY = rowY + SLIDER_HEIGHT - 5;
        RenderUtil.drawRect(trackX, trackY, trackX + trackWidth, trackY + 3, 0xFF101014);
        double filled = (setting.getValue() - setting.getMin()) / (setting.getMax() - setting.getMin());
        RenderUtil.drawRect(trackX, trackY, trackX + trackWidth * filled, trackY + 3, COLOR_ACCENT);
    }

    private String formatValue(NumberSetting setting) {
        if (setting.getStep() >= 1.0D) {
            return String.valueOf(setting.getValueInt());
        }
        if (setting.getStep() >= 0.1D) {
            return String.format(Locale.US, "%.1f", setting.getValue());
        }
        return String.format(Locale.US, "%.2f", setting.getValue());
    }

    private void drawMode(ModeSetting setting, int panelX, int rowY) {
        RenderUtil.drawString(setting.getName() + ": ", panelX + 6, rowY + 2, COLOR_TEXT, false);
        RenderUtil.drawString(setting.getMode(),
                panelX + 6 + RenderUtil.getStringWidth(setting.getName() + ": "),
                rowY + 2, COLOR_ACCENT, false);
    }

    /** Retorna true se o clique foi consumido por este botão. */
    public boolean mouseClicked(int panelX, int drawY, int mouseX, int mouseY, int mouseButton) {
        if (RenderUtil.isHovered(mouseX, mouseY, panelX, drawY, Panel.WIDTH, HEIGHT)) {
            if (mouseButton == 0) {
                module.toggle();
            } else if (mouseButton == 1) {
                expanded = !expanded;
                if (!expanded) {
                    binding = false;
                }
            }
            return true;
        }

        if (!expanded) {
            return false;
        }

        int offsetY = drawY + HEIGHT;
        for (Setting setting : module.getSettings()) {
            int rowHeight = settingHeight(setting);
            if (RenderUtil.isHovered(mouseX, mouseY, panelX, offsetY, Panel.WIDTH, rowHeight)) {
                if (mouseButton == 0) {
                    if (setting instanceof BooleanSetting) {
                        ((BooleanSetting) setting).toggle();
                    } else if (setting instanceof NumberSetting) {
                        draggingSlider = (NumberSetting) setting; // valor atualiza no drawScreen
                    } else if (setting instanceof ModeSetting) {
                        ((ModeSetting) setting).cycle();
                    }
                }
                return true;
            }
            offsetY += rowHeight;
        }

        if (RenderUtil.isHovered(mouseX, mouseY, panelX, offsetY, Panel.WIDTH, BIND_HEIGHT)) {
            if (mouseButton == 0) {
                binding = true;
            }
            return true;
        }
        return false;
    }

    public void mouseReleased(int state) {
        if (state == 0) {
            draggingSlider = null;
        }
    }

    /** Consome a tecla quando está escutando o bind (ESC/DELETE removem). */
    public boolean keyTyped(int keyCode) {
        if (!binding) {
            return false;
        }
        if (keyCode == Keyboard.KEY_ESCAPE || keyCode == Keyboard.KEY_DELETE) {
            module.setKeyCode(0);
        } else if (keyCode > 0) {
            module.setKeyCode(keyCode);
        }
        binding = false;
        return true;
    }

    public void onGuiClosed() {
        binding = false;
        draggingSlider = null;
    }
}
