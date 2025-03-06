package net.replaceitem.integratedcircuit;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.*;
import net.minecraft.world.block.OrientationHelper;
import net.minecraft.world.block.WireOrientation;
import net.minecraft.world.tick.ScheduledTickView;
import net.replaceitem.integratedcircuit.circuit.Circuit;
import net.replaceitem.integratedcircuit.circuit.CircuitSerializer;
import net.replaceitem.integratedcircuit.circuit.ServerCircuit;
import net.replaceitem.integratedcircuit.network.packet.EditIntegratedCircuitS2CPacket;
import net.replaceitem.integratedcircuit.util.FlatDirection;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class IntegratedCircuitBlock extends HorizontalFacingBlock implements BlockEntityProvider {
    public static final MapCodec<IntegratedCircuitBlock> CODEC = createCodec(IntegratedCircuitBlock::new);
    protected static final VoxelShape SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 2.0, 16.0);

    public IntegratedCircuitBlock(Settings settings) {
        super(settings);
        setDefaultState(this.getStateManager().getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.NORTH));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
        stateManager.add(Properties.HORIZONTAL_FACING);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return Block.hasTopRim(world, pos.down());
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(Properties.HORIZONTAL_FACING, ctx.getHorizontalPlayerFacing());
    }
    
    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if(world.getBlockEntity(pos) instanceof IntegratedCircuitBlockEntity integratedCircuitBlockEntity && integratedCircuitBlockEntity.getCircuit() != null) {
            integratedCircuitBlockEntity.getCircuit().tick();
        }
        world.scheduleBlockTick(pos, state.getBlock(), 1);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        if (direction == Direction.DOWN && !this.canPlaceAt(state, world, pos))
            return Blocks.AIR.getDefaultState();
        return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }


    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, @Nullable WireOrientation wireOrientation, boolean notify) {
        super.neighborUpdate(state, world, pos, sourceBlock, wireOrientation, notify);
        if(world.isClient) return;
        if(world.getBlockEntity(pos) instanceof IntegratedCircuitBlockEntity integratedCircuitBlockEntity && integratedCircuitBlockEntity.getCircuit() != null) {
            for (FlatDirection direction : FlatDirection.VALUES) {
                integratedCircuitBlockEntity.getCircuit().getContext().readExternalPower(direction);
            }
        }
        ensureTicking(world, pos);    
    }

    @Override
    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        this.updateTargets(world, pos);
        ensureTicking(world, pos);
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (moved || state.isOf(newState.getBlock())) return;
        super.onStateReplaced(state, world, pos, newState, moved);
        this.updateTargets(world, pos);
    }
    
    private void ensureTicking(World world, BlockPos pos) {
        if(!world.getBlockTickScheduler().isQueued(pos, this)) {
            world.scheduleBlockTick(pos, this, 0);
        }
    }
    
    public void updateTarget(World world, BlockPos pos, Direction direction) {
        BlockPos blockPos = pos.offset(direction);
        WireOrientation wireOrientation = OrientationHelper.getEmissionOrientation(world, direction, Direction.UP);
        world.updateNeighbor(blockPos, this, wireOrientation);
        world.updateNeighborsExcept(blockPos, this, direction.getOpposite(), wireOrientation);
    }

    protected void updateTargets(World world, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            updateTarget(world, pos, direction);
        }
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient() && player instanceof ServerPlayerEntity serverPlayerEntity && world.getBlockEntity(pos) instanceof IntegratedCircuitBlockEntity integratedCircuitBlockEntity) {
            ServerCircuit circuit = integratedCircuitBlockEntity.getCircuit();

            if (circuit == null)
                return ActionResult.FAIL;

            DataResult<NbtElement> circuitNbt = CircuitSerializer.writeCircuit(circuit);

            if (circuitNbt.error().isPresent()) {
                IntegratedCircuit.LOGGER.error(circuitNbt.error().get().message());
                return ActionResult.FAIL;
            }

            NbtElement nbtElement = circuitNbt.result().orElseThrow();

            if (!(nbtElement instanceof NbtCompound compound))
                return ActionResult.FAIL;

            integratedCircuitBlockEntity.addEditor(serverPlayerEntity);

            Text customName = integratedCircuitBlockEntity.getCustomName();

            if (customName == null) {
                customName = Text.of("");
            }

            ServerPlayNetworking.send(
                serverPlayerEntity,
                new EditIntegratedCircuitS2CPacket(
                    pos,
                    integratedCircuitBlockEntity.getName(),
                    customName,
                    compound
                )
            );
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        if (itemStack.contains(DataComponentTypes.CUSTOM_NAME) && world.getBlockEntity(pos) instanceof IntegratedCircuitBlockEntity integratedCircuitBlockEntity)
            integratedCircuitBlockEntity.setCustomName(itemStack.getName());
        this.updateTargets(world, pos);
        world.updateNeighbor(pos, this, null);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new IntegratedCircuitBlockEntity(pos, state);
    }

    public int getInputPower(RedstoneView view, BlockPos pos, BlockState state, FlatDirection dir) {
        Direction direction = dir.toVanillaDirection(state);

        BlockPos blockPos = pos.offset(direction);
        BlockState blockState = view.getBlockState(blockPos);

        int i = view.getEmittedRedstonePower(blockPos, direction);
        if (i >= 15) {
            return i;
        }
        return Math.max(i, blockState.isOf(Blocks.REDSTONE_WIRE) ? blockState.get(RedstoneWireBlock.POWER) : 0);
    }

    @Override
    protected boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    @Override
    protected int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return state.getWeakRedstonePower(world, pos, direction);
    }

    @Override
    protected int getWeakRedstonePower(BlockState state, BlockView view, BlockPos pos, Direction direction) {
        if(direction.getAxis().isVertical()) return 0;
        FlatDirection circuitDirection = FlatDirection.fromVanillaDirection(state, direction.getOpposite());
        if(view.getBlockEntity(pos) instanceof IntegratedCircuitBlockEntity integratedCircuitBlockEntity && integratedCircuitBlockEntity.getCircuit() != null)
            return integratedCircuitBlockEntity.getCircuit().getPortOutputStrength(circuitDirection);
        return 0;
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof IntegratedCircuitBlockEntity integratedCircuitBlockEntity) {
            ServerCircuit circuit = integratedCircuitBlockEntity.getCircuit();
            if (!world.isClient && player.isCreative() && circuit != null && !circuit.isEmpty()) {
                dropStacks(state, world, pos, blockEntity, player, player.getMainHandStack());
            }
        }
        return super.onBreak(world, pos, state, player);
    }

    @Override
    protected List<ItemStack> getDroppedStacks(BlockState state, LootWorldContext.Builder builder) {
        List<ItemStack> list = super.getDroppedStacks(state, builder);

        BlockEntity blockEntity = builder.getOptional(LootContextParameters.BLOCK_ENTITY);
        if (blockEntity instanceof IntegratedCircuitBlockEntity integratedCircuitBlockEntity) {
            Circuit circuit = integratedCircuitBlockEntity.getCircuit();

            if(circuit == null || circuit.isEmpty()) { // If it's empty, get rid of the BlockEntityData, so it stacks with other empty circuits
                for(ItemStack stack : list) {
                    stack.remove(DataComponentTypes.BLOCK_ENTITY_DATA);
                }
            }
        }
        return list;
    }
    
    public int getPortRenderStrength(BlockRenderView view, BlockPos pos, FlatDirection circuitDirection) {
        if(view.getBlockEntity(pos) instanceof IntegratedCircuitBlockEntity integratedCircuitBlockEntity) {
            return integratedCircuitBlockEntity.getPortRenderStrength(circuitDirection);
        }
        return 0;
    }

    @Override
    protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
        return CODEC;
    }

    public static IntegratedCircuitBlock fromColor(DyeColor color) {
        return switch (color) {
            case WHITE -> IntegratedCircuit.Blocks.WHITE_INTEGRATED_CIRCUIT;
            case ORANGE -> IntegratedCircuit.Blocks.ORANGE_INTEGRATED_CIRCUIT;
            case MAGENTA -> IntegratedCircuit.Blocks.MAGENTA_INTEGRATED_CIRCUIT;
            case LIGHT_BLUE -> IntegratedCircuit.Blocks.LIGHT_BLUE_INTEGRATED_CIRCUIT;
            case YELLOW -> IntegratedCircuit.Blocks.YELLOW_INTEGRATED_CIRCUIT;
            case LIME -> IntegratedCircuit.Blocks.LIME_INTEGRATED_CIRCUIT;
            case PINK -> IntegratedCircuit.Blocks.PINK_INTEGRATED_CIRCUIT;
            case GRAY -> IntegratedCircuit.Blocks.GRAY_INTEGRATED_CIRCUIT;
            case LIGHT_GRAY -> IntegratedCircuit.Blocks.LIGHT_GRAY_INTEGRATED_CIRCUIT;
            case CYAN -> IntegratedCircuit.Blocks.CYAN_INTEGRATED_CIRCUIT;
            case PURPLE -> IntegratedCircuit.Blocks.PURPLE_INTEGRATED_CIRCUIT;
            case BLUE -> IntegratedCircuit.Blocks.BLUE_INTEGRATED_CIRCUIT;
            case BROWN -> IntegratedCircuit.Blocks.BROWN_INTEGRATED_CIRCUIT;
            case GREEN -> IntegratedCircuit.Blocks.GREEN_INTEGRATED_CIRCUIT;
            case RED -> IntegratedCircuit.Blocks.RED_INTEGRATED_CIRCUIT;
            case BLACK -> IntegratedCircuit.Blocks.BLACK_INTEGRATED_CIRCUIT;
        };
    }
}
