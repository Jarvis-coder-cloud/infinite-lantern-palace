package com.lanternpalace.infinitycastle.gen;

import static com.lanternpalace.infinitycastle.gen.Mat.*;

/**
 * Architectural primitives. All coordinates are local to the Canvas transform; y values are absolute local
 * heights (so a caller passes the level the structure stands on). +Z is the "front" of every structure.
 * Loops must always run to completion and consume the Rnd identically, regardless of chunk clipping.
 */
final class Build {
    private Build() {}

    static final int AIR_C = Cell.of(AIR);

    static int c(Mat m) { return Cell.of(m); }

    // ---------------------------------------------------------------- small pieces

    static void perimeter(Canvas c, int hx, int hz, int y, int cell) {
        c.fill(-hx, y, -hz, hx, y, -hz, cell);
        c.fill(-hx, y, hz, hx, y, hz, cell);
        c.fill(-hx, y, -hz, -hx, y, hz, cell);
        c.fill(hx, y, -hz, hx, y, hz, cell);
    }

    static void lampPost(Canvas c, int x, int y, int z) {
        c.fill(x, y, z, x, y + 1, z, c(DARK_OAK_LOG));
        c.set(x, y + 2, z, Cell.lantern(LANTERN, false));
    }

    /** Chain hanging down from block (x,y,z)'s underside, ending in a lantern. Needs a solid block at (x,y+1,z)... caller's duty. */
    static void hangLantern(Canvas c, int x, int y, int z, int chainLen) {
        c.fill(x, y - chainLen + 1, z, x, y, z, c(CHAIN));
        c.set(x, y - chainLen, z, Cell.lantern(LANTERN, true));
    }

    static void banner(Canvas c, int x, int top, int z, boolean alongX, int len) {
        int x2 = alongX ? x + 1 : x, z2 = alongX ? z : z + 1;
        c.fill(x, top - len + 1, z, x2, top, z2, c(RED_WOOL));
        c.fill(x, top, z, x2, top, z2, c(BLACK_WOOL));
        c.fill(x, top - 2, z, x2, top - 2, z2, c(GOLD_BLOCK));
    }

    private static void ab(Canvas c, int x, int z, boolean alongX, int a1, int y1, int b1, int a2, int y2, int b2, int cell) {
        if (alongX) c.fill(x + a1, y1, z + b1, x + a2, y2, z + b2, cell);
        else c.fill(x + b1, y1, z + a1, x + b2, y2, z + a2, cell);
    }

    /** Torii gate. Beam runs along X when alongX (people walk through along Z). y = lowest pillar block. */
    static void torii(Canvas c, int x, int y, int z, boolean alongX, int half, int height) {
        int red = c(RED_NETHER_BRICKS);
        ab(c, x, z, alongX, -half, y, 0, -half, y, 0, c(POLISHED_DEEPSLATE));
        ab(c, x, z, alongX, half, y, 0, half, y, 0, c(POLISHED_DEEPSLATE));
        ab(c, x, z, alongX, -half, y + 1, 0, -half, y + height - 1, 0, red);
        ab(c, x, z, alongX, half, y + 1, 0, half, y + height - 1, 0, red);
        ab(c, x, z, alongX, -half, y + height - 3, 0, half, y + height - 3, 0, red);          // nuki
        ab(c, x, z, alongX, -half - 2, y + height, -1, half + 2, y + height, 1, c(DEEPSLATE_TILES)); // kasagi
        ab(c, x, z, alongX, -half - 3, y + height + 1, 0, half + 3, y + height + 1, 0, Cell.slab(DEEPSLATE_TILE_SLAB, false));
        ab(c, x, z, alongX, 0, y + height - 2, 0, 0, y + height - 2, 0, c(GOLD_BLOCK));
        ab(c, x, z, alongX, 0, y + height - 4, 0, 0, y + height - 4, 0, Cell.lantern(LANTERN, true));
    }

    // ---------------------------------------------------------------- bases

    /** Platform whose surface block is at y=0, with an inverted stone cone, a spire shaft and a hanging lantern. */
    static void island(Canvas c, Style s, int px, int pz, int depth) {
        c.fill(-px, 0, -pz, px, 0, pz, c(s.stone()));
        c.fill(-px + 1, 0, -pz + 1, px - 1, 0, pz - 1, c(DEEPSLATE_TILES));
        for (int d = 1; d <= depth; d++) {
            int hx = Math.max(2, px - (d * (px - 2)) / depth);
            int hz = Math.max(2, pz - (d * (pz - 2)) / depth);
            Mat m = d % 5 == 0 ? POLISHED_DEEPSLATE : (d % 2 == 0 ? DEEPSLATE_BRICKS : s.stone());
            c.fill(-hx, -d, -hz, hx, -d, hz, c(m));
        }
        c.fill(-1, -depth - 5, -1, 1, -depth - 1, 1, c(POLISHED_DEEPSLATE));
        c.fill(0, -depth - 9, 0, 0, -depth - 6, 0, c(CHAIN));
        c.set(0, -depth - 10, 0, Cell.lantern(LANTERN, true));
    }

