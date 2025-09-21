package me.thedivazo.zombiecouriers.ai.courier;

import me.thedivazo.zombiecouriers.ai.StateMachine;
import me.thedivazo.zombiecouriers.capability.iventory.CourierInventoryManager;
import me.thedivazo.zombiecouriers.capability.iventory.ICourierInventory;
import me.thedivazo.zombiecouriers.capability.state.State;
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

import java.util.*;
import java.util.stream.Collectors;

public class DistributionGoal extends CourierMoveGoal {

    private final int homeScanRadius = 100;
    private final int doorScanRadius = 10;

    private final Deque<BlockPos> nexts = new ArrayDeque<>();
    private final Set<BlockPos> visited = new HashSet<>();

    public DistributionGoal(CreatureEntity entity, StateMachine stateMachine) {
        super(entity, stateMachine, State.DISTRIBUTION, 0.8d, 1d);
    }

    @Override
    public void arrived() {
        BlockPos currentDoor = getTarget();
        visited.add(currentDoor);
        ItemStack dropItemStack = CourierInventoryManager
                .getCourierInventory(entity)
                .map(ICourierInventory::pollOne)
                .orElse(ItemStack.EMPTY);

        ItemEntity dropItemEntity = new ItemEntity(entity.level, currentDoor.getX() + 0.5, currentDoor.getY() + 0.5, currentDoor.getZ() + 0.5, dropItemStack);
        dropItemEntity.setNoPickUpDelay();
        dropItemEntity.setDeltaMovement(Vector3d.ZERO);
        entity.level.addFreshEntity(dropItemEntity);

        setTarget(pollNearestUnvisitedDoor());
        if (dropItemStack.isEmpty()) {
            nextStage();
        }
        else if (CourierInventoryManager
                .getCourierInventory(entity)
                .map(inventory -> inventory.getCount() <= 0)
                .orElse(true)
        ) {
            nextStage();
        }
        if (nexts.isEmpty()) {
            visited.clear();
        }
    }

    private BlockPos pollNearestUnvisitedDoor() {
        if (nexts.isEmpty()) return null;
        while (!nexts.isEmpty() && visited.contains(nexts.peek())) {
            nexts.poll();
        }
        return nexts.poll();
    }

    @Override
    public boolean recalculatePath() {
        if (nexts.isEmpty()) {
            refillQueue();
            setTarget(nexts.poll());
        }
        return super.recalculatePath();
    }

    @Override
    protected boolean onUse() {
        return tryRecalculatePath();
    }

    @Override
    protected boolean onContinueToUse() {
        return !nexts.isEmpty();
    }

    public void nextStage() {
        stateMachine.setState(State.FARM_GARDEN_BED);
    }

    public void refillQueue() {
        ServerWorld serverWorld = (ServerWorld) entity.level;
        PointOfInterestManager poi = serverWorld.getPoiManager();

        Iterable<BlockPos> homes = poi.getInRange(
                target ->
                        target == PointOfInterestType.HOME ||
                        target == PointOfInterestType.ALL_JOBS ||
                        target == PointOfInterestType.MEETING,
                entity.blockPosition(),
                homeScanRadius,
                PointOfInterestManager.Status.ANY
        ).map(PointOfInterest::getPos).collect(Collectors.toSet());

        Set<BlockPos> foundDoors = new HashSet<>();
        for (BlockPos home : homes) {
            BlockPos door = findNearestDoorAround(serverWorld, home, doorScanRadius);
            if (door != null) {
                BlockPos lower = ensureLowerHalf(serverWorld, door);
                if (!visited.contains(lower)) {
                    foundDoors.add(lower.immutable());
                }
            }
        }
        if (foundDoors.isEmpty()) return;

        BlockPos start = (getTarget() != null && foundDoors.contains(getTarget()))
                ? getTarget()
                : entity.blockPosition();

        List<BlockPos> sorted = sortByGreedyPath(foundDoors, start);
        nexts.addAll(sorted);
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

    private static BlockPos findNearestDoorAround(World world, BlockPos bedPos, int radius) {
        BlockPos bestDoor = null;
        int bestDY = Integer.MAX_VALUE;
        double bestDistSqr = -1.0;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos blockPos = bedPos.offset(dx, dy, dz);
                    BlockState blockState = world.getBlockState(blockPos);
                    if (!(blockState.getBlock() instanceof DoorBlock)) continue;

                    BlockPos lower = (blockState.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER) ? blockPos.below() : blockPos;
                    BlockState lowerState = world.getBlockState(lower);
                    if (!(lowerState.getBlock() instanceof DoorBlock)) continue;

                    int dyAbs = Math.abs(lower.getY() - bedPos.getY());

                    double distSqr = lower.distSqr(bedPos);

                    boolean better =
                            (dyAbs < bestDY) ||
                                    (dyAbs == bestDY && distSqr > bestDistSqr);

                    if (better) {
                        bestDoor = lower.immutable();
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
}
