package me.thedivazo.zombiecouriers.ai.courier;

import me.thedivazo.zombiecouriers.ai.StateMachine;
import me.thedivazo.zombiecouriers.capability.iventory.CourierInventoryManager;
import me.thedivazo.zombiecouriers.capability.state.State;
import me.thedivazo.zombiecouriers.util.BlockPosUtil;
import net.minecraft.block.BlockState;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;

import java.util.*;

public class FarmGardenBedGoal extends CourierMoveGoal {
    private final int searchRange = 100;
    private final int verticalSearchRange = 3;

    Queue<BlockPos> crops = new ArrayDeque<>();

    Deque<BlockPos> stack = new ArrayDeque<>();
    Set<BlockPos> visited = new HashSet<>();

    private boolean hasFermed = false;

    public FarmGardenBedGoal(CreatureEntity entity, StateMachine stateMachine) {
        super(entity, stateMachine, State.FARM_GARDEN_BED, 1.5d, 1d);
        setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
    }

    //Скомунизжено из MoveToBlockGoal
    public BlockPos findCrop() {
        int i = this.searchRange;
        int j = this.verticalSearchRange;
        BlockPos blockpos = entity.blockPosition();
        BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

        for (int k = 0; k <= j; k = k > 0 ? -k : 1 - k) {
            for (int l = 0; l < i; ++l) {
                for (int i1 = 0; i1 <= l; i1 = i1 > 0 ? -i1 : 1 - i1) {
                    for (int j1 = i1 < l && i1 > -l ? l : 0; j1 <= l; j1 = j1 > 0 ? -j1 : 1 - j1) {
                        blockpos$mutable.setWithOffset(blockpos, i1, k - 1, j1);
                        if (
                                BlockPosUtil.isRipeFarmlandCropAt(entity.level, blockpos$mutable)
                        ) {
                            return blockpos$mutable;
                        }
                    }
                }
            }
        }

        return null;
    }

    public void findGardenBed() {
        BlockPos cropStart = findCrop();
        if (cropStart == null) return;
        BlockPos gardenStart = cropStart.below();

        visited.add(gardenStart);
        stack.push(gardenStart);

        while (!stack.isEmpty()) {
            BlockPos acceptionGarden = stack.pop();

            if (BlockPosUtil.isFarmBlock(entity.level, acceptionGarden)) {
                BlockPos crop = acceptionGarden.above();
                if (BlockPosUtil.isMaxAgeCrop(entity.level, crop)) {
                    crops.add(crop);
                }

                List<BlockPos> neighbours = BlockPosUtil.neighbors4(acceptionGarden);
                for (BlockPos neighbour : neighbours) {
                    if (visited.add(neighbour)) {
                        stack.push(neighbour);
                    }
                }
            }
        }
        setTarget(crops.poll());
    }

    @Override
    public void start() {
        findGardenBed();
    }

    @Override
    public void arrived() {
        if (getTarget() == null) return;
        if (BlockPosUtil.isMaxAgeCrop(entity.level, getTarget())) {

            BlockState state = entity.level.getBlockState(getTarget());

            List<ItemStack> loot = state.getDrops(
                    new LootContext.Builder((ServerWorld) entity.level)
                            .withRandom(entity.getRandom())
                            .withParameter(LootParameters.TOOL, ItemStack.EMPTY)
                            .withParameter(LootParameters.THIS_ENTITY, entity)
                            .withParameter(LootParameters.ORIGIN, Vector3d.ZERO)
            );

            CourierInventoryManager.getCourierInventory(entity).ifPresent(inventory -> {
                for (ItemStack itemStack : loot) {
                    inventory.add(itemStack);
                }
            });

            entity.level.destroyBlock(getTarget(), false, entity);
            hasFermed = true;
        }
        setTarget(crops.poll());
        if (getTarget() == null && hasFermed) {
            hasFermed = false;
            nextStage();
        }
    }

    @Override
    public boolean recalculatePath() {
        if (getTarget() == null) {
            findGardenBed();
        }
        return super.recalculatePath();
    }

    @Override
    protected boolean onUse() {
        return tryRecalculatePath();
    }

    @Override
    protected boolean onContinueToUse() {
        return !crops.isEmpty();
    }

    public void nextStage() {
        stateMachine.setState(State.DISTRIBUTION);
    }
}
