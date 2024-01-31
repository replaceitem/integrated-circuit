package net.replaceitem.integratedcircuit;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.*;
import net.replaceitem.integratedcircuit.circuit.Circuit;
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
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (direction == Direction.DOWN && !this.canPlaceAt(state, world, pos))
            return Blocks.AIR.getDefaultState();
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        this.updateTargets(world, pos, state);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (moved || state.isOf(newState.getBlock())) return;
        super.onStateReplaced(state, world, pos, newState, moved);
        this.updateTargets(world, pos, state);
    }

    protected void updateTargets(World world, BlockPos pos, BlockState state) {
        for (Direction direction : Direction.values()) {
            BlockPos blockPos = pos.offset(direction.getOpposite());
            world.updateNeighbor(blockPos, this, pos);
            world.updateNeighborsExcept(blockPos, this, direction);
        }
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if(!world.isClient() && player instanceof ServerPlayerEntity serverPlayerEntity && world.getBlockEntity(pos) instanceof IntegratedCircuitBlockEntity integratedCircuitBlockEntity) {
            integratedCircuitBlockEntity.addEditor(serverPlayerEntity);
            new EditIntegratedCircuitS2CPacket(pos, integratedCircuitBlockEntity.getName(), integratedCircuitBlockEntity.getCircuit().toNbt()).send(serverPlayerEntity);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        if (itemStack.hasCustomName() && world.getBlockEntity(pos) instanceof IntegratedCircuitBlockEntity integratedCircuitBlockEntity)
            integratedCircuitBlockEntity.setCustomName(itemStack.getName());
        this.updateTargets(world, pos, state);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new IntegratedCircuitBlockEntity(pos, state);
    }
    public int getInputPower(RedstoneView view, BlockPos pos, FlatDirection dir) {
        BlockState circuitBlockState = view.getBlockState(pos);
        Direction direction = dir.toVanillaDirection(circuitBlockState);

        BlockPos blockPos = pos.offset(direction);
        BlockState blockState = view.getBlockState(blockPos);

        int i = view.getEmittedRedstonePower(blockPos, direction);
        if (i >= 15) {
            return i;
        }
        return Math.max(i, blockState.isOf(Blocks.REDSTONE_WIRE) ? blockState.get(RedstoneWireBlock.POWER) : 0);
    }
    public int getOutputPower(BlockView view, BlockPos pos, FlatDirection dir) {
        if(view.getBlockEntity(pos) instanceof IntegratedCircuitBlockEntity integratedCircuitBlockEntity)
            return integratedCircuitBlockEntity.getOutputStrength(dir);
        return 0;
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
        return getOutputPower(view, pos, FlatDirection.fromVanillaDirection(state, direction.getOpposite()));
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if(world.isClient() || type != IntegratedCircuit.INTEGRATED_CIRCUIT_BLOCK_ENTITY) return null;
        return (world1, pos, state1, blockEntity) -> {
            if(!world.isChunkLoaded(pos) || !world.getBlockState(pos).isIn(IntegratedCircuit.INTEGRATED_CIRCUITS_BLOCK_TAG)) return;
            if(blockEntity instanceof IntegratedCircuitBlockEntity integratedCircuitBlockEntity) {
                integratedCircuitBlockEntity.getCircuit().tick(world1, pos, state1, blockEntity);
            }
        };
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof IntegratedCircuitBlockEntity integratedCircuitBlockEntity) {
            if (!world.isClient && player.isCreative() && !integratedCircuitBlockEntity.getCircuit().isEmpty()) {
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

            if(circuit.isEmpty()) { // If it's empty, get rid of the NBT data so it stacks with other empty circuits
                for(ItemStack stack : list) {
                    stack.removeSubNbt(BlockItem.BLOCK_ENTITY_TAG_KEY);
                }
            }
        }
        return list;
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
            default -> IntegratedCircuit.INTEGRATED_CIRCUIT_BLOCK;
        };
    }
}
