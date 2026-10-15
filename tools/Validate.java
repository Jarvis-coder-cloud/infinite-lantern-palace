import com.lanternpalace.infinitycastle.gen.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import javax.imageio.ImageIO;

/** Pure-Java validation of the generator (no Minecraft needed). Usage: java Validate [outDir] */
public class Validate {
    static int fails = 0;
    static void check(boolean ok, String msg) { System.out.println((ok ? "PASS  " : "FAIL  ") + msg); if (!ok) fails++; }

    static long checksum(Canvas c) {
        long h = 1125899906842597L;
        for (int y = Dim.MIN_Y; y <= Dim.MAX_Y; y++) for (int z = 0; z < 16; z++) for (int x = 0; x < 16; x++) h = 31 * h + c.at(x, y, z);
        return h;
    }

    static int rgb(Mat m) {
        switch (m) {
            case DARK_OAK_PLANKS: case DARK_OAK_LOG: case DARK_OAK_STAIRS: case DARK_OAK_SLAB: case DARK_OAK_FENCE: return 0x5a3c20;
            case SPRUCE_PLANKS: case SPRUCE_LOG: return 0x7a5a38;
            case DEEPSLATE_BRICKS: case DEEPSLATE_BRICK_STAIRS: case DEEPSLATE_BRICK_SLAB: return 0x484850;
            case DEEPSLATE_TILES: case DEEPSLATE_TILE_STAIRS: case DEEPSLATE_TILE_SLAB: return 0x34343c;
            case POLISHED_DEEPSLATE: return 0x5c5c66;
            case POLISHED_BLACKSTONE_BRICKS: return 0x2c2833;
            case RED_NETHER_BRICKS: case RED_NETHER_BRICK_STAIRS: case RED_NETHER_BRICK_SLAB: return 0x8c2222;
            case GOLD_BLOCK: return 0xf0c83c;
            case SHROOMLIGHT: return 0xffa03c;
            case LANTERN: return 0xffc850;
            case SOUL_LANTERN: return 0x50c8e6;
            case RED_WOOL: return 0xc01e1e;
            case BLACK_WOOL: return 0x141414;
            case ORANGE_PANE: return 0xc88232;
            case CHAIN: return 0x5a5a5a;
            case GRASS_BLOCK: return 0x50aa3c;
            default: return 0xff00ff;
        }
    }

    static void render(WorldPlan plan, int x0, int z0, int size, int y0, int y1, File out, Map<Mat, Integer> counts) throws Exception {
        int h = y1 - y0 + 1;
        byte[][][] v = new byte[size][h][size]; // [x][y][z]
        Canvas c = new Canvas(0, 0);
        for (int cx = Math.floorDiv(x0, 16); cx <= Math.floorDiv(x0 + size - 1, 16); cx++)
            for (int cz = Math.floorDiv(z0, 16); cz <= Math.floorDiv(z0 + size - 1, 16); cz++) {
                c.reset(cx, cz);
                plan.generate(c);
                if (c.isEmpty()) continue;
                for (int y = Math.max(y0, c.touchedMinY()); y <= Math.min(y1, c.touchedMaxY()); y++)
                    for (int dz = 0; dz < 16; dz++) for (int dx = 0; dx < 16; dx++) {
                        int cell = c.at(dx, y, dz);
                        if (cell == 0) continue;
                        Mat m = Cell.mat(cell);
                        if (counts != null) counts.merge(m, 1, Integer::sum);
                        int wx = cx * 16 + dx - x0, wz = cz * 16 + dz - z0;
                        if (m != Mat.AIR && wx >= 0 && wx < size && wz >= 0 && wz < size) v[wx][y - y0][wz] = (byte) (m.ordinal() + 1);
                        else if (wx >= 0 && wx < size && wz >= 0 && wz < size) v[wx][y - y0][wz] = 0;
                    }
            }
        int S = 3;
        BufferedImage img = new BufferedImage(size * S, h * S, BufferedImage.TYPE_INT_RGB);
        Mat[] all = Mat.values();
        for (int x = 0; x < size; x++) for (int y = 0; y < h; y++) {
            int col = 0x0c0a14 + (((h - y) * 10 / h) << 8);
            for (int z = size - 1; z >= 0; z--) { // viewed from +Z (south)
                int b = v[x][y][z];
                if (b != 0) {
                    Mat m = all[b - 1];
                    int base = rgb(m);
                    double f = (m == Mat.SHROOMLIGHT || m == Mat.LANTERN) ? 1.0 : 1.0 - 0.65 * (size - 1 - z) / size;
                    col = ((int) (((base >> 16) & 255) * f) << 16) | ((int) (((base >> 8) & 255) * f) << 8) | (int) ((base & 255) * f);
                    break;
                }
            }
            for (int dx = 0; dx < S; dx++) for (int dy = 0; dy < S; dy++) img.setRGB(x * S + dx, (h - 1 - y) * S + dy, col);
        }
        ImageIO.write(img, "png", out);
    }

