package com.lanternpalace.infinitycastle.gen;

import java.util.HashMap;
import java.util.Map;

/**
 * Deterministic layout of the whole dimension. Everything is a pure function of (world seed, grid cell), so any
 * chunk can ask "what overlaps me?" independently. Floating castles live in a 96-block grid with 8 vertical bands;
 * neighbouring upright castles are linked by bridges, and four causeways link the palace to the first ring.
 */
public final class WorldPlan {
    static final int CELL = 96;
    static final int[] BAND_Y = {28, 66, 124, 184, 246, 310, 372, 436};
    private static final int[][] FORCED = {{2, 0}, {-3, -1}, {-1, 2}, {0, -3}}; // E, W, S, N ring cells (band 1)

    private static volatile WorldPlan cached;

    public static WorldPlan of(long seed) {
        WorldPlan p = cached;
        if (p == null || p.seed != seed) { p = new WorldPlan(seed); cached = p; }
        return p;
    }

    private final long seed;
    private final Map<Long, CastleSpec> specs = new HashMap<>();
    private final Map<Long, Bridge> bridges = new HashMap<>();
    private final Bridge[] causeways = new Bridge[4];

    private WorldPlan(long seed) {
        this.seed = seed;
        CastleSpec e = spec(2, 0, 1), w = spec(-3, -1, 1), s = spec(-1, 2, 1), n = spec(0, -3, 1);
        int y = Dim.PALACE_Y;
        int h = Palace.HALF - 1;
        Rnd r = new Rnd(seed ^ 0xCA05E);
        causeways[0] = new Bridge(h, y, 0, e.cx - e.wx + 1, e.cy, e.cz + r.next(7) - 3, true, false);
        causeways[1] = new Bridge(-h, y, 0, w.cx + w.wx - 1, w.cy, w.cz + r.next(7) - 3, true, false);
        causeways[2] = new Bridge(0, y, h, s.cx + r.next(7) - 3, s.cy, s.cz - s.wz + 1, false, false);
        causeways[3] = new Bridge(0, y, -h, n.cx + r.next(7) - 3, n.cy, n.cz + n.wz - 1, false, false);
    }

    private static long key(int i, int j, int k) { return ((long) (i & 0xFFFFF) << 32) | ((long) (j & 0xFFFFF) << 8) | k; }

    private static boolean excluded(int i, int j) { return i >= -2 && i <= 1 && j >= -2 && j <= 1; }

    private static boolean forced(int i, int j) {
        for (int[] f : FORCED) if (f[0] == i && f[1] == j) return true;
        return false;
    }

    private int primary(int i, int j) {
        if (forced(i, j)) return 1;
        long g = Rnd.hash(seed, i >> 1, j >> 1, 11);
        int p = (int) ((g >>> 33) % 8);
        long h = Rnd.hash(seed, i, j, 12);
        if (((h >>> 33) % 100) < 30) p = (int) ((h >>> 40) % 8);
        return p;
    }

    private int secondary(int i, int j, int p) {
        long h = Rnd.hash(seed, i, j, 13);
        if (((h >>> 33) % 100) >= 45) return -1;
        int cnt = 0;
        for (int b = 0; b < 8; b++) if (Math.abs(b - p) >= 2) cnt++;
        int pick = (int) ((h >>> 40) % cnt);
        for (int b = 0; b < 8; b++) if (Math.abs(b - p) >= 2 && pick-- == 0) return b;
        return -1;
    }

    synchronized CastleSpec spec(int i, int j, int k) {
        long key = key(i, j, k);
        if (specs.size() > 40000) { specs.clear(); bridges.clear(); }
        if (specs.containsKey(key)) return specs.get(key);
        CastleSpec out = null;
        if (!excluded(i, j)) {
            int p = primary(i, j);
            if (k == p || k == secondary(i, j, p)) {
                Rnd r = new Rnd(Rnd.hash(seed, i, j, 100 + k));
                int cx = i * CELL + CELL / 2 + r.next(29) - 14;
                int cz = j * CELL + CELL / 2 + r.next(29) - 14;
                int cy = BAND_Y[k] + (k == 1 ? 0 : r.next(13) - 6);
                out = new CastleSpec(seed, i, j, k, cx, cy, cz, forced(i, j) && k == 1);
            }
        }
        specs.put(key, out);
        return out;
    }

