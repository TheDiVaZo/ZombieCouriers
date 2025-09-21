package me.thedivazo.zombiecouriers.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropsBlock;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.server.ServerWorld;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class BlockPosUtil {

    @Nullable
    public static BlockPos getNearbyVillage(ServerWorld serverLevel, BlockPos nearPos, int radius) {
        return serverLevel.findNearestMapFeature(Structure.VILLAGE, nearPos, radius, false);
    }

    public static double distanceToSqr(LivingEntity livingEntity, BlockPos pos) {
        return livingEntity.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
    }

    public static boolean isFarmBlock(IWorldReader worldReader, BlockPos pos) {
        BlockState blockState = worldReader.getBlockState(pos);
        return blockState.getBlock() instanceof FarmlandBlock;
    }

    public static boolean isMaxAgeCrop(IWorldReader world, BlockPos pos) {
        if (world == null || pos == null) return false;

        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        if (!(block instanceof CropsBlock)) return false;

        return ((CropsBlock) block).isMaxAge(state);
    }

    public static boolean isRipeFarmlandCropAt(IWorldReader world, BlockPos pos) {
        if (!isMaxAgeCrop(world, pos)) return false;

        return isFarmBlock(world, pos.below());
    }

    public static List<BlockPos> neighbors4(BlockPos p) {
        return Arrays.asList(
                p.north(), p.south(), p.west(), p.east()
        );
    }

}
