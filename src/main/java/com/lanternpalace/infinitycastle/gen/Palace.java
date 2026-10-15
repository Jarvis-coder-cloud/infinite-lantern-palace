package com.lanternpalace.infinitycastle.gen;

import static com.lanternpalace.infinitycastle.gen.Mat.*;

/** The central palace, drawn in world coordinates around (0,0). Front (courtyard, spawn) is +Z. */
final class Palace {
    private Palace() {}

    static final int HALF = 90;       // outer terrace half-size
    static final int Y = Dim.PALACE_Y;

    static void draw(Canvas c) {
        if (!c.overlaps(-HALF - 12, -HALF - 12, HALF + 12, HALF + 12)) return;
        c.identity();
        Style royal = Style.ALL[4], dark = Style.ALL[0], crim = Style.ALL[1];

        // ---- inverted cone underneath the great terrace
        for (int y = Y - 1; y >= 4; y--) {
            int d = Y - y;
            int half = Math.max(18, HALF - (d * 8) / 5);
            Mat m = d % 7 == 0 ? POLISHED_DEEPSLATE : (d % 2 == 0 ? DEEPSLATE_BRICKS : DEEPSLATE_TILES);
            c.fill(-half, y, -half, half, y, half, Build.c(m));
        }
        c.fill(0, -8, 0, 0, 3, 0, Build.c(CHAIN));
        c.set(0, -9, 0, Cell.lantern(LANTERN, true));

        // ---- terrace 1
        c.fill(-HALF, Y, -HALF, HALF, Y, HALF, Build.c(DEEPSLATE_TILES));
        Build.perimeter(c, HALF, HALF, Y, Build.c(POLISHED_DEEPSLATE));
        c.fill(-2, Y, -HALF + 1, 2, Y, HALF - 1, Build.c(DARK_OAK_PLANKS));
        c.fill(-HALF + 1, Y, -2, HALF - 1, Y, 2, Build.c(DARK_OAK_PLANKS));
        // friendly lawns for the peaceful animals
        c.fill(10, Y, 62, 40, Y, 76, Build.c(GRASS_BLOCK));
        c.fill(-40, Y, 62, -10, Y, 76, Build.c(GRASS_BLOCK));
        // spawn marker
        c.fill(-1, Y, 68, 1, Y, 70, Build.c(RED_NETHER_BRICKS));
        c.set(0, Y, 69, Build.c(GOLD_BLOCK));
        Build.rim(c, HALF, HALF, Y, 4);
        for (int x = -84; x <= 84; x += 14) { Build.banner(c, x, Y - 1, HALF, true, 9); Build.banner(c, x, Y - 1, -HALF, true, 9); }
        for (int z = -84; z <= 84; z += 14) { Build.banner(c, HALF, Y - 1, z, false, 9); Build.banner(c, -HALF, Y - 1, z, false, 9); }
        for (int v = -81; v <= 81; v += 6) {
            Build.hangLantern(c, v, Y - 1, HALF, 3); Build.hangLantern(c, v, Y - 1, -HALF, 3);
            Build.hangLantern(c, HALF, Y - 1, v, 3); Build.hangLantern(c, -HALF, Y - 1, v, 3);
        }
        // lantern post grid
        for (int x = -80; x <= 80; x += 10) {
            for (int z = -80; z <= 80; z += 10) {
                int ax = Math.abs(x), az = Math.abs(z);
                if (x == 0 || z == 0) continue;
                if (ax <= 62 && az <= 62) continue;
                if (ax >= 70 && az >= 70) continue;
                if (ax <= 20 && z >= 76) continue;
                Build.lampPost(c, x, Y + 1, z);
            }
        }

        // ---- terrace 2 and the red stairway
        c.fill(-54, Y + 1, -54, 54, Y + 5, 54, Build.c(DEEPSLATE_BRICKS));
        c.fill(-54, Y + 6, -54, 54, Y + 6, 54, Build.c(DEEPSLATE_TILES));
        Build.perimeter(c, 54, 54, Y + 4, Build.c(RED_NETHER_BRICKS));
        Build.rim(c, 54, 54, Y + 6, 4);
        for (int n = 0; n < 6; n++) {
            int z = 60 - n;
            if (n >= 1) c.fill(-5, Y + 1, z, 5, Y + n, z, Build.c(RED_NETHER_BRICKS));
            c.fill(-5, Y + 1 + n, z, 5, Y + 1 + n, z, Cell.stair(RED_NETHER_BRICK_STAIRS, 0, false));
        }
        c.fill(-5, Y + 7, 54, 5, Y + 8, 54, Build.AIR_C);
        for (int n = 0; n < 6; n += 2) { Build.lampPost(c, -7, Y + 1 + n, 60 - n); Build.lampPost(c, 7, Y + 1 + n, 60 - n); }

        // ---- central tower (9 tiers) on a stepped podium
        c.offset(0, -16);
        Build.podium(c, royal, 21, 21, Y + 6, 3);
        Build.pagoda(c, royal, 21, 21, 9, 2, 7, Y + 9, 7001, 80, false);
        // ---- great hall in front of it
        c.offset(0, 28);
        Build.pagoda(c, royal, 34, 12, 2, 3, 7, Y + 6, 7002, 80, true);
        // ---- side halls
        for (int sx = -1; sx <= 1; sx += 2) {
            c.offset(sx * 44, -6);
            Build.pagoda(c, dark, 8, 18, 3, 2, 6, Y + 6, 7003 + sx, 75, false);
        }
        // ---- terrace-2 corner pagodas
        int[][] corner2 = {{-46, -46}, {46, -46}, {-46, 44}, {46, 44}};
        for (int q = 0; q < 4; q++) {
            c.offset(corner2[q][0], corner2[q][1]);
            Build.pagoda(c, crim, 5, 5, 5, 1, 6, Y + 6, 7010 + q, 70, false);
        }
        // ---- terrace-1 corner pagodas
        int[][] corner1 = {{-76, -76}, {76, -76}, {-76, 76}, {76, 76}};
        for (int q = 0; q < 4; q++) {
            c.offset(corner1[q][0], corner1[q][1]);
            Build.pagoda(c, q % 2 == 0 ? crim : dark, 6, 6, 5, 1, 6, Y, 7020 + q, 70, false);
        }
        // ---- south gate house with a passage through it
        c.offset(0, 84);
        Build.pagoda(c, royal, 16, 5, 2, 2, 7, Y, 7030, 85, false);
        c.offset(0, 0);
        c.fill(-3, Y + 1, 78, 3, Y + 5, HALF + 1, Build.AIR_C);

        // ---- torii procession and lamp lines in the courtyard
        Build.torii(c, 0, Y + 1, 64, true, 6, 10);
        Build.torii(c, 0, Y + 1, 74, true, 5, 9);
        for (int z = 62; z <= 80; z += 6) { Build.lampPost(c, -9, Y + 1, z); Build.lampPost(c, 9, Y + 1, z); }
        c.identity();
    }
}
