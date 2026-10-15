package com.lanternpalace.infinitycastle.gen;

/** Deterministic description of one floating castle (derived purely from world seed + grid cell). */
final class CastleSpec {
    static final int PAGODA = 0, KEEP = 1, HALL = 2, SHRINE = 3, CITADEL = 4;

    final int i, j, k, cx, cy, cz, rot, type, style;
    final int a, b, floors, px, pz, depth, wx, wz;
    final boolean flip, forced;
    final long seed;

    CastleSpec(long worldSeed, int i, int j, int k, int cx, int cy, int cz, boolean forced) {
        this.i = i; this.j = j; this.k = k; this.cx = cx; this.cy = cy; this.cz = cz; this.forced = forced;
        Rnd r = new Rnd(Rnd.hash(worldSeed, i, j, 200 + k));
        this.seed = r.nextLong();
        this.rot = r.next(4);
        this.flip = !forced && r.chance(30);
        int t = r.next(100);
        this.type = t < 30 ? PAGODA : t < 55 ? KEEP : t < 70 ? HALL : t < 85 ? SHRINE : CITADEL;
        this.style = r.next(4);
        int pa, pb = 0, pf, ppx, ppz, pd;
        switch (type) {
            case PAGODA: pa = 5 + r.next(3); pb = pa; pf = 3 + r.next(3); ppx = pa + 8; ppz = ppx; pd = 12; break;
            case KEEP:   pa = 9 + r.next(3); pb = 8 + r.next(3); pf = 3 + r.next(2); ppx = pa + 13; ppz = pb + 13; pd = 14; break;
            case HALL:   pa = 14 + r.next(7); pb = 6 + r.next(3); pf = 2; ppx = pa + 8; ppz = pb + 10; pd = 12; break;
            case SHRINE: pa = 4; pb = 4; pf = 1; ppx = 14; ppz = 18; pd = 10; break;
            default:     pa = 8; pb = 8; pf = 4; ppx = 27; ppz = 27; pd = 16; break;
        }
        this.a = pa; this.b = pb; this.floors = pf; this.px = ppx; this.pz = ppz; this.depth = pd;
        boolean odd = (rot & 1) == 1;
        this.wx = odd ? ppz : ppx;
        this.wz = odd ? ppx : ppz;
    }

    boolean overlaps(Canvas c) {
        int m = 3;
        return c.overlaps(cx - wx - m, cz - wz - m, cx + wx + m, cz + wz + m);
    }

    void draw(Canvas c) {
        c.transform(cx, cy, cz, rot, flip);
        Castles.build(c, this);
        c.identity();
    }
}
