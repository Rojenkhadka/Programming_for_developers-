import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class Q5a_TouristOptimizerGUI extends JFrame {


    static class TouristSpot {
        final String   name;
        final double   latitude, longitude;
        final int      entryFee, openHour, closeHour, visitHours;
        final String[] tags;

        TouristSpot(String name, double lat, double lon,
                    int fee, int open, int close, String[] tags, int visitHours) {
            this.name = name; this.latitude = lat; this.longitude = lon;
            this.entryFee = fee; this.openHour = open; this.closeHour = close;
            this.tags = tags; this.visitHours = visitHours;
        }

        int interestScore(String[] userTags) {
            int score = 0;
            for (String u : userTags)
                for (String t : tags)
                    if (t.equalsIgnoreCase(u.trim())) score++;
            return score;
        }

        public String toString() { return name; }
    }

    static final List<TouristSpot> ALL_SPOTS = Arrays.asList(
        new TouristSpot("Pashupatinath Temple",  27.7104, 85.3488, 100, 6,  18,
                        new String[]{"culture","religious"}, 2),
        new TouristSpot("Swayambhunath Stupa",   27.7149, 85.2906, 200, 7,  17,
                        new String[]{"culture","heritage"},  2),
        new TouristSpot("Garden of Dreams",      27.7125, 85.3170, 150, 9,  21,
                        new String[]{"nature","relaxation"}, 1),
        new TouristSpot("Chandragiri Hills",     27.6616, 85.2458, 700, 9,  17,
                        new String[]{"nature","adventure"},  3),
        new TouristSpot("Kathmandu Durbar Square",27.7048, 85.3076, 100, 10, 17,
                        new String[]{"culture","heritage"},  2)
    );


    private final JTextField budgetField = new JTextField("1500", 8);
    private final JTextField timeField   = new JTextField("8",    5);
    private final JTextField tagsField   = new JTextField("culture,heritage", 18);
    private final JTextArea  resultArea  = new JTextArea();
    private final MapPanel   mapPanel    = new MapPanel();


    public Q5a_TouristOptimizerGUI() {
        setTitle("Tourist Spot Optimizer - Kathmandu, Nepal");
        setSize(990, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));


        JPanel ip = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        ip.setBorder(BorderFactory.createTitledBorder("User Preferences"));
        ip.add(new JLabel("Budget (NPR):")); ip.add(budgetField);
        ip.add(new JLabel("Time available (hours):")); ip.add(timeField);
        ip.add(new JLabel("Interests (comma-separated):")); ip.add(tagsField);
        JButton btn = new JButton("Plan Itinerary");
        btn.setBackground(new Color(34, 139, 34)); btn.setForeground(Color.WHITE);
        ip.add(btn);


        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        resultArea.setEditable(false);


        mapPanel.setBorder(BorderFactory.createTitledBorder("Route Map (Coordinate Plot)"));
        mapPanel.setPreferredSize(new Dimension(380, 400));

        JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                       new JScrollPane(resultArea), mapPanel);
        sp.setDividerLocation(570);

        add(ip,  BorderLayout.NORTH);
        add(sp,  BorderLayout.CENTER);

        JLabel foot = new JLabel("  Greedy = O(n log n)  |  Brute-Force = O(2^n)  |  n=5 spots",
                                 SwingConstants.CENTER);
        foot.setFont(new Font("SansSerif", Font.ITALIC, 11));
        foot.setForeground(Color.GRAY);
        add(foot, BorderLayout.SOUTH);

        btn.addActionListener(e -> planItinerary());
        setVisible(true);
    }


    static class Selection {
        final TouristSpot spot;
        final String      reason;
        Selection(TouristSpot s, String r) { spot = s; reason = r; }
    }


    private List<Selection> greedyOptimize(int budget, int time, String[] tags) {
        List<TouristSpot> candidates = new ArrayList<>(ALL_SPOTS);
        candidates.sort((a, b) -> {
            int sa = a.interestScore(tags), sb = b.interestScore(tags);
            if (sa != sb) return sb - sa;                                    // higher score first
            return Double.compare((double) sb / (b.entryFee + 1),           // then value/rupee
                                  (double) sa / (a.entryFee + 1));
        });
        List<Selection> selected = new ArrayList<>();
        int usedBudget = 0, usedTime = 0;
        for (TouristSpot s : candidates) {
            if (usedBudget + s.entryFee <= budget && usedTime + s.visitHours <= time) {
                int sc = s.interestScore(tags);
                String reason = sc > 0
                    ? "Matched " + sc + " interest tag(s); fits budget & time"
                    : "Affordable option; fits remaining budget & time";
                selected.add(new Selection(s, reason));
                usedBudget += s.entryFee;
                usedTime   += s.visitHours;
            }
        }
        return selected;
    }


    private List<TouristSpot> bruteForceOptimize(int budget, int time, String[] tags) {
        int n = ALL_SPOTS.size();
        List<TouristSpot> best = new ArrayList<>();
        int bestScore = -1;
        for (int mask = 0; mask < (1 << n); mask++) {
            List<TouristSpot> combo = new ArrayList<>();
            int cost = 0, t = 0, score = 0;
            for (int i = 0; i < n; i++) {
                if ((mask & (1 << i)) != 0) {
                    TouristSpot s = ALL_SPOTS.get(i);
                    cost  += s.entryFee;
                    t     += s.visitHours;
                    score += s.interestScore(tags) + 1;
                    combo.add(s);
                }
            }
            if (cost <= budget && t <= time && score > bestScore) {
                bestScore = score;
                best = new ArrayList<>(combo);
            }
        }
        return best;
    }


    private void planItinerary() {
        int budget, time;
        try {
            budget = Integer.parseInt(budgetField.getText().trim());
            time   = Integer.parseInt(timeField.getText().trim());
            if (budget <= 0 || time <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Please enter valid positive integers for budget and time.",
                "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String raw = tagsField.getText().trim();
        String[] tags = raw.isEmpty() ? new String[]{} : raw.split("\\s*,\\s*");

        List<Selection>    greedyResult = greedyOptimize(budget, time, tags);
        List<TouristSpot>  bruteResult  = bruteForceOptimize(budget, time, tags);

        StringBuilder sb = new StringBuilder();


        sb.append("=================================================================\n");
        sb.append("  GREEDY HEURISTIC ITINERARY\n");
        sb.append("  Strategy: interest-match DESC, then value/rupee DESC\n");
        sb.append("=================================================================\n");
        sb.append(String.format("  %-28s %-13s %-9s  %s%n",
                  "Spot", "Schedule", "Fee(NPR)", "Tags"));
        sb.append("  " + "-".repeat(66) + "\n");

        int curHour = 9, totalFee = 0, totalTime = 0;
        List<TouristSpot> greedySpots = new ArrayList<>();
        for (Selection sel : greedyResult) {
            TouristSpot s = sel.spot;
            greedySpots.add(s);
            sb.append(String.format("  %-28s %02d:00-%02d:00  NPR %-5d  %s%n",
                      s.name, curHour, curHour + s.visitHours,
                      s.entryFee, String.join(", ", s.tags)));
            sb.append("    -> ").append(sel.reason).append("\n");
            curHour   += s.visitHours + 1;
            totalFee  += s.entryFee;
            totalTime += s.visitHours;
        }
        if (greedyResult.isEmpty())
            sb.append("  No spots fit within the given constraints.\n");

        sb.append("  " + "-".repeat(66) + "\n");
        sb.append(String.format("  Spots: %d  |  Total Cost: NPR %d  |  Time Used: %d hrs%n",
                  greedyResult.size(), totalFee, totalTime));

        // ---- brute-force section ----
        sb.append("\n=================================================================\n");
        sb.append("  BRUTE-FORCE OPTIMAL  (evaluates all 2^5 = 32 subsets)\n");
        sb.append("=================================================================\n");
        int bFee = 0, bTime = 0;
        for (TouristSpot s : bruteResult) {
            sb.append(String.format("  %-28s  NPR %-5d  %d hr(s)%n",
                      s.name, s.entryFee, s.visitHours));
            bFee  += s.entryFee;
            bTime += s.visitHours;
        }
        if (bruteResult.isEmpty())
            sb.append("  No valid combination found.\n");
        sb.append("  " + "-".repeat(66) + "\n");
        sb.append(String.format("  Spots: %d  |  Total Cost: NPR %d  |  Time Used: %d hrs%n",
                  bruteResult.size(), bFee, bTime));


        sb.append("\n=================================================================\n");
        sb.append("  COMPARISON — Accuracy vs. Performance Trade-off\n");
        sb.append("=================================================================\n");
        sb.append(String.format("  %-16s  %-8s  %-12s  %-10s%n",
                  "Method", "Spots", "Cost (NPR)", "Time (hr)"));
        sb.append(String.format("  %-16s  %-8d  %-12d  %-10d%n",
                  "Greedy", greedyResult.size(), totalFee, totalTime));
        sb.append(String.format("  %-16s  %-8d  %-12d  %-10d%n",
                  "Brute-Force", bruteResult.size(), bFee, bTime));
        sb.append("\n  Time Complexity:\n");
        sb.append("    Greedy     : O(n log n)  — fast, scalable, near-optimal\n");
        sb.append("    Brute-Force: O(2^n)      — exact, only feasible for small n\n");
        sb.append("\n  For n=5 both finish instantly.\n");
        sb.append("  For n>=20, brute-force becomes impractical (>1M subsets).\n");
        sb.append("  Greedy commits to locally optimal picks and may miss\n");
        sb.append("  the global optimum, but runs in O(n log n) time.\n");

        resultArea.setText(sb.toString());
        resultArea.setCaretPosition(0);
        mapPanel.setRoute(greedySpots);
    }


    static class MapPanel extends JPanel {
        private List<TouristSpot> route = Collections.emptyList();

        void setRoute(List<TouristSpot> r) { this.route = r; repaint(); }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);

            int pad = 50;
            int W   = getWidth()  - pad * 2;
            int H   = getHeight() - pad * 2 - 30;

            double minLat = ALL_SPOTS.stream().mapToDouble(s -> s.latitude).min().orElse(0);
            double maxLat = ALL_SPOTS.stream().mapToDouble(s -> s.latitude).max().orElse(1);
            double minLon = ALL_SPOTS.stream().mapToDouble(s -> s.longitude).min().orElse(0);
            double maxLon = ALL_SPOTS.stream().mapToDouble(s -> s.longitude).max().orElse(1);
            double dLat   = Math.max(maxLat - minLat, 0.001);
            double dLon   = Math.max(maxLon - minLon, 0.001);

            int[][] sc = new int[ALL_SPOTS.size()][2];
            for (int i = 0; i < ALL_SPOTS.size(); i++) {
                TouristSpot s = ALL_SPOTS.get(i);
                sc[i][0] = pad + (int) ((s.longitude - minLon) / dLon * W);
                sc[i][1] = pad + (int) ((1 - (s.latitude - minLat) / dLat) * H);
            }

            for (int i = 0; i < ALL_SPOTS.size(); i++) {
                g2.setColor(new Color(200, 200, 200));
                g2.fillOval(sc[i][0] - 6, sc[i][1] - 6, 12, 12);
                g2.setColor(new Color(80, 80, 80));
                g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
                g2.drawString(ALL_SPOTS.get(i).name.split(" ")[0],
                              sc[i][0] + 9, sc[i][1] + 4);
            }

            if (route.isEmpty()) {
                g2.setColor(Color.GRAY);
                g2.setFont(new Font("SansSerif", Font.ITALIC, 11));
                g2.drawString("Click \"Plan Itinerary\" to see the route.",
                              pad, getHeight() - 12);
                return;
            }

            List<int[]> pts = new ArrayList<>();
            for (TouristSpot rs : route)
                for (int i = 0; i < ALL_SPOTS.size(); i++)
                    if (ALL_SPOTS.get(i) == rs) { pts.add(sc[i]); break; }

            g2.setStroke(new BasicStroke(2.5f));
            g2.setColor(new Color(30, 100, 200));
            for (int i = 1; i < pts.size(); i++)
                g2.drawLine(pts.get(i-1)[0], pts.get(i-1)[1],
                            pts.get(i)[0],   pts.get(i)[1]);

            for (int i = 0; i < pts.size(); i++) {
                g2.setColor(new Color(34, 139, 34));
                g2.fillOval(pts.get(i)[0] - 10, pts.get(i)[1] - 10, 20, 20);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 11));
                g2.drawString(String.valueOf(i + 1),
                              pts.get(i)[0] - 4, pts.get(i)[1] + 5);
            }

            int ly = getHeight() - 12;
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            g2.setColor(new Color(34, 139, 34)); g2.fillRect(pad, ly - 11, 12, 12);
            g2.setColor(Color.BLACK);            g2.drawString("Selected route", pad + 16, ly);
            g2.setColor(new Color(200, 200, 200)); g2.fillRect(pad + 130, ly - 11, 12, 12);
            g2.setColor(Color.BLACK);            g2.drawString("Not selected",  pad + 146, ly);
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(Q5a_TouristOptimizerGUI::new);
    }
}
