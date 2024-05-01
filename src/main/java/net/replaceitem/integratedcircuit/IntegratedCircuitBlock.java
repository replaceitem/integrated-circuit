package net.replaceitem.integratedcircuit;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.BlockView;
import net.minecraft.world.RedstoneView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
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
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return Block.hasTopRim(world, pos.down());
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(Properties.HORIZONTAL_FACING, ctx.getHorizontalPlayerFacing());
    }
    
    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if(world.getBlockEntity(pos) instanceof IntegratedCircuitBlockEntity integratedCircuitBlockEntity && integratedCircuitBlockEntity.getCircuit() != null) {
            integratedCircuitBlockEntity.getCircuit().tick();
        }
        world.scheduleBlockTick(pos, state.getBlock(), 1);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (direction == Direction.DOWN && !this.canPlaceAt(state, world, pos))
            return Blocks.AIR.getDefaultState();
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        super.neighborUpdate(state, world, pos, sourceBlock, sourcePos, notify);
        if(world.isClient) return;
        if(world.getBlockEntity(pos) instanceof IntegratedCircuitBlockEntity integratedCircuitBlockEntity && integratedCircuitBlockEntity.getCircuit() != null) {
            for (FlatDirection direction : FlatDirection.VALUES) {
                integratedCircuitBlockEntity.getCircuit().getContext().readExternalPower(direction);
            }
        }
        world.scheduleBlockTick(pos, state.getBlock(), 0);
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        this.updateTargets(world, pos);
        world.scheduleBlockTick(pos, state.getBlock(), 0);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (moved || state.isOf(newState.getBlock())) return;
        super.onStateReplaced(state, world, pos, newState, moved);
        this.updateTargets(world, pos);
    }
    
    public void updateTarget(World world, BlockPos pos, Direction direction) {
        BlockPos blockPos = pos.offset(direction);
        world.updateNeighbor(blockPos, this, pos);
        world.updateNeighborsExcept(blockPos, this, direction.getOpposite());
    }

    protected void updateTargets(World world, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            updateTarget(world, pos, direction);
        }
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if(!world.isClient() && player instanceof ServerPlayerEntity serverPlayerEntity && world.getBlockEntity(pos) instanceof IntegratedCircuitBlockEntity integratedCircuitBlockEntity) {
            ServerCircuit circuit = integratedCircuitBlockEntity.getCircuit();
            if(circuit == null) return ActionResult.FAIL;
            integratedCircuitBlockEntity.addEditor(serverPlayerEntity);
            new EditIntegratedCircuitS2CPacket(pos, integratedCircuitBlockEntity.getName(), CircuitSerializer.writeCircuit(circuit)).send(serverPlayerEntity);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        if (itemStack.hasCustomName() && world.getBlockEntity(pos) instanceof IntegratedCircuitBlockEntity integratedCircuitBlockEntity)
            integratedCircuitBlockEntity.setCustomName(itemStack.getName());
        this.updateTargets(world, pos);
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
    public boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    @Override
    public int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return state.getWeakRedstonePower(world, pos, direction);
    }

    @Override
    public int getWeakRedstonePower(BlockState state, BlockView view, BlockPos pos, Direction direction) {
        if(direction.getAxis().isVertical()) return 0;
        FlatDirection circuitDirection = FlatDirection.fromVanillaDirection(state, direction.getOpposite());
        if(view.getBlockEntity(pos) instanceof IntegratedCircuitBlockEntity integratedCircuitBlockEntity)
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
    public List<ItemStack> getDroppedStacks(BlockState state, LootContextParameterSet.Builder builder) {
        List<ItemStack> list = super.getDroppedStacks(state, builder);

        BlockEntity blockEntity = builder.getOptional(LootContextParameters.BLOCK_ENTITY);
        if (blockEntity instanceof IntegratedCircuitBlockEntity integratedCircuitBlockEntity) {
            Circuit circuit = integratedCircuitBlockEntity.getCircuit();

            if(circuit == null || circuit.isEmpty()) { // If it's empty, get rid of the NBT data so it stacks with other empty circuits
                for(ItemStack stack : list) {
                    stack.removeSubNbt(BlockItem.BLOCK_ENTITY_TAG_KEY);
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
            case WHITE -> IntegratedCircuit.WHITE_INTEGRATED_CIRCUIT_BLOCK;
            case ORANGE -> IntegratedCircuit.ORANGE_INTEGRATED_CIRCUIT_BLOCK;
            case MAGENTA -> IntegratedCircuit.MAGENTA_INTEGRATED_CIRCUIT_BLOCK;
            case LIGHT_BLUE -> IntegratedCircuit.LIGHT_BLUE_INTEGRATED_CIRCUIT_BLOCK;
            case YELLOW -> IntegratedCircuit.YELLOW_INTEGRATED_CIRCUIT_BLOCK;
            case LIME -> IntegratedCircuit.LIME_INTEGRATED_CIRCUIT_BLOCK;
            case PINK -> IntegratedCircuit.PINK_INTEGRATED_CIRCUIT_BLOCK;
            case GRAY -> IntegratedCircuit.GRAY_INTEGRATED_CIRCUIT_BLOCK;
            case LIGHT_GRAY -> IntegratedCircuit.LIGHT_GRAY_INTEGRATED_CIRCUIT_BLOCK;
            case CYAN -> IntegratedCircuit.CYAN_INTEGRATED_CIRCUIT_BLOCK;
            case PURPLE -> IntegratedCircuit.PURPLE_INTEGRATED_CIRCUIT_BLOCK;
            case BLUE -> IntegratedCircuit.BLUE_INTEGRATED_CIRCUIT_BLOCK;
            case BROWN -> IntegratedCircuit.BROWN_INTEGRATED_CIRCUIT_BLOCK;
            case GREEN -> IntegratedCircuit.GREEN_INTEGRATED_CIRCUIT_BLOCK;
            case RED -> IntegratedCircuit.RED_INTEGRATED_CIRCUIT_BLOCK;
            case BLACK -> IntegratedCircuit.BLACK_INTEGRATED_CIRCUIT_BLOCK;
        };
    }
}
