package com.mineclient.modules.combat;

import com.mineclient.module.Category;
import com.mineclient.module.Module;
import com.mineclient.module.ModuleManager;
import com.mineclient.settings.NumberSetting;
import com.mineclient.util.RayTraceUtil;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * Aumenta o alcance de ataque recalculando o objectMouseOver com raio maior.
 * Limite de 6 blocos: acima disso o servidor integrado (singleplayer/LAN)
 * descarta o ataque em NetHandlerPlayServer#processUseEntity.
 */
public class Reach extends Module {

    private final NumberSetting range = new NumberSetting("Alcance", 3.5, 3.0, 6.0, 0.1);

    public Reach() {
        super("Reach", "Aumenta o alcance de ataque (3 a 6 blocos).", Category.COMBAT);
        addSettings(range);
    }

    /** Garante o alvo estendido no momento exato do clique de ataque. */
    @SubscribeEvent
    public void onMouse(MouseEvent event) {
        if (!inGame()) {
            return;
        }
        if (event.button == 0 && event.buttonstate) {
            update(1.0F);
        }
    }

    /** Mantém o alvo estendido após o getMouseOver de cada frame (crosshair/combate contínuo). */
    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !inGame()) {
            return;
        }
        if (mc.currentScreen == null) {
            update(event.renderTickTime);
        }
    }

    private void update(float partialTicks) {
        Hitboxes hitboxes = ModuleManager.getModule(Hitboxes.class);
        double expand = hitboxes != null ? hitboxes.getExpand() : 0.0D;
        RayTraceUtil.updateMouseOver(partialTicks, range.getValue(), expand);
    }

    public double getRange() {
        return range.getValue();
    }
}
