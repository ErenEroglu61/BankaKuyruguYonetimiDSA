package com.bankqueue.datastructures;

/**
 * ┌─────────────────────────────────────────────┐
 * MIN-HEAP — Dizi Tabanlı Tam İkili Ağaç
 * insert     → O(log n)
 * extractMin → O(log n)
 * peekMin    → O(1)
 * └─────────────────────────────────────────────┘
 */
public class MinHeap<T extends Comparable<T>> {
    private final java.util.ArrayList<T> h = new java.util.ArrayList<>();

    public void insert(T item) {
        h.add(item);
        bubbleUp(h.size() - 1);
    }

    public T extractMin() {
        if (isEmpty()) return null;
        T min = h.get(0);
        T last = h.remove(h.size() - 1);
        if (!isEmpty()) {
            h.set(0, last);
            sinkDown(0);
        }
        return min;
    }

    public T peekMin() {
        return isEmpty() ? null : h.get(0);
    }

    public boolean isEmpty() {
        return h.isEmpty();
    }

    public int size() {
        return h.size();
    }

    public void clear() {
        h.clear();
    }

    public java.util.List<T> toSortedList() {
        MinHeap<T> tmp = new MinHeap<>();
        for (T item : h) tmp.insert(item);
        java.util.List<T> res = new java.util.ArrayList<>();
        while (!tmp.isEmpty()) res.add(tmp.extractMin());
        return res;
    }

    private void bubbleUp(int i) {
        while (i > 0) {
            int p = (i - 1) / 2;
            if (h.get(i).compareTo(h.get(p)) < 0) {
                swap(i, p);
                i = p;
            } else break;
        }
    }

    private void sinkDown(int i) {
        int n = h.size();
        while (true) {
            int s = i, l = 2 * i + 1, r = 2 * i + 2;
            if (l < n && h.get(l).compareTo(h.get(s)) < 0) s = l;
            if (r < n && h.get(r).compareTo(h.get(s)) < 0) s = r;
            if (s == i) break;
            swap(i, s);
            i = s;
        }
    }

    private void swap(int a, int b) {
        T t = h.get(a);
        h.set(a, h.get(b));
        h.set(b, t);
    }
}
