package com.lanternpalace.infinitycastle.gen;

/**
 * Abstract material palette. The generator only deals in these; the Minecraft layer maps them to real
 * BlockStates. An id of the form "a|b" means "use a, fall back to b" (for blocks renamed between versions).
 */
public enum Mat {
    AIR("air", Kind.BLOCK),
    DARK_OAK_PLANKS("dark_oak_planks", Kind.BLOCK),
    DARK_OAK_LOG("dark_oak_log", Kind.BLOCK),
    DARK_OAK_STAIRS("dark_oak_stairs", Kind.STAIR),
    DARK_OAK_SLAB("dark_oak_slab", Kind.SLAB),
    DARK_OAK_FENCE("dark_oak_fence", Kind.FENCE),
    SPRUCE_PLANKS("spruce_planks", Kind.BLOCK),
    SPRUCE_LOG("spruce_log", Kind.BLOCK),
    DEEPSLATE_BRICKS("deepslate_bricks", Kind.BLOCK),
    DEEPSLATE_BRICK_STAIRS("deepslate_brick_stairs", Kind.STAIR),
    DEEPSLATE_BRICK_SLAB("deepslate_brick_slab", Kind.SLAB),
    DEEPSLATE_TILES("deepslate_tiles", Kind.BLOCK),
    DEEPSLATE_TILE_STAIRS("deepslate_tile_stairs", Kind.STAIR),
    DEEPSLATE_TILE_SLAB("deepslate_tile_slab", Kind.SLAB),
    POLISHED_DEEPSLATE("polished_deepslate", Kind.BLOCK),
    POLISHED_BLACKSTONE_BRICKS("polished_blackstone_bricks", Kind.BLOCK),
    RED_NETHER_BRICKS("red_nether_bricks", Kind.BLOCK),
    RED_NETHER_BRICK_STAIRS("red_nether_brick_stairs", Kind.STAIR),
    RED_NETHER_BRICK_SLAB("red_nether_brick_slab", Kind.SLAB),
    GOLD_BLOCK("gold_block", Kind.BLOCK),
    SHROOMLIGHT("shroomlight", Kind.BLOCK),
    GRASS_BLOCK("grass_block", Kind.BLOCK),
    LANTERN("lantern", Kind.LANTERN),
    SOUL_LANTERN("soul_lantern", Kind.LANTERN),
    RED_WOOL("red_wool", Kind.BLOCK),
    BLACK_WOOL("black_wool", Kind.BLOCK),
    ORANGE_PANE("orange_stained_glass_pane", Kind.FENCE),
    CHAIN("iron_chain|chain", Kind.BLOCK);

    /** How a material's orientation flags (see {@link Cell}) are interpreted. */
    public enum Kind { BLOCK, STAIR, SLAB, FENCE, LANTERN }

    public final String id;
    public final Kind kind;

    Mat(String id, Kind kind) {
        this.id = id;
        this.kind = kind;
    }
}
