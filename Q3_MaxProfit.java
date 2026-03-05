public class Q3_MaxProfit {

    public static int maxProfit(int maxTrades, int[] dailyPrices) {
        int n = dailyPrices.length;

        if (n < 2 || maxTrades == 0) return 0;

        // If we can make unlimited trades, capture every upward move (greedy)
        if (maxTrades >= n / 2) {
            int profit = 0;
            for (int i = 1; i < n; i++) {
                if (dailyPrices[i] > dailyPrices[i - 1])
                    profit += dailyPrices[i] - dailyPrices[i - 1];
            }
            return profit;
        }

        int[][] dp = new int[maxTrades + 1][n];

        for (int k = 1; k <= maxTrades; k++) {
            int maxSoFar = -dailyPrices[0];

            for (int i = 1; i < n; i++) {
                dp[k][i] = Math.max(dp[k][i - 1], dailyPrices[i] + maxSoFar);

                maxSoFar = Math.max(maxSoFar, dp[k - 1][i] - dailyPrices[i]);
            }
        }

        return dp[maxTrades][n - 1];
    }

    public static void main(String[] args) {
        System.out.println("=".repeat(65));
        System.out.println("Q3 – Commodity Trading: Max Profit with K Trades");
        System.out.println("=".repeat(65));

        Object[][] tests = {
            { "Example 1 – Basic trade (NPR)",
               2, new int[]{2000,4000,1000}, 2000,
               "Buy@2000 sell@4000 = 2000 NPR" },
            { "Two profitable trades",
               2, new int[]{3,2,6,5,0,3},   7,
               "Buy@2 sell@6 (+4), buy@0 sell@3 (+3) = 7" },
            { "One trade allowed",
               1, new int[]{1,5,3,6,4},     5,
               "Buy@1 sell@6 = 5" },
            { "Always decreasing – no profit",
               2, new int[]{5,4,3,2,1},     0,  "" },
            { "Zero trades allowed",
               0, new int[]{1,10,100},      0,  "" },
            { "Single day",
               1, new int[]{500},           0,  "" },
            { "Unlimited trades path (k >= n/2)",
              100, new int[]{1,2,3,4,5},    4,
               "Capture every consecutive rise: 1+1+1+1 = 4" },
        };

        boolean allPassed = true;
        for (Object[] tc : tests) {
            String label    = (String) tc[0];
            int    trades   = (int)    tc[1];
            int[]  prices   = (int[])  tc[2];
            int    expected = (int)    tc[3];
            String note     = (String) tc[4];

            int     got  = maxProfit(trades, prices);
            boolean pass = (got == expected);
            if (!pass) allPassed = false;

            System.out.printf("%n[%s] %s%n", pass ? "PASS ✓" : "FAIL ✗", label);
            System.out.printf("  maxTrades : %d%n", trades);
            System.out.printf("  Expected  : %d%n", expected);
            System.out.printf("  Got       : %d%n", got);
            if (!note.isEmpty()) System.out.printf("  Note      : %s%n", note);
        }

        System.out.println("\n" + "=".repeat(65));
        System.out.println(allPassed ? "All tests passed!" : "Some tests FAILED.");
        System.out.println("=".repeat(65));
    }
}
