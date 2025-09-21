package me.thedivazo.zombiecouriers.ai;

import me.thedivazo.zombiecouriers.capability.state.Event;
import me.thedivazo.zombiecouriers.capability.state.State;
import me.thedivazo.zombiecouriers.capability.state.StateContainerManager;
import net.minecraft.entity.MobEntity;

import java.util.Arrays;

public class StateMachine {
    private final MobEntity entity;

    private final EventAction eventAction;

    public StateMachine(MobEntity entity, EventAction... eventActions) {
        this.entity = entity;

        EventAction[] copiedEventAction = Arrays.copyOf(eventActions, eventActions.length);
        this.eventAction = (curEntity, event) -> {
            for (EventAction transition : copiedEventAction) {
                transition.doAction(curEntity, event);
            }
        };
    }

    public interface EventAction {
        void doAction(MobEntity entity, Event event);
    }

    public boolean hasState(State supposedState) {
        return StateContainerManager.getCourierState(entity)
                .map(stateContainer -> stateContainer.getState() == supposedState)
                .orElse(false);
    }

    public void setState(State newState) {
        StateContainerManager.getCourierState(entity).ifPresent(stateContainer ->{
            stateContainer.setState(newState);
        });
    }

    public void sendEvent(Event event) {
        eventAction.doAction(entity, event);
    }
}
