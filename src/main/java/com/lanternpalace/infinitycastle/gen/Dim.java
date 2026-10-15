package com.lanternpalace.infinitycastle.gen;

/** Constants shared by the dimension data files and the generator. Must match dimension_type/infinity_castle.json. */
public final class Dim {
    private Dim() {}

    public static final int MIN_Y = -64;
    public static final int HEIGHT = 576;                 // multiple of 16
    public static final int MAX_Y = MIN_Y + HEIGHT - 1;   // 511

    /** Palace terrace surface level (top solid block). */
    public static final int PALACE_Y = 66;

    /** Safe arrival point: centre of the courtyard in front of the great stairway. */
    public static final double SPAWN_X = 0.5;
    public static final double SPAWN_Y = PALACE_Y + 1;
    public static final double SPAWN_Z = 69.5;
}