    /** Fence rim with lantern posts around a platform whose surface is at y. */
    static void rim(Canvas c, int px, int pz, int y, int step) {
        int fx = Cell.rail(DARK_OAK_FENCE, true, false), fz = Cell.rail(DARK_OAK_FENCE, false, true);
        c.fill(-px, y + 1, -pz, px, y + 1, -pz, fx);
        c.fill(-px, y + 1, pz, px, y + 1, pz, fx);
        c.fill(-px, y + 1, -pz, -px, y + 1, pz, fz);
        c.fill(px, y + 1, -pz, px, y + 1, pz, fz);
        int both = Cell.rail(DARK_OAK_FENCE, true, true);
        c.set(-px, y + 1, -pz, both); c.set(px, y + 1, -pz, both);
        c.set(-px, y + 1, pz, both); c.set(px, y + 1, pz, both);
        int lan = Cell.lantern(LANTERN, false);
        for (int x = -px; x <= px; x += step) { c.set(x, y + 2, -pz, lan); c.set(x, y + 2, pz, lan); }
        for (int z = -pz + step; z <= pz - step; z += step) { c.set(-px, y + 2, z, lan); c.set(px, y + 2, z, lan); }
    }

    /** Stepped stone podium with a real stairway on its +Z face. Top layer is at y0+height. */
    static void podium(Canvas c, Style s, int hx, int hz, int y0, int height) {
        for (int t = 0; t < height; t++) {
            int e = 2 + (height - 1 - t);
            Mat m = t % 2 == 0 ? s.stone() : DEEPSLATE_BRICKS;
            c.fill(-hx - e, y0 + 1 + t, -hz - e, hx + e, y0 + 1 + t, hz + e, c(m));
        }
        for (int t = 0; t < height; t++) {
            int e = 2 + (height - 1 - t);
            c.fill(-1, y0 + 1 + t, hz + e, 1, y0 + 1 + t, hz + e, Cell.stair(DEEPSLATE_BRICK_STAIRS, 0, false));
        }
    }

    // ---------------------------------------------------------------- building

    private static boolean pillarAt(int v, int h) { return v == -h || v == h || Math.floorMod(v + h, 4) == 0; }
    private static boolean windowAt(int v, int h) { int m = Math.floorMod(v + h, 4); return m == 1 || m == 2; }

    private static void window(Canvas c, int x, int y, int z, int rows, boolean lit, boolean alongX) {
        int cell = lit ? c(SHROOMLIGHT) : Cell.rail(ORANGE_PANE, alongX, !alongX);
        c.fill(x, y, z, x, y + rows - 1, z, cell);
    }

    /** One floor of a building: floor + balcony at y, walls up to y+h-1 (beam), pillars, lit windows. */
    static void storey(Canvas c, Style s, int hx, int hz, int y, int h, int litPct, long seed, boolean door) {
        Rnd r = new Rnd(seed);
        int wall = c(s.wall()), pil = c(s.pillar()), trim = c(s.trim());
        c.fill(-hx - 1, y, -hz - 1, hx + 1, y, hz + 1, c(s.floor()));
        c.fill(-hx, y + 1, -hz, hx, y + h - 1, hz, wall);
        c.fill(-hx + 1, y + 1, -hz + 1, hx - 1, y + h - 1, hz - 1, AIR_C);
        perimeter(c, hx, hz, y + h - 1, trim);
        for (int x = -hx; x <= hx; x++) {
            if (pillarAt(x, hx)) { c.fill(x, y + 1, -hz, x, y + h - 1, -hz, pil); c.fill(x, y + 1, hz, x, y + h - 1, hz, pil); }
        }
        for (int z = -hz; z <= hz; z++) {
            if (pillarAt(z, hz)) { c.fill(-hx, y + 1, z, -hx, y + h - 1, z, pil); c.fill(hx, y + 1, z, hx, y + h - 1, z, pil); }
        }
        int rows = h >= 6 ? 2 : 1;
        for (int x = -hx + 1; x <= hx - 1; x++) {
            if (!windowAt(x, hx)) continue;
            boolean l1 = r.chance(litPct), l2 = r.chance(litPct);
            window(c, x, y + 2, -hz, rows, l1, true);
            if (!(door && Math.abs(x) <= 2)) window(c, x, y + 2, hz, rows, l2, true);
        }
        for (int z = -hz + 1; z <= hz - 1; z++) {
            if (!windowAt(z, hz)) continue;
            boolean l1 = r.chance(litPct), l2 = r.chance(litPct);
            window(c, -hx, y + 2, z, rows, l1, false);
            window(c, hx, y + 2, z, rows, l2, false);
        }
        if (door) {
            c.fill(-1, y + 1, hz, 1, Math.min(y + 3, y + h - 2), hz, AIR_C);
            c.fill(-2, y + 1, hz, -2, y + h - 2, hz, pil);
            c.fill(2, y + 1, hz, 2, y + h - 2, hz, pil);
        }
        // ceiling lanterns (hang from the roof underside at y+h)
        int lan = Cell.lantern(LANTERN, true);
        c.set(0, y + h - 1, 0, lan);
        for (int x = -hx + 4; x <= hx - 4; x += 8)
            for (int z = -hz + 4; z <= hz - 4; z += 8) c.set(x, y + h - 1, z, lan);
        // balcony railing with lantern posts
        int R = hx + 1, Q = hz + 1;
        int fx = Cell.rail(DARK_OAK_FENCE, true, false), fz = Cell.rail(DARK_OAK_FENCE, false, true);
        c.fill(-R, y + 1, -Q, R, y + 1, -Q, fx);
        c.fill(-R, y + 1, Q, R, y + 1, Q, fx);
        c.fill(-R, y + 1, -Q, -R, y + 1, Q, fz);
        c.fill(R, y + 1, -Q, R, y + 1, Q, fz);
        int lp = Cell.lantern(LANTERN, false);
        for (int x = -R; x <= R; x += 3) { c.set(x, y + 2, -Q, lp); c.set(x, y + 2, Q, lp); }
        for (int z = -Q; z <= Q; z += 3) { c.set(-R, y + 2, z, lp); c.set(R, y + 2, z, lp); }
        if (door) c.fill(-1, y + 1, Q, 1, y + 2, Q, AIR_C);
    }

