import java.util.*;

public class Q6_EmergencyLogistics {


    static final String[] NODES = {"KTM", "JA", "JB", "PH", "BS"};
    static final Map<String, Integer> NODE_IDX = new HashMap<>();
    static { for (int i = 0; i < NODES.length; i++) NODE_IDX.put(NODES[i], i); }


    static final Object[][] SAFETY_EDGES = {
        {"KTM","JA",0.90}, {"KTM","JB",0.80},
        {"JA","KTM",0.90}, {"JA","PH",0.95}, {"JA","BS",0.70},
        {"JB","KTM",0.80}, {"JB","JA",0.60}, {"JB","BS",0.90},
        {"PH","JA",0.95},  {"PH","BS",0.85},
        {"BS","JA",0.70},  {"BS","JB",0.90}, {"BS","PH",0.85},
    };


    static final Object[][] CAPACITY_EDGES = {
        {"KTM","JA",10}, {"KTM","JB",15},
        {"JA","KTM",10}, {"JA","PH", 8}, {"JA","BS", 5},
        {"JB","KTM",15}, {"JB","JA", 4}, {"JB","BS",12},
        {"PH","JA", 8},  {"PH","BS", 6},
        {"BS","JA", 5},  {"BS","JB",12}, {"BS","PH", 6},
    };


    static class SafetyResult {
        double[] safetyProb;
        String[] prev;
    }


    static SafetyResult safestPaths(String source) {
        int n = NODES.length;

        @SuppressWarnings("unchecked")
        List<double[]>[] adj = new List[n];   // [toIdx, prob, logWeight]
        for (int i = 0; i < n; i++) adj[i] = new ArrayList<>();

        for (Object[] e : SAFETY_EDGES) {
            int    u   = NODE_IDX.get(e[0]);
            int    v   = NODE_IDX.get(e[1]);
            double p   = (double) e[2];
            double w   = -Math.log(p);          // TRANSFORMATION
            adj[u].add(new double[]{v, p, w});
        }

        double[] dist       = new double[n];
        double[] safetyProb = new double[n];
        String[] prev       = new String[n];
        Arrays.fill(dist, Double.MAX_VALUE);
        Arrays.fill(safetyProb, 0.0);

        int src = NODE_IDX.get(source);
        dist[src]       = 0.0;
        safetyProb[src] = 1.0;

        PriorityQueue<double[]> pq =
            new PriorityQueue<>(Comparator.comparingDouble(a -> a[0]));
        pq.offer(new double[]{0.0, src});

        while (!pq.isEmpty()) {
            double[] cur = pq.poll();
            double dU = cur[0];
            int    u  = (int) cur[1];
            if (dU > dist[u]) continue;   // stale entry

            for (double[] edge : adj[u]) {
                int    v      = (int) edge[0];
                double pUV    = edge[1];
                double wPrime = edge[2];

                double newDist   = dist[u] + wPrime;
                double newSafety = safetyProb[u] * pUV;
                if (newDist < dist[v]) {
                    dist[v]       = newDist;
                    safetyProb[v] = newSafety;
                    prev[v]       = source.equals(NODES[u]) ? source : NODES[u];
                    // rebuild prev correctly
                    prev[v]       = NODES[u];
                    pq.offer(new double[]{newDist, v});
                }
            }
        }

        SafetyResult sr = new SafetyResult();
        sr.safetyProb = safetyProb;
        sr.prev       = prev;
        return sr;
    }


    static List<String> reconstructPath(String[] prev, String target) {
        LinkedList<String> path = new LinkedList<>();
        String node = target;
        while (node != null) {
            path.addFirst(node);
            node = prev[NODE_IDX.get(node)];
        }
        return path;
    }

    static int N_NODES = NODES.length;

    static int[][] buildResidualGraph() {
        int[][] cap = new int[N_NODES][N_NODES];
        for (Object[] e : CAPACITY_EDGES) {
            int u = NODE_IDX.get(e[0]);
            int v = NODE_IDX.get(e[1]);
            cap[u][v] += (int) e[2];
        }
        return cap;
    }

    static boolean bfs(int[][] cap, int src, int sink, int[] parent) {
        boolean[] visited = new boolean[N_NODES];
        Queue<Integer> queue = new LinkedList<>();
        queue.add(src);
        visited[src] = true;
        Arrays.fill(parent, -1);

        while (!queue.isEmpty()) {
            int u = queue.poll();
            for (int v = 0; v < N_NODES; v++) {
                if (!visited[v] && cap[u][v] > 0) {
                    visited[v] = true;
                    parent[v]  = u;
                    if (v == sink) return true;
                    queue.add(v);
                }
            }
        }
        return false;
    }

