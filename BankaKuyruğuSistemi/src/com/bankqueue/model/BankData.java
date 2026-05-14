package com.bankqueue.model;

import com.bankqueue.datastructures.MinHeap;

import java.io.*;
import java.util.*;

/**
 * ┌─────────────────────────────────────────────┐
 *   KALICI DEPOLAMA KATMANI — File I/O
 *   CSV  → müşteri geçmişi & randevular
 *   TXT  → simülasyon durumu
 * └─────────────────────────────────────────────┘
 */
public class BankData {

    private static final String HISTORY_FILE     = "bank_history.csv";
    private static final String APPOINTMENT_FILE = "bank_appointments.csv";
    private static final String STATE_FILE       = "bank_state.txt";

    // ── Geçmiş ───────────────────────────────────────────────────
    public static void saveHistory(List<Customer> list) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(HISTORY_FILE))) {
            pw.println("id,name,arrivalSecond,servedSecond,cashierNo,type,waitTime");
            for (Customer c : list) pw.println(c.toCsv());
        } catch (IOException e) { err("Kayıt", e); }
    }

    public static void appendHistory(Customer c) {
        boolean exists = new File(HISTORY_FILE).exists();
        try (PrintWriter pw = new PrintWriter(new FileWriter(HISTORY_FILE, true))) {
            if (!exists) pw.println("id,name,arrivalSecond,servedSecond,cashierNo,type,waitTime");
            pw.println(c.toCsv());
        } catch (IOException e) { err("Append", e); }
    }

    public static List<Customer> loadHistory() {
        List<Customer> list = new ArrayList<>();
        File f = new File(HISTORY_FILE);
        if (!f.exists()) return list;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line; boolean first = true; int maxId = 0;
            while ((line = br.readLine()) != null) {
                if (first) { first = false; continue; }
                String[] p = line.split(",", -1);
                if (p.length < 7) continue;
                int id = Integer.parseInt(p[0].trim());
                Customer c = new Customer(p[1].trim(),
                    Integer.parseInt(p[2].trim()),
                    Customer.Type.valueOf(p[5].trim()));
                int served = Integer.parseInt(p[3].trim());
                int cashier = Integer.parseInt(p[4].trim());
                if (served >= 0) c.serve(served, cashier);
                if (id > maxId) maxId = id;
                list.add(c);
            }
            Customer.setCounter(maxId + 1);
        } catch (IOException | NumberFormatException e) { err("Yükleme", e); }
        return list;
    }

    // ── Randevular ────────────────────────────────────────────────
    public static void saveAppointments(MinHeap<Appointment> heap) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(APPOINTMENT_FILE))) {
            pw.println("apptId,name,scheduledSecond");
            for (Appointment a : heap.toSortedList())
                if (!a.isTriggered()) pw.println(a.toCsv());
        } catch (IOException e) { err("Randevu kayıt", e); }
    }

    public static void loadAppointments(MinHeap<Appointment> heap) {
        File f = new File(APPOINTMENT_FILE);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line; boolean first = true; int maxId = 0;
            while ((line = br.readLine()) != null) {
                if (first) { first = false; continue; }
                String[] p = line.split(",", -1);
                if (p.length < 3) continue;
                int id = Integer.parseInt(p[0].trim());
                Appointment a = new Appointment(p[1].trim(), Integer.parseInt(p[2].trim()));
                heap.insert(a);
                if (id > maxId) maxId = id;
            }
            Appointment.setCounter(maxId + 1);
        } catch (IOException | NumberFormatException e) { err("Randevu yükleme", e); }
    }

    // ── Durum ────────────────────────────────────────────────────
    public static void saveState(int simTime, int totalServed, long totalWait) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(STATE_FILE))) {
            pw.println("simTime="      + simTime);
            pw.println("totalServed="  + totalServed);
            pw.println("totalWait="    + totalWait);
            pw.println("customerIdCounter=" + Customer.getCounter());
            pw.println("apptIdCounter="     + Appointment.getCounter());
        } catch (IOException e) { err("Durum kayıt", e); }
    }

    public static Map<String, String> loadState() {
        Map<String, String> map = new HashMap<>();
        File f = new File(STATE_FILE);
        if (!f.exists()) return map;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] kv = line.split("=", 2);
                if (kv.length == 2) map.put(kv[0].trim(), kv[1].trim());
            }
        } catch (IOException e) { err("Durum yükleme", e); }
        return map;
    }

    public static void deleteAll() {
        new File(HISTORY_FILE).delete();
        new File(APPOINTMENT_FILE).delete();
        new File(STATE_FILE).delete();
    }

    private static void err(String ctx, Exception e) {
        System.err.println("[src.com.bankqueue.model.BankData] " + ctx + " hatası: " + e.getMessage());
    }
}