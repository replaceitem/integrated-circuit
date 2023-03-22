package net.replaceitem.integratedcircuit;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Nameable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.replaceitem.integratedcircuit.circuit.ServerCircuit;
import net.replaceitem.integratedcircuit.util.FlatDirection;

import java.util.Set;
import java.util.WeakHashMap;

public class IntegratedCircuitBlockEntity extends BlockEntity implements Nameable {

    protected WeakHashMap<ServerPlayerEntity, Object> editors;

    protected Text customName;
    protected ServerCircuit circuit;
    
    protected byte[] outputStrengths = new byte[] {0,0,0,0};

    public IntegratedCircuitBlockEntity(BlockPos pos, BlockState state) {
        super(IntegratedCircuit.INTEGRATED_CIRCUIT_BLOCK_ENTITY, pos, state);
        this.circuit = new ServerCircuit(this);
        this.editors = new WeakHashMap<>();
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        if(hasCustomName()) nbt.putString("CustomName", Text.Serializer.toJson(this.customName));
        this.circuit.writeNbt(nbt);
        nbt.putByteArray("outputStrengths", outputStrengths.clone());
        super.writeNbt(nbt);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.customName = nbt.contains("CustomName", NbtElement.STRING_TYPE) ? Text.Serializer.fromJson(nbt.getString("CustomName")) : null;
        this.circuit.readNbt(nbt);
        this.outputStrengths = nbt.getByteArray("outputStrengths");
    }



    public void setCustomName(Text name) {
        this.customName = name;
    }

    @Override
    public Text getCustomName() {
        return this.customName;
    }

    @Override
    public Text getName() {
        if(hasCustomName()) return this.customName;
        return IntegratedCircuit.INTEGRATED_CIRCUIT_BLOCK.getName();
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

    public void setOutputStrength(FlatDirection direction, int power) {
        this.outputStrengths[direction.toInt()] = (byte) power;
    }

    public int getOutputStrength(FlatDirection direction) {
        return this.outputStrengths[direction.toInt()];
    }

    public ServerCircuit getCircuit() {
        return circuit;
    }

    @Override
    public void setWorld(World world) {
        super.setWorld(world);
        this.circuit.onWorldIsPresent();
    }
}
