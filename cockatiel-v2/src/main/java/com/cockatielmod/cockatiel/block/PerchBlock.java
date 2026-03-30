package com.cockatielmod.cockatiel.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PerchBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<PerchBlock> CODEC = simpleCodec(PerchBlock::new);

    // Pole: 7-9 x 0-14 z 7-9, Bar along Z: 7-9 x 12-14 z 2-14
    private static final VoxelShape POLE = Block.box(7, 0, 7, 9, 14, 9);
    private static final VoxelShape BAR_NS = Block.box(7, 12, 2, 9, 14, 14);
    private static final VoxelShape BAR_EW = Block.box(2, 12, 7, 14, 14, 9);
    private static final VoxelShape SHAPE_NS = Shapes.or(POLE, BAR_NS);
    private static final VoxelShape SHAPE_EW = Shapes.or(POLE, BAR_EW);

    public PerchBlock(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(FACING, net.minecraft.core.Direction.NORTH));
    }

    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec() { return CODEC; }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return switch (state.getValue(FACING)) {
            case EAST, WEST -> SHAPE_EW;
            default         -> SHAPE_NS;
        };
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState s, Rotation r) { return s.setValue(FACING, r.rotate(s.getValue(FACING))); }
    @Override
    public BlockState mirror(BlockState s, Mirror m)   { return s.rotate(m.getRotation(s.getValue(FACING))); }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(FACING); }
    @Override
    public RenderShape getRenderShape(BlockState s) { return RenderShape.MODEL; }
}
