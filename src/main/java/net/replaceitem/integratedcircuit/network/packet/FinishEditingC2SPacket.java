package net.replaceitem.integratedcircuit.network.packet;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.replaceitem.integratedcircuit.IntegratedCircuitIdentifier;

public class FinishEditingC2SPacket {

    public static final Identifier ID = new IntegratedCircuitIdentifier("finish_editing_c2s_packet");

    public final BlockPos pos;

    public FinishEditingC2SPacket(BlockPos pos) {
        this.pos = pos;
    }

    public FinishEditingC2SPacket(PacketByteBuf buf) {
        this(
                buf.readBlockPos()
        );
    }

    public PacketByteBuf getBuffer() {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(pos);
        return buf;
    }

    public void send() {
        ClientPlayNetworking.send(ID, this.getBuffer());
    }
}
