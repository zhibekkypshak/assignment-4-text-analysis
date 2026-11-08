package algorithms;

import java.util.ArrayList;
import java.util.List;

public class RabinKarp {

    private static final long BASE = 256;
    private static final long MOD = 1_000_000_007;

    public static long comparisons = 0;
    public static long spuriousHits = 0;

    public static List<Integer> search(String text, String pattern) {
        return search(text, pattern, BASE, MOD);
    }

    public static List<Integer> search(String text, String pattern, long base, long mod) {
        comparisons = 0;
        spuriousHits = 0;
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

        long patternHash = 0;
        long textHash = 0;
        long highestPower = 1;

        for (int i = 0; i < m - 1; i++) {
            highestPower = (highestPower * base) % mod;
        }

        for (int i = 0; i < m; i++) {
            patternHash = (patternHash * base + pattern.charAt(i)) % mod;
            textHash = (textHash * base + text.charAt(i)) % mod;
        }

        for (int i = 0; i <= n - m; i++) {

            if (patternHash == textHash) {
                boolean equal = true;
                for (int j = 0; j < m; j++) {
                    comparisons++;
                    if (text.charAt(i + j) != pattern.charAt(j)) {
                        equal = false;
                        break;
                    }
                }
                if (equal) {
                    result.add(i);
                } else {
                    spuriousHits++;
                }
            }

            if (i < n - m) {
                textHash = (textHash - text.charAt(i) * highestPower % mod + mod) % mod;
                textHash = (textHash * base + text.charAt(i + m)) % mod;
            }
        }
        return result;
    }
}
