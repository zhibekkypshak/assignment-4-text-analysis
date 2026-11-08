package algorithms;

import java.util.ArrayList;
import java.util.List;

public class KMP {

    public static long comparisons = 0;

    static int[] buildLPS(String pattern) {
        int[] lps = new int[pattern.length()];

        int len = 0;
        int i = 1;

        while (i < pattern.length()) {
            comparisons++;
            if (pattern.charAt(i) == pattern.charAt(len)) {
                len++;
                lps[i] = len;
                i++;
            } else if (len != 0) {
                len = lps[len - 1];
            } else {
                lps[i] = 0;
                i++;
            }
        }
        return lps;
    }

    public static int[] lpsTable(String pattern) {
        comparisons = 0;
        return buildLPS(pattern);
    }

    public static List<Integer> search(String text, String pattern) {
        comparisons = 0;
        List<Integer> result = new ArrayList<>();

        int n = text.length();
        int m = pattern.length();

        if (m == 0) {
            for (int i = 0; i <= n; i++) {
                result.add(i);
            }
            return result;
        }
        if (m > n) {
            return result;
        }

        int[] lps = buildLPS(pattern);

        int i = 0;
        int j = 0;

        while (i < n) {
            comparisons++;
            if (text.charAt(i) == pattern.charAt(j)) {
                i++;
                j++;
                if (j == m) {
                    result.add(i - m);
                    j = lps[j - 1];
                }
            } else if (j > 0) {
                j = lps[j - 1];
            } else {
                i++;
            }
        }
        return result;
    }
}

