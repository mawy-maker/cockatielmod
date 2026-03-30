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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class CockatielCageBlock extends BaseEntityBlock {
    public static final MapCodec<CockatielCageBlock> CODEC = simpleCodec(CockatielCageBlock::new);

    // EnumProperty<Direction> instead of DirectionProperty (1.21.1)
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty OCCUPIED = BooleanProperty.create("occupied");
    public static final BooleanProperty OPEN     = BooleanProperty.create("open");

    private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 16, 15);

    public CockatielCageBlock(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(OCCUPIED, false)
                .setValue(OPEN, false));
    }

    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    @Override
    public VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) { return SHAPE; }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    // 1.21.1: useWithoutItem (replaces use)
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof CockatielCageBlockEntity cage)) return InteractionResult.PASS;

        boolean wasOpen = state.getValue(OPEN);
        level.setBlock(pos, state.setValue(OPEN, !wasOpen), 3);

        if (wasOpen) {
            // Closing door → release bird
            if (cage.hasCockatiel()) {
                cage.releaseCockatiel(level, pos);
                player.displayClientMessage(Component.translatable("message.cockatiel.cage_released"), true);
            }
        } else {
            // Opening door → try to store nearby tamed bird
            CockatielEntity nearby = findTamedBird(level, pos, player);
            if (nearby != null) {
                cage.storeCockatiel(nearby);
                level.setBlock(pos, state.setValue(OCCUPIED, true).setValue(OPEN, false), 3);
                player.displayClientMessage(Component.translatable("message.cockatiel.cage_stored"), true);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Nullable
    private CockatielEntity findTamedBird(Level level, BlockPos pos, Player player) {
        var list = level.getEntitiesOfClass(CockatielEntity.class, new AABB(pos).inflate(3.0));
        for (var b : list)
            if (b.isTamed() && b.getOwnerUUID().map(u -> u.equals(player.getUUID())).orElse(false))
                return b;
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public BlockState rotate(BlockState s, Rotation r) { return s.setValue(FACING, r.rotate(s.getValue(FACING))); }
    @Override
    public BlockState mirror(BlockState s, Mirror m)   { return s.rotate(m.getRotation(s.getValue(FACING))); }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(FACING, OCCUPIED, OPEN);
    }

    @Override public RenderShape getRenderShape(BlockState s) { return RenderShape.MODEL; }

    @Nullable @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CockatielCageBlockEntity(pos, state);
    }

    @Nullable @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                   BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.COCKATIEL_CAGE.get(),
                CockatielCageBlockEntity::tick);
    }
}
