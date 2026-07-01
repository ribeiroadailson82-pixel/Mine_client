package com.mineclient.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public final class RenderUtil {

    private RenderUtil() {
    }

    public static void drawRect(double left, double top, double right, double bottom, int color) {
        if (left > right) {
            double swap = left;
            left = right;
            right = swap;
        }
        if (top > bottom) {
            double swap = top;
            top = bottom;
            bottom = swap;
        }

        float alpha = (color >> 24 & 255) / 255.0F;
        float red = (color >> 16 & 255) / 255.0F;
        float green = (color >> 8 & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(red, green, blue, alpha);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(left, bottom, 0.0D).endVertex();
        worldRenderer.pos(right, bottom, 0.0D).endVertex();
        worldRenderer.pos(right, top, 0.0D).endVertex();
        worldRenderer.pos(left, top, 0.0D).endVertex();
        tessellator.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void drawBorderedRect(double left, double top, double right, double bottom,
                                        double borderWidth, int fillColor, int borderColor) {
        drawRect(left, top, right, bottom, fillColor);
        drawRect(left, top, right, top + borderWidth, borderColor);
        drawRect(left, bottom - borderWidth, right, bottom, borderColor);
        drawRect(left, top, left + borderWidth, bottom, borderColor);
        drawRect(right - borderWidth, top, right, bottom, borderColor);
    }

    public static void drawString(String text, float x, float y, int color, boolean shadow) {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
        fontRenderer.drawString(text, x, y, color, shadow);
    }

    public static int getStringWidth(String text) {
        return Minecraft.getMinecraft().fontRendererObj.getStringWidth(text);
    }

    public static int getFontHeight() {
        return Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT;
    }

    public static boolean isHovered(int mouseX, int mouseY, double x, double y, double width, double height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}
