package net.replaceitem.integratedcircuit;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.replaceitem.integratedcircuit.network.packet.EditIntegratedCircuitS2CPacket;
import net.replaceitem.integratedcircuit.util.FlatDirection;
import org.jetbrains.annotations.Nullable;

public class IntegratedCircuitBlock extends HorizontalFacingBlock implements BlockEntityProvider {
    protected static final VoxelShape SHAPE = VoxelShapes.union(
            Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 2.0, 16.0),
            Block.createCuboidShape(4.0, 2.0, 4.0, 12.0, 3.0, 12.0)
    );
    public IntegratedCircuitBlock() {
        super(AbstractBlock.Settings.create().breakInstantly().sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.DESTROY));
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
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        updateTargets(world, pos, state);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (moved || state.isOf(newState.getBlock())) {
            return;
        }
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
            new EditIntegratedCircuitS2CPacket(pos, integratedCircuitBlockEntity.getName(), integratedCircuitBlockEntity.circuit.toNbt()).send(serverPlayerEntity);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        if (itemStack.hasCustomName() && world.getBlockEntity(pos) instanceof IntegratedCircuitBlockEntity integratedCircuitBlockEntity) {
            integratedCircuitBlockEntity.setCustomName(itemStack.getName());
        }
        this.updateTargets(world, pos, state);
    }

    public int getInputPower(World world, BlockPos pos, FlatDirection dir) {
        BlockState circuitBlockState = world.getBlockState(pos);
        Direction facing = circuitBlockState.get(FACING);
        Direction direction = dir.rotated(FlatDirection.fromVanillaDirection(facing).toInt()).getVanillaDirection();
        BlockPos blockPos = pos.offset(direction);
        BlockState blockState = world.getBlockState(blockPos);
        int i = world.getEmittedRedstonePower(blockPos, direction);
        if (i >= 15) {
            return i;
        }
        return Math.max(i, blockState.isOf(Blocks.REDSTONE_WIRE) ? blockState.get(RedstoneWireBlock.POWER) : 0);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new IntegratedCircuitBlockEntity(pos, state);
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
    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        if(direction.getAxis().isVertical()) return 0;
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if(blockEntity instanceof IntegratedCircuitBlockEntity integratedCircuitBlockEntity) {
            Direction facing = state.get(FACING);
            FlatDirection newDir = FlatDirection.fromVanillaDirection(direction).rotated(-FlatDirection.fromVanillaDirection(facing).toInt());
            return integratedCircuitBlockEntity.getOutputStrength(newDir.getOpposite());
        }
        return 0;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if(type != IntegratedCircuit.INTEGRATED_CIRCUIT_BLOCK_ENTITY) return null;
        return (world1, pos, state1, blockEntity) -> {
            IntegratedCircuitBlockEntity integratedCircuitBlockEntity = (IntegratedCircuitBlockEntity) blockEntity;
            integratedCircuitBlockEntity.getCircuit().tick(world1, pos, state1, blockEntity);
        };
    }
}
