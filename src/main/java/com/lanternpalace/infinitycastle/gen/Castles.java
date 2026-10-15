package com.lanternpalace.infinitycastle.gen;

import static com.lanternpalace.infinitycastle.gen.Mat.*;

/** The five castle families. Local origin = platform centre, surface at y=0, front = +Z. */
final class Castles {
    private Castles() {}

    static void build(Canvas c, CastleSpec sp) {
        Style s = Style.ALL[sp.style];
        Style s2 = Style.ALL[(sp.style + 1) % 4];
        long seed = sp.seed;
        Build.island(c, s, sp.px, sp.pz, sp.depth);
        Build.rim(c, sp.px, sp.pz, 0, 4);
        switch (sp.type) {
            case CastleSpec.PAGODA: {
                Build.podium(c, s, sp.a, sp.a, 0, 2);
                Build.pagoda(c, s, sp.a, sp.a, sp.floors, 1, 5, 2, seed, 65, true);
                int q = sp.px - 3;
                for (int sx = -1; sx <= 1; sx += 2) for (int sz = -1; sz <= 1; sz += 2) Build.lampPost(c, sx * q, 1, sz * q);
                Build.torii(c, 0, 1, sp.a + 6, true, 3, 7);
                break;
            }
            case CastleSpec.KEEP: {
                Build.podium(c, s, sp.a, sp.b, 0, 3);
                Build.pagoda(c, s, sp.a, sp.b, sp.floors, 2, 5, 3, seed, 60, true);
                for (int sx = -1; sx <= 1; sx += 2) {
                    c.offset(sx * (sp.px - 5), -(sp.pz - 5));
                    Build.pagoda(c, s2, 3, 3, 3, 1, 5, 1, seed + 7, 55, false);
                }
                c.offset(0, 0);
                Build.torii(c, 0, 1, sp.b + 8, true, 4, 8);
                Build.lampPost(c, -6, 1, sp.b + 6); Build.lampPost(c, 6, 1, sp.b + 6);
                break;
            }
            case CastleSpec.HALL: {
                Build.podium(c, s, sp.a, sp.b, 0, 2);
                Build.pagoda(c, s, sp.a, sp.b, 2, 3, 6, 2, seed, 70, true);
                Build.torii(c, 0, 1, sp.b + 7, true, 4, 8);
                for (int x = -sp.a; x <= sp.a; x += 6) { Build.lampPost(c, x, 1, sp.b + 5); Build.lampPost(c, x, 1, -(sp.b + 5)); }
                break;
            }
            case CastleSpec.SHRINE: {
                c.offset(0, -6);
                Build.podium(c, s, 4, 4, 0, 1);
                Build.pagoda(c, s, 4, 4, 1, 1, 5, 1, seed, 70, true);
                c.offset(0, 0);
                for (int z = 4; z <= 14; z += 5) Build.torii(c, 0, 1, z, true, 3, 7);
                Build.lampPost(c, -5, 1, 7); Build.lampPost(c, 5, 1, 7);
                Build.lampPost(c, -5, 1, 12); Build.lampPost(c, 5, 1, 12);
                break;
            }
            default: { // CITADEL
                for (int y = 1; y <= 3; y++) Build.perimeter(c, 22, 22, y, Build.c(DEEPSLATE_BRICKS));
                int lan = Cell.lantern(LANTERN, false);
                for (int x = -22; x <= 22; x += 3) { c.set(x, 4, -22, lan); c.set(x, 4, 22, lan); }
                for (int z = -19; z <= 19; z += 3) { c.set(-22, 4, z, lan); c.set(22, 4, z, lan); }
                c.fill(-2, 1, 22, 2, 4, 22, Build.AIR_C);
                Build.torii(c, 0, 1, 25, true, 3, 7);
                Build.podium(c, s, 8, 8, 0, 3);
                Build.pagoda(c, s, 8, 8, 4, 2, 5, 3, seed, 60, true);
                for (int sx = -1; sx <= 1; sx += 2) for (int sz = -1; sz <= 1; sz += 2) {
                    c.offset(sx * 18, sz * 18);
                    Build.pagoda(c, s2, 4, 4, 4, 1, 5, 1, seed + 11 + sx + 3 * sz, 60, false);
                }
                c.offset(0, 0);
                break;
            }
        }
        // banners hanging from the platform edge
        Build.banner(c, sp.px, -1, -1, false, 8);
        Build.banner(c, -sp.px, -1, -1, false, 8);
        Build.banner(c, -1, -1, sp.pz, true, 8);
        Build.banner(c, -1, -1, -sp.pz, true, 8);
    }
}
