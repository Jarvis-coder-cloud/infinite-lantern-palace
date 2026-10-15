package com.lanternpalace.infinitycastle.gen;

import static com.lanternpalace.infinitycastle.gen.Mat.*;

/** Architectural palette for a building. */
public record Style(Mat pillar, Mat wall, Mat floor, Mat trim, Mat roofStair, Mat roofBlock, Mat stone) {
    public static final Style[] ALL = {
        // 0: dark wood, slate roof, red trim
        new Style(DARK_OAK_LOG, DARK_OAK_PLANKS, DARK_OAK_PLANKS, RED_NETHER_BRICKS, DEEPSLATE_TILE_STAIRS, DEEPSLATE_TILES, DEEPSLATE_BRICKS),
        // 1: crimson roof
        new Style(RED_NETHER_BRICKS, DARK_OAK_PLANKS, DARK_OAK_PLANKS, POLISHED_BLACKSTONE_BRICKS, RED_NETHER_BRICK_STAIRS, RED_NETHER_BRICKS, DEEPSLATE_BRICKS),
        // 2: stone walls, wooden roof
        new Style(POLISHED_DEEPSLATE, DEEPSLATE_BRICKS, DEEPSLATE_TILES, RED_NETHER_BRICKS, DARK_OAK_STAIRS, DARK_OAK_PLANKS, POLISHED_BLACKSTONE_BRICKS),
        // 3: spruce
        new Style(SPRUCE_LOG, SPRUCE_PLANKS, SPRUCE_PLANKS, DARK_OAK_PLANKS, DEEPSLATE_BRICK_STAIRS, DEEPSLATE_BRICKS, DEEPSLATE_BRICKS),
        // 4: royal (palace): red pillars, gold trim, slate roof
        new Style(RED_NETHER_BRICKS, DEEPSLATE_BRICKS, DARK_OAK_PLANKS, GOLD_BLOCK, DEEPSLATE_TILE_STAIRS, DEEPSLATE_TILES, POLISHED_DEEPSLATE),
    };
}
