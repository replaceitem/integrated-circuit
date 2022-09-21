package net.replaceitem.integratedcircuit.network.packet;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.replaceitem.integratedcircuit.util.IntegratedCircuitIdentifier;
import net.replaceitem.integratedcircuit.circuit.ClientCircuit;

public class EditIntegratedCircuitS2CPacket {

    public static final Identifier ID = new IntegratedCircuitIdentifier("edit_integrated_circuit_s2c_packet");

    public final BlockPos pos;
    public final Text name;
    public final NbtCompound circuit;

    public EditIntegratedCircuitS2CPacket(BlockPos pos, Text name, NbtCompound circuit) {
        this.pos = pos;
        this.name = name;
        this.circuit = circuit;
    }

    public EditIntegratedCircuitS2CPacket(PacketByteBuf buf) {
        this(
                buf.readBlockPos(),
                buf.readText(),
                buf.readNbt()
        );
    }

    public PacketByteBuf getBuffer() {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(pos);
        buf.writeText(name);
        buf.writeNbt(circuit);
        return buf;
    }
    
    public ClientCircuit getClientCircuit() {
        return ClientCircuit.fromNbt(circuit);
    }

    public void send(ServerPlayerEntity serverPlayerEntity) {
        ServerPlayNetworking.send(serverPlayerEntity, ID, this.getBuffer());
    }
}
