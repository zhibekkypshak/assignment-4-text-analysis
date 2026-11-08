
package algorithms;

public class Kasai {

    public static int[] buildLCP(String text, int[] suffixArray) {
        int n = text.length();

        if (suffixArray.length != n) {
            throw new IllegalArgumentException(
                    "Suffix array length must equal text length"
            );
        }

        int[] rank = new int[n];
        boolean[] seen = new boolean[n];

        for (int i = 0; i < n; i++) {
            int suffix = suffixArray[i];

            if (suffix < 0 || suffix >= n || seen[suffix]) {
                throw new IllegalArgumentException(
                        "Invalid suffix array"
                );
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

            while (i + k < n && j + k < n &&
                    text.charAt(i + k) == text.charAt(j + k)) {
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

