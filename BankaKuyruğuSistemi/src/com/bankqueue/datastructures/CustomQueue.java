package com.bankqueue.datastructures;

/**
 * ┌─────────────────────────────────────────────┐
 * FIFO KUYRUK — Tek Yönlü Bağlı Liste
 * enqueue → O(1)  |  dequeue → O(1)
 * peek    → O(1)  |  toList  → O(n)
 * └─────────────────────────────────────────────┘
 */
class CustomQueue<T> {
    private Node<T> front, rear;
    private int size;

    void enqueue(T data) {
        Node<T> n = new Node<>(data);
        if (rear == null) {
            front = rear = n;
        } else {
            rear.next = n;
            rear = n;
        }
        size++;
    }

    T dequeue() {
        if (isEmpty()) throw new RuntimeException("Kuyruk boş!");
        T d = front.data;
        front = front.next;
        if (front == null) rear = null;
        size--;
        return d;
    }

    T peek() {
        return isEmpty() ? null : front.data;
    }

    boolean isEmpty() {
        return size == 0;
    }

    int size() {
        return size;
    }

    void clear() {
        front = null;
        rear = null;
        size = 0;
    }

    java.util.List<T> toList() {
        java.util.List<T> list = new java.util.ArrayList<>(size);
        Node<T> cur = front;
        while (cur != null) {
            list.add(cur.data);
            cur = cur.next;
        }
        return list;
    }
}
