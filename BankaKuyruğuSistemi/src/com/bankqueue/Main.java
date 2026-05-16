package com.bankqueue;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;
import com.bankqueue.simulation.SimulationEngine;
import com.bankqueue.ui.*;
import com.bankqueue.model.*;
/**
 * ┌─────────────────────────────────────────────┐
 *   ANA GUI KATMANI
 *   Sadece görünüm + SimulationEngine çağrısı.
 *   İş mantığı içermez.
 * └─────────────────────────────────────────────┘
 *  Dosya yapısı:
 *  ├── theme.json          ← Renk paleti
 *  ├── Theme.java          ← JSON → Color
 *  ├── UIHelper.java       ← Swing bileşen fabrikası
 *  ├── DataStructures.java ← Node, CustomQueue, MinHeap
 *  ├── Models.java         ← Customer, Cashier, Appointment
 *  ├── SimulationEngine.java ← Tüm iş mantığı
 *  ├── src.com.bankqueue.model.BankData.java       ← File I/O (CSV + TXT)
 *  ├── ChartPanel.java     ← Grafik bileşeni
 *  └── BankaKuyrukSistemi.java ← Bu dosya (GUI)
 */
public class Main extends JFrame
        implements SimulationEngine.EventListener {

    private final SimulationEngine engine = new SimulationEngine();
    private javax.swing.Timer simTimer;
    private boolean           running = false;
    private JLabel lblTime, lblQTotal, lblServed, lblAvgWait;
    private JButton   btnStartStop;
    private JSlider   sldSpeed;
    private JTextArea logArea;
    private JPanel queueCardsPanel;
    private JPanel cashierPanel;
    private JTextField tfApptName;
    private JSpinner   spnApptMin;
    private JPanel     apptListPanel;
    private ChartPanel waitChart, queueChart;
    private DefaultTableModel historyModel;

    public Main() {
        super("🏦  Banka Kuyruk Sistemi");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1120, 730);
        setMinimumSize(new Dimension(960, 650));
        setLocationRelativeTo(null);

        engine.setListener(this);
        buildUI();
        buildTimer();
        setVisible(true);
        onStateChanged();
    }

    @Override
    public void onLog(String msg) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(msg + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    @Override
    public void onStateChanged() {
        SwingUtilities.invokeLater(this::refreshAll);
    }

    private void buildUI() {
        JPanel root = UIHelper.panel(new BorderLayout(10, 10));
        root.setBorder(UIHelper.padding(12, 12));

        root.add(buildTopBar(),  BorderLayout.NORTH);
        root.add(buildCenter(),  BorderLayout.CENTER);
        root.add(buildLogBar(),  BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JPanel buildTopBar() {
        JPanel p = UIHelper.surfacePanel(new BorderLayout());
        p.setBorder(BorderFactory.createCompoundBorder(
            new UIHelper.RoundBorder(12, Theme.BORDER),
            UIHelper.padding(12, 20)));

        lblTime = UIHelper.label("⏱  00:00:00", Theme.PRIMARY, 16, Font.BOLD);

        JPanel stats = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        stats.setOpaque(false);
        lblQTotal  = UIHelper.label("Kuyruk: 0",  Theme.FG2,     13);
        lblServed  = UIHelper.label("Servis: 0",  Theme.SUCCESS, 13, Font.BOLD);
        lblAvgWait = UIHelper.label("Ort: 0s",    Theme.WARNING, 13, Font.BOLD);
        stats.add(lblQTotal); stats.add(lblServed);
        stats.add(lblAvgWait); stats.add(lblTime);

        JLabel title = UIHelper.label("🏦  Banka Kuyruk Sistemi", Theme.FG, 18, Font.BOLD);
        p.add(title, BorderLayout.WEST);
        p.add(stats, BorderLayout.EAST);
        return p;
    }

    private JSplitPane buildCenter() {
        JSplitPane sp = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT, buildTabs(), buildSidePanel());
        sp.setDividerLocation(710);
        sp.setDividerSize(7);
        sp.setBackground(Theme.BG);
        sp.setBorder(null);
        return sp;
    }

    private JTabbedPane buildTabs() {
        JTabbedPane tp = new JTabbedPane();
        tp.setBackground(Theme.BG);
        tp.setForeground(Theme.FG);
        tp.setFont(Theme.bold(13));

        tp.addTab("📋 Kuyruk",      buildQueueTab());
        tp.addTab("🏪 Gişeler",     buildCashierTab());
        tp.addTab("⏰ Randevular",  buildApptTab());
        tp.addTab("📊 Analiz",      buildChartTab());
        tp.addTab("📜 Geçmiş",      buildHistoryTab());
        return tp;
    }

    private JScrollPane buildQueueTab() {
        queueCardsPanel = UIHelper.panel(null);
        queueCardsPanel.setLayout(new BoxLayout(queueCardsPanel, BoxLayout.Y_AXIS));
        queueCardsPanel.setBorder(UIHelper.padding(8, 8));
        return UIHelper.scroll(queueCardsPanel);
    }

    private JPanel buildCashierTab() {
        JPanel p = UIHelper.panel(new BorderLayout(8, 8));
        p.setBorder(UIHelper.padding(10, 10));

        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        topRow.setBackground(Theme.BG);
        topRow.add(UIHelper.label("Gişe Sayısı:", Theme.FG, 13, Font.BOLD));
        JSpinner spn = UIHelper.spinner(engine.cashierCount, 1, 5, 1);
        JButton  btn = UIHelper.button("Uygula", Theme.PRIMARY);
        btn.setPreferredSize(new Dimension(90, 32));
        btn.addActionListener(e -> {
            engine.setCashierCount((Integer) spn.getValue());
            refreshCashierPanel();
        });
        topRow.add(spn); topRow.add(btn);
        topRow.add(UIHelper.label("  Her gişeye en-az-dolu dağıtım — LinkedList Queue",
                                  Theme.FG2, 11, Font.ITALIC));
        p.add(topRow, BorderLayout.NORTH);

        cashierPanel = UIHelper.panel(new GridLayout(1, engine.cashierCount, 8, 0));
        JScrollPane sc = UIHelper.scroll(cashierPanel);
        sc.setBorder(null);
        p.add(sc, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildApptTab() {
        JPanel p = UIHelper.panel(new BorderLayout(8, 10));
        p.setBorder(UIHelper.padding(10, 10));

        // Randevu ekleme formu
        JPanel form = UIHelper.surfacePanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        form.setBorder(BorderFactory.createCompoundBorder(
            new UIHelper.RoundBorder(8, Theme.BORDER), UIHelper.padding(4, 8)));
        tfApptName = UIHelper.textField();
        tfApptName.setPreferredSize(new Dimension(160, 32));
        spnApptMin = UIHelper.spinner(5, 1, 120, 1);
        JButton btnAdd = UIHelper.button("+ Randevu Ekle", Theme.PURPLE);
        btnAdd.setPreferredSize(new Dimension(140, 32));
        btnAdd.addActionListener(e -> {
            engine.addAppointment(tfApptName.getText(), (Integer) spnApptMin.getValue());
            tfApptName.setText("");
        });

        form.add(UIHelper.label("Ad:", Theme.FG, 12, Font.BOLD));
        form.add(tfApptName);
        form.add(UIHelper.label("Kaç dakika sonra?", Theme.FG2, 12));
        form.add(spnApptMin);
        form.add(UIHelper.label("dk", Theme.FG2, 12));
        form.add(btnAdd);
        p.add(form, BorderLayout.NORTH);

        apptListPanel = UIHelper.panel(null);
        apptListPanel.setLayout(new BoxLayout(apptListPanel, BoxLayout.Y_AXIS));
        JScrollPane sc = UIHelper.scroll(apptListPanel);
        sc.setBorder(UIHelper.titledBorder("  ⏰  Bekleyen Randevular  (Min-Heap sırası)  "));
        p.add(sc, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildChartTab() {
        JPanel p = UIHelper.panel(new GridLayout(2, 1, 8, 8));
        p.setBorder(UIHelper.padding(10, 10));

        waitChart  = new ChartPanel("Bekleme Süresi (sn)", engine.waitSamples,  Theme.PRIMARY);
        queueChart = new ChartPanel("Kuyruk Yoğunluğu",    engine.queueSamples, Theme.SUCCESS);

        p.add(wrapChart(waitChart,  "📈  Bekleme Süresi"));
        p.add(wrapChart(queueChart, "📉  Kuyruk Yoğunluğu"));
        return p;
    }

    private JPanel wrapChart(ChartPanel chart, String title) {
        JPanel w = UIHelper.surfacePanel(new BorderLayout());
        w.setBorder(BorderFactory.createCompoundBorder(
            UIHelper.titledBorder("  " + title + "  "), UIHelper.padding(6, 6)));
        w.add(chart, BorderLayout.CENTER);
        return w;
    }

    private JScrollPane buildHistoryTab() {
        String[] cols = {"#","Ad","Tür","Gişe","Varış","Servis","Bekleme"};
        historyModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tbl = new JTable(historyModel);
        tbl.setBackground(Theme.SURFACE);
        tbl.setForeground(Theme.FG);
        tbl.setGridColor(Theme.BORDER);
        tbl.getTableHeader().setBackground(Theme.SURF2);
        tbl.getTableHeader().setForeground(Theme.FG2);
        tbl.getTableHeader().setFont(Theme.bold(12));
        tbl.setFont(Theme.plain(12));
        tbl.setRowHeight(26);
        tbl.setSelectionBackground(new Color(Theme.PRIMARY.getRed(),
                                             Theme.PRIMARY.getGreen(),
                                             Theme.PRIMARY.getBlue(), 60));

        int[] widths = {40, 160, 65, 55, 80, 80, 80};
        for (int i = 0; i < widths.length; i++)
            tbl.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        JScrollPane sc = new JScrollPane(tbl);
        sc.setBackground(Theme.SURFACE);
        sc.getViewport().setBackground(Theme.SURFACE);
        sc.setBorder(null);
        return sc;
    }

    private JPanel buildSidePanel() {
        JPanel p = UIHelper.panel(null);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(UIHelper.padding(2, 8));

        p.add(buildAddBox());    p.add(UIHelper.vgap(8));
        p.add(buildServeBox());  p.add(UIHelper.vgap(8));
        p.add(buildSimBox());    p.add(UIHelper.vgap(8));
        p.add(buildIOBox());     p.add(UIHelper.vgap(8));
        p.add(buildResetBtn());
        p.add(Box.createVerticalGlue());
        return p;
    }

    private JPanel buildAddBox() {
        JPanel p = UIHelper.surfacePanel(new BorderLayout(6, 6));
        p.setBorder(BorderFactory.createCompoundBorder(
            UIHelper.titledBorder("  ➕  Müşteri Ekle  "), UIHelper.padding(8, 8)));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        JTextField tfName = UIHelper.textField();
        JCheckBox  cbVip  = new JCheckBox("VIP ⭐");
        cbVip.setForeground(Theme.WARNING);
        cbVip.setBackground(Theme.SURFACE);
        cbVip.setFont(Theme.bold(12));

        JButton btn = UIHelper.button("Ekle", Theme.PRIMARY);
        tfName.addActionListener(e -> { engine.addCustomer(tfName.getText(), cbVip.isSelected()); tfName.setText(""); cbVip.setSelected(false); });
        btn.addActionListener(e -> { engine.addCustomer(tfName.getText(), cbVip.isSelected()); tfName.setText(""); cbVip.setSelected(false); });

        JPanel nameRow = UIHelper.panel(new BorderLayout(4, 0));
        nameRow.add(tfName, BorderLayout.CENTER);
        nameRow.add(cbVip,  BorderLayout.EAST);

        p.add(UIHelper.label("Ad Soyad:", Theme.FG2, 11), BorderLayout.NORTH);
        p.add(nameRow, BorderLayout.CENTER);
        p.add(btn,     BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildServeBox() {
        JPanel p = UIHelper.panel(new GridLayout(2, 1, 4, 4));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 82));

        JButton btnAll = UIHelper.button("➡  Tüm Gişelerde Servis", Theme.SUCCESS);
        btnAll.addActionListener(e -> engine.serveAll());

        JButton btn1 = UIHelper.button("➡  Gişe #1 Servis", Theme.TEAL);
        btn1.addActionListener(e -> engine.serveCashier(0));

        p.add(btnAll); p.add(btn1);
        return p;
    }

    private JPanel buildSimBox() {
        JPanel p = UIHelper.surfacePanel(new BorderLayout(6, 8));
        p.setBorder(BorderFactory.createCompoundBorder(
            UIHelper.titledBorder("  ⚙  Simülasyon  "), UIHelper.padding(8, 8)));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        btnStartStop = UIHelper.button("▶  Başlat", Theme.SUCCESS);
        btnStartStop.addActionListener(e -> toggleSim());

        sldSpeed = new JSlider(1, 10, 3);
        sldSpeed.setBackground(Theme.SURFACE);
        sldSpeed.setForeground(Theme.FG2);
        sldSpeed.setPaintTicks(true);
        sldSpeed.setMajorTickSpacing(3);

        JPanel row = UIHelper.surfacePanel(new BorderLayout(4, 0));
        row.add(UIHelper.label("Hız:", Theme.FG2, 11), BorderLayout.WEST);
        row.add(sldSpeed, BorderLayout.CENTER);

        p.add(btnStartStop, BorderLayout.NORTH);
        p.add(row,          BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildIOBox() {
        JPanel p = UIHelper.panel(new GridLayout(1, 2, 4, 0));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        JButton btnSave = UIHelper.button("💾 Kaydet", Theme.PURPLE);
        btnSave.addActionListener(e -> {
            engine.save();
            JOptionPane.showMessageDialog(this, "Veriler kaydedildi ✅", "Kayıt", JOptionPane.INFORMATION_MESSAGE);
        });
        JButton btnLoad = UIHelper.button("📂 Yükle", new Color(71, 85, 105));
        btnLoad.addActionListener(e -> {
            engine.load();
            // Geçmiş tablosunu sıfırla + doldur
            historyModel.setRowCount(0);
            for (Customer c : engine.history) addHistoryRow(c);
        });
        p.add(btnSave); p.add(btnLoad);
        return p;
    }

    private JButton buildResetBtn() {
        JButton b = UIHelper.button("🔄  Sıfırla", new Color(100, 116, 139));
        b.addActionListener(e -> {
            simTimer.stop(); running = false;
            btnStartStop.setText("▶  Başlat");
            btnStartStop.setBackground(Theme.SUCCESS);
            engine.reset();
            historyModel.setRowCount(0);
        });
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        return b;
    }

    private JPanel buildLogBar() {
        logArea = new JTextArea(5, 0);
        logArea.setBackground(Theme.SURFACE);
        logArea.setForeground(Theme.FG2);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        logArea.setEditable(false);
        logArea.setBorder(UIHelper.padding(4, 8));

        JScrollPane sc = new JScrollPane(logArea);
        sc.setBorder(UIHelper.titledBorder("  📝  İşlem Geçmişi  "));
        sc.setPreferredSize(new Dimension(0, 118));

        JPanel wrap = UIHelper.panel(new BorderLayout());
        wrap.add(sc);
        return wrap;
    }

    private void buildTimer() {
        simTimer = new javax.swing.Timer(400, e -> {
            engine.tick();
            lblTime.setText("⏱  " + SimulationEngine.fmt(engine.simTime));
            waitChart.repaint();
            queueChart.repaint();
            simTimer.setDelay(Math.max(80, 1000 / sldSpeed.getValue()));
        });
    }

    private void toggleSim() {
        running = !running;
        if (running) {
            simTimer.start();
            btnStartStop.setText("⏸  Durdur");
            btnStartStop.setBackground(Theme.DANGER);
            onLog("▶️  Simülasyon başladı.");
        } else {
            simTimer.stop();
            btnStartStop.setText("▶  Devam Et");
            btnStartStop.setBackground(Theme.SUCCESS);
            onLog("⏸  Simülasyon durdu.");
        }
    }

    private void refreshAll() {
        lblQTotal.setText("Kuyruk: " + engine.totalQueueSize());
        lblServed.setText("Servis: " + engine.totalServed);
        lblAvgWait.setText("Ort: "   + engine.avgWait() + "s");
        refreshQueueCards();
        refreshCashierPanel();
        refreshApptList();
    }

    private void refreshQueueCards() {
        queueCardsPanel.removeAll();
        boolean any = false;
        for (Cashier cs : engine.cashiers) {
            List<Customer> list = cs.list();
            if (list.isEmpty()) continue;
            any = true;
            queueCardsPanel.add(sectionLabel("Gişe #" + cs.getId()));
            queueCardsPanel.add(UIHelper.vgap(3));
            int pos = 0;
            for (Customer c : list) {
                queueCardsPanel.add(customerCard(c, pos++, cs.getId()));
                queueCardsPanel.add(UIHelper.vgap(3));
            }
            queueCardsPanel.add(UIHelper.vgap(6));
        }
        if (!any) {
            JLabel e = UIHelper.label("Tüm kuyruklar boş", Theme.FG2, 13, Font.ITALIC);
            e.setAlignmentX(CENTER_ALIGNMENT);
            e.setBorder(UIHelper.padding(24, 0));
            queueCardsPanel.add(e);
        }
        queueCardsPanel.revalidate();
        queueCardsPanel.repaint();
    }

    private void refreshCashierPanel() {
        cashierPanel.removeAll();
        cashierPanel.setLayout(new GridLayout(1, engine.cashierCount, 8, 0));
        for (Cashier cs : engine.cashiers) cashierPanel.add(cashierCard(cs));
        cashierPanel.revalidate();
        cashierPanel.repaint();
    }

    private void refreshApptList() {
        apptListPanel.removeAll();
        List<Appointment> list = engine.apptHeap.toSortedList();
        if (list.isEmpty()) {
            JLabel e = UIHelper.label("Bekleyen randevu yok", Theme.FG2, 12, Font.ITALIC);
            e.setAlignmentX(CENTER_ALIGNMENT);
            e.setBorder(UIHelper.padding(16, 0));
            apptListPanel.add(e);
        } else {
            for (Appointment a : list) {
                apptListPanel.add(apptCard(a));
                apptListPanel.add(UIHelper.vgap(4));
            }
        }
        apptListPanel.revalidate();
        apptListPanel.repaint();
    }

    private void addHistoryRow(Customer c) {
        historyModel.addRow(new Object[]{
            "#" + c.getId(), c.getName(),
            c.getType() == Customer.Type.VIP ? "⭐ VIP" : "Norm.",
            "G#" + (c.getCashierNo() < 0 ? "-" : c.getCashierNo()),
            SimulationEngine.fmt(c.getArrivalSecond()),
            c.isServed() ? SimulationEngine.fmt(c.getServedSecond()) : "-",
            c.isServed() ? c.getWaitTime() + "s" : "-"
        });
    }

    private JPanel customerCard(Customer c, int pos, int cashierId) {
        boolean first = (pos == 0);
        boolean vip   = c.getType() == Customer.Type.VIP;

        Color bg  = vip   ? new Color(60, 20, 100)
                  : first ? new Color(100, 40, 0)
                  : Theme.SURFACE;
        Color bdr = vip   ? Theme.PURPLE
                  : first ? Theme.PRIMARY
                  : Theme.BORDER;

        JPanel card = UIHelper.panel(new BorderLayout(8, 0));
        card.setBackground(bg);
        card.setBorder(BorderFactory.createCompoundBorder(
            new UIHelper.RoundBorder(8, bdr), UIHelper.padding(9, 12)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel numLbl = UIHelper.label((pos+1) + ".", Theme.WARNING, 13, Font.BOLD);
        numLbl.setPreferredSize(new Dimension(26, 0));

        JPanel center = UIHelper.panel(new BorderLayout(0, 2));
        center.setBackground(bg);
        center.add(UIHelper.label(c.getName(), Theme.FG, 13, Font.BOLD), BorderLayout.CENTER);
        String sub = vip ? "⭐ VIP" : (first ? "Sıradaki" : "");
        if (!sub.isEmpty())
            center.add(UIHelper.label(sub, vip ? Theme.WARNING : Theme.SUCCESS, 10, Font.BOLD),
                       BorderLayout.SOUTH);

        JLabel waitLbl = UIHelper.label(
            (engine.simTime - c.getArrivalSecond()) + "s",
            first ? new Color(255, 200, 120) : Theme.FG2, 11);
        waitLbl.setHorizontalAlignment(SwingConstants.RIGHT);

        card.add(numLbl,  BorderLayout.WEST);
        card.add(center,  BorderLayout.CENTER);
        card.add(waitLbl, BorderLayout.EAST);
        return card;
    }

    private JPanel cashierCard(Cashier cs) {
        JPanel p = UIHelper.surfacePanel(new BorderLayout(0, 6));
        p.setBorder(BorderFactory.createCompoundBorder(
            new UIHelper.RoundBorder(10, cs.isOpen() ? Theme.PRIMARY : Theme.BORDER),
            UIHelper.padding(10, 10)));

        JPanel header = UIHelper.surfacePanel(new BorderLayout(4, 0));
        header.add(UIHelper.label("Gişe #" + cs.getId(), Theme.FG, 14, Font.BOLD), BorderLayout.WEST);
        header.add(UIHelper.label(cs.isOpen() ? "● Açık" : "● Kapalı",
                                  cs.isOpen() ? Theme.SUCCESS : Theme.DANGER, 11, Font.BOLD),
                   BorderLayout.EAST);

        JButton toggle = UIHelper.button(cs.isOpen() ? "Kapat" : "Aç",
                                         cs.isOpen() ? Theme.DANGER : Theme.SUCCESS);
        toggle.setPreferredSize(new Dimension(58, 24));
        toggle.addActionListener(e -> { cs.setOpen(!cs.isOpen()); refreshCashierPanel(); });
        header.add(toggle, BorderLayout.CENTER);
        p.add(header, BorderLayout.NORTH);

        JPanel mid = UIHelper.surfacePanel(new GridLayout(2, 2, 4, 4));
        mid.add(miniStat("Kuyruk",   "" + cs.queueSize()));
        mid.add(miniStat("Servis",   "" + cs.getTotalServed()));
        mid.add(miniStat("Ort.Bkl.", String.format("%.1fs", cs.avgWait())));
        mid.add(miniStat("Durum",    cs.isOpen() ? "Açık" : "Kapalı"));
        p.add(mid, BorderLayout.CENTER);

        Customer next = cs.peek();
        if (next != null) {
            JLabel nl = UIHelper.label("▶ " + next.getName(), Theme.WARNING, 11, Font.BOLD);
            nl.setBorder(UIHelper.padding(4, 0));
            p.add(nl, BorderLayout.SOUTH);
        }
        return p;
    }

    private JPanel apptCard(Appointment a) {
        int remaining = a.getScheduledSecond() - engine.simTime;
        Color col = remaining <= 30 ? Theme.DANGER
                  : remaining <= 120 ? Theme.WARNING
                  : Theme.TEAL;

        JPanel card = UIHelper.surfacePanel(new BorderLayout(8, 0));
        card.setBorder(BorderFactory.createCompoundBorder(
            new UIHelper.RoundBorder(8, col), UIHelper.padding(8, 12)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(UIHelper.label("📅 " + a.getCustomerName(), Theme.FG, 13, Font.BOLD),
                 BorderLayout.WEST);

        JPanel right = UIHelper.surfacePanel(new BorderLayout(0, 2));
        right.add(UIHelper.label(SimulationEngine.fmt(a.getScheduledSecond()), Theme.FG2, 11),
                  BorderLayout.NORTH);
        right.add(UIHelper.label(remaining > 0 ? remaining + "s kaldı" : "Şimdi!",
                                 col, 11, Font.BOLD), BorderLayout.SOUTH);
        card.add(right, BorderLayout.EAST);
        return card;
    }

    private JLabel sectionLabel(String text) {
        JLabel l = UIHelper.label("— " + text + " —", Theme.PRIMARY, 11, Font.BOLD);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setBorder(UIHelper.padding(4, 4));
        return l;
    }

    private JPanel miniStat(String lbl, String val) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(UIHelper.label(lbl, Theme.FG2, 10), BorderLayout.NORTH);
        p.add(UIHelper.label(val, Theme.FG,  13, Font.BOLD), BorderLayout.CENTER);
        return p;
    }

    static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(Main::new);
    }
}
