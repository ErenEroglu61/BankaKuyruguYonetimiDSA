package com.bankqueue.model;

import com.bankqueue.simulation.SimulationEngine;

// ═══════════════════════════════════════════════════════════════
//  RANDEVU MODELİ  (Min-Heap'te saklanır)
// ═══════════════════════════════════════════════════════════════
public class Appointment implements Comparable<Appointment> {
    private static int counter = 1;

    private final int id;
    private final String customerName;
    private final int scheduledSecond;
    private boolean triggered = false;

    public Appointment(String customerName, int scheduledSecond) {
        this.id = counter++;
        this.customerName = customerName;
        this.scheduledSecond = scheduledSecond;
    }

    @Override
    public int compareTo(Appointment o) {
        return Integer.compare(scheduledSecond, o.scheduledSecond);
    }

    int getId() {
        return id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public int getScheduledSecond() {
        return scheduledSecond;
    }

    public boolean isTriggered() {
        return triggered;
    }

    void trigger() {
        triggered = true;
    }

    public static void resetCounter() {
        counter = 1;
    }

    public static void setCounter(int v) {
        counter = v;
    }

    public static int getCounter() {
        return counter;
    }

    public String toCsv() {
        return id + "," + customerName + "," + scheduledSecond;
    }

    @Override
    public String toString() {
        return String.format("R#%03d  %s  @%s", id, customerName,
            SimulationEngine.fmt(scheduledSecond));
    }
}
