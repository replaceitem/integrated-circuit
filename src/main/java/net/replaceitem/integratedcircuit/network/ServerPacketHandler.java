package net.replaceitem.integratedcircuit.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.replaceitem.integratedcircuit.IntegratedCircuit;
import net.replaceitem.integratedcircuit.IntegratedCircuitBlockEntity;
import net.replaceitem.integratedcircuit.network.packet.*;

import java.util.Objects;

public class ServerPacketHandler {
    public static void receiveComponentInteraction(ComponentInteractionC2SPacket packet, ServerPlayNetworking.Context context) {
        ServerPlayerEntity player = context.player();
        ServerWorld world = player.getWorld();
        if(
                world.getBlockState(packet.blockPos()).isIn(IntegratedCircuit.Tags.INTEGRATED_CIRCUITS_BLOCK_TAG) &&
                world.getBlockEntity(packet.blockPos()) instanceof IntegratedCircuitBlockEntity integratedCircuitBlockEntity &&
                integratedCircuitBlockEntity.getCircuit() != null
        ) {
            if(!integratedCircuitBlockEntity.isEditing(player)) return;
            integratedCircuitBlockEntity.getCircuit().useComponent(packet.pos(), player);
        }
    }

    public static void receiveFinishEditingPacket(FinishEditingC2SPacket packet, ServerPlayNetworking.Context context) {
        ServerPlayerEntity player = context.player();
        player.updateLastActionTime();
        ServerWorld serverWorld = player.getWorld();
        BlockEntity blockEntity = serverWorld.getBlockEntity(packet.pos());

        if (blockEntity instanceof IntegratedCircuitBlockEntity integratedCircuitBlockEntity) {
            integratedCircuitBlockEntity.removeEditor(player);
        }
    }

    public static void receiveRenameCircuitPacket(RenameCircuitC2SPacket packet, ServerPlayNetworking.Context context) {
        BlockPos pos = packet.pos();
        ServerPlayerEntity renamingPlayer = context.player();
        ServerWorld serverWorld = renamingPlayer.getWorld();
        BlockEntity blockEntity = serverWorld.getBlockEntity(pos);

        renamingPlayer.updateLastActionTime();

        if (blockEntity instanceof IntegratedCircuitBlockEntity integratedCircuitBlockEntity) {

            Text currentName = integratedCircuitBlockEntity.getCustomName();
            Text newName = packet.newName();

            if (!Objects.equals(currentName, newName)) {
                integratedCircuitBlockEntity.setCustomName(newName);

                for (ServerPlayerEntity player : integratedCircuitBlockEntity.getEditingPlayers()) {
                    ServerPlayNetworking.send(
                        player,
                        new CircuitNameUpdateS2CPacket(newName, pos)
                    );
                }
            }
        }
    }

    public static void receivePlaceComponentPacket(PlaceComponentC2SPacket packet, ServerPlayNetworking.Context context) {
        ServerPlayerEntity player = context.player();
        ServerWorld world = player.getWorld();
        if(
                world.getBlockState(packet.blockPos()).isIn(IntegratedCircuit.Tags.INTEGRATED_CIRCUITS_BLOCK_TAG) && 
                world.getBlockEntity(packet.blockPos()) instanceof IntegratedCircuitBlockEntity integratedCircuitBlockEntity &&
                integratedCircuitBlockEntity.getCircuit() != null
        ) {
            if(!integratedCircuitBlockEntity.isEditing(player)) return;
            integratedCircuitBlockEntity.getCircuit().placeComponentState(packet.pos(), packet.component(), packet.rotation());
        }
    }
}
