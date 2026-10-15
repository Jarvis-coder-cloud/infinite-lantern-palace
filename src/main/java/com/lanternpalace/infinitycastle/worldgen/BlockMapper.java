package com.lanternpalace.infinitycastle.worldgen;

import com.lanternpalace.infinitycastle.InfinityCastleMod;
import com.lanternpalace.infinitycastle.gen.Cell;
import com.lanternpalace.infinitycastle.gen.Mat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;

/** Translates the generator's abstract cells into real vanilla BlockStates (cached). */
public final class BlockMapper {
    private BlockMapper() {}

    private static final Direction[] FACINGS = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
    private static final Map<Integer, BlockState> STATES = new ConcurrentHashMap<>();
    private static final Block[] BLOCKS = new Block[Mat.values().length];

    public static BlockState state(int cell) {
        BlockState s = STATES.get(cell);
        if (s == null) {
            s = build(cell);
            STATES.put(cell, s);
        }
        return s;
    }

    private static Block block(Mat m) {
        Block b = BLOCKS[m.ordinal()];
        if (b != null) return b;
        if (m == Mat.AIR) {
            b = Blocks.AIR;
        } else {
            for (String id : m.id.split("\\|")) {
                Block candidate = BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath("minecraft", id));
                if (candidate != Blocks.AIR) { b = candidate; break; }
            }
            if (b == null) {
                InfinityCastleMod.LOGGER.warn("Unknown block '{}' for {}, substituting deepslate bricks", m.id, m);
                b = Blocks.DEEPSLATE_BRICKS;
            }
        }
        BLOCKS[m.ordinal()] = b;
        return b;
    }

    private static BlockState build(int cell) {
        Mat m = Cell.mat(cell);
        BlockState s = block(m).defaultBlockState();
        boolean top = Cell.top(cell);
        switch (m.kind) {
            case STAIR:
                return s.trySetValue(BlockStateProperties.HORIZONTAL_FACING, FACINGS[Cell.facing(cell)])
                        .trySetValue(BlockStateProperties.HALF, top ? Half.TOP : Half.BOTTOM);
            case SLAB:
                return s.trySetValue(BlockStateProperties.SLAB_TYPE, top ? SlabType.TOP : SlabType.BOTTOM);
            case FENCE:
                return s.trySetValue(BlockStateProperties.EAST, Cell.connectX(cell))
                        .trySetValue(BlockStateProperties.WEST, Cell.connectX(cell))
                        .trySetValue(BlockStateProperties.NORTH, Cell.connectZ(cell))
                        .trySetValue(BlockStateProperties.SOUTH, Cell.connectZ(cell));
            case LANTERN:
                return s.trySetValue(BlockStateProperties.HANGING, top);
            default:
                return s;
        }
    }
}
