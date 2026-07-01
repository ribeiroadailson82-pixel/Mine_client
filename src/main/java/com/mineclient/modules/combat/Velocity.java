package com.mineclient.modules.combat;

import com.mineclient.event.PacketEvent;
import com.mineclient.module.Category;
import com.mineclient.module.Module;
import com.mineclient.settings.BooleanSetting;
import com.mineclient.settings.NumberSetting;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Reduz o knockback recebido interceptando os pacotes de velocidade.
 * Horizontal/Vertical definem o percentual de knockback MANTIDO
 * (0 = anula tudo, 100 = vanilla).
 */
public class Velocity extends Module {

    private final NumberSetting horizontal = new NumberSetting("Horizontal", 0, 0, 100, 5);
    private final NumberSetting vertical = new NumberSetting("Vertical", 0, 0, 100, 5);
    private final BooleanSetting explosions = new BooleanSetting("Explosoes", true);

    public Velocity() {
        super("Velocity", "Reduz ou anula o knockback recebido.", Category.COMBAT);
        addSettings(horizontal, vertical, explosions);
    }

    /** Roda na thread netty — mudanças no jogador só via mc.addScheduledTask. */
    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if (event.getDirection() != PacketEvent.Direction.INBOUND || mc.thePlayer == null) {
            return;
        }

        final double h = horizontal.getValue();
        final double v = vertical.getValue();

        if (event.getPacket() instanceof S12PacketEntityVelocity) {
            final S12PacketEntityVelocity packet = (S12PacketEntityVelocity) event.getPacket();
            if (packet.getEntityID() != mc.thePlayer.getEntityId()) {
                return;
            }
            event.setCanceled(true);
            if (h == 0.0D && v == 0.0D) {
                return;
            }
            // aplica só a fração configurada (valores do pacote vêm como int * 8000)
            mc.addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    if (mc.thePlayer == null) {
                        return;
                    }
                    mc.thePlayer.motionX = packet.getMotionX() / 8000.0D * (h / 100.0D);
                    mc.thePlayer.motionY = packet.getMotionY() / 8000.0D * (v / 100.0D);
                    mc.thePlayer.motionZ = packet.getMotionZ() / 8000.0D * (h / 100.0D);
                }
            });
            return;
        }

        if (event.getPacket() instanceof S27PacketExplosion) {
            if (!explosions.getValue() || (h >= 100.0D && v >= 100.0D)) {
                return;
            }
            // não cancela: o pacote também processa efeitos visuais/blocos.
            // O vanilla soma o valor cheio; subtrai o excedente para sobrar só a fração.
            final S27PacketExplosion packet = (S27PacketExplosion) event.getPacket();
            mc.addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    if (mc.thePlayer == null) {
                        return;
                    }
                    mc.thePlayer.motionX -= packet.func_149149_c() * (1.0D - h / 100.0D);
                    mc.thePlayer.motionY -= packet.func_149144_d() * (1.0D - v / 100.0D);
                    mc.thePlayer.motionZ -= packet.func_149147_e() * (1.0D - h / 100.0D);
                }
            });
        }
    }
}
