package com.mineclient.modules.combat;

import com.mineclient.module.Category;
import com.mineclient.module.Module;
import com.mineclient.module.ModuleManager;
import com.mineclient.settings.NumberSetting;
import com.mineclient.util.RayTraceUtil;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Expande a hitbox efetiva das entidades na seleção de alvo. Quando o Reach
 * está ativo, a expansão é aplicada por ele; sozinho, aplica no alcance padrão.
 */
public class Hitboxes extends Module {

    private final NumberSetting expand = new NumberSetting("Expansao", 0.1, 0.0, 0.5, 0.05);

    public Hitboxes() {
        super("Hitboxes", "Expande a hitbox das entidades para facilitar a mira.", Category.COMBAT);
        addSettings(expand);
    }

    /** Expansão atual em blocos (0 quando o módulo está desligado). */
    public float getExpand() {
        return isEnabled() ? (float) expand.getValue() : 0.0F;
    }

    @SubscribeEvent
    public void onMouse(MouseEvent event) {
        if (!inGame()) {
            return;
        }
        Reach reach = ModuleManager.getModule(Reach.class);
        if (reach != null && reach.isEnabled()) {
            return; // Reach já recalcula a mira usando getExpand()
        }
        if (event.button == 0 && event.buttonstate) {
            RayTraceUtil.updateMouseOver(1.0F, 3.0D, expand.getValue());
        }
    }
}
