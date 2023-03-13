package net.replaceitem.integratedcircuit;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.Nameable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.replaceitem.integratedcircuit.circuit.ServerCircuit;
import net.replaceitem.integratedcircuit.util.FlatDirection;

import java.util.UUID;

public class IntegratedCircuitBlockEntity extends BlockEntity implements Nameable {

    protected UUID editor;

    protected Text customName;
    protected ServerCircuit circuit;
    
    protected byte[] outputStrengths = new byte[] {0,0,0,0};

    public IntegratedCircuitBlockEntity(BlockPos pos, BlockState state) {
        super(IntegratedCircuit.INTEGRATED_CIRCUIT_BLOCK_ENTITY, pos, state);
        this.circuit = new ServerCircuit(this);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        if(hasCustomName()) nbt.putString("CustomName", Text.Serializer.toJson(this.customName));
        this.circuit.writeNbt(nbt);
        nbt.putByteArray("outputStrengths", outputStrengths);
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

    public UUID getEditor() {
        return this.editor;
    }

    public void setEditor(UUID editor) {
        this.editor = editor;
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
