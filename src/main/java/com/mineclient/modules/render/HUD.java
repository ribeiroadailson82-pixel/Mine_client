package com.mineclient.modules.render;

import com.mineclient.MineClient;
import com.mineclient.module.Category;
import com.mineclient.module.Module;
import com.mineclient.module.ModuleManager;
import com.mineclient.settings.BooleanSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Overlay com watermark, FPS, CPS, coordenadas e lista de módulos ativos.
 * Não desenha com o F3 aberto para não competir com o debug vanilla.
 */
public class HUD extends Module {

    private static final int COLOR_WATERMARK = 0xFF55FFFF;
    private static final int COLOR_TEXT = 0xFFFFFFFF;

    private final BooleanSetting fps = new BooleanSetting("FPS", true);
    private final BooleanSetting cps = new BooleanSetting("CPS", true);
    private final BooleanSetting coords = new BooleanSetting("Coordenadas", true);
    private final BooleanSetting moduleList = new BooleanSetting("Lista de modulos", true);

    // Acessado apenas na thread do client (MouseEvent e render rodam nela).
    private final ArrayDeque<Long> clicks = new ArrayDeque<Long>();

    public HUD() {
        super("HUD", "Mostra FPS, CPS, coordenadas e módulos ativos.", Category.RENDER);
        addSettings(fps, cps, coords, moduleList);
    }

    @SubscribeEvent
    public void onMouse(MouseEvent event) {
        if (event.button == 0 && event.buttonstate) {
            clicks.addLast(Long.valueOf(System.currentTimeMillis()));
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) {
            return;
        }
        if (!inGame() || mc.gameSettings.showDebugInfo) {
            return;
        }

        FontRenderer font = mc.fontRendererObj;
        int lineHeight = font.FONT_HEIGHT + 2;
        float x = 2.0F;
        float y = 2.0F;

        font.drawStringWithShadow(MineClient.NAME + " " + MineClient.VERSION, x, y, COLOR_WATERMARK);
        y += lineHeight;

        if (fps.getValue()) {
            font.drawStringWithShadow("FPS: " + Minecraft.getDebugFPS(), x, y, COLOR_TEXT);
            y += lineHeight;
        }
        if (cps.getValue()) {
            font.drawStringWithShadow("CPS: " + countClicks(), x, y, COLOR_TEXT);
            y += lineHeight;
        }
        if (coords.getValue()) {
            int px = MathHelper.floor_double(mc.thePlayer.posX);
            int py = MathHelper.floor_double(mc.thePlayer.posY);
            int pz = MathHelper.floor_double(mc.thePlayer.posZ);
            font.drawStringWithShadow("XYZ: " + px + " / " + py + " / " + pz, x, y, COLOR_TEXT);
            y += lineHeight;
        }

        if (moduleList.getValue()) {
            drawModuleList(font, lineHeight);
        }
    }

    /** Remove cliques com mais de 1s e retorna quantos restaram. */
    private int countClicks() {
        long cutoff = System.currentTimeMillis() - 1000L;
        while (!clicks.isEmpty() && clicks.peekFirst().longValue() < cutoff) {
            clicks.pollFirst();
        }
        return clicks.size();
    }

    private void drawModuleList(FontRenderer font, int lineHeight) {
        List<Module> enabled = new ArrayList<Module>();
        for (Module module : ModuleManager.getModules()) {
            if (module.isEnabled()) {
                enabled.add(module);
            }
        }

        final FontRenderer sortFont = font;
        Collections.sort(enabled, new Comparator<Module>() {
            @Override
            public int compare(Module a, Module b) {
                return sortFont.getStringWidth(b.getName()) - sortFont.getStringWidth(a.getName());
            }
        });

        int screenWidth = new ScaledResolution(mc).getScaledWidth();
        float y = 2.0F;
        for (Module module : enabled) {
            String name = module.getName();
            float x = screenWidth - font.getStringWidth(name) - 2.0F;
            font.drawStringWithShadow(name, x, y, categoryColor(module.getCategory()));
            y += lineHeight;
        }
    }

    private int categoryColor(Category category) {
        switch (category) {
            case COMBAT:
                return 0xFFFF5555;
            case PLAYER:
                return 0xFF55FF55;
            case RENDER:
            default:
                return 0xFF5555FF;
        }
    }
}
