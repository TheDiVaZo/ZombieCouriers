package me.thedivazo.zombiecouriers.ai;

import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class LeavesBreakGoal extends Goal {
    private static final int LEAF_BREAK_COOLDOWN = 5;

    private final CreatureEntity entity;
    private int leafBreakCurrentCooldown = 0;

    public LeavesBreakGoal(CreatureEntity entity) {
        this.entity = entity;
    }

    @Override
    public void tick() {
        if (leafBreakCurrentCooldown-- <= 0) {
            breakLeaves(entity, 1.5D);
            leafBreakCurrentCooldown = LEAF_BREAK_COOLDOWN;
        }
    }

    @Override
    public boolean canUse() {
        return leafBreakCurrentCooldown-- <= 0;
    }

    private static void breakLeaves(MobEntity entity, double maxDist) {
        World world = entity.level;
        Vector3d dir = entity.getLookAngle().normalize();
        if (dir.lengthSqr() < 1e-6D) return;

        double height = entity.getBbHeight();
        double[] yOffsets = { 0.2D, height * 0.5D, Math.max(0.9D, height * 0.9D) };

        BlockPos lastBroken = null;
        for (double yOff : yOffsets) {
            Vector3d base = entity.position().add(0.0D, yOff, 0.0D);

            for (double t = 0.2D; t <= maxDist; t += 0.2D) {
                BlockPos blockPos = new BlockPos(base.add(dir.scale(t)));
                if (blockPos.equals(lastBroken)) continue;

                BlockState blockState = world.getBlockState(blockPos);

                if (blockState.getBlock() instanceof LeavesBlock) {
                    world.destroyBlock(blockPos, false, entity);
                    lastBroken = blockPos.immutable();
                    break;
                }

                if (!blockState.getMaterial().isReplaceable() && !blockState.getCollisionShape(world, blockPos).isEmpty()) {
                    break;
                }
            }
        }
    }
}
