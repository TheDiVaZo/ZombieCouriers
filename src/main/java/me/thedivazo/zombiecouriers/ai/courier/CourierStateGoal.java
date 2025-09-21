package me.thedivazo.zombiecouriers.ai.courier;

import me.thedivazo.zombiecouriers.ai.StateMachine;
import me.thedivazo.zombiecouriers.capability.state.State;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.goal.Goal;

public abstract class CourierStateGoal extends Goal {
    protected final CreatureEntity entity;
    protected final StateMachine stateMachine;
    protected final State goalState;

    protected CourierStateGoal(CreatureEntity entity, StateMachine stateMachine, State goalState) {
        this.entity = entity;
        this.stateMachine = stateMachine;
        this.goalState = goalState;
    }

    @Override
    public final boolean canContinueToUse() {
        if (!stateMachine.hasState(goalState) || !entity.isAlive()) return false;
        return onContinueToUse();
    }

    @Override
    public final boolean canUse() {
        if (!stateMachine.hasState(goalState) || !entity.isAlive()) return false;
        return onUse();
    }

    protected abstract boolean onUse();

    protected abstract boolean onContinueToUse();
}
