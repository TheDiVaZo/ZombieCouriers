package me.thedivazo.zombiecouriers.util;

import net.minecraft.util.math.BlockPos;

import java.util.*;
import java.util.stream.Collectors;

public class HomeSort {
    public static Collection<Pair<BlockPos, Set<BlockPos>>> sortByNearest(Collection<Pair<BlockPos, Set<BlockPos>>> points, BlockPos start) {
        if (points == null || start == null) throw new IllegalArgumentException("points/start must not be null");

        List<Pair<BlockPos, Set<BlockPos>>> remaining = new ArrayList<>(points);
        List<Pair<BlockPos, Set<BlockPos>>> result = new ArrayList<>(remaining.size());

        BlockPos current = start;

        while (!remaining.isEmpty()) {
            int bestIdx = -1;
            double bestDist2 = Double.MAX_VALUE;
            BlockPos bestPos = null;

            for (int i = 0, n = remaining.size(); i < n; i++) {
                BlockPos blockPos = remaining.get(i).getFirst();
                double dist2 = current.distSqr(blockPos);

                if (dist2 < bestDist2 || (dist2 == bestDist2 && bestPos != null && tieBreak(blockPos, bestPos) < 0)) {
                    bestIdx = i;
                    bestDist2 = dist2;
                    bestPos = blockPos;
                }
            }

            // Оптимизация: Свапаем удаляемый элемент в конец и удаляем с конца вместо удаления из середины. Круто я придумал да
            int last = remaining.size() - 1;
            Collections.swap(remaining, bestIdx, last);
            Pair<BlockPos, Set<BlockPos>> chosen = remaining.remove(last);

            result.add(chosen);
            current = chosen.getFirst();
        }
        return result;
    }

    public static Collection<Pair<BlockPos, Set<BlockPos>>> sortByDistance(Collection<Pair<BlockPos, Set<BlockPos>>> points, BlockPos start) {
        return points.stream()
                .sorted(Comparator.comparingDouble(point -> point.getFirst().distSqr(start)))
                .collect(Collectors.toList());
    }

    private static int tieBreak(BlockPos a, BlockPos b) {
        int compareValue = Integer.compare(a.getX(), b.getX());
        if (compareValue != 0) return compareValue;
        compareValue = Integer.compare(a.getY(), b.getY());
        if (compareValue != 0) return compareValue;
        return Integer.compare(a.getZ(), b.getZ());
    }
}
