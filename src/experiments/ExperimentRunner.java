package experiments;

import algorithms.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.function.LongSupplier;

public class ExperimentRunner {

    static final long SEED = 42;
    static final String ALPHABET = "abcd";
    static final int WARMUP_RUNS = 3;
    static final int TIMED_RUNS = 5;

    static final int[] DP_SIZES = {100, 500, 1000};
    static final int[] TEXT_SIZES = {10_000, 50_000, 100_000};
    static final int[] KASAI_SIZES = {1000, 5000, 10_000};
    static final int PATTERN_LENGTH = 50;
    static final int ADVERSARIAL_PATTERN_LENGTH = 100;

    private static final List<String> rows = new ArrayList<>();
    private static volatile long sink;

    public static void runAll(String csvFile) throws IOException {
        rows.clear();
        warmUpJvm();
        runDynamicProgramming();
        runStringMatching();
        runKasai();
        writeCsv(csvFile);
    }

    private static double timeMs(LongSupplier task) {
        for (int i = 0; i < WARMUP_RUNS; i++) {
            sink += task.getAsLong();
        }
        long total = 0;
        for (int i = 0; i < TIMED_RUNS; i++) {
            long start = System.nanoTime();
            long value = task.getAsLong();
            long end = System.nanoTime();
            total += end - start;
            sink += value;
        }
        return total / (double) TIMED_RUNS / 1_000_000.0;
    }

    private static void warmUpJvm() {
        System.out.println("Warming up the JVM...");
        Random rnd = new Random(SEED + 999);
        String a = randomString(600, rnd);
        String b = randomString(600, rnd);
        String text = randomString(30_000, rnd);
        String pattern = text.substring(15_000, 15_040);
        String s = randomString(4_000, rnd);
        int[] sa = SuffixArrayBuilder.build(s);
        for (int round = 0; round < 50; round++) {
            sink += LCS.findLength(a, b) + EditDistance.calculate(a, b);
            sink += NaiveSearch.search(text, pattern).size();
            sink += KMP.search(text, pattern).size();
            sink += RabinKarp.search(text, pattern).size();
            sink += Kasai.buildLCP(s, sa)[s.length() / 2];
        }
    }


    static String randomString(int length, Random rnd) {
        char[] chars = new char[length];
        for (int i = 0; i < length; i++) {
            chars[i] = ALPHABET.charAt(rnd.nextInt(ALPHABET.length()));
        }
        return new String(chars);
    }

    private static String absentPattern(String text, int length, Random rnd) {
        while (true) {
            String candidate = randomString(length, rnd);
            if (NaiveSearch.search(text, candidate).isEmpty()) {
                return candidate;
            }
        }
    }


    private static void runDynamicProgramming() {
        System.out.println("== LCS and Edit Distance ==");
        for (int n : DP_SIZES) {
            Random rnd = new Random(SEED + n);
            String a = randomString(n, rnd);
            String b = randomString(n, rnd);
            long cells = (long) n * n;

            double lcsMs = timeMs(() -> LCS.findLength(a, b));
            int lcsValue = LCS.findLength(a, b);
            addRow("dp", "LCS", "random", n, lcsMs, cells, lcsValue);

            double edMs = timeMs(() -> EditDistance.calculate(a, b));
            int edValue = EditDistance.calculate(a, b);
            addRow("dp", "EditDistance", "random", n, edMs, cells, edValue);

            System.out.printf(Locale.ROOT, "n=%-5d LCS=%d (%.3f ms)  EditDistance=%d (%.3f ms)%n",
                    n, lcsValue, lcsMs, edValue, edMs);
        }
    }


    private static void runStringMatching() {
        System.out.println("== String matching ==");
        for (int n : TEXT_SIZES) {
            Random rnd = new Random(SEED + 1_000_000L + n);
            String text = randomString(n, rnd);
            String present = text.substring(n / 2, n / 2 + PATTERN_LENGTH);
            String absent = absentPattern(text, PATTERN_LENGTH, rnd);

            matchingWorkload("random-present", text, present);
            matchingWorkload("random-absent", text, absent);

            String allA = "a".repeat(n);
            matchingWorkload("adversarial-no-match", allA,
                    "a".repeat(ADVERSARIAL_PATTERN_LENGTH - 1) + "b");
            matchingWorkload("adversarial-all-match", allA,
                    "a".repeat(ADVERSARIAL_PATTERN_LENGTH));
        }
    }

    private static void matchingWorkload(String workload, String text, String pattern) {
        List<Integer> expected = NaiveSearch.search(text, pattern);
        if (!expected.equals(KMP.search(text, pattern)) || !expected.equals(RabinKarp.search(text, pattern))) {
            throw new IllegalStateException("Algorithms disagree on workload " + workload);
        }
        int n = text.length();
        int occurrences = expected.size();

        double naiveMs = timeMs(() -> NaiveSearch.search(text, pattern).size());
        addRow("matching", "Naive", workload, n, naiveMs, NaiveSearch.comparisons, occurrences);

        double kmpMs = timeMs(() -> KMP.search(text, pattern).size());
        addRow("matching", "KMP", workload, n, kmpMs, KMP.comparisons, occurrences);

        double rkMs = timeMs(() -> RabinKarp.search(text, pattern).size());
        addRow("matching", "RabinKarp", workload, n, rkMs, RabinKarp.comparisons, occurrences);

        System.out.printf(Locale.ROOT,
                "%-22s n=%-7d occ=%-6d Naive %8.3f ms (%d cmp) | KMP %8.3f ms (%d cmp) | RK %8.3f ms (%d cmp, %d spurious)%n",
                workload, n, occurrences,
                naiveMs, NaiveSearch.comparisons, kmpMs, KMP.comparisons, rkMs, RabinKarp.comparisons,
                RabinKarp.spuriousHits);
    }


    private static void runKasai() {
        System.out.println("== Kasai ==");
        for (int n : KASAI_SIZES) {
            Random rnd = new Random(SEED + 5_000_000L + n);
            String s = randomString(n, rnd);
            int[] sa = SuffixArrayBuilder.build(s);

            double ms = timeMs(() -> Kasai.buildLCP(s, sa)[n / 2]);
            long comparisons = Kasai.comparisons;

            int[] lcp = Kasai.buildLCP(s, sa);
            if (!Arrays.equals(lcp, SuffixArrayBuilder.bruteForceLcp(s, sa))) {
                throw new IllegalStateException("Kasai disagrees with brute force for n=" + n);
            }
            int maxLcp = Arrays.stream(lcp).max().orElse(0);

            addRow("kasai", "Kasai", "random", n, ms, comparisons, maxLcp);
            System.out.printf(Locale.ROOT, "n=%-6d %8.4f ms  comparisons=%d (%.2f per char)  maxLCP=%d%n",
                    n, ms, comparisons, comparisons / (double) n, maxLcp);
        }
    }


    private static void addRow(String experiment, String algorithm, String workload,
                               int size, double avgMs, long operations, long result) {
        rows.add(String.join(",", experiment, algorithm, workload, String.valueOf(size),
                String.format(Locale.ROOT, "%.6f", avgMs), String.valueOf(operations), String.valueOf(result)));
    }

    private static void writeCsv(String csvFile) throws IOException {
        Path path = Path.of(csvFile);
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(path))) {
            out.println("experiment,algorithm,workload,size,avg_ms,operations,result");
            rows.forEach(out::println);
        }
    }
}
