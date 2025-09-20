package me.thedivazo.zombiecouriers.capability;

import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;

public class SimpleCapabilityStorage<T extends INBT, C extends INBTSerializable<T>> implements Capability.IStorage<C> {
    @Nullable
    @Override
    public INBT writeNBT(Capability<C> capability, C instance, Direction side) {
        return instance.serializeNBT();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void readNBT(Capability<C> capability, C instance, Direction side, INBT nbt) {
        instance.deserializeNBT((T) nbt);
    }


}
