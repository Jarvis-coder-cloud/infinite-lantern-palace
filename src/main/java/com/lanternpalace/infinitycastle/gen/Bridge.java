package com.lanternpalace.infinitycastle.gen;

import static com.lanternpalace.infinitycastle.gen.Mat.*;

/**
 * A Z-shaped bridge (three axis-aligned legs) whose deck rises/falls with stair steps. Open bridges get rails,
 * lanterns, hanging banners and torii; covered bridges become lantern-lit corridors.
 */
final class Bridge {
    private final int n;
    private final int[] xs, zs, ys, dxs, dzs;
    private final boolean[] corner, nearCorner;
    private final boolean covered;
    private final int minX, maxX, minZ, maxZ;

    Bridge(int ax, int ay, int az, int bx, int by, int bz, boolean xFirst, boolean covered) {
        this.covered = covered;
        int[] px = {ax, 0, 0, bx}, pz = {az, 0, 0, bz};
        if (xFirst) { int m = (ax + bx) / 2; px[1] = m; pz[1] = az; px[2] = m; pz[2] = bz; }
        else { int m = (az + bz) / 2; px[1] = ax; pz[1] = m; px[2] = bx; pz[2] = m; }
        int total = 0;
        for (int s = 0; s < 3; s++) total += Math.abs(px[s + 1] - px[s]) + Math.abs(pz[s + 1] - pz[s]);
        n = total + 1;
        xs = new int[n]; zs = new int[n]; ys = new int[n]; dxs = new int[n]; dzs = new int[n];
        corner = new boolean[n]; nearCorner = new boolean[n];
        int d = 0;
        for (int s = 0; s < 3; s++) {
            int ddx = px[s + 1] - px[s], ddz = pz[s + 1] - pz[s];
            int len = Math.abs(ddx) + Math.abs(ddz);
            int sx = Integer.signum(ddx), sz = Integer.signum(ddz);
            for (int t = 0; t < len; t++) { xs[d] = px[s] + sx * t; zs[d] = pz[s] + sz * t; dxs[d] = sx; dzs[d] = sz; d++; }
        }
        xs[d] = bx; zs[d] = bz;
        dxs[d] = d > 0 ? dxs[d - 1] : 1; dzs[d] = d > 0 ? dzs[d - 1] : 0;
        int x0 = Integer.MAX_VALUE, x1 = Integer.MIN_VALUE, z0 = Integer.MAX_VALUE, z1 = Integer.MIN_VALUE;
        for (int q = 0; q < n; q++) {
            ys[q] = ay + (int) Math.round((double) (by - ay) * q / Math.max(1, n - 1));
            if (q > 0 && q < n - 1 && (dxs[q] != dxs[q - 1] || dzs[q] != dzs[q - 1])) corner[q] = true;
            x0 = Math.min(x0, xs[q]); x1 = Math.max(x1, xs[q]); z0 = Math.min(z0, zs[q]); z1 = Math.max(z1, zs[q]);
        }
        for (int q = 0; q < n; q++) if (corner[q]) for (int w = Math.max(0, q - 7); w <= Math.min(n - 1, q + 7); w++) nearCorner[w] = true;
        minX = x0 - 8; maxX = x1 + 8; minZ = z0 - 8; maxZ = z1 + 8;
    }

    boolean overlaps(Canvas c) { return c.overlaps(minX, minZ, maxX, maxZ); }

    private static int facing(int dx, int dz) { return dz < 0 ? 0 : dx > 0 ? 1 : dz > 0 ? 2 : 3; }

