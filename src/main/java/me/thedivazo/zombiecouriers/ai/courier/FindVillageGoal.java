package me.thedivazo.zombiecouriers.ai.courier;

import me.thedivazo.zombiecouriers.ai.StateMachine;
import me.thedivazo.zombiecouriers.capability.state.Event;
import me.thedivazo.zombiecouriers.capability.state.State;
import me.thedivazo.zombiecouriers.util.BlockPosUtil;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

public class FindVillageGoal extends CourierMoveGoal {
    public FindVillageGoal(CreatureEntity entity, StateMachine stateMachine) {
        super(entity, stateMachine, State.FIND_VILLAGE, 40, 1d);
    }

    public BlockPos calculateTarget() {
        return BlockPosUtil.getNearbyVillage((ServerWorld) entity.level, entity.blockPosition(), 20);
    }

    @Override
    public void arrived() {
        stateMachine.setState(State.FARM_GARDEN_BED);
        stateMachine.sendEvent(Event.CHANGE_STATE);
    }

    @Override
    protected boolean onUse() {
        return true;
    }

    @Override
    protected boolean onContinueToUse() {
        return onUse();
    }

    @Override
    public void start() {
        stateMachine.sendEvent(Event.SEARCH_VILLAGE);
        setTarget(calculateTarget());
    }

    @Override
    public boolean tryRecalculatePath() {
        boolean isRecalculated = super.tryRecalculatePath();
        if (isRecalculated) {
            stateMachine.sendEvent(Event.GO_TO_VILLAGE);
        }
        return isRecalculated;
    }
}
