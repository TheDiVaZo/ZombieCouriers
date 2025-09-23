package me.thedivazo.zombiecouriers.ai.courier;

import lombok.Getter;
import me.thedivazo.zombiecouriers.ai.Event;
import me.thedivazo.zombiecouriers.ai.StateMachine;
import me.thedivazo.zombiecouriers.capability.iventory.CourierInventoryManager;
import me.thedivazo.zombiecouriers.capability.iventory.ICourierInventory;
import me.thedivazo.zombiecouriers.capability.state.State;
import me.thedivazo.zombiecouriers.capability.village.AttachedVillageManager;
import me.thedivazo.zombiecouriers.capability.village.IAttachedVillageContainer;
import me.thedivazo.zombiecouriers.util.HomeSort;
import me.thedivazo.zombiecouriers.util.Pair;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.village.PointOfInterest;
import net.minecraft.village.PointOfInterestManager;
import net.minecraft.village.PointOfInterestType;
import net.minecraft.world.server.ServerWorld;

import java.util.*;
import java.util.stream.Collectors;

public class DistributionGoal extends CourierMoveGoal {

    private final int homeScanRadius = 110;
    private final int doorScanRadius = 7;

    private final Deque<Pair<BlockPos, Set<BlockPos>>> cacheHomeBlocksAndDoors = new ArrayDeque<>();

    @Getter
    private Pair<BlockPos, Set<BlockPos>> homeTarget = null;

    public DistributionGoal(CreatureEntity entity, StateMachine stateMachine) {
        super(entity, stateMachine, State.DISTRIBUTION, 0.8d, 1.25d);
    }

    private void setHomeBlocksAndDoors(Deque<Pair<BlockPos, Set<BlockPos>>> nextHomeBlocksAndDoors) {
        AttachedVillageManager
                .getAttachedVillage(entity)
                .ifPresent(village -> village.setHomeBlocksAndDoors(nextHomeBlocksAndDoors));
    }

    private Deque<Pair<BlockPos, Set<BlockPos>>> getHomesAndDoors() {
        Deque<Pair<BlockPos, Set<BlockPos>>> homeBlocksAndDoors = AttachedVillageManager
                .getAttachedVillage(entity)
                .map(IAttachedVillageContainer::getHomeBlocksAndDoors)
                .orElse(null);
        if (homeBlocksAndDoors == null) {
            setHomeBlocksAndDoors(cacheHomeBlocksAndDoors);
            homeBlocksAndDoors = cacheHomeBlocksAndDoors;
        }

        return homeBlocksAndDoors;
    }

    public void setHomeTarget(Pair<BlockPos, Set<BlockPos>> homeTarget) {
        BlockPos target;
        if (homeTarget == null) {
            target = null;
        }
        else {
            target = homeTarget.getSecond()
                    .stream()
                    .findAny()
                    .orElse(homeTarget.getFirst());
        }
        super.setTarget(target);
        this.homeTarget = homeTarget;
    }

    @Override
    public boolean isCloserThan() {
        Pair<BlockPos, Set<BlockPos>> homeTarget = getHomeTarget();
        if (homeTarget == null) return false;
        for (BlockPos door : homeTarget.getSecond()) {
            if (door.closerThan(entity.position(), this.acceptedDistance)) {
                setTarget(door);
                return true;
            }
        }
        return super.isCloserThan();
    }

    @Override
    public void arrived() {
        ItemEntity dropItemEntity = getDropFromInventory();

        entity.level.addFreshEntity(dropItemEntity);

        getHomesAndDoors().remove();
        setHomeTarget(getHomesAndDoors().peek());

        if (isEmptyInventory()) {
            nextStage();
        }
    }

    private boolean isEmptyInventory() {
        return CourierInventoryManager
                .getCourierInventory(entity)
                .map(inventory -> inventory.getCount() <= 0)
                .orElse(true);
    }

    private ItemEntity getDropFromInventory() {
        BlockPos currentDoor = getTarget();
        ItemStack dropItemStack = CourierInventoryManager
                .getCourierInventory(entity)
                .map(ICourierInventory::pollOne)
                .orElse(ItemStack.EMPTY);

        ItemEntity dropItemEntity = new ItemEntity(entity.level, currentDoor.getX() + 0.5, currentDoor.getY() + 0.5, currentDoor.getZ() + 0.5, dropItemStack);
        dropItemEntity.setNoPickUpDelay();
        dropItemEntity.setDeltaMovement(Vector3d.ZERO);
        return dropItemEntity;
    }

