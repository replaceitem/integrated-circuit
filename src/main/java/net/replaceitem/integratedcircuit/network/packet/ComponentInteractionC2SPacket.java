package net.replaceitem.integratedcircuit.network.packet;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;
import net.replaceitem.integratedcircuit.IntegratedCircuit;
import net.replaceitem.integratedcircuit.util.ComponentPos;

public record ComponentInteractionC2SPacket(
        ComponentPos pos,
        BlockPos blockPos
) implements CustomPayload {
    public static final CustomPayload.Id<ComponentInteractionC2SPacket> ID = new Id<>(IntegratedCircuit.id("component_interaction_c2s_packet"));

    public static final PacketCodec<RegistryByteBuf, ComponentInteractionC2SPacket> PACKET_CODEC = PacketCodec.tuple(
            ComponentPos.PACKET_CODEC, ComponentInteractionC2SPacket::pos,
            BlockPos.PACKET_CODEC, ComponentInteractionC2SPacket::blockPos,
            ComponentInteractionC2SPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
