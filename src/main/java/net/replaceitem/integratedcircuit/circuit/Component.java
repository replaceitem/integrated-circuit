package net.replaceitem.integratedcircuit.circuit;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Holder;
import net.minecraft.core.IdMapper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.StateDefinition;
import net.replaceitem.integratedcircuit.IntegratedCircuit;
import net.replaceitem.integratedcircuit.circuit.components.FacingComponent;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

public abstract class Component {
    public static final StreamCodec<RegistryFriendlyByteBuf, Component> PACKET_CODEC = ByteBufCodecs.registry(IntegratedCircuit.COMPONENTS_REGISTRY_KEY);

    //public static final MapCodec<Component> CODEC = createCodec(settings1 -> new Component(settings1));
    private final Holder.Reference<Component> registryEntry = IntegratedCircuit.COMPONENTS_REGISTRY.createIntrusiveHolder(this);
    public static final IdMapper<ComponentState> STATE_IDS = new IdMapper<>();
    
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
    private final StateDefinition<Component, ComponentState> stateDefinition;
    
    public static final FlatDirection[] DIRECTIONS = new FlatDirection[]{FlatDirection.WEST, FlatDirection.EAST, FlatDirection.NORTH, FlatDirection.SOUTH};

    public Component(Settings settings) {
        this.settings = settings;
        StateDefinition.Builder<Component, ComponentState> builder = new StateDefinition.Builder<>(this);
        this.appendProperties(builder);
        this.stateDefinition = builder.create(Component::getDefaultState, ComponentState::new);
        this.setDefaultState(this.stateDefinition.any());
    }

    public Settings getSettings() {
        return settings;
    }

    public net.minecraft.network.chat.Component getName() {
        return net.minecraft.network.chat.Component.translatable(IntegratedCircuit.COMPONENTS_REGISTRY.getKey(this).toLanguageKey("integrated_circuit.component"));
    }

    public void appendProperties(StateDefinition.Builder<Component, ComponentState> builder) {
        
    }

    public void setDefaultState(ComponentState defaultState) {
        this.defaultState = defaultState;
    }

    public final ComponentState getDefaultState() {
        return this.defaultState;
    }

    @Nullable
    public ComponentState getPlacementState(Circuit circuit, ComponentPos pos, FlatDirection rotation) {
        ComponentState defaultState = this.getDefaultState();
        if(this.stateDefinition.getProperties().contains(FacingComponent.FACING)) return defaultState.setValue(FacingComponent.FACING, rotation);
        return defaultState;
    }

    public abstract @Nullable Identifier getItemTexture();
    public abstract @Nullable Identifier getToolTexture();

    public abstract void extractRenderState(GuiGraphicsExtractor drawContext, int x, int y, float a, ComponentState state);

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
    
    public void onUse(ComponentState state, Circuit circuit, ComponentPos pos, Player player) {
        
    }
    
    public void onPlaced(ServerCircuit circuit, ComponentPos pos, ComponentState state) {
        
    }

    public void scheduledTick(ComponentState state, ServerCircuit circuit, ComponentPos pos, RandomSource random) {
        
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
     * Works slightly different from {@link net.minecraft.world.level.block.RedStoneWireBlock#increasePower(BlockState)},
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
        return IntegratedCircuit.COMPONENTS_REGISTRY.getKey(this).toString();
    }


    protected static <C extends Component> RecordCodecBuilder<C, Settings> createSettingsCodec() {
        return Settings.CODEC.fieldOf("properties").forGetter(Component::getSettings);
    }

    public static <C extends Component> MapCodec<C> createCodec(Function<Settings, C> componentFromSetting) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(createSettingsCodec()).apply(instance, componentFromSetting));
    }

    public boolean emitsRedstonePower(ComponentState state) {
        return false;
    }

    public net.minecraft.network.chat.Component getHoverInfoText(ComponentState state) {
        return net.minecraft.network.chat.Component.empty();
    }

    public StateDefinition<Component, ComponentState> getStateDefinition() {
        return stateDefinition;
    }


    public static class Settings {

        public static final Codec<Settings> CODEC = MapCodec.unitCodec(Settings::new);
        
        public Settings() {
            
        }

        public Settings sounds(SoundType soundGroup) {
            this.soundGroup = soundGroup;
            return this;
        }

        public SoundType soundGroup = SoundType.STONE;
    }
}
