package com.bankqueue.model;

import com.bankqueue.datastructures.CustomQueue;

import java.util.List;

//  GİŞE MODELİ, banka sorumlusu

public class Cashier {
    private final int id;
    private final CustomQueue<Customer> queue = new CustomQueue<>();
    private boolean open = true;
    private int totalServed;
    private long totalWait;

    public Cashier(int id) {
        this.id = id;
    }

    public void enqueue(Customer c) {
        queue.enqueue(c);
    }

    public Customer serve(int simTime) {
        if (queue.isEmpty()) return null;
        Customer c = queue.dequeue();
        c.serve(simTime, id);
        totalServed++;
        totalWait += c.getWaitTime();
        return c;
    }

    public int getId() {
        return id;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean v) {
        open = v;
    }

    public int queueSize() {
        return queue.size();
    }

    public boolean queueEmpty() {
        return queue.isEmpty();
    }

    public Customer peek() {
        return queue.peek();
    }

    public List<Customer> list() {
        return queue.toList();
    }

    public int getTotalServed() {
        return totalServed;
    }

    public double avgWait() {
        return totalServed == 0 ? 0 : (double) totalWait / totalServed;
    }

    public void reset() {
        queue.clear();
        totalServed = 0;
        totalWait = 0;
        open = true;
    }
}
