package com.lanternpalace.infinitycastle.gen;

/** Small deterministic PRNG (SplitMix64) plus a coordinate hash. No shared state, safe for worldgen threads. */
public final class Rnd {
    private long s;

    public Rnd(long seed) { this.s = seed; }

    private static long mix(long z) {
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }

    public long nextLong() {
        s += 0x9E3779B97F4A7C15L;
        return mix(s);
    }

    /** Uniform-ish int in [0, bound). */
    public int next(int bound) { return (int) ((nextLong() >>> 33) % bound); }

    public boolean chance(int percent) { return next(100) < percent; }

    public static long hash(long seed, long a, long b, long c) {
        long h = mix(seed ^ 0x5DEECE66DL);
        h = mix(h + a * 0x9E3779B97F4A7C15L);
        h = mix(h ^ (b * 0xC2B2AE3D27D4EB4FL));
        h = mix(h + c * 0x165667B19E3779F9L);
        return h;
    }
}
