package net.replaceitem.integratedcircuit.circuit.components;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ComparatorMode;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.ticks.TickPriority;
import net.replaceitem.integratedcircuit.IntegratedCircuit;
import net.replaceitem.integratedcircuit.circuit.Circuit;
import net.replaceitem.integratedcircuit.circuit.Component;
import net.replaceitem.integratedcircuit.circuit.ComponentState;
import net.replaceitem.integratedcircuit.circuit.ServerCircuit;
import net.replaceitem.integratedcircuit.client.gui.IntegratedCircuitScreen;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.FlatDirection;
import org.jetbrains.annotations.Nullable;

public class ComparatorComponent extends AbstractRedstoneGateComponent {
    private static final Identifier ITEM_TEXTURE = Identifier.withDefaultNamespace("textures/item/comparator.png");
    private static final Identifier TOOL_TEXTURE = IntegratedCircuit.id("toolbox/icons/comparator");
    private static final Identifier TEXTURE = IntegratedCircuit.id("textures/integrated_circuit/comparator.png");
    private static final Identifier TEXTURE_ON = IntegratedCircuit.id("textures/integrated_circuit/comparator_on.png");
    private static final Identifier TEXTURE_TORCH_OFF = IntegratedCircuit.id("textures/integrated_circuit/torch_top_off.png");
    private static final Identifier TEXTURE_TORCH_ON = IntegratedCircuit.id("textures/integrated_circuit/torch_top_on.png");

    public static final EnumProperty<ComparatorMode> MODE = BlockStateProperties.MODE_COMPARATOR;
    public static final IntegerProperty OUTPUT_POWER = BlockStateProperties.POWER;

    public ComparatorComponent(Settings settings) {
        super(settings);
        this.setDefaultState(this.getStateDefinition().any().setValue(FACING, FlatDirection.NORTH).setValue(POWERED, false).setValue(MODE, ComparatorMode.COMPARE).setValue(OUTPUT_POWER, 0));
    }

    @Override
    public @Nullable Identifier getItemTexture() {
        return ITEM_TEXTURE;
    }

    @Override
    public @Nullable Identifier getToolTexture() {
        return TOOL_TEXTURE;
    }

    @Override
    public net.minecraft.network.chat.Component getHoverInfoText(ComponentState state) {
        int signalStrength = state.getValue(OUTPUT_POWER);
        return IntegratedCircuitScreen.getSignalStrengthText(signalStrength);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor drawContext, int x, int y, float a, ComponentState state) {
        boolean powered = state.getValue(POWERED);
        int rot = state.getValue(FACING).getOpposite().getIndex();
        IntegratedCircuitScreen.extractComponentTextureRenderState(drawContext, powered ? TEXTURE_ON : TEXTURE, x, y, rot, a);

        Identifier torchTexture = powered ? TEXTURE_TORCH_ON : TEXTURE_TORCH_OFF;

        IntegratedCircuitScreen.extractPartialTextureRenderState(drawContext, torchTexture, x, y, 3, 10, 4, 4, rot, a);
        IntegratedCircuitScreen.extractPartialTextureRenderState(drawContext, torchTexture, x, y, 9, 10, 4, 4, rot, a);

        Identifier modeTorchTexture = state.getValue(MODE) == ComparatorMode.SUBTRACT ? TEXTURE_TORCH_ON : TEXTURE_TORCH_OFF;
        IntegratedCircuitScreen.extractPartialTextureRenderState(drawContext, modeTorchTexture, x, y, 6, 1, 4, 4, rot, a);
    }


    @Override
    protected int getUpdateDelayInternal(ComponentState state) {
        return 2;
    }

    @Override
    protected int getOutputLevel(Circuit circuit, ComponentPos pos, ComponentState state) {
        return state.getValue(OUTPUT_POWER);
    }

    private int calculateOutputSignal(Circuit world, ComponentPos pos, ComponentState state) {
        int i = this.getPower(world, pos, state);
        if (i == 0) {
            return 0;
        }
        int j = this.getMaxInputLevelSides(world, pos, state);
        if (j > i) {
            return 0;
        }
        if (state.getValue(MODE) == ComparatorMode.SUBTRACT) {
            return i - j;
        }
        return i;
    }

