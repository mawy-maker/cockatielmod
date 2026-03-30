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
    private final CockatielEntity bird;
    private BlockPos target;
    private int cooldown;

    public CockatielStealCropsGoal(CockatielEntity bird) {
        this.bird = bird;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (cooldown-- > 0) return false;
        long fed = bird.getLastFedTime();
        if (fed == 0) return false;
        long hunger = CockatielConfig.HUNGER_THRESHOLD_TICKS.get();
        if (bird.level().getGameTime() - fed < hunger) return false;
        if (bird.getRandom().nextDouble() > 0.02) return false;
        target = findRipeCrop();
        return target != null;
    }

    @Override
    public boolean canContinueToUse() {
        return target != null
                && isRipe(bird.level().getBlockState(target))
                && !bird.blockPosition().closerThan(target, 2.0);
    }

    @Override
    public void start() {
        if (target != null)
            bird.getNavigation().moveTo(target.getX() + 0.5, target.getY() + 1.0, target.getZ() + 0.5, 1.1);
    }

    @Override
    public void tick() {
        if (target != null && bird.blockPosition().closerThan(target, 2.0)) {
            if (isRipe(bird.level().getBlockState(target))) {
                bird.level().destroyBlock(target, true);
                cooldown = 600;
                target = null;
            }
        }
    }

    @Override
    public void stop() {
        if (cooldown <= 0) cooldown = 200;
        target = null;
    }

    private BlockPos findRipeCrop() {
        BlockPos c = bird.blockPosition();
        for (BlockPos p : BlockPos.betweenClosed(c.offset(-12, -3, -12), c.offset(12, 3, 12))) {
            if (isRipe(bird.level().getBlockState(p))) return p.immutable();
        }
        return null;
    }

    private boolean isRipe(BlockState state) {
        if (state.getBlock() instanceof CropBlock cb)
            return state.getValue(CropBlock.AGE) == cb.getMaxAge();
        if (state.is(Blocks.BEETROOTS))
            return state.getValue(BeetrootBlock.AGE) == 3;
        return false;
    }
}
