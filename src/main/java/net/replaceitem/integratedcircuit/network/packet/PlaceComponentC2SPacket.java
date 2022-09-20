package net.replaceitem.integratedcircuit.network.packet;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.replaceitem.integratedcircuit.IntegratedCircuitIdentifier;
import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.circuit.ComponentPos;
import net.replaceitem.integratedcircuit.circuit.Components;
import net.replaceitem.integratedcircuit.util.Direction;

public class PlaceComponentC2SPacket {
    public static final Identifier ID = new IntegratedCircuitIdentifier("place_component_c2s_packet");

    public final ComponentPos pos;
    public final BlockPos blockPos;
    public final Component component;
    public final Direction rotation;

    public PlaceComponentC2SPacket(ComponentPos pos, BlockPos blockPos, Component component, Direction rotation) {
        this.pos = pos;
        this.blockPos = blockPos;
        this.component = component;
        this.rotation = rotation;
    }

    public PlaceComponentC2SPacket(PacketByteBuf buf) {
        this(
                new ComponentPos(buf.readByte(), buf.readByte()),
                buf.readBlockPos(),
                Components.getComponentById(buf.readByte()),
                Direction.VALUES[buf.readByte()]
        );
    }

    public PacketByteBuf getBuffer() {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeByte(pos.getX());
        buf.writeByte(pos.getY());
        buf.writeBlockPos(blockPos);
        buf.writeByte(component.getId());
        buf.writeByte(rotation.toInt());
        return buf;
    }

    public void send() {
        ClientPlayNetworking.send(ID, this.getBuffer());
    }
}
