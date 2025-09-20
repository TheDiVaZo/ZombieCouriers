package me.thedivazo.zombiecouriers.ai;

import me.thedivazo.zombiecouriers.capability.state.State;
import me.thedivazo.zombiecouriers.capability.state.StateContainerManager;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;

public abstract class CourierStateGoal extends Goal {
    protected final MobEntity entity;
    protected final State goalState;

    protected CourierStateGoal(MobEntity entity, State goalState) {
        this.entity = entity;
        this.goalState = goalState;
    }

    protected boolean hasState(State supposedState) {
        return StateContainerManager.getCourierState(entity)
                .map(stateContainer -> stateContainer.getState() == supposedState)
                .orElse(false);
    }

    protected void setState(State newState) {
        StateContainerManager.getCourierState(entity).ifPresent(stateContainer -> stateContainer.setState(newState));
    }

    @Override
    public final boolean canContinueToUse() {
        if (!hasState(goalState)) return false;
        return isContinueToUse();
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public final boolean canUse() {
        if (!hasState(goalState)) return false;
        return isUse();
    }

    protected abstract boolean isUse();

    protected abstract boolean isContinueToUse();
}
