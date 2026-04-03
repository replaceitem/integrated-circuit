package net.replaceitem.integratedcircuit;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.redstone.ExperimentalRedstoneUtils;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.replaceitem.integratedcircuit.circuit.Circuit;
import net.replaceitem.integratedcircuit.circuit.CircuitSerializer;
import net.replaceitem.integratedcircuit.circuit.ServerCircuit;
import net.replaceitem.integratedcircuit.network.packet.EditIntegratedCircuitS2CPacket;
import net.replaceitem.integratedcircuit.util.FlatDirection;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class IntegratedCircuitBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final MapCodec<IntegratedCircuitBlock> CODEC = simpleCodec(IntegratedCircuitBlock::new);
    protected static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0);

    public IntegratedCircuitBlock(Properties settings) {
        super(settings);
        registerDefaultState(this.getStateDefinition().any().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
    }

    public static List<BlockTintSource> createBlockTintSources() {
        return Arrays.stream(FlatDirection.VALUES).map(IntegratedCircuitBlock::createBlockTintSource).toList();
    }

    private static BlockTintSource createBlockTintSource(FlatDirection circuitDirection) {
        return new BlockTintSource() {
            @Override
            public int color(BlockState state) {
                return RedStoneWireBlock.getColorForPower(0);
            }

            @Override
            public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
                return RedStoneWireBlock.getColorForPower(getPower(level, pos));
            }

            public int getPower(BlockAndTintGetter level, BlockPos pos) {
                if(level.getBlockEntity(pos) instanceof IntegratedCircuitBlockEntity integratedCircuitBlockEntity) {
                    return integratedCircuitBlockEntity.getPortRenderStrength(circuitDirection);
                }
                return 0;
            }
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateManager) {
        stateManager.add(BlockStateProperties.HORIZONTAL_FACING);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        return Block.canSupportRigidBlock(world, pos.below());
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, ctx.getHorizontalDirection());
    }
    
    @Override
    protected void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if(world.getBlockEntity(pos) instanceof IntegratedCircuitBlockEntity integratedCircuitBlockEntity && integratedCircuitBlockEntity.getCircuit() != null) {
            integratedCircuitBlockEntity.getCircuit().tick();
        }
        world.scheduleTick(pos, state.getBlock(), 1);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader world, ScheduledTickAccess tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (direction == Direction.DOWN && !this.canSurvive(state, world, pos))
            return Blocks.AIR.defaultBlockState();
        return super.updateShape(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }


    @Override
    protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block sourceBlock, @Nullable Orientation wireOrientation, boolean notify) {
        super.neighborChanged(state, world, pos, sourceBlock, wireOrientation, notify);
        if(world.isClientSide()) return;
        if(world.getBlockEntity(pos) instanceof IntegratedCircuitBlockEntity integratedCircuitBlockEntity && integratedCircuitBlockEntity.getCircuit() != null) {
            for (FlatDirection direction : FlatDirection.VALUES) {
                integratedCircuitBlockEntity.getCircuit().getContext().readExternalPower(direction);
            }
        }
        ensureTicking(world, pos);    
    }

    @Override
    protected void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean notify) {
        this.updateTargets(world, pos);
        ensureTicking(world, pos);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean moved) {
        if (moved) return;
        this.updateTargets(world, pos);
    }
    
    private void ensureTicking(Level world, BlockPos pos) {
        if(!world.getBlockTicks().hasScheduledTick(pos, this)) {
            world.scheduleTick(pos, this, 0);
        }
    }
    
    public void updateTarget(Level world, BlockPos pos, Direction direction) {
        BlockPos blockPos = pos.relative(direction);
        Orientation wireOrientation = ExperimentalRedstoneUtils.initialOrientation(world, direction, Direction.UP);
        world.neighborChanged(blockPos, this, wireOrientation);
        world.updateNeighborsAtExceptFromFacing(blockPos, this, direction.getOpposite(), wireOrientation);
    }

    protected void updateTargets(Level world, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            updateTarget(world, pos, direction);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (!world.isClientSide() && player instanceof ServerPlayer serverPlayerEntity && world.getBlockEntity(pos) instanceof IntegratedCircuitBlockEntity integratedCircuitBlockEntity) {
            ServerCircuit circuit = integratedCircuitBlockEntity.getCircuit();

            if (circuit == null)
                return InteractionResult.FAIL;

            DataResult<Tag> circuitNbt = CircuitSerializer.writeCircuit(circuit);

            if (circuitNbt.error().isPresent()) {
                IntegratedCircuit.LOGGER.error(circuitNbt.error().get().message());
                return InteractionResult.FAIL;
            }

            Tag nbtElement = circuitNbt.result().orElseThrow();

            if (!(nbtElement instanceof CompoundTag compound))
                return InteractionResult.FAIL;

            integratedCircuitBlockEntity.addEditor(serverPlayerEntity);

            Component customName = integratedCircuitBlockEntity.getCustomName();

            if (customName == null) {
                customName = Component.empty();
            }

            ServerPlayNetworking.send(
                serverPlayerEntity,
                new EditIntegratedCircuitS2CPacket(
                    pos,
                    customName,
                    compound
                )
            );
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        if (itemStack.has(DataComponents.CUSTOM_NAME) && world.getBlockEntity(pos) instanceof IntegratedCircuitBlockEntity integratedCircuitBlockEntity)
            integratedCircuitBlockEntity.setCustomName(itemStack.getHoverName());
        this.updateTargets(world, pos);
        world.neighborChanged(pos, this, null);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new IntegratedCircuitBlockEntity(pos, state);
    }

    public int getInputPower(SignalGetter view, BlockPos pos, BlockState state, FlatDirection dir) {
        Direction direction = dir.toVanillaDirection(state);

        BlockPos blockPos = pos.relative(direction);
        BlockState blockState = view.getBlockState(blockPos);

        int i = view.getSignal(blockPos, direction);
        if (i >= 15) {
            return i;
        }
        return Math.max(i, blockState.is(Blocks.REDSTONE_WIRE) ? blockState.getValue(RedStoneWireBlock.POWER) : 0);
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
        return state.getSignal(world, pos, direction);
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter view, BlockPos pos, Direction direction) {
        if(direction.getAxis().isVertical()) return 0;
        FlatDirection circuitDirection = FlatDirection.fromVanillaDirection(state, direction.getOpposite());
        if(view.getBlockEntity(pos) instanceof IntegratedCircuitBlockEntity integratedCircuitBlockEntity && integratedCircuitBlockEntity.getCircuit() != null)
            return integratedCircuitBlockEntity.getCircuit().getPortOutputStrength(circuitDirection);
        return 0;
    }

    @Override
    public BlockState playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof IntegratedCircuitBlockEntity integratedCircuitBlockEntity) {
            ServerCircuit circuit = integratedCircuitBlockEntity.getCircuit();
            if (!world.isClientSide() && player.isCreative() && circuit != null && !circuit.isEmpty()) {
                dropResources(state, world, pos, blockEntity, player, player.getMainHandItem());
            }
        }
        return super.playerWillDestroy(world, pos, state, player);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> list = super.getDrops(state, builder);

        BlockEntity blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof IntegratedCircuitBlockEntity integratedCircuitBlockEntity) {
            Circuit circuit = integratedCircuitBlockEntity.getCircuit();

            if(circuit == null || circuit.isEmpty()) { // If it's empty, get rid of the BlockEntityData, so it stacks with other empty circuits
                for(ItemStack stack : list) {
                    stack.remove(DataComponents.BLOCK_ENTITY_DATA);
                }
            }
        }
        return list;
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
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
