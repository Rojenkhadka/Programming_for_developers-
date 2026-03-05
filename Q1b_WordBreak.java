import java.util.*;

public class Q1b_WordBreak {

    public static List<String> wordBreak(String userQuery,
                                         List<String> marketingKeywordsDictionary) {
        int n = userQuery.length();
        Set<String> wordSet = new HashSet<>(marketingKeywordsDictionary);

        boolean[] dp = new boolean[n + 1];
        dp[0] = true;

        for (int end = 1; end <= n; end++) {
            for (int start = 0; start < end; start++) {
                if (dp[start] && wordSet.contains(userQuery.substring(start, end))) {
                    dp[end] = true;
                    break;
                }
            }
        }

        List<String> results = new ArrayList<>();
        if (!dp[n]) return results;   

        backtrack(userQuery, wordSet, dp, 0, new LinkedList<>(), results);
        return results;
    }

    private static void backtrack(String query, Set<String> wordSet,
                                   boolean[] dp, int start,
                                   LinkedList<String> current, List<String> results) {
        if (start == query.length()) {
            results.add(String.join(" ", current));
            return;
        }
        for (int end = start + 1; end <= query.length(); end++) {
            String word = query.substring(start, end);
            // Only recurse if the word is valid AND the rest of the string is reachable
            if (wordSet.contains(word) && dp[end]) {
                current.addLast(word);
                backtrack(query, wordSet, dp, end, current, results);
                current.removeLast();
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("=".repeat(65));
        System.out.println("Q1b – Marketing Keyword Segmentation: Word Break II");
        System.out.println("=".repeat(65));

        Object[][] tests = {
            { "Example 1 – Basic Keyword Segmentation",
              "nepaltrekkingguide",
              new String[]{"nepal","trekking","guide","nepaltrekking"},
              new String[]{"nepal trekking guide","nepaltrekking guide"} },

            { "Example 2 – Complex Keyword Combinations",
              "visitkathmandunepal",
              new String[]{"visit","kathmandu","nepal","visitkathmandu","kathmandunepal"},
              new String[]{"visit kathmandu nepal","visitkathmandu nepal","visit kathmandunepal"} },

            { "Example 3 – No Valid Segmentation",
              "everesthikingtrail",
              new String[]{"everest","hiking","trek"},
              new String[]{} },

            { "Single word exact match",
              "nepal",
              new String[]{"nepal","kathmandu"},
              new String[]{"nepal"} },

            { "Reuse of words",
              "aaa",
              new String[]{"a","aa"},
              new String[]{"a a a","a aa","aa a"} },
        };

        boolean allPassed = true;
        for (Object[] tc : tests) {
            String   label    = (String)   tc[0];
            String   query    = (String)   tc[1];
            String[] dictArr  = (String[]) tc[2];
            String[] expArr   = (String[]) tc[3];

            List<String> result   = wordBreak(query, Arrays.asList(dictArr));
            Set<String>  resSet   = new HashSet<>(result);
            Set<String>  expSet   = new HashSet<>(Arrays.asList(expArr));
            boolean      pass     = resSet.equals(expSet);
            if (!pass) allPassed = false;

            System.out.printf("\n[%s] %s%n", pass ? "PASS ✓" : "FAIL ✗", label);
            System.out.printf("  Query      : '%s'%n", query);
            System.out.printf("  Expected   : %s%n", new TreeSet<>(expSet));
            System.out.printf("  Got        : %s%n", new TreeSet<>(resSet));
        }

        System.out.println("\n" + "=".repeat(65));
        System.out.println(allPassed ? "All tests passed!" : "Some tests FAILED.");
        System.out.println("=".repeat(65));
    }
}
