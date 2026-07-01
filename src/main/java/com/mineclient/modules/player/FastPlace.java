package com.mineclient.modules.player;

import com.mineclient.MineClient;
import com.mineclient.module.Category;
import com.mineclient.module.Module;
import com.mineclient.settings.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;

/**
 * Reduz o delay de 4 ticks entre cliques direitos (colocar blocos etc.)
 * ajustando o campo privado Minecraft.rightClickDelayTimer via reflection.
 */
public class FastPlace extends Module {

    private final NumberSetting delay = new NumberSetting("Delay", 0, 0, 4, 1);

    // cache do campo privado (SRG: field_71467_ac); findField já o torna acessível
    private static Field rightClickDelayTimer;

    public FastPlace() {
        super("FastPlace", "Reduz o delay entre cliques direitos.", Category.PLAYER);
        addSettings(delay);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !inGame()) {
            return;
        }
        try {
            if (rightClickDelayTimer == null) {
                rightClickDelayTimer = ReflectionHelper.findField(Minecraft.class,
                        "rightClickDelayTimer", "field_71467_ac");
            }
            int target = delay.getValueInt();
            if (rightClickDelayTimer.getInt(mc) > target) {
                rightClickDelayTimer.setInt(mc, target);
            }
        } catch (Throwable t) {
            MineClient.LOGGER.error("FastPlace: falha ao acessar rightClickDelayTimer, desligando.", t);
            setEnabled(false);
        }
    }
}
