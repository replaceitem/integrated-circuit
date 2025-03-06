package net.replaceitem.integratedcircuit.network.packet;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.math.BlockPos;
import net.replaceitem.integratedcircuit.IntegratedCircuit;

public record RenameCircuitC2SPacket(
    Text newName,
    BlockPos pos
) implements CustomPayload {
    public static final CustomPayload.Id<RenameCircuitC2SPacket> ID = new CustomPayload.Id<>(IntegratedCircuit.id("rename_circuit_c2s_packet"));

    public static final PacketCodec<RegistryByteBuf, RenameCircuitC2SPacket> PACKET_CODEC = PacketCodec.tuple(
        TextCodecs.PACKET_CODEC, RenameCircuitC2SPacket::newName,
        BlockPos.PACKET_CODEC, RenameCircuitC2SPacket::pos,
        RenameCircuitC2SPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
