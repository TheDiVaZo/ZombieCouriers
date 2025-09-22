package me.thedivazo.zombiecouriers.ai.courier;

import me.thedivazo.zombiecouriers.ai.StateMachine;
import me.thedivazo.zombiecouriers.capability.iventory.CourierInventoryManager;
import me.thedivazo.zombiecouriers.capability.iventory.ICourierInventory;
import me.thedivazo.zombiecouriers.ai.Event;
import me.thedivazo.zombiecouriers.capability.state.State;
import me.thedivazo.zombiecouriers.capability.village.AttachedVillageManager;
import me.thedivazo.zombiecouriers.capability.village.IAttachedVillageContainer;
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
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import org.apache.logging.log4j.LogManager;

import java.util.*;
import java.util.stream.Collectors;

public class DistributionGoal extends CourierMoveGoal {

    private final int homeScanRadius = 110;
    private final int doorScanRadius = 7;

    private final Deque<BlockPos> cacheActiveDoors = new ArrayDeque<>();

    public DistributionGoal(CreatureEntity entity, StateMachine stateMachine) {
        super(entity, stateMachine, State.DISTRIBUTION, 0.8d, 1.25d);
    }

    private void setActiveDoors(Deque<BlockPos> nextDoors) {
        AttachedVillageManager
                .getAttachedVillage(entity)
                .ifPresent(village -> village.setActiveDoors(nextDoors));
    }

    private Deque<BlockPos> getActiveDoors() {
        Deque<BlockPos> activeDoors = AttachedVillageManager
                .getAttachedVillage(entity)
                .map(IAttachedVillageContainer::getActiveDoors)
                .orElse(null);
        if (activeDoors == null) {
            setActiveDoors(cacheActiveDoors);
            activeDoors = cacheActiveDoors;
        }

        return activeDoors;
    }

    @Override
    public void arrived() {
        BlockPos currentDoor = getTarget();

        ItemStack dropItemStack = CourierInventoryManager
                .getCourierInventory(entity)
                .map(ICourierInventory::pollOne)
                .orElse(ItemStack.EMPTY);

        ItemEntity dropItemEntity = new ItemEntity(entity.level, currentDoor.getX() + 0.5, currentDoor.getY() + 0.5, currentDoor.getZ() + 0.5, dropItemStack);
        dropItemEntity.setNoPickUpDelay();
        dropItemEntity.setDeltaMovement(Vector3d.ZERO);
        entity.level.addFreshEntity(dropItemEntity);
        stateMachine.sendEvent(Event.DROP_CROP);

        setTarget(pollNearestUnvisitedDoor());
        if (CourierInventoryManager
                .getCourierInventory(entity)
                .map(inventory -> inventory.getCount() <= 0)
                .orElse(true)
        ) {
            nextStage();
        }
    }

    private BlockPos pollNearestUnvisitedDoor() {
        getActiveDoors().poll();
        if (getActiveDoors().isEmpty()) return null;
        return getActiveDoors().peek();
    }

    @Override
    public boolean recalculatePath() {
        if (getTarget() == null && !getActiveDoors().isEmpty()) {
            setTarget(getActiveDoors().peek());
        }
        if (getActiveDoors().isEmpty()) {
            refillQueue();
            setTarget(pollNearestUnvisitedDoor());
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
        return !getActiveDoors().isEmpty();
    }

    public void nextStage() {
        stateMachine.setState(State.FARM_GARDEN_BED);
        stateMachine.sendEvent(Event.CHANGE_STATE);
    }

    public void refillQueue() {
        ServerWorld serverWorld = (ServerWorld) entity.level;
        PointOfInterestManager poi = serverWorld.getPoiManager();

        Set<BlockPos> homes = poi.getInRange(
                target ->
                        target == PointOfInterestType.HOME ||
                        PointOfInterestType.ALL_JOBS.test(target) ||
                        target == PointOfInterestType.MEETING ||
                        target == PointOfInterestType.UNEMPLOYED,
                AttachedVillageManager.getAttachedVillage(entity)
                                .map(IAttachedVillageContainer::getVillageCenter)
                                .orElse(entity.blockPosition()),
                homeScanRadius,
                PointOfInterestManager.Status.ANY
        ).map(PointOfInterest::getPos).collect(Collectors.toSet());

        Set<BlockPos> foundDoors = new HashSet<>();
        for (BlockPos home : homes) {
            BlockPos door = findNearestDoorAround(serverWorld, home, doorScanRadius);
            if (door != null) {
                BlockPos lower = ensureLowerHalf(serverWorld, door);
                foundDoors.add(lower.immutable());
            }
        }
        if (foundDoors.isEmpty()) return;

        BlockPos start = entity.blockPosition();

        List<BlockPos> sorted = sortByGreedyPath(foundDoors, start);
        getActiveDoors().addAll(sorted);
    }

    private BlockPos ensureLowerHalf(World world, BlockPos pos) {
        BlockState st = world.getBlockState(pos);
        if (st.getBlock() instanceof DoorBlock) {
            if (st.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER) {
                return pos.below();
            }
        }
        return pos;
    }

    private static BlockPos findNearestDoorAround(ServerWorld world, BlockPos center, int radius) {
        BlockPos bestDoor = null;
        int bestDY = Integer.MAX_VALUE;
        double bestDistSqr = Double.POSITIVE_INFINITY;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos p = center.offset(dx, dy, dz);
                    BlockState state = world.getBlockState(p);
                    if (!(state.getBlock() instanceof DoorBlock)) continue;

                    if (state.getValue(DoorBlock.HALF) != DoubleBlockHalf.LOWER) continue;

                    int dyAbs = Math.abs(p.getY() - center.getY());
                    double distSqr = p.distSqr(center);

                    boolean better = (dyAbs < bestDY) || (dyAbs == bestDY && distSqr < bestDistSqr);
                    if (better) {
                        bestDoor = p.immutable();
                        bestDY = dyAbs;
                        bestDistSqr = distSqr;
                    }
                }
            }
        }
        return bestDoor;
    }

    private static List<BlockPos> sortByGreedyPath(Collection<BlockPos> doors, BlockPos start) {
        List<BlockPos> remaining = new ArrayList<>(doors);
        List<BlockPos> result = new ArrayList<>(remaining.size());

        remaining.sort(Comparator.comparingDouble(pos -> pos.distSqr(start)));
        BlockPos pivot = remaining.remove(0);
        result.add(pivot);

        while (!remaining.isEmpty()) {
            final BlockPos last = result.get(result.size() - 1);
            int bestIndex = 0;
            double best = Double.MAX_VALUE;

            for (int i = 0; i < remaining.size(); i++) {
                BlockPos pos = remaining.get(i);
                double dist = pos.distSqr(last);
                if (dist < best) {
                    best = dist;
                    bestIndex = i;
                }
            }
            pivot = remaining.remove(bestIndex);
            result.add(pivot);
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