    private CastleSpec target(CastleSpec a, int di, int dj) {
        CastleSpec best = null;
        int bestDy = Integer.MAX_VALUE;
        for (int k = 0; k < 8; k++) {
            CastleSpec b = spec(a.i + di, a.j + dj, k);
            if (b == null || b.flip) continue;
            int dy = Math.abs(b.cy - a.cy);
            if (dy <= 70 && dy < bestDy) { best = b; bestDy = dy; }
        }
        return best;
    }

    private synchronized Bridge bridge(CastleSpec a, boolean east) {
        long key = (key(a.i, a.j, a.k) << 1) | (east ? 1 : 0);
        if (bridges.containsKey(key)) return bridges.get(key);
        Bridge out = null;
        CastleSpec b = east ? target(a, 1, 0) : target(a, 0, 1);
        if (b != null) {
            Rnd r = new Rnd(Rnd.hash(seed, a.i * 7 + b.i, a.j * 13 + b.j, a.k * 5 + b.k));
            int oa = r.next(7) - 3, ob = r.next(7) - 3;
            boolean covered = r.chance(35);
            if (east) {
                int ax = a.cx + a.wx - 1, bx = b.cx - b.wx + 1;
                if (bx - ax >= 8) out = new Bridge(ax, a.cy, a.cz + oa, bx, b.cy, b.cz + ob, true, covered);
            } else {
                int az = a.cz + a.wz - 1, bz = b.cz - b.wz + 1;
                if (bz - az >= 8) out = new Bridge(a.cx + oa, a.cy, az, b.cx + ob, b.cy, bz, false, covered);
            }
        }
        bridges.put(key, out);
        return out;
    }

    /** Fill the canvas (one chunk) with everything that overlaps it. Pure: depends only on seed + chunk coords. */
    public void generate(Canvas c) {
        Palace.draw(c);
        int i0 = Math.floorDiv(c.minX, CELL) - 1, i1 = Math.floorDiv(c.minX + 15, CELL) + 1;
        int j0 = Math.floorDiv(c.minZ, CELL) - 1, j1 = Math.floorDiv(c.minZ + 15, CELL) + 1;
        for (int i = i0; i <= i1; i++)
            for (int j = j0; j <= j1; j++)
                for (int k = 0; k < 8; k++) {
                    CastleSpec sp = spec(i, j, k);
                    if (sp != null && sp.overlaps(c)) sp.draw(c);
                }
        for (int i = i0; i <= i1; i++)
            for (int j = j0; j <= j1; j++)
                for (int k = 0; k < 8; k++) {
                    CastleSpec a = spec(i, j, k);
                    if (a == null || a.flip) continue;
                    Bridge e = bridge(a, true);
                    if (e != null && e.overlaps(c)) e.draw(c);
                    Bridge s = bridge(a, false);
                    if (s != null && s.overlaps(c)) s.draw(c);
                }
        for (Bridge b : causeways) if (b.overlaps(c)) b.draw(c);
    }

    /** Number of floating castles whose centre lies within a square of the given half-size. */
    public int countCastles(int halfSize) {
        int r = halfSize / CELL + 1, n = 0;
        for (int i = -r; i <= r; i++) for (int j = -r; j <= r; j++) for (int k = 0; k < 8; k++) {
            CastleSpec s = spec(i, j, k);
            if (s != null && Math.abs(s.cx) <= halfSize && Math.abs(s.cz) <= halfSize) n++;
        }
        return n;
    }
}
