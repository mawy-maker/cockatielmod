package com.cockatielmod.cockatiel.block;

import com.cockatielmod.cockatiel.blockentity.CockatielCageBlockEntity;
import com.cockatielmod.cockatiel.entity.CockatielEntity;
import com.cockatielmod.cockatiel.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class CockatielCageBlock extends BaseEntityBlock {
    public static final MapCodec<CockatielCageBlock> CODEC = simpleCodec(CockatielCageBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty OCCUPIED = BooleanProperty.create("occupied");
    public static final BooleanProperty OPEN = BooleanProperty.create("open");

    private static final VoxelShape CAGE_SHAPE = Block.box(1, 0, 1, 15, 16, 15);

    public CockatielCageBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(OCCUPIED, false)
                .setValue(OPEN, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return CAGE_SHAPE;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    // 1.21.1 uses useWithoutItem instead of use
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof CockatielCageBlockEntity cage) {
            boolean isOpen = state.getValue(OPEN);
            level.setBlock(pos, state.setValue(OPEN, !isOpen), 3);
            if (isOpen) {
                if (cage.hasCockatiel()) {
                    cage.releaseCockatiel(level, pos);
                    player.displayClientMessage(Component.translatable("message.cockatiel.cage_released"), true);
                }
            } else {
                CockatielEntity nearby = findNearbyCockatiel(level, pos, player);
                if (nearby != null) {
                    cage.storeCockatiel(nearby);
                    level.setBlock(pos, state.setValue(OCCUPIED, true).setValue(OPEN, false), 3);
                    player.displayClientMessage(Component.translatable("message.cockatiel.cage_stored"), true);
                }
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Nullable
    private CockatielEntity findNearbyCockatiel(Level level, BlockPos pos, Player player) {
        var list = level.getEntitiesOfClass(CockatielEntity.class,
                new net.minecraft.world.phys.AABB(pos).inflate(3.0));
        for (CockatielEntity bird : list)
            if (bird.isTamed() && bird.getOwnerUUID().map(u -> u.equals(player.getUUID())).orElse(false))
                return bird;
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation r) { return state.setValue(FACING, r.rotate(state.getValue(FACING))); }
    @Override
    public BlockState mirror(BlockState state, Mirror m) { return state.rotate(m.getRotation(state.getValue(FACING))); }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OCCUPIED, OPEN);
    }

    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Nullable @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CockatielCageBlockEntity(pos, state);
    }

    @Nullable @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                   BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.COCKATIEL_CAGE.get(), CockatielCageBlockEntity::tick);
    }
}
