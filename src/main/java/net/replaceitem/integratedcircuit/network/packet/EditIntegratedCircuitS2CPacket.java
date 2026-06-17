package net.replaceitem.integratedcircuit.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.replaceitem.integratedcircuit.IntegratedCircuit;

public record EditIntegratedCircuitS2CPacket(
        BlockPos pos,
        Component customName,
        CompoundTag circuitNbt
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<EditIntegratedCircuitS2CPacket> ID = new CustomPacketPayload.Type<>(IntegratedCircuit.id("edit_integrated_circuit_s2c_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, EditIntegratedCircuitS2CPacket> PACKET_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, EditIntegratedCircuitS2CPacket::pos,
        ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC, EditIntegratedCircuitS2CPacket::customName,
        ByteBufCodecs.COMPOUND_TAG, EditIntegratedCircuitS2CPacket::circuitNbt, // TODO use packet codec for circuit
        EditIntegratedCircuitS2CPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
