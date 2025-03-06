package net.replaceitem.integratedcircuit.circuit;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.IdList;
import net.minecraft.util.math.random.Random;
import net.replaceitem.integratedcircuit.IntegratedCircuit;
import net.replaceitem.integratedcircuit.circuit.components.FacingComponent;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public abstract class Component {
    public static final PacketCodec<RegistryByteBuf, Component> PACKET_CODEC = PacketCodecs.registryValue(IntegratedCircuit.COMPONENTS_REGISTRY_KEY);

    //public static final MapCodec<Component> CODEC = createCodec(settings1 -> new Component(settings1));
    private final RegistryEntry.Reference<Component> registryEntry = IntegratedCircuit.COMPONENTS_REGISTRY.createEntry(this);
    public static final IdList<ComponentState> STATE_IDS = new IdList<>();
    
    // copy from Block, only few are needed, but all kept for future
    public static final int NOTIFY_NEIGHBORS = 1;
    public static final int NOTIFY_LISTENERS = 2;
    public static final int NO_REDRAW = 4;
    public static final int REDRAW_ON_MAIN_THREAD = 8;
    public static final int FORCE_STATE = 16;
    public static final int SKIP_DROPS = 32;
    public static final int MOVED = 64;
    public static final int SKIP_LIGHTING_UPDATES = 128;

    public static final int NOTIFY_ALL = 3;
    
    private final Settings settings;
    private ComponentState defaultState;
    private final StateManager<Component, ComponentState> stateManager;
    
    public static final FlatDirection[] DIRECTIONS = new FlatDirection[]{FlatDirection.WEST, FlatDirection.EAST, FlatDirection.NORTH, FlatDirection.SOUTH};

    public Component(Settings settings) {
        this.settings = settings;
        StateManager.Builder<Component, ComponentState> builder = new StateManager.Builder<>(this);
        this.appendProperties(builder);
        this.stateManager = builder.build(Component::getDefaultState, ComponentState::new);
        this.setDefaultState(this.stateManager.getDefaultState());
    }

    public Settings getSettings() {
        return settings;
    }

    public Text getName() {
        return Text.translatable(IntegratedCircuit.COMPONENTS_REGISTRY.getId(this).toTranslationKey("integrated_circuit.component"));
    }

    public void appendProperties(StateManager.Builder<Component, ComponentState> builder) {
        
    }

    public void setDefaultState(ComponentState defaultState) {
        this.defaultState = defaultState;
    }

    public final ComponentState getDefaultState() {
        return this.defaultState;
    }
    
    public ComponentState getPlacementState(Circuit circuit, ComponentPos pos, FlatDirection rotation) {
        ComponentState defaultState = this.getDefaultState();
        if(this.stateManager.getProperties().contains(FacingComponent.FACING)) return defaultState.with(FacingComponent.FACING, rotation);
        return defaultState;
    }

    public abstract @Nullable Identifier getItemTexture();
    public abstract @Nullable Identifier getToolTexture();

    public abstract void render(DrawContext drawContext, int x, int y, float a, ComponentState state);

    public static void replace(ComponentState state, ComponentState newState, Circuit world, ComponentPos pos, int flags) {
        replace(state, newState, world, pos, flags, 512);
    }

    public static void replace(ComponentState state, ComponentState newState, Circuit world, ComponentPos pos, int flags, int maxUpdateDepth) {
        if (newState != state) {
            if (newState.isAir()) {
                if(!world.isClient) {
                    world.breakBlock(pos, maxUpdateDepth);
                }
            } else {
                world.setComponentState(pos, newState, flags & ~SKIP_DROPS, maxUpdateDepth);
            }
        }
    }

    public void neighborUpdate(ComponentState state, Circuit circuit, ComponentPos pos, Component sourceBlock, ComponentPos sourcePos, boolean notify) {
        
    }

    public void onBlockAdded(ComponentState state, Circuit circuit, ComponentPos pos, ComponentState oldState) {
        
    }
    
    public void onStateReplaced(ComponentState state, Circuit circuit, ComponentPos pos, ComponentState newState) {
        
    }
    
    public void onUse(ComponentState state, Circuit circuit, ComponentPos pos, PlayerEntity player) {
        
    }
    
    public void onPlaced(ServerCircuit circuit, ComponentPos pos, ComponentState state) {
        
    }

    public void scheduledTick(ComponentState state, ServerCircuit circuit, ComponentPos pos, Random random) {
        
    }

    public ComponentState getStateForNeighborUpdate(ComponentState state, FlatDirection direction, ComponentState neighborState, Circuit circuit, ComponentPos pos, ComponentPos neighborPos) {
        return state;
    }

    public void prepare(ComponentState state, CircuitAccess circuit, ComponentPos pos, int flags, int maxUpdateDepth) {}

    public abstract boolean isSolidBlock(Circuit circuit, ComponentPos pos);

    public boolean isSideSolidFullSquare(Circuit circuit, ComponentPos blockPos, FlatDirection direction) {
        return isSolidBlock(circuit, blockPos);
    }

    public int getWeakRedstonePower(ComponentState state, Circuit circuit, ComponentPos pos, FlatDirection direction) {
        return 0;
    }

    public int getStrongRedstonePower(ComponentState state, Circuit circuit, ComponentPos pos, FlatDirection direction) {
        return 0;
    }

    public boolean hasComparatorOutput(ComponentState componentState) {
        return false;
    }

    public int getComparatorOutput(ComponentState state, Circuit circuit, ComponentPos pos) {
        return 0;
    }

    /**
     * Works slightly different from {@link net.minecraft.block.RedstoneWireBlock#increasePower(BlockState)},
     * in that it gets called from the component where the power is checked at, not where it's checked from.
     * This is done so different behaviours can be used for wire/port/crossover, without several checks.
     */
    @SuppressWarnings("JavadocReference")
    public int increasePower(ComponentState state, FlatDirection side) {
        return 0;
    }

    public boolean canPlaceAt(ComponentState state, Circuit circuit, ComponentPos pos) {
        return true;
    }


    @Override
    public String toString() {
        return IntegratedCircuit.COMPONENTS_REGISTRY.getId(this).toString();
    }


    protected static <C extends Component> RecordCodecBuilder<C, Component.Settings> createSettingsCodec() {
        return Component.Settings.CODEC.fieldOf("properties").forGetter(Component::getSettings);
    }

    public static <C extends Component> MapCodec<C> createCodec(Function<Component.Settings, C> componentFromSetting) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(createSettingsCodec()).apply(instance, componentFromSetting));
    }

    public boolean emitsRedstonePower(ComponentState state) {
        return false;
    }

    public Text getHoverInfoText(ComponentState state) {
        return Text.empty();
    }

    public StateManager<Component, ComponentState> getStateManager() {
        return stateManager;
    }


    public static class Settings {

        public static final Codec<Settings> CODEC = Codec.unit(Settings::new);
        
        public Settings() {
            
        }

        public Settings sounds(BlockSoundGroup soundGroup) {
            this.soundGroup = soundGroup;
            return this;
        }

        public BlockSoundGroup soundGroup = BlockSoundGroup.STONE;
    }
}
