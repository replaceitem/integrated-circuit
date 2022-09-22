package net.replaceitem.integratedcircuit.network.packet;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.replaceitem.integratedcircuit.util.IntegratedCircuitIdentifier;
import net.replaceitem.integratedcircuit.util.ComponentPos;

public class ComponentInteractionC2SPacket {
    public static final Identifier ID = new IntegratedCircuitIdentifier("component_interaction_c2s_packet");

    public final ComponentPos pos;
    public final BlockPos blockPos;

    public ComponentInteractionC2SPacket(ComponentPos pos, BlockPos blockPos) {
        this.pos = pos;
        this.blockPos = blockPos;
    }

    public ComponentInteractionC2SPacket(PacketByteBuf buf) {
        this(
                new ComponentPos(buf.readByte(), buf.readByte()),
                buf.readBlockPos()
        );
    }

    public PacketByteBuf getBuffer() {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeByte(pos.getX());
        buf.writeByte(pos.getY());
        buf.writeBlockPos(blockPos);
        return buf;
    }

    public void send() {
        ClientPlayNetworking.send(ID, this.getBuffer());
    }
}