    /** Hip roof. Layer t sits at y+t and is inset by t. Stops with a solid flat cap after maxLayers or at the ridge. */
    static void roof(Canvas c, Style s, int hx, int hz, int y, int over, int maxLayers) {
        int a = hx + over, b = hz + over;
        int block = c(s.roofBlock());
        for (int t = 0; t < maxLayers; t++) {
            int ax = a - t, bz = b - t;
            if (ax < 0 || bz < 0) break;
            int yy = y + t;
            if (t == maxLayers - 1 || ax == 0 || bz == 0) {
                c.fill(-ax, yy, -bz, ax, yy, bz, block);
                break;
            }
            c.fill(-ax + 1, yy, -bz + 1, ax - 1, yy, bz - 1, block);
            c.fill(-ax + 1, yy, -bz, ax - 1, yy, -bz, Cell.stair(s.roofStair(), 2, false));
            c.fill(-ax + 1, yy, bz, ax - 1, yy, bz, Cell.stair(s.roofStair(), 0, false));
            c.fill(-ax, yy, -bz + 1, -ax, yy, bz - 1, Cell.stair(s.roofStair(), 1, false));
            c.fill(ax, yy, -bz + 1, ax, yy, bz - 1, Cell.stair(s.roofStair(), 3, false));
            c.set(-ax, yy, -bz, block); c.set(ax, yy, -bz, block);
            c.set(-ax, yy, bz, block); c.set(ax, yy, bz, block);
        }
        // lanterns hanging from the eaves
        int lan = Cell.lantern(LANTERN, true);
        for (int x = -a; x <= a; x += 4) { c.set(x, y - 1, -b, lan); c.set(x, y - 1, b, lan); }
        for (int z = -b + 4; z <= b - 4; z += 4) { c.set(-a, y - 1, z, lan); c.set(a, y - 1, z, lan); }
    }

    static void finial(Canvas c, int ax, int az, int ry) {
        int ex = ax > az ? ax - az : 0, ez = az > ax ? az - ax : 0;
        c.set(ex, ry + 1, ez, c(GOLD_BLOCK));
        c.set(-ex, ry + 1, -ez, c(GOLD_BLOCK));
        c.fill(0, ry + 1, 0, 0, ry + 4, 0, Cell.rail(DARK_OAK_FENCE, false, false));
        c.set(0, ry + 5, 0, c(GOLD_BLOCK));
        c.set(0, ry + 6, 0, Cell.lantern(LANTERN, false));
    }

    /**
     * Multi-tier pagoda standing on level y0. Each tier shrinks by `shrink` on every side; the roof of tier i ends in a
     * flat cap that tier i+1 sits on. Returns the y of the highest finial block.
     */
    static int pagoda(Canvas c, Style s, int hx, int hz, int floors, int shrink, int h, int y0, long seed, int litPct, boolean door) {
        final int over = 2;
        int y = y0;
        for (int i = 0; i < floors; i++) {
            int ax = hx - i * shrink, az = hz - i * shrink;
            boolean last = i == floors - 1 || hx - (i + 1) * shrink < 2 || hz - (i + 1) * shrink < 2;
            storey(c, s, ax, az, y, h, litPct, seed * 31 + i, door && i == 0);
            roof(c, s, ax, az, y + h, over, last ? 999 : shrink + over);
            if (last) {
                int ry = y + h + Math.min(ax, az) + over;
                finial(c, ax, az, ry);
                return ry + 6;
            }
            y += h + shrink + over - 1;
        }
        return y;
    }
}
