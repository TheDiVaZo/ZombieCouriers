package me.thedivazo.zombiecouriers.capability.village;

import me.thedivazo.zombiecouriers.util.Pair;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Deque;
import java.util.Set;

public interface IAttachedVillageContainer extends INBTSerializable<CompoundNBT> {
    boolean isSetVillageCenter();

    BlockPos getVillageCenter();

    Deque<Pair<BlockPos, Set<BlockPos>>> getHomeBlocksAndDoors();

    void setVillageCenter(BlockPos villageCenter);

    void setHomeBlocksAndDoors(Deque<Pair<BlockPos, Set<BlockPos>>> doors);
}
