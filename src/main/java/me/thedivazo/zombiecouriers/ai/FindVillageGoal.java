package me.thedivazo.zombiecouriers.ai;

import me.thedivazo.zombiecouriers.capability.state.State;
import me.thedivazo.zombiecouriers.util.BlockPosFunction;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.GroundPathHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import org.apache.logging.log4j.LogManager;

import java.util.EnumSet;

public class FindVillageGoal extends CourierStateGoal {
    private final double distanceIsFound = 30;
    private final double distanceIsFoundSqr = distanceIsFound * distanceIsFound;
    private final int searchDistanceChunk = 40;
    private final int scanCooldownTick = 40;

    private int currentTickScan = 0;

    private BlockPos target = null;

    public FindVillageGoal(MobEntity entity) {
        super(entity, State.FIND_VILLAGE);
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean isUse() {
        if (!entity.isAlive() || !GroundPathHelper.hasGroundPathNavigation(entity)) return false;

        currentTickScan++;
        if (currentTickScan < scanCooldownTick) {
            return target != null && BlockPosFunction.distanceToSqr(entity, target) > distanceIsFoundSqr;
        }
        LogManager.getLogger().info("Scan tick" + currentTickScan);
        currentTickScan = 0;

        if (target != null && BlockPosFunction.distanceToSqr(entity, target) > distanceIsFoundSqr) {
            return true;
        }
        else if (target != null) {
            setState(State.FIND_GARDEN_BED);
            return false;
        }

        target = BlockPosFunction.getNearbyVillage((ServerWorld) entity.level, entity.blockPosition(), searchDistanceChunk);
        LogManager.getLogger().info("Target found: " + target);
        return target != null;
    }

    @Override
    public void start() {
        if (target != null && entity.isAlive()) {
            moveToTarget();
        }
    }

    @Override
    public boolean isContinueToUse() {
        if (target == null || !entity.isAlive()) return false;

        double currentDistanceSqr = BlockPosFunction.distanceToSqr(entity, target);
        return currentDistanceSqr > distanceIsFoundSqr;
    }

    @Override
    public void tick() {
        if (target == null) return;

        if (entity.getNavigation().isDone() || entity.getNavigation().getPath() == null) {
            moveToTarget();
        }
    }

    @Override
    public void stop() {
        if (target != null) {
            double distanceSqr = BlockPosFunction.distanceToSqr(entity, target);
            if (distanceSqr <= distanceIsFoundSqr) {
                setState(State.FIND_GARDEN_BED);
            }
        }
    }

    private void moveToTarget() {
        entity.getNavigation().moveTo(
                target.getX() + 0.5D,
                target.getY() + 0.5D,
                target.getZ() + 0.5D,
                1.0D
        );
    }
}
