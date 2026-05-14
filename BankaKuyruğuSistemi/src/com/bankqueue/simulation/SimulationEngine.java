package com.bankqueue.simulation;

import src.com.bankqueue.model.BankData;

import java.util.ArrayList;
import java.util.List;

/**
 * ┌─────────────────────────────────────────────┐
 *   SİMÜLASYON MOTORU
 *   Tüm iş mantığı burada — UI'dan bağımsız.
 *   GUI sadece bu sınıfı çağırır.
 * └─────────────────────────────────────────────┘
 */
public class SimulationEngine {

    // ── Durum ─────────────────────────────────────────────────────
    int     simTime      = 0;
    int     totalServed  = 0;
    long    totalWait    = 0;
    int     cashierCount = 2;
    private int autoNameIdx = 0;

    Cashier[]              cashiers;
    MinHeap<Appointment>   apptHeap = new MinHeap<>();
    List<Customer>         history  = new ArrayList<>();

    // Grafik verileri
    List<Integer> waitSamples  = new ArrayList<>();
    List<Integer> queueSamples = new ArrayList<>();

    // Olay dinleyicisi → GUI güncelleme
    private EventListener listener;

    public interface EventListener {
        void onLog(String msg);
        void onStateChanged();
    }

    // ─────────────────────────────────────────────────────────────
    public SimulationEngine() {
        initCashiers(cashierCount);
        tryLoadState();
    }

    public void setListener(EventListener l) { this.listener = l; }

    // ── Gişe ─────────────────────────────────────────────────────
    public void initCashiers(int count) {
        cashierCount = count;
        cashiers     = new Cashier[count];
        for (int i = 0; i < count; i++) cashiers[i] = new Cashier(i + 1);
    }

    public void setCashierCount(int newCount) {
        List<Customer> pending = new ArrayList<>();
        for (Cashier c : cashiers) pending.addAll(c.list());
        initCashiers(newCount);
        int i = 0;
        for (Customer c : pending) cashiers[(i++) % newCount].enqueue(c);
        log("🏪  Gişe sayısı → " + newCount);
        notify_();
    }

    // ── Müşteri Ekle ──────────────────────────────────────────────
    public void addCustomer(String name, boolean vip) {
        if (name == null || name.isBlank()) name = autoName();
        Customer.Type type = vip ? Customer.Type.VIP : Customer.Type.NORMAL;
        Customer c = new Customer(name.trim(), simTime, type);

        Cashier target = leastBusy();
        target.enqueue(c);

        log("✅ [" + fmt(simTime) + "]  " + c + " → Gişe #" + target.getId()
            + "  (kuyruk: " + target.queueSize() + ")");
        notify_();
    }

    // ── Servis ────────────────────────────────────────────────────
    public void serveAll() {
        boolean any = false;
        for (Cashier cs : cashiers) {
            if (cs.isOpen() && !cs.queueEmpty()) {
                Customer c = cs.serve(simTime);
                if (c != null) { recordServed(c, cs); any = true; }
            }
        }
        if (!any) log("⚠️  Tüm gişeler boş.");
        notify_();
    }

    public void serveCashier(int idx) {
        if (idx >= cashiers.length) return;
        Cashier cs = cashiers[idx];
        if (cs.queueEmpty()) { log("⚠️  Gişe #" + (idx+1) + " boş."); return; }
        Customer c = cs.serve(simTime);
        if (c != null) recordServed(c, cs);
        notify_();
    }

    private void recordServed(Customer c, Cashier cs) {
        totalServed++;
        totalWait += c.getWaitTime();
        history.add(c);
        waitSamples.add(c.getWaitTime());
        BankData.appendHistory(c);
        log("🔔 [" + fmt(simTime) + "]  " + c
            + "  servis edildi  (Gişe #" + cs.getId()
            + ", Bekleme: " + c.getWaitTime() + "s)");
    }

    // ── Randevu ───────────────────────────────────────────────────
    public void addAppointment(String name, int minutes) {
        if (name == null || name.isBlank()) name = autoName();
        int targetSec = simTime + minutes * 60;
        Appointment a = new Appointment(name.trim(), targetSec);
        apptHeap.insert(a);
        log("📅 Randevu eklendi: " + a + "  [Heap: " + apptHeap.size() + "]");
        BankData.saveAppointments(apptHeap);
        notify_();
    }

