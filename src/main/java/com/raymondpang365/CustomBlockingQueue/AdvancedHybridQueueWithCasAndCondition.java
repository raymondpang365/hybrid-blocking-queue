package com.raymondpang365.CustomBlockingQueue;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class AdvancedHybridQueueWithCasAndCondition<T> {
    private final AtomicReference<StackNode<T>> head;
    private final AtomicReference<StackNode<T>> tail;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();
    private final Condition notFull = lock.newCondition();
    private final int capacity;
    private final AtomicInteger size = new AtomicInteger(0);

    public AdvancedHybridQueueWithCasAndCondition(int capacity) {
        this.capacity = capacity;
        StackNode<T> dummy = new StackNode<>(null);
        head = new AtomicReference<>(dummy);
        tail = new AtomicReference<>(dummy);
    }

    public void enqueue(T item) throws InterruptedException {
        StackNode<T> newNode = new StackNode<>(item);

        // First, attempt CAS operation
        while (true) {
            StackNode<T> oldTail = tail.get();
            if (size.get() == capacity) {
                break; // Full, fall back to lock-based enqueue
            }
            if (tail.compareAndSet(oldTail, newNode)) {
                oldTail.setNext(newNode);
                incrementSize();
                return;
            }
        }

        // Fallback to lock-based enqueue if CAS fails due to full queue
        lock.lock();
        try {
            while (size.get() == capacity) {
                notFull.await();
            }
            StackNode<T> oldTail = tail.getAndSet(newNode);
            oldTail.setNext(newNode);
            incrementSizeWithLock();
        } finally {
            lock.unlock();
        }
    }

    public T dequeue() throws InterruptedException {
        // First, attempt CAS operation
        while (true) {
            StackNode<T> oldHead = head.get();
            StackNode<T> newHead = oldHead.getNext();
            if (newHead == null) {
                break; // Empty, fall back to lock-based dequeue
            }
            if (head.compareAndSet(oldHead, newHead)) {
                decrementSize();
                return newHead.getData();
            }
        }

        // Fallback to lock-based dequeue if CAS fails due to empty queue
        lock.lock();
        try {
            while (size.get() == 0) {
                notEmpty.await();
            }
            StackNode<T> oldHead = head.get();
            StackNode<T> newHead = oldHead.getNext();
            head.set(newHead);
            decrementSizeWithLock();
            return newHead.getData();
        } finally {
            lock.unlock();
        }
    }

    private void incrementSize() {
        int newSize = size.incrementAndGet();
        if (newSize == 1) {
            // Signal not empty when size transitions from 0 to 1
            lock.lock();
            try {
                notEmpty.signal();
            } finally {
                lock.unlock();
            }
        }
    }

    private void decrementSize() {
        int newSize = size.decrementAndGet();
        if (newSize == capacity - 1) {
            // Signal not full when size transitions from capacity to capacity - 1
            lock.lock();
            try {
                notFull.signal();
            } finally {
                lock.unlock();
            }
        }
    }

    private void incrementSizeWithLock() {
        size.incrementAndGet();
        notEmpty.signal();
    }

    private void decrementSizeWithLock() {
        size.decrementAndGet();
        notFull.signal();
    }

    public int getSize() {
        return size.get();
    }
}