package com.cockatielmod.cockatiel.entity.ai;

import com.cockatielmod.cockatiel.entity.CockatielEntity;
import com.cockatielmod.cockatiel.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class CockatielSitOnPerchGoal extends Goal {
    private final CockatielEntity bird;
    private BlockPos target;
    private int cooldown;

    public CockatielSitOnPerchGoal(CockatielEntity bird) {
        this.bird = bird;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (cooldown-- > 0) return false;
        target = findPerch();
        return target != null;
    }

    @Override
    public boolean canContinueToUse() {
        return target != null
                && bird.level().getBlockState(target).is(ModBlocks.PERCH.get())
                && !bird.blockPosition().closerThan(target, 1.5);
    }

    @Override
    public void start() {
        if (target != null)
            bird.getNavigation().moveTo(target.getX() + 0.5, target.getY() + 1.0, target.getZ() + 0.5, 1.0);
    }

    @Override
    public void stop() {
        cooldown = 300;
        target = null;
    }

    private BlockPos findPerch() {
        BlockPos center = bird.blockPosition();
        int r = 16;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-r, -4, -r), center.offset(r, 4, r))) {
            if (bird.level().getBlockState(pos).is(ModBlocks.PERCH.get()))
                return pos.immutable();
        }
        return null;
    }
}
