import java.util.HashMap;
import java.util.Map;

public class Q1a_MaxPointsOnLine {


    private static int gcd(int a, int b) {
        a = Math.abs(a);
        b = Math.abs(b);
        while (b != 0) {
            int t = b;
            b = a % b;
            a = t;
        }
        return a;
    }
   
    public static int maxPoints(int[][] customerLocations) {
        int n = customerLocations.length;
        if (n <= 2) return n;

        int maxPts = 1;

        for (int i = 0; i < n; i++) {
          Map<String, Integer> slopeCount = new HashMap<>();
          int duplicates = 0;   
          int localMax   = 0;

          for (int j = i + 1; j < n; j++) {
            int dx = customerLocations[j][0] - customerLocations[i][0];
            int dy = customerLocations[j][1] - customerLocations[i][1];

            if (dx == 0 && dy == 0) {
              duplicates++;
              continue;
            }

            int g = gcd(Math.abs(dx), Math.abs(dy));
            dx /= g;
            dy /= g;

            if (dx < 0) { dx = -dx; dy = -dy; }
            else if (dx == 0) { dy = Math.abs(dy); }

            String key = dx + "/" + dy;
            int cnt = slopeCount.getOrDefault(key, 0) + 1;
            slopeCount.put(key, cnt);
            localMax = Math.max(localMax, cnt);
          }

          maxPts = Math.max(maxPts, localMax + duplicates + 1);
        }
        return maxPts;
    }

    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("Q1a – Signal Repeater Placement: Max Points on a Line");
        System.out.println("=".repeat(60));

        Object[][] tests = {
            // { label, input[][],  expected }
            { "Example 1 – Ideal Repeater (diagonal)",
              new int[][]{{1,1},{2,2},{3,3}},                        3 },
            { "Example 2 – Complex Repeater",
              new int[][]{{1,1},{3,2},{5,3},{4,1},{2,3},{1,4}},      4 },
            { "Single point",
              new int[][]{{0,0}},                                    1 },
            { "Two points",
              new int[][]{{1,0},{2,0}},                              2 },
            { "Vertical line",
              new int[][]{{0,0},{0,1},{0,2},{0,3}},                  4 },
            { "Horizontal line",
              new int[][]{{0,0},{1,0},{2,0},{3,0}},                  4 },
            { "Duplicate points",
              new int[][]{{1,1},{1,1},{2,2},{3,3}},                  4 },
        };

        boolean allPassed = true;
        for (Object[] tc : tests) {
            String  label    = (String)   tc[0];
            int[][] input    = (int[][])  tc[1];
            int     expected = (int)      tc[2];
            int     got      = maxPoints(input);
            boolean pass     = (got == expected);
            if (!pass) allPassed = false;

            System.out.printf("\n[%s] %s%n", pass ? "PASS ✓" : "FAIL ✗", label);
            System.out.printf("  Expected : %d%n", expected);
            System.out.printf("  Got      : %d%n", got);
        }

        System.out.println("\n" + "=".repeat(60));
        System.out.println(allPassed ? "All tests passed!" : "Some tests FAILED.");
        System.out.println("=".repeat(60));
    }
}