    @Override
    protected boolean hasPower(Circuit circuit, ComponentPos pos, ComponentState state) {
        int i = this.getPower(circuit, pos, state);
        if (i == 0) {
            return false;
        }
        int j = this.getMaxInputLevelSides(circuit, pos, state);
        if (i > j) {
            return true;
        }
        return i == j && state.getValue(MODE) == ComparatorMode.COMPARE;
    }


    @Override
    protected int getPower(Circuit circuit, ComponentPos pos, ComponentState state) {
        int power = super.getPower(circuit, pos, state);
        FlatDirection direction = state.getValue(FACING);
        ComponentPos offsetPos = pos.offset(direction);
        ComponentState offsetState = circuit.getComponentState(offsetPos);
        if (offsetState.hasComparatorOutput()) {
            power = offsetState.getComparatorOutput(circuit, offsetPos);
        } else if (power < 15 && offsetState.isSolidBlock(circuit, offsetPos)) {
            offsetPos = offsetPos.offset(direction);
            offsetState = circuit.getComponentState(offsetPos);
            if (offsetState.hasComparatorOutput()) {
                power = offsetState.getComparatorOutput(circuit, offsetPos);
            }
        }
        return power;
    }

    @Override
    public void onUse(ComponentState state, Circuit circuit, ComponentPos pos, Player player) {
        state = state.cycle(MODE);
        circuit.setComponentState(pos, state, Block.UPDATE_CLIENTS);
        this.update(circuit, pos, state);
        float f = state.getValue(MODE) == ComparatorMode.SUBTRACT ? 0.55f : 0.5f;
        circuit.playSound(player, SoundEvents.COMPARATOR_CLICK, SoundSource.BLOCKS, 1, f);
    }

    @Override
    protected void updatePowered(Circuit circuit, ComponentPos pos, ComponentState state) {
        if (circuit.getCircuitTickScheduler().isTicking(pos, this)) {
            return;
        }
        int calculatedOutputSignal = this.calculateOutputSignal(circuit, pos, state);
        int outputSignal = state.getValue(OUTPUT_POWER);
        if (calculatedOutputSignal != outputSignal || state.getValue(POWERED) != this.hasPower(circuit, pos, state)) {
            TickPriority tickPriority = this.isTargetNotAligned(circuit, pos, state) ? TickPriority.HIGH : TickPriority.NORMAL;
            circuit.scheduleBlockTick(pos, this, 2, tickPriority);
        }
    }

    private void update(Circuit world, ComponentPos pos, ComponentState state) {
        int i = this.calculateOutputSignal(world, pos, state);
        int j = state.getValue(OUTPUT_POWER);
        state = state.setValue(OUTPUT_POWER, i); // this is done in a BE in vanilla, so in this case we need to re-place the state, which is done below
        if (j != i || state.getValue(MODE) == ComparatorMode.COMPARE) {
            boolean hasPower = this.hasPower(world, pos, state);
            boolean powered = state.getValue(POWERED);
            if (powered && !hasPower) {
                world.setComponentState(pos, state.setValue(POWERED, false), NOTIFY_LISTENERS);
            } else if (!powered && hasPower) {
                world.setComponentState(pos, state.setValue(POWERED, true), NOTIFY_LISTENERS);
            } else if (j != i) {
                world.setComponentState(pos, state, NOTIFY_LISTENERS); // if SS changed and we haven't already placed the state above
            }
            this.updateTarget(world, pos, state);
        }
    }

    @Override
    public void scheduledTick(ComponentState state, ServerCircuit circuit, ComponentPos pos, RandomSource random) {
        this.update(circuit, pos, state);
    }

    @Override
    public void appendProperties(StateDefinition.Builder<Component, ComponentState> builder) {
        super.appendProperties(builder);
        builder.add(MODE);
        builder.add(OUTPUT_POWER);
    }
}
