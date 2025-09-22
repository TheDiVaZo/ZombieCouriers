package me.thedivazo.zombiecouriers.capability.village;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Deque;
import java.util.List;

public interface IAttachedVillageContainer extends INBTSerializable<CompoundNBT> {
    boolean isSetVillageCenter();

    BlockPos getVillageCenter();

    Deque<BlockPos> getActiveDoors();

    void setVillageCenter(BlockPos villageCenter);

    void setActiveDoors(Deque<BlockPos> doors);
}