    static int edmondsKarp(int src, int sink) {
        int[][] cap   = buildResidualGraph();
        int[]   parent = new int[N_NODES];
        int     maxFlow = 0;
        int     step    = 0;

        while (bfs(cap, src, sink, parent)) {
            // Find bottleneck
            int pathFlow = Integer.MAX_VALUE;
            List<String> pathNames = new ArrayList<>();
            for (int v = sink; v != src; v = parent[v]) {
                int u = parent[v];
                pathFlow = Math.min(pathFlow, cap[u][v]);
            }

            // Reconstruct path for display
            LinkedList<String> pList = new LinkedList<>();
            for (int v = sink; v != -1; v = parent[v]) pList.addFirst(NODES[v]);

            // Augment flow
            for (int v = sink; v != src; v = parent[v]) {
                int u = parent[v];
                cap[u][v] -= pathFlow;
                cap[v][u] += pathFlow;
            }

            maxFlow += pathFlow;
            step++;

            System.out.printf("%nStep %d: Augmenting path  %s%n", step,
                String.join(" → ", pList));
            System.out.printf("  Bottleneck (flow pushed) : %d trucks/hr%n", pathFlow);
            System.out.printf("  Cumulative total flow    : %d trucks/hr%n", maxFlow);
            System.out.println("  Residual capacities on path edges after augmentation:");
            for (int i = 1; i < pList.size(); i++) {
                int u = NODE_IDX.get(pList.get(i-1));
                int v = NODE_IDX.get(pList.get(i));
                System.out.printf("    %s → %s: residual capacity = %d%n",
                    NODES[u], NODES[v], cap[u][v]);
            }
        }

        boolean[] reachable = new boolean[N_NODES];
        Queue<Integer> q2 = new LinkedList<>();
        q2.add(src); reachable[src] = true;
        while (!q2.isEmpty()) {
            int u = q2.poll();
            for (int v = 0; v < N_NODES; v++) {
                if (!reachable[v] && cap[u][v] > 0) {
                    reachable[v] = true;
                    q2.add(v);
                }
            }
        }
        System.out.println("\n" + "=".repeat(52));
        System.out.printf("  MAXIMUM FLOW (KTM → BS) = %d trucks/hour%n", maxFlow);
        System.out.println("=".repeat(52));
        System.out.println("\nMinimum s-t Cut:");
        System.out.print("  Reachable from KTM : {");
        for (int i = 0; i < N_NODES; i++) if (reachable[i]) System.out.print(NODES[i]+" ");
        System.out.println("}");
        System.out.print("  Non-reachable side : {");
        for (int i = 0; i < N_NODES; i++) if (!reachable[i]) System.out.print(NODES[i]+" ");
        System.out.println("}");
        System.out.println("  Cut edges (reachable → non-reachable):");
        int cutCap = 0;
        for (Object[] e : CAPACITY_EDGES) {
            int u = NODE_IDX.get(e[0]);
            int v = NODE_IDX.get(e[1]);
            if (reachable[u] && !reachable[v]) {
                int c = (int) e[2];
                cutCap += c;
                System.out.printf("    %s → %s: capacity = %d%n", e[0], e[1], c);
            }
        }
        System.out.printf("  Total cut capacity = %d trucks/hour%n", cutCap);
        System.out.printf("%n  Max-Flow (%d) == Min-Cut (%d)  → Max-Flow Min-Cut theorem ✓%n",
            maxFlow, cutCap);

        return maxFlow;
    }

    public static void main(String[] args) {
        System.out.println("=".repeat(70));
        System.out.println("Q6: EMERGENCY SUPPLY LOGISTICS – EARTHQUAKE RESPONSE (Nepal)");
        System.out.println("=".repeat(70));


        System.out.println("\n PART B – QUESTION 1: Problem Modeling");
        System.out.println("-".repeat(55));
        System.out.println("(a) Standard Dijkstra (additive/distance weights) is unsuitable:");
        System.out.println("    Safety is the PRODUCT of edge probabilities, not a sum.");
        System.out.println("    Dijkstra minimises sums and has no mechanism for products.");
        System.out.println();
        System.out.println("(b) Using raw probabilities in a 'max' Dijkstra also fails:");
        System.out.println("    Multiplying by p ≤ 1 never increases the path weight.");
        System.out.println("    Dijkstra's greedy extraction requires that once a node is");
        System.out.println("    finalised, no future path can improve it – this property");
        System.out.println("    breaks under multiplicative probabilities, giving wrong answers.");


        System.out.println("\n" + "=".repeat(70));
        System.out.println(" PART B – QUESTION 2: Safest Path from KTM");
        System.out.println("-".repeat(55));
        System.out.println("Transformation: w'(e) = -ln(p(e))  ⟹  min-sum ≡ max-product");
        System.out.println();

        SafetyResult sr = safestPaths("KTM");
        System.out.printf("%-12s %-12s  %s%n", "Destination", "Max Safety", "Safest Path");
        System.out.println("-".repeat(55));
        for (String node : new String[]{"JA","JB","PH","BS"}) {
            List<String> path = reconstructPath(sr.prev, node);
            System.out.printf("%-12s %-12.4f  %s%n",
                node, sr.safetyProb[NODE_IDX.get(node)],
                String.join(" → ", path));
        }

        System.out.println();
        System.out.println("Proof of Correctness (Q2c):");
        System.out.println("  Let P* be the path maximising Π p(e).");
        System.out.println("  Transformation: argmax Π p(e) ≡ argmin Σ -ln p(e)");
        System.out.println("  Since 0 < p(e) ≤ 1,  w'(e) = -ln p(e) ≥ 0  (non-negative ✓).");
        System.out.println("  Standard Dijkstra correctly finds min-weight paths on");
        System.out.println("  non-negative edge weights, which by the bijection above");
        System.out.println("  corresponds to the max-product-probability path. ∎");

        System.out.println("\n" + "=".repeat(70));
        System.out.println("PART B – QUESTION 3: Max Flow KTM → BS (Edmonds-Karp)");
        System.out.println("-".repeat(55));

        int src  = NODE_IDX.get("KTM");
        int sink = NODE_IDX.get("BS");
        edmondsKarp(src, sink);
    }
}
