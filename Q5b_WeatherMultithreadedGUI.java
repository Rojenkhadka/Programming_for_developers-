import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Q5b_WeatherMultithreadedGUI extends JFrame {

    private static final String[] CITIES = {
        "Kathmandu", "Pokhara", "Biratnagar", "Nepalgunj", "Dhangadhi"
    };


    private static double[] fetchMockWeather(String city) throws InterruptedException {

        Random rnd = new Random(city.hashCode());
        Thread.sleep(200 + rnd.nextInt(600));
        double temp     = 15 + (Math.abs(city.hashCode() % 20));
        double humidity = 55 + (Math.abs(city.hashCode() % 40));
        double pressure = 1010 + (Math.abs(city.hashCode() % 30));
        double wind     = 5  + (Math.abs(city.hashCode() % 25));
        return new double[]{temp, humidity, pressure, wind};
    }

    private final DefaultTableModel tableModel;
    private final JTable            table;
    private final JTextArea         logArea    = new JTextArea(6, 50);
    private final ChartPanel        chartPanel = new ChartPanel();
    private final JButton           fetchBtn   = new JButton("Fetch Weather (Parallel)");
    private final JButton           seqBtn     = new JButton("Sequential Benchmark");
    private final JLabel            statusLbl  = new JLabel("Ready.");

    private long lastParallelMs   = 0;
    private long lastSequentialMs = 0;

    public Q5b_WeatherMultithreadedGUI() {
        setTitle("Multi-threaded Weather Data Collector – Nepal Cities");
        setSize(820, 680);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(6, 6));

        String[] cols = {"City", "Temp (°C)", "Humidity (%)", "Pressure (hPa)", "Wind (km/h)", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        for (String city : CITIES)
            tableModel.addRow(new Object[]{city, "--", "--", "--", "--", "Waiting"});

        table = new JTable(tableModel);
        table.setRowHeight(24);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getColumnModel().getColumn(0).setPreferredWidth(130);
        JScrollPane tableSP = new JScrollPane(table);
        tableSP.setBorder(BorderFactory.createTitledBorder("Weather Data"));
        tableSP.setPreferredSize(new Dimension(780, 180));

        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JScrollPane logSP = new JScrollPane(logArea);
        logSP.setBorder(BorderFactory.createTitledBorder("Thread Log"));

        chartPanel.setBorder(BorderFactory.createTitledBorder("Sequential vs Parallel Latency"));
        chartPanel.setPreferredSize(new Dimension(780, 160));


        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        fetchBtn.setBackground(new Color(30, 100, 200)); fetchBtn.setForeground(Color.WHITE);
        seqBtn.setBackground(new Color(160, 60, 60));   seqBtn.setForeground(Color.WHITE);
        btnPanel.add(fetchBtn); btnPanel.add(seqBtn); btnPanel.add(statusLbl);

        JPanel centre = new JPanel();
        centre.setLayout(new BoxLayout(centre, BoxLayout.Y_AXIS));
        centre.add(tableSP);
        centre.add(logSP);
        centre.add(chartPanel);

        add(btnPanel, BorderLayout.NORTH);
        add(new JScrollPane(centre), BorderLayout.CENTER);

        fetchBtn.addActionListener(e -> fetchParallel());
        seqBtn.addActionListener(e  -> fetchSequential());

        setVisible(true);
    }

    private void fetchParallel() {
        setButtons(false);
        resetTable();
        log("=== Parallel fetch started (" + CITIES.length + " threads) ===");
        AtomicInteger done = new AtomicInteger(0);
        long start = System.currentTimeMillis();

        for (int idx = 0; idx < CITIES.length; idx++) {
            final int i = idx;
            Thread t = new Thread(() -> {
                String city = CITIES[i];
                log("  Thread-" + (i+1) + " [" + city + "] started");
                try {
                    double[] w = fetchMockWeather(city);
                    long elapsed = System.currentTimeMillis() - start;
                    SwingUtilities.invokeLater(() -> {
                        tableModel.setValueAt(String.format("%.1f", w[0]), i, 1);
                        tableModel.setValueAt(String.format("%.0f", w[1]), i, 2);
                        tableModel.setValueAt(String.format("%.1f", w[2]), i, 3);
                        tableModel.setValueAt(String.format("%.1f", w[3]), i, 4);
                        tableModel.setValueAt("Done (" + elapsed + " ms)", i, 5);
                    });
                    log("  Thread-" + (i+1) + " [" + city + "] done in " + elapsed + " ms");
                } catch (InterruptedException ex) {
                    SwingUtilities.invokeLater(() -> tableModel.setValueAt("Error", i, 5));
                }
                if (done.incrementAndGet() == CITIES.length) {
                    long total = System.currentTimeMillis() - start;
                    lastParallelMs = total;
                    SwingUtilities.invokeLater(() -> {
                        log("=== All threads done. Parallel total: " + total + " ms ===");
                        statusLbl.setText("Parallel done in " + total + " ms");
                        chartPanel.setValues(lastSequentialMs, lastParallelMs);
                        setButtons(true);
                    });
                }
            }, "WeatherThread-" + (i+1));
            t.setDaemon(true);
            t.start();
        }
    }

    private void fetchSequential() {
        setButtons(false);
        resetTable();
        log("=== Sequential fetch started (1 thread) ===");
        new Thread(() -> {
            long start = System.currentTimeMillis();
            for (int i = 0; i < CITIES.length; i++) {
                final int idx = i;
                String city = CITIES[i];
                log("  Fetching [" + city + "] sequentially...");
                try {
                    double[] w = fetchMockWeather(city);
                    long elapsed = System.currentTimeMillis() - start;
                    SwingUtilities.invokeLater(() -> {
                        tableModel.setValueAt(String.format("%.1f", w[0]), idx, 1);
                        tableModel.setValueAt(String.format("%.0f", w[1]), idx, 2);
                        tableModel.setValueAt(String.format("%.1f", w[2]), idx, 3);
                        tableModel.setValueAt(String.format("%.1f", w[3]), idx, 4);
                        tableModel.setValueAt("Done (" + elapsed + " ms)", idx, 5);
                    });
                    log("  [" + city + "] done at " + elapsed + " ms");
                } catch (InterruptedException ex) {
                    SwingUtilities.invokeLater(() -> tableModel.setValueAt("Error", idx, 5));
                }
            }
            long total = System.currentTimeMillis() - start;
            lastSequentialMs = total;
            SwingUtilities.invokeLater(() -> {
                log("=== Sequential done. Total: " + total + " ms ===");
                statusLbl.setText("Sequential done in " + total + " ms");
                chartPanel.setValues(lastSequentialMs, lastParallelMs);
                setButtons(true);
            });
        }, "SeqThread").start();
    }

    private void resetTable() {
        for (int i = 0; i < CITIES.length; i++) {
            tableModel.setValueAt("--", i, 1);
            tableModel.setValueAt("--", i, 2);
            tableModel.setValueAt("--", i, 3);
            tableModel.setValueAt("--", i, 4);
            tableModel.setValueAt("Fetching...", i, 5);
        }
    }

    private void log(String msg) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(msg + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private void setButtons(boolean enabled) {
        fetchBtn.setEnabled(enabled);
        seqBtn.setEnabled(enabled);
    }

    static class ChartPanel extends JPanel {
        private long seqMs = 0;
        private long parMs = 0;

        void setValues(long seq, long par) { seqMs = seq; parMs = par; repaint(); }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (seqMs == 0 && parMs == 0) {
                g.setColor(Color.GRAY);
                g.setFont(new Font("SansSerif", Font.ITALIC, 12));
                g.drawString("Run both benchmarks to see comparison.", 20, getHeight() / 2);
                return;
            }
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int pad = 50, barW = 80, gap = 60;
            int maxH = getHeight() - pad * 2;
            long maxMs = Math.max(Math.max(seqMs, parMs), 1);

            // Sequential bar
            int hSeq = (int) ((double) seqMs / maxMs * maxH);
            int xSeq = pad + gap;
            g2.setColor(new Color(200, 80, 80));
            g2.fillRect(xSeq, getHeight() - pad - hSeq, barW, hSeq);
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            g2.drawString(seqMs + " ms", xSeq + 5, getHeight() - pad - hSeq - 4);
            g2.drawString("Sequential", xSeq, getHeight() - pad + 16);

            // Parallel bar
            int hPar = (int) ((double) parMs / maxMs * maxH);
            int xPar = xSeq + barW + gap;
            g2.setColor(new Color(30, 130, 200));
            g2.fillRect(xPar, getHeight() - pad - hPar, barW, hPar);
            g2.setColor(Color.BLACK);
            g2.drawString(parMs + " ms", xPar + 5, getHeight() - pad - hPar - 4);
            g2.drawString("Parallel",   xPar, getHeight() - pad + 16);

            // baseline
            g2.setColor(Color.DARK_GRAY);
            g2.drawLine(pad / 2, getHeight() - pad, getWidth() - pad / 2, getHeight() - pad);

            // speedup label
            if (parMs > 0) {
                double speedup = (double) seqMs / parMs;
                g2.setColor(new Color(34, 139, 34));
                g2.setFont(new Font("SansSerif", Font.BOLD, 13));
                g2.drawString(String.format("Speedup: %.2fx", speedup),
                              xPar + barW + 30, getHeight() / 2);
            }

            // axis label
            g2.setColor(Color.GRAY);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            g2.drawString("Latency (ms)", 4, getHeight() / 2);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Q5b_WeatherMultithreadedGUI::new);
    }
}