    void draw(Canvas c) {
        c.identity();
        final int planks = Build.c(DARK_OAK_PLANKS), log = Build.c(DARK_OAK_LOG);
        final int roofBlock = Build.c(DEEPSLATE_TILES);
        final int cMinX = c.minX - 8, cMaxX = c.minX + 23, cMinZ = c.minZ - 8, cMaxZ = c.minZ + 23;
        final int headroom = covered ? 3 : 2;
        for (int d = 0; d < n; d++) {
            int x = xs[d], z = zs[d];
            if (x < cMinX || x > cMaxX || z < cMinZ || z > cMaxZ) continue;
            int dx = dxs[d], dz = dzs[d], y = ys[d];
            boolean alongX = dx != 0;
            int f = facing(dx, dz);
            boolean up = d > 0 && y > ys[d - 1] && !corner[d];
            boolean down = d < n - 1 && ys[d + 1] < y && !corner[d];
            int deck = up ? Cell.stair(DARK_OAK_STAIRS, f, false) : down ? Cell.stair(DARK_OAK_STAIRS, (f + 2) & 3, false) : planks;
            for (int l = -2; l <= 2; l++) c.set(alongX ? x : x + l, y, alongX ? z + l : z, deck);
            if (corner[d]) c.fill(x - 2, y, z - 2, x + 2, y, z + 2, planks);
            // rails + lanterns
            int rail = Cell.rail(DARK_OAK_FENCE, alongX, !alongX);
            for (int s = -2; s <= 2; s += 4) {
                int rx = alongX ? x : x + s, rz = alongX ? z + s : z;
                c.set(rx, y + 1, rz, rail);
                if (d % 4 == 0) c.set(rx, y + 2, rz, Cell.lantern(LANTERN, false));
            }
            // supports + hanging lantern under the deck
            if (d % 8 == 4 && !corner[d]) {
                for (int l = -2; l <= 2; l++) c.set(alongX ? x : x + l, y - 1, alongX ? z + l : z, planks);
                for (int s = -2; s <= 2; s += 4) c.fill(alongX ? x : x + s, y - 3, alongX ? z + s : z, alongX ? x : x + s, y - 2, alongX ? z + s : z, Cell.rail(DARK_OAK_FENCE, false, false));
                Build.hangLantern(c, x, y - 2, z, 2);
            }
            if (!covered) {
                if (d % 16 == 8 && !nearCorner[d]) {
                    Build.banner(c, alongX ? x : x + 3, y, alongX ? z + 3 : z, alongX, 7);
                    Build.banner(c, alongX ? x : x - 3, y, alongX ? z - 3 : z, alongX, 7);
                }
                if (d % 28 == 14 && d > 12 && d < n - 12 && !nearCorner[d]) Build.torii(c, x, y + 1, z, !alongX, 3, 8);
            } else {
                if (d % 4 == 0 && !corner[d]) {
                    for (int s = -2; s <= 2; s += 4) c.fill(alongX ? x : x + s, y + 1, alongX ? z + s : z, alongX ? x : x + s, y + 4, alongX ? z + s : z, log);
                }
                for (int s = -2; s <= 2; s += 4) c.set(alongX ? x : x + s, y + 4, alongX ? z + s : z, planks);
                // roof across the width
                int lowF = alongX ? 2 : 1, highF = alongX ? 0 : 3;
                c.set(alongX ? x : x - 3, y + 5, alongX ? z - 3 : z, Cell.stair(DEEPSLATE_TILE_STAIRS, lowF, false));
                c.set(alongX ? x : x + 3, y + 5, alongX ? z + 3 : z, Cell.stair(DEEPSLATE_TILE_STAIRS, highF, false));
                for (int l = -2; l <= 2; l++) c.set(alongX ? x : x + l, y + 5, alongX ? z + l : z, roofBlock);
                c.set(x, y + 6, z, Cell.slab(DEEPSLATE_TILE_SLAB, false));
                if (d % 4 == 2) c.set(x, y + 4, z, Cell.lantern(LANTERN, true));
                if (corner[d]) {
                    c.fill(x - 3, y + 5, z - 3, x + 3, y + 5, z + 3, roofBlock);
                    for (int sx = -2; sx <= 2; sx += 4) for (int sz = -2; sz <= 2; sz += 4) c.fill(x + sx, y + 1, z + sz, x + sx, y + 4, z + sz, log);
                }
            }
        }
        // pass 2: keep the walking lane clear (rails of one leg must not block the neighbouring leg)
        for (int d = 0; d < n; d++) {
            int x = xs[d], z = zs[d];
            if (x < cMinX || x > cMaxX || z < cMinZ || z > cMaxZ) continue;
            boolean alongX = dxs[d] != 0;
            int y = ys[d];
            if (alongX) c.fill(x, y + 1, z - 1, x, y + headroom, z + 1, Build.AIR_C);
            else c.fill(x - 1, y + 1, z, x + 1, y + headroom, z, Build.AIR_C);
        }
    }
}
