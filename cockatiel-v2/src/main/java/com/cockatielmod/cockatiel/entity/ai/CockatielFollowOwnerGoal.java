package com.cockatielmod.cockatiel.entity.ai;

import com.cockatielmod.cockatiel.entity.CockatielEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.PathType;

import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;

public class CockatielFollowOwnerGoal extends Goal {
    private final CockatielEntity bird;
    private final float speed;
    private final float startDist;
    private final float stopDist;
    private Player owner;
    private int recalcTimer;

    public CockatielFollowOwnerGoal(CockatielEntity bird, float speed, float startDist, float stopDist) {
        this.bird = bird;
        this.speed = speed;
        this.startDist = startDist;
        this.stopDist = stopDist;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!bird.isTamed()) return false;
        Optional<UUID> uid = bird.getOwnerUUID();
        if (uid.isEmpty()) return false;
        owner = bird.level().getPlayerByUUID(uid.get());
        if (owner == null) return false;
        return bird.distanceToSqr(owner) >= startDist * startDist;
    }

    @Override
    public boolean canContinueToUse() {
        return owner != null && owner.isAlive()
                && bird.distanceToSqr(owner) > stopDist * stopDist;
    }

    @Override
    public void start() {
        recalcTimer = 0;
        bird.setPathfindingMalus(PathType.WATER, -1f);
    }

    @Override
    public void stop() {
        owner = null;
        bird.getNavigation().stop();
        bird.setPathfindingMalus(PathType.WATER, 0f);
    }

    @Override
    public void tick() {
        if (owner == null) return;
        bird.getLookControl().setLookAt(owner, 10f, bird.getMaxHeadXRot());
        if (--recalcTimer <= 0) {
            recalcTimer = 10;
            bird.getNavigation().moveTo(owner, speed);
        }
    }
}
