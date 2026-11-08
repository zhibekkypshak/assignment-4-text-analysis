package algorithms;

public class Kasai {

    public static long comparisons = 0;

    public static int[] buildLCP(String text, int[] suffixArray) {
        comparisons = 0;
        int n = text.length();

        if (suffixArray.length != n) {
            throw new IllegalArgumentException("Suffix array length must equal text length");
        }

        int[] rank = new int[n];
        boolean[] seen = new boolean[n];

        for (int i = 0; i < n; i++) {
            int suffix = suffixArray[i];

            if (suffix < 0 || suffix >= n || seen[suffix]) {
                throw new IllegalArgumentException("Invalid suffix array");
            }

            seen[suffix] = true;
            rank[suffix] = i;
        }

        int[] lcp = new int[n];
        int k = 0;

        for (int i = 0; i < n; i++) {
            int r = rank[i];

            if (r == 0) {
                k = 0;
                continue;
            }

            int j = suffixArray[r - 1];

            while (i + k < n && j + k < n) {
                comparisons++;
                if (text.charAt(i + k) != text.charAt(j + k)) {
                    break;
                }
                k++;
            }

            lcp[r] = k;

            if (k > 0) {
                k--;
            }
        }
        return lcp;
    }
}

