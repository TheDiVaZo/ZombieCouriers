package me.thedivazo.zombiecouriers.ai.courier;

import lombok.Getter;
import lombok.Setter;
import me.thedivazo.zombiecouriers.ai.StateMachine;
import me.thedivazo.zombiecouriers.capability.state.State;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.util.math.BlockPos;

import java.util.EnumSet;

public abstract class CourierMoveGoal extends CourierStateGoal {
    protected final double acceptedDistance;
    protected final double speedModifier;
    @Setter
    @Getter
    private BlockPos target;

    private final int recalculateTick = 15;
    private int currentRecalculateTick = 0;

    protected CourierMoveGoal(
            CreatureEntity entity,
            StateMachine stateMachine,
            State goalState,
            double acceptedDistance,
            double speedModifier
    ) {
        super(entity, stateMachine, goalState);
        this.acceptedDistance = acceptedDistance;
        this.speedModifier = speedModifier;
        setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
    }

    public boolean recalculatePath() {
        return moveToTarget();
    }

    public boolean tryRecalculatePath() {
        if (currentRecalculateTick == 0) {
            return recalculatePath();
        }

        if (currentRecalculateTick > recalculateTick) {
            currentRecalculateTick = 0;
        }
        else {
            currentRecalculateTick++;
        }
        return false;
    }

    public boolean moveToTarget() {
        if (target == null) return false;
        return entity.getNavigation().moveTo(
                target.getX() + 0.5D,
                target.getY() + 0.5D + 0.5D,
                target.getZ() + 0.5D,
                1.0D
        );
    }

    public void stopMove() {
        if (!entity.getNavigation().isDone()) {
            entity.getNavigation().stop();
        }
    }

    public abstract void arrived();

    @Override
    public void tick() {
        if (isCloserThan()) {
            stopMove();
            arrived();
        }
        tryRecalculatePath();
    }

    public boolean isCloserThan() {
        BlockPos blockpos = this.getTarget();
        if (blockpos == null) return false;
        return blockpos.closerThan(entity.position(), this.acceptedDistance);
    }

    @Override
    public void stop() {
        stopMove();
    }
}
