package me.thedivazo.zombiecouriers.capability.state;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

public interface ICourierStateContainer extends INBTSerializable<CompoundNBT> {
    State getState();
    State setState(State state);
}
