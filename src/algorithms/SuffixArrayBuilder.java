package algorithms;

import java.util.Arrays;

public class SuffixArrayBuilder {

    public static int[] build(String s) {
        int n = s.length();
        Integer[] idx = new Integer[n];
        for (int i = 0; i < n; i++) {
            idx[i] = i;
        }
        Arrays.sort(idx, (a, b) -> compareSuffixes(s, a, b));

        int[] sa = new int[n];
        for (int i = 0; i < n; i++) {
            sa[i] = idx[i];
        }
        return sa;
    }

    private static int compareSuffixes(String s, int a, int b) {
        int n = s.length();
        while (a < n && b < n) {
            if (s.charAt(a) != s.charAt(b)) {
                return Character.compare(s.charAt(a), s.charAt(b));
            }
            a++;
            b++;
        }
        return Integer.compare(n - a, n - b);
    }

    public static int[] bruteForceLcp(String s, int[] sa) {
        int n = s.length();
        int[] lcp = new int[n];
        for (int r = 1; r < n; r++) {
            int a = sa[r - 1];
            int b = sa[r];
            int h = 0;
            while (a + h < n && b + h < n && s.charAt(a + h) == s.charAt(b + h)) {
                h++;
            }
            lcp[r] = h;
        }
        return lcp;
    }
}