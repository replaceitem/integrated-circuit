package net.replaceitem.integratedcircuit;

import com.mojang.serialization.DataResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.Nameable;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.replaceitem.integratedcircuit.circuit.Circuit;
import net.replaceitem.integratedcircuit.circuit.CircuitSerializer;
import net.replaceitem.integratedcircuit.circuit.ServerCircuit;
import net.replaceitem.integratedcircuit.circuit.components.PortComponent;
import net.replaceitem.integratedcircuit.circuit.context.BlockEntityServerCircuitContext;
import net.replaceitem.integratedcircuit.util.FlatDirection;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;

public class IntegratedCircuitBlockEntity extends BlockEntity implements Nameable {
    private static final Object DUMMY = new Object();
    protected WeakHashMap<ServerPlayer, Object> editors;
    
    @Nullable
    protected Component customName;

    @Nullable
    private ServerCircuit circuit;
    @Nullable
    private CompoundTag circuitNbt;

    protected byte[] renderSignalStrengths = new byte[] {0,0,0,0};

    public IntegratedCircuitBlockEntity(BlockPos pos, BlockState state) {
        super(IntegratedCircuit.INTEGRATED_CIRCUIT_BLOCK_ENTITY, pos, state);
        this.circuit = null; // on server, this is only initialized once the world is present. On client this will always be null
        this.editors = new WeakHashMap<>(4);
    }
    
    private void tryCreateCircuit() {
        Level world = this.getLevel();
        if(world != null && !world.isClientSide()) {
            BlockEntityServerCircuitContext context = new BlockEntityServerCircuitContext(this);
            if(this.circuitNbt != null) {
                this.circuit = new CircuitSerializer(this.circuitNbt).readServerCircuit(context);
                this.circuitNbt = null;
                for (int i = 0; i < Circuit.PORT_COUNT; i++) {
                    renderSignalStrengths[i] = circuit.getPorts()[i].getValue(PortComponent.POWER).byteValue();
                }
            } else {
                this.circuit = new ServerCircuit(context);
            }
        }
    }
    
    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder componentMapBuilder) {
        super.collectImplicitComponents(componentMapBuilder);

        componentMapBuilder.set(DataComponents.CUSTOM_NAME, this.customName);
        
        Optional.ofNullable(circuit)
                .map(CircuitSerializer::writeCircuit)
                .or(() -> Optional.ofNullable(circuitNbt).map(DataResult::success))
                .orElseGet(() -> DataResult.error(() -> "No circuit or circuitNbt to serialize"))
                .ifSuccess(nbtElement -> {
                    if (nbtElement instanceof CompoundTag compound)
                        componentMapBuilder.set(IntegratedCircuit.CIRCUIT_DATA, CustomData.of(compound));
                });
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        
        this.customName = components.get(DataComponents.CUSTOM_NAME);
        
        CustomData circuitComponent = components.get(IntegratedCircuit.CIRCUIT_DATA);
        if(circuitComponent != null) {
            this.circuitNbt = circuitComponent.copyTag();
        }
        tryCreateCircuit();
    }

    @Override
    public void removeComponentsFromTag(ValueOutput view) {
        super.removeComponentsFromTag(view);
        view.discard("CustomName");
        view.discard("circuit");
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        this.customName = parseCustomNameSafe(view, "CustomName");
        this.circuitNbt = view.read("circuit", CompoundTag.CODEC).orElse(null);
        tryCreateCircuit();

        // only received from toInitialChunkDataNbt on the client
        view.read("outputStrengths", ExtraCodecs.NBT)
                .flatMap(nbtElement ->
                        nbtElement instanceof ByteArrayTag byteArray ?
                                Optional.of(byteArray) :
                                Optional.empty()
                )
                .ifPresent(bytes -> this.renderSignalStrengths = bytes.getAsByteArray());
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        view.storeNullable("CustomName", ComponentSerialization.CODEC, this.customName);

        if(circuit != null) {
            view.storeNullable("circuit", ServerCircuit.CODEC.withContext(this.circuit.getContext()), this.circuit);
        } else if(circuitNbt != null) {
            view.store("circuit", CompoundTag.CODEC, circuitNbt);
        }
    }

    public void setCustomName(@Nullable Component name) {
        this.customName = name;
        this.setChanged();

        if (level != null) {
            BlockPos pos = getBlockPos();
            BlockState state = level.getBlockState(pos);

            level.sendBlockUpdated(
                pos,
                this.getBlockState(),
                state,
                Block.UPDATE_CLIENTS
            );
        }
    }

    @Override
    @Nullable
    public Component getCustomName() {
        return this.customName;
    }

    @Override
    public Component getName() {
        Component name = this.customName;
        if (name == null) return Component.translatable("block.integrated_circuit.integrated_circuit");
        return name;
    }

    public Set<ServerPlayer> getEditingPlayers() {
        return editors.keySet();
    }

    public boolean isEditing(ServerPlayer player) {
        return this.editors.containsKey(player);
    }

    public void addEditor(ServerPlayer player) {
        // clean up to at least keep duplicate players away (removed players can be left over in the map, but as long as one player has no duplicates here, should be fine)
        for (ServerPlayer serverPlayerEntity : this.editors.keySet()) {
            if(serverPlayerEntity.getRemovalReason() != null) this.editors.remove(serverPlayerEntity);
        }
        this.editors.put(player, DUMMY);
    }

    public void removeEditor(ServerPlayer player) {
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
    public void setLevel(Level world) {
        super.setLevel(world);
        tryCreateCircuit();
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
        CompoundTag nbt = new CompoundTag();
        nbt.putByteArray("outputStrengths", this.renderSignalStrengths.clone());
        if(this.customName != null) {
            nbt.store("CustomName", ComponentSerialization.CODEC, this.customName);
        }
        return nbt;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
