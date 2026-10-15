package com.lanternpalace.infinitycastle.gen;

/**
 * A "cell" is a packed int describing one block: bits 0-7 = material ordinal + 1 (0 means "untouched"),
 * bits 8-9 = horizontal facing (0=N,1=E,2=S,3=W), bit 10 = top/hanging/inverted, bit 11 = connects along X,
 * bit 12 = connects along Z.
 */
public final class Cell {
    private Cell() {}

    public static final int TOP = 1 << 10;
    public static final int CX = 1 << 11;
    public static final int CZ = 1 << 12;
    private static final Mat[] ALL = Mat.values();

    public static int of(Mat m) { return m.ordinal() + 1; }

    public static int stair(Mat m, int facing, boolean top) {
        return of(m) | ((facing & 3) << 8) | (top ? TOP : 0);
    }

    public static int slab(Mat m, boolean top) { return of(m) | (top ? TOP : 0); }

    public static int lantern(Mat m, boolean hanging) { return of(m) | (hanging ? TOP : 0); }

    /** A fence/pane-like block; connects along X and/or Z. */
    public static int rail(Mat m, boolean alongX, boolean alongZ) {
        return of(m) | (alongX ? CX : 0) | (alongZ ? CZ : 0);
    }

    public static Mat mat(int cell) { return ALL[(cell & 0xFF) - 1]; }
    public static int facing(int cell) { return (cell >> 8) & 3; }
    public static boolean top(int cell) { return (cell & TOP) != 0; }
    public static boolean connectX(int cell) { return (cell & CX) != 0; }
    public static boolean connectZ(int cell) { return (cell & CZ) != 0; }
}
