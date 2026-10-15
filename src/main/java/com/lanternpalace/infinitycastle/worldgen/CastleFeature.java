package com.lanternpalace.infinitycastle.worldgen;

import com.lanternpalace.infinitycastle.gen.Canvas;
import com.lanternpalace.infinitycastle.gen.WorldPlan;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Runs once per chunk during feature decoration (it is the only entry in the lantern_void biome's feature list).
 * It rasterises every structure overlapping this chunk and writes only inside this chunk's 16x16 column, so
 * generation is seamless, deterministic (seed + coordinates only) and never touches neighbouring chunks.
 */
public final class CastleFeature implements Feature {
    public static final MapCodec<CastleFeature> CODEC = MapCodec.unit(new CastleFeature());
    private static final ThreadLocal<Canvas> CANVAS = ThreadLocal.withInitial(() -> new Canvas(0, 0));

    @Override
    public MapCodec<? extends Feature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        int cx = origin.getX() >> 4, cz = origin.getZ() >> 4;
        Canvas canvas = CANVAS.get();
        canvas.reset(cx, cz);
        WorldPlan.of(level.getSeed()).generate(canvas);
        if (canvas.isEmpty()) return false;

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int baseX = cx << 4, baseZ = cz << 4;
        for (int y = canvas.touchedMinY(); y <= canvas.touchedMaxY(); y++) {
            for (int dz = 0; dz < 16; dz++) {
                for (int dx = 0; dx < 16; dx++) {
                    int cell = canvas.at(dx, y, dz);
                    if (cell == 0) continue;
                    level.setBlock(pos.set(baseX + dx, y, baseZ + dz), BlockMapper.state(cell), 2);
                }
            }
        }
        return true;
    }
}
