package net.replaceitem.integratedcircuit;

import com.mojang.serialization.DataResult;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.nbt.NbtByteArray;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Nameable;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.replaceitem.integratedcircuit.circuit.Circuit;
import net.replaceitem.integratedcircuit.circuit.CircuitSerializer;
import net.replaceitem.integratedcircuit.circuit.ServerCircuit;
import net.replaceitem.integratedcircuit.circuit.components.PortComponent;
import net.replaceitem.integratedcircuit.circuit.context.BlockEntityServerCircuitContext;
import net.replaceitem.integratedcircuit.util.FlatDirection;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;

public class IntegratedCircuitBlockEntity extends BlockEntity implements Nameable {
    private static final Object DUMMY = new Object();
    protected WeakHashMap<ServerPlayerEntity, Object> editors;
    
    @Nullable
    protected Text customName;

    @Nullable
    private ServerCircuit circuit;
    @Nullable
    private NbtCompound circuitNbt;

    protected byte[] renderSignalStrengths = new byte[] {0,0,0,0};

    public IntegratedCircuitBlockEntity(BlockPos pos, BlockState state) {
        super(IntegratedCircuit.INTEGRATED_CIRCUIT_BLOCK_ENTITY, pos, state);
        this.circuit = null; // on server, this is only initialized once the world is present. On client this will always be null
        this.editors = new WeakHashMap<>(4);
    }
    
    private void tryCreateCircuit() {
        World world = this.getWorld();
        if(world != null && !world.isClient) {
            BlockEntityServerCircuitContext context = new BlockEntityServerCircuitContext(this);
            if(this.circuitNbt != null) {
                this.circuit = new CircuitSerializer(this.circuitNbt).readServerCircuit(context);
                this.circuitNbt = null;
                for (int i = 0; i < Circuit.PORT_COUNT; i++) {
                    renderSignalStrengths[i] = circuit.getPorts()[i].get(PortComponent.POWER).byteValue();
                }
            } else {
                this.circuit = new ServerCircuit(context);
            }
        }
    }
    
    @Override
    protected void addComponents(ComponentMap.Builder componentMapBuilder) {
        super.addComponents(componentMapBuilder);

        componentMapBuilder.add(DataComponentTypes.CUSTOM_NAME, this.customName);
        
        Optional.ofNullable(circuit)
                .map(CircuitSerializer::writeCircuit)
                .or(() -> Optional.ofNullable(circuitNbt).map(DataResult::success))
                .orElseGet(() -> DataResult.error(() -> "No circuit or circuitNbt to serialize"))
                .ifSuccess(nbtElement -> {
                    if (nbtElement instanceof NbtCompound compound)
                        componentMapBuilder.add(IntegratedCircuit.CIRCUIT_DATA, NbtComponent.of(compound));
                });
    }

    @Override
    protected void readComponents(ComponentsAccess components) {
        super.readComponents(components);
        
        this.customName = components.get(DataComponentTypes.CUSTOM_NAME);
        
        NbtComponent circuitComponent = components.get(IntegratedCircuit.CIRCUIT_DATA);
        if(circuitComponent != null) {
            this.circuitNbt = circuitComponent.copyNbt();
        }
        tryCreateCircuit();
    }

    @Override
    public void removeFromCopiedStackData(WriteView view) {
        super.removeFromCopiedStackData(view);
        view.remove("CustomName");
        view.remove("circuit");
    }

    @Override
    protected void readData(ReadView view) {
        this.customName = tryParseCustomName(view, "CustomName");
        this.circuitNbt = view.read("circuit", NbtCompound.CODEC).orElse(null);
        tryCreateCircuit();

        // only received from toInitialChunkDataNbt on the client
        view.read("outputStrengths", Codecs.NBT_ELEMENT)
                .flatMap(nbtElement ->
                        nbtElement instanceof NbtByteArray byteArray ?
                                Optional.of(byteArray) :
                                Optional.empty()
                )
                .ifPresent(bytes -> this.renderSignalStrengths = bytes.getByteArray());
    }

    @Override
    protected void writeData(WriteView view) {
        view.putNullable("CustomName", TextCodecs.CODEC, this.customName);

        if(circuit != null) {
            view.putNullable("circuit", ServerCircuit.CODEC.withContext(this.circuit.getContext()), this.circuit);
        } else if(circuitNbt != null) {
            view.put("circuit", NbtCompound.CODEC, circuitNbt);
        }
    }

    public void setCustomName(@Nullable Text name) {
        this.customName = name;
        this.markDirty();

        if (world != null) {
            BlockPos pos = getPos();
            BlockState state = world.getBlockState(pos);

            world.updateListeners(
                pos,
                this.getCachedState(),
                state,
                Block.NOTIFY_LISTENERS
            );
        }
    }

    @Override
    @Nullable
    public Text getCustomName() {
        return this.customName;
    }

    @Override
    public Text getName() {
        if(hasCustomName()) return this.customName;
        return Text.translatable("block.integrated_circuit.integrated_circuit");
    }

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

    public void setRenderSignalStrength(FlatDirection direction, int power) {
        this.renderSignalStrengths[direction.getIndex()] = (byte) power;
    }

    public int getPortRenderStrength(FlatDirection direction) {
        return this.renderSignalStrengths[direction.getIndex()];
    }

    @Nullable
    public ServerCircuit getCircuit() {
        return this.circuit;
    }

    @Override
    public void setWorld(World world) {
        super.setWorld(world);
        tryCreateCircuit();
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound nbt = new NbtCompound();
        nbt.putByteArray("outputStrengths", this.renderSignalStrengths.clone());
        if(this.hasCustomName()) {
            nbt.put("CustomName", TextCodecs.CODEC, this.customName);
        }
        return nbt;
    }

    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }
}
