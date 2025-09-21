package me.thedivazo.zombiecouriers.ai.courier;

import me.thedivazo.zombiecouriers.ai.StateMachine;
import me.thedivazo.zombiecouriers.capability.state.State;
import net.minecraft.entity.CreatureEntity;

public class FirstStateSetGoal extends CourierStateGoal{
    public FirstStateSetGoal(CreatureEntity entity, StateMachine stateMachine) {
        super(entity, stateMachine, null);
    }

    @Override
    public void tick() {
        stateMachine.setState(State.FIND_VILLAGE);
    }

    @Override
    protected boolean onUse() {
        return true;
    }

    @Override
    protected boolean onContinueToUse() {
        return true;
    }
}