    @Override
    public boolean recalculatePath() {
        if (getHomesAndDoors().isEmpty()) {
            refillQueue();
            setHomeTarget(getHomesAndDoors().peek());
        }
        else if (getTarget() == null) {
            setHomeTarget(getHomesAndDoors().peek());
        }

        boolean pathIsRecalculated = super.recalculatePath();
        if (pathIsRecalculated) {
            stateMachine.sendEvent(Event.GO_TO_NEXT_DOOR);
        }
        else {
            stateMachine.sendEvent(Event.SEARCH_DOOR);
        }

        return pathIsRecalculated;
    }

    @Override
    protected boolean onUse() {
        return tryRecalculatePath();
    }

    @Override
    protected boolean onContinueToUse() {
        return !getHomesAndDoors().isEmpty();
    }

    public void nextStage() {
        stateMachine.setState(State.FARM_GARDEN_BED);
        stateMachine.sendEvent(Event.CHANGE_STATE);
    }

    public void refillQueue() {
        Set<BlockPos> bedsAndCraftersPos = getAllBedsAndCrafters();

        List<Pair<BlockPos, Set<BlockPos>>> foundHomes = filteringByHomesAndGet(bedsAndCraftersPos);
        if (foundHomes.isEmpty()) return;

        Collection<Pair<BlockPos, Set<BlockPos>>> sorted = HomeSort.sortByNearest(foundHomes, getStartPosition());

        getHomesAndDoors().addAll(sorted);
    }

    private BlockPos getStartPosition() {
        return AttachedVillageManager.getAttachedVillage(entity)
                .filter(IAttachedVillageContainer::isSetVillageCenter)
                .map(IAttachedVillageContainer::getVillageCenter)
                .orElse(entity.blockPosition());
    }

    private Set<BlockPos> getAllBedsAndCrafters() {
        ServerWorld serverWorld = (ServerWorld) entity.level;

        PointOfInterestManager poi = serverWorld.getPoiManager();

        return poi.getInRange(
                target ->
                        target == PointOfInterestType.HOME ||
                                PointOfInterestType.ALL_JOBS.test(target) ||
                                target == PointOfInterestType.MEETING ||
                                target == PointOfInterestType.UNEMPLOYED ||
                                target == PointOfInterestType.MASON,
                AttachedVillageManager.getAttachedVillage(entity)
                        .map(IAttachedVillageContainer::getVillageCenter)
                        .orElse(entity.blockPosition()),
                homeScanRadius,
                PointOfInterestManager.Status.ANY
        ).map(PointOfInterest::getPos).collect(Collectors.toSet());
    }

    private List<Pair<BlockPos, Set<BlockPos>>> filteringByHomesAndGet(Set<BlockPos> bedsAndCrafters) {
        ServerWorld serverWorld = (ServerWorld) entity.level;

        List<Pair<BlockPos, Set<BlockPos>>> result = new ArrayList<>();

        for (BlockPos block : bedsAndCrafters) {
            Set<BlockPos> doors = findNearestDoorAround(serverWorld, block, doorScanRadius);
            if (!doors.isEmpty()) {
                result.add(new Pair<>(block, doors));
            }
        }

        return result;
    }

    private static Set<BlockPos> findNearestDoorAround(ServerWorld world, BlockPos center, int radius) {
        Set<BlockPos> result = new HashSet<>();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos blockPos = center.offset(dx, dy, dz);
                    BlockState state = world.getBlockState(blockPos);
                    if (!(state.getBlock() instanceof DoorBlock)) continue;

                    if (state.getValue(DoorBlock.HALF) != DoubleBlockHalf.LOWER) continue;

                    result.add(blockPos);
                }
            }
        }
        return result;
    }

    @Override
    public boolean moveToTarget() {
        if (getTarget() == null) return false;
        return entity.getNavigation().moveTo(
                entity.getNavigation().createPath(getTarget(), 2),
                1.0D
        );
    }
}
