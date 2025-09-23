package me.thedivazo.zombiecouriers.capability.village;

import lombok.Getter;
import lombok.Setter;
import me.thedivazo.zombiecouriers.util.Pair;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

public class AttachedVillageContainer implements IAttachedVillageContainer {
    private static final String CENTER_NBT_KEY = "Center";

    private static final String HOME_NBT_KEY = "Home";
    private static final String HOME_BLOCK_NBT_KEY = "HomeBlock";
    private static final String ACTIVE_DOORS_NBT_KEY = "ActiveDoors";

    @Getter
    @Setter
    private BlockPos villageCenter;

    @Setter
    @Getter
    private Deque<Pair<BlockPos, Set<BlockPos>>> homeBlocksAndDoors = new ArrayDeque<>();


    @Override
    public CompoundNBT serializeNBT() {
        ListNBT homesTagList = new ListNBT();

        for (Pair<BlockPos, Set<BlockPos>> homeBlockAndDoors : homeBlocksAndDoors) {
            if (homeBlockAndDoors == null) continue;

            CompoundNBT homeBlockAndDoorsTag = new CompoundNBT();

            homeBlockAndDoorsTag.put(
                    HOME_BLOCK_NBT_KEY,
                    NBTUtil.writeBlockPos(homeBlockAndDoors.getFirst())
            );

            ListNBT activeDoorsTag = new ListNBT();

            for (BlockPos door : homeBlockAndDoors.getSecond()) {
                activeDoorsTag.add(NBTUtil.writeBlockPos(door));
            }

            homeBlockAndDoorsTag.put(ACTIVE_DOORS_NBT_KEY, activeDoorsTag);

            homesTagList.add(homeBlockAndDoorsTag);
        }

        CompoundNBT tag = new CompoundNBT();

        if (villageCenter != null) {
            tag.put(CENTER_NBT_KEY, NBTUtil.writeBlockPos(villageCenter));
        }
        tag.put(HOME_NBT_KEY, homesTagList);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        homeBlocksAndDoors = new ArrayDeque<>();

        ListNBT homesTagList = nbt.getList(HOME_NBT_KEY, Constants.NBT.TAG_COMPOUND);

        for (int i = 0; i < homesTagList.size(); i++) {
            CompoundNBT homeBlockAndDoorsTag = homesTagList.getCompound(i);

            CompoundNBT homeBlockNbt = homeBlockAndDoorsTag.getCompound(HOME_BLOCK_NBT_KEY);
            ListNBT activeDoorsNbt = homeBlockAndDoorsTag.getList(ACTIVE_DOORS_NBT_KEY, Constants.NBT.TAG_COMPOUND);

            Set<BlockPos> doors = new HashSet<>();
            for (int doorIndex = 0; doorIndex < activeDoorsNbt.size(); doorIndex++) {
                doors.add(NBTUtil.readBlockPos(activeDoorsNbt.getCompound(doorIndex)));
            }

            Pair<BlockPos, Set<BlockPos>> homeBlockAndDoorsPair = new Pair<>(
                    NBTUtil.readBlockPos(homeBlockNbt),
                    doors
            );

            homeBlocksAndDoors.add(homeBlockAndDoorsPair);
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
