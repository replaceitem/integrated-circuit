package net.replaceitem.integratedcircuit.circuit;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.collection.IndexedIterable;
import net.minecraft.world.chunk.PalettedContainer;
import net.replaceitem.integratedcircuit.util.ComponentPos;

public class CircuitSection {
    public static final PalettedContainer.PaletteProvider COMPONENT_STATE_PALETTE_PROVIDER = new FlatPaletteProvider(15) {
        @Override
        public <A> PalettedContainer.DataProvider<A> createDataProvider(IndexedIterable<A> idList, int bits) {
            return PalettedContainer.PaletteProvider.BLOCK_STATE.createDataProvider(idList, bits);
        }
    };
    
    public static final Codec<PalettedContainer<ComponentState>> PALETTE_CODEC = PalettedContainer.createPalettedContainerCodec(
            Component.STATE_IDS, ComponentState.CODEC, CircuitSection.COMPONENT_STATE_PALETTE_PROVIDER, Components.AIR.getDefaultState()
    );
    
    public static final Codec<CircuitSection> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PALETTE_CODEC.fieldOf("component_states").forGetter(CircuitSection::getComponentStateContainer)
    ).apply(instance, CircuitSection::new));
    
    public static PalettedContainer<ComponentState> createContainer() {
        return new PalettedContainer<>(Component.STATE_IDS, Components.AIR.getDefaultState(), COMPONENT_STATE_PALETTE_PROVIDER);
    }
    
    private final PalettedContainer<ComponentState> stateContainer;

    public CircuitSection(PalettedContainer<ComponentState> stateContainer) {
        this.stateContainer = stateContainer;
    }
    
    public CircuitSection() {
        this.stateContainer = createContainer();
    }

    public PalettedContainer<ComponentState> getComponentStateContainer() {
        return stateContainer;
    }

    public ComponentState getComponentState(int x, int y) {
        return stateContainer.get(x, y, 0);
    }

    public void setComponentState(ComponentPos pos, ComponentState state) {
        stateContainer.set(pos.getX(), pos.getY(), 0, state);
    }

    public void readPacket(PacketByteBuf buf) {
        // TODO use this for sending circuits to clients
        this.stateContainer.readPacket(buf);
    }

    public void writePacket(PacketByteBuf buf) {
        this.stateContainer.writePacket(buf);
    }

    public boolean isEmpty() {
        return !stateContainer.hasAny(componentState -> componentState != Components.AIR_DEFAULT_STATE);
    }
}
