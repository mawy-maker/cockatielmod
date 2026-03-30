package com.cockatielmod.cockatiel.entity.ai;

import com.cockatielmod.cockatiel.entity.CockatielEntity;
import com.cockatielmod.cockatiel.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.Block;

import java.util.EnumSet;

public class CockatielSitOnPerchGoal extends Goal {
    private final CockatielEntity cockatiel;
    private BlockPos targetPerch;
    private int searchCooldown;

    public CockatielSitOnPerchGoal(CockatielEntity cockatiel) {
        this.cockatiel = cockatiel;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (searchCooldown > 0) {
            searchCooldown--;
            return false;
        }
        // Look for a nearby perch
        targetPerch = findNearbyPerch();
        return targetPerch != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (targetPerch == null) return false;
        return !cockatiel.blockPosition().equals(targetPerch) &&
               cockatiel.level().getBlockState(targetPerch).is(ModBlocks.PERCH.get());
    }

    @Override
    public void start() {
        if (targetPerch != null) {
            cockatiel.getNavigation().moveTo(
                    targetPerch.getX() + 0.5,
                    targetPerch.getY() + 1.0,
                    targetPerch.getZ() + 0.5,
                    1.0
            );
        }
    }

    @Override
    public void stop() {
        searchCooldown = 300; // 15 seconds before searching again
        targetPerch = null;
    }

    private BlockPos findNearbyPerch() {
        BlockPos center = cockatiel.blockPosition();
        int radius = 16;
        Block perchBlock = ModBlocks.PERCH.get();

        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-radius, -4, -radius),
                center.offset(radius, 4, radius))) {
            if (cockatiel.level().getBlockState(pos).is(perchBlock)) {
                return pos.immutable();
            }
        }
        return null;
    }
}
