package me.thedivazo.zombiecouriers.capability.state;

import net.minecraft.nbt.CompoundNBT;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CourierStateContainer implements ICourierStateContainer {
    private static final String STATE_NBT_KEY = "state";
    private static final Logger LOGGER = LogManager.getLogger();

    private State state = null;

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
        try {
            state = State.valueOf(nbt.getString(STATE_NBT_KEY));
        } catch (IllegalArgumentException e) {
            state = null;
            LOGGER.warn("Invalid nbt key: {}", nbt.getString(STATE_NBT_KEY), e);
        }
    }
}
