package net.replaceitem.integratedcircuit.network.packet;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.replaceitem.integratedcircuit.IntegratedCircuit;
import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.circuit.ComponentState;
import net.replaceitem.integratedcircuit.util.ComponentPos;

import java.util.Collection;

public class ComponentUpdateS2CPacket {
    public static final Identifier ID = IntegratedCircuit.id("component_update_s2c_packet");

    public final ComponentPos pos;
    public final ComponentState state;

    public ComponentUpdateS2CPacket(ComponentPos pos, ComponentState state) {
        this.pos = pos;
        this.state = state;
    }

    public ComponentUpdateS2CPacket(PacketByteBuf buf) {
        this(
                new ComponentPos(buf.readByte(), buf.readByte()),
                buf.readRegistryValue(Component.STATE_IDS)
        );
    }

    public PacketByteBuf getBuffer() {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeByte(pos.getX());
        buf.writeByte(pos.getY());
        buf.writeRegistryValue(Component.STATE_IDS, state);
        return buf;
    }

    public void send(ServerPlayerEntity serverPlayerEntity) {
        ServerPlayNetworking.send(serverPlayerEntity, ID, this.getBuffer());
    }

    public void send(Collection<ServerPlayerEntity> players) {
        PacketByteBuf buffer = this.getBuffer();
        for (ServerPlayerEntity player : players) {
            ServerPlayNetworking.send(player, ID, buffer);
        }
    }
}
