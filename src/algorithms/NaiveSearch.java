
package algorithms;

import java.util.ArrayList;
import java.util.List;

public class NaiveSearch {

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

        for (int i = 0; i <= n - m; i++) {
            int j = 0;

            while (j < m &&
                    text.charAt(i + j) == pattern.charAt(j)) {
                j++;
            }

            if (j == m) {
                result.add(i);
            }
        }

        return result;
    }
}

