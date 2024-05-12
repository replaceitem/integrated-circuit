package net.replaceitem.integratedcircuit.network.packet;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.replaceitem.integratedcircuit.IntegratedCircuit;
import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.circuit.ComponentState;
import net.replaceitem.integratedcircuit.util.ComponentPos;

public record ComponentUpdateS2CPacket(
        ComponentPos pos,
        ComponentState state
) implements CustomPayload {
    public static final CustomPayload.Id<ComponentUpdateS2CPacket> ID = new CustomPayload.Id<>(IntegratedCircuit.id("component_update_s2c_packet"));

    public static final PacketCodec<RegistryByteBuf, ComponentUpdateS2CPacket> PACKET_CODEC = PacketCodec.tuple(
            ComponentPos.PACKET_CODEC, ComponentUpdateS2CPacket::pos,
            PacketCodecs.entryOf(Component.STATE_IDS), ComponentUpdateS2CPacket::state,
            ComponentUpdateS2CPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
