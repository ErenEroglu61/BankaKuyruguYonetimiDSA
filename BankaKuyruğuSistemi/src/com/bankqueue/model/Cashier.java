package com.bankqueue.model;

import java.util.List;

// ═══════════════════════════════════════════════════════════════
//  GİŞE MODELİ
// ═══════════════════════════════════════════════════════════════
class Cashier {
    private final int id;
    private final CustomQueue<Customer> queue = new CustomQueue<>();
    private boolean open = true;
    private int totalServed;
    private long totalWait;

    Cashier(int id) {
        this.id = id;
    }

    void enqueue(Customer c) {
        queue.enqueue(c);
    }

    Customer serve(int simTime) {
        if (queue.isEmpty()) return null;
        Customer c = queue.dequeue();
        c.serve(simTime, id);
        totalServed++;
        totalWait += c.getWaitTime();
        return c;
    }

    int getId() {
        return id;
    }

    boolean isOpen() {
        return open;
    }

    void setOpen(boolean v) {
        open = v;
    }

    int queueSize() {
        return queue.size();
    }

    boolean queueEmpty() {
        return queue.isEmpty();
    }

    Customer peek() {
        return queue.peek();
    }

    List<Customer> list() {
        return queue.toList();
    }

    int getTotalServed() {
        return totalServed;
    }

    double avgWait() {
        return totalServed == 0 ? 0 : (double) totalWait / totalServed;
    }

    void reset() {
        queue.clear();
        totalServed = 0;
        totalWait = 0;
        open = true;
    }
}
