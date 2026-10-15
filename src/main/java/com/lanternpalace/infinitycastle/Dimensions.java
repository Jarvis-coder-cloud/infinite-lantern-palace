package com.lanternpalace.infinitycastle;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public final class Dimensions {
    private Dimensions() {}

    /** Matches data/lantern_palace/dimension/infinity_castle.json */
    public static final String CASTLE_ID = "lantern_palace:infinity_castle";
    public static final ResourceKey<Level> CASTLE = ResourceKey.create(Registries.DIMENSION, InfinityCastleMod.id("infinity_castle"));

    /** Maps a level key to the id string understood by `execute in`. Unknown / modded dimensions fall back to the overworld. */
    public static String commandId(ResourceKey<Level> key) {
        if (key.equals(CASTLE)) return CASTLE_ID;
        if (key.equals(Level.NETHER)) return "minecraft:the_nether";
        if (key.equals(Level.END)) return "minecraft:the_end";
        return "minecraft:overworld";
    }
}
