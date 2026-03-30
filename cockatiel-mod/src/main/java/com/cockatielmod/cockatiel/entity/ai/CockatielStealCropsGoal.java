package com.cockatielmod.cockatiel.entity.ai;

import com.cockatielmod.cockatiel.CockatielConfig;
import com.cockatielmod.cockatiel.entity.CockatielEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.BeetrootBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumSet;

public class CockatielStealCropsGoal extends Goal {
    private final CockatielEntity cockatiel;
    private BlockPos targetCrop;
    private int cooldown;

    public CockatielStealCropsGoal(CockatielEntity cockatiel) {
        this.cockatiel = cockatiel;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (cooldown > 0) { cooldown--; return false; }
        long currentTime = cockatiel.level().getGameTime();
        long lastFed = cockatiel.getLastFedTime();
        long hungerThreshold = CockatielConfig.HUNGER_THRESHOLD_TICKS.get();
        if (lastFed > 0 && currentTime - lastFed < hungerThreshold) return false;
        if (cockatiel.getRandom().nextDouble() > 0.01) return false;
        targetCrop = findNearbyRipeCrop();
        return targetCrop != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (targetCrop == null) return false;
        BlockState state = cockatiel.level().getBlockState(targetCrop);
        return isRipeCrop(state) && !cockatiel.blockPosition().closerThan(targetCrop, 2.0);
    }

    @Override
    public void start() {
        if (targetCrop != null) {
            cockatiel.getNavigation().moveTo(
                    targetCrop.getX() + 0.5, targetCrop.getY() + 1.0, targetCrop.getZ() + 0.5, 1.1);
        }
    }

    @Override
    public void tick() {
        if (targetCrop != null && cockatiel.blockPosition().closerThan(targetCrop, 2.0)) {
            BlockState state = cockatiel.level().getBlockState(targetCrop);
            if (isRipeCrop(state)) {
                cockatiel.level().destroyBlock(targetCrop, true);
                cooldown = 600;
                targetCrop = null;
            }
        }
    }

    @Override
    public void stop() {
        targetCrop = null;
        if (cooldown == 0) cooldown = 200;
    }

    private BlockPos findNearbyRipeCrop() {
        BlockPos center = cockatiel.blockPosition();
        int radius = 12;
        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-radius, -3, -radius), center.offset(radius, 3, radius))) {
            if (isRipeCrop(cockatiel.level().getBlockState(pos))) return pos.immutable();
        }
        return null;
    }

    private boolean isRipeCrop(BlockState state) {
        if (state.getBlock() instanceof CropBlock cropBlock) {
            return state.getValue(CropBlock.AGE) == cropBlock.getMaxAge();
        }
        if (state.is(Blocks.BEETROOTS)) {
            return state.getValue(BeetrootBlock.AGE) == 3;
        }
        return false;
    }
}