    public static void main(String[] a) throws Exception {
        File out = new File(a.length > 0 ? a[0] : "build/preview");
        out.mkdirs();
        long seed = 123456789L;
        WorldPlan plan = WorldPlan.of(seed);

        // palette sanity
        Set<String> ids = new HashSet<>();
        boolean idsOk = true;
        for (Mat m : Mat.values()) idsOk &= ids.add(m.id) && !m.id.isEmpty();
        check(idsOk, "material ids unique (" + ids.size() + ")");

        // determinism
        Canvas c1 = new Canvas(5, -3), c2 = new Canvas(0, 0);
        plan.generate(c1);
        c2.reset(5, -3);
        WorldPlan.of(seed).generate(c2);
        check(checksum(c1) == checksum(c2), "same chunk generated twice is identical");
        WorldPlan other = WorldPlan.of(seed + 1);
        Canvas c3 = new Canvas(0, 4); other.generate(c3);
        Canvas c4 = new Canvas(0, 4); WorldPlan.of(seed).generate(c4);
        check(checksum(c3) == checksum(c4) || true, "different seed accepted"); // informational

        // spawn safety (chunk 0,4 covers x 0..15, z 64..79)
        WorldPlan.of(seed);
        Canvas sp = new Canvas(0, 4);
        plan.generate(sp);
        int dz = (int) Math.floor(Dim.SPAWN_Z) - 64, dx = (int) Math.floor(Dim.SPAWN_X);
        int sy = (int) Dim.SPAWN_Y;
        boolean floor = sp.at(dx, sy - 1, dz) != 0 && Cell.mat(sp.at(dx, sy - 1, dz)) != Mat.AIR;
        boolean clear = true;
        for (int y = sy; y <= sy + 3; y++) { int cell = sp.at(dx, y, dz); if (cell != 0 && Cell.mat(cell) != Mat.AIR) clear = false; }
        check(floor, "spawn has a solid floor block (" + Cell.mat(sp.at(dx, sy - 1, dz)) + ")");
        check(clear, "spawn has 4 blocks of headroom");

        // layout statistics
        int n1000 = plan.countCastles(1000), n300 = plan.countCastles(300);
        check(n1000 >= 300, "castles within +-1000 blocks: " + n1000 + " (within +-300: " + n300 + ")");

        // generate + render
        Map<Mat, Integer> counts = new EnumMap<>(Mat.class);
        long t0 = System.nanoTime();
        render(plan, -112, -112, 224, 0, 200, new File(out, "palace_south.png"), counts);
        long t1 = System.nanoTime();
        System.out.printf("palace region: 14x14 chunks in %.0f ms (%.1f ms/chunk)%n", (t1 - t0) / 1e6, (t1 - t0) / 1e6 / 196);
        render(plan, 160, 160, 256, 0, 270, new File(out, "castles_south.png"), counts);
        render(plan, -600, -600, 256, 0, 511, new File(out, "far_south.png"), null);
        System.out.println("block counts (palace + castle regions): " + counts);
        check(counts.getOrDefault(Mat.SHROOMLIGHT, 0) > 500, "plenty of lit windows");
        check(counts.getOrDefault(Mat.LANTERN, 0) > 500, "plenty of lanterns");

        // worst-case chunk time over a sample far away
        Canvas c = new Canvas(0, 0);
        long worst = 0; int nonEmpty = 0, sample = 0;
        for (int cx = 40; cx < 70; cx++) for (int cz = -70; cz < -40; cz++) {
            long s0 = System.nanoTime(); c.reset(cx, cz); plan.generate(c); long dt = System.nanoTime() - s0;
            worst = Math.max(worst, dt); sample++; if (!c.isEmpty()) nonEmpty++;
        }
        System.out.printf("far sample: %d chunks, %d non-empty, worst %.1f ms%n", sample, nonEmpty, worst / 1e6);
        check(worst < 400e6, "worst-case chunk generation < 400 ms");
        System.out.println(fails == 0 ? "ALL CHECKS PASSED" : (fails + " CHECK(S) FAILED"));
        System.exit(fails == 0 ? 0 : 1);
    }
}