    /** Min-Heap'ten olgunlaşan randevuları tetikle — O(log n) */
    public void checkAppointments() {
        while (!apptHeap.isEmpty()) {
            Appointment top = apptHeap.peekMin();
            if (top.getScheduledSecond() <= simTime) {
                apptHeap.extractMin();
                Customer c = new Customer(top.getCustomerName(), simTime, Customer.Type.NORMAL);
                cashiers[0].enqueue(c);
                log("🔔 Randevu tetiklendi: " + top.getCustomerName()
                    + " → Gişe #1  [Heap: " + apptHeap.size() + " kaldı]");
            } else break;
        }
    }

    // ── Simülasyon Adımı ──────────────────────────────────────────
    public void tick() {
        simTime++;
        checkAppointments();

        // Otomatik müşteri: her 6 sn
        if (simTime % 6 == 0) {
            Customer.Type t = (simTime % 30 == 0) ? Customer.Type.VIP : Customer.Type.NORMAL;
            Customer c = new Customer(autoName(), simTime, t);
            Cashier tgt = leastBusy();
            tgt.enqueue(c);
            log("🤖 Oto → " + c + " → Gişe #" + tgt.getId());
        }

        // Otomatik servis: her 9 sn
        if (simTime % 9 == 0) serveAll();

        // Grafik örnekleme: her 5 sn
        if (simTime % 5 == 0) {
            int totalQ = 0;
            for (Cashier cs : cashiers) totalQ += cs.queueSize();
            queueSamples.add(totalQ);
        }

        notify_();
    }

    // ── Kayıt / Yükleme ───────────────────────────────────────────
    public void save() {
        BankData.saveHistory(history);
        BankData.saveAppointments(apptHeap);
        BankData.saveState(simTime, totalServed, totalWait);
        log("💾  Veriler kaydedildi.");
    }

    public void load() {
        List<Customer> loaded = BankData.loadHistory();
        history.addAll(loaded);
        BankData.loadAppointments(apptHeap);
        var state = BankData.loadState();
        if (state.containsKey("simTime"))     simTime     = Integer.parseInt(state.get("simTime"));
        if (state.containsKey("totalServed")) totalServed = Integer.parseInt(state.get("totalServed"));
        if (state.containsKey("totalWait"))   totalWait   = Long.parseLong(state.get("totalWait"));
        log("📂  " + loaded.size() + " müşteri + " + apptHeap.size() + " randevu yüklendi.");
        notify_();
    }

    private void tryLoadState() {
        var state = BankData.loadState();
        if (state.containsKey("simTime"))     simTime     = Integer.parseInt(state.getOrDefault("simTime","0"));
        if (state.containsKey("totalServed")) totalServed = Integer.parseInt(state.getOrDefault("totalServed","0"));
        if (state.containsKey("totalWait"))   totalWait   = Long.parseLong(state.getOrDefault("totalWait","0"));
        BankData.loadAppointments(apptHeap);
    }

    // ── Sıfırla ───────────────────────────────────────────────────
    public void reset() {
        simTime = 0; totalServed = 0; totalWait = 0; autoNameIdx = 0;
        for (Cashier cs : cashiers) cs.reset();
        apptHeap.clear(); history.clear();
        waitSamples.clear(); queueSamples.clear();
        Customer.resetCounter(); Appointment.resetCounter();
        BankData.deleteAll();
        log("🔄  Sistem sıfırlandı.");
        notify_();
    }

    // ── Yardımcılar ───────────────────────────────────────────────
    private Cashier leastBusy() {
        Cashier t = cashiers[0];
        for (Cashier cs : cashiers)
            if (cs.isOpen() && cs.queueSize() < t.queueSize()) t = cs;
        return t;
    }

    public int totalQueueSize() {
        int n = 0;
        for (Cashier cs : cashiers) n += cs.queueSize();
        return n;
    }

    public long avgWait() {
        return totalServed == 0 ? 0 : totalWait / totalServed;
    }

    private void log(String msg) {
        if (listener != null) listener.onLog(msg);
    }

    private void notify_() {
        if (listener != null) listener.onStateChanged();
    }

    private String autoName() {
        String[] names = {
            "Ahmet Yılmaz","Ayşe Demir","Mehmet Çelik","Fatma Kaya",
            "Ali Öztürk","Zeynep Şahin","Hüseyin Arslan","Emine Güneş",
            "Mustafa Acar","Hatice Yıldız","Ömer Polat","Merve Koç",
            "Can Tekin","Selin Arslan","Burak Yıldız","Elif Şimşek"
        };
        return names[autoNameIdx++ % names.length];
    }

    /** Saniyeyi SS:DD:SS formatına çevirir */
    public static String fmt(int s) {
        return String.format("%02d:%02d:%02d", s/3600, (s%3600)/60, s%60);
    }
}
