package com.bankqueue.model;

// ═══════════════════════════════════════════════════════════════
//  RANDEVU MODELİ  (Min-Heap'te saklanır)
// ═══════════════════════════════════════════════════════════════
class Appointment implements Comparable<Appointment> {
    private static int counter = 1;

    private final int id;
    private final String customerName;
    private final int scheduledSecond;
    private boolean triggered = false;

    Appointment(String customerName, int scheduledSecond) {
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

    String getCustomerName() {
        return customerName;
    }

    int getScheduledSecond() {
        return scheduledSecond;
    }

    boolean isTriggered() {
        return triggered;
    }

    void trigger() {
        triggered = true;
    }

    static void resetCounter() {
        counter = 1;
    }

    static void setCounter(int v) {
        counter = v;
    }

    static int getCounter() {
        return counter;
    }

    String toCsv() {
        return id + "," + customerName + "," + scheduledSecond;
    }

    @Override
    public String toString() {
        return String.format("R#%03d  %s  @%s", id, customerName,
            SimulationEngine.fmt(scheduledSecond));
    }
}
