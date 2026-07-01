package com.mineclient.modules.combat;

import com.mineclient.module.Category;
import com.mineclient.module.Module;
import com.mineclient.settings.BooleanSetting;
import com.mineclient.settings.NumberSetting;
import com.mineclient.util.TimerUtil;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Mouse;

import java.util.Random;

/**
 * Dispara cliques de ataque enquanto o botão esquerdo estiver pressionado.
 * O delay entre cliques é sorteado a cada clique para variar o CPS.
 */
public class AutoClicker extends Module {

    private final NumberSetting cpsMin = new NumberSetting("CPS Min", 8, 1, 20, 1);
    private final NumberSetting cpsMax = new NumberSetting("CPS Max", 12, 1, 20, 1);
    private final BooleanSetting onBlocks = new BooleanSetting("Em blocos", false);

    private final TimerUtil timer = new TimerUtil();
    private final Random random = new Random();
    private long nextDelay;

    public AutoClicker() {
        super("AutoClicker", "Clica automaticamente segurando o botão esquerdo.", Category.COMBAT);
        addSettings(cpsMin, cpsMax, onBlocks);
    }

    @Override
    protected void onEnable() {
        timer.reset();
        nextDelay = rollDelay();
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !inGame()) {
            return;
        }
        if (mc.currentScreen != null || !Mouse.isButtonDown(0)) {
            return;
        }
        // evita atrapalhar mineração: não clica olhando para bloco
        if (!onBlocks.getValue() && mc.objectMouseOver != null
                && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            return;
        }
        if (timer.hasElapsed(nextDelay)) {
            KeyBinding.onTick(mc.gameSettings.keyBindAttack.getKeyCode());
            timer.reset();
            nextDelay = rollDelay();
        }
    }

    /** Sorteia o próximo delay em ms entre 1000/cpsMax e 1000/cpsMin. */
    private long rollDelay() {
        int low = cpsMin.getValueInt();
        int high = cpsMax.getValueInt();
        // garante coerência mesmo com sliders invertidos
        int cpsLow = Math.min(low, high);
        int cpsHigh = Math.max(low, high);
        int delayMin = (int) (1000.0D / cpsHigh);
        int delayMax = (int) (1000.0D / cpsLow);
        return delayMin + random.nextInt(delayMax - delayMin + 1);
    }
}
