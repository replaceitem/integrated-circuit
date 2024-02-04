package net.replaceitem.integratedcircuit;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Nameable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.replaceitem.integratedcircuit.circuit.context.BlockEntityServerCircuitContext;
import net.replaceitem.integratedcircuit.circuit.ServerCircuit;
import net.replaceitem.integratedcircuit.util.FlatDirection;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.WeakHashMap;

public class IntegratedCircuitBlockEntity extends BlockEntity implements Nameable {
    protected WeakHashMap<ServerPlayerEntity, Object> editors;
    protected Text customName;

    @Nullable
    private ServerCircuit circuit;

    protected byte[] outputStrengths = new byte[] {0,0,0,0};

    public IntegratedCircuitBlockEntity(BlockPos pos, BlockState state) {
        super(IntegratedCircuit.INTEGRATED_CIRCUIT_BLOCK_ENTITY, pos, state);
        this.circuit = null; // save some memory by only initializing this once accessed to not waste this space on the client block entities
        this.editors = new WeakHashMap<>(4);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        if(nbt.contains("CustomName", NbtElement.STRING_TYPE)) {
            this.customName = Text.Serialization.fromJson(nbt.getString("CustomName"));
        }
        if(nbt.contains("outputStrengths")) {
            this.outputStrengths = nbt.getByteArray("outputStrengths");
        }
        this.getCircuit().readNbt(nbt);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        if(this.hasCustomName()) {
            nbt.putString("CustomName", Text.Serialization.toJsonString(this.customName));
        }
        nbt.putByteArray("outputStrengths", this.outputStrengths.clone());

        this.getCircuit().writeNbt(nbt);
    }

    public void setCustomName(Text name) {
        this.customName = name;
        this.markDirty();
    }

    @Override
    public Text getCustomName() {
        return this.customName;
    }

    @Override
    public Text getName() {
        if(hasCustomName()) return this.customName;
        return Text.translatable("block.integrated_circuit.integrated_circuit");
    }

    private static final Object DUMMY = new Object();

    public Set<ServerPlayerEntity> getEditingPlayers() {
        return editors.keySet();
    }

    public boolean isEditing(ServerPlayerEntity player) {
        return this.editors.containsKey(player);
    }

    public void addEditor(ServerPlayerEntity player) {
        // clean up to at least keep duplicate players away (removed players can be left over in the map, but as long as one player has no duplicates here, should be fine)
        for (ServerPlayerEntity serverPlayerEntity : this.editors.keySet()) {
            if(serverPlayerEntity.getRemovalReason() != null) this.editors.remove(serverPlayerEntity);
        }
        this.editors.put(player, DUMMY);
    }

    public void removeEditor(ServerPlayerEntity player) {
        this.editors.remove(player);
    }

    public void setOutputStrength(World world, BlockState state, FlatDirection direction, int power) {
        this.outputStrengths[direction.toInt()] = (byte) power;
        this.markDirty();
        world.updateListeners(pos, state, state, Block.NOTIFY_LISTENERS);
    }

    public int getOutputStrength(FlatDirection direction) {
        return this.outputStrengths[direction.toInt()];
    }

    public ServerCircuit getCircuit() {
        if(this.circuit == null) {
            this.circuit = new ServerCircuit(new BlockEntityServerCircuitContext(this));
            if(this.hasWorld()) {
                this.circuit.onWorldIsPresent();
            }
        }
        return this.circuit;
    }

    @Override
    public void setWorld(World world) {
        super.setWorld(world);
        if(this.circuit != null) {
            this.circuit.onWorldIsPresent();
        }
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putByteArray("outputStrengths", this.outputStrengths.clone());
        return nbt;
    }

    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    public void tick(World world, BlockPos pos, BlockState state) {
        this.getCircuit().tick();
    }
}
