package me.thedivazo.zombiecouriers.capability.village;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayDeque;
import java.util.Deque;

public class AttachedVillageContainer implements IAttachedVillageContainer {
    private static final String CENTER_NBT_KEY = "Center";
    private final String ACTIVE_DOORS_NBT_KEY = "ActiveDoors";

    @Getter
    @Setter
    private BlockPos villageCenter;

    @Setter
    @Getter
    private Deque<BlockPos> activeDoors = new ArrayDeque<>();


    @Override
    public CompoundNBT serializeNBT() {
        ListNBT nbtTagList = new ListNBT();

        for (BlockPos activeDoor : activeDoors) {
            if (activeDoor == null) continue;

            CompoundNBT activeDoorTag = NBTUtil.writeBlockPos(activeDoor);
            nbtTagList.add(activeDoorTag);
        }

        CompoundNBT tag = new CompoundNBT();

        if (villageCenter != null) {
            tag.put(CENTER_NBT_KEY, NBTUtil.writeBlockPos(villageCenter));
        }
        tag.put(ACTIVE_DOORS_NBT_KEY, nbtTagList);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        activeDoors = new ArrayDeque<>();


        ListNBT nbtTagList = nbt.getList(ACTIVE_DOORS_NBT_KEY, Constants.NBT.TAG_COMPOUND);

        for (int i = 0; i < nbtTagList.size(); i++) {
            CompoundNBT activeDoor = nbtTagList.getCompound(i);
            activeDoors.add(NBTUtil.readBlockPos(activeDoor));
        }

        if (nbt.contains(CENTER_NBT_KEY)) {
            villageCenter = NBTUtil.readBlockPos(nbt.getCompound(CENTER_NBT_KEY));
        }
    }

    @Override
    public boolean isSetVillageCenter() {
        return villageCenter != null;
    }
}
