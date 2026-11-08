package tests;
import algorithms.*;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class TestRunner {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        testLCS();
        testEditDistance();
        testStringMatching();
        testKMPTable();
        testRabinKarpCollisions();
        testKasai();

        System.out.println();
        System.out.println("Passed: " + passed + ", failed: " + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void check(String name, Object expected, Object actual) {
        if (Objects.deepEquals(expected, actual)) {
            passed++;
            System.out.println("PASS  " + name);
        } else {
            failed++;
            System.out.println("FAIL  " + name + "  expected=" + str(expected) + "  actual=" + str(actual));
        }
    }

    private static String str(Object o) {
        return o instanceof int[] a ? Arrays.toString(a) : String.valueOf(o);
    }

    private static void matchCase(String name, String text, String pattern, Integer... expected) {
        List<Integer> exp = Arrays.asList(expected);
        List<Integer> naive = NaiveSearch.search(text, pattern);
        List<Integer> kmp = KMP.search(text, pattern);
        List<Integer> rk = RabinKarp.search(text, pattern);
        check(name + " [Naive]", exp, naive);
        check(name + " [KMP]", exp, kmp);
        check(name + " [Rabin-Karp]", exp, rk);
        check(name + " [all three identical]", true, naive.equals(kmp) && kmp.equals(rk));
    }

    private static List<Integer> oracle(String text, String pattern) {
        List<Integer> r = new ArrayList<>();
        for (int i = 0; i + pattern.length() <= text.length(); i++) {
            if (text.startsWith(pattern, i)) {
                r.add(i);
            }
        }
        return r;
    }

    private static String randomString(Random rnd, int len, String alphabet) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(alphabet.charAt(rnd.nextInt(alphabet.length())));
        }
        return sb.toString();
    }

    private static void testLCS() {
        System.out.println("== LCS ==");
        check("identical strings", 3, LCS.findLength("abc", "abc"));
        check("completely different strings", 0, LCS.findLength("abc", "xyz"));
        check("first string empty", 0, LCS.findLength("", "abc"));
        check("second string empty", 0, LCS.findLength("abc", ""));
        check("both empty", 0, LCS.findLength("", ""));
        check("AGGTAB / GXTXAYB", 4, LCS.findLength("AGGTAB", "GXTXAYB"));
        check("ABCBDAB / BDCABA", 4, LCS.findLength("ABCBDAB", "BDCABA"));
        check("symmetry", LCS.findLength("AGGTAB", "GXTXAYB"), LCS.findLength("GXTXAYB", "AGGTAB"));
    }

    private static void testEditDistance() {
        System.out.println("== Edit distance ==");
        check("identical strings", 0, EditDistance.calculate("abc", "abc"));
        check("both empty", 0, EditDistance.calculate("", ""));
        check("first empty", 3, EditDistance.calculate("", "abc"));
        check("second empty", 3, EditDistance.calculate("abc", ""));
        check("substitution cat->cut", 1, EditDistance.calculate("cat", "cut"));
        check("insertion cat->cart", 1, EditDistance.calculate("cat", "cart"));
        check("deletion cart->cat", 1, EditDistance.calculate("cart", "cat"));
        check("kitten->sitting", 3, EditDistance.calculate("kitten", "sitting"));
        check("sunday->saturday", 3, EditDistance.calculate("sunday", "saturday"));
        check("symmetry", EditDistance.calculate("kitten", "sitting"), EditDistance.calculate("sitting", "kitten"));
    }


    private static void testStringMatching() {
        System.out.println("== String matching ==");
        matchCase("pattern at the beginning", "abcdef", "abc", 0);
        matchCase("pattern in the middle", "xxabcxx", "abc", 2);
        matchCase("pattern at the end", "xxxabc", "abc", 3);
        matchCase("multiple occurrences", "abababab", "abab", 0, 2, 4);
        matchCase("overlapping occurrences", "aaaaa", "aa", 0, 1, 2, 3);
        matchCase("pattern absent", "abcdef", "xyz");
        matchCase("pattern longer than text", "ab", "abc");
        matchCase("pattern equals text", "abc", "abc", 0);
        matchCase("empty pattern", "abc", "", 0, 1, 2, 3);
        matchCase("empty pattern, empty text", "", "", 0);
        matchCase("empty text, non-empty pattern", "", "a");

        Random rnd = new Random(1);
        boolean allOk = true;
        for (int t = 0; t < 2000; t++) {
            String text = randomString(rnd, rnd.nextInt(30), "ab");
            String pat = randomString(rnd, rnd.nextInt(6), "ab");
            List<Integer> exp = oracle(text, pat);
            allOk &= exp.equals(NaiveSearch.search(text, pat))
                    && exp.equals(KMP.search(text, pat))
                    && exp.equals(RabinKarp.search(text, pat));
        }
        check("2000 random cases: all three equal the oracle", true, allOk);
    }

    private static void testKMPTable() {
        System.out.println("== KMP LPS table ==");
        check("LPS(ababaca)", new int[]{0, 0, 1, 2, 3, 0, 1}, KMP.lpsTable("ababaca"));
        check("LPS(aabaaab)", new int[]{0, 1, 0, 1, 2, 2, 3}, KMP.lpsTable("aabaaab"));
        check("LPS(aaaa)", new int[]{0, 1, 2, 3}, KMP.lpsTable("aaaa"));
        check("LPS(abcd)", new int[]{0, 0, 0, 0}, KMP.lpsTable("abcd"));
    }

    private static void testRabinKarpCollisions() {
        System.out.println("== Rabin-Karp collisions ==");
        Random rnd = new Random(2);
        boolean exact = true;
        long totalSpurious = 0;
        for (int t = 0; t < 500; t++) {
            String text = randomString(rnd, 60, "abcd");
            String pat = randomString(rnd, 3, "abcd");
            List<Integer> res = RabinKarp.search(text, pat, 256, 7);
            totalSpurious += RabinKarp.spuriousHits;
            exact &= res.equals(oracle(text, pat));
        }
        check("modulus 7: results still exact", true, exact);
        check("modulus 7: collisions really happened (and were rejected)", true, totalSpurious > 0);
        RabinKarp.search("abcabcabc", "abc");
        check("normal modulus: no spurious hits on a simple input", 0L, RabinKarp.spuriousHits);
    }

    private static void testKasai() {
        System.out.println("== Kasai ==");

        int[] bananaSa = {5, 3, 1, 0, 4, 2};
        check("banana: suffix array builder", bananaSa, SuffixArrayBuilder.build("banana"));
        check("banana: LCP", new int[]{0, 1, 3, 0, 0, 2}, Kasai.buildLCP("banana", bananaSa));

        int[] abraSa = {10, 7, 0, 3, 5, 8, 1, 4, 6, 9, 2};
        check("abracadabra: suffix array builder", abraSa, SuffixArrayBuilder.build("abracadabra"));
        check("abracadabra: LCP", new int[]{0, 1, 4, 1, 1, 0, 3, 0, 0, 0, 2}, Kasai.buildLCP("abracadabra", abraSa));

        int[] missSa = {10, 7, 4, 1, 0, 9, 8, 6, 3, 5, 2};
        check("mississippi: LCP", new int[]{0, 1, 1, 4, 0, 0, 1, 0, 2, 1, 3}, Kasai.buildLCP("mississippi", missSa));

        check("aaaa: LCP", new int[]{0, 1, 2, 3}, Kasai.buildLCP("aaaa", new int[]{3, 2, 1, 0}));
        check("single character", new int[]{0}, Kasai.buildLCP("a", new int[]{0}));
        check("empty string", new int[]{}, Kasai.buildLCP("", new int[]{}));

        Random rnd = new Random(3);
        boolean same = true;
        boolean linear = true;
        for (int t = 0; t < 300; t++) {
            String alphabet = new String[]{"a", "ab", "abcd"}[t % 3];
            String s = randomString(rnd, rnd.nextInt(40), alphabet);
            int[] sa = SuffixArrayBuilder.build(s);
            int[] lcp = Kasai.buildLCP(s, sa);
            same &= Arrays.equals(lcp, SuffixArrayBuilder.bruteForceLcp(s, sa));
            linear &= Kasai.comparisons <= 3L * s.length();
        }
        check("300 random strings: Kasai equals brute-force LCP", true, same);
        check("comparisons <= 3n", true, linear);

        check("wrong suffix array length is rejected", true, throwsIllegalArgument("abc", new int[]{0, 1}));
        check("duplicate entry is rejected", true, throwsIllegalArgument("abc", new int[]{0, 0, 1}));
        check("out-of-range entry is rejected", true, throwsIllegalArgument("abc", new int[]{0, 1, 5}));
    }

    private static boolean throwsIllegalArgument(String s, int[] sa) {
        try {
            Kasai.buildLCP(s, sa);
            return false;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }
}
