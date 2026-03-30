package com.cockatielmod.cockatiel.entity.ai;

import com.cockatielmod.cockatiel.entity.CockatielEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.PathType;

import java.util.EnumSet;
import java.util.UUID;

public class CockatielFollowOwnerGoal extends Goal {
    private final CockatielEntity cockatiel;
    private final double speedModifier;
    private final float startDistance;
    private final float stopDistance;
    private Player owner;
    private int timeToRecalcPath;

    public CockatielFollowOwnerGoal(CockatielEntity cockatiel, double speedModifier, float startDistance, float stopDistance) {
        this.cockatiel = cockatiel;
        this.speedModifier = speedModifier;
        this.startDistance = startDistance;
        this.stopDistance = stopDistance;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        if (!(cockatiel.getNavigation() instanceof FlyingPathNavigation)) {
            throw new IllegalArgumentException("Unsupported entity type for FollowOwnerGoal");
        }
    }

    @Override
    public boolean canUse() {
        if (!cockatiel.isTamed()) return false;
        java.util.Optional<UUID> ownerUUID = cockatiel.getOwnerUUID();
        if (ownerUUID.isEmpty()) return false;
        this.owner = cockatiel.level().getPlayerByUUID(ownerUUID.get());
        if (this.owner == null) return false;
        return cockatiel.distanceToSqr(owner) >= (double)(startDistance * startDistance);
    }

    @Override
    public boolean canContinueToUse() {
        if (owner == null || !owner.isAlive()) return false;
        return cockatiel.distanceToSqr(owner) > (double)(stopDistance * stopDistance);
    }

    @Override
    public void start() {
        this.timeToRecalcPath = 0;
        cockatiel.setPathfindingMalus(PathType.WATER, -1.0f);
    }

    @Override
    public void stop() {
        this.owner = null;
        cockatiel.getNavigation().stop();
        cockatiel.setPathfindingMalus(PathType.WATER, 0.0f);
    }

    @Override
    public void tick() {
        cockatiel.getLookControl().setLookAt(owner, 10.0f, cockatiel.getMaxHeadXRot());
        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = 10;
            cockatiel.getNavigation().moveTo(owner, speedModifier);
        }
    }
}
