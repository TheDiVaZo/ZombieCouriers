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
    private static final int LEAF_BREAK_CD = 5;

    private final CreatureEntity entity;
    private int leafBreakerCooldown = 0;

    public LeavesBreakGoal(CreatureEntity entity) {
        this.entity = entity;
    }

    @Override
    public void tick() {
        if (leafBreakerCooldown-- <= 0) {
            breakLeaves(entity, 1.5D);
            leafBreakerCooldown = LEAF_BREAK_CD;
        }
    }

    @Override
    public boolean canUse() {
        return leafBreakerCooldown-- <= 0;
    }

    private static void breakLeaves(MobEntity mob, double maxDist) {
        World world = mob.level;
        if (world.isClientSide) return;

        Vector3d dir = mob.getLookAngle().normalize();
        if (dir.lengthSqr() < 1e-6) return;

        double h = mob.getBbHeight();
        double[] yOffsets = { 0.2D, h * 0.5D, Math.max(0.9D, h * 0.9D) };

        BlockPos lastBroken = null;
        for (double yOff : yOffsets) {
            Vector3d base = mob.position().add(0.0D, yOff, 0.0D);

            for (double t = 0.2D; t <= maxDist; t += 0.2D) {
                BlockPos p = new BlockPos(base.add(dir.scale(t)));
                if (lastBroken != null && p.equals(lastBroken)) continue;

                BlockState st = world.getBlockState(p);

                if (st.getBlock() instanceof LeavesBlock) {
                    world.destroyBlock(p, false, mob);
                    lastBroken = p.immutable();
                    break;
                }

                if (!st.getMaterial().isReplaceable() && !st.getCollisionShape(world, p).isEmpty()) {
                    break;
                }
            }
        }
    }
}
