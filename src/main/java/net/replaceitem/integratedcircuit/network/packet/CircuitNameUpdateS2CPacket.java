package net.replaceitem.integratedcircuit.network.packet;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.math.BlockPos;
import net.replaceitem.integratedcircuit.IntegratedCircuit;

public record CircuitNameUpdateS2CPacket(
    Text newName,
    BlockPos pos
) implements CustomPayload {
    public static final CustomPayload.Id<CircuitNameUpdateS2CPacket> ID = new CustomPayload.Id<>(IntegratedCircuit.id("circuit_name_update_s2c_packet"));

    public static final PacketCodec<RegistryByteBuf, CircuitNameUpdateS2CPacket> PACKET_CODEC = PacketCodec.tuple(
        TextCodecs.PACKET_CODEC, CircuitNameUpdateS2CPacket::newName,
        BlockPos.PACKET_CODEC, CircuitNameUpdateS2CPacket::pos,
        CircuitNameUpdateS2CPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
