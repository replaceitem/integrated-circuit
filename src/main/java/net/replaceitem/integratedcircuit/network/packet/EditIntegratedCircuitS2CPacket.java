package net.replaceitem.integratedcircuit.network.packet;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.replaceitem.integratedcircuit.IntegratedCircuit;
import net.replaceitem.integratedcircuit.circuit.CircuitSerializer;
import net.replaceitem.integratedcircuit.circuit.ClientCircuit;
import net.replaceitem.integratedcircuit.circuit.context.ClientWorldClientCircuitContext;

public class EditIntegratedCircuitS2CPacket {

    public static final Identifier ID = IntegratedCircuit.id("edit_integrated_circuit_s2c_packet");

    public final BlockPos pos;
    public final Text name;
    public final NbtCompound circuitNbt;

    public EditIntegratedCircuitS2CPacket(BlockPos pos, Text name, NbtCompound circuitNbt) {
        this.pos = pos;
        this.name = name;
        this.circuitNbt = circuitNbt;
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
        buf.writeNbt(circuitNbt);
        return buf;
    }
    
    public ClientCircuit getClientCircuit(ClientWorld world, BlockPos pos) {
        return new CircuitSerializer(circuitNbt).readClientCircuit(new ClientWorldClientCircuitContext(world, pos));
    }

    public void send(ServerPlayerEntity serverPlayerEntity) {
        ServerPlayNetworking.send(serverPlayerEntity, ID, this.getBuffer());
    }
}
