package com.mineclient.event;

import net.minecraft.network.Packet;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Disparado para cada pacote que entra/sai da conexão. Cancelar o evento
 * descarta o pacote. ATENÇÃO: é postado na thread de rede (netty) — mudanças
 * no mundo/jogador devem ser agendadas com mc.addScheduledTask(...).
 */
@Cancelable
public class PacketEvent extends Event {

    public enum Direction {
        INBOUND,
        OUTBOUND
    }

    private final Packet<?> packet;
    private final Direction direction;

    public PacketEvent(Packet<?> packet, Direction direction) {
        this.packet = packet;
        this.direction = direction;
    }

    public Packet<?> getPacket() {
        return packet;
    }

    public Direction getDirection() {
        return direction;
    }
}
