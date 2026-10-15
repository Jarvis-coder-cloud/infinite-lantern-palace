package com.lanternpalace.infinitycastle.gen;

import java.util.Arrays;

/**
 * A 16 x HEIGHT x 16 column of cells for exactly one chunk. Structures are drawn in *local* coordinates through
 * an origin / quarter-turn rotation / vertical flip transform; every write is clipped to this chunk, which is what
 * makes generation seamless: each chunk independently re-draws every structure that overlaps it.
 */
public final class Canvas {
    private final int[] cells = new int[256 * Dim.HEIGHT];
    public int minX, minZ;
    private int lo = Integer.MAX_VALUE, hi = Integer.MIN_VALUE; // touched y offsets

    // transform
    private int ox, oy, oz, rot, offX, offZ;
    private boolean flip;

    public void reset(int chunkX, int chunkZ) {
        if (hi >= lo) Arrays.fill(cells, lo << 8, (hi + 1) << 8, 0);
        lo = Integer.MAX_VALUE;
        hi = Integer.MIN_VALUE;
        minX = chunkX << 4;
        minZ = chunkZ << 4;
        identity();
    }

    public Canvas(int chunkX, int chunkZ) { reset(chunkX, chunkZ); }

    public void identity() { transform(0, 0, 0, 0, false); }

    /** Local (0,0,0) maps to world (x,y,z); rot = clockwise quarter turns seen from above; flip mirrors vertically. */
    public void transform(int x, int y, int z, int rotation, boolean flipY) {
        ox = x; oy = y; oz = z; rot = rotation & 3; flip = flipY; offX = 0; offZ = 0;
    }

    /** Shifts local origin horizontally (in local space, so it rotates with the structure). */
    public void offset(int lx, int lz) { offX = lx; offZ = lz; }

    public boolean overlaps(int x1, int z1, int x2, int z2) {
        return x2 >= minX && x1 <= minX + 15 && z2 >= minZ && z1 <= minZ + 15;
    }

    /** Cell at chunk-relative x/z (0..15) and absolute y; 0 = untouched. */
    public int at(int dx, int y, int dz) { return cells[((y - Dim.MIN_Y) << 8) | (dz << 4) | dx]; }

    public int touchedMinY() { return lo + Dim.MIN_Y; }
    public int touchedMaxY() { return hi + Dim.MIN_Y; }
    public boolean isEmpty() { return hi < lo; }

    private int wx(int lx, int lz) {
        lx += offX; lz += offZ;
        switch (rot) {
            case 0: return ox + lx;
            case 1: return ox - lz;
            case 2: return ox - lx;
            default: return ox + lz;
        }
    }

    private int wz(int lx, int lz) {
        lx += offX; lz += offZ;
        switch (rot) {
            case 0: return oz + lz;
            case 1: return oz + lx;
            case 2: return oz - lz;
            default: return oz - lx;
        }
    }

    private int wy(int ly) { return flip ? oy - ly : oy + ly; }

    /** Applies rotation / flip to a cell's orientation bits. */
    private int xf(int cell) {
        if (cell == 0) return 0;
        switch (Cell.mat(cell).kind) {
            case STAIR: {
                int f = (Cell.facing(cell) + rot) & 3;
                int v = (cell & ~(3 << 8)) | (f << 8);
                return flip ? v ^ Cell.TOP : v;
            }
            case SLAB:
            case LANTERN:
                return flip ? cell ^ Cell.TOP : cell;
            case FENCE:
                if ((rot & 1) == 1) {
                    boolean x = Cell.connectX(cell), z = Cell.connectZ(cell);
                    return (cell & ~(Cell.CX | Cell.CZ)) | (z ? Cell.CX : 0) | (x ? Cell.CZ : 0);
                }
                return cell;
            default:
                return cell;
        }
    }

    public void set(int x, int y, int z, int cell) { fill(x, y, z, x, y, z, cell); }

    /** Inclusive box in local coordinates. */
    public void fill(int x1, int y1, int z1, int x2, int y2, int z2, int cell) {
        int ax = wx(x1, z1), az = wz(x1, z1), bx = wx(x2, z2), bz = wz(x2, z2);
        int ya = wy(y1), yb = wy(y2);
        int xa = Math.max(Math.min(ax, bx), minX), xb = Math.min(Math.max(ax, bx), minX + 15);
        int za = Math.max(Math.min(az, bz), minZ), zb = Math.min(Math.max(az, bz), minZ + 15);
        int yl = Math.max(Math.min(ya, yb), Dim.MIN_Y), yh = Math.min(Math.max(ya, yb), Dim.MAX_Y);
        if (xa > xb || za > zb || yl > yh) return;
        int v = xf(cell);
        lo = Math.min(lo, yl - Dim.MIN_Y);
        hi = Math.max(hi, yh - Dim.MIN_Y);
        for (int y = yl; y <= yh; y++) {
            int base = (y - Dim.MIN_Y) << 8;
            for (int z = za; z <= zb; z++) {
                int row = base | ((z - minZ) << 4);
                for (int x = xa; x <= xb; x++) cells[row | (x - minX)] = v;
            }
        }
    }
}
