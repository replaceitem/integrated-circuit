package net.replaceitem.integratedcircuit.circuit.components;

import net.minecraft.block.Block;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.tick.TickPriority;
import net.replaceitem.integratedcircuit.circuit.Circuit;
import net.replaceitem.integratedcircuit.circuit.ServerCircuit;
import net.replaceitem.integratedcircuit.circuit.state.ComponentState;
import net.replaceitem.integratedcircuit.circuit.state.property.BooleanComponentProperty;
import net.replaceitem.integratedcircuit.circuit.state.property.IntComponentProperty;
import net.replaceitem.integratedcircuit.client.IntegratedCircuitScreen;
import net.replaceitem.integratedcircuit.util.ComponentPos;
import net.replaceitem.integratedcircuit.util.IntegratedCircuitIdentifier;

public class ComparatorComponent extends AbstractRedstoneGateComponent {

    private static final BooleanComponentProperty SUBTRACT_MODE = new BooleanComponentProperty("subtract_mode", 3);
    private static final IntComponentProperty OUTPUT_POWER = new IntComponentProperty("output_power", 4, 4);

    public ComparatorComponent(int id, Settings settings) {
        super(id, settings);
    }

    private static final Identifier ITEM_TEXTURE = new Identifier("textures/item/comparator.png");

    public static final Identifier TEXTURE = new IntegratedCircuitIdentifier("textures/integrated_circuit/comparator.png");
    public static final Identifier TEXTURE_ON = new IntegratedCircuitIdentifier("textures/integrated_circuit/comparator_on.png");

    public static final Identifier TEXTURE_TORCH_OFF = new IntegratedCircuitIdentifier("textures/integrated_circuit/torch_top_off.png");
    public static final Identifier TEXTURE_TORCH_ON = new IntegratedCircuitIdentifier("textures/integrated_circuit/torch_top_on.png");

    @Override
    public Identifier getItemTexture() {
        return ITEM_TEXTURE;
    }

    @Override
    public Text getHoverInfoText(ComponentState state) {
        int signalStrength = state.get(OUTPUT_POWER);
        return IntegratedCircuitScreen.getSignalStrengthText(signalStrength);
    }

    @Override
    public void render(DrawContext drawContext, int x, int y, float a, ComponentState state) {
        boolean powered = state.get(POWERED);
        int rot = state.get(FACING).getOpposite().getIndex();
        IntegratedCircuitScreen.renderComponentTexture(drawContext, powered ? TEXTURE_ON : TEXTURE, x, y, rot, 1, 1, 1, a);
        
        Identifier torchTexture = powered ? TEXTURE_TORCH_ON : TEXTURE_TORCH_OFF;

        IntegratedCircuitScreen.renderPartialTexture(drawContext, torchTexture, x, y, 3, 10, 4, 4, rot, 1, 1, 1, a);
        IntegratedCircuitScreen.renderPartialTexture(drawContext, torchTexture, x, y, 9, 10, 4, 4, rot, 1, 1, 1, a);

        Identifier modeTorchTexture = state.get(SUBTRACT_MODE) ? TEXTURE_TORCH_ON : TEXTURE_TORCH_OFF;
        IntegratedCircuitScreen.renderPartialTexture(drawContext, modeTorchTexture, x, y, 6, 1, 4, 4, rot, 1, 1, 1, a);
    }


    @Override
    protected int getUpdateDelayInternal(ComponentState state) {
        return 2;
    }

    @Override
    protected int getOutputLevel(Circuit circuit, ComponentPos pos, ComponentState state) {
        return state.get(OUTPUT_POWER);
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
        if (state.get(SUBTRACT_MODE)) {
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
        return i == j && !state.get(SUBTRACT_MODE);
    }


    @Override
    protected int getPower(Circuit circuit, ComponentPos pos, ComponentState state) {
        // overriding is unnecessary, but when blocks get added that have a comparator power level, this needs to be changed
        return super.getPower(circuit, pos, state);
    }

    @Override
    public void onUse(ComponentState state, Circuit circuit, ComponentPos pos, PlayerEntity player) {
        state = state.cycle(SUBTRACT_MODE);
        circuit.setComponentState(pos, state, Block.NOTIFY_LISTENERS);
        this.update(circuit, pos, state);
        float f = state.get(SUBTRACT_MODE) ? 0.55f : 0.5f;
        circuit.playSound(player, SoundEvents.BLOCK_COMPARATOR_CLICK, SoundCategory.BLOCKS, 0.3f, f);
    }

    @Override
    protected void updatePowered(Circuit circuit, ComponentPos pos, ComponentState state) {
        if (circuit.getCircuitTickScheduler().isTicking(pos, this)) {
            return;
        }
        int calculatedOutputSignal = this.calculateOutputSignal(circuit, pos, state);
        int outputSignal = state.get(OUTPUT_POWER);
        if (calculatedOutputSignal != outputSignal || state.get(POWERED) != this.hasPower(circuit, pos, state)) {
            TickPriority tickPriority = this.isTargetNotAligned(circuit, pos, state) ? TickPriority.HIGH : TickPriority.NORMAL;
            circuit.scheduleBlockTick(pos, this, 2, tickPriority);
        }
    }

    private void update(Circuit world, ComponentPos pos, ComponentState state) {
        int i = this.calculateOutputSignal(world, pos, state);
        int j = state.get(OUTPUT_POWER);
        state = state.with(OUTPUT_POWER, i); // this is done in a BE in vanilla, so in this case we need to re-place the state, which is done below
        if (j != i || !state.get(SUBTRACT_MODE)) {
            boolean hasPower = this.hasPower(world, pos, state);
            boolean powered = state.get(POWERED);
            if (powered && !hasPower) {
                world.setComponentState(pos, state.with(POWERED, false), NOTIFY_LISTENERS);
            } else if (!powered && hasPower) {
                world.setComponentState(pos, state.with(POWERED, true), NOTIFY_LISTENERS);
            } else if (j != i) {
                world.setComponentState(pos, state, NOTIFY_LISTENERS); // if SS changed and we haven't already placed the state above
            }
            this.updateTarget(world, pos, state);
        }
    }

    @Override
    public void scheduledTick(ComponentState state, ServerCircuit circuit, ComponentPos pos, Random random) {
        this.update(circuit, pos, state);
    }

    @Override
    public boolean isSolidBlock(Circuit circuit, ComponentPos pos) {
        return false;
    }

    @Override
    public void appendProperties(ComponentState.PropertyBuilder builder) {
        super.appendProperties(builder);
        builder.append(SUBTRACT_MODE);
        builder.append(OUTPUT_POWER);
    }
}
