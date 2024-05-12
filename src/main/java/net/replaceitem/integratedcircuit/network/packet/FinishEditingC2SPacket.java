package net.replaceitem.integratedcircuit.network.packet;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;
import net.replaceitem.integratedcircuit.IntegratedCircuit;

public record FinishEditingC2SPacket(BlockPos pos) implements CustomPayload {
    public static final CustomPayload.Id<FinishEditingC2SPacket> ID = new CustomPayload.Id<>(IntegratedCircuit.id("finish_editing_c2s_packet"));
    
    public static final PacketCodec<RegistryByteBuf, FinishEditingC2SPacket> PACKET_CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, FinishEditingC2SPacket::pos, FinishEditingC2SPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
