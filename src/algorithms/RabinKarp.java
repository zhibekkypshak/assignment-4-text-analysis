
package algorithms;

import java.util.ArrayList;
import java.util.List;

public class RabinKarp {

    private static final long BASE = 256;
    private static final long MOD = 1_000_000_007;

    public static List<Integer> search(String text, String pattern) {
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
            highestPower = (highestPower * BASE) % MOD;
        }

        for (int i = 0; i < m; i++) {
            patternHash = (patternHash * BASE
                    + pattern.charAt(i)) % MOD;

            textHash = (textHash * BASE
                    + text.charAt(i)) % MOD;
        }

        for (int i = 0; i <= n - m; i++) {

            if (patternHash == textHash &&
                    text.regionMatches(i, pattern, 0, m)) {
                result.add(i);
            }

            if (i < n - m) {
                textHash = (textHash
                        - text.charAt(i) * highestPower % MOD
                        + MOD) % MOD;

                textHash = (textHash * BASE
                        + text.charAt(i + m)) % MOD;
            }
        }

        return result;
    }
}

