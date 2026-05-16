package com.bankqueue.datastructures;

// Fifo queue - Singly Linked List

public class CustomQueue<T> {
    private Node<T> front, rear;
    private int size;

    public void enqueue(T data) {
        Node<T> n = new Node<>(data);
        if (rear == null) {
            front = rear = n;
        } else {
            rear.next = n;
            rear = n;
        }
        size++;
    }

    public T dequeue() {
        if (isEmpty()) throw new RuntimeException("Kuyruk boş!");
        T d = front.data;
        front = front.next;
        if (front == null) rear = null;
        size--;
        return d;
    }

    public T peek() {
        return isEmpty() ? null : front.data;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public void clear() {
        front = null;
        rear = null;
        size = 0;
    }

    public java.util.List<T> toList() {
        java.util.List<T> list = new java.util.ArrayList<>(size);
        Node<T> cur = front;
        while (cur != null) {
            list.add(cur.data);
            cur = cur.next;
        }
        return list;
    }
}
