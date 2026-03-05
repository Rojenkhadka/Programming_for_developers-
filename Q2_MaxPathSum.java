public class Q2_MaxPathSum {

    static class TreeNode {
        int val;
        TreeNode left, right;
        TreeNode(int val) { this.val = val; }
        TreeNode(int val, TreeNode left, TreeNode right) {
            this.val = val; this.left = left; this.right = right;
        }
    }

    private static int globalMax;

    public static int maxPathSum(TreeNode root) {
        globalMax = Integer.MIN_VALUE;
        dfs(root);
        return globalMax;
    }

    private static int dfs(TreeNode node) {
        if (node == null) return 0;

        int leftGain  = Math.max(dfs(node.left),  0);
        int rightGain = Math.max(dfs(node.right), 0);

        int pathThroughNode = node.val + leftGain + rightGain;
        globalMax = Math.max(globalMax, pathThroughNode);

        return node.val + Math.max(leftGain, rightGain);
    }

    public static TreeNode buildTree(Integer[] values) {
        if (values == null || values.length == 0 || values[0] == null) return null;

        TreeNode root = new TreeNode(values[0]);
        java.util.Queue<TreeNode> queue = new java.util.LinkedList<>();
        queue.add(root);
        int i = 1;

        while (!queue.isEmpty() && i < values.length) {
            TreeNode node = queue.poll();
            if (i < values.length && values[i] != null) {
                node.left = new TreeNode(values[i]);
                queue.add(node.left);
            }
            i++;
            if (i < values.length && values[i] != null) {
                node.right = new TreeNode(values[i]);
                queue.add(node.right);
            }
            i++;
        }
        return root;
    }

    public static void main(String[] args) {
        System.out.println("=".repeat(65));
        System.out.println("Q2 – Hydropower Cascade: Binary Tree Maximum Path Sum");
        System.out.println("=".repeat(65));

        Object[][] tests = {
            { "Example 1 – Upper + Main + Lower tributary",
              new Integer[]{1, 2, 3},                          6,
              "2(upper) → 1(main) → 3(lower) = 6" },
            { "Example 2 – High-cost site, major plant path",
              new Integer[]{-10, 9, 20, null, null, 15, 7},   42,
              "15(run-of-river) → 20(major) → 7(mini-hydro) = 42" },
            { "Single positive node",
              new Integer[]{5},                                 5,  "" },
            { "Single negative node",
              new Integer[]{-3},                               -3,  "" },
            { "All negative – best is root alone",
              new Integer[]{-1, -2, -3},                      -1,
              "Both children drag value down further" },
            { "Path not through root",
              new Integer[]{1, -2, -3, 4, 5},                  7,
              "4 → -2 → 5 = 7" },
        };

        boolean allPassed = true;
        for (Object[] tc : tests) {
            String    label    = (String)    tc[0];
            Integer[] vals     = (Integer[]) tc[1];
            int       expected = (int)       tc[2];
            String    note     = (String)    tc[3];

            TreeNode root = buildTree(vals);
            int      got  = maxPathSum(root);
            boolean  pass = (got == expected);
            if (!pass) allPassed = false;

            System.out.printf("%n[%s] %s%n", pass ? "PASS ✓" : "FAIL ✗", label);
            System.out.printf("  Expected : %d%n", expected);
            System.out.printf("  Got      : %d%n", got);
            if (!note.isEmpty()) System.out.printf("  Note     : %s%n", note);
        }

        System.out.println("\n" + "=".repeat(65));
        System.out.println(allPassed ? "All tests passed!" : "Some tests FAILED.");
        System.out.println("=".repeat(65));
    }
}
