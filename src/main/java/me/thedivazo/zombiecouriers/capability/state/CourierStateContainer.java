package me.thedivazo.zombiecouriers.capability.state;

import net.minecraft.nbt.CompoundNBT;

public class CourierStateContainer implements ICourierStateContainer {
    private static final String STATE_NBT_KEY = "state";

    private State state = State.FIND_VILLAGE;

    @Override
    public State getState() {
        return state;
    }

    @Override
    public State setState(State state) {
        return this.state = state;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putString(STATE_NBT_KEY, state.name());
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        this.state = State.valueOf(nbt.getString(STATE_NBT_KEY));
    }
}
