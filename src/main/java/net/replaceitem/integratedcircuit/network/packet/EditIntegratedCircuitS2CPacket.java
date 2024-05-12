package net.replaceitem.integratedcircuit.network.packet;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.math.BlockPos;
import net.replaceitem.integratedcircuit.IntegratedCircuit;
import net.replaceitem.integratedcircuit.circuit.CircuitSerializer;
import net.replaceitem.integratedcircuit.circuit.ClientCircuit;
import net.replaceitem.integratedcircuit.circuit.context.ClientWorldClientCircuitContext;

public record EditIntegratedCircuitS2CPacket(
        BlockPos pos,
        Text name,
        NbtCompound circuitNbt
) implements CustomPayload {
    public static final CustomPayload.Id<EditIntegratedCircuitS2CPacket> ID = new CustomPayload.Id<>(IntegratedCircuit.id("edit_integrated_circuit_s2c_packet"));

    public static final PacketCodec<RegistryByteBuf, EditIntegratedCircuitS2CPacket> PACKET_CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, EditIntegratedCircuitS2CPacket::pos,
            TextCodecs.PACKET_CODEC, EditIntegratedCircuitS2CPacket::name,
            PacketCodecs.NBT_COMPOUND, EditIntegratedCircuitS2CPacket::circuitNbt, // TODO use packet codec for circuit
            EditIntegratedCircuitS2CPacket::new
    );
    
    public ClientCircuit getClientCircuit(ClientWorld world, BlockPos pos) {
        return new CircuitSerializer(circuitNbt).readClientCircuit(new ClientWorldClientCircuitContext(world, pos));
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
