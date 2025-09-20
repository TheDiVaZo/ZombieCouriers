package me.thedivazo.zombiecouriers.util;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;

public class BlockPosFunction {

    @Nullable
    public static BlockPos getNearbyVillage(ServerWorld serverLevel, BlockPos nearPos, int radius) {
        return serverLevel.findNearestMapFeature(Structure.VILLAGE, nearPos, radius, false);
    }

    public static double distanceToSqr(LivingEntity livingEntity, BlockPos pos) {
        return livingEntity.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
    }
}
