package net.replaceitem.integratedcircuit.network.packet;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;
import net.replaceitem.integratedcircuit.IntegratedCircuit;
import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;

public record PlaceComponentC2SPacket(
        ComponentPos pos,
        BlockPos blockPos,
        Component component,
        FlatDirection rotation
) implements CustomPayload {
    public static final CustomPayload.Id<PlaceComponentC2SPacket> ID = new CustomPayload.Id<>(IntegratedCircuit.id("place_component_c2s_packet"));
    
    public static final PacketCodec<RegistryByteBuf, PlaceComponentC2SPacket> PACKET_CODEC = PacketCodec.tuple(
            ComponentPos.PACKET_CODEC, PlaceComponentC2SPacket::pos,
            BlockPos.PACKET_CODEC, PlaceComponentC2SPacket::blockPos,
            Component.PACKET_CODEC, PlaceComponentC2SPacket::component,
            FlatDirection.PACKET_CODEC, PlaceComponentC2SPacket::rotation,
            PlaceComponentC2SPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
