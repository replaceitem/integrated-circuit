package net.replaceitem.integratedcircuit.network.packet;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.replaceitem.integratedcircuit.IntegratedCircuitIdentifier;
import net.replaceitem.integratedcircuit.circuit.ComponentPos;
import net.replaceitem.integratedcircuit.circuit.Components;
import net.replaceitem.integratedcircuit.circuit.state.ComponentState;

public class ComponentUpdateS2CPacket {
    public static final Identifier ID = new IntegratedCircuitIdentifier("component_update_s2c_packet");

    public final ComponentPos pos;
    public final ComponentState state;

    public ComponentUpdateS2CPacket(ComponentPos pos, ComponentState state) {
        this.pos = pos;
        this.state = state;
    }

    public ComponentUpdateS2CPacket(PacketByteBuf buf) {
        this(
                new ComponentPos(buf.readByte(), buf.readByte()),
                Components.createComponentState(buf.readShort())
        );
    }

    public PacketByteBuf getBuffer() {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeByte(pos.getX());
        buf.writeByte(pos.getY());
        buf.writeShort(state.encode());
        return buf;
    }

    public void send(ServerPlayerEntity serverPlayerEntity) {
        ServerPlayNetworking.send(serverPlayerEntity, ID, this.getBuffer());
    }
}
